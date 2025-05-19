package gg.sunken.sdk.locator;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a resource file and returns the classes it contains.
 *
 * @author santio
 */
@Slf4j
public class FileResourceLocator implements Iterable<Class<?>> {

    private final String resource;
    private final ClassLoader loader;
    private final List<Class<?>> iterableClasses = new ArrayList<>();
    private final List<Class<?>> streamableClasses = new ArrayList<>();
    private Iterator<Class<?>> iterableIterator;
    private Iterator<Class<?>> streamIterator;
    private boolean loadedAllClasses;
    private int reloadCount;
    protected FileResourceLocator(final String resource, final ClassLoader loader) {
        this.loader = loader == null ? ClassLoader.getSystemClassLoader() : loader;
        this.resource = resource;
    }

    public static FileResourceLocator load(final String resource, final ClassLoader loader) {
        return new FileResourceLocator(resource, loader);
    }

    public static FileResourceLocator load(final String resource) {
        return new FileResourceLocator(resource, Thread.currentThread().getContextClassLoader());
    }

    private static void fail(final String message, final Throwable cause) throws FileLocatorError {
        throw new FileLocatorError(message, cause);
    }

    private static void fail(final String message) throws FileLocatorError {
        throw new FileLocatorError(message);
    }

    private static void fail(final URL url, final int line, final String message) throws FileLocatorError {
        throw new FileLocatorError(url + ":" + line + ": " + message);
    }

    private Iterator<Class<?>> newLookupIterator() {
        return new LazyClassPathLookupIterator();
    }

    @NonNull
    @Override
    public Iterator<Class<?>> iterator() {
        if (iterableIterator == null) iterableIterator = newLookupIterator();

        return new Iterator<>() {

            final int expectedReloadCount = FileResourceLocator.this.reloadCount;

            int index;

            private void checkReloadCount() {
                if (expectedReloadCount != FileResourceLocator.this.reloadCount) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public boolean hasNext() {
                checkReloadCount();
                if (index < iterableClasses.size()) return true;
                return iterableIterator.hasNext();
            }

            @Override
            public Class<?> next() {
                checkReloadCount();
                Class<?> next;
                if (index < iterableClasses.size()) {
                    next = iterableClasses.get(index);
                } else {
                    next = iterableIterator.next();
                    iterableClasses.add(next);
                }
                index++;
                return next;
            }
        };
    }

    public Stream<Class<?>> stream() {
        if (loadedAllClasses) return streamableClasses.stream();
        if (streamIterator == null) streamIterator = newLookupIterator();
        Spliterator<Class<?>> spliterator = new LoaderSpliterator(streamIterator);
        return StreamSupport.stream(spliterator, false);
    }

    public void reload() {
        iterableIterator = null;
        iterableClasses.clear();

        streamIterator = null;
        streamableClasses.clear();

        loadedAllClasses = false;

        reloadCount++;
    }

    @Override
    public String toString() {
        return "gg.sunken.locator.FileResourceLocator";
    }

    public static final class FileLocatorError extends Error {
        public FileLocatorError(final String message) {
            super(message);
        }

        public FileLocatorError(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private final class LazyClassPathLookupIterator implements Iterator<Class<?>> {
        Set<String> providerNames = new HashSet<>();
        URL config;
        Iterator<String> pending;
        FileLocatorError nextError;
        Class<?> nextClass;

        LazyClassPathLookupIterator() {
        }

        private int parseLine(final URL url, final BufferedReader reader, final int lineCount, final Set<String> names) throws IOException {
            String line = reader.readLine();
            if (line == null) return -1;

            int ci = line.indexOf('#');
            if (ci >= 0) line = line.substring(0, ci);
            line = line.trim();

            int length = line.length();
            if (length != 0) {
                if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) {
                    fail(url, lineCount, "Provider entry must not contain spaces or tabs");
                }
                int cp = line.codePointAt(0);
                if (!Character.isJavaIdentifierStart(cp)) {
                    fail(url, lineCount, "Provider entry must be a valid Java identifier");
                }
                int start = Character.charCount(cp);
                for (int i = start; i < length; i += Character.charCount(cp)) {
                    cp = line.codePointAt(i);
                    if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                        fail(url, lineCount, "Provider entry must be a valid Java identifier");
                    }
                }
                if (this.providerNames.add(line)) names.add(line);
            }
            return lineCount + 1;
        }

        private Iterator<String> parse(final URL url) {
            final Set<String> names = new LinkedHashSet<>();
            try {
                URLConnection conn = url.openConnection();
                conn.setUseCaches(false);
                try (InputStream in = conn.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    int lineCount = 1;
                    //noinspection StatementWithEmptyBody
                    while ((lineCount = parseLine(url, reader, lineCount, names)) >= 0) ;
                }
            } catch (final IOException e) {
                fail("Error reading configuration file", e);
            }
            return names.iterator();
        }

        private Class<?> nextProviderClass() {
            if (config == null) {
                if (loader == null) {
                    config = ClassLoader.getSystemResource(resource);
                } else if (loader == ClassLoader.getPlatformClassLoader()) {
                    config = null;
                } else {
                    config = loader.getResource(resource);
                }
            }
            if (config == null) return null;
            if (pending == null) pending = parse(config);
            if (!pending.hasNext()) return null;
            String className = pending.next();

            try {
                return Class.forName(className, false, loader);
            } catch (final ClassNotFoundException e) {
                log.error("Provider {} not found", className, e);
                nextError = new FileLocatorError("Provider " + className + " not found");
                return null;
            }
        }

        private boolean hasNextClass() {
            while (nextClass == null && nextError == null) {
                try {
                    Class<?> clazz = nextProviderClass();
                    if (clazz == null) return false;
                    if (clazz.getModule().isNamed()) continue;
                    nextClass = clazz;
                } catch (final FileLocatorError e) {
                    nextError = e;
                }
            }
            return true;
        }

        private Class<?> nextClass() {
            if (!hasNextClass()) throw new NoSuchElementException();

            Class<?> result = nextClass;
            if (result != null) {
                nextClass = null;
                return result;
            } else {
                FileLocatorError e = nextError;
                nextError = null;
                throw e;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNextClass();
        }

        @Override
        public Class<?> next() {
            return nextClass();
        }
    }

    private class LoaderSpliterator implements Spliterator<Class<?>> {
        final int expectedReloadCount = FileResourceLocator.this.reloadCount;
        final Iterator<Class<?>> iterator;
        int index;

        LoaderSpliterator(final Iterator<Class<?>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Spliterator<Class<?>> trySplit() {
            return null;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Class<?>> action) {
            if (expectedReloadCount != FileResourceLocator.this.reloadCount) {
                throw new ConcurrentModificationException();
            }
            Class<?> next = null;
            if (index < streamableClasses.size()) {
                next = streamableClasses.get(index);
            } else if (iterator.hasNext()) {
                next = iterator.next();
                streamableClasses.add(next);
                index++;
            } else {
                loadedAllClasses = true;
            }
            if (next != null) {
                action.accept(next);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }
    }
}
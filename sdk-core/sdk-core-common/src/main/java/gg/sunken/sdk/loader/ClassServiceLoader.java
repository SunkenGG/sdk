package gg.sunken.sdk.loader;

/*
 * Copyright (c) 2005, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

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
 * Handles loading services from the classpath, this is a copy of the JDK's {@code java.util.ServiceLoader} class
 * however modified to instead return the Class of the service rather than the instance.
 *
 * @param <S> the service type
 * @author santio
 */
public final class ClassServiceLoader<S> implements Iterable<Class<? extends S>> {

    private final Class<S> serviceClass;
    private final ClassLoader loader;
    private final List<Class<? extends S>> iterableClasses = new ArrayList<>();
    private final List<Class<? extends S>> streamableClasses = new ArrayList<>();
    private Iterator<Class<? extends S>> iterableIterator;
    private Iterator<Class<? extends S>> streamIterator;
    private boolean loadedAllClasses;
    private int reloadCount;
    private ClassServiceLoader(Class<S> serviceClass, ClassLoader loader) {
        Objects.requireNonNull(serviceClass);

        if (loader == null) loader = ClassLoader.getSystemClassLoader();

        this.serviceClass = serviceClass;
        this.loader = loader;
    }

    /**
     * Creates a new service loader for the given service class and class loader.
     *
     * @param serviceClass the service class to load
     * @param loader       the class loader to use
     * @param <S>          the service type
     * @return a new service loader
     */
    public static <S> ClassServiceLoader<S> load(Class<S> serviceClass, ClassLoader loader) {
        return new ClassServiceLoader<>(serviceClass, loader);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * current thread's {@linkplain Thread#getContextClassLoader
     * context class loader}.
     *
     * <p> An invocation of this convenience method of the form
     * <pre>{@code
     *     ClassServiceLoader.load(service)
     * }</pre>
     * <p>
     * is equivalent to
     *
     * <pre>{@code
     *     ClassServiceLoader.load(service, Thread.currentThread().getContextClassLoader())
     * }</pre>
     *
     * @param <S>          the class of the service type
     * @param serviceClass The interface or abstract class representing the service
     * @return A new service loader
     * @apiNote Service loader objects obtained with this method should not be
     * cached VM-wide. For example, different applications in the same VM may
     * have different thread context class loaders. A lookup by one application
     * may locate a service provider that is only visible via its thread
     * context class loader and so is not suitable to be located by the other
     * application. Memory leaks can also arise. A thread local may be suited
     * to some applications.
     */
    public static <S> ClassServiceLoader<S> load(Class<S> serviceClass) {
        return new ClassServiceLoader<>(serviceClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a new service loader for the given service type, using the
     * {@linkplain ClassLoader#getPlatformClassLoader() platform class loader}.
     *
     * <p> This convenience method is equivalent to: </p>
     *
     * <pre>{@code
     *     ClassServiceLoader.load(service, ClassLoader.getPlatformClassLoader())
     * }</pre>
     *
     * <p> This method is intended for use when only installed providers are
     * desired.  The resulting service will only find and load providers that
     * have been installed into the current Java virtual machine; providers on
     * the application's module path or class path will be ignored.
     *
     * @param <S>          the class of the service type
     * @param serviceClass The interface or abstract class representing the service
     * @return A new service loader
     */
    public static <S> ClassServiceLoader<S> loadInstalled(Class<S> serviceClass) {
        return new ClassServiceLoader<>(serviceClass, ClassLoader.getPlatformClassLoader());
    }

    private static void fail(Class<?> service, String message, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + message, cause);
    }

    private static void fail(Class<?> service, String message) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + message);
    }

    private static void fail(Class<?> service, URL u, int line, String message) throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + message);
    }

    private Iterator<Class<? extends S>> newLookupIterator() {
        return new ClassServiceLoader.LazyClassPathLookupIterator();
    }

    /**
     * Returns an iterator to lazily load the classes defined as implementations of this loader's service.
     *
     * <p>To achieve laziness the actual work of locating and classloading implementations is done by the iterator itself. Its
     * {@link Iterator#hasNext() hasNext} and {@link Iterator#next() next} methods can throw a {@link ServiceConfigurationError}
     * due to any underlying ServiceLoading error. To write robust code it is only necessary to catch {@link ServiceConfigurationError}
     * when using the iterator. If an error is thrown then subsequent invocations of the iterator will make a best effort to locate
     * and load the next available class, but in general such recovery cannot be guaranteed.</p>
     *
     * <p>Caching: The iterator returned by this method first yields all of the elements of the iterable cache,
     * in the order that they were loaded. It then lazily loads any remaining service classes, adding each one
     * to the cache in turn. If this loader's iterable caches are cleared by invoking the {@link #reload() reload}
     * method then existing iterators for this loader should be discarded.
     * The {@code hasNext} and {@code next} methods of the iterator throw {@link ConcurrentModificationException
     * ConcurrentModificationException} if used after the iterable cache has been cleared.</p>
     *
     * <p>The iterator returned by this method does not support removal. Invoking its {@link Iterator#remove() remove}
     * method will cause an {@link UnsupportedOperationException} to be thrown.</p>
     *
     * @return an iterator that lazily loads providers for this loader's service
     * @apiNote Throwing an error (as opposed to an exception) in these cases may seem extreme. The rationale for
     * this behaviour is that a malformed provider-configuration file, like a malformed class file, indicates a serious
     * problem with the way the Java virtual machine is configured or is being used. As such it is preferable to throw
     * an error rather than try to recover or, even worse, fail silently.
     */
    @Override
    public Iterator<Class<? extends S>> iterator() {
        if (iterableIterator == null) {
            iterableIterator = newLookupIterator();
        }

        return new Iterator<>() {

            final int expectedReloadCount = ClassServiceLoader.this.reloadCount;

            int index;

            private void checkReloadCount() {
                if (ClassServiceLoader.this.reloadCount != expectedReloadCount) {
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
            public Class<? extends S> next() {
                checkReloadCount();
                Class<? extends S> next;
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

    /**
     * Returns a stream to lazily load the classes defined as implementations of this loader's service.
     *
     * <p>To achieve laziness the actual work of locating and classloading implementations is done when
     * processing the stream. If a service implementation cannot be loaded then a {@link ServiceConfigurationError}
     * will be thrown by whatever method caused the service class to be loaded.</p>
     *
     * <p>Caching: When processing the stream then implementations that were previously loaded by stream operations are
     * processed first, in load order. It then lazily loads any remaining service implementations. IOf this loader's stream
     * caches are cleared by invoking the {@link #reload() reload} method then existing streams for this service loader
     * should be discarded. The returned stream's source {@link Spliterator spliterator} is <em>fail-fast</em> and
     * will throw {@link ConcurrentModificationException} if the stream cache has been cleared.</p>
     *
     * @return a stream that lazily loads implementation classes for this loader's service
     */
    public Stream<Class<? extends S>> stream() {
        if (loadedAllClasses) return streamableClasses.stream();

        if (streamIterator == null) streamIterator = newLookupIterator();

        Spliterator<Class<? extends S>> spliterator = new ClassServiceLoader.LoaderSpliterator(streamIterator);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Load the first available implementation class of this loader's service. This convenience method is equivalent to
     * invoking the {@link #iterator() iterator} method and obtaining the first element. It therefore returns the first
     * element from the iterables cache if possible, it otherwise attempts to locate and classload the first implementation
     * class.
     *
     * @return the first implementation class of this loader's service, or {@link Optional#empty()} if no implementation
     * class is located.
     */
    public Optional<Class<? extends S>> findFirst() {
        Iterator<Class<? extends S>> iterator = iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Clear this loader's caches so that all providers will be reloaded.
     * <p>
     * After invoking this method, subsequent invocations of the {@link #iterator() iterator} or {@link #stream() stream}
     * methods will lazily locate implementation classes from scratch, just as is done by a newly-created loader.
     * <p>
     * This method is intended for use in situations in which new service loaders can be installed into a running JVM.
     */
    public void reload() {
        iterableIterator = null;
        iterableClasses.clear();

        streamIterator = null;
        streamableClasses.clear();

        loadedAllClasses = false;

        reloadCount++;
    }

    private final class LazyClassPathLookupIterator<T> implements Iterator<Class<? extends T>> {
        static final String PREFIX = "META-INF/services/";

        Set<String> providerNames = new HashSet<>();
        Enumeration<URL> configs;
        Iterator<String> pending;

        ServiceConfigurationError nextError;
        Class<? extends T> nextClass;

        LazyClassPathLookupIterator() {
        }

        private int parseLine(URL url, BufferedReader reader, int lineCount, Set<String> names) throws IOException {
            String line = reader.readLine();
            if (line == null) return -1;

            int ci = line.indexOf('#');
            if (ci >= 0) line = line.substring(0, ci);
            line = line.trim();

            int length = line.length();
            if (length != 0) {
                if ((line.indexOf(' ') >= 0 || line.indexOf('\t') >= 0)) {
                    fail(serviceClass, url, lineCount, "Illegal configuration-file syntax");
                }
                int cp = line.codePointAt(0);
                if (!Character.isJavaIdentifierStart(cp)) {
                    fail(serviceClass, url, lineCount, "Illegal provider-class name: " + line);
                }
                int start = Character.charCount(cp);
                for (int i = start; i < length; i += Character.charCount(cp)) {
                    cp = line.codePointAt(i);
                    if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                        fail(serviceClass, url, lineCount, "Illegal provider-class name: " + line);
                    }
                }
                if (providerNames.add(line)) names.add(line);
            }
            return lineCount + 1;
        }

        private Iterator<String> parse(URL url) {
            Set<String> names = new LinkedHashSet<>();
            try {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                try (InputStream in = connection.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    int lineCount = 1;
                    //noinspection StatementWithEmptyBody
                    while ((lineCount = parseLine(url, reader, lineCount, names)) >= 0) ;
                }
            } catch (IOException e) {
                fail(serviceClass, "Error reading configuration file", e);
            }
            return names.iterator();
        }

        private Class<?> nextProviderClass() {
            if (configs == null) {
                try {
                    String fullName = PREFIX + serviceClass.getName();
                    if (loader == null) {
                        configs = ClassLoader.getSystemResources(fullName);
                    } else if (loader == ClassLoader.getPlatformClassLoader()) {
                        configs = Collections.emptyEnumeration();
                    } else {
                        configs = loader.getResources(fullName);
                    }
                } catch (IOException e) {
                    fail(serviceClass, "Error locating configuration files", e);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) return null;
                pending = parse(configs.nextElement());
            }
            String className = pending.next();
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
                fail(serviceClass, "Provider " + className + " not found");
                return null;
            }
        }

        private boolean hasNextService() {
            while (nextClass == null && nextError == null) {
                try {
                    Class<?> clazz = nextProviderClass();
                    if (clazz == null) return false;
                    if (clazz.getModule().isNamed()) continue;
                    if (serviceClass.isAssignableFrom(clazz)) {
                        //noinspection unchecked
                        nextClass = (Class<? extends T>) clazz;
                    } else {
                        fail(serviceClass, "Provider " + clazz.getName() + " not a subtype");
                    }
                } catch (ServiceConfigurationError e) {
                    nextError = e;
                }
            }
            return true;
        }

        private Class<? extends T> nextService() {
            if (!hasNextService()) throw new NoSuchElementException();

            Class<? extends T> result = nextClass;
            if (result != null) {
                nextClass = null;
                return result;
            } else {
                ServiceConfigurationError e = nextError;
                nextError = null;
                throw e;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNextService();
        }

        @Override
        public Class<? extends T> next() {
            return nextService();
        }
    }

    private class LoaderSpliterator<T> implements Spliterator<Class<? extends T>> {
        final int expectedReloadCount = ClassServiceLoader.this.reloadCount;
        final Iterator<Class<? extends T>> iterator;
        int index;

        LoaderSpliterator(Iterator<Class<? extends T>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Spliterator<Class<? extends T>> trySplit() {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super Class<? extends T>> action) {
            if (ClassServiceLoader.this.reloadCount != expectedReloadCount) {
                throw new ConcurrentModificationException();
            }
            Class<? extends T> next = null;
            if (index < streamableClasses.size()) {
                next = (Class<? extends T>) streamableClasses.get(index);
            } else if (iterator.hasNext()) {
                next = iterator.next();
                streamableClasses.add((Class<? extends S>) next);
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
package gg.sunken.sdk.guice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class GuiceServiceLoader<T> implements Iterable<Class<? extends T>> {
    private final Set<Class<? extends T>> classes;

    private GuiceServiceLoader(Set<Class<? extends T>> classes) {
        this.classes = Collections.unmodifiableSet(classes);
    }

    @Override
    public Iterator<Class<? extends T>> iterator() {
        return classes.iterator();
    }

    public Set<Class<? extends T>> getClasses() {
        return classes;
    }

    public static <T> GuiceServiceLoader<T> load(Class<T> service, ClassLoader classLoader) {
        String serviceFile = "META-INF/services/" + service.getName();
        Set<Class<? extends T>> result = new LinkedHashSet<>();

        try {
            Enumeration<URL> resources = classLoader.getResources(serviceFile);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream in = url.openStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    for (String line : reader.lines().toList()) {
                        String trimmed = line.trim();
                        if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                            try {
                                Class<?> cls = Class.forName(trimmed, false, classLoader);
                                if (service.isAssignableFrom(cls)) {
                                    result.add(cls.asSubclass(service));
                                }
                            } catch (ClassNotFoundException e) {
                                System.err.println("Failed to load class " + trimmed + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read service file: " + e.getMessage());
        }

        return new GuiceServiceLoader<>(result);
    }

    public static <T> GuiceServiceLoader<T> load(Class<T> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }
}

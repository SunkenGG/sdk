package gg.sunken.sdk.utils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    public static List<Class<?>> getAllClasses(Class<?> mainClass) {
        List<Class<?>> classes = new ArrayList<>();
        String basePackage = mainClass.getPackage().getName();
        String basePath = basePackage.replace('.', '/');

        try {
            URL jarUrl = mainClass.getProtectionDomain().getCodeSource().getLocation();
            JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
            JarFile jarFile = connection.getJarFile();

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(basePath) && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }
}

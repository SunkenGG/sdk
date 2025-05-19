package gg.sunken.sdk.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import gg.sunken.sdk.guice.annotations.GuiceOverride;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Provides utility methods for discovering and preparing Guice modules using the Java SPI mechanism.
 */
@UtilityClass
public class GuiceSPI {

    /**
     * Loads all modules from the classpath using the default Module type.
     *
     * @param classLoader the class loader to scan with
     * @return a combined Guice module
     */
    public Module discoverModules(ClassLoader classLoader) {
        return discoverModules(Module.class, classLoader);
    }

    /**
     * Loads modules of a specific class from the classpath and separates overrides.
     *
     * @param moduleType  the module type to look for
     * @param classLoader the class loader to use
     * @return a merged module that applies any overrides if present
     */
    public Module discoverModules(Class<? extends Module> moduleType, ClassLoader classLoader) {
        List<Module> baseModules = new ArrayList<>();
        List<Module> overrideModules = new ArrayList<>();

        for (Module module : ServiceLoader.load(moduleType, classLoader)) {
            if (module.getClass().isAnnotationPresent(GuiceOverride.class)) {
                overrideModules.add(module);
            } else {
                baseModules.add(module);
            }
        }

        return overrideModules.isEmpty()
                ? Modules.combine(baseModules)
                : Modules.override(baseModules).with(overrideModules);
    }

    /**
     * Uses an Injector to instantiate module classes found on the classpath.
     *
     * @param injector    the Guice injector to use for instantiation
     * @param classLoader the class loader to scan
     * @return a combined module
     */
    public Module discoverInjectedModules(Injector injector, ClassLoader classLoader) {
        return discoverInjectedModules(Module.class, injector, classLoader);
    }

    /**
     * Uses an Injector to instantiate module classes of a specific type.
     *
     * @param moduleType  the module type to discover
     * @param injector    the injector for creating instances
     * @param classLoader the class loader to scan
     * @return a merged module
     */
    public Module discoverInjectedModules(Class<? extends Module> moduleType, Injector injector, ClassLoader classLoader) {
        List<Module> baseModules = new ArrayList<>();
        List<Module> overrideModules = new ArrayList<>();

        for (Class<? extends Module> moduleClass : GuiceServiceLoader.load(moduleType, classLoader)) {
            Module instance = injector.getInstance(moduleClass);
            if (moduleClass.isAnnotationPresent(GuiceOverride.class)) {
                overrideModules.add(instance);
            } else {
                baseModules.add(instance);
            }
        }

        return overrideModules.isEmpty()
                ? Modules.combine(baseModules)
                : Modules.override(baseModules).with(overrideModules);
    }
}

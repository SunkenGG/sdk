package gg.sunken.sdk.guice;

import gg.sunken.sdk.guice.annotations.GuiceFactory;
import gg.sunken.sdk.guice.processor.GuiceFactoryLocator;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Uses a service loader to load all {@link GuiceFactory} annotated interfaces and registers them as a module
 *
 * @author santio
 */
@Slf4j
public class DynamicFactoryModule extends AbstractModule {

    private final ClassLoader classLoader;

    /**
     * Creates a new dynamic factory module with the given class loader
     *
     * @param classLoader the class loader to use
     */
    public DynamicFactoryModule(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Creates a new dynamic factory module with the current thread's context class loader
     */
    public DynamicFactoryModule() {
        this(Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected void configure() {
        final List<Class<? extends GuiceFactory>> factories = this.loadFactories();

        factories.forEach(factory -> {
            this.install(new FactoryModuleBuilder()
                    .implement(GuiceFactory.class, factory)
                    .build(factory));
        });
    }

    private List<Class<? extends GuiceFactory>> loadFactories() {
        final GuiceFactoryLocator locator = GuiceFactoryLocator.load(this.classLoader);
        final Class<? extends GuiceFactory>[] factories = locator.stream()
                .toArray(Class[]::new);

        return Arrays.stream(factories)
                .filter(provider -> {
                    if (!provider.isInterface()) {
                        log.warn("A non-interface was marked with @AutoService(GuiceFactory.class), this is not valid. {}", provider.getName());
                        return false;
                    }

                    return true;
                })
                .toList();
    }

}
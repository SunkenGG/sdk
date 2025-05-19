package gg.sunken.sdk.guice.processor;

import gg.sunken.sdk.guice.annotations.GuiceFactory;
import gg.sunken.sdk.locator.FileResourceLocator;

/**
 * Automatic discovery of {@link GuiceFactory} interfaces.
 *
 * @author santio
 */
public final class GuiceFactoryLocator extends FileResourceLocator {
    private GuiceFactoryLocator(final ClassLoader loader) {
        super(GuiceFactoryAnnotationProcessor.GUICE_FACTORY_CLASSES(), loader);
    }

    public static GuiceFactoryLocator load(final ClassLoader loader) {
        return new GuiceFactoryLocator(loader);
    }

    public static GuiceFactoryLocator load() {
        return new GuiceFactoryLocator(Thread.currentThread().getContextClassLoader());
    }
}
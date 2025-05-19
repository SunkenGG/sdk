package gg.sunken.sdk.config;

import com.google.inject.MembersInjector;
import java.lang.reflect.Field;

public class ConfigValueInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final ConfigProvider provider;
    private final String path;
    private final String file;

    public ConfigValueInjector(Field field, ConfigProvider provider) {
        this.field = field;
        this.provider = provider;
        ConfigValue annotation = field.getAnnotation(ConfigValue.class);
        this.path = annotation.value();
        this.file = annotation.file();
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T instance) {
        try {
            Object value = provider.getValue(instance.getClass(), file, path);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject config value for " + field.getName(), e);
        }
    }
}

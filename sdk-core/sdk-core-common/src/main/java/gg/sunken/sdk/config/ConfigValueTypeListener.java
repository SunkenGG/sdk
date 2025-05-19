package gg.sunken.sdk.config;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;

public class ConfigValueTypeListener implements TypeListener {
    private final ConfigProvider provider;

    public ConfigValueTypeListener(ConfigProvider provider) {
        this.provider = provider;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        for (Field field : type.getRawType().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                encounter.register(new ConfigValueInjector<>(field, provider));
            }
        }
    }
}

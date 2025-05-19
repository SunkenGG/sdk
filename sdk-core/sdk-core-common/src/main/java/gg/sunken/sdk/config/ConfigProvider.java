package gg.sunken.sdk.config;

import gg.sunken.sdk.utils.ReflectionUtil;
import gg.sunken.sdk.utils.StringUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigProvider {
    private final ConcurrentHashMap<String, YamlDocument> configs = new ConcurrentHashMap<>();

    public ConfigProvider(File dataFolder) {
        loadConfig("config.yml", new File(dataFolder, "config.yml"));
    }

    public void loadConfig(String name, File file) {
        try {
            configs.put(name, YamlDocument.create(file));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file: " + file.getName(), e);
        }
    }

    public <T> T getValue(Class<T> clazz, String file, String path) {
        YamlDocument document = configs.get(file);
        if (document == null) {
            throw new IllegalStateException("Config file not loaded: " + file);
        }

        if (!document.isSection(path)) {
            Object o = document.get(path);
            if (o == null) {
                throw new IllegalStateException("Config path not found: " + path);
            }

            if (clazz.isInstance(o)) {
                return clazz.cast(o);
            } else {
                throw new IllegalStateException("Config value is not of type " + clazz.getName() + ": " + path);
            }
        }

        Section section = document.getSection(path);
        if (section == null) {
            throw new IllegalStateException("Config section not found: " + path);
        }

        return mapSectionToObject(clazz, section);
    }

    private <T> T mapSectionToObject(Class<T> clazz, Section section) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            Map<String, VarHandle> varHandles = ReflectionUtil.getAllVarHandles(clazz);

            for (Map.Entry<String, VarHandle> entry : varHandles.entrySet()) {
                String fieldName = entry.getKey();
                VarHandle varHandle = entry.getValue();
                String route = StringUtils.camelCaseToDashCase(fieldName);
                boolean isSection = section.isSection(route);
                Object value = section.get(route);

                if (value == null) {
                    continue;
                }

                if (isSimpleType(varHandle.varType())) {
                    varHandle.set(instance, value);
                } else if (isSection) {
                    varHandle.set(instance, mapSectionToObject((Class<?>) varHandle.varType(), section.getSection(route)));
                } else if (List.class.isAssignableFrom(varHandle.varType()) && value instanceof List) {
                    varHandle.set(instance, value);
                } else {
                    throw new IllegalStateException("Cannot map field: " + fieldName + " with value: " + value);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map section to object: " + clazz.getName(), e);
        }
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                Number.class.isAssignableFrom(type) ||
                type == Boolean.class ||
                type == Character.class;
    }
}

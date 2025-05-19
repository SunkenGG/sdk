package gg.sunken.sdk.lang;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class I18n {
    private static final MiniMessage MINI = MiniMessage.miniMessage(); //TODO custom mini

    // Registry: locale → key → MiniMessage string
    private static final Map<Locale, Map<String, List<String>>> JAVA_MESSAGES = new ConcurrentHashMap<>();
    private static final Map<Locale, Map<String, List<String>>> BEDROCK_MESSAGES = new ConcurrentHashMap<>();

    // Cache parsed templates: locale + key → MiniMessage template
    private static final Map<String, List<Component>> JAVA_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<Component>> BEDROCK_CACHE = new ConcurrentHashMap<>();

    @Setter
    @Getter
    private static Locale defaultLocale = Locale.ENGLISH;

    public static void register(String baseName, ClassLoader classLoader) {
        loadToMap(JAVA_MESSAGES, baseName, classLoader);
        loadToMap(BEDROCK_MESSAGES, baseName + "_bedrock", classLoader);
    }

    private static void loadToMap(Map<Locale, Map<String, List<String>>> target, String baseName, ClassLoader loader) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, defaultLocale, loader, new Utf8Control());
            Map<String, List<String>> map = new HashMap<>();
            for (String key : bundle.keySet()) {

                String value = bundle.getString(key);

                List<String> list = map.computeIfAbsent(key, k -> new ArrayList<>());
                String[] split = value.split("\n");

                for (String s : split) {
                    if (s.isEmpty()) continue;
                    list.add(s);
                }

                if (list.isEmpty()) continue;
                map.put(key, list);
            }
            target.put(defaultLocale, map);
        } catch (MissingResourceException ignored) {}
    }

    public static Map<String, List<String>> getAllKeys(Locale locale, boolean isBedrock) {
        Map<Locale, Map<String, List<String>>> registry = isBedrock ? BEDROCK_MESSAGES : JAVA_MESSAGES;
        return registry.getOrDefault(locale, registry.get(defaultLocale));
    }

    public static List<Component> mm(Locale locale, boolean isBedrock, String key, Object... args) {
        String cacheKey = locale.toString() + ":" + key;
        Map<Locale, Map<String, List<String>>> registry = isBedrock ? BEDROCK_MESSAGES : JAVA_MESSAGES;
        Map<String, List<Component>> cache = isBedrock ? BEDROCK_CACHE : JAVA_CACHE;

        Map<String, List<String>> messages = registry.getOrDefault(locale, registry.get(defaultLocale));
        List<String> raw = messages != null ? messages.get(key) : null;

        if (raw == null) return List.of(MINI.deserialize("<red>!" + key + "!</red>"));
        cache.computeIfAbsent(cacheKey, k -> {
            String[] split = k.split("\n");
            List<Component> components = new ArrayList<>();

            for (String s : split) {
                if (s.isEmpty()) continue;
                components.add(MINI.deserialize(s));
            }
            return components;

        });

        List<Component> components = cache.get(cacheKey);
        if (args.length == 0) return components;

        List<Component> parsed = new ArrayList<>();

        for (Component component : components) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) break;

                String name = args[i].toString();
                Object value = args[i + 1];

                if (value instanceof Component) component = component.replaceText(t -> t.matchLiteral(name).replacement((Component) value));
                else component = component.replaceText(t -> t.matchLiteral(name).replacement(value.toString()));

                parsed.add(component);
            }
        }

        return parsed;
    }

    public static List<String> raw(Locale locale, boolean isBedrock, String key) {
        Map<Locale, Map<String, List<String>>> registry = isBedrock ? BEDROCK_MESSAGES : JAVA_MESSAGES;
        Map<String, List<String>> messages = registry.getOrDefault(locale, registry.get(defaultLocale));
        return messages != null ? messages.getOrDefault(key, List.of("!" + key + "!")) : List.of("!" + key + "!");
    }

    private static class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload) throws java.io.IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (var stream = loader.getResourceAsStream(resourceName)) {
                if (stream != null) {
                    return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                }
            }
            return null;
        }
    }
}

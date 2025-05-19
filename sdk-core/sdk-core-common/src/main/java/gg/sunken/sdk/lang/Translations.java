package gg.sunken.sdk.lang;

import gg.sunken.sdk.exceptions.NotImplementedException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Translations {
    private static final Map<String, Message> MESSAGES = new ConcurrentHashMap<>();

    public static void register(Message message) {
        MESSAGES.put(message.getKey(), message);
    }

    public static void registerAll(Message... messages) {
        for (Message message : messages) {
            register(message);
        }
    }

    public static void send(String key, Audience audience, Object... args) {
        Message message = MESSAGES.get(key);
        if (message == null) {
            throw new RuntimeException("Unknown translation key: " + key);
        }

        boolean bedrock = false;
        Optional<UUID> uuid = audience.get(Identity.UUID);
        if (uuid.isPresent() && uuid.get().getMostSignificantBits() == 0) {
            bedrock = true;
        }

        Optional<Locale> locale = audience.get(Identity.LOCALE);
        Locale loc = Locale.ENGLISH;
        if (locale.isPresent()) {
            loc = locale.get();
        }

        List<Component> components = I18n.mm(loc, bedrock, key, args);
        switch (message.getType()) {

        }

//        Component component = I18n.mm(loc, bedrock, key, args);
//        audience.sendMessage(component);
    }


}

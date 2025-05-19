package gg.sunken.sdk.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TraceUtils {
    private static Logger logger;
    private static Consumer<Component> globalNotifier = msg -> {};

    private TraceUtils() {}

    public static void init(Logger platformLogger, Consumer<Component> notifier) {
        logger = platformLogger;
        globalNotifier = notifier != null ? notifier : msg -> {};
    }

    public static void error(Audience affected, String message, Throwable throwable) {
        Component userMessage = Component.text("An internal error occurred. Please check the console.").color(NamedTextColor.RED);
        Component adminMessage = Component.text("[!] " + message).color(NamedTextColor.RED);

        if (logger != null) {
            logger.severe("[!] " + message);
            logger.log(Level.SEVERE, "Exception trace:", throwable);
        }

        affected.sendMessage(userMessage);
        globalNotifier.accept(adminMessage);
    }

    public static void warn(Audience affected, String message) {
        Component userMessage = Component.text(message).color(NamedTextColor.YELLOW);
        Component adminMessage = Component.text("[!] " + message).color(NamedTextColor.YELLOW);

        if (logger != null) {
            logger.warning("[!] " + message);
        }

        affected.sendMessage(userMessage);
        globalNotifier.accept(adminMessage);
    }

    public static void info(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }
}

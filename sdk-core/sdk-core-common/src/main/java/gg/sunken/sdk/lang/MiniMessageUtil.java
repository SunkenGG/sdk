package gg.sunken.sdk.lang;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;

public class MiniMessageUtil {

    private ColorScheme defaultColorScheme;
    private final Map<ColorScheme, MiniMessage> miniMessageMap = new HashMap<>();

    public MiniMessageUtil(ColorScheme defaultColorScheme) {
        this.defaultColorScheme = defaultColorScheme;
        registerMiniMessage(defaultColorScheme);
    }

    public void registerMiniMessage(ColorScheme colorScheme) {
        if (miniMessageMap.containsKey(colorScheme)) {
            return;
        }
        
        miniMessageMap.put(colorScheme, MiniMessage.builder()
                .editTags(builder -> {
                    //todo: commented to allow compiling
//                    builder.
                }).build());
    }


}

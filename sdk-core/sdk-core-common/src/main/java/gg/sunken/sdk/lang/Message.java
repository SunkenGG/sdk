package gg.sunken.sdk.lang;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;

@Builder
@Getter
public class Message {
    private final String key;
    private final Type type;
    private final Sound[] sounds;

    public Message(String key, Type type, Sound... sounds) {
        this.key = key;
        this.type = type;
        this.sounds = sounds;
    }

    public Message(String key, Type type) {
        this.key = key;
        this.type = type;
        this.sounds = new Sound[0];
    }

    public Message(String key) {
        this.key = key;
        this.type = Type.CHAT;
        this.sounds = new Sound[0];
    }

    public enum Type {
        CHAT,
        ACTION_BAR,
        TITLE
    }
}

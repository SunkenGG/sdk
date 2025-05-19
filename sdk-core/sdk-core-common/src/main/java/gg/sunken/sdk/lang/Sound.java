package gg.sunken.sdk.lang;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Sound {
    private final String sound;
    private final float volume;
    private final float pitch;
    private final float offset;
    private final int tickOffset;

    public Sound(String sound, float volume, float pitch, float offset, int tickOffset) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.offset = offset;
        this.tickOffset = tickOffset;
    }

    public static Sound of(String sound, float volume, float pitch, float offset, int tickOffset) {
        return new Sound(sound, volume, pitch, offset, tickOffset);
    }

    public static Sound of(String sound, float volume, float pitch) {
        return new Sound(sound, volume, pitch, 0.5f, 0);
    }

    public static Sound of(String sound) {
        return new Sound(sound, 1.0f, 1.0f, 0.5f, 0);
    }
}
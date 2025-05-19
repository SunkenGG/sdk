package gg.sunken.sdk.lang;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Title {
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public static Title of(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return new Title(title, subtitle, fadeIn, stay, fadeOut);
    }

    public static Title of(String title, String subtitle) {
        return new Title(title, subtitle, 10, 70, 20);
    }

    public static Title of(String title) {
        return new Title(title, "", 10, 70, 20);
    }
}

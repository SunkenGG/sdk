package gg.sunken.sdk.lang;

import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true, fluent = true, makeFinal = true)
public class ColorScheme {
    protected Color primary;
    protected Color secondary;
    protected Color tertiary;

    protected Color quaternary;

    protected Color white;
    protected Color black;
    protected Color gray;
    protected Color darkGray;
    protected Color lightGray;
    protected Color red;
    protected Color green;
    protected Color blue;
    protected Color yellow;
    protected Color cyan;
    protected Color magenta;
    protected Color orange;
    protected Color purple;
    protected Color pink;
    protected Color brown;
    protected Color gold;
    private final int hashCode;

    public ColorScheme(Color primary, Color secondary, Color tertiary, Color quaternary,
                       Color white, Color black, Color gray, Color darkGray, Color lightGray,
                       Color red, Color green, Color blue, Color yellow, Color cyan,
                       Color magenta, Color orange, Color purple, Color pink, Color brown,
                       Color gold) {
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
        this.quaternary = quaternary;

        this.white = white;
        this.black = black;
        this.gray = gray;
        this.darkGray = darkGray;
        this.lightGray = lightGray;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.yellow = yellow;
        this.cyan = cyan;
        this.magenta = magenta;
        this.orange = orange;
        this.purple = purple;
        this.pink = pink;
        this.brown = brown;
        this.gold = gold;

        this.hashCode = Objects.hash(primary, secondary, tertiary, quaternary, white, black, gray, darkGray, lightGray, red, green, blue, yellow, cyan, magenta, orange, purple, pink, brown, gold);
    }

    public Color shade(Color color, float factor) {
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        return new Color(r, g, b, color.getAlpha());
    }

    public Color tint(Color color, float factor) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * factor);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * factor);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * factor);
        return new Color(r, g, b, color.getAlpha());
    }

    public Color mix(Color color1, Color color2, float ratio) {
        int r = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
        int g = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
        int b = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
        return new Color(r, g, b, color1.getAlpha());
    }

    public Color invert(Color color) {
        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
    }

    public Color grayscale(Color color) {
        int gray = (int) (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11);
        return new Color(gray, gray, gray, color.getAlpha());
    }

    public Color transparent(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * alpha));
    }

    public Color transparent(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public Color transparent(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
    }

    public Color opaque(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }

    public Color brighter(Color color) {
        return new Color(Math.min(color.getRed() + 30, 255), Math.min(color.getGreen() + 30, 255), Math.min(color.getBlue() + 30, 255), color.getAlpha());
    }

    public Color brighter(Color color, float factor) {
        int r = (int) (color.getRed() * (1 + factor));
        int g = (int) (color.getGreen() * (1 + factor));
        int b = (int) (color.getBlue() * (1 + factor));
        return new Color(r, g, b, color.getAlpha());
    }

    public Color darker(Color color) {
        return new Color(Math.max(color.getRed() - 30, 0), Math.max(color.getGreen() - 30, 0), Math.max(color.getBlue() - 30, 0), color.getAlpha());
    }

    public Color darker(Color color, float factor) {
        int r = (int) (color.getRed() * (1 - factor));
        int g = (int) (color.getGreen() * (1 - factor));
        int b = (int) (color.getBlue() * (1 - factor));
        return new Color(r, g, b, color.getAlpha());
    }

    public Color lighter(Color color) {
        return new Color(Math.min(color.getRed() + 30, 255), Math.min(color.getGreen() + 30, 255), Math.min(color.getBlue() + 30, 255), color.getAlpha());
    }

    public Color lighter(Color color, float factor) {
        int r = (int) (color.getRed() * (1 + factor));
        int g = (int) (color.getGreen() * (1 + factor));
        int b = (int) (color.getBlue() * (1 + factor));
        return new Color(r, g, b, color.getAlpha());
    }

    public TagResolver[] tagResolvers() {
        List<TagResolver> resolvers = new ArrayList<>();

        makeShades(resolvers, primary, "primary");
        makeShades(resolvers, secondary, "secondary");
        makeShades(resolvers, tertiary, "tertiary");
        makeShades(resolvers, quaternary, "quaternary");

        makeShades(resolvers, white, "white");
        makeShades(resolvers, black, "black");
        makeShades(resolvers, gray, "gray");
        makeShades(resolvers, darkGray, "dark_gray");
        makeShades(resolvers, lightGray, "light_gray");
        makeShades(resolvers, red, "red");
        makeShades(resolvers, green, "green");
        makeShades(resolvers, blue, "blue");
        makeShades(resolvers, yellow, "yellow");
        makeShades(resolvers, cyan, "cyan");
        makeShades(resolvers, magenta, "magenta");
        makeShades(resolvers, orange, "orange");
        makeShades(resolvers, purple, "purple");
        makeShades(resolvers, pink, "pink");
        makeShades(resolvers, brown, "brown");
        makeShades(resolvers, gold, "gold");

        return resolvers.toArray(new TagResolver[0]);
    }

    /**
     * @param tagResolvers tag resolver list to add to
     * @param baseColor base color to create shades from
     * @param name name of the color
     */
    private void makeShades(List<TagResolver> tagResolvers, Color baseColor, String name) {
        tagResolvers.add(Placeholder.styling(name, TextColor.fromHexString(String.format("#%02x%02x%02x", baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue()))));

        tagResolvers.add(Placeholder.styling(name + ":invert", TextColor.fromHexString(String.format("#%02x%02x%02x", 255 - baseColor.getRed(), 255 - baseColor.getGreen(), 255 - baseColor.getBlue()))));
        tagResolvers.add(Placeholder.styling(name + ":grayscale", TextColor.fromHexString(String.format("#%02x%02x%02x", (int) (baseColor.getRed() * 0.3 + baseColor.getGreen() * 0.59 + baseColor.getBlue() * 0.11), (int) (baseColor.getRed() * 0.3 + baseColor.getGreen() * 0.59 + baseColor.getBlue() * 0.11), (int) (baseColor.getRed() * 0.3 + baseColor.getGreen() * 0.59 + baseColor.getBlue() * 0.11)))));
        tagResolvers.add(Placeholder.styling(name + ":lighter", TextColor.fromHexString(String.format("#%02x%02x%02x", Math.min(baseColor.getRed() + 30, 255), Math.min(baseColor.getGreen() + 30, 255), Math.min(baseColor.getBlue() + 30, 255)))));
        tagResolvers.add(Placeholder.styling(name + ":darker", TextColor.fromHexString(String.format("#%02x%02x%02x", Math.max(baseColor.getRed() - 30, 0), Math.max(baseColor.getGreen() - 30, 0), Math.max(baseColor.getBlue() - 30, 0)))));

        for (int i = 100; i <= 900; i += 100) {
            Color color = i == 500 ? baseColor : i > 500 ? shade(baseColor, i / 1000f) : tint(baseColor, i / 1000f);
            tagResolvers.add(Placeholder.styling(name + ":" + i, TextColor.fromHexString(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))));

            color = i == 500 ? baseColor : i > 500 ? shade(baseColor, i / 1000f) : tint(baseColor, i / 1000f);
            tagResolvers.add(Placeholder.styling(name + ":" + i + ":invert", TextColor.fromHexString(String.format("#%02x%02x%02x", 255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()))));
            tagResolvers.add(Placeholder.styling(name + ":" + i + ":grayscale", TextColor.fromHexString(String.format("#%02x%02x%02x", (int) (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11), (int) (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11), (int) (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11)))));

            tagResolvers.add(Placeholder.styling(name + ":" + i + ":lighter", TextColor.fromHexString(String.format("#%02x%02x%02x", Math.min(color.getRed() + 30, 255), Math.min(color.getGreen() + 30, 255), Math.min(color.getBlue() + 30, 255)))));
            tagResolvers.add(Placeholder.styling(name + ":" + i + ":darker", TextColor.fromHexString(String.format("#%02x%02x%02x", Math.max(color.getRed() - 30, 0), Math.max(color.getGreen() - 30, 0), Math.max(color.getBlue() - 30, 0)))));

            for(int j = 100; j <= 900; j += 100) {
                color = i == 500 ? baseColor : i > 500 ? shade(baseColor, i / 1000f) : tint(baseColor, i / 1000f);
                color = j == 500 ? color : j > 500 ? darker(color, j / 1000f) : lighter(color, j / 1000f);
                tagResolvers.add(Placeholder.styling(name + ":" + i + ":" + j, TextColor.fromHexString(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))));
            }
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}

package gg.sunken.sdk.utils;


import net.kyori.adventure.audience.Audience;

public class AdventureUtils {

    private final static char COLOR_CHAR = '§';


    /**
     * @param message      The message to center
     * @param centerOffset The offset to center the message
     * @return The spaces needed for the message
     */
    public static String spacesToCenter(String message, int centerOffset) {
        if (message == null || message.isEmpty()) return "";

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        boolean isHex = false;
        int hexCount = 0;

        for (char c : message.toCharArray()) {
            if (c == COLOR_CHAR) {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L'; // &l
                isHex = c == 'x' || c == 'X'; // &x&r&r&g&g&b&b
                hexCount = isHex ? 12 : 0; // 14 char for hex codes but we are skipping the two one
            } else if (previousCode && isBold) {
                isBold = false;
            } else if (previousCode && isHex) {
                hexCount--;
                if (hexCount == 0) {
                    isHex = false;
                }
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int CENTER_PX = 154 + centerOffset;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString();
    }

}

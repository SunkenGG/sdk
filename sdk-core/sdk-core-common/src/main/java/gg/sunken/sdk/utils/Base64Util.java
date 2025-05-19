package gg.sunken.sdk.utils;

import java.util.Base64;

public class Base64Util {
    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}

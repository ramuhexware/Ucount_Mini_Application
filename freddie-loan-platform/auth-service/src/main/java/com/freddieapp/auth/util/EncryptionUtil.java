package com.freddieapp.auth.util;

import java.util.Base64;

public class EncryptionUtil {

    public static String encode(String raw) {
        if (raw == null) return null;
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    public static String decode(String encoded) {
        if (encoded == null) return null;
        return new String(Base64.getDecoder().decode(encoded));
    }
}

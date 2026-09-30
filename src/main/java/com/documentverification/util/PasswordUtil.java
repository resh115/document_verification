package com.documentverification.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean matches(String password, String hash) {
        return password != null
                && hash != null
                && BCrypt.checkpw(password, hash);
    }
}

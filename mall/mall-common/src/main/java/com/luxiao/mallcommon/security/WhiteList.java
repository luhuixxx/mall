package com.luxiao.mallcommon.security;

public final class WhiteList {

    public static final String[] PATHS = new String[]{
            "/api/security/public-key",
            "/api/user/login",
            "/api/user/register",
            "/api/employee/login"
    };

    public static boolean matchesPrefix(String path) {
        if (path == null) return false;
        for (String p : PATHS) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }
}


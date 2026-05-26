package com.sanly.registry.util;

public final class UserAgentParser {

    private UserAgentParser() {}

    /** Parses a User-Agent string into a human-readable label like "Chrome on Windows". */
    public static String parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown Device";
        return parseBrowser(userAgent) + " on " + parseOs(userAgent);
    }

    private static String parseBrowser(String ua) {
        if (ua.contains("Edg/") || ua.contains("Edge/")) return "Edge";
        if (ua.contains("OPR/") || ua.contains("Opera/"))  return "Opera";
        if (ua.contains("Chrome/"))                         return "Chrome";
        if (ua.contains("Firefox/"))                        return "Firefox";
        if (ua.contains("Safari/"))                         return "Safari";
        if (ua.contains("curl/"))                           return "cURL";
        return "Browser";
    }

    private static String parseOs(String ua) {
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Android"))                        return "Android";
        if (ua.contains("Windows"))                        return "Windows";
        if (ua.contains("Mac OS X"))                       return "macOS";
        if (ua.contains("Linux"))                          return "Linux";
        return "Unknown OS";
    }

    /** Maps a request URI to a readable action category. */
    public static String categorizeUri(String uri) {
        if (uri == null) return "Portal";
        if (uri.contains("/auth/"))           return "Authentication";
        if (uri.contains("/citizens"))        return "Identity";
        if (uri.contains("/sessions"))        return "Session Management";
        if (uri.contains("/data-requests"))   return "Data Requests";
        if (uri.contains("/signature"))       return "Signatures";
        return "Portal";
    }
}

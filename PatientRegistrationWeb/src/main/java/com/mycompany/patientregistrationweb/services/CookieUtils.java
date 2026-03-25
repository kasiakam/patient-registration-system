package com.mycompany.patientregistrationweb.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cookie helper for reading and writing cookies.
 * Values are sanitized to avoid spaces or commas which may be rejected by browsers.
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
public class CookieUtils {

    /** Private constructor to prevent instantiation. */
    private CookieUtils() {}

    /**
     * Reads cookie value by name.
     *
     * @param request HTTP request
     * @param name cookie name
     * @return cookie value or {@code null} if missing
     */
    public static String read(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * Writes a cookie with a sanitized value.
     *
     * @param response HTTP response
     * @param name cookie name
     * @param value cookie value (will be sanitized)
     * @param maxAgeSeconds cookie lifetime in seconds
     */
    public static void write(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        String safeValue = sanitize(value);
        Cookie cookie = new Cookie(name, safeValue);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Sanitizes cookie values by removing spaces and commas.
     *
     * @param raw raw cookie value
     * @return sanitized cookie value
     */
    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace(" ", "_").replace(",", "_");
    }
}
package com.bookstore.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public final class AppUtils {
    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final Random RANDOM = new Random();

    private AppUtils() {
    }

    public static synchronized long nextId() {
        long seq = COUNTER.incrementAndGet() & 0xFFFF;
        return (System.currentTimeMillis() << 16) | seq;
    }

    public static String nextOrderNo() {
        return "ORD" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + (1000 + RANDOM.nextInt(9000));
    }

    public static String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(b & 255);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static boolean passwordMatch(String raw, String encoded) {
        return sha256(raw).equals(encoded);
    }

    public static String str(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }

    public static Long lng(Map<String, Object> map, String key) {
        String v = str(map, key);
        return v == null || v.isEmpty() ? null : Long.valueOf(v);
    }

    public static Integer integer(Map<String, Object> map, String key) {
        String v = str(map, key);
        return v == null || v.isEmpty() ? null : Integer.valueOf(v);
    }

    public static BigDecimal decimal(Map<String, Object> map, String key) {
        String v = str(map, key);
        return v == null || v.isEmpty() ? null : new BigDecimal(v);
    }
}

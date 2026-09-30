package com.documentverification.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Server-side adapter for Supabase Storage.
 * The secret/service key is read only from the server environment and is never sent to the browser.
 */
public final class SupabaseStorage {
    private static final String BUCKET = envOr("SUPABASE_STORAGE_BUCKET", "documents");
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 60000;

    private SupabaseStorage() { }

    public static String upload(InputStream input, String objectPath, long size, String contentType) throws IOException {
        byte[] data = readAll(input, size);
        HttpURLConnection c = open("/storage/v1/object/" + BUCKET + "/" + encodePath(objectPath), "POST");
        c.setRequestProperty("Content-Type", contentType == null ? "application/octet-stream" : contentType);
        c.setRequestProperty("x-upsert", "false");
        c.setFixedLengthStreamingMode(data.length);
        c.setDoOutput(true);
        try (OutputStream out = c.getOutputStream()) { out.write(data); }
        int status = c.getResponseCode();
        if (status < 200 || status >= 300) throw new IOException("Supabase Storage upload failed (HTTP " + status + "): " + responseText(c));
        c.disconnect();
        return objectPath;
    }

    public static byte[] download(String objectPath) throws IOException {
        HttpURLConnection c = open("/storage/v1/object/authenticated/" + BUCKET + "/" + encodePath(objectPath), "GET");
        int status = c.getResponseCode();
        if (status < 200 || status >= 300) throw new IOException("Supabase Storage download failed (HTTP " + status + "): " + responseText(c));
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            return out.toByteArray();
        } finally { c.disconnect(); }
    }

    public static void delete(String objectPath) throws IOException {
        HttpURLConnection c = open("/storage/v1/object/" + BUCKET + "/" + encodePath(objectPath), "DELETE");
        int status = c.getResponseCode();
        if (status < 200 || status >= 300) throw new IOException("Supabase Storage delete failed (HTTP " + status + "): " + responseText(c));
        c.disconnect();
    }

    private static HttpURLConnection open(String path, String method) throws IOException {
        String base = required("SUPABASE_URL").replaceAll("/$", "");
        URL url = URI.create(base + path).toURL();
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(CONNECT_TIMEOUT_MS);
        c.setReadTimeout(READ_TIMEOUT_MS);
        c.setRequestProperty("apikey", secretKey());
        c.setRequestProperty("Authorization", "Bearer " + secretKey());
        c.setRequestProperty("Accept", "application/json");
        return c;
    }

    private static byte[] readAll(InputStream in, long expectedSize) throws IOException {
        if (expectedSize > 10L * 1024L * 1024L) throw new IOException("Maximum file size is 10 MB.");
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) Math.max(0, Math.min(expectedSize, 1024L * 1024L)));
        byte[] buffer = new byte[8192];
        long total = 0;
        int n;
        while ((n = in.read(buffer)) != -1) {
            total += n;
            if (total > 10L * 1024L * 1024L) throw new IOException("Maximum file size is 10 MB.");
            out.write(buffer, 0, n);
        }
        if (total != expectedSize) throw new IOException("Uploaded file size changed during transfer.");
        return out.toByteArray();
    }

    private static String encodePath(String path) {
        StringBuilder b = new StringBuilder();
        for (String part : path.split("/", -1)) {
            if (b.length() > 0) b.append('/');
            b.append(URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return b.toString();
    }

    private static String responseText(HttpURLConnection c) {
        try (InputStream in = c.getErrorStream() != null ? c.getErrorStream() : c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return "no response body";
            byte[] b = new byte[4096]; int n;
            while ((n = in.read(b)) != -1) out.write(b, 0, n);
            String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
            return text.length() > 500 ? text.substring(0, 500) : text;
        } catch (Exception e) { return "unable to read response"; }
    }

    private static String secretKey() {
        String key = System.getenv("SUPABASE_SECRET_KEY");
        if (key == null || key.trim().isEmpty()) key = System.getenv("SUPABASE_SERVICE_ROLE_KEY");
        if (key == null || key.trim().isEmpty()) key = System.getProperty("SUPABASE_SECRET_KEY");
        if (key == null || key.trim().isEmpty()) key = System.getProperty("SUPABASE_SERVICE_ROLE_KEY");
        if (key == null || key.trim().isEmpty()) throw new IllegalStateException("Missing SUPABASE_SECRET_KEY (or legacy SUPABASE_SERVICE_ROLE_KEY).");
        return key.trim();
    }

    private static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException("Missing configuration: " + key);
        return value.trim();
    }

    private static String envOr(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) value = System.getProperty(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}

package com.documentverification.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** File validation only. Actual file persistence is handled by Supabase Storage. */
public final class FileUtil {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED = new HashSet<String>(Arrays.asList("pdf", "doc", "docx"));
    private FileUtil() { }

    public static String validateExtension(String name) {
        if (name == null || name.trim().isEmpty() || !name.contains("."))
            throw new IllegalArgumentException("A file extension is required.");
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED.contains(ext))
            throw new IllegalArgumentException("Only PDF, DOC and DOCX files are allowed.");
        return ext;
    }

    public static String createSafeFileName(String extension) {
        return java.util.UUID.randomUUID().toString() + "." + extension;
    }

    public static void validateContent(InputStream input, String extension) throws IOException {
        byte[] h = new byte[8];
        int n = input.read(h);
        if (n < 4) throw new IOException("File content is too small.");
        if ("pdf".equals(extension)) {
            if (!(h[0]=='%' && h[1]=='P' && h[2]=='D' && h[3]=='F'))
                throw new IOException("Invalid PDF content.");
        } else {
            if (!(h[0]=='P' && h[1]=='K'))
                throw new IOException("Invalid Office document content.");
        }
    }

    public static long maxFileSize() { return MAX_FILE_SIZE; }
}

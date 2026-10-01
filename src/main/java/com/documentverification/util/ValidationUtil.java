package com.documentverification.util;

public final class ValidationUtil {

  private ValidationUtil() {}

  public static String required(String value, String field) {
    if (
      value == null || value.trim().isEmpty()
    ) throw new IllegalArgumentException(field + " is required.");
    return value.trim();
  }

  public static long positiveId(String value, String field) {
    try {
      long n = Long.parseLong(value);
      if (n <= 0) throw new NumberFormatException();
      return n;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid " + field + ".");
    }
  }

  public static String email(String value) {
    String v = required(value, "Email").toLowerCase();
    if (
      !v.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    ) throw new IllegalArgumentException("Invalid email address.");
    return v;
  }
}

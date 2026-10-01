package com.documentverification.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** PostgreSQL/Supabase JDBC connection factory. */
public final class DBConnection {

  private DBConnection() {}

  static {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static Connection getConnection() throws SQLException {
    String url = required("DB_URL");
    String user = required("DB_USERNAME");
    String password = required("DB_PASSWORD");

    return DriverManager.getConnection(url, user, password);
  }

  private static String required(String key) {
    String value = System.getenv(key);

    if (value == null || value.trim().isEmpty()) {
      value = System.getProperty(key);
    }

    if (value == null || value.trim().isEmpty()) {
      throw new IllegalStateException("Missing database configuration: " + key);
    }

    return value.trim();
  }
}

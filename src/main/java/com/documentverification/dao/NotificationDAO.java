package com.documentverification.dao;

import com.documentverification.util.DBConnection;
import java.sql.*;
import java.util.*;

public class NotificationDAO {

  public int countUnread(long org, long user) throws Exception {
    String q =
      "SELECT COUNT(*) FROM notifications WHERE organization_id=? AND user_id=? AND is_read=FALSE";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, user);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getInt(1);
      }
    }
  }

  public List<Map<String, Object>> findAll(long org, long user)
    throws Exception {
    String q =
      "SELECT * FROM notifications WHERE organization_id=? AND user_id=? ORDER BY created_at DESC";
    List<Map<String, Object>> l = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, user);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          Map<String, Object> m = new HashMap<>();
          m.put("id", r.getLong("id"));
          m.put("message", r.getString("message"));
          m.put("type", r.getString("type"));
          m.put("read", r.getBoolean("is_read"));
          m.put("createdAt", r.getTimestamp("created_at"));
          l.add(m);
        }
      }
    }
    return l;
  }

  public List<Map<String, Object>> findAllForAdmin(long org) throws Exception {
    String q =
      "SELECT n.*, u.name AS user_name, u.email AS user_email " +
      "FROM notifications n " +
      "LEFT JOIN users u ON u.id = n.user_id " +
      "WHERE n.organization_id=? ORDER BY n.created_at DESC LIMIT 100";
    List<Map<String, Object>> l = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          Map<String, Object> m = new HashMap<>();
          m.put("id", r.getLong("id"));
          m.put("userId", r.getLong("user_id"));
          m.put("userName", r.getString("user_name"));
          m.put("userEmail", r.getString("user_email"));
          m.put("message", r.getString("message"));
          m.put("type", r.getString("type"));
          m.put("read", r.getBoolean("is_read"));
          m.put("createdAt", r.getTimestamp("created_at"));
          l.add(m);
        }
      }
    }
    return l;
  }

  public List<String> findUnreadMessages(long org, long user) throws Exception {
    String q =
      "SELECT message FROM notifications WHERE organization_id=? AND user_id=? AND is_read=FALSE ORDER BY created_at DESC";
    List<String> l = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, user);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          l.add(r.getString(1));
        }
      }
    }
    return l;
  }

  public void create(long org, long user, Long doc, String message, String type)
    throws Exception {
    String q =
      "INSERT INTO notifications(organization_id, user_id, document_id, message, type) VALUES(?, ?, ?, ?, ?)";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, user);
      if (doc == null) {
        p.setNull(3, Types.BIGINT);
      } else {
        p.setLong(3, doc);
      }
      p.setString(4, message);
      p.setString(5, type);
      p.executeUpdate();
    }
  }

  public int broadcast(long org, String message, String type) throws Exception {
    String q =
      "INSERT INTO notifications(organization_id, user_id, message, type) " +
      "SELECT ?, id, ?, ? FROM users WHERE organization_id=? AND active=TRUE";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setString(2, message);
      p.setString(3, type);
      p.setLong(4, org);
      return p.executeUpdate();
    }
  }

  public void markRead(long org, long user, long id) throws Exception {
    String q =
      "UPDATE notifications SET is_read=TRUE WHERE id=? AND organization_id=? AND user_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, id);
      p.setLong(2, org);
      p.setLong(3, user);
      p.executeUpdate();
    }
  }

  public void markAllRead(long org, long user) throws Exception {
    String q =
      "UPDATE notifications SET is_read=TRUE WHERE organization_id=? AND user_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, user);
      p.executeUpdate();
    }
  }

  public void delete(long org, long id) throws Exception {
    String q = "DELETE FROM notifications WHERE id=? AND organization_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, id);
      p.setLong(2, org);
      p.executeUpdate();
    }
  }
}

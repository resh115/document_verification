package com.documentverification.dao;

import com.documentverification.model.User;
import com.documentverification.util.DBConnection;
import java.sql.*;
import java.util.*;

public class UserDAO {

  public List<Map<String, Object>> findRoles(long organizationId)
    throws Exception {
    String sql =
      "SELECT id,name,description FROM roles WHERE organization_id=? OR organization_id IS NULL OR organization_id=1 ORDER BY name";
    List<Map<String, Object>> out = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setLong(1, organizationId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          Map<String, Object> m = new HashMap<>();
          m.put("id", r.getLong("id"));
          m.put("name", r.getString("name"));
          m.put("description", r.getString("description"));
          out.add(m);
        }
      }
    }
    return out;
  }

  public void update(long org, long id, long role, String name, String email)
    throws Exception {
    String sql =
      "UPDATE users SET role_id=?,name=?,email=? WHERE id=? AND organization_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setLong(1, role);
      p.setString(2, name);
      p.setString(3, email);
      p.setLong(4, id);
      p.setLong(5, org);
      p.executeUpdate();
    }
  }

  public User authenticate(String email, String password) throws Exception {
    String sql =
      "SELECT u.*,r.name role_name,o.name organization_name FROM users u JOIN roles r ON r.id=u.role_id LEFT JOIN organizations o ON o.id=u.organization_id WHERE LOWER(u.email)=LOWER(?) AND u.active=TRUE";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, email);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) return null;
        User u = map(r);
        return com.documentverification.util.PasswordUtil.matches(
          password,
          u.getPasswordHash()
        )
          ? u
          : null;
      }
    }
  }

  public List<User> findAll(long org) throws Exception {
    return findByOrganization(org);
  }

  public List<User> findByOrganization(long org) throws Exception {
    return query(
      "SELECT u.*,r.name role_name,o.name organization_name FROM users u JOIN roles r ON r.id=u.role_id LEFT JOIN organizations o ON o.id=u.organization_id WHERE u.organization_id=? ORDER BY u.name",
      org
    );
  }

  public List<User> findAllUsers() throws Exception {
    List<User> out = new ArrayList<>();
    String q =
      "SELECT u.*,r.name role_name,o.name organization_name FROM users u JOIN roles r ON r.id=u.role_id LEFT JOIN organizations o ON o.id=u.organization_id ORDER BY u.organization_id,u.name";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q);
      ResultSet r = p.executeQuery()
    ) {
      while (r.next()) out.add(map(r));
    }
    return out;
  }

  public long create(User u) throws Exception {
    String q =
      "INSERT INTO users(organization_id,role_id,name,email,password_hash,active) VALUES(?,?,?,?,?,TRUE)";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(
        q,
        Statement.RETURN_GENERATED_KEYS
      )
    ) {
      p.setLong(1, u.getOrganizationId());
      p.setLong(2, u.getRoleId());
      p.setString(3, u.getName());
      p.setString(4, u.getEmail());
      p.setString(5, u.getPasswordHash());
      p.executeUpdate();
      try (ResultSet k = p.getGeneratedKeys()) {
        if (k.next()) return k.getLong(1);
      }
    }
    throw new IllegalStateException("User was not created.");
  }

  public void setActive(long id, long org, boolean active) throws Exception {
    String q = "UPDATE users SET active=? WHERE id=? AND organization_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setBoolean(1, active);
      p.setLong(2, id);
      p.setLong(3, org);
      p.executeUpdate();
    }
  }

  public User findById(long id) throws Exception {
    String q =
      "SELECT u.*,r.name role_name,o.name organization_name FROM users u JOIN roles r ON r.id=u.role_id LEFT JOIN organizations o ON o.id=u.organization_id WHERE u.id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? map(r) : null;
      }
    }
  }

  private List<User> query(String q, long org) throws Exception {
    List<User> out = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) out.add(map(r));
      }
    }
    return out;
  }

  private User map(ResultSet r) throws Exception {
    User u = new User();
    u.setId(r.getLong("id"));
    u.setOrganizationId(r.getLong("organization_id"));
    u.setRoleId(r.getLong("role_id"));
    u.setName(r.getString("name"));
    u.setEmail(r.getString("email"));
    u.setPasswordHash(r.getString("password_hash"));
    u.setRoleName(r.getString("role_name"));
    u.setOrganizationName(r.getString("organization_name"));
    u.setActive(r.getBoolean("active"));
    return u;
  }
}

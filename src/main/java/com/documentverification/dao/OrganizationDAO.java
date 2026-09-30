package com.documentverification.dao;

import com.documentverification.model.Organization;
import com.documentverification.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrganizationDAO {

    public List<Organization> findAll() throws Exception {
        String sql = "SELECT * FROM organizations ORDER BY id ASC";
        List<Organization> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Organization> findActive() throws Exception {
        String sql = "SELECT * FROM organizations WHERE active=TRUE ORDER BY name ASC";
        List<Organization> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Organization findById(long id) throws Exception {
        String sql = "SELECT * FROM organizations WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public long create(String name, String code, String type) throws Exception {
        String sql = "INSERT INTO organizations(name, code, type, active) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, code.toUpperCase().trim());
            ps.setString(3, type.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    seedDefaultTenantData(conn, newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Failed to create organization");
    }

    public void update(long id, String name, String code, String type) throws Exception {
        String sql = "UPDATE organizations SET name=?, code=?, type=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, code.toUpperCase().trim());
            ps.setString(3, type.trim());
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    public void setActive(long id, boolean active) throws Exception {
        String sql = "UPDATE organizations SET active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public int getUserCount(long organizationId) throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE organization_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    public int getDocumentCount(long organizationId) throws Exception {
        String sql = "SELECT COUNT(*) FROM documents WHERE organization_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void seedDefaultTenantData(Connection conn, long orgId) {
        String[][] roles = {
            {"ADMIN", "System & Organization Administrator"},
            {"STAFF", "Staff / Faculty / Verifier"},
            {"SUBMITTER", "Student / Submitter / Applicant"}
        };
        for (String[] r : roles) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO roles (organization_id, name, description) VALUES (?, ?, ?) " +
                    "ON CONFLICT (organization_id, name) DO NOTHING")) {
                ps.setLong(1, orgId);
                ps.setString(2, r[0]);
                ps.setString(3, r[1]);
                ps.executeUpdate();
            } catch (Exception ignored) {}
        }
    }

    private Organization map(ResultSet rs) throws SQLException {
        Organization org = new Organization();
        org.setId(rs.getLong("id"));
        org.setName(rs.getString("name"));
        org.setCode(rs.getString("code"));
        org.setType(rs.getString("type"));
        org.setActive(rs.getBoolean("active"));
        return org;
    }
}

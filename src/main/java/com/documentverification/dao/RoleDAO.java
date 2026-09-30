package com.documentverification.dao;

import com.documentverification.model.Role;
import com.documentverification.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

    public List<Role> findAll(long organizationId) throws Exception {
        String sql = "SELECT * FROM roles WHERE organization_id = ? OR organization_id IS NULL ORDER BY name ASC";
        List<Role> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Role findById(long id) throws Exception {
        String sql = "SELECT * FROM roles WHERE id = ?";
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

    public Role findByName(long organizationId, String name) throws Exception {
        String sql = "SELECT * FROM roles WHERE (organization_id = ? OR organization_id IS NULL) AND LOWER(name) = LOWER(?) LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, organizationId);
            ps.setString(2, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public long create(long organizationId, String name, String description) throws Exception {
        String cleanName = name.trim().toUpperCase();
        Role existing = findByName(organizationId, cleanName);
        if (existing != null) {
            return existing.getId();
        }

        String sql = "INSERT INTO roles(organization_id, name, description) VALUES(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, organizationId);
            ps.setString(2, cleanName);
            ps.setString(3, description != null ? description.trim() : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create role");
    }

    public boolean delete(long organizationId, long id) throws Exception {
        Role r = findById(id);
        if (r == null) return false;
        if ("ADMIN".equalsIgnoreCase(r.getName()) || "SUBMITTER".equalsIgnoreCase(r.getName())) {
            throw new IllegalArgumentException("System base roles (ADMIN, SUBMITTER) cannot be deleted.");
        }

        if (getUserCount(id) > 0) {
            throw new IllegalArgumentException("Cannot delete role: active users are assigned to this role.");
        }

        String sql = "DELETE FROM roles WHERE id = ? AND (organization_id = ? OR organization_id IS NULL)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, organizationId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getUserCount(long roleId) throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getStageCount(long roleId) throws Exception {
        String sql = "SELECT COUNT(*) FROM workflow_stages ws JOIN users u ON u.id=ws.user_id WHERE u.role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Role map(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setId(rs.getLong("id"));
        long orgId = rs.getLong("organization_id");
        r.setOrganizationId(rs.wasNull() ? null : orgId);
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        return r;
    }
}

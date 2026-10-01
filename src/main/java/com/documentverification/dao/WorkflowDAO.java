package com.documentverification.dao;

import com.documentverification.model.Workflow;
import com.documentverification.model.WorkflowStage;
import com.documentverification.util.DBConnection;
import java.sql.*;
import java.util.*;

public class WorkflowDAO {

  public List<Workflow> findByOrganization(long org) throws Exception {
    String q =
      "SELECT w.*,dt.name AS document_type_name FROM workflows w JOIN document_types dt ON dt.id=w.document_type_id WHERE w.organization_id=? ORDER BY w.name";
    List<Workflow> l = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          Workflow w = new Workflow();
          w.setId(r.getLong("id"));
          w.setOrganizationId(org);
          w.setDocumentTypeId(r.getLong("document_type_id"));
          w.setName(r.getString("name"));
          w.setActive(r.getBoolean("active"));
          l.add(w);
        }
      }
    }
    return l;
  }

  public Workflow find(long org, long id) throws Exception {
    return one(
      "SELECT * FROM workflows WHERE organization_id=? AND id=? AND active=TRUE",
      org,
      id
    );
  }

  public Workflow findActiveByDocumentType(long org, long type)
    throws Exception {
    String q =
      "SELECT * FROM workflows WHERE organization_id=? AND document_type_id=? AND active=TRUE ORDER BY id LIMIT 1";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, type);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? map(r, org) : null;
      }
    }
  }

  private Workflow one(String q, long org, long id) throws Exception {
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? map(r, org) : null;
      }
    }
  }

  private Workflow map(ResultSet r, long org) throws Exception {
    Workflow w = new Workflow();
    w.setId(r.getLong("id"));
    w.setOrganizationId(org);
    w.setDocumentTypeId(r.getLong("document_type_id"));
    w.setName(r.getString("name"));
    w.setActive(r.getBoolean("active"));
    return w;
  }

  public List<WorkflowStage> findStages(long org, long workflowId)
    throws Exception {
    String q =
      "SELECT ws.*,u.name user_name,u.email user_email FROM workflow_stages ws JOIN workflows w ON w.id=ws.workflow_id JOIN users u ON u.id=ws.user_id WHERE w.organization_id=? AND ws.workflow_id=? ORDER BY ws.stage_order";
    List<WorkflowStage> l = new ArrayList<>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, workflowId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) l.add(stage(r));
      }
    }
    return l;
  }

  private WorkflowStage stage(ResultSet r) throws Exception {
    WorkflowStage s = new WorkflowStage();
    s.setId(r.getLong("id"));
    s.setWorkflowId(r.getLong("workflow_id"));
    s.setStageOrder(r.getInt("stage_order"));
    s.setStageName(r.getString("stage_name"));
    s.setUserId(r.getLong("user_id"));
    s.setUserName(r.getString("user_name"));
    s.setUserEmail(r.getString("user_email"));
    return s;
  }

  public long create(long org, long type, String name) throws Exception {
    String q =
      "INSERT INTO workflows(organization_id,document_type_id,name) VALUES(?,?,?)";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(
        q,
        Statement.RETURN_GENERATED_KEYS
      )
    ) {
      p.setLong(1, org);
      p.setLong(2, type);
      p.setString(3, name);
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (r.next()) return r.getLong(1);
      }
    }
    throw new SQLException("Workflow not created");
  }

  public void update(long org, long id, long type, String name)
    throws Exception {
    String q =
      "UPDATE workflows SET document_type_id=?,name=? WHERE id=? AND organization_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, type);
      p.setString(2, name);
      p.setLong(3, id);
      p.setLong(4, org);
      p.executeUpdate();
    }
  }

  public void setActive(long org, long id, boolean active) throws Exception {
    String q = "UPDATE workflows SET active=? WHERE id=? AND organization_id=?";
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

  public long addStage(
    long org,
    long workflow,
    long order,
    String name,
    long userId
  ) throws Exception {
    String check = "SELECT id FROM workflows WHERE id=? AND organization_id=?";
    String uq =
      "SELECT u.id FROM users u JOIN roles r ON r.id=u.role_id WHERE u.id=? AND u.organization_id=? AND u.active=TRUE AND UPPER(r.name) <> 'ADMIN'";
    String q =
      "INSERT INTO workflow_stages(workflow_id,stage_order,stage_name,user_id) VALUES(?,?,?,?)";
    try (Connection c = DBConnection.getConnection()) {
      try (PreparedStatement x = c.prepareStatement(check)) {
        x.setLong(1, workflow);
        x.setLong(2, org);
        try (ResultSet r = x.executeQuery()) {
          if (!r.next()) throw new SecurityException("Workflow not found.");
        }
      }
      try (PreparedStatement x = c.prepareStatement(uq)) {
        x.setLong(1, userId);
        x.setLong(2, org);
        try (ResultSet r = x.executeQuery()) {
          if (!r.next()) throw new SecurityException(
            "Verifier must belong to this organization, be active, and cannot be an administrator."
          );
        }
      }
      try (
        PreparedStatement p = c.prepareStatement(
          q,
          Statement.RETURN_GENERATED_KEYS
        )
      ) {
        p.setLong(1, workflow);
        p.setLong(2, order);
        p.setString(3, name);
        p.setLong(4, userId);
        p.executeUpdate();
        try (ResultSet r = p.getGeneratedKeys()) {
          if (r.next()) return r.getLong(1);
        }
      }
    }
    throw new SQLException("Stage not created");
  }

  public void updateStage(
    long org,
    long id,
    long order,
    String name,
    long userId
  ) throws Exception {
    String check =
      "SELECT s.id FROM workflow_stages s JOIN workflows w ON w.id=s.workflow_id WHERE s.id=? AND w.organization_id=?";
    String uq =
      "SELECT u.id FROM users u JOIN roles r ON r.id=u.role_id WHERE u.id=? AND u.organization_id=? AND u.active=TRUE AND UPPER(r.name) <> 'ADMIN'";
    String q =
      "UPDATE workflow_stages ws SET stage_order=?,stage_name=?,user_id=? WHERE ws.id=? AND EXISTS (SELECT 1 FROM workflows w WHERE w.id=ws.workflow_id AND w.organization_id=?)";
    try (Connection c = DBConnection.getConnection()) {
      try (PreparedStatement x = c.prepareStatement(check)) {
        x.setLong(1, id);
        x.setLong(2, org);
        try (ResultSet r = x.executeQuery()) {
          if (!r.next()) throw new SecurityException(
            "Workflow stage not found."
          );
        }
      }
      try (PreparedStatement x = c.prepareStatement(uq)) {
        x.setLong(1, userId);
        x.setLong(2, org);
        try (ResultSet r = x.executeQuery()) {
          if (!r.next()) throw new SecurityException(
            "Verifier must belong to this organization, be active, and cannot be an administrator."
          );
        }
      }
      try (PreparedStatement p = c.prepareStatement(q)) {
        p.setLong(1, order);
        p.setString(2, name);
        p.setLong(3, userId);
        p.setLong(4, id);
        p.setLong(5, org);
        p.executeUpdate();
      }
    }
  }

  public void deleteStage(long org, long id) throws Exception {
    String q =
      "DELETE FROM workflow_stages ws WHERE ws.id=? AND EXISTS (SELECT 1 FROM workflows w WHERE w.id=ws.workflow_id AND w.organization_id=?)";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, id);
      p.setLong(2, org);
      p.executeUpdate();
    }
  }

  public WorkflowStage findStage(long org, long id) throws Exception {
    String q =
      "SELECT ws.*,u.name user_name,u.email user_email FROM workflow_stages ws JOIN workflows w ON w.id=ws.workflow_id JOIN users u ON u.id=ws.user_id WHERE w.organization_id=? AND ws.id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? stage(r) : null;
      }
    }
  }

  public WorkflowStage findStageByOrder(long org, long workflowId, int order)
    throws Exception {
    String q =
      "SELECT ws.*,u.name user_name,u.email user_email FROM workflow_stages ws JOIN workflows w ON w.id=ws.workflow_id JOIN users u ON u.id=ws.user_id WHERE w.organization_id=? AND ws.workflow_id=? AND ws.stage_order=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, workflowId);
      p.setInt(3, order);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? stage(r) : null;
      }
    }
  }
}

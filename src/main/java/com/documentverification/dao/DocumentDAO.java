package com.documentverification.dao;

import com.documentverification.model.Document;
import com.documentverification.util.DBConnection;
import java.sql.*;
import java.util.*;

public class DocumentDAO {

  public List<Document> findBySubmitter(long org, long user) throws Exception {
    return find(
      "SELECT d.* FROM documents d WHERE d.organization_id=? AND d.submitter_id=? ORDER BY d.updated_at DESC",
      org,
      user
    );
  }

  public List<Document> findByOrganization(long org) throws Exception {
    return find(
      "SELECT d.* FROM documents d WHERE d.organization_id=? ORDER BY d.updated_at DESC",
      org
    );
  }

  public List<Document> findApprovedByOrganization(long org) throws Exception {
    return find(
      "SELECT d.* FROM documents d WHERE d.organization_id=? AND d.status='APPROVED' ORDER BY d.updated_at DESC",
      org
    );
  }

  public List<Document> findApprovedBySubmitter(long org, long user)
    throws Exception {
    return find(
      "SELECT d.* FROM documents d WHERE d.organization_id=? AND d.submitter_id=? AND d.status='APPROVED' ORDER BY d.updated_at DESC",
      org,
      user
    );
  }

  public Document findById(long org, long id) throws Exception {
    String q = "SELECT * FROM documents WHERE organization_id=? AND id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? map(r) : null;
      }
    }
  }

  public long create(Document d) throws Exception {
    try (Connection c = DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try {
        long id = create(c, d);
        c.commit();
        return id;
      } catch (Exception e) {
        c.rollback();
        throw e;
      }
    }
  }

  public long create(Connection c, Document d) throws Exception {
    String q =
      "INSERT INTO documents(organization_id,submitter_id,document_type_id,workflow_id,current_stage_id,title,description,status,current_stage_order,current_version) VALUES(?,?,?,?,?,?,?,?,?,?)";
    try (
      PreparedStatement p = c.prepareStatement(
        q,
        Statement.RETURN_GENERATED_KEYS
      )
    ) {
      p.setLong(1, d.getOrganizationId());
      p.setLong(2, d.getSubmitterId());
      p.setLong(3, d.getDocumentTypeId());
      p.setLong(4, d.getWorkflowId());
      if (d.getCurrentStageId() > 0) p.setLong(5, d.getCurrentStageId());
      else p.setNull(5, Types.BIGINT);
      p.setString(6, d.getTitle());
      p.setString(7, d.getDescription());
      p.setString(8, d.getStatus());
      p.setInt(9, d.getCurrentStageOrder());
      p.setInt(10, d.getCurrentVersion());
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (r.next()) return r.getLong(1);
      }
    }
    throw new SQLException("Document was not created");
  }

  public void updateStatus(
    Connection c,
    long org,
    long id,
    String status,
    long stageId,
    int stageOrder,
    int version
  ) throws Exception {
    String q =
      "UPDATE documents SET status=?,current_stage_id=?,current_stage_order=?,current_version=? WHERE organization_id=? AND id=?";
    try (PreparedStatement p = c.prepareStatement(q)) {
      p.setString(1, status);
      if (stageId > 0) p.setLong(2, stageId);
      else p.setNull(2, Types.BIGINT);
      p.setInt(3, stageOrder);
      p.setInt(4, version);
      p.setLong(5, org);
      p.setLong(6, id);
      if (p.executeUpdate() != 1) throw new SecurityException(
        "Document not found"
      );
    }
  }

  public void updateStatus(
    long org,
    long id,
    String status,
    long stageId,
    int stageOrder,
    int version
  ) throws Exception {
    try (Connection c = DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try {
        updateStatus(c, org, id, status, stageId, stageOrder, version);
        c.commit();
      } catch (Exception e) {
        c.rollback();
        throw e;
      }
    }
  }

  private List<Document> find(String q, long... v) throws Exception {
    List<Document> l = new ArrayList<Document>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      for (int i = 0; i < v.length; i++) p.setLong(i + 1, v[i]);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) l.add(map(r));
      }
    }
    return l;
  }

  private Document map(ResultSet r) throws Exception {
    Document d = new Document();
    d.setId(r.getLong("id"));
    d.setOrganizationId(r.getLong("organization_id"));
    d.setSubmitterId(r.getLong("submitter_id"));
    d.setDocumentTypeId(r.getLong("document_type_id"));
    d.setWorkflowId(r.getLong("workflow_id"));
    d.setCurrentStageId(r.getLong("current_stage_id"));
    d.setTitle(r.getString("title"));
    d.setDescription(r.getString("description"));
    d.setStatus(r.getString("status"));
    d.setCurrentStageOrder(r.getInt("current_stage_order"));
    d.setCurrentVersion(r.getInt("current_version"));
    d.setCreatedAt(r.getTimestamp("created_at"));
    d.setUpdatedAt(r.getTimestamp("updated_at"));
    return d;
  }
}

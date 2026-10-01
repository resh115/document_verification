package com.documentverification.dao;

import com.documentverification.util.DBConnection;
import java.sql.*;
import java.util.*;

public class DashboardDAO {

  public Map<String, Integer> admin(long org) throws SQLException {
    Map<String, Integer> m = new HashMap<>();
    m.put("tenants", countAll("SELECT COUNT(*) FROM organizations"));
    m.put(
      "users",
      count("SELECT COUNT(*) FROM users WHERE organization_id=?", org)
    );
    m.put(
      "workflows",
      count("SELECT COUNT(*) FROM workflows WHERE organization_id=?", org)
    );
    m.put(
      "documents",
      count("SELECT COUNT(*) FROM documents WHERE organization_id=?", org)
    );
    return m;
  }

  public Map<String, Integer> submitter(long org, long user)
    throws SQLException {
    Map<String, Integer> m = new HashMap<>();
    m.put(
      "documents",
      count2(
        "SELECT COUNT(*) FROM documents WHERE organization_id=? AND submitter_id=?",
        org,
        user
      )
    );
    m.put(
      "pending",
      count2(
        "SELECT COUNT(*) FROM documents WHERE organization_id=? AND submitter_id=? AND status IN ('PENDING_APPROVAL','PENDING','IN_REVIEW')",
        org,
        user
      )
    );
    m.put(
      "approved",
      count2(
        "SELECT COUNT(*) FROM documents WHERE organization_id=? AND submitter_id=? AND status='APPROVED'",
        org,
        user
      )
    );
    m.put(
      "changes",
      count2(
        "SELECT COUNT(*) FROM documents WHERE organization_id=? AND submitter_id=? AND status='CHANGES_REQUIRED'",
        org,
        user
      )
    );
    m.put(
      "rejected",
      count2(
        "SELECT COUNT(*) FROM documents WHERE organization_id=? AND submitter_id=? AND status='REJECTED'",
        org,
        user
      )
    );
    return m;
  }

  public Map<String, Integer> approver(long org, long user)
    throws SQLException {
    Map<String, Integer> m = new HashMap<>();
    m.put("queue", countApproverQueue(org, user));
    m.put(
      "approved",
      count2(
        "SELECT COUNT(*) FROM approval_actions a JOIN documents d ON d.id=a.document_id WHERE d.organization_id=? AND a.approver_id=? AND a.action='APPROVE'",
        org,
        user
      )
    );
    m.put(
      "rejected",
      count2(
        "SELECT COUNT(*) FROM approval_actions a JOIN documents d ON d.id=a.document_id WHERE d.organization_id=? AND a.approver_id=? AND a.action='REJECT'",
        org,
        user
      )
    );
    m.put(
      "changes",
      count2(
        "SELECT COUNT(*) FROM approval_actions a JOIN documents d ON d.id=a.document_id WHERE d.organization_id=? AND a.approver_id=? AND a.action='REQUEST_CHANGES'",
        org,
        user
      )
    );
    return m;
  }

  private int countApproverQueue(long org, long user) throws SQLException {
    return count2(
      "SELECT COUNT(*) FROM documents d JOIN workflow_stages s ON s.id=d.current_stage_id WHERE d.organization_id=? AND s.user_id=? AND d.status IN ('PENDING_APPROVAL','PENDING','IN_REVIEW')",
      org,
      user
    );
  }

  private int countAll(String q) throws SQLException {
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q);
      ResultSet r = p.executeQuery()
    ) {
      r.next();
      return r.getInt(1);
    }
  }

  private int count(String q, long a) throws SQLException {
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, a);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getInt(1);
      }
    }
  }

  private int count2(String q, long a, long b) throws SQLException {
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, a);
      p.setLong(2, b);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getInt(1);
      }
    }
  }
}

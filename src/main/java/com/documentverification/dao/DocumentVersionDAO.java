package com.documentverification.dao;

import com.documentverification.model.DocumentVersion;
import com.documentverification.util.DBConnection;
import java.sql.*;
import java.util.*;

public class DocumentVersionDAO {

  public List<DocumentVersion> findByDocument(long org, long doc)
    throws Exception {
    String q =
      "SELECT v.* FROM document_versions v JOIN documents d ON d.id=v.document_id WHERE d.organization_id=? AND v.document_id=? ORDER BY v.version_number DESC";
    List<DocumentVersion> l = new ArrayList<DocumentVersion>();
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, doc);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) l.add(map(r));
      }
    }
    return l;
  }

  public DocumentVersion find(long org, long doc, long id) throws Exception {
    String q =
      "SELECT v.* FROM document_versions v JOIN documents d ON d.id=v.document_id WHERE d.organization_id=? AND v.document_id=? AND v.id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, org);
      p.setLong(2, doc);
      p.setLong(3, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? map(r) : null;
      }
    }
  }

  public int nextVersion(long doc) throws Exception {
    String q =
      "SELECT COALESCE(MAX(version_number),0)+1 FROM document_versions WHERE document_id=?";
    try (
      Connection c = DBConnection.getConnection();
      PreparedStatement p = c.prepareStatement(q)
    ) {
      p.setLong(1, doc);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getInt(1);
      }
    }
  }

  public long create(
    long doc,
    int version,
    long user,
    String original,
    String stored,
    String path,
    long size
  ) throws Exception {
    try (Connection c = DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try {
        long id = create(c, doc, version, user, original, stored, path, size);
        c.commit();
        return id;
      } catch (Exception e) {
        c.rollback();
        throw e;
      }
    }
  }

  public long create(
    Connection c,
    long doc,
    int version,
    long user,
    String original,
    String stored,
    String path,
    long size
  ) throws Exception {
    String q =
      "INSERT INTO document_versions(document_id,version_number,uploaded_by,original_file_name,stored_file_name,file_path,file_size) VALUES(?,?,?,?,?,?,?)";
    try (
      PreparedStatement p = c.prepareStatement(
        q,
        Statement.RETURN_GENERATED_KEYS
      )
    ) {
      p.setLong(1, doc);
      p.setInt(2, version);
      p.setLong(3, user);
      p.setString(4, original);
      p.setString(5, stored);
      p.setString(6, path);
      p.setLong(7, size);
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (r.next()) return r.getLong(1);
      }
    }
    throw new SQLException("Version was not created");
  }

  private DocumentVersion map(ResultSet r) throws Exception {
    DocumentVersion v = new DocumentVersion();
    v.setId(r.getLong("id"));
    v.setDocumentId(r.getLong("document_id"));
    v.setVersionNumber(r.getInt("version_number"));
    v.setUploadedBy(r.getLong("uploaded_by"));
    v.setOriginalFileName(r.getString("original_file_name"));
    v.setStoredFileName(r.getString("stored_file_name"));
    v.setFilePath(r.getString("file_path"));
    v.setFileSize(r.getLong("file_size"));
    v.setUploadedAt(r.getTimestamp("uploaded_at"));
    return v;
  }
}

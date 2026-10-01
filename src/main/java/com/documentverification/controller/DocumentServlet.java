package com.documentverification.controller;

import com.documentverification.dao.*;
import com.documentverification.model.*;
import com.documentverification.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/documents")
@MultipartConfig(
  maxFileSize = 10 * 1024 * 1024,
  maxRequestSize = 12 * 1024 * 1024
)
public class DocumentServlet extends HttpServlet {

  private final DocumentDAO documents = new DocumentDAO();
  private final DocumentVersionDAO versions = new DocumentVersionDAO();
  private final DocumentTypeDAO types = new DocumentTypeDAO();
  private final WorkflowDAO workflows = new WorkflowDAO();
  private final AuditDAO audit = new AuditDAO();

  protected void doGet(HttpServletRequest r, HttpServletResponse s)
    throws ServletException, IOException {
    String role = (String) r.getSession().getAttribute("role");
    if ("ADMIN".equals(role)) {
      s.sendError(403, "Admins cannot view or submit documents.");
      return;
    }
    long org = (Long) r.getSession().getAttribute("organizationId"),
      uid = (Long) r.getSession().getAttribute("userId");
    try {
      String action = r.getParameter("action");
      if ("download".equals(action)) {
        download(r, s, org, uid);
        return;
      }
      if ("view".equals(action)) {
        long id = ValidationUtil.positiveId(
          r.getParameter("id"),
          "document id"
        );
        Document d = documents.findById(org, id);
        if (
          d == null || (d.getSubmitterId() != uid && "SUBMITTER".equals(role))
        ) {
          s.sendError(403);
          return;
        }
        r.setAttribute("document", d);
        r.setAttribute("versions", versions.findByDocument(org, id));
        r.setAttribute("history", new ApprovalDAO().findHistory(org, id));
        r.setAttribute("csrfToken", CsrfUtil.token(r.getSession()));
        r.getRequestDispatcher(
          "/WEB-INF/views/submitter/document-view.jsp"
        ).forward(r, s);
        return;
      }
      r.setAttribute("documents", documents.findBySubmitter(org, uid));
      r.setAttribute("documentTypes", types.findActive(org));
      r.setAttribute("csrfToken", CsrfUtil.token(r.getSession()));
      r.getRequestDispatcher("/WEB-INF/views/submitter/documents.jsp").forward(
        r,
        s
      );
    } catch (Exception e) {
      throw new ServletException("Unable to load documents", e);
    }
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse s)
    throws ServletException, IOException {
    String role = (String) r.getSession().getAttribute("role");
    if ("ADMIN".equals(role)) {
      s.sendError(403, "Admins cannot submit or revise documents.");
      return;
    }
    if (!CsrfUtil.valid(r)) {
      s.sendError(403, "Invalid CSRF token");
      return;
    }
    long org = (Long) r.getSession().getAttribute("organizationId"),
      uid = (Long) r.getSession().getAttribute("userId");
    String action = r.getParameter("action");
    try {
      if ("revise".equals(action)) {
        revise(r, org, uid);
        s.sendRedirect(
          r.getContextPath() +
            "/documents?action=view&id=" +
            r.getParameter("documentId")
        );
        return;
      }
      create(r, org, uid);
      s.sendRedirect(r.getContextPath() + "/documents");
    } catch (IllegalArgumentException e) {
      try {
        r.setAttribute("error", e.getMessage());
        r.setAttribute("documentTypes", types.findActive(org));
        r.setAttribute("documents", documents.findBySubmitter(org, uid));
        r.setAttribute("csrfToken", CsrfUtil.token(r.getSession()));
        r.getRequestDispatcher(
          "/WEB-INF/views/submitter/documents.jsp"
        ).forward(r, s);
      } catch (Exception viewError) {
        throw new ServletException("Unable to render document form", viewError);
      }
    } catch (Exception e) {
      throw new ServletException("Unable to process document", e);
    }
  }

  private void create(HttpServletRequest r, long org, long uid)
    throws Exception {
    String title = ValidationUtil.required(r.getParameter("title"), "Title"),
      desc = r.getParameter("description");
    long type = ValidationUtil.positiveId(
      r.getParameter("documentTypeId"),
      "document type"
    );
    Part part = r.getPart("documentFile");
    if (part == null || part.getSize() == 0) throw new IllegalArgumentException(
      "Select a document."
    );
    String ext = FileUtil.validateExtension(part.getSubmittedFileName());
    try (InputStream in = part.getInputStream()) {
      FileUtil.validateContent(in, ext);
    }
    Workflow w = workflows.findActiveByDocumentType(org, type);
    if (w == null) throw new IllegalArgumentException(
      "No active workflow is configured for this document type."
    );
    List<WorkflowStage> stages = workflows.findStages(org, w.getId());
    if (stages.isEmpty()) throw new IllegalArgumentException(
      "Workflow has no approval stages."
    );

    Connection c = DBConnection.getConnection();
    String objectPath = null;
    boolean uploaded = false;
    try {
      c.setAutoCommit(false);
      Document d = new Document();
      d.setOrganizationId(org);
      d.setSubmitterId(uid);
      d.setDocumentTypeId(type);
      d.setWorkflowId(w.getId());
      d.setCurrentStageId(stages.get(0).getId());
      d.setTitle(title);
      d.setDescription(desc);
      d.setStatus("PENDING_APPROVAL");
      d.setCurrentStageOrder(stages.get(0).getStageOrder());
      d.setCurrentVersion(1);
      long docId = documents.create(c, d);
      String stored = FileUtil.createSafeFileName(ext);
      objectPath = storagePath(org, docId, 1, stored);
      try (InputStream in = part.getInputStream()) {
        SupabaseStorage.upload(
          in,
          objectPath,
          part.getSize(),
          contentType(ext)
        );
      }
      uploaded = true;
      versions.create(
        c,
        docId,
        1,
        uid,
        part.getSubmittedFileName(),
        stored,
        objectPath,
        part.getSize()
      );
      notifyStage(c, org, docId, stages.get(0));
      audit.log(
        org,
        uid,
        "DOCUMENT_CREATED",
        "Document #" + docId + " submitted",
        rRemote(r)
      );
      c.commit();
    } catch (Exception e) {
      try {
        c.rollback();
      } catch (Exception ignored) {}
      if (uploaded && objectPath != null) try {
        SupabaseStorage.delete(objectPath);
      } catch (Exception ignored) {}
      throw e;
    } finally {
      try {
        c.close();
      } catch (Exception ignored) {}
    }
  }

  private void revise(HttpServletRequest r, long org, long uid)
    throws Exception {
    long id = ValidationUtil.positiveId(
      r.getParameter("documentId"),
      "document id"
    );
    Document d = documents.findById(org, id);
    if (d == null || d.getSubmitterId() != uid) throw new SecurityException(
      "Not your document"
    );
    if (
      !"CHANGES_REQUIRED".equals(d.getStatus())
    ) throw new IllegalArgumentException(
      "Only documents requiring changes can be revised."
    );
    WorkflowStage stage = workflows.findStage(org, d.getCurrentStageId());
    if (stage == null) {
      List<ApprovalAction> history = new ApprovalDAO().findHistory(org, id);
      if (history.isEmpty()) throw new IllegalArgumentException(
        "The approval stage for this revision could not be found."
      );
      stage = workflows.findStageByOrder(
        org,
        d.getWorkflowId(),
        history.get(history.size() - 1).getStageOrder()
      );
    }
    if (stage == null) throw new IllegalArgumentException(
      "The approval stage for this revision could not be found."
    );
    Part part = r.getPart("documentFile");
    if (part == null || part.getSize() == 0) throw new IllegalArgumentException(
      "Select a revised document."
    );
    String ext = FileUtil.validateExtension(part.getSubmittedFileName());
    try (InputStream in = part.getInputStream()) {
      FileUtil.validateContent(in, ext);
    }
    int v = d.getCurrentVersion() + 1;
    String stored = FileUtil.createSafeFileName(ext),
      objectPath = storagePath(org, id, v, stored);
    boolean uploaded = false;
    try (InputStream in = part.getInputStream()) {
      SupabaseStorage.upload(in, objectPath, part.getSize(), contentType(ext));
      uploaded = true;
    }
    Connection c = DBConnection.getConnection();
    try {
      c.setAutoCommit(false);
      versions.create(
        c,
        id,
        v,
        uid,
        part.getSubmittedFileName(),
        stored,
        objectPath,
        part.getSize()
      );
      String q =
        "UPDATE documents SET status='PENDING_APPROVAL',current_version=?,current_stage_id=?,current_stage_order=? WHERE id=? AND organization_id=? AND submitter_id=?";
      try (PreparedStatement p = c.prepareStatement(q)) {
        p.setInt(1, v);
        p.setLong(2, stage.getId());
        p.setInt(3, stage.getStageOrder());
        p.setLong(4, id);
        p.setLong(5, org);
        p.setLong(6, uid);
        if (p.executeUpdate() != 1) throw new SecurityException(
          "Document could not be revised."
        );
      }
      notifyStage(c, org, id, stage);
      audit.log(
        org,
        uid,
        "VERSION_UPLOADED",
        "Version " + v + " uploaded for document #" + id,
        rRemote(r)
      );
      c.commit();
    } catch (Exception e) {
      try {
        c.rollback();
      } catch (Exception ignored) {}
      if (uploaded) try {
        SupabaseStorage.delete(objectPath);
      } catch (Exception ignored) {}
      throw e;
    } finally {
      try {
        c.close();
      } catch (Exception ignored) {}
    }
  }

  private void notifyStage(Connection c, long org, long doc, WorkflowStage st)
    throws Exception {
    if (st == null || st.getUserId() <= 0) return;
    try (
      PreparedStatement n = c.prepareStatement(
        "INSERT INTO notifications(organization_id,user_id,document_id,message,type) VALUES(?,?,?,?,?)"
      )
    ) {
      n.setLong(1, org);
      n.setLong(2, st.getUserId());
      n.setLong(3, doc);
      n.setString(
        4,
        "Document #" +
          doc +
          " is waiting for your verification at " +
          st.getStageName() +
          "."
      );
      n.setString(5, "WORKFLOW");
      n.executeUpdate();
    }
  }

  private void download(
    HttpServletRequest r,
    HttpServletResponse s,
    long org,
    long uid
  ) throws Exception {
    String role = (String) r.getSession().getAttribute("role");
    if ("ADMIN".equals(role)) throw new SecurityException(
      "Admins cannot download documents."
    );
    long doc = ValidationUtil.positiveId(r.getParameter("id"), "document id"),
      vid = ValidationUtil.positiveId(
        r.getParameter("versionId"),
        "version id"
      );
    Document d = documents.findById(org, doc);
    if (d == null) throw new SecurityException("Document not found");
    if (
      "SUBMITTER".equals(role) && d.getSubmitterId() != uid
    ) throw new SecurityException("Access denied");
    DocumentVersion v = versions.find(org, doc, vid);
    if (v == null) throw new SecurityException("Version not found");
    String mimeType = getServletContext().getMimeType(v.getOriginalFileName());
    s.setContentType(
      mimeType != null
        ? mimeType
        : contentType(FileUtil.validateExtension(v.getOriginalFileName()))
    );
    s.setContentLengthLong(v.getFileSize());
    s.setHeader(
      "Content-Disposition",
      "attachment; filename=\"" +
        v.getOriginalFileName().replaceAll("[\\\"\\r\\n]", "_") +
        "\""
    );
    SupabaseStorage.download(v.getFilePath(), s.getOutputStream());
  }

  private static String storagePath(
    long org,
    long doc,
    int version,
    String stored
  ) {
    return (
      "organizations/" +
      org +
      "/documents/" +
      doc +
      "/v" +
      version +
      "/" +
      stored
    );
  }

  private static String contentType(String ext) {
    if ("pdf".equals(ext)) return "application/pdf";
    if ("doc".equals(ext)) return "application/msword";
    if (
      "docx".equals(ext)
    ) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    return "application/octet-stream";
  }

  private String rRemote(HttpServletRequest r) {
    return r.getRemoteAddr();
  }
}

package com.documentverification.controller;

import com.documentverification.dao.*;
import com.documentverification.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet("/admin/workflows")
public class WorkflowServlet extends HttpServlet {

  private final WorkflowDAO dao = new WorkflowDAO();
  private final DocumentTypeDAO types = new DocumentTypeDAO();
  private final UserDAO users = new UserDAO();
  private final AuditDAO audit = new AuditDAO();

  protected void doGet(HttpServletRequest r, HttpServletResponse s)
    throws ServletException, IOException {
    try {
      long org = (Long) r.getSession().getAttribute("organizationId");
      r.setAttribute("workflows", dao.findByOrganization(org));
      r.setAttribute("documentTypes", types.findAll(org));
      r.setAttribute("users", users.findByOrganization(org));
      String wid = r.getParameter("workflowId");
      if (wid != null && !wid.trim().isEmpty()) r.setAttribute(
        "stages",
        dao.findStages(org, Long.parseLong(wid))
      );
      r.setAttribute("csrfToken", CsrfUtil.token(r.getSession()));
      r.getRequestDispatcher("/WEB-INF/views/admin/workflows.jsp").forward(
        r,
        s
      );
    } catch (Exception e) {
      throw new ServletException("Unable to load workflows", e);
    }
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse s)
    throws ServletException, IOException {
    if (!CsrfUtil.valid(r)) {
      s.sendError(403, "Invalid CSRF token");
      return;
    }
    long org = (Long) r.getSession().getAttribute("organizationId"),
      uid = (Long) r.getSession().getAttribute("userId");
    try {
      String a = r.getParameter("action");
      if ("create".equals(a)) dao.create(
        org,
        Long.parseLong(r.getParameter("documentTypeId")),
        ValidationUtil.required(r.getParameter("name"), "Workflow name")
      );
      else if ("update".equals(a)) dao.update(
        org,
        Long.parseLong(r.getParameter("id")),
        Long.parseLong(r.getParameter("documentTypeId")),
        ValidationUtil.required(r.getParameter("name"), "Workflow name")
      );
      else if ("toggle".equals(a)) dao.setActive(
        org,
        Long.parseLong(r.getParameter("id")),
        "1".equals(r.getParameter("active"))
      );
      else if ("addStage".equals(a)) dao.addStage(
        org,
        Long.parseLong(r.getParameter("workflowId")),
        Long.parseLong(r.getParameter("stageOrder")),
        ValidationUtil.required(r.getParameter("stageName"), "Stage name"),
        Long.parseLong(r.getParameter("userId"))
      );
      else if ("updateStage".equals(a)) dao.updateStage(
        org,
        Long.parseLong(r.getParameter("id")),
        Long.parseLong(r.getParameter("stageOrder")),
        ValidationUtil.required(r.getParameter("stageName"), "Stage name"),
        Long.parseLong(r.getParameter("userId"))
      );
      else if ("deleteStage".equals(a)) dao.deleteStage(
        org,
        Long.parseLong(r.getParameter("id"))
      );
      audit.log(
        org,
        uid,
        "WORKFLOW_CHANGED",
        "Workflow configuration changed",
        r.getRemoteAddr()
      );
      String wid = r.getParameter("workflowId");
      s.sendRedirect(
        r.getContextPath() +
          "/admin/workflows" +
          (wid != null ? "?workflowId=" + wid : "")
      );
    } catch (Exception e) {
      throw new ServletException("Unable to modify workflow", e);
    }
  }
}

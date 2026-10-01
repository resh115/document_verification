package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.dao.DocumentTypeDAO;
import com.documentverification.util.CsrfUtil;
import com.documentverification.util.ValidationUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/admin/document-types")
public class AdminServlet extends HttpServlet {

  private final DocumentTypeDAO types = new DocumentTypeDAO();
  private final AuditDAO audit = new AuditDAO();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    HttpSession session = request.getSession();
    long organizationId = (Long) session.getAttribute("organizationId");

    try {
      request.setAttribute("items", types.findAll(organizationId));
      request.setAttribute("csrfToken", CsrfUtil.token(session));
      request
        .getRequestDispatcher("/WEB-INF/views/admin/document-types.jsp")
        .forward(request, response);
    } catch (Exception e) {
      throw new ServletException("Unable to load document types", e);
    }
  }

  @Override
  protected void doPost(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws ServletException, IOException {
    if (!CsrfUtil.valid(request)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    String action = request.getParameter("action");
    if (
      !"create".equals(action) &&
      !"update".equals(action) &&
      !"toggle".equals(action)
    ) {
      response.sendError(
        HttpServletResponse.SC_BAD_REQUEST,
        "Unsupported action"
      );
      return;
    }

    HttpSession session = request.getSession();
    long organizationId = (Long) session.getAttribute("organizationId");
    long userId = (Long) session.getAttribute("userId");

    try {
      long documentTypeId;
      if ("create".equals(action)) {
        String name = ValidationUtil.required(
          request.getParameter("name"),
          "Name"
        );
        documentTypeId = types.create(
          organizationId,
          name,
          request.getParameter("description")
        );
      } else {
        documentTypeId = Long.parseLong(request.getParameter("id"));
        if ("update".equals(action)) {
          String name = ValidationUtil.required(
            request.getParameter("name"),
            "Name"
          );
          types.update(
            organizationId,
            documentTypeId,
            name,
            request.getParameter("description")
          );
        } else {
          types.setActive(
            organizationId,
            documentTypeId,
            "1".equals(request.getParameter("active"))
          );
        }
      }

      audit.log(
        organizationId,
        userId,
        "DOCUMENT_TYPE_CHANGED",
        "Document type #" + documentTypeId,
        request.getRemoteAddr()
      );
      response.sendRedirect(request.getContextPath() + "/admin/document-types");
    } catch (IllegalArgumentException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      throw new ServletException(
        "Unable to process document type operation",
        e
      );
    }
  }
}

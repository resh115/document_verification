package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.dao.OrganizationDAO;
import com.documentverification.model.Organization;
import com.documentverification.util.CsrfUtil;
import com.documentverification.util.ValidationUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/admin/tenants")
public class TenantServlet extends HttpServlet {

  private final OrganizationDAO orgDAO = new OrganizationDAO();
  private final AuditDAO auditDAO = new AuditDAO();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    String switchId = request.getParameter("switchOrg");
    if (switchId != null && !switchId.trim().isEmpty()) {
      try {
        long targetOrg = Long.parseLong(switchId);
        Organization org = orgDAO.findById(targetOrg);
        if (org != null) {
          session.setAttribute("organizationId", org.getId());
          session.setAttribute("organizationName", org.getName());
          session.setAttribute("organizationCode", org.getCode());
        }
        response.sendRedirect(request.getContextPath() + "/admin/tenants");
        return;
      } catch (Exception ignored) {}
    }

    try {
      List<Organization> tenants = orgDAO.findAll();
      Map<Long, Integer> userCounts = new HashMap<>();
      Map<Long, Integer> docCounts = new HashMap<>();
      for (Organization t : tenants) {
        userCounts.put(t.getId(), orgDAO.getUserCount(t.getId()));
        docCounts.put(t.getId(), orgDAO.getDocumentCount(t.getId()));
      }

      request.setAttribute("tenants", tenants);
      request.setAttribute("userCounts", userCounts);
      request.setAttribute("docCounts", docCounts);
      request.setAttribute("csrfToken", CsrfUtil.token(request.getSession()));
      request
        .getRequestDispatcher("/WEB-INF/views/admin/tenants.jsp")
        .forward(request, response);
    } catch (Exception e) {
      throw new ServletException("Unable to load tenants", e);
    }
  }

  @Override
  protected void doPost(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws ServletException, IOException {
    if (!CsrfUtil.valid(request)) {
      response.sendError(
        HttpServletResponse.SC_FORBIDDEN,
        "Invalid CSRF token"
      );
      return;
    }

    long adminUid = (Long) request.getSession().getAttribute("userId");
    long currentOrg = (Long) request
      .getSession()
      .getAttribute("organizationId");
    String action = request.getParameter("action");

    try {
      if ("create".equals(action)) {
        String name = ValidationUtil.required(
          request.getParameter("name"),
          "Tenant name"
        );
        String code = ValidationUtil.required(
          request.getParameter("code"),
          "Tenant code"
        );
        String type = ValidationUtil.required(
          request.getParameter("type"),
          "Organization type"
        );

        long newOrgId = orgDAO.create(name, code, type);
        auditDAO.log(
          currentOrg,
          adminUid,
          "TENANT_CREATED",
          "Tenant created: " + name + " (" + code + ")",
          request.getRemoteAddr()
        );
      } else if ("toggle".equals(action)) {
        long targetId = Long.parseLong(request.getParameter("id"));
        boolean active = "1".equals(request.getParameter("active"));
        orgDAO.setActive(targetId, active);
        auditDAO.log(
          currentOrg,
          adminUid,
          "TENANT_UPDATED",
          "Tenant #" + targetId + " active=" + active,
          request.getRemoteAddr()
        );
      }
      response.sendRedirect(request.getContextPath() + "/admin/tenants");
    } catch (Exception e) {
      throw new ServletException("Unable to process tenant operation", e);
    }
  }
}

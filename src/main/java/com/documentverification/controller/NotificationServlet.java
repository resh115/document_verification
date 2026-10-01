package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.dao.NotificationDAO;
import com.documentverification.dao.UserDAO;
import com.documentverification.model.User;
import com.documentverification.util.CsrfUtil;
import com.documentverification.util.ValidationUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

  private final NotificationDAO dao = new NotificationDAO();
  private final UserDAO userDAO = new UserDAO();
  private final AuditDAO auditDAO = new AuditDAO();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      long org = (Long) request.getSession().getAttribute("organizationId");
      long uid = (Long) request.getSession().getAttribute("userId");
      String role = (String) request.getSession().getAttribute("role");

      List<Map<String, Object>> notifications;
      if ("ADMIN".equals(role)) {
        notifications = dao.findAllForAdmin(org);
        List<User> orgUsers = userDAO.findAll(org);
        request.setAttribute("tenantUsers", orgUsers);
      } else {
        notifications = dao.findAll(org, uid);
      }

      String format = request.getParameter("format");
      String accept = request.getHeader("Accept");
      boolean wantsXml =
        "xml".equalsIgnoreCase(format) ||
        "application/xml".equalsIgnoreCase(accept) ||
        (accept != null &&
          accept.contains("application/xml") &&
          !accept.contains("text/html"));

      if (wantsXml) {
        renderXml(response, notifications);
        return;
      }

      request.setAttribute("notifications", notifications);
      request.setAttribute(
        "unreadNotificationCount",
        dao.countUnread(org, uid)
      );
      request.setAttribute("csrfToken", CsrfUtil.token(request.getSession()));
      request
        .getRequestDispatcher("/WEB-INF/views/notifications.jsp")
        .forward(request, response);
    } catch (Exception e) {
      throw new ServletException("Unable to load notifications", e);
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

    try {
      long org = (Long) request.getSession().getAttribute("organizationId");
      long uid = (Long) request.getSession().getAttribute("userId");
      String role = (String) request.getSession().getAttribute("role");
      String action = request.getParameter("action");

      if ("create".equals(action) || "broadcast".equals(action)) {
        if (!"ADMIN".equals(role)) {
          response.sendError(
            HttpServletResponse.SC_FORBIDDEN,
            "Only administrators can send notifications."
          );
          return;
        }

        String message = ValidationUtil.required(
          request.getParameter("message"),
          "Notification message"
        );
        String type = request.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
          type = "ANNOUNCEMENT";
        }
        type = type.trim().toUpperCase();

        String target = request.getParameter("targetUserId");
        if (
          target == null ||
          "all".equalsIgnoreCase(target) ||
          "0".equals(target.trim())
        ) {
          int sentCount = dao.broadcast(org, message, type);
          auditDAO.log(
            org,
            uid,
            "NOTIFICATION_BROADCAST",
            "Admin broadcasted " + type + " to " + sentCount + " users",
            request.getRemoteAddr()
          );
        } else {
          long targetUserId = Long.parseLong(target.trim());
          dao.create(org, targetUserId, null, message, type);
          auditDAO.log(
            org,
            uid,
            "NOTIFICATION_SENT",
            "Admin sent " + type + " to user #" + targetUserId,
            request.getRemoteAddr()
          );
        }

        response.sendRedirect(request.getContextPath() + "/notifications");
        return;
      }

      if ("delete".equals(action)) {
        if ("ADMIN".equals(role)) {
          String id = request.getParameter("id");
          if (id != null && !id.trim().isEmpty()) {
            dao.delete(org, Long.parseLong(id.trim()));
          }
        }
        response.sendRedirect(request.getContextPath() + "/notifications");
        return;
      }

      String id = request.getParameter("id");
      if (id == null || id.trim().isEmpty()) {
        dao.markAllRead(org, uid);
      } else {
        dao.markRead(org, uid, Long.parseLong(id.trim()));
      }
      response.sendRedirect(request.getContextPath() + "/notifications");
    } catch (Exception e) {
      throw new ServletException("Unable to process notification request", e);
    }
  }

  private void renderXml(
    HttpServletResponse response,
    List<Map<String, Object>> notifications
  ) throws IOException {
    response.setContentType("application/xml;charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println("<notifications count=\"" + notifications.size() + "\">");
    for (Map<String, Object> item : notifications) {
      out.println("  <notification>");
      out.println("    <id>" + item.get("id") + "</id>");
      out.println(
        "    <type>" + escapeXml(String.valueOf(item.get("type"))) + "</type>"
      );
      out.println(
        "    <message>" +
          escapeXml(String.valueOf(item.get("message"))) +
          "</message>"
      );
      out.println("    <isRead>" + item.get("read") + "</isRead>");
      out.println("    <createdAt>" + item.get("createdAt") + "</createdAt>");
      if (item.containsKey("userName") && item.get("userName") != null) {
        out.println(
          "    <recipient>" +
            escapeXml(String.valueOf(item.get("userName"))) +
            "</recipient>"
        );
      }
      out.println("  </notification>");
    }
    out.println("</notifications>");
  }

  private String escapeXml(String input) {
    if (input == null) return "";
    return input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;");
  }
}

package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.dao.UserDAO;
import com.documentverification.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  private final UserDAO userDAO = new UserDAO();
  private final AuditDAO auditDAO = new AuditDAO();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    request.getRequestDispatcher("/login.jsp").forward(request, response);
  }

  @Override
  protected void doPost(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws ServletException, IOException {
    String email = request.getParameter("email");
    String password = request.getParameter("password");

    if (
      email == null ||
      password == null ||
      email.trim().isEmpty() ||
      password.trim().isEmpty()
    ) {
      request.setAttribute("error", "Email and password are required.");
      request.getRequestDispatcher("/login.jsp").forward(request, response);
      return;
    }

    try {
      User user = userDAO.authenticate(email.trim(), password);

      if (user == null) {
        request.setAttribute("error", "Invalid credentials.");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
        return;
      }

      HttpSession oldSession = request.getSession(false);

      if (oldSession != null) {
        oldSession.invalidate();
      }

      HttpSession session = request.getSession(true);

      session.setAttribute("userId", user.getId());
      session.setAttribute("organizationId", user.getOrganizationId());
      session.setAttribute("roleId", user.getRoleId());
      session.setAttribute("role", user.getRoleName());
      session.setAttribute("userName", user.getName());

      session.setMaxInactiveInterval(30 * 60);

      auditDAO.log(
        user.getOrganizationId(),
        user.getId(),
        "LOGIN",
        "Successful login",
        request.getRemoteAddr()
      );

      String role = user.getRoleName();

      if ("ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
      } else if (
        "SUBMITTER".equalsIgnoreCase(role) || "STUDENT".equalsIgnoreCase(role)
      ) {
        response.sendRedirect(
          request.getContextPath() + "/submitter/dashboard"
        );
      } else {
        response.sendRedirect(request.getContextPath() + "/approver/dashboard");
      }
    } catch (Exception exception) {
      throw new ServletException("Unable to authenticate user.", exception);
    }
  }
}

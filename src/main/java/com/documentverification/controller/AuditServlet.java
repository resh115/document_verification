package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.util.CsrfUtil;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/audit")
public class AuditServlet extends HttpServlet {

  private final AuditDAO dao = new AuditDAO();

  protected void doGet(HttpServletRequest r, HttpServletResponse s)
    throws ServletException, IOException {
    try {
      long org = (Long) r.getSession().getAttribute("organizationId");
      r.setAttribute("logs", dao.findAll(org));
      r.setAttribute("csrfToken", CsrfUtil.token(r.getSession()));
      r.getRequestDispatcher("/WEB-INF/views/admin/audit.jsp").forward(r, s);
    } catch (Exception e) {
      throw new ServletException("Unable to load audit logs", e);
    }
  }
}

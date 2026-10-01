package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

  protected void doPost(HttpServletRequest r, HttpServletResponse s)
    throws IOException {
    HttpSession x = r.getSession(false);
    if (x != null) {
      Object org = x.getAttribute("organizationId"),
        uid = x.getAttribute("userId");
      try {
        if (org != null && uid != null) new AuditDAO().log(
          (Long) org,
          (Long) uid,
          "LOGOUT",
          "User logged out",
          r.getRemoteAddr()
        );
      } catch (Exception ignored) {}
      x.invalidate();
    }
    s.sendRedirect(r.getContextPath() + "/login");
  }

  protected void doGet(HttpServletRequest r, HttpServletResponse s)
    throws IOException {
    doPost(r, s);
  }
}

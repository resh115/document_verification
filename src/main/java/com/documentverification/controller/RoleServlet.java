package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.dao.RoleDAO;
import com.documentverification.model.Role;
import com.documentverification.util.CsrfUtil;
import com.documentverification.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/roles")
public class RoleServlet extends HttpServlet {

    private final RoleDAO roleDAO = new RoleDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long org = (Long) request.getSession().getAttribute("organizationId");
            List<Role> roles = roleDAO.findAll(org);

            Map<Long, Integer> userCounts = new HashMap<>();
            Map<Long, Integer> stageCounts = new HashMap<>();
            for (Role r : roles) {
                userCounts.put(r.getId(), roleDAO.getUserCount(r.getId()));
                stageCounts.put(r.getId(), roleDAO.getStageCount(r.getId()));
            }

            request.setAttribute("roles", roles);
            request.setAttribute("userCounts", userCounts);
            request.setAttribute("stageCounts", stageCounts);
            request.setAttribute("csrfToken", CsrfUtil.token(request.getSession()));
            request.getRequestDispatcher("/WEB-INF/views/admin/roles.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load roles", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.valid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        long org = (Long) request.getSession().getAttribute("organizationId");
        long uid = (Long) request.getSession().getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                String name = ValidationUtil.required(request.getParameter("name"), "Role name");
                String description = request.getParameter("description");
                long roleId = roleDAO.create(org, name, description);
                auditDAO.log(org, uid, "ROLE_CREATED",
                        "Created custom role '" + name.toUpperCase() + "' (ID: " + roleId + ")", request.getRemoteAddr());
            } else if ("delete".equals(action)) {
                long roleId = Long.parseLong(request.getParameter("id"));
                Role r = roleDAO.findById(roleId);
                String roleName = r != null ? r.getName() : String.valueOf(roleId);
                roleDAO.delete(org, roleId);
                auditDAO.log(org, uid, "ROLE_DELETED",
                        "Deleted role '" + roleName + "' (ID: " + roleId + ")", request.getRemoteAddr());
            }

            response.sendRedirect(request.getContextPath() + "/admin/roles");
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("roleError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/roles");
        } catch (Exception e) {
            throw new ServletException("Unable to process role request", e);
        }
    }
}

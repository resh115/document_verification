package com.documentverification.controller;

import com.documentverification.dao.AuditDAO;
import com.documentverification.dao.OrganizationDAO;
import com.documentverification.dao.UserDAO;
import com.documentverification.model.Organization;
import com.documentverification.model.User;
import com.documentverification.util.CsrfUtil;
import com.documentverification.util.PasswordUtil;
import com.documentverification.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class UserServlet extends HttpServlet {

    private final UserDAO dao = new UserDAO();
    private final OrganizationDAO orgDao = new OrganizationDAO();
    private final AuditDAO audit = new AuditDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            long org = (Long) r.getSession().getAttribute("organizationId");
            String tenantParam = r.getParameter("tenantId");
            long selectedOrg = (tenantParam != null && !tenantParam.trim().isEmpty())
                    ? Long.parseLong(tenantParam)
                    : org;

            List<User> userList = (tenantParam != null && !tenantParam.trim().isEmpty())
                    ? dao.findByOrganization(selectedOrg)
                    : dao.findAllUsers();

            List<Organization> organizations = orgDao.findAll();

            r.setAttribute("users", userList);
            r.setAttribute("organizations", organizations);
            r.setAttribute("selectedOrg", selectedOrg);
            r.setAttribute("roles", dao.findRoles(selectedOrg));
            r.setAttribute("csrfToken", CsrfUtil.token(r.getSession()));

            r.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(r, s);
        } catch (Exception e) {
            throw new ServletException("Unable to load user management", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        if (!CsrfUtil.valid(r)) {
            s.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        long sessionOrg = (Long) r.getSession().getAttribute("organizationId");
        long uid = (Long) r.getSession().getAttribute("userId");

        try {
            String action = r.getParameter("action");
            long id = r.getParameter("id") == null ? 0 : Long.parseLong(r.getParameter("id"));

            long targetOrg = sessionOrg;
            String orgParam = r.getParameter("organizationId");
            if (orgParam != null && !orgParam.trim().isEmpty()) {
                targetOrg = Long.parseLong(orgParam);
            }

            if ("create".equals(action)) {
                User u = new User();
                u.setOrganizationId(targetOrg);

                long finalRoleId = 0;
                String customRole = r.getParameter("customRoleName");
                com.documentverification.dao.RoleDAO roleDao = new com.documentverification.dao.RoleDAO();
                if (customRole != null && !customRole.trim().isEmpty()) {
                    com.documentverification.model.Role existingRole = roleDao.findByName(targetOrg, customRole.trim());
                    if (existingRole != null) {
                        finalRoleId = existingRole.getId();
                    } else {
                        finalRoleId = roleDao.create(targetOrg, customRole.trim().toUpperCase(), "Role: " + customRole.trim());
                    }
                } else if (r.getParameter("roleId") != null && !r.getParameter("roleId").trim().isEmpty()) {
                    finalRoleId = Long.parseLong(r.getParameter("roleId"));
                }
                if (finalRoleId == 0) {
                    com.documentverification.model.Role defaultRole = roleDao.findByName(targetOrg, "STAFF");
                    finalRoleId = defaultRole != null ? defaultRole.getId() : 2;
                }
                u.setRoleId(finalRoleId);
                u.setName(ValidationUtil.required(r.getParameter("name"), "Name"));
                u.setEmail(ValidationUtil.email(r.getParameter("email")));
                u.setPasswordHash(PasswordUtil.hash(ValidationUtil.required(r.getParameter("password"), "Password")));
                dao.create(u);
                audit.log(targetOrg, uid, "USER_CREATED", "User created: " + u.getEmail() + " in Org #" + targetOrg, r.getRemoteAddr());
            } else if ("update".equals(action)) {
                long finalRoleId = Long.parseLong(r.getParameter("roleId"));
                String customRole = r.getParameter("customRoleName");
                com.documentverification.dao.RoleDAO roleDao = new com.documentverification.dao.RoleDAO();
                if (customRole != null && !customRole.trim().isEmpty()) {
                    com.documentverification.model.Role existingRole = roleDao.findByName(targetOrg, customRole.trim());
                    finalRoleId = existingRole != null ? existingRole.getId()
                            : roleDao.create(targetOrg, customRole.trim().toUpperCase(), "Role: " + customRole.trim());
                }
                dao.update(targetOrg, id, finalRoleId,
                        ValidationUtil.required(r.getParameter("name"), "Name"),
                        ValidationUtil.email(r.getParameter("email")));
                audit.log(targetOrg, uid, "USER_UPDATED", "User #" + id + " updated", r.getRemoteAddr());
            } else if ("toggle".equals(action)) {
                if (id == uid) {
                    throw new IllegalArgumentException("You cannot deactivate your own account.");
                }
                dao.setActive(id, targetOrg, "1".equals(r.getParameter("active")));
                audit.log(targetOrg, uid, "USER_CHANGED", "User #" + id + " status toggled", r.getRemoteAddr());
            }

            s.sendRedirect(r.getContextPath() + "/admin/users" + (targetOrg != sessionOrg ? "?tenantId=" + targetOrg : ""));
        } catch (Exception e) {
            throw new ServletException("Unable to process user action", e);
        }
    }
}

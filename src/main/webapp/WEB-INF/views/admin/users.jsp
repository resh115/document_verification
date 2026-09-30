<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.documentverification.model.User" %>
<%@ page import="com.documentverification.model.Organization" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    List<Organization> organizations = (List<Organization>) request.getAttribute("organizations");
    List<Map<String, Object>> roles = (List<Map<String, Object>>) request.getAttribute("roles");
    Long selectedOrg = (Long) request.getAttribute("selectedOrg");
    Long sessionOrgId = (Long) session.getAttribute("organizationId");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management | VerityFlow</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
</head>
<body>
<div class="app-bg"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>

<div class="app-shell">
    <aside class="sidebar">
        <a class="brand" href="<%= request.getContextPath() %>/admin/dashboard">
            <span class="brand-mark small">V</span>
            <span>VerityFlow</span>
        </a>
        <nav>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/dashboard">Overview</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/tenants">Tenants</a>
            <a class="nav-link active" href="<%= request.getContextPath() %>/admin/users">Users</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/workflows">Workflows</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/document-types">Document Types</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/audit">Audit Logs</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/notifications">Notifications</a>
        </nav>
        <div class="sidebar-bottom">
            <a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a>
        </div>
    </aside>

    <main class="content">
        <header class="topbar">
            <div>
                <span class="eyebrow">MULTI-TENANT USER DIRECTORY</span>
                <h1>Users & Access</h1>
                <p class="muted">Provision users directly for their respective organization and role.</p>
            </div>
            <div class="profile-chip">
                <span class="avatar">A</span>
                <span><%= session.getAttribute("userName") %></span>
            </div>
        </header>

        <section class="dashboard-grid">
            <article class="panel">
                <div class="panel-heading">
                    <div>
                        <span class="eyebrow">CREATE USER</span>
                        <h2>Add Team Member</h2>
                    </div>
                </div>

                <form method="post" action="<%= request.getContextPath() %>/admin/users">
                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                    <input type="hidden" name="action" value="create">

                    <label>Tenant / Organization</label>
                    <select name="organizationId" required>
                        <% if (organizations != null) {
                            for (Organization org : organizations) { %>
                                <option value="<%= org.getId() %>" <%= org.getId() == selectedOrg ? "selected" : "" %>>
                                    <%= org.getName() %> (<%= org.getCode() %>)
                                </option>
                        <%  }
                        } %>
                    </select>

                    <label>Full Name</label>
                    <input name="name" placeholder="Alex Morgan" required>

                    <label>Work Email</label>
                    <input name="email" type="email" placeholder="alex@company.com" required>

                    <label for="temp-user-password">Temporary Password</label>
                    <div class="password-input-wrap">
                        <input id="temp-user-password" name="password" type="password" minlength="6" placeholder="Enter password" required>
                        <button type="button" class="password-toggle-btn" aria-label="Show password" title="Show password" onclick="togglePasswordVisibility('temp-user-password', this)">
                            <svg class="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                        </button>
                    </div>

                    <label>Role / Position</label>
                    <select name="roleId" id="user-role-select">
                        <% if (roles != null) {
                            for (Map<String, Object> role : roles) {
                                String rDesc = (String) role.get("description");
                        %>
                                <option value="<%= role.get("id") %>"><%= role.get("name") %><%= (rDesc != null && !rDesc.trim().isEmpty()) ? (" — " + rDesc) : "" %></option>
                        <%  }
                        } %>
                        <option value="CUSTOM">+ Type Custom Role / Job Title</option>
                    </select>

                    <div id="custom-role-input-wrap" style="display: none; margin-top: 6px; padding: 10px; background: rgba(16, 185, 129, 0.08); border-radius: 8px; border: 1px dashed rgba(16, 185, 129, 0.35);">
                        <label style="font-size: 12px; color: #6ee7b7; font-weight: 700;">Custom Role / Title Name</label>
                        <input name="customRoleName" id="custom-role-name-input" placeholder="e.g. Professor, HOD, Dean, Registrar, Student">
                        <small class="muted" style="display:block; margin-top:3px;">Admin can assign any title according to institutional requirements.</small>
                    </div>

                    <button class="shiny-cta btn-wide" type="submit" style="margin-top: 18px;">Create User</button>
                </form>
            </article>

            <article class="panel panel-large">
                <div class="panel-heading">
                    <div>
                        <span class="eyebrow">DIRECTORY</span>
                        <h2>Organization Users</h2>
                    </div>
                    <span class="badge"><%= users != null ? users.size() : 0 %> Users</span>
                </div>

                <div class="table-search-wrap">
                    <div class="table-search-input-box">
                        <svg class="table-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <input id="user-search-input" class="table-search-input" placeholder="Filter users by name, email, or role...">
                    </div>
                    <span id="user-count-badge" class="search-count-badge"><%= users != null ? users.size() : 0 %> records</span>
                </div>

                <div class="table-wrap">
                    <table id="users-table">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Tenant</th>
                            <th>Role</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% if (users != null) {
                            for (User user : users) { %>
                        <tr>
                            <td><strong><%= user.getName() %></strong></td>
                            <td>
                                <%= user.getEmail() %>
                                <button type="button" class="copy-chip" title="Copy email address" onclick="copyToClipboard('<%= user.getEmail() %>', this)">Copy</button>
                            </td>
                            <td><span class="badge"><%= user.getOrganizationName() != null ? user.getOrganizationName() : ("Org #" + user.getOrganizationId()) %></span></td>
                            <td><span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #34d399; border-color: rgba(16, 185, 129, 0.3);"><%= user.getRoleName() %></span></td>
                            <td>
                                <span class="status <%= user.isActive() ? "status-approved" : "status-rejected" %>">
                                    <span class="pulse-dot <%= user.isActive() ? "green" : "red" %>"></span>
                                    <%= user.isActive() ? "Active" : "Inactive" %>
                                </span>
                            </td>
                        </tr>
                        <%  }
                        } %>
                        </tbody>
                    </table>
                </div>
            </article>
        </section>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", () => {
        initTableFilter("user-search-input", "#users-table", "user-count-badge");
        const roleSel = document.getElementById("user-role-select");
        const customBox = document.getElementById("custom-role-input-wrap");
        if (roleSel && customBox) {
            roleSel.addEventListener("change", () => {
                customBox.style.display = roleSel.value === "CUSTOM" ? "block" : "none";
                if (roleSel.value === "CUSTOM") {
                    document.getElementById("custom-role-name-input").focus();
                }
            });
        }
    });
</script>
</body>
</html>

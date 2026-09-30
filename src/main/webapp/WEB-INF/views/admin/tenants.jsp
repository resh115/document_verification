<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.documentverification.model.Organization" %>
<%
    List<Organization> tenants = (List<Organization>) request.getAttribute("tenants");
    Map<Long, Integer> userCounts = (Map<Long, Integer>) request.getAttribute("userCounts");
    Long activeOrgId = (Long) session.getAttribute("organizationId");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tenants & Multi-Tenancy | VerityFlow</title>
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
            <a class="nav-link active" href="<%= request.getContextPath() %>/admin/tenants">Tenants</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/users">Users</a>
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
                <span class="eyebrow">MULTI-TENANT MANAGEMENT</span>
                <h1>Tenants & Organizations</h1>
                <p class="muted">Provision organizations and manage their respective tenant resources.</p>
            </div>
            <div class="profile-chip">
                <span class="avatar">A</span>
                <span>Active Tenant #<%= activeOrgId %></span>
            </div>
        </header>

        <section class="dashboard-grid">
            <article class="panel">
                <div class="panel-heading">
                    <div>
                        <span class="eyebrow">PROVISION</span>
                        <h2>Add New Tenant</h2>
                    </div>
                </div>

                <form method="post" action="<%= request.getContextPath() %>/admin/tenants">
                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                    <input type="hidden" name="action" value="create">

                    <label>Organization Name</label>
                    <input name="name" placeholder="Acme Global Corporation" required>

                    <label>Tenant Code (Unique)</label>
                    <input name="code" placeholder="ACME-CORP" style="text-transform: uppercase;" required>

                    <label>Organization Type</label>
                    <select name="type" required>
                        <option value="Enterprise">Enterprise</option>
                        <option value="Healthcare">Healthcare</option>
                        <option value="Financial Services">Financial Services</option>
                        <option value="Government">Government</option>
                        <option value="Education">Education</option>
                        <option value="Technology">Technology</option>
                    </select>

                    <button class="shiny-cta btn-wide" type="submit">Create Tenant</button>
                </form>
            </article>

            <article class="panel panel-large">
                <div class="panel-heading">
                    <div>
                        <span class="eyebrow">TENANT REGISTRY</span>
                        <h2>Configured Organizations</h2>
                    </div>
                    <span class="badge"><%= tenants.size() %> Tenants</span>
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th>Tenant</th>
                            <th>Code</th>
                            <th>Type</th>
                            <th>Users</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (Organization tenant : tenants) {
                            boolean isCurrent = activeOrgId != null && activeOrgId.equals(tenant.getId());
                        %>
                        <tr style="<%= isCurrent ? "background: rgba(16, 185, 129, 0.12);" : "" %>">
                            <td>
                                <strong><%= tenant.getName() %></strong>
                                <% if (isCurrent) { %>
                                    <span class="status status-approved" style="margin-left: 8px; font-size: 11px;">Current</span>
                                <% } %>
                            </td>
                            <td><code><%= tenant.getCode() %></code></td>
                            <td><span class="badge"><%= tenant.getType() %></span></td>
                            <td><%= userCounts != null && userCounts.containsKey(tenant.getId()) ? userCounts.get(tenant.getId()) : 0 %></td>
                            <td>
                                <span class="status <%= tenant.isActive() ? "status-approved" : "status-rejected" %>">
                                    <%= tenant.isActive() ? "Active" : "Inactive" %>
                                </span>
                            </td>
                            <td style="white-space: nowrap;">
                                <% if (!isCurrent) { %>
                                    <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/tenants?switchOrg=<%= tenant.getId() %>">Switch</a>
                                <% } %>
                                <form method="post" action="<%= request.getContextPath() %>/admin/tenants" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                    <input type="hidden" name="action" value="toggle">
                                    <input type="hidden" name="id" value="<%= tenant.getId() %>">
                                    <input type="hidden" name="active" value="<%= tenant.isActive() ? "0" : "1" %>">
                                    <button class="ghost-btn" type="submit">
                                        <%= tenant.isActive() ? "Deactivate" : "Activate" %>
                                    </button>
                                </form>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </article>
        </section>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>

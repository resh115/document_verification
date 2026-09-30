<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentverification.model.Organization" %>
<%
    List<Organization> tenants = (List<Organization>) request.getAttribute("tenants");
    Organization currentTenant = (Organization) request.getAttribute("currentTenant");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | VerityFlow</title>
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
            <a class="nav-link active" href="<%= request.getContextPath() %>/admin/dashboard">Overview</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/admin/tenants">Tenants</a>
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
                <span class="eyebrow">ADMIN CONSOLE &bull; MULTI-TENANT</span>
                <h1>Good to see you, <%= session.getAttribute("userName") %>.</h1>
                <p class="muted">
                    Active Tenant: 
                    <strong><%= session.getAttribute("organizationName") != null ? session.getAttribute("organizationName") : "Default Organization" %></strong>
                    (<%= session.getAttribute("organizationCode") != null ? session.getAttribute("organizationCode") : "DEF" %>)
                </p>
            </div>

            <div style="display: flex; gap: 12px; align-items: center;">
                <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/tenants">Switch Organization</a>
                <div class="profile-chip">
                    <span class="avatar">A</span>
                    <span><%= session.getAttribute("role") %></span>
                </div>
            </div>
        </header>

        <!-- Admin Quick Action Center -->
        <div style="display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap;">
            <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/workflows">+ Build Custom Workflow</a>
            <a class="ghost-btn compact" href="<%= request.getContextPath() %>/admin/users">+ Add User</a>
            <a class="ghost-btn compact" href="<%= request.getContextPath() %>/notifications">📢 Broadcast Alert</a>
            <a class="ghost-btn compact" href="<%= request.getContextPath() %>/admin/audit">📜 Audit Logs</a>
        </div>

        <section class="metric-grid">
            <article class="metric-card">
                <span>Total Tenants</span>
                <strong>${stats.tenants}</strong>
                <small>Multi-tenant organizations</small>
            </article>

            <article class="metric-card">
                <span>Tenant Users</span>
                <strong>${stats.users}</strong>
                <small>In current tenant</small>
            </article>

            <article class="metric-card">
                <span>Workflows Configured</span>
                <strong>${stats.workflows}</strong>
                <small>Approval chain templates</small>
            </article>

            <article class="metric-card">
                <span>Documents</span>
                <strong>${stats.documents}</strong>
                <small>Documents in current organization</small>
            </article>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">ORGANIZATIONS</span>
                    <h2>Multi-Tenant Directory</h2>
                </div>
                <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/tenants">+ New Tenant</a>
            </div>

            <div class="table-search-wrap">
                <div class="table-search-input-box">
                    <svg class="table-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                    <input id="tenant-search-input" class="table-search-input" placeholder="Search tenants by name, code, or type...">
                </div>
                <span id="tenant-count-badge" class="search-count-badge"><%= tenants != null ? tenants.size() : 0 %> organizations</span>
            </div>

            <div class="table-wrap">
                <table id="tenants-table">
                    <thead>
                        <tr>
                            <th>Tenant Name</th>
                            <th>Code</th>
                            <th>Industry / Type</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (tenants != null && !tenants.isEmpty()) { %>
                        <% for (Organization org : tenants) { %>
                        <tr style="<%= (session.getAttribute("organizationId") != null && session.getAttribute("organizationId").equals(org.getId())) ? "background: rgba(16, 185, 129, 0.12);" : "" %>">
                            <td>
                                <strong><%= org.getName() %></strong>
                                <% if (session.getAttribute("organizationId") != null && session.getAttribute("organizationId").equals(org.getId())) { %>
                                    <span class="badge" style="background: rgba(16, 185, 129, 0.25); color: #34d399; border: 1px solid #10b981; margin-left: 6px;">CURRENT</span>
                                <% } %>
                            </td>
                            <td><code><%= org.getCode() %></code></td>
                            <td><%= org.getType() != null ? org.getType() : "Standard" %></td>
                            <td>
                                <% if (org.isActive()) { %>
                                    <span class="status status-approved">
                                        <span class="pulse-dot green"></span>
                                        Active
                                    </span>
                                <% } else { %>
                                    <span class="status status-rejected">
                                        <span class="pulse-dot red"></span>
                                        Suspended
                                    </span>
                                <% } %>
                            </td>
                            <td>
                                <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/tenants?switchOrg=<%= org.getId() %>">
                                    <%= (session.getAttribute("organizationId") != null && session.getAttribute("organizationId").equals(org.getId())) ? "Selected" : "Switch To" %>
                                </a>
                            </td>
                        </tr>
                        <% } %>
                    <% } else { %>
                        <tr><td colspan="5">No tenants provisioned yet.</td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>

    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", () => {
        initTableFilter("tenant-search-input", "#tenants-table", "tenant-count-badge");
    });
</script>
</body>
</html>

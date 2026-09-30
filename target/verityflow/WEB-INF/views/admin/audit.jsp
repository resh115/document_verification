<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<% List<Map<String,Object>> logs = (List<Map<String,Object>>) request.getAttribute("logs"); %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Audit Logs | VerityFlow</title>
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
                    <a class="nav-link" href="<%= request.getContextPath() %>/admin/users">Users</a>
                    <a class="nav-link" href="<%= request.getContextPath() %>/admin/workflows">Workflows</a>
                            <a class="nav-link" href="<%= request.getContextPath() %>/admin/document-types">Document Types</a>
                    <a class="nav-link active" href="<%= request.getContextPath() %>/admin/audit">Audit Logs</a>
                    <a class="nav-link" href="<%= request.getContextPath() %>/notifications">Notifications</a>
                </nav>
                <div class="sidebar-bottom">
                    <a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a>
                </div>
            </aside>
            <main class="content">
                <header class="topbar">
                    <div>
                        <span class="eyebrow">SECURITY HISTORY</span>
                        <h1>Audit logs</h1>
                        <p class="muted">Review activity across your organization.</p>
                    </div>
                </header>
                <section class="panel">
                    <div class="panel-heading">
                        <div>
                            <span class="eyebrow">AUDIT TRAIL</span>
                            <h2>Recent Activity</h2>
                        </div>
                        <span id="audit-count-badge" class="badge"><%= logs.size() %> events</span>
                    </div>

                    <div style="margin: 18px 0 22px 0; display: flex; flex-direction: column; gap: 14px;">
                        <div class="table-search-wrap" style="margin-bottom: 0;">
                            <div class="table-search-input-box" style="width: 100%;">
                                <svg class="table-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="11" cy="11" r="8"></circle>
                                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                                </svg>
                                <input id="audit-search-input" class="table-search-input" placeholder="Search audit events, users, IP addresses, or descriptions...">
                            </div>
                        </div>

                        <div class="filter-pills-wrap" style="margin-bottom: 0; display: flex; gap: 10px; flex-wrap: wrap;">
                            <button type="button" class="filter-pill active" onclick="filterAuditEvents('ALL', this)">All Events (<%= logs.size() %>)</button>
                            <button type="button" class="filter-pill" onclick="filterAuditEvents('LOGIN', this)">🔑 Logins</button>
                            <button type="button" class="filter-pill" onclick="filterAuditEvents('USER', this)">👤 User Actions</button>
                            <button type="button" class="filter-pill" onclick="filterAuditEvents('DOCUMENT', this)">📄 Documents</button>
                            <button type="button" class="filter-pill" onclick="filterAuditEvents('WORKFLOW', this)">🔄 Workflows</button>
                        </div>
                    </div>

                    <div class="table-wrap">
                        <table id="audit-table">
                            <thead>
                                <tr>
                                    <th>Time</th>
                                    <th>Event</th>
                                    <th>User</th>
                                    <th>Description</th>
                                    <th>IP address</th>
                                </tr>
                            </thead>
                            <tbody><% for (Map<String,Object> log : logs) { %><tr data-event-type="<%= log.get("eventType") %>">
                                    <td><%= log.get("createdAt") %></td>
                                    <td>
                                        <span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #34d399;"><%= log.get("eventType") %></span>
                                    </td>
                                    <td><strong><%= log.get("userName") == null ? "System" : log.get("userName") %></strong></td>
                                    <td><%= log.get("description") %></td>
                                    <td><code><%= log.get("ip") == null ? "" : log.get("ip") %></code></td>
                                </tr><% } %></tbody>
                            </table>
                        </div>
                    </section>
                </main>
            </div>
        <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", () => {
                initAuditFilters("audit-search-input", "#audit-table", "#audit-count-badge");
            });
        </script>
        </body>
    </html>

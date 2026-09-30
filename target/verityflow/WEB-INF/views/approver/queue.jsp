<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentverification.model.Document" %>
<% List<Document> queue = (List<Document>) request.getAttribute("queue"); %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Approval Queue | VerityFlow</title>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
    </head>
    <body>
        <div class="app-bg"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>
        <div class="app-shell">
            <aside class="sidebar">
                <a class="brand" href="<%= request.getContextPath() %><%= "ADMIN".equals(session.getAttribute("role")) ? "/admin/dashboard" : "/approver/dashboard" %>">
                    <span class="brand-mark small">V</span>
                    <span>VerityFlow</span>
                </a>
                <nav>
                    <% if ("ADMIN".equals(session.getAttribute("role"))) { %>
                        <a class="nav-link" href="<%= request.getContextPath() %>/admin/dashboard">Admin dashboard</a>
                    <% } else { %>
                        <a class="nav-link" href="<%= request.getContextPath() %>/approver/dashboard">Approver dashboard</a>
                    <% } %>
                    <a class="nav-link active" href="<%= request.getContextPath() %>/approval">Approval queue</a>
                    <a class="nav-link" href="<%= request.getContextPath() %>/notifications"><span class="nav-label">Notifications</span><% if (request.getAttribute("unreadNotificationCount") != null && (Integer) request.getAttribute("unreadNotificationCount") > 0) { %><span class="notification-badge" data-notification-count><%= request.getAttribute("unreadNotificationCount") %></span><% } %></a>
                </nav>
                <div class="sidebar-bottom">
                    <a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a>
                </div>
            </aside>
            <main class="content">
                <header class="topbar">
                    <div>
                        <span class="eyebrow">APPROVAL WORKSPACE</span>
                        <h1>Approval queue</h1>
                        <p class="muted">Documents assigned to your current approval stage.</p>
                    </div>
                </header>
                <section class="panel">
                    <div class="panel-heading">
                        <h2>Waiting for review</h2>
                        <span class="badge"><%= queue.size() %> documents</span>
                    </div>
                    <div class="table-search-wrap">
                        <div class="table-search-input-box">
                            <svg class="table-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                            <input id="queue-search-input" class="table-search-input" placeholder="Search queue by document title, ID, or stage...">
                        </div>
                        <span id="queue-count-badge" class="search-count-badge"><%= queue.size() %> items</span>
                    </div>
                    <div class="table-wrap">
                        <table id="queue-table">
                            <thead>
                                <tr>
                                    <th>Document</th>
                                    <th>Status</th>
                                    <th>Stage</th>
                                    <th>Version</th>
                                    <th>Updated</th>
                                    <th>
                                    </th>
                                </tr>
                            </thead>
                            <tbody><% if (queue.isEmpty()) { %><tr><td colspan="6"><div class="empty-state"><p>No documents are waiting for your approval.</p><a class="shiny-cta compact" href="<%= request.getContextPath() %>/approver/dashboard">Back to dashboard</a></div></td></tr><% } %><% for (Document document : queue) { %><tr>
                                    <td>
                                        <strong><%= document.getTitle() %></strong>
                                        <br>
                                        <small style="color:var(--accent);">#<%= document.getId() %></small>
                                    </td>
                                    <td>
                                        <span class="status status-pending">
                                            <span class="pulse-dot amber"></span>
                                            <%= document.getStatus() %>
                                        </span>
                                    </td>
                                    <td><span class="badge" style="background: rgba(16, 185, 129, 0.15); color: #34d399;">Level <%= document.getCurrentStageOrder() %></span></td>
                                    <td>V<%= document.getCurrentVersion() %></td>
                                    <td><%= document.getUpdatedAt() %></td>
                                    <td>
                                        <a class="shiny-cta compact" href="<%= request.getContextPath() %>/approval?action=view&id=<%= document.getId() %>">Review</a>
                                    </td>
                                </tr><% } %></tbody>
                            </table>
                        </div>
                    </section>
                </main>
            </div>
        <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", () => {
                initTableFilter("queue-search-input", "#queue-table", "queue-count-badge");
            });
        </script>
        </body>
    </html>

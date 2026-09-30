<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentverification.model.Document" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Workspace | VerityFlow</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
</head>
<body>
<div class="app-bg"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>

<div class="app-shell">
    <aside class="sidebar">
        <a class="brand" href="<%= request.getContextPath() %>/submitter/dashboard">
            <span class="brand-mark small">V</span>
            <span>VerityFlow</span>
        </a>
        <nav>
            <a class="nav-link active" href="<%= request.getContextPath() %>/submitter/dashboard">Workspace</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/documents">My documents</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/notifications"><span class="nav-label">Notifications</span><% if (request.getAttribute("unreadNotificationCount") != null && (Integer) request.getAttribute("unreadNotificationCount") > 0) { %><span class="notification-badge" data-notification-count><%= request.getAttribute("unreadNotificationCount") %></span><% } %></a>
        </nav>
        <div class="sidebar-bottom">
            <a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a>
        </div>
    </aside>

    <main class="content">
        <header class="topbar">
            <div>
                <span class="eyebrow">SUBMITTER WORKSPACE</span>
                <h1>My documents</h1>
                <p class="muted">Track every submission and its approval stage.</p>
            </div>
            <a class="shiny-cta" href="<%= request.getContextPath() %>/documents">View documents →</a>
        </header>

        <section class="metric-grid">
            <article class="metric-card"><span>In review</span><strong>${stats.pending}</strong><small>Currently moving through workflow</small></article>
            <article class="metric-card"><span>Changes required</span><strong>${stats.changes}</strong><small>Needs a revised version</small></article>
            <article class="metric-card"><span>Approved</span><strong>${stats.approved}</strong><small>Successfully completed</small></article>
            <article class="metric-card"><span>Rejected</span><strong>${stats.rejected}</strong><small>Workflow ended</small></article>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">COMPLETED</span>
                    <h2>Approved documents</h2>
                </div>
                <span class="badge"><%= ((List<Document>) request.getAttribute("approvedDocuments")).size() %> total</span>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Document</th><th>Status</th><th>Version</th><th>Updated</th><th></th></tr></thead>
                    <tbody>
                    <% for (Document document : (List<Document>) request.getAttribute("approvedDocuments")) { %>
                    <tr>
                        <td><strong><%= document.getTitle() %></strong><br><small>#<%= document.getId() %></small></td>
                        <td><span class="status status-approved">Approved</span></td>
                        <td>V<%= document.getCurrentVersion() %></td>
                        <td><%= document.getUpdatedAt() %></td>
                        <td><a class="shiny-cta compact" href="<%= request.getContextPath() %>/documents?action=view&id=<%= document.getId() %>">View</a></td>
                    </tr>
                    <% } %>
                    <% if (((List<Document>) request.getAttribute("approvedDocuments")).isEmpty()) { %><tr><td colspan="5">No approved documents yet.</td></tr><% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">RECENT ACTIVITY</span>
                    <h2>Latest document movement</h2>
                </div>
            </div>
            <div class="empty-state">
                <p>Open My documents to view the latest submission and approval history from the database.</p>
            </div>
        </section>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/notifications.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>

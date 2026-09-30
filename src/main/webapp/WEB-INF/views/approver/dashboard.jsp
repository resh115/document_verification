<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Approvals | VerityFlow</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
</head>
<body>
<div class="app-bg"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>

<div class="app-shell">
    <aside class="sidebar">
        <a class="brand" href="<%= request.getContextPath() %>/approver/dashboard">
            <span class="brand-mark small">V</span><span>VerityFlow</span>
        </a>
        <nav>
            <a class="nav-link active" href="<%= request.getContextPath() %>/approval">Approval queue</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/approval?action=history">History</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/notifications"><span class="nav-label">Notifications</span><% if (request.getAttribute("unreadNotificationCount") != null && (Integer) request.getAttribute("unreadNotificationCount") > 0) { %><span class="notification-badge" data-notification-count><%= request.getAttribute("unreadNotificationCount") %></span><% } %></a>
        </nav>
        <div class="sidebar-bottom"><a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a></div>
    </aside>

    <main class="content">
        <header class="topbar">
            <div>
                <span class="eyebrow">APPROVER WORKSPACE</span>
                <h1>Approval queue</h1>
                <p class="muted">Review assigned documents and move the workflow forward.</p>
            </div>
            <div class="profile-chip"><span class="avatar">A</span><span><%= session.getAttribute("role") %></span></div>
        </header>

        <section class="metric-grid">
            <article class="metric-card"><span>Waiting for me</span><strong>${stats.queue}</strong><small>Assigned at your current stage</small></article>
            <article class="metric-card"><span>Rejected by me</span><strong>${stats.rejected}</strong><small>Workflow actions recorded</small></article>
            <article class="metric-card"><span>Approved</span><strong>${stats.approved}</strong><small>This month</small></article>
            <article class="metric-card"><span>Changes requested</span><strong>${stats.changes}</strong><small>Returned to submitters</small></article>
        </section>

        <section class="panel">
            <div class="panel-heading"><div><span class="eyebrow">PENDING</span><h2>Documents waiting for review</h2></div></div>
            <div class="empty-state">
                <p>Your current approval queue is available through the Approval Queue action. Items are loaded from the database for your organization and assigned stage.</p>
                <a class="shiny-cta" href="<%= request.getContextPath() %>/approval">Open approval queue</a>
            </div>
        </section>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>

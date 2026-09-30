<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.documentverification.model.User" %>
<%
    List<Map<String, Object>> notifications = (List<Map<String, Object>>) request.getAttribute("notifications");
    List<User> tenantUsers = (List<User>) request.getAttribute("tenantUsers");
    String role = (String) session.getAttribute("role");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications &amp; Announcements | VerityFlow</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
</head>
<body>
<div class="app-bg"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>

<div class="app-shell">
    <aside class="sidebar">
        <a class="brand" href="<%= request.getContextPath() %>/">
            <span class="brand-mark small">V</span>
            <span>VerityFlow</span>
        </a>
        <nav>
            <% if ("ADMIN".equals(role)) { %>
                <a class="nav-link" href="<%= request.getContextPath() %>/admin/dashboard">Overview</a>
                <a class="nav-link" href="<%= request.getContextPath() %>/admin/tenants">Tenants</a>
                <a class="nav-link" href="<%= request.getContextPath() %>/admin/users">Users</a>
                <a class="nav-link" href="<%= request.getContextPath() %>/admin/workflows">Workflows</a>
                    <a class="nav-link" href="<%= request.getContextPath() %>/admin/document-types">Document Types</a>
                <a class="nav-link" href="<%= request.getContextPath() %>/admin/audit">Audit Logs</a>
            <% } else if (!"SUBMITTER".equalsIgnoreCase(role)) { %>
                <a class="nav-link" href="<%= request.getContextPath() %>/approver/dashboard">Approver dashboard</a>
                <a class="nav-link" href="<%= request.getContextPath() %>/approval">Approval queue</a>
            <% } else { %>
                <a class="nav-link" href="<%= request.getContextPath() %>/submitter/dashboard">Workspace</a>
                <a class="nav-link" href="<%= request.getContextPath() %>/documents">My documents</a>
            <% } %>
            <a class="nav-link active" href="<%= request.getContextPath() %>/notifications">
                <span class="nav-label">Notifications</span>
                <% if (request.getAttribute("unreadNotificationCount") != null && (Integer) request.getAttribute("unreadNotificationCount") > 0) { %>
                    <span class="notification-badge" data-notification-count><%= request.getAttribute("unreadNotificationCount") %></span>
                <% } %>
            </a>
        </nav>
        <div class="sidebar-bottom">
            <a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a>
        </div>
    </aside>

    <main class="content">
        <header class="topbar">
            <div>
                <span class="eyebrow">INBOX &bull; <%= "ADMIN".equals(role) ? "TENANT BROADCAST & NOTIFICATIONS" : "SYSTEM ALERTS" %></span>
                <h1>Notifications</h1>
                <p class="muted"><%= "ADMIN".equals(role) ? "Manage and broadcast organizational alerts to tenant users." : "Workflow updates and actions that need your attention." %></p>
            </div>
            <div style="display: flex; gap: 8px; align-items: center;">
                <a class="ghost-btn compact" href="<%= request.getContextPath() %>/notifications?format=xml" target="_blank" title="View XML format">Export XML</a>
                <% if (!"ADMIN".equals(role)) { %>
                    <form method="post" action="<%= request.getContextPath() %>/notifications" style="display:inline">
                        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                        <button class="shiny-cta compact" type="submit">Mark all read</button>
                    </form>
                <% } %>
            </div>
        </header>

        <% if ("ADMIN".equals(role)) { %>
        <section class="panel" style="margin-bottom: 24px;">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">BROADCAST &bull; DISPATCH</span>
                    <h2>Compose New Notification</h2>
                </div>
            </div>

            <form method="post" action="<%= request.getContextPath() %>/notifications" class="form-grid">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <input type="hidden" name="action" value="broadcast">

                <div class="form-group">
                    <label>Target Audience / Recipient</label>
                    <select name="targetUserId" required>
                        <option value="all">📢 Broadcast to All Users in Organization</option>
                        <% if (tenantUsers != null) { %>
                            <optgroup label="Direct to User">
                            <% for (User u : tenantUsers) { %>
                                <option value="<%= u.getId() %>"><%= u.getName() %> (<%= u.getEmail() %> - <%= u.getRoleName() %>)</option>
                            <% } %>
                            </optgroup>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label>Notification Type</label>
                    <select name="type" required>
                        <option value="ANNOUNCEMENT">Announcement (Gold / Amber)</option>
                        <option value="ALERT">Priority Alert (Red)</option>
                        <option value="SYSTEM">System Notice (Cyan)</option>
                        <option value="WORKFLOW">Workflow Info (Green)</option>
                    </select>
                </div>

                <div class="form-group" style="grid-column: 1 / -1;">
                    <label>Message Content</label>
                    <textarea name="message" rows="3" placeholder="Enter notification message or instructions for users..." required style="width: 100%;"></textarea>
                </div>

                <div style="grid-column: 1 / -1;">
                    <button class="shiny-cta" type="submit">Send Notification</button>
                </div>
            </form>
        </section>
        <% } %>

        <section class="panel">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow"><%= "ADMIN".equals(role) ? "ORGANIZATION AUDIT" : "INBOX" %></span>
                    <h2>Notification Stream</h2>
                </div>
                <span class="badge"><%= notifications != null ? notifications.size() : 0 %> messages</span>
            </div>

            <!-- Clean Notification Filter & Search Toolbar -->
            <div class="notification-toolbar" style="margin: 20px 0 24px 0; display: flex; flex-direction: column; gap: 16px;">
                <div class="table-search-wrap" style="margin-bottom: 0;">
                    <div class="table-search-input-box" style="width: 100%;">
                        <svg class="table-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <input id="notif-search-input" class="table-search-input" placeholder="Filter notifications by keyword, recipient, or date...">
                    </div>
                </div>

                <div class="filter-pills-wrap" style="margin-bottom: 0; display: flex; gap: 10px; flex-wrap: wrap;">
                    <button type="button" class="filter-pill active" onclick="filterNotificationCards('ALL', this)">All Notifications (<%= notifications != null ? notifications.size() : 0 %>)</button>
                    <button type="button" class="filter-pill" onclick="filterNotificationCards('ANNOUNCEMENT', this)">📢 Announcements</button>
                    <button type="button" class="filter-pill" onclick="filterNotificationCards('ALERT', this)">⚠️ Priority Alerts</button>
                    <button type="button" class="filter-pill" onclick="filterNotificationCards('WORKFLOW', this)">🔄 Workflow Info</button>
                    <button type="button" class="filter-pill" onclick="filterNotificationCards('STATUS', this)">📋 Status Updates</button>
                    <button type="button" class="filter-pill" onclick="filterNotificationCards('SYSTEM', this)">⚙️ System</button>
                </div>
            </div>

            <div id="notif-cards-container" style="display: flex; flex-direction: column; gap: 16px; padding: 4px 0;">
                <% if (notifications != null && !notifications.isEmpty()) { %>
                    <% for (Map<String, Object> item : notifications) { 
                        String type = (String) item.get("type");
                        boolean isRead = Boolean.TRUE.equals(item.get("read"));
                        String badgeClass = "badge-announcement";
                        if ("ALERT".equalsIgnoreCase(type)) {
                            badgeClass = "badge-alert";
                        } else if ("SYSTEM".equalsIgnoreCase(type)) {
                            badgeClass = "badge-system";
                        } else if ("WORKFLOW".equalsIgnoreCase(type)) {
                            badgeClass = "badge-workflow";
                        } else if ("STATUS".equalsIgnoreCase(type)) {
                            badgeClass = "badge-status";
                        }
                        String typeLower = type != null ? type.toLowerCase() : "info";
                        String iconSymbol = "📢";
                        if ("ALERT".equalsIgnoreCase(type)) {
                            iconSymbol = "⚠️";
                        } else if ("SYSTEM".equalsIgnoreCase(type)) {
                            iconSymbol = "⚙️";
                        } else if ("WORKFLOW".equalsIgnoreCase(type)) {
                            iconSymbol = "🔄";
                        } else if ("STATUS".equalsIgnoreCase(type)) {
                            iconSymbol = "📋";
                        }
                    %>
                    <div class="notification-card <%= isRead ? "read" : "unread" %> type-<%= typeLower %>">
                        <div class="notif-type-icon notif-icon-<%= typeLower %>">
                            <%= iconSymbol %>
                        </div>
                        <div style="flex: 1; min-width: 0;">
                            <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 8px; flex-wrap: wrap;">
                                <span class="badge <%= badgeClass %>"><%= type != null ? type : "INFO" %></span>
                                <% if (!isRead) { %>
                                    <span class="badge" style="background: #ef4444; color: #fff; font-size: 0.65rem; padding: 2px 7px; border: none; font-weight: 700;">NEW</span>
                                <% } %>
                                <span class="muted" style="font-size: 0.8rem;"><%= item.get("createdAt") %></span>
                                <% if ("ADMIN".equals(role) && item.containsKey("userName") && item.get("userName") != null) { %>
                                    <span class="muted" style="font-size: 0.8rem; margin-left: auto;">
                                        Recipient: <strong style="color: var(--text-bright);"><%= item.get("userName") %></strong> (<%= item.get("userEmail") %>)
                                    </span>
                                <% } %>
                            </div>
                            <div class="notification-msg">
                                <%= item.get("message") %>
                            </div>
                        </div>

                        <div style="display: flex; gap: 8px; align-items: center; flex-shrink: 0;">
                            <% if (!isRead && !"ADMIN".equals(role)) { %>
                                <form method="post" action="<%= request.getContextPath() %>/notifications" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                    <input type="hidden" name="id" value="<%= item.get("id") %>">
                                    <button class="shiny-cta compact" type="submit">Mark read</button>
                                </form>
                            <% } %>

                            <% if ("ADMIN".equals(role)) { %>
                                <form method="post" action="<%= request.getContextPath() %>/notifications" style="display:inline;" onsubmit="return confirm('Delete this notification?');">
                                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="<%= item.get("id") %>">
                                    <button class="ghost-btn compact" type="submit" style="color: #fda4af; border: 1px solid rgba(244, 63, 94, 0.4); background: rgba(244, 63, 94, 0.1); font-size: 12px; padding: 5px 10px;">Delete</button>
                                </form>
                            <% } %>
                        </div>
                    </div>
                    <% } %>
                <% } else { %>
                    <div style="text-align: center; padding: 48px; color: var(--muted);">
                        <p style="font-size: 1.1rem; margin-bottom: 8px; font-weight: 600; color: var(--text);">No notifications found</p>
                        <small>Notifications and alerts dispatched to your organization will appear here.</small>
                    </div>
                <% } %>
            </div>
        </section>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", () => {
        const notifInput = document.getElementById("notif-search-input");
        if (notifInput) {
            notifInput.addEventListener("input", () => {
                const query = notifInput.value.trim().toLowerCase();
                const cards = document.querySelectorAll(".notification-card");
                cards.forEach(card => {
                    const text = card.textContent.toLowerCase();
                    card.style.display = (query === "" || text.includes(query)) ? "flex" : "none";
                });
            });
        }
    });
</script>
</body>
</html>

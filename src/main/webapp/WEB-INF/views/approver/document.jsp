<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentverification.model.Document" %>
<%@ page import="com.documentverification.model.DocumentVersion" %>
<%@ page import="com.documentverification.model.ApprovalAction" %>
<% Document document = (Document) request.getAttribute("document"); List<DocumentVersion> versions = (List<DocumentVersion>) request.getAttribute("versions"); List<ApprovalAction> history = (List<ApprovalAction>) request.getAttribute("history"); %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Review Document | VerityFlow</title>
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
                        <span class="eyebrow">DOCUMENT REVIEW</span>
                        <h1><%= document.getTitle() %></h1>
                        <p class="muted"><%= document.getDescription() == null ? "" : document.getDescription() %></p>
                    </div>
                    <span class="status <%= "APPROVED".equals(document.getStatus()) ? "status-approved" : ("REJECTED".equals(document.getStatus()) ? "status-rejected" : "status-pending") %>">
                        <span class="pulse-dot <%= "APPROVED".equals(document.getStatus()) ? "green" : ("REJECTED".equals(document.getStatus()) ? "red" : "amber") %>"></span>
                        <%= document.getStatus() %>
                    </span>
                </header>

                <!-- Document Verification Chain Progress Tracker -->
                <div class="doc-stepper">
                    <div class="doc-step-item <%= "APPROVED".equals(document.getStatus()) || document.getCurrentStageOrder() > 1 ? "completed" : (document.getCurrentStageOrder() == 1 ? "active" : "") %>">
                        <div class="doc-step-badge"><%= "APPROVED".equals(document.getStatus()) || document.getCurrentStageOrder() > 1 ? "✓" : "1" %></div>
                        <div class="doc-step-info">
                            <strong>Level 1 Verification</strong>
                            <small><%= document.getCurrentStageOrder() == 1 && !"APPROVED".equals(document.getStatus()) ? "Current Review" : ("APPROVED".equals(document.getStatus()) || document.getCurrentStageOrder() > 1 ? "Passed ✓" : "Pending") %></small>
                        </div>
                    </div>
                    <div class="doc-step-arrow">&rarr;</div>
                    <div class="doc-step-item <%= "APPROVED".equals(document.getStatus()) || document.getCurrentStageOrder() > 2 ? "completed" : (document.getCurrentStageOrder() == 2 ? "active" : "") %>">
                        <div class="doc-step-badge"><%= "APPROVED".equals(document.getStatus()) || document.getCurrentStageOrder() > 2 ? "✓" : "2" %></div>
                        <div class="doc-step-info">
                            <strong>Level 2 Sign-off</strong>
                            <small><%= document.getCurrentStageOrder() == 2 && !"APPROVED".equals(document.getStatus()) ? "Current Review" : ("APPROVED".equals(document.getStatus()) || document.getCurrentStageOrder() > 2 ? "Passed ✓" : "Upcoming") %></small>
                        </div>
                    </div>
                    <div class="doc-step-arrow">&rarr;</div>
                    <div class="doc-step-item <%= "APPROVED".equals(document.getStatus()) ? "completed" : "" %>">
                        <div class="doc-step-badge"><%= "APPROVED".equals(document.getStatus()) ? "✓" : "★" %></div>
                        <div class="doc-step-info">
                            <strong>Final Verification</strong>
                            <small><%= "APPROVED".equals(document.getStatus()) ? "Approved & Sealed" : "Pending final clearance" %></small>
                        </div>
                    </div>
                </div>

                <section class="panel">
                    <div class="panel-heading">
                        <h2>Versions</h2>
                    </div>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>Version</th>
                                    <th>File</th>
                                    <th>Uploaded</th>
                                    <th>
                                    </th>
                                </tr>
                            </thead>
                            <tbody><% for (DocumentVersion version : versions) { %><tr>
                                    <td>V<%= version.getVersionNumber() %></td>
                                    <td><%= version.getOriginalFileName() %></td>
                                    <td><%= version.getUploadedAt() %></td>
                                    <td>
                                        <a class="shiny-cta compact" href="<%= request.getContextPath() %>/documents?action=download&id=<%= document.getId() %>&versionId=<%= version.getId() %>">Download</a>
                                    </td>
                                </tr><% } %></tbody>
                            </table>
                        </div>
                    </section>
                    <section class="panel">
                        <div class="panel-heading">
                            <h2>Decision</h2>
                        </div>
                        <form method="post" action="<%= request.getContextPath() %>/approval">
                            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                            <input type="hidden" name="documentId" value="<%= document.getId() %>">
                            <input type="hidden" name="versionId" value="<%= versions.get(0).getId() %>">
                            <label>Comment</label>
                            <textarea name="comment" rows="4">
                            </textarea>
                            <div class="decision-actions">
                                <button class="shiny-cta compact" name="action" value="APPROVE" type="submit">Approve</button>
                                <button class="shiny-cta compact" name="action" value="REQUEST_CHANGES" type="submit">Request changes</button>
                                <button class="shiny-cta compact" name="action" value="REJECT" type="submit">Reject</button>
                            </div>
                        </form>
                    </section>
                    <section class="panel">
                        <div class="panel-heading">
                            <h2>History</h2>
                        </div>
                        <div class="table-wrap">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Action</th>
                                        <th>Approver</th>
                                        <th>Comment</th>
                                        <th>Time</th>
                                    </tr>
                                </thead>
                                <tbody><% for (ApprovalAction action : history) { %><tr>
                                        <td><%= action.getAction() %></td>
                                        <td><%= action.getApproverRole() %></td>
                                        <td><%= action.getComment() == null ? "" : action.getComment() %></td>
                                        <td><%= action.getActedAt() %></td>
                                    </tr><% } %></tbody>
                                </table>
                            </div>
                        </section>
                    </main>
                </div>
            <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
            </body>
        </html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentverification.model.Document" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Documents | VerityFlow</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
</head>
<body>
<div class="app-bg"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>
<div class="app-shell">
    <aside class="sidebar">
        <a class="brand" href="<%= request.getContextPath() %>/submitter/dashboard">
            <span class="brand-mark small">V</span><span>VerityFlow</span>
        </a>
        <nav>
            <a class="nav-link" href="<%= request.getContextPath() %>/submitter/dashboard">Workspace</a>
            <a class="nav-link active" href="<%= request.getContextPath() %>/documents">My documents</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/notifications"><span class="nav-label">Notifications</span><% if (request.getAttribute("unreadNotificationCount") != null && (Integer) request.getAttribute("unreadNotificationCount") > 0) { %><span class="notification-badge" data-notification-count><%= request.getAttribute("unreadNotificationCount") %></span><% } %></a>
        </nav>
        <div class="sidebar-bottom"><a class="nav-link" href="<%= request.getContextPath() %>/logout">Sign out</a></div>
    </aside>

    <main class="content">
        <header class="topbar">
            <div>
                <span class="eyebrow">DOCUMENTS</span>
                <h1>My submissions</h1>
                <p class="muted">Every version and approval stage stays traceable.</p>
            </div>
            <a class="shiny-cta" href="#upload-form">Upload document</a>
        </header>

        <section class="panel">
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Document</th><th>Status</th><th>Stage</th><th>Version</th><th>Updated</th><th></th></tr></thead>
                    <tbody>
                    <%
                        List<Document> documents =
                                (List<Document>) request.getAttribute("documents");

                        for (Document document : documents) {
                    %>
                    <tr>
                        <td><strong><%= document.getTitle() %></strong><br><small>#<%= document.getId() %></small></td>
                        <td><span class="status"><%= document.getStatus() %></span></td>
                        <td>Stage <%= document.getCurrentStageOrder() %></td>
                        <td>V<%= document.getCurrentVersion() %></td>
                        <td><%= document.getUpdatedAt() %></td>
                        <td><a class="shiny-cta compact" href="<%= request.getContextPath() %>/documents?action=view&id=<%= document.getId() %>">View</a></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel" id="upload-form">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">NEW SUBMISSION</span>
                    <h2>Upload document</h2>
                </div>
            </div>
            <% if (request.getAttribute("error") != null) { %>
                <p class="muted"><%= request.getAttribute("error") %></p>
            <% } %>
            <form method="post" action="<%= request.getContextPath() %>/documents" enctype="multipart/form-data">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <label>Title</label>
                <input name="title" required maxlength="180">

                <label>Description</label>
                <textarea name="description" rows="4"></textarea>

                <label>Document type</label>
                <select name="documentTypeId" required>
                    <option value="">Select a type</option>
                    <% for (com.documentverification.model.DocumentType type : (List<com.documentverification.model.DocumentType>) request.getAttribute("documentTypes")) { %>
                        <option value="<%= type.getId() %>"><%= type.getName() %></option>
                    <% } %>
                </select>

                <label>File</label>
                <div class="file-upload">
                    <input id="documentFile" name="documentFile" type="file" accept=".pdf,.doc,.docx" required>
                </div>
                <div class="upload-actions">
                    <small>PDF, DOC, or DOCX. Maximum size: 10 MB.</small>
                    <button class="shiny-cta" type="submit">
                        Submit for approval
                        <span>→</span>
                    </button>
                </div>

            </form>
        </section>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>

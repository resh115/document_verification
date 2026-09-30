<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentverification.model.DocumentType" %>
<% List<DocumentType> types = (List<DocumentType>) request.getAttribute("items"); %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Document Types | VerityFlow</title>
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
                            <a class="nav-link active" href="<%= request.getContextPath() %>/admin/document-types">Document Types</a>
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
                        <span class="eyebrow">WORKFLOW CATALOG</span>
                        <h1>Document types</h1>
                        <p class="muted">Define the documents your organization reviews.</p>
                    </div>
                </header>
                <section class="dashboard-grid">
                    <article class="panel">
                        <div class="panel-heading">
                            <h2>Add document type</h2>
                        </div>
                        <form method="post" action="<%= request.getContextPath() %>/admin/document-types">
                            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                            <input type="hidden" name="action" value="create">
                            <label>Name</label>
                            <input name="name" required>
                            <label>Description</label>
                            <textarea name="description" rows="4">
                            </textarea>
                            <button class="shiny-cta btn-wide" type="submit">Create type</button>
                        </form>
                    </article>
                    <article class="panel panel-large">
                        <div class="panel-heading">
                            <h2>Document types</h2>
                            <span class="badge"><%= types.size() %> total</span>
                        </div>
                        <div class="table-wrap">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Description</th>
                                        <th>Status</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody><% for (DocumentType type : types) { %><tr>
                                        <td>
                                            <strong><%= type.getName() %></strong>
                                        </td>
                                        <td><%= type.getDescription() == null ? "" : type.getDescription() %></td>
                                        <td>
                                            <span class="status <%= type.isActive() ? "status-approved" : "status-pending" %>"><%= type.isActive() ? "Active" : "Inactive" %></span>
                                        </td>
                                        <td>
                                            <form method="post" action="<%= request.getContextPath() %>/admin/document-types">
                                                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                                <input type="hidden" name="action" value="toggle">
                                                <input type="hidden" name="id" value="<%= type.getId() %>">
                                                <input type="hidden" name="active" value="<%= type.isActive() ? "0" : "1" %>">
                                                <button class="shiny-cta compact" type="submit"><%= type.isActive() ? "Deactivate" : "Activate" %></button>
                                            </form>
                                        </td>
                                    </tr><% } %></tbody>
                                </table>
                            </div>
                        </article>
                    </section>
                </main>
            </div>
        <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
        </body>
    </html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.documentverification.model.Role" %>
<%
    List<Role> roles = (List<Role>) request.getAttribute("roles");
    Map<Long, Integer> userCounts = (Map<Long, Integer>) request.getAttribute("userCounts");
    Map<Long, Integer> stageCounts = (Map<Long, Integer>) request.getAttribute("stageCounts");
    String roleError = (String) session.getAttribute("roleError");
    if (roleError != null) {
        session.removeAttribute("roleError");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Roles &amp; Verifier Levels | VerityFlow</title>
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
            <a class="nav-link active" href="<%= request.getContextPath() %>/admin/roles">Roles</a>
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
                <span class="eyebrow">CUSTOM ORGANIZATIONAL ROLES &bull; ASSIGNABLE</span>
                <h1>Roles &amp; Verifier Levels</h1>
                <p class="muted">
                    Create custom role names (e.g. Dean, HOD, Registrar, Principal) and choose which role verifies documents at each workflow level.
                </p>
            </div>
            <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/workflows">Configure Workflow Levels &rarr;</a>
        </header>

        <% if (roleError != null) { %>
            <div class="alert alert-error" style="margin-bottom: 20px;">
                <%= roleError %>
            </div>
        <% } %>

        <section class="panel" style="margin-bottom: 24px;">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">PROVISION ROLE</span>
                    <h2>Create Custom Role</h2>
                </div>
            </div>

            <!-- Quick Template Preset Pills for Education, Healthcare, Enterprise -->
            <div style="margin-bottom: 16px; padding: 12px 16px; background: rgba(99, 102, 241, 0.08); border: 1px dashed rgba(99, 102, 241, 0.3); border-radius: 8px;">
                <span style="font-size: 0.85rem; color: #a5b4fc; font-weight: 600; margin-right: 8px;">Quick Education Presets:</span>
                <button type="button" class="shiny-cta compact" style="margin: 2px;" onclick="fillPreset('DEAN', 'College Dean - Level 2 Senior Verification')">Dean</button>
                <button type="button" class="shiny-cta compact" style="margin: 2px;" onclick="fillPreset('HOD', 'Lead Verifier - Level 1 Initial Verification')">HOD</button>
                <button type="button" class="shiny-cta compact" style="margin: 2px;" onclick="fillPreset('REGISTRAR', 'University Registrar - Level 3 Final Sanction')">Registrar</button>
                <button type="button" class="shiny-cta compact" style="margin: 2px;" onclick="fillPreset('PRINCIPAL', 'Institution Principal - Executive Clearance')">Principal</button>
                <button type="button" class="shiny-cta compact" style="margin: 2px;" onclick="fillPreset('FACULTY_ADVISOR', 'Academic Mentor / Faculty Advisor')">Faculty Advisor</button>
                <button type="button" class="shiny-cta compact" style="margin: 2px;" onclick="fillPreset('EXAM_CONTROLLER', 'Controller of Examinations')">Exam Controller</button>
            </div>

            <form method="post" action="<%= request.getContextPath() %>/admin/roles" class="form-grid">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <input type="hidden" name="action" value="create">

                <div class="form-group">
                    <label>Role Name (e.g. DEAN, HOD, REGISTRAR)</label>
                    <input id="roleNameInput" name="name" placeholder="e.g. DEAN or HOD" required style="text-transform: uppercase;">
                </div>

                <div class="form-group">
                    <label>Description / Verification Scope</label>
                    <input id="roleDescInput" name="description" placeholder="e.g. Reviews assigned documents" required>
                </div>

                <div style="grid-column: 1 / -1;">
                    <button class="shiny-cta" type="submit">+ Create Role</button>
                </div>
            </form>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">DIRECTORY</span>
                    <h2>Assignable Roles</h2>
                </div>
                <span class="badge"><%= roles != null ? roles.size() : 0 %> configured</span>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Role Name</th>
                            <th>Description</th>
                            <th>Assigned Users</th>
                            <th>Workflow Levels Using Role</th>
                            <th>Type</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (roles != null && !roles.isEmpty()) { %>
                        <% for (Role r : roles) { 
                            int uCount = (userCounts != null && userCounts.containsKey(r.getId())) ? userCounts.get(r.getId()) : 0;
                            int sCount = (stageCounts != null && stageCounts.containsKey(r.getId())) ? stageCounts.get(r.getId()) : 0;
                            boolean isSystemRole = "ADMIN".equalsIgnoreCase(r.getName()) || "SUBMITTER".equalsIgnoreCase(r.getName());
                        %>
                        <tr>
                            <td>
                                <span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid #10b981; font-weight: 800; font-size: 0.85rem; padding: 4px 10px;">
                                    <%= r.getName() %>
                                </span>
                            </td>
                            <td><%= r.getDescription() != null ? r.getDescription() : "Standard role" %></td>
                            <td>
                                <strong><%= uCount %></strong> <span class="muted">users</span>
                            </td>
                            <td>
                                <% if (sCount > 0) { %>
                                    <span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #6ee7b7; border: 1px solid rgba(16, 185, 129, 0.4);">
                                        <%= sCount %> Level(s)
                                    </span>
                                <% } else { %>
                                    <span class="muted">None</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (isSystemRole) { %>
                                    <span class="badge" style="background: rgba(148, 163, 184, 0.15); color: #cbd5e1; border: 1px solid rgba(148, 163, 184, 0.3);">System Base</span>
                                <% } else { %>
                                    <span class="badge" style="background: rgba(6, 182, 212, 0.18); color: #67e8f9; border: 1px solid rgba(6, 182, 212, 0.4);">Custom Assignable</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (!isSystemRole && uCount == 0 && sCount == 0) { %>
                                <form method="post" action="<%= request.getContextPath() %>/admin/roles" style="display:inline;" onsubmit="return confirm('Delete custom role <%= r.getName() %>?');">
                                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="<%= r.getId() %>">
                                    <button class="ghost-btn compact" type="submit" style="color: #fda4af; border: 1px solid rgba(244, 63, 94, 0.4); background: rgba(244, 63, 94, 0.15); font-size: 12px; padding: 4px 10px;">Delete</button>
                                </form>
                                <% } else if (isSystemRole) { %>
                                    <span class="muted" style="font-size: 0.8rem;">Protected</span>
                                <% } else { %>
                                    <span class="muted" style="font-size: 0.8rem;" title="Role is currently assigned to users or workflow levels">In Use</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    <% } else { %>
                        <tr><td colspan="6">No roles defined.</td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>

<script>
function fillPreset(name, desc) {
    document.getElementById('roleNameInput').value = name;
    document.getElementById('roleDescInput').value = desc;
    document.getElementById('roleNameInput').focus();
}
</script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>

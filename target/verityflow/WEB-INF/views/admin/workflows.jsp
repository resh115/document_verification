<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.documentverification.model.DocumentType" %>
<%@ page import="com.documentverification.model.User" %>
<%@ page import="com.documentverification.model.Workflow" %>
<%@ page import="com.documentverification.model.WorkflowStage" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Workflow Builder | VerityFlow</title>
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
            <a class="nav-link active" href="<%= request.getContextPath() %>/admin/workflows">Workflows</a>
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
                <span class="eyebrow">WORKFLOW ENGINE</span>
                <h1>Approval Chains &amp; Verification Levels</h1>
                <p class="muted">Admin creates workflows and assigns which user verifies documents at each level.</p>
            </div>
            <button class="shiny-cta" type="button" onclick="document.getElementById('new-workflow-panel').style.display='block'; window.scrollTo(0,0);">+ New Workflow</button>
        </header>

        <!-- New Workflow Creation Form (Toggled) -->
        <section id="new-workflow-panel" class="panel" style="display: none; margin-bottom: 24px;">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">CREATE WORKFLOW</span>
                    <h2>Add New Approval Workflow</h2>
                </div>
                <button type="button" class="shiny-cta compact" onclick="document.getElementById('new-workflow-panel').style.display='none';">Cancel</button>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/admin/workflows">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <input type="hidden" name="action" value="create">

                <label>Workflow Name</label>
                <input name="name" placeholder="e.g. Student Leave Application, Marksheet Verification" required>

                <label>Document Type</label>
                <select name="documentTypeId" required>
                    <% 
                        List<DocumentType> docTypes = (List<DocumentType>) request.getAttribute("documentTypes");
                        if (docTypes != null) {
                            for (DocumentType dt : docTypes) { 
                    %>
                        <option value="<%= dt.getId() %>"><%= dt.getName() %></option>
                    <% 
                            }
                        } 
                    %>
                </select>

                <button class="shiny-cta" type="submit" style="margin-top: 15px;">Create Workflow</button>
            </form>
        </section>

        <!-- Workflows List -->
        <section class="panel">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">CONFIGURATION</span>
                    <h2>Active Workflows</h2>
                </div>
            </div>

            <div class="table-search-wrap">
                <div class="table-search-input-box">
                    <svg class="table-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                    <input id="workflow-search-input" class="table-search-input" placeholder="Search workflows by name or document type...">
                </div>
                <span id="workflow-count-badge" class="search-count-badge"><%= request.getAttribute("workflows") != null ? ((List) request.getAttribute("workflows")).size() : 0 %> templates</span>
            </div>

            <div class="workflow-list" id="active-workflows-list">
            <%
                List<Workflow> workflows =
                        (List<Workflow>) request.getAttribute("workflows");

                for (Workflow workflow : workflows) {
            %>
                <article class="workflow-card">
                    <div class="workflow-icon">WF</div>
                    <div class="workflow-info">
                        <h3><%= workflow.getName() %></h3>
                        <p>Document type #<%= workflow.getDocumentTypeId() %></p>
                    </div>
                    <span class="status status-approved">Active</span>
                    <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/workflows?workflowId=<%= workflow.getId() %>">Configure Levels →</a>
                </article>
            <% } %>
            </div>
        </section>

        <!-- Stage Configuration for Selected Workflow -->
        <% if (request.getAttribute("stages") != null) { 
            List<WorkflowStage> stageList = (List<WorkflowStage>) request.getAttribute("stages");
        %>
        <section class="panel workflow-config-panel" style="margin-top: 24px;">
            <div class="panel-heading">
                <div>
                    <span class="eyebrow">WORKFLOW VERIFICATION CHAIN</span>
                    <h2>Workflow Verification Levels</h2>
                    <p class="muted" style="margin-top:4px; font-size:13px;">Each level is assigned to a specific user. Documents progress sequentially: Level 1 &rarr; Level 2 &rarr; Level 3 &rarr; Approved.</p>
                </div>
                <a class="shiny-cta compact" href="<%= request.getContextPath() %>/admin/workflows">← Back to workflows</a>
            </div>

            <!-- Visual Pipeline Stepper -->
            <% if (!stageList.isEmpty()) { %>
            <div class="pipeline-flow">
                <% for (int i = 0; i < stageList.size(); i++) {
                    WorkflowStage s = stageList.get(i);
                    String vName = s.getUserName() != null ? s.getUserName() : "Assigned User";
                    String initials = "V";
                    if (vName != null && !vName.trim().isEmpty()) {
                        String[] parts = vName.trim().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
                        if (parts.length > 1 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                            initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
                        } else if (parts.length > 0 && !parts[0].isEmpty()) {
                            initials = ("" + parts[0].charAt(0)).toUpperCase();
                        }
                    }
                %>
                <div class="pipeline-step">
                    <div class="pipeline-step-header">
                        <span class="pipeline-level-pill">Level <%= s.getStageOrder() %></span>
                        <span class="status status-pending" style="font-size: 10px; padding: 2px 7px;">Sequential</span>
                    </div>
                    <div class="pipeline-step-title"><%= s.getStageName() %></div>
                    <div class="pipeline-user-box">
                        <div class="user-avatar-circle"><%= initials %></div>
                        <div class="pipeline-user-meta">
                            <strong><%= vName %></strong>
                            <span><%= s.getUserEmail() != null ? s.getUserEmail() : "Assigned Verifier" %></span>
                        </div>
                    </div>
                </div>
                <div class="pipeline-connector">&rarr;</div>
                <% } %>
                <div class="pipeline-finish-step">
                    <div class="pipeline-finish-icon">✓</div>
                    <strong style="color: #34d399; font-size: 13px; display: block;">Fully Approved</strong>
                    <span style="color: var(--muted); font-size: 11px;">Document Cleared</span>
                </div>
            </div>
            <% } %>

            <div class="workflow-list">
                <% 
                    if (stageList.isEmpty()) { 
                %>
                    <div class="empty-state" style="padding: 28px; text-align: center; color: var(--muted); background: rgba(5,13,8,0.5); border-radius: 12px; border: 1px dashed var(--border);">
                        <p style="font-size: 15px; color: #f0fdf4; margin-bottom: 6px;">No verification levels defined yet for this workflow.</p>
                        <p style="font-size: 13px;">Assign the first user at Level 1 below to activate the approval chain.</p>
                    </div>
                <% 
                    }
                    for (WorkflowStage stage : stageList) { 
                        String verifierDisplay = stage.getUserName() != null ? stage.getUserName() : "Assigned User";
                        String verifierEmail = stage.getUserEmail() != null ? (" (" + stage.getUserEmail() + ")") : "";
                %>
                <article class="workflow-card">
                    <div class="workflow-icon" style="min-width:74px; text-align:center; font-weight:800; font-size: 13px;">Level <%= stage.getStageOrder() %></div>
                    <div class="workflow-info">
                        <h3>Level <%= stage.getStageOrder() %>: <%= stage.getStageName() %></h3>
                        <p>
                            Assigned Verifier: <strong style="color:var(--accent);"><%= verifierDisplay %></strong><%= verifierEmail %>
                        </p>
                    </div>
                    <form method="post" action="<%= request.getContextPath() %>/admin/workflows" style="margin-left:auto; display:inline;" onsubmit="return confirm('Remove Level <%= stage.getStageOrder() %> from this workflow?');">
                        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                        <input type="hidden" name="action" value="deleteStage">
                        <input type="hidden" name="id" value="<%= stage.getId() %>">
                        <input type="hidden" name="workflowId" value="<%= request.getParameter("workflowId") %>">
                        <button class="ghost-btn compact" type="submit" style="background: rgba(244, 63, 94, 0.1); border: 1px solid rgba(244, 63, 94, 0.4); color: #fda4af; font-size: 12px; padding: 4px 10px;">Remove Level</button>
                    </form>
                </article>
                <% } %>
            </div>

            <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid var(--border);">
                <span class="eyebrow">ASSIGN VERIFICATION LEVEL</span>
                <h3 style="margin-bottom: 6px;">Add Verification Level</h3>
                <p class="muted" style="margin-bottom: 16px; font-size: 13px;">Admin assigns the level order and the specific user's name who verifies documents at this level. No domain or role confusion.</p>

                <form method="post" action="<%= request.getContextPath() %>/admin/workflows">
                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                    <input type="hidden" name="action" value="addStage">
                    <input type="hidden" name="workflowId" value="<%= request.getParameter("workflowId") %>">
                    
                    <label>Verification Level (Order)</label>
                    <input name="stageOrder" type="number" min="1" value="<%= stageList.size() + 1 %>" required>
                    <small class="muted" style="display:block; margin-top:2px; margin-bottom:10px;">Level number: 1 = Initial verifier, 2 = Second verifier, 3 = Third verifier, etc.</small>

                    <label>Level Name / Description</label>
                    <input name="stageName" placeholder="e.g. Initial Verification, Finance Review, Final Approval" required>

                    <label>Assigned Verifier (User Name)</label>
                    <select name="userId" required>
                        <option value="">-- Select User Name to Verify at this Level --</option>
                        <% 
                            List<User> userList = (List<User>) request.getAttribute("users");
                            if (userList != null) {
                                for (User u : userList) {
                                    if ("ADMIN".equalsIgnoreCase(u.getRoleName())) continue; // Admins do not verify
                        %>
                            <option value="<%= u.getId() %>">
                                <%= u.getName() %> — <%= u.getEmail() %> (<%= u.getRoleName() != null ? u.getRoleName() : "User" %>)
                            </option>
                        <% 
                                }
                            } 
                        %>
                    </select>

                    <button class="shiny-cta" type="submit" style="margin-top: 15px;">+ Assign Level to User</button>
                </form>
            </div>
        </section>
        <% } %>
    </main>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", () => {
        const wfInput = document.getElementById("workflow-search-input");
        if (wfInput) {
            wfInput.addEventListener("input", () => {
                const query = wfInput.value.trim().toLowerCase();
                const cards = document.querySelectorAll("#active-workflows-list .workflow-card");
                let count = 0;
                cards.forEach(card => {
                    const text = card.textContent.toLowerCase();
                    const match = query === "" || text.includes(query);
                    card.style.display = match ? "flex" : "none";
                    if (match) count++;
                });
                const countBadge = document.getElementById("workflow-count-badge");
                if (countBadge) {
                    countBadge.textContent = query === "" 
                        ? `<%= request.getAttribute("workflows") != null ? ((List) request.getAttribute("workflows")).size() : 0 %> templates`
                        : `Showing ${count} of <%= request.getAttribute("workflows") != null ? ((List) request.getAttribute("workflows")).size() : 0 %>`;
                }
            });
        }
    });
</script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in | VerityFlow</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=20260920">
</head>
<body class="auth-page">
    <canvas id="globe-canvas" aria-label="Interactive wireframe Earth"></canvas>

    <main class="auth-shell">
        <section class="auth-brand">
            <span class="eyebrow">DOCUMENT OPERATIONS</span>
            <h1>Verify with clarity.<br><span>Approve with control.</span></h1>
            <p>
                A configurable document verification workspace for
                organizations that need accountable, multi-stage approvals.
            </p>

            <div class="feature-strip">
                <span>RBAC</span>
                <span>Versioning</span>
                <span>Audit Trail</span>
            </div>
        </section>

        <section class="auth-card glass-card">
            <div class="card-heading">
                <span class="status-dot"></span>
                <span>Secure workspace</span>
            </div>

            <h2>Welcome back</h2>
            <p class="muted">Sign in to continue to your verification workspace.</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

                 <form method="post" action="<%= request.getContextPath() %>/login" autocomplete="off">
                <label for="email">Work email</label>
                <input id="email" name="email" type="email"
                      placeholder="test@example.com" autocomplete="off" required>

                <label for="password">Password</label>
                <div class="password-input-wrap">
                    <input id="password" name="password" type="password"
                          placeholder="Enter your password" autocomplete="new-password" required>
                    <button type="button" class="password-toggle-btn" aria-label="Show password" title="Show password" onclick="togglePasswordVisibility('password', this)">
                        <svg class="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </button>
                </div>

                <button class="shiny-cta btn-wide" type="submit">
                    Sign in
                    <span>→</span>
                </button>
            </form>

            <div class="sample-note">
                <strong>Sample environment</strong>
                <p>Use the sample credentials provided in README.md.</p>
            </div>
        </section>
    </main>
    <script src="https://unpkg.com/three@0.160.0/build/three.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/topojson-client@3.1.0/dist/topojson-client.min.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/globe.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>

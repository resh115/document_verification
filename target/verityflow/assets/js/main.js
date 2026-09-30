document.addEventListener("DOMContentLoaded", () => {
    initPageNavigationControls();
    document.querySelectorAll(".nav-link").forEach(link => {
        link.addEventListener("click", () => {
            document.body.classList.add("page-transition");
        });
    });

    const surfaces = document.querySelectorAll(".panel, .metric-card, .workflow-card, .approval-row, .glass-card");
    const buttons = document.querySelectorAll(".btn, .ghost-btn");

    document.addEventListener("pointermove", event => {
        surfaces.forEach(surface => {
            const bounds = surface.getBoundingClientRect();
            const distanceX = Math.max(bounds.left - event.clientX, 0, event.clientX - bounds.right);
            const distanceY = Math.max(bounds.top - event.clientY, 0, event.clientY - bounds.bottom);
            const distance = Math.hypot(distanceX, distanceY);

            if (distance > 150) {
                surface.style.setProperty("--shine-opacity", "0");
                return;
            }

            surface.style.setProperty("--shine-opacity", String(1 - distance / 150));
            surface.style.setProperty("--pointer-x", `${event.clientX - bounds.left}px`);
            surface.style.setProperty("--pointer-y", `${event.clientY - bounds.top}px`);
        });

        buttons.forEach(button => {
            const bounds = button.getBoundingClientRect();
            button.style.setProperty("--button-x", `${event.clientX - bounds.left}px`);
            button.style.setProperty("--button-y", `${event.clientY - bounds.top}px`);
        });
    });
});

/**
 * Toggle password visibility between password and text format with eye icon update
 */
function togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const isPassword = input.type === "password";
    input.type = isPassword ? "text" : "password";

    if (btn) {
        if (isPassword) {
            // State: now visible -> show slashed eye (eye-off)
            btn.innerHTML = '<svg class="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
                + '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>'
                + '<line x1="1" y1="1" x2="23" y2="23"></line>'
                + '</svg>';
            btn.setAttribute("title", "Hide password");
            btn.setAttribute("aria-label", "Hide password");
        } else {
            // State: now hidden -> show open eye
            btn.innerHTML = '<svg class="eye-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
                + '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>'
                + '<circle cx="12" cy="12" r="3"></circle>'
                + '</svg>';
            btn.setAttribute("title", "Show password");
            btn.setAttribute("aria-label", "Show password");
        }
    }
}
window.togglePasswordVisibility = togglePasswordVisibility;

/**
 * Live instant table filter search
 */
function initTableFilter(inputId, tableSelector, countId) {
    const input = document.getElementById(inputId);
    const table = document.querySelector(tableSelector);
    if (!input || !table) return;

    const countElem = countId ? document.getElementById(countId) : null;
    const rows = table.querySelectorAll("tbody tr");
    const totalCount = rows.length;

    input.addEventListener("input", () => {
        const query = input.value.trim().toLowerCase();
        let visibleCount = 0;

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            if (query === "" || text.includes(query)) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        if (countElem) {
            countElem.textContent = query === "" 
                ? `${totalCount} records` 
                : `Showing ${visibleCount} of ${totalCount}`;
        }
    });
}
window.initTableFilter = initTableFilter;

/**
 * Copy text to clipboard with temporary visual feedback
 */
function copyToClipboard(text, btn) {
    if (!navigator.clipboard) {
        const textArea = document.createElement("textarea");
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand("copy");
        document.body.removeChild(textArea);
    } else {
        navigator.clipboard.writeText(text);
    }

    if (btn) {
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<span>✓ Copied!</span>';
        btn.style.borderColor = '#10b981';
        btn.style.color = '#34d399';
        setTimeout(() => {
            btn.innerHTML = originalHtml;
            btn.style.borderColor = '';
            btn.style.color = '';
        }, 1500);
    }
}
window.copyToClipboard = copyToClipboard;


/**
 * Combined audit-log search + event-category filtering.
 * Categories are mapped from the event names written by the backend.
 */
function initAuditFilters(inputId, tableSelector, countSelector) {
    const input = document.getElementById(inputId);
    const table = document.querySelector(tableSelector);
    const count = document.querySelector(countSelector);
    if (!input || !table) return;

    window.__auditFilter = { category: "ALL" };

    const apply = () => {
        const query = input.value.trim().toLowerCase();
        const rows = Array.from(table.querySelectorAll("tbody tr"));
        let visible = 0;

        rows.forEach(row => {
            const event = (row.dataset.eventType || "").toUpperCase();
            const text = row.textContent.toLowerCase();
            const category = window.__auditFilter.category;
            const categoryMatch = category === "ALL" || auditEventMatchesCategory(event, category);
            const searchMatch = !query || text.includes(query);
            const show = categoryMatch && searchMatch;
            row.style.display = show ? "" : "none";
            if (show) visible++;
        });

        if (count) {
            count.textContent = query || window.__auditFilter.category !== "ALL"
                ? `${visible} of ${rows.length} events`
                : `${rows.length} events`;
        }
    };

    input.addEventListener("input", apply);
    window.__applyAuditFilters = apply;
    apply();
}

function auditEventMatchesCategory(event, category) {
    switch (category) {
        case "LOGIN":
            return event === "LOGIN";
        case "USER":
            return event.startsWith("USER_") || event.startsWith("ROLE_");
        case "DOCUMENT":
            return event.startsWith("DOCUMENT") || event === "VERSION_UPLOADED"
                || event === "APPROVE" || event === "REJECT" || event === "REQUEST_CHANGES";
        case "WORKFLOW":
            return event.startsWith("WORKFLOW");
        default:
            return true;
    }
}

function filterAuditEvents(category, pillBtn) {
    window.__auditFilter = window.__auditFilter || { category: "ALL" };
    window.__auditFilter.category = category;
    document.querySelectorAll(".filter-pill").forEach(p => p.classList.remove("active"));
    if (pillBtn) pillBtn.classList.add("active");
    if (typeof window.__applyAuditFilters === "function") window.__applyAuditFilters();
}
window.initAuditFilters = initAuditFilters;
window.filterAuditEvents = filterAuditEvents;

/**
 * Add compact global navigation controls: browser Back and scroll-to-top.
 * The controls appear on application pages and stay out of the login screen.
 */
function initPageNavigationControls() {
    if (document.body.classList.contains("auth-page") || location.pathname.endsWith("/login.jsp")) return;
    if (document.getElementById("page-navigation-controls")) return;

    const wrap = document.createElement("div");
    wrap.id = "page-navigation-controls";
    wrap.className = "page-navigation-controls";
    wrap.innerHTML = `
        <button type="button" class="page-nav-btn" id="page-back-btn" title="Go back" aria-label="Go back">← Back</button>
        <button type="button" class="page-nav-btn page-top-btn" id="page-top-btn" title="Scroll to top" aria-label="Scroll to top">↑</button>`;
    document.body.appendChild(wrap);

    const back = document.getElementById("page-back-btn");
    const top = document.getElementById("page-top-btn");
    const brand = document.querySelector(".brand");

    back.addEventListener("click", () => {
        if (window.history.length > 1) {
            window.history.back();
        } else if (brand && brand.href) {
            window.location.href = brand.href;
        } else {
            window.scrollTo({ top: 0, behavior: "smooth" });
        }
    });

    top.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));

    const updateTopButton = () => {
        top.classList.toggle("visible", window.scrollY > 300);
    };
    window.addEventListener("scroll", updateTopButton, { passive: true });
    updateTopButton();
}
window.initPageNavigationControls = initPageNavigationControls;

/**
 * Instant filter for notification cards by category
 */
function filterNotificationCards(category, pillBtn) {
    const cards = document.querySelectorAll(".notification-card");
    const pills = document.querySelectorAll(".filter-pill");
    
    pills.forEach(p => p.classList.remove("active"));
    if (pillBtn) pillBtn.classList.add("active");

    cards.forEach(card => {
        if (category === "ALL") {
            card.style.display = "flex";
        } else {
            const isMatch = card.classList.contains(`type-${category.toLowerCase()}`);
            card.style.display = isMatch ? "flex" : "none";
        }
    });
}
window.filterNotificationCards = filterNotificationCards;

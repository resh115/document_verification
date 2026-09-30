<%@ page contentType="text/html;charset=UTF-8" %>
<%
    Object userId = session.getAttribute("userId");

    if (userId != null) {
        String role = (String) session.getAttribute("role");

        if ("ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else if ("APPROVER".equals(role) || "FINANCE".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/approver/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/submitter/dashboard");
        }
        return;
    }

    response.sendRedirect(request.getContextPath() + "/login");
%>

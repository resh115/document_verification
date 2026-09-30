package com.documentverification.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String role = (String) httpRequest.getSession().getAttribute("role");
        String uri = httpRequest.getRequestURI();

        if ("ADMIN".equals(role)) {
            if (uri.endsWith("/approval") || uri.contains("/approver/") || uri.endsWith("/documents")) {
                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Administrators cannot view or verify documents. Admin role is restricted to multi-tenant organization, user, and notification management."
                );
                return;
            }
        }

        if (uri.contains("/admin/") && !"ADMIN".equals(role)) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Administrator access required."
            );
            return;
        }

        boolean isSubmitterType = "SUBMITTER".equalsIgnoreCase(role) || "STUDENT".equalsIgnoreCase(role);

        if ((uri.contains("/approver/") || uri.endsWith("/approval"))
                && ("ADMIN".equalsIgnoreCase(role) || isSubmitterType || role == null)) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Verifier / Staff access required."
            );
            return;
        }

        if ((uri.contains("/submitter/") || uri.endsWith("/documents"))
                && !isSubmitterType) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Submitter / Student access required."
            );
            return;
        }

        chain.doFilter(request, response);
    }
}

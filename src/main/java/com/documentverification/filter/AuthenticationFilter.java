package com.documentverification.filter;

import com.documentverification.dao.NotificationDAO;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class AuthenticationFilter implements Filter {

    private final NotificationDAO notifications = new NotificationDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String context = httpRequest.getContextPath();

        boolean publicResource =
                uri.equals(context + "/")
                || uri.equals(context + "/login")
                || uri.startsWith(context + "/assets/");

        if (publicResource
                || httpRequest.getSession(false) != null
                && httpRequest.getSession(false).getAttribute("userId") != null) {
            if (!publicResource) {
                try {
                    long organizationId = (Long) httpRequest.getSession().getAttribute("organizationId");
                    long userId = (Long) httpRequest.getSession().getAttribute("userId");
                    httpRequest.setAttribute("unreadNotificationCount",
                            notifications.countUnread(organizationId, userId));
                } catch (Exception e) {
                    httpRequest.setAttribute("unreadNotificationCount", 0);
                }
            }
            chain.doFilter(request, response);
            return;
        }

        httpResponse.sendRedirect(context + "/login");
    }
}

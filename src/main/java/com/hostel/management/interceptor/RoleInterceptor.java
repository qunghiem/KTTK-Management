package com.hostel.management.interceptor;

import com.hostel.management.model.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // Nếu đường dẫn bắt đầu bằng /admin
        if (requestURI.startsWith("/admin")) {
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect("/login");
                return false;
            }

            User user = (User) session.getAttribute("user");
            if (!"ADMIN".equals(user.getRole())) {
                response.sendRedirect("/home");
                return false;
            }
        }

        return true;
    }
}
package com.hostel.management.controller;

import com.hostel.management.model.User;
import com.hostel.management.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("user", new User());
        // Lưu đường dẫn redirect (nếu có) để sử dụng sau khi đăng nhập
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirect", redirect);
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User loginUser,
                        @RequestParam(required = false) String redirect,
                        HttpSession session,
                        Model model) {
        System.out.println("Login attempt - Username: " + loginUser.getUsername() + ", Password length: " + loginUser.getPassword().length());

        User authenticatedUser = authService.authenticate(loginUser);

        if (authenticatedUser != null) {
            System.out.println("Login successful for: " + authenticatedUser.getUsername() + ", Role: " + authenticatedUser.getRole());
            // Lưu thông tin người dùng vào session
            session.setAttribute("user", authenticatedUser);

            // Nếu có đường dẫn redirect, chuyển hướng đến đó
            if (redirect != null && !redirect.isEmpty()) {
                System.out.println("Redirecting to: " + redirect);
                return "redirect:" + redirect;
            }
            return "redirect:/home";
        } else {
            System.out.println("Login failed for: " + loginUser.getUsername());
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
            // Giữ lại đường dẫn redirect nếu có
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            User registeredUser = authService.registerNewUser(user);
            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/verify-account")
    public String verifyAccount(@RequestParam String token, Model model) {
        // Mô phỏng xác minh tài khoản
        boolean success = authService.validateToken(token);

        if (success) {
            model.addAttribute("success", "Tài khoản đã được xác minh.");
        } else {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
        }

        return "auth/verify-account";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@ModelAttribute User user, Model model) {
        try {
            User resetUser = authService.resetPassword(user);
            if (resetUser != null) {
                model.addAttribute("message", "Yêu cầu đặt lại mật khẩu đã được gửi đến email của bạn.");
            } else {
                model.addAttribute("error", "Không tìm thấy tài khoản với email này.");
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/login-process")
    public String processLogin(@ModelAttribute User loginUser,
                               @RequestParam(required = false) String redirect,
                               HttpSession session,
                               Model model) {
        System.out.println("Processing login with username: " + loginUser.getUsername());

        User authenticatedUser = authService.authenticate(loginUser);

        if (authenticatedUser != null) {
            System.out.println("Authentication successful for: " + authenticatedUser.getUsername());
            session.setAttribute("user", authenticatedUser);

            // Kiểm tra role và điều hướng tương ứng
            if ("ADMIN".equals(authenticatedUser.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                if (redirect != null && !redirect.isEmpty()) {
                    return "redirect:" + redirect;
                }
                return "redirect:/home";
            }
        } else {
            System.out.println("Authentication failed for: " + loginUser.getUsername());
            return "redirect:/login?error=invalid";
        }
    }
}
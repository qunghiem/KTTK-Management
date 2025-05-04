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
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = authService.authenticate(username, password);

        if (user != null) {
            // Lưu thông tin người dùng vào session
            session.setAttribute("user", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.registerNewUser(user);
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
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

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, Model model) {
        boolean success = authService.resetPassword(email);

        if (success) {
            model.addAttribute("success", "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn.");
        } else {
            model.addAttribute("error", "Email không tồn tại trong hệ thống.");
        }

        return "auth/reset-password";
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
}
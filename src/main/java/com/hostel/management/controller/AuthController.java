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
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String redirect,
                        HttpSession session,
                        Model model) {
        User user = authService.authenticate(username, password);

        if (user != null) {
            // Lưu thông tin người dùng vào session
            session.setAttribute("user", user);

            // Nếu có đường dẫn redirect, chuyển hướng đến đó
            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
            // Giữ lại đường dẫn redirect nếu có
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("user", new User());
        // Lưu đường dẫn redirect (nếu có) để sử dụng sau khi đăng ký
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirect", redirect);
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,  // BindingResult phải ngay sau @Valid @ModelAttribute
                           @RequestParam(required = false) String confirmPassword,
                           @RequestParam(required = false) String redirect,
                           Model model) {
        if (result.hasErrors()) {
            // Giữ lại đường dẫn redirect nếu có
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }
            return "auth/register";
        }

        // Kiểm tra mật khẩu xác nhận
        if (confirmPassword != null && !user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            // Giữ lại đường dẫn redirect nếu có
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }
            return "auth/register";
        }

        try {
            authService.registerNewUser(user);
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            // Chuyển đường dẫn redirect (nếu có) đến trang đăng nhập
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // Giữ lại đường dẫn redirect nếu có
            if (redirect != null && !redirect.isEmpty()) {
                model.addAttribute("redirect", redirect);
            }
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
package com.hostel.management.service;

import com.hostel.management.model.User;
import com.hostel.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    public String generateToken(User user) {
        // Trong ứng dụng thực tế, sử dụng JWT
        return "token-" + user.getUsername() + "-" + System.currentTimeMillis();
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("token-");
    }

    public User registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        user.setRole("CUSTOMER");
        return userRepository.save(user);
    }

    public boolean resetPassword(String email) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // Trong ứng dụng thực tế, gửi email
            return true;
        }

        return false;
    }
}
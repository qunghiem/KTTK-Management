package com.hostel.management.service;

import com.hostel.management.model.User;
import com.hostel.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User authenticate(User loginUser) {
        System.out.println("Authenticating: " + loginUser.getUsername());

        // Tìm kiếm theo username
        User user = userRepository.findByUsername(loginUser.getUsername());
        if (user != null) {
            System.out.println("Found user by username: " + user.getUsername());
        }

        // Nếu không tìm thấy theo username, thử tìm theo email
        if (user == null) {
            user = userRepository.findByEmail(loginUser.getUsername());
            if (user != null) {
                System.out.println("Found user by email: " + user.getEmail());
            }
        }

        if (user == null) {
            System.out.println("User not found");
            return null;
        }

        // In ra để kiểm tra
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Input password: " + loginUser.getPassword());

        // Kiểm tra mật khẩu
        // Thử so sánh trực tiếp trước
        if (loginUser.getPassword().equals(user.getPassword())) {
            System.out.println("Direct password match successful");
            return user;
        }

        // Nếu so sánh trực tiếp không thành công, thử với mã hóa
        boolean passwordMatches = passwordEncoder.matches(loginUser.getPassword(), user.getPassword());
        System.out.println("Password matches with BCrypt: " + passwordMatches);

        if (passwordMatches) {
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

    public User resetPassword(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null) {
            // Trong ứng dụng thực tế, gửi email
            // Tạo mật khẩu mới và cập nhật
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                return userRepository.save(existingUser);
            }
            return existingUser;
        }

        return null;
    }
}
package com.hostel.management.config;

import com.hostel.management.model.User;
import com.hostel.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Kiểm tra nếu đã tồn tại tài khoản test
        if (!userRepository.existsByUsername("nxquan")) {
            User testUser = new User();
            testUser.setUsername("nxquan");
            testUser.setPassword(passwordEncoder.encode("nxquan")); // Mật khẩu mã hóa
            testUser.setEmail("nxquan@example.com");
            testUser.setRole("CUSTOMER");
            userRepository.save(testUser);
            System.out.println("Đã tạo tài khoản test với mật khẩu: nxquan");

            // In mật khẩu đã mã hóa để tham khảo
            System.out.println("Mật khẩu đã mã hóa: " + testUser.getPassword());
        }

        // Thêm tài khoản admin nếu chưa có
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@example.com");
            adminUser.setRole("ADMIN");
            userRepository.save(adminUser);
            System.out.println("Đã tạo tài khoản admin với mật khẩu: admin123");
            System.out.println("Mật khẩu đã mã hóa của admin: " + adminUser.getPassword());
        }

        // Cho phép bạn xem các tài khoản hiện có
        System.out.println("Danh sách tài khoản hiện có:");
        userRepository.findAll().forEach(user -> {
            System.out.println("ID: " + user.getId() +
                    ", Username: " + user.getUsername() +
                    ", Email: " + user.getEmail() +
                    ", Role: " + user.getRole());
        });
    }
}
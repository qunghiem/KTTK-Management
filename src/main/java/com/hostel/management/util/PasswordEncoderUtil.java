package com.hostel.management.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "matkhau123";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Mật khẩu gốc: " + rawPassword);
        System.out.println("Mật khẩu đã mã hóa: " + encodedPassword);

        // Kiểm tra mật khẩu
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("Kết quả kiểm tra: " + matches);
    }
}
package com.timekeeping.utilities;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("admin"); // Change "admin" to your actual password
        System.out.println("Encoded password: " + encodedPassword);
    }
}

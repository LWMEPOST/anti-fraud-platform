package com.antifraud;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String password = "admin123";
        
        // 数据库中存储的哈希
        String storedHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje";
        
        // 验证
        boolean matches = encoder.matches(password, storedHash);
        
        System.out.println("Password: " + password);
        System.out.println("Stored Hash: " + storedHash);
        System.out.println("Matches: " + matches);
        
        // 生成新的哈希
        String newHash = encoder.encode(password);
        System.out.println("New Hash: " + newHash);
        
        // 验证新哈希
        boolean newMatches = encoder.matches(password, newHash);
        System.out.println("New Hash Matches: " + newMatches);
    }
}
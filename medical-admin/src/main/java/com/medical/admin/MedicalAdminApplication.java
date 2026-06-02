package com.medical.admin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 中医药品管理系统 - 启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.medical")
public class MedicalAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalAdminApplication.class, args);
        System.out.println("\n================================================");
        System.out.println("  中医药品管理系统启动成功！");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("================================================\n");
    }

    /**
     * 启动时打印 admin123 的 BCrypt 哈希（用于数据库初始化）
     */
    @Bean
    CommandLineRunner printPasswordHash() {
        return args -> {
            String hash = new BCryptPasswordEncoder().encode("admin123");
            System.out.println(">>> admin123 的BCrypt哈希: " + hash);
        };
    }
}

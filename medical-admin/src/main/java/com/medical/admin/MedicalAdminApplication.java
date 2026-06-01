package com.medical.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

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
}

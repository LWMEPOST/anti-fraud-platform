package com.antifraud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.antifraud.mapper")
public class AntiFraudPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AntiFraudPlatformApplication.class, args);
        System.out.println("==========================================");
        System.out.println("   高校反诈防骗宣传平台启动成功！");
        System.out.println("   接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("==========================================");
    }
}

package com.dillon.cianalyst.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.dillon.cianalyst")
public class CiFailureAnalystApplication {
    public static void main(String[] args) {
        SpringApplication.run(CiFailureAnalystApplication.class, args);
    }
    
}

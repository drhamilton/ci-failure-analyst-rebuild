package com.dillon.cianalyst.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dillon.cianalyst")
@EnableScheduling
public class CiFailureAnalystApplication {
    public static void main(String[] args) {
        SpringApplication.run(CiFailureAnalystApplication.class, args);
    }
    
}

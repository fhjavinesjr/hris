package com.emloyeeportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hris.common", "com.emloyeeportal"})
public class EmployeePortal {
    public static void main(String[] args) {
        SpringApplication.run(EmployeePortal.class, args);
    }
}

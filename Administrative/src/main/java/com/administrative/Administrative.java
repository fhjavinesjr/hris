package com.administrative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hris.common", "com.administrative"})
public class Administrative {
    public static void main(String[] args) {
        SpringApplication.run(Administrative.class, args);
    }
}

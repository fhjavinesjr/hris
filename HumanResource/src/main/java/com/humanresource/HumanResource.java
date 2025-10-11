package com.humanresource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hris.common","com.humanresource"})
public class HumanResource {
    public static void main(String[] args) {
        SpringApplication.run(HumanResource.class, args);
    }
}
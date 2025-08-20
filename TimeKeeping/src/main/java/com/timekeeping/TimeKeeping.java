package com.timekeeping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hris.common","com.timekeeping"})
public class TimeKeeping {
    public static void main(String[] args) {
        SpringApplication.run(TimeKeeping.class, args);
    }
}

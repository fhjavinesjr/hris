package com.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hris.common","com.payroll"})
public class Payroll {
    public static void main( String[] args ) {
        SpringApplication.run(Payroll.class, args);
    }
}

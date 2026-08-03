package com.primehr;

import com.primehr.config.PrimeHrProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PrimeHrProperties.class)
public class PrimeHRApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrimeHRApplication.class, args);
    }
}

package com.hris;

import com.administrative.configs.AdministrativeSecurityConfig;
import com.timekeeping.configs.TimeKeepingSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@ComponentScan(
        basePackages = {
                "com.timekeeping",
                "com.administrative",
                "com.hris.common"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TimeKeepingSecurityConfig.class,
                        AdministrativeSecurityConfig.class
                }
        )
)
public class HRISApplication {

    public static void main(String[] args) {
        SpringApplication.run(HRISApplication.class, args);
    }
}
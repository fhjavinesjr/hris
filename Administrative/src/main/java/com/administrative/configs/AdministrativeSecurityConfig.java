package com.administrative.configs;

import com.hris.common.utilities.JwtFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class AdministrativeSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(AdministrativeSecurityConfig.class);

    private final JwtFilter jwtFilter;

    public AdministrativeSecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean(name = "administrativeSecurityFilterChain")
    public SecurityFilterChain administrativeSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(List.of("http://localhost:3080","http://localhost:3081","http://localhost:3082","http://localhost:3083","http://localhost:3084","http://localhost:3085","http://192.168.68.128:3082")); // Frontend URL (React/Next.js)
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                    corsConfiguration.setAllowCredentials(true); // If you need cookies/session
                    return corsConfiguration;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/employee/login", "/api/employee/register").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/secure").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // this is for h2-console
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//            .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
//                if (request.getRequestURI().startsWith("/h2-console")) {
//                    response.sendError(HttpServletResponse.SC_OK, "OK"); // Allow H2-console requests
//                } else {
//                    log.error("\uD83D\uDEA8 Authentication failed! {}", authException.getMessage());
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
//                }
//            }))
                .build();

    }

    @Bean(name = "administrativePasswordEncoder")
    public PasswordEncoder administrativePasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
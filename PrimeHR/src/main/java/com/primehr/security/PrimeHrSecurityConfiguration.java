package com.primehr.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.config.PrimeHrProperties;
import com.primehr.shared.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Configuration
public class PrimeHrSecurityConfiguration {

    @Bean
    public SecurityFilterChain primeHrSecurityFilterChain(HttpSecurity http,
                                                           PrimeHrJwtAuthenticationFilter jwtFilter,
                                                           @Qualifier("primeHrCorsConfigurationSource")
                                                           CorsConfigurationSource corsConfigurationSource,
                                                           ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/primehr/v1/admin/**").denyAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/primehr/v1/admin/**").denyAll()
                        .requestMatchers("/api/primehr/v1/admin/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/primehr/v1/**")
                        .hasAuthority(PrimeHrAuthorities.COMPETENCY_READ)
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> writeSecurityError(
                                response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                "Authentication required", request.getRequestURI()))
                        .accessDeniedHandler((request, response, exception) -> writeSecurityError(
                                response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                "Access denied", request.getRequestURI())))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean(name = "primeHrCorsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource(PrimeHrProperties properties) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(properties.cors().allowedOrigins());
        cors.setAllowedOriginPatterns(properties.cors().allowedOriginPatterns());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
        cors.setExposedHeaders(List.of("X-Correlation-Id"));
        cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public FilterRegistrationBean<PrimeHrJwtAuthenticationFilter> disableContainerJwtFilterRegistration(
            PrimeHrJwtAuthenticationFilter filter) {
        FilterRegistrationBean<PrimeHrJwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    private static void writeSecurityError(HttpServletResponse response, ObjectMapper objectMapper,
                                           int status, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ApiErrorResponse(Instant.now(), status, message, path, List.of()));
    }
}

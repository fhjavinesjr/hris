package com.hris.common.utilities;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().startsWith("/h2-console") ||
            request.getRequestURI().startsWith("/api/employee/login") ||
            request.getRequestURI().startsWith("/api/employee/register")) {

            filterChain.doFilter(request, response);
            return;
        }

        log.info("JwtFilter triggered for request: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Extracting claims from token: {}", token);

            try {
                DecodedJWT decodedJWT = jwtUtil.extractClaims(token);
                String employeeNo = decodedJWT.getSubject();
                String role = decodedJWT.getClaim("role").asString();
                log.info("Decoded JWT - Username: {}, Role: {}", employeeNo, role);

                if(employeeNo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(employeeNo, null, List.of(new SimpleGrantedAuthority(role)));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    log.info("Authentication SUCCESSFUL set for user: {}", employeeNo);
                }

            } catch(Exception e) {
                log.error("Authentication FAILED JWT Validation failed: {}", e.getMessage());
            }

        } else {
            log.warn("No Authorization header found in request");
        }

        log.info("\uD83D\uDD0D Final Authentication in SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());

        log.info("\uD83D\uDD0D SecurityContext BEFORE filtering: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
        log.info("\uD83D\uDD0D SecurityContext AFTER filtering: {}", SecurityContextHolder.getContext().getAuthentication());
    }
}

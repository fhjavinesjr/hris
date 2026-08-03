package com.primehr.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.primehr.config.PrimeHrProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrimeHrJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTVerifier verifier;
    private final CompetencyReadPermissionResolver permissionResolver;

    public PrimeHrJwtAuthenticationFilter(PrimeHrProperties properties,
                                          CompetencyReadPermissionResolver permissionResolver) {
        String secret = properties.security().jwtSecret();
        if (secret.isBlank()) {
            throw new IllegalStateException("PRIMEHR_JWT_SECRET must be configured");
        }
        this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        this.permissionResolver = permissionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                DecodedJWT jwt = verifier.verify(authorization.substring(7));
                String employeeNo = jwt.getSubject();
                String role = jwt.getClaim("role").asString();
                if (employeeNo != null && !employeeNo.isBlank()) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (role != null && !role.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }
                    if (permissionResolver.canReadCompetencies(employeeNo, role)) {
                        authorities.add(new SimpleGrantedAuthority(PrimeHrAuthorities.COMPETENCY_READ));
                    }
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(employeeNo, null, authorities));
                }
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}

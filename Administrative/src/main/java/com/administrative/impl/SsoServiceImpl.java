package com.administrative.impl;

import com.administrative.dtos.PermissionRulesetDTO;
import com.administrative.dtos.SsoExchangeResponse;
import com.administrative.dtos.SsoLaunchResponse;
import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.entitymodels.SsoLoginTicket;
import com.administrative.repositories.PermissionRulesetRepository;
import com.administrative.repositories.SsoLoginTicketRepository;
import com.administrative.services.SsoService;
import com.administrative.services.SystemConfigService;
import com.administrative.sso.SsoTarget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.common.utilities.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class SsoServiceImpl implements SsoService {
    private static final String INSTALL_ADMIN_EMPLOYEE_NO = "admin";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SsoLoginTicketRepository ticketRepository;
    private final PermissionRulesetRepository permissionRepository;
    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final long ticketTtlSeconds;

    public SsoServiceImpl(SsoLoginTicketRepository ticketRepository,
                          PermissionRulesetRepository permissionRepository,
                          SystemConfigService systemConfigService,
                          ObjectMapper objectMapper,
                          JwtUtil jwtUtil,
                          @Value("${security.sso.ticket-ttl-seconds:60}") long ticketTtlSeconds) {
        this.ticketRepository = ticketRepository;
        this.permissionRepository = permissionRepository;
        this.systemConfigService = systemConfigService;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.ticketTtlSeconds = Math.max(15, Math.min(ticketTtlSeconds, 300));
    }

    @Override
    @Transactional
    public SsoLaunchResponse launch(String employeeNo, String employeeRole, String requestedTarget) {
        SsoTarget target = parseTarget(requestedTarget);
        requireTargetAccess(employeeNo, employeeRole, target);

        Instant now = Instant.now();
        ticketRepository.deleteByExpiresAtBefore(now);
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant expiresAt = now.plusSeconds(ticketTtlSeconds);

        ticketRepository.save(new SsoLoginTicket(
                sha256(code), employeeNo, employeeRole, target.getValue(), now, expiresAt));
        return new SsoLaunchResponse(code, target.getValue(), expiresAt);
    }

    @Override
    @Transactional
    public SsoExchangeResponse exchange(String code, String requestedTarget) throws Exception {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SSO code is required");
        }

        SsoTarget target = parseTarget(requestedTarget);
        SsoLoginTicket ticket = ticketRepository.findForUpdateByCodeHash(sha256(code.trim()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "SSO code is invalid"));

        Instant now = Instant.now();
        if (ticket.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SSO code has already been used");
        }
        if (!now.isBefore(ticket.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SSO code has expired");
        }
        if (!ticket.getTargetApp().equals(target.getValue())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "SSO code is not valid for this application");
        }

        PermissionRuleset ruleset = requireTargetAccess(
                ticket.getEmployeeNo(), ticket.getEmployeeRole(), target);
        ticket.setConsumedAt(now);
        ticketRepository.save(ticket);

        return new SsoExchangeResponse(
                jwtUtil.generateToken(ticket.getEmployeeNo(), ticket.getEmployeeRole()),
                ticket.getEmployeeNo(),
                ticket.getEmployeeRole(),
                target.getValue(),
                toDto(ruleset),
                systemConfigService.getAllConfigs());
    }

    private SsoTarget parseTarget(String value) {
        try {
            return SsoTarget.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private PermissionRuleset requireTargetAccess(String employeeNo, String employeeRole, SsoTarget target) {
        if (INSTALL_ADMIN_EMPLOYEE_NO.equalsIgnoreCase(employeeNo)
                || "1".equals(employeeRole == null ? "" : employeeRole.trim().replaceFirst("(?i)^ROLE_", ""))) {
            return null;
        }

        PermissionRuleset ruleset = resolveRuleset(employeeRole)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No permission ruleset is assigned to this account"));
        if (Boolean.TRUE.equals(ruleset.getIsAdministrator())) {
            return ruleset;
        }

        try {
            JsonNode moduleAccess = objectMapper.readTree(ruleset.getPortalModuleAccess());
            if (moduleAccess != null && moduleAccess.path(target.getPermissionKey()).asBoolean(false)) {
                return ruleset;
            }
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The assigned permission ruleset has invalid Portal module access",
                    exception);
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You do not have access to " + target.getValue());
    }

    private Optional<PermissionRuleset> resolveRuleset(String employeeRole) {
        if (employeeRole == null || employeeRole.isBlank()) {
            return Optional.empty();
        }
        String normalizedRole = employeeRole.trim().replaceFirst("(?i)^ROLE_", "");
        try {
            return permissionRepository.findById(Long.valueOf(normalizedRole));
        } catch (NumberFormatException ignored) {
            return permissionRepository.findByPermissionNameIgnoreCase(normalizedRole);
        }
    }

    private PermissionRulesetDTO toDto(PermissionRuleset ruleset) {
        if (ruleset == null) return null;
        return new PermissionRulesetDTO(
                ruleset.getPermissionId(), ruleset.getPermissionName(), ruleset.getIsAdministrator(),
                ruleset.getPermissionData(), ruleset.getPortalModuleAccess());
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}

package com.administrative.repositories;

import com.administrative.dtos.SsoLaunchResponse;
import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.entitymodels.SsoLoginTicket;
import com.administrative.impl.SsoServiceImpl;
import com.administrative.services.SystemConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.common.utilities.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SsoServiceImplTest {
    @Mock private SsoLoginTicketRepository ticketRepository;
    @Mock private PermissionRulesetRepository permissionRepository;
    @Mock private SystemConfigService systemConfigService;
    @Mock private JwtUtil jwtUtil;
    private SsoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SsoServiceImpl(ticketRepository, permissionRepository,
                systemConfigService, new ObjectMapper(), jwtUtil, 60);
    }

    @Test
    void launchStoresOnlyHashedCodeAndBindsTarget() {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"administrative\":true}")));
        SsoLaunchResponse response = service.launch("001", "2", "administrative");

        ArgumentCaptor<SsoLoginTicket> captor = ArgumentCaptor.forClass(SsoLoginTicket.class);
        verify(ticketRepository).save(captor.capture());
        SsoLoginTicket saved = captor.getValue();
        assertEquals("administrative", saved.getTargetApp());
        assertNotEquals(response.code(), saved.getCodeHash());
        assertEquals(64, saved.getCodeHash().length());
    }

    @Test
    void launchRejectsModuleWithoutPermission() {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"administrative\":false}")));
        assertThrows(ResponseStatusException.class,
                () -> service.launch("001", "2", "administrative"));
    }

    @Test
    void exchangeConsumesTicketAndIssuesNormalJwt() throws Exception {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"hrManagement\":true}")));
        when(jwtUtil.generateToken("001", "2")).thenReturn("jwt");
        when(systemConfigService.getAllConfigs()).thenReturn(List.of());

        SsoLaunchResponse launch = service.launch("001", "2", "hrm");
        ArgumentCaptor<SsoLoginTicket> captor = ArgumentCaptor.forClass(SsoLoginTicket.class);
        verify(ticketRepository).save(captor.capture());
        SsoLoginTicket ticket = captor.getValue();
        when(ticketRepository.findForUpdateByCodeHash(anyString())).thenReturn(Optional.of(ticket));

        var response = service.exchange(launch.code(), "hrm");
        assertEquals("jwt", response.token());
        assertEquals("001", response.employeeNo());
        assertNotNull(ticket.getConsumedAt());
    }

    @Test
    void exchangeRejectsConsumedTicket() {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"payroll\":true}")));
        SsoLaunchResponse launch = service.launch("001", "2", "payroll");
        ArgumentCaptor<SsoLoginTicket> captor = ArgumentCaptor.forClass(SsoLoginTicket.class);
        verify(ticketRepository).save(captor.capture());
        SsoLoginTicket ticket = captor.getValue();
        ticket.setConsumedAt(java.time.Instant.now());
        when(ticketRepository.findForUpdateByCodeHash(anyString())).thenReturn(Optional.of(ticket));

        assertThrows(ResponseStatusException.class,
                () -> service.exchange(launch.code(), "payroll"));
    }

    @Test
    void exchangeRejectsCodeIssuedForAnotherApplication() {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"administrative\":true}")));
        SsoLaunchResponse launch = service.launch("001", "2", "administrative");
        ArgumentCaptor<SsoLoginTicket> captor = ArgumentCaptor.forClass(SsoLoginTicket.class);
        verify(ticketRepository).save(captor.capture());
        when(ticketRepository.findForUpdateByCodeHash(anyString()))
                .thenReturn(Optional.of(captor.getValue()));

        assertThrows(ResponseStatusException.class,
                () -> service.exchange(launch.code(), "payroll"));
    }

    @Test
    void primeHrLaunchAndExchangeUseTheDedicatedPortalPermission() throws Exception {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"primeHr\":true}")));
        when(jwtUtil.generateToken("001", "2")).thenReturn("prime-jwt");
        when(systemConfigService.getAllConfigs()).thenReturn(List.of());

        SsoLaunchResponse launch = service.launch("001", "2", "primehr");
        ArgumentCaptor<SsoLoginTicket> captor = ArgumentCaptor.forClass(SsoLoginTicket.class);
        verify(ticketRepository).save(captor.capture());
        when(ticketRepository.findForUpdateByCodeHash(anyString())).thenReturn(Optional.of(captor.getValue()));

        var exchange = service.exchange(launch.code(), "primehr");
        assertEquals("primehr", exchange.target());
        assertEquals("prime-jwt", exchange.token());
    }

    @Test
    void primeHrLaunchRejectsMissingModulePermission() {
        when(permissionRepository.findById(2L))
                .thenReturn(Optional.of(ruleset("{\"primeHr\":false}")));
        assertThrows(ResponseStatusException.class, () -> service.launch("001", "2", "primehr"));
    }

    @Test
    void installAdministratorCanLaunchPrimeHrWithoutARuleset() {
        SsoLaunchResponse launch = service.launch("admin", "1", "primehr");
        assertNotNull(launch.code());
        verify(ticketRepository).save(org.mockito.ArgumentMatchers.any(SsoLoginTicket.class));
    }

    @Test
    void establishedRoleOneCanLaunchPrimeHrWithoutARuleset() {
        SsoLaunchResponse launch = service.launch("001", "ROLE_1", "primehr");
        assertNotNull(launch.code());
        verify(ticketRepository).save(org.mockito.ArgumentMatchers.any(SsoLoginTicket.class));
    }

    @Test
    void roleOneUsesMatchingRulesetBeforeLegacyAdministratorFallback() throws Exception {
        PermissionRuleset userRuleset = new PermissionRuleset(
                "USER", false, "{}", "{\"hrManagement\":true}");
        userRuleset.setPermissionId(1L);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(userRuleset));
        when(jwtUtil.generateToken("001", "1")).thenReturn("user-jwt");
        when(systemConfigService.getAllConfigs()).thenReturn(List.of());

        SsoLaunchResponse launch = service.launch("001", "1", "hrm");
        ArgumentCaptor<SsoLoginTicket> captor = ArgumentCaptor.forClass(SsoLoginTicket.class);
        verify(ticketRepository).save(captor.capture());
        when(ticketRepository.findForUpdateByCodeHash(anyString()))
                .thenReturn(Optional.of(captor.getValue()));

        var exchange = service.exchange(launch.code(), "hrm");

        assertNotNull(exchange.permission());
        assertEquals("USER", exchange.permission().getPermissionName());
        assertFalse(exchange.permission().getIsAdministrator());
    }

    @Test
    void primeHrExchangeRejectsExpiredTicket() {
        SsoLoginTicket expired = new SsoLoginTicket("hash", "001", "2", "primehr",
                Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
        when(ticketRepository.findForUpdateByCodeHash(anyString())).thenReturn(Optional.of(expired));
        assertThrows(ResponseStatusException.class, () -> service.exchange("expired-code", "primehr"));
    }

    private PermissionRuleset ruleset(String portalModuleAccess) {
        PermissionRuleset ruleset = new PermissionRuleset("MANAGER", false, "{}", portalModuleAccess);
        ruleset.setPermissionId(2L);
        return ruleset;
    }
}

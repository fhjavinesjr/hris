package com.primehr.rsp.handoff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.config.PrimeHrProperties;
import com.primehr.integration.humanresource.HumanResourceAppointmentHandoffClient;
import com.primehr.rsp.applicant.infrastructure.*;
import com.primehr.rsp.handoff.application.AppointmentHandoffServiceImpl;
import com.primehr.rsp.handoff.domain.AppointmentHandoff;
import com.primehr.rsp.handoff.infrastructure.*;
import com.primehr.rsp.selection.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentHandoffDeliveryTest {
    private final AppointmentHandoffRepository handoffs=mock(AppointmentHandoffRepository.class);
    private final AppointmentHandoffAttemptRepository attempts=mock(AppointmentHandoffAttemptRepository.class);
    private final HumanResourceAppointmentHandoffClient client=mock(HumanResourceAppointmentHandoffClient.class);
    private final PrimeHrAuditService audit=mock(PrimeHrAuditService.class);
    private final AppointmentHandoffServiceImpl service=service();

    @Test
    void timeoutPersistsRetryableStateAndAnAttemptWithoutChangingPayload() {
        AppointmentHandoff value=handoff();stub(value);
        when(client.deliver(any())).thenReturn(new HumanResourceAppointmentHandoffClient.DeliveryResult("UNAVAILABLE",null,"timeout",null));
        var result=service.deliver("agency-1","handoff-1",0,"actor-1","submit");
        assertEquals("RETRYABLE_FAILURE",result.status());assertEquals("f".repeat(64),result.payloadFingerprint());
        verify(attempts).save(any());
    }

    @Test
    void reconciliationRecoversWhenThePostResponseWasLost() {
        AppointmentHandoff value=handoff();stub(value);
        when(client.deliver(any())).thenReturn(new HumanResourceAppointmentHandoffClient.DeliveryResult("UNAVAILABLE",null,"response lost",null));
        service.deliver("agency-1","handoff-1",0,"actor-1","submit");
        var receipt=new HumanResourceAppointmentHandoffClient.Receipt("receipt-1","handoff-1","agency-1",1,"f".repeat(64),"selection-1","application-1","applicant-1","RECEIVED",0);
        when(client.reconcile("agency-1","handoff-1","correlation-1")).thenReturn(new HumanResourceAppointmentHandoffClient.DeliveryResult("RECONCILED",200,null,receipt));
        var result=service.deliver("agency-1","handoff-1",0,"actor-1","reconcile");
        assertEquals("ACKNOWLEDGED",result.status());assertEquals("receipt-1",result.receiptId());
    }

    private void stub(AppointmentHandoff value){when(handoffs.findByIdAndAgencyId("handoff-1","agency-1")).thenReturn(Optional.of(value));when(handoffs.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));when(attempts.countByAgencyIdAndHandoffId("agency-1","handoff-1")).thenReturn(0L);when(attempts.findByAgencyIdAndHandoffIdOrderByAttemptNumberAsc("agency-1","handoff-1")).thenReturn(List.of());}
    private AppointmentHandoff handoff(){return new AppointmentHandoff("handoff-1","agency-1","selection-1","application-1","applicant-1",1,"{}","f".repeat(64),"correlation-1");}
    private AppointmentHandoffServiceImpl service(){PrimeHrProperties properties=mock(PrimeHrProperties.class);when(properties.humanResource()).thenReturn(new PrimeHrProperties.HumanResource("http://hrm",1000,1000));return new AppointmentHandoffServiceImpl(handoffs,attempts,mock(SelectionCaseRepository.class),mock(SelectionCandidateDecisionRepository.class),mock(OfferResponseRepository.class),mock(PositionApplicationRepository.class),mock(ApplicantProfileRepository.class),mock(ApplicantAccountRepository.class),client,audit,new ObjectMapper(),properties);}
}

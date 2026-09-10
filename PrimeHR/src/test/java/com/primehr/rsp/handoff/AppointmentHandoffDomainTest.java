package com.primehr.rsp.handoff;

import com.primehr.rsp.handoff.domain.AppointmentHandoff;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentHandoffDomainTest {
    @Test
    void enforcesDurableDeliveryRetryAndAcknowledgmentLifecycle() {
        AppointmentHandoff handoff=handoff();Instant now=Instant.now();
        handoff.ready(now);handoff.sent(now);handoff.deliveryFailed("timeout");
        assertEquals(AppointmentHandoff.Status.RETRYABLE_FAILURE,handoff.getStatus());
        handoff.sent(now.plusSeconds(1));handoff.acknowledge("receipt-1","RECEIVED",0,now.plusSeconds(1));
        assertEquals(AppointmentHandoff.Status.ACKNOWLEDGED,handoff.getStatus());
        assertEquals("receipt-1",handoff.getReceiptId());
    }

    @Test
    void cannotCancelAfterHumanResourceAcknowledgesTheHandoff() {
        AppointmentHandoff handoff=handoff();Instant now=Instant.now();handoff.ready(now);handoff.sent(now);handoff.acknowledge("receipt-1","RECEIVED",0,now);
        assertThrows(IllegalLifecycleTransitionException.class,()->handoff.cancel(now));
    }

    @Test
    void closesOnlyAfterHumanResourceReportsCompletion() {
        AppointmentHandoff handoff=handoff();Instant now=Instant.now();handoff.ready(now);handoff.sent(now);handoff.acknowledge("receipt-1","RECEIVED",0,now);
        assertThrows(IllegalLifecycleTransitionException.class,()->handoff.close(now));
        handoff.reconcileReceipt("COMPLETED",1);handoff.close(now.plusSeconds(1));
        assertEquals(AppointmentHandoff.Status.CLOSED,handoff.getStatus());
    }

    private AppointmentHandoff handoff(){return new AppointmentHandoff("handoff-1","agency-1","selection-1","application-1","applicant-1",1,"{}","f".repeat(64),"correlation-1");}
}

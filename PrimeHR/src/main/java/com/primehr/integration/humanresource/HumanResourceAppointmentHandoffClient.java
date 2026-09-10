package com.primehr.integration.humanresource;

import com.fasterxml.jackson.databind.JsonNode;

public interface HumanResourceAppointmentHandoffClient {
    DeliveryResult deliver(DeliveryRequest request);
    DeliveryResult reconcile(String agencyId, String handoffId, String correlationId);

    record DeliveryRequest(int schemaVersion,String handoffId,String agencyId,String selectionId,
                           String applicationId,String applicantId,String sourceFingerprint,
                           JsonNode payloadSnapshot,String correlationId,String sourceActor){}
    record Receipt(String receiptId,String handoffId,String agencyId,int schemaVersion,String sourceFingerprint,
                   String selectionId,String applicationId,String applicantId,String state,long recordVersion){}
    record DeliveryResult(String category,Integer httpStatus,String diagnostic,Receipt receipt){
        public boolean acknowledged(){return receipt!=null;}
    }
}

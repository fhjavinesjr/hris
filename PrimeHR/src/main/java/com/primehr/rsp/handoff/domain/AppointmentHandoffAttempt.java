package com.primehr.rsp.handoff.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Entity
@Table(name="rsp_appointment_handoff_attempt")
public class AppointmentHandoffAttempt extends RspAuditedEntity {
    @Column(name="handoff_id",nullable=false,length=36) private String handoffId;
    @Column(name="attempt_number",nullable=false) private int attemptNumber;
    @Column(name="attempted_at",nullable=false) private Instant attemptedAt;
    @Column(name="endpoint_identity",nullable=false,length=500) private String endpointIdentity;
    @Column(name="result_category",nullable=false,length=40) private String resultCategory;
    @Column(name="http_status") private Integer httpStatus;
    @Nationalized @Column(name="safe_diagnostic",length=1000) private String safeDiagnostic;
    @Column(name="payload_fingerprint",nullable=false,length=64) private String payloadFingerprint;
    protected AppointmentHandoffAttempt(){}
    public AppointmentHandoffAttempt(String agency,String handoff,int number,Instant at,String endpoint,String category,Integer status,String diagnostic,String fingerprint){super(agency);handoffId=requiredText(handoff,"handoffId");if(number<1)throw new IllegalArgumentException("attemptNumber must be positive");attemptNumber=number;attemptedAt=java.util.Objects.requireNonNull(at);endpointIdentity=requiredText(endpoint,"endpointIdentity");resultCategory=requiredText(category,"resultCategory");httpStatus=status;safeDiagnostic=optionalText(diagnostic);payloadFingerprint=requiredText(fingerprint,"payloadFingerprint");}
    public String getHandoffId(){return handoffId;}public int getAttemptNumber(){return attemptNumber;}public Instant getAttemptedAt(){return attemptedAt;}public String getEndpointIdentity(){return endpointIdentity;}public String getResultCategory(){return resultCategory;}public Integer getHttpStatus(){return httpStatus;}public String getSafeDiagnostic(){return safeDiagnostic;}public String getPayloadFingerprint(){return payloadFingerprint;}
}

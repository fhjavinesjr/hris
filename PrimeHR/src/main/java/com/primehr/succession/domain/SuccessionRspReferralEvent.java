package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="succession_rsp_referral_event",uniqueConstraints=
        @UniqueConstraint(name="uk_succ_rsp_referral_event_seq",columnNames={"agency_id","referral_id","event_sequence"}))
public class SuccessionRspReferralEvent extends RspAuditedEntity {
    @Column(name="referral_id",nullable=false,length=36) private String referralId;
    @Column(name="previous_event_id",length=36) private String previousEventId;
    @Column(name="event_sequence",nullable=false) private int eventSequence;
    @Column(name="event_type",nullable=false,length=30) private String eventType;
    @Column(name="resulting_status",nullable=false,length=30) private String resultingStatus;
    @Column(name="employee_visible_reason",nullable=false,length=4000) private String employeeVisibleReason;
    @Column(name="restricted_reason",nullable=false,length=4000) private String restrictedReason;
    @Column(name="recorded_by",nullable=false,length=100) private String recordedBy;
    @Column(name="recorded_at",nullable=false) private Instant recordedAt;
    protected SuccessionRspReferralEvent() { }
    public SuccessionRspReferralEvent(String agency,String referralId,SuccessionRspReferralEvent previous,String type,String status,String visible,String restricted,String actor,Instant at){super(agency);this.referralId=requiredText(referralId,"referralId");previousEventId=previous==null?null:previous.getId();eventSequence=previous==null?1:previous.eventSequence+1;eventType=requiredText(type,"eventType");resultingStatus=requiredText(status,"resultingStatus");employeeVisibleReason=requiredText(visible,"employeeVisibleReason");restrictedReason=requiredText(restricted,"restrictedReason");recordedBy=requiredText(actor,"actor");recordedAt=at;}
    public String getReferralId(){return referralId;}public String getPreviousEventId(){return previousEventId;}public int getEventSequence(){return eventSequence;}public String getEventType(){return eventType;}public String getResultingStatus(){return resultingStatus;}public String getEmployeeVisibleReason(){return employeeVisibleReason;}public String getRestrictedReason(){return restrictedReason;}public String getRecordedBy(){return recordedBy;}public Instant getRecordedAt(){return recordedAt;}
}

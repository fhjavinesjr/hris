package com.primehr.learningdevelopment.completion.domain;

import com.primehr.learningdevelopment.delivery.domain.LdOfferingSession;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;

@Entity
@Table(name="ld_session_attendance", uniqueConstraints=
        @UniqueConstraint(name="uk_ld_attendance_case_session",columnNames={"agency_id","completion_case_id","session_id"}))
public class LdSessionAttendance extends RspAuditedEntity {
    public enum Status { UNRECORDED, PRESENT, PARTIAL, ABSENT }
    @Column(name="completion_case_id",nullable=false,length=36) private String completionCaseId;
    @Column(name="session_id",nullable=false,length=36) private String sessionId;
    @Column(name="session_title",nullable=false,length=200) private String sessionTitle;
    @Column(name="starts_at",nullable=false) private OffsetDateTime startsAt;
    @Column(name="ends_at",nullable=false) private OffsetDateTime endsAt;
    @Column(name="planned_hours",nullable=false,precision=9,scale=2) private BigDecimal plannedHours;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="attended_hours",nullable=false,precision=9,scale=2) private BigDecimal attendedHours;
    @Column(name="credited_hours",nullable=false,precision=9,scale=2) private BigDecimal creditedHours;
    @Column(length=2000) private String remarks;
    @Column(name="recorded_by",length=100) private String recordedBy;
    @Column(name="recorded_at") private Instant recordedAt;
    protected LdSessionAttendance(){}
    public LdSessionAttendance(String agency,String caseId,LdOfferingSession session){super(agency);completionCaseId=requiredText(caseId,"completionCaseId");if(session==null)throw new IllegalArgumentException("session is required");sessionId=requiredText(session.getId(),"sessionId");sessionTitle=requiredText(session.getTitle(),"sessionTitle");startsAt=session.getStartsAt();endsAt=session.getEndsAt();plannedHours=session.getCreditedHours().setScale(2,RoundingMode.HALF_UP);status=Status.UNRECORDED;attendedHours=zero();creditedHours=zero();}
    public void record(Status status,BigDecimal attended,BigDecimal credited,String remarks,String actor,Instant at){if(status==null||status==Status.UNRECORDED)throw new IllegalArgumentException("A recorded attendance status is required");BigDecimal actual=scale(attended,"attendedHours"),credit=scale(credited,"creditedHours");if(actual.compareTo(plannedHours)>0||credit.compareTo(actual)>0||credit.compareTo(plannedHours)>0)throw new IllegalArgumentException("Attendance and credited hours cannot exceed the session limits");if(status==Status.ABSENT&&(actual.signum()!=0||credit.signum()!=0))throw new IllegalArgumentException("Absent attendance cannot have hours");if(status==Status.PRESENT&&actual.signum()<=0)throw new IllegalArgumentException("Present attendance requires positive hours");this.status=status;attendedHours=actual;creditedHours=credit;this.remarks=optionalText(remarks);recordedBy=requiredText(actor,"actor");recordedAt=at==null?Instant.now():at;}
    private static BigDecimal scale(BigDecimal v,String n){if(v==null||v.signum()<0)throw new IllegalArgumentException(n+" cannot be negative");return v.setScale(2,RoundingMode.HALF_UP);}private static BigDecimal zero(){return BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP);}
    public String getCompletionCaseId(){return completionCaseId;}public String getSessionId(){return sessionId;}public String getSessionTitle(){return sessionTitle;}public OffsetDateTime getStartsAt(){return startsAt;}public OffsetDateTime getEndsAt(){return endsAt;}public BigDecimal getPlannedHours(){return plannedHours;}public Status getStatus(){return status;}public BigDecimal getAttendedHours(){return attendedHours;}public BigDecimal getCreditedHours(){return creditedHours;}public String getRemarks(){return remarks;}public String getRecordedBy(){return recordedBy;}public Instant getRecordedAt(){return recordedAt;}
}

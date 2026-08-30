package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="rsp_applicant_account", uniqueConstraints=@UniqueConstraint(name="uk_rsp_applicant_email",columnNames={"agency_id","normalized_email"}))
public class ApplicantAccount extends RspAuditedEntity {
    public enum Status { ACTIVE, LOCKED, DISABLED }
    @Column(name="normalized_email",nullable=false,length=320) private String normalizedEmail;
    @Column(name="email",nullable=false,length=320) private String email;
    @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
    @Column(name="display_name",nullable=false,length=200) private String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="failed_attempts",nullable=false) private int failedAttempts;
    @Column(name="locked_until") private Instant lockedUntil;
    @Column(name="last_login_at") private Instant lastLoginAt;
    protected ApplicantAccount() {}
    public ApplicantAccount(String agency,String email,String hash,String name){super(agency);this.email=requiredText(email,"email");normalizedEmail=normalize(email);passwordHash=requiredText(hash,"passwordHash");displayName=requiredText(name,"displayName");status=Status.ACTIVE;}
    public static String normalize(String email){return requiredText(email,"email").toLowerCase(java.util.Locale.ROOT);}
    public boolean lockedAt(Instant now){return status==Status.DISABLED||(status==Status.LOCKED&&lockedUntil!=null&&lockedUntil.isAfter(now));}
    public void failed(Instant now,int max,long minutes){failedAttempts++;if(failedAttempts>=max){status=Status.LOCKED;lockedUntil=now.plusSeconds(minutes*60);}}
    public void login(Instant now){status=Status.ACTIVE;failedAttempts=0;lockedUntil=null;lastLoginAt=now;}
    public void changeAccount(String newEmail,String newDisplayName){email=requiredText(newEmail,"email");normalizedEmail=normalize(newEmail);displayName=requiredText(newDisplayName,"displayName");}
    public void changeProfileName(String value){displayName=requiredText(value,"displayName");}
    public String getNormalizedEmail(){return normalizedEmail;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getDisplayName(){return displayName;} public Status getStatus(){return status;} public int getFailedAttempts(){return failedAttempts;} public Instant getLockedUntil(){return lockedUntil;} public Instant getLastLoginAt(){return lastLoginAt;}
}

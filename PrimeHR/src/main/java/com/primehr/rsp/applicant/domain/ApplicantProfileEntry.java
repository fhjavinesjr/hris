package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="rsp_applicant_profile_entry",uniqueConstraints=@UniqueConstraint(name="uk_rsp_profile_entry_order",columnNames={"agency_id","profile_id","entry_type","display_order"}))
public class ApplicantProfileEntry extends RspAuditedEntity {
    public enum Type { EDUCATION, WORK_EXPERIENCE, TRAINING, ELIGIBILITY, LICENSE, REFERENCE }
    @Column(name="profile_id",nullable=false,length=36) private String profileId;
    @Enumerated(EnumType.STRING) @Column(name="entry_type",nullable=false,length=30) private Type type;
    @Column(nullable=false,length=300) private String title;
    @Column(name="organization_name",length=300) private String organizationName;
    @Column(name="date_from") private LocalDate dateFrom;
    @Column(name="date_to") private LocalDate dateTo;
    @Column(length=2000) private String details;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected ApplicantProfileEntry(){}
    public ApplicantProfileEntry(String agency,String profile,Type type,String title,String organization,LocalDate from,LocalDate to,String details,int order){super(agency);profileId=requiredText(profile,"profileId");this.type=java.util.Objects.requireNonNull(type);this.title=requiredText(title,"title");organizationName=optionalText(organization);dateFrom=from;if(to!=null&&from!=null&&to.isBefore(from))throw new IllegalArgumentException("dateTo cannot precede dateFrom");dateTo=to;this.details=optionalText(details);if(order<0)throw new IllegalArgumentException("displayOrder cannot be negative");displayOrder=order;}
    public String getProfileId(){return profileId;} public Type getType(){return type;} public String getTitle(){return title;} public String getOrganizationName(){return organizationName;} public LocalDate getDateFrom(){return dateFrom;} public LocalDate getDateTo(){return dateTo;} public String getDetails(){return details;} public int getDisplayOrder(){return displayOrder;}
}

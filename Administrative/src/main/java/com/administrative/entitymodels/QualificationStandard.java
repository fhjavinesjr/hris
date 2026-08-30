package com.administrative.entitymodels;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="qualification_standard", uniqueConstraints={
        @UniqueConstraint(name="uk_qs_job_version", columnNames={"jobPositionId","definitionVersion"})})
public class QualificationStandard {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="qualificationStandardId") private Long id;
    @Column(name="jobPositionId",nullable=false) private Long jobPositionId;
    @Column(name="definitionVersion",nullable=false) private int definitionVersion;
    @Column(name="supersedesId") private Long supersedesId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private QualificationStandardStatus status;
    @Column(nullable=false,length=2000) private String education;
    @Column(nullable=false,length=2000) private String training;
    @Column(nullable=false,length=2000) private String experience;
    @Column(nullable=false,length=2000) private String eligibility;
    @Column(length=2000) private String licenseRequirement;
    @Column(length=1000) private String sourceBasis;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Column(nullable=false,length=100) private String createdBy;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false,length=100) private String updatedBy;
    @Column(nullable=false) private Instant updatedAt;
    @Column(length=100) private String publishedBy;
    private Instant publishedAt;
    @Version @Column(nullable=false) private long recordVersion;

    protected QualificationStandard() {}

    public static QualificationStandard draft(Long jobPositionId,int version,Long supersedesId,
            String education,String training,String experience,String eligibility,String license,
            String sourceBasis,LocalDate from,LocalDate to,String actor){
        QualificationStandard q=new QualificationStandard();q.jobPositionId=jobPositionId;
        q.definitionVersion=version;q.supersedesId=supersedesId;q.status=QualificationStandardStatus.DRAFT;
        q.apply(education,training,experience,eligibility,license,sourceBasis,from,to,actor);
        q.createdBy=actor;q.createdAt=q.updatedAt;return q;
    }
    public void update(String education,String training,String experience,String eligibility,String license,
            String sourceBasis,LocalDate from,LocalDate to,String actor){requireDraft();apply(education,training,
            experience,eligibility,license,sourceBasis,from,to,actor);}
    public void archive(String actor){requireDraft();status=QualificationStandardStatus.ARCHIVED;touch(actor);}
    public void publish(String actor,Instant at){requireDraft();status=QualificationStandardStatus.ACTIVE;
        publishedBy=actor;publishedAt=at;touch(actor);}
    public void close(LocalDate date,String actor){if(status!=QualificationStandardStatus.ACTIVE)throw new IllegalStateException("Only ACTIVE standards can be closed");effectiveTo=date;touch(actor);}
    private void apply(String e,String t,String x,String elig,String license,String basis,LocalDate from,LocalDate to,String actor){
        education=text(e,"education");training=text(t,"training");experience=text(x,"experience");eligibility=text(elig,"eligibility");
        licenseRequirement=optional(license);sourceBasis=optional(basis);if(from!=null&&to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot be before effectiveFrom");
        effectiveFrom=from;effectiveTo=to;touch(actor);
    }
    private void touch(String actor){updatedBy=text(actor,"actor");updatedAt=Instant.now();}
    private void requireDraft(){if(status!=QualificationStandardStatus.DRAFT)throw new IllegalStateException("Only DRAFT qualification standards can be changed");}
    private static String text(String v,String f){if(v==null||v.isBlank())throw new IllegalArgumentException(f+" is required");return v.trim();}
    private static String optional(String v){return v==null||v.isBlank()?null:v.trim();}
    public Long getId(){return id;} public Long getJobPositionId(){return jobPositionId;} public int getDefinitionVersion(){return definitionVersion;}
    public Long getSupersedesId(){return supersedesId;} public QualificationStandardStatus getStatus(){return status;}
    public String getEducation(){return education;} public String getTraining(){return training;} public String getExperience(){return experience;}
    public String getEligibility(){return eligibility;} public String getLicenseRequirement(){return licenseRequirement;} public String getSourceBasis(){return sourceBasis;}
    public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;}
    public String getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;} public String getUpdatedBy(){return updatedBy;}
    public Instant getUpdatedAt(){return updatedAt;} public String getPublishedBy(){return publishedBy;} public Instant getPublishedAt(){return publishedAt;}
    public long getRecordVersion(){return recordVersion;}
}

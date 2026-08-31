package com.primehr.rsp.screening.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "rsp_screening_policy", uniqueConstraints = @UniqueConstraint(
        name = "uk_rsp_screening_policy_version", columnNames = {"agency_id", "normalized_code", "definition_version"}))
public class ScreeningPolicy extends RspAuditedEntity {
    public enum Status { DRAFT, PUBLISHED, SUPERSEDED }
    @Column(nullable=false,length=80) private String code;
    @Column(name="normalized_code",nullable=false,length=80) private String normalizedCode;
    @Column(nullable=false,length=200) private String name;
    @Column(length=2000) private String description;
    @Column(name="definition_version",nullable=false) private int definitionVersion;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="effective_from") private LocalDate effectiveFrom;
    @Column(name="effective_to") private LocalDate effectiveTo;
    @Column(name="published_by",length=100) private String publishedBy;
    @Column(name="published_at") private Instant publishedAt;
    protected ScreeningPolicy() {}
    public ScreeningPolicy(String agency,String code,String name,String description,int version,String supersedesId) {
        super(agency); apply(code,name,description,null,null); if(version<1) throw new IllegalArgumentException("definitionVersion must be positive");
        this.definitionVersion=version; this.supersedesId=optionalText(supersedesId); status=Status.DRAFT;
    }
    public void update(String code,String name,String description,LocalDate from,LocalDate to) { requireDraft(); apply(code,name,description,from,to); }
    public void publish(LocalDate from,LocalDate to,String actor,Instant at) { requireDraft(); if(from==null) throw new IllegalArgumentException("effectiveFrom is required"); validateDates(from,to); effectiveFrom=from; effectiveTo=to; publishedBy=requiredText(actor,"actor"); publishedAt=Objects.requireNonNull(at); status=Status.PUBLISHED; }
    public void supersede(LocalDate successorEffectiveFrom){
        if(status!=Status.PUBLISHED) throw new IllegalLifecycleTransitionException("Only PUBLISHED policies may be superseded");
        Objects.requireNonNull(successorEffectiveFrom,"successorEffectiveFrom");
        if(!successorEffectiveFrom.isAfter(effectiveFrom)) throw new IllegalArgumentException("A successor must become effective after its predecessor");
        LocalDate predecessorEnd=successorEffectiveFrom.minusDays(1);
        if(effectiveTo==null||effectiveTo.isAfter(predecessorEnd)) effectiveTo=predecessorEnd;
        status=Status.SUPERSEDED;
    }
    private void apply(String code,String name,String description,LocalDate from,LocalDate to){this.code=requiredText(code,"code");normalizedCode=this.code.toUpperCase(java.util.Locale.ROOT);this.name=requiredText(name,"name");this.description=optionalText(description);validateDates(from,to);effectiveFrom=from;effectiveTo=to;}
    private static void validateDates(LocalDate from,LocalDate to){if(from!=null&&to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");}
    private void requireDraft(){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Published screening policies are immutable; create a successor");}
    public String getCode(){return code;} public String getNormalizedCode(){return normalizedCode;} public String getName(){return name;} public String getDescription(){return description;} public int getDefinitionVersion(){return definitionVersion;} public String getSupersedesId(){return supersedesId;} public Status getStatus(){return status;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public String getPublishedBy(){return publishedBy;} public Instant getPublishedAt(){return publishedAt;}
}

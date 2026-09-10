package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "spms_commitment_route", uniqueConstraints =
        @UniqueConstraint(name = "uk_spms_commitment_route_revision", columnNames = {"agency_id", "commitment_version_id", "route_revision"}))
public class PerformanceCommitmentRoute extends RspAuditedEntity {
    public enum Status { ACTIVE, RETURNED, COMPLETED, REJECTED, WITHDRAWN, SUPERSEDED, VOIDED }
    @Column(name="commitment_version_id",nullable=false,length=36) private String commitmentVersionId;
    @Column(name="route_revision",nullable=false) private int routeRevision;
    @Column(name="request_code",nullable=false,length=100) private String requestCode;
    @Column(name="business_unit_id",nullable=false) private Long businessUnitId;
    @Column(name="organization_fingerprint",nullable=false,length=64) private String organizationFingerprint;
    @Column(name="route_fingerprint",nullable=false,length=64) private String routeFingerprint;
    @Column(name="submitted_content_fingerprint",nullable=false,length=64) private String submittedContentFingerprint;
    @Column(name="submitted_content_revision",nullable=false) private int submittedContentRevision;
    @Column(name="current_level",nullable=false) private int currentLevel;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="submitted_by",nullable=false,length=100) private String submittedBy;
    @Column(name="submitted_at",nullable=false) private Instant submittedAt;
    protected PerformanceCommitmentRoute() {}
    public PerformanceCommitmentRoute(String agency,String version,int revision,String requestCode,Long unit,String organizationFingerprint,String routeFingerprint,String contentFingerprint,int contentRevision,String actor,Instant at){
        super(agency);commitmentVersionId=requiredText(version,"commitmentVersionId");if(revision<1)throw new IllegalArgumentException("routeRevision must be positive");routeRevision=revision;this.requestCode=requiredText(requestCode,"requestCode");businessUnitId=unit;this.organizationFingerprint=requiredText(organizationFingerprint,"organizationFingerprint");this.routeFingerprint=requiredText(routeFingerprint,"routeFingerprint");submittedContentFingerprint=requiredText(contentFingerprint,"submittedContentFingerprint");submittedContentRevision=contentRevision;currentLevel=1;status=Status.ACTIVE;submittedBy=requiredText(actor,"submittedBy");submittedAt=at;
    }
    public void advance(int next){currentLevel=next;}
    public void finish(Status value){status=value;}
    public String getCommitmentVersionId(){return commitmentVersionId;} public int getRouteRevision(){return routeRevision;} public String getRequestCode(){return requestCode;} public Long getBusinessUnitId(){return businessUnitId;} public String getOrganizationFingerprint(){return organizationFingerprint;} public String getRouteFingerprint(){return routeFingerprint;} public String getSubmittedContentFingerprint(){return submittedContentFingerprint;} public int getSubmittedContentRevision(){return submittedContentRevision;} public int getCurrentLevel(){return currentLevel;} public Status getStatus(){return status;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;}
}

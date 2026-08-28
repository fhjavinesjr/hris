package com.primehr.assessment.domain;

import com.primehr.competency.domain.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity @EntityListeners(AuditingEntityListener.class)
@Table(name="prime_person_competency_result",uniqueConstraints=@UniqueConstraint(name="uk_prime_person_result",columnNames={"person_profile_id","competency_id"}))
public class PersonCompetencyResult {
 @Id @Column(length=36,nullable=false,updatable=false) private String id;
 @Column(name="agency_id",length=64,nullable=false,updatable=false) private String agencyId;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="person_profile_id",nullable=false,updatable=false) private PersonCompetencyProfile personProfile;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="competency_id",nullable=false,updatable=false) private Competency competency;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="attained_proficiency_level_id",nullable=false,updatable=false) private ProficiencyLevel attainedLevel;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="validated_rating_id",nullable=false,updatable=false,unique=true) private AssessmentValidatedRating validatedRating;
 @Column(nullable=false,updatable=false) private boolean active;
 @Version @Column(name="record_version",nullable=false) private long version;
 @CreatedBy @Column(name="created_by",length=100,nullable=false,updatable=false) private String createdBy;
 @CreatedDate @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @LastModifiedBy @Column(name="updated_by",length=100,nullable=false) private String updatedBy;
 @LastModifiedDate @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected PersonCompetencyResult(){}
 public PersonCompetencyResult(String agencyId,PersonCompetencyProfile profile,AssessmentValidatedRating rating){if(!agencyId.equals(profile.getAgencyId()))throw new IllegalArgumentException("Result agency does not match");this.agencyId=agencyId;this.personProfile=profile;this.validatedRating=rating;this.competency=rating.getCompetency();this.attainedLevel=rating.getFinalLevel();this.active=true;}
 @PrePersist void assignId(){if(id==null)id=UUID.randomUUID().toString();}
 public String getId(){return id;} public Competency getCompetency(){return competency;} public ProficiencyLevel getAttainedLevel(){return attainedLevel;} public AssessmentValidatedRating getValidatedRating(){return validatedRating;}
}

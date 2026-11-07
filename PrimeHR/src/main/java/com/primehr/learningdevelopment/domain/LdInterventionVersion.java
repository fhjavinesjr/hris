package com.primehr.learningdevelopment.domain;
import jakarta.persistence.*;
@Entity @Table(name="ld_intervention_version",uniqueConstraints=@UniqueConstraint(name="uk_ld_intervention_version",columnNames={"agency_id","intervention_id","definition_version"}))
public class LdInterventionVersion extends LdVersionedDefinition {
 public enum Category{CLASSROOM,ONLINE_BLENDED,COACHING,MENTORING,JOB_ROTATION,JOB_SHADOWING,SELF_DEVELOPMENT,SCHOLARSHIP,SPECIAL_PROJECT,KNOWLEDGE_SHARING,OTHER}
 public enum Modality{IN_PERSON,ONLINE,BLENDED,WORKPLACE,SELF_DIRECTED,OTHER}
 @Column(name="intervention_id",nullable=false,length=36)private String interventionId;@Enumerated(EnumType.STRING)@Column(nullable=false,length=30)private Category category;@Enumerated(EnumType.STRING)@Column(name="default_modality",nullable=false,length=30)private Modality defaultModality;@Column(name="intended_outcomes",nullable=false,length=3000)private String intendedOutcomes;@Column(name="delivery_requirements",length=3000)private String deliveryRequirements;
 protected LdInterventionVersion(){}
 public LdInterventionVersion(String agency,String intervention,int version,String prior,String title,String description,Category category,Modality modality,String outcomes,String requirements){super(agency,version,prior,title,description);interventionId=requiredText(intervention,"interventionId");apply(category,modality,outcomes,requirements);}
 public void update(String title,String description,Category category,Modality modality,String outcomes,String requirements){changeDefinition(title,description);apply(category,modality,outcomes,requirements);}
 private void apply(Category c,Modality m,String o,String r){category=java.util.Objects.requireNonNull(c,"category");defaultModality=java.util.Objects.requireNonNull(m,"defaultModality");intendedOutcomes=requiredText(o,"intendedOutcomes");deliveryRequirements=optionalText(r);}
 public String getInterventionId(){return interventionId;}public Category getCategory(){return category;}public Modality getDefaultModality(){return defaultModality;}public String getIntendedOutcomes(){return intendedOutcomes;}public String getDeliveryRequirements(){return deliveryRequirements;}
}

package com.primehr.learningdevelopment.domain;
import com.primehr.rsp.domain.RspAuditedEntity;import jakarta.persistence.*;import java.util.*;
@Entity @Table(name="ld_provider",uniqueConstraints=@UniqueConstraint(name="uk_ld_provider_code",columnNames={"agency_id","normalized_code"}))
public class LdProvider extends RspAuditedEntity {
 public enum Type{INTERNAL_GOVERNMENT,EXTERNAL_GOVERNMENT,ACADEMIC,PROFESSIONAL,PRIVATE,INTERNATIONAL,OTHER} public enum Status{ACTIVE,INACTIVE}
 @Column(nullable=false,length=80)private String code;@Column(name="normalized_code",nullable=false,length=80)private String normalizedCode;@Column(nullable=false,length=250)private String name;@Enumerated(EnumType.STRING)@Column(name="provider_type",nullable=false,length=30)private Type providerType;@Column(name="accreditation_reference",length=500)private String accreditationReference;@Column(name="contact_details",length=1000)private String contactDetails;@Enumerated(EnumType.STRING)@Column(nullable=false,length=20)private Status status;
 protected LdProvider(){}public LdProvider(String agency,String code,String name,Type type,String accreditation,String contact){super(agency);status=Status.ACTIVE;apply(code,name,type,accreditation,contact);}
 public void update(String code,String name,Type type,String accreditation,String contact){apply(code,name,type,accreditation,contact);}public void deactivate(){status=Status.INACTIVE;}public void activate(){status=Status.ACTIVE;}
 private void apply(String c,String n,Type t,String a,String d){code=requiredText(c,"code");normalizedCode=code.toUpperCase(Locale.ROOT);name=requiredText(n,"name");providerType=Objects.requireNonNull(t,"providerType");accreditationReference=optionalText(a);contactDetails=optionalText(d);}
 public String getCode(){return code;}public String getName(){return name;}public Type getProviderType(){return providerType;}public String getAccreditationReference(){return accreditationReference;}public String getContactDetails(){return contactDetails;}public Status getStatus(){return status;}
}

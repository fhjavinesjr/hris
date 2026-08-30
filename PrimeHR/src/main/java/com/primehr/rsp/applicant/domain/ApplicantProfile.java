package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="rsp_applicant_profile",uniqueConstraints=@UniqueConstraint(name="uk_rsp_applicant_profile",columnNames={"agency_id","applicant_id"}))
public class ApplicantProfile extends RspAuditedEntity {
    @Column(name="applicant_id",nullable=false,length=36) private String applicantId;
    @Column(name="given_name",nullable=false,length=100) private String givenName;
    @Column(name="middle_name",length=100) private String middleName;
    @Column(name="family_name",nullable=false,length=100) private String familyName;
    @Column(name="suffix",length=30) private String suffix;
    @Column(name="birth_date") private LocalDate birthDate;
    @Column(name="mobile_number",length=50) private String mobileNumber;
    @Column(name="address_line",length=500) private String addressLine;
    @Column(name="city",length=100) private String city;
    @Column(name="province",length=100) private String province;
    @Column(name="postal_code",length=20) private String postalCode;
    @Column(name="citizenship",length=100) private String citizenship;
    @Column(name="declaration_accepted",nullable=false) private boolean declarationAccepted;
    protected ApplicantProfile(){}
    public ApplicantProfile(String agency,String applicant,String given,String family){super(agency);applicantId=requiredText(applicant,"applicantId");givenName=requiredText(given,"givenName");familyName=requiredText(family,"familyName");}
    public void update(String given,String middle,String family,String suffix,LocalDate birth,String mobile,String address,String city,String province,String postal,String citizenship,boolean declaration){givenName=requiredText(given,"givenName");middleName=optionalText(middle);familyName=requiredText(family,"familyName");this.suffix=optionalText(suffix);birthDate=birth;mobileNumber=optionalText(mobile);addressLine=optionalText(address);this.city=optionalText(city);this.province=optionalText(province);postalCode=optionalText(postal);this.citizenship=optionalText(citizenship);declarationAccepted=declaration;}
    public String getApplicantId(){return applicantId;} public String getGivenName(){return givenName;} public String getMiddleName(){return middleName;} public String getFamilyName(){return familyName;} public String getSuffix(){return suffix;} public LocalDate getBirthDate(){return birthDate;} public String getMobileNumber(){return mobileNumber;} public String getAddressLine(){return addressLine;} public String getCity(){return city;} public String getProvince(){return province;} public String getPostalCode(){return postalCode;} public String getCitizenship(){return citizenship;} public boolean isDeclarationAccepted(){return declarationAccepted;}
}

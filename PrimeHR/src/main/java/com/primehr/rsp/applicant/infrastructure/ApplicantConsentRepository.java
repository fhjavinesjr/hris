package com.primehr.rsp.applicant.infrastructure;
import com.primehr.rsp.applicant.domain.ApplicantConsent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ApplicantConsentRepository extends JpaRepository<ApplicantConsent,String>{boolean existsByAgencyIdAndApplicantIdAndNoticeIdAndWithdrawnAtIsNull(String agency,String applicant,String notice);}

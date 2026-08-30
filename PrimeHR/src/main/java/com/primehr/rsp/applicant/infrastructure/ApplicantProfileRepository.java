package com.primehr.rsp.applicant.infrastructure;
import com.primehr.rsp.applicant.domain.ApplicantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ApplicantProfileRepository extends JpaRepository<ApplicantProfile,String>{Optional<ApplicantProfile> findByAgencyIdAndApplicantId(String agency,String applicant);}

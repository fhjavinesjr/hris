package com.primehr.rsp.applicant.infrastructure;
import com.primehr.rsp.applicant.domain.ApplicantAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ApplicantAccountRepository extends JpaRepository<ApplicantAccount,String>{Optional<ApplicantAccount> findByAgencyIdAndNormalizedEmail(String agency,String email);Optional<ApplicantAccount> findByIdAndAgencyId(String id,String agency);}

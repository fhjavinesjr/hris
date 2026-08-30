package com.primehr.rsp.applicant.infrastructure;
import com.primehr.rsp.applicant.domain.ApplicantDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;import java.util.Optional;
public interface ApplicantDocumentRepository extends JpaRepository<ApplicantDocument,String>{List<ApplicantDocument> findByAgencyIdAndApplicantIdAndActiveTrueOrderByUploadedAtDesc(String agency,String applicant);Optional<ApplicantDocument> findByIdAndAgencyIdAndApplicantId(String id,String agency,String applicant);}

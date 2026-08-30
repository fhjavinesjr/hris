package com.primehr.rsp.applicant.infrastructure;
import com.primehr.rsp.applicant.domain.ApplicantProfileEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ApplicantProfileEntryRepository extends JpaRepository<ApplicantProfileEntry,String>{List<ApplicantProfileEntry> findByAgencyIdAndProfileIdOrderByTypeAscDisplayOrderAsc(String agency,String profile);void deleteByAgencyIdAndProfileId(String agency,String profile);}

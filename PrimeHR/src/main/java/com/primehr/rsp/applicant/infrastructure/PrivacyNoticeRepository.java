package com.primehr.rsp.applicant.infrastructure;
import com.primehr.rsp.applicant.domain.PrivacyNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PrivacyNoticeRepository extends JpaRepository<PrivacyNotice,String>{List<PrivacyNotice> findByAgencyIdAndStatusOrderByDefinitionVersionDesc(String agency,PrivacyNotice.Status status);}

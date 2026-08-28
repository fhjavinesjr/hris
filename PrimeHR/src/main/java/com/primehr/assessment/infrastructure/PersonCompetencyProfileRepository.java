package com.primehr.assessment.infrastructure;
import com.primehr.assessment.domain.PersonCompetencyProfile;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PersonCompetencyProfileRepository extends JpaRepository<PersonCompetencyProfile,String>{
 Optional<PersonCompetencyProfile> findByAgencyIdAndId(String agencyId,String id);
 Optional<PersonCompetencyProfile> findFirstByAgencyIdAndSubjectEmployeeNoIgnoreCaseAndValidFromLessThanEqualAndValidToIsNullOrderByProfileVersionDescValidatedAtDesc(
   String agencyId,String employeeNo,LocalDate asOf);
 Optional<PersonCompetencyProfile> findFirstByAgencyIdAndSubjectEmployeeNoIgnoreCaseOrderByProfileVersionDescValidatedAtDesc(
   String agencyId,String employeeNo);
 Page<PersonCompetencyProfile> findByAgencyIdAndSubjectEmployeeNoIgnoreCase(String agencyId,String employeeNo,Pageable pageable);
 boolean existsByAssessmentCaseId(String caseId);
 @Query("select profile from PersonCompetencyProfile profile where profile.agencyId=:agencyId " +
   "and upper(profile.subjectEmployeeNo)=upper(:employeeNo) and profile.validFrom<=:asOf " +
   "and (profile.validTo is null or profile.validTo>=:asOf) order by profile.profileVersion desc, profile.validatedAt desc")
 List<PersonCompetencyProfile> findEffective(@Param("agencyId") String agencyId,@Param("employeeNo") String employeeNo,
                                             @Param("asOf") LocalDate asOf,Pageable pageable);
}

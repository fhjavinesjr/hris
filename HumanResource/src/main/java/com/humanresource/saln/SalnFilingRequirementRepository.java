package com.humanresource.saln;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SalnFilingRequirementRepository extends JpaRepository<SalnFilingRequirement,Long>{List<SalnFilingRequirement> findByEmployeeIdOrderByDueDateDesc(Long employeeId);Optional<SalnFilingRequirement> findByEmployeeIdAndFilingTypeAndReferenceDate(Long employeeId,SalnTypes.FilingType type,java.time.LocalDate referenceDate);boolean existsByEmployeeIdAndFilingTypeAndReferenceDate(Long employeeId,SalnTypes.FilingType type,java.time.LocalDate referenceDate);}

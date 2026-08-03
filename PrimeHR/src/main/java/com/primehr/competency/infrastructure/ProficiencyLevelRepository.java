package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProficiencyLevelRepository extends JpaRepository<ProficiencyLevel, String> {
}

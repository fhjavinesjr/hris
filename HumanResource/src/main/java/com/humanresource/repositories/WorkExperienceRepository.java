package com.humanresource.repositories;

import com.humanresource.entitymodels.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {

    List<WorkExperience> findByPersonalDataId(Long personalDataId);

    int deleteByPersonalDataId(Long personalDataId);

}

package com.humanresource.repositories;

import com.humanresource.entitymodels.LearningAndDevelopment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningAndDevelopmentRepository extends JpaRepository<LearningAndDevelopment, Long> {

    List<LearningAndDevelopment> findByPersonalDataId(Long personalDataId);

    int deleteByPersonalDataId(Long personalDataId);

}
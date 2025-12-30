package com.humanresource.repositories;

import com.humanresource.entitymodels.EducationalBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackground, Long> {

    List<EducationalBackground> findByPersonalDataId(Long personalDataId);

    int deleteByPersonalDataId(Long personalDataId);

}

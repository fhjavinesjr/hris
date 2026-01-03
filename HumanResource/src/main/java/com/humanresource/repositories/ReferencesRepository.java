package com.humanresource.repositories;

import com.humanresource.entitymodels.References;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferencesRepository extends JpaRepository<References, Long> {

    List<References> findByPersonalDataId(Long personalDataId);

    int deleteByPersonalDataId(Long personalDataId);

}
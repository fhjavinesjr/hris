package com.humanresource.repositories;

import com.humanresource.entitymodels.VoluntaryWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoluntaryWorkRepository extends JpaRepository<VoluntaryWork, Long> {

    List<VoluntaryWork> findByPersonalDataId(Long personalDataId);

    int deleteByPersonalDataId(Long personalDataId);

}
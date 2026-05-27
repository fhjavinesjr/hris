package com.administrative.repositories;

import com.administrative.entitymodels.PagIbigContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagIbigContributionRepository extends JpaRepository<PagIbigContribution, Long> {

    @Query("SELECT p FROM PagIbigContribution p ORDER BY p.effectivityDate DESC LIMIT 1")
    Optional<PagIbigContribution> findLatest();
}

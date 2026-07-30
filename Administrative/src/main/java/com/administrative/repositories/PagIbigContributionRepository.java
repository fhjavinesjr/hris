package com.administrative.repositories;

import com.administrative.entitymodels.PagIbigContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagIbigContributionRepository extends JpaRepository<PagIbigContribution, Long> {

    Optional<PagIbigContribution> findFirstByOrderByEffectivityDateDesc();
}

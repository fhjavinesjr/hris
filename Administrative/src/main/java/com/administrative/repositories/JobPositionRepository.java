package com.administrative.repositories;

import com.administrative.entitymodels.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    Page<JobPosition> findByJobPositionNameContainingIgnoreCase(String search, Pageable pageable);
}

package com.administrative.repositories;

import com.administrative.entitymodels.ManagePersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagePersonnelRepository extends JpaRepository<ManagePersonnel, Long> {
    // Custom queries if needed
}

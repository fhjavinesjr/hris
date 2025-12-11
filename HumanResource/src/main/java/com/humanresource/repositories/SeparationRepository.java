package com.humanresource.repositories;

import com.humanresource.entitymodels.Separation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeparationRepository extends JpaRepository<Separation, Long> {

    List<Separation> findByEmployeeId(Long employeeId);

}

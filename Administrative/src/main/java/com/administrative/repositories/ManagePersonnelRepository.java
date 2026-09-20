package com.administrative.repositories;

import com.administrative.entitymodels.ManagePersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ManagePersonnelRepository extends JpaRepository<ManagePersonnel, Long> {
    List<ManagePersonnel> findByEmployeeIdOrderById(Long employeeId);
    List<ManagePersonnel> findByBusinessUnitIdOrderById(Long businessUnitId);
}

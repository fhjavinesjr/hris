package com.humanresource.repositories;

import com.humanresource.entitymodels.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {

    List<OvertimeRequest> findByEmployeeIdOrderByDateFiledDesc(Long employeeId);

    List<OvertimeRequest> findByStatusOrderByDateFiledDesc(String status);

    List<OvertimeRequest> findByEmployeeIdAndStatusOrderByDateFiledDesc(Long employeeId, String status);
}

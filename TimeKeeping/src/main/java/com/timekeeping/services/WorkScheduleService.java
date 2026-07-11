package com.timekeeping.services;

import com.timekeeping.dtos.WorkScheduleDTO;
import org.springframework.stereotype.Repository;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface WorkScheduleService {

    WorkScheduleDTO createWorkSchedule(WorkScheduleDTO workScheduleDTO) throws Exception;

    List<WorkScheduleDTO> getAllWorkSchedule(String employeeId, LocalDateTime monthStart, LocalDateTime monthEnd) throws Exception;

    WorkScheduleDTO updateWorkSchedule(Long wsId, WorkScheduleDTO workScheduleDTO) throws Exception;

    Boolean deleteWorkSchedule(Long wsId) throws Exception;

    int bulkCreateDayOff(List<WorkScheduleDTO> dtos) throws Exception;

    void generateWorkScheduleReport(Long areaId, Long businessUnitId, LocalDate fromDate, LocalDate toDate, String preparedBy, String preparedByPos, String approvedBy, String approvedByPos, OutputStream out) throws Exception;

    Map<String, String> getWorkScheduleSignatoryInfo(String employeeId);

}

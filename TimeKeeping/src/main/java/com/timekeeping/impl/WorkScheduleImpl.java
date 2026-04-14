package com.timekeeping.impl;

import com.timekeeping.dtos.WorkScheduleDTO;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.repositories.WorkScheduleRepository;
import com.timekeeping.services.WorkScheduleService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WorkScheduleImpl implements WorkScheduleService {

    private static final Logger log = LoggerFactory.getLogger(WorkScheduleImpl.class);
    private final WorkScheduleRepository workScheduleRepository;

    public WorkScheduleImpl(WorkScheduleRepository workScheduleRepository) {
        this.workScheduleRepository = workScheduleRepository;
    }

    @Transactional
    @Override
    public WorkScheduleDTO createWorkSchedule(WorkScheduleDTO workScheduleDTO) throws Exception {
        try {
            WorkSchedule workSchedule = new WorkSchedule(workScheduleDTO.getEmployeeId(), workScheduleDTO.getTsCode(), workScheduleDTO.getWsDateTime());
            workSchedule.setIsDayOff(workScheduleDTO.getIsDayOff() != null && workScheduleDTO.getIsDayOff());
            WorkSchedule wsSaved = workScheduleRepository.save(workSchedule);
            workScheduleDTO.setWsId(wsSaved.getWsId());
            return workScheduleDTO;
        } catch (Exception e) {
            log.info("Creation of Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<WorkScheduleDTO> getAllWorkSchedule(String employeeId, LocalDateTime monthStart, LocalDateTime monthEnd) throws Exception {
        try {
            List<WorkScheduleDTO> workScheduleDTOList = new ArrayList<>();
            Optional<List<WorkSchedule>> opWorkSchedules = workScheduleRepository.findByEmployeeIdAndWsDateTimeBetween(employeeId, monthStart, monthEnd);
            if(opWorkSchedules.isPresent()) {
                List<WorkSchedule> workSchedules = opWorkSchedules.get();
                for(WorkSchedule workSchedule : workSchedules) {
                    WorkScheduleDTO workScheduleDTO = new WorkScheduleDTO(workSchedule.getWsId(), workSchedule.getEmployeeId(), workSchedule.getTsCode(), workSchedule.getWsDateTime());
                    workScheduleDTO.setIsDayOff(workSchedule.getIsDayOff());
                    workScheduleDTOList.add(workScheduleDTO);
                }
            }

            return workScheduleDTOList;
        } catch (Exception e) {
            log.info("Getting All Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }

    public WorkSchedule getWorkScheduleById(Long wsId) {
        return workScheduleRepository.findById(wsId).orElseThrow(() -> new RuntimeException("Work Schedule not found"));
    }

    @Transactional
    @Override
    public WorkScheduleDTO updateWorkSchedule(Long wsId, WorkScheduleDTO workScheduleDTO) throws Exception {
        try {
            WorkSchedule workSchedule = getWorkScheduleById(wsId);
            workSchedule = new WorkSchedule(workScheduleDTO.getEmployeeId(), workScheduleDTO.getTsCode(), workScheduleDTO.getWsDateTime());
            workSchedule.setWsId(wsId);
            workSchedule.setIsDayOff(workScheduleDTO.getIsDayOff() != null && workScheduleDTO.getIsDayOff());
            workScheduleRepository.save(workSchedule);
            return workScheduleDTO;
        } catch(Exception e) {
            log.info("Update Work Schedule Failed: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteWorkSchedule(Long wsId) throws Exception {
        try {
            workScheduleRepository.deleteById(wsId);
            return true;
        } catch(Exception e) {
            log.info("Delete Work Schedule Failed: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    @Override
    public int bulkCreateDayOff(List<WorkScheduleDTO> dtos) throws Exception {
        int count = 0;
        for (WorkScheduleDTO dto : dtos) {
            try {
                WorkSchedule ws = new WorkSchedule();
                ws.setEmployeeId(dto.getEmployeeId());
                ws.setWsDateTime(dto.getWsDateTime());
                ws.setIsDayOff(true);
                workScheduleRepository.save(ws);
                count++;
            } catch (Exception e) {
                log.warn("Failed to save day-off for {}: {}", dto.getEmployeeId(), e.getMessage());
            }
        }
        return count;
    }
}
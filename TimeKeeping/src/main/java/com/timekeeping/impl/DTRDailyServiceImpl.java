package com.timekeeping.impl;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.dtos.DTRSegmentDTO;
import com.timekeeping.entitymodels.DTRDaily;
import com.timekeeping.entitymodels.DTRSegment;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.services.DTRDailyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DTRDailyServiceImpl implements DTRDailyService {
    private final DTRDailyRepository dtrDailyRepository;

    public DTRDailyServiceImpl(DTRDailyRepository dtrDailyRepository) {
        this.dtrDailyRepository = dtrDailyRepository;
    }

    @Override
    @Transactional
    public DTRDailyDTO createOrUpdateDTRDaily(DTRDailyDTO dto) {
        DTRDaily entity = toEntity(dto);
        DTRDaily saved = dtrDailyRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    public List<DTRDailyDTO> getEmployeeDTRDaily(String employeeId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Convert LocalDateTime to LocalDate for the repository query
        // DTR_DAILY uses LocalDate (work_date) not LocalDateTime
        return dtrDailyRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, fromDate.toLocalDate(), toDate.toLocalDate())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private DTRDailyDTO toDTO(DTRDaily entity) {
        DTRDailyDTO dto = new DTRDailyDTO();
        dto.setDtrDailyId(entity.getDtrDailyId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setWorkDate(entity.getWorkDate().atStartOfDay());
        dto.setTotalWorkMinutes(entity.getTotalWorkMinutes());
        dto.setTotalLateMinutes(entity.getTotalLateMinutes());
        dto.setTotalUndertimeMinutes(entity.getTotalUndertimeMinutes());
        dto.setTotalOvertimeMinutes(entity.getTotalOvertimeMinutes());
        dto.setAttendanceStatus(entity.getAttendanceStatus());
        if (entity.getSegments() != null) {
            dto.setSegments(entity.getSegments().stream().map(this::toSegmentDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private DTRSegmentDTO toSegmentDTO(DTRSegment segment) {
        DTRSegmentDTO dto = new DTRSegmentDTO();
        dto.setDtrSegmentId(segment.getDtrSegmentId());
        dto.setSegmentNo(segment.getSegmentNo());
        dto.setTimeIn(segment.getTimeIn());
        dto.setBreakOut(segment.getBreakOut());
        dto.setBreakIn(segment.getBreakIn());
        dto.setTimeOut(segment.getTimeOut());
        dto.setWorkMinutes(segment.getWorkMinutes());
        dto.setLateMinutes(segment.getLateMinutes());
        dto.setUndertimeMinutes(segment.getUndertimeMinutes());
        dto.setOvertimeMinutes(segment.getOvertimeMinutes());
        return dto;
    }

    private DTRDaily toEntity(DTRDailyDTO dto) {
        DTRDaily entity = new DTRDaily();
        entity.setDtrDailyId(dto.getDtrDailyId());
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setWorkDate(dto.getWorkDate().toLocalDate());
        entity.setTotalWorkMinutes(dto.getTotalWorkMinutes());
        entity.setTotalLateMinutes(dto.getTotalLateMinutes());
        entity.setTotalUndertimeMinutes(dto.getTotalUndertimeMinutes());
        entity.setTotalOvertimeMinutes(dto.getTotalOvertimeMinutes());
        entity.setAttendanceStatus(dto.getAttendanceStatus());
        if (dto.getSegments() != null) {
            entity.setSegments(dto.getSegments().stream().map(s -> toSegmentEntity(s, entity)).collect(Collectors.toList()));
        }
        return entity;
    }

    private DTRSegment toSegmentEntity(DTRSegmentDTO dto, DTRDaily parent) {
        DTRSegment entity = new DTRSegment();
        entity.setDtrSegmentId(dto.getDtrSegmentId());
        entity.setDtrDaily(parent);
        entity.setSegmentNo(dto.getSegmentNo());
        entity.setTimeIn(dto.getTimeIn());
        entity.setBreakOut(dto.getBreakOut());
        entity.setBreakIn(dto.getBreakIn());
        entity.setTimeOut(dto.getTimeOut());
        entity.setWorkMinutes(dto.getWorkMinutes());
        entity.setLateMinutes(dto.getLateMinutes());
        entity.setUndertimeMinutes(dto.getUndertimeMinutes());
        entity.setOvertimeMinutes(dto.getOvertimeMinutes());
        return entity;
    }
}


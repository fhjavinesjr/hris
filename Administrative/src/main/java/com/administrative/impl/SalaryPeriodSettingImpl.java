package com.administrative.impl;

import com.administrative.dtos.SalaryPeriodSettingDTO;
import com.administrative.entitymodels.SalaryPeriodSetting;
import com.administrative.repositories.SalaryPeriodSettingRepository;
import com.administrative.services.SalaryPeriodSettingService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaryPeriodSettingImpl implements SalaryPeriodSettingService {

    private static final Logger log = LoggerFactory.getLogger(SalaryPeriodSettingImpl.class);
    private final SalaryPeriodSettingRepository repository;

    public SalaryPeriodSettingImpl(SalaryPeriodSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public SalaryPeriodSettingDTO create(SalaryPeriodSettingDTO dto) throws Exception {
        try {
            SalaryPeriodSetting entity = toEntity(dto);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception e) {
            log.error("Error creating SalaryPeriodSetting: ", e);
            return null;
        }
    }

    @Override
    public List<SalaryPeriodSettingDTO> getAll() throws Exception {
        return repository.findAllByOrderBySalaryTypeAscNthOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryPeriodSettingDTO> getByContext(String context) throws Exception {
        // "BOTH" records are always returned regardless of requested context
        List<String> contexts = Arrays.asList(context, "BOTH");
        return repository.findByPeriodContextInAndIsActiveTrueOrderBySalaryTypeAscNthOrderAsc(contexts)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SalaryPeriodSettingDTO update(Long id, SalaryPeriodSettingDTO dto) throws Exception {
        try {
            SalaryPeriodSetting entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SalaryPeriodSetting not found: " + id));
            entity.setSalaryType(dto.getSalaryType());
            entity.setNthOrder(dto.getNthOrder());
            entity.setPeriodContext(dto.getPeriodContext());
            entity.setCutoffStartDay(dto.getCutoffStartDay());
            entity.setCutoffStartMonthOffset(dto.getCutoffStartMonthOffset());
            entity.setCutoffEndDay(dto.getCutoffEndDay());
            entity.setCutoffEndMonthOffset(dto.getCutoffEndMonthOffset());
            entity.setSalaryReleaseStartDay(dto.getSalaryReleaseStartDay());
            entity.setSalaryReleaseEndDay(dto.getSalaryReleaseEndDay());
            entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = repository.save(entity);
            return toDTO(entity);
        } catch (Exception e) {
            log.error("Error updating SalaryPeriodSetting: ", e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long id) throws Exception {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting SalaryPeriodSetting: ", e);
            return false;
        }
    }

    private SalaryPeriodSettingDTO toDTO(SalaryPeriodSetting entity) {
        SalaryPeriodSettingDTO dto = new SalaryPeriodSettingDTO();
        dto.setSalaryPeriodSettingId(entity.getSalaryPeriodSettingId());
        dto.setSalaryType(entity.getSalaryType());
        dto.setNthOrder(entity.getNthOrder());
        dto.setPeriodContext(entity.getPeriodContext());
        dto.setCutoffStartDay(entity.getCutoffStartDay());
        dto.setCutoffStartMonthOffset(entity.getCutoffStartMonthOffset());
        dto.setCutoffEndDay(entity.getCutoffEndDay());
        dto.setCutoffEndMonthOffset(entity.getCutoffEndMonthOffset());
        dto.setSalaryReleaseStartDay(entity.getSalaryReleaseStartDay());
        dto.setSalaryReleaseEndDay(entity.getSalaryReleaseEndDay());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    private SalaryPeriodSetting toEntity(SalaryPeriodSettingDTO dto) {
        SalaryPeriodSetting entity = new SalaryPeriodSetting();
        entity.setSalaryPeriodSettingId(dto.getSalaryPeriodSettingId());
        entity.setSalaryType(dto.getSalaryType());
        entity.setNthOrder(dto.getNthOrder());
        entity.setPeriodContext(dto.getPeriodContext());
        entity.setCutoffStartDay(dto.getCutoffStartDay());
        entity.setCutoffStartMonthOffset(dto.getCutoffStartMonthOffset());
        entity.setCutoffEndDay(dto.getCutoffEndDay());
        entity.setCutoffEndMonthOffset(dto.getCutoffEndMonthOffset());
        entity.setSalaryReleaseStartDay(dto.getSalaryReleaseStartDay());
        entity.setSalaryReleaseEndDay(dto.getSalaryReleaseEndDay());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return entity;
    }
}

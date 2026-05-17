package com.administrative.impl;

import com.administrative.dtos.EarningTypeDTO;
import com.administrative.entitymodels.EarningType;
import com.administrative.repositories.EarningTypeRepository;
import com.administrative.services.EarningTypeService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EarningTypeImpl implements EarningTypeService {

    private static final Logger log = LoggerFactory.getLogger(EarningTypeImpl.class);
    private final EarningTypeRepository earningTypeRepository;

    public EarningTypeImpl(EarningTypeRepository earningTypeRepository) {
        this.earningTypeRepository = earningTypeRepository;
    }

    @Transactional
    @Override
    public EarningTypeDTO createEarningType(EarningTypeDTO dto) throws Exception {
        try {
            EarningType entity = new EarningType();
            mapDtoToEntity(dto, entity);
            earningTypeRepository.save(entity);
            dto.setEarningTypeId(entity.getEarningTypeId());
            return dto;
        } catch (Exception e) {
            log.error("Error creating EarningType: ", e);
            return null;
        }
    }

    @Override
    public List<EarningTypeDTO> getAllEarningTypes() throws Exception {
        List<EarningType> list = earningTypeRepository.findAll();
        List<EarningTypeDTO> dtoList = new ArrayList<>();
        for (EarningType entity : list) {
            dtoList.add(mapEntityToDto(entity));
        }
        return dtoList;
    }

    @Transactional
    @Override
    public EarningTypeDTO updateEarningType(Long earningTypeId, EarningTypeDTO dto) throws Exception {
        try {
            EarningType entity = earningTypeRepository.findById(earningTypeId)
                    .orElseThrow(() -> new RuntimeException("EarningType not found"));
            mapDtoToEntity(dto, entity);
            earningTypeRepository.save(entity);
            return dto;
        } catch (Exception e) {
            log.error("Error updating EarningType: ", e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteEarningType(Long earningTypeId) throws Exception {
        try {
            earningTypeRepository.deleteById(earningTypeId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting EarningType: ", e);
            return false;
        }
    }

    private void mapDtoToEntity(EarningTypeDTO dto, EarningType entity) {
        entity.setAccountingCode(dto.getAccountingCode());
        entity.setName(dto.getName());
        entity.setTaxable(dto.getTaxable());
        entity.setAllowance(dto.getAllowance());
        entity.setDailyBasis(dto.getDailyBasis());
        entity.setBasic(dto.getBasic());
        entity.setRata(dto.getRata());
        entity.setHonorarium(dto.getHonorarium());
        entity.setEcola(dto.getEcola());
        entity.setUp(dto.getUp());
        entity.setFixedHousing(dto.getFixedHousing());
        entity.setRepresentation(dto.getRepresentation());
        entity.setTransportation(dto.getTransportation());
        entity.setLongevity(dto.getLongevity());
        entity.setLaundry(dto.getLaundry());
        entity.setHazardPay(dto.getHazardPay());
        entity.setPera(dto.getPera());
        entity.setSubsistence(dto.getSubsistence());
        entity.setSpecialPayroll(dto.getSpecialPayroll());
    }

    private EarningTypeDTO mapEntityToDto(EarningType entity) {
        EarningTypeDTO dto = new EarningTypeDTO();
        dto.setEarningTypeId(entity.getEarningTypeId());
        dto.setAccountingCode(entity.getAccountingCode());
        dto.setName(entity.getName());
        dto.setTaxable(entity.getTaxable());
        dto.setAllowance(entity.getAllowance());
        dto.setDailyBasis(entity.getDailyBasis());
        dto.setBasic(entity.getBasic());
        dto.setRata(entity.getRata());
        dto.setHonorarium(entity.getHonorarium());
        dto.setEcola(entity.getEcola());
        dto.setUp(entity.getUp());
        dto.setFixedHousing(entity.getFixedHousing());
        dto.setRepresentation(entity.getRepresentation());
        dto.setTransportation(entity.getTransportation());
        dto.setLongevity(entity.getLongevity());
        dto.setLaundry(entity.getLaundry());
        dto.setHazardPay(entity.getHazardPay());
        dto.setPera(entity.getPera());
        dto.setSubsistence(entity.getSubsistence());
        dto.setSpecialPayroll(entity.getSpecialPayroll());
        return dto;
    }
}

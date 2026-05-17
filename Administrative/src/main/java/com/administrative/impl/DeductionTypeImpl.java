package com.administrative.impl;

import com.administrative.dtos.DeductionTypeDTO;
import com.administrative.entitymodels.DeductionType;
import com.administrative.repositories.DeductionTypeRepository;
import com.administrative.services.DeductionTypeService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeductionTypeImpl implements DeductionTypeService {

    private static final Logger log = LoggerFactory.getLogger(DeductionTypeImpl.class);
    private final DeductionTypeRepository deductionTypeRepository;

    public DeductionTypeImpl(DeductionTypeRepository deductionTypeRepository) {
        this.deductionTypeRepository = deductionTypeRepository;
    }

    @Transactional
    @Override
    public DeductionTypeDTO createDeductionType(DeductionTypeDTO dto) throws Exception {
        try {
            DeductionType entity = new DeductionType();
            mapDtoToEntity(dto, entity);
            deductionTypeRepository.save(entity);
            dto.setDeductionTypeId(entity.getDeductionTypeId());
            return dto;
        } catch (Exception e) {
            log.error("Error creating DeductionType: ", e);
            return null;
        }
    }

    @Override
    public List<DeductionTypeDTO> getAllDeductionTypes() throws Exception {
        List<DeductionType> list = deductionTypeRepository.findAll();
        List<DeductionTypeDTO> dtoList = new ArrayList<>();
        for (DeductionType entity : list) {
            dtoList.add(mapEntityToDto(entity));
        }
        return dtoList;
    }

    @Transactional
    @Override
    public DeductionTypeDTO updateDeductionType(Long deductionTypeId, DeductionTypeDTO dto) throws Exception {
        try {
            DeductionType entity = deductionTypeRepository.findById(deductionTypeId)
                    .orElseThrow(() -> new RuntimeException("DeductionType not found"));
            mapDtoToEntity(dto, entity);
            deductionTypeRepository.save(entity);
            return dto;
        } catch (Exception e) {
            log.error("Error updating DeductionType: ", e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteDeductionType(Long deductionTypeId) throws Exception {
        try {
            deductionTypeRepository.deleteById(deductionTypeId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting DeductionType: ", e);
            return false;
        }
    }

    private void mapDtoToEntity(DeductionTypeDTO dto, DeductionType entity) {
        entity.setAccountingCode(dto.getAccountingCode());
        entity.setName(dto.getName());
        entity.setMandatoryDeduction(dto.getMandatoryDeduction());
        entity.setGsis(dto.getGsis());
        entity.setPhilHealth(dto.getPhilHealth());
        entity.setPagIbig(dto.getPagIbig());
        entity.setWithholdingTax(dto.getWithholdingTax());
        entity.setUnion(dto.getUnion());
        entity.setOthers(dto.getOthers());
    }

    private DeductionTypeDTO mapEntityToDto(DeductionType entity) {
        DeductionTypeDTO dto = new DeductionTypeDTO();
        dto.setDeductionTypeId(entity.getDeductionTypeId());
        dto.setAccountingCode(entity.getAccountingCode());
        dto.setName(entity.getName());
        dto.setMandatoryDeduction(entity.getMandatoryDeduction());
        dto.setGsis(entity.getGsis());
        dto.setPhilHealth(entity.getPhilHealth());
        dto.setPagIbig(entity.getPagIbig());
        dto.setWithholdingTax(entity.getWithholdingTax());
        dto.setUnion(entity.getUnion());
        dto.setOthers(entity.getOthers());
        return dto;
    }
}

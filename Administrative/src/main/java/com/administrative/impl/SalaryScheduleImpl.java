package com.administrative.impl;

import com.administrative.dtos.SalaryScheduleDTO;
import com.administrative.entitymodels.SalarySchedule;
import com.administrative.repositories.SalaryScheduleRepository;
import com.administrative.services.SalaryScheduleService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaryScheduleImpl implements SalaryScheduleService {

    private static final Logger log = LoggerFactory.getLogger(SalaryScheduleImpl.class);
    private final SalaryScheduleRepository salaryScheduleRepository;

    public SalaryScheduleImpl(SalaryScheduleRepository salaryScheduleRepository) {
        this.salaryScheduleRepository = salaryScheduleRepository;
    }

    @Transactional
    @Override
    public List<SalaryScheduleDTO> createSalarySchedule(List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception {
        for(SalaryScheduleDTO salaryScheduleDTO : salaryScheduleDTOList) {
            SalarySchedule salarySchedule = new SalarySchedule(salaryScheduleDTO.getEffectDate(), salaryScheduleDTO.getExecutiveOrderNo(),
                    salaryScheduleDTO.getNbcDate(), salaryScheduleDTO.getExecutiveOrderNo(), salaryScheduleDTO.getExecutiveOrderDate(),
                    salaryScheduleDTO.getSalaryGrade(), salaryScheduleDTO.getSalaryStep(), salaryScheduleDTO.getMonthlySalary(),
                    salaryScheduleDTO.getCreatedOrModifiedByEployeeId());

            salaryScheduleRepository.save(salarySchedule);
        }

        return salaryScheduleDTOList;
    }

    @Override
    public List<SalaryScheduleDTO> getAllSalarySchedule() throws Exception {
        return List.of();
    }

    @Override
    public SalaryScheduleDTO getSalaryScheduleById(Long SalaryScheduleId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public SalaryScheduleDTO updateSalarySchedule(Long SalaryScheduleId, SalaryScheduleDTO salaryScheduleDTO) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public Boolean deleteSalarySchedule(Long SalaryScheduleId) throws Exception {
        return null;
    }
}

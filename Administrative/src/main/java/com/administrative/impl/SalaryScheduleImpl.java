package com.administrative.impl;

import com.administrative.dtos.SalaryScheduleDTO;
import com.administrative.entitymodels.SalarySchedule;
import com.administrative.repositories.SalaryScheduleRepository;
import com.administrative.services.SalaryScheduleService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            SalarySchedule salarySchedule = new SalarySchedule(salaryScheduleDTO.getEffectivityDate(), salaryScheduleDTO.getNbcNo(), salaryScheduleDTO.getNbcDate(),
                    salaryScheduleDTO.getEoNo(), salaryScheduleDTO.getEoDate(), salaryScheduleDTO.getSalaryGrade(),
                    salaryScheduleDTO.getSalaryStep(), salaryScheduleDTO.getMonthlySalary(), salaryScheduleDTO.getCreatedOrModifiedByEployeeId());

            salaryScheduleRepository.save(salarySchedule);
        }

        return salaryScheduleDTOList;
    }

    @Override
    public List<SalaryScheduleDTO> getAllSalarySchedule() throws Exception {
        List<SalarySchedule> salaryScheduleList = salaryScheduleRepository.findAll();
        List<SalaryScheduleDTO> salaryScheduleDTOList = new ArrayList<>();

        for(SalarySchedule salarySchedule : salaryScheduleList) {
            SalaryScheduleDTO salaryScheduleDTO = new SalaryScheduleDTO();
            salaryScheduleDTO.setSalaryScheduleId(salarySchedule.getSalaryScheduleId());
            salaryScheduleDTO.setEffectivityDate(salarySchedule.getEffectivityDate());
            salaryScheduleDTO.setNbcNo(salarySchedule.getNbcNo());
            salaryScheduleDTO.setNbcDate(salarySchedule.getNbcDate());
            salaryScheduleDTO.setEoNo(salarySchedule.getEoNo());
            salaryScheduleDTO.setEoDate(salarySchedule.getEoDate());
            salaryScheduleDTO.setSalaryGrade(salarySchedule.getSalaryGrade());
            salaryScheduleDTO.setSalaryStep(salarySchedule.getSalaryStep());
            salaryScheduleDTO.setMonthlySalary(salarySchedule.getMonthlySalary());
            salaryScheduleDTO.setCreatedOrModifiedByEployeeId(salarySchedule.getCreatedOrModifiedByEployeeId());

            salaryScheduleDTOList.add(salaryScheduleDTO);
        }

        return salaryScheduleDTOList;
    }

    @Override
    public SalaryScheduleDTO getSalaryScheduleById(Long SalaryScheduleId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public List<SalaryScheduleDTO> updateSalarySchedule(List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception {

        try {
            for(SalaryScheduleDTO salaryScheduleDTO : salaryScheduleDTOList) {
                Optional<SalarySchedule> salaryScheduleOpt = salaryScheduleRepository.findById(salaryScheduleDTO.getSalaryScheduleId());
                if(salaryScheduleOpt.isPresent()) {
                    SalarySchedule salarySchedule = getSalarySchedule(salaryScheduleDTO, salaryScheduleOpt);
                    if(salarySchedule != null) {
                        salaryScheduleRepository.save(salarySchedule);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error updating Salary Schedule: ", e);
            return null;
        }

        return salaryScheduleDTOList;
    }

    @Transactional
    @Override
    public Boolean deleteSalarySchedule(LocalDateTime effectivityDate) throws Exception {
        try {
            salaryScheduleRepository.deleteByEffectivityDate(effectivityDate);
            return true;
        } catch (Exception e) {
            log.error("Error deleting Salary Schedule: ", e);
            return false;
        }
    }

    private static SalarySchedule getSalarySchedule(SalaryScheduleDTO salaryScheduleDTO, Optional<SalarySchedule> salaryScheduleOpt) {
        try {
            SalarySchedule salarySchedule = salaryScheduleOpt.get();
            salarySchedule.setEffectivityDate(salaryScheduleDTO.getEffectivityDate());
            salarySchedule.setNbcNo(salaryScheduleDTO.getNbcNo());
            salarySchedule.setNbcDate(salaryScheduleDTO.getNbcDate());
            salarySchedule.setEoNo(salaryScheduleDTO.getEoNo());
            salarySchedule.setEoDate(salaryScheduleDTO.getEoDate());
            salarySchedule.setSalaryGrade(salaryScheduleDTO.getSalaryGrade());
            salarySchedule.setSalaryStep(salaryScheduleDTO.getSalaryStep());
            salarySchedule.setMonthlySalary(salaryScheduleDTO.getMonthlySalary());
            salarySchedule.setCreatedOrModifiedByEployeeId(salaryScheduleDTO.getCreatedOrModifiedByEployeeId());
            return salarySchedule;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public SalaryScheduleDTO getSalaryScheduleByDateAssumptionAndSalaryGradeAndSalaryStep(LocalDateTime assumptionToDutyDate, Long salaryGrade, Long salaryStep) {
        SalarySchedule salarySchedule = salaryScheduleRepository.findByEffectivityDateAndSalaryGradeAndSalaryStep(assumptionToDutyDate, salaryGrade, salaryStep);

        if (salarySchedule == null) {
            throw new RuntimeException("No SalarySchedule found for the given date and grade/step.");
        }

        SalaryScheduleDTO salaryScheduleDTO = new SalaryScheduleDTO();
        salaryScheduleDTO.setSalaryScheduleId(salarySchedule.getSalaryScheduleId());
        salaryScheduleDTO.setEffectivityDate(salarySchedule.getEffectivityDate());
        salaryScheduleDTO.setNbcNo(salarySchedule.getNbcNo());
        salaryScheduleDTO.setNbcDate(salarySchedule.getNbcDate());
        salaryScheduleDTO.setEoNo(salarySchedule.getEoNo());
        salaryScheduleDTO.setEoDate(salarySchedule.getEoDate());
        salaryScheduleDTO.setSalaryGrade(salarySchedule.getSalaryGrade());
        salaryScheduleDTO.setSalaryStep(salarySchedule.getSalaryStep());
        salaryScheduleDTO.setMonthlySalary(salarySchedule.getMonthlySalary());
        salaryScheduleDTO.setCreatedOrModifiedByEployeeId(salarySchedule.getCreatedOrModifiedByEployeeId());

        return salaryScheduleDTO;
    }

}
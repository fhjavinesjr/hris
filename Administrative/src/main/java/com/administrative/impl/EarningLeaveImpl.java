package com.administrative.impl;

import com.administrative.dtos.DayEquivalentMinutesDTO;
import com.administrative.dtos.EarningLeaveDTO;
import com.administrative.entitymodels.DayEquivalentMinutes;
import com.administrative.entitymodels.EarningLeave;
import com.administrative.repositories.EarningLeaveRepository;
import com.administrative.services.EarningLeaveService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EarningLeaveImpl implements EarningLeaveService {

    private static final Logger log = LoggerFactory.getLogger(EarningLeaveImpl.class);
    private final EarningLeaveRepository earningLeaveRepository;

    public EarningLeaveImpl(EarningLeaveRepository earningLeaveRepository) {
        this.earningLeaveRepository = earningLeaveRepository;
    }

    @Transactional
    @Override
    public List<EarningLeaveDTO> createEarningLeave(List<EarningLeaveDTO> earningLeaveDTOList) throws Exception {
        try {
            for(EarningLeaveDTO earningLeaveDTO : earningLeaveDTOList) {
                EarningLeave earningLeave = new EarningLeave(earningLeaveDTO.getEarningLeaveId(), earningLeaveDTO.getEffectivityDate(), earningLeaveDTO.getDay(), earningLeaveDTO.getEarn());
                earningLeaveRepository.save(earningLeave);
            }

            return earningLeaveDTOList;
        } catch(Exception e) {
            log.error("Error in creating EarningLeave: ", e);
            return null;
        }
    }

    @Override
    public List<EarningLeaveDTO> getAllEarningLeave() throws Exception {
        List<EarningLeave> earningLeaveList = earningLeaveRepository.findAll();
        List<EarningLeaveDTO> earningLeaveDTOList = new ArrayList<>();

        for(EarningLeave earningLeave : earningLeaveList) {
            EarningLeaveDTO earningLeaveDTO = new EarningLeaveDTO();
            earningLeaveDTO.setEarningLeaveId(earningLeave.getEarningLeaveId());
            earningLeaveDTO.setEffectivityDate(earningLeave.getEffectivityDate());
            earningLeaveDTO.setDay(earningLeave.getDay());
            earningLeaveDTO.setEarn(earningLeave.getEarn());

            earningLeaveDTOList.add(earningLeaveDTO);
        }

        return earningLeaveDTOList;
    }

    @Override
    public EarningLeaveDTO getEarningLeaveById(Long earningLeaveId) throws Exception {
        return null;
    }

    @Override
    public List<EarningLeaveDTO> getEarningLeaveByEffectivityDate(LocalDateTime effectivityDate) throws Exception {
        try {
            List<EarningLeave> earningLeaveList = earningLeaveRepository.findByEffectivityDate(effectivityDate);
            List<EarningLeaveDTO> earningLeaveDTOList = new ArrayList<>();

            for(EarningLeave earningLeave : earningLeaveList) {
                EarningLeaveDTO earningLeaveDTO = new EarningLeaveDTO();
                earningLeaveDTO.setEarningLeaveId(earningLeave.getEarningLeaveId());
                earningLeaveDTO.setEffectivityDate(earningLeave.getEffectivityDate());
                earningLeaveDTO.setDay(earningLeave.getDay());
                earningLeaveDTO.setEarn(earningLeave.getEarn());

                earningLeaveDTOList.add(earningLeaveDTO);
            }

            return earningLeaveDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching EarningLeave: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public List<EarningLeaveDTO> updateEarningLeave(List<EarningLeaveDTO> earningLeaveDTOList) throws Exception {
        try {
            for(EarningLeaveDTO earningLeaveDTO : earningLeaveDTOList) {
                if(earningLeaveDTO.getEarningLeaveId() == null || earningLeaveDTO.getEarningLeaveId() == 0) {
                    //added new entry values
                    EarningLeave earningLeave = new EarningLeave();
                    earningLeave.setEffectivityDate(earningLeaveDTO.getEffectivityDate());
                    earningLeave.setDay(earningLeaveDTO.getDay());
                    earningLeave.setEarn(earningLeaveDTO.getEarn());

                    earningLeaveRepository.save(earningLeave);
                    continue;
                }

                //modify existing entry values
                EarningLeave earningLeave = earningLeaveRepository.findById(earningLeaveDTO.getEarningLeaveId()).orElseThrow(() -> new RuntimeException("EarningLeave not found"));
                if(earningLeave != null) {
                    earningLeave.setEffectivityDate(earningLeaveDTO.getEffectivityDate());
                    earningLeave.setDay(earningLeaveDTO.getDay());
                    earningLeave.setEarn(earningLeaveDTO.getEarn());

                    earningLeaveRepository.save(earningLeave);
                }
            }

            return earningLeaveDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching EarningLeave: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteEarningLeave(LocalDateTime effectivityDate) throws Exception {
        try {
            earningLeaveRepository.deleteByEffectivityDate(effectivityDate);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete EarningLeave: {}", e.getMessage());
        }

        return false;
    }

    @Transactional
    @Override
    public Boolean deleteEarningLeaveById(List<EarningLeaveDTO> earningLeaveDTOList) throws Exception {
        try {
            for(EarningLeaveDTO earningLeaveDTO : earningLeaveDTOList) {
                earningLeaveRepository.deleteById(earningLeaveDTO.getEarningLeaveId());
            }

            return true;
        } catch(Exception e) {
            log.error("Error failed deleting EarningLeave: {}", e.getMessage());
        }

        return null;
    }
}
package com.administrative.impl;

import com.administrative.dtos.DayEquivalentMinutesDTO;
import com.administrative.entitymodels.DayEquivalentMinutes;
import com.administrative.repositories.DayEquivalentMinutesRepository;
import com.administrative.services.DayEquivalentMinutesService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DayEquivalentMinutesImpl implements DayEquivalentMinutesService {

    private static final Logger log = LoggerFactory.getLogger(DayEquivalentMinutesImpl.class);
    private final DayEquivalentMinutesRepository dayEquivalentMinutesRepository;

    public DayEquivalentMinutesImpl(DayEquivalentMinutesRepository dayEquivalentMinutesRepository) {
        this.dayEquivalentMinutesRepository = dayEquivalentMinutesRepository;
    }

    @Transactional
    @Override
    public List<DayEquivalentMinutesDTO> createDayEquivalentMinutes(List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception {
        try {
            for(DayEquivalentMinutesDTO dayEquivalentMinutesDTO : dayEquivalentMinutesDTOList) {
                DayEquivalentMinutes dayEquivalentMinutes = new DayEquivalentMinutes(dayEquivalentMinutesDTO.getDayEquivalentMinutesId(), dayEquivalentMinutesDTO.getEffectivityDate(), dayEquivalentMinutesDTO.getMinutes(), dayEquivalentMinutesDTO.getMinutesEquivalent());
                dayEquivalentMinutesRepository.save(dayEquivalentMinutes);
            }

            return dayEquivalentMinutesDTOList;
        } catch(Exception e) {
            log.error("Error in creating DayEquivalentMinutes: ", e);
            return null;
        }
    }

    @Override
    public List<DayEquivalentMinutesDTO> getAllDayEquivalentMinutes() throws Exception {
        List<DayEquivalentMinutes> dayEquivalentMinutesList = dayEquivalentMinutesRepository.findAll();
        List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList = new ArrayList<>();

        for(DayEquivalentMinutes dayEquivalentMinutes : dayEquivalentMinutesList) {
            DayEquivalentMinutesDTO dayEquivalentMinutesDTO = new DayEquivalentMinutesDTO();
            dayEquivalentMinutesDTO.setDayEquivalentMinutesId(dayEquivalentMinutes.getDayEquivalentMinutesId());
            dayEquivalentMinutesDTO.setEffectivityDate(dayEquivalentMinutes.getEffectivityDate());
            dayEquivalentMinutesDTO.setMinutes(dayEquivalentMinutes.getMinutes());
            dayEquivalentMinutesDTO.setMinutesEquivalent(dayEquivalentMinutes.getMinutesEquivalent());

            dayEquivalentMinutesDTOList.add(dayEquivalentMinutesDTO);
        }

        return dayEquivalentMinutesDTOList;
    }

    @Override
    public DayEquivalentMinutesDTO getDayEquivalentMinutesById(Long dayEquivalentMinutesId) throws Exception {
        return null;
    }

    @Override
    public List<DayEquivalentMinutesDTO> getDayEquivalentMinutesByEffectivityDate(LocalDateTime effectivityDate) throws Exception {
        try {
            List<DayEquivalentMinutes> dayEquivalentMinutesList = dayEquivalentMinutesRepository.findByEffectivityDate(effectivityDate);
            List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList = new ArrayList<>();

            for(DayEquivalentMinutes dayEquivalentMinutes : dayEquivalentMinutesList) {
                DayEquivalentMinutesDTO dayEquivalentMinutesDTO = new DayEquivalentMinutesDTO();
                dayEquivalentMinutesDTO.setDayEquivalentMinutesId(dayEquivalentMinutes.getDayEquivalentMinutesId());
                dayEquivalentMinutesDTO.setEffectivityDate(dayEquivalentMinutes.getEffectivityDate());
                dayEquivalentMinutesDTO.setMinutes(dayEquivalentMinutes.getMinutes());
                dayEquivalentMinutesDTO.setMinutesEquivalent(dayEquivalentMinutes.getMinutesEquivalent());

                dayEquivalentMinutesDTOList.add(dayEquivalentMinutesDTO);
            }

            return dayEquivalentMinutesDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching DayEquivalentMinutes: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public List<DayEquivalentMinutesDTO> updateDayEquivalentMinutes(List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception {
        try {
            for(DayEquivalentMinutesDTO dayEquivalentMinutesDTO : dayEquivalentMinutesDTOList) {
                if(dayEquivalentMinutesDTO.getDayEquivalentMinutesId() == null || dayEquivalentMinutesDTO.getDayEquivalentMinutesId() == 0) {
                    //added new entry values
                    DayEquivalentMinutes dayEquivalentMinutes = new DayEquivalentMinutes();
                    dayEquivalentMinutes.setEffectivityDate(dayEquivalentMinutesDTO.getEffectivityDate());
                    dayEquivalentMinutes.setMinutes(dayEquivalentMinutesDTO.getMinutes());
                    dayEquivalentMinutes.setMinutesEquivalent(dayEquivalentMinutesDTO.getMinutesEquivalent());

                    dayEquivalentMinutesRepository.save(dayEquivalentMinutes);
                    continue;
                }

                //modify existing entry values
                DayEquivalentMinutes dayEquivalentMinutes = dayEquivalentMinutesRepository.findById(dayEquivalentMinutesDTO.getDayEquivalentMinutesId()).orElseThrow(() -> new RuntimeException("DayEquivalentMinutes not found"));
                if(dayEquivalentMinutes != null) {
                    dayEquivalentMinutes.setEffectivityDate(dayEquivalentMinutesDTO.getEffectivityDate());
                    dayEquivalentMinutes.setMinutes(dayEquivalentMinutesDTO.getMinutes());
                    dayEquivalentMinutes.setMinutesEquivalent(dayEquivalentMinutesDTO.getMinutesEquivalent());

                    dayEquivalentMinutesRepository.save(dayEquivalentMinutes);
                }
            }

            return dayEquivalentMinutesDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching DayEquivalentMinutes: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteDayEquivalentMinutes(LocalDateTime effectivityDate) throws Exception {
        try {
            dayEquivalentMinutesRepository.deleteByEffectivityDate(effectivityDate);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete DayEquivalentMinutes: {}", e.getMessage());
        }

        return false;
    }

    @Transactional
    @Override
    public Boolean deleteDayEquivalentMinutesById(List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception {
        try {
            for(DayEquivalentMinutesDTO dayEquivalentMinutesDTO : dayEquivalentMinutesDTOList) {
                dayEquivalentMinutesRepository.deleteById(dayEquivalentMinutesDTO.getDayEquivalentMinutesId());
            }

            return true;
        } catch(Exception e) {
            log.error("Error failed deleting DayEquivalentMinutes: {}", e.getMessage());
        }

        return null;
    }
}
package com.administrative.impl;

import com.administrative.dtos.DayEquivalentHoursDTO;
import com.administrative.entitymodels.DayEquivalentHours;
import com.administrative.entitymodels.DayEquivalentMinutes;
import com.administrative.repositories.DayEquivalentHoursRepository;
import com.administrative.services.DayEquivalentHoursService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DayEquivalentHoursImpl implements DayEquivalentHoursService {

    private static final Logger log = LoggerFactory.getLogger(DayEquivalentHoursImpl.class);
    private final DayEquivalentHoursRepository dayEquivalentHoursRepository;

    public DayEquivalentHoursImpl(DayEquivalentHoursRepository dayEquivalentHoursRepository) {
        this.dayEquivalentHoursRepository = dayEquivalentHoursRepository;
    }

    @Transactional
    @Override
    public List<DayEquivalentHoursDTO> createDayEquivalentHours(List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception {
        try {
            for(DayEquivalentHoursDTO dayEquivalentHoursDTO : dayEquivalentHoursDTOList) {
                DayEquivalentHours dayEquivalentHours = new DayEquivalentHours(dayEquivalentHoursDTO.getDayEquivalentHoursId(), dayEquivalentHoursDTO.getEffectivityDate(), dayEquivalentHoursDTO.getHours(), dayEquivalentHoursDTO.getHoursEquivalent());
                dayEquivalentHoursRepository.save(dayEquivalentHours);
            }

            return dayEquivalentHoursDTOList;
        } catch(Exception e) {
            log.error("Error in creating DayEquivalentHours: ", e);
            return null;
        }
    }

    @Override
    public List<DayEquivalentHoursDTO> getAllDayEquivalentHours() throws Exception {
        List<DayEquivalentHours> dayEquivalentHoursList = dayEquivalentHoursRepository.findAll();
        List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList = new ArrayList<>();

        for(DayEquivalentHours dayEquivalentHours : dayEquivalentHoursList) {
            DayEquivalentHoursDTO dayEquivalentHoursDTO = new DayEquivalentHoursDTO();
            dayEquivalentHoursDTO.setDayEquivalentHoursId(dayEquivalentHours.getDayEquivalentHoursId());
            dayEquivalentHoursDTO.setEffectivityDate(dayEquivalentHours.getEffectivityDate());
            dayEquivalentHoursDTO.setHours(dayEquivalentHours.getHours());
            dayEquivalentHoursDTO.setHoursEquivalent(dayEquivalentHours.getHoursEquivalent());

            dayEquivalentHoursDTOList.add(dayEquivalentHoursDTO);
        }

        return dayEquivalentHoursDTOList;
    }

    @Override
    public DayEquivalentHoursDTO getDayEquivalentHoursById(Long dayEquivalentHoursId) throws Exception {
        return null;
    }

    @Override
    public List<DayEquivalentHoursDTO> getDayEquivalentHoursByEffectivityDate(LocalDateTime effectivityDate) throws Exception {
        try {
            List<DayEquivalentHours> dayEquivalentHoursList = dayEquivalentHoursRepository.findByEffectivityDate(effectivityDate);
            List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList = new ArrayList<>();

            for(DayEquivalentHours dayEquivalentHours : dayEquivalentHoursList) {
                DayEquivalentHoursDTO dayEquivalentHoursDTO = new DayEquivalentHoursDTO();
                dayEquivalentHoursDTO.setDayEquivalentHoursId(dayEquivalentHours.getDayEquivalentHoursId());
                dayEquivalentHoursDTO.setEffectivityDate(dayEquivalentHours.getEffectivityDate());
                dayEquivalentHoursDTO.setHours(dayEquivalentHours.getHours());
                dayEquivalentHoursDTO.setHoursEquivalent(dayEquivalentHours.getHoursEquivalent());

                dayEquivalentHoursDTOList.add(dayEquivalentHoursDTO);
            }

            return dayEquivalentHoursDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching DayEquivalentHours: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public List<DayEquivalentHoursDTO> updateDayEquivalentHours(List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception {
        try {
            for(DayEquivalentHoursDTO dayEquivalentHoursDTO : dayEquivalentHoursDTOList) {
                if(dayEquivalentHoursDTO.getDayEquivalentHoursId() == null || dayEquivalentHoursDTO.getDayEquivalentHoursId() == 0) {
                    //added new entry values
                    DayEquivalentHours dayEquivalentHours = new DayEquivalentHours();
                    dayEquivalentHours.setEffectivityDate(dayEquivalentHoursDTO.getEffectivityDate());
                    dayEquivalentHours.setHours(dayEquivalentHoursDTO.getHours());
                    dayEquivalentHours.setHoursEquivalent(dayEquivalentHoursDTO.getHoursEquivalent());

                    dayEquivalentHoursRepository.save(dayEquivalentHours);
                    continue;
                }

                //modify existing entry values
                DayEquivalentHours dayEquivalentHours = dayEquivalentHoursRepository.findById(dayEquivalentHoursDTO.getDayEquivalentHoursId()).orElseThrow(() -> new RuntimeException("DayEquivalentHours not found"));
                if(dayEquivalentHours != null) {
                    dayEquivalentHours.setEffectivityDate(dayEquivalentHoursDTO.getEffectivityDate());
                    dayEquivalentHours.setHours(dayEquivalentHoursDTO.getHours());
                    dayEquivalentHours.setHoursEquivalent(dayEquivalentHoursDTO.getHoursEquivalent());

                    dayEquivalentHoursRepository.save(dayEquivalentHours);
                }
            }

            return dayEquivalentHoursDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching DayEquivalentHours: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteDayEquivalentHours(LocalDateTime effectivityDate) throws Exception {
        try {
            dayEquivalentHoursRepository.deleteByEffectivityDate(effectivityDate);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete DayEquivalentHours: {}", e.getMessage());
        }

        return false;
    }

    @Transactional
    @Override
    public Boolean deleteDayEquivalentHoursById(List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception {
        try {
            for(DayEquivalentHoursDTO dayEquivalentHoursDTO : dayEquivalentHoursDTOList) {
                dayEquivalentHoursRepository.deleteById(dayEquivalentHoursDTO.getDayEquivalentHoursId());
            }

            return true;
        } catch(Exception e) {
            log.error("Error failed deleting DayEquivalentHours: {}", e.getMessage());
        }

        return null;
    }
}
package com.administrative.services;

import com.administrative.dtos.DayEquivalentMinutesDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DayEquivalentMinutesService {

    List<DayEquivalentMinutesDTO> createDayEquivalentMinutes(List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception;

    List<DayEquivalentMinutesDTO> getAllDayEquivalentMinutes() throws Exception;

    DayEquivalentMinutesDTO getDayEquivalentMinutesById(Long dayEquivalentMinutesId) throws Exception;

    List<DayEquivalentMinutesDTO> getDayEquivalentMinutesByEffectivityDate(LocalDateTime effectivityDate) throws Exception;

    List<DayEquivalentMinutesDTO> updateDayEquivalentMinutes(List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception;

    Boolean deleteDayEquivalentMinutes(LocalDateTime effectivityDate) throws Exception;

    Boolean deleteDayEquivalentMinutesById(List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception;

}

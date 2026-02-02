package com.administrative.services;

import com.administrative.dtos.DayEquivalentHoursDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DayEquivalentHoursService {

    List<DayEquivalentHoursDTO> createDayEquivalentHours(List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception;

    List<DayEquivalentHoursDTO> getAllDayEquivalentHours() throws Exception;

    DayEquivalentHoursDTO getDayEquivalentHoursById(Long dayEquivalentHoursId) throws Exception;

    List<DayEquivalentHoursDTO> getDayEquivalentHoursByEffectivityDate(LocalDateTime effectivityDate) throws Exception;

    List<DayEquivalentHoursDTO> updateDayEquivalentHours(List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception;

    Boolean deleteDayEquivalentHours(LocalDateTime effectivityDate) throws Exception;

    Boolean deleteDayEquivalentHoursById(List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception;

}

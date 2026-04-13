package com.administrative.services;

import com.administrative.dtos.HolidayDTO;

import java.util.List;

public interface HolidayService {

    HolidayDTO createHoliday(HolidayDTO holidayDTO) throws Exception;

    List<HolidayDTO> getAllHoliday() throws Exception;

    HolidayDTO updateHoliday(Long holidayId, HolidayDTO holidayDTO) throws Exception;

    Boolean deleteHoliday(Long holidayId) throws Exception;
}

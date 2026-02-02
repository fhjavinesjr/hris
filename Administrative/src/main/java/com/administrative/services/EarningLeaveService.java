package com.administrative.services;

import com.administrative.dtos.DayEquivalentMinutesDTO;
import com.administrative.dtos.EarningLeaveDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EarningLeaveService {

    List<EarningLeaveDTO> createEarningLeave(List<EarningLeaveDTO> earningLeaveDTOList) throws Exception;

    List<EarningLeaveDTO> getAllEarningLeave() throws Exception;

    EarningLeaveDTO getEarningLeaveById(Long earningLeaveId) throws Exception;

    List<EarningLeaveDTO> getEarningLeaveByEffectivityDate(LocalDateTime effectivityDate) throws Exception;

    List<EarningLeaveDTO> updateEarningLeave(List<EarningLeaveDTO> earningLeaveDTOList) throws Exception;

    Boolean deleteEarningLeave(LocalDateTime effectivityDate) throws Exception;

    Boolean deleteEarningLeaveById(List<EarningLeaveDTO> earningLeaveDTOList) throws Exception;

}

package com.administrative.services;

import com.administrative.dtos.HazardPayDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HazardPayService {

    List<HazardPayDTO> createHazardPay(List<HazardPayDTO> hazardPayDTOList) throws Exception;

    List<HazardPayDTO> getAllHazardPay() throws Exception;

    HazardPayDTO getHazardPayById(Long hazardPayId) throws Exception;

    List<HazardPayDTO> getHazardPayByEffectivityDate(LocalDateTime effectivityDate) throws Exception;

    List<HazardPayDTO> updateHazardPay(List<HazardPayDTO> hazardPayDTOList) throws Exception;

    Boolean deleteHazardPay(LocalDateTime effectivityDate) throws Exception;

    Boolean deleteHazardPayById(List<HazardPayDTO> hazardPayDTOList) throws Exception;

}

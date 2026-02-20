package com.administrative.impl;

import com.administrative.dtos.SettingsDTO;
import com.administrative.entitymodels.Settings;
import com.administrative.repositories.SettingsRepository;
import com.administrative.services.SettingsService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettingsImpl implements SettingsService {

    private static final Logger log = LoggerFactory.getLogger(SettingsImpl.class);
    private final SettingsRepository settingsRepository;

    public SettingsImpl(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional
    @Override
    public SettingsDTO createSettings(SettingsDTO settingsDTO) throws Exception {
        try {
            Settings settings = new Settings(settingsDTO.getSettingsId(), settingsDTO.getSystemStartDate(), settingsDTO.getCompanyName()
                ,settingsDTO.getShortName(), settingsDTO.getCity(), settingsDTO.getAddress()
                ,settingsDTO.getIsoNo(), settingsDTO.getZipCode(), settingsDTO.getTelMobileNo()
                ,settingsDTO.getEmailAddress(), settingsDTO.getTinNo(), settingsDTO.getPagIbigNo()
                ,settingsDTO.getPhilHealthNo(), settingsDTO.getHospitalAgency(), settingsDTO.getLeftHeaderLogo()
                ,settingsDTO.getMainLogo(), settingsDTO.getRightHeaderLogo());
            settingsRepository.save(settings);

            return settingsDTO;
        } catch(Exception e) {
            log.error("Error in creating Settings: ", e);
            return null;
        }
    }

    @Override
    public List<SettingsDTO> getAllSettings() throws Exception {
        List<Settings> settingsList = settingsRepository.findAll();
        List<SettingsDTO> settingsDTOList = new ArrayList<>();

        for(Settings settings : settingsList) {
            SettingsDTO settingsDTO = new SettingsDTO();

            settingsDTOList.add(settingsDTO);
        }

        return settingsDTOList;
    }

    @Override
    public SettingsDTO getSettingsById(Long settingsId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public SettingsDTO updateSettings(Long settingsId, SettingsDTO settingsDTO) throws Exception {
        try {
            Settings settings = settingsRepository.findById(settingsId).orElseThrow(() -> new RuntimeException("Settings not found"));
            if(settings != null) {
                settings.setSystemStartDate(settingsDTO.getSystemStartDate());
                settings.setCompanyName(settingsDTO.getCompanyName());
                settings.setShortName(settingsDTO.getShortName());
                settings.setCity(settingsDTO.getCity());
                settings.setAddress(settingsDTO.getAddress());
                settings.setIsoNo(settingsDTO.getIsoNo());
                settings.setZipCode(settingsDTO.getZipCode());
                settings.setTelMobileNo(settingsDTO.getTelMobileNo());
                settings.setEmailAddress(settingsDTO.getEmailAddress());
                settings.setTinNo(settingsDTO.getTinNo());
                settings.setPagIbigNo(settingsDTO.getPagIbigNo());
                settings.setPhilHealthNo(settingsDTO.getPhilHealthNo());
                settings.setHospitalAgency(settingsDTO.getHospitalAgency());
                settings.setLeftHeaderLogo(settingsDTO.getLeftHeaderLogo());
                settings.setMainLogo(settingsDTO.getMainLogo());
                settings.setRightHeaderLogo(settingsDTO.getRightHeaderLogo());

                settingsRepository.save(settings);

                return settingsDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching Settings: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteSettings(Long settingsId) throws Exception {
        try {
            settingsRepository.deleteById(settingsId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete Settings: {}", e.getMessage());
        }

        return false;
    }
}
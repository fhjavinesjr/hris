package com.administrative.impl;

import com.administrative.dtos.HolidayDTO;
import com.administrative.entitymodels.Holiday;
import com.administrative.repositories.HolidayRepository;
import com.administrative.services.HolidayService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class HolidayImpl implements HolidayService {

    private static final Logger log = LoggerFactory.getLogger(HolidayImpl.class);
    private final HolidayRepository holidayRepository;

    public HolidayImpl(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional
    @Override
    public HolidayDTO createHoliday(HolidayDTO holidayDTO) throws Exception {
        try {
            if (holidayRepository.existsByCode(holidayDTO.getCode())) {
                throw new RuntimeException("Holiday code already exists.");
            }

            Holiday holiday = new Holiday(
                    holidayDTO.getHolidayId(),
                    holidayDTO.getCode(),
                    holidayDTO.getName(),
                    holidayDTO.getHolidayDate(),
                    holidayDTO.getObservedDate(),
                    holidayDTO.getHolidayType(),
                    holidayDTO.getHolidayScope(),
                    holidayDTO.getLocalityCode(),
                    holidayDTO.getSourceReference(),
                    holidayDTO.getWithPay(),
                    holidayDTO.getIsWorkingHoliday(),
                    holidayDTO.getIsActive(),
                    holidayDTO.getRecurringAlways() != null && holidayDTO.getRecurringAlways()
            );
            holidayRepository.save(holiday);

            return holidayDTO;
        } catch (Exception e) {
            log.error("Error in creating Holiday: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<HolidayDTO> getAllHoliday() throws Exception {
        try {
            List<HolidayDTO> holidayDTOList = new ArrayList<>();
            List<Holiday> holidays = holidayRepository.findAllByOrderByHolidayDateAsc();

            for (Holiday holiday : holidays) {
                HolidayDTO holidayDTO = new HolidayDTO(
                        holiday.getHolidayId(),
                        holiday.getCode(),
                        holiday.getName(),
                        holiday.getHolidayDate(),
                        holiday.getObservedDate(),
                        holiday.getHolidayType(),
                        holiday.getHolidayScope(),
                        holiday.getLocalityCode(),
                        holiday.getSourceReference(),
                        holiday.getWithPay(),
                        holiday.getIsWorkingHoliday(),
                        holiday.getIsActive(),
                        holiday.getRecurringAlways()
                );

                holidayDTOList.add(holidayDTO);
            }

            return holidayDTOList;
        } catch (Exception e) {
            log.error("Error in fetching Holiday: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public HolidayDTO updateHoliday(Long holidayId, HolidayDTO holidayDTO) throws Exception {
        try {
            if (holidayRepository.existsByCodeAndHolidayIdNot(holidayDTO.getCode(), holidayId)) {
                throw new RuntimeException("Holiday code already exists.");
            }

            Holiday holiday = holidayRepository.findById(holidayId)
                    .orElseThrow(() -> new RuntimeException("Holiday not found"));

            holiday.setCode(holidayDTO.getCode());
            holiday.setName(holidayDTO.getName());
            holiday.setHolidayDate(holidayDTO.getHolidayDate());
            holiday.setObservedDate(holidayDTO.getObservedDate());
            holiday.setHolidayType(holidayDTO.getHolidayType());
            holiday.setHolidayScope(holidayDTO.getHolidayScope());
            holiday.setLocalityCode(holidayDTO.getLocalityCode());
            holiday.setSourceReference(holidayDTO.getSourceReference());
            holiday.setWithPay(holidayDTO.getWithPay());
            holiday.setIsWorkingHoliday(holidayDTO.getIsWorkingHoliday());
            holiday.setIsActive(holidayDTO.getIsActive());
            holiday.setRecurringAlways(holidayDTO.getRecurringAlways() != null && holidayDTO.getRecurringAlways());

            holidayRepository.save(holiday);

            return holidayDTO;
        } catch (Exception e) {
            log.error("Error in updating Holiday: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteHoliday(Long holidayId) throws Exception {
        try {
            holidayRepository.deleteById(holidayId);
            return true;
        } catch (Exception e) {
            log.error("Error in deleting Holiday: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<HolidayDTO> getHolidaysByRange(LocalDate from, LocalDate to) throws Exception {
        try {
            // 1. Non-recurring holidays with an exact date in the range
            List<Holiday> nonRecurring = holidayRepository.findByHolidayDateBetweenAndIsActiveTrue(from, to);
            List<HolidayDTO> result = new ArrayList<>();
            Set<Long> addedIds = new HashSet<>();

            for (Holiday h : nonRecurring) {
                result.add(toDTO(h, h.getHolidayDate()));
                addedIds.add(h.getHolidayId());
            }

            // 2. Recurring holidays — project their month/day onto every year in the range
            List<Holiday> recurring = holidayRepository.findByRecurringAlwaysTrueAndIsActiveTrue();
            int fromYear = from.getYear();
            int toYear = to.getYear();

            for (Holiday h : recurring) {
                if (addedIds.contains(h.getHolidayId())) continue;

                int month = h.getHolidayDate().getMonthValue();
                int day   = h.getHolidayDate().getDayOfMonth();

                for (int year = fromYear; year <= toYear; year++) {
                    LocalDate projected;
                    try {
                        projected = LocalDate.of(year, month, day);
                    } catch (DateTimeException e) {
                        // e.g. Feb 29 on a non-leap year — skip
                        continue;
                    }
                    if (!projected.isBefore(from) && !projected.isAfter(to)) {
                        result.add(toDTO(h, projected));
                    }
                }
            }

            log.info("Fetched {} holidays (incl. recurring) from {} to {}", result.size(), from, to);
            return result;
        } catch (Exception e) {
            log.error("Error in fetching holidays by range: {}", e.getMessage());
            throw e;
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private HolidayDTO toDTO(Holiday h, LocalDate effectiveDate) {
        HolidayDTO dto = new HolidayDTO(
                h.getHolidayId(),
                h.getCode(),
                h.getName(),
                effectiveDate,
                h.getObservedDate(),
                h.getHolidayType(),
                h.getHolidayScope(),
                h.getLocalityCode(),
                h.getSourceReference(),
                h.getWithPay(),
                h.getIsWorkingHoliday(),
                h.getIsActive(),
                h.getRecurringAlways()
        );
        return dto;
    }
}

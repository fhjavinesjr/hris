package com.administrative.services;

import com.administrative.dtos.SupervisedBusinessUnitDTO;
import com.administrative.entitymodels.ManagePersonnel;
import com.administrative.repositories.BusinessUnitsRepository;
import com.administrative.repositories.ManagePersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
public class ManagePersonnelService {
    @Autowired
    private ManagePersonnelRepository repository;
    @Autowired
    private BusinessUnitsRepository businessUnitsRepository;

    public List<ManagePersonnel> getAll() {
        List<ManagePersonnel> records = repository.findAll();
        boolean changed = false;
        for (ManagePersonnel record : records) {
            if (record.getOtherStatus() != null
                    && !record.getOtherStatus().isBlank()
                    && !"OIC".equalsIgnoreCase(record.getOtherStatus().trim())) {
                record.setOtherStatus(null);
                record.setOicEffectiveFrom(null);
                record.setOicEffectiveTo(null);
                changed = true;
            }
        }
        return changed ? repository.saveAll(records) : records;
    }

    public ManagePersonnel save(ManagePersonnel mp) {
        return repository.save(mp);
    }

    @Transactional
    public List<ManagePersonnel> saveAll(List<ManagePersonnel> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("At least one personnel designation is required.");
        }

        List<ManagePersonnel> existing = repository.findAll();
        for (ManagePersonnel item : list) {
            normalizeAndValidate(item);
            boolean duplicate = existing.stream().anyMatch(saved ->
                    saved.getEmployeeId().equals(item.getEmployeeId())
                            && saved.getBusinessUnitId().equals(item.getBusinessUnitId()));
            if (duplicate) {
                throw new IllegalArgumentException("The employee is already designated in the selected Business Unit.");
            }
            if ("Yes".equalsIgnoreCase(item.getBase())) {
                boolean alreadyHasBase = existing.stream().anyMatch(saved ->
                        saved.getEmployeeId().equals(item.getEmployeeId())
                                && "Yes".equalsIgnoreCase(saved.getBase()));
                if (alreadyHasBase) {
                    throw new IllegalArgumentException("An employee may have only one Main Base of Approval Level.");
                }
            }
            validateHeadRules(item, existing);
            existing.add(item);
        }
        return repository.saveAll(list);
    }

    @Transactional
    public ManagePersonnel update(Long id, ManagePersonnel requested) {
        ManagePersonnel current = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Personnel designation was not found."));
        requested.setId(id);
        requested.setEmployeeId(current.getEmployeeId());
        requested.setBusinessUnitId(current.getBusinessUnitId());
        requested.setAreaId(current.getAreaId());
        normalizeAndValidate(requested);
        List<ManagePersonnel> others = repository.findAll().stream()
                .filter(item -> !item.getId().equals(id))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if ("Yes".equalsIgnoreCase(requested.getBase())
                && others.stream().anyMatch(item -> item.getEmployeeId().equals(requested.getEmployeeId())
                && "Yes".equalsIgnoreCase(item.getBase()))) {
            throw new IllegalArgumentException("An employee may have only one Main Base of Approval Level.");
        }
        validateHeadRules(requested, others);
        return repository.save(requested);
    }

    private void normalizeAndValidate(ManagePersonnel item) {
        if (item.getEmployeeId() == null || item.getBusinessUnitId() == null || item.getAreaId() == null) {
            throw new IllegalArgumentException("Employee, Area, and Business Unit are required.");
        }
        String status = item.getOtherStatus() == null ? "" : item.getOtherStatus().trim();
        if (!status.isEmpty() && !"OIC".equalsIgnoreCase(status)) {
            status = "";
        }
        item.setOtherStatus(status.isEmpty() ? null : "OIC");
        if ("OIC".equals(item.getOtherStatus())) {
            if (!item.isHead()) {
                throw new IllegalArgumentException("An OIC designation must also be marked as Head.");
            }
            if (item.getOicEffectiveFrom() == null) {
                throw new IllegalArgumentException("OIC Effective From is required.");
            }
            if (item.getOicEffectiveTo() != null
                    && item.getOicEffectiveTo().isBefore(item.getOicEffectiveFrom())) {
                throw new IllegalArgumentException("OIC Effective To cannot be before Effective From.");
            }
        } else {
            item.setOicEffectiveFrom(null);
            item.setOicEffectiveTo(null);
        }
    }

    private void validateHeadRules(ManagePersonnel candidate, List<ManagePersonnel> records) {
        if (!candidate.isHead()) return;
        boolean candidateIsOic = "OIC".equals(candidate.getOtherStatus());
        List<ManagePersonnel> unitHeads = records.stream()
                .filter(saved -> saved.getBusinessUnitId().equals(candidate.getBusinessUnitId()) && saved.isHead())
                .toList();
        if (!candidateIsOic) {
            boolean regularHeadExists = unitHeads.stream()
                    .anyMatch(saved -> !"OIC".equalsIgnoreCase(String.valueOf(saved.getOtherStatus())));
            if (regularHeadExists) {
                throw new IllegalArgumentException("Only one regular Head is allowed in a Business Unit.");
            }
            return;
        }
        boolean overlaps = unitHeads.stream()
                .filter(saved -> "OIC".equalsIgnoreCase(String.valueOf(saved.getOtherStatus())))
                .anyMatch(saved -> periodsOverlap(
                        candidate.getOicEffectiveFrom(), candidate.getOicEffectiveTo(),
                        saved.getOicEffectiveFrom(), saved.getOicEffectiveTo()));
        if (overlaps) {
            throw new IllegalArgumentException("OIC effective periods may not overlap in the same Business Unit.");
        }
    }

    private boolean periodsOverlap(java.time.LocalDate leftFrom, java.time.LocalDate leftTo,
                                   java.time.LocalDate rightFrom, java.time.LocalDate rightTo) {
        if (leftFrom == null || rightFrom == null) return true;
        return (rightTo == null || !leftFrom.isAfter(rightTo))
                && (leftTo == null || !rightFrom.isAfter(leftTo));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<SupervisedBusinessUnitDTO> getSupervisedUnits(Long employeeId,
                                                              LocalDate fromDate,
                                                              LocalDate toDate) {
        if (employeeId == null) throw new IllegalArgumentException("Authenticated employee is required.");
        LocalDate from = fromDate == null ? LocalDate.now() : fromDate;
        LocalDate to = toDate == null ? from : toDate;
        if (to.isBefore(from)) throw new IllegalArgumentException("To date cannot be before From date.");

        List<ManagePersonnel> all = getAll();
        List<SupervisedBusinessUnitDTO> result = new ArrayList<>();
        for (ManagePersonnel assignment : all) {
            if (!assignment.isHead() || !employeeId.equals(assignment.getEmployeeId())) continue;
            boolean oic = "OIC".equalsIgnoreCase(String.valueOf(assignment.getOtherStatus()));
            List<ManagePersonnel> unitOics = all.stream()
                    .filter(item -> item.getBusinessUnitId().equals(assignment.getBusinessUnitId()))
                    .filter(ManagePersonnel::isHead)
                    .filter(item -> "OIC".equalsIgnoreCase(String.valueOf(item.getOtherStatus())))
                    .toList();
            boolean authorized;
            if (oic) {
                authorized = covers(assignment.getOicEffectiveFrom(), assignment.getOicEffectiveTo(), from, to);
            } else {
                authorized = unitOics.stream().noneMatch(item -> periodsOverlap(
                        from, to, item.getOicEffectiveFrom(), item.getOicEffectiveTo()));
            }
            if (!authorized) continue;

            String unitName = businessUnitsRepository.findById(assignment.getBusinessUnitId())
                    .map(unit -> unit.getBusinessUnitsName())
                    .orElse("Business Unit #" + assignment.getBusinessUnitId());
            List<Long> personnelIds = all.stream()
                    .filter(item -> item.getBusinessUnitId().equals(assignment.getBusinessUnitId()))
                    .map(ManagePersonnel::getEmployeeId)
                    .distinct()
                    .toList();
            result.add(new SupervisedBusinessUnitDTO(
                    assignment.getBusinessUnitId(), unitName, oic ? "OIC" : "HEAD",
                    assignment.getOicEffectiveFrom(), assignment.getOicEffectiveTo(), personnelIds));
        }
        return result;
    }

    private boolean covers(LocalDate authorityFrom, LocalDate authorityTo, LocalDate requestFrom, LocalDate requestTo) {
        return authorityFrom != null && !authorityFrom.isAfter(requestFrom)
                && (authorityTo == null || !authorityTo.isBefore(requestTo));
    }
}

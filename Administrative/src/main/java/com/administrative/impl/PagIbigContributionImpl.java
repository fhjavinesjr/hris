package com.administrative.impl;

import com.administrative.dtos.PagIbigContributionDTO;
import com.administrative.entitymodels.PagIbigContribution;
import com.administrative.repositories.PagIbigContributionRepository;
import com.administrative.services.PagIbigContributionService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagIbigContributionImpl implements PagIbigContributionService {

    private static final Logger log = LoggerFactory.getLogger(PagIbigContributionImpl.class);
    private final PagIbigContributionRepository repository;

    public PagIbigContributionImpl(PagIbigContributionRepository repository) {
        this.repository = repository;
    }

    /**
     * Seeds the default Pag-IBIG contribution amounts on first startup.
     * Standard mandatory amount: ₱100 for both employee and employer.
     */
    @PostConstruct
    @Transactional
    public void seedDefaults() {
        if (repository.count() == 0) {
            PagIbigContribution defaults = new PagIbigContribution(
                    null,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    100.0,   // employeeShare (csShareEe)
                    100.0    // employerShare (csShareEr)
            );
            repository.save(defaults);
            log.info("PagIbigContribution: seeded default (employeeShare=100.00, employerShare=100.00)");
        }
    }

    @Transactional
    @Override
    public PagIbigContributionDTO createPagIbigContribution(PagIbigContributionDTO dto) throws Exception {
        try {
            PagIbigContribution entity = new PagIbigContribution(
                    null,
                    dto.getEffectivityDate(),
                    dto.getEmployeeShare(),
                    dto.getEmployerShare()
            );
            entity = repository.save(entity);
            dto.setPagIbigContributionId(entity.getPagIbigContributionId());
            return dto;
        } catch (Exception e) {
            log.error("Error creating PagIbigContribution: ", e);
            return null;
        }
    }

    @Override
    public List<PagIbigContributionDTO> getAllPagIbigContribution() throws Exception {
        return repository.findAll().stream()
                .map(e -> new PagIbigContributionDTO(
                        e.getPagIbigContributionId(),
                        e.getEffectivityDate(),
                        e.getEmployeeShare(),
                        e.getEmployerShare()))
                .collect(Collectors.toList());
    }

    @Override
    public PagIbigContributionDTO getCurrent() throws Exception {
        return repository.findLatest()
                .map(e -> new PagIbigContributionDTO(
                        e.getPagIbigContributionId(),
                        e.getEffectivityDate(),
                        e.getEmployeeShare(),
                        e.getEmployerShare()))
                .orElse(new PagIbigContributionDTO(null, null, 100.0, 100.0));
    }

    @Transactional
    @Override
    public PagIbigContributionDTO updatePagIbigContribution(Long id, PagIbigContributionDTO dto) throws Exception {
        try {
            return repository.findById(id).map(entity -> {
                entity.setEffectivityDate(dto.getEffectivityDate());
                entity.setEmployeeShare(dto.getEmployeeShare());
                entity.setEmployerShare(dto.getEmployerShare());
                repository.save(entity);
                dto.setPagIbigContributionId(entity.getPagIbigContributionId());
                return dto;
            }).orElse(null);
        } catch (Exception e) {
            log.error("Error updating PagIbigContribution id={}: ", id, e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deletePagIbigContribution(Long id) throws Exception {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting PagIbigContribution id={}: ", id, e);
            return false;
        }
    }
}

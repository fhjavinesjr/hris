package com.administrative.controllers;

import com.administrative.entitymodels.ManagePersonnel;
import com.administrative.dtos.SupervisedBusinessUnitDTO;
import com.administrative.services.ManagePersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manage-personnel")
public class ManagePersonnelController {
    @Autowired
    private ManagePersonnelService service;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/get-all")
    public List<ManagePersonnel> getAll() {
        return service.getAll();
    }

    @PostMapping("/save")
    public List<ManagePersonnel> save(@RequestBody List<ManagePersonnel> list) {
        return service.saveAll(list);
    }

    @PutMapping("/update/{id}")
    public ManagePersonnel update(@PathVariable Long id, @RequestBody ManagePersonnel value) {
        return service.update(id, value);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/supervised-units")
    public List<SupervisedBusinessUnitDTO> getSupervisedUnits(
            Authentication authentication,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated employee is required.");
        }
        List<Long> ids = jdbcTemplate.query(
                "SELECT employeeId FROM employee WHERE LOWER(employeeNo) = LOWER(?)",
                (rs, rowNum) -> rs.getLong(1), authentication.getName());
        if (ids.size() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated employee record was not found.");
        }
        return service.getSupervisedUnits(ids.get(0), fromDate, toDate);
    }
}

package com.administrative.controllers;

import com.administrative.entitymodels.ManagePersonnel;
import com.administrative.services.ManagePersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manage-personnel")
public class ManagePersonnelController {
    @Autowired
    private ManagePersonnelService service;

    @GetMapping("/get-all")
    public List<ManagePersonnel> getAll() {
        return service.getAll();
    }

    @PostMapping("/save")
    public List<ManagePersonnel> save(@RequestBody List<ManagePersonnel> list) {
        return service.saveAll(list);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

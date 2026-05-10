package com.administrative.controllers;

import com.administrative.dtos.AnnouncementDTO;
import com.administrative.services.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnnouncementController {

    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    @GetMapping("/announcement/get-all")
    public ResponseEntity<List<AnnouncementDTO>> getAll() {
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/announcement/create")
    public ResponseEntity<AnnouncementDTO> create(@RequestBody AnnouncementDTO dto) {
        try {
            return ResponseEntity.ok(service.create(dto));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/announcement/update/{id}")
    public ResponseEntity<AnnouncementDTO> update(@PathVariable Long id, @RequestBody AnnouncementDTO dto) {
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/announcement/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            Boolean deleted = service.delete(id);
            if (Boolean.TRUE.equals(deleted)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.administrative.services;

import com.administrative.entitymodels.ManagePersonnel;
import com.administrative.repositories.ManagePersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagePersonnelService {
    @Autowired
    private ManagePersonnelRepository repository;

    public List<ManagePersonnel> getAll() {
        return repository.findAll();
    }

    public ManagePersonnel save(ManagePersonnel mp) {
        return repository.save(mp);
    }

    public List<ManagePersonnel> saveAll(List<ManagePersonnel> list) {
        return repository.saveAll(list);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

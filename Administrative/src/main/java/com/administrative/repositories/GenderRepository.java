package com.administrative.repositories;

import com.administrative.entitymodels.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenderRepository extends JpaRepository<Gender, Long> {



}

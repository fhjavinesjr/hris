package com.humanresource.repositories;

import com.humanresource.entitymodels.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {

    PersonalData findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);

}

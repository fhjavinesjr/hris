package com.humanresource.repositories;

import com.humanresource.entitymodels.Children;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildrenRepository extends JpaRepository<Children, Long> {

    List<Children> findByPersonalDataId(Long personalDataId);

    int deleteByPersonalDataId(Long personalDataId);

}

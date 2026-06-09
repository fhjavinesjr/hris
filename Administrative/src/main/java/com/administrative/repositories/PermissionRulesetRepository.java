package com.administrative.repositories;

import com.administrative.entitymodels.PermissionRuleset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRulesetRepository extends JpaRepository<PermissionRuleset, Long> {
    boolean existsByPermissionNameAndPermissionIdNot(String permissionName, Long permissionId);
    boolean existsByPermissionName(String permissionName);
}

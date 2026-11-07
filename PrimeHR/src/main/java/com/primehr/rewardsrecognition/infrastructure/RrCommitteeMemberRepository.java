package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrCommitteeMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RrCommitteeMemberRepository extends JpaRepository<RrCommitteeMember, String> {
    List<RrCommitteeMember> findByAgencyIdAndCharterVersionIdOrderByMemberRoleAscDisplayNameAsc(String agencyId, String charterVersionId);
    void deleteByAgencyIdAndCharterVersionId(String agencyId, String charterVersionId);
}

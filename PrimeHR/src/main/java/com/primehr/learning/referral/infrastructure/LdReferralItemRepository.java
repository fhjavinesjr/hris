package com.primehr.learning.referral.infrastructure;

import com.primehr.learning.referral.domain.LdReferralItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface LdReferralItemRepository extends JpaRepository<LdReferralItem, String> {
    List<LdReferralItem> findByReferralIdOrderByDisplayOrderAscCompetencyCodeAsc(String referralId);
    Optional<LdReferralItem> findByIdAndReferralId(String id, String referralId);
    Optional<LdReferralItem> findByReferralIdAndGapItemId(String referralId, String gapItemId);

    @Query("select count(item)>0 from LdReferralItem item join item.referral referral " +
            "where item.agencyId=:agencyId and item.analysis.id=:analysisId and item.gapItem.id=:gapItemId " +
            "and item.active=true and referral.status in ('DRAFT','REFERRED')")
    boolean existsActiveClaim(@Param("agencyId") String agencyId, @Param("analysisId") String analysisId,
                              @Param("gapItemId") String gapItemId);

    @Query("select count(item) from LdReferralItem item join item.referral referral " +
            "where item.agencyId=:agencyId and item.analysis.id=:analysisId and item.gapItem.id=:gapItemId " +
            "and item.active=true and referral.status in ('DRAFT','REFERRED')")
    long countActiveClaims(@Param("agencyId") String agencyId, @Param("analysisId") String analysisId,
                           @Param("gapItemId") String gapItemId);
}

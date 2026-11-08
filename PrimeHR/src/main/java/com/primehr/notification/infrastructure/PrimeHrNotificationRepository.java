package com.primehr.notification.infrastructure;

import com.primehr.notification.domain.PrimeHrNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface PrimeHrNotificationRepository extends JpaRepository<PrimeHrNotification, String> {
    Optional<PrimeHrNotification> findByAgencyIdAndRecipientKeyAndEventKey(
            String agencyId, String recipientKey, String eventKey);

    Optional<PrimeHrNotification> findByIdAndAgencyIdAndRecipientKey(
            String id, String agencyId, String recipientKey);

    @Query("""
            select n from PrimeHrNotification n
             where n.agencyId = :agencyId and n.recipientKey = :recipientKey
               and (:includeDismissed = true or n.dismissedAt is null)
               and (:unreadOnly = false or (n.readAt is null and n.dismissedAt is null))
             order by n.createdAt desc, n.id desc
            """)
    Page<PrimeHrNotification> feed(@Param("agencyId") String agencyId,
                                   @Param("recipientKey") String recipientKey,
                                   @Param("unreadOnly") boolean unreadOnly,
                                   @Param("includeDismissed") boolean includeDismissed,
                                   Pageable pageable);

    long countByAgencyIdAndRecipientKeyAndReadAtIsNullAndDismissedAtIsNull(
            String agencyId, String recipientKey);

    List<PrimeHrNotification> findByAgencyIdAndSourceDomainAndSourceAggregateTypeAndSourceAggregateIdAndKindAndActionState(
            String agencyId, PrimeHrNotification.SourceDomain sourceDomain, String sourceAggregateType,
            String sourceAggregateId, PrimeHrNotification.Kind kind, PrimeHrNotification.ActionState actionState);

    @Query("""
            select n from PrimeHrNotification n
             where n.agencyId = :agencyId
               and n.eventCode = :eventCode
               and n.kind = :kind
               and n.actionState = :actionState
               and n.dueAt is not null and n.dueAt >= :from and n.dueAt <= :through
             order by n.dueAt, n.id
            """)
    Page<PrimeHrNotification> reminderCandidates(@Param("agencyId") String agencyId,
                                                  @Param("eventCode") PrimeHrNotification.EventCode eventCode,
                                                  @Param("kind") PrimeHrNotification.Kind kind,
                                                  @Param("actionState") PrimeHrNotification.ActionState actionState,
                                                  @Param("from") Instant from,
                                                  @Param("through") Instant through,
                                                  Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PrimeHrNotification n
               set n.readAt = :readAt,
                   n.updatedAt = :readAt,
                   n.updatedBy = :updatedBy,
                   n.version = n.version + 1
             where n.agencyId = :agencyId and n.recipientKey = :recipientKey
               and n.readAt is null and n.dismissedAt is null
            """)
    int markAllRead(@Param("agencyId") String agencyId,
                    @Param("recipientKey") String recipientKey,
                    @Param("readAt") Instant readAt,
                    @Param("updatedBy") String updatedBy);
}

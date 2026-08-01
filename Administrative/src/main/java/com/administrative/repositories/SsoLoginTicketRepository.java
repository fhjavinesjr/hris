package com.administrative.repositories;

import com.administrative.entitymodels.SsoLoginTicket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.Instant;

@Repository
public interface SsoLoginTicketRepository extends JpaRepository<SsoLoginTicket, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ticket from SsoLoginTicket ticket where ticket.codeHash = :codeHash")
    Optional<SsoLoginTicket> findForUpdateByCodeHash(@Param("codeHash") String codeHash);

    long deleteByExpiresAtBefore(Instant cutoff);
}

package com.timekeeping.repositories;

import com.timekeeping.entitymodels.DTRSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DTRSegmentRepository extends JpaRepository<DTRSegment, Long> {
    List<DTRSegment> findByDtrDaily_DtrDailyId(Long dtrDailyId);
}


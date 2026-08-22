package com.primehr.positionprofile.infrastructure;

import com.primehr.positionprofile.domain.PositionProfile;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.positionprofile.domain.PositionTargetType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Locale;
import java.time.LocalDate;

public final class PositionProfileSpecifications {
    private PositionProfileSpecifications() {
    }

    public static Specification<PositionProfile> filter(String agencyId, PositionProfileStatus status,
                                                        PositionTargetType targetType, String search) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("agencyId"), agencyId));
            if (status != null) predicates.add(builder.equal(root.get("status"), status.name()));
            if (targetType != null) predicates.add(builder.equal(root.get("targetType"), targetType.name()));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("sourceJobPositionName")), pattern),
                        builder.like(builder.lower(root.get("sourcePlantillaName")), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<PositionProfile> effective(String agencyId, PositionTargetType targetType,
                                                           Long jobPositionId, Long plantillaId,
                                                           LocalDate asOf) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("agencyId"), agencyId));
            predicates.add(builder.equal(root.get("status"), PositionProfileStatus.ACTIVE.name()));
            predicates.add(builder.equal(root.get("targetType"), targetType.name()));
            predicates.add(builder.equal(root.get("jobPositionId"), jobPositionId));
            if (targetType == PositionTargetType.PLANTILLA) {
                predicates.add(builder.equal(root.get("plantillaId"), plantillaId));
            } else {
                predicates.add(builder.isNull(root.get("plantillaId")));
            }
            predicates.add(builder.lessThanOrEqualTo(root.get("effectiveFrom"), asOf));
            predicates.add(builder.or(builder.isNull(root.get("effectiveTo")),
                    builder.greaterThanOrEqualTo(root.get("effectiveTo"), asOf)));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}

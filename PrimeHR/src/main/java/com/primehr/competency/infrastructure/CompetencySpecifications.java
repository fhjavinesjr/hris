package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.Competency;
import com.primehr.shared.audit.AgencyAuditableEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CompetencySpecifications {

    private CompetencySpecifications() {
    }

    public static <T extends AgencyAuditableEntity> Specification<T> scoped(String agencyId,
                                                                             Boolean active,
                                                                             LocalDate asOf) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("agencyId"), agencyId));
            if (active != null) {
                Predicate effective = builder.and(
                        builder.isTrue(root.get("active")),
                        builder.or(builder.isNull(root.get("effectiveFrom")),
                                builder.lessThanOrEqualTo(root.get("effectiveFrom"), asOf)),
                        builder.or(builder.isNull(root.get("effectiveTo")),
                                builder.greaterThanOrEqualTo(root.get("effectiveTo"), asOf)));
                predicates.add(active ? effective : builder.not(effective));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Competency> competencyFilter(String agencyId, String categoryId,
                                                               Boolean active, String search, LocalDate asOf) {
        Specification<Competency> specification = scoped(agencyId, active, asOf);
        if (categoryId != null && !categoryId.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("category").get("id"), categoryId.trim()));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("code")), pattern),
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("definition")), pattern)));
        }
        return specification;
    }
}

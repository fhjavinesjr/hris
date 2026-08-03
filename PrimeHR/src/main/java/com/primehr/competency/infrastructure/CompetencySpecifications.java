package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.Competency;
import com.primehr.shared.audit.AgencyAuditableEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import com.primehr.competency.domain.DefinitionStatus;
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

    public static <T extends AgencyAuditableEntity> Specification<T> adminFilter(
            String agencyId, DefinitionStatus status, String search, LocalDate asOf) {
        return (root, query, builder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(builder.equal(root.get("agencyId"), agencyId));
            if (status != null) predicates.add(builder.equal(root.get("status"), status.name()));
            if (asOf != null) {
                predicates.add(builder.or(builder.isNull(root.get("effectiveFrom")),
                        builder.lessThanOrEqualTo(root.get("effectiveFrom"), asOf)));
                predicates.add(builder.or(builder.isNull(root.get("effectiveTo")),
                        builder.greaterThanOrEqualTo(root.get("effectiveTo"), asOf)));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("code")), pattern),
                        builder.like(builder.lower(root.get("name")), pattern)));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    public static Specification<Competency> adminCompetencyFilter(
            String agencyId, DefinitionStatus status, String categoryId, String search, LocalDate asOf) {
        Specification<Competency> specification = adminFilter(agencyId, status, search, asOf);
        if (categoryId != null && !categoryId.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("category").get("id"), categoryId.trim()));
        }
        return specification;
    }
}

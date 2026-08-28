package com.humanresource.integration.primehr;

import com.humanresource.repositories.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class AssessmentSubjectIntegrationServiceImpl implements AssessmentSubjectIntegrationService {
    private final EmployeeRepository employees;

    public AssessmentSubjectIntegrationServiceImpl(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Override
    public AssessmentSubjectPageResponse list(String search, int page, int size, boolean activeOnly) {
        validatePage(page, size);
        if (!activeOnly) throw new IllegalArgumentException("Phase 3.1 supports active assessment subjects only");
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        Instant fetchedAt = Instant.now();
        Page<AssessmentSubjectRow> result = employees.findPrimeHrAssessmentSubjects(term, LocalDateTime.now(),
                PageRequest.of(page, size, Sort.by("employeeNo").ascending().and(Sort.by("employeeId"))));
        return new AssessmentSubjectPageResponse(result.getContent().stream()
                .map(row -> response(row, fetchedAt)).toList(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Override
    public AssessmentSubjectResponse get(Long employeeId) {
        if (employeeId == null || employeeId < 1) throw new IllegalArgumentException("employeeId must be positive");
        AssessmentSubjectRow row = employees.findPrimeHrAssessmentSubject(employeeId, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Eligible active assessment subject " + employeeId + " was not found"));
        return response(row, Instant.now());
    }

    private static AssessmentSubjectResponse response(AssessmentSubjectRow row, Instant fetchedAt) {
        String displayName = Stream.of(row.firstname(), row.lastname(), row.suffix())
                .filter(value -> value != null && !value.isBlank()).map(String::trim)
                .collect(Collectors.joining(" "));
        String source = String.join("|", value(row.employeeId()), value(row.employeeNo()), displayName,
                value(row.employeeUpdatedAt()), value(row.appointmentId()), value(row.assumptionToDutyDate()),
                value(row.jobPositionId()), value(row.plantillaId()));
        return new AssessmentSubjectResponse(row.employeeId(), row.employeeNo(), displayName, true,
                row.appointmentId(), row.assumptionToDutyDate(), longValue(row.jobPositionId()),
                longValue(row.plantillaId()), fingerprint(source),
                latest(row.employeeUpdatedAt(), row.assumptionToDutyDate()), fetchedAt);
    }

    private static LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.isAfter(right) ? left : right;
    }

    private static Long longValue(Integer value) { return value == null ? null : value.longValue(); }
    private static String value(Object value) { return value == null ? "" : value.toString().trim(); }

    private static String fingerprint(String source) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }
}

package com.administrative.impl;

import com.administrative.dtos.PositionTargetPageResponse;
import com.administrative.dtos.PositionTargetResponse;
import com.administrative.dtos.PositionTargetType;
import com.administrative.entitymodels.JobPosition;
import com.administrative.entitymodels.Plantilla;
import com.administrative.repositories.JobPositionRepository;
import com.administrative.repositories.PlantillaRepository;
import com.administrative.services.PositionTargetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PositionTargetServiceImpl implements PositionTargetService {
    private final JobPositionRepository jobPositions;
    private final PlantillaRepository plantillas;

    public PositionTargetServiceImpl(JobPositionRepository jobPositions, PlantillaRepository plantillas) {
        this.jobPositions = jobPositions;
        this.plantillas = plantillas;
    }

    @Override
    public PositionTargetPageResponse list(PositionTargetType type, String search, int page, int size) {
        validatePage(page, size);
        String term = search == null ? "" : search.trim();
        Instant fetchedAt = Instant.now();
        if (type == PositionTargetType.JOB_POSITION) {
            Page<JobPosition> result = term.isEmpty()
                    ? jobPositions.findAll(PageRequest.of(page, size,
                    Sort.by("jobPositionName").ascending().and(Sort.by("jobPositionId"))))
                    : jobPositions.findByJobPositionNameContainingIgnoreCase(term, PageRequest.of(page, size,
                    Sort.by("jobPositionName").ascending().and(Sort.by("jobPositionId"))));
            return page(result, item -> jobPosition(item, fetchedAt));
        }

        Page<Plantilla> result = term.isEmpty()
                ? plantillas.findAll(PageRequest.of(page, size,
                Sort.by("plantillaName").ascending().and(Sort.by("plantillaId"))))
                : plantillas.findByPlantillaNameContainingIgnoreCase(term, PageRequest.of(page, size,
                Sort.by("plantillaName").ascending().and(Sort.by("plantillaId"))));
        Map<Long, JobPosition> parents = jobPositions.findAllById(result.getContent().stream()
                        .map(Plantilla::getJobPositionId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(JobPosition::getJobPositionId, Function.identity()));
        return page(result, item -> plantilla(item, requireParent(item, parents), fetchedAt));
    }

    @Override
    public PositionTargetResponse get(PositionTargetType type, Long id) {
        if (id == null || id < 1) throw new IllegalArgumentException("id must be positive");
        Instant fetchedAt = Instant.now();
        if (type == PositionTargetType.JOB_POSITION) {
            return jobPosition(jobPositions.findById(id)
                    .orElseThrow(() -> notFound(type, id)), fetchedAt);
        }
        Plantilla plantilla = plantillas.findById(id).orElseThrow(() -> notFound(type, id));
        JobPosition parent = jobPositions.findById(plantilla.getJobPositionId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        "The Plantilla parent Job Position was not found"));
        return plantilla(plantilla, parent, fetchedAt);
    }

    private static PositionTargetResponse jobPosition(JobPosition item, Instant fetchedAt) {
        String source = String.join("|", PositionTargetType.JOB_POSITION.name(), value(item.getJobPositionId()),
                value(item.getJobPositionName()), value(item.getSalaryGrade()), value(item.getSalaryStep()));
        return new PositionTargetResponse(PositionTargetType.JOB_POSITION, item.getJobPositionId(),
                item.getJobPositionId(), item.getJobPositionName(), item.getSalaryGrade(), item.getSalaryStep(),
                null, null, fingerprint(source), fetchedAt);
    }

    private static PositionTargetResponse plantilla(Plantilla item, JobPosition parent, Instant fetchedAt) {
        String source = String.join("|", PositionTargetType.PLANTILLA.name(), value(item.getPlantillaId()),
                value(item.getPlantillaName()), value(parent.getJobPositionId()),
                value(parent.getJobPositionName()), value(parent.getSalaryGrade()), value(parent.getSalaryStep()));
        return new PositionTargetResponse(PositionTargetType.PLANTILLA, item.getPlantillaId(),
                parent.getJobPositionId(), parent.getJobPositionName(), parent.getSalaryGrade(),
                parent.getSalaryStep(), item.getPlantillaId(), item.getPlantillaName(), fingerprint(source), fetchedAt);
    }

    private static JobPosition requireParent(Plantilla item, Map<Long, JobPosition> parents) {
        JobPosition parent = parents.get(item.getJobPositionId());
        if (parent == null) throw new ResponseStatusException(NOT_FOUND,
                "The Plantilla parent Job Position was not found");
        return parent;
    }

    private static <T> PositionTargetPageResponse page(Page<T> source,
                                                       Function<T, PositionTargetResponse> mapper) {
        return new PositionTargetPageResponse(source.getContent().stream().map(mapper).toList(),
                source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages(),
                source.isFirst(), source.isLast());
    }

    private static String fingerprint(String source) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static ResponseStatusException notFound(PositionTargetType type, Long id) {
        return new ResponseStatusException(NOT_FOUND, type + " target " + id + " was not found");
    }

    private static void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }
}

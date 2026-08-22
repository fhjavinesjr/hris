package com.administrative.impl;

import com.administrative.dtos.PositionTargetType;
import com.administrative.entitymodels.JobPosition;
import com.administrative.entitymodels.Plantilla;
import com.administrative.repositories.JobPositionRepository;
import com.administrative.repositories.PlantillaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PositionTargetServiceImplTest {
    private final JobPositionRepository jobPositions = mock(JobPositionRepository.class);
    private final PlantillaRepository plantillas = mock(PlantillaRepository.class);
    private final PositionTargetServiceImpl service = new PositionTargetServiceImpl(jobPositions, plantillas);

    @Test
    void mapsJobPositionWithoutCreatingASecondMaster() {
        JobPosition position = position(14L, "Administrative Officer IV", 15L, 2L);
        when(jobPositions.findById(14L)).thenReturn(Optional.of(position));

        var result = service.get(PositionTargetType.JOB_POSITION, 14L);

        assertThat(result.targetId()).isEqualTo(14L);
        assertThat(result.jobPositionName()).isEqualTo("Administrative Officer IV");
        assertThat(result.plantillaId()).isNull();
        assertThat(result.sourceFingerprint()).hasSize(64);
        assertThat(result.fetchedAt()).isNotNull();
    }

    @Test
    void plantillaResponseIncludesItsAuthoritativeParentJobPosition() {
        Plantilla plantilla = new Plantilla(25L, "HRMO-001", 14L);
        JobPosition position = position(14L, "Administrative Officer IV", 15L, 2L);
        when(plantillas.findById(25L)).thenReturn(Optional.of(plantilla));
        when(jobPositions.findById(14L)).thenReturn(Optional.of(position));

        var result = service.get(PositionTargetType.PLANTILLA, 25L);

        assertThat(result.type()).isEqualTo(PositionTargetType.PLANTILLA);
        assertThat(result.plantillaName()).isEqualTo("HRMO-001");
        assertThat(result.jobPositionId()).isEqualTo(14L);
        assertThat(result.sourceFingerprint()).hasSize(64);
    }

    @Test
    void paginatesAndSearchesByTargetType() {
        JobPosition position = position(14L, "Administrative Officer IV", 15L, 2L);
        when(jobPositions.findByJobPositionNameContainingIgnoreCase(eq("officer"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(position)));

        var result = service.list(PositionTargetType.JOB_POSITION, " officer ", 0, 20);

        assertThat(result.content()).singleElement()
                .satisfies(item -> assertThat(item.targetId()).isEqualTo(14L));
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidPagination() {
        assertThatThrownBy(() -> service.list(PositionTargetType.JOB_POSITION, null, -1, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.list(PositionTargetType.JOB_POSITION, null, 0, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static JobPosition position(Long id, String name, Long grade, Long step) {
        JobPosition position = new JobPosition(name, grade, step);
        position.setJobPositionId(id);
        return position;
    }
}

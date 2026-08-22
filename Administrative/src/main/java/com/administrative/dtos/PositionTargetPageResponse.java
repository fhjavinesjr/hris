package com.administrative.dtos;

import java.util.List;

public record PositionTargetPageResponse(
        List<PositionTargetResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}

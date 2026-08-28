package com.humanresource.integration.primehr;

import java.util.List;

public record AssessmentSubjectPageResponse(List<AssessmentSubjectResponse> content, int page, int size,
        long totalElements, int totalPages, boolean first, boolean last) {
}

package com.primehr.integration.humanresource;

import java.util.List;

public record HumanResourceAssessmentSubjectPage(List<HumanResourceAssessmentSubject> content, int page, int size,
        long totalElements, int totalPages, boolean first, boolean last) {
}

package com.primehr.succession.report;

import java.util.List;

public final class SuccessionReportData {
    private SuccessionReportData() { }

    public static final class Row {
        private final String section;
        private final String label;
        private final String value;

        public Row(String section, String label, String value) {
            this.section = section;
            this.label = label;
            this.value = value;
        }

        public String getSection() { return section; }
        public String getLabel() { return label; }
        public String getValue() { return value; }
    }

    public record Analytics(int minimumCohort, int cohortSize, boolean suppressed,
                            String suppressionReason, List<CoverageMetric> metrics) { }
    public record CoverageMetric(String coverageStatus, long positions, long benchMembers) { }
}

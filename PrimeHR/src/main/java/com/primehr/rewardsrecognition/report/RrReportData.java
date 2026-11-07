package com.primehr.rewardsrecognition.report;

import java.util.List;

public final class RrReportData {
    private RrReportData() {
    }

    public static final class Row {
        private final String section;
        private final String label;
        private final String value;

        public Row(String section, String label, String value) {
            this.section = section;
            this.label = label;
            this.value = value;
        }

        public String getSection() {
            return section;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }

    public record Analytics(
            int year,
            int minimumCohort,
            int cohortSize,
            boolean suppressed,
            String suppressionReason,
            List<Metric> metrics
    ) {
    }

    public record Metric(String category, long awards, long revoked) {
    }
}

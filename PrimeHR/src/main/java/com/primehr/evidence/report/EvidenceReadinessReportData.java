package com.primehr.evidence.report;

public final class EvidenceReadinessReportData {
    private EvidenceReadinessReportData() { }

    public static final class Row {
        private final String section;
        private final String item;
        private final String value;
        private final String status;

        public Row(String section, String item, String value, String status) {
            this.section = section;
            this.item = item;
            this.value = value;
            this.status = status;
        }

        public String getSection() { return section; }
        public String getItem() { return item; }
        public String getValue() { return value; }
        public String getStatus() { return status; }
    }
}

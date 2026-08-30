package com.primehr.rsp.report;

/** JavaBean used only as the portable Jasper data source. */
public final class VacancyNoticeReportRow {
    private final Integer rowNumber;
    private final String competency;
    private final String requiredLevel;
    private final String classification;
    private final String criticality;
    private final String remarks;

    public VacancyNoticeReportRow(Integer rowNumber, String competency, String requiredLevel,
                                  String classification, String criticality, String remarks) {
        this.rowNumber = rowNumber;
        this.competency = competency;
        this.requiredLevel = requiredLevel;
        this.classification = classification;
        this.criticality = criticality;
        this.remarks = remarks;
    }

    public Integer getRowNumber() { return rowNumber; }
    public String getCompetency() { return competency; }
    public String getRequiredLevel() { return requiredLevel; }
    public String getClassification() { return classification; }
    public String getCriticality() { return criticality; }
    public String getRemarks() { return remarks; }
}

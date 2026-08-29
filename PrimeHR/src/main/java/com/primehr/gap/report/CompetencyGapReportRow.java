package com.primehr.gap.report;

/** JavaBean used only as the portable Jasper data source. */
public final class CompetencyGapReportRow {
    private final Integer rowNumber;
    private final String competency;
    private final String requiredLevel;
    private final String attainedLevel;
    private final String formulaResult;
    private final String classification;
    private final String requirement;
    private final String priority;
    private final String explanation;

    public CompetencyGapReportRow(Integer rowNumber, String competency, String requiredLevel,
                                  String attainedLevel, String formulaResult, String classification,
                                  String requirement, String priority, String explanation) {
        this.rowNumber = rowNumber;
        this.competency = competency;
        this.requiredLevel = requiredLevel;
        this.attainedLevel = attainedLevel;
        this.formulaResult = formulaResult;
        this.classification = classification;
        this.requirement = requirement;
        this.priority = priority;
        this.explanation = explanation;
    }

    public Integer getRowNumber() { return rowNumber; }
    public String getCompetency() { return competency; }
    public String getRequiredLevel() { return requiredLevel; }
    public String getAttainedLevel() { return attainedLevel; }
    public String getFormulaResult() { return formulaResult; }
    public String getClassification() { return classification; }
    public String getRequirement() { return requirement; }
    public String getPriority() { return priority; }
    public String getExplanation() { return explanation; }
}

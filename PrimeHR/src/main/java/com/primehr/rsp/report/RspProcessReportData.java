package com.primehr.rsp.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.primehr.rsp.domain.VacancyPublicationStatus;
import com.primehr.rsp.evaluation.domain.EvaluationProceeding;
import com.primehr.rsp.selection.domain.SelectionCase;

public final class RspProcessReportData {
    private RspProcessReportData() {}

    public enum DateBasis { PUBLICATION_DATE, PUBLICATION_CLOSING_DATE, PROCEEDING_FINALIZATION_DATE,
        SELECTION_FINALIZATION_DATE, HANDOFF_ACKNOWLEDGMENT_DATE }

    public record RegisterQuery(LocalDate from, LocalDate to, DateBasis dateBasis,
                                boolean includeHistory, int page, int size,
                                VacancyPublicationStatus publicationStatus,
                                EvaluationProceeding.Status proceedingStatus,
                                SelectionCase.Status selectionStatus, SelectionCase.Outcome outcome,
                                Long jobPositionId, Long plantillaId, Long areaId, Long businessUnitId) {}

    public record RegisterPage(String agencyId, LocalDate from, LocalDate to, String dateBasis,
                               boolean includeHistory, String filterSummary, String timezone, int page, int size,
                               long totalElements, int totalPages, List<RegisterRow> rows) {}

    public static final class RegisterRow {
        private final String publicationId; private final String vacancy; private final String businessUnit;
        private final String closingDate; private final String publicationStatus; private final int applicationCount;
        private final int withdrawnCount; private final int qualifiedCount; private final int disqualifiedCount;
        private final int correctedScreeningCount; private final String proceedingStatus; private final String selectionId;
        private final String selectionRevision; private final String selectionStatus; private final String outcome;
        private final String offerStatus; private final String handoffStatus; private final String receiptState;
        private final int failedHandoffAttempts; private final String finalizedAt;
        public RegisterRow(String publicationId,String vacancy,String businessUnit,String closingDate,String publicationStatus,
                           int applicationCount,int withdrawnCount,int qualifiedCount,int disqualifiedCount,
                           int correctedScreeningCount,String proceedingStatus,String selectionId,String selectionRevision,
                           String selectionStatus,String outcome,String offerStatus,String handoffStatus,String receiptState,
                           int failedHandoffAttempts,String finalizedAt){this.publicationId=publicationId;this.vacancy=vacancy;
            this.businessUnit=businessUnit;this.closingDate=closingDate;this.publicationStatus=publicationStatus;
            this.applicationCount=applicationCount;this.withdrawnCount=withdrawnCount;this.qualifiedCount=qualifiedCount;
            this.disqualifiedCount=disqualifiedCount;this.correctedScreeningCount=correctedScreeningCount;
            this.proceedingStatus=proceedingStatus;this.selectionId=selectionId;this.selectionRevision=selectionRevision;
            this.selectionStatus=selectionStatus;this.outcome=outcome;this.offerStatus=offerStatus;
            this.handoffStatus=handoffStatus;this.receiptState=receiptState;this.failedHandoffAttempts=failedHandoffAttempts;
            this.finalizedAt=finalizedAt;}
        public String getPublicationId(){return publicationId;} public String getVacancy(){return vacancy;}
        public String getBusinessUnit(){return businessUnit;} public String getClosingDate(){return closingDate;}
        public String getPublicationStatus(){return publicationStatus;} public int getApplicationCount(){return applicationCount;}
        public int getWithdrawnCount(){return withdrawnCount;} public int getQualifiedCount(){return qualifiedCount;}
        public int getDisqualifiedCount(){return disqualifiedCount;} public int getCorrectedScreeningCount(){return correctedScreeningCount;}
        public String getProceedingStatus(){return proceedingStatus;} public String getSelectionId(){return selectionId;}
        public String getSelectionRevision(){return selectionRevision;} public String getSelectionStatus(){return selectionStatus;}
        public String getOutcome(){return outcome;} public String getOfferStatus(){return offerStatus;}
        public String getHandoffStatus(){return handoffStatus;} public String getReceiptState(){return receiptState;}
        public int getFailedHandoffAttempts(){return failedHandoffAttempts;} public String getFinalizedAt(){return finalizedAt;}
    }

    public static final class Metric {
        private final String code; private final String label; private final long numerator; private final Long denominator;
        private final BigDecimal value; private final String unit; private final String definition;
        public Metric(String code,String label,long numerator,Long denominator,BigDecimal value,String unit,String definition){this.code=code;this.label=label;this.numerator=numerator;this.denominator=denominator;this.value=value;this.unit=unit;this.definition=definition;}
        public String getCode(){return code;} public String getLabel(){return label;} public long getNumerator(){return numerator;}
        public Long getDenominator(){return denominator;} public BigDecimal getValue(){return value;} public String getUnit(){return unit;}
        public String getDefinition(){return definition;} public String code(){return code;} public String label(){return label;}
        public long numerator(){return numerator;} public Long denominator(){return denominator;} public BigDecimal value(){return value;}
        public String unit(){return unit;} public String definition(){return definition;}
    }
    public record Analytics(String agencyId, LocalDate from, LocalDate to, String dateBasis,
                            String timezone, boolean includeHistory, String filterSummary, List<Metric> metrics) {}
}

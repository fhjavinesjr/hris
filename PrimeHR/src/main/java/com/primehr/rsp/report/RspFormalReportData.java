package com.primehr.rsp.report;

import java.math.BigDecimal;
import java.util.List;

public final class RspFormalReportData {
    private RspFormalReportData() {}

    public record Comparative(String agency, String proceedingId, String publicationId, String vacancy,
                              String assignment, String policy, String committee, String meeting,
                              String sourceFingerprint, String finalized, String generated,
                              List<ComparativeRow> rows) {}
    public static final class ComparativeRow {
        private final int rowNumber;private final String candidate;private final String applicationId;
        private final BigDecimal totalScore;private final int rankNumber;private final int tieGroup;
        private final String stageBreakdown;private final String gateResult;private final String recommendation;
        public ComparativeRow(int rowNumber,String candidate,String applicationId,BigDecimal totalScore,int rankNumber,int tieGroup,String stageBreakdown,String gateResult,String recommendation){this.rowNumber=rowNumber;this.candidate=candidate;this.applicationId=applicationId;this.totalScore=totalScore;this.rankNumber=rankNumber;this.tieGroup=tieGroup;this.stageBreakdown=stageBreakdown;this.gateResult=gateResult;this.recommendation=recommendation;}
        public int getRowNumber(){return rowNumber;}public String getCandidate(){return candidate;}public String getApplicationId(){return applicationId;}public BigDecimal getTotalScore(){return totalScore;}public int getRankNumber(){return rankNumber;}public int getTieGroup(){return tieGroup;}public String getStageBreakdown(){return stageBreakdown;}public String getGateResult(){return gateResult;}public String getRecommendation(){return recommendation;}
    }

    public record Selection(String agency, String selectionId, String proceedingId, String publicationId,
                            String vacancy, String revision, String outcome, String authorityDecision,
                            String offer, String handoff, String sourceFingerprint, String finalized,
                            String generated, List<SelectionRow> rows) {}
    public static final class SelectionRow {
        private final int rowNumber;private final String candidate;private final String applicationId;
        private final BigDecimal totalScore;private final int rankNumber;private final String recommendation;private final String disposition;
        public SelectionRow(int rowNumber,String candidate,String applicationId,BigDecimal totalScore,int rankNumber,String recommendation,String disposition){this.rowNumber=rowNumber;this.candidate=candidate;this.applicationId=applicationId;this.totalScore=totalScore;this.rankNumber=rankNumber;this.recommendation=recommendation;this.disposition=disposition;}
        public int getRowNumber(){return rowNumber;}public String getCandidate(){return candidate;}public String getApplicationId(){return applicationId;}public BigDecimal getTotalScore(){return totalScore;}public int getRankNumber(){return rankNumber;}public String getRecommendation(){return recommendation;}public String getDisposition(){return disposition;}
    }

    public record EvidenceIndex(String agency, String selectionId, String proceedingId, String publicationId,
                                String vacancy, String sourceFingerprint, String terminalStatus,
                                String generated, List<EvidenceRow> rows) {}
    public static final class EvidenceRow {
        private final int rowNumber;private final String recordType;private final String recordId;private final String revision;private final String classification;private final String retentionTag;private final String checksum;private final String custodian;private final String status;private final String eventAt;
        public EvidenceRow(int rowNumber,String recordType,String recordId,String revision,String classification,String retentionTag,String checksum,String custodian,String status,String eventAt){this.rowNumber=rowNumber;this.recordType=recordType;this.recordId=recordId;this.revision=revision;this.classification=classification;this.retentionTag=retentionTag;this.checksum=checksum;this.custodian=custodian;this.status=status;this.eventAt=eventAt;}
        public int getRowNumber(){return rowNumber;}public String getRecordType(){return recordType;}public String getRecordId(){return recordId;}public String getRevision(){return revision;}public String getClassification(){return classification;}public String getRetentionTag(){return retentionTag;}public String getChecksum(){return checksum;}public String getCustodian(){return custodian;}public String getStatus(){return status;}public String getEventAt(){return eventAt;}
        public String recordType(){return recordType;}public String recordId(){return recordId;}public String revision(){return revision;}public String classification(){return classification;}public String retentionTag(){return retentionTag;}public String checksum(){return checksum;}public String custodian(){return custodian;}public String status(){return status;}public String eventAt(){return eventAt;}
    }
}

package com.primehr.performancemanagement.report.application;

import com.primehr.integration.administrative.AdministrativePerformanceReportSourceClient;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;
import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.*;
import com.primehr.performancemanagement.report.infrastructure.PerformanceReportMetadataSnapshotRepository;
import com.primehr.shared.exception.ApplicationConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@Transactional
public class PerformanceReportMetadataPreparationService {
    private final PerformanceReportMetadataSnapshotRepository snapshots;private final PerformanceReportMetadataSnapshotService capture;
    private final PerformanceCommitmentRouteRepository routes;private final PerformanceCommitmentRouteStepRepository steps;
    private final AdministrativePerformanceReportSourceClient administrative;
    public PerformanceReportMetadataPreparationService(PerformanceReportMetadataSnapshotRepository snapshots,PerformanceReportMetadataSnapshotService capture,PerformanceCommitmentRouteRepository routes,PerformanceCommitmentRouteStepRepository steps,AdministrativePerformanceReportSourceClient administrative){this.snapshots=snapshots;this.capture=capture;this.routes=routes;this.steps=steps;this.administrative=administrative;}

    public PerformanceReportMetadataSnapshot commitment(String agency,ReportKind kind,PerformanceCommitmentVersion v,String token,String actor,PermissionDataScope scope){return ensure(agency,kind,SourceType.COMMITMENT_VERSION,v.getId(),v.getDefinitionVersion(),v.getVersion(),v.getContentFingerprint(),v,token,actor,scope);}
    public PerformanceReportMetadataSnapshot finalRating(String agency,ReportKind kind,PerformanceFinalRating rating,PerformanceCommitmentVersion v,String token,String actor,PermissionDataScope scope){return ensure(agency,kind,SourceType.FINAL_RATING,rating.getId(),rating.getRevisionNo(),rating.getVersion(),rating.getCalculationFingerprint(),v,token,actor,scope);}
    public PerformanceReportMetadataSnapshot monitoring(String agency,PerformanceMonitoringCase value,PerformanceCommitmentVersion v,String token,String actor,PermissionDataScope scope){return ensure(agency,ReportKind.MONITORING,SourceType.MONITORING_CASE,value.getId(),value.getAccomplishmentRevision(),value.getVersion(),sha(value.getCommitmentFingerprint(),String.valueOf(value.getAccomplishmentRevision()),value.getStatus().name()),v,token,actor,scope);}
    public PerformanceReportMetadataSnapshot coaching(String agency,PerformanceCoachingSession value,PerformanceCommitmentVersion v,String token,String actor,PermissionDataScope scope){return ensure(agency,ReportKind.COACHING,SourceType.COACHING_SESSION,value.getId(),value.getRevisionNo(),value.getVersion(),sha(value.getId(),String.valueOf(value.getRevisionNo()),value.getStatus().name()),v,token,actor,scope);}
    public PerformanceReportMetadataSnapshot intervention(String agency,ReportKind kind,PerformanceIntervention value,PerformanceCommitmentVersion v,String token,String actor,PermissionDataScope scope){return ensure(agency,kind,SourceType.INTERVENTION,value.getId(),1,value.getVersion(),sha(value.getId(),value.getFinalRatingId(),value.getStatus().name(),value.getAction().name()),v,token,actor,scope);}

    private PerformanceReportMetadataSnapshot ensure(String agency,ReportKind kind,SourceType sourceType,String sourceId,int businessRevision,long recordVersion,String sourceFingerprint,PerformanceCommitmentVersion v,String token,String actor,PermissionDataScope scope){Optional<PerformanceReportMetadataSnapshot> existing=snapshots.findByAgencyIdAndReportKindAndSourceTypeAndSourceIdAndSourceBusinessRevision(agency,kind,sourceType,sourceId,businessRevision);if(existing.isPresent()){access(existing.get(),actor,scope);return existing.get();}PerformanceCommitmentRoute route=routes.findFirstByAgencyIdAndCommitmentVersionIdOrderByRouteRevisionDesc(agency,v.getId()).orElseThrow(()->new ApplicationConflictException("Approved commitment route metadata is missing"));List<PerformanceCommitmentRouteStep> routeSteps=steps.findByAgencyIdAndRouteIdOrderByLevelAsc(agency,route.getId());if(routeSteps.isEmpty())throw new ApplicationConflictException("Approved commitment route steps are missing");access(v,routeSteps,actor,scope);var branding=administrative.branding(token);var owner=administrative.position(v.getOwnerPlantillaId(),token);PerformanceCommitmentRouteStep supervisor=routeSteps.get(0),approver=routeSteps.get(routeSteps.size()-1);var supervisorPosition=administrative.position(supervisor.getPlantillaId(),token);var approverPosition=administrative.position(approver.getPlantillaId(),token);PerformanceReportMetadataInput input=new PerformanceReportMetadataInput(kind,sourceType,sourceId,businessRevision,recordVersion,sourceFingerprint,"SPMS_"+kind.name(),1,v.getOwnerEmployeeId(),v.getOwnerEmployeeNo(),v.getOwnerName(),owner.jobPositionName(),owner.plantillaName(),v.getAreaId(),v.getAreaName(),v.getBusinessUnitId(),v.getBusinessUnitName(),supervisor.getEmployeeNo(),supervisor.getEmployeeName(),supervisorPosition.jobPositionName(),approver.getEmployeeNo(),approver.getEmployeeName(),approverPosition.jobPositionName(),v.getPeriodStart(),v.getPeriodEnd(),branding.companyName(),branding.address(),branding.sourceFingerprint());return capture.capture(agency,input,actor);}
    private static void access(PerformanceReportMetadataSnapshot value,String actor,PermissionDataScope scope){if(scope==PermissionDataScope.AGENCY_WIDE||same(actor,value.getEmployeeNo()))return;if(scope==PermissionDataScope.ASSIGNED_RECORDS&&(same(actor,value.getSupervisorEmployeeNo())||same(actor,value.getApproverEmployeeNo())))return;throw new AccessDeniedException("Report is outside the permitted record scope");}
    private static void access(PerformanceCommitmentVersion value,List<PerformanceCommitmentRouteStep> routeSteps,String actor,PermissionDataScope scope){if(scope==PermissionDataScope.AGENCY_WIDE||same(actor,value.getOwnerEmployeeNo()))return;if(scope==PermissionDataScope.ASSIGNED_RECORDS&&routeSteps.stream().anyMatch(step->same(actor,step.getEmployeeNo())))return;throw new AccessDeniedException("Report is outside the permitted record scope");}
    private static boolean same(String left,String right){return right!=null&&right.equalsIgnoreCase(left);}
    private static String sha(String... values){try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(String.join("\u001f",values).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}

package com.primehr.performancemanagement;

import com.primehr.integration.administrative.AdministrativePerformancePlanningClient;
import com.primehr.integration.humanresource.*;
import com.primehr.PrimeHRApplication;
import com.primehr.performancemanagement.api.PerformanceObjectiveDtos.*;
import com.primehr.performancemanagement.api.PerformanceObjectiveDtos;
import com.primehr.performancemanagement.api.PerformancePlanAssignmentDtos.*;
import com.primehr.performancemanagement.application.*;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes=PrimeHRApplication.class) @ActiveProfiles("test") @Transactional
class PerformancePlanningFoundationIntegrationTest {
    @Autowired PerformanceObjectiveService objectiveService;@Autowired PerformancePlanAssignmentService assignmentService;
    @Autowired PerformancePolicyRepository policyRoots;@Autowired PerformancePolicyVersionRepository policyVersions;
    @Autowired PerformanceCycleRepository cycles;@Autowired PerformanceTemplateRepository templateRoots;
    @Autowired PerformanceTemplateVersionRepository templateVersions;
    @MockBean AdministrativePerformancePlanningClient administrative;
    @MockBean HumanResourcePerformanceParticipantClient humanResource;

    @Test void objectiveVersionsAreImmutableAuditedAndRequirePublishedHierarchy(){String a="PLAN-OBJECTIVE";PerformancePolicyVersion policy=policy(a);
        ObjectiveResponse agency=objectiveService.create(a,new ObjectiveInput("AGENCY-1","Agency objective","Improve service","Faster care","Agency strategy",PerformanceObjectiveVersion.Level.AGENCY,null,null,policy.getId(),null,null,null,null,null,null),"Bearer x",null);
        ObjectiveResponse published=objectiveService.publish(a,agency.id(),new PerformanceObjectiveDtos.Transition(agency.recordVersion(),"approved",LocalDate.of(2026,1,1),null),"Bearer x",null);
        assertThat(published.status()).isEqualTo("PUBLISHED");assertThatThrownBy(()->objectiveService.update(a,published.id(),new ObjectiveInput("AGENCY-1","Changed","Improve service","Faster care","Agency strategy",PerformanceObjectiveVersion.Level.AGENCY,null,null,policy.getId(),null,null,null,null,null,published.recordVersion()),"Bearer x",null)).hasMessageContaining("draft");
        when(administrative.organization("AREA",7L,"Bearer x")).thenReturn(new AdministrativePerformancePlanningClient.OrganizationTarget("AREA",7L,"MED","Medical",7L,"Medical","org-fp",Instant.now()));
        ObjectiveResponse area=objectiveService.create(a,new ObjectiveInput("AREA-1","Area objective","Improve clinics","Shorter queues","Agency strategy",PerformanceObjectiveVersion.Level.AREA,7L,published.id(),policy.getId(),null,null,null,null,null,null),"Bearer x",null);
        assertThat(area.parentObjectiveVersionId()).isEqualTo(published.id());assertThat(area.sourceFingerprint()).isEqualTo("org-fp");
    }
    @Test void employeeAssignmentSnapshotsAllAuthoritiesAndRejectsDriftAtActivation(){String a="PLAN-ASSIGNMENT";PerformancePolicyVersion policy=policy(a);PerformanceCycle cycle=cycle(a,policy);PerformanceTemplateVersion template=template(a,policy);ObjectiveResponse objective=objectiveService.create(a,new ObjectiveInput("A","Agency","Statement","Outcome","Strategy",PerformanceObjectiveVersion.Level.AGENCY,null,null,policy.getId(),null,null,null,null,null,null),"Bearer x",null);objective=objectiveService.publish(a,objective.id(),new PerformanceObjectiveDtos.Transition(objective.recordVersion(),"approved",LocalDate.of(2026,1,1),null),"Bearer x",null);
        HumanResourceAssessmentSubject employee=new HumanResourceAssessmentSubject(10L,"E-10","Employee Ten",true,100L,LocalDateTime.of(2025,1,1,8,0),20L,30L,"participant-fp",LocalDateTime.now(),Instant.now());when(humanResource.get(10L,"Bearer x")).thenReturn(employee);
        var membership=new AdministrativePerformancePlanningClient.PersonnelMembership(10L,5L,"BU5","Clinic",7L,"Medical",false,false,"MAIN","membership-fp",Instant.now());when(administrative.membership(10L,"Bearer x")).thenReturn(membership);when(administrative.organization("BUSINESS_UNIT",5L,"Bearer x")).thenReturn(new AdministrativePerformancePlanningClient.OrganizationTarget("BUSINESS_UNIT",5L,"BU5","Clinic",7L,"Medical","bu-fp",Instant.now()));when(administrative.approvalRoute(5L,"PERFORMANCE_INDIVIDUAL_COMMITMENT","Bearer x")).thenReturn(new AdministrativePerformancePlanningClient.ApprovalRoute(1L,"PERFORMANCE_INDIVIDUAL_COMMITMENT",2,5L,7L,List.of(new AdministrativePerformancePlanningClient.ApprovalRouteStep(1L,1,11L)),"route-fp",Instant.now()));
        AssignmentResponse draft=assignmentService.create(a,new AssignmentInput(cycle.getId(),template.getId(),PerformancePlanAssignment.SubjectType.EMPLOYEE,10L,null,10L,List.of(objective.id()),null),"Bearer x",null);assertThat(draft.ownerAppointmentId()).isEqualTo(100L);assertThat(draft.organizationFingerprint()).isEqualTo("membership-fp");
        when(administrative.membership(10L,"Bearer x")).thenReturn(new AdministrativePerformancePlanningClient.PersonnelMembership(10L,5L,"BU5","Clinic",7L,"Medical",false,false,"MAIN","changed-membership",Instant.now()));
        assertThatThrownBy(()->assignmentService.activate(a,draft.id(),new com.primehr.performancemanagement.api.PerformancePlanAssignmentDtos.Transition(draft.recordVersion(),"activate"),"Bearer x",null)).hasMessageContaining("Organization membership changed");
    }
    private PerformancePolicyVersion policy(String a){PerformancePolicy r=policyRoots.saveAndFlush(new PerformancePolicy(a,"P"));PerformancePolicyVersion v=policyVersions.saveAndFlush(new PerformancePolicyVersion(a,r.getId(),1,null,"Policy",null,"Basis","SPMS",PerformancePolicyVersion.Frequency.ANNUAL,false,false,false,false,false));v.publish(LocalDate.of(2026,1,1),null,"tester",Instant.now());return policyVersions.saveAndFlush(v);}
    private PerformanceCycle cycle(String a,PerformancePolicyVersion p){return cycles.saveAndFlush(new PerformanceCycle(a,"C","Cycle",LocalDate.of(2026,1,1),LocalDate.of(2026,12,31),p.getId(),"Asia/Manila"));}
    private PerformanceTemplateVersion template(String a,PerformancePolicyVersion p){PerformanceTemplate r=templateRoots.saveAndFlush(new PerformanceTemplate(a,"IPCR"));PerformanceTemplateVersion v=templateVersions.saveAndFlush(new PerformanceTemplateVersion(a,r.getId(),p.getId(),"unused-scale",1,null,"IPCR",null,PerformanceTemplateVersion.FormType.IPCR,"IPCR","Basis"));v.publish(LocalDate.of(2026,1,1),null,"tester",Instant.now());return templateVersions.saveAndFlush(v);}
}

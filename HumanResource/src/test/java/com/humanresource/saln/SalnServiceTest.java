package com.humanresource.saln;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.entitymodels.Employee;
import com.humanresource.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalnServiceTest {
    @Mock SalnRepository salns; @Mock SalnVersionRepository versions; @Mock SalnAuditRepository audits;
    @Mock SalnCorrectionRepository corrections; @Mock SalnFilingRequirementRepository requirements;
    @Mock EmployeeRepository employees;
    private SalnService service;
    private Saln current;

    @BeforeEach void setUp(){
        service=new SalnService(salns,versions,audits,corrections,requirements,employees,new ObjectMapper().findAndRegisterModules());
        Employee employee=new Employee();employee.setEmployeeId(7L);employee.setEmployeeNo("E-007");employee.setFirstname("Juan");employee.setLastname("Dela Cruz");employee.setPosition("Officer");
        lenient().when(employees.findByEmployeeNoIgnoreCase("E-007")).thenReturn(Optional.of(employee));
        lenient().when(employees.findById(7L)).thenReturn(Optional.of(employee));
        lenient().when(salns.save(any())).thenAnswer(invocation->{Saln s=invocation.getArgument(0);if(s.getId()==null)s.setId(10L);current=s;return s;});
        lenient().when(salns.saveAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));
        lenient().when(salns.findByIdForUpdate(10L)).thenAnswer(invocation->Optional.ofNullable(current));
        lenient().when(salns.findDetailedById(10L)).thenAnswer(invocation->Optional.ofNullable(current));
        lenient().when(requirements.findByEmployeeIdAndFilingTypeAndReferenceDate(any(),any(),any())).thenReturn(Optional.empty());
        lenient().when(corrections.findBySalnIdAndResolvedFalse(10L)).thenReturn(List.of());
    }

    @Test void calculatesTotalsAndEnforcesCorrectionVersioning(){
        SalnDtos.Response draft=service.createDraft("E-007",request(true));
        assertEquals(new BigDecimal("150000.00"),draft.totals().totalAssets());
        assertEquals(new BigDecimal("30000.00"),draft.totals().totalLiabilities());
        assertEquals(new BigDecimal("120000.00"),draft.totals().netWorth());
        assertEquals(LocalDate.of(2027,4,30),draft.dueDate());

        SalnDtos.Response submitted=service.submit("E-007",10L,false);
        assertEquals(SalnTypes.Status.SUBMITTED,submitted.status());assertEquals(1,submitted.versionNo());
        service.startReview(10L,"reviewer");
        service.returnForCorrection(10L,"reviewer",new SalnDtos.CorrectionRequest(List.of(new SalnDtos.CorrectionItem("Assets","Cost","Verify cost."))));
        service.updateDraft("E-007",10L,request(true));
        SalnDtos.Response resubmitted=service.submit("E-007",10L,true);
        assertEquals(SalnTypes.Status.RESUBMITTED,resubmitted.status());assertEquals(2,resubmitted.versionNo());
        service.startReview(10L,"reviewer");service.markCompliant(10L,"reviewer","Complete");
        SalnDtos.Response locked=service.lock(10L,"reviewer","Final");
        assertEquals(SalnTypes.Status.LOCKED,locked.status());
        verify(versions,times(2)).save(any(SalnVersion.class));
    }

    @Test void refusesSubmissionWithoutCertification(){
        service.createDraft("E-007",request(false));
        var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.submit("E-007",10L,false));
        assertTrue(error.getReason().contains("certification"));
        verify(versions,never()).save(any());
    }

    @Test void employeeCannotReadAnotherEmployeesSaln(){
        service.createDraft("E-007",request(true));
        Employee other=new Employee();other.setEmployeeId(8L);other.setEmployeeNo("E-008");
        when(employees.findByEmployeeNoIgnoreCase("E-008")).thenReturn(Optional.of(other));
        var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.myGet("E-008",10L));
        assertEquals(403,error.getStatusCode().value());
    }

    @Test void historicalEncodingPreservesDeclarantSourceAndOriginalFilingDate(){
        LocalDate originalFilingDate=LocalDate.of(2008,4,24);
        SalnDtos.Response encoded=service.encodeHistorical("hr-encoder",new SalnDtos.HistoricalRequest(7L,SalnTypes.SourceType.PAPER,originalFilingDate,"Archive Box 12 / Folder 7",request(true)));
        assertEquals(SalnTypes.Status.COMPLIANT,encoded.status());assertEquals(SalnTypes.SourceType.PAPER,encoded.sourceType());assertEquals(originalFilingDate,encoded.submissionDate());assertEquals("E-007",encoded.employeeNo());assertEquals(1,encoded.versionNo());
        verify(versions).save(any(SalnVersion.class));verify(audits).save(argThat(audit->audit.getAction().equals("HISTORICAL_SALN_ENCODED")&&audit.getPerformedBy().equals("hr-encoder")));
    }

    @Test void annualBulkRequirementsPreviewAndGenerationAreEligibilityBasedAndIdempotent(){
        Employee second=new Employee();second.setEmployeeId(8L);second.setEmployeeNo("E-008");second.setFirstname("Maria");second.setLastname("Santos");
        Employee first=employees.findById(7L).orElseThrow();when(employees.findSalnEligibleEmployees(any())).thenReturn(List.of(first,second));when(employees.count()).thenReturn(3L);
        when(requirements.existsByEmployeeIdAndFilingTypeAndReferenceDate(7L,SalnTypes.FilingType.ANNUAL,LocalDate.of(2026,12,31))).thenReturn(true);
        when(requirements.existsByEmployeeIdAndFilingTypeAndReferenceDate(8L,SalnTypes.FilingType.ANNUAL,LocalDate.of(2026,12,31))).thenReturn(false);
        Saln existingDraft=new Saln();existingDraft.setId(88L);existingDraft.setStatus(SalnTypes.Status.DRAFT);
        when(salns.findFirstByEmployeeIdAndFilingTypeAndReferenceDateAndStatusNotOrderByUpdatedAtDesc(8L,SalnTypes.FilingType.ANNUAL,LocalDate.of(2026,12,31),SalnTypes.Status.VOIDED)).thenReturn(Optional.of(existingDraft));

        SalnDtos.AnnualBulkRequirementResult preview=service.previewAnnualRequirements(2026);
        assertEquals(2,preview.eligibleEmployees());assertEquals(1,preview.requirementsCreated());assertEquals(1,preview.alreadyExisting());assertEquals(1,preview.excludedEmployees());assertEquals(LocalDate.of(2027,4,30),preview.dueDate());
        SalnDtos.AnnualBulkRequirementResult result=service.generateAnnualRequirements("hr-admin",2026);
        assertEquals(1,result.requirementsCreated());assertEquals(1,result.alreadyExisting());
        @SuppressWarnings("unchecked") org.mockito.ArgumentCaptor<List<SalnFilingRequirement>> captor=org.mockito.ArgumentCaptor.forClass(List.class);
        verify(requirements).saveAll(captor.capture());assertEquals(1,captor.getValue().size());SalnFilingRequirement created=captor.getValue().get(0);assertEquals(8L,created.getEmployeeId());assertEquals(SalnTypes.RequirementStatus.DRAFT,created.getStatus());assertEquals(88L,created.getLinkedSalnId());verify(requirements).flush();
    }

    private SalnDtos.DraftRequest request(boolean certified){
        var real=new SalnDtos.RealPropertyItem(SalnTypes.OwnerType.DECLARANT,null,"House and lot","Residential","Quezon City",new BigDecimal("100000"),new BigDecimal("125000"),2020,"Purchase",new BigDecimal("100000"));
        var personal=new SalnDtos.PersonalPropertyItem(SalnTypes.OwnerType.DECLARANT,null,"Vehicle",2021,new BigDecimal("50000"));
        var liability=new SalnDtos.LiabilityItem(SalnTypes.OwnerType.DECLARANT,null,"Auto loan","Government Bank",new BigDecimal("30000"));
        return new SalnDtos.DraftRequest(SalnTypes.FilingType.ANNUAL,2026,LocalDate.of(2026,12,31),"P","Officer","ISOFT Agency","Main Office",null,null,null,null,SalnTypes.FilingMode.NOT_APPLICABLE,null,true,true,certified,"PhilSys","123",LocalDate.of(2025,1,1),List.of(),List.of(real),List.of(personal),List.of(liability),List.of(),List.of(),null);
    }
}

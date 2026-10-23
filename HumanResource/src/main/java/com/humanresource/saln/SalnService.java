package com.humanresource.saln;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.entitymodels.Employee;
import com.humanresource.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SalnService {
    private final SalnRepository salns;
    private final SalnVersionRepository versions;
    private final SalnAuditRepository audits;
    private final SalnCorrectionRepository corrections;
    private final SalnFilingRequirementRepository requirements;
    private final EmployeeRepository employees;
    private final ObjectMapper objectMapper;

    public SalnService(SalnRepository salns, SalnVersionRepository versions, SalnAuditRepository audits,
                       SalnCorrectionRepository corrections, SalnFilingRequirementRepository requirements,
                       EmployeeRepository employees, ObjectMapper objectMapper) {
        this.salns=salns;this.versions=versions;this.audits=audits;this.corrections=corrections;
        this.requirements=requirements;this.employees=employees;this.objectMapper=objectMapper;
    }

    @Transactional
    public List<SalnDtos.Summary> myList(String employeeNo){Employee e=employee(employeeNo);return salns.findByEmployeeIdOrderByCreatedAtDesc(e.getEmployeeId()).stream().map(this::summary).toList();}

    @Transactional
    public SalnDtos.Response myGet(String employeeNo,Long id){return response(owned(employeeNo,id));}

    @Transactional
    public SalnDtos.Response createDraft(String employeeNo,SalnDtos.DraftRequest request){
        Employee e=employee(employeeNo);Dates dates=dates(request.filingType(),request.salnYear(),request.referenceDate());
        if(salns.existsByEmployeeIdAndFilingTypeAndSalnYearAndAsOfDateAndStatusNot(e.getEmployeeId(),request.filingType(),request.salnYear(),dates.asOf(),SalnTypes.Status.VOIDED))
            throw conflict("An active SALN already exists for this filing type and reference period.");
        Saln s=new Saln();s.setEmployeeId(e.getEmployeeId());s.setEmployeeNo(e.getEmployeeNo());
        s.setDeclarantFamilyName(clean(e.getLastname()));s.setDeclarantFirstName(clean(e.getFirstname()));s.setDeclarantPosition(e.getPosition());
        s.setStatus(SalnTypes.Status.DRAFT);s.setSourceType(SalnTypes.SourceType.ONLINE);s.setVersionNo(0);
        LocalDateTime now=LocalDateTime.now();s.setCreatedAt(now);s.setCreatedBy(employeeNo);s.setUpdatedAt(now);s.setUpdatedBy(employeeNo);
        apply(s,request,dates);s=salns.save(s);audit(s,"SALN_CREATED",employeeNo,null,s.getStatus(),null);
        Long salnId=s.getId();
        requirements.findByEmployeeIdAndFilingTypeAndReferenceDate(e.getEmployeeId(),request.filingType(),request.referenceDate()).ifPresent(r->r.link(salnId,SalnTypes.RequirementStatus.DRAFT));
        return response(s);
    }

    @Transactional
    public SalnDtos.Response updateDraft(String employeeNo,Long id,SalnDtos.DraftRequest request){
        Saln s=ownedForUpdate(employeeNo,id);requireStatus(s,SalnTypes.Status.DRAFT,SalnTypes.Status.FOR_CORRECTION);
        Dates dates=dates(request.filingType(),request.salnYear(),request.referenceDate());
        boolean keyChanged=!Objects.equals(s.getFilingType(),request.filingType())||!Objects.equals(s.getSalnYear(),request.salnYear())||!Objects.equals(s.getAsOfDate(),dates.asOf());
        if(keyChanged&&salns.existsByEmployeeIdAndFilingTypeAndSalnYearAndAsOfDateAndStatusNot(s.getEmployeeId(),request.filingType(),request.salnYear(),dates.asOf(),SalnTypes.Status.VOIDED))
            throw conflict("An active SALN already exists for this filing type and reference period.");
        apply(s,request,dates);s.setUpdatedAt(LocalDateTime.now());s.setUpdatedBy(employeeNo);audit(s,"SALN_UPDATED",employeeNo,s.getStatus(),s.getStatus(),null);return response(s);
    }

    @Transactional
    public void discardDraft(String employeeNo,Long id){Saln s=ownedForUpdate(employeeNo,id);requireStatus(s,SalnTypes.Status.DRAFT);audits.deleteAll(audits.findBySalnIdOrderByPerformedAtDesc(id));salns.delete(s);}

    @Transactional
    public SalnDtos.Response submit(String employeeNo,Long id,boolean resubmit){
        Saln s=ownedForUpdate(employeeNo,id);SalnTypes.Status expected=resubmit?SalnTypes.Status.FOR_CORRECTION:SalnTypes.Status.DRAFT;requireStatus(s,expected);validateForSubmission(s);
        SalnTypes.Status old=s.getStatus(),next=resubmit?SalnTypes.Status.RESUBMITTED:SalnTypes.Status.SUBMITTED;
        s.setVersionNo(s.getVersionNo()+1);s.setStatus(next);s.setSubmissionDate(LocalDate.now());s.setSubmittedAt(LocalDateTime.now());s.setSubmittedBy(employeeNo);s.setUpdatedAt(LocalDateTime.now());s.setUpdatedBy(employeeNo);
        salns.saveAndFlush(s);snapshot(s,employeeNo);if(resubmit)corrections.findBySalnIdAndResolvedFalse(id).forEach(SalnCorrection::resolve);
        audit(s,resubmit?"SALN_RESUBMITTED":"SALN_SUBMITTED",employeeNo,old,next,null);
        requirements.findByEmployeeIdAndFilingTypeAndReferenceDate(s.getEmployeeId(),s.getFilingType(),s.getReferenceDate()).ifPresent(r->r.link(s.getId(),SalnTypes.RequirementStatus.FILED));
        return response(s);
    }

    @Transactional
    public List<SalnDtos.Summary> adminList(Integer year,SalnTypes.FilingType type,SalnTypes.Status status,String employeeSearch){
        String q=employeeSearch==null?"":employeeSearch.trim().toLowerCase(Locale.ROOT);
        return salns.findAll().stream().filter(s->year==null||Objects.equals(year,s.getSalnYear())).filter(s->type==null||type==s.getFilingType())
                .filter(s->status==null||status==s.getStatus()).filter(s->q.isEmpty()||s.getEmployeeNo().toLowerCase(Locale.ROOT).contains(q)||employeeName(s).toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(Saln::getUpdatedAt).reversed()).map(this::summary).toList();
    }

    @Transactional public SalnDtos.Response adminGet(Long id){return response(get(id));}

    @Transactional public SalnDtos.Response startReview(Long id,String actor){Saln s=forUpdate(id);requireStatus(s,SalnTypes.Status.SUBMITTED,SalnTypes.Status.RESUBMITTED);return transition(s,SalnTypes.Status.UNDER_REVIEW,actor,"SALN_REVIEW_STARTED",null);}

    @Transactional public SalnDtos.Response returnForCorrection(Long id,String actor,SalnDtos.CorrectionRequest request){
        Saln s=forUpdate(id);requireStatus(s,SalnTypes.Status.UNDER_REVIEW);request.items().forEach(i->corrections.save(new SalnCorrection(id,s.getVersionNo(),clean(i.section()),clean(i.field()),i.message().trim(),actor)));
        return transition(s,SalnTypes.Status.FOR_CORRECTION,actor,"SALN_RETURNED_FOR_CORRECTION",request.items().get(0).message());
    }

    @Transactional public SalnDtos.Response markCompliant(Long id,String actor,String remarks){Saln s=forUpdate(id);requireStatus(s,SalnTypes.Status.UNDER_REVIEW);s.setCompliantAt(LocalDateTime.now());s.setCompliantBy(actor);return transition(s,SalnTypes.Status.COMPLIANT,actor,"SALN_MARKED_COMPLIANT",remarks);}

    @Transactional public SalnDtos.Response lock(Long id,String actor,String remarks){Saln s=forUpdate(id);requireStatus(s,SalnTypes.Status.COMPLIANT);s.setLockedAt(LocalDateTime.now());s.setLockedBy(actor);return transition(s,SalnTypes.Status.LOCKED,actor,"SALN_LOCKED",remarks);}

    @Transactional public SalnDtos.Response repositorySubmission(Long id,String actor,SalnDtos.RepositoryRequest request){
        Saln s=forUpdate(id);requireStatus(s,SalnTypes.Status.COMPLIANT,SalnTypes.Status.LOCKED);if(s.getRepositorySubmittedAt()!=null)throw conflict("Repository submission has already been recorded.");
        SalnTypes.Status old=s.getStatus();s.setRepositoryAgency(request.repositoryAgency().trim());s.setRepositorySubmittedAt(request.submittedAt());s.setRepositorySubmittedBy(actor);s.setRepositoryReferenceNo(request.referenceNo().trim());s.setRepositoryRemarks(clean(request.remarks()));s.setStatus(SalnTypes.Status.LOCKED);s.setLockedAt(s.getLockedAt()==null?LocalDateTime.now():s.getLockedAt());s.setLockedBy(s.getLockedBy()==null?actor:s.getLockedBy());
        audit(s,"SALN_REPOSITORY_SUBMITTED",actor,old,SalnTypes.Status.LOCKED,request.referenceNo());return response(s);
    }

    @Transactional public SalnDtos.Response voidSaln(Long id,String actor,SalnDtos.VoidRequest request){Saln s=forUpdate(id);if(s.getStatus()==SalnTypes.Status.VOIDED)throw conflict("SALN is already voided.");SalnTypes.Status old=s.getStatus();s.setStatus(SalnTypes.Status.VOIDED);s.setVoidedAt(LocalDateTime.now());s.setVoidedBy(actor);s.setVoidReason(request.reason().trim());s.setReplacementSalnId(request.replacementSalnId());audit(s,"SALN_VOIDED",actor,old,s.getStatus(),request.reason());return response(s);}

    @Transactional public SalnDtos.Response encodeHistorical(String actor,SalnDtos.HistoricalRequest request){
        if(request.sourceType()==SalnTypes.SourceType.ONLINE)throw bad("Historical records must use PAPER, LEGACY_MIGRATION, or ADMIN_ENCODED source type.");
        Employee e=employees.findById(request.employeeId()).orElseThrow(()->notFound("Employee not found."));SalnDtos.DraftRequest d=request.declaration();Dates dates=dates(d.filingType(),d.salnYear(),d.referenceDate());
        if(salns.existsByEmployeeIdAndFilingTypeAndSalnYearAndAsOfDateAndStatusNot(e.getEmployeeId(),d.filingType(),d.salnYear(),dates.asOf(),SalnTypes.Status.VOIDED))throw conflict("An active SALN already exists for this filing period.");
        Saln s=new Saln();s.setEmployeeId(e.getEmployeeId());s.setEmployeeNo(e.getEmployeeNo());s.setDeclarantFamilyName(clean(e.getLastname()));s.setDeclarantFirstName(clean(e.getFirstname()));s.setDeclarantPosition(e.getPosition());s.setSourceType(request.sourceType());s.setStatus(SalnTypes.Status.COMPLIANT);s.setVersionNo(1);s.setSubmissionDate(request.originalFilingDate());s.setSubmittedAt(request.originalFilingDate().atStartOfDay());s.setSubmittedBy(e.getEmployeeNo());s.setCompliantAt(LocalDateTime.now());s.setCompliantBy(actor);s.setSourceDocumentReference(clean(request.sourceDocumentReference()));s.setCreatedAt(LocalDateTime.now());s.setCreatedBy(actor);s.setUpdatedAt(LocalDateTime.now());s.setUpdatedBy(actor);apply(s,d,dates);validateForSubmission(s);s=salns.saveAndFlush(s);snapshot(s,e.getEmployeeNo());audit(s,"HISTORICAL_SALN_ENCODED",actor,null,s.getStatus(),request.sourceDocumentReference());return response(s);
    }

    @Transactional public SalnDtos.RequirementResponse createRequirement(String actor,SalnDtos.RequirementRequest request){Employee e=employees.findById(request.employeeId()).orElseThrow(()->notFound("Employee not found."));Dates dates=dates(request.filingType(),request.filingType()==SalnTypes.FilingType.ANNUAL?request.referenceDate().getYear():request.referenceDate().getYear(),request.referenceDate());SalnFilingRequirement r=new SalnFilingRequirement(e.getEmployeeId(),e.getEmployeeNo(),request.filingType(),request.referenceDate(),dates.asOf().getYear(),dates.due(),actor);try{return requirement(requirements.save(r));}catch(org.springframework.dao.DataIntegrityViolationException ex){throw conflict("This filing requirement already exists.");}}

    @Transactional public SalnDtos.AnnualBulkRequirementResult previewAnnualRequirements(Integer year){
        AnnualRequirementScope scope=annualRequirementScope(year);long existing=scope.eligible().stream().filter(e->requirements.existsByEmployeeIdAndFilingTypeAndReferenceDate(e.getEmployeeId(),SalnTypes.FilingType.ANNUAL,scope.referenceDate())).count();
        return bulkResult(scope,scope.eligible().size()-existing,existing);
    }

    @Transactional public SalnDtos.AnnualBulkRequirementResult generateAnnualRequirements(String actor,Integer year){
        AnnualRequirementScope scope=annualRequirementScope(year);List<SalnFilingRequirement> created=new ArrayList<>();long existing=0;
        for(Employee employee:scope.eligible()){
            if(requirements.existsByEmployeeIdAndFilingTypeAndReferenceDate(employee.getEmployeeId(),SalnTypes.FilingType.ANNUAL,scope.referenceDate())){existing++;continue;}
            SalnFilingRequirement requirement=new SalnFilingRequirement(employee.getEmployeeId(),employee.getEmployeeNo(),SalnTypes.FilingType.ANNUAL,scope.referenceDate(),year,scope.dueDate(),actor);
            salns.findFirstByEmployeeIdAndFilingTypeAndReferenceDateAndStatusNotOrderByUpdatedAtDesc(employee.getEmployeeId(),SalnTypes.FilingType.ANNUAL,scope.referenceDate(),SalnTypes.Status.VOIDED).ifPresent(saln->requirement.link(saln.getId(),saln.getStatus()==SalnTypes.Status.DRAFT?SalnTypes.RequirementStatus.DRAFT:SalnTypes.RequirementStatus.FILED));
            created.add(requirement);
        }
        try{requirements.saveAll(created);requirements.flush();}catch(org.springframework.dao.DataIntegrityViolationException ex){throw conflict("Annual requirements changed during generation. Refresh the preview and try again.");}
        return bulkResult(scope,created.size(),existing);
    }

    @Transactional public List<SalnDtos.RequirementResponse> requirements(){return requirements.findAll().stream().map(this::refreshRequirement).map(this::requirement).toList();}
    @Transactional public List<SalnDtos.RequirementResponse> myRequirements(String employeeNo){Employee e=employee(employeeNo);return requirements.findByEmployeeIdOrderByDueDateDesc(e.getEmployeeId()).stream().map(this::refreshRequirement).map(this::requirement).toList();}
    @Transactional public List<SalnCorrection> myCorrections(String employeeNo,Long id){owned(employeeNo,id);return corrections.findBySalnIdOrderByRequestedAtDesc(id);}
    @Transactional public List<SalnAudit> audit(Long id){get(id);return audits.findBySalnIdOrderByPerformedAtDesc(id);}
    @Transactional public List<SalnVersion> versions(Long id){get(id);return versions.findBySalnIdOrderByVersionNoDesc(id);}
    @Transactional public void recordPrint(Long id,String actor){Saln s=get(id);audit(s,"SALN_PRINTED",actor,s.getStatus(),s.getStatus(),null);}

    @Transactional public SalnDtos.Dashboard dashboard(Integer year,SalnTypes.FilingType type){List<SalnFilingRequirement> rs=requirements.findAll().stream().map(this::refreshRequirement).filter(r->year==null||r.getSalnYear()==year).filter(r->type==null||r.getFilingType()==type).toList();List<Saln> ss=salns.findAll().stream().filter(s->year==null||Objects.equals(year,s.getSalnYear())).filter(s->type==null||s.getFilingType()==type).toList();return new SalnDtos.Dashboard(rs.size(),rs.stream().filter(r->r.getStatus()==SalnTypes.RequirementStatus.FILED).count(),rs.stream().filter(r->r.getStatus()==SalnTypes.RequirementStatus.NOT_FILED).count(),count(ss,SalnTypes.Status.DRAFT),count(ss,SalnTypes.Status.SUBMITTED)+count(ss,SalnTypes.Status.RESUBMITTED),count(ss,SalnTypes.Status.UNDER_REVIEW),count(ss,SalnTypes.Status.FOR_CORRECTION),count(ss,SalnTypes.Status.COMPLIANT)+count(ss,SalnTypes.Status.LOCKED),rs.stream().filter(r->r.getStatus()==SalnTypes.RequirementStatus.OVERDUE).count());}
    @Transactional public String complianceCsv(Integer year,SalnTypes.FilingType type,SalnTypes.Status status,String search){StringBuilder csv=new StringBuilder("Employee No,Employee Name,Filing Type,SALN Year,Due Date,Submission Date,Status,Reviewer,Compliant At\r\n");for(SalnDtos.Summary row:adminList(year,type,status,search)){csv.append(csv(row.employeeNo())).append(',').append(csv(row.employeeName())).append(',').append(row.filingType()).append(',').append(row.salnYear()).append(',').append(row.dueDate()).append(',').append(row.submissionDate()==null?"":row.submissionDate()).append(',').append(row.status()).append(',').append(csv(row.reviewedBy())).append(',').append(row.compliantAt()==null?"":row.compliantAt()).append("\r\n");}return csv.toString();}

    private long count(List<Saln> list,SalnTypes.Status status){return list.stream().filter(s->s.getStatus()==status).count();}
    private AnnualRequirementScope annualRequirementScope(Integer year){if(year==null||year<1900||year>2200)throw bad("A valid SALN year is required.");LocalDate reference=LocalDate.of(year,12,31),due=LocalDate.of(year+1,4,30);List<Employee> eligible=employees.findSalnEligibleEmployees(reference.atTime(23,59,59));return new AnnualRequirementScope(year,reference,due,eligible,Math.max(0,employees.count()-eligible.size()));}
    private SalnDtos.AnnualBulkRequirementResult bulkResult(AnnualRequirementScope scope,long created,long existing){return new SalnDtos.AnnualBulkRequirementResult(scope.year(),scope.referenceDate(),scope.dueDate(),scope.eligible().size(),created,existing,scope.excluded());}
    private record AnnualRequirementScope(int year,LocalDate referenceDate,LocalDate dueDate,List<Employee> eligible,long excluded){}
    private SalnFilingRequirement refreshRequirement(SalnFilingRequirement r){if(r.getLinkedSalnId()==null&&r.getDueDate().isBefore(LocalDate.now())&&r.getStatus()==SalnTypes.RequirementStatus.NOT_FILED)r.link(null,SalnTypes.RequirementStatus.OVERDUE);return r;}
    private SalnDtos.RequirementResponse requirement(SalnFilingRequirement r){return new SalnDtos.RequirementResponse(r.getId(),r.getEmployeeId(),r.getEmployeeNo(),r.getFilingType(),r.getReferenceDate(),r.getSalnYear(),r.getDueDate(),r.getStatus(),r.getLinkedSalnId());}
    private SalnDtos.Response transition(Saln s,SalnTypes.Status next,String actor,String action,String remarks){SalnTypes.Status old=s.getStatus();s.setStatus(next);s.setUpdatedAt(LocalDateTime.now());s.setUpdatedBy(actor);if(next==SalnTypes.Status.UNDER_REVIEW){s.setReviewedAt(LocalDateTime.now());s.setReviewedBy(actor);}audit(s,action,actor,old,next,remarks);return response(s);}
    private void snapshot(Saln s,String actor){try{versions.save(new SalnVersion(s.getId(),s.getVersionNo(),s.getStatus(),objectMapper.writeValueAsString(response(s)),LocalDateTime.now(),actor));}catch(JsonProcessingException e){throw new IllegalStateException("Unable to preserve the submitted SALN version.",e);}}
    private void audit(Saln s,String action,String actor,SalnTypes.Status oldStatus,SalnTypes.Status newStatus,String remarks){audits.save(new SalnAudit(s.getId(),s.getEmployeeId(),action,actor,oldStatus,newStatus,clean(remarks),s.getVersionNo()));}

    private void apply(Saln s,SalnDtos.DraftRequest d,Dates dates){s.setFilingType(d.filingType());s.setSalnYear(d.salnYear());s.setReferenceDate(d.referenceDate());s.setAsOfDate(dates.asOf());s.setDueDate(dates.due());s.setDeclarantMiddleInitial(clean(d.declarantMiddleInitial()));s.setDeclarantPosition(clean(d.declarantPosition())==null?s.getDeclarantPosition():clean(d.declarantPosition()));s.setDeclarantAgencyOffice(d.declarantAgencyOffice().trim());s.setDeclarantOfficeAddress(d.declarantOfficeAddress().trim());s.setSpouseFullName(clean(d.spouseFullName()));s.setSpousePosition(clean(d.spousePosition()));s.setSpouseAgencyOffice(clean(d.spouseAgencyOffice()));s.setSpouseOfficeAddress(clean(d.spouseOfficeAddress()));s.setFilingMode(d.filingMode());s.setMultipleSpouses(clean(d.multipleSpouses()));s.setBusinessInterestsNone(d.businessInterestsNone());s.setRelativesInGovernmentNone(d.relativesInGovernmentNone());s.setCertificationAccepted(d.certificationAccepted());s.setGovernmentIdType(clean(d.governmentIdType()));s.setGovernmentIdNo(clean(d.governmentIdNo()));s.setGovernmentIdDateIssued(d.governmentIdDateIssued());s.setRemarks(clean(d.remarks()));s.setDependents(list(d.dependents()).stream().map(this::dependent).toList());s.setRealProperties(list(d.realProperties()).stream().map(this::real).toList());s.setPersonalProperties(list(d.personalProperties()).stream().map(this::personal).toList());s.setLiabilities(list(d.liabilities()).stream().map(this::liability).toList());s.setBusinessInterests(list(d.businessInterests()).stream().map(this::business).toList());s.setGovernmentRelatives(list(d.governmentRelatives()).stream().map(this::relative).toList());}
    private void validateForSubmission(Saln s){if(!s.isCertificationAccepted())throw bad("The declarant certification must be accepted before submission.");if(blank(s.getDeclarantAgencyOffice())||blank(s.getDeclarantOfficeAddress()))throw bad("Declarant agency/office and office address are required.");if(blank(s.getGovernmentIdType())||blank(s.getGovernmentIdNo())||s.getGovernmentIdDateIssued()==null)throw bad("Government-issued ID details are required.");if(s.isBusinessInterestsNone()&&!s.getBusinessInterests().isEmpty())throw bad("Business interests cannot be listed when 'none' is selected.");if(!s.isBusinessInterestsNone()&&s.getBusinessInterests().isEmpty())throw bad("Declare business interests or explicitly select none.");if(s.isRelativesInGovernmentNone()&&!s.getGovernmentRelatives().isEmpty())throw bad("Government relatives cannot be listed when 'none' is selected.");if(!s.isRelativesInGovernmentNone()&&s.getGovernmentRelatives().isEmpty())throw bad("Declare relatives in government or explicitly select none.");s.getRealProperties().forEach(x->validateOwner(x.ownerType,x.ownerName));s.getPersonalProperties().forEach(x->validateOwner(x.ownerType,x.ownerName));s.getLiabilities().forEach(x->validateOwner(x.ownerType,x.ownerName));s.getBusinessInterests().forEach(x->validateOwner(x.ownerType,x.ownerName));}
    private void validateOwner(SalnTypes.OwnerType type,String name){if(type!=SalnTypes.OwnerType.DECLARANT&&blank(name))throw bad("Owner name is required for spouse or child entries.");}
    private Dates dates(SalnTypes.FilingType type,Integer year,LocalDate reference){if(type==null||year==null||reference==null)throw bad("Filing type, SALN year, and reference date are required.");if(type==SalnTypes.FilingType.ANNUAL){LocalDate asOf=LocalDate.of(year,12,31);if(!reference.equals(asOf))throw bad("Annual SALN reference date must be December 31 of the SALN year.");return new Dates(asOf,LocalDate.of(year+1,4,30));}if(year!=reference.getYear())throw bad("SALN year must match the assumption or separation reference date year.");return new Dates(reference,reference.plusDays(30));}
    private record Dates(LocalDate asOf,LocalDate due){}

    private Employee employee(String no){if(blank(no))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authenticated employee is required.");return employees.findByEmployeeNoIgnoreCase(no).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authenticated employee record was not found."));}
    private Saln owned(String no,Long id){Employee e=employee(no);Saln s=get(id);if(!s.getEmployeeId().equals(e.getEmployeeId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You may only access your own SALN.");return s;}
    private Saln ownedForUpdate(String no,Long id){Employee e=employee(no);Saln s=forUpdate(id);if(!s.getEmployeeId().equals(e.getEmployeeId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You may only access your own SALN.");return s;}
    private Saln get(Long id){return salns.findDetailedById(id).orElseThrow(()->notFound("SALN not found."));}
    private Saln forUpdate(Long id){return salns.findByIdForUpdate(id).orElseThrow(()->notFound("SALN not found."));}
    private void requireStatus(Saln s,SalnTypes.Status... allowed){if(Arrays.stream(allowed).noneMatch(x->x==s.getStatus()))throw conflict("SALN status " + s.getStatus()+" does not allow this action.");}

    private SalnDtos.Response response(Saln s){SalnDtos.Totals t=totals(s);return new SalnDtos.Response(s.getId(),s.getEmployeeId(),s.getEmployeeNo(),s.getFilingType(),s.getSalnYear(),s.getReferenceDate(),s.getAsOfDate(),s.getDueDate(),s.getSubmissionDate(),s.getStatus(),s.getSourceType(),s.getVersionNo(),s.getDeclarantFamilyName(),s.getDeclarantFirstName(),s.getDeclarantMiddleInitial(),s.getDeclarantPosition(),s.getDeclarantAgencyOffice(),s.getDeclarantOfficeAddress(),s.getSpouseFullName(),s.getSpousePosition(),s.getSpouseAgencyOffice(),s.getSpouseOfficeAddress(),s.getFilingMode(),s.getMultipleSpouses(),s.isBusinessInterestsNone(),s.isRelativesInGovernmentNone(),s.isCertificationAccepted(),s.getGovernmentIdType(),s.getGovernmentIdNo(),s.getGovernmentIdDateIssued(),s.getDependents().stream().map(x->new SalnDtos.DependentItem(x.name,x.relationship,x.age)).toList(),s.getRealProperties().stream().map(x->new SalnDtos.RealPropertyItem(x.ownerType,x.ownerName,x.description,x.kind,x.exactLocation,x.assessedValue,x.fairMarketValue,x.acquisitionYear,x.acquisitionMode,x.acquisitionCost)).toList(),s.getPersonalProperties().stream().map(x->new SalnDtos.PersonalPropertyItem(x.ownerType,x.ownerName,x.description,x.acquisitionYear,x.acquisitionCost)).toList(),s.getLiabilities().stream().map(x->new SalnDtos.LiabilityItem(x.ownerType,x.ownerName,x.nature,x.creditorName,x.outstandingBalance)).toList(),s.getBusinessInterests().stream().map(x->new SalnDtos.BusinessInterestItem(x.ownerType,x.ownerName,x.entityName,x.businessAddress,x.nature,x.dateAcquired)).toList(),s.getGovernmentRelatives().stream().map(x->new SalnDtos.GovernmentRelativeItem(x.name,x.relationship,x.position,x.agencyOfficeAddress)).toList(),t,s.getRemarks(),s.getSourceDocumentReference(),s.getRepositoryAgency(),s.getRepositorySubmittedAt(),s.getRepositoryReferenceNo(),s.getRepositoryRemarks(),s.getCreatedAt(),s.getUpdatedAt(),s.getSubmittedAt(),s.getReviewedBy(),s.getReviewedAt(),s.getCompliantBy(),s.getCompliantAt(),s.getLockedAt());}
    private SalnDtos.Summary summary(Saln s){SalnDtos.Totals t=totals(s);return new SalnDtos.Summary(s.getId(),s.getEmployeeId(),s.getEmployeeNo(),employeeName(s),s.getFilingType(),s.getSalnYear(),s.getDueDate(),s.getSubmissionDate(),s.getStatus(),s.getSourceType(),s.getVersionNo(),t.totalAssets(),t.totalLiabilities(),t.netWorth(),s.getReviewedBy(),s.getCompliantAt());}
    private String employeeName(Saln s){return (s.getDeclarantFamilyName()+", "+s.getDeclarantFirstName()).trim();}
    private SalnDtos.Totals totals(Saln s){BigDecimal real=sum(s.getRealProperties().stream().map(x->x.acquisitionCost).toList());BigDecimal personal=sum(s.getPersonalProperties().stream().map(x->x.acquisitionCost).toList());BigDecimal liabilities=sum(s.getLiabilities().stream().map(x->x.outstandingBalance).toList());BigDecimal assets=real.add(personal);return new SalnDtos.Totals(real,personal,assets,liabilities,assets.subtract(liabilities).setScale(2,RoundingMode.HALF_UP));}
    private BigDecimal sum(List<BigDecimal> values){return values.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO.setScale(2),BigDecimal::add).setScale(2,RoundingMode.HALF_UP);}
    private Saln.Dependent dependent(SalnDtos.DependentItem d){Saln.Dependent x=new Saln.Dependent();x.name=d.name().trim();x.relationship=d.relationship().trim();x.age=d.age();return x;}
    private Saln.RealProperty real(SalnDtos.RealPropertyItem d){Saln.RealProperty x=new Saln.RealProperty();x.ownerType=d.ownerType();x.ownerName=clean(d.ownerName());x.description=d.description().trim();x.kind=d.kind().trim();x.exactLocation=d.exactLocation().trim();x.assessedValue=money(d.assessedValue());x.fairMarketValue=money(d.fairMarketValue());x.acquisitionYear=d.acquisitionYear();x.acquisitionMode=clean(d.acquisitionMode());x.acquisitionCost=money(d.acquisitionCost());return x;}
    private Saln.PersonalProperty personal(SalnDtos.PersonalPropertyItem d){Saln.PersonalProperty x=new Saln.PersonalProperty();x.ownerType=d.ownerType();x.ownerName=clean(d.ownerName());x.description=d.description().trim();x.acquisitionYear=d.acquisitionYear();x.acquisitionCost=money(d.acquisitionCost());return x;}
    private Saln.Liability liability(SalnDtos.LiabilityItem d){Saln.Liability x=new Saln.Liability();x.ownerType=d.ownerType();x.ownerName=clean(d.ownerName());x.nature=d.nature().trim();x.creditorName=d.creditorName().trim();x.outstandingBalance=money(d.outstandingBalance());return x;}
    private Saln.BusinessInterest business(SalnDtos.BusinessInterestItem d){Saln.BusinessInterest x=new Saln.BusinessInterest();x.ownerType=d.ownerType();x.ownerName=clean(d.ownerName());x.entityName=d.entityName().trim();x.businessAddress=d.businessAddress().trim();x.nature=d.nature().trim();x.dateAcquired=d.dateAcquired();return x;}
    private Saln.GovernmentRelative relative(SalnDtos.GovernmentRelativeItem d){Saln.GovernmentRelative x=new Saln.GovernmentRelative();x.name=d.name().trim();x.relationship=d.relationship().trim();x.position=d.position().trim();x.agencyOfficeAddress=d.agencyOfficeAddress().trim();return x;}
    private BigDecimal money(BigDecimal v){return v==null?null:v.setScale(2,RoundingMode.HALF_UP);}
    private static <T> List<T> list(List<T> v){return v==null?List.of():v;}
    private static boolean blank(String v){return v==null||v.isBlank();}
    private static String clean(String v){return blank(v)?null:v.trim();}
    private static String csv(Object value){String text=value==null?"":String.valueOf(value);return "\""+text.replace("\"","\"\"")+"\"";}
    private static ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private static ResponseStatusException conflict(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
    private static ResponseStatusException notFound(String m){return new ResponseStatusException(HttpStatus.NOT_FOUND,m);}
}

package com.primehr.assessment.application;

import com.primehr.assessment.api.AssessmentExecutionDtos.*;
import com.primehr.assessment.api.AssessmentValidationDtos.*;
import com.primehr.assessment.domain.*;
import com.primehr.assessment.infrastructure.*;
import com.primehr.competency.domain.*;
import com.primehr.competency.infrastructure.ProficiencyLevelRepository;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.positionprofile.domain.PositionProfileRequirement;
import com.primehr.positionprofile.infrastructure.PositionProfileRequirementRepository;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service @Transactional
public class AssessmentValidationServiceImpl implements AssessmentValidationService {
 private final AssessmentCaseRepository cases; private final AssessorAssignmentRepository assignments;
 private final AssessmentRatingRepository ratings; private final AssessmentEvidenceRepository evidence;
 private final PositionProfileRequirementRepository requirements; private final ProficiencyLevelRepository levels;
 private final AssessmentValidationRepository validations; private final AssessmentValidatedRatingRepository validatedRatings;
 private final PersonCompetencyProfileRepository profiles; private final PersonCompetencyResultRepository results;
 private final PrimeHrAuditService audit;
 public AssessmentValidationServiceImpl(AssessmentCaseRepository cases,AssessorAssignmentRepository assignments,
  AssessmentRatingRepository ratings,AssessmentEvidenceRepository evidence,PositionProfileRequirementRepository requirements,
  ProficiencyLevelRepository levels,AssessmentValidationRepository validations,AssessmentValidatedRatingRepository validatedRatings,
  PersonCompetencyProfileRepository profiles,PersonCompetencyResultRepository results,PrimeHrAuditService audit){
  this.cases=cases;this.assignments=assignments;this.ratings=ratings;this.evidence=evidence;this.requirements=requirements;this.levels=levels;
  this.validations=validations;this.validatedRatings=validatedRatings;this.profiles=profiles;this.results=results;this.audit=audit;
 }
 @Override @Transactional(readOnly=true) public PageResponse<ValidationListItem> pending(String agency,int page,int size){page(page,size);return PageResponse.from(cases.findByAgencyIdAndStatus(agency,AssessmentCaseStatus.FOR_VALIDATION.name(),PageRequest.of(page,size,Sort.by("forValidationAt").ascending())),this::listItem);}
 @Override @Transactional(readOnly=true) public ValidationCaseResponse get(String agency,String caseId){return detail(caseEntity(agency,caseId));}
 @Override public ValidationResultResponse validate(String agency,String caseId,ValidateCaseRequest request,String actor,boolean administrator,String correlation){
  String normalizedActor=actor(actor);
  if(request==null||request.validFrom()==null)throw new IllegalArgumentException("Validation request and validFrom are required");
  AssessmentCase item=caseEntity(agency,caseId); version(item.getVersion(),request.caseRecordVersion());
  if(item.getStatus()!=AssessmentCaseStatus.FOR_VALIDATION)throw new IllegalLifecycleTransitionException("Only a FOR_VALIDATION case may be validated");
  if(validations.existsByAssessmentCaseId(caseId)||profiles.existsByAssessmentCaseId(caseId))throw new OptimisticConflictException("This assessment case is already validated");
  List<AssessorAssignment> contribution=assignments.findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(caseId);
  if(contribution.isEmpty()||contribution.stream().anyMatch(a->a.getStatus()!=AssessorAssignmentStatus.SUBMITTED))throw new IllegalLifecycleTransitionException("Every active contribution must be submitted");
  boolean ownContribution=contribution.stream().anyMatch(a->a.getAssessorEmployeeNo().equalsIgnoreCase(normalizedActor));
  if(request.administratorOverride()&&!administrator)throw new AccessDeniedException("Only an administrator may request a validation override");
  if(ownContribution&&(!administrator||!request.administratorOverride()))throw new AccessDeniedException("A validator cannot validate their own contribution");
  if(request.administratorOverride()&&(request.overrideReason()==null||request.overrideReason().isBlank()))throw new IllegalArgumentException("Administrator override reason is required");
  requireExpectedContributions(contribution,request.expectedContributions());
  List<PositionProfileRequirement> required=requirements.findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(item.getTool().getPositionProfile().getId(),agency);
  Map<String,FinalDecision> decisions=uniqueDecisions(request.decisions());
  Set<String> requiredIds=required.stream().map(r->r.getCompetency().getId()).collect(Collectors.toCollection(LinkedHashSet::new));
  if(!decisions.keySet().equals(requiredIds))throw new IllegalArgumentException("Exactly one final decision is required for every profile competency");
  Map<String,AssessorAssignment> contributionById=contribution.stream().collect(Collectors.toMap(AssessorAssignment::getId,Function.identity()));
  Map<String,List<AssessmentRating>> ratingByCompetency=ratings.findActiveForCase(caseId).stream()
    .collect(Collectors.groupingBy(r->r.getCompetency().getId()));
  for(String competencyId:requiredIds){List<AssessmentRating> source=ratingByCompetency.getOrDefault(competencyId,List.of());if(source.size()!=contribution.size())throw new IllegalArgumentException("Every submitted contribution must rate every competency");validateContributors(decisions.get(competencyId),source,contributionById);}
  PersonCompetencyProfile predecessor=profiles.findFirstByAgencyIdAndSubjectEmployeeNoIgnoreCaseOrderByProfileVersionDescValidatedAtDesc(agency,item.getSubjectEmployeeNo()).orElse(null);
  if(predecessor!=null&&!request.validFrom().isAfter(predecessor.getValidFrom()))throw new IllegalArgumentException("A successor validFrom must be after the latest profile validFrom");
  AssessmentValidation validation=validations.saveAndFlush(new AssessmentValidation(agency,item,normalizedActor,request.administratorOverride(),request.validationRemarks(),request.overrideReason(),Instant.now()));
  List<AssessmentValidatedRating> official=new ArrayList<>();
  for(PositionProfileRequirement requirement:required){FinalDecision decision=decisions.get(requirement.getCompetency().getId());ProficiencyLevel level=levels.findByIdAndAgencyId(decision.finalLevelId(),agency).orElseThrow(()->new ResourceNotFoundException("Final proficiency level not found"));String ids=decision.contributingAssignmentIds().stream().distinct().sorted().collect(Collectors.joining(","));official.add(new AssessmentValidatedRating(agency,validation,requirement.getCompetency(),level,decision.remarks(),ids));}
  official=validatedRatings.saveAllAndFlush(official);
  if(predecessor!=null){predecessor.closeBefore(request.validFrom());profiles.saveAndFlush(predecessor);}
  String methods=contribution.stream().map(a->a.getMethod().name()).distinct().sorted().collect(Collectors.joining(","));
  PersonCompetencyProfile profile=profiles.saveAndFlush(new PersonCompetencyProfile(agency,item,validation,predecessor,predecessor==null?1:predecessor.getProfileVersion()+1,request.validFrom(),request.reassessmentDate(),methods));
  List<PersonCompetencyResult> profileResults=official.stream().map(r->new PersonCompetencyResult(agency,profile,r)).toList();results.saveAll(profileResults);results.flush();
  contribution.forEach(AssessorAssignment::markValidated);assignments.saveAll(contribution);assignments.flush();item.validate();item=cases.saveAndFlush(item);
  ValidationResultResponse response=result(item,validation,profile,official);audit.record(agency,"VALIDATE_ASSESSMENT","ASSESSMENT_CASE",caseId,profile.getProfileVersion(),item.getVersion(),null,response,request.administratorOverride()?request.overrideReason():request.validationRemarks(),correlation);
  return response;
 }
 @Override @Transactional(readOnly=true) public PersonProfileResponse latest(String agency,String employeeNo,LocalDate asOf,String actor,PermissionDataScope scope){authorize(employeeNo,actor,scope);List<PersonCompetencyProfile> found=profiles.findEffective(agency,employeeNo,asOf==null?LocalDate.now():asOf,PageRequest.of(0,1));if(found.isEmpty())throw new ResourceNotFoundException("No valid person competency profile was found");return profile(found.get(0));}
 @Override @Transactional(readOnly=true) public PageResponse<PersonProfileResponse> history(String agency,String employeeNo,int page,int size,String actor,PermissionDataScope scope){authorize(employeeNo,actor,scope);page(page,size);return PageResponse.from(profiles.findByAgencyIdAndSubjectEmployeeNoIgnoreCase(agency,employeeNo,PageRequest.of(page,size,Sort.by(Sort.Order.desc("profileVersion"),Sort.Order.desc("validatedAt")))),this::profile);}
 @Override @Transactional(readOnly=true) public PersonProfileResponse version(String agency,String profileId,String actor,PermissionDataScope scope){PersonCompetencyProfile item=profiles.findByAgencyIdAndId(agency,profileId).orElseThrow(()->new ResourceNotFoundException("Person competency profile not found"));authorize(item.getSubjectEmployeeNo(),actor,scope);return profile(item);}
 private void requireExpectedContributions(List<AssessorAssignment> current,List<ExpectedContribution> expected){Map<String,Long> supplied=expected.stream().collect(Collectors.toMap(ExpectedContribution::assignmentId,ExpectedContribution::recordVersion,(a,b)->{throw new IllegalArgumentException("Duplicate expected contribution");}));if(supplied.size()!=current.size())throw new OptimisticConflictException("Expected contribution versions do not match the case");for(AssessorAssignment a:current)version(a.getVersion(),supplied.get(a.getId()));}
 private static Map<String,FinalDecision> uniqueDecisions(List<FinalDecision> decisions){return decisions.stream().collect(Collectors.toMap(FinalDecision::competencyVersionId,Function.identity(),(a,b)->{throw new IllegalArgumentException("Duplicate final competency decision");},LinkedHashMap::new));}
 private static void validateContributors(FinalDecision decision,List<AssessmentRating> source,Map<String,AssessorAssignment> assignments){Set<String> expected=source.stream().map(r->r.getAssignment().getId()).collect(Collectors.toSet());Set<String> supplied=new HashSet<>(decision.contributingAssignmentIds());if(supplied.size()!=decision.contributingAssignmentIds().size()||!supplied.equals(expected)||!assignments.keySet().containsAll(supplied))throw new IllegalArgumentException("Contributing assignment IDs must exactly match submitted competency ratings");}
 private ValidationCaseResponse detail(AssessmentCase item){if(item.getStatus()!=AssessmentCaseStatus.FOR_VALIDATION)throw new IllegalLifecycleTransitionException("Assessment case is not awaiting validation");List<PositionProfileRequirement> req=requirements.findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(item.getTool().getPositionProfile().getId(),item.getAgencyId());return new ValidationCaseResponse(item.getId(),item.getStatus(),item.getSubjectEmployeeNo(),item.getSubjectDisplayName(),item.getVersion(),req.stream().map(this::requirement).toList(),assignments.findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(item.getId()).stream().map(this::contribution).toList());}
 private RequirementResponse requirement(PositionProfileRequirement r){Competency c=r.getCompetency();ProficiencyLevel l=r.getRequiredProficiencyLevel();return new RequirementResponse(c.getId(),c.getCode(),c.getName(),c.getDefinitionVersion(),l.getId(),l.getCode(),r.getClassification().name(),r.getCriticalityCode(),levels.findByScaleIdAndActiveTrueOrderByLevelOrderAsc(c.getProficiencyScale().getId()).stream().map(x->new LevelOptionResponse(x.getId(),x.getCode(),x.getLabel(),x.getLevelOrder())).toList());}
 private ContributionResponse contribution(AssessorAssignment a){return new ContributionResponse(a.getId(),a.getMethod(),a.getAssessorEmployeeNo(),a.getAssessorDisplayName(),a.getStatus(),a.getVersion(),a.getSubmittedBy(),a.getSubmittedAt(),ratings.findByAssignmentIdAndActiveTrueOrderByCompetencyCode(a.getId()).stream().map(this::rating).toList());}
 private RatingResponse rating(AssessmentRating r){return new RatingResponse(r.getId(),r.getCompetency().getId(),r.getAttainedLevel().getId(),r.getAttainedLevel().getCode(),r.getRemarks(),r.getBehavioralNotes(),r.getVersion(),evidence.findByRatingIdAndActiveTrueOrderByEvidenceDateDescIdAsc(r.getId()).stream().map(e->new EvidenceResponse(e.getId(),e.getEvidenceType(),e.getTitleReference(),e.getEvidenceDate(),e.getDescription(),e.getSourceSystem(),e.getSourceReference(),e.getVersion())).toList());}
 private ValidationListItem listItem(AssessmentCase c){return new ValidationListItem(c.getId(),c.getTool().getCycle().getName(),c.getTool().getName(),c.getSubjectEmployeeNo(),c.getSubjectDisplayName(),c.getForValidationAt(),c.getVersion());}
 private ValidationResultResponse result(AssessmentCase c,AssessmentValidation v,PersonCompetencyProfile p,List<AssessmentValidatedRating> official){return new ValidationResultResponse(v.getId(),c.getId(),p.getId(),p.getProfileVersion(),c.getStatus(),v.getValidatedAt(),c.getVersion(),official.stream().map(r->new ValidatedDecisionResponse(r.getCompetency().getId(),r.getCompetency().getCode(),r.getFinalLevel().getId(),r.getFinalLevel().getCode(),r.getValidationRemarks(),List.of(r.getContributingAssignmentIds().split(",")))).toList());}
 private PersonProfileResponse profile(PersonCompetencyProfile p){return new PersonProfileResponse(p.getId(),p.getSubjectEmployeeNo(),p.getSubjectDisplayName(),p.getProfileVersion(),p.getValidFrom(),p.getValidTo(),p.getReassessmentDate(),p.getStatus(),p.getValidatedAt(),p.getPredecessor()==null?null:p.getPredecessor().getId(),p.getCycleId(),p.getToolId(),p.getPositionProfileId(),p.getPositionProfileDefinitionVersion(),p.getPositionProfileContentRevision(),results.findByPersonProfileIdOrderByCompetencyCode(p.getId()).stream().map(r->new PersonResultResponse(r.getCompetency().getId(),r.getCompetency().getCode(),r.getCompetency().getName(),r.getAttainedLevel().getId(),r.getAttainedLevel().getCode(),r.getAttainedLevel().getLabel(),r.getValidatedRating().getValidationRemarks())).toList());}
 private AssessmentCase caseEntity(String agency,String id){return cases.findByAgencyIdAndId(agency,id).orElseThrow(()->new ResourceNotFoundException("Assessment case not found"));}
 private static void authorize(String employeeNo,String actor,PermissionDataScope scope){actor=actor(actor);if(employeeNo.equalsIgnoreCase(actor))return;if(scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("The configured data scope does not allow this person profile");}
 private static String actor(String a){if(a==null||a.isBlank())throw new AccessDeniedException("Authenticated employee identity is required");return a.trim();}
 private static void version(long actual,Long expected){if(expected==null||actual!=expected)throw new OptimisticConflictException("Expected recordVersion "+expected+" but current version is "+actual);}
 private static void page(int page,int size){if(page<0||size<1||size>100)throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");}
}

package com.primehr.rsp.report;

import com.primehr.rsp.applicant.domain.PositionApplication;
import com.primehr.rsp.applicant.infrastructure.PositionApplicationRepository;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.rsp.evaluation.domain.EvaluationProceeding;
import com.primehr.rsp.evaluation.infrastructure.EvaluationCandidateRepository;
import com.primehr.rsp.evaluation.infrastructure.EvaluationProceedingRepository;
import com.primehr.rsp.handoff.domain.AppointmentHandoff;
import com.primehr.rsp.handoff.domain.AppointmentHandoffAttempt;
import com.primehr.rsp.handoff.infrastructure.AppointmentHandoffAttemptRepository;
import com.primehr.rsp.handoff.infrastructure.AppointmentHandoffRepository;
import com.primehr.rsp.infrastructure.VacancyPublicationRepository;
import com.primehr.rsp.infrastructure.VacancyPublicationChannelRepository;
import com.primehr.rsp.domain.VacancyPublicationChannel;
import com.primehr.integration.administrative.AdministrativeOrganizationScopeClient;
import com.primehr.rsp.report.RspProcessReportData.*;
import com.primehr.rsp.screening.domain.ScreeningCase;
import com.primehr.rsp.screening.infrastructure.ScreeningCaseRepository;
import com.primehr.rsp.screening.infrastructure.ScreeningDecisionRepository;
import com.primehr.rsp.screening.domain.ScreeningDecision;
import com.primehr.rsp.selection.domain.OfferResponse;
import com.primehr.rsp.selection.domain.SelectionCase;
import com.primehr.rsp.selection.infrastructure.OfferResponseRepository;
import com.primehr.rsp.selection.infrastructure.SelectionCaseRepository;
import com.primehr.shared.audit.PrimeHrAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RspProcessReportServiceImpl implements RspProcessReportService {
    static final String TIMEZONE="Asia/Manila";
    static final String TEMPLATE_VERSION="PHASE-5F.3-v1";
    private static final int MAX_RANGE_DAYS=366;
    private static final int MAX_SCAN=10_000;
    private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").withZone(ZoneId.of(TIMEZONE));
    private final VacancyPublicationRepository publications; private final PositionApplicationRepository applications;
    private final VacancyPublicationChannelRepository channels; private final ScreeningDecisionRepository decisions;
    private final ScreeningCaseRepository screenings; private final EvaluationProceedingRepository proceedings;
    private final EvaluationCandidateRepository candidates; private final SelectionCaseRepository selections;
    private final OfferResponseRepository offers; private final AppointmentHandoffRepository handoffs;
    private final AppointmentHandoffAttemptRepository attempts; private final RspProcessReportRenderer renderer;
    private final PrimeHrAuditService audit;
    private final AdministrativeOrganizationScopeClient organizations;

    public RspProcessReportServiceImpl(VacancyPublicationRepository publications,VacancyPublicationChannelRepository channels,
            PositionApplicationRepository applications,ScreeningCaseRepository screenings,ScreeningDecisionRepository decisions,EvaluationProceedingRepository proceedings,
            EvaluationCandidateRepository candidates,SelectionCaseRepository selections,OfferResponseRepository offers,
            AppointmentHandoffRepository handoffs,AppointmentHandoffAttemptRepository attempts,
            RspProcessReportRenderer renderer,PrimeHrAuditService audit,AdministrativeOrganizationScopeClient organizations){this.publications=publications;this.channels=channels;this.applications=applications;
        this.screenings=screenings;this.decisions=decisions;this.proceedings=proceedings;this.candidates=candidates;this.selections=selections;
        this.offers=offers;this.handoffs=handoffs;this.attempts=attempts;this.renderer=renderer;this.audit=audit;
        this.organizations=organizations;}

    @Override public RegisterPage register(String agency,RegisterQuery query,String actor,String authorization,String correlationId){
        validate(query);List<Snapshot> matching=matchingSnapshots(agency,query,authorization);
        int start=Math.min(query.page()*query.size(),matching.size());int end=Math.min(start+query.size(),matching.size());
        List<RegisterRow> rows=matching.subList(start,end).stream().flatMap(s->rows(agency,s,query).stream()).toList();
        int pages=(int)Math.ceil(matching.size()/(double)query.size());
        RegisterPage result=new RegisterPage(agency,query.from(),query.to(),query.dateBasis().name(),query.includeHistory(),
                filterSummary(query),TIMEZONE,query.page(),query.size(),matching.size(),pages,rows);
        audit(agency,"VIEW_RSP_REGISTER_REPORT","RSP_REGISTER",actor,query,rows.size(),null,correlationId);
        return result;
    }
    @Override public byte[] registerPdf(String agency,RegisterQuery query,String actor,String authorization,String correlationId){
        RegisterPage data=register(agency,query,actor,authorization,correlationId);byte[] pdf=renderer.register(data,generated(actor));
        audit(agency,"GENERATE_RSP_REGISTER_REPORT","RSP_REGISTER_PDF",actor,query,data.rows().size(),pdf,correlationId);return pdf;
    }
    @Override public Analytics analytics(String agency,RegisterQuery query,String actor,String authorization,String correlationId){
        validate(query);List<Snapshot> snapshots=matchingSnapshots(agency,query,authorization);
        long submitted=snapshots.stream().flatMap(s->s.applications.stream()).filter(a->a.getStatus()!=PositionApplication.Status.DRAFT).count();
        long withdrawn=snapshots.stream().flatMap(s->s.applications.stream()).filter(a->a.getStatus()==PositionApplication.Status.WITHDRAWN).count();
        List<ScreeningCase> currentScreens=snapshots.stream().flatMap(s->s.currentScreenings.stream()).toList();
        long screeningFinal=currentScreens.stream().filter(c->c.getStatus()==ScreeningCase.Status.QUALIFIED||c.getStatus()==ScreeningCase.Status.DISQUALIFIED).count();
        long qualified=currentScreens.stream().filter(c->c.getStatus()==ScreeningCase.Status.QUALIFIED).count();
        long corrected=snapshots.stream().mapToLong(s->s.screeningHistory.values().stream().filter(v->v.size()>1).count()).sum();
        long admitted=snapshots.stream().filter(s->s.proceeding!=null).mapToLong(s->candidates.countByAgencyIdAndProceedingId(agency,s.proceeding.getId())).sum();
        long evaluated=snapshots.stream().filter(s->s.proceeding!=null&&s.proceeding.getStatus()==EvaluationProceeding.Status.FINALIZED).mapToLong(s->candidates.countByAgencyIdAndProceedingId(agency,s.proceeding.getId())).sum();
        List<SelectionCase> currentSelections=snapshots.stream().map(s->s.selection).filter(Objects::nonNull).toList();
        long finalized=currentSelections.stream().filter(s->s.getStatus()==SelectionCase.Status.FINALIZED||s.getStatus()==SelectionCase.Status.SUPERSEDED).count();
        long selected=currentSelections.stream().filter(s->s.getOutcome()==SelectionCase.Outcome.SELECTED).count();
        long noSelection=currentSelections.stream().filter(s->s.getOutcome()==SelectionCase.Outcome.NO_SELECTION).count();
        long deferred=currentSelections.stream().filter(s->s.getOutcome()==SelectionCase.Outcome.DEFERRED).count();
        List<OfferResponse> currentOffers=currentSelections.stream().map(s->offers.findByAgencyIdAndSelectionCaseId(agency,s.getId()).orElse(null)).filter(Objects::nonNull).toList();
        long accepted=currentOffers.stream().filter(o->o.getStatus()==OfferResponse.Status.ACCEPTED).count();
        long declined=currentOffers.stream().filter(o->o.getStatus()==OfferResponse.Status.DECLINED).count();
        long expired=currentOffers.stream().filter(o->o.getStatus()==OfferResponse.Status.EXPIRED).count();
        long pending=currentOffers.stream().filter(o->o.getStatus()==OfferResponse.Status.PENDING).count();
        List<AppointmentHandoff> currentHandoffs=currentSelections.stream().map(s->latestHandoff(agency,s.getId())).filter(Objects::nonNull).toList();
        long acknowledged=currentHandoffs.stream().filter(h->h.getStatus()==AppointmentHandoff.Status.ACKNOWLEDGED||h.getStatus()==AppointmentHandoff.Status.CLOSED).count();
        long failed=currentHandoffs.stream().filter(h->h.getStatus()==AppointmentHandoff.Status.RETRYABLE_FAILURE).count();
        List<BigDecimal> publicationToClose=new ArrayList<>(),closeToScreening=new ArrayList<>(),screeningToProceeding=new ArrayList<>(),proceedingToSelection=new ArrayList<>(),selectionToOffer=new ArrayList<>(),offerToHandoff=new ArrayList<>(),applicationToScreening=new ArrayList<>();
        for(Snapshot s:snapshots){Map<String,PositionApplication> appById=s.applications.stream().collect(Collectors.toMap(a->a.getId(),Function.identity()));
            addDuration(publicationToClose,publicationAt(agency,s.publication),s.publication.getClosedAt());
            Instant latestScreen=s.currentScreenings.stream().map(ScreeningCase::getFinalizedAt).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
            for(ScreeningCase c:s.currentScreenings){PositionApplication a=appById.get(c.getApplication().getId());if(a!=null)addDuration(applicationToScreening,a.getSubmittedAt(),c.getFinalizedAt());addDuration(closeToScreening,s.publication.getClosedAt(),c.getFinalizedAt());}
            Instant proceedingFinal=finalizedAt(s.proceeding);addDuration(screeningToProceeding,latestScreen,proceedingFinal);
            if(s.selection!=null){addDuration(proceedingToSelection,proceedingFinal,s.selection.getFinalizedAt());OfferResponse offer=offers.findByAgencyIdAndSelectionCaseId(agency,s.selection.getId()).orElse(null);if(offer!=null){addDuration(selectionToOffer,s.selection.getFinalizedAt(),offer.getRespondedAt());AppointmentHandoff handoff=latestHandoff(agency,s.selection.getId());if(offer.getStatus()==OfferResponse.Status.ACCEPTED&&handoff!=null)addDuration(offerToHandoff,offer.getRespondedAt(),handoff.getAcknowledgedAt());}}}
        List<Metric> metrics=new ArrayList<>();
        metrics.add(count("VACANCIES","Vacancies in cohort",snapshots.size(),"Publications matching the exact event-date and filter cohort."));
        for(var status:com.primehr.rsp.domain.VacancyPublicationStatus.values())metrics.add(count("VACANCY_STATUS_"+status,"Vacancy status: "+status,snapshots.stream().filter(s->s.publication.getStatus()==status).count(),"Matching publications in "+status+" status."));
        Map<String,Set<String>> channelPublications=new TreeMap<>();for(Snapshot snapshot:snapshots)for(VacancyPublicationChannel channel:channels.findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(snapshot.publication.getId(),agency))if(channel.isActive())channelPublications.computeIfAbsent(channel.getChannelName(),ignored->new LinkedHashSet<>()).add(snapshot.publication.getId());
        channelPublications.forEach((name,ids)->metrics.add(count("PUBLICATION_CHANNEL_"+metricCode(name),"Publication channel: "+name,ids.size(),"Distinct matching publications using the active channel.")));
        metrics.add(count("APPLICATIONS_SUBMITTED","Current application records",submitted,"Latest application version per applicant/publication in the exact filtered publication cohort."));
        metrics.add(count("APPLICATIONS_WITHDRAWN","Withdrawn applications",withdrawn,"Current applications whose terminal status is WITHDRAWN."));
        metrics.add(ratio("SCREENING_FINALIZED","Screening completion rate",screeningFinal,submitted,"Current QUALIFIED or DISQUALIFIED screening cases / current applications."));
        metrics.add(count("APPLICATIONS_SCREENED","Screened applications",screeningFinal,"Current applications with a finalized QUALIFIED or DISQUALIFIED screening case."));
        metrics.add(ratio("SCREENING_QUALIFIED","Qualification rate",qualified,screeningFinal,"Current QUALIFIED screening cases / finalized screening cases."));
        metrics.add(count("SCREENING_DISQUALIFIED","Disqualified screening cases",screeningFinal-qualified,"Current DISQUALIFIED screening cases."));
        Map<String,Long> reasonCounts=currentScreens.stream().filter(c->c.getStatus()==ScreeningCase.Status.DISQUALIFIED).map(c->decisions.findByAgencyIdAndCaseId(agency,c.getId()).orElse(null)).filter(Objects::nonNull).map(ScreeningDecision::getReasonCode).map(v->v==null?"UNSPECIFIED":v).collect(Collectors.groupingBy(Function.identity(),TreeMap::new,Collectors.counting()));
        reasonCounts.forEach((reason,value)->metrics.add(count("DISQUALIFICATION_REASON_"+metricCode(reason),"Disqualification reason: "+reason,value,"Current disqualified screening decisions by controlled reason code.")));
        metrics.add(count("SCREENING_CORRECTED_APPLICATIONS","Applications with screening corrections",corrected,"Current applications having more than one preserved screening-case revision."));
        metrics.add(ratio("CANDIDATES_ADMITTED","Evaluation admission rate",admitted,qualified,"Candidates admitted to the latest proceeding / qualified screening cases."));
        metrics.add(count("CANDIDATES_EVALUATED","Evaluated candidates",evaluated,"Candidates in the latest FINALIZED evaluation proceeding for each matching publication."));
        metrics.add(ratio("SELECTIONS_FINALIZED","Selection finalization rate",finalized,snapshots.size(),"Latest FINALIZED or SUPERSEDED selection cases / publications."));
        metrics.add(count("OUTCOME_SELECTED","Selected outcomes",selected,"Latest selection revision with SELECTED outcome."));
        metrics.add(count("OUTCOME_NO_SELECTION","No-selection outcomes",noSelection,"Latest selection revision with NO_SELECTION outcome."));
        metrics.add(count("OUTCOME_DEFERRED","Deferred outcomes",deferred,"Latest selection revision with DEFERRED outcome."));
        metrics.add(count("OFFER_ACCEPTED","Accepted offers",accepted,"Offer tied to the latest selection revision with ACCEPTED status."));
        metrics.add(count("OFFER_DECLINED","Declined offers",declined,"Offer tied to the latest selection revision with DECLINED status."));
        metrics.add(count("OFFER_EXPIRED","Expired offers",expired,"Offer tied to the latest selection revision with EXPIRED status."));
        metrics.add(count("OFFER_PENDING","Pending offers",pending,"Offer tied to the latest selection revision with PENDING status."));
        metrics.add(count("HANDOFF_ACKNOWLEDGED","Acknowledged/closed handoffs",acknowledged,"Latest handoff revision in ACKNOWLEDGED or CLOSED status."));
        metrics.add(count("HANDOFF_RETRYABLE_FAILURE","Failed current handoffs",failed,"Latest handoff revision in RETRYABLE_FAILURE status; attempt detail remains auditable."));
        for(AppointmentHandoff.Status status:AppointmentHandoff.Status.values())metrics.add(count("HANDOFF_STATUS_"+status,"Handoff status: "+status,currentHandoffs.stream().filter(h->h.getStatus()==status).count(),"Latest handoff revision in "+status+" status; status measures reconcile to latest handoffs."));
        metrics.add(average("AVG_PUBLICATION_TO_CLOSE_DAYS","Average publication-to-close days",publicationToClose,"Publication publishedAt to closedAt; null/incomplete/negative pairs excluded."));
        metrics.add(average("AVG_CLOSE_TO_SCREENING_DAYS","Average close-to-screening-final days",closeToScreening,"Publication closedAt to each current screening finalizedAt; null/incomplete/negative pairs excluded."));
        metrics.add(average("AVG_SCREENING_TO_PROCEEDING_DAYS","Average screening-to-proceeding-final days",screeningToProceeding,"Latest current screening finalizedAt to finalized proceeding updatedAt; null/incomplete/negative pairs excluded."));
        metrics.add(average("AVG_PROCEEDING_TO_SELECTION_DAYS","Average proceeding-to-selection-final days",proceedingToSelection,"Finalized proceeding updatedAt to latest selection finalizedAt; null/incomplete/negative pairs excluded."));
        metrics.add(average("AVG_SELECTION_TO_OFFER_DAYS","Average selection-to-offer-response days",selectionToOffer,"Selection finalizedAt to offer respondedAt; pending/null/negative pairs excluded."));
        metrics.add(average("AVG_ACCEPTED_OFFER_TO_HANDOFF_DAYS","Average accepted-offer-to-handoff days",offerToHandoff,"Accepted offer respondedAt to handoff acknowledgedAt; null/incomplete/negative pairs excluded."));
        metrics.add(average("AVG_APPLICATION_TO_SCREENING_DAYS","Average application-to-screening days",applicationToScreening,"SubmittedAt to finalizedAt; null/incomplete/negative pairs excluded; elapsed 24-hour days."));
        if(query.includeHistory()){long selectionHistory=snapshots.stream().mapToLong(s->Math.max(0,s.selectionHistory.size()-1)).sum();long screeningHistory=snapshots.stream().mapToLong(s->s.screeningHistory.values().stream().mapToLong(v->Math.max(0,v.size()-1)).sum()).sum();metrics.add(count("SUPERSEDED_SELECTION_REVISIONS","Superseded selection revisions",selectionHistory,"Historical selection revisions excluded from current operational totals."));metrics.add(count("SUPERSEDED_SCREENING_REVISIONS","Superseded screening revisions",screeningHistory,"Historical screening revisions excluded from current operational totals."));}
        Analytics result=new Analytics(agency,query.from(),query.to(),query.dateBasis().name(),TIMEZONE,query.includeHistory(),filterSummary(query),List.copyOf(metrics));
        audit(agency,"VIEW_RSP_PROCESS_ANALYTICS","RSP_PROCESS_ANALYTICS",actor,query,metrics.size(),null,correlationId);return result;
    }
    @Override public byte[] analyticsPdf(String agency,RegisterQuery query,String actor,String authorization,String correlationId){
        Analytics data=analytics(agency,query,actor,authorization,correlationId);byte[] pdf=renderer.analytics(data,generated(actor));
        audit(agency,"GENERATE_RSP_PROCESS_ANALYTICS","RSP_PROCESS_ANALYTICS_PDF",actor,query,data.metrics().size(),pdf,correlationId);return pdf;}

    private List<RegisterRow> rows(String agency,Snapshot s,RegisterQuery query){
        List<SelectionCase> selected=query.includeHistory()?s.selectionHistory:(s.selection==null?List.of():List.of(s.selection));
        selected=selected.stream().filter(value->selectionMatches(value,query)).toList();
        if(selected.isEmpty())return List.of(row(agency,s,null));return selected.stream().map(v->row(agency,s,v)).toList();}
    private RegisterRow row(String agency,Snapshot s,SelectionCase selection){long withdrawn=s.applications.stream().filter(a->a.getStatus()==PositionApplication.Status.WITHDRAWN).count();
        long qualified=s.currentScreenings.stream().filter(c->c.getStatus()==ScreeningCase.Status.QUALIFIED).count();long disqualified=s.currentScreenings.stream().filter(c->c.getStatus()==ScreeningCase.Status.DISQUALIFIED).count();
        long corrected=s.screeningHistory.values().stream().filter(v->v.size()>1).count();OfferResponse offer=selection==null?null:offers.findByAgencyIdAndSelectionCaseId(agency,selection.getId()).orElse(null);
        AppointmentHandoff handoff=selection==null?null:latestHandoff(agency,selection.getId());int failures=handoff==null?0:(int)attempts.findByAgencyIdAndHandoffIdOrderByAttemptNumberAsc(agency,handoff.getId()).stream().filter(a->isFailure(a)).count();
        return new RegisterRow(s.publication.getId(),vacancy(s.publication),s.publication.getBusinessUnitName(),s.publication.getClosingDate().toString(),s.publication.getStatus().name(),
                s.applications.size(),(int)withdrawn,(int)qualified,(int)disqualified,(int)corrected,s.proceeding==null?"NOT_STARTED":s.proceeding.getStatus().name(),
                selection==null?"N/A":selection.getId(),selection==null?"N/A":"v"+selection.getCaseRevision(),selection==null?"NOT_STARTED":selection.getStatus().name(),
                selection==null||selection.getOutcome()==null?"N/A":selection.getOutcome().name(),offer==null?"N/A":offer.getStatus().name(),handoff==null?"N/A":handoff.getStatus().name(),
                handoff==null||handoff.getReceiptState()==null?"N/A":handoff.getReceiptState(),failures,selection==null?"N/A":time(selection.getFinalizedAt()));}
    private Snapshot snapshot(String agency,VacancyPublication p,boolean history){List<PositionApplication> allApps=applications.findByAgencyIdAndPublicationIdOrderByCreatedAtAsc(agency,p.getId());
        Map<String,PositionApplication> latestApps=new LinkedHashMap<>();for(PositionApplication a:allApps)latestApps.merge(a.getApplicantId(),a,(x,y)->x.getApplicationVersion()>=y.getApplicationVersion()?x:y);
        List<ScreeningCase> allScreens=screenings.findByAgencyIdAndPublicationIdOrderByOpenedAtAsc(agency,p.getId());Map<String,List<ScreeningCase>> byApp=allScreens.stream().collect(Collectors.groupingBy(c->c.getApplication().getId(),LinkedHashMap::new,Collectors.toList()));
        List<ScreeningCase> current=new ArrayList<>();for(PositionApplication a:latestApps.values()){List<ScreeningCase> cases=byApp.getOrDefault(a.getId(),List.of());cases.stream().max(Comparator.comparingInt(ScreeningCase::getCaseRevision)).ifPresent(current::add);}
        List<EvaluationProceeding> proceedingHistory=proceedings.findByAgencyIdAndPublicationIdOrderByCreatedAtDesc(agency,p.getId());EvaluationProceeding proceeding=proceedingHistory.stream().findFirst().orElse(null);
        List<SelectionCase> selectionHistory=selections.findByAgencyIdAndPublicationIdOrderByCaseRevisionAsc(agency,p.getId());SelectionCase selection=selectionHistory.stream().max(Comparator.comparingInt(SelectionCase::getCaseRevision)).orElse(null);
        return new Snapshot(p,List.copyOf(latestApps.values()),byApp,current,proceeding,selection,selectionHistory);}
    private List<Snapshot> matchingSnapshots(String agency,RegisterQuery query,String authorization){Set<Long> areaUnits=query.areaId()==null?null:organizations.businessUnitIdsForArea(query.areaId(),authorization);List<Snapshot> result=new ArrayList<>();for(VacancyPublication publication:allPublications(agency)){Snapshot snapshot=snapshot(agency,publication,query.includeHistory());if(matches(agency,snapshot,query,areaUnits))result.add(snapshot);}result.sort(Comparator.comparing((Snapshot value)->eventDate(agency,value,query.dateBasis())).reversed().thenComparing(value->value.publication.getId()));return result;}
    private List<VacancyPublication> allPublications(String agency){List<VacancyPublication> result=new ArrayList<>();int page=0;Page<VacancyPublication> batch;do{batch=publications.findByAgencyId(agency,PageRequest.of(page++,100));result.addAll(batch.getContent());if(result.size()>MAX_SCAN)throw new IllegalArgumentException("Report cohort exceeds the 10000-publication scan limit; narrow the request filters");}while(batch.hasNext());return result;}
    private boolean matches(String agency,Snapshot snapshot,RegisterQuery query,Set<Long> areaUnits){VacancyPublication publication=snapshot.publication;LocalDate event=eventDate(agency,snapshot,query.dateBasis());if(event==null||event.isBefore(query.from())||event.isAfter(query.to()))return false;if(query.publicationStatus()!=null&&publication.getStatus()!=query.publicationStatus())return false;if(query.jobPositionId()!=null&&!query.jobPositionId().equals(publication.getJobPositionId()))return false;if(query.plantillaId()!=null&&!query.plantillaId().equals(publication.getPlantillaId()))return false;if(query.businessUnitId()!=null&&!query.businessUnitId().equals(publication.getBusinessUnitId()))return false;if(areaUnits!=null&&!areaUnits.contains(publication.getBusinessUnitId()))return false;if(query.proceedingStatus()!=null&&(snapshot.proceeding==null||snapshot.proceeding.getStatus()!=query.proceedingStatus()))return false;return query.selectionStatus()==null&&query.outcome()==null||selectionCandidates(snapshot,query.includeHistory()).stream().anyMatch(value->selectionMatches(value,query));}
    private static List<SelectionCase> selectionCandidates(Snapshot snapshot,boolean history){return history?snapshot.selectionHistory:(snapshot.selection==null?List.of():List.of(snapshot.selection));}
    private static boolean selectionMatches(SelectionCase value,RegisterQuery query){return (query.selectionStatus()==null||value.getStatus()==query.selectionStatus())&&(query.outcome()==null||value.getOutcome()==query.outcome());}
    private LocalDate eventDate(String agency,Snapshot snapshot,DateBasis basis){return switch(basis){case PUBLICATION_DATE->toDate(publicationAt(agency,snapshot.publication));case PUBLICATION_CLOSING_DATE->snapshot.publication.getClosingDate();case PROCEEDING_FINALIZATION_DATE->toDate(finalizedAt(snapshot.proceeding));case SELECTION_FINALIZATION_DATE->toDate(snapshot.selection==null?null:snapshot.selection.getFinalizedAt());case HANDOFF_ACKNOWLEDGMENT_DATE->{AppointmentHandoff handoff=snapshot.selection==null?null:latestHandoff(agency,snapshot.selection.getId());yield toDate(handoff==null?null:handoff.getAcknowledgedAt());}};}
    private Instant publicationAt(String agency,VacancyPublication publication){if(publication.getPublishedAt()!=null)return publication.getPublishedAt();return channels.findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(publication.getId(),agency).stream().filter(VacancyPublicationChannel::isActive).map(VacancyPublicationChannel::getPublicationDate).min(Comparator.naturalOrder()).map(value->value.atStartOfDay(ZoneId.of(TIMEZONE)).toInstant()).orElse(null);}
    private static Instant finalizedAt(EvaluationProceeding proceeding){return proceeding!=null&&proceeding.getStatus()==EvaluationProceeding.Status.FINALIZED?proceeding.getUpdatedAt():null;}
    private static LocalDate toDate(Instant value){return value==null?null:value.atZone(ZoneId.of(TIMEZONE)).toLocalDate();}
    private AppointmentHandoff latestHandoff(String agency,String selection){return handoffs.findByAgencyIdAndSelectionCaseIdOrderByHandoffRevisionDesc(agency,selection).stream().findFirst().orElse(null);}
    private static boolean isFailure(AppointmentHandoffAttempt a){String c=a.getResultCategory();return c!=null&&!c.equalsIgnoreCase("ACKNOWLEDGED")&&!c.equalsIgnoreCase("SUCCESS");}
    private static void validate(RegisterQuery q){Objects.requireNonNull(q,"query");Objects.requireNonNull(q.from(),"from");Objects.requireNonNull(q.to(),"to");Objects.requireNonNull(q.dateBasis(),"dateBasis");if(q.to().isBefore(q.from()))throw new IllegalArgumentException("to must not be before from");if(q.from().plusDays(MAX_RANGE_DAYS).isBefore(q.to()))throw new IllegalArgumentException("Date range must not exceed 366 days");if(q.page()<0||q.page()>100_000)throw new IllegalArgumentException("page must be between 0 and 100000");if(q.size()<1||q.size()>100)throw new IllegalArgumentException("size must be between 1 and 100");positive(q.jobPositionId(),"jobPositionId");positive(q.plantillaId(),"plantillaId");positive(q.areaId(),"areaId");positive(q.businessUnitId(),"businessUnitId");}
    private static void positive(Long value,String name){if(value!=null&&value<1)throw new IllegalArgumentException(name+" must be positive");}
    private static String metricCode(String value){String normalized=value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+","_").replaceAll("^_+|_+$","");return normalized.isBlank()?"UNSPECIFIED":normalized;}
    private static String filterSummary(RegisterQuery q){List<String> values=new ArrayList<>();addFilter(values,"publication",q.publicationStatus());addFilter(values,"proceeding",q.proceedingStatus());addFilter(values,"selection",q.selectionStatus());addFilter(values,"outcome",q.outcome());addFilter(values,"jobPositionId",q.jobPositionId());addFilter(values,"plantillaId",q.plantillaId());addFilter(values,"areaId",q.areaId());addFilter(values,"businessUnitId",q.businessUnitId());return values.isEmpty()?"No additional filters":"Filters: "+String.join("; ",values);}
    private static void addFilter(List<String> values,String name,Object value){if(value!=null)values.add(name+"="+value);}
    private static void addDuration(List<BigDecimal> out,Instant from,Instant to){if(from!=null&&to!=null&&!to.isBefore(from))out.add(BigDecimal.valueOf(Duration.between(from,to).toMinutes()).divide(BigDecimal.valueOf(1440),4,RoundingMode.HALF_UP));}
    private static Metric count(String code,String label,long value,String definition){return new Metric(code,label,value,null,BigDecimal.valueOf(value),"COUNT",definition);}
    private static Metric ratio(String code,String label,long numerator,long denominator,String definition){BigDecimal v=denominator==0?BigDecimal.ZERO:BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(denominator),2,RoundingMode.HALF_UP);return new Metric(code,label,numerator,denominator,v,"PERCENT",definition);}
    private static Metric average(String code,String label,List<BigDecimal> values,String definition){BigDecimal v=values.isEmpty()?BigDecimal.ZERO:values.stream().reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(values.size()),2,RoundingMode.HALF_UP);return new Metric(code,label,values.size(),null,v,"DAYS",definition);}
    private void audit(String agency,String action,String aggregate,String actor,RegisterQuery q,int count,byte[] pdf,String correlation){Map<String,Object> metadata=new LinkedHashMap<>();metadata.put("requestedBy",actor);metadata.put("dateBasis",q.dateBasis().name());metadata.put("from",q.from().toString());metadata.put("to",q.to().toString());metadata.put("includeHistory",q.includeHistory());metadata.put("publicationStatus",q.publicationStatus());metadata.put("proceedingStatus",q.proceedingStatus());metadata.put("selectionStatus",q.selectionStatus());metadata.put("outcome",q.outcome());metadata.put("jobPositionId",q.jobPositionId());metadata.put("plantillaId",q.plantillaId());metadata.put("areaId",q.areaId());metadata.put("businessUnitId",q.businessUnitId());metadata.put("timezone",TIMEZONE);metadata.put("resultCount",count);metadata.put("templateVersion",TEMPLATE_VERSION);if(pdf!=null)metadata.put("outputSha256",sha(pdf));audit.record(agency,action,aggregate,agency,null,null,null,metadata,null,correlation);}
    private static String generated(String actor){return (actor==null?"unknown":actor)+" at "+TIME.format(Instant.now())+"; "+TIMEZONE+"; "+TEMPLATE_VERSION;}
    private static String vacancy(VacancyPublication p){return p.getJobPositionName()+" / "+p.getPlantillaName()+" (#"+p.getPlantillaId()+")";}
    private static String time(Instant value){return value==null?"N/A":TIME.format(value);}
    private static String sha(byte[] value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private record Snapshot(VacancyPublication publication,List<PositionApplication> applications,Map<String,List<ScreeningCase>> screeningHistory,List<ScreeningCase> currentScreenings,EvaluationProceeding proceeding,SelectionCase selection,List<SelectionCase> selectionHistory){}
}

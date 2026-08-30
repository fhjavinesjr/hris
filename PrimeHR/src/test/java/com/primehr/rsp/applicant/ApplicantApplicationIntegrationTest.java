package com.primehr.rsp.applicant;

import com.primehr.integration.administrative.AdministrativeRspPositionSource;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancy;
import com.primehr.positionprofile.api.PositionProfileResponse;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.rsp.applicant.api.*;
import com.primehr.rsp.applicant.application.*;
import com.primehr.rsp.applicant.domain.*;
import com.primehr.rsp.applicant.infrastructure.*;
import com.primehr.rsp.domain.*;
import com.primehr.rsp.infrastructure.*;
import com.primehr.shared.exception.*;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApplicantApplicationIntegrationTest {
    @Autowired ApplicantFoundationService foundation;
    @Autowired ApplicantApplicationService applications;
    @Autowired PrivacyNoticeRepository notices;
    @Autowired RecruitmentPlanRepository plans;
    @Autowired VacancyRequestRepository vacancies;
    @Autowired VacancyPublicationRepository publications;
    @Autowired PositionApplicationRepository applicationRepository;
    @Autowired PrimeHrAuditEventRepository auditEvents;

    @BeforeEach
    void privacyNotice() {
        notices.save(new PrivacyNotice("TEST-AGENCY", "Applicant Privacy Notice", "Recruitment terms",
                "Agency retention policy", 1, LocalDate.now().minusDays(1), null,
                PrivacyNotice.Status.ACTIVE));
    }

    @Test
    void lifecyclePreservesImmutableEvidenceOwnershipAndCommunicationHistory() {
        ApplicantDtos.Session applicant = applicant("lifecycle@example.com", true);
        byte[] original = "%PDF-1.7 original evidence".getBytes(StandardCharsets.US_ASCII);
        ApplicantDtos.Document document = foundation.upload(applicant.account().id(), "PDS", "SENSITIVE",
                new MockMultipartFile("file", "pds.pdf", "application/pdf", original), null);
        VacancyPublication publication = publication(LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10), false, false);

        ApplicationDtos.Application draft = applications.create(applicant.account().id(),
                new ApplicationDtos.Create(publication.getId()), "create-correlation");
        ApplicationDtos.Application saved = applications.save(applicant.account().id(), draft.id(),
                new ApplicationDtos.Save(List.of(document.id()), draft.recordVersion()), "save-correlation");
        assertThat(saved.recordVersion()).isGreaterThan(draft.recordVersion());

        ApplicationDtos.Application submitted = applications.submit(applicant.account().id(), draft.id(),
                new ApplicationDtos.Submit(saved.recordVersion()), "submit-correlation");
        assertThat(submitted.status()).isEqualTo("SUBMITTED");
        assertThat(submitted.safeStatus()).isEqualTo("SUBMITTED");
        assertThat(submitted.acknowledgmentNumber()).startsWith("APP-");
        assertThat(submitted.documents()).singleElement().satisfies(value -> {
            assertThat(value.checksum()).isEqualTo(document.checksum());
            assertThat(value.originalFilename()).isEqualTo("pds.pdf");
        });
        PositionApplication persisted = applicationRepository.findById(submitted.id()).orElseThrow();
        assertThat(persisted.getVacancySnapshot()).contains("Accountant III");
        assertThat(persisted.getQualificationSnapshot()).contains("Bachelor degree");
        assertThat(persisted.getProfileSnapshot()).contains("lifecycle@example.com").doesNotContain("password");

        ApplicantDtos.Profile current = foundation.profile(applicant.account().id());
        foundation.saveProfile(applicant.account().id(), new ApplicantDtos.SaveProfile("Changed", null,
                "After Submission", null, null, null, null, null, null, null, null, true,
                List.of(), current.recordVersion()));
        foundation.upload(applicant.account().id(), "PDS", "SENSITIVE",
                new MockMultipartFile("file", "replacement.pdf", "application/pdf",
                        "%PDF-1.7 replacement".getBytes(StandardCharsets.US_ASCII)), document.id());
        ApplicationDtos.Application immutable = applications.staffApplication("TEST-AGENCY", submitted.id());
        assertThat(immutable.documents()).singleElement()
                .extracting(ApplicationDtos.DocumentEvidence::checksum).isEqualTo(document.checksum());
        assertThat(applications.staffDocument("TEST-AGENCY", submitted.id(),
                immutable.documents().get(0).id(), "staff").stream()).hasBinaryContent(original);
        assertThat(auditEvents.findByAgencyIdAndAggregateTypeAndAggregateId("TEST-AGENCY",
                "POSITION_APPLICATION", submitted.id(), PageRequest.of(0, 50)).getContent())
                .extracting(value -> value.getAction()).contains("STAFF_DOWNLOAD_APPLICATION_DOCUMENT");

        applications.sendStaffMessage("TEST-AGENCY", submitted.id(),
                new ApplicationDtos.StaffMessage("Information", "Your application is on file."),
                "staff-001", "message-correlation");
        assertThat(applications.applicantCommunications(applicant.account().id(), submitted.id()))
                .extracting(ApplicationDtos.Communication::direction)
                .containsExactly("SYSTEM_TO_APPLICANT", "STAFF_TO_APPLICANT");

        assertThatThrownBy(() -> applications.create(applicant.account().id(),
                new ApplicationDtos.Create(publication.getId()), null))
                .isInstanceOf(ApplicationConflictException.class);
        ApplicantDtos.Session other = applicant("other@example.com", true);
        assertThatThrownBy(() -> applications.applicantApplication(other.account().id(), submitted.id()))
                .isInstanceOf(ResourceNotFoundException.class);

        ApplicationDtos.Application withdrawn = applications.withdraw(applicant.account().id(), submitted.id(),
                new ApplicationDtos.Withdraw("Accepted another opportunity", submitted.recordVersion()),
                "withdraw-correlation");
        assertThat(withdrawn.status()).isEqualTo("WITHDRAWN");
        assertThat(applications.withdraw(applicant.account().id(), submitted.id(),
                new ApplicationDtos.Withdraw("retry", submitted.recordVersion()), null).status())
                .isEqualTo("WITHDRAWN");
        assertThatThrownBy(() -> applications.create(applicant.account().id(),
                new ApplicationDtos.Create(publication.getId()), null))
                .isInstanceOf(ApplicationConflictException.class)
                .hasMessageContaining("previous application");
    }

    @Test
    void readinessAndOptimisticConflictsRejectUnsafeChanges() {
        ApplicantDtos.Session applicant = applicant("readiness@example.com", true);
        VacancyPublication future = publication(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), false, false);
        VacancyPublication expired = publication(LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), false, false);
        VacancyPublication cancelled = publication(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), true, false);
        VacancyPublication closed = publication(LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), false, true);
        for (VacancyPublication value : List.of(future, expired, cancelled, closed)) {
            assertThatThrownBy(() -> applications.create(applicant.account().id(),
                    new ApplicationDtos.Create(value.getId()), null))
                    .isInstanceOf(ApplicationConflictException.class)
                    .hasMessage("The vacancy is not open for applications");
        }

        VacancyPublication open = publication(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), false, false);
        ApplicationDtos.Application draft = applications.create(applicant.account().id(),
                new ApplicationDtos.Create(open.getId()), null);
        assertThatThrownBy(() -> applications.save(applicant.account().id(), draft.id(),
                new ApplicationDtos.Save(List.of("missing-document"), draft.recordVersion() + 1), null))
                .isInstanceOf(OptimisticConflictException.class);
        assertThatThrownBy(() -> applications.submit(applicant.account().id(), draft.id(),
                new ApplicationDtos.Submit(draft.recordVersion()), null))
                .isInstanceOf(ApplicationConflictException.class)
                .hasMessageContaining("Required application documents are missing");
    }

    private ApplicantDtos.Session applicant(String email, boolean declaration) {
        ApplicantDtos.Session session = foundation.register(new ApplicantDtos.Register(email,
                "correct-password-123", "Test", "Applicant", foundation.currentNotice().id(), true),
                "127.0.0.1", "test");
        ApplicantDtos.Profile profile = foundation.profile(session.account().id());
        foundation.saveProfile(session.account().id(), new ApplicantDtos.SaveProfile("Test", null,
                "Applicant", null, LocalDate.of(1990, 1, 1), null, null, null, null, null,
                "Filipino", declaration, List.of(), profile.recordVersion()));
        return session;
    }

    private VacancyPublication publication(LocalDate opening, LocalDate closing,
                                           boolean cancel, boolean close) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        RecruitmentPlan plan = new RecruitmentPlan("TEST-AGENCY", "RP-" + suffix, "Plan",
                LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), null);
        plan.submit("submitter", Instant.now()); plan.approve("approver", Instant.now()); plans.saveAndFlush(plan);
        VacancyRequest vacancy = new VacancyRequest("TEST-AGENCY", plan, VacancyType.ACTUAL,
                null, null, null, null, "HIGH", null, "Required", administrative(), occupancy(), profile());
        vacancy.submit("submitter", Instant.now()); vacancy.authorize("approver", Instant.now());
        vacancies.saveAndFlush(vacancy);
        VacancyPublication publication = new VacancyPublication("TEST-AGENCY", vacancy, VacancyVisibility.BOTH,
                opening, closing, "Submit required documents", "Contact HR", "Vacancy notice", Instant.now());
        publication.submit("submitter", Instant.now()); publication.approve("approver", Instant.now());
        publication.publish("publisher", Instant.now());
        if (cancel) publication.cancel("publisher", Instant.now());
        if (close) publication.close("publisher", Instant.now());
        return publications.saveAndFlush(publication);
    }

    private static AdministrativeRspPositionSource administrative() {
        return new AdministrativeRspPositionSource(3L, "Plantilla #3", 14L, "Accountant III",
                19L, 1L, 2L, "FIN", "Finance", 7L, 2,
                "Bachelor degree", "8 hours", "2 years", "RA 1080", null, "CSC MC",
                LocalDate.now().minusYears(1), null, "a".repeat(64), Instant.now());
    }

    private static HumanResourcePlantillaOccupancy occupancy() {
        return new HumanResourcePlantillaOccupancy(3L, false, null, null, "b".repeat(64), Instant.now());
    }

    private static PositionProfileResponse profile() {
        return new PositionProfileResponse("profile-1", "Profile", null, PositionProfileStatus.ACTIVE,
                4, null, LocalDate.now().minusYears(1), null, 3L, 8L,
                "submitter", Instant.now(), "approver", Instant.now(), null, List.of());
    }
}

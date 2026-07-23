package com.humanresource.impl;

import com.humanresource.entitymodels.Children;
import com.humanresource.entitymodels.CivilServiceEligibility;
import com.humanresource.entitymodels.EducationalBackground;
import com.humanresource.entitymodels.LearningAndDevelopment;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.entitymodels.References;
import com.humanresource.entitymodels.VoluntaryWork;
import com.humanresource.entitymodels.WorkExperience;
import com.humanresource.repositories.ChildrenRepository;
import com.humanresource.repositories.CivilServiceEligibilityRepository;
import com.humanresource.repositories.EducationalBackgroundRepository;
import com.humanresource.repositories.LearningAndDevelopmentRepository;
import com.humanresource.repositories.PersonalDataRepository;
import com.humanresource.repositories.ReferencesRepository;
import com.humanresource.repositories.VoluntaryWorkRepository;
import com.humanresource.repositories.WorkExperienceRepository;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PDSReportServiceImplTest {

    private PersonalDataRepository personalDataRepository;
    private EducationalBackgroundRepository educationalBackgroundRepository;
    private ChildrenRepository childrenRepository;
    private CivilServiceEligibilityRepository civilServiceEligibilityRepository;
    private WorkExperienceRepository workExperienceRepository;
    private VoluntaryWorkRepository voluntaryWorkRepository;
    private LearningAndDevelopmentRepository learningAndDevelopmentRepository;
    private ReferencesRepository referencesRepository;
    private PDSReportServiceImpl service;

    @BeforeEach
    void setUp() {
        personalDataRepository = mock(PersonalDataRepository.class);
        educationalBackgroundRepository = mock(EducationalBackgroundRepository.class);
        childrenRepository = mock(ChildrenRepository.class);
        civilServiceEligibilityRepository = mock(CivilServiceEligibilityRepository.class);
        workExperienceRepository = mock(WorkExperienceRepository.class);
        voluntaryWorkRepository = mock(VoluntaryWorkRepository.class);
        learningAndDevelopmentRepository = mock(LearningAndDevelopmentRepository.class);
        referencesRepository = mock(ReferencesRepository.class);

        service = new PDSReportServiceImpl(
                personalDataRepository,
                educationalBackgroundRepository,
                childrenRepository,
                civilServiceEligibilityRepository,
                workExperienceRepository,
                voluntaryWorkRepository,
                learningAndDevelopmentRepository,
                referencesRepository,
                new PDSReportDataMapper()
        );
    }

    @Test
    void generatesFourPagePdfWithoutAReportDatabaseConnection() throws Exception {
        PersonalData personalData = new PersonalData();
        personalData.setPersonalDataId(100L);
        personalData.setEmployeeId(200L);
        personalData.setSurname("Dela Cruz");
        personalData.setFirstname("Juan");
        personalData.setMiddlename("Santos");
        personalData.setDob(LocalDateTime.of(1990, 5, 6, 0, 0));
        personalData.setPob("Quezon City");
        personalData.setSex_id(1);
        personalData.setCivilStatus_id(1);
        personalData.setCitizenship("Filipino");
        personalData.setSkillOrHobby("Programming");
        personalData.setDistinction("Outstanding Employee");
        personalData.setAssociation("Government IT Association");
        personalData.setQ34a("yes");
        personalData.setQ34b("no");
        byte[] onePixelPng = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
        );
        personalData.setEmployeePicture(onePixelPng);
        personalData.setEmployeeSignature(onePixelPng);

        when(personalDataRepository.findByEmployeeId(200L)).thenReturn(personalData);
        when(educationalBackgroundRepository.findByPersonalDataId(100L))
                .thenReturn(List.of(education()));
        when(childrenRepository.findByPersonalDataId(100L)).thenReturn(List.of(child()));
        when(civilServiceEligibilityRepository.findByPersonalDataId(100L))
                .thenReturn(List.of(civilServiceEligibility()));
        when(workExperienceRepository.findByPersonalDataId(100L))
                .thenReturn(List.of(workExperience()));
        when(voluntaryWorkRepository.findByPersonalDataId(100L))
                .thenReturn(List.of(voluntaryWork()));
        when(learningAndDevelopmentRepository.findByPersonalDataId(100L))
                .thenReturn(List.of(learningAndDevelopment()));
        when(referencesRepository.findByPersonalDataId(100L))
                .thenReturn(List.of(reference()));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.generatePDS(200L, output);

        byte[] pdf = output.toByteArray();
        assertTrue(pdf.length > 10_000);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertEquals(4, reader.getNumberOfPages());
        } finally {
            reader.close();
        }

        String previewOutput = System.getProperty("pds.preview.output");
        if (previewOutput != null && !previewOutput.isBlank()) {
            Files.write(Path.of(previewOutput), pdf);
        }
    }

    @Test
    void rejectsAnEmployeeWithoutPersonalDataBeforeLoadingChildRecords() {
        when(personalDataRepository.findByEmployeeId(404L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.generatePDS(404L, new ByteArrayOutputStream())
        );

        assertTrue(exception.getMessage().contains("404"));
        verify(childrenRepository, never()).findByPersonalDataId(org.mockito.ArgumentMatchers.any());
    }

    private EducationalBackground education() {
        EducationalBackground education = new EducationalBackground();
        education.setEducationalBackgroundId(1L);
        education.setPersonalDataId(100L);
        education.setLevelOfEducation("College");
        education.setNameOfSchool("State University");
        education.setDegreeCourse("Bachelor of Science in Information Technology");
        education.setScoreGrade(1.25d);
        education.setYearGraduated(2012L);
        education.setFromDate(LocalDateTime.of(2008, 6, 1, 0, 0));
        education.setToDate(LocalDateTime.of(2012, 3, 31, 0, 0));
        education.setHonorsReceived("Cum Laude");
        return education;
    }

    private Children child() {
        Children child = new Children();
        child.setChildrenId(1L);
        child.setPersonalDataId(100L);
        child.setChildFullname("Maria Dela Cruz");
        child.setDob(LocalDateTime.of(2018, 1, 2, 0, 0));
        return child;
    }

    private CivilServiceEligibility civilServiceEligibility() {
        CivilServiceEligibility eligibility = new CivilServiceEligibility();
        eligibility.setCivilServiceEligibilityId(1L);
        eligibility.setPersonalDataId(100L);
        eligibility.setCareerServiceName("Career Service Professional");
        eligibility.setCivilServiceRating(88.75d);
        eligibility.setDateOfExamination(LocalDateTime.of(2015, 4, 12, 0, 0));
        eligibility.setPlaceOfExamination("Manila");
        eligibility.setLicenseNumber("CSP-12345");
        eligibility.setLicenseValidityDate(LocalDateTime.of(2030, 4, 12, 0, 0));
        return eligibility;
    }

    private WorkExperience workExperience() {
        WorkExperience experience = new WorkExperience();
        experience.setWorkExperienceId(1L);
        experience.setPersonalDataId(100L);
        experience.setFromDate(LocalDateTime.of(2020, 1, 1, 0, 0));
        experience.setPositionTitle("Information Technology Officer");
        experience.setAgencyName("Example Government Agency");
        experience.setMonthlySalary(50_000d);
        experience.setPayGrade(18L);
        experience.setWorkStatus("Permanent");
        experience.setBoolGovernmentService("yes");
        return experience;
    }

    private VoluntaryWork voluntaryWork() {
        VoluntaryWork voluntaryWork = new VoluntaryWork();
        voluntaryWork.setVoluntaryWorkId(1L);
        voluntaryWork.setPersonalDataId(100L);
        voluntaryWork.setOrganizationName("Community Technology Program");
        voluntaryWork.setFromDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        voluntaryWork.setToDate(LocalDateTime.of(2024, 2, 1, 0, 0));
        voluntaryWork.setVoluntaryHrs(40L);
        voluntaryWork.setPositionTitle("Volunteer Trainer");
        return voluntaryWork;
    }

    private LearningAndDevelopment learningAndDevelopment() {
        LearningAndDevelopment learning = new LearningAndDevelopment();
        learning.setLearningAndDevelopmentId(1L);
        learning.setPersonalDataId(100L);
        learning.setProgramName("Government Digital Transformation");
        learning.setFromDate(LocalDateTime.of(2025, 6, 10, 0, 0));
        learning.setToDate(LocalDateTime.of(2025, 6, 12, 0, 0));
        learning.setLndHrs(24L);
        learning.setLndType("Technical");
        learning.setConductedBy("Civil Service Institute");
        return learning;
    }

    private References reference() {
        References reference = new References();
        reference.setReferencesId(1L);
        reference.setPersonalDataId(100L);
        reference.setRefName("Ana Reyes");
        reference.setAddress("Quezon City");
        reference.setContactNo("09171234567");
        return reference;
    }
}

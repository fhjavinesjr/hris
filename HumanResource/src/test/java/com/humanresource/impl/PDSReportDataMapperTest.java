package com.humanresource.impl;

import com.humanresource.dtos.PDSReportData;
import com.humanresource.entitymodels.Children;
import com.humanresource.entitymodels.EducationalBackground;
import com.humanresource.entitymodels.PersonalData;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PDSReportDataMapperTest {

    private final PDSReportDataMapper mapper = new PDSReportDataMapper();

    @Test
    void mapsProviderNeutralTypesAndSelectsOneCoherentEducationRecord() {
        PersonalData personalData = personalData();
        EducationalBackground olderCollege = education(
                1L,
                "college",
                "Old College",
                "Old Course",
                LocalDateTime.of(2010, 6, 1, 0, 0),
                LocalDateTime.of(2014, 3, 31, 0, 0)
        );
        EducationalBackground latestCollege = education(
                2L,
                "  COLLEGE - Undergraduate",
                "Latest College",
                "Latest Course",
                LocalDateTime.of(2015, 6, 1, 0, 0),
                LocalDateTime.of(2019, 3, 31, 0, 0)
        );

        Children youngerChild = child(
                2L,
                "Younger Child",
                LocalDateTime.of(2018, 1, 2, 0, 0)
        );
        Children olderChild = child(
                1L,
                "Older Child",
                LocalDateTime.of(2015, 1, 2, 0, 0)
        );

        PDSReportData data = mapper.map(
                personalData,
                List.of(olderCollege, latestCollege),
                List.of(youngerChild, olderChild),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                LocalDate.of(2026, 7, 23)
        );

        Map<String, Object> pageOne = data.getPageOneRow();
        assertEquals("Latest College", pageOne.get("edu4SchoolName"));
        assertEquals("Latest Course", pageOne.get("edu4DegreeCourse"));
        assertEquals("06/01/2015", pageOne.get("edu4DateFromString"));
        assertEquals("03/31/2019", pageOne.get("eduDateToString4"));
        assertEquals(175.0d, pageOne.get("height"));
        assertEquals("Filipino", pageOne.get("nationality"));
        assertEquals("Dual Citizenship", pageOne.get("nationalityDualCitizenship"));

        Map<String, Object> pageFour = data.getPageFourRow();
        assertEquals(Boolean.TRUE, pageFour.get("qa1"));
        assertEquals(Boolean.FALSE, pageFour.get("qa2"));
        assertEquals(java.sql.Date.valueOf("2026-07-23"), pageFour.get("dateSignature"));
        assertInstanceOf(InputStream.class, pageFour.get("picture"));
        assertInstanceOf(InputStream.class, pageFour.get("signature"));

        assertEquals("Older Child", data.getChildrenRows().get(0).get("lastname"));
        assertEquals("Younger Child", data.getChildrenRows().get(1).get("lastname"));
    }

    @Test
    void mapsNullAndUnknownQuestionValuesToSafeReportDefaults() {
        PersonalData personalData = new PersonalData();
        personalData.setPersonalDataId(10L);
        personalData.setFirstname("Test");
        personalData.setQ34a(null);
        personalData.setQ34b("unexpected");

        PDSReportData data = mapper.map(
                personalData,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                LocalDate.of(2026, 7, 23)
        );

        assertFalse((Boolean) data.getPageFourRow().get("qa1"));
        assertFalse((Boolean) data.getPageFourRow().get("qa2"));
        assertTrue(data.getOtherInformationRows().size() == 1);
        assertEquals("", data.getOtherInformationRows().get(0).get("skill"));
    }

    private PersonalData personalData() {
        PersonalData personalData = new PersonalData();
        personalData.setPersonalDataId(10L);
        personalData.setEmployeeId(20L);
        personalData.setSurname("Dela Cruz");
        personalData.setFirstname("Juan");
        personalData.setMiddlename("Santos");
        personalData.setDob(LocalDateTime.of(1990, 5, 6, 0, 0));
        personalData.setSex_id(1);
        personalData.setCivilStatus_id(1);
        personalData.setHeight(175);
        personalData.setWeight(70);
        personalData.setCitizenship("Filipino - Dual");
        personalData.setQ34a("YES");
        personalData.setQ34b("no");
        personalData.setEmployeePicture(new byte[]{1, 2, 3});
        personalData.setEmployeeSignature(new byte[]{4, 5, 6});
        return personalData;
    }

    private EducationalBackground education(
            Long id,
            String level,
            String school,
            String course,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        EducationalBackground education = new EducationalBackground();
        education.setEducationalBackgroundId(id);
        education.setLevelOfEducation(level);
        education.setNameOfSchool(school);
        education.setDegreeCourse(course);
        education.setFromDate(fromDate);
        education.setToDate(toDate);
        return education;
    }

    private Children child(Long id, String name, LocalDateTime birthDate) {
        Children child = new Children();
        child.setChildrenId(id);
        child.setChildFullname(name);
        child.setDob(birthDate);
        return child;
    }
}

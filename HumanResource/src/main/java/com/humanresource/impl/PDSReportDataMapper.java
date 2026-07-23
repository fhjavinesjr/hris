package com.humanresource.impl;

import com.humanresource.dtos.PDSReportData;
import com.humanresource.entitymodels.Children;
import com.humanresource.entitymodels.CivilServiceEligibility;
import com.humanresource.entitymodels.EducationalBackground;
import com.humanresource.entitymodels.LearningAndDevelopment;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.entitymodels.References;
import com.humanresource.entitymodels.VoluntaryWork;
import com.humanresource.entitymodels.WorkExperience;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class PDSReportDataMapper {

    private static final DateTimeFormatter PDS_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ROOT);
    private static final Set<String> TRUE_VALUES = Set.of("true", "yes", "y", "1");
    private static final Comparator<LocalDateTime> ASCENDING_DATE =
            Comparator.nullsLast(Comparator.naturalOrder());
    private static final Comparator<LocalDateTime> DESCENDING_DATE =
            Comparator.nullsLast(Comparator.reverseOrder());
    private static final Comparator<Long> ASCENDING_ID =
            Comparator.nullsLast(Comparator.naturalOrder());
    private static final Comparator<Long> DESCENDING_ID =
            Comparator.nullsLast(Comparator.reverseOrder());

    public PDSReportData map(
            PersonalData personalData,
            List<EducationalBackground> educationalBackgrounds,
            List<Children> children,
            List<CivilServiceEligibility> civilServiceEligibilities,
            List<WorkExperience> workExperiences,
            List<VoluntaryWork> voluntaryWorks,
            List<LearningAndDevelopment> learningAndDevelopments,
            List<References> references,
            LocalDate signatureDate) {

        return new PDSReportData(
                pageOneRow(personalData, educationalBackgrounds),
                pageFourRow(personalData, signatureDate),
                childrenRows(children),
                civilServiceRows(civilServiceEligibilities),
                workExperienceRows(workExperiences),
                voluntaryWorkRows(voluntaryWorks),
                learningAndDevelopmentRows(learningAndDevelopments),
                otherInformationRows(personalData),
                referenceRows(references)
        );
    }

    private Map<String, Object> pageOneRow(
            PersonalData personalData,
            List<EducationalBackground> educationalBackgrounds) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("lastname", text(personalData.getSurname()));
        row.put("firstname", text(personalData.getFirstname()));
        row.put("middlename", text(personalData.getMiddlename()));
        row.put("nameExtension", text(personalData.getExtname()));
        row.put("birthDate", sqlDate(personalData.getDob()));
        row.put("birthPlace", text(personalData.getPob()));
        row.put("residentaddress", text(personalData.getResAddress()));
        row.put("residentzipcode", text(personalData.getResZip()));
        row.put("height", toDouble(personalData.getHeight()));
        row.put("weight", toDouble(personalData.getWeight()));
        row.put("bloodType", text(personalData.getBloodType()));
        row.put("gsisNo", text(personalData.getGsisId()));
        row.put("pagibigNo", text(personalData.getPagibigId()));
        row.put("philHealthNo", text(personalData.getPhilhealthNo()));
        row.put("sssNo", text(personalData.getSssNo()));
        row.put("tinNo", text(personalData.getTinNo()));
        row.put("agencyEmployeeNo", text(personalData.getAgencyEmpNo()));
        row.put("telNo", text(personalData.getTelNo()));
        row.put("mobileNo", text(personalData.getMobileNo()));
        row.put("email", text(personalData.getEmail()));
        row.put("spLastname", text(personalData.getSpouseSurname()));
        row.put("spFirstname", text(personalData.getSpouseFirstname()));
        row.put("spMiddlename", text(personalData.getSpouseMiddlename()));
        row.put("spOccupation", text(personalData.getSpouseOccupation()));
        row.put("spBusinessName", text(personalData.getSpouseEmployer()));
        row.put("spBusinessAddress", text(personalData.getSpouseBusinessAddress()));
        row.put("spBusinessTelNo", text(personalData.getSpouseTelNo()));
        row.put("faLastname", text(personalData.getFatherSurname()));
        row.put("faFirstname", text(personalData.getFatherFirstname()));
        row.put("faMiddlename", text(personalData.getFatherMiddlename()));
        row.put("moLastname", text(personalData.getMotherSurname()));
        row.put("moFirstname", text(personalData.getMotherFirstname()));
        row.put("moMiddlename", text(personalData.getMotherMiddlename()));
        row.put("csIdNo", "");
        row.put("gender", gender(personalData.getSex_id()));
        row.put("civilStatus", civilStatus(personalData.getCivilStatus_id()));
        row.put("permanentAddress", text(personalData.getPermAddress()));
        row.put("zipCode", text(personalData.getPermZip()));
        row.put("spnameExtension", "");
        row.put("fanameExtenshion", "");
        row.put("maidenName", "");

        String citizenship = text(personalData.getCitizenship());
        String normalizedCitizenship = citizenship.toUpperCase(Locale.ROOT);
        row.put("nationality",
                normalizedCitizenship.contains("FILIPINO") ? "Filipino" : citizenship);
        row.put("nationalityDualCitizenship",
                normalizedCitizenship.contains("DUAL") ? "Dual Citizenship" : "");
        row.put("nationalityIndicateCountry", "");

        putEducation(
                row,
                "edu1",
                "edu1DateFromString",
                "eduDateToString",
                selectEducation(educationalBackgrounds, "ELEMENTARY")
        );
        putEducation(
                row,
                "edu2",
                "edu2DateFromString",
                "eduDateToString2",
                selectEducation(educationalBackgrounds, "SECONDARY")
        );
        putEducation(
                row,
                "edu3",
                "edu3DateFromString",
                "eduDateToString3",
                selectEducation(educationalBackgrounds, "VOCATIONAL")
        );
        putEducation(
                row,
                "edu4",
                "edu4DateFromString",
                "eduDateToString4",
                selectEducation(educationalBackgrounds, "COLLEGE")
        );
        putEducation(
                row,
                "edu5",
                "edu5DateFromString",
                "eduDateToString6",
                selectEducation(educationalBackgrounds, "GRADUATE")
        );

        return row;
    }

    private Map<String, Object> pageFourRow(PersonalData personalData, LocalDate signatureDate) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("qa1", booleanValue(personalData.getQ34a()));
        row.put("qa2", booleanValue(personalData.getQ34b()));
        row.put("qa2Detail", text(personalData.getQ34bDetails()));
        row.put("qa3", booleanValue(personalData.getQ35a()));
        row.put("qa3Detail", text(personalData.getQ35aDetails()));
        row.put("qa4", booleanValue(personalData.getQ35b()));
        row.put("qa4Detail", text(personalData.getQ35bDetails()));
        row.put("qa4Detail2", text(personalData.getQ35bStatus()));
        row.put("qa5", booleanValue(personalData.getQ36()));
        row.put("qa5Detail", text(personalData.getQ36Details()));
        row.put("qa6", booleanValue(personalData.getQ37a()));
        row.put("qa6Detail", text(personalData.getQ37aDetails()));
        row.put("qa7", booleanValue(personalData.getQ37b()));
        row.put("qa7Detail", text(personalData.getQ37bDetails()));
        row.put("qa8", booleanValue(personalData.getQ37c()));
        row.put("qa8Detail", text(personalData.getQ37cDetails()));
        row.put("qa9", booleanValue(personalData.getQ38()));
        row.put("qa9Detail", text(personalData.getQ38Details()));
        row.put("qa10", booleanValue(personalData.getQ39a()));
        row.put("qa10Detail", text(personalData.getQ39aDetails()));
        row.put("qa11", booleanValue(personalData.getQ39b()));
        row.put("qa11Detail", text(personalData.getQ39bDetails()));
        row.put("qa12", booleanValue(personalData.getQ39c()));
        row.put("qa12Detail", text(personalData.getQ39cDetails()));
        row.put("picture", inputStream(personalData.getEmployeePicture()));
        row.put("govtIssuedId", text(personalData.getGovIdType()));
        row.put("govtIssuedIdNo", text(personalData.getGovIdNumber()));
        row.put("ctcIssueDate", sqlDate(personalData.getGovIdDate()));
        row.put("ctcIssuePlace", text(personalData.getGovIdPlace()));
        row.put("signature", inputStream(personalData.getEmployeeSignature()));
        row.put("personAdministerOath", "");
        row.put("personAdministerOathDate", null);
        row.put("lastname", text(personalData.getSurname()));
        row.put("firstname", text(personalData.getFirstname()));
        row.put("middlename", text(personalData.getMiddlename()));
        row.put("dateSignature", signatureDate == null ? null : Date.valueOf(signatureDate));
        row.put("fullnameEmp", fullName(personalData));

        return row;
    }

    private List<Map<String, ?>> childrenRows(List<Children> children) {
        List<Children> sorted = new ArrayList<>(children);
        sorted.sort(
                Comparator.comparing(Children::getDob, ASCENDING_DATE)
                        .thenComparing(
                                child -> text(child.getChildFullname()),
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparing(Children::getChildrenId, ASCENDING_ID)
        );

        List<Map<String, ?>> rows = new ArrayList<>(sorted.size());
        for (Children child : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("firstname", "");
            row.put("mi", "");
            row.put("lastname", text(child.getChildFullname()));
            row.put("birthDate", sqlDate(child.getDob()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, ?>> civilServiceRows(
            List<CivilServiceEligibility> civilServiceEligibilities) {
        List<CivilServiceEligibility> sorted = new ArrayList<>(civilServiceEligibilities);
        sorted.sort(
                Comparator.comparing(
                                CivilServiceEligibility::getDateOfExamination,
                                DESCENDING_DATE
                        )
                        .thenComparing(
                                CivilServiceEligibility::getCivilServiceEligibilityId,
                                DESCENDING_ID
                        )
        );

        List<Map<String, ?>> rows = new ArrayList<>(sorted.size());
        for (CivilServiceEligibility eligibility : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("career", text(eligibility.getCareerServiceName()));
            row.put("rating", eligibility.getCivilServiceRating());
            row.put("dateTaken", sqlDate(eligibility.getDateOfExamination()));
            row.put("licenseNo", text(eligibility.getLicenseNumber()));
            row.put("licenseReleaseDate", sqlDate(eligibility.getLicenseValidityDate()));
            row.put("remarks", "");
            row.put("placeTaken", text(eligibility.getPlaceOfExamination()));
            row.put("dateTakenString", formattedDate(eligibility.getDateOfExamination()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, ?>> workExperienceRows(List<WorkExperience> workExperiences) {
        List<WorkExperience> sorted = new ArrayList<>(workExperiences);
        sorted.sort(
                Comparator.comparing(WorkExperience::getFromDate, DESCENDING_DATE)
                        .thenComparing(WorkExperience::getWorkExperienceId, DESCENDING_ID)
        );

        List<Map<String, ?>> rows = new ArrayList<>(sorted.size());
        for (WorkExperience experience : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("employmentDate", sqlDate(experience.getFromDate()));
            row.put("separationDate", sqlDate(experience.getToDate()));
            row.put("position", text(experience.getPositionTitle()));
            row.put("companyAndDepartment", text(experience.getAgencyName()));
            row.put("salary", experience.getMonthlySalary());
            row.put("salaryIncStep", toDouble(experience.getPayGrade()));
            row.put("appointmentStatus", text(experience.getWorkStatus()));
            row.put("isGovernmentService", booleanValue(experience.getBoolGovernmentService()));
            row.put(
                    "separationDateTo",
                    experience.getToDate() == null
                            ? "Present"
                            : formattedDate(experience.getToDate())
            );
            row.put(
                    "salaryIncStepString",
                    experience.getPayGrade() == null ? "" : experience.getPayGrade().toString()
            );
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, ?>> voluntaryWorkRows(List<VoluntaryWork> voluntaryWorks) {
        List<VoluntaryWork> sorted = new ArrayList<>(voluntaryWorks);
        sorted.sort(
                Comparator.comparing(VoluntaryWork::getFromDate, DESCENDING_DATE)
                        .thenComparing(VoluntaryWork::getVoluntaryWorkId, DESCENDING_ID)
        );

        List<Map<String, ?>> rows = new ArrayList<>(sorted.size());
        for (VoluntaryWork voluntaryWork : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nameAndAddress", text(voluntaryWork.getOrganizationName()));
            row.put("dateFrom", sqlDate(voluntaryWork.getFromDate()));
            row.put("dateTo", sqlDate(voluntaryWork.getToDate()));
            row.put("noOfHours", toInteger(voluntaryWork.getVoluntaryHrs()));
            row.put("positionAndNature", text(voluntaryWork.getPositionTitle()));
            row.put("dateToString", formattedDate(voluntaryWork.getToDate()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, ?>> learningAndDevelopmentRows(
            List<LearningAndDevelopment> learningAndDevelopments) {
        List<LearningAndDevelopment> sorted = new ArrayList<>(learningAndDevelopments);
        sorted.sort(
                Comparator.comparing(
                                LearningAndDevelopment::getFromDate,
                                DESCENDING_DATE
                        )
                        .thenComparing(
                                LearningAndDevelopment::getLearningAndDevelopmentId,
                                DESCENDING_ID
                        )
        );

        List<Map<String, ?>> rows = new ArrayList<>(sorted.size());
        for (LearningAndDevelopment learning : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", text(learning.getProgramName()));
            row.put("dateFrom", sqlDate(learning.getFromDate()));
            row.put("dateTo", sqlDate(learning.getToDate()));
            row.put("noOfHours", toInteger(learning.getLndHrs()));
            row.put("sponsoredBy", text(learning.getConductedBy()));
            row.put("dateRange", dateRange(learning.getFromDate(), learning.getToDate()));
            row.put("typeOfLd", text(learning.getLndType()));
            row.put("dateToString", formattedDate(learning.getToDate()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, ?>> otherInformationRows(PersonalData personalData) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skill", text(personalData.getSkillOrHobby()));
        row.put("recognition", text(personalData.getDistinction()));
        row.put("organization", text(personalData.getAssociation()));
        return List.of(row);
    }

    private List<Map<String, ?>> referenceRows(List<References> references) {
        List<References> sorted = new ArrayList<>(references);
        sorted.sort(Comparator.comparing(References::getReferencesId, ASCENDING_ID));

        List<Map<String, ?>> rows = new ArrayList<>(sorted.size());
        for (References reference : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", text(reference.getRefName()));
            row.put("address", text(reference.getAddress()));
            row.put("telNo", text(reference.getContactNo()));
            rows.add(row);
        }
        return rows;
    }

    private void putEducation(
            Map<String, Object> row,
            String fieldPrefix,
            String dateFromField,
            String dateToField,
            EducationalBackground education) {
        row.put(
                fieldPrefix + "SchoolName",
                education == null ? "" : text(education.getNameOfSchool())
        );
        row.put(
                fieldPrefix + "DegreeCourse",
                education == null ? "" : text(education.getDegreeCourse())
        );
        row.put(fieldPrefix + "GradesUnits", education == null ? null : education.getScoreGrade());
        row.put(
                fieldPrefix + "YearGraduated",
                education == null ? null : toInteger(education.getYearGraduated())
        );
        row.put(
                fieldPrefix + "ScholarshipHonors",
                education == null ? "" : text(education.getHonorsReceived())
        );
        row.put(
                dateFromField,
                education == null ? "" : formattedDate(education.getFromDate())
        );
        row.put(
                dateToField,
                education == null ? "" : formattedDate(education.getToDate())
        );
    }

    private EducationalBackground selectEducation(
            List<EducationalBackground> educationalBackgrounds,
            String levelPrefix) {
        return educationalBackgrounds.stream()
                .filter(education -> startsWithIgnoreCase(
                        education.getLevelOfEducation(),
                        levelPrefix
                ))
                .max(
                        Comparator.comparing(
                                        EducationalBackground::getToDate,
                                        Comparator.nullsFirst(Comparator.naturalOrder())
                                )
                                .thenComparing(
                                        EducationalBackground::getFromDate,
                                        Comparator.nullsFirst(Comparator.naturalOrder())
                                )
                                .thenComparing(
                                        EducationalBackground::getEducationalBackgroundId,
                                        Comparator.nullsFirst(Comparator.naturalOrder())
                                )
                )
                .orElse(null);
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value != null
                && value.stripLeading().toUpperCase(Locale.ROOT).startsWith(prefix);
    }

    private static String gender(Integer sexId) {
        if (Integer.valueOf(1).equals(sexId)) {
            return "Male";
        }
        if (Integer.valueOf(2).equals(sexId)) {
            return "Female";
        }
        return "";
    }

    private static String civilStatus(Integer civilStatusId) {
        if (civilStatusId == null) {
            return "";
        }
        return switch (civilStatusId) {
            case 1 -> "Single";
            case 2 -> "Married";
            case 3 -> "Widowed";
            case 4 -> "Separated";
            case 5 -> "Other/s";
            default -> "";
        };
    }

    private static Boolean booleanValue(String value) {
        return value != null && TRUE_VALUES.contains(value.strip().toLowerCase(Locale.ROOT));
    }

    private static ByteArrayInputStream inputStream(byte[] bytes) {
        return bytes == null || bytes.length == 0 ? null : new ByteArrayInputStream(bytes);
    }

    private static Date sqlDate(LocalDateTime value) {
        return value == null ? null : Date.valueOf(value.toLocalDate());
    }

    private static String formattedDate(LocalDateTime value) {
        return value == null ? "" : PDS_DATE_FORMAT.format(value);
    }

    private static String dateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        String from = formattedDate(fromDate);
        String to = formattedDate(toDate);
        if (from.isEmpty()) {
            return to;
        }
        return to.isEmpty() ? from : from + " - " + to;
    }

    private static String fullName(PersonalData personalData) {
        return String.join(
                " ",
                text(personalData.getFirstname()),
                text(personalData.getMiddlename()),
                text(personalData.getSurname())
        ).trim().replaceAll("\\s+", " ");
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static Double toDouble(Number value) {
        return value == null ? null : value.doubleValue();
    }

    private static Integer toInteger(Number value) {
        return value == null ? null : Math.toIntExact(value.longValue());
    }
}

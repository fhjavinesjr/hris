package com.humanresource.onboarding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.PersonnelActionReportData;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.impl.EmployeeAppointmentReportDataLoader;
import com.humanresource.onboarding.AppointmentDocumentDtos.CreateDocumentCommand;
import com.humanresource.onboarding.AppointmentDocumentDtos.DocumentKind;
import com.humanresource.onboarding.AppointmentDocumentDtos.FinalizeDocumentCommand;
import com.humanresource.onboarding.AppointmentDocumentDtos.UpdateDocumentCommand;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppointmentDocumentServiceTest {
    private JdbcTemplate jdbc;
    private AppointmentDocumentService service;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .setName("appointment_documents_" + System.nanoTime()
                        + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
                .build();
        jdbc = new JdbcTemplate(dataSource);
        schema();

        EmployeeAppointmentRepository appointments = mock(EmployeeAppointmentRepository.class);
        EmployeeRepository employees = mock(EmployeeRepository.class);
        PersonalDataRepository personalData = mock(PersonalDataRepository.class);
        EmployeeAppointmentReportDataLoader loader = mock(EmployeeAppointmentReportDataLoader.class);
        AppointmentDocumentReportRenderer renderer = mock(AppointmentDocumentReportRenderer.class);

        EmployeeAppointment appointment = new EmployeeAppointment();
        appointment.setEmployeeAppointmentId(8L);
        appointment.setEmployeeId(7L);
        appointment.setAppointmentIssuedDate(LocalDateTime.of(2026, 8, 1, 0, 0));
        appointment.setAssumptionToDutyDate(LocalDateTime.of(2026, 8, 4, 0, 0));
        when(appointments.findById(8L)).thenReturn(Optional.of(appointment));

        Employee signatory = new Employee();
        signatory.setEmployeeId(9L);
        signatory.setFirstname("Maria");
        signatory.setLastname("Santos");
        signatory.setPosition("Medical Center Chief");
        when(employees.findById(9L)).thenReturn(Optional.of(signatory));
        when(personalData.findByEmployeeId(7L)).thenReturn(null);
        when(loader.load(8L)).thenReturn(new PersonnelActionReportData(
                "ZCMC", "Zamboanga City", "Juan Dela Cruz", "Appointment", "August 4, 2026",
                "", "Administrative Officer V", "", "", "", "HRMO", "", "Medical Division",
                "", null, null));
        when(renderer.oath(any())).thenReturn("%PDF-oath".getBytes(StandardCharsets.US_ASCII));
        when(renderer.assumption(any())).thenReturn("%PDF-assumption".getBytes(StandardCharsets.US_ASCII));

        service = new AppointmentDocumentService(jdbc, new ObjectMapper(), appointments, employees,
                personalData, loader, renderer);
    }

    @Test
    void finalizationIsImmutableAndCorrectionSupersedesTheFinalizedSnapshot() {
        var draft = service.create("agency", 8L, oath(null), "encoder", "corr-1");
        assertEquals("DRAFT", draft.status());
        assertThrows(ResponseStatusException.class,
                () -> service.renderDocument("agency", 8L, draft.id(), "reader", "corr-2"));

        var finalized = service.finalizeDocument("agency", 8L, draft.id(),
                new FinalizeDocumentCommand(0, "Signed original reviewed"), "finalizer", "corr-3");
        assertEquals("FINALIZED", finalized.status());
        assertNotNull(finalized.sourceFingerprint());
        assertEquals("%PDF-oath", new String(service.renderDocument("agency", 8L, draft.id(),
                "reader", "corr-4"), StandardCharsets.US_ASCII));
        ResponseStatusException immutable = assertThrows(ResponseStatusException.class,
                () -> service.update("agency", 8L, draft.id(), new UpdateDocumentCommand(
                        LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null,
                        "Zamboanga City", 9L, null, null, 1), "editor", "corr-5"));
        assertEquals(HttpStatus.CONFLICT, immutable.getStatusCode());

        var correction = service.create("agency", 8L, oath(draft.id()), "encoder", "corr-6");
        assertEquals(draft.id(), correction.supersedesDocumentId());
        assertEquals("SUPERSEDED", jdbc.queryForObject(
                "select status from hrm_appointment_document where id=?", String.class, draft.id()));
    }

    @Test
    void assumptionDateMustMatchAuthoritativeAppointment() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create("agency", 8L, new CreateDocumentCommand(
                        DocumentKind.ASSUMPTION_TO_DUTY, LocalDate.of(2026, 8, 5), null,
                        LocalDate.of(2026, 8, 6), "Zamboanga City", null, 9L, 9L,
                        null, "Prepared"), "encoder", "corr"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private CreateDocumentCommand oath(String supersedes) {
        return new CreateDocumentCommand(DocumentKind.OATH_OF_OFFICE,
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null,
                "Zamboanga City", 9L, null, null, supersedes,
                supersedes == null ? "Prepared" : "Corrected venue metadata");
    }

    private void schema() {
        jdbc.execute("create table hrm_appointment_provenance(agency_id varchar(64),employee_appointment_id bigint,onboarding_case_id varchar(36))");
        jdbc.execute("create table hrm_onboarding_case(id varchar(36),agency_id varchar(64),status varchar(30))");
        jdbc.execute("insert into hrm_onboarding_case values('case-1','agency','APPOINTMENT_CREATED')");
        jdbc.execute("insert into hrm_appointment_provenance values('agency',8,'case-1')");
        jdbc.execute("create table hrm_appointment_document(id varchar(36) primary key,agency_id varchar(64),appointment_id bigint,onboarding_case_id varchar(36),document_kind varchar(30),status varchar(20),official_template_code varchar(80),official_template_version varchar(40),official_template_checksum varchar(64),source_snapshot varchar(12000),source_fingerprint varchar(64),oath_date date,assumption_date date,issue_date date,venue varchar(300),administering_employee_id bigint,administering_name varchar(300),administering_position varchar(300),certifying_employee_id bigint,certifying_name varchar(300),certifying_position varchar(300),attesting_employee_id bigint,attesting_name varchar(300),attesting_position varchar(300),supersedes_id varchar(36),record_version bigint,created_by varchar(100),created_at timestamp,updated_by varchar(100),updated_at timestamp,finalized_by varchar(100),finalized_at timestamp,superseded_by varchar(100),superseded_at timestamp)");
        jdbc.execute("create table hrm_appointment_document_audit_event(id varchar(36),agency_id varchar(64),document_id varchar(36),subject_id varchar(100),action_code varchar(80),actor varchar(100),reason varchar(500),before_metadata varchar(1000),after_metadata varchar(1000),correlation_id varchar(100),occurred_at timestamp)");
    }
}

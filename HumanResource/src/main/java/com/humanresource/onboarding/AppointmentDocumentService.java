package com.humanresource.onboarding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.PersonnelActionReportData;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.impl.EmployeeAppointmentReportDataLoader;
import com.humanresource.onboarding.AppointmentDocumentDtos.*;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.reports.JasperReportRegistry;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
import microsoft.sql.DateTimeOffset;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AppointmentDocumentService {
    static final String OATH_CODE = "CSC-CS-FORM-32";
    static final String ASSUMPTION_CODE = "CSC-CS-FORM-4";
    static final String FORM_VERSION = "REVISED-2025";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")
            .withZone(ZoneId.of("Asia/Manila"));

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final EmployeeAppointmentRepository appointments;
    private final EmployeeRepository employees;
    private final PersonalDataRepository personalData;
    private final EmployeeAppointmentReportDataLoader appointmentLoader;
    private final AppointmentDocumentReportRenderer renderer;

    public AppointmentDocumentService(JdbcTemplate jdbc, ObjectMapper json,
                                      EmployeeAppointmentRepository appointments,
                                      EmployeeRepository employees,
                                      PersonalDataRepository personalData,
                                      EmployeeAppointmentReportDataLoader appointmentLoader,
                                      AppointmentDocumentReportRenderer renderer) {
        this.jdbc = jdbc;
        this.json = json;
        this.appointments = appointments;
        this.employees = employees;
        this.personalData = personalData;
        this.appointmentLoader = appointmentLoader;
        this.renderer = renderer;
    }

    @Transactional
    public DocumentResponse create(String agency, long appointmentId, CreateDocumentCommand command,
                                   String actor, String correlationId) {
        EmployeeAppointment appointment = appointment(appointmentId);
        String intakeId = intake(agency, appointmentId);
        validate(command.kind(), command.issueDate(), command.oathDate(), command.assumptionDate(),
                command.venue(), command.administeringEmployeeId(), command.certifyingEmployeeId(),
                command.attestingEmployeeId(), appointment);

        if (currentCount(agency, appointmentId, command.kind()) > 0) {
            if (blank(command.supersedesDocumentId())) {
                throw conflict("A current document of this kind already exists");
            }
            Map<String, Object> predecessor =
                    requireDocument(agency, appointmentId, command.supersedesDocumentId());
            if (!"FINALIZED".equals(text(predecessor, "status"))
                    || !command.kind().name().equals(text(predecessor, "document_kind"))) {
                throw conflict("Only the current finalized document may be corrected");
            }
            Instant supersededAt = Instant.now();
            int changed = jdbc.update(
                    "UPDATE hrm_appointment_document SET status='SUPERSEDED',superseded_by=?,"
                            + "superseded_at=?,updated_by=?,updated_at=?,record_version=record_version+1 "
                            + "WHERE id=? AND status='FINALIZED'",
                    actor, timestamp(supersededAt), actor, timestamp(supersededAt),
                    command.supersedesDocumentId());
            if (changed != 1) throw conflict("The predecessor document changed");
            audit(agency, command.supersedesDocumentId(), command.supersedesDocumentId(),
                    "SUPERSEDE", actor, command.reason(), "FINALIZED", "SUPERSEDED", correlationId);
        } else if (!blank(command.supersedesDocumentId())) {
            throw conflict("A correction must supersede the current finalized document");
        }

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        jdbc.update(
                "INSERT INTO hrm_appointment_document("
                        + "id,agency_id,appointment_id,onboarding_case_id,document_kind,status,"
                        + "official_template_code,official_template_version,official_template_checksum,"
                        + "oath_date,assumption_date,issue_date,venue,administering_employee_id,"
                        + "certifying_employee_id,attesting_employee_id,supersedes_id,record_version,"
                        + "created_by,created_at,updated_by,updated_at) "
                        + "VALUES(?,?,?,?,?,'DRAFT',?,?,?,?,?,?,?,?,?,?,?,0,?,?,?,?)",
                id, agency, appointmentId, intakeId, command.kind().name(), code(command.kind()),
                FORM_VERSION, templateChecksum(command.kind()), command.oathDate(),
                command.assumptionDate(), command.issueDate(), command.venue().trim(),
                command.administeringEmployeeId(), command.certifyingEmployeeId(),
                command.attestingEmployeeId(),
                blank(command.supersedesDocumentId()) ? null : command.supersedesDocumentId(),
                actor, timestamp(now), actor, timestamp(now));
        audit(agency, id, id, "CREATE_DRAFT", actor, command.reason(), null, "DRAFT", correlationId);
        return response(requireDocument(agency, appointmentId, id));
    }

    @Transactional
    public DocumentResponse update(String agency, long appointmentId, String documentId,
                                   UpdateDocumentCommand command, String actor, String correlationId) {
        Map<String, Object> current = requireDocument(agency, appointmentId, documentId);
        if (!"DRAFT".equals(text(current, "status"))) throw conflict("Finalized documents are immutable");
        if (longValue(current, "record_version") != command.recordVersion()) {
            throw conflict("Appointment document record version changed");
        }
        DocumentKind kind = DocumentKind.valueOf(text(current, "document_kind"));
        EmployeeAppointment appointment = appointment(appointmentId);
        validate(kind, command.issueDate(), command.oathDate(), command.assumptionDate(), command.venue(),
                command.administeringEmployeeId(), command.certifyingEmployeeId(),
                command.attestingEmployeeId(), appointment);
        int changed = jdbc.update(
                "UPDATE hrm_appointment_document SET oath_date=?,assumption_date=?,issue_date=?,venue=?,"
                        + "administering_employee_id=?,certifying_employee_id=?,attesting_employee_id=?,"
                        + "record_version=record_version+1,updated_by=?,updated_at=? "
                        + "WHERE id=? AND agency_id=? AND appointment_id=? AND status='DRAFT' AND record_version=?",
                command.oathDate(), command.assumptionDate(), command.issueDate(), command.venue().trim(),
                command.administeringEmployeeId(), command.certifyingEmployeeId(),
                command.attestingEmployeeId(), actor, timestamp(Instant.now()), documentId, agency,
                appointmentId, command.recordVersion());
        if (changed != 1) throw conflict("Appointment document changed");
        audit(agency, documentId, documentId, "UPDATE_DRAFT", actor, null,
                metadata(current), "DRAFT", correlationId);
        return response(requireDocument(agency, appointmentId, documentId));
    }

    @Transactional
    public DocumentResponse finalizeDocument(String agency, long appointmentId, String documentId,
                                             FinalizeDocumentCommand command, String actor,
                                             String correlationId) {
        Map<String, Object> current = requireDocument(agency, appointmentId, documentId);
        if (!"DRAFT".equals(text(current, "status"))) throw conflict("Only a draft document may be finalized");
        if (longValue(current, "record_version") != command.recordVersion()) {
            throw conflict("Appointment document record version changed");
        }
        DocumentKind kind = DocumentKind.valueOf(text(current, "document_kind"));
        EmployeeAppointment appointment = appointment(appointmentId);
        validate(kind, localDate(current, "issue_date"), localDate(current, "oath_date"),
                localDate(current, "assumption_date"), text(current, "venue"),
                longOrNull(current, "administering_employee_id"),
                longOrNull(current, "certifying_employee_id"),
                longOrNull(current, "attesting_employee_id"), appointment);

        AppointmentReportData.LegalDocument snapshot = legalSnapshot(current, appointment, actor);
        String fingerprint = sha(write(snapshot));
        snapshot = withFingerprint(snapshot, fingerprint);
        String snapshotJson = write(snapshot);
        Instant now = Instant.now();
        int changed = jdbc.update(
                "UPDATE hrm_appointment_document SET status='FINALIZED',source_snapshot=?,source_fingerprint=?,"
                        + "administering_name=?,administering_position=?,certifying_name=?,certifying_position=?,"
                        + "attesting_name=?,attesting_position=?,finalized_by=?,finalized_at=?,"
                        + "updated_by=?,updated_at=?,record_version=record_version+1 "
                        + "WHERE id=? AND agency_id=? AND appointment_id=? AND status='DRAFT' AND record_version=?",
                snapshotJson, fingerprint, snapshot.administeringName(), snapshot.administeringPosition(),
                snapshot.certifyingName(), snapshot.certifyingPosition(), snapshot.attestingName(),
                snapshot.attestingPosition(), actor, timestamp(now), actor, timestamp(now),
                documentId, agency, appointmentId, command.recordVersion());
        if (changed != 1) throw conflict("Appointment document changed");
        audit(agency, documentId, documentId, "FINALIZE", actor, command.reason(),
                "DRAFT", "FINALIZED:" + fingerprint, correlationId);
        return response(requireDocument(agency, appointmentId, documentId));
    }

    @Transactional
    public byte[] renderDocument(String agency, long appointmentId, String documentId,
                                 String actor, String correlationId) {
        Map<String, Object> document = requireDocument(agency, appointmentId, documentId);
        if (!"FINALIZED".equals(text(document, "status"))
                && !"SUPERSEDED".equals(text(document, "status"))) {
            throw conflict("Only a finalized document may be rendered");
        }
        String snapshotJson = text(document, "source_snapshot");
        try {
            AppointmentReportData.LegalDocument data =
                    json.readValue(snapshotJson, AppointmentReportData.LegalDocument.class);
            if (!sha(write(withFingerprint(data, ""))).equals(text(document, "source_fingerprint"))
                    || !data.sourceFingerprint().equals(text(document, "source_fingerprint"))) {
                throw conflict("Finalized document source fingerprint is stale");
            }
            byte[] pdf = DocumentKind.OATH_OF_OFFICE.name().equals(text(document, "document_kind"))
                    ? renderer.oath(data) : renderer.assumption(data);
            audit(agency, documentId, documentId, "GENERATE_PDF", actor, null,
                    text(document, "source_fingerprint"),
                    "PDF:" + sha(pdf) + ":" + pdf.length, correlationId);
            return pdf;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read finalized appointment document", exception);
        }
    }

    @Transactional
    public byte[] onboardingCompletion(String agency, String intakeId, String actor,
                                       String correlationId) {
        Map<String, Object> intake = one(
                "SELECT * FROM hrm_onboarding_case WHERE id=? AND agency_id=?", intakeId, agency);
        if (intake == null) throw notFound("Onboarding intake was not found");
        if (!"COMPLETED".equals(text(intake, "status"))) {
            throw conflict("Onboarding Completion Record requires a COMPLETED intake");
        }
        Map<String, Object> receipt = one(
                "SELECT handoff_id,selection_id,application_id FROM rsp_appointment_handoff_receipt "
                        + "WHERE id=?", text(intake, "handoff_receipt_id"));
        PersonnelActionReportData appointmentData =
                appointmentLoader.load(longValue(intake, "appointment_id"));
        List<AppointmentReportData.CompletionRow> rows = new ArrayList<>();
        int number = 1;
        for (Map<String, Object> item : jdbc.queryForList(
                "SELECT item_code,label,status,evidence_fingerprint,completed_by,verified_by "
                        + "FROM hrm_onboarding_item WHERE onboarding_case_id=? "
                        + "ORDER BY display_order,item_code", intakeId)) {
            rows.add(new AppointmentReportData.CompletionRow(number++, text(item, "item_code"),
                    text(item, "label"), text(item, "status"), nullable(item, "evidence_fingerprint"),
                    nullable(item, "completed_by"), nullable(item, "verified_by")));
        }
        if (rows.isEmpty()) throw conflict("Completed onboarding has no checklist snapshot");
        String source = write(Map.of(
                "intakeId", intakeId,
                "recordVersion", longValue(intake, "record_version"),
                "sourceFingerprint", text(intake, "source_fingerprint"),
                "items", rows));
        AppointmentReportData.OnboardingCompletion data =
                new AppointmentReportData.OnboardingCompletion(
                        appointmentData.getCompanyName(), appointmentData.getCompanyAddress(), intakeId,
                        text(receipt, "handoff_id"), text(receipt, "selection_id"),
                        text(receipt, "application_id"),
                        longValue(intake, "employee_id") + " / " + appointmentData.getEmployeeName(),
                        String.valueOf(longValue(intake, "appointment_id")),
                        text(intake, "template_id") + " v" + longValue(intake, "template_version"),
                        time(instant(intake, "updated_at")), sha(source), generated(actor), rows);
        byte[] pdf = renderer.onboarding(data);
        jdbc.update(
                "INSERT INTO hrm_onboarding_audit_event(id,agency_id,onboarding_case_id,action_code,"
                        + "actor,safe_summary,occurred_at) VALUES(?,?,?,?,?,?,?)",
                UUID.randomUUID().toString(), agency, intakeId, "GENERATE_COMPLETION_REPORT", actor,
                "source=" + data.sourceFingerprint() + ";output=" + sha(pdf) + ";bytes=" + pdf.length,
                timestamp(Instant.now()));
        return pdf;
    }

    @Transactional
    public void auditDenied(String agency, String subjectId, String feature, String actor,
                            String correlationId) {
        audit(agency, null, subjectId, "DENY_REPORT", actor, feature,
                null, "DENIED", correlationId);
    }

    private AppointmentReportData.LegalDocument legalSnapshot(
            Map<String, Object> document, EmployeeAppointment appointment, String actor) {
        PersonnelActionReportData r = appointmentLoader.load(appointment.getEmployeeAppointmentId());
        PersonalData p = personalData.findByEmployeeId(appointment.getEmployeeId());
        Employee oathOfficer = employee(longOrNull(document, "administering_employee_id"));
        Employee head = employee(longOrNull(document, "certifying_employee_id"));
        Employee hrmo = employee(longOrNull(document, "attesting_employee_id"));
        return new AppointmentReportData.LegalDocument(
                r.getCompanyName(), r.getCompanyAddress(), r.getEmployeeName(),
                p == null ? "" : first(p.getPermAddress(), p.getResAddress()),
                r.getToPosition(), first(r.getToSection(), r.getToDivision()),
                p == null ? "" : safe(p.getGovIdType()),
                p == null ? "" : safe(p.getGovIdNumber()),
                p == null || p.getGovIdDate() == null ? "" : DATE.format(p.getGovIdDate().toLocalDate()),
                format(localDate(document, "issue_date")),
                format(localDate(document, "oath_date")),
                format(localDate(document, "assumption_date")),
                text(document, "venue"),
                name(oathOfficer), position(oathOfficer),
                name(head), position(head),
                name(hrmo), position(hrmo),
                text(document, "official_template_code"),
                text(document, "official_template_version"),
                "", generated(actor));
    }

    private static AppointmentReportData.LegalDocument withFingerprint(
            AppointmentReportData.LegalDocument d, String fingerprint) {
        return new AppointmentReportData.LegalDocument(
                d.agencyName(), d.agencyAddress(), d.appointeeName(), d.appointeeAddress(),
                d.position(), d.office(), d.governmentIdType(), d.governmentIdNumber(),
                d.governmentIdDate(), d.issueDate(), d.oathDate(), d.assumptionDate(),
                d.venue(), d.administeringName(), d.administeringPosition(),
                d.certifyingName(), d.certifyingPosition(), d.attestingName(),
                d.attestingPosition(), d.templateCode(), d.templateVersion(),
                fingerprint, d.generated());
    }

    private void validate(DocumentKind kind, LocalDate issueDate, LocalDate oathDate,
                          LocalDate assumptionDate, String venue, Long administeringId,
                          Long certifyingId, Long attestingId, EmployeeAppointment appointment) {
        if (blank(venue)) throw bad("Venue is required");
        LocalDate appointmentIssued = appointment.getAppointmentIssuedDate().toLocalDate();
        LocalDate appointedAssumption = appointment.getAssumptionToDutyDate().toLocalDate();
        if (issueDate.isBefore(appointmentIssued)) {
            throw bad("Document issue date cannot precede the appointment issue date");
        }
        if (kind == DocumentKind.OATH_OF_OFFICE) {
            if (oathDate == null || oathDate.isBefore(appointmentIssued)) {
                throw bad("A valid oath date is required");
            }
            if (administeringId == null) throw bad("The officer administering the oath is required");
            if (assumptionDate != null || certifyingId != null || attestingId != null) {
                throw bad("Assumption certification fields do not apply to an Oath of Office");
            }
            employee(administeringId);
        } else {
            if (assumptionDate == null || !assumptionDate.equals(appointedAssumption)) {
                throw bad("Assumption date must equal the authoritative appointment assumption date");
            }
            if (certifyingId == null || attestingId == null) {
                throw bad("Head of Office and attesting HRMO are required");
            }
            if (oathDate != null || administeringId != null) {
                throw bad("Oath fields do not apply to an assumption certification");
            }
            employee(certifyingId);
            employee(attestingId);
        }
    }

    private EmployeeAppointment appointment(long appointmentId) {
        return appointments.findById(appointmentId)
                .orElseThrow(() -> notFound("Employee appointment was not found"));
    }

    private Employee employee(Long employeeId) {
        if (employeeId == null) return null;
        return employees.findById(employeeId)
                .orElseThrow(() -> bad("A selected signatory employee was not found"));
    }

    private String intake(String agency, long appointmentId) {
        Map<String, Object> row = one(
                "SELECT onboarding_case_id FROM hrm_appointment_provenance "
                        + "WHERE agency_id=? AND employee_appointment_id=?", agency, appointmentId);
        if (row == null) throw conflict("Appointment has no authoritative onboarding provenance");
        Map<String, Object> intake = one(
                "SELECT status FROM hrm_onboarding_case WHERE id=? AND agency_id=?",
                text(row, "onboarding_case_id"), agency);
        if (intake == null || !Set.of("APPOINTMENT_CREATED", "COMPLETED")
                .contains(text(intake, "status"))) {
            throw conflict("Appointment onboarding source is not valid");
        }
        return text(row, "onboarding_case_id");
    }

    private int currentCount(String agency, long appointmentId, DocumentKind kind) {
        Number count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hrm_appointment_document WHERE agency_id=? "
                        + "AND appointment_id=? AND document_kind=? AND status IN ('DRAFT','FINALIZED')",
                Number.class, agency, appointmentId, kind.name());
        return count == null ? 0 : count.intValue();
    }

    private Map<String, Object> requireDocument(String agency, long appointmentId, String documentId) {
        Map<String, Object> row = one(
                "SELECT * FROM hrm_appointment_document WHERE id=? AND agency_id=? AND appointment_id=?",
                documentId, agency, appointmentId);
        if (row == null) throw notFound("Appointment document was not found");
        return row;
    }

    private DocumentResponse response(Map<String, Object> row) {
        return new DocumentResponse(
                text(row, "id"), longValue(row, "appointment_id"),
                text(row, "onboarding_case_id"),
                DocumentKind.valueOf(text(row, "document_kind")),
                text(row, "status"), text(row, "official_template_code"),
                text(row, "official_template_version"), localDate(row, "issue_date"),
                localDate(row, "oath_date"), localDate(row, "assumption_date"),
                text(row, "venue"), longOrNull(row, "administering_employee_id"),
                longOrNull(row, "certifying_employee_id"),
                longOrNull(row, "attesting_employee_id"),
                nullable(row, "supersedes_id"), nullable(row, "source_fingerprint"),
                longValue(row, "record_version"),
                nullable(row, "finalized_by"), instantOrNull(row, "finalized_at"));
    }

    private void audit(String agency, String documentId, String subjectId, String action,
                       String actor, String reason, String before, String after,
                       String correlationId) {
        jdbc.update(
                "INSERT INTO hrm_appointment_document_audit_event("
                        + "id,agency_id,document_id,subject_id,action_code,actor,reason,"
                        + "before_metadata,after_metadata,correlation_id,occurred_at) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                UUID.randomUUID().toString(), agency, documentId, subjectId, action, actor,
                reason, before, after, correlationId, timestamp(Instant.now()));
    }

    private String templateChecksum(DocumentKind kind) {
        String path = kind == DocumentKind.OATH_OF_OFFICE
                ? "reports/csc_oath_of_office_2025.jrxml"
                : "reports/csc_assumption_to_duty_2025.jrxml";
        try {
            return sha(JasperReportRegistry.bytes(path));
        } catch (Exception exception) {
            throw new IllegalStateException("Official CSC template is unavailable", exception);
        }
    }

    private static String code(DocumentKind kind) {
        return kind == DocumentKind.OATH_OF_OFFICE ? OATH_CODE : ASSUMPTION_CODE;
    }

    private Map<String, Object> one(String sql, Object... arguments) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, arguments);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String metadata(Map<String, Object> row) {
        return text(row, "status") + ":v" + longValue(row, "record_version");
    }

    private static String name(Employee employee) {
        if (employee == null) return "";
        String suffix = blank(employee.getSuffix()) ? "" : " " + employee.getSuffix().trim();
        return (safe(employee.getFirstname()) + " " + safe(employee.getLastname()) + suffix).trim();
    }

    private static String position(Employee employee) {
        return employee == null ? "" : safe(employee.getPosition());
    }

    private static String first(String first, String second) {
        return blank(first) ? safe(second) : first.trim();
    }

    private static String format(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    private static String generated(String actor) {
        return safe(actor) + " at " + TIME.format(Instant.now()) + "; Asia/Manila";
    }

    private static String time(Instant value) {
        return value == null ? "" : TIME.format(value);
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException(key + " is missing");
        }
        return value.toString();
    }

    private static String nullable(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString();
    }

    private static long longValue(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private static Long longOrNull(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    private static LocalDate localDate(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date date) return date.toLocalDate();
        if (value instanceof LocalDate date) return date;
        return LocalDate.parse(value.toString());
    }

    private static Instant instant(Map<String, Object> row, String key) {
        Instant value = instantOrNull(row, key);
        if (value == null) throw new IllegalStateException(key + " is missing");
        return value;
    }

    private static Instant instantOrNull(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof DateTimeOffset offset) return offset.getOffsetDateTime().toInstant();
        if (value instanceof OffsetDateTime offset) return offset.toInstant();
        if (value instanceof LocalDateTime local) return local.atZone(ZoneId.systemDefault()).toInstant();
        return Instant.parse(value.toString());
    }

    private static Timestamp timestamp(Instant value) {
        return Timestamp.from(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String sha(String value) {
        return sha(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}

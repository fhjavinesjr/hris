package com.humanresource.onboarding;

import com.humanresource.onboarding.AppointmentDocumentDtos.*;
import com.humanresource.onboarding.HrmPermissionGuard.Action;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hrm/v1")
public class AppointmentDocumentController {
    public static final String APPOINTMENT_REPORT = "hrm.appointment-report";
    public static final String ONBOARDING_REPORT = "hrm.onboarding-report";
    public static final String APPOINTMENT_DOCUMENTS = "hrm.appointment-documents";

    private final AppointmentDocumentService service;
    private final HrmPermissionGuard permissions;

    public AppointmentDocumentController(AppointmentDocumentService service,
                                         HrmPermissionGuard permissions) {
        this.service = service;
        this.permissions = permissions;
    }

    @PostMapping("/appointments/{appointmentId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse create(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader("X-Agency-Id") String agency,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable long appointmentId,
            @Valid @RequestBody CreateDocumentCommand command) {
        authorize(token, agency, appointmentId + "", APPOINTMENT_DOCUMENTS, Action.ADD,
                authentication, correlationId);
        return service.create(agency, appointmentId, command, authentication.getName(), correlationId);
    }

    @PutMapping("/appointments/{appointmentId}/documents/{documentId}")
    public DocumentResponse update(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader("X-Agency-Id") String agency,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable long appointmentId,
            @PathVariable String documentId,
            @Valid @RequestBody UpdateDocumentCommand command) {
        authorize(token, agency, documentId, APPOINTMENT_DOCUMENTS, Action.EDIT,
                authentication, correlationId);
        return service.update(agency, appointmentId, documentId, command,
                authentication.getName(), correlationId);
    }

    @PostMapping("/appointments/{appointmentId}/documents/{documentId}/finalize")
    public DocumentResponse finalizeDocument(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader("X-Agency-Id") String agency,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable long appointmentId,
            @PathVariable String documentId,
            @Valid @RequestBody FinalizeDocumentCommand command) {
        authorize(token, agency, documentId, APPOINTMENT_DOCUMENTS, Action.FINALIZE,
                authentication, correlationId);
        return service.finalizeDocument(agency, appointmentId, documentId, command,
                authentication.getName(), correlationId);
    }

    @GetMapping(value = "/appointments/{appointmentId}/documents/{documentId}.pdf",
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> documentPdf(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader("X-Agency-Id") String agency,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable long appointmentId,
            @PathVariable String documentId) {
        authorize(token, agency, documentId, APPOINTMENT_DOCUMENTS, Action.ACCESS,
                authentication, correlationId);
        return pdf("AppointmentDocument_" + documentId + ".pdf",
                service.renderDocument(agency, appointmentId, documentId,
                        authentication.getName(), correlationId));
    }

    @GetMapping(value = "/appointment-intakes/{intakeId}/onboarding-completion.pdf",
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> onboardingPdf(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader("X-Agency-Id") String agency,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable String intakeId) {
        authorize(token, agency, intakeId, ONBOARDING_REPORT, Action.ACCESS,
                authentication, correlationId);
        return pdf("OnboardingCompletion_" + intakeId + ".pdf",
                service.onboardingCompletion(agency, intakeId, authentication.getName(), correlationId));
    }

    private void authorize(String token, String agency, String subject, String feature, Action action,
                           Authentication authentication, String correlationId) {
        try {
            permissions.require(token, feature, action);
        } catch (AccessDeniedException denied) {
            service.auditDenied(agency, subject, feature, authentication.getName(), correlationId);
            throw denied;
        }
    }

    private static ResponseEntity<byte[]> pdf(String fileName, byte[] body) {
        String safe = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(safe).build().toString())
                .body(body);
    }
}

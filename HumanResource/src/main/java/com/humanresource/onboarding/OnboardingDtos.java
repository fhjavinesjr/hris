package com.humanresource.onboarding;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public final class OnboardingDtos {
    private OnboardingDtos(){}
    public record TemplateItem(@NotBlank String code,@NotBlank String label,String instructions,boolean required,
            boolean evidenceRequired,String evidenceClassification,String retentionTag,@NotBlank String responsibleRole,
            @PositiveOrZero int displayOrder,@NotBlank String completionRule){}
    public record TemplateCommand(@NotBlank String code,LocalDate effectiveFrom,LocalDate effectiveTo,@NotEmpty List<@Valid TemplateItem> items){}
    public record TemplateResponse(String id,String code,int definitionVersion,String status,LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion){}
    public record VersionCommand(@PositiveOrZero long recordVersion){}
    public record ReasonCommand(@NotBlank String reason,@PositiveOrZero long recordVersion){}
    public record IdentityCommand(@NotBlank String decision,Long employeeId,String employeeNo,String biometricNo,String role,@PositiveOrZero long recordVersion){}
    public record ChecklistCommand(@NotBlank String status,String evidenceReference,String evidenceFingerprint,@PositiveOrZero long recordVersion){}
    public record AppointmentCommand(@NotNull Long natureOfAppointmentId,@NotNull LocalDateTime appointmentIssuedDate,@NotNull LocalDateTime assumptionToDutyDate,@NotBlank String details,@PositiveOrZero long recordVersion){}
    public record ActivationCommand(@NotBlank String token,@Size(min=10,max=200) String password){}
    public record ItemResponse(String id,String code,String label,boolean required,boolean evidenceRequired,String status,String evidenceReference,String completedBy,String verifiedBy,long recordVersion){}
    public record IntakeResponse(String id,String handoffId,String selectionId,String applicationId,String applicantId,String status,String identityDecision,Long employeeId,String employeeNo,Long appointmentId,long recordVersion,List<ItemResponse> items){}
    public record AppointmentResult(String intakeId,Long employeeId,Long appointmentId,String status,String activationToken,Instant activationExpiresAt,long recordVersion){}
}

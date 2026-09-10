DO $$
BEGIN
 IF to_regclass('"${hrmSchema}".employeeappointment') IS NOT NULL AND EXISTS (
  SELECT 1 FROM "${hrmSchema}".employeeappointment WHERE activeAppointment=TRUE GROUP BY employeeId HAVING COUNT(*)>1
 ) THEN RAISE EXCEPTION 'Phase 5E preflight: duplicate active appointments exist for an employee'; END IF;
 IF to_regclass('"${hrmSchema}".employeeappointment') IS NOT NULL AND EXISTS (
  SELECT 1 FROM "${hrmSchema}".employeeappointment WHERE activeAppointment=TRUE GROUP BY plantillaId HAVING COUNT(*)>1
 ) THEN RAISE EXCEPTION 'Phase 5E preflight: duplicate active Plantilla occupants exist'; END IF;
END $$;

CREATE TABLE "${hrmSchema}".hrm_onboarding_template (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, template_code VARCHAR(80) NOT NULL,
 definition_version INTEGER NOT NULL, status VARCHAR(20) NOT NULL, effective_from DATE, effective_to DATE,
 published_at TIMESTAMP WITH TIME ZONE, superseded_at TIMESTAMP WITH TIME ZONE,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_hrm_onboarding_template_version UNIQUE(agency_id,template_code,definition_version),
 CONSTRAINT ck_hrm_onboarding_template_status CHECK(status IN ('DRAFT','PUBLISHED','SUPERSEDED'))
);
CREATE UNIQUE INDEX uk_hrm_onboarding_template_current ON "${hrmSchema}".hrm_onboarding_template(agency_id,template_code) WHERE status='PUBLISHED';
CREATE TABLE "${hrmSchema}".hrm_onboarding_template_item (
 id VARCHAR(36) PRIMARY KEY, template_id VARCHAR(36) NOT NULL REFERENCES "${hrmSchema}".hrm_onboarding_template(id),
 item_code VARCHAR(80) NOT NULL, label VARCHAR(200) NOT NULL, instructions TEXT,
 required_item BOOLEAN NOT NULL, evidence_required BOOLEAN NOT NULL, evidence_classification VARCHAR(40),
 retention_tag VARCHAR(80), responsible_role VARCHAR(80) NOT NULL, display_order INTEGER NOT NULL,
 completion_rule VARCHAR(40) NOT NULL, CONSTRAINT uk_hrm_onboarding_template_item UNIQUE(template_id,item_code)
);
CREATE TABLE "${hrmSchema}".hrm_onboarding_case (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, handoff_receipt_id VARCHAR(36) NOT NULL REFERENCES "${hrmSchema}".rsp_appointment_handoff_receipt(id),
 handoff_id VARCHAR(36) NOT NULL, template_id VARCHAR(36) NOT NULL REFERENCES "${hrmSchema}".hrm_onboarding_template(id),
 template_version INTEGER NOT NULL, template_snapshot TEXT NOT NULL, status VARCHAR(30) NOT NULL,
 identity_decision VARCHAR(30), employee_id BIGINT, proposed_employee_no VARCHAR(100), proposed_biometric_no VARCHAR(100),
 proposed_role VARCHAR(50), return_reason VARCHAR(500), cancellation_reason VARCHAR(500),
 appointment_id BIGINT, source_fingerprint VARCHAR(64) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_hrm_onboarding_handoff UNIQUE(agency_id,handoff_id),
 CONSTRAINT ck_hrm_onboarding_status CHECK(status IN ('RECEIVED','IN_REVIEW','RETURNED','READY_FOR_APPOINTMENT','APPOINTMENT_CREATED','COMPLETED','CANCELLED')),
 CONSTRAINT ck_hrm_identity_decision CHECK(identity_decision IS NULL OR identity_decision IN ('LINK_EXISTING_EMPLOYEE','CREATE_NEW_EMPLOYEE'))
);
CREATE TABLE "${hrmSchema}".hrm_onboarding_item (
 id VARCHAR(36) PRIMARY KEY, onboarding_case_id VARCHAR(36) NOT NULL REFERENCES "${hrmSchema}".hrm_onboarding_case(id),
 template_item_id VARCHAR(36) NOT NULL, item_code VARCHAR(80) NOT NULL, label VARCHAR(200) NOT NULL,
 required_item BOOLEAN NOT NULL, evidence_required BOOLEAN NOT NULL, evidence_classification VARCHAR(40), retention_tag VARCHAR(80),
 responsible_role VARCHAR(80) NOT NULL, display_order INTEGER NOT NULL, completion_rule VARCHAR(40) NOT NULL,
 status VARCHAR(20) NOT NULL, evidence_reference VARCHAR(500), evidence_fingerprint VARCHAR(64),
 completed_by VARCHAR(100), completed_at TIMESTAMP WITH TIME ZONE, verified_by VARCHAR(100), verified_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0, CONSTRAINT uk_hrm_onboarding_case_item UNIQUE(onboarding_case_id,item_code),
 CONSTRAINT ck_hrm_onboarding_item_status CHECK(status IN ('PENDING','SUBMITTED','VERIFIED','REJECTED','WAIVED'))
);
CREATE TABLE "${hrmSchema}".hrm_appointment_provenance (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, onboarding_case_id VARCHAR(36) NOT NULL REFERENCES "${hrmSchema}".hrm_onboarding_case(id),
 handoff_id VARCHAR(36) NOT NULL, selection_id VARCHAR(36) NOT NULL, application_id VARCHAR(36) NOT NULL,
 employee_id BIGINT NOT NULL, employee_appointment_id BIGINT NOT NULL, plantilla_id INTEGER NOT NULL,
 source_fingerprint VARCHAR(64) NOT NULL, administrative_fingerprint VARCHAR(64) NOT NULL,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_hrm_appointment_provenance_case UNIQUE(onboarding_case_id),
 CONSTRAINT uk_hrm_appointment_provenance_result UNIQUE(agency_id,handoff_id)
);
CREATE TABLE "${hrmSchema}".employee_activation_invitation (
 id VARCHAR(36) PRIMARY KEY, employee_id BIGINT NOT NULL, onboarding_case_id VARCHAR(36) NOT NULL REFERENCES "${hrmSchema}".hrm_onboarding_case(id),
 token_hash VARCHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
 consumed_at TIMESTAMP WITH TIME ZONE, revoked_at TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_hrm_activation_case UNIQUE(onboarding_case_id)
);
CREATE TABLE "${hrmSchema}".hrm_onboarding_audit_event (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, onboarding_case_id VARCHAR(36) NOT NULL,
 action_code VARCHAR(80) NOT NULL, actor VARCHAR(100) NOT NULL, safe_summary VARCHAR(500),
 occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_hrm_onboarding_audit_case ON "${hrmSchema}".hrm_onboarding_audit_event(agency_id,onboarding_case_id,occurred_at);
CREATE UNIQUE INDEX uk_hrm_active_appointment_employee ON "${hrmSchema}".employeeappointment(employeeId) WHERE activeAppointment=TRUE;
CREATE UNIQUE INDEX uk_hrm_active_appointment_plantilla ON "${hrmSchema}".employeeappointment(plantillaId) WHERE activeAppointment=TRUE;

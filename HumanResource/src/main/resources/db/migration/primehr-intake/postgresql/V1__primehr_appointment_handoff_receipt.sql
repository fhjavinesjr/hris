CREATE TABLE "${hrmSchema}".rsp_appointment_handoff_receipt (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, handoff_id VARCHAR(36) NOT NULL,
 schema_version INTEGER NOT NULL, source_fingerprint VARCHAR(64) NOT NULL, selection_id VARCHAR(36) NOT NULL,
 application_id VARCHAR(36) NOT NULL, applicant_id VARCHAR(36) NOT NULL, state VARCHAR(30) NOT NULL,
 payload_snapshot TEXT NOT NULL, received_at TIMESTAMP WITH TIME ZONE NOT NULL,
 correlation_id VARCHAR(100), source_actor VARCHAR(100) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_hrm_rsp_handoff UNIQUE(agency_id,handoff_id),
 CONSTRAINT uk_hrm_rsp_selection UNIQUE(agency_id,selection_id),
 CONSTRAINT ck_hrm_rsp_handoff_schema CHECK(schema_version>=1),
 CONSTRAINT ck_hrm_rsp_handoff_state CHECK(state IN ('RECEIVED','IN_REVIEW','APPOINTMENT_CREATED','COMPLETED','CANCELLED'))
);
CREATE INDEX ix_hrm_rsp_handoff_application ON "${hrmSchema}".rsp_appointment_handoff_receipt(agency_id,application_id,applicant_id);

CREATE TABLE "${primehrSchema}".rsp_appointment_handoff (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 selection_case_id VARCHAR(36) NOT NULL, application_id VARCHAR(36) NOT NULL, applicant_id VARCHAR(36) NOT NULL,
 schema_version INTEGER NOT NULL, handoff_revision INTEGER NOT NULL, current_selection_key VARCHAR(36),
 status VARCHAR(30) NOT NULL, payload_snapshot TEXT NOT NULL, payload_fingerprint VARCHAR(64) NOT NULL,
 correlation_id VARCHAR(100), receipt_id VARCHAR(36), receipt_state VARCHAR(30), receipt_record_version BIGINT,
 ready_at TIMESTAMP WITH TIME ZONE, sent_at TIMESTAMP WITH TIME ZONE, acknowledged_at TIMESTAMP WITH TIME ZONE,
 cancelled_at TIMESTAMP WITH TIME ZONE, closed_at TIMESTAMP WITH TIME ZONE, last_failure VARCHAR(1000),
 CONSTRAINT fk_rsp_handoff_selection FOREIGN KEY(selection_case_id) REFERENCES "${primehrSchema}".rsp_selection_case(id),
 CONSTRAINT fk_rsp_handoff_application FOREIGN KEY(application_id) REFERENCES "${primehrSchema}".rsp_position_application(id),
 CONSTRAINT uk_rsp_handoff_revision UNIQUE(agency_id,selection_case_id,handoff_revision),
 CONSTRAINT uk_rsp_handoff_current UNIQUE(agency_id,current_selection_key),
 CONSTRAINT ck_rsp_handoff_schema CHECK(schema_version>=1 AND handoff_revision>=1),
 CONSTRAINT ck_rsp_handoff_status CHECK(status IN ('DRAFT','READY','SENT','ACKNOWLEDGED','RETRYABLE_FAILURE','CANCELLED','CLOSED')),
 CONSTRAINT ck_rsp_handoff_receipt CHECK((status IN ('ACKNOWLEDGED','CLOSED') AND receipt_id IS NOT NULL AND acknowledged_at IS NOT NULL) OR status NOT IN ('ACKNOWLEDGED','CLOSED')),
 CONSTRAINT ck_rsp_handoff_terminal CHECK((status='CANCELLED' AND cancelled_at IS NOT NULL AND current_selection_key IS NULL) OR (status='CLOSED' AND closed_at IS NOT NULL AND current_selection_key IS NULL) OR status NOT IN ('CANCELLED','CLOSED'))
);
CREATE INDEX ix_rsp_handoff_status ON "${primehrSchema}".rsp_appointment_handoff(agency_id,status,updated_at);
CREATE INDEX ix_rsp_handoff_application ON "${primehrSchema}".rsp_appointment_handoff(agency_id,application_id,applicant_id);

CREATE TABLE "${primehrSchema}".rsp_appointment_handoff_attempt (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 handoff_id VARCHAR(36) NOT NULL, attempt_number INTEGER NOT NULL, attempted_at TIMESTAMP WITH TIME ZONE NOT NULL,
 endpoint_identity VARCHAR(500) NOT NULL, result_category VARCHAR(40) NOT NULL, http_status INTEGER,
 safe_diagnostic VARCHAR(1000), payload_fingerprint VARCHAR(64) NOT NULL,
 CONSTRAINT fk_rsp_handoff_attempt_handoff FOREIGN KEY(handoff_id) REFERENCES "${primehrSchema}".rsp_appointment_handoff(id),
 CONSTRAINT uk_rsp_handoff_attempt UNIQUE(agency_id,handoff_id,attempt_number),
 CONSTRAINT ck_rsp_handoff_attempt CHECK(attempt_number>=1 AND (http_status IS NULL OR (http_status>=100 AND http_status<=599)))
);
CREATE INDEX ix_rsp_handoff_attempt_time ON "${primehrSchema}".rsp_appointment_handoff_attempt(agency_id,handoff_id,attempted_at);

CREATE TABLE ${primehrSchema}.spms_commitment_route (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, commitment_version_id VARCHAR(36) NOT NULL,
 route_revision INT NOT NULL, request_code VARCHAR(100) NOT NULL, business_unit_id BIGINT NOT NULL,
 organization_fingerprint VARCHAR(64) NOT NULL, route_fingerprint VARCHAR(64) NOT NULL,
 submitted_content_fingerprint VARCHAR(64) NOT NULL, submitted_content_revision INT NOT NULL,
 current_level INT NOT NULL, status VARCHAR(20) NOT NULL, submitted_by VARCHAR(100) NOT NULL,
 submitted_at DATETIMEOFFSET NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_route_version FOREIGN KEY(commitment_version_id) REFERENCES ${primehrSchema}.spms_commitment_version(id),
 CONSTRAINT uk_spms_commitment_route_revision UNIQUE(agency_id,commitment_version_id,route_revision),
 CONSTRAINT ck_spms_commitment_route_revision CHECK(route_revision>0 AND submitted_content_revision>=0 AND current_level>0),
 CONSTRAINT ck_spms_commitment_route_status CHECK(status IN ('ACTIVE','RETURNED','COMPLETED','REJECTED','WITHDRAWN','SUPERSEDED','VOIDED')));
CREATE TABLE ${primehrSchema}.spms_commitment_route_step (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, route_id VARCHAR(36) NOT NULL,
 route_level INT NOT NULL, action_type VARCHAR(20) NOT NULL, employee_id BIGINT NOT NULL,
 employee_no VARCHAR(100) NOT NULL, employee_name VARCHAR(300) NOT NULL, appointment_id BIGINT NOT NULL,
 job_position_id BIGINT NOT NULL, plantilla_id BIGINT NOT NULL, participant_fingerprint VARCHAR(64) NOT NULL,
 organization_fingerprint VARCHAR(64) NOT NULL, status VARCHAR(20) NOT NULL, decided_by VARCHAR(100),
 decided_at DATETIMEOFFSET, decision_reason VARCHAR(2000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_route_step_route FOREIGN KEY(route_id) REFERENCES ${primehrSchema}.spms_commitment_route(id),
 CONSTRAINT uk_spms_commitment_route_step UNIQUE(agency_id,route_id,route_level),
 CONSTRAINT ck_spms_commitment_route_step_level CHECK(route_level>0),
 CONSTRAINT ck_spms_commitment_route_step_action CHECK(action_type IN ('RECOMMEND','APPROVE')),
 CONSTRAINT ck_spms_commitment_route_step_status CHECK(status IN ('PENDING','CURRENT','RECOMMENDED','RETURNED','APPROVED','REJECTED','SKIPPED')));
CREATE TABLE ${primehrSchema}.spms_commitment_action (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, commitment_version_id VARCHAR(36) NOT NULL,
 route_id VARCHAR(36), route_step_id VARCHAR(36), action_type VARCHAR(24) NOT NULL,
 request_key VARCHAR(100) NOT NULL, actor_employee_no VARCHAR(100) NOT NULL,
 prior_status VARCHAR(24) NOT NULL, result_status VARCHAR(24) NOT NULL,
 content_revision INT NOT NULL, lifecycle_revision INT NOT NULL, route_revision INT,
 action_reason VARCHAR(2000), override_milestone VARCHAR(80), acted_at DATETIMEOFFSET NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_action_version FOREIGN KEY(commitment_version_id) REFERENCES ${primehrSchema}.spms_commitment_version(id),
 CONSTRAINT fk_spms_commitment_action_route FOREIGN KEY(route_id) REFERENCES ${primehrSchema}.spms_commitment_route(id),
 CONSTRAINT fk_spms_commitment_action_step FOREIGN KEY(route_step_id) REFERENCES ${primehrSchema}.spms_commitment_route_step(id),
 CONSTRAINT uk_spms_commitment_action_request UNIQUE(agency_id,commitment_version_id,request_key),
 CONSTRAINT ck_spms_commitment_action_revision CHECK(content_revision>=0 AND lifecycle_revision>=0 AND (route_revision IS NULL OR route_revision>0)),
 CONSTRAINT ck_spms_commitment_action_type CHECK(action_type IN ('SUBMIT','RESUBMIT','WITHDRAW','RECOMMEND','RETURN','APPROVE','REJECT','AMEND','VOID','ROUTE_REBASE','WINDOW_OVERRIDE')));
CREATE INDEX ix_spms_commitment_route_current ON ${primehrSchema}.spms_commitment_route(agency_id,commitment_version_id,route_revision,status);
CREATE INDEX ix_spms_commitment_route_actor ON ${primehrSchema}.spms_commitment_route_step(agency_id,employee_no,status);
CREATE INDEX ix_spms_commitment_action_history ON ${primehrSchema}.spms_commitment_action(agency_id,commitment_version_id,acted_at);

CREATE TABLE "${primehrSchema}".spms_monitoring_case (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, commitment_version_id VARCHAR(36) NOT NULL,
 cycle_id VARCHAR(36) NOT NULL, policy_version_id VARCHAR(36) NOT NULL, form_type VARCHAR(20) NOT NULL,
 owner_employee_id BIGINT NOT NULL, owner_employee_no VARCHAR(100) NOT NULL, owner_name VARCHAR(300) NOT NULL,
 business_unit_id BIGINT NOT NULL, supervisor_employee_no VARCHAR(100) NOT NULL,
 route_fingerprint VARCHAR(64) NOT NULL, commitment_fingerprint VARCHAR(64) NOT NULL,
 status VARCHAR(32) NOT NULL, accomplishment_revision INTEGER NOT NULL DEFAULT 0,
 accomplishment_submitted_by VARCHAR(100), accomplishment_submitted_at TIMESTAMP WITH TIME ZONE,
 return_reason VARCHAR(2000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_monitoring_commitment FOREIGN KEY(commitment_version_id) REFERENCES "${primehrSchema}".spms_commitment_version(id),
 CONSTRAINT uk_spms_monitoring_commitment UNIQUE(agency_id,commitment_version_id),
 CONSTRAINT ck_spms_monitoring_case_status CHECK(status IN ('OPEN','ACCOMPLISHMENT_SUBMITTED','RETURNED','READY_FOR_RATING','VOIDED')),
 CONSTRAINT ck_spms_monitoring_accomplishment_revision CHECK(accomplishment_revision>=0));
CREATE TABLE "${primehrSchema}".spms_monitoring_update (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL,
 commitment_item_id VARCHAR(36) NOT NULL, root_update_id VARCHAR(36) NOT NULL, revision_no INTEGER NOT NULL,
 supersedes_id VARCHAR(36), reporting_date DATE NOT NULL, narrative_accomplishment VARCHAR(4000) NOT NULL,
 accomplished_value DECIMAL(19,6), progress_percent DECIMAL(7,4), issues_risks VARCHAR(3000),
 support_needed VARCHAR(3000), employee_remarks VARCHAR(3000), status VARCHAR(20) NOT NULL,
 submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_monitoring_update_case FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT fk_spms_monitoring_update_item FOREIGN KEY(commitment_item_id) REFERENCES "${primehrSchema}".spms_commitment_item(id),
 CONSTRAINT fk_spms_monitoring_update_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".spms_monitoring_update(id),
 CONSTRAINT uk_spms_monitoring_update_revision UNIQUE(agency_id,root_update_id,revision_no),
 CONSTRAINT ck_spms_monitoring_update_revision CHECK(revision_no>0),
 CONSTRAINT ck_spms_monitoring_progress CHECK(progress_percent IS NULL OR (progress_percent>=0 AND progress_percent<=100)),
 CONSTRAINT ck_spms_monitoring_update_status CHECK(status IN ('DRAFT','SUBMITTED','ACCEPTED','RETURNED','SUPERSEDED','VOIDED')));
CREATE TABLE "${primehrSchema}".spms_monitoring_feedback (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_update_id VARCHAR(36) NOT NULL,
 decision VARCHAR(20) NOT NULL, feedback VARCHAR(3000), actor_employee_no VARCHAR(100) NOT NULL,
 acted_at TIMESTAMP WITH TIME ZONE NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_monitoring_feedback_update FOREIGN KEY(monitoring_update_id) REFERENCES "${primehrSchema}".spms_monitoring_update(id),
 CONSTRAINT ck_spms_monitoring_feedback_decision CHECK(decision IN ('ACCEPT','RETURN')));
CREATE TABLE "${primehrSchema}".spms_monitoring_evidence (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL,
 monitoring_update_id VARCHAR(36) NOT NULL, commitment_item_id VARCHAR(36) NOT NULL,
 original_filename VARCHAR(255) NOT NULL, storage_provider VARCHAR(30) NOT NULL, storage_object_key VARCHAR(500) NOT NULL,
 media_type VARCHAR(120) NOT NULL, byte_size BIGINT NOT NULL, checksum VARCHAR(64) NOT NULL,
 confidentiality VARCHAR(30) NOT NULL, supersedes_id VARCHAR(36), status VARCHAR(20) NOT NULL,
 void_reason VARCHAR(2000), uploaded_by VARCHAR(100) NOT NULL, uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_monitoring_evidence_case FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT fk_spms_monitoring_evidence_update FOREIGN KEY(monitoring_update_id) REFERENCES "${primehrSchema}".spms_monitoring_update(id),
 CONSTRAINT fk_spms_monitoring_evidence_item FOREIGN KEY(commitment_item_id) REFERENCES "${primehrSchema}".spms_commitment_item(id),
 CONSTRAINT fk_spms_monitoring_evidence_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".spms_monitoring_evidence(id),
 CONSTRAINT ck_spms_monitoring_evidence_size CHECK(byte_size>0),
 CONSTRAINT ck_spms_monitoring_evidence_confidentiality CHECK(confidentiality IN ('INTERNAL','SENSITIVE')),
 CONSTRAINT ck_spms_monitoring_evidence_status CHECK(status IN ('ACTIVE','SUPERSEDED','VOIDED')));
CREATE TABLE "${primehrSchema}".spms_monitoring_action (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL,
 subject_id VARCHAR(36), action_type VARCHAR(40) NOT NULL, request_key VARCHAR(100) NOT NULL,
 actor_employee_no VARCHAR(100) NOT NULL, action_reason VARCHAR(2000), acted_at TIMESTAMP WITH TIME ZONE NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_monitoring_action_case FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT uk_spms_monitoring_action_request UNIQUE(agency_id,monitoring_case_id,request_key));
CREATE INDEX ix_spms_monitoring_owner ON "${primehrSchema}".spms_monitoring_case(agency_id,owner_employee_no,status);
CREATE INDEX ix_spms_monitoring_supervisor ON "${primehrSchema}".spms_monitoring_case(agency_id,supervisor_employee_no,status);
CREATE INDEX ix_spms_monitoring_update_case ON "${primehrSchema}".spms_monitoring_update(agency_id,monitoring_case_id,commitment_item_id,status);
CREATE INDEX ix_spms_monitoring_evidence_case ON "${primehrSchema}".spms_monitoring_evidence(agency_id,monitoring_case_id,status);
CREATE INDEX ix_spms_monitoring_action_history ON "${primehrSchema}".spms_monitoring_action(agency_id,monitoring_case_id,acted_at);

CREATE TABLE "${primehrSchema}".spms_coaching_session (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL,
 root_session_id VARCHAR(36) NOT NULL, revision_no INTEGER NOT NULL, supersedes_id VARCHAR(36), session_at TIMESTAMP WITH TIME ZONE NOT NULL,
 agenda VARCHAR(2000) NOT NULL, goal VARCHAR(2000) NOT NULL, observed_issue VARCHAR(3000) NOT NULL, agreed_action VARCHAR(3000) NOT NULL,
 resources_support VARCHAR(3000), action_due_date DATE, next_meeting_at TIMESTAMP WITH TIME ZONE,
 employee_visible_feedback VARCHAR(3000), private_notes VARCHAR(3000), employee_response VARCHAR(3000), status VARCHAR(20) NOT NULL,
 issued_by VARCHAR(100), issued_at TIMESTAMP WITH TIME ZONE, acknowledged_by VARCHAR(100), acknowledged_at TIMESTAMP WITH TIME ZONE,
 void_reason VARCHAR(2000), record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_coaching_case FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT fk_spms_coaching_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".spms_coaching_session(id),
 CONSTRAINT uk_spms_coaching_revision UNIQUE(agency_id,root_session_id,revision_no),
 CONSTRAINT ck_spms_coaching_revision CHECK(revision_no>0),
 CONSTRAINT ck_spms_coaching_status CHECK(status IN ('DRAFT','ISSUED','ACKNOWLEDGED','SUPERSEDED','VOIDED')));
CREATE TABLE "${primehrSchema}".spms_coaching_session_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, coaching_session_id VARCHAR(36) NOT NULL, commitment_item_id VARCHAR(36) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_coaching_item_session FOREIGN KEY(coaching_session_id) REFERENCES "${primehrSchema}".spms_coaching_session(id),
 CONSTRAINT fk_spms_coaching_item_commitment FOREIGN KEY(commitment_item_id) REFERENCES "${primehrSchema}".spms_commitment_item(id),
 CONSTRAINT uk_spms_coaching_session_item UNIQUE(agency_id,coaching_session_id,commitment_item_id));
CREATE TABLE "${primehrSchema}".spms_coaching_action_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL, coaching_session_id VARCHAR(36) NOT NULL,
 description VARCHAR(3000) NOT NULL, accountable_employee_no VARCHAR(100) NOT NULL, due_date DATE NOT NULL, status VARCHAR(20) NOT NULL,
 progress_note VARCHAR(3000), completed_at TIMESTAMP WITH TIME ZONE, verified_by VARCHAR(100), verified_at TIMESTAMP WITH TIME ZONE, reopen_reason VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_coaching_action_case FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT fk_spms_coaching_action_session FOREIGN KEY(coaching_session_id) REFERENCES "${primehrSchema}".spms_coaching_session(id),
 CONSTRAINT ck_spms_coaching_action_status CHECK(status IN ('OPEN','IN_PROGRESS','COMPLETED','CANCELLED')));
CREATE TABLE "${primehrSchema}".spms_mid_cycle_review (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL,
 snapshot_update_count INTEGER NOT NULL, snapshot_open_action_count INTEGER NOT NULL, snapshot_missing_evidence_count INTEGER NOT NULL,
 supervisor_narrative VARCHAR(4000) NOT NULL, employee_narrative VARCHAR(4000), amendment_recommended BOOLEAN NOT NULL,
 amendment_reason VARCHAR(2000), status VARCHAR(40) NOT NULL, submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE,
 acknowledged_by VARCHAR(100), acknowledged_at TIMESTAMP WITH TIME ZONE, closure_reason VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_mid_cycle_case FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT uk_spms_mid_cycle_case UNIQUE(agency_id,monitoring_case_id),
 CONSTRAINT ck_spms_mid_cycle_counts CHECK(snapshot_update_count>=0 AND snapshot_open_action_count>=0 AND snapshot_missing_evidence_count>=0),
 CONSTRAINT ck_spms_mid_cycle_status CHECK(status IN ('DRAFT','SUBMITTED','ACKNOWLEDGED','CLOSED_WITHOUT_ACKNOWLEDGMENT','SUPERSEDED','VOIDED')),
 CONSTRAINT ck_spms_mid_cycle_amendment CHECK(amendment_recommended=FALSE OR amendment_reason IS NOT NULL));
CREATE INDEX ix_spms_coaching_case ON "${primehrSchema}".spms_coaching_session(agency_id,monitoring_case_id,status);
CREATE INDEX ix_spms_coaching_action_case ON "${primehrSchema}".spms_coaching_action_item(agency_id,monitoring_case_id,status,due_date);
CREATE INDEX ix_spms_coaching_action_accountable ON "${primehrSchema}".spms_coaching_action_item(agency_id,accountable_employee_no,status);
CREATE INDEX ix_spms_mid_cycle_case ON "${primehrSchema}".spms_mid_cycle_review(agency_id,monitoring_case_id,status);

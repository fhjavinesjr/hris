CREATE TABLE ${primehrSchema}.spms_commitment (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, assignment_id VARCHAR(36) NOT NULL,
 current_version_id VARCHAR(36), record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_assignment FOREIGN KEY(assignment_id) REFERENCES ${primehrSchema}.spms_plan_assignment(id),
 CONSTRAINT uk_spms_commitment_assignment UNIQUE(agency_id,assignment_id));
CREATE TABLE ${primehrSchema}.spms_commitment_version (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, commitment_id VARCHAR(36) NOT NULL,
 assignment_id VARCHAR(36) NOT NULL, definition_version INT NOT NULL, supersedes_id VARCHAR(36),
 cycle_id VARCHAR(36) NOT NULL, policy_version_id VARCHAR(36) NOT NULL, template_version_id VARCHAR(36) NOT NULL,
 rating_scale_version_id VARCHAR(36) NOT NULL, objective_version_ids VARCHAR(4000), form_type VARCHAR(20) NOT NULL,
 form_label VARCHAR(200) NOT NULL, period_start DATE NOT NULL, period_end DATE NOT NULL, calendar_revision INT NOT NULL,
 subject_type VARCHAR(30) NOT NULL, subject_id BIGINT NOT NULL, subject_code VARCHAR(80), subject_name VARCHAR(200) NOT NULL,
 area_id BIGINT, area_name VARCHAR(200), business_unit_id BIGINT NOT NULL, business_unit_name VARCHAR(200) NOT NULL,
 owner_employee_id BIGINT NOT NULL, owner_employee_no VARCHAR(100) NOT NULL, owner_name VARCHAR(300) NOT NULL,
 owner_appointment_id BIGINT NOT NULL, owner_job_position_id BIGINT NOT NULL, owner_plantilla_id BIGINT NOT NULL,
 organization_fingerprint VARCHAR(64) NOT NULL, participant_fingerprint VARCHAR(64) NOT NULL,
 assignment_fingerprint VARCHAR(64) NOT NULL, content_fingerprint VARCHAR(64) NOT NULL,
 source_fetched_at DATETIMEOFFSET NOT NULL, status VARCHAR(24) NOT NULL,
 content_revision INT NOT NULL DEFAULT 0, lifecycle_revision INT NOT NULL DEFAULT 0,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_version_root FOREIGN KEY(commitment_id) REFERENCES ${primehrSchema}.spms_commitment(id),
 CONSTRAINT fk_spms_commitment_version_assignment FOREIGN KEY(assignment_id) REFERENCES ${primehrSchema}.spms_plan_assignment(id),
 CONSTRAINT fk_spms_commitment_version_prior FOREIGN KEY(supersedes_id) REFERENCES ${primehrSchema}.spms_commitment_version(id),
 CONSTRAINT fk_spms_commitment_version_cycle FOREIGN KEY(cycle_id) REFERENCES ${primehrSchema}.spms_cycle(id),
 CONSTRAINT fk_spms_commitment_version_policy FOREIGN KEY(policy_version_id) REFERENCES ${primehrSchema}.spms_policy_version(id),
 CONSTRAINT fk_spms_commitment_version_template FOREIGN KEY(template_version_id) REFERENCES ${primehrSchema}.spms_template_version(id),
 CONSTRAINT fk_spms_commitment_version_scale FOREIGN KEY(rating_scale_version_id) REFERENCES ${primehrSchema}.spms_rating_scale_version(id),
 CONSTRAINT uk_spms_commitment_version UNIQUE(agency_id,commitment_id,definition_version),
 CONSTRAINT ck_spms_commitment_period CHECK(period_end>=period_start),CONSTRAINT ck_spms_commitment_definition CHECK(definition_version>0),
 CONSTRAINT ck_spms_commitment_revisions CHECK(content_revision>=0 AND lifecycle_revision>=0),
 CONSTRAINT ck_spms_commitment_status CHECK(status IN ('DRAFT','AMENDMENT_DRAFT','SUBMITTED','IN_REVIEW','RETURNED','REJECTED','APPROVED','WITHDRAWN','VOIDED','SUPERSEDED')));
ALTER TABLE ${primehrSchema}.spms_commitment ADD CONSTRAINT fk_spms_commitment_current FOREIGN KEY(current_version_id) REFERENCES ${primehrSchema}.spms_commitment_version(id);
CREATE TABLE ${primehrSchema}.spms_commitment_section (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, commitment_version_id VARCHAR(36) NOT NULL,
 source_section_id VARCHAR(36) NOT NULL, section_type VARCHAR(20) NOT NULL, section_code VARCHAR(80) NOT NULL,
 title VARCHAR(200) NOT NULL, description VARCHAR(2000), weight_percent DECIMAL(7,4) NOT NULL, display_order INT NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_section_version FOREIGN KEY(commitment_version_id) REFERENCES ${primehrSchema}.spms_commitment_version(id),
 CONSTRAINT fk_spms_commitment_section_source FOREIGN KEY(source_section_id) REFERENCES ${primehrSchema}.spms_template_section(id),
 CONSTRAINT uk_spms_commitment_section_order UNIQUE(agency_id,commitment_version_id,display_order),
 CONSTRAINT ck_spms_commitment_section_weight CHECK(weight_percent>0),CONSTRAINT ck_spms_commitment_section_order CHECK(display_order>0));
CREATE TABLE ${primehrSchema}.spms_commitment_item (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, commitment_version_id VARCHAR(36) NOT NULL,
 commitment_section_id VARCHAR(36) NOT NULL, source_item_id VARCHAR(36) NOT NULL, indicator_version_id VARCHAR(36) NOT NULL,
 label VARCHAR(200) NOT NULL, output_description VARCHAR(2000) NOT NULL, key_result_area VARCHAR(500) NOT NULL,
 performance_indicator VARCHAR(1000) NOT NULL, success_statement VARCHAR(2000) NOT NULL, measure_type VARCHAR(30) NOT NULL,
 unit_of_measure VARCHAR(100) NOT NULL, direction VARCHAR(30) NOT NULL, weight_percent DECIMAL(7,4) NOT NULL,
 required_item BIT NOT NULL, source_requires_evidence BIT NOT NULL, source_evidence_requirement VARCHAR(2000),
 indicator_fingerprint VARCHAR(64) NOT NULL, committed_value DECIMAL(19,6), committed_from DECIMAL(19,6), committed_to DECIMAL(19,6),
 target_start DATE, target_end DATE, deliverable_clarification VARCHAR(2000), responsible_employee_id BIGINT,
 responsible_employee_no VARCHAR(100), responsible_name VARCHAR(300), responsible_fingerprint VARCHAR(64),
 planning_remarks VARCHAR(2000), evidence_clarification VARCHAR(2000), display_order INT NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_commitment_item_version FOREIGN KEY(commitment_version_id) REFERENCES ${primehrSchema}.spms_commitment_version(id),
 CONSTRAINT fk_spms_commitment_item_section FOREIGN KEY(commitment_section_id) REFERENCES ${primehrSchema}.spms_commitment_section(id),
 CONSTRAINT fk_spms_commitment_item_source FOREIGN KEY(source_item_id) REFERENCES ${primehrSchema}.spms_template_item(id),
 CONSTRAINT fk_spms_commitment_item_indicator FOREIGN KEY(indicator_version_id) REFERENCES ${primehrSchema}.spms_success_indicator_version(id),
 CONSTRAINT uk_spms_commitment_item_order UNIQUE(agency_id,commitment_section_id,display_order),
 CONSTRAINT ck_spms_commitment_item_weight CHECK(weight_percent>0),CONSTRAINT ck_spms_commitment_item_order CHECK(display_order>0),
 CONSTRAINT ck_spms_commitment_item_period CHECK(target_end IS NULL OR target_start IS NULL OR target_end>=target_start),
 CONSTRAINT ck_spms_commitment_item_range CHECK(committed_to IS NULL OR committed_from IS NULL OR committed_to>=committed_from));
CREATE TABLE ${primehrSchema}.spms_commitment_cascade (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, upstream_item_id VARCHAR(36) NOT NULL,
 downstream_item_id VARCHAR(36) NOT NULL, mode VARCHAR(30) NOT NULL, share_percent DECIMAL(7,4), active BIT NOT NULL,
 source_fingerprint VARCHAR(64) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_cascade_upstream FOREIGN KEY(upstream_item_id) REFERENCES ${primehrSchema}.spms_commitment_item(id),
 CONSTRAINT fk_spms_cascade_downstream FOREIGN KEY(downstream_item_id) REFERENCES ${primehrSchema}.spms_commitment_item(id),
 CONSTRAINT uk_spms_commitment_cascade UNIQUE(agency_id,upstream_item_id,downstream_item_id,mode),
 CONSTRAINT ck_spms_cascade_mode CHECK(mode IN ('REFERENCE_ONLY','RESPONSIBILITY_SHARE')),
 CONSTRAINT ck_spms_cascade_share CHECK((mode='REFERENCE_ONLY' AND share_percent IS NULL) OR (mode='RESPONSIBILITY_SHARE' AND share_percent>0 AND share_percent<=100.0000)));
CREATE INDEX ix_spms_commitment_assignment ON ${primehrSchema}.spms_commitment(agency_id,assignment_id);
CREATE INDEX ix_spms_commitment_status ON ${primehrSchema}.spms_commitment_version(agency_id,cycle_id,status,form_type);
CREATE INDEX ix_spms_commitment_owner ON ${primehrSchema}.spms_commitment_version(agency_id,owner_employee_id,status);
CREATE INDEX ix_spms_commitment_section ON ${primehrSchema}.spms_commitment_section(agency_id,commitment_version_id,display_order);
CREATE INDEX ix_spms_commitment_item ON ${primehrSchema}.spms_commitment_item(agency_id,commitment_version_id,display_order);
CREATE INDEX ix_spms_cascade_upstream ON ${primehrSchema}.spms_commitment_cascade(agency_id,upstream_item_id,active);
CREATE INDEX ix_spms_cascade_downstream ON ${primehrSchema}.spms_commitment_cascade(agency_id,downstream_item_id,active);

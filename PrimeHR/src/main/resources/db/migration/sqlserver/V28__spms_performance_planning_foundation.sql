CREATE TABLE ${primehrSchema}.spms_objective (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT uk_spms_objective_code UNIQUE(agency_id,normalized_code)
);
CREATE TABLE ${primehrSchema}.spms_objective_version (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, objective_id VARCHAR(36) NOT NULL,
 definition_version INT NOT NULL, supersedes_id VARCHAR(36), title VARCHAR(200) NOT NULL,
 objective_statement VARCHAR(2000) NOT NULL, expected_outcome VARCHAR(2000) NOT NULL,
 strategy_reference VARCHAR(1000) NOT NULL, objective_level VARCHAR(30) NOT NULL,
 organization_id BIGINT, organization_code VARCHAR(80), organization_name VARCHAR(200), parent_area_id BIGINT,
 parent_objective_version_id VARCHAR(36), policy_version_id VARCHAR(36) NOT NULL,
 indicator_version_id VARCHAR(36), target_value DECIMAL(19,6), target_from DECIMAL(19,6),
 target_to DECIMAL(19,6), unit_of_measure VARCHAR(100), source_fingerprint VARCHAR(64),
 status VARCHAR(20) NOT NULL, effective_from DATE, effective_to DATE,
 published_by VARCHAR(100), published_at DATETIMEOFFSET,
 retired_by VARCHAR(100), retired_at DATETIMEOFFSET, retirement_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_objective_root FOREIGN KEY(objective_id) REFERENCES ${primehrSchema}.spms_objective(id),
 CONSTRAINT fk_spms_objective_prior FOREIGN KEY(supersedes_id) REFERENCES ${primehrSchema}.spms_objective_version(id),
 CONSTRAINT fk_spms_objective_parent FOREIGN KEY(parent_objective_version_id) REFERENCES ${primehrSchema}.spms_objective_version(id),
 CONSTRAINT fk_spms_objective_policy FOREIGN KEY(policy_version_id) REFERENCES ${primehrSchema}.spms_policy_version(id),
 CONSTRAINT fk_spms_objective_indicator FOREIGN KEY(indicator_version_id) REFERENCES ${primehrSchema}.spms_success_indicator_version(id),
 CONSTRAINT uk_spms_objective_version UNIQUE(agency_id,objective_id,definition_version),
 CONSTRAINT ck_spms_objective_level CHECK(objective_level IN ('AGENCY','AREA','BUSINESS_UNIT')),
 CONSTRAINT ck_spms_objective_status CHECK(status IN ('DRAFT','PUBLISHED','RETIRED')),
 CONSTRAINT ck_spms_objective_effective CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_spms_objective_target CHECK(target_to IS NULL OR target_from IS NULL OR target_to>=target_from)
);
CREATE TABLE ${primehrSchema}.spms_plan_assignment (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, cycle_id VARCHAR(36) NOT NULL,
 template_version_id VARCHAR(36) NOT NULL, subject_type VARCHAR(30) NOT NULL, subject_id BIGINT NOT NULL,
 subject_code VARCHAR(80), subject_name VARCHAR(200) NOT NULL, area_id BIGINT, area_name VARCHAR(200),
 routing_business_unit_id BIGINT NOT NULL, routing_business_unit_name VARCHAR(200) NOT NULL,
 owner_employee_id BIGINT NOT NULL, owner_employee_no VARCHAR(100) NOT NULL, owner_name VARCHAR(300) NOT NULL,
 owner_appointment_id BIGINT NOT NULL, owner_job_position_id BIGINT NOT NULL, owner_plantilla_id BIGINT NOT NULL,
 form_type VARCHAR(20) NOT NULL, organization_fingerprint VARCHAR(64) NOT NULL,
 participant_fingerprint VARCHAR(64) NOT NULL, route_fingerprint VARCHAR(64) NOT NULL,
 status VARCHAR(20) NOT NULL, activated_by VARCHAR(100), activated_at DATETIMEOFFSET,
 retired_by VARCHAR(100), retired_at DATETIMEOFFSET, transition_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_assignment_cycle FOREIGN KEY(cycle_id) REFERENCES ${primehrSchema}.spms_cycle(id),
 CONSTRAINT fk_spms_assignment_template FOREIGN KEY(template_version_id) REFERENCES ${primehrSchema}.spms_template_version(id),
 CONSTRAINT uk_spms_assignment_subject UNIQUE(agency_id,cycle_id,subject_type,subject_id,form_type),
 CONSTRAINT ck_spms_assignment_subject CHECK(subject_type IN ('AREA','BUSINESS_UNIT','EMPLOYEE')),
 CONSTRAINT ck_spms_assignment_status CHECK(status IN ('DRAFT','ACTIVE','RETIRED'))
);
CREATE TABLE ${primehrSchema}.spms_plan_assignment_objective (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, assignment_id VARCHAR(36) NOT NULL,
 objective_version_id VARCHAR(36) NOT NULL, display_order INT NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_assignment_objective_assignment FOREIGN KEY(assignment_id) REFERENCES ${primehrSchema}.spms_plan_assignment(id),
 CONSTRAINT fk_spms_assignment_objective_version FOREIGN KEY(objective_version_id) REFERENCES ${primehrSchema}.spms_objective_version(id),
 CONSTRAINT uk_spms_assignment_objective UNIQUE(agency_id,assignment_id,objective_version_id),
 CONSTRAINT uk_spms_assignment_objective_order UNIQUE(agency_id,assignment_id,display_order)
);
CREATE INDEX ix_spms_objective_status ON ${primehrSchema}.spms_objective_version(agency_id,status,objective_level);
CREATE INDEX ix_spms_objective_parent ON ${primehrSchema}.spms_objective_version(agency_id,parent_objective_version_id);
CREATE INDEX ix_spms_assignment_cycle ON ${primehrSchema}.spms_plan_assignment(agency_id,cycle_id,status);
CREATE INDEX ix_spms_assignment_owner ON ${primehrSchema}.spms_plan_assignment(agency_id,owner_employee_id,status);
CREATE INDEX ix_spms_assignment_objective ON ${primehrSchema}.spms_plan_assignment_objective(agency_id,assignment_id,display_order);

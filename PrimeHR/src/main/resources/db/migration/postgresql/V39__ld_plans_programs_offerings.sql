CREATE TABLE "${primehrSchema}".ld_consolidated_plan (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, series_id VARCHAR(36) NOT NULL,
 plan_version INTEGER NOT NULL, supersedes_id VARCHAR(36), scope_type VARCHAR(20) NOT NULL,
 organization_type VARCHAR(30) NOT NULL, organization_id BIGINT NOT NULL, organization_code VARCHAR(80) NOT NULL,
 organization_name VARCHAR(200) NOT NULL, area_id BIGINT, area_name VARCHAR(200), organization_fingerprint VARCHAR(64) NOT NULL,
 policy_version_id VARCHAR(36) NOT NULL, policy_fingerprint VARCHAR(64) NOT NULL, planning_year INTEGER NOT NULL,
 title VARCHAR(200) NOT NULL, objectives VARCHAR(4000) NOT NULL, period_start DATE NOT NULL, period_end DATE NOT NULL,
 estimated_cost NUMERIC(19,2) NOT NULL, funding_source VARCHAR(500) NOT NULL,
 accountable_employee_id BIGINT NOT NULL, accountable_employee_no VARCHAR(100) NOT NULL,
 accountable_name VARCHAR(300) NOT NULL, accountable_appointment_id BIGINT NOT NULL, accountable_fingerprint VARCHAR(64) NOT NULL,
 status VARCHAR(20) NOT NULL, content_revision INTEGER NOT NULL, submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE,
 approved_by VARCHAR(100), approved_at TIMESTAMP WITH TIME ZONE, published_by VARCHAR(100), published_at TIMESTAMP WITH TIME ZONE, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_plan_supersedes FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".ld_consolidated_plan(id),
 CONSTRAINT fk_ld_plan_policy FOREIGN KEY(policy_version_id) REFERENCES "${primehrSchema}".ld_governance_policy_version(id),
 CONSTRAINT uk_ld_plan_series_version UNIQUE(agency_id,series_id,plan_version),
 CONSTRAINT uk_ld_plan_scope_period UNIQUE(agency_id,scope_type,organization_id,planning_year,plan_version),
 CONSTRAINT ck_ld_plan_scope CHECK(scope_type IN ('OFFICE','AGENCY')),
 CONSTRAINT ck_ld_plan_org_type CHECK(organization_type IN ('AREA','BUSINESS_UNIT')),
 CONSTRAINT ck_ld_plan_version CHECK(plan_version>=1 AND content_revision>=1),
 CONSTRAINT ck_ld_plan_year CHECK(planning_year BETWEEN 2000 AND 2200),
 CONSTRAINT ck_ld_plan_dates CHECK(period_end>=period_start), CONSTRAINT ck_ld_plan_cost CHECK(estimated_cost>=0),
 CONSTRAINT ck_ld_plan_status CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','PUBLISHED','CANCELLED')));
CREATE INDEX ix_ld_plan_scope ON "${primehrSchema}".ld_consolidated_plan(agency_id,scope_type,organization_id,planning_year,status);
CREATE INDEX ix_ld_plan_accountable ON "${primehrSchema}".ld_consolidated_plan(agency_id,accountable_employee_no,status);

CREATE TABLE "${primehrSchema}".ld_consolidated_plan_source (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, plan_id VARCHAR(36) NOT NULL,
 source_type VARCHAR(30) NOT NULL, source_id VARCHAR(36) NOT NULL, source_version BIGINT NOT NULL,
 source_fingerprint VARCHAR(64) NOT NULL, employee_no VARCHAR(100), organization_id BIGINT NOT NULL,
 organization_name VARCHAR(200) NOT NULL, development_objective VARCHAR(3000) NOT NULL,
 success_measure VARCHAR(2000) NOT NULL, target_date DATE, priority VARCHAR(20) NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_plan_source_plan FOREIGN KEY(plan_id) REFERENCES "${primehrSchema}".ld_consolidated_plan(id),
 CONSTRAINT uk_ld_plan_source UNIQUE(agency_id,plan_id,source_type,source_id),
 CONSTRAINT ck_ld_plan_source_type CHECK(source_type IN ('APPROVED_IDP_ITEM','ORGANIZATIONAL_NEED')),
 CONSTRAINT ck_ld_plan_source_values CHECK(source_version>=0 AND display_order>=1));
CREATE INDEX ix_ld_plan_source_order ON "${primehrSchema}".ld_consolidated_plan_source(agency_id,plan_id,display_order);

CREATE TABLE "${primehrSchema}".ld_program (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, series_id VARCHAR(36) NOT NULL,
 program_version INTEGER NOT NULL, supersedes_id VARCHAR(36), plan_id VARCHAR(36) NOT NULL, plan_version INTEGER NOT NULL,
 plan_fingerprint VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL, title VARCHAR(200) NOT NULL,
 intervention_version_id VARCHAR(36) NOT NULL, intervention_fingerprint VARCHAR(64) NOT NULL,
 evaluation_framework_version_id VARCHAR(36) NOT NULL, evaluation_fingerprint VARCHAR(64) NOT NULL,
 intended_outcomes VARCHAR(4000) NOT NULL, delivery_strategy VARCHAR(4000) NOT NULL,
 schedule_start DATE NOT NULL, schedule_end DATE NOT NULL, estimated_hours NUMERIC(9,2) NOT NULL,
 estimated_cost NUMERIC(19,2) NOT NULL, funding_source VARCHAR(500) NOT NULL,
 accountable_employee_id BIGINT NOT NULL, accountable_employee_no VARCHAR(100) NOT NULL,
 accountable_name VARCHAR(300) NOT NULL, accountable_appointment_id BIGINT NOT NULL, accountable_fingerprint VARCHAR(64) NOT NULL,
 status VARCHAR(20) NOT NULL, content_revision INTEGER NOT NULL, submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE,
 approved_by VARCHAR(100), approved_at TIMESTAMP WITH TIME ZONE, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_program_supersedes FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".ld_program(id),
 CONSTRAINT fk_ld_program_plan FOREIGN KEY(plan_id) REFERENCES "${primehrSchema}".ld_consolidated_plan(id),
 CONSTRAINT fk_ld_program_intervention FOREIGN KEY(intervention_version_id) REFERENCES "${primehrSchema}".ld_intervention_version(id),
 CONSTRAINT fk_ld_program_evaluation FOREIGN KEY(evaluation_framework_version_id) REFERENCES "${primehrSchema}".ld_evaluation_framework_version(id),
 CONSTRAINT uk_ld_program_series_version UNIQUE(agency_id,series_id,program_version),
 CONSTRAINT uk_ld_program_code_version UNIQUE(agency_id,normalized_code,program_version),
 CONSTRAINT ck_ld_program_version CHECK(program_version>=1 AND plan_version>=1 AND content_revision>=1),
 CONSTRAINT ck_ld_program_dates CHECK(schedule_end>=schedule_start),
 CONSTRAINT ck_ld_program_values CHECK(estimated_hours>0 AND estimated_cost>=0),
 CONSTRAINT ck_ld_program_status CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','CANCELLED')));
CREATE INDEX ix_ld_program_plan ON "${primehrSchema}".ld_program(agency_id,plan_id,status);
CREATE INDEX ix_ld_program_accountable ON "${primehrSchema}".ld_program(agency_id,accountable_employee_no,status);

CREATE TABLE "${primehrSchema}".ld_program_source (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, program_id VARCHAR(36) NOT NULL,
 plan_source_id VARCHAR(36) NOT NULL, source_fingerprint VARCHAR(64) NOT NULL,
 development_objective VARCHAR(3000) NOT NULL, success_measure VARCHAR(2000) NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_program_source_program FOREIGN KEY(program_id) REFERENCES "${primehrSchema}".ld_program(id),
 CONSTRAINT fk_ld_program_source_plan_source FOREIGN KEY(plan_source_id) REFERENCES "${primehrSchema}".ld_consolidated_plan_source(id),
 CONSTRAINT uk_ld_program_source UNIQUE(agency_id,program_id,plan_source_id),
 CONSTRAINT ck_ld_program_source_order CHECK(display_order>=1));
CREATE INDEX ix_ld_program_source_order ON "${primehrSchema}".ld_program_source(agency_id,program_id,display_order);

CREATE TABLE "${primehrSchema}".ld_offering (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, series_id VARCHAR(36) NOT NULL,
 offering_version INTEGER NOT NULL, supersedes_id VARCHAR(36), program_id VARCHAR(36) NOT NULL, program_version INTEGER NOT NULL,
 program_fingerprint VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL,
 title VARCHAR(200) NOT NULL, modality VARCHAR(30) NOT NULL, venue_or_platform VARCHAR(1000) NOT NULL, capacity INTEGER NOT NULL,
 provider_id VARCHAR(36), provider_name VARCHAR(250), provider_fingerprint VARCHAR(64),
 evaluation_framework_version_id VARCHAR(36) NOT NULL, evaluation_fingerprint VARCHAR(64) NOT NULL,
 approved_budget NUMERIC(19,2) NOT NULL, funding_source VARCHAR(500) NOT NULL, currency_code VARCHAR(3) NOT NULL,
 registration_open TIMESTAMP WITH TIME ZONE, registration_close TIMESTAMP WITH TIME ZONE,
 status VARCHAR(20) NOT NULL, content_revision INTEGER NOT NULL, submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE,
 approved_by VARCHAR(100), approved_at TIMESTAMP WITH TIME ZONE, published_by VARCHAR(100), published_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_offering_supersedes FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".ld_offering(id),
 CONSTRAINT fk_ld_offering_program FOREIGN KEY(program_id) REFERENCES "${primehrSchema}".ld_program(id),
 CONSTRAINT fk_ld_offering_provider FOREIGN KEY(provider_id) REFERENCES "${primehrSchema}".ld_provider(id),
 CONSTRAINT fk_ld_offering_evaluation FOREIGN KEY(evaluation_framework_version_id) REFERENCES "${primehrSchema}".ld_evaluation_framework_version(id),
 CONSTRAINT uk_ld_offering_series_version UNIQUE(agency_id,series_id,offering_version),
 CONSTRAINT uk_ld_offering_code_version UNIQUE(agency_id,normalized_code,offering_version),
 CONSTRAINT ck_ld_offering_version CHECK(offering_version>=1 AND program_version>=1 AND content_revision>=1),
 CONSTRAINT ck_ld_offering_modality CHECK(modality IN ('IN_PERSON','ONLINE','BLENDED','WORKPLACE','SELF_DIRECTED','OTHER')),
 CONSTRAINT ck_ld_offering_values CHECK(capacity>=1 AND approved_budget>=0),
 CONSTRAINT ck_ld_offering_registration CHECK(registration_close IS NULL OR registration_open IS NULL OR registration_close>=registration_open),
 CONSTRAINT ck_ld_offering_status CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','PUBLISHED','CANCELLED')));
CREATE INDEX ix_ld_offering_program ON "${primehrSchema}".ld_offering(agency_id,program_id,status);
CREATE INDEX ix_ld_offering_status ON "${primehrSchema}".ld_offering(agency_id,status,registration_open);

CREATE TABLE "${primehrSchema}".ld_offering_session (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, offering_id VARCHAR(36) NOT NULL,
 title VARCHAR(200) NOT NULL, starts_at TIMESTAMP WITH TIME ZONE NOT NULL, ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
 venue_or_platform VARCHAR(1000) NOT NULL, credited_hours NUMERIC(9,2) NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_offering_session FOREIGN KEY(offering_id) REFERENCES "${primehrSchema}".ld_offering(id),
 CONSTRAINT uk_ld_offering_session_order UNIQUE(agency_id,offering_id,display_order),
 CONSTRAINT ck_ld_offering_session_values CHECK(ends_at>starts_at AND credited_hours>0 AND display_order>=1));
CREATE INDEX ix_ld_offering_session_time ON "${primehrSchema}".ld_offering_session(agency_id,offering_id,starts_at);

CREATE TABLE "${primehrSchema}".ld_offering_resource (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, offering_id VARCHAR(36) NOT NULL,
 resource_person_id VARCHAR(36) NOT NULL, resource_role VARCHAR(30) NOT NULL, person_code VARCHAR(80) NOT NULL,
 person_source VARCHAR(20) NOT NULL, employee_no VARCHAR(100), display_name VARCHAR(250) NOT NULL, provider_id VARCHAR(36),
 expertise VARCHAR(2000), credentials VARCHAR(2000), resource_fingerprint VARCHAR(64) NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_ld_offering_resource_offering FOREIGN KEY(offering_id) REFERENCES "${primehrSchema}".ld_offering(id),
 CONSTRAINT fk_ld_offering_resource_person FOREIGN KEY(resource_person_id) REFERENCES "${primehrSchema}".ld_resource_person(id),
 CONSTRAINT uk_ld_offering_resource UNIQUE(agency_id,offering_id,resource_person_id,resource_role),
 CONSTRAINT ck_ld_offering_resource_role CHECK(resource_role IN ('LEAD_FACILITATOR','RESOURCE_PERSON','COORDINATOR','SUPPORT_STAFF')),
 CONSTRAINT ck_ld_offering_resource_order CHECK(display_order>=1));
CREATE INDEX ix_ld_offering_resource_order ON "${primehrSchema}".ld_offering_resource(agency_id,offering_id,display_order);

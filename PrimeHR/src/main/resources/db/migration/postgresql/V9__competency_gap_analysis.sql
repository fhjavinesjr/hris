CREATE TABLE "${primehrSchema}".prime_gap_priority_scheme (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(50) NOT NULL,
 name VARCHAR(200) NOT NULL, description VARCHAR(2000), status VARCHAR(30) NOT NULL,
 definition_version INTEGER NOT NULL, supersedes_id VARCHAR(36),
 effective_from DATE, effective_to DATE, active BOOLEAN NOT NULL, display_order INTEGER NOT NULL,
 published_by VARCHAR(100), published_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_gap_scheme_predecessor FOREIGN KEY(supersedes_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_scheme(id),
 CONSTRAINT uk_prime_gap_scheme_version UNIQUE(agency_id,code,definition_version),
 CONSTRAINT ck_prime_gap_scheme_status CHECK(status IN ('DRAFT','ACTIVE','ARCHIVED')),
 CONSTRAINT ck_prime_gap_scheme_version CHECK(definition_version>=1),
 CONSTRAINT ck_prime_gap_scheme_code_upper CHECK(code=UPPER(code)),
 CONSTRAINT ck_prime_gap_scheme_dates CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_prime_gap_scheme_order CHECK(display_order>=0),
 CONSTRAINT ck_prime_gap_scheme_publication CHECK(
  (status='ACTIVE' AND active=TRUE AND published_by IS NOT NULL AND published_at IS NOT NULL AND effective_from IS NOT NULL)
  OR (status IN ('DRAFT','ARCHIVED') AND active=FALSE AND published_by IS NULL AND published_at IS NULL))
);

CREATE TABLE "${primehrSchema}".prime_gap_priority_level (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, scheme_id VARCHAR(36) NOT NULL,
 code VARCHAR(50) NOT NULL, label VARCHAR(150) NOT NULL, description VARCHAR(1000),
 priority_rank INTEGER NOT NULL, active BOOLEAN NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_gap_level_scheme FOREIGN KEY(scheme_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_scheme(id),
 CONSTRAINT uk_prime_gap_level_code UNIQUE(scheme_id,code),
 CONSTRAINT uk_prime_gap_level_rank UNIQUE(scheme_id,priority_rank),
 CONSTRAINT ck_prime_gap_level_code_upper CHECK(code=UPPER(code)),
 CONSTRAINT ck_prime_gap_level_rank CHECK(priority_rank>=1),
 CONSTRAINT ck_prime_gap_level_order CHECK(display_order>=0)
);

CREATE TABLE "${primehrSchema}".prime_gap_priority_rule (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, scheme_id VARCHAR(36) NOT NULL,
 gap_classification VARCHAR(30) NOT NULL, minimum_gap INTEGER, maximum_gap INTEGER,
 requirement_classification VARCHAR(30), criticality_code VARCHAR(50),
 priority_level_id VARCHAR(36) NOT NULL, explanation VARCHAR(1000),
 active BOOLEAN NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_gap_rule_scheme FOREIGN KEY(scheme_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_scheme(id),
 CONSTRAINT fk_prime_gap_rule_level FOREIGN KEY(priority_level_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_level(id),
 CONSTRAINT uk_prime_gap_rule_order UNIQUE(scheme_id,display_order),
 CONSTRAINT ck_prime_gap_rule_classification CHECK(gap_classification IN ('BELOW','NOT_ASSESSED')),
 CONSTRAINT ck_prime_gap_rule_requirement CHECK(requirement_classification IS NULL OR requirement_classification IN ('MANDATORY','DESIRABLE')),
 CONSTRAINT ck_prime_gap_rule_range CHECK((minimum_gap IS NULL OR minimum_gap>=1)
  AND (maximum_gap IS NULL OR maximum_gap>=1)
  AND (minimum_gap IS NULL OR maximum_gap IS NULL OR maximum_gap>=minimum_gap)),
 CONSTRAINT ck_prime_gap_rule_not_assessed CHECK(gap_classification<>'NOT_ASSESSED' OR (minimum_gap IS NULL AND maximum_gap IS NULL)),
 CONSTRAINT ck_prime_gap_rule_order CHECK(display_order>=0)
);

CREATE TABLE "${primehrSchema}".prime_competency_gap_analysis (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 subject_employee_id BIGINT NOT NULL, subject_employee_no VARCHAR(100) NOT NULL,
 subject_display_name VARCHAR(300) NOT NULL, appointment_id BIGINT NOT NULL,
 job_position_id BIGINT NOT NULL, plantilla_id BIGINT,
 hrm_source_fingerprint VARCHAR(128) NOT NULL,
 source_job_position_name VARCHAR(200) NOT NULL, source_plantilla_name VARCHAR(200),
 source_salary_grade BIGINT, source_salary_step BIGINT,
 position_profile_id VARCHAR(36) NOT NULL, position_profile_definition_version INTEGER NOT NULL,
 position_profile_content_revision BIGINT NOT NULL,
 person_profile_id VARCHAR(36) NOT NULL, person_profile_version INTEGER NOT NULL,
 person_profile_valid_from DATE NOT NULL, person_profile_valid_to DATE,
 priority_scheme_id VARCHAR(36) NOT NULL, priority_scheme_definition_version INTEGER NOT NULL,
 analysis_date DATE NOT NULL, request_key VARCHAR(100) NOT NULL,
 generated_by VARCHAR(100) NOT NULL, generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_gap_analysis_position_profile FOREIGN KEY(position_profile_id)
  REFERENCES "${primehrSchema}".prime_position_profile(id),
 CONSTRAINT fk_prime_gap_analysis_person_profile FOREIGN KEY(person_profile_id)
  REFERENCES "${primehrSchema}".prime_person_competency_profile(id),
 CONSTRAINT fk_prime_gap_analysis_priority_scheme FOREIGN KEY(priority_scheme_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_scheme(id),
 CONSTRAINT uk_prime_gap_analysis_request UNIQUE(agency_id,request_key),
 CONSTRAINT uk_prime_gap_analysis_source UNIQUE(agency_id,subject_employee_id,analysis_date,
  position_profile_id,person_profile_id,priority_scheme_id),
 CONSTRAINT ck_prime_gap_analysis_profile_versions CHECK(position_profile_definition_version>=1 AND person_profile_version>=1),
 CONSTRAINT ck_prime_gap_analysis_policy_version CHECK(priority_scheme_definition_version>=1),
 CONSTRAINT ck_prime_gap_analysis_person_dates CHECK(person_profile_valid_to IS NULL OR person_profile_valid_to>=person_profile_valid_from)
);

CREATE TABLE "${primehrSchema}".prime_competency_gap_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, analysis_id VARCHAR(36) NOT NULL,
 position_requirement_id VARCHAR(36) NOT NULL, competency_id VARCHAR(36) NOT NULL,
 person_result_id VARCHAR(36), required_proficiency_level_id VARCHAR(36) NOT NULL,
 attained_proficiency_level_id VARCHAR(36), priority_level_id VARCHAR(36), matched_rule_id VARCHAR(36),
 competency_code VARCHAR(50) NOT NULL, competency_name VARCHAR(200) NOT NULL,
 competency_definition_version INTEGER NOT NULL, scale_id VARCHAR(36) NOT NULL,
 scale_definition_version INTEGER NOT NULL,
 required_level_code VARCHAR(50) NOT NULL, required_level_label VARCHAR(150) NOT NULL,
 required_level_order INTEGER NOT NULL, attained_level_code VARCHAR(50), attained_level_label VARCHAR(150),
 attained_level_order INTEGER, gap_value INTEGER, gap_classification VARCHAR(30) NOT NULL,
 not_assessed_reason VARCHAR(40), requirement_classification VARCHAR(30) NOT NULL,
 criticality_code VARCHAR(50), priority_code VARCHAR(50), priority_label VARCHAR(150),
 priority_rank INTEGER, priority_explanation VARCHAR(1000), display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prime_gap_item_analysis FOREIGN KEY(analysis_id)
  REFERENCES "${primehrSchema}".prime_competency_gap_analysis(id),
 CONSTRAINT fk_prime_gap_item_requirement FOREIGN KEY(position_requirement_id)
  REFERENCES "${primehrSchema}".prime_position_profile_requirement(id),
 CONSTRAINT fk_prime_gap_item_competency FOREIGN KEY(competency_id)
  REFERENCES "${primehrSchema}".prime_competency(id),
 CONSTRAINT fk_prime_gap_item_person_result FOREIGN KEY(person_result_id)
  REFERENCES "${primehrSchema}".prime_person_competency_result(id),
 CONSTRAINT fk_prime_gap_item_required_level FOREIGN KEY(required_proficiency_level_id)
  REFERENCES "${primehrSchema}".prime_proficiency_level(id),
 CONSTRAINT fk_prime_gap_item_attained_level FOREIGN KEY(attained_proficiency_level_id)
  REFERENCES "${primehrSchema}".prime_proficiency_level(id),
 CONSTRAINT fk_prime_gap_item_priority_level FOREIGN KEY(priority_level_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_level(id),
 CONSTRAINT fk_prime_gap_item_rule FOREIGN KEY(matched_rule_id)
  REFERENCES "${primehrSchema}".prime_gap_priority_rule(id),
 CONSTRAINT uk_prime_gap_item_competency UNIQUE(analysis_id,competency_id),
 CONSTRAINT ck_prime_gap_item_classification CHECK(gap_classification IN ('BELOW','MEETS','EXCEEDS','NOT_ASSESSED')),
 CONSTRAINT ck_prime_gap_item_not_assessed_reason CHECK(not_assessed_reason IS NULL OR not_assessed_reason IN ('NO_RESULT','VERSION_NOT_COMPARABLE')),
 CONSTRAINT ck_prime_gap_item_requirement CHECK(requirement_classification IN ('MANDATORY','DESIRABLE')),
 CONSTRAINT ck_prime_gap_item_definition_versions CHECK(competency_definition_version>=1 AND scale_definition_version>=1),
 CONSTRAINT ck_prime_gap_item_values CHECK(
  (gap_classification='NOT_ASSESSED' AND gap_value IS NULL AND attained_level_order IS NULL AND not_assessed_reason IS NOT NULL)
 OR (gap_classification<>'NOT_ASSESSED' AND gap_value IS NOT NULL AND attained_level_order IS NOT NULL AND not_assessed_reason IS NULL)),
 CONSTRAINT ck_prime_gap_item_formula CHECK(gap_value IS NULL OR gap_value=required_level_order-attained_level_order),
 CONSTRAINT ck_prime_gap_item_priority CHECK(
  (gap_classification IN ('BELOW','NOT_ASSESSED') AND priority_level_id IS NOT NULL AND priority_code IS NOT NULL AND priority_rank IS NOT NULL)
  OR (gap_classification IN ('MEETS','EXCEEDS') AND priority_level_id IS NULL AND priority_code IS NULL AND priority_rank IS NULL)),
 CONSTRAINT ck_prime_gap_item_orders CHECK(required_level_order>=1 AND (attained_level_order IS NULL OR attained_level_order>=1) AND display_order>=0)
);

CREATE INDEX ix_prime_gap_scheme_effective ON "${primehrSchema}".prime_gap_priority_scheme(agency_id,status,effective_from,effective_to);
CREATE INDEX ix_prime_gap_level_scheme ON "${primehrSchema}".prime_gap_priority_level(scheme_id,active,priority_rank);
CREATE INDEX ix_prime_gap_rule_scheme ON "${primehrSchema}".prime_gap_priority_rule(scheme_id,active,display_order);
CREATE INDEX ix_prime_gap_analysis_employee ON "${primehrSchema}".prime_competency_gap_analysis(agency_id,subject_employee_no,analysis_date);
CREATE INDEX ix_prime_gap_analysis_profiles ON "${primehrSchema}".prime_competency_gap_analysis(position_profile_id,person_profile_id,priority_scheme_id);
CREATE INDEX ix_prime_gap_item_filter ON "${primehrSchema}".prime_competency_gap_item(analysis_id,gap_classification,priority_code);

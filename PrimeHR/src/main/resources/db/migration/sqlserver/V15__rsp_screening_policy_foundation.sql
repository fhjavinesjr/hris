CREATE TABLE [${primehrSchema}].rsp_screening_policy (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_policy PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL,
 name NVARCHAR(200) NOT NULL, description NVARCHAR(2000) NULL, definition_version INT NOT NULL,
 supersedes_id VARCHAR(36) NULL, status VARCHAR(20) NOT NULL,
 effective_from DATE NULL, effective_to DATE NULL, published_by VARCHAR(100) NULL,
 published_at DATETIMEOFFSET NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_policy_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].rsp_screening_policy(id),
 CONSTRAINT uk_rsp_screening_policy_version UNIQUE(agency_id,normalized_code,definition_version),
 CONSTRAINT ck_rsp_screening_policy_version CHECK(definition_version>=1),
 CONSTRAINT ck_rsp_screening_policy_status CHECK(status IN ('DRAFT','PUBLISHED','SUPERSEDED')),
 CONSTRAINT ck_rsp_screening_policy_dates CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_rsp_screening_policy_publish CHECK((status='DRAFT' AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','SUPERSEDED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
);
CREATE INDEX ix_rsp_screening_policy_lookup ON [${primehrSchema}].rsp_screening_policy(agency_id,status,effective_from,effective_to);

CREATE TABLE [${primehrSchema}].rsp_screening_policy_criterion (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_criterion PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, policy_id VARCHAR(36) NOT NULL,
 code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL, label NVARCHAR(300) NOT NULL,
 internal_instructions NVARCHAR(2000) NULL, public_guidance NVARCHAR(1000) NULL,
 category VARCHAR(40) NOT NULL, evaluation_mode VARCHAR(30) NOT NULL, source_key VARCHAR(100) NULL,
 threshold_value DECIMAL(19,4) NULL, threshold_unit VARCHAR(30) NULL,
 mandatory BIT NOT NULL, disqualifying BIT NOT NULL, allows_not_applicable BIT NOT NULL,
 requires_remarks BIT NOT NULL, requires_evidence BIT NOT NULL, display_order INT NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_criterion_policy FOREIGN KEY(policy_id) REFERENCES [${primehrSchema}].rsp_screening_policy(id),
 CONSTRAINT uk_rsp_screening_criterion_code UNIQUE(agency_id,policy_id,normalized_code),
 CONSTRAINT uk_rsp_screening_criterion_order UNIQUE(agency_id,policy_id,display_order),
 CONSTRAINT ck_rsp_screening_criterion_order CHECK(display_order>=0),
 CONSTRAINT ck_rsp_screening_criterion_category CHECK(category IN ('REQUIRED_DOCUMENT','EDUCATION','TRAINING','EXPERIENCE','ELIGIBILITY','LICENSE','COMPETENCY_PREREQUISITE','SCREENING_QUESTION')),
 CONSTRAINT ck_rsp_screening_criterion_mode CHECK(evaluation_mode IN ('PRESENCE','NUMERIC_THRESHOLD','DATE_OR_DURATION','MANUAL_REVIEW','DECLARATION')),
 CONSTRAINT ck_rsp_screening_criterion_disqualifying CHECK(disqualifying=0 OR mandatory=1),
 CONSTRAINT ck_rsp_screening_criterion_numeric CHECK(evaluation_mode<>'NUMERIC_THRESHOLD' OR (threshold_value IS NOT NULL AND threshold_unit IS NOT NULL)),
 CONSTRAINT ck_rsp_screening_criterion_source CHECK(evaluation_mode='MANUAL_REVIEW' OR source_key IS NOT NULL)
);
CREATE INDEX ix_rsp_screening_criterion_policy ON [${primehrSchema}].rsp_screening_policy_criterion(agency_id,policy_id,display_order);

CREATE TABLE [${primehrSchema}].rsp_screening_reason_code (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_reason PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, policy_id VARCHAR(36) NOT NULL,
 code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL, label NVARCHAR(300) NOT NULL,
 public_safe_text NVARCHAR(1000) NOT NULL, outcome_compatibility VARCHAR(20) NOT NULL,
 remarks_required BIT NOT NULL, display_order INT NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_reason_policy FOREIGN KEY(policy_id) REFERENCES [${primehrSchema}].rsp_screening_policy(id),
 CONSTRAINT uk_rsp_screening_reason_code UNIQUE(agency_id,policy_id,normalized_code),
 CONSTRAINT uk_rsp_screening_reason_order UNIQUE(agency_id,policy_id,display_order),
 CONSTRAINT ck_rsp_screening_reason_order CHECK(display_order>=0),
 CONSTRAINT ck_rsp_screening_reason_outcome CHECK(outcome_compatibility IN ('QUALIFIED','DISQUALIFIED'))
);
CREATE INDEX ix_rsp_screening_reason_policy ON [${primehrSchema}].rsp_screening_reason_code(agency_id,policy_id,display_order);

CREATE TABLE [${primehrSchema}].rsp_publication_screening_policy (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_publication_screening PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, vacancy_publication_id VARCHAR(36) NOT NULL,
 screening_policy_id VARCHAR(36) NOT NULL, policy_snapshot NVARCHAR(MAX) NOT NULL,
 policy_fingerprint VARCHAR(64) NOT NULL, bound_by VARCHAR(100) NOT NULL, bound_at DATETIMEOFFSET NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_publication_screening_vacancy FOREIGN KEY(vacancy_publication_id) REFERENCES [${primehrSchema}].rsp_vacancy_publication(id),
 CONSTRAINT fk_rsp_publication_screening_policy FOREIGN KEY(screening_policy_id) REFERENCES [${primehrSchema}].rsp_screening_policy(id),
 CONSTRAINT uk_rsp_publication_screening_policy UNIQUE(agency_id,vacancy_publication_id)
);
CREATE INDEX ix_rsp_publication_screening_lookup ON [${primehrSchema}].rsp_publication_screening_policy(agency_id,screening_policy_id);

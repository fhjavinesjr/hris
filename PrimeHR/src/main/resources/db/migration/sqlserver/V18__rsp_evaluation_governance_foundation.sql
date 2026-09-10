CREATE TABLE [${primehrSchema}].rsp_evaluation_policy (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_policy PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL,
 name NVARCHAR(200) NOT NULL, description NVARCHAR(2000) NULL, merit_selection_plan_reference NVARCHAR(500) NOT NULL,
 definition_version INT NOT NULL, supersedes_id VARCHAR(36) NULL, status VARCHAR(20) NOT NULL,
 effective_from DATE NULL, effective_to DATE NULL, rounding_scale INT NOT NULL,
 rounding_mode VARCHAR(20) NOT NULL, tie_rule VARCHAR(30) NOT NULL, aggregation_mode VARCHAR(40) NOT NULL,
 published_by VARCHAR(100) NULL, published_at DATETIMEOFFSET NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_evaluation_policy_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy(id),
 CONSTRAINT uk_rsp_evaluation_policy_version UNIQUE(agency_id,normalized_code,definition_version),
 CONSTRAINT ck_rsp_evaluation_policy_version CHECK(definition_version>=1),
 CONSTRAINT ck_rsp_evaluation_policy_status CHECK(status IN ('DRAFT','PUBLISHED','SUPERSEDED')),
 CONSTRAINT ck_rsp_evaluation_policy_dates CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_rsp_evaluation_policy_rounding CHECK(rounding_scale BETWEEN 0 AND 6 AND rounding_mode IN ('HALF_UP','HALF_EVEN','DOWN')),
 CONSTRAINT ck_rsp_evaluation_policy_tie CHECK(tie_rule IN ('COMPETITION','DENSE')),
 CONSTRAINT ck_rsp_evaluation_policy_aggregation CHECK(aggregation_mode='NORMALIZED_WEIGHTED_SUM'),
 CONSTRAINT ck_rsp_evaluation_policy_publish CHECK((status='DRAFT' AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','SUPERSEDED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
);
CREATE INDEX ix_rsp_evaluation_policy_lookup ON [${primehrSchema}].rsp_evaluation_policy(agency_id,status,effective_from,effective_to);

CREATE TABLE [${primehrSchema}].rsp_evaluation_policy_stage (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_stage PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, policy_id VARCHAR(36) NOT NULL, code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL, name NVARCHAR(200) NOT NULL, instructions NVARCHAR(2000) NULL,
 stage_type VARCHAR(40) NOT NULL, stage_mode VARCHAR(20) NOT NULL, weight DECIMAL(7,4) NOT NULL,
 maximum_score DECIMAL(12,4) NULL, passing_score DECIMAL(12,4) NULL, minimum_raters INT NOT NULL,
 requires_evidence BIT NOT NULL, display_order INT NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_evaluation_stage_policy FOREIGN KEY(policy_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy(id),
 CONSTRAINT uk_rsp_evaluation_stage_code UNIQUE(agency_id,policy_id,normalized_code),
 CONSTRAINT uk_rsp_evaluation_stage_order UNIQUE(agency_id,policy_id,display_order),
 CONSTRAINT ck_rsp_evaluation_stage_type CHECK(stage_type IN ('WRITTEN_EXAMINATION','TECHNICAL_SKILLS_TEST','COMPETENCY_BASED_INTERVIEW','BEHAVIORAL_EVENT_INTERVIEW','BACKGROUND_REFERENCE_CHECK','OTHER_CONFIGURED')),
 CONSTRAINT ck_rsp_evaluation_stage_mode CHECK(stage_mode IN ('SCORED','GATE','INFORMATIONAL')),
 CONSTRAINT ck_rsp_evaluation_stage_values CHECK(display_order>=0 AND minimum_raters>=1 AND weight>=0 AND weight<=100 AND (maximum_score IS NULL OR maximum_score>0) AND (passing_score IS NULL OR passing_score>=0) AND (passing_score IS NULL OR maximum_score IS NULL OR passing_score<=maximum_score)),
 CONSTRAINT ck_rsp_evaluation_stage_weight CHECK((stage_mode='SCORED' AND weight>0 AND maximum_score IS NOT NULL) OR (stage_mode<>'SCORED' AND weight=0))
);
CREATE INDEX ix_rsp_evaluation_stage_policy ON [${primehrSchema}].rsp_evaluation_policy_stage(agency_id,policy_id,display_order);

CREATE TABLE [${primehrSchema}].rsp_evaluation_policy_criterion (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_criterion PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, policy_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL,
 code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL, label NVARCHAR(300) NOT NULL,
 instructions NVARCHAR(2000) NULL, weight DECIMAL(7,4) NOT NULL, maximum_score DECIMAL(12,4) NULL,
 requires_remarks BIT NOT NULL, display_order INT NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_evaluation_criterion_policy FOREIGN KEY(policy_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy(id),
 CONSTRAINT fk_rsp_evaluation_criterion_stage FOREIGN KEY(stage_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_stage(id),
 CONSTRAINT uk_rsp_evaluation_criterion_code UNIQUE(agency_id,stage_id,normalized_code),
 CONSTRAINT uk_rsp_evaluation_criterion_order UNIQUE(agency_id,stage_id,display_order),
 CONSTRAINT ck_rsp_evaluation_criterion_values CHECK(display_order>=0 AND weight>=0 AND weight<=100 AND (maximum_score IS NULL OR maximum_score>0))
);
CREATE INDEX ix_rsp_evaluation_criterion_stage ON [${primehrSchema}].rsp_evaluation_policy_criterion(agency_id,stage_id,display_order);

CREATE TABLE [${primehrSchema}].rsp_publication_evaluation_policy (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_publication_evaluation PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, vacancy_publication_id VARCHAR(36) NOT NULL,
 evaluation_policy_id VARCHAR(36) NOT NULL, policy_snapshot NVARCHAR(MAX) NOT NULL,
 policy_fingerprint VARCHAR(64) NOT NULL, bound_by VARCHAR(100) NOT NULL, bound_at DATETIMEOFFSET NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_publication_evaluation_vacancy FOREIGN KEY(vacancy_publication_id) REFERENCES [${primehrSchema}].rsp_vacancy_publication(id),
 CONSTRAINT fk_rsp_publication_evaluation_policy FOREIGN KEY(evaluation_policy_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy(id),
 CONSTRAINT uk_rsp_publication_evaluation_policy UNIQUE(agency_id,vacancy_publication_id)
);
CREATE INDEX ix_rsp_publication_evaluation_lookup ON [${primehrSchema}].rsp_publication_evaluation_policy(agency_id,evaluation_policy_id);

CREATE TABLE [${primehrSchema}].prime_committee (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_committee PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL,
 committee_type VARCHAR(30) NOT NULL, name NVARCHAR(200) NOT NULL, legal_basis NVARCHAR(1000) NOT NULL,
 definition_version INT NOT NULL, supersedes_id VARCHAR(36) NULL, status VARCHAR(20) NOT NULL,
 effective_from DATE NULL, effective_to DATE NULL, minimum_voting_members INT NOT NULL,
 published_by VARCHAR(100) NULL, published_at DATETIMEOFFSET NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_committee_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].prime_committee(id),
 CONSTRAINT uk_prime_committee_version UNIQUE(agency_id,normalized_code,definition_version),
 CONSTRAINT ck_prime_committee_type CHECK(committee_type='HRMPSB'),
 CONSTRAINT ck_prime_committee_status CHECK(status IN ('DRAFT','PUBLISHED','SUPERSEDED')),
 CONSTRAINT ck_prime_committee_dates CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from),
 CONSTRAINT ck_prime_committee_voters CHECK(minimum_voting_members>=1),
 CONSTRAINT ck_prime_committee_publish CHECK((status='DRAFT' AND published_by IS NULL AND published_at IS NULL) OR (status IN ('PUBLISHED','SUPERSEDED') AND effective_from IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
);
CREATE INDEX ix_prime_committee_lookup ON [${primehrSchema}].prime_committee(agency_id,committee_type,status,effective_from,effective_to);

CREATE TABLE [${primehrSchema}].prime_committee_member (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_committee_member PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, committee_id VARCHAR(36) NOT NULL, employee_no VARCHAR(100) NOT NULL,
 member_role VARCHAR(30) NOT NULL, representation NVARCHAR(300) NULL, effective_from DATE NOT NULL,
 effective_to DATE NULL, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_committee_member_committee FOREIGN KEY(committee_id) REFERENCES [${primehrSchema}].prime_committee(id),
 CONSTRAINT uk_prime_committee_member UNIQUE(agency_id,committee_id,employee_no),
 CONSTRAINT ck_prime_committee_member_role CHECK(member_role IN ('CHAIRPERSON','VICE_CHAIRPERSON','MEMBER','SECRETARIAT','ALTERNATE','OBSERVER')),
 CONSTRAINT ck_prime_committee_member_dates CHECK(effective_to IS NULL OR effective_to>=effective_from)
);
CREATE INDEX ix_prime_committee_member_employee ON [${primehrSchema}].prime_committee_member(agency_id,employee_no,effective_from,effective_to);

CREATE TABLE [${primehrSchema}].rsp_evaluation_proceeding (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_proceeding PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, vacancy_publication_id VARCHAR(36) NOT NULL,
 publication_policy_binding_id VARCHAR(36) NOT NULL, committee_id VARCHAR(36) NOT NULL,
 planned_as_of_date DATE NOT NULL, status VARCHAR(30) NOT NULL, candidate_set_revision INT NOT NULL DEFAULT 0, current_publication_key VARCHAR(36) NULL,
 vacancy_snapshot NVARCHAR(MAX) NOT NULL, policy_snapshot NVARCHAR(MAX) NOT NULL,
 committee_snapshot NVARCHAR(MAX) NOT NULL, vacancy_fingerprint VARCHAR(64) NOT NULL,
 policy_fingerprint VARCHAR(64) NOT NULL, committee_fingerprint VARCHAR(64) NOT NULL,
 opened_by VARCHAR(100) NULL, opened_at DATETIMEOFFSET NULL, cancelled_by VARCHAR(100) NULL,
 cancelled_at DATETIMEOFFSET NULL, cancellation_reason NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_evaluation_proceeding_vacancy FOREIGN KEY(vacancy_publication_id) REFERENCES [${primehrSchema}].rsp_vacancy_publication(id),
 CONSTRAINT fk_rsp_evaluation_proceeding_binding FOREIGN KEY(publication_policy_binding_id) REFERENCES [${primehrSchema}].rsp_publication_evaluation_policy(id),
 CONSTRAINT fk_rsp_evaluation_proceeding_committee FOREIGN KEY(committee_id) REFERENCES [${primehrSchema}].prime_committee(id),
 CONSTRAINT uk_rsp_evaluation_current_publication UNIQUE(agency_id,current_publication_key),
 CONSTRAINT ck_rsp_evaluation_proceeding_status CHECK(status IN ('DRAFT','OPEN','IN_ASSESSMENT','FOR_DELIBERATION','FINALIZED','CANCELLED')),
 CONSTRAINT ck_rsp_evaluation_candidate_revision CHECK(candidate_set_revision>=0),
 CONSTRAINT ck_rsp_evaluation_proceeding_lifecycle CHECK((status='DRAFT' AND opened_by IS NULL AND opened_at IS NULL AND cancelled_by IS NULL AND cancelled_at IS NULL) OR (status IN ('OPEN','IN_ASSESSMENT','FOR_DELIBERATION','FINALIZED') AND opened_by IS NOT NULL AND opened_at IS NOT NULL AND cancelled_by IS NULL AND cancelled_at IS NULL) OR (status='CANCELLED' AND cancelled_by IS NOT NULL AND cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL))
);
CREATE INDEX ix_rsp_evaluation_proceeding_queue ON [${primehrSchema}].rsp_evaluation_proceeding(agency_id,status,planned_as_of_date);
CREATE INDEX ix_rsp_evaluation_proceeding_committee ON [${primehrSchema}].rsp_evaluation_proceeding(agency_id,committee_id,status);

CREATE TABLE [${primehrSchema}].rsp_evaluation_candidate (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_candidate PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL, application_id VARCHAR(36) NOT NULL,
 screening_case_id VARCHAR(36) NOT NULL, applicant_id VARCHAR(36) NOT NULL, status VARCHAR(20) NOT NULL,
 current_application_key VARCHAR(36) NULL, application_version INT NOT NULL, screening_case_revision INT NOT NULL,
 application_snapshot NVARCHAR(MAX) NOT NULL, screening_snapshot NVARCHAR(MAX) NOT NULL,
 application_fingerprint VARCHAR(64) NOT NULL, screening_fingerprint VARCHAR(64) NOT NULL,
 admitted_by VARCHAR(100) NOT NULL, admitted_at DATETIMEOFFSET NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_evaluation_candidate_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_evaluation_candidate_application FOREIGN KEY(application_id) REFERENCES [${primehrSchema}].rsp_position_application(id),
 CONSTRAINT fk_rsp_evaluation_candidate_screening FOREIGN KEY(screening_case_id) REFERENCES [${primehrSchema}].rsp_screening_case(id),
 CONSTRAINT uk_rsp_evaluation_candidate_application UNIQUE(agency_id,proceeding_id,application_id),
 CONSTRAINT uk_rsp_evaluation_candidate_current UNIQUE(agency_id,proceeding_id,current_application_key),
 CONSTRAINT ck_rsp_evaluation_candidate_status CHECK(status='ADMITTED'),
 CONSTRAINT ck_rsp_evaluation_candidate_versions CHECK(application_version>=1 AND screening_case_revision>=1)
);
CREATE INDEX ix_rsp_evaluation_candidate_application ON [${primehrSchema}].rsp_evaluation_candidate(agency_id,application_id,status);
CREATE INDEX ix_rsp_evaluation_candidate_proceeding ON [${primehrSchema}].rsp_evaluation_candidate(agency_id,proceeding_id,status);

ALTER TABLE [${primehrSchema}].rsp_position_application DROP CONSTRAINT ck_rsp_application_status;
ALTER TABLE [${primehrSchema}].rsp_position_application DROP CONSTRAINT ck_rsp_application_safe_status;
ALTER TABLE [${primehrSchema}].rsp_position_application ADD CONSTRAINT ck_rsp_application_status CHECK(status IN ('DRAFT','SUBMITTED','UNDER_SCREENING','QUALIFIED','DISQUALIFIED','WITHDRAWN'));
ALTER TABLE [${primehrSchema}].rsp_position_application ADD CONSTRAINT ck_rsp_application_safe_status CHECK(safe_status IN ('DRAFT','SUBMITTED','UNDER REVIEW','QUALIFIED','NOT QUALIFIED','WITHDRAWN'));

CREATE TABLE [${primehrSchema}].rsp_screening_case (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_case PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 application_id VARCHAR(36) NOT NULL, vacancy_publication_id VARCHAR(36) NOT NULL,
 screening_policy_id VARCHAR(36) NOT NULL, policy_definition_version INT NOT NULL, case_revision INT NOT NULL,
 supersedes_id VARCHAR(36) NULL, current_application_key VARCHAR(36) NULL, status VARCHAR(20) NOT NULL,
 recommendation VARCHAR(20) NULL, recommendation_reason_code_id VARCHAR(36) NULL,
 recommendation_reason_code VARCHAR(80) NULL, recommendation_explanation NVARCHAR(2000) NULL,
 recommendation_safe_reason NVARCHAR(1000) NULL, policy_snapshot NVARCHAR(MAX) NOT NULL, application_snapshot NVARCHAR(MAX) NOT NULL,
 opened_by VARCHAR(100) NOT NULL, opened_at DATETIMEOFFSET NOT NULL, submitted_by VARCHAR(100) NULL,
 submitted_at DATETIMEOFFSET NULL, returned_by VARCHAR(100) NULL, returned_at DATETIMEOFFSET NULL,
 return_reason NVARCHAR(2000) NULL, finalized_by VARCHAR(100) NULL, finalized_at DATETIMEOFFSET NULL,
 cancelled_by VARCHAR(100) NULL, cancelled_at DATETIMEOFFSET NULL, cancellation_reason NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_case_application FOREIGN KEY(application_id) REFERENCES [${primehrSchema}].rsp_position_application(id),
 CONSTRAINT fk_rsp_screening_case_publication FOREIGN KEY(vacancy_publication_id) REFERENCES [${primehrSchema}].rsp_vacancy_publication(id),
 CONSTRAINT fk_rsp_screening_case_policy FOREIGN KEY(screening_policy_id) REFERENCES [${primehrSchema}].rsp_screening_policy(id),
 CONSTRAINT fk_rsp_screening_case_reason FOREIGN KEY(recommendation_reason_code_id) REFERENCES [${primehrSchema}].rsp_screening_reason_code(id),
 CONSTRAINT fk_rsp_screening_case_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].rsp_screening_case(id),
 CONSTRAINT uk_rsp_screening_case_revision UNIQUE(agency_id,application_id,case_revision),
 CONSTRAINT ck_rsp_screening_case_version CHECK(policy_definition_version>=1 AND case_revision>=1),
 CONSTRAINT ck_rsp_screening_case_status CHECK(status IN ('DRAFT','RETURNED','SUBMITTED','QUALIFIED','DISQUALIFIED','CANCELLED')),
 CONSTRAINT ck_rsp_screening_case_recommendation CHECK(recommendation IS NULL OR recommendation IN ('QUALIFIED','DISQUALIFIED')),
 CONSTRAINT ck_rsp_screening_case_current CHECK(current_application_key IS NULL OR current_application_key=application_id)
);
CREATE UNIQUE INDEX uk_rsp_screening_case_current ON [${primehrSchema}].rsp_screening_case(agency_id,current_application_key) WHERE current_application_key IS NOT NULL;
CREATE INDEX ix_rsp_screening_case_queue ON [${primehrSchema}].rsp_screening_case(agency_id,status,opened_at);
CREATE INDEX ix_rsp_screening_case_publication ON [${primehrSchema}].rsp_screening_case(agency_id,vacancy_publication_id,status);

CREATE TABLE [${primehrSchema}].rsp_screening_assignment (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_assignment PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 case_id VARCHAR(36) NOT NULL, employee_no VARCHAR(100) NOT NULL, process_role VARCHAR(20) NOT NULL,
 active BIT NOT NULL, assigned_by VARCHAR(100) NOT NULL, assigned_at DATETIMEOFFSET NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_assignment_case FOREIGN KEY(case_id) REFERENCES [${primehrSchema}].rsp_screening_case(id),
 CONSTRAINT uk_rsp_screening_assignment UNIQUE(agency_id,case_id,employee_no,process_role),
 CONSTRAINT ck_rsp_screening_assignment_role CHECK(process_role IN ('SCREENER','VALIDATOR'))
);
CREATE INDEX ix_rsp_screening_assignment_queue ON [${primehrSchema}].rsp_screening_assignment(agency_id,employee_no,process_role,active);

CREATE TABLE [${primehrSchema}].rsp_screening_finding (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_finding PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 case_id VARCHAR(36) NOT NULL, criterion_id VARCHAR(36) NOT NULL, criterion_code VARCHAR(80) NOT NULL,
 criterion_label NVARCHAR(300) NOT NULL, mandatory BIT NOT NULL, disqualifying BIT NOT NULL,
 allows_not_applicable BIT NOT NULL, requires_remarks BIT NOT NULL, requires_evidence BIT NOT NULL,
 display_order INT NOT NULL, result VARCHAR(30) NULL, remarks NVARCHAR(2000) NULL, human_confirmed BIT NOT NULL,
 confirmed_by VARCHAR(100) NULL, confirmed_at DATETIMEOFFSET NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_finding_case FOREIGN KEY(case_id) REFERENCES [${primehrSchema}].rsp_screening_case(id),
 CONSTRAINT fk_rsp_screening_finding_criterion FOREIGN KEY(criterion_id) REFERENCES [${primehrSchema}].rsp_screening_policy_criterion(id),
 CONSTRAINT uk_rsp_screening_finding UNIQUE(agency_id,case_id,criterion_id),
 CONSTRAINT ck_rsp_screening_finding_result CHECK(result IS NULL OR result IN ('MET','NOT_MET','NEEDS_REVIEW','NOT_APPLICABLE')),
 CONSTRAINT ck_rsp_screening_finding_order CHECK(display_order>=0)
);
CREATE INDEX ix_rsp_screening_finding_case ON [${primehrSchema}].rsp_screening_finding(agency_id,case_id,display_order);

CREATE TABLE [${primehrSchema}].rsp_screening_evidence_link (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_evidence PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 case_id VARCHAR(36) NOT NULL, finding_id VARCHAR(36) NOT NULL, evidence_type VARCHAR(50) NOT NULL,
 reference_id VARCHAR(200) NOT NULL, label NVARCHAR(500) NOT NULL, staff_declaration NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_evidence_case FOREIGN KEY(case_id) REFERENCES [${primehrSchema}].rsp_screening_case(id),
 CONSTRAINT fk_rsp_screening_evidence_finding FOREIGN KEY(finding_id) REFERENCES [${primehrSchema}].rsp_screening_finding(id),
 CONSTRAINT uk_rsp_screening_evidence UNIQUE(agency_id,finding_id,evidence_type,reference_id),
 CONSTRAINT ck_rsp_screening_evidence_type CHECK(evidence_type IN ('APPLICATION_DOCUMENT','PROFILE_SNAPSHOT','VACANCY_SNAPSHOT','QUALIFICATION_STANDARD_SNAPSHOT','COMPETENCY_SNAPSHOT','STAFF_DECLARATION'))
);
CREATE INDEX ix_rsp_screening_evidence_case ON [${primehrSchema}].rsp_screening_evidence_link(agency_id,case_id,finding_id);

CREATE TABLE [${primehrSchema}].rsp_screening_decision (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_screening_decision PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 case_id VARCHAR(36) NOT NULL, outcome VARCHAR(20) NOT NULL, reason_code_id VARCHAR(36) NULL,
 reason_code VARCHAR(80) NULL, internal_explanation NVARCHAR(2000) NULL,
 applicant_safe_reason NVARCHAR(1000) NOT NULL, recommended_by VARCHAR(100) NOT NULL,
 validated_by VARCHAR(100) NOT NULL, decided_at DATETIMEOFFSET NOT NULL, administrator_override BIT NOT NULL,
 override_reason NVARCHAR(2000) NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_screening_decision_case FOREIGN KEY(case_id) REFERENCES [${primehrSchema}].rsp_screening_case(id),
 CONSTRAINT fk_rsp_screening_decision_reason FOREIGN KEY(reason_code_id) REFERENCES [${primehrSchema}].rsp_screening_reason_code(id),
 CONSTRAINT uk_rsp_screening_decision_case UNIQUE(agency_id,case_id),
 CONSTRAINT ck_rsp_screening_decision_outcome CHECK(outcome IN ('QUALIFIED','DISQUALIFIED')),
 CONSTRAINT ck_rsp_screening_decision_override CHECK((administrator_override=0 AND override_reason IS NULL) OR (administrator_override=1 AND override_reason IS NOT NULL))
);
CREATE INDEX ix_rsp_screening_decision_outcome ON [${primehrSchema}].rsp_screening_decision(agency_id,outcome,decided_at);

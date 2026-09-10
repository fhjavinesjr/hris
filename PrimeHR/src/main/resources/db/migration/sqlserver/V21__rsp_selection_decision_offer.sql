CREATE TABLE ${primehrSchema}.rsp_selection_case (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 vacancy_publication_id VARCHAR(36) NOT NULL, comparative_evaluation_id VARCHAR(36) NOT NULL,
 comparative_fingerprint VARCHAR(64) NOT NULL, meeting_id VARCHAR(36) NOT NULL, case_revision INT NOT NULL,
 supersedes_id VARCHAR(36) NULL, current_publication_key VARCHAR(36) NULL, status VARCHAR(20) NOT NULL,
 outcome VARCHAR(20) NULL, selected_candidate_id VARCHAR(36) NULL, assigned_approver_employee_no VARCHAR(100) NOT NULL,
 decision_reason NVARCHAR(3000) NULL, variance_basis NVARCHAR(1000) NULL, variance_reason NVARCHAR(3000) NULL,
 review_date DATE NULL, offer_response_deadline DATETIME2 NOT NULL, selected_safe_text NVARCHAR(2000) NOT NULL,
 non_selected_safe_text NVARCHAR(2000) NOT NULL, source_snapshot NVARCHAR(MAX) NOT NULL,
 source_fingerprint VARCHAR(64) NOT NULL, submitted_by VARCHAR(100) NULL, submitted_at DATETIME2 NULL,
 approved_by VARCHAR(100) NULL, approved_at DATETIME2 NULL, finalized_by VARCHAR(100) NULL, finalized_at DATETIME2 NULL,
 returned_by VARCHAR(100) NULL, returned_at DATETIME2 NULL, return_reason NVARCHAR(2000) NULL,
 administrator_exception_reason NVARCHAR(2000) NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIME2 NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIME2 NOT NULL,
 CONSTRAINT fk_rsp_selection_proceeding FOREIGN KEY(proceeding_id) REFERENCES ${primehrSchema}.rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_selection_publication FOREIGN KEY(vacancy_publication_id) REFERENCES ${primehrSchema}.rsp_vacancy_publication(id),
 CONSTRAINT fk_rsp_selection_comparative FOREIGN KEY(comparative_evaluation_id) REFERENCES ${primehrSchema}.rsp_comparative_evaluation(id),
 CONSTRAINT fk_rsp_selection_meeting FOREIGN KEY(meeting_id) REFERENCES ${primehrSchema}.rsp_hrmpsb_meeting(id),
 CONSTRAINT fk_rsp_selection_prior FOREIGN KEY(supersedes_id) REFERENCES ${primehrSchema}.rsp_selection_case(id),
 CONSTRAINT uk_rsp_selection_revision UNIQUE(agency_id,proceeding_id,case_revision),
 CONSTRAINT ck_rsp_selection_revision CHECK(case_revision>=1),
 CONSTRAINT ck_rsp_selection_status CHECK(status IN ('DRAFT','SUBMITTED','RETURNED','APPROVED','FINALIZED','CANCELLED','SUPERSEDED')),
 CONSTRAINT ck_rsp_selection_outcome CHECK(outcome IS NULL OR outcome IN ('SELECTED','NO_SELECTION','DEFERRED')),
 CONSTRAINT ck_rsp_selection_choice CHECK((outcome='SELECTED' AND selected_candidate_id IS NOT NULL AND review_date IS NULL) OR (outcome='DEFERRED' AND selected_candidate_id IS NULL AND review_date IS NOT NULL) OR (outcome='NO_SELECTION' AND selected_candidate_id IS NULL AND review_date IS NULL) OR outcome IS NULL),
 CONSTRAINT ck_rsp_selection_variance CHECK((variance_basis IS NULL AND variance_reason IS NULL) OR (variance_basis IS NOT NULL AND variance_reason IS NOT NULL)),
 CONSTRAINT ck_rsp_selection_submit CHECK(status NOT IN ('SUBMITTED','APPROVED','FINALIZED') OR (submitted_by IS NOT NULL AND submitted_at IS NOT NULL)),
 CONSTRAINT ck_rsp_selection_approve CHECK(status NOT IN ('APPROVED','FINALIZED') OR (approved_by IS NOT NULL AND approved_at IS NOT NULL)),
 CONSTRAINT ck_rsp_selection_finalize CHECK(status<>'FINALIZED' OR (finalized_by IS NOT NULL AND finalized_at IS NOT NULL))
);
CREATE UNIQUE INDEX uk_rsp_selection_current ON ${primehrSchema}.rsp_selection_case(agency_id,current_publication_key) WHERE current_publication_key IS NOT NULL;
CREATE INDEX ix_rsp_selection_source ON ${primehrSchema}.rsp_selection_case(agency_id,proceeding_id,status);

CREATE TABLE ${primehrSchema}.rsp_selection_candidate_decision (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, selection_case_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36) NOT NULL, application_id VARCHAR(36) NOT NULL, applicant_id VARCHAR(36) NOT NULL,
 total_score DECIMAL(12,4) NOT NULL, rank_number INT NOT NULL, tie_group INT NOT NULL, excluded BIT NOT NULL,
 exclusion_reason NVARCHAR(1000) NULL, recommendation VARCHAR(40) NOT NULL, recommendation_reason NVARCHAR(3000) NOT NULL,
 selected_case_key VARCHAR(36) NULL, selected BIT NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIME2 NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIME2 NOT NULL,
 CONSTRAINT fk_rsp_selection_candidate_case FOREIGN KEY(selection_case_id) REFERENCES ${primehrSchema}.rsp_selection_case(id),
 CONSTRAINT fk_rsp_selection_candidate_source FOREIGN KEY(candidate_id) REFERENCES ${primehrSchema}.rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_selection_candidate_app FOREIGN KEY(application_id) REFERENCES ${primehrSchema}.rsp_position_application(id),
 CONSTRAINT uk_rsp_selection_candidate UNIQUE(agency_id,selection_case_id,candidate_id),
 CONSTRAINT uk_rsp_selection_application UNIQUE(agency_id,selection_case_id,application_id),
 CONSTRAINT ck_rsp_selection_candidate_values CHECK(total_score>=0 AND total_score<=100 AND rank_number>=1 AND tie_group>=1),
 CONSTRAINT ck_rsp_selection_candidate_recommendation CHECK(recommendation IN ('ENDORSED_FOR_SELECTION_DECISION','NOT_ENDORSED','DEFERRED')),
 CONSTRAINT ck_rsp_selection_candidate_selected CHECK((selected=1 AND selected_case_key=selection_case_id AND excluded=0) OR (selected=0 AND selected_case_key IS NULL))
);
CREATE UNIQUE INDEX uk_rsp_selection_one_selected ON ${primehrSchema}.rsp_selection_candidate_decision(agency_id,selected_case_key) WHERE selected_case_key IS NOT NULL;
CREATE INDEX ix_rsp_selection_candidate_applicant ON ${primehrSchema}.rsp_selection_candidate_decision(agency_id,applicant_id,application_id);

CREATE TABLE ${primehrSchema}.rsp_selection_notice (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, selection_case_id VARCHAR(36) NOT NULL,
 application_id VARCHAR(36) NOT NULL, applicant_id VARCHAR(36) NOT NULL, kind VARCHAR(20) NOT NULL,
 safe_text NVARCHAR(2000) NOT NULL, released_at DATETIME2 NOT NULL, current_notice_key VARCHAR(100) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIME2 NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIME2 NOT NULL,
 CONSTRAINT fk_rsp_selection_notice_case FOREIGN KEY(selection_case_id) REFERENCES ${primehrSchema}.rsp_selection_case(id),
 CONSTRAINT fk_rsp_selection_notice_app FOREIGN KEY(application_id) REFERENCES ${primehrSchema}.rsp_position_application(id),
 CONSTRAINT uk_rsp_selection_notice_case UNIQUE(agency_id,selection_case_id,application_id),
 CONSTRAINT ck_rsp_selection_notice_kind CHECK(kind IN ('SELECTED','NOT_SELECTED','NO_SELECTION','DEFERRED','SUPERSEDED')),
 CONSTRAINT ck_rsp_selection_notice_current CHECK((kind='SUPERSEDED' AND current_notice_key IS NULL) OR (kind<>'SUPERSEDED' AND current_notice_key=application_id))
);
CREATE UNIQUE INDEX uk_rsp_selection_notice_current ON ${primehrSchema}.rsp_selection_notice(agency_id,current_notice_key) WHERE current_notice_key IS NOT NULL;

CREATE TABLE ${primehrSchema}.rsp_offer_response (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, selection_case_id VARCHAR(36) NOT NULL,
 application_id VARCHAR(36) NOT NULL, applicant_id VARCHAR(36) NOT NULL, status VARCHAR(20) NOT NULL,
 response_deadline DATETIME2 NOT NULL, idempotency_key VARCHAR(100) NULL, responded_at DATETIME2 NULL,
 applicant_comment NVARCHAR(1000) NULL, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIME2 NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIME2 NOT NULL,
 CONSTRAINT fk_rsp_offer_case FOREIGN KEY(selection_case_id) REFERENCES ${primehrSchema}.rsp_selection_case(id),
 CONSTRAINT fk_rsp_offer_app FOREIGN KEY(application_id) REFERENCES ${primehrSchema}.rsp_position_application(id),
 CONSTRAINT uk_rsp_offer_case UNIQUE(agency_id,selection_case_id), CONSTRAINT uk_rsp_offer_application UNIQUE(agency_id,application_id),
 CONSTRAINT ck_rsp_offer_status CHECK(status IN ('PENDING','ACCEPTED','DECLINED','EXPIRED')),
 CONSTRAINT ck_rsp_offer_response CHECK((status='PENDING' AND responded_at IS NULL AND idempotency_key IS NULL) OR (status IN ('ACCEPTED','DECLINED') AND responded_at IS NOT NULL AND idempotency_key IS NOT NULL) OR status='EXPIRED')
);
CREATE UNIQUE INDEX uk_rsp_offer_idempotency ON ${primehrSchema}.rsp_offer_response(agency_id,idempotency_key) WHERE idempotency_key IS NOT NULL;

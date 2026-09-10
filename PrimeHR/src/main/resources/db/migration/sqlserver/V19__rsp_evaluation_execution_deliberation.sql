ALTER TABLE [${primehrSchema}].rsp_evaluation_proceeding ADD execution_revision INT NOT NULL CONSTRAINT df_rsp_evaluation_execution_revision DEFAULT 0;

CREATE TABLE [${primehrSchema}].rsp_evaluation_session (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_session PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL, session_revision INT NOT NULL,
 status VARCHAR(20) NOT NULL, starts_at DATETIMEOFFSET NOT NULL, ends_at DATETIMEOFFSET NOT NULL,
 time_zone VARCHAR(80) NOT NULL, delivery_mode VARCHAR(30) NOT NULL, venue NVARCHAR(500) NULL,
 instructions NVARCHAR(2000) NULL, capacity INT NULL, responsible_employee_no VARCHAR(100) NOT NULL,
 scheduled_by VARCHAR(100) NULL, scheduled_at DATETIMEOFFSET NULL, completed_by VARCHAR(100) NULL,
 completed_at DATETIMEOFFSET NULL, cancelled_by VARCHAR(100) NULL, cancelled_at DATETIMEOFFSET NULL,
 change_reason NVARCHAR(2000) NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_eval_session_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_eval_session_stage FOREIGN KEY(stage_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_stage(id),
 CONSTRAINT uk_rsp_eval_session_revision UNIQUE(agency_id,proceeding_id,stage_id,session_revision),
 CONSTRAINT ck_rsp_eval_session_status CHECK(status IN ('DRAFT','SCHEDULED','COMPLETED','CANCELLED')),
 CONSTRAINT ck_rsp_eval_session_mode CHECK(delivery_mode IN ('ONSITE','REMOTE_APPROVED','HYBRID')),
 CONSTRAINT ck_rsp_eval_session_values CHECK(session_revision>=1 AND ends_at>starts_at AND (capacity IS NULL OR capacity>0)),
 CONSTRAINT ck_rsp_eval_session_lifecycle CHECK((status='DRAFT' AND scheduled_at IS NULL AND completed_at IS NULL AND cancelled_at IS NULL) OR (status='SCHEDULED' AND scheduled_at IS NOT NULL AND completed_at IS NULL AND cancelled_at IS NULL) OR (status='COMPLETED' AND scheduled_at IS NOT NULL AND completed_at IS NOT NULL AND cancelled_at IS NULL) OR (status='CANCELLED' AND cancelled_at IS NOT NULL AND change_reason IS NOT NULL))
);
CREATE INDEX ix_rsp_eval_session_queue ON [${primehrSchema}].rsp_evaluation_session(agency_id,proceeding_id,status,starts_at);

CREATE TABLE [${primehrSchema}].rsp_evaluation_session_candidate (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_eval_session_candidate PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 session_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NOT NULL, invitation_status VARCHAR(20) NOT NULL,
 attendance_status VARCHAR(20) NOT NULL, invited_at DATETIMEOFFSET NULL, confirmed_at DATETIMEOFFSET NULL,
 attendance_recorded_by VARCHAR(100) NULL, attendance_recorded_at DATETIMEOFFSET NULL, attendance_reason NVARCHAR(1000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_eval_sc_session FOREIGN KEY(session_id) REFERENCES [${primehrSchema}].rsp_evaluation_session(id),
 CONSTRAINT fk_rsp_eval_sc_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_eval_session_candidate UNIQUE(agency_id,session_id,candidate_id),
 CONSTRAINT ck_rsp_eval_sc_invitation CHECK(invitation_status IN ('PENDING','INVITED','CONFIRMED','DECLINED')),
 CONSTRAINT ck_rsp_eval_sc_attendance CHECK(attendance_status IN ('PENDING','ATTENDED','NO_SHOW','EXCUSED','WITHDRAWN'))
);
CREATE INDEX ix_rsp_eval_sc_candidate ON [${primehrSchema}].rsp_evaluation_session_candidate(agency_id,candidate_id,attendance_status);

CREATE TABLE [${primehrSchema}].rsp_evaluation_assignment (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_assignment PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NULL, candidate_id VARCHAR(36) NULL,
 employee_no VARCHAR(100) NOT NULL, assignment_role VARCHAR(30) NOT NULL, active BIT NOT NULL, current_assignment_key VARCHAR(300) NULL,
 alternate_for_employee_no VARCHAR(100) NULL, assigned_by VARCHAR(100) NOT NULL, assigned_at DATETIMEOFFSET NOT NULL,
 deactivated_by VARCHAR(100) NULL, deactivated_at DATETIMEOFFSET NULL, deactivation_reason NVARCHAR(1000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_eval_assignment_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_eval_assignment_stage FOREIGN KEY(stage_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_stage(id),
 CONSTRAINT fk_rsp_eval_assignment_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT ck_rsp_eval_assignment_role CHECK(assignment_role IN ('EXAMINER','VALIDATOR','PANEL_MEMBER','REFERENCE_CHECKER','SECRETARIAT','OBSERVER','ALTERNATE')),
 CONSTRAINT uk_rsp_eval_assignment_active UNIQUE(agency_id,current_assignment_key),
 CONSTRAINT ck_rsp_eval_assignment_active CHECK((active=1 AND current_assignment_key IS NOT NULL AND deactivated_at IS NULL) OR (active=0 AND current_assignment_key IS NULL AND deactivated_at IS NOT NULL AND deactivation_reason IS NOT NULL))
);
CREATE INDEX ix_rsp_eval_assignment_employee ON [${primehrSchema}].rsp_evaluation_assignment(agency_id,employee_no,active,proceeding_id);

CREATE TABLE [${primehrSchema}].rsp_conflict_declaration (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_conflict_declaration PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NULL, employee_no VARCHAR(100) NOT NULL,
 outcome VARCHAR(40) NOT NULL, declaration NVARCHAR(2000) NOT NULL, declared_at DATETIMEOFFSET NOT NULL,
 resolved_outcome VARCHAR(30) NULL, resolved_by VARCHAR(100) NULL, resolved_at DATETIMEOFFSET NULL, resolution_reason NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_conflict_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_conflict_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_conflict_scope UNIQUE(agency_id,proceeding_id,candidate_id,employee_no),
 CONSTRAINT ck_rsp_conflict_outcome CHECK(outcome IN ('NO_CONFLICT','POTENTIAL_CONFLICT_REVIEW_REQUIRED','CONFLICT_RECUSED')),
 CONSTRAINT ck_rsp_conflict_resolution CHECK((outcome<>'POTENTIAL_CONFLICT_REVIEW_REQUIRED' AND resolved_at IS NULL) OR (outcome='POTENTIAL_CONFLICT_REVIEW_REQUIRED' AND (resolved_at IS NULL OR (resolved_outcome IN ('CLEARED','RECUSED') AND resolved_by IS NOT NULL AND resolution_reason IS NOT NULL))))
);
CREATE INDEX ix_rsp_conflict_actor ON [${primehrSchema}].rsp_conflict_declaration(agency_id,proceeding_id,employee_no,outcome);

CREATE TABLE [${primehrSchema}].rsp_stage_result (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_stage_result PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, session_id VARCHAR(36) NULL, candidate_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL,
 result_revision INT NOT NULL, supersedes_id VARCHAR(36) NULL, current_result_key VARCHAR(200) NULL, status VARCHAR(20) NOT NULL,
 raw_score DECIMAL(12,4) NULL, maximum_score DECIMAL(12,4) NULL, normalized_score DECIMAL(12,4) NULL,
 gate_outcome VARCHAR(20) NULL, remarks NVARCHAR(2000) NULL, evidence_required BIT NOT NULL,
 recorded_by VARCHAR(100) NOT NULL, submitted_by VARCHAR(100) NULL, submitted_at DATETIMEOFFSET NULL,
 validated_by VARCHAR(100) NULL, validated_at DATETIMEOFFSET NULL, returned_by VARCHAR(100) NULL,
 returned_at DATETIMEOFFSET NULL, return_reason NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_stage_result_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_stage_result_session FOREIGN KEY(session_id) REFERENCES [${primehrSchema}].rsp_evaluation_session(id),
 CONSTRAINT fk_rsp_stage_result_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_stage_result_stage FOREIGN KEY(stage_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_stage(id),
 CONSTRAINT fk_rsp_stage_result_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].rsp_stage_result(id),
 CONSTRAINT uk_rsp_stage_result_revision UNIQUE(agency_id,proceeding_id,candidate_id,stage_id,result_revision),
 CONSTRAINT uk_rsp_stage_result_current UNIQUE(agency_id,current_result_key),
 CONSTRAINT ck_rsp_stage_result_status CHECK(status IN ('DRAFT','RETURNED','SUBMITTED','VALIDATED','SUPERSEDED')),
 CONSTRAINT ck_rsp_stage_result_scores CHECK((raw_score IS NULL OR raw_score>=0) AND (maximum_score IS NULL OR maximum_score>0) AND (normalized_score IS NULL OR (normalized_score>=0 AND normalized_score<=100)) AND (gate_outcome IS NULL OR gate_outcome IN ('PASS','FAIL','NOT_APPLICABLE'))),
 CONSTRAINT ck_rsp_stage_result_sod CHECK(validated_by IS NULL OR validated_by<>recorded_by)
);
CREATE INDEX ix_rsp_stage_result_queue ON [${primehrSchema}].rsp_stage_result(agency_id,proceeding_id,stage_id,status,candidate_id);

CREATE TABLE [${primehrSchema}].rsp_panel_rating (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_panel_rating PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL,
 rater_employee_no VARCHAR(100) NOT NULL, rating_revision INT NOT NULL, supersedes_id VARCHAR(36) NULL,
 current_rating_key VARCHAR(300) NULL, status VARCHAR(20) NOT NULL, normalized_score DECIMAL(12,4) NULL,
 overall_remarks NVARCHAR(2000) NULL, submitted_at DATETIMEOFFSET NULL, returned_by VARCHAR(100) NULL,
 returned_at DATETIMEOFFSET NULL, return_reason NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_panel_rating_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_panel_rating_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_panel_rating_stage FOREIGN KEY(stage_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_stage(id),
 CONSTRAINT fk_rsp_panel_rating_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].rsp_panel_rating(id),
 CONSTRAINT uk_rsp_panel_rating_revision UNIQUE(agency_id,proceeding_id,candidate_id,stage_id,rater_employee_no,rating_revision),
 CONSTRAINT uk_rsp_panel_rating_current UNIQUE(agency_id,current_rating_key),
 CONSTRAINT ck_rsp_panel_rating_status CHECK(status IN ('DRAFT','RETURNED','SUBMITTED','SUPERSEDED')),
 CONSTRAINT ck_rsp_panel_rating_score CHECK(normalized_score IS NULL OR (normalized_score>=0 AND normalized_score<=100))
);
CREATE INDEX ix_rsp_panel_rating_queue ON [${primehrSchema}].rsp_panel_rating(agency_id,proceeding_id,stage_id,status,candidate_id);

CREATE TABLE [${primehrSchema}].rsp_panel_rating_item (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_panel_rating_item PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 rating_id VARCHAR(36) NOT NULL, criterion_id VARCHAR(36) NOT NULL, raw_score DECIMAL(12,4) NOT NULL,
 maximum_score DECIMAL(12,4) NOT NULL, normalized_score DECIMAL(12,4) NOT NULL, weighted_score DECIMAL(12,4) NOT NULL,
 remarks NVARCHAR(2000) NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_panel_item_rating FOREIGN KEY(rating_id) REFERENCES [${primehrSchema}].rsp_panel_rating(id),
 CONSTRAINT fk_rsp_panel_item_criterion FOREIGN KEY(criterion_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_criterion(id),
 CONSTRAINT uk_rsp_panel_item_criterion UNIQUE(agency_id,rating_id,criterion_id),
 CONSTRAINT ck_rsp_panel_item_scores CHECK(raw_score>=0 AND maximum_score>0 AND normalized_score>=0 AND normalized_score<=100 AND weighted_score>=0 AND weighted_score<=100)
);

CREATE TABLE [${primehrSchema}].rsp_reference_check (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_reference_check PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL,
 check_type VARCHAR(80) NOT NULL, source_category VARCHAR(100) NOT NULL, lawful_basis_reference NVARCHAR(500) NOT NULL,
 requested_at DATETIMEOFFSET NOT NULL, completed_at DATETIMEOFFSET NULL, checker_employee_no VARCHAR(100) NOT NULL,
 outcome VARCHAR(30) NOT NULL, applicant_safe_text NVARCHAR(1000) NULL, confidential_notes NVARCHAR(4000) NULL,
 validated_by VARCHAR(100) NULL, validated_at DATETIMEOFFSET NULL, status VARCHAR(20) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_reference_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_reference_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_reference_stage FOREIGN KEY(stage_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy_stage(id),
 CONSTRAINT uk_rsp_reference_check UNIQUE(agency_id,proceeding_id,candidate_id,stage_id,check_type),
 CONSTRAINT ck_rsp_reference_outcome CHECK(outcome IN ('PENDING','SATISFACTORY','CONCERN','INCONCLUSIVE','NOT_APPLICABLE')),
 CONSTRAINT ck_rsp_reference_status CHECK(status IN ('DRAFT','COMPLETED','VALIDATED')),
 CONSTRAINT ck_rsp_reference_sod CHECK(validated_by IS NULL OR validated_by<>checker_employee_no)
);

CREATE TABLE [${primehrSchema}].rsp_evaluation_evidence (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_evaluation_evidence PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NULL, owner_type VARCHAR(40) NOT NULL, owner_id VARCHAR(36) NOT NULL,
 classification VARCHAR(30) NOT NULL, original_filename NVARCHAR(255) NOT NULL, media_type VARCHAR(120) NOT NULL,
 byte_size BIGINT NOT NULL, checksum_sha256 VARCHAR(64) NOT NULL, storage_provider VARCHAR(30) NOT NULL,
 storage_object_key VARCHAR(500) NOT NULL, retention_tag VARCHAR(100) NOT NULL, uploaded_by VARCHAR(100) NOT NULL,
 uploaded_at DATETIMEOFFSET NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_evidence_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_evidence_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_evidence_object UNIQUE(storage_object_key),
 CONSTRAINT ck_rsp_evidence_owner CHECK(owner_type IN ('STAGE_RESULT','PANEL_RATING','REFERENCE_CHECK','MEETING','RESOLUTION')),
 CONSTRAINT ck_rsp_evidence_class CHECK(classification IN ('CONFIDENTIAL','HRMPSB_RESTRICTED')),
 CONSTRAINT ck_rsp_evidence_size CHECK(byte_size>0)
);
CREATE INDEX ix_rsp_evidence_owner ON [${primehrSchema}].rsp_evaluation_evidence(agency_id,owner_type,owner_id);

CREATE TABLE [${primehrSchema}].rsp_hrmpsb_meeting (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_hrmpsb_meeting PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, meeting_revision INT NOT NULL, status VARCHAR(20) NOT NULL,
 scheduled_at DATETIMEOFFSET NOT NULL, time_zone VARCHAR(80) NOT NULL, venue NVARCHAR(500) NULL,
 agenda NVARCHAR(4000) NOT NULL, minutes NVARCHAR(MAX) NULL, quorum_required INT NOT NULL,
 finalized_by VARCHAR(100) NULL, finalized_at DATETIMEOFFSET NULL, finalization_reason NVARCHAR(2000) NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_hrmpsb_meeting_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT uk_rsp_hrmpsb_meeting_revision UNIQUE(agency_id,proceeding_id,meeting_revision),
 CONSTRAINT ck_rsp_hrmpsb_meeting_status CHECK(status IN ('DRAFT','SCHEDULED','IN_DELIBERATION','FINALIZED','CANCELLED')),
 CONSTRAINT ck_rsp_hrmpsb_meeting_quorum CHECK(quorum_required>=1),
 CONSTRAINT ck_rsp_hrmpsb_meeting_final CHECK((status='FINALIZED' AND finalized_by IS NOT NULL AND finalized_at IS NOT NULL AND finalization_reason IS NOT NULL) OR status<>'FINALIZED')
);

CREATE TABLE [${primehrSchema}].rsp_hrmpsb_attendance (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_hrmpsb_attendance PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 meeting_id VARCHAR(36) NOT NULL, employee_no VARCHAR(100) NOT NULL, member_role VARCHAR(30) NOT NULL,
 attendance_status VARCHAR(20) NOT NULL, voting_eligible BIT NOT NULL, recorded_by VARCHAR(100) NOT NULL,
 recorded_at DATETIMEOFFSET NOT NULL, remarks NVARCHAR(1000) NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_hrmpsb_attendance_meeting FOREIGN KEY(meeting_id) REFERENCES [${primehrSchema}].rsp_hrmpsb_meeting(id),
 CONSTRAINT uk_rsp_hrmpsb_attendance UNIQUE(agency_id,meeting_id,employee_no),
 CONSTRAINT ck_rsp_hrmpsb_attendance_status CHECK(attendance_status IN ('PRESENT','ABSENT','EXCUSED','RECUSED'))
);

CREATE TABLE [${primehrSchema}].rsp_hrmpsb_resolution (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_hrmpsb_resolution PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 meeting_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NOT NULL, recommendation VARCHAR(40) NOT NULL,
 reason NVARCHAR(3000) NOT NULL, decision_method VARCHAR(30) NOT NULL, votes_for INT NULL, votes_against INT NULL,
 abstentions INT NULL, recorded_by VARCHAR(100) NOT NULL, recorded_at DATETIMEOFFSET NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_hrmpsb_resolution_meeting FOREIGN KEY(meeting_id) REFERENCES [${primehrSchema}].rsp_hrmpsb_meeting(id),
 CONSTRAINT fk_rsp_hrmpsb_resolution_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_hrmpsb_resolution_candidate UNIQUE(agency_id,meeting_id,candidate_id),
 CONSTRAINT ck_rsp_hrmpsb_recommendation CHECK(recommendation IN ('ENDORSED_FOR_SELECTION_DECISION','NOT_ENDORSED','DEFERRED')),
 CONSTRAINT ck_rsp_hrmpsb_method CHECK(decision_method IN ('CONSENSUS','VOTE')),
 CONSTRAINT ck_rsp_hrmpsb_votes CHECK((votes_for IS NULL OR votes_for>=0) AND (votes_against IS NULL OR votes_against>=0) AND (abstentions IS NULL OR abstentions>=0))
);

CREATE TABLE [${primehrSchema}].rsp_comparative_evaluation (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_comparative_evaluation PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 proceeding_id VARCHAR(36) NOT NULL, evaluation_revision INT NOT NULL, supersedes_id VARCHAR(36) NULL,
 status VARCHAR(20) NOT NULL, current_final_key VARCHAR(36) NULL, policy_id VARCHAR(36) NOT NULL, policy_definition_version INT NOT NULL,
 policy_fingerprint VARCHAR(64) NOT NULL, calculation_snapshot NVARCHAR(MAX) NOT NULL,
 calculation_fingerprint VARCHAR(64) NOT NULL, generated_by VARCHAR(100) NOT NULL, generated_at DATETIMEOFFSET NOT NULL,
 finalized_by VARCHAR(100) NULL, finalized_at DATETIMEOFFSET NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_comparative_proceeding FOREIGN KEY(proceeding_id) REFERENCES [${primehrSchema}].rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_comparative_policy FOREIGN KEY(policy_id) REFERENCES [${primehrSchema}].rsp_evaluation_policy(id),
 CONSTRAINT fk_rsp_comparative_prior FOREIGN KEY(supersedes_id) REFERENCES [${primehrSchema}].rsp_comparative_evaluation(id),
 CONSTRAINT uk_rsp_comparative_revision UNIQUE(agency_id,proceeding_id,evaluation_revision),
 CONSTRAINT uk_rsp_comparative_final UNIQUE(agency_id,current_final_key),
 CONSTRAINT ck_rsp_comparative_status CHECK(status IN ('GENERATED','FINALIZED','SUPERSEDED')),
 CONSTRAINT ck_rsp_comparative_final CHECK((status='FINALIZED' AND current_final_key IS NOT NULL AND finalized_by IS NOT NULL AND finalized_at IS NOT NULL) OR (status<>'FINALIZED' AND current_final_key IS NULL))
);

CREATE TABLE [${primehrSchema}].rsp_comparative_evaluation_item (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_comparative_item PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 comparative_evaluation_id VARCHAR(36) NOT NULL, candidate_id VARCHAR(36) NOT NULL, total_score DECIMAL(12,4) NOT NULL,
 rank_number INT NOT NULL, tie_group INT NOT NULL, stage_breakdown NVARCHAR(MAX) NOT NULL, excluded BIT NOT NULL,
 exclusion_reason NVARCHAR(1000) NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_comparative_item_eval FOREIGN KEY(comparative_evaluation_id) REFERENCES [${primehrSchema}].rsp_comparative_evaluation(id),
 CONSTRAINT fk_rsp_comparative_item_candidate FOREIGN KEY(candidate_id) REFERENCES [${primehrSchema}].rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_comparative_item_candidate UNIQUE(agency_id,comparative_evaluation_id,candidate_id),
 CONSTRAINT ck_rsp_comparative_item_values CHECK(total_score>=0 AND total_score<=100 AND rank_number>=1 AND tie_group>=1 AND ((excluded=0 AND exclusion_reason IS NULL) OR excluded=1))
);

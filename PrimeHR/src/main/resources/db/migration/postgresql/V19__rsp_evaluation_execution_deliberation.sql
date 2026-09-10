ALTER TABLE "${primehrSchema}".rsp_evaluation_proceeding ADD COLUMN execution_revision INTEGER NOT NULL DEFAULT 0;

CREATE TABLE "${primehrSchema}".rsp_evaluation_session (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 stage_id VARCHAR(36) NOT NULL, session_revision INTEGER NOT NULL, status VARCHAR(20) NOT NULL,
 starts_at TIMESTAMP WITH TIME ZONE NOT NULL, ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
 time_zone VARCHAR(80) NOT NULL, delivery_mode VARCHAR(30) NOT NULL, venue VARCHAR(500), instructions VARCHAR(2000),
 capacity INTEGER, responsible_employee_no VARCHAR(100) NOT NULL, scheduled_by VARCHAR(100),
 scheduled_at TIMESTAMP WITH TIME ZONE, completed_by VARCHAR(100), completed_at TIMESTAMP WITH TIME ZONE,
 cancelled_by VARCHAR(100), cancelled_at TIMESTAMP WITH TIME ZONE, change_reason VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_eval_session_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_eval_session_stage FOREIGN KEY(stage_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy_stage(id),
 CONSTRAINT uk_rsp_eval_session_revision UNIQUE(agency_id,proceeding_id,stage_id,session_revision),
 CONSTRAINT ck_rsp_eval_session_status CHECK(status IN ('DRAFT','SCHEDULED','COMPLETED','CANCELLED')),
 CONSTRAINT ck_rsp_eval_session_mode CHECK(delivery_mode IN ('ONSITE','REMOTE_APPROVED','HYBRID')),
 CONSTRAINT ck_rsp_eval_session_values CHECK(session_revision>=1 AND ends_at>starts_at AND (capacity IS NULL OR capacity>0)),
 CONSTRAINT ck_rsp_eval_session_lifecycle CHECK((status='DRAFT' AND scheduled_at IS NULL AND completed_at IS NULL AND cancelled_at IS NULL) OR (status='SCHEDULED' AND scheduled_at IS NOT NULL AND completed_at IS NULL AND cancelled_at IS NULL) OR (status='COMPLETED' AND scheduled_at IS NOT NULL AND completed_at IS NOT NULL AND cancelled_at IS NULL) OR (status='CANCELLED' AND cancelled_at IS NOT NULL AND change_reason IS NOT NULL))
);
CREATE INDEX ix_rsp_eval_session_queue ON "${primehrSchema}".rsp_evaluation_session(agency_id,proceeding_id,status,starts_at);

CREATE TABLE "${primehrSchema}".rsp_evaluation_session_candidate (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, session_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36) NOT NULL, invitation_status VARCHAR(20) NOT NULL, attendance_status VARCHAR(20) NOT NULL,
 invited_at TIMESTAMP WITH TIME ZONE, confirmed_at TIMESTAMP WITH TIME ZONE, attendance_recorded_by VARCHAR(100),
 attendance_recorded_at TIMESTAMP WITH TIME ZONE, attendance_reason VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_eval_sc_session FOREIGN KEY(session_id) REFERENCES "${primehrSchema}".rsp_evaluation_session(id),
 CONSTRAINT fk_rsp_eval_sc_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_eval_session_candidate UNIQUE(agency_id,session_id,candidate_id),
 CONSTRAINT ck_rsp_eval_sc_invitation CHECK(invitation_status IN ('PENDING','INVITED','CONFIRMED','DECLINED')),
 CONSTRAINT ck_rsp_eval_sc_attendance CHECK(attendance_status IN ('PENDING','ATTENDED','NO_SHOW','EXCUSED','WITHDRAWN'))
);
CREATE INDEX ix_rsp_eval_sc_candidate ON "${primehrSchema}".rsp_evaluation_session_candidate(agency_id,candidate_id,attendance_status);

CREATE TABLE "${primehrSchema}".rsp_evaluation_assignment (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 stage_id VARCHAR(36), candidate_id VARCHAR(36), employee_no VARCHAR(100) NOT NULL,
 assignment_role VARCHAR(30) NOT NULL, active BOOLEAN NOT NULL, current_assignment_key VARCHAR(300), alternate_for_employee_no VARCHAR(100),
 assigned_by VARCHAR(100) NOT NULL, assigned_at TIMESTAMP WITH TIME ZONE NOT NULL, deactivated_by VARCHAR(100),
 deactivated_at TIMESTAMP WITH TIME ZONE, deactivation_reason VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_eval_assignment_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_eval_assignment_stage FOREIGN KEY(stage_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy_stage(id),
 CONSTRAINT fk_rsp_eval_assignment_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT ck_rsp_eval_assignment_role CHECK(assignment_role IN ('EXAMINER','VALIDATOR','PANEL_MEMBER','REFERENCE_CHECKER','SECRETARIAT','OBSERVER','ALTERNATE')),
 CONSTRAINT uk_rsp_eval_assignment_active UNIQUE(agency_id,current_assignment_key),
 CONSTRAINT ck_rsp_eval_assignment_active CHECK((active AND current_assignment_key IS NOT NULL AND deactivated_at IS NULL) OR (NOT active AND current_assignment_key IS NULL AND deactivated_at IS NOT NULL AND deactivation_reason IS NOT NULL))
);
CREATE INDEX ix_rsp_eval_assignment_employee ON "${primehrSchema}".rsp_evaluation_assignment(agency_id,employee_no,active,proceeding_id);

CREATE TABLE "${primehrSchema}".rsp_conflict_declaration (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36), employee_no VARCHAR(100) NOT NULL, outcome VARCHAR(40) NOT NULL,
 declaration VARCHAR(2000) NOT NULL, declared_at TIMESTAMP WITH TIME ZONE NOT NULL, resolved_outcome VARCHAR(30),
 resolved_by VARCHAR(100), resolved_at TIMESTAMP WITH TIME ZONE, resolution_reason VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_conflict_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_conflict_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_conflict_scope UNIQUE(agency_id,proceeding_id,candidate_id,employee_no),
 CONSTRAINT ck_rsp_conflict_outcome CHECK(outcome IN ('NO_CONFLICT','POTENTIAL_CONFLICT_REVIEW_REQUIRED','CONFLICT_RECUSED')),
 CONSTRAINT ck_rsp_conflict_resolution CHECK((outcome<>'POTENTIAL_CONFLICT_REVIEW_REQUIRED' AND resolved_at IS NULL) OR (outcome='POTENTIAL_CONFLICT_REVIEW_REQUIRED' AND (resolved_at IS NULL OR (resolved_outcome IN ('CLEARED','RECUSED') AND resolved_by IS NOT NULL AND resolution_reason IS NOT NULL))))
);
CREATE INDEX ix_rsp_conflict_actor ON "${primehrSchema}".rsp_conflict_declaration(agency_id,proceeding_id,employee_no,outcome);

CREATE TABLE "${primehrSchema}".rsp_stage_result (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 session_id VARCHAR(36), candidate_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL,
 result_revision INTEGER NOT NULL, supersedes_id VARCHAR(36), current_result_key VARCHAR(200), status VARCHAR(20) NOT NULL,
 raw_score NUMERIC(12,4), maximum_score NUMERIC(12,4), normalized_score NUMERIC(12,4), gate_outcome VARCHAR(20),
 remarks VARCHAR(2000), evidence_required BOOLEAN NOT NULL, recorded_by VARCHAR(100) NOT NULL,
 submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE, validated_by VARCHAR(100),
 validated_at TIMESTAMP WITH TIME ZONE, returned_by VARCHAR(100), returned_at TIMESTAMP WITH TIME ZONE,
 return_reason VARCHAR(2000), record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_stage_result_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_stage_result_session FOREIGN KEY(session_id) REFERENCES "${primehrSchema}".rsp_evaluation_session(id),
 CONSTRAINT fk_rsp_stage_result_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_stage_result_stage FOREIGN KEY(stage_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy_stage(id),
 CONSTRAINT fk_rsp_stage_result_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".rsp_stage_result(id),
 CONSTRAINT uk_rsp_stage_result_revision UNIQUE(agency_id,proceeding_id,candidate_id,stage_id,result_revision),
 CONSTRAINT uk_rsp_stage_result_current UNIQUE(agency_id,current_result_key),
 CONSTRAINT ck_rsp_stage_result_status CHECK(status IN ('DRAFT','RETURNED','SUBMITTED','VALIDATED','SUPERSEDED')),
 CONSTRAINT ck_rsp_stage_result_scores CHECK((raw_score IS NULL OR raw_score>=0) AND (maximum_score IS NULL OR maximum_score>0) AND (normalized_score IS NULL OR (normalized_score>=0 AND normalized_score<=100)) AND (gate_outcome IS NULL OR gate_outcome IN ('PASS','FAIL','NOT_APPLICABLE'))),
 CONSTRAINT ck_rsp_stage_result_sod CHECK(validated_by IS NULL OR validated_by<>recorded_by)
);
CREATE INDEX ix_rsp_stage_result_queue ON "${primehrSchema}".rsp_stage_result(agency_id,proceeding_id,stage_id,status,candidate_id);

CREATE TABLE "${primehrSchema}".rsp_panel_rating (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL, rater_employee_no VARCHAR(100) NOT NULL,
 rating_revision INTEGER NOT NULL, supersedes_id VARCHAR(36), current_rating_key VARCHAR(300), status VARCHAR(20) NOT NULL,
 normalized_score NUMERIC(12,4), overall_remarks VARCHAR(2000), submitted_at TIMESTAMP WITH TIME ZONE,
 returned_by VARCHAR(100), returned_at TIMESTAMP WITH TIME ZONE, return_reason VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_panel_rating_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_panel_rating_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_panel_rating_stage FOREIGN KEY(stage_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy_stage(id),
 CONSTRAINT fk_rsp_panel_rating_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".rsp_panel_rating(id),
 CONSTRAINT uk_rsp_panel_rating_revision UNIQUE(agency_id,proceeding_id,candidate_id,stage_id,rater_employee_no,rating_revision),
 CONSTRAINT uk_rsp_panel_rating_current UNIQUE(agency_id,current_rating_key),
 CONSTRAINT ck_rsp_panel_rating_status CHECK(status IN ('DRAFT','RETURNED','SUBMITTED','SUPERSEDED')),
 CONSTRAINT ck_rsp_panel_rating_score CHECK(normalized_score IS NULL OR (normalized_score>=0 AND normalized_score<=100))
);
CREATE INDEX ix_rsp_panel_rating_queue ON "${primehrSchema}".rsp_panel_rating(agency_id,proceeding_id,stage_id,status,candidate_id);

CREATE TABLE "${primehrSchema}".rsp_panel_rating_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, rating_id VARCHAR(36) NOT NULL,
 criterion_id VARCHAR(36) NOT NULL, raw_score NUMERIC(12,4) NOT NULL, maximum_score NUMERIC(12,4) NOT NULL,
 normalized_score NUMERIC(12,4) NOT NULL, weighted_score NUMERIC(12,4) NOT NULL, remarks VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_panel_item_rating FOREIGN KEY(rating_id) REFERENCES "${primehrSchema}".rsp_panel_rating(id),
 CONSTRAINT fk_rsp_panel_item_criterion FOREIGN KEY(criterion_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy_criterion(id),
 CONSTRAINT uk_rsp_panel_item_criterion UNIQUE(agency_id,rating_id,criterion_id),
 CONSTRAINT ck_rsp_panel_item_scores CHECK(raw_score>=0 AND maximum_score>0 AND normalized_score>=0 AND normalized_score<=100 AND weighted_score>=0 AND weighted_score<=100)
);

CREATE TABLE "${primehrSchema}".rsp_reference_check (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36) NOT NULL, stage_id VARCHAR(36) NOT NULL, check_type VARCHAR(80) NOT NULL,
 source_category VARCHAR(100) NOT NULL, lawful_basis_reference VARCHAR(500) NOT NULL,
 requested_at TIMESTAMP WITH TIME ZONE NOT NULL, completed_at TIMESTAMP WITH TIME ZONE,
 checker_employee_no VARCHAR(100) NOT NULL, outcome VARCHAR(30) NOT NULL, applicant_safe_text VARCHAR(1000),
 confidential_notes VARCHAR(4000), validated_by VARCHAR(100), validated_at TIMESTAMP WITH TIME ZONE,
 status VARCHAR(20) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_reference_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_reference_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT fk_rsp_reference_stage FOREIGN KEY(stage_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy_stage(id),
 CONSTRAINT uk_rsp_reference_check UNIQUE(agency_id,proceeding_id,candidate_id,stage_id,check_type),
 CONSTRAINT ck_rsp_reference_outcome CHECK(outcome IN ('PENDING','SATISFACTORY','CONCERN','INCONCLUSIVE','NOT_APPLICABLE')),
 CONSTRAINT ck_rsp_reference_status CHECK(status IN ('DRAFT','COMPLETED','VALIDATED')),
 CONSTRAINT ck_rsp_reference_sod CHECK(validated_by IS NULL OR validated_by<>checker_employee_no)
);

CREATE TABLE "${primehrSchema}".rsp_evaluation_evidence (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36), owner_type VARCHAR(40) NOT NULL, owner_id VARCHAR(36) NOT NULL,
 classification VARCHAR(30) NOT NULL, original_filename VARCHAR(255) NOT NULL, media_type VARCHAR(120) NOT NULL,
 byte_size BIGINT NOT NULL, checksum_sha256 VARCHAR(64) NOT NULL, storage_provider VARCHAR(30) NOT NULL,
 storage_object_key VARCHAR(500) NOT NULL, retention_tag VARCHAR(100) NOT NULL, uploaded_by VARCHAR(100) NOT NULL,
 uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_evidence_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_evidence_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_evidence_object UNIQUE(storage_object_key),
 CONSTRAINT ck_rsp_evidence_owner CHECK(owner_type IN ('STAGE_RESULT','PANEL_RATING','REFERENCE_CHECK','MEETING','RESOLUTION')),
 CONSTRAINT ck_rsp_evidence_class CHECK(classification IN ('CONFIDENTIAL','HRMPSB_RESTRICTED')),
 CONSTRAINT ck_rsp_evidence_size CHECK(byte_size>0)
);
CREATE INDEX ix_rsp_evidence_owner ON "${primehrSchema}".rsp_evaluation_evidence(agency_id,owner_type,owner_id);

CREATE TABLE "${primehrSchema}".rsp_hrmpsb_meeting (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 meeting_revision INTEGER NOT NULL, status VARCHAR(20) NOT NULL, scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
 time_zone VARCHAR(80) NOT NULL, venue VARCHAR(500), agenda VARCHAR(4000) NOT NULL, minutes VARCHAR(32000),
 quorum_required INTEGER NOT NULL, finalized_by VARCHAR(100), finalized_at TIMESTAMP WITH TIME ZONE,
 finalization_reason VARCHAR(2000), record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_hrmpsb_meeting_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT uk_rsp_hrmpsb_meeting_revision UNIQUE(agency_id,proceeding_id,meeting_revision),
 CONSTRAINT ck_rsp_hrmpsb_meeting_status CHECK(status IN ('DRAFT','SCHEDULED','IN_DELIBERATION','FINALIZED','CANCELLED')),
 CONSTRAINT ck_rsp_hrmpsb_meeting_quorum CHECK(quorum_required>=1),
 CONSTRAINT ck_rsp_hrmpsb_meeting_final CHECK((status='FINALIZED' AND finalized_by IS NOT NULL AND finalized_at IS NOT NULL AND finalization_reason IS NOT NULL) OR status<>'FINALIZED')
);

CREATE TABLE "${primehrSchema}".rsp_hrmpsb_attendance (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, meeting_id VARCHAR(36) NOT NULL,
 employee_no VARCHAR(100) NOT NULL, member_role VARCHAR(30) NOT NULL, attendance_status VARCHAR(20) NOT NULL,
 voting_eligible BOOLEAN NOT NULL, recorded_by VARCHAR(100) NOT NULL, recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
 remarks VARCHAR(1000), record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_hrmpsb_attendance_meeting FOREIGN KEY(meeting_id) REFERENCES "${primehrSchema}".rsp_hrmpsb_meeting(id),
 CONSTRAINT uk_rsp_hrmpsb_attendance UNIQUE(agency_id,meeting_id,employee_no),
 CONSTRAINT ck_rsp_hrmpsb_attendance_status CHECK(attendance_status IN ('PRESENT','ABSENT','EXCUSED','RECUSED'))
);

CREATE TABLE "${primehrSchema}".rsp_hrmpsb_resolution (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, meeting_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36) NOT NULL, recommendation VARCHAR(40) NOT NULL, reason VARCHAR(3000) NOT NULL,
 decision_method VARCHAR(30) NOT NULL, votes_for INTEGER, votes_against INTEGER, abstentions INTEGER,
 recorded_by VARCHAR(100) NOT NULL, recorded_at TIMESTAMP WITH TIME ZONE NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_hrmpsb_resolution_meeting FOREIGN KEY(meeting_id) REFERENCES "${primehrSchema}".rsp_hrmpsb_meeting(id),
 CONSTRAINT fk_rsp_hrmpsb_resolution_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_hrmpsb_resolution_candidate UNIQUE(agency_id,meeting_id,candidate_id),
 CONSTRAINT ck_rsp_hrmpsb_recommendation CHECK(recommendation IN ('ENDORSED_FOR_SELECTION_DECISION','NOT_ENDORSED','DEFERRED')),
 CONSTRAINT ck_rsp_hrmpsb_method CHECK(decision_method IN ('CONSENSUS','VOTE')),
 CONSTRAINT ck_rsp_hrmpsb_votes CHECK((votes_for IS NULL OR votes_for>=0) AND (votes_against IS NULL OR votes_against>=0) AND (abstentions IS NULL OR abstentions>=0))
);

CREATE TABLE "${primehrSchema}".rsp_comparative_evaluation (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, proceeding_id VARCHAR(36) NOT NULL,
 evaluation_revision INTEGER NOT NULL, supersedes_id VARCHAR(36), status VARCHAR(20) NOT NULL, current_final_key VARCHAR(36),
 policy_id VARCHAR(36) NOT NULL, policy_definition_version INTEGER NOT NULL, policy_fingerprint VARCHAR(64) NOT NULL,
 calculation_snapshot VARCHAR(32000) NOT NULL, calculation_fingerprint VARCHAR(64) NOT NULL, generated_by VARCHAR(100) NOT NULL,
 generated_at TIMESTAMP WITH TIME ZONE NOT NULL, finalized_by VARCHAR(100), finalized_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_comparative_proceeding FOREIGN KEY(proceeding_id) REFERENCES "${primehrSchema}".rsp_evaluation_proceeding(id),
 CONSTRAINT fk_rsp_comparative_policy FOREIGN KEY(policy_id) REFERENCES "${primehrSchema}".rsp_evaluation_policy(id),
 CONSTRAINT fk_rsp_comparative_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".rsp_comparative_evaluation(id),
 CONSTRAINT uk_rsp_comparative_revision UNIQUE(agency_id,proceeding_id,evaluation_revision),
 CONSTRAINT uk_rsp_comparative_final UNIQUE(agency_id,current_final_key),
 CONSTRAINT ck_rsp_comparative_status CHECK(status IN ('GENERATED','FINALIZED','SUPERSEDED')),
 CONSTRAINT ck_rsp_comparative_final CHECK((status='FINALIZED' AND current_final_key IS NOT NULL AND finalized_by IS NOT NULL AND finalized_at IS NOT NULL) OR (status<>'FINALIZED' AND current_final_key IS NULL))
);

CREATE TABLE "${primehrSchema}".rsp_comparative_evaluation_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, comparative_evaluation_id VARCHAR(36) NOT NULL,
 candidate_id VARCHAR(36) NOT NULL, total_score NUMERIC(12,4) NOT NULL, rank_number INTEGER NOT NULL,
 tie_group INTEGER NOT NULL, stage_breakdown VARCHAR(32000) NOT NULL, excluded BOOLEAN NOT NULL, exclusion_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_comparative_item_eval FOREIGN KEY(comparative_evaluation_id) REFERENCES "${primehrSchema}".rsp_comparative_evaluation(id),
 CONSTRAINT fk_rsp_comparative_item_candidate FOREIGN KEY(candidate_id) REFERENCES "${primehrSchema}".rsp_evaluation_candidate(id),
 CONSTRAINT uk_rsp_comparative_item_candidate UNIQUE(agency_id,comparative_evaluation_id,candidate_id),
 CONSTRAINT ck_rsp_comparative_item_values CHECK(total_score>=0 AND total_score<=100 AND rank_number>=1 AND tie_group>=1 AND ((NOT excluded AND exclusion_reason IS NULL) OR excluded))
);

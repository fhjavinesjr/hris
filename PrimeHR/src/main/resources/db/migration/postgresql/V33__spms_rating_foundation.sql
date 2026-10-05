CREATE TABLE "${primehrSchema}".spms_rating_case (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, monitoring_case_id VARCHAR(36) NOT NULL, commitment_version_id VARCHAR(36) NOT NULL,
 cycle_id VARCHAR(36) NOT NULL, policy_version_id VARCHAR(36) NOT NULL, template_version_id VARCHAR(36) NOT NULL, rating_scale_version_id VARCHAR(36) NOT NULL,
 form_type VARCHAR(20) NOT NULL, owner_employee_id BIGINT NOT NULL, owner_employee_no VARCHAR(100) NOT NULL, owner_name VARCHAR(300) NOT NULL,
 business_unit_id BIGINT NOT NULL, supervisor_employee_no VARCHAR(100) NOT NULL, accomplishment_revision INTEGER NOT NULL, requires_self_assessment BOOLEAN NOT NULL,
 route_fingerprint VARCHAR(64) NOT NULL, commitment_fingerprint VARCHAR(64) NOT NULL, source_fingerprint VARCHAR(64) NOT NULL, status VARCHAR(30) NOT NULL,
 opened_by VARCHAR(100) NOT NULL, opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_rating_case_monitoring FOREIGN KEY(monitoring_case_id) REFERENCES "${primehrSchema}".spms_monitoring_case(id),
 CONSTRAINT fk_spms_rating_case_commitment FOREIGN KEY(commitment_version_id) REFERENCES "${primehrSchema}".spms_commitment_version(id),
 CONSTRAINT fk_spms_rating_case_cycle FOREIGN KEY(cycle_id) REFERENCES "${primehrSchema}".spms_cycle(id),
 CONSTRAINT fk_spms_rating_case_policy FOREIGN KEY(policy_version_id) REFERENCES "${primehrSchema}".spms_policy_version(id),
 CONSTRAINT fk_spms_rating_case_template FOREIGN KEY(template_version_id) REFERENCES "${primehrSchema}".spms_template_version(id),
 CONSTRAINT fk_spms_rating_case_scale FOREIGN KEY(rating_scale_version_id) REFERENCES "${primehrSchema}".spms_rating_scale_version(id),
 CONSTRAINT uk_spms_rating_case_revision UNIQUE(agency_id,monitoring_case_id,accomplishment_revision),
 CONSTRAINT ck_spms_rating_case_revision CHECK(accomplishment_revision>0),
 CONSTRAINT ck_spms_rating_case_status CHECK(status IN ('OPEN','SUPERVISOR_SUBMITTED','VOIDED')));
CREATE TABLE "${primehrSchema}".spms_rating_source_item (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, rating_case_id VARCHAR(36) NOT NULL, commitment_item_id VARCHAR(36) NOT NULL,
 commitment_section_id VARCHAR(36) NOT NULL, indicator_version_id VARCHAR(36) NOT NULL, monitoring_update_id VARCHAR(36) NOT NULL,
 item_label VARCHAR(200) NOT NULL, section_code VARCHAR(80) NOT NULL, section_weight NUMERIC(7,4) NOT NULL, item_weight NUMERIC(7,4) NOT NULL,
 reporting_date DATE NOT NULL, accomplishment_narrative VARCHAR(4000) NOT NULL, accomplished_value NUMERIC(19,6), evidence_fingerprint VARCHAR(64) NOT NULL, display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_rating_source_case FOREIGN KEY(rating_case_id) REFERENCES "${primehrSchema}".spms_rating_case(id),
 CONSTRAINT fk_spms_rating_source_item FOREIGN KEY(commitment_item_id) REFERENCES "${primehrSchema}".spms_commitment_item(id),
 CONSTRAINT fk_spms_rating_source_section FOREIGN KEY(commitment_section_id) REFERENCES "${primehrSchema}".spms_commitment_section(id),
 CONSTRAINT fk_spms_rating_source_indicator FOREIGN KEY(indicator_version_id) REFERENCES "${primehrSchema}".spms_success_indicator_version(id),
 CONSTRAINT fk_spms_rating_source_update FOREIGN KEY(monitoring_update_id) REFERENCES "${primehrSchema}".spms_monitoring_update(id),
 CONSTRAINT uk_spms_rating_source_item UNIQUE(agency_id,rating_case_id,commitment_item_id),
 CONSTRAINT ck_spms_rating_source_weights CHECK(section_weight>0 AND section_weight<=100 AND item_weight>0 AND item_weight<=100));
CREATE TABLE "${primehrSchema}".spms_rating_assessment (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, rating_case_id VARCHAR(36) NOT NULL, root_assessment_id VARCHAR(36) NOT NULL,
 revision_no INTEGER NOT NULL, supersedes_id VARCHAR(36), assessment_type VARCHAR(20) NOT NULL, author_employee_no VARCHAR(100) NOT NULL, summary VARCHAR(4000),
 raw_score NUMERIC(19,8) NOT NULL, rounded_score NUMERIC(19,6) NOT NULL, rating_band_id VARCHAR(36) NOT NULL, rating_label VARCHAR(200) NOT NULL,
 formula_version VARCHAR(40) NOT NULL, calculation_fingerprint VARCHAR(64) NOT NULL, status VARCHAR(20) NOT NULL, submitted_by VARCHAR(100), submitted_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_rating_assessment_case FOREIGN KEY(rating_case_id) REFERENCES "${primehrSchema}".spms_rating_case(id),
 CONSTRAINT fk_spms_rating_assessment_prior FOREIGN KEY(supersedes_id) REFERENCES "${primehrSchema}".spms_rating_assessment(id),
 CONSTRAINT fk_spms_rating_assessment_band FOREIGN KEY(rating_band_id) REFERENCES "${primehrSchema}".spms_rating_band(id),
 CONSTRAINT uk_spms_rating_assessment_revision UNIQUE(agency_id,root_assessment_id,revision_no),
 CONSTRAINT ck_spms_rating_assessment_type CHECK(assessment_type IN ('SELF','SUPERVISOR')),
 CONSTRAINT ck_spms_rating_assessment_status CHECK(status IN ('DRAFT','SUBMITTED','SUPERSEDED','VOIDED')),
 CONSTRAINT ck_spms_rating_assessment_revision CHECK(revision_no>0));
CREATE TABLE "${primehrSchema}".spms_rating_item_result (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, assessment_id VARCHAR(36) NOT NULL, source_item_id VARCHAR(36) NOT NULL,
 raw_score NUMERIC(19,8) NOT NULL, rounded_score NUMERIC(19,6) NOT NULL, weighted_contribution NUMERIC(19,8) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_rating_item_assessment FOREIGN KEY(assessment_id) REFERENCES "${primehrSchema}".spms_rating_assessment(id),
 CONSTRAINT fk_spms_rating_item_source FOREIGN KEY(source_item_id) REFERENCES "${primehrSchema}".spms_rating_source_item(id),
 CONSTRAINT uk_spms_rating_item_result UNIQUE(agency_id,assessment_id,source_item_id));
CREATE TABLE "${primehrSchema}".spms_rating_dimension_result (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, assessment_id VARCHAR(36) NOT NULL, item_result_id VARCHAR(36) NOT NULL,
 dimension_id VARCHAR(36) NOT NULL, dimension_code VARCHAR(80) NOT NULL, dimension_type VARCHAR(30) NOT NULL, actual_value NUMERIC(19,6), manual_band_id VARCHAR(36),
 matched_level_id VARCHAR(36) NOT NULL, matched_band_id VARCHAR(36) NOT NULL, numeric_score NUMERIC(19,6) NOT NULL, dimension_weight NUMERIC(7,4) NOT NULL,
 weighted_contribution NUMERIC(19,8) NOT NULL, narrative VARCHAR(2000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_rating_dimension_assessment FOREIGN KEY(assessment_id) REFERENCES "${primehrSchema}".spms_rating_assessment(id),
 CONSTRAINT fk_spms_rating_dimension_item FOREIGN KEY(item_result_id) REFERENCES "${primehrSchema}".spms_rating_item_result(id),
 CONSTRAINT fk_spms_rating_dimension_definition FOREIGN KEY(dimension_id) REFERENCES "${primehrSchema}".spms_indicator_dimension(id),
 CONSTRAINT fk_spms_rating_dimension_manual_band FOREIGN KEY(manual_band_id) REFERENCES "${primehrSchema}".spms_rating_band(id),
 CONSTRAINT fk_spms_rating_dimension_level FOREIGN KEY(matched_level_id) REFERENCES "${primehrSchema}".spms_indicator_dimension_level(id),
 CONSTRAINT fk_spms_rating_dimension_band FOREIGN KEY(matched_band_id) REFERENCES "${primehrSchema}".spms_rating_band(id),
 CONSTRAINT uk_spms_rating_dimension_result UNIQUE(agency_id,item_result_id,dimension_id),
 CONSTRAINT ck_spms_rating_dimension_weight CHECK(dimension_weight>0 AND dimension_weight<=100),
 CONSTRAINT ck_spms_rating_dimension_actual CHECK((actual_value IS NULL)<>(manual_band_id IS NULL)));
CREATE TABLE "${primehrSchema}".spms_rating_action (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, rating_case_id VARCHAR(36) NOT NULL, subject_id VARCHAR(36) NOT NULL,
 action_type VARCHAR(50) NOT NULL, request_key VARCHAR(100) NOT NULL, actor_employee_no VARCHAR(100) NOT NULL, reason VARCHAR(2000), acted_at TIMESTAMP WITH TIME ZONE NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_spms_rating_action_case FOREIGN KEY(rating_case_id) REFERENCES "${primehrSchema}".spms_rating_case(id),
 CONSTRAINT uk_spms_rating_action_request UNIQUE(agency_id,rating_case_id,request_key));
CREATE INDEX ix_spms_rating_case_owner ON "${primehrSchema}".spms_rating_case(agency_id,owner_employee_no,status);
CREATE INDEX ix_spms_rating_case_supervisor ON "${primehrSchema}".spms_rating_case(agency_id,supervisor_employee_no,status);
CREATE INDEX ix_spms_rating_assessment_case ON "${primehrSchema}".spms_rating_assessment(agency_id,rating_case_id,assessment_type,status);
CREATE INDEX ix_spms_rating_action_case ON "${primehrSchema}".spms_rating_action(agency_id,rating_case_id,acted_at);

CREATE TABLE "${primehrSchema}".prime_assessment_validation (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 assessment_case_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_assessment_case(id),
 validator_employee_no VARCHAR(100) NOT NULL, administrator_override BOOLEAN NOT NULL,
 validation_remarks VARCHAR(4000), override_reason VARCHAR(1000), validated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 active BOOLEAN NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_prime_assessment_validation_case UNIQUE(assessment_case_id),
 CONSTRAINT ck_prime_validation_override_reason CHECK (administrator_override = FALSE OR override_reason IS NOT NULL)
);
CREATE TABLE "${primehrSchema}".prime_assessment_validated_rating (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 validation_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_assessment_validation(id),
 competency_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_competency(id),
 final_proficiency_level_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_proficiency_level(id),
 validation_remarks VARCHAR(2000), contributing_assignment_ids VARCHAR(4000) NOT NULL,
 active BOOLEAN NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_prime_validated_rating UNIQUE(validation_id,competency_id)
);
CREATE TABLE "${primehrSchema}".prime_person_competency_profile (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 assessment_case_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_assessment_case(id),
 validation_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_assessment_validation(id),
 predecessor_id VARCHAR(36) REFERENCES "${primehrSchema}".prime_person_competency_profile(id),
 subject_employee_id BIGINT NOT NULL, subject_employee_no VARCHAR(100) NOT NULL, subject_display_name VARCHAR(300) NOT NULL,
 appointment_id BIGINT NOT NULL, job_position_id BIGINT NOT NULL, plantilla_id BIGINT,
 cycle_id VARCHAR(36) NOT NULL, tool_id VARCHAR(36) NOT NULL, position_profile_id VARCHAR(36) NOT NULL,
 position_profile_definition_version INTEGER NOT NULL, position_profile_content_revision BIGINT NOT NULL,
 profile_version INTEGER NOT NULL, valid_from DATE NOT NULL, valid_to DATE, reassessment_date DATE,
 source_method_summary VARCHAR(1000) NOT NULL, status VARCHAR(30) NOT NULL, validated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 active BOOLEAN NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_prime_person_profile_case UNIQUE(assessment_case_id),
 CONSTRAINT uk_prime_person_profile_validation UNIQUE(validation_id),
 CONSTRAINT uk_prime_person_profile_version UNIQUE(agency_id,subject_employee_id,profile_version),
 CONSTRAINT ck_prime_person_profile_status CHECK(status='VALIDATED'),
 CONSTRAINT ck_prime_person_profile_version CHECK(profile_version>=1),
 CONSTRAINT ck_prime_person_profile_dates CHECK((valid_to IS NULL OR valid_to>=valid_from) AND (reassessment_date IS NULL OR reassessment_date>valid_from))
);
CREATE TABLE "${primehrSchema}".prime_person_competency_result (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 person_profile_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_person_competency_profile(id),
 competency_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_competency(id),
 attained_proficiency_level_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_proficiency_level(id),
 validated_rating_id VARCHAR(36) NOT NULL REFERENCES "${primehrSchema}".prime_assessment_validated_rating(id),
 active BOOLEAN NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_prime_person_result UNIQUE(person_profile_id,competency_id),
 CONSTRAINT uk_prime_person_result_validated UNIQUE(validated_rating_id)
);
CREATE INDEX ix_prime_validation_status ON "${primehrSchema}".prime_assessment_validation(agency_id,validated_at);
CREATE INDEX ix_prime_person_profile_latest ON "${primehrSchema}".prime_person_competency_profile(agency_id,subject_employee_no,valid_from,valid_to,profile_version);
CREATE INDEX ix_prime_person_result_profile ON "${primehrSchema}".prime_person_competency_result(person_profile_id,competency_id);

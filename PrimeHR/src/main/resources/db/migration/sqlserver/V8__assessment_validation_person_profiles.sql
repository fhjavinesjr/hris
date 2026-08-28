CREATE TABLE [${primehrSchema}].prime_assessment_validation (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_assessment_validation PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 assessment_case_id VARCHAR(36) NOT NULL, validator_employee_no VARCHAR(100) NOT NULL,
 administrator_override BIT NOT NULL, validation_remarks NVARCHAR(4000) NULL, override_reason NVARCHAR(1000) NULL,
 validated_at DATETIMEOFFSET NOT NULL, active BIT NOT NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_prime_validation_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_validation_case FOREIGN KEY(assessment_case_id) REFERENCES [${primehrSchema}].prime_assessment_case(id),
 CONSTRAINT uk_prime_assessment_validation_case UNIQUE(assessment_case_id),
 CONSTRAINT ck_prime_validation_override_reason CHECK(administrator_override=0 OR override_reason IS NOT NULL)
);
CREATE TABLE [${primehrSchema}].prime_assessment_validated_rating (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_validated_rating PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 validation_id VARCHAR(36) NOT NULL, competency_id VARCHAR(36) NOT NULL, final_proficiency_level_id VARCHAR(36) NOT NULL,
 validation_remarks NVARCHAR(2000) NULL, contributing_assignment_ids VARCHAR(4000) NOT NULL, active BIT NOT NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_prime_validated_rating_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_validated_rating_validation FOREIGN KEY(validation_id) REFERENCES [${primehrSchema}].prime_assessment_validation(id),
 CONSTRAINT fk_prime_validated_rating_competency FOREIGN KEY(competency_id) REFERENCES [${primehrSchema}].prime_competency(id),
 CONSTRAINT fk_prime_validated_rating_level FOREIGN KEY(final_proficiency_level_id) REFERENCES [${primehrSchema}].prime_proficiency_level(id),
 CONSTRAINT uk_prime_validated_rating UNIQUE(validation_id,competency_id)
);
CREATE TABLE [${primehrSchema}].prime_person_competency_profile (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_person_profile PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 assessment_case_id VARCHAR(36) NOT NULL, validation_id VARCHAR(36) NOT NULL, predecessor_id VARCHAR(36) NULL,
 subject_employee_id BIGINT NOT NULL, subject_employee_no VARCHAR(100) NOT NULL, subject_display_name NVARCHAR(300) NOT NULL,
 appointment_id BIGINT NOT NULL, job_position_id BIGINT NOT NULL, plantilla_id BIGINT NULL,
 cycle_id VARCHAR(36) NOT NULL, tool_id VARCHAR(36) NOT NULL, position_profile_id VARCHAR(36) NOT NULL,
 position_profile_definition_version INT NOT NULL, position_profile_content_revision BIGINT NOT NULL,
 profile_version INT NOT NULL, valid_from DATE NOT NULL, valid_to DATE NULL, reassessment_date DATE NULL,
 source_method_summary VARCHAR(1000) NOT NULL, status VARCHAR(30) NOT NULL, validated_at DATETIMEOFFSET NOT NULL,
 active BIT NOT NULL, record_version BIGINT NOT NULL CONSTRAINT df_prime_person_profile_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_person_profile_case FOREIGN KEY(assessment_case_id) REFERENCES [${primehrSchema}].prime_assessment_case(id),
 CONSTRAINT fk_prime_person_profile_validation FOREIGN KEY(validation_id) REFERENCES [${primehrSchema}].prime_assessment_validation(id),
 CONSTRAINT fk_prime_person_profile_predecessor FOREIGN KEY(predecessor_id) REFERENCES [${primehrSchema}].prime_person_competency_profile(id),
 CONSTRAINT uk_prime_person_profile_case UNIQUE(assessment_case_id), CONSTRAINT uk_prime_person_profile_validation UNIQUE(validation_id),
 CONSTRAINT uk_prime_person_profile_version UNIQUE(agency_id,subject_employee_id,profile_version),
 CONSTRAINT ck_prime_person_profile_status CHECK(status='VALIDATED'), CONSTRAINT ck_prime_person_profile_version CHECK(profile_version>=1),
 CONSTRAINT ck_prime_person_profile_dates CHECK((valid_to IS NULL OR valid_to>=valid_from) AND (reassessment_date IS NULL OR reassessment_date>valid_from))
);
CREATE TABLE [${primehrSchema}].prime_person_competency_result (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_person_result PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 person_profile_id VARCHAR(36) NOT NULL, competency_id VARCHAR(36) NOT NULL, attained_proficiency_level_id VARCHAR(36) NOT NULL,
 validated_rating_id VARCHAR(36) NOT NULL, active BIT NOT NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_prime_person_result_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_person_result_profile FOREIGN KEY(person_profile_id) REFERENCES [${primehrSchema}].prime_person_competency_profile(id),
 CONSTRAINT fk_prime_person_result_competency FOREIGN KEY(competency_id) REFERENCES [${primehrSchema}].prime_competency(id),
 CONSTRAINT fk_prime_person_result_level FOREIGN KEY(attained_proficiency_level_id) REFERENCES [${primehrSchema}].prime_proficiency_level(id),
 CONSTRAINT fk_prime_person_result_validated FOREIGN KEY(validated_rating_id) REFERENCES [${primehrSchema}].prime_assessment_validated_rating(id),
 CONSTRAINT uk_prime_person_result UNIQUE(person_profile_id,competency_id), CONSTRAINT uk_prime_person_result_validated UNIQUE(validated_rating_id)
);
CREATE INDEX ix_prime_validation_status ON [${primehrSchema}].prime_assessment_validation(agency_id,validated_at);
CREATE INDEX ix_prime_person_profile_latest ON [${primehrSchema}].prime_person_competency_profile(agency_id,subject_employee_no,valid_from,valid_to,profile_version);
CREATE INDEX ix_prime_person_result_profile ON [${primehrSchema}].prime_person_competency_result(person_profile_id,competency_id);

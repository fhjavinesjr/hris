ALTER TABLE [${primehrSchema}].rsp_recruitment_plan ADD
 submitted_by VARCHAR(100) NULL,
 submitted_at DATETIMEOFFSET NULL,
 approved_by VARCHAR(100) NULL,
 approved_at DATETIMEOFFSET NULL;

ALTER TABLE [${primehrSchema}].rsp_vacancy_request ADD
 submitted_by VARCHAR(100) NULL,
 submitted_at DATETIMEOFFSET NULL,
 decided_by VARCHAR(100) NULL,
 decided_at DATETIMEOFFSET NULL;

CREATE TABLE [${primehrSchema}].rsp_vacancy_publication (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_vacancy_publication PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 vacancy_request_id VARCHAR(36) NOT NULL,
 status VARCHAR(30) NOT NULL,
 visibility VARCHAR(20) NOT NULL,
 opening_date DATE NOT NULL,
 closing_date DATE NOT NULL,
 instructions NVARCHAR(4000) NOT NULL,
 place_of_assignment NVARCHAR(300) NOT NULL,
 contact_guidance NVARCHAR(2000) NOT NULL,
 notice_text NVARCHAR(4000) NOT NULL,
 plantilla_id BIGINT NOT NULL,
 plantilla_name NVARCHAR(200) NOT NULL,
 job_position_id BIGINT NOT NULL,
 job_position_name NVARCHAR(200) NOT NULL,
 salary_grade BIGINT NULL,
 salary_step BIGINT NULL,
 business_unit_id BIGINT NOT NULL,
 business_unit_code VARCHAR(100) NULL,
 business_unit_name NVARCHAR(300) NOT NULL,
 qualification_standard_id BIGINT NOT NULL,
 qualification_standard_version INT NOT NULL,
 education_requirement NVARCHAR(2000) NOT NULL,
 training_requirement NVARCHAR(2000) NOT NULL,
 experience_requirement NVARCHAR(2000) NOT NULL,
 eligibility_requirement NVARCHAR(2000) NOT NULL,
 license_requirement NVARCHAR(2000) NULL,
 qualification_source_basis NVARCHAR(1000) NULL,
 position_profile_id VARCHAR(36) NOT NULL,
 position_profile_definition_version INT NOT NULL,
 position_profile_record_revision BIGINT NOT NULL,
 administrative_fingerprint VARCHAR(64) NOT NULL,
 hrm_fingerprint VARCHAR(64) NOT NULL,
 source_snapshot_at DATETIMEOFFSET NOT NULL,
 submitted_by VARCHAR(100) NULL,
 submitted_at DATETIMEOFFSET NULL,
 approved_by VARCHAR(100) NULL,
 approved_at DATETIMEOFFSET NULL,
 published_by VARCHAR(100) NULL,
 published_at DATETIMEOFFSET NULL,
 closed_by VARCHAR(100) NULL,
 closed_at DATETIMEOFFSET NULL,
 cancelled_by VARCHAR(100) NULL,
 cancelled_at DATETIMEOFFSET NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_rsp_publication_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_publication_vacancy FOREIGN KEY(vacancy_request_id)
   REFERENCES [${primehrSchema}].rsp_vacancy_request(id),
 CONSTRAINT uk_rsp_publication_vacancy UNIQUE(vacancy_request_id),
 CONSTRAINT ck_rsp_publication_status CHECK(status IN
   ('DRAFT','SUBMITTED','RETURNED','APPROVED','PUBLISHED','CLOSED','CANCELLED')),
 CONSTRAINT ck_rsp_publication_visibility CHECK(visibility IN ('INTERNAL','EXTERNAL','BOTH')),
 CONSTRAINT ck_rsp_publication_dates CHECK(closing_date>=opening_date),
 CONSTRAINT ck_rsp_publication_versions CHECK(
   qualification_standard_version>=1 AND position_profile_definition_version>=1
   AND position_profile_record_revision>=0)
);

CREATE TABLE [${primehrSchema}].rsp_vacancy_publication_channel (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_publication_channel PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 publication_id VARCHAR(36) NOT NULL,
 channel_name NVARCHAR(200) NOT NULL,
 publication_date DATE NOT NULL,
 reference NVARCHAR(1000) NULL,
 active BIT NOT NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_rsp_publication_channel_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_publication_channel FOREIGN KEY(publication_id)
   REFERENCES [${primehrSchema}].rsp_vacancy_publication(id),
 CONSTRAINT uk_rsp_publication_channel UNIQUE(publication_id,channel_name)
);

CREATE TABLE [${primehrSchema}].rsp_vacancy_publication_requirement (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_rsp_publication_requirement PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 publication_id VARCHAR(36) NOT NULL,
 competency_version_id VARCHAR(36) NOT NULL,
 competency_code VARCHAR(100) NOT NULL,
 competency_name NVARCHAR(200) NOT NULL,
 competency_definition_version INT NOT NULL,
 required_level_id VARCHAR(36) NOT NULL,
 required_level_code VARCHAR(100) NOT NULL,
 required_level_label NVARCHAR(200) NOT NULL,
 classification VARCHAR(30) NOT NULL,
 criticality_code VARCHAR(100) NULL,
 remarks NVARCHAR(1000) NULL,
 display_order INT NOT NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_rsp_publication_requirement_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_rsp_publication_requirement FOREIGN KEY(publication_id)
   REFERENCES [${primehrSchema}].rsp_vacancy_publication(id),
 CONSTRAINT uk_rsp_publication_requirement UNIQUE(publication_id,competency_version_id),
 CONSTRAINT ck_rsp_publication_requirement_versions CHECK(competency_definition_version>=1)
);

CREATE INDEX ix_rsp_publication_status
 ON [${primehrSchema}].rsp_vacancy_publication(agency_id,status,opening_date,closing_date);
CREATE INDEX ix_rsp_publication_plantilla
 ON [${primehrSchema}].rsp_vacancy_publication(agency_id,plantilla_id,status);
CREATE INDEX ix_rsp_publication_channel
 ON [${primehrSchema}].rsp_vacancy_publication_channel(agency_id,publication_id,active);
CREATE INDEX ix_rsp_publication_requirement
 ON [${primehrSchema}].rsp_vacancy_publication_requirement(agency_id,publication_id,display_order);

ALTER TABLE "${primehrSchema}".rsp_recruitment_plan
 ADD COLUMN submitted_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".rsp_recruitment_plan
 ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "${primehrSchema}".rsp_recruitment_plan
 ADD COLUMN approved_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".rsp_recruitment_plan
 ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE "${primehrSchema}".rsp_vacancy_request
 ADD COLUMN submitted_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".rsp_vacancy_request
 ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "${primehrSchema}".rsp_vacancy_request
 ADD COLUMN decided_by VARCHAR(100);
ALTER TABLE "${primehrSchema}".rsp_vacancy_request
 ADD COLUMN decided_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE "${primehrSchema}".rsp_vacancy_publication (
 id VARCHAR(36) PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 vacancy_request_id VARCHAR(36) NOT NULL,
 status VARCHAR(30) NOT NULL,
 visibility VARCHAR(20) NOT NULL,
 opening_date DATE NOT NULL,
 closing_date DATE NOT NULL,
 instructions VARCHAR(4000) NOT NULL,
 place_of_assignment VARCHAR(300) NOT NULL,
 contact_guidance VARCHAR(2000) NOT NULL,
 notice_text VARCHAR(4000) NOT NULL,
 plantilla_id BIGINT NOT NULL,
 plantilla_name VARCHAR(200) NOT NULL,
 job_position_id BIGINT NOT NULL,
 job_position_name VARCHAR(200) NOT NULL,
 salary_grade BIGINT,
 salary_step BIGINT,
 business_unit_id BIGINT NOT NULL,
 business_unit_code VARCHAR(100),
 business_unit_name VARCHAR(300) NOT NULL,
 qualification_standard_id BIGINT NOT NULL,
 qualification_standard_version INTEGER NOT NULL,
 education_requirement VARCHAR(2000) NOT NULL,
 training_requirement VARCHAR(2000) NOT NULL,
 experience_requirement VARCHAR(2000) NOT NULL,
 eligibility_requirement VARCHAR(2000) NOT NULL,
 license_requirement VARCHAR(2000),
 qualification_source_basis VARCHAR(1000),
 position_profile_id VARCHAR(36) NOT NULL,
 position_profile_definition_version INTEGER NOT NULL,
 position_profile_record_revision BIGINT NOT NULL,
 administrative_fingerprint VARCHAR(64) NOT NULL,
 hrm_fingerprint VARCHAR(64) NOT NULL,
 source_snapshot_at TIMESTAMP WITH TIME ZONE NOT NULL,
 submitted_by VARCHAR(100),
 submitted_at TIMESTAMP WITH TIME ZONE,
 approved_by VARCHAR(100),
 approved_at TIMESTAMP WITH TIME ZONE,
 published_by VARCHAR(100),
 published_at TIMESTAMP WITH TIME ZONE,
 closed_by VARCHAR(100),
 closed_at TIMESTAMP WITH TIME ZONE,
 cancelled_by VARCHAR(100),
 cancelled_at TIMESTAMP WITH TIME ZONE,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_publication_vacancy FOREIGN KEY(vacancy_request_id)
   REFERENCES "${primehrSchema}".rsp_vacancy_request(id),
 CONSTRAINT uk_rsp_publication_vacancy UNIQUE(vacancy_request_id),
 CONSTRAINT ck_rsp_publication_status CHECK(status IN
   ('DRAFT','SUBMITTED','RETURNED','APPROVED','PUBLISHED','CLOSED','CANCELLED')),
 CONSTRAINT ck_rsp_publication_visibility CHECK(visibility IN ('INTERNAL','EXTERNAL','BOTH')),
 CONSTRAINT ck_rsp_publication_dates CHECK(closing_date>=opening_date),
 CONSTRAINT ck_rsp_publication_versions CHECK(
   qualification_standard_version>=1 AND position_profile_definition_version>=1
   AND position_profile_record_revision>=0)
);

CREATE TABLE "${primehrSchema}".rsp_vacancy_publication_channel (
 id VARCHAR(36) PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 publication_id VARCHAR(36) NOT NULL,
 channel_name VARCHAR(200) NOT NULL,
 publication_date DATE NOT NULL,
 reference VARCHAR(1000),
 active BOOLEAN NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_publication_channel FOREIGN KEY(publication_id)
   REFERENCES "${primehrSchema}".rsp_vacancy_publication(id),
 CONSTRAINT uk_rsp_publication_channel UNIQUE(publication_id,channel_name)
);

CREATE TABLE "${primehrSchema}".rsp_vacancy_publication_requirement (
 id VARCHAR(36) PRIMARY KEY,
 agency_id VARCHAR(64) NOT NULL,
 publication_id VARCHAR(36) NOT NULL,
 competency_version_id VARCHAR(36) NOT NULL,
 competency_code VARCHAR(100) NOT NULL,
 competency_name VARCHAR(200) NOT NULL,
 competency_definition_version INTEGER NOT NULL,
 required_level_id VARCHAR(36) NOT NULL,
 required_level_code VARCHAR(100) NOT NULL,
 required_level_label VARCHAR(200) NOT NULL,
 classification VARCHAR(30) NOT NULL,
 criticality_code VARCHAR(100),
 remarks VARCHAR(1000),
 display_order INTEGER NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_publication_requirement FOREIGN KEY(publication_id)
   REFERENCES "${primehrSchema}".rsp_vacancy_publication(id),
 CONSTRAINT uk_rsp_publication_requirement UNIQUE(publication_id,competency_version_id),
 CONSTRAINT ck_rsp_publication_requirement_versions CHECK(competency_definition_version>=1)
);

CREATE INDEX ix_rsp_publication_status
 ON "${primehrSchema}".rsp_vacancy_publication(agency_id,status,opening_date,closing_date);
CREATE INDEX ix_rsp_publication_plantilla
 ON "${primehrSchema}".rsp_vacancy_publication(agency_id,plantilla_id,status);
CREATE INDEX ix_rsp_publication_channel
 ON "${primehrSchema}".rsp_vacancy_publication_channel(agency_id,publication_id,active);
CREATE INDEX ix_rsp_publication_requirement
 ON "${primehrSchema}".rsp_vacancy_publication_requirement(agency_id,publication_id,display_order);

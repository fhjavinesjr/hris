CREATE TABLE "${primehrSchema}".rsp_recruitment_plan (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(100) NOT NULL,
 title VARCHAR(200) NOT NULL, period_start DATE NOT NULL, period_end DATE NOT NULL,
 description VARCHAR(4000), status VARCHAR(30) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_rsp_plan_code UNIQUE(agency_id,code),
 CONSTRAINT ck_rsp_plan_dates CHECK(period_end>=period_start),
 CONSTRAINT ck_rsp_plan_status CHECK(status IN ('DRAFT','SUBMITTED','RETURNED','APPROVED','ARCHIVED'))
);

CREATE TABLE "${primehrSchema}".rsp_vacancy_request (
 id VARCHAR(36) PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, plan_id VARCHAR(36) NOT NULL,
 active BOOLEAN NOT NULL, status VARCHAR(30) NOT NULL, vacancy_type VARCHAR(30) NOT NULL,
 anticipated_vacancy_date DATE, anticipated_reason_code VARCHAR(80), anticipated_explanation VARCHAR(2000),
 authority_reference VARCHAR(500), recruitment_priority VARCHAR(80) NOT NULL, target_fill_date DATE,
 justification VARCHAR(4000) NOT NULL,
 plantilla_id BIGINT NOT NULL, plantilla_name VARCHAR(200) NOT NULL,
 job_position_id BIGINT NOT NULL, job_position_name VARCHAR(200) NOT NULL,
 salary_grade BIGINT, salary_step BIGINT, business_unit_id BIGINT NOT NULL,
 business_unit_code VARCHAR(100), business_unit_name VARCHAR(300) NOT NULL,
 qualification_standard_id BIGINT NOT NULL, qualification_standard_version INTEGER NOT NULL,
 education_requirement VARCHAR(2000) NOT NULL, training_requirement VARCHAR(2000) NOT NULL,
 experience_requirement VARCHAR(2000) NOT NULL, eligibility_requirement VARCHAR(2000) NOT NULL,
 license_requirement VARCHAR(2000), qualification_source_basis VARCHAR(1000),
 position_profile_id VARCHAR(36) NOT NULL, position_profile_definition_version INTEGER NOT NULL,
 position_profile_record_revision BIGINT NOT NULL,
 administrative_fingerprint VARCHAR(64) NOT NULL, administrative_fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
 occupied BOOLEAN NOT NULL, active_appointment_id BIGINT,
 occupancy_assumption_date TIMESTAMP, hrm_fingerprint VARCHAR(64) NOT NULL,
 hrm_fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_rsp_vacancy_plan FOREIGN KEY(plan_id) REFERENCES "${primehrSchema}".rsp_recruitment_plan(id),
 CONSTRAINT fk_rsp_vacancy_profile FOREIGN KEY(position_profile_id) REFERENCES "${primehrSchema}".prime_position_profile(id),
 CONSTRAINT uk_rsp_vacancy_plan_plantilla UNIQUE(plan_id,plantilla_id),
 CONSTRAINT ck_rsp_vacancy_status CHECK(status IN ('DRAFT','SUBMITTED','RETURNED','AUTHORIZED','DECLINED','CANCELLED')),
 CONSTRAINT ck_rsp_vacancy_type CHECK(vacancy_type IN ('ACTUAL','ANTICIPATED')),
 CONSTRAINT ck_rsp_vacancy_anticipated CHECK((vacancy_type='ACTUAL' AND occupied=FALSE AND anticipated_vacancy_date IS NULL AND anticipated_reason_code IS NULL AND anticipated_explanation IS NULL)
  OR (vacancy_type='ANTICIPATED' AND anticipated_vacancy_date IS NOT NULL AND anticipated_reason_code IS NOT NULL AND anticipated_explanation IS NOT NULL)),
 CONSTRAINT ck_rsp_vacancy_occupant CHECK((occupied=FALSE AND active_appointment_id IS NULL AND occupancy_assumption_date IS NULL)
  OR (occupied=TRUE AND active_appointment_id IS NOT NULL AND occupancy_assumption_date IS NOT NULL)),
 CONSTRAINT ck_rsp_vacancy_versions CHECK(qualification_standard_version>=1 AND position_profile_definition_version>=1 AND position_profile_record_revision>=0)
);

CREATE INDEX ix_rsp_plan_period ON "${primehrSchema}".rsp_recruitment_plan(agency_id,status,period_start,period_end);
CREATE INDEX ix_rsp_vacancy_plantilla ON "${primehrSchema}".rsp_vacancy_request(agency_id,plantilla_id,status,active);
CREATE INDEX ix_rsp_vacancy_plan ON "${primehrSchema}".rsp_vacancy_request(agency_id,plan_id,status);

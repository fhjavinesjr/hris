CREATE TABLE [${primehrSchema}].prime_ld_referral (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_ld_referral PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 analysis_id VARCHAR(36) NOT NULL, subject_employee_id BIGINT NOT NULL,
 subject_employee_no VARCHAR(100) NOT NULL, subject_display_name NVARCHAR(300) NOT NULL,
 analysis_date DATE NOT NULL, position_name NVARCHAR(200) NOT NULL,
 status VARCHAR(30) NOT NULL, development_need NVARCHAR(4000) NOT NULL,
 recommended_intervention NVARCHAR(4000) NOT NULL, target_completion_date DATE NULL,
 referral_reason NVARCHAR(1000) NULL, remarks NVARCHAR(2000) NULL,
 referred_by VARCHAR(100) NULL, referred_at DATETIMEOFFSET NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_prime_ld_referral_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_ld_referral_analysis FOREIGN KEY(analysis_id) REFERENCES [${primehrSchema}].prime_competency_gap_analysis(id),
 CONSTRAINT ck_prime_ld_referral_status CHECK(status IN ('DRAFT','REFERRED','ARCHIVED')),
 CONSTRAINT ck_prime_ld_referral_submission CHECK(
  (status='REFERRED' AND referred_by IS NOT NULL AND referred_at IS NOT NULL)
  OR (status IN ('DRAFT','ARCHIVED') AND referred_by IS NULL AND referred_at IS NULL))
);

CREATE TABLE [${primehrSchema}].prime_ld_referral_item (
 id VARCHAR(36) NOT NULL CONSTRAINT pk_prime_ld_referral_item PRIMARY KEY, agency_id VARCHAR(64) NOT NULL,
 referral_id VARCHAR(36) NOT NULL, analysis_id VARCHAR(36) NOT NULL,
 gap_item_id VARCHAR(36) NOT NULL, active BIT NOT NULL,
 competency_code VARCHAR(50) NOT NULL, competency_name NVARCHAR(200) NOT NULL,
 gap_classification VARCHAR(30) NOT NULL, not_assessed_reason VARCHAR(40) NULL,
 gap_value INT NULL, priority_code VARCHAR(50) NULL, priority_label NVARCHAR(150) NULL,
 priority_rank INT NULL, display_order INT NOT NULL,
 record_version BIGINT NOT NULL CONSTRAINT df_prime_ld_referral_item_version DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_prime_ld_referral_item_referral FOREIGN KEY(referral_id) REFERENCES [${primehrSchema}].prime_ld_referral(id),
 CONSTRAINT fk_prime_ld_referral_item_analysis FOREIGN KEY(analysis_id) REFERENCES [${primehrSchema}].prime_competency_gap_analysis(id),
 CONSTRAINT fk_prime_ld_referral_item_gap FOREIGN KEY(gap_item_id) REFERENCES [${primehrSchema}].prime_competency_gap_item(id),
 CONSTRAINT uk_prime_ld_referral_item UNIQUE(referral_id,gap_item_id),
 CONSTRAINT ck_prime_ld_referral_item_class CHECK(gap_classification IN ('BELOW','NOT_ASSESSED')),
 CONSTRAINT ck_prime_ld_referral_item_reason CHECK(not_assessed_reason IS NULL OR not_assessed_reason IN ('NO_RESULT','VERSION_NOT_COMPARABLE')),
 CONSTRAINT ck_prime_ld_referral_item_values CHECK(
  (gap_classification='NOT_ASSESSED' AND gap_value IS NULL AND not_assessed_reason IS NOT NULL)
  OR (gap_classification='BELOW' AND gap_value IS NOT NULL AND gap_value>0 AND not_assessed_reason IS NULL)),
 CONSTRAINT ck_prime_ld_referral_item_order CHECK(display_order>=0)
);

CREATE INDEX ix_prime_ld_referral_employee ON [${primehrSchema}].prime_ld_referral(agency_id,subject_employee_no,status);
CREATE INDEX ix_prime_ld_referral_analysis ON [${primehrSchema}].prime_ld_referral(agency_id,analysis_id,status);
CREATE INDEX ix_prime_ld_referral_item_gap ON [${primehrSchema}].prime_ld_referral_item(agency_id,analysis_id,gap_item_id,active);

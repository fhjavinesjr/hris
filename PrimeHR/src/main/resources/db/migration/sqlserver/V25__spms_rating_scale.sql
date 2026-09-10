CREATE TABLE ${primehrSchema}.spms_rating_scale (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, code VARCHAR(80) NOT NULL,
 normalized_code VARCHAR(80) NOT NULL, record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT uk_spms_rating_scale_code UNIQUE(agency_id,normalized_code)
);

CREATE TABLE ${primehrSchema}.spms_rating_scale_version (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, rating_scale_id VARCHAR(36) NOT NULL,
 policy_version_id VARCHAR(36) NOT NULL, definition_version INT NOT NULL, supersedes_id VARCHAR(36),
 title VARCHAR(200) NOT NULL, description VARCHAR(2000), legal_basis VARCHAR(1000) NOT NULL,
 minimum_score DECIMAL(19,6) NOT NULL, maximum_score DECIMAL(19,6) NOT NULL,
 rounding_scale INT NOT NULL, rounding_mode VARCHAR(20) NOT NULL, measure_type VARCHAR(30) NOT NULL,
 direction VARCHAR(30) NOT NULL, aggregation VARCHAR(30) NOT NULL, missing_value_policy VARCHAR(20) NOT NULL,
 dimension_score_source VARCHAR(30) NOT NULL, status VARCHAR(20) NOT NULL, band_revision INT NOT NULL DEFAULT 0,
 effective_from DATE, effective_to DATE, published_by VARCHAR(100), published_at DATETIMEOFFSET,
 retired_by VARCHAR(100), retired_at DATETIMEOFFSET, retirement_reason VARCHAR(1000),
 record_version BIGINT NOT NULL DEFAULT 0, created_by VARCHAR(100) NOT NULL,
 created_at DATETIMEOFFSET NOT NULL, updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_rating_scale_version_scale FOREIGN KEY(rating_scale_id) REFERENCES ${primehrSchema}.spms_rating_scale(id),
 CONSTRAINT fk_spms_rating_scale_version_policy FOREIGN KEY(policy_version_id) REFERENCES ${primehrSchema}.spms_policy_version(id),
 CONSTRAINT fk_spms_rating_scale_version_prior FOREIGN KEY(supersedes_id) REFERENCES ${primehrSchema}.spms_rating_scale_version(id),
 CONSTRAINT uk_spms_rating_scale_version UNIQUE(agency_id,rating_scale_id,definition_version),
 CONSTRAINT ck_spms_rating_scale_number CHECK(definition_version>=1),
 CONSTRAINT ck_spms_rating_scale_range CHECK(maximum_score>=minimum_score),
 CONSTRAINT ck_spms_rating_scale_precision CHECK(rounding_scale BETWEEN 0 AND 6),
 CONSTRAINT ck_spms_rating_scale_rounding CHECK(rounding_mode IN ('HALF_UP','HALF_EVEN','DOWN')),
 CONSTRAINT ck_spms_rating_scale_measure CHECK(measure_type IN ('COUNT','NUMBER','PERCENTAGE','CURRENCY','DURATION','DATE_MILESTONE','BOOLEAN','MANUAL_RUBRIC')),
 CONSTRAINT ck_spms_rating_scale_direction CHECK(direction IN ('HIGHER_IS_BETTER','LOWER_IS_BETTER','EXACT_TARGET','WITHIN_RANGE','MANUAL_RUBRIC')),
 CONSTRAINT ck_spms_rating_scale_aggregation CHECK(aggregation='WEIGHTED_AVERAGE'),
 CONSTRAINT ck_spms_rating_scale_missing CHECK(missing_value_policy='ERROR'),
 CONSTRAINT ck_spms_rating_scale_source CHECK(dimension_score_source IN ('THRESHOLD_BAND','MANUAL_RUBRIC')),
 CONSTRAINT ck_spms_rating_scale_status CHECK(status IN ('DRAFT','PUBLISHED','RETIRED')),
 CONSTRAINT ck_spms_rating_scale_dates CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from)
);
CREATE INDEX ix_spms_rating_scale_effective ON ${primehrSchema}.spms_rating_scale_version(agency_id,rating_scale_id,status,effective_from,effective_to);

CREATE TABLE ${primehrSchema}.spms_rating_band (
 id VARCHAR(36) NOT NULL PRIMARY KEY, agency_id VARCHAR(64) NOT NULL, rating_scale_version_id VARCHAR(36) NOT NULL,
 code VARCHAR(80) NOT NULL, normalized_code VARCHAR(80) NOT NULL, numeric_score DECIMAL(19,6) NOT NULL,
 label VARCHAR(200) NOT NULL, lower_bound DECIMAL(19,6) NOT NULL, upper_bound DECIMAL(19,6) NOT NULL,
 display_order INT NOT NULL, guidance VARCHAR(2000), record_version BIGINT NOT NULL DEFAULT 0,
 created_by VARCHAR(100) NOT NULL, created_at DATETIMEOFFSET NOT NULL,
 updated_by VARCHAR(100) NOT NULL, updated_at DATETIMEOFFSET NOT NULL,
 CONSTRAINT fk_spms_rating_band_version FOREIGN KEY(rating_scale_version_id) REFERENCES ${primehrSchema}.spms_rating_scale_version(id),
 CONSTRAINT uk_spms_rating_band_code UNIQUE(agency_id,rating_scale_version_id,normalized_code),
 CONSTRAINT uk_spms_rating_band_order UNIQUE(agency_id,rating_scale_version_id,display_order),
 CONSTRAINT ck_spms_rating_band_order CHECK(display_order>=1),
 CONSTRAINT ck_spms_rating_band_bounds CHECK(upper_bound>=lower_bound)
);
CREATE INDEX ix_spms_rating_band_version ON ${primehrSchema}.spms_rating_band(agency_id,rating_scale_version_id,display_order);

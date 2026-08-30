-- Phase 5A.1 reviewable Administrative migration for SQL Server.
-- Apply once to the Administrative database before changing ddl-auto to validate/none.
CREATE TABLE dbo.qualification_standard (
    qualificationStandardId BIGINT IDENTITY(1,1) NOT NULL
        CONSTRAINT pk_qualification_standard PRIMARY KEY,
    jobPositionId BIGINT NOT NULL,
    definitionVersion INT NOT NULL,
    supersedesId BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    education NVARCHAR(2000) NOT NULL,
    training NVARCHAR(2000) NOT NULL,
    experience NVARCHAR(2000) NOT NULL,
    eligibility NVARCHAR(2000) NOT NULL,
    licenseRequirement NVARCHAR(2000) NULL,
    sourceBasis NVARCHAR(1000) NULL,
    effectiveFrom DATE NULL,
    effectiveTo DATE NULL,
    createdBy VARCHAR(100) NOT NULL,
    createdAt DATETIMEOFFSET NOT NULL,
    updatedBy VARCHAR(100) NOT NULL,
    updatedAt DATETIMEOFFSET NOT NULL,
    publishedBy VARCHAR(100) NULL,
    publishedAt DATETIMEOFFSET NULL,
    recordVersion BIGINT NOT NULL CONSTRAINT df_qs_record_version DEFAULT 0,
    CONSTRAINT fk_qs_job_position FOREIGN KEY (jobPositionId)
        REFERENCES dbo.job_position(jobPositionId),
    CONSTRAINT fk_qs_supersedes FOREIGN KEY (supersedesId)
        REFERENCES dbo.qualification_standard(qualificationStandardId),
    CONSTRAINT uk_qs_job_version UNIQUE (jobPositionId, definitionVersion),
    CONSTRAINT ck_qs_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_qs_effectivity CHECK (effectiveTo IS NULL OR effectiveFrom IS NULL OR effectiveTo >= effectiveFrom)
);

CREATE INDEX ix_qs_job_status_effective
    ON dbo.qualification_standard(jobPositionId, status, effectiveFrom, effectiveTo);

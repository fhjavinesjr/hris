ALTER TABLE [${primehrSchema}].rsp_evaluation_proceeding
    DROP CONSTRAINT uk_rsp_evaluation_current_publication;

CREATE UNIQUE INDEX uk_rsp_evaluation_current_publication
    ON [${primehrSchema}].rsp_evaluation_proceeding(agency_id, current_publication_key)
    WHERE current_publication_key IS NOT NULL;

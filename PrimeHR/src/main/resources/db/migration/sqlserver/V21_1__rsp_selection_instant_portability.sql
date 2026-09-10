ALTER TABLE ${primehrSchema}.rsp_selection_case DROP CONSTRAINT ck_rsp_selection_submit;
ALTER TABLE ${primehrSchema}.rsp_selection_case DROP CONSTRAINT ck_rsp_selection_approve;
ALTER TABLE ${primehrSchema}.rsp_selection_case DROP CONSTRAINT ck_rsp_selection_finalize;
ALTER TABLE ${primehrSchema}.rsp_offer_response DROP CONSTRAINT ck_rsp_offer_response;

ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN offer_response_deadline DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN submitted_at DATETIMEOFFSET NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN approved_at DATETIMEOFFSET NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN finalized_at DATETIMEOFFSET NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN returned_at DATETIMEOFFSET NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN created_at DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_case ALTER COLUMN updated_at DATETIMEOFFSET NOT NULL;

ALTER TABLE ${primehrSchema}.rsp_selection_candidate_decision ALTER COLUMN created_at DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_candidate_decision ALTER COLUMN updated_at DATETIMEOFFSET NOT NULL;

ALTER TABLE ${primehrSchema}.rsp_selection_notice ALTER COLUMN released_at DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_notice ALTER COLUMN created_at DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_selection_notice ALTER COLUMN updated_at DATETIMEOFFSET NOT NULL;

ALTER TABLE ${primehrSchema}.rsp_offer_response ALTER COLUMN response_deadline DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_offer_response ALTER COLUMN responded_at DATETIMEOFFSET NULL;
ALTER TABLE ${primehrSchema}.rsp_offer_response ALTER COLUMN created_at DATETIMEOFFSET NOT NULL;
ALTER TABLE ${primehrSchema}.rsp_offer_response ALTER COLUMN updated_at DATETIMEOFFSET NOT NULL;

ALTER TABLE ${primehrSchema}.rsp_selection_case ADD CONSTRAINT ck_rsp_selection_submit CHECK(status NOT IN ('SUBMITTED','APPROVED','FINALIZED') OR (submitted_by IS NOT NULL AND submitted_at IS NOT NULL));
ALTER TABLE ${primehrSchema}.rsp_selection_case ADD CONSTRAINT ck_rsp_selection_approve CHECK(status NOT IN ('APPROVED','FINALIZED') OR (approved_by IS NOT NULL AND approved_at IS NOT NULL));
ALTER TABLE ${primehrSchema}.rsp_selection_case ADD CONSTRAINT ck_rsp_selection_finalize CHECK(status<>'FINALIZED' OR (finalized_by IS NOT NULL AND finalized_at IS NOT NULL));
ALTER TABLE ${primehrSchema}.rsp_offer_response ADD CONSTRAINT ck_rsp_offer_response CHECK((status='PENDING' AND responded_at IS NULL AND idempotency_key IS NULL) OR (status IN ('ACCEPTED','DECLINED') AND responded_at IS NOT NULL AND idempotency_key IS NOT NULL) OR status='EXPIRED');

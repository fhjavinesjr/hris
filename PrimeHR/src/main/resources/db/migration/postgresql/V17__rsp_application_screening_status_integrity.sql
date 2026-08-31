ALTER TABLE ${primehrSchema}.rsp_position_application DROP CONSTRAINT ck_rsp_application_submission;

ALTER TABLE ${primehrSchema}.rsp_position_application ADD CONSTRAINT ck_rsp_application_submission CHECK(
  (status='DRAFT' AND acknowledgment_number IS NULL AND submitted_at IS NULL)
  OR (status IN ('SUBMITTED','UNDER_SCREENING','QUALIFIED','DISQUALIFIED','WITHDRAWN')
      AND acknowledgment_number IS NOT NULL AND submitted_at IS NOT NULL)
);

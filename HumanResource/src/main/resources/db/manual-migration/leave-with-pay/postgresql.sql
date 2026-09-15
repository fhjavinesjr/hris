ALTER TABLE leave_application
    ADD COLUMN IF NOT EXISTS withPay boolean NOT NULL DEFAULT TRUE;

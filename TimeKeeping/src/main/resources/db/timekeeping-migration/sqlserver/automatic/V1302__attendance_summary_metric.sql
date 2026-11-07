IF COL_LENGTH('mra_print_day', 'metric_minutes') IS NULL
    ALTER TABLE mra_print_day ADD metric_minutes INT NOT NULL DEFAULT 0;

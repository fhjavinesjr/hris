IF OBJECT_ID('mra_print_day', 'U') IS NULL
    CREATE TABLE mra_print_day (
        run_id VARCHAR(36) NOT NULL,
        created_at DATETIME2(6) NOT NULL,
        employee_id BIGINT NOT NULL,
        employee_no VARCHAR(100) NOT NULL,
        employee_name VARCHAR(255) NOT NULL,
        business_unit_id BIGINT NOT NULL,
        business_unit VARCHAR(255) NOT NULL,
        work_date DATE NOT NULL,
        attendance_status VARCHAR(100) NOT NULL,
        leave_type VARCHAR(100) NULL,
        work_minutes INT NOT NULL,
        late_minutes INT NOT NULL,
        undertime_minutes INT NOT NULL,
        overtime_minutes INT NOT NULL,
        absent_minutes INT NOT NULL,
        overtime_approved INT NOT NULL,
        remarks VARCHAR(1000) NULL,
        CONSTRAINT pk_mra_print_day PRIMARY KEY (run_id, employee_id, business_unit_id, work_date)
    );

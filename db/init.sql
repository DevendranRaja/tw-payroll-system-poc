-------------------------------------------------------
-- ENUM TYPES
-------------------------------------------------------

CREATE TYPE employee_status AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE pay_cycle_type AS ENUM ('WEEKLY', 'BIWEEKLY', 'MONTHLY');
CREATE TYPE payroll_status AS ENUM ('PROCESSED', 'FAILED', 'SUBMITTED', 'SUBMISSION_FAILED');
CREATE TYPE integration_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');

-------------------------------------------------------
-- Trigger function for auto-updating updated_at
-------------------------------------------------------
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------------------
-- employee_master
-------------------------------------------------------

CREATE TABLE employee_master (
    employee_id VARCHAR(10) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    designation VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    pay_group_id INT NOT NULL,
    status employee_status DEFAULT 'ACTIVE',
    joining_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger for updated_at
CREATE TRIGGER trg_employee_master_update
BEFORE UPDATE ON employee_master
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- Add index for department
CREATE INDEX idx_employee_master_department ON employee_master(department);

-------------------------------------------------------
-- pay_group
-------------------------------------------------------

CREATE TABLE pay_group (
    pay_group_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_name VARCHAR(50) NOT NULL,
    payment_cycle pay_cycle_type NOT NULL,
    base_tax_rate DECIMAL(5,2) DEFAULT 10.00,
    benefit_rate DECIMAL(5,2) DEFAULT 5.00,
    deduction_rate DECIMAL(5,2) DEFAULT 2.50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-------------------------------------------------------
-- payroll_run
-------------------------------------------------------

CREATE TABLE payroll_run (
    payroll_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    gross_pay DECIMAL(10,2),
    tax_deduction DECIMAL(10,2),
    benefit_addition DECIMAL(10,2),
    net_pay DECIMAL(10,2),
    status payroll_status DEFAULT 'PROCESSED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee_master(employee_id),
    CONSTRAINT uq_payroll_run_emp_period UNIQUE (employee_id, pay_period_start, pay_period_end)
);

-------------------------------------------------------
-- error_log
-------------------------------------------------------

CREATE TABLE error_log (
    error_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    module_name VARCHAR(50),
    employee_id VARCHAR(10),
    error_message VARCHAR(255),
    error_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (employee_id) REFERENCES employee_master(employee_id)
);

-------------------------------------------------------
-- payslip
-------------------------------------------------------

CREATE TABLE payslip (
    payslip_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    payroll_id INT NOT NULL,
    pay_period DATE NOT NULL,
    gross_pay DECIMAL(10,2),
    net_pay DECIMAL(10,2),
    --tax DECIMAL(10,2),
    benefits DECIMAL(10,2),
    earnings_json JSONB,
    deductions_json JSONB,
    file_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee_master(employee_id),
    FOREIGN KEY (payroll_id) REFERENCES payroll_run(payroll_id)
);

-------------------------------------------------------
-- bank_integration_log
-------------------------------------------------------

CREATE TABLE bank_integration_log (
    integration_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payroll_id INT NOT NULL,
    employee_id VARCHAR(10),
    batch_id VARCHAR(20),
    status integration_status DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee_master(employee_id),
    FOREIGN KEY (payroll_id) REFERENCES payroll_run(payroll_id)
);

-- Tracks the overall bulk transmission
CREATE TABLE payroll_batch (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_ref_id VARCHAR(50) NOT NULL UNIQUE,
    pay_period VARCHAR(7) NOT NULL,
    total_amount DECIMAL(15,2),
    status VARCHAR(20) NOT NULL,
    log_message VARCHAR(255),
    employee_count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add index for batch_ref_id
CREATE INDEX idx_payroll_batch_ref_id ON payroll_batch(batch_ref_id);

-- Tracks individual employee status within that batch
CREATE TABLE payroll_batch_log (
    log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_ref_id VARCHAR(50) NOT NULL,
    employee_id VARCHAR(10) NOT NULL,
    status VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_ref_id) REFERENCES payroll_batch(batch_ref_id)
);

-------------------------------------------------------
-- pay_period
-------------------------------------------------------

CREATE TABLE pay_period (
    pay_period_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    pay_group_id INT NOT NULL REFERENCES pay_group(pay_group_id),

    period_start_date DATE NOT NULL,
    period_end_date DATE NOT NULL,

    range VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE pay_period ADD CONSTRAINT uk_pay_period_group_start_end UNIQUE (pay_group_id, period_start_date, period_end_date);

-- Indexes for faster lookup
CREATE INDEX idx_pay_period_group ON pay_period(pay_group_id);
CREATE INDEX idx_pay_period_range ON pay_period(range);

----------------------------------------------------
-- Earning type
----------------------------------------------------

CREATE TABLE earning_type (
    earning_type_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

------------------------------------------------------
-- Payroll earnings
------------------------------------------------------

CREATE TABLE payroll_earnings (
    payroll_earnings_id INT AUTO_INCREMENT PRIMARY KEY,
    payroll_id INT NOT NULL,
    earning_type_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (payroll_id) REFERENCES payroll_run(payroll_id),
    FOREIGN KEY (earning_type_id) REFERENCES earning_type(earning_type_id)
);

--------------------------------------------------------
-- Deduction type
--------------------------------------------------------

CREATE TABLE deduction_type (
    deduction_type_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

-------------------------------------------------------
-- Payroll deductions
-------------------------------------------------------

CREATE TABLE payroll_deductions (
    payroll_deduction_id INT AUTO_INCREMENT PRIMARY KEY,
    payroll_id INT NOT NULL,
    deduction_type_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (payroll_id) REFERENCES payroll_run(payroll_id),
    FOREIGN KEY (deduction_type_id) REFERENCES deduction_type(deduction_type_id)
);

-------------------------------------------------------

ALTER TABLE employee_master
ADD COLUMN base_salary DECIMAL(10,2) NOT NULL


-------------------------------------------------------
-- timesheet_summary
-------------------------------------------------------

CREATE TABLE timesheet_summary (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    employee_id VARCHAR(10) NOT NULL,
    pay_period_id INT NOT NULL,

    no_of_days_worked INT CHECK (no_of_days_worked >= 0),
    hours_worked DECIMAL(6,2) CHECK (hours_worked >= 0),
    holiday_hours DECIMAL(6,2) DEFAULT 0 CHECK (holiday_hours >= 0),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES employee_master(employee_id),
    FOREIGN KEY (pay_period_id) REFERENCES pay_period(pay_period_id),

    UNIQUE (employee_id, pay_period_id)
);

-- Trigger for updated_at auto update
CREATE TRIGGER trg_timesheet_summary_update
BEFORE UPDATE ON timesheet_summary
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- Index for faster lookup
CREATE INDEX idx_timesheet_employee ON timesheet_summary(employee_id);
CREATE INDEX idx_timesheet_period ON timesheet_summary(pay_period_id);

-------------------------------------------------------
-- INSERT DATA
-------------------------------------------------------

INSERT INTO pay_group (group_name, payment_cycle, base_tax_rate, benefit_rate, deduction_rate) VALUES
('Regular Staff', 'MONTHLY', 10.00, 5.00, 2.00),
('Contract Staff', 'WEEKLY', 8.00, 3.00, 1.50),
('Expat Staff', 'MONTHLY', 12.00, 8.00, 3.00);

INSERT INTO employee_master (
    employee_id, first_name, last_name, department, designation, email, pay_group_id, status, joining_date
) VALUES
('E001', 'Jin', 'Park', 'Finance', 'Analyst', 'jin.park@company.com', 1, 'ACTIVE', '2021-02-12'),
('E002', 'Mina', 'Choi', 'HR', 'HR Manager', 'mina.cho@company.com', 1, 'ACTIVE', '2020-08-01'),
('E003', 'Ravi', 'Kumar', 'Engineering', 'Backend Dev', 'ravi.kumar@company.com', 1, 'ACTIVE', '2022-05-15'),
('E004', 'Sujin', 'Lee', 'Engineering', 'Frontend Dev', 'sujin.lee@company.com', 1, 'ACTIVE', '2022-10-05'),
('E005', 'Alex', 'Kim', 'Finance', 'Accountant', 'alex.kim@company.com', 1, 'ACTIVE', '2021-07-20'),
('E006', 'Rohan', 'Sharma', 'Operations', 'Supervisor', 'rohan.sharma@company.com', 2, 'ACTIVE', '2023-01-12'),
('E007', 'Yuna', 'Han', 'Support', 'CSR', 'yuna.han@company.com', 2, 'ACTIVE', '2023-03-09'),
('E008', 'Eunji', 'Kang', 'Engineering', 'QA Engineer', 'eunji.kang@company.com', 1, 'ACTIVE', '2022-06-20'),
('E009', 'Daniel', 'Cho', 'Sales', 'Sales Lead', 'daniel.cho@company.com', 3, 'ACTIVE', '2021-11-15'),
('E010', 'Grace', 'Lim', 'Legal', 'Compliance Officer', 'grace.lim@company.com', 3, 'ACTIVE', '2020-09-30');

INSERT INTO payroll_run (
    employee_id, pay_period_start, pay_period_end, gross_pay, tax_deduction, benefit_addition, net_pay
) VALUES
('E001', '2025-10-01', '2025-10-31', 5000.00, 500.00, 250.00, 4750.00),
('E002', '2025-10-01', '2025-10-31', 7000.00, 700.00, 350.00, 6650.00),
('E003', '2025-10-01', '2025-10-31', 6000.00, 600.00, 300.00, 5700.00),
('E004', '2025-10-01', '2025-10-31', 5800.00, 580.00, 290.00, 5510.00),
('E005', '2025-10-01', '2025-10-31', 6200.00, 620.00, 310.00, 5890.00),
('E006', '2025-10-01', '2025-10-07', 1200.00, 96.00, 36.00, 1140.00),
('E007', '2025-10-01', '2025-10-07', 1000.00, 80.00, 30.00, 950.00),
('E008', '2025-10-01', '2025-10-31', 5500.00, 550.00, 275.00, 5225.00),
('E009', '2025-10-01', '2025-10-31', 8500.00, 1020.00, 680.00, 8160.00),
('E010', '2025-10-01', '2025-10-31', 9000.00, 1080.00, 720.00, 8640.00);

INSERT INTO payslip (
    employee_id, payroll_id, pay_period, gross_pay, net_pay, benefits, earnings_json, deductions_json, file_path
) VALUES
('E001', 1, '2025-10-31', 5000.00, 4750.00, 250.00,'{"grossPay": 5000.00, "benefits": 250.00}','{"tax": 500.00}','/payslips/E001_OCT2025.pdf'),
('E002', 2, '2025-10-31', 7000.00, 6650.00, 350.00,'{"grossPay": 7000.00, "benefits": 350.00}','{"tax": 700.00}','/payslips/E002_OCT2025.pdf'),
('E003', 3, '2025-10-31', 6000.00, 5700.00, 300.00,'{"grossPay": 6000.00, "benefits": 300.00}','{"tax": 600.00}','/payslips/E003_OCT2025.pdf'),
('E004', 4, '2025-10-31', 5800.00, 5510.00, 290.00,'{"grossPay": 5800.00, "benefits": 290.00}','{"tax": 580.00}','/payslips/E004_OCT2025.pdf');

INSERT INTO bank_integration_log (
    payroll_id, employee_id, batch_id, status, retry_count, message
) VALUES
(1, 'E001', 'BATCH202510', 'SUCCESS', 0, 'Processed successfully'),
(2, 'E002', 'BATCH202510', 'SUCCESS', 0, 'Processed successfully'),
(3, 'E003', 'BATCH202510', 'FAILED', 1, 'Network timeout – retry pending'),
(4, 'E004', 'BATCH202510', 'SUCCESS', 0, 'Processed successfully'),
(5, 'E005', 'BATCH202510', 'PENDING', 0, 'Awaiting approval');

INSERT INTO error_log (module_name, employee_id, error_message)
VALUES
('Payroll Calculation', 'E003', 'Negative working hours detected'),
('Payroll Calculation', 'E007', 'Invalid pay group ID reference'),
('Bank Integration', 'E005', 'Bank account verification failed');

INSERT INTO pay_period (pay_group_id, period_start_date, period_end_date, range)
VALUES
(1, '2025-01-01', '2025-01-31', 'JAN-2025'),
(1, '2025-02-01', '2025-02-28', 'FEB-2025'),
(2, '2025-06-01', '2025-06-07', '01-07 JUN25'),
(2, '2025-06-08', '2025-06-14', '08-14 JUN25'),
(2, '2025-06-15', '2025-06-21', '15-21 JUN25'),
(2, '2025-06-22', '2025-06-28', '22-28 JUN25');

INSERT INTO earning_type (name, description) VALUES
('Basic Salary', 'Monthly basic salary component'),
('HRA', 'House Rent Allowance'),
('Bonus', 'Performance-based bonus payment'),
('Shift Allowance', 'Allowance for night shifts worked');

INSERT INTO payroll_earnings (payroll_id, earning_type_id, amount) VALUES
(1, 1, 4000.00),
(1, 2, 800.00),
(1, 3, 200.00),
(2, 1, 5500.00),
(2, 2, 1200.00),
(2, 3, 300.00);

INSERT INTO deduction_type (name, description) VALUES
('Provident Fund', 'Employee provident fund contribution'),
('Professional Tax', 'Monthly professional tax deduction'),
('Income Tax', 'Monthly income tax deduction');

INSERT INTO payroll_deductions (payroll_id, deduction_type_id, amount) VALUES
(1, 1, 250.00),
(1, 2, 100.00),
(1, 3, 150.00),
(2, 1, 350.00),
(2, 2, 100.00),
(2, 3, 200.00);

UPDATE employee_master SET base_salary = 5000 WHERE employee_id = 'E001';
UPDATE employee_master SET base_salary = 7000 WHERE employee_id = 'E002';
UPDATE employee_master SET base_salary = 6000 WHERE employee_id = 'E003';
UPDATE employee_master SET base_salary = 5800 WHERE employee_id = 'E004';
UPDATE employee_master SET base_salary = 6200 WHERE employee_id = 'E005';
UPDATE employee_master SET base_salary = 1200 WHERE employee_id = 'E006';
UPDATE employee_master SET base_salary = 1000 WHERE employee_id = 'E007';
UPDATE employee_master SET base_salary = 5500 WHERE employee_id = 'E008';
UPDATE employee_master SET base_salary = 8500 WHERE employee_id = 'E009';
UPDATE employee_master SET base_salary = 9000 WHERE employee_id = 'E010';


INSERT INTO timesheet_summary
(employee_id, pay_period_id, no_of_days_worked, hours_worked, holiday_hours)
VALUES
('E001', 1, 22, 176.00, 8.00),
('E002', 2, 20, 160.00, 0.00);
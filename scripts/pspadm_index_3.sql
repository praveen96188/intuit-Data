\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

create index concurrently idx_company_src_company_var_ops on pspadm.psp_company USING BTREE (lower(source_company_id) varchar_pattern_ops);
create index concurrently idx_payrollrun_compfk_pyrl_type on psp_payroll_run (company_fk, payroll_run_type);
create index concurrently idx_employee_user_auth_id on pspadm.psp_employee USING BTREE (user_auth_id);

SELECT CURRENT_TIMESTAMP;

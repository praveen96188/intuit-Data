\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

drop index psp_bill_u1 ;
create unique index concurrently psp_bill_u1 on psp_bill (company_usage_fk, bill_date);
drop index psp_company_u1;
create unique index concurrently psp_company_u1 on psp_company (source_company_id, source_system_cd);
drop index psp_company_usage_u1;
create unique index concurrently psp_company_usage_u1 on psp_company_usage (source_company_id, source_system_cd, license_id, entitlement_id);
drop index psp_employee_usage_u1;
create unique index concurrently psp_employee_usage_u1 on psp_employee_usage (usage_period_fk, source_employee_id);
drop index psp_entitlement_u1;
create unique index concurrently psp_entitlement_u1 on psp_entitlement (license_number, entitlement_offering_code);
drop index psp_payroll_run_u1;
create unique index concurrently psp_payroll_run_u1 on psp_payroll_run (company_fk, source_pay_run_id);
drop index psp_system_parameter_spcode_u1;
create unique index concurrently psp_system_parameter_spcode_u1 on psp_system_parameter (system_parameter_cd);
drop index psp_usage_period_u1;
create unique index concurrently psp_usage_period_u1 on psp_usage_period (company_usage_fk, start_date, end_date);

SELECT CURRENT_TIMESTAMP;


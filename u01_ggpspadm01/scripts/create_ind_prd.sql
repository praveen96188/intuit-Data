\timing

set search_path to pspadm;

select current_timestamp;
create index concurrently psp_employee_w2_totals_fk6 on pspadm.psp_employee_w2_totals  USING BTREE (company_law_fk);
create index concurrently psp_ledger_operation_i1 on pspadm.psp_ledger_operation  USING BTREE (source_company_id);
select current_timestamp;


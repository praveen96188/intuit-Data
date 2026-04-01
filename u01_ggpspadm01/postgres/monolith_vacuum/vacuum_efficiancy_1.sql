\timing

vacuum (analyze, verbose) pspadm.psp_employee_law_qtr_totals;
vacuum (analyze, verbose) pspadm.psp_batch_job_audit_log   ;
vacuum (analyze, verbose) pspadm.psp_pstub_address;
vacuum (analyze, verbose) pspadm.psp_pstub_msg    ;
SELECT pg_sleep(60);
vacuum (analyze, verbose) pspadm.psp_gems_monthly_balance  ;
vacuum (analyze, verbose) pspadm.psp_bill  ;
vacuum (analyze, verbose) pspadm.psp_ledger_operation      ;
vacuum (analyze, verbose) pspadm.psp_ledger_operation_job  ;
SELECT pg_sleep(60);
vacuum (analyze, verbose) pspadm.psp_sql_execution_log_entry;
vacuum (analyze, verbose) pspadm.psp_nachafile    ;
vacuum (analyze, verbose) pspadm.psp_employee_bank_account ;
vacuum (analyze, verbose) pg_depend ;
SELECT pg_sleep(60);
vacuum (analyze, verbose) pspadm.psp_company_event_p4      ;
vacuum (analyze, verbose) pspadm.psp_companyagency_frmtemplate;
vacuum (analyze, verbose) pspadm.psp_on_hold_reason ;
vacuum (analyze, verbose) pspadm.psp_gems_upload_batch     ;
vacuum (analyze, verbose) pspadm.psp_company_event_email   ;
vacuum (analyze, verbose) pspadm.psp_employee_usage ;
SELECT pg_sleep(60);
vacuum (analyze, verbose) pspadm.psp_asst_bundle_bill_detail;
vacuum (analyze, verbose) pspadm.psp_auth_user    ;
vacuum (analyze, verbose) pspadm.psp_agency_id_requirement ;
vacuum (analyze, verbose) pspadm.psp_pstub_employee_info_p0;


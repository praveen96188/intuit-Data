--cleanup
drop type if exists pspadm.mmt_record;
drop type if exists pspadm.ft_record;
drop type if exists pspadm.fts_record;
drop FUNCTION if exists pspadm.fn_date_add;
drop FUNCTION if exists pspadm.fn_gems_accounts_receivable_calc_reveivables;
drop FUNCTION if exists pspadm.fn_gems_accounts_receivable_main;
drop FUNCTION if exists pspadm.fn_get_edr_count;
drop FUNCTION if exists pspadm.fn_get_env;
drop FUNCTION if exists pspadm.fn_get_last_day_of_quarter;
drop FUNCTION if exists pspadm.fn_get_ledger_balance;
drop FUNCTION if exists pspadm.fn_get_mmt_count;
drop FUNCTION if exists pspadm.fn_get_psid;
drop FUNCTION if exists pspadm.fn_get_psp_timestamp;
drop PROCEDURE if exists pspadm.prc_achtransactionprocessor;
drop PROCEDURE if exists pspadm.prc_calculate_ledger_balance;
drop PROCEDURE if exists pspadm.prc_calculate_w2_totals;
drop PROCEDURE if exists pspadm.prc_cost_co_plsql_jobs_processor;
drop PROCEDURE if exists pspadm.prc_eftps_payments_events;
drop PROCEDURE if exists pspadm.prc_eftps_payments_mmt_status;
drop PROCEDURE if exists pspadm.prc_eftps_payments_response;
drop PROCEDURE if exists pspadm.prc_eftps_payments_return;
drop PROCEDURE if exists pspadm.prc_eftps_payments_sent;
drop PROCEDURE if exists pspadm.prc_eftps_payments_sent_events;
drop PROCEDURE if exists pspadm.prc_gems_accounts_receivable_assoc_fin_txn_states;
drop PROCEDURE if exists pspadm.prc_gems_accounts_receivable_create_upload_batch;
drop PROCEDURE if exists pspadm.prc_gems_accounts_receivable_supercede_upload_batch;
drop PROCEDURE if exists pspadm.prc_offload;
drop PROCEDURE if exists pspadm.prc_offload_insert_fts;
drop PROCEDURE if exists pspadm.prc_offload_upd_agency_status;
drop PROCEDURE if exists pspadm.prc_offload_update_ft;
drop PROCEDURE if exists pspadm.prc_offload_update_mmt;
drop PROCEDURE if exists pspadm.prc_offload_update_payroll;
drop PROCEDURE if exists pspadm.prc_payroll_fraudbatch_purge_dbupgrade_plsql_jobs_processor;
drop PROCEDURE if exists pspadm.prc_payroll_item_totals_comp_qtr_payroll_item_tot;
drop PROCEDURE if exists pspadm.prc_payroll_item_totals_qtr_payroll_item_tot;
drop PROCEDURE if exists pspadm.prc_payroll_item_totals_year_payroll_item_tot;
drop PROCEDURE if exists pspadm.prc_recalc_atf_payments_eftps;
drop PROCEDURE if exists pspadm.prc_recalculate_atf_payments;
drop PROCEDURE if exists pspadm.prc_remove_company_fast;
drop PROCEDURE if exists pspadm.prc_set_psp_event_log;
drop PROCEDURE if exists pspadm.prc_upd_company_ledger_balance;
drop PROCEDURE if exists pspadm.prc_update_ledger_balance;
drop PROCEDURE if exists pspadm.prc_update_nacha_file_trace_number;

--Create Types and procedures order:
\i 'Type/record_type.sql'
\i 'Procedure/prc_set_psp_event_log.sql'
\i 'Procedure/prc_achtransactionprocessor.sql'
\i 'Procedure/prc_calculate_ledger_balance.sql'
\i 'Procedure/prc_calculate_w2_totals.sql'
\i 'Procedure/prc_cost_co_plsql_jobs_processor.sql'
\i 'Procedure/prc_eftps_payments_events.sql'
\i 'Procedure/prc_eftps_payments_mmt_status.sql'
\i 'Procedure/prc_eftps_payments_response.sql'
\i 'Procedure/prc_eftps_payments_return.sql'
\i 'Procedure/prc_eftps_payments_sent_events.sql'
\i 'Procedure/prc_gems_accounts_receivable_assoc_fin_txn_states.sql'
\i 'Procedure/prc_gems_accounts_receivable_create_upload_batch.sql'
\i 'Procedure/prc_gems_accounts_receivable_supercede_upload_batch.sql'
\i 'Procedure/prc_eftps_payments_sent.sql'
\i 'Procedure/prc_offload_insert_fts.sql'
\i 'Procedure/prc_offload_upd_agency_status.sql'
\i 'Procedure/prc_offload_update_ft.sql'
\i 'Procedure/prc_offload_update_mmt.sql'
\i 'Procedure/prc_offload_update_payroll.sql'
\i 'Procedure/prc_offload.sql'
\i 'Procedure/prc_payroll_fraudBatch_purge_dbUpgrade_plsql_jobs_processor.sql'
\i 'Procedure/prc_payroll_item_totals_comp_qtr_payroll_item_tot.sql'
\i 'Procedure/prc_payroll_item_totals_qtr_payroll_item_tot.sql'
\i 'Procedure/prc_payroll_item_totals_year_payroll_item_tot.sql'
\i 'Procedure/prc_recalc_atf_payments_eftps.sql'
\i 'Procedure/prc_recalculate_atf_payments.sql'
\i 'Procedure/prc_remove_company_fast.sql'
\i 'Procedure/prc_upd_company_ledger_balance.sql'
\i 'Procedure/prc_update_ledger_balance.sql'
\i 'Procedure/prc_update_nacha_file_trace_number.sql'

--Create Function Order:
\i 'Function/fn_get_env.sql'
\i 'Function/fn_get_psp_timestamp.sql'
\i 'Function/fn_get_last_day_of_quarter.sql'
\i 'Function/fn_get_edr_count.sql'
\i 'Function/fn_get_ledger_balance.sql'
\i 'Function/fn_get_psid.sql'
\i 'Function/fn_date_add.sql'
\i 'Function/fn_get_mmt_count.sql'
\i 'Function/fn_gems_accounts_receivable_calc_reveivables.sql'
\i 'Function/fn_gems_accounts_receivable_main.sql'


Except this we need to validate:
1. psp_entry_detail_record_staging table is present in all the DBs
2. pspapp user should have rights/grants for CRUD operation
3. Synonym for pspapp
4. All user should able to get select grant on psp_entry_detail_record_staging table


select
    routine_catalog AS DatabaseName
     ,routine_schema AS SchemaName
     ,routine_name AS FunctionName
     ,routine_type AS ObjectType
from information_schema.routines
where routine_schema = 'pspadm' order by ObjectType;


drop type pspadm.mmt_record;
drop type pspadm.ft_record;
drop type pspadm.fts_record;
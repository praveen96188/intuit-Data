set search_path to pspadm;

\i Procedure/prc_achtransactionprocessor.sql
\i Procedure/prc_calculate_ledger_balance.sql
\i Procedure/prc_calculate_w2_totals.sql
\i Procedure/prc_cost_co_plsql_jobs_processor.sql
\i Procedure/prc_eftps_payments_events.sql
\i Procedure/prc_eftps_payments_mmt_status.sql
\i Procedure/prc_eftps_payments_response.sql
\i Procedure/prc_eftps_payments_return.sql
\i Procedure/prc_eftps_payments_sent.sql
\i Procedure/prc_eftps_payments_sent_events.sql
\i Procedure/prc_gems_accounts_receivable_assoc_fin_txn_states.sql
\i Procedure/prc_gems_accounts_receivable_create_upload_batch.sql
\i Procedure/prc_gems_accounts_receivable_supercede_upload_batch.sql
\i Procedure/prc_offload.sql
\i Procedure/prc_offload_insert_fts.sql
\i Procedure/prc_offload_upd_agency_status.sql
\i Procedure/prc_offload_update_ft.sql
\i Procedure/prc_offload_update_mmt.sql
\i Procedure/prc_offload_update_payroll.sql
\i Procedure/prc_payroll_fraudBatch_purge_dbUpgrade_plsql_jobs_processor.sql
\i Procedure/prc_payroll_item_totals_comp_qtr_payroll_item_tot.sql
\i Procedure/prc_payroll_item_totals_qtr_payroll_item_tot.sql
\i Procedure/prc_payroll_item_totals_year_payroll_item_tot.sql
\i Procedure/prc_recalc_atf_payments_eftps.sql
\i Procedure/prc_recalculate_atf_payments.sql
\i Procedure/prc_remove_company_fast.sql
\i Procedure/prc_set_psp_event_log.sql
\i Procedure/prc_upd_company_ledger_balance.sql
\i Procedure/prc_update_ledger_balance.sql
\i Procedure/prc_update_nacha_file_trace_number.sql

set search_path to pspadm;

\i Function/fn_date_add.sql
\i Function/fn_gems_accounts_receivable_calc_reveivables.sql
\i Function/fn_gems_accounts_receivable_main.sql
\i Function/fn_get_edr_count.sql
\i Function/fn_get_env.sql
\i Function/fn_get_last_day_of_quarter.sql
\i Function/fn_get_ledger_balance.sql
\i Function/fn_get_mmt_count.sql
\i Function/fn_get_psid.sql
\i Function/fn_get_psp_timestamp.sql

set search_path to pspadm;

\i Trigger/tr_generated_audit_triggers.sql
\i Trigger/tr_upd_company_event_timestamp.sql
\i Trigger/tr_upd_dd_limits.sql

set search_path to pspadm;

\i Sequence/tp_401k_signup_batch_id_sequence.sql
\i Sequence/subscription_number_sequence.sql
\i Sequence/psid_sequences_by_state.sql
\i Sequence/mmt_transaction_number_sequence.sql
\i Sequence/eftps_payment_sequence.sql
\i Sequence/eftps_file_sequence.sql
\i Sequence/ee_calculation_token_sequence.sql
\i Sequence/usage_billing_token_sequence.sql
\i Sequence/trace_number_sequence.sql
\i Sequence/trace_nbr_sequence.sql
\i Sequence/tp_401k_upload_batch_id_sequence.sql
\i Sequence/token_number_sequence.sql
\i Sequence/qbdt_source_comp_id_sequence.sql
\i Sequence/nacha_file_unique_id_sequence.sql
\i Sequence/gems_upload_batch_id_sequence.sql
\i Sequence/eftps_segment_sequence.sql
\i Sequence/atf_batch_id_sequence.sql
\i Sequence/assisted_usage_billing_token_sequence.sql

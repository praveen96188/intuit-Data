 --psp_pstub_employee_info_veridata_view
 ---psp_compensation_veridata_view
 --psp_ledger_balance_veridata_view
 --psp_money_movement_transaction_veridata_view
 --psp_company_event_veridata_view
 --psp_compensation_view
 --psp_financial_transaction_view
 --psp_company_event_detail_veridata_view
 --psp_paycheck_usage_view
 --psp_company_event_email_param_veridata_view
 psp_financial_transaction_veridata_view
 --psp_paycheck_veridata_view
 --psp_paycheck_split_veridata_view
 --psp_property_audit_veridata_view
 --psp_qbdt_paycheck_info_veridata_view
 --psp_qbdt_payline_info_veridata_view
 --psp_entry_detail_record_veridata_view
 --psp_financial_trans_state_veridata_view
 --psp_pstub_pay_item_veridata_view
 --psp_tax_veridata_view
 --psp_disburse_advice_tax_liab_veridata_view
 --psp_qbdt_transaction_info_veridata_view
 --psp_paystub_veridata_view
 --psp_deduction_veridata_view
 --psp_pstub_paid_timeoff_item_veridata_view

select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_pstub_employee_info_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_compensation_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_ledger_balance_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_money_movement_transaction_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_company_event_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_company_event_detail_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_company_event_email_param_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_financial_transaction_p%';


select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_paycheck_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_paycheck_split_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_property_audit_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_qbdt_paycheck_info_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_qbdt_payline_info_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_entry_detail_record_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_financial_trans_state_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_pstub_pay_item_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_tax_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_disburse_advice_tax_liab_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_qbdt_transaction_info_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_paystub_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_deduction_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_pstub_paid_timeoff_item_p%';



select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_entity_update_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_entitlement_message_p%';
select  'select * from pspadm.'||tablename||' UNION ALL' from pg_tables where tablename like 'psp_paycheck_usage_%';
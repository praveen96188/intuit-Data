set echo on feedback on
spool TEMP_CR_GL_INDEXES_FOR_VAL_PART1
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
exec dbms_streams.set_tag (hextoraw(1));
create index pspadm.idx_psp_compensation_global on pspadm.psp_compensation (compensation_seq) online parallel (degree 16) tablespace psp_idx01;
create index pspadm.idx_psp_ledger_balance_global on pspadm.psp_ledger_balance (ledger_balance_seq) online parallel (degree 16) tablespace psp_idx04;
create index pspadm.idx_psp_money_movement_transaction_global on pspadm.psp_money_movement_transaction (money_movement_transaction_seq) online parallel (degree 16) tablespace psp_idx01;
create index pspadm.idx_psp_company_event_global on pspadm.psp_company_event (company_event_seq) online parallel (degree 16) tablespace psp_idx02;
create index pspadm.idx_psp_company_event_detail_global on pspadm.psp_company_event_detail (company_event_detail_seq) online parallel (degree 16) tablespace psp_idx03;
create index pspadm.idx_psp_company_event_email_param_global on pspadm.psp_company_event_email_param (company_event_email_param_seq) online parallel (degree 16) tablespace psp_idx04;
create index pspadm.idx_psp_financial_transaction_global on pspadm.psp_financial_transaction (financial_transaction_seq) online parallel (degree 16) tablespace psp_idx01;
create index pspadm.idx_psp_paycheck_global on pspadm.psp_paycheck (paycheck_seq) online parallel (degree 16) tablespace psp_idx02;
create index pspadm.idx_psp_paycheck_split_global on pspadm.psp_paycheck_split (paycheck_split_seq) online parallel (degree 16) tablespace psp_idx03;
create index pspadm.idx_psp_property_audit_global on pspadm.psp_property_audit (property_audit_seq) online parallel (degree 16) tablespace psp_idx04;
create index pspadm.idx_psp_qbdt_paycheck_info_global on pspadm.psp_qbdt_paycheck_info (qbdt_paycheck_info_seq) online parallel (degree 16) tablespace psp_idx01;
create index pspadm.idx_psp_qbdt_payline_info_global on pspadm.psp_qbdt_payline_info (qbdt_payline_info_seq) online parallel (degree 16) tablespace psp_idx03;
--create index pspadm.idx_psp_entry_detail_record_global on pspadm.psp_entry_detail_record (entry_detail_record_seq) online parallel (degree 16) tablespace psp_idx02;
--create index pspadm.idx_psp_financial_trans_state_global on pspadm.psp_financial_trans_state (financial_trans_state_seq) online parallel (degree 16) tablespace psp_idx03;
--create index pspadm.idx_psp_pstub_pay_item_global on pspadm.psp_pstub_pay_item (pstub_pay_item_seq) online parallel (degree 16) tablespace psp_idx01;
--create index pspadm.idx_psp_tax_global on pspadm.psp_tax (tax_seq) online parallel (degree 16) tablespace psp_idx04;
--create index pspadm.idx_psp_disburse_advice_tax_liab_global on pspadm.psp_disburse_advice_tax_liab (disburse_advice_tax_liab_seq) online parallel (degree 16) tablespace psp_idx01;
--create index pspadm.idx_psp_qbdt_transaction_info_global on pspadm.psp_qbdt_transaction_info (qbdt_transaction_info_seq) online parallel (degree 16) tablespace psp_idx02;
--create index pspadm.idx_psp_paystub_global on pspadm.psp_paystub (paystub_seq) online parallel (degree 16) tablespace psp_idx03;
--create index pspadm.idx_psp_deduction_global on pspadm.psp_deduction (deduction_seq) online parallel (degree 16) tablespace psp_idx04;
--create index pspadm.idx_psp_pstub_paid_timeoff_item_global on pspadm.psp_pstub_paid_timeoff_item (pstub_paid_timeoff_item_seq) online parallel (degree 16) tablespace psp_idx01;
--create index pspadm.idx_psp_pstub_employee_info_global on pspadm.psp_pstub_employee_info (pstub_employee_info_seq) online parallel (degree 16) tablespace psp_idx02;

alter index pspadm.idx_psp_compensation_global noparallel;
alter index pspadm.idx_psp_ledger_balance_global noparallel;
alter index pspadm.idx_psp_money_movement_transaction_global noparallel;
alter index pspadm.idx_psp_company_event_global noparallel;
alter index pspadm.idx_psp_company_event_detail_global noparallel;
alter index pspadm.idx_psp_company_event_email_param_global noparallel;
alter index pspadm.idx_psp_financial_transaction_global noparallel;
alter index pspadm.idx_psp_paycheck_global noparallel;
alter index pspadm.idx_psp_paycheck_split_global noparallel;
alter index pspadm.idx_psp_property_audit_global noparallel;
alter index pspadm.idx_psp_qbdt_paycheck_info_global noparallel;
alter index pspadm.idx_psp_qbdt_payline_info_global noparallel;
--alter index pspadm.idx_psp_entry_detail_record_global noparallel;
--alter index pspadm.idx_psp_financial_trans_state_global noparallel;
--alter index pspadm.idx_psp_pstub_pay_item_global noparallel;
--alter index pspadm.idx_psp_tax_global noparallel;
--alter index pspadm.idx_psp_disburse_advice_tax_liab_global noparallel;
--alter index pspadm.idx_psp_qbdt_transaction_info_global noparallel;
--alter index pspadm.idx_psp_paystub_global noparallel;
--alter index pspadm.idx_psp_deduction_global noparallel;
--alter index pspadm.idx_psp_pstub_paid_timeoff_item_global noparallel;
--alter index pspadm.idx_psp_pstub_employee_info_global noparallel;
--exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');

spool off

set echo on feedback on timing on
spool CR_UNIQ_IND_PART_TABLES_1

create unique index pspadm.ft_seq_u on pspadm.psp_financial_transaction (financial_transaction_seq) tablespace PSP_IDX03 ;
create unique index pspadm.pcom_seq_u on pspadm.psp_compensation (compensation_seq) tablespace PSP_IDX01;
create unique index pspadm.lb_seq_u on pspadm.psp_ledger_balance (ledger_balance_seq) tablespace PSP_IDX04;
create unique index pspadm.mmt_seq_u on pspadm.psp_money_movement_transaction (money_movement_transaction_seq) tablespace PSP_IDX01;
create unique index pspadm.ce_seq_u on pspadm.psp_company_event (company_event_seq) tablespace PSP_IDX02;
spool off


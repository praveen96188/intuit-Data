set echo on feedback on timing on
spool CR_UNIQ_IND_PART_TABLES_4
create unique index pspadm.pt_seq_u on pspadm.psp_tax (tax_seq) tablespace PSP_IDX04;
create unique index pspadm.datl_seq_u on pspadm.psp_disburse_advice_tax_liab (disburse_advice_tax_liab_seq) tablespace PSP_IDX01;
create unique index pspadm.qti_seq_u on pspadm.psp_qbdt_transaction_info (qbdt_transaction_info_seq) tablespace PSP_IDX02;
create unique index pspadm.ps_seq_u on pspadm.psp_paystub (paystub_seq) tablespace PSP_IDX03;
create unique index pspadm.pd_seq_u on pspadm.psp_deduction (deduction_seq) tablespace PSP_IDX04;
create unique index pspadm.ppti_seq_u on pspadm.psp_pstub_paid_timeoff_item (pstub_paid_timeoff_item_seq) tablespace PSP_IDX01;
create unique index pspadm.pei_seq_u on pspadm.psp_pstub_employee_info (pstub_employee_info_seq) tablespace PSP_IDX02;
spool off

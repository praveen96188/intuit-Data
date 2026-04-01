set echo on feedback on timing on
spool CR_UNIQ_IND_PART_TABLES_3
create unique index pspadm.qpci_seq_u on pspadm.psp_qbdt_paycheck_info (qbdt_paycheck_info_seq) tablespace PSP_IDX01;
create unique index pspadm.qpli_seq_u on pspadm.psp_qbdt_payline_info (qbdt_payline_info_seq) tablespace PSP_IDX03;
create unique index pspadm.edr_seq_u on pspadm.psp_entry_detail_record (entry_detail_record_seq) tablespace PSP_IDX02;
create unique index pspadm.fts_seq_u on pspadm.psp_financial_trans_state (financial_trans_state_seq) tablespace PSP_IDX03;
create unique index pspadm.ppi_seq_u on pspadm.psp_pstub_pay_item (pstub_pay_item_seq) tablespace PSP_IDX01;
spool off

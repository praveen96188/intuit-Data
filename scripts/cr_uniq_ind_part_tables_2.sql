set echo on feedback on timing on
spool CR_UNIQ_IND_PART_TABLES_2
create unique index pspadm.ced_seq_u on pspadm.psp_company_event_detail (company_event_detail_seq) tablespace PSP_IDX03;
create unique index pspadm.ceep_seq_u on pspadm.psp_company_event_email_param (company_event_email_param_seq) tablespace PSP_IDX04;
create unique index pspadm.pc_seq_u on pspadm.psp_paycheck (paycheck_seq) tablespace PSP_IDX02;
create unique index pspadm.pcs_seq_u on pspadm.psp_paycheck_split (paycheck_split_seq) tablespace PSP_IDX03;
create unique index pspadm.pa_seq_u on pspadm.psp_property_audit (property_audit_seq) tablespace PSP_IDX04;
spool off

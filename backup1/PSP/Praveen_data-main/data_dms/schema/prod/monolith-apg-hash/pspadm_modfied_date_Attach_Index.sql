\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


--Partiton tables
create index idx_ce_mod_date ON ONLY  pspadm.psp_company_event USING BTREE (modified_date);
create index idx_ced_mod_date ON ONLY  pspadm.psp_company_event_detail USING BTREE (modified_date);
create index idx_ceep_mod_date ON ONLY  pspadm.psp_company_event_email_param USING BTREE (modified_date);
create index idx_compensation_mod_date ON ONLY  pspadm.psp_compensation USING BTREE (modified_date);
create index idx_deduction_mod_date ON ONLY  pspadm.psp_deduction USING BTREE (modified_date);
create index idx_datl_mod_date ON ONLY  pspadm.psp_disburse_advice_tax_liab USING BTREE (modified_date);
create index idx_ent_msg_mod_date ON ONLY  pspadm.psp_entitlement_message USING BTREE (modified_date);
create index idx_ent_upd_mod_date ON ONLY  pspadm.psp_entity_update USING BTREE (modified_date);
create index idx_edr_mod_date ON ONLY  pspadm.psp_entry_detail_record USING BTREE (modified_date);
create index idx_ft_mod_date ON ONLY  pspadm.psp_financial_transaction USING BTREE (modified_date);
create index idx_fts_mod_date ON ONLY  pspadm.psp_financial_trans_state USING BTREE (modified_date);
create index idx_lb_mod_date ON ONLY  pspadm.psp_ledger_balance USING BTREE (modified_date);
create index idx_mmt_mod_date ON ONLY  pspadm.psp_money_movement_transaction USING BTREE (modified_date);
create index idx_paycheck_mod_date ON ONLY  pspadm.psp_paycheck USING BTREE (modified_date);
create index idx_paycheck_split_mod_date ON ONLY  pspadm.psp_paycheck_split USING BTREE (modified_date);
create index idx_pchk_usg_mod_date ON ONLY  pspadm.psp_paycheck_usage USING BTREE (modified_date);
create index idx_paystub_mod_date ON ONLY  pspadm.psp_paystub USING BTREE (modified_date);
create index idx_prop_aud_mod_date ON ONLY  pspadm.psp_property_audit USING BTREE (modified_date);
create index idx_pei_mod_date ON ONLY  pspadm.psp_pstub_employee_info USING BTREE (modified_date);
create index idx_ppti_mod_date ON ONLY  pspadm.psp_pstub_paid_timeoff_item USING BTREE (modified_date);
create index idx_pstub_pay_item_mod_date ON ONLY  pspadm.psp_pstub_pay_item USING BTREE (modified_date);
create index idx_qpi_mod_date ON ONLY  pspadm.psp_qbdt_paycheck_info USING BTREE (modified_date);
create index idx_qpli_mod_date ON ONLY  pspadm.psp_qbdt_payline_info USING BTREE (modified_date);
create index idx_qti_mod_date ON ONLY  pspadm.psp_qbdt_transaction_info USING BTREE (modified_date);
create index idx_tax_mod_date ON ONLY  pspadm.psp_tax  USING BTREE (modified_date);


--Hash Part
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p0 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p1 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p2 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p3 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p4 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p5 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p6 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p7 ;


alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p0  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p1  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p2  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p3  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p4  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p5  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p6  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p7  ;


alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p0  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p1  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p2  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p3  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p4  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p5  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p6  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p7  ;


alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p0 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p1 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p2 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p3 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p4 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p5 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p6 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p7 ;

alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p0 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p1 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p2 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p3 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p4 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p5 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p6 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p7 ;


alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p0 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p1 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p2 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p3 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p4 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p5 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p6 ;
alter index idx_ceep_mod_date attach partition  pspadm.idx_ceep_mod_date_p7 ;


alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p0 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p1 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p2 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p3 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p4 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p5 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p6 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p7 ;

alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p0 ;
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p1 ;
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p2 ;
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p3 ;

alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p0;
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p1;
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p2;
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p3;


alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p0 ;
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p1 ;
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p2 ;
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p3 ;

alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p0 ;
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p1 ;
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p2 ;
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p3 ;

alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p0 ;
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p1 ;
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p2 ;
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p3 ;


alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p0 ;
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p1 ;
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p2 ;
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p3 ;

alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p0 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p1 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p10 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p11 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p12 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p13 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p14 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p15 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p2 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p3 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p4 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p5 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p6 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p7 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p8 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p9 ;

alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p0 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p1 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p10 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p11 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p12 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p13 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p14 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p15 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p2 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p3 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p4 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p5 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p6 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p7 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p8 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p9 ;


alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p0 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p1 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p10 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p11 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p12 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p13 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p14 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p15 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p2 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p3 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p4 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p5 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p6 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p7 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p8 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p9 ;


alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p0 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p1 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p10 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p11 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p12 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p13 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p14 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p15 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p2 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p3 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p4 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p5 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p6 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p7 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p8 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p9 ;


alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p0 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p1 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p10 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p11 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p12 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p13 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p14 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p15 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p2 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p3 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p4 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p5 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p6 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p7 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p8 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p9 ;

alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p0 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p1 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p10 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p11 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p12 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p13 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p14 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p15 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p2 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p3 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p4 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p5 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p6 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p7 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p8 ;
alter index idx_qpli_mod_date attach partition  pspadm.idx_qpli_mod_date_p9 ;


alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p0 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p1 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p10 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p11 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p12 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p13 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p14 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p15 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p2 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p3 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p4 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p5 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p6 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p7 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p8 ;
alter index idx_ft_mod_date attach partition  pspadm.idx_ft_mod_date_p9 ;

alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p0 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p1 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p10 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p11 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p12 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p13 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p14 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p15 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p2 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p3 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p4 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p5 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p6 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p7 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p8 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p9 ;


alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p0 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p1 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p10 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p11 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p12 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p13 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p14 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p15 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p2 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p3 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p4 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p5 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p6 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p7 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p8 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p9 ;


--Range Part

alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2012 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2013 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2014 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2015 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2016 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2017 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2018 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2019 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2020 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2021 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2022 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2023 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2024 ;

alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2012 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2013 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2014 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2015 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2016 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2017 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2018 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2019 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2020 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2021 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2022 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2023 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2024 ;


alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112022;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122022;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122024;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122025;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112026;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122026;


SELECT CURRENT_TIMESTAMP;


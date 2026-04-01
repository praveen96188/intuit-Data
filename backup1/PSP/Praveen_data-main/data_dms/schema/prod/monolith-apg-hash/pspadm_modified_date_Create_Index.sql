\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;




--Hash Partition tables
create index concurrently idx_compensation_mod_date_p0 on pspadm.psp_compensation_p0 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p1 on pspadm.psp_compensation_p1 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p2 on pspadm.psp_compensation_p2 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p3 on pspadm.psp_compensation_p3 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p4 on pspadm.psp_compensation_p4 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p5 on pspadm.psp_compensation_p5 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p6 on pspadm.psp_compensation_p6 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p7 on pspadm.psp_compensation_p7 USING BTREE  (modified_date);



create index concurrently idx_lb_mod_date_p0 on pspadm.psp_ledger_balance_p0 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p1 on pspadm.psp_ledger_balance_p1 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p2 on pspadm.psp_ledger_balance_p2 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p3 on pspadm.psp_ledger_balance_p3 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p4 on pspadm.psp_ledger_balance_p4 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p5 on pspadm.psp_ledger_balance_p5 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p6 on pspadm.psp_ledger_balance_p6 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p7 on pspadm.psp_ledger_balance_p7 USING BTREE  (modified_date) ;


create index concurrently idx_mmt_mod_date_p0 on pspadm.psp_money_movement_transaction_p0 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p1 on pspadm.psp_money_movement_transaction_p1 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p2 on pspadm.psp_money_movement_transaction_p2 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p3 on pspadm.psp_money_movement_transaction_p3 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p4 on pspadm.psp_money_movement_transaction_p4 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p5 on pspadm.psp_money_movement_transaction_p5 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p6 on pspadm.psp_money_movement_transaction_p6 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p7 on pspadm.psp_money_movement_transaction_p7 USING BTREE  (modified_date) ;


create index concurrently idx_ce_mod_date_p0 on pspadm.psp_company_event_p0 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p1 on pspadm.psp_company_event_p1 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p2 on pspadm.psp_company_event_p2 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p3 on pspadm.psp_company_event_p3 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p4 on pspadm.psp_company_event_p4 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p5 on pspadm.psp_company_event_p5 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p6 on pspadm.psp_company_event_p6 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p7 on pspadm.psp_company_event_p7 USING BTREE  (modified_date);

create index concurrently idx_ced_mod_date_p0 on pspadm.psp_company_event_detail_p0 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p1 on pspadm.psp_company_event_detail_p1 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p2 on pspadm.psp_company_event_detail_p2 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p3 on pspadm.psp_company_event_detail_p3 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p4 on pspadm.psp_company_event_detail_p4 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p5 on pspadm.psp_company_event_detail_p5 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p6 on pspadm.psp_company_event_detail_p6 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p7 on pspadm.psp_company_event_detail_p7 USING BTREE  (modified_date );


create index concurrently idx_ceep_mod_date_p0 on pspadm.psp_company_event_email_param_p0 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p1 on pspadm.psp_company_event_email_param_p1 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p2 on pspadm.psp_company_event_email_param_p2 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p3 on pspadm.psp_company_event_email_param_p3 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p4 on pspadm.psp_company_event_email_param_p4 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p5 on pspadm.psp_company_event_email_param_p5 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p6 on pspadm.psp_company_event_email_param_p6 USING BTREE  (modified_date);
create index concurrently idx_ceep_mod_date_p7 on pspadm.psp_company_event_email_param_p7 USING BTREE  (modified_date);


create index concurrently idx_prop_aud_mod_date_p0 on pspadm.psp_property_audit_p0 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p1 on pspadm.psp_property_audit_p1 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p2 on pspadm.psp_property_audit_p2 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p3 on pspadm.psp_property_audit_p3 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p4 on pspadm.psp_property_audit_p4 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p5 on pspadm.psp_property_audit_p5 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p6 on pspadm.psp_property_audit_p6 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p7 on pspadm.psp_property_audit_p7 USING BTREE  (modified_date);

create index concurrently idx_datl_mod_date_p0 on pspadm.psp_disburse_advice_tax_liab_p0 USING BTREE  (modified_date);
create index concurrently idx_datl_mod_date_p1 on pspadm.psp_disburse_advice_tax_liab_p1 USING BTREE  (modified_date);
create index concurrently idx_datl_mod_date_p2 on pspadm.psp_disburse_advice_tax_liab_p2 USING BTREE  (modified_date);
create index concurrently idx_datl_mod_date_p3 on pspadm.psp_disburse_advice_tax_liab_p3 USING BTREE  (modified_date);

create index concurrently idx_qti_mod_date_p0 on pspadm.psp_qbdt_transaction_info_p0 USING BTREE  (modified_date);
create index concurrently idx_qti_mod_date_p1 on pspadm.psp_qbdt_transaction_info_p1 USING BTREE  (modified_date);
create index concurrently idx_qti_mod_date_p2 on pspadm.psp_qbdt_transaction_info_p2 USING BTREE  (modified_date);
create index concurrently idx_qti_mod_date_p3 on pspadm.psp_qbdt_transaction_info_p3 USING BTREE  (modified_date);

create index concurrently idx_paystub_mod_date_p0 on pspadm.psp_paystub_p0 USING BTREE  (modified_date);
create index concurrently idx_paystub_mod_date_p1 on pspadm.psp_paystub_p1 USING BTREE  (modified_date);
create index concurrently idx_paystub_mod_date_p2 on pspadm.psp_paystub_p2 USING BTREE  (modified_date);
create index concurrently idx_paystub_mod_date_p3 on pspadm.psp_paystub_p3 USING BTREE  (modified_date);

create index concurrently idx_deduction_mod_date_p0 on pspadm.psp_deduction_p0 USING BTREE  (modified_date);
create index concurrently idx_deduction_mod_date_p1 on pspadm.psp_deduction_p1 USING BTREE  (modified_date);
create index concurrently idx_deduction_mod_date_p2 on pspadm.psp_deduction_p2 USING BTREE  (modified_date);
create index concurrently idx_deduction_mod_date_p3 on pspadm.psp_deduction_p3 USING BTREE  (modified_date);






create index concurrently idx_ppti_mod_date_p0 on pspadm.psp_pstub_paid_timeoff_item_p0 USING BTREE  (modified_date);
create index concurrently idx_ppti_mod_date_p1 on pspadm.psp_pstub_paid_timeoff_item_p1 USING BTREE  (modified_date);
create index concurrently idx_ppti_mod_date_p2 on pspadm.psp_pstub_paid_timeoff_item_p2 USING BTREE  (modified_date);
create index concurrently idx_ppti_mod_date_p3 on pspadm.psp_pstub_paid_timeoff_item_p3 USING BTREE  (modified_date);


create index concurrently idx_pei_mod_date_p0 on pspadm.psp_pstub_employee_info_p0 USING BTREE  (modified_date);
create index concurrently idx_pei_mod_date_p1 on pspadm.psp_pstub_employee_info_p1 USING BTREE  (modified_date);
create index concurrently idx_pei_mod_date_p2 on pspadm.psp_pstub_employee_info_p2 USING BTREE  (modified_date);
create index concurrently idx_pei_mod_date_p3 on pspadm.psp_pstub_employee_info_p3 USING BTREE  (modified_date);

create index concurrently idx_edr_mod_date_p0 on pspadm.psp_entry_detail_record_p0 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p1 on pspadm.psp_entry_detail_record_p1 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p10 on pspadm.psp_entry_detail_record_p10 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p11 on pspadm.psp_entry_detail_record_p11 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p12 on pspadm.psp_entry_detail_record_p12 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p13 on pspadm.psp_entry_detail_record_p13 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p14 on pspadm.psp_entry_detail_record_p14 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p15 on pspadm.psp_entry_detail_record_p15 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p2 on pspadm.psp_entry_detail_record_p2 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p3 on pspadm.psp_entry_detail_record_p3 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p4 on pspadm.psp_entry_detail_record_p4 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p5 on pspadm.psp_entry_detail_record_p5 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p6 on pspadm.psp_entry_detail_record_p6 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p7 on pspadm.psp_entry_detail_record_p7 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p8 on pspadm.psp_entry_detail_record_p8 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p9 on pspadm.psp_entry_detail_record_p9 USING BTREE  (modified_date);

create index concurrently idx_fts_mod_date_p0 on pspadm.psp_financial_trans_state_p0 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p1 on pspadm.psp_financial_trans_state_p1 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p10 on pspadm.psp_financial_trans_state_p10 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p11 on pspadm.psp_financial_trans_state_p11 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p12 on pspadm.psp_financial_trans_state_p12 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p13 on pspadm.psp_financial_trans_state_p13 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p14 on pspadm.psp_financial_trans_state_p14 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p15 on pspadm.psp_financial_trans_state_p15 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p2 on pspadm.psp_financial_trans_state_p2 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p3 on pspadm.psp_financial_trans_state_p3 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p4 on pspadm.psp_financial_trans_state_p4 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p5 on pspadm.psp_financial_trans_state_p5 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p6 on pspadm.psp_financial_trans_state_p6 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p7 on pspadm.psp_financial_trans_state_p7 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p8 on pspadm.psp_financial_trans_state_p8 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p9 on pspadm.psp_financial_trans_state_p9 USING BTREE  (modified_date);


create index concurrently idx_pstub_pay_item_mod_date_p0 on pspadm.psp_pstub_pay_item_p0 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p1 on pspadm.psp_pstub_pay_item_p1 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p10 on pspadm.psp_pstub_pay_item_p10 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p11 on pspadm.psp_pstub_pay_item_p11 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p12 on pspadm.psp_pstub_pay_item_p12 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p13 on pspadm.psp_pstub_pay_item_p13 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p14 on pspadm.psp_pstub_pay_item_p14 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p15 on pspadm.psp_pstub_pay_item_p15 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p2 on pspadm.psp_pstub_pay_item_p2 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p3 on pspadm.psp_pstub_pay_item_p3 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p4 on pspadm.psp_pstub_pay_item_p4 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p5 on pspadm.psp_pstub_pay_item_p5 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p6 on pspadm.psp_pstub_pay_item_p6 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p7 on pspadm.psp_pstub_pay_item_p7 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p8 on pspadm.psp_pstub_pay_item_p8 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p9 on pspadm.psp_pstub_pay_item_p9 USING BTREE  (modified_date);


create index concurrently idx_tax_mod_date_p0 on pspadm.psp_tax_p0 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p1 on pspadm.psp_tax_p1 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p10 on pspadm.psp_tax_p10 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p11 on pspadm.psp_tax_p11 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p12 on pspadm.psp_tax_p12 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p13 on pspadm.psp_tax_p13 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p14 on pspadm.psp_tax_p14 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p15 on pspadm.psp_tax_p15 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p2 on pspadm.psp_tax_p2 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p3 on pspadm.psp_tax_p3 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p4 on pspadm.psp_tax_p4 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p5 on pspadm.psp_tax_p5 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p6 on pspadm.psp_tax_p6 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p7 on pspadm.psp_tax_p7 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p8 on pspadm.psp_tax_p8 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p9 on pspadm.psp_tax_p9 USING BTREE  (modified_date);


create index concurrently idx_qpi_mod_date_p0 on pspadm.psp_qbdt_paycheck_info_p0 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p1 on pspadm.psp_qbdt_paycheck_info_p1 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p10 on pspadm.psp_qbdt_paycheck_info_p10 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p11 on pspadm.psp_qbdt_paycheck_info_p11 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p12 on pspadm.psp_qbdt_paycheck_info_p12 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p13 on pspadm.psp_qbdt_paycheck_info_p13 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p14 on pspadm.psp_qbdt_paycheck_info_p14 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p15 on pspadm.psp_qbdt_paycheck_info_p15 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p2 on pspadm.psp_qbdt_paycheck_info_p2 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p3 on pspadm.psp_qbdt_paycheck_info_p3 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p4 on pspadm.psp_qbdt_paycheck_info_p4 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p5 on pspadm.psp_qbdt_paycheck_info_p5 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p6 on pspadm.psp_qbdt_paycheck_info_p6 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p7 on pspadm.psp_qbdt_paycheck_info_p7 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p8 on pspadm.psp_qbdt_paycheck_info_p8 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p9 on pspadm.psp_qbdt_paycheck_info_p9 USING BTREE  (modified_date);

create index concurrently idx_qpli_mod_date_p0 on pspadm.psp_qbdt_payline_info_p0 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p1 on pspadm.psp_qbdt_payline_info_p1 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p10 on pspadm.psp_qbdt_payline_info_p10 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p11 on pspadm.psp_qbdt_payline_info_p11 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p12 on pspadm.psp_qbdt_payline_info_p12 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p13 on pspadm.psp_qbdt_payline_info_p13 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p14 on pspadm.psp_qbdt_payline_info_p14 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p15 on pspadm.psp_qbdt_payline_info_p15 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p2 on pspadm.psp_qbdt_payline_info_p2 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p3 on pspadm.psp_qbdt_payline_info_p3 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p4 on pspadm.psp_qbdt_payline_info_p4 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p5 on pspadm.psp_qbdt_payline_info_p5 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p6 on pspadm.psp_qbdt_payline_info_p6 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p7 on pspadm.psp_qbdt_payline_info_p7 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p8 on pspadm.psp_qbdt_payline_info_p8 USING BTREE  (modified_date);
create index concurrently idx_qpli_mod_date_p9 on pspadm.psp_qbdt_payline_info_p9 USING BTREE  (modified_date);


create index concurrently idx_ft_mod_date_p0 on pspadm.psp_financial_transaction_p0 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p1 on pspadm.psp_financial_transaction_p1 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p10 on pspadm.psp_financial_transaction_p10 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p11 on pspadm.psp_financial_transaction_p11 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p12 on pspadm.psp_financial_transaction_p12 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p13 on pspadm.psp_financial_transaction_p13 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p14 on pspadm.psp_financial_transaction_p14 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p15 on pspadm.psp_financial_transaction_p15 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p2 on pspadm.psp_financial_transaction_p2 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p3 on pspadm.psp_financial_transaction_p3 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p4 on pspadm.psp_financial_transaction_p4 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p5 on pspadm.psp_financial_transaction_p5 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p6 on pspadm.psp_financial_transaction_p6 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p7 on pspadm.psp_financial_transaction_p7 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p8 on pspadm.psp_financial_transaction_p8 USING BTREE  (modified_date);
create index concurrently idx_ft_mod_date_p9 on pspadm.psp_financial_transaction_p9 USING BTREE  (modified_date);

create index concurrently idx_paycheck_mod_date_p0 on pspadm.psp_paycheck_p0 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p1 on pspadm.psp_paycheck_p1 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p10 on pspadm.psp_paycheck_p10 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p11 on pspadm.psp_paycheck_p11 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p12 on pspadm.psp_paycheck_p12 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p13 on pspadm.psp_paycheck_p13 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p14 on pspadm.psp_paycheck_p14 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p15 on pspadm.psp_paycheck_p15 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p2 on pspadm.psp_paycheck_p2 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p3 on pspadm.psp_paycheck_p3 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p4 on pspadm.psp_paycheck_p4 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p5 on pspadm.psp_paycheck_p5 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p6 on pspadm.psp_paycheck_p6 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p7 on pspadm.psp_paycheck_p7 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p8 on pspadm.psp_paycheck_p8 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p9 on pspadm.psp_paycheck_p9 USING BTREE  (modified_date);


create index concurrently idx_paycheck_split_mod_date_p0 on pspadm.psp_paycheck_split_p0 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p1 on pspadm.psp_paycheck_split_p1 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p10 on pspadm.psp_paycheck_split_p10 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p11 on pspadm.psp_paycheck_split_p11 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p12 on pspadm.psp_paycheck_split_p12 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p13 on pspadm.psp_paycheck_split_p13 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p14 on pspadm.psp_paycheck_split_p14 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p15 on pspadm.psp_paycheck_split_p15 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p2 on pspadm.psp_paycheck_split_p2 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p3 on pspadm.psp_paycheck_split_p3 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p4 on pspadm.psp_paycheck_split_p4 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p5 on pspadm.psp_paycheck_split_p5 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p6 on pspadm.psp_paycheck_split_p6 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p7 on pspadm.psp_paycheck_split_p7 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p8 on pspadm.psp_paycheck_split_p8 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p9 on pspadm.psp_paycheck_split_p9 USING BTREE  (modified_date);


--Range Partition tables

create index concurrently idx_ent_msg_mod_date_2012 on pspadm.psp_entitlement_message_2012  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2013 on pspadm.psp_entitlement_message_2013  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2014 on pspadm.psp_entitlement_message_2014  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2015 on pspadm.psp_entitlement_message_2015  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2016 on pspadm.psp_entitlement_message_2016  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2017 on pspadm.psp_entitlement_message_2017  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2018 on pspadm.psp_entitlement_message_2018  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2019 on pspadm.psp_entitlement_message_2019  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2020 on pspadm.psp_entitlement_message_2020  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2021 on pspadm.psp_entitlement_message_2021  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2022 on pspadm.psp_entitlement_message_2022  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2023 on pspadm.psp_entitlement_message_2023  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2024 on pspadm.psp_entitlement_message_2024  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2025 on pspadm.psp_entitlement_message_2025  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2026 on pspadm.psp_entitlement_message_2026  USING BTREE (modified_date);


create index concurrently idx_pchk_usg_mod_date_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (modified_date);


create index concurrently idx_ent_upd_mod_date_m112022 on pspadm.psp_entity_update_m112022 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122022 on pspadm.psp_entity_update_m122022 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m012023 on pspadm.psp_entity_update_m012023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m022023 on pspadm.psp_entity_update_m022023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m032023 on pspadm.psp_entity_update_m032023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m042023 on pspadm.psp_entity_update_m042023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m052023 on pspadm.psp_entity_update_m052023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m062023 on pspadm.psp_entity_update_m062023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m072023 on pspadm.psp_entity_update_m072023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m082023 on pspadm.psp_entity_update_m082023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m092023 on pspadm.psp_entity_update_m092023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m102023 on pspadm.psp_entity_update_m102023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m112023 on pspadm.psp_entity_update_m112023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122023 on pspadm.psp_entity_update_m122023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m012024 on pspadm.psp_entity_update_m012024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m022024 on pspadm.psp_entity_update_m022024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m032024 on pspadm.psp_entity_update_m032024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m042024 on pspadm.psp_entity_update_m042024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m052024 on pspadm.psp_entity_update_m052024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m062024 on pspadm.psp_entity_update_m062024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m072024 on pspadm.psp_entity_update_m072024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m082024 on pspadm.psp_entity_update_m082024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m092024 on pspadm.psp_entity_update_m092024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m102024 on pspadm.psp_entity_update_m102024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m112024 on pspadm.psp_entity_update_m112024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122024 on pspadm.psp_entity_update_m122024 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m012025 on pspadm.psp_entity_update_m012025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m022025 on pspadm.psp_entity_update_m022025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m032025 on pspadm.psp_entity_update_m032025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m042025 on pspadm.psp_entity_update_m042025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m052025 on pspadm.psp_entity_update_m052025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m062025 on pspadm.psp_entity_update_m062025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m072025 on pspadm.psp_entity_update_m072025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m082025 on pspadm.psp_entity_update_m082025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m092025 on pspadm.psp_entity_update_m092025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m102025 on pspadm.psp_entity_update_m102025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m112025 on pspadm.psp_entity_update_m112025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122025 on pspadm.psp_entity_update_m122025 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m012026 on pspadm.psp_entity_update_m012026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m022026 on pspadm.psp_entity_update_m022026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m032026 on pspadm.psp_entity_update_m032026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m042026 on pspadm.psp_entity_update_m042026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m052026 on pspadm.psp_entity_update_m052026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m062026 on pspadm.psp_entity_update_m062026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m072026 on pspadm.psp_entity_update_m072026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m082026 on pspadm.psp_entity_update_m082026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m092026 on pspadm.psp_entity_update_m092026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m102026 on pspadm.psp_entity_update_m102026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m112026 on pspadm.psp_entity_update_m112026 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122026 on pspadm.psp_entity_update_m122026 USING BTREE (modified_date);



--Non-partition tables
create index concurrently idx_prl_run_mod_date on pspadm.psp_payroll_run  USING BTREE (modified_date);
create index concurrently idx_bill_det_mod_date on pspadm.psp_billing_detail  USING BTREE (modified_date);
create index concurrently idx_cdl_mod_date on pspadm.psp_company_daily_liability  USING BTREE (modified_date);
create index concurrently idx_csba_mod_date on pspadm.psp_company_service_bank_acct  USING BTREE (modified_date);
create index concurrently idx_elqt_mod_date on pspadm.psp_employee_law_qtr_totals  USING BTREE (modified_date);
create index concurrently idx_epi_mod_date on pspadm.psp_employee_payroll_item  USING BTREE (modified_date);
create index concurrently idx_emp_tax_mod_date  on	psp_employee_tax  USING BTREE (modified_date);
create index concurrently idx_emp_cont_mod_date on pspadm.psp_employer_contribution  USING BTREE (modified_date);
create index concurrently idx_individual_mod_date on pspadm.psp_individual  USING BTREE (modified_date);
create index concurrently idx_pdditem_mod_date on pspadm.psp_pstub_dditem  USING BTREE (modified_date);
create index concurrently idx_ttmd_mod_date on pspadm.psp_tax_table_misc_data  USING BTREE (modified_date);
create index concurrently idx_ewt_mod_date on pspadm.psp_employee_w2_totals  USING BTREE (modified_date);


SELECT CURRENT_TIMESTAMP;


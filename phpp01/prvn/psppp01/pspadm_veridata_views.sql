\timing

set search_path to pspadm;


CREATE VIEW pspadm.psp_pstub_employee_info_veridata_view as  
select * from pspadm.psp_pstub_employee_info_p3 union all
select * from pspadm.psp_pstub_employee_info_p0 union all
select * from pspadm.psp_pstub_employee_info_p1 union all
select * from pspadm.psp_pstub_employee_info_p2 ;

CREATE VIEW pspadm.psp_compensation_veridata_view as  
select * from pspadm.psp_compensation_p0 union all
select * from pspadm.psp_compensation_p1 union all
select * from pspadm.psp_compensation_p2 union all
select * from pspadm.psp_compensation_p3 union all
select * from pspadm.psp_compensation_p4 union all
select * from pspadm.psp_compensation_p5 union all
select * from pspadm.psp_compensation_p6 union all
select * from pspadm.psp_compensation_p7 ;

CREATE VIEW pspadm.psp_ledger_balance_veridata_view as 
select * from pspadm.psp_ledger_balance_p0 union all
select * from pspadm.psp_ledger_balance_p1 union all
select * from pspadm.psp_ledger_balance_p2 union all
select * from pspadm.psp_ledger_balance_p3 union all
select * from pspadm.psp_ledger_balance_p4 union all
select * from pspadm.psp_ledger_balance_p5 union all
select * from pspadm.psp_ledger_balance_p6 union all
select * from pspadm.psp_ledger_balance_p7 ;


CREATE VIEW pspadm.psp_money_movement_transaction_veridata_view as 
select * from pspadm.psp_money_movement_transaction_p1 union all
select * from pspadm.psp_money_movement_transaction_p2 union all
select * from pspadm.psp_money_movement_transaction_p3 union all
select * from pspadm.psp_money_movement_transaction_p4 union all
select * from pspadm.psp_money_movement_transaction_p5 union all
select * from pspadm.psp_money_movement_transaction_p7 union all
select * from pspadm.psp_money_movement_transaction_p6 union all
select * from pspadm.psp_money_movement_transaction_p0 ;

CREATE VIEW pspadm.psp_company_event_veridata_view as 
select * from pspadm.psp_company_event_p7 union all
select * from pspadm.psp_company_event_p0 union all
select * from pspadm.psp_company_event_p1 union all
select * from pspadm.psp_company_event_p2 union all
select * from pspadm.psp_company_event_p3 union all
select * from pspadm.psp_company_event_p4 union all
select * from pspadm.psp_company_event_p5 union all
select * from pspadm.psp_company_event_p6 ;

CREATE VIEW pspadm.psp_company_event_detail_veridata_view as 
select * from pspadm.psp_company_event_detail_p0 union all
select * from pspadm.psp_company_event_detail_p2 union all
select * from pspadm.psp_company_event_detail_p3 union all
select * from pspadm.psp_company_event_detail_p4 union all
select * from pspadm.psp_company_event_detail_p5 union all
select * from pspadm.psp_company_event_detail_p7 union all
select * from pspadm.psp_company_event_detail_p6 union all
select * from pspadm.psp_company_event_detail_p1 ;

CREATE VIEW pspadm.psp_company_event_email_param_veridata_view as 
select * from pspadm.psp_company_event_email_param_p1 union all
select * from pspadm.psp_company_event_email_param_p2 union all
select * from pspadm.psp_company_event_email_param_p0 union all
select * from pspadm.psp_company_event_email_param_p5 union all
select * from pspadm.psp_company_event_email_param_p3 union all
select * from pspadm.psp_company_event_email_param_p6 union all
select * from pspadm.psp_company_event_email_param_p7 union all
select * from pspadm.psp_company_event_email_param_p4 ;

CREATE VIEW pspadm.psp_financial_transaction_veridata_view as
select * from pspadm.psp_financial_transaction_p5 union all
select * from pspadm.psp_financial_transaction_p11 union all
select * from pspadm.psp_financial_transaction_p3 union all
select * from pspadm.psp_financial_transaction_p4 union all
select * from pspadm.psp_financial_transaction_p6 union all
select * from pspadm.psp_financial_transaction_p10 union all
select * from pspadm.psp_financial_transaction_p12 union all
select * from pspadm.psp_financial_transaction_p13 union all
select * from pspadm.psp_financial_transaction_p15 union all
select * from pspadm.psp_financial_transaction_p0 union all
select * from pspadm.psp_financial_transaction_p1 union all
select * from pspadm.psp_financial_transaction_p8 union all
select * from pspadm.psp_financial_transaction_p9 union all
select * from pspadm.psp_financial_transaction_p2 union all
select * from pspadm.psp_financial_transaction_p7 union all
select * from pspadm.psp_financial_transaction_p14 ;

CREATE VIEW pspadm.psp_paycheck_split_veridata_view as 
select * from pspadm.psp_paycheck_split_p0 union all
select * from pspadm.psp_paycheck_split_p1 union all
select * from pspadm.psp_paycheck_split_p2 union all
select * from pspadm.psp_paycheck_split_p3 union all
select * from pspadm.psp_paycheck_split_p4 union all
select * from pspadm.psp_paycheck_split_p5 union all
select * from pspadm.psp_paycheck_split_p6 union all
select * from pspadm.psp_paycheck_split_p7 union all
select * from pspadm.psp_paycheck_split_p8 union all
select * from pspadm.psp_paycheck_split_p9 union all
select * from pspadm.psp_paycheck_split_p10 union all
select * from pspadm.psp_paycheck_split_p11 union all
select * from pspadm.psp_paycheck_split_p12 union all
select * from pspadm.psp_paycheck_split_p13 union all
select * from pspadm.psp_paycheck_split_p14 union all
select * from pspadm.psp_paycheck_split_p15 ;

CREATE VIEW pspadm.psp_qbdt_payline_info_veridata_view as 
select * from pspadm.psp_qbdt_payline_info_p13 union all
select * from pspadm.psp_qbdt_payline_info_p14 union all
select * from pspadm.psp_qbdt_payline_info_p0 union all
select * from pspadm.psp_qbdt_payline_info_p6 union all
select * from pspadm.psp_qbdt_payline_info_p7 union all
select * from pspadm.psp_qbdt_payline_info_p8 union all
select * from pspadm.psp_qbdt_payline_info_p15 union all
select * from pspadm.psp_qbdt_payline_info_p1 union all
select * from pspadm.psp_qbdt_payline_info_p2 union all
select * from pspadm.psp_qbdt_payline_info_p3 union all
select * from pspadm.psp_qbdt_payline_info_p4 union all
select * from pspadm.psp_qbdt_payline_info_p5 union all
select * from pspadm.psp_qbdt_payline_info_p9 union all
select * from pspadm.psp_qbdt_payline_info_p10 union all
select * from pspadm.psp_qbdt_payline_info_p11 union all
select * from pspadm.psp_qbdt_payline_info_p12 ;


CREATE VIEW pspadm.psp_financial_trans_state_veridata_view as 
select * from pspadm.psp_financial_trans_state_p8 union all
select * from pspadm.psp_financial_trans_state_p12 union all
select * from pspadm.psp_financial_trans_state_p14 union all
select * from pspadm.psp_financial_trans_state_p0 union all
select * from pspadm.psp_financial_trans_state_p3 union all
select * from pspadm.psp_financial_trans_state_p1 union all
select * from pspadm.psp_financial_trans_state_p2 union all
select * from pspadm.psp_financial_trans_state_p9 union all
select * from pspadm.psp_financial_trans_state_p11 union all
select * from pspadm.psp_financial_trans_state_p5 union all
select * from pspadm.psp_financial_trans_state_p7 union all
select * from pspadm.psp_financial_trans_state_p13 union all
select * from pspadm.psp_financial_trans_state_p10 union all
select * from pspadm.psp_financial_trans_state_p4 union all
select * from pspadm.psp_financial_trans_state_p15 union all
select * from pspadm.psp_financial_trans_state_p6 ;

CREATE VIEW pspadm.psp_pstub_pay_item_veridata_view as 
select * from pspadm.psp_pstub_pay_item_p0 union all
select * from pspadm.psp_pstub_pay_item_p1 union all
select * from pspadm.psp_pstub_pay_item_p5 union all
select * from pspadm.psp_pstub_pay_item_p6 union all
select * from pspadm.psp_pstub_pay_item_p2 union all
select * from pspadm.psp_pstub_pay_item_p15 union all
select * from pspadm.psp_pstub_pay_item_p3 union all
select * from pspadm.psp_pstub_pay_item_p4 union all
select * from pspadm.psp_pstub_pay_item_p10 union all
select * from pspadm.psp_pstub_pay_item_p7 union all
select * from pspadm.psp_pstub_pay_item_p8 union all
select * from pspadm.psp_pstub_pay_item_p9 union all
select * from pspadm.psp_pstub_pay_item_p11 union all
select * from pspadm.psp_pstub_pay_item_p12 union all
select * from pspadm.psp_pstub_pay_item_p13 union all
select * from pspadm.psp_pstub_pay_item_p14 ;

CREATE VIEW pspadm.psp_tax_veridata_view as 
select * from pspadm.psp_tax_p7 union all
select * from pspadm.psp_tax_p4 union all
select * from pspadm.psp_tax_p5 union all
select * from pspadm.psp_tax_p6 union all
select * from pspadm.psp_tax_p8 union all
select * from pspadm.psp_tax_p10 union all
select * from pspadm.psp_tax_p9 union all
select * from pspadm.psp_tax_p11 union all
select * from pspadm.psp_tax_p12 union all
select * from pspadm.psp_tax_p1 union all
select * from pspadm.psp_tax_p2 union all
select * from pspadm.psp_tax_p3 union all
select * from pspadm.psp_tax_p0 union all
select * from pspadm.psp_tax_p14 union all
select * from pspadm.psp_tax_p15 union all
select * from pspadm.psp_tax_p13 ;

CREATE VIEW pspadm.psp_paycheck_veridata_view as 
select * from pspadm.psp_paycheck_p1 union all
select * from pspadm.psp_paycheck_p9 union all
select * from pspadm.psp_paycheck_p0 union all
select * from pspadm.psp_paycheck_p3 union all
select * from pspadm.psp_paycheck_p2 union all
select * from pspadm.psp_paycheck_p5 union all
select * from pspadm.psp_paycheck_p4 union all
select * from pspadm.psp_paycheck_p8 union all
select * from pspadm.psp_paycheck_p6 union all
select * from pspadm.psp_paycheck_p10 union all
select * from pspadm.psp_paycheck_p7 union all
select * from pspadm.psp_paycheck_p14 union all
select * from pspadm.psp_paycheck_p11 union all
select * from pspadm.psp_paycheck_p12 union all
select * from pspadm.psp_paycheck_p13 union all
select * from pspadm.psp_paycheck_p15 ;

CREATE VIEW pspadm.qbdt_paycheck_info_veridata_view as 
select * from pspadm.psp_qbdt_paycheck_info_p14 union all
select * from pspadm.psp_qbdt_paycheck_info_p0 union all
select * from pspadm.psp_qbdt_paycheck_info_p1 union all
select * from pspadm.psp_qbdt_paycheck_info_p3 union all
select * from pspadm.psp_qbdt_paycheck_info_p4 union all
select * from pspadm.psp_qbdt_paycheck_info_p2 union all
select * from pspadm.psp_qbdt_paycheck_info_p5 union all
select * from pspadm.psp_qbdt_paycheck_info_p6 union all
select * from pspadm.psp_qbdt_paycheck_info_p7 union all
select * from pspadm.psp_qbdt_paycheck_info_p8 union all
select * from pspadm.psp_qbdt_paycheck_info_p9 union all
select * from pspadm.psp_qbdt_paycheck_info_p10 union all
select * from pspadm.psp_qbdt_paycheck_info_p11 union all
select * from pspadm.psp_qbdt_paycheck_info_p12 union all
select * from pspadm.psp_qbdt_paycheck_info_p13 union all
select * from pspadm.psp_qbdt_paycheck_info_p15 ;


CREATE VIEW pspadm.psp_entry_detail_record_veridata_view as 
select * from pspadm.psp_entry_detail_record_p15 union all
select * from pspadm.psp_entry_detail_record_p2 union all
select * from pspadm.psp_entry_detail_record_p10 union all
select * from pspadm.psp_entry_detail_record_p14 union all
select * from pspadm.psp_entry_detail_record_p4 union all
select * from pspadm.psp_entry_detail_record_p8 union all
select * from pspadm.psp_entry_detail_record_p0 union all
select * from pspadm.psp_entry_detail_record_p1 union all
select * from pspadm.psp_entry_detail_record_p3 union all
select * from pspadm.psp_entry_detail_record_p7 union all
select * from pspadm.psp_entry_detail_record_p11 union all
select * from pspadm.psp_entry_detail_record_p13 union all
select * from pspadm.psp_entry_detail_record_p6 union all
select * from pspadm.psp_entry_detail_record_p5 union all
select * from pspadm.psp_entry_detail_record_p12 ;

CREATE VIEW pspadm.psp_property_audit_veridata_view as 
select * from pspadm.psp_property_audit_p2 union all
select * from pspadm.psp_property_audit_p3 union all
select * from pspadm.psp_property_audit_p6 union all
select * from pspadm.psp_property_audit_p7 union all
select * from pspadm.psp_property_audit_p1 union all
select * from pspadm.psp_property_audit_p0 union all
select * from pspadm.psp_property_audit_p4 union all
select * from pspadm.psp_property_audit_p5 ;

CREATE VIEW pspadm.psp_disburse_advice_tax_liab_veridata_view as 
select * from pspadm.psp_disburse_advice_tax_liab_p1 union all
select * from pspadm.psp_disburse_advice_tax_liab_p3 union all
select * from pspadm.psp_disburse_advice_tax_liab_p2 union all
select * from pspadm.psp_disburse_advice_tax_liab_p0 ;

CREATE VIEW pspadm.psp_qbdt_transaction_info_veridata_view as 
select * from pspadm.psp_qbdt_transaction_info_p2 union all
select * from pspadm.psp_qbdt_transaction_info_p3 union all
select * from pspadm.psp_qbdt_transaction_info_p0 union all
select * from pspadm.psp_qbdt_transaction_info_p1 ;

CREATE VIEW pspadm.psp_paystub_veridata_view as 
select * from pspadm.psp_paystub_p1 union all
select * from pspadm.psp_paystub_p3 union all
select * from pspadm.psp_paystub_p2 union all
select * from pspadm.psp_paystub_p0 ;

CREATE VIEW pspadm.psp_deduction_veridata_view as 
select * from pspadm.psp_deduction_p3 union all
select * from pspadm.psp_deduction_p1 union all
select * from pspadm.psp_deduction_p2 union all
select * from pspadm.psp_deduction_p0 ;

CREATE VIEW pspadm.psp_pstub_paid_timeoff_item_veridata_view as 
select * from pspadm.psp_pstub_paid_timeoff_item_p2 union all
select * from pspadm.psp_pstub_paid_timeoff_item_p0 union all
select * from pspadm.psp_pstub_paid_timeoff_item_p1 union all
select * from pspadm.psp_pstub_paid_timeoff_item_p3 ;

--Range partition tables

CREATE VIEW pspadm.psp_paycheck_usage_veridata_view as
select * from pspadm.psp_paycheck_usage_2020 union all
select * from pspadm.psp_paycheck_usage_2021 union all
select * from pspadm.psp_paycheck_usage_2018 union all
select * from pspadm.psp_paycheck_usage_2013 union all
select * from pspadm.psp_paycheck_usage_2019 union all
select * from pspadm.psp_paycheck_usage_2014 union all
select * from pspadm.psp_paycheck_usage_2012 union all
select * from pspadm.psp_paycheck_usage_2022 union all
select * from pspadm.psp_paycheck_usage_2023 union all
select * from pspadm.psp_paycheck_usage_2015 union all
select * from pspadm.psp_paycheck_usage_2016 union all
select * from pspadm.psp_paycheck_usage_2017 union all
select * from pspadm.psp_paycheck_usage_2024;

CREATE VIEW pspadm.psp_entitlement_message_veridata_view as
select * from pspadm.psp_entitlement_message_2012 union all
select * from pspadm.psp_entitlement_message_2014 union all
select * from pspadm.psp_entitlement_message_2016 union all
select * from pspadm.psp_entitlement_message_2015 union all
select * from pspadm.psp_entitlement_message_2017 union all
select * from pspadm.psp_entitlement_message_2018 union all
select * from pspadm.psp_entitlement_message_2019 union all
select * from pspadm.psp_entitlement_message_2020 union all
select * from pspadm.psp_entitlement_message_2021 union all
select * from pspadm.psp_entitlement_message_2022 union all
select * from pspadm.psp_entitlement_message_2023 union all
select * from pspadm.psp_entitlement_message_2013 union all
select * from pspadm.psp_entitlement_message_2024 ;

CREATE VIEW pspadm.psp_entity_update_veridata_view as
select * from pspadm.psp_entity_update_m042023 union all
select * from pspadm.psp_entity_update_m032023 union all
select * from pspadm.psp_entity_update_m022023 union all
select * from pspadm.psp_entity_update_m012023 union all
select * from pspadm.psp_entity_update_m122022 union all
select * from pspadm.psp_entity_update_m112022 union all
select * from pspadm.psp_entity_update_m112022 union all
select * from pspadm.psp_entity_update_m052023 union all
select * from pspadm.psp_entity_update_m062023 union all
select * from pspadm.psp_entity_update_m072023 union all
select * from pspadm.psp_entity_update_m082023 union all
select * from pspadm.psp_entity_update_m092023 union all
select * from pspadm.psp_entity_update_m102023 union all
select * from pspadm.psp_entity_update_m112023 union all
select * from pspadm.psp_entity_update_m122023 ;




-- ------------ Write CREATE-TRIGGER-stage scripts -----------

CREATE TRIGGER psp_address_at
AFTER INSERT OR UPDATE
ON pspadm.psp_address
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_address_at$psp_address();



CREATE TRIGGER psp_bpcompany_service_info_at
AFTER INSERT OR UPDATE
ON pspadm.psp_bpcompany_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_bpcompany_service_info_at$psp_bpcompany_service_info();



CREATE TRIGGER psp_cmpmtmplt_pmtmtd_at
AFTER INSERT OR UPDATE
ON pspadm.psp_comp_pmttemplate_pmtmethod
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_cmpmtmplt_pmtmtd_at$psp_comp_pmttemplate_pmtmethod();



CREATE TRIGGER psp_company_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_at$psp_company();



CREATE TRIGGER psp_company_agency_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company_agency
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_agency_at$psp_company_agency();



CREATE TRIGGER psp_company_bank_account_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company_bank_account
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_bank_account_at$psp_company_bank_account();



CREATE TRIGGER tr_ins_company_event_timestamp
BEFORE INSERT
ON pspadm.psp_company_event
FOR EACH ROW
EXECUTE PROCEDURE pspadm.tr_ins_company_event_timestamp$psp_company_event();



CREATE TRIGGER tr_upd_company_event_timestamp
BEFORE UPDATE
ON pspadm.psp_company_event
FOR EACH ROW
EXECUTE PROCEDURE pspadm.tr_upd_company_event_timestamp$psp_company_event();



CREATE TRIGGER psp_company_law_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company_law
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_law_at$psp_company_law();



CREATE TRIGGER psp_company_offer_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company_offer
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_offer_at$psp_company_offer();



CREATE TRIGGER psp_company_payroll_item_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company_payroll_item
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_payroll_item_at$psp_company_payroll_item();



CREATE TRIGGER psp_company_service_at
AFTER INSERT OR UPDATE
ON pspadm.psp_company_service
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_company_service_at$psp_company_service();



CREATE TRIGGER psp_cmpnyagency_pmttplt_at
AFTER INSERT OR UPDATE
ON pspadm.psp_companyagency_pmttemplate
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_cmpnyagency_pmttplt_at$psp_companyagency_pmttemplate();



CREATE TRIGGER psp_contact_at
AFTER INSERT OR UPDATE
ON pspadm.psp_contact
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_contact_at$psp_contact();



CREATE TRIGGER psp_ddcompany_service_info_at
AFTER INSERT OR UPDATE
ON pspadm.psp_ddcompany_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_ddcompany_service_info_at$psp_ddcompany_service_info();



CREATE TRIGGER psp_employee_at
AFTER INSERT OR UPDATE
ON pspadm.psp_employee
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_employee_at$psp_employee();



CREATE TRIGGER psp_entity_change_at
AFTER INSERT OR UPDATE
ON pspadm.psp_entity_change
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_entity_change_at$psp_entity_change();



CREATE TRIGGER trc_ledger_balance_calc_ai
AFTER INSERT
ON pspadm.psp_financial_trans_state
FOR EACH ROW
EXECUTE PROCEDURE pspadm.trc_ledger_balance_calc_ai$psp_financial_trans_state();



CREATE TRIGGER psp_individual_at
AFTER INSERT OR UPDATE
ON pspadm.psp_individual
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_individual_at$psp_individual();



CREATE TRIGGER psp_money_mvmt_trans_at
AFTER INSERT OR UPDATE
ON pspadm.psp_money_movement_transaction
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_money_mvmt_trans_at$psp_money_movement_transaction();



CREATE TRIGGER tr_upd_dd_limits
BEFORE INSERT OR UPDATE
ON pspadm.psp_property_audit
FOR EACH ROW
EXECUTE PROCEDURE pspadm.tr_upd_dd_limits$psp_property_audit();



CREATE TRIGGER psp_quickbooks_info_at
AFTER INSERT OR UPDATE
ON pspadm.psp_quickbooks_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_quickbooks_info_at$psp_quickbooks_info();



CREATE TRIGGER psp_tax_cs_info_at
AFTER INSERT OR UPDATE
ON pspadm.psp_tax_company_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_tax_cs_info_at$psp_tax_company_service_info();



CREATE TRIGGER psp_tax_penalty_interest_at
AFTER INSERT OR UPDATE
ON pspadm.psp_tax_penalty_interest
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_tax_penalty_interest_at$psp_tax_penalty_interest();



CREATE TRIGGER ht_psp_agency_check_batch_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_agency_check_batch
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_agency_check_batch_iud();



CREATE TRIGGER ht_psp_agency_id_requirement_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_agency_id_requirement
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_agency_id_requirement_iud();



CREATE TRIGGER ht_psp_bpcompany_service_info_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_bpcompany_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_bpcompany_service_info_iud();



CREATE TRIGGER ht_psp_cdcompany_service_info_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_cdcompany_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_cdcompany_service_info_iud();



CREATE TRIGGER ht_psp_check_print_batch_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_check_print_batch
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_check_print_batch_iud();



CREATE TRIGGER ht_psp_company_paycheck_batch_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_company_paycheck_batch
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_company_paycheck_batch_iud();



CREATE TRIGGER ht_psp_company_service_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_company_service
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_company_service_iud();



CREATE TRIGGER ht_psp_contact_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_contact
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_contact_iud();



CREATE TRIGGER ht_psp_ddcompany_service_info_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_ddcompany_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_ddcompany_service_info_iud();



CREATE TRIGGER ht_psp_dep_freq_ledger_operati_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_dep_freq_ledger_operati
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_dep_freq_ledger_operati_iud();



CREATE TRIGGER ht_psp_deposit_frequency_req_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_deposit_frequency_req
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_deposit_frequency_req_iud();



CREATE TRIGGER ht_psp_edi_tax_file_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_edi_tax_file
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_edi_tax_file_iud();



CREATE TRIGGER ht_psp_eftps_file_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_eftps_file
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_eftps_file_iud();



CREATE TRIGGER ht_psp_employee_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_employee
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_employee_iud();



CREATE TRIGGER ht_psp_individual_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_individual
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_individual_iud();



CREATE TRIGGER ht_psp_ledger_operation_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_ledger_operation
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_ledger_operation_iud();



CREATE TRIGGER ht_psp_manual_requirement_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_manual_requirement
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_manual_requirement_iud();



CREATE TRIGGER ht_psp_payment_method_requirem_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_payment_method_requirem
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_payment_method_requirem_iud();



CREATE TRIGGER ht_psp_payment_requirement_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_payment_requirement
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_payment_requirement_iud();



CREATE TRIGGER ht_psp_racompany_service_info_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_racompany_service_info
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_racompany_service_info_iud();



CREATE TRIGGER ht_psp_rate_ledger_operation_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_rate_ledger_operation
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_rate_ledger_operation_iud();



CREATE TRIGGER ht_psp_state_edi_tax_file_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_state_edi_tax_file
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_state_edi_tax_file_iud();



CREATE TRIGGER ht_psp_system_payment_requirem_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_system_payment_requirem
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_system_payment_requirem_iud();



CREATE TRIGGER ht_psp_system_requirement_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_system_requirement
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_system_requirement_iud();



CREATE TRIGGER ht_psp_tax_company_service_inf_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_tax_company_service_inf
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_tax_company_service_inf_iud();



CREATE TRIGGER ht_psp_threshold_requirement_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_threshold_requirement
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_threshold_requirement_iud();



CREATE TRIGGER ht_psp_tp401kcompany_service_i_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.ht_psp_tp401kcompany_service_i
FOR EACH ROW
EXECUTE PROCEDURE pspadm.ht_psp_tp401kcompany_service_i_iud();



CREATE TRIGGER quest_sl_temp_explain1_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.quest_sl_temp_explain1
FOR EACH ROW
EXECUTE PROCEDURE pspadm.quest_sl_temp_explain1_iud();



CREATE TRIGGER quest_sl_temp_explain2_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.quest_sl_temp_explain2
FOR EACH ROW
EXECUTE PROCEDURE pspadm.quest_sl_temp_explain2_iud();



CREATE TRIGGER sys_temp_fbt_iud
INSTEAD OF INSERT OR UPDATE OR DELETE
ON pspadm.sys_temp_fbt
FOR EACH ROW
EXECUTE PROCEDURE pspadm.sys_temp_fbt_iud();




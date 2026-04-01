-- ------------ Write CREATE-TRIGGER-stage scripts -----------

CREATE TRIGGER psp_address_at
AFTER INSERT OR UPDATE
ON pspadm.psp_address
FOR EACH ROW
EXECUTE PROCEDURE pspadm.psp_address_at$psp_address();



CREATE TRIGGER tr_upd_intuit_tax_bank_acct
AFTER UPDATE
ON pspadm.psp_bank_account
FOR EACH ROW
EXECUTE PROCEDURE pspadm.tr_upd_intuit_tax_bank_acct$psp_bank_account();



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



CREATE TRIGGER tr_upd_company_event_timestamp
BEFORE UPDATE
ON pspadm.psp_company_event
FOR EACH ROW
EXECUTE PROCEDURE pspadm.tr_upd_company_event_timestamp$psp_company_event();



CREATE TRIGGER tr_upd_company_evnt_timestamp
BEFORE UPDATE
ON pspadm.psp_company_event
FOR EACH ROW
EXECUTE PROCEDURE pspadm.tr_upd_company_evnt_timestamp$psp_company_event();



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




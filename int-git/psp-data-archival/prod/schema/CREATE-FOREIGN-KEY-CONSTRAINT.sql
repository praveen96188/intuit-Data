-- ------------ Write CREATE-FOREIGN-KEY-CONSTRAINT-stage scripts -----------

ALTER TABLE pspadm.psp_achenrollment
ADD CONSTRAINT psp_achenrollment_fk2 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_achenrollment_detail
ADD CONSTRAINT psp_achenrollment_detail_fk1 FOREIGN KEY (a_c_h_enrollment_fk, realm_id) 
REFERENCES pspadm.psp_achenrollment (achenrollment_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_achenrollment_detail
ADD CONSTRAINT psp_achenrollment_detail_fk2 FOREIGN KEY (response_file_fk, realm_id) 
REFERENCES pspadm.psp_achenrollment_file (achenrollment_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_achenrollment_detail
ADD CONSTRAINT psp_achenrollment_detail_fk3 FOREIGN KEY (request_file_fk, realm_id) 
REFERENCES pspadm.psp_achenrollment_file (achenrollment_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_additional_filing_amount
ADD CONSTRAINT psp_additional_filing_amou_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ade_law_map
ADD CONSTRAINT psp_ade_law_map_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ade_law_map
ADD CONSTRAINT psp_ade_law_map_fk2 FOREIGN KEY (ade_law_map_fk, realm_id) 
REFERENCES pspadm.psp_ade_law_map (ade_law_map_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_agency_check_batch
ADD CONSTRAINT psp_agency_check_batch_fk1 FOREIGN KEY (agency_check_batch_seq, realm_id) 
REFERENCES pspadm.psp_check_print_batch (check_print_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_agency_check_batch
ADD CONSTRAINT psp_agency_check_batch_fk2 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_agency_id_requirement
ADD CONSTRAINT psp_agency_id_requirement_fk1 FOREIGN KEY (agency_id_requirement_seq, realm_id) 
REFERENCES pspadm.psp_payment_method_requirement (payment_method_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_agency_id_requirement
ADD CONSTRAINT psp_agency_id_requirement_fk2 FOREIGN KEY (payment_template_agency_id_fk, realm_id) 
REFERENCES pspadm.psp_payment_template_agency_id (payment_template_agency_id_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_agency_rate_request
ADD CONSTRAINT psp_agency_rate_request_fk1 FOREIGN KEY (agency_fk, realm_id) 
REFERENCES pspadm.psp_agency (agency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_annual_billing_item
ADD CONSTRAINT psp_annualbillingitem_fk1 FOREIGN KEY (annual_billing_batch_fk, realm_id) 
REFERENCES pspadm.psp_annual_billing_batch (annual_billing_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_annual_billing_item
ADD CONSTRAINT psp_annualbillingitem_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_assisted_bundle_bill
ADD CONSTRAINT psp_assistedbundlebill_fk1 FOREIGN KEY (asst_bundle_comp_usage_fk, realm_id) 
REFERENCES pspadm.psp_asst_bundle_comp_usage (asst_bundle_comp_usage_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_asst_bundle_bill_detail
ADD CONSTRAINT psp_asstbundlebilldetail_fk1 FOREIGN KEY (assisted_bundle_bill_fk, realm_id) 
REFERENCES pspadm.psp_assisted_bundle_bill (assisted_bundle_bill_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_atfdata_extract_file
ADD CONSTRAINT psp_atfdata_extract_file_fk1 FOREIGN KEY (a_t_f_data_extract_batch_fk, realm_id) 
REFERENCES pspadm.psp_atfdata_extract_batch (atfdata_extract_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_atfpayments_to_process
ADD CONSTRAINT psp_atfpayments_to_process_fk2 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_atfpayments_to_process
ADD CONSTRAINT psp_atfpayments_to_process_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_atfpayrolls_to_process
ADD CONSTRAINT psp_atfpayrolls_to_process_fk1 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_auth_role
ADD CONSTRAINT psp_auth_role_fk1 FOREIGN KEY (auth_domain_fk, realm_id) 
REFERENCES pspadm.psp_auth_domain (domain_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_auth_user_auth_role__assoc
ADD CONSTRAINT psp_userrole_fk_role FOREIGN KEY (auth_role_fk, realm_id) 
REFERENCES pspadm.psp_auth_role (auth_role_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_auth_user_auth_role__assoc
ADD CONSTRAINT psp_userrole_fk_user FOREIGN KEY (auth_user_fk, realm_id) 
REFERENCES pspadm.psp_auth_user (auth_user_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_authrole_operation_assoc
ADD CONSTRAINT psp_authrole_opt_fk_opt FOREIGN KEY (auth_operation_fk, realm_id) 
REFERENCES pspadm.psp_auth_operation (operation_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_authrole_operation_assoc
ADD CONSTRAINT psp_role_operation_fk_role FOREIGN KEY (auth_role_fk, realm_id) 
REFERENCES pspadm.psp_auth_role (auth_role_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_batch_job_parameter
ADD CONSTRAINT psp_batch_job_parameter_fk1 FOREIGN KEY (batch_job_setup_fk, realm_id) 
REFERENCES pspadm.psp_batch_job_setup (job_type, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_bill
ADD CONSTRAINT psp_bill_fk1 FOREIGN KEY (company_usage_fk, realm_id) 
REFERENCES pspadm.psp_company_usage (company_usage_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_bill_payment
ADD CONSTRAINT psp_bill_payment_fk1 FOREIGN KEY (payee_fk, realm_id) 
REFERENCES pspadm.psp_payee (payee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_bill_payment
ADD CONSTRAINT psp_bill_payment_fk2 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_bill_payment_split
ADD CONSTRAINT psp_bill_payment_split_fk1 FOREIGN KEY (bill_payment_fk, realm_id) 
REFERENCES pspadm.psp_bill_payment (bill_payment_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_bill_payment_split
ADD CONSTRAINT psp_bill_payment_split_fk2 FOREIGN KEY (payee_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_payee_bank_account (payee_bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_billing_detail
ADD CONSTRAINT psp_billing_detail_fk1 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_billing_detail
ADD CONSTRAINT psp_billing_detail_fk2 FOREIGN KEY (offering_svcchg_price_fk, realm_id) 
REFERENCES pspadm.psp_svcchgprice (svcchgprice_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_bpcompany_service_info
ADD CONSTRAINT psp_bpcompany_service_info_fk1 FOREIGN KEY (bpcompany_service_info_seq, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_cdcompany_service_info
ADD CONSTRAINT psp_cdcompany_service_info_fk1 FOREIGN KEY (cdcompany_service_info_seq, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_check_print_batch
ADD CONSTRAINT psp_check_print_batch_fk1 FOREIGN KEY (recon_plus_file_fk, realm_id) 
REFERENCES pspadm.psp_accounting_report_file (accounting_report_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_check_print_batch
ADD CONSTRAINT psp_check_print_batch_fk2 FOREIGN KEY (positive_pay_file_fk, realm_id) 
REFERENCES pspadm.psp_accounting_report_file (accounting_report_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_check_print_paycheck
ADD CONSTRAINT psp_check_print_paycheck_fk1 FOREIGN KEY (company_paycheck_batch_fk, realm_id) 
REFERENCES pspadm.psp_company_paycheck_batch (company_paycheck_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_check_print_paycheck
ADD CONSTRAINT psp_checkprintpaycheck_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_check_print_signature
ADD CONSTRAINT psp_check_print_signature_fk1 FOREIGN KEY (sourcesys_printedchk_info_fk, realm_id) 
REFERENCES pspadm.psp_sourcesys_printedchk_info (sourcesys_printedchk_info_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_comp_adjust_submission
ADD CONSTRAINT psp_company_adjustment_sub_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_comp_adjust_submission
ADD CONSTRAINT psp_company_adjustment_sub_fk3 FOREIGN KEY (void_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_comp_adjust_submission
ADD CONSTRAINT psp_company_adjustment_sub_fk4 FOREIGN KEY (original_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_comp_pmt_template_agencyid
ADD CONSTRAINT psp_companypaymenttemplate_fk2 FOREIGN KEY (company_agency_pmt_template_fk, realm_id) 
REFERENCES pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_comp_pmttemplate_pmtmethod
ADD CONSTRAINT psp_companypaymenttemplate_fk1 FOREIGN KEY (company_agency_pmt_template_fk, realm_id) 
REFERENCES pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk2 FOREIGN KEY (payroll_frequency_fk, realm_id) 
REFERENCES pspadm.psp_payroll_frequency (payroll_freq_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk3 FOREIGN KEY (mailing_address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk4 FOREIGN KEY (legal_address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk5 FOREIGN KEY (offload_group_fk, realm_id) 
REFERENCES pspadm.psp_offload_group (offload_group_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk6 FOREIGN KEY (funding_model_fk, realm_id) 
REFERENCES pspadm.psp_funding_model (funding_model_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk7 FOREIGN KEY (annual_billing_batch_fk, realm_id) 
REFERENCES pspadm.psp_annual_billing_batch (annual_billing_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company
ADD CONSTRAINT psp_company_fk8 FOREIGN KEY (compliance_address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_additional_info
ADD CONSTRAINT psp_company_additional_inf_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_additional_info
ADD CONSTRAINT psp_company_additional_inf_fk2 FOREIGN KEY (industry_type_fk, realm_id) 
REFERENCES pspadm.psp_industry_type (industry_type_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_additional_info
ADD CONSTRAINT psp_company_additional_inf_fk3 FOREIGN KEY (ownership_type_fk, realm_id) 
REFERENCES pspadm.psp_ownership_type (ownership_type_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_agency
ADD CONSTRAINT psp_company_agency_fk1 FOREIGN KEY (agency_fk, realm_id) 
REFERENCES pspadm.psp_agency (agency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_agency
ADD CONSTRAINT psp_company_agency_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_bank_account
ADD CONSTRAINT psp_company_bank_account_fk1 FOREIGN KEY (bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_bank_account
ADD CONSTRAINT psp_company_bank_account_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_daily_liability
ADD CONSTRAINT psp_company_daily_liabilit_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_daily_liability
ADD CONSTRAINT psp_company_daily_liabilit_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_event
ADD CONSTRAINT psp_company_event_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_event_detail
ADD CONSTRAINT psp_company_event_detail_fk1 FOREIGN KEY (company_event_fk, realm_id) 
REFERENCES pspadm.psp_company_event (company_event_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_event_detail
ADD CONSTRAINT psp_company_event_detail_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_event_email
ADD CONSTRAINT psp_company_event_email_fk1 FOREIGN KEY (company_event_fk, realm_id) 
REFERENCES pspadm.psp_company_event (company_event_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_event_email_param
ADD CONSTRAINT psp_company_event_email_pa_fk1 FOREIGN KEY (company_event_email_fk, realm_id) 
REFERENCES pspadm.psp_company_event_email (company_event_email_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_event_email_param
ADD CONSTRAINT psp_company_event_email_pa_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_filing_amount
ADD CONSTRAINT psp_companyfilingamount_fk1 FOREIGN KEY (company_agency_pmt_template_fk, realm_id) 
REFERENCES pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_law
ADD CONSTRAINT psp_company_law_fk1 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_law
ADD CONSTRAINT psp_company_law_fk2 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_law
ADD CONSTRAINT psp_company_law_fk4 FOREIGN KEY (additional_company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_law_rate
ADD CONSTRAINT psp_company_law_rate_fk1 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_note
ADD CONSTRAINT psp_company_note_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_note
ADD CONSTRAINT psp_companynote_fk1 FOREIGN KEY (company_event_fk, realm_id) 
REFERENCES pspadm.psp_company_event (company_event_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_offer
ADD CONSTRAINT psp_company_offer_fk1 FOREIGN KEY (offer_fk, realm_id) 
REFERENCES pspadm.psp_offer (offer_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_offer
ADD CONSTRAINT psp_company_offer_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_offering
ADD CONSTRAINT psp_company_offering_fk1 FOREIGN KEY (offering_fk, realm_id) 
REFERENCES pspadm.psp_offering (offering_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_offering
ADD CONSTRAINT psp_company_offering_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_paycheck_batch
ADD CONSTRAINT psp_company_paycheck_batch_fk1 FOREIGN KEY (company_paycheck_batch_seq, realm_id) 
REFERENCES pspadm.psp_check_print_batch (check_print_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_paycheck_batch
ADD CONSTRAINT psp_company_paycheck_batch_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_payroll_item
ADD CONSTRAINT psp_company_payroll_item_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_payroll_item
ADD CONSTRAINT psp_company_payroll_item_fk2 FOREIGN KEY (payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_payroll_item (payroll_item_code, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_payroll_item
ADD CONSTRAINT psp_company_payroll_item_fk4 FOREIGN KEY (additional_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_pin
ADD CONSTRAINT psp_company_pin_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_rate_request
ADD CONSTRAINT psp_company_rate_request_fk1 FOREIGN KEY (agency_rate_request_fk, realm_id) 
REFERENCES pspadm.psp_agency_rate_request (agency_rate_request_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_rate_request
ADD CONSTRAINT psp_company_rate_request_fk2 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_service
ADD CONSTRAINT psp_company_service_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_service
ADD CONSTRAINT psp_company_service_fk2 FOREIGN KEY (service_fk, realm_id) 
REFERENCES pspadm.psp_service (service_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_service
ADD CONSTRAINT psp_company_service_fk3 FOREIGN KEY (funding_model_fk, realm_id) 
REFERENCES pspadm.psp_funding_model (funding_model_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_service_bank_acct
ADD CONSTRAINT psp_company_service_bank_a_fk1 FOREIGN KEY (company_service_fk, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_service_bank_acct
ADD CONSTRAINT psp_company_service_bank_a_fk2 FOREIGN KEY (company_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_company_bank_account (company_bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_service_bank_acct
ADD CONSTRAINT psp_company_service_bank_a_fk3 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_company_tfssubmission
ADD CONSTRAINT psp_company_tfssubmission_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_companyagency_frmtemplate
ADD CONSTRAINT psp_company_agency_form_te_fk1 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_companyagency_frmtemplate
ADD CONSTRAINT psp_company_agency_form_te_fk2 FOREIGN KEY (form_template_fk, realm_id) 
REFERENCES pspadm.psp_form_template (form_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_companyagency_pmttemplate
ADD CONSTRAINT psp_company_agency_payment_fk1 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_companyagency_pmttemplate
ADD CONSTRAINT psp_company_agency_payment_fk2 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_compensation
ADD CONSTRAINT psp_compensation_fk2 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_compensation
ADD CONSTRAINT psp_compensation_fk4 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_contact
ADD CONSTRAINT psp_contact_fk1 FOREIGN KEY (contact_seq, realm_id) 
REFERENCES pspadm.psp_individual (individual_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_contact
ADD CONSTRAINT psp_contact_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ddcompany_service_info
ADD CONSTRAINT psp_ddcompany_service_info_fk1 FOREIGN KEY (ddcompany_service_info_seq, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_deduction
ADD CONSTRAINT psp_deduction_fk2 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_deduction
ADD CONSTRAINT psp_deduction_fk4 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_dep_freq_ledger_operation
ADD CONSTRAINT psp_deposit_frequency_ledg_fk1 FOREIGN KEY (dep_freq_ledger_operation_seq, realm_id) 
REFERENCES pspadm.psp_ledger_operation (ledger_operation_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_deposit_frequency_file_rec
ADD CONSTRAINT psp_deposit_frequency_file_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_deposit_frequency_file_rec
ADD CONSTRAINT psp_depositfrequencyfilere_fk1 FOREIGN KEY (deposit_frequency_file_fk, realm_id) 
REFERENCES pspadm.psp_deposit_frequency_file (deposit_frequency_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_deposit_frequency_req
ADD CONSTRAINT psp_deposit_frequency_requ_fk1 FOREIGN KEY (deposit_frequency_req_seq, realm_id) 
REFERENCES pspadm.psp_payment_requirement (payment_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_dicrfile
ADD CONSTRAINT psp_dicrfile_fk1 FOREIGN KEY (n_a_c_h_a_file_fk, realm_id) 
REFERENCES pspadm.psp_nachafile (nachafile_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_disburse_advice
ADD CONSTRAINT psp_disburse_advice_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_disburse_advice_tax_liab
ADD CONSTRAINT psp_disburse_advice_tax_li_fk1 FOREIGN KEY (disburse_advice_fk, realm_id) 
REFERENCES pspadm.psp_disburse_advice (disburse_advice_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_disburse_advice_tax_liab
ADD CONSTRAINT psp_disburse_advice_tax_li_fk2 FOREIGN KEY (tips_liability_fk, realm_id) 
REFERENCES pspadm.psp_disburse_advice_tax_liab (disburse_advice_tax_liab_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_disburse_advice_tax_liab
ADD CONSTRAINT psp_disburse_advice_tax_li_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_edi_payment_detail
ADD CONSTRAINT psp_edi_payment_detail_fk1 FOREIGN KEY (parent_file_fk, realm_id) 
REFERENCES pspadm.psp_state_edi_tax_file (state_edi_tax_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_edi_payment_detail
ADD CONSTRAINT psp_edi_payment_detail_fk2 FOREIGN KEY (response_file_fk, realm_id) 
REFERENCES pspadm.psp_state_edi_tax_file (state_edi_tax_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ee_payrollitem_qtrtotals
ADD CONSTRAINT psp_employee_payroll_item__fk1 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ee_payrollitem_qtrtotals
ADD CONSTRAINT psp_employee_payroll_item__fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_effective_deposit_freq
ADD CONSTRAINT psp_effective_deposit_freq_fk1 FOREIGN KEY (company_agency_pmt_template_fk, realm_id) 
REFERENCES pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_effective_deposit_freq
ADD CONSTRAINT psp_effective_deposit_freq_fk2 FOREIGN KEY (payment_template_frequency_fk, realm_id) 
REFERENCES pspadm.psp_pmt_template_frequency (payment_template_frequency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_enrollment
ADD CONSTRAINT psp_eftps_enrollment_fk1 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_enrollment_detail
ADD CONSTRAINT psp_eftps_enrollment_detai_fk1 FOREIGN KEY (eftps_enrollment_fk, realm_id) 
REFERENCES pspadm.psp_eftps_enrollment (eftps_enrollment_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_enrollment_detail
ADD CONSTRAINT psp_eftps_enrollment_detai_fk2 FOREIGN KEY (parent_file_fk, realm_id) 
REFERENCES pspadm.psp_eftps_file (eftps_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_enrollment_detail
ADD CONSTRAINT psp_eftps_enrollment_detai_fk3 FOREIGN KEY (response_file_fk, realm_id) 
REFERENCES pspadm.psp_eftps_file (eftps_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_file
ADD CONSTRAINT psp_eftps_file_fk1 FOREIGN KEY (ack_file_fk, realm_id) 
REFERENCES pspadm.psp_eftps_file (eftps_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_file
ADD CONSTRAINT psp_eftps_file_fk2 FOREIGN KEY (eftps_file_seq, realm_id) 
REFERENCES pspadm.psp_edi_tax_file (edi_tax_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_payment_detail
ADD CONSTRAINT psp_eftps_payment_detail_fk1 FOREIGN KEY (parent_file_fk, realm_id) 
REFERENCES pspadm.psp_eftps_file (eftps_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_payment_detail
ADD CONSTRAINT psp_eftps_payment_detail_fk2 FOREIGN KEY (return_file_fk, realm_id) 
REFERENCES pspadm.psp_eftps_file (eftps_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_eftps_payment_detail
ADD CONSTRAINT psp_eftps_payment_detail_fk3 FOREIGN KEY (response_file_fk, realm_id) 
REFERENCES pspadm.psp_eftps_file (eftps_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_emp_totals_payroll_run
ADD CONSTRAINT psp_emp_totals_payroll_run_fk1 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_emp_totals_payroll_run
ADD CONSTRAINT psp_emp_totals_payroll_run_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee
ADD CONSTRAINT psp_employee_fk1 FOREIGN KEY (employee_seq, realm_id) 
REFERENCES pspadm.psp_individual (individual_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee
ADD CONSTRAINT psp_employee_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_accrual
ADD CONSTRAINT psp_employee_accrual_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_bank_account
ADD CONSTRAINT psp_employee_bank_account_fk1 FOREIGN KEY (bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_bank_account
ADD CONSTRAINT psp_employee_bank_account_fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_custom_field
ADD CONSTRAINT psp_employee_custom_field_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_law_qtr_totals
ADD CONSTRAINT psp_employee_law_qtr_total_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_law_qtr_totals
ADD CONSTRAINT psp_employee_law_qtr_total_fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_law_qtr_totals
ADD CONSTRAINT psp_employee_law_qtr_total_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_law_qtr_totals
ADD CONSTRAINT psp_employee_law_qtr_total_fk6 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_payroll_item
ADD CONSTRAINT psp_employee_payroll_item_fk1 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_payroll_item
ADD CONSTRAINT psp_employee_payroll_item_fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_tax
ADD CONSTRAINT psp_employee_tax_fk1 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_tax
ADD CONSTRAINT psp_employee_tax_fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_usage
ADD CONSTRAINT psp_employeeusage_fk1 FOREIGN KEY (usage_period_fk, realm_id) 
REFERENCES pspadm.psp_usage_period (usage_period_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_w2_totals
ADD CONSTRAINT psp_employee_w2_totals_fk1 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_w2_totals
ADD CONSTRAINT psp_employee_w2_totals_fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_w2_totals
ADD CONSTRAINT psp_employee_w2_totals_fk3 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_w2_totals
ADD CONSTRAINT psp_employee_w2_totals_fk6 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employee_wage_plan
ADD CONSTRAINT psp_employee_wage_plan_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employer_contribution
ADD CONSTRAINT psp_employer_contribution_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employer_contribution
ADD CONSTRAINT psp_employercontribution_fk1 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_employer_preference
ADD CONSTRAINT psp_employer_preference_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entitlement
ADD CONSTRAINT psp_entitlement_fk1 FOREIGN KEY (entitlement_code_fk, realm_id) 
REFERENCES pspadm.psp_entitlement_code (entitlement_code_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entitlement_code_offering
ADD CONSTRAINT psp_entitlement_code_offer_fk1 FOREIGN KEY (entitlement_code_fk, realm_id) 
REFERENCES pspadm.psp_entitlement_code (entitlement_code_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entitlement_code_offering
ADD CONSTRAINT psp_entitlement_code_offer_fk2 FOREIGN KEY (offering_fk, realm_id) 
REFERENCES pspadm.psp_offering (offering_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entitlement_unit
ADD CONSTRAINT psp_entitlement_unit_fk1 FOREIGN KEY (entitlement_fk, realm_id) 
REFERENCES pspadm.psp_entitlement (entitlement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entitlement_unit
ADD CONSTRAINT psp_entitlementunit_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entity_change
ADD CONSTRAINT psp_entitychange_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entry_detail_record
ADD CONSTRAINT psp_entry_detail_record_fk1 FOREIGN KEY (intuit_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_intuit_bank_account (intuit_bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entry_detail_record
ADD CONSTRAINT psp_entry_detail_record_fk2 FOREIGN KEY (n_a_c_h_a_file_fk, realm_id) 
REFERENCES pspadm.psp_nachafile (nachafile_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_entry_detail_record
ADD CONSTRAINT psp_entry_detail_record_fk4 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_event_as400_sync
ADD CONSTRAINT psp_event_as400_sync_fk1 FOREIGN KEY (company_event_fk, realm_id) 
REFERENCES pspadm.psp_company_event (company_event_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_evttp_srcsys_assoc
ADD CONSTRAINT psp_evttp_srcsys_fk_evttp FOREIGN KEY (interesting_event_types_fk, realm_id) 
REFERENCES pspadm.psp_event_type (event_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_evttp_srcsys_assoc
ADD CONSTRAINT psp_evttp_srcsys_fk_srcsys FOREIGN KEY (source_system_fk, realm_id) 
REFERENCES pspadm.psp_source_system (source_system_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_failed_payroll_run
ADD CONSTRAINT psp_failed_payroll_run_fk1 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fee
ADD CONSTRAINT psp_fee_fk1 FOREIGN KEY (source_system_fk, realm_id) 
REFERENCES pspadm.psp_source_system (source_system_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fee
ADD CONSTRAINT psp_fee_fk2 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT psp_financial_transaction__fk4 FOREIGN KEY (gems_upload_batch_fk, realm_id) 
REFERENCES pspadm.psp_gems_upload_batch (gems_upload_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT psp_financial_transaction__fk6 FOREIGN KEY (transaction_state_fk, realm_id) 
REFERENCES pspadm.psp_transaction_state (transaction_state_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT psp_financial_transaction__fk7 FOREIGN KEY (transaction_response_fk, realm_id) 
REFERENCES pspadm.psp_transaction_response (transaction_response_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT psp_financialtransactionst_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT psp_financialtransactionst_fk2 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk11 FOREIGN KEY (comp_adjust_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk13 FOREIGN KEY (bill_payment_split_fk, realm_id) 
REFERENCES pspadm.psp_bill_payment_split (bill_payment_split_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk14 FOREIGN KEY (tax_penalty_interest_fk, realm_id) 
REFERENCES pspadm.psp_tax_penalty_interest (tax_penalty_interest_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk16 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk2 FOREIGN KEY (credit_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk3 FOREIGN KEY (debit_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk4 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk5 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk7 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk8 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financial_transaction_fk9 FOREIGN KEY (current_transaction_state_fk, realm_id) 
REFERENCES pspadm.psp_transaction_state (transaction_state_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT psp_financialtransaction_fk1 FOREIGN KEY (billing_detail_fk, realm_id) 
REFERENCES pspadm.psp_billing_detail (billing_detail_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_txn_action
ADD CONSTRAINT psp_financial_transaction__fk1 FOREIGN KEY (action_event_fk, realm_id) 
REFERENCES pspadm.psp_action_event (code, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_txn_action
ADD CONSTRAINT psp_financial_transaction__fk2 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_financial_txn_action
ADD CONSTRAINT psp_financial_transaction__fk3 FOREIGN KEY (transaction_state_fk, realm_id) 
REFERENCES pspadm.psp_transaction_state (transaction_state_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_forecast_detail
ADD CONSTRAINT psp_forecastdetail_fk1 FOREIGN KEY (forecast_fk, realm_id) 
REFERENCES pspadm.psp_forecast (forecast_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_form_template
ADD CONSTRAINT psp_form_template_fk1 FOREIGN KEY (agency_fk, realm_id) 
REFERENCES pspadm.psp_agency (agency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_form_template
ADD CONSTRAINT psp_form_template_fk2 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_address
ADD CONSTRAINT psp_fraudaddress_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_bank_account
ADD CONSTRAINT psp_fraudbankaccount_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_company
ADD CONSTRAINT psp_fraudcompany_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_contact
ADD CONSTRAINT psp_fraudcontact_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_event
ADD CONSTRAINT psp_fraud_event_fk1 FOREIGN KEY (company_event_fk, realm_id) 
REFERENCES pspadm.psp_company_event (company_event_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_event
ADD CONSTRAINT psp_fraud_event_fk2 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_event
ADD CONSTRAINT psp_fraud_event_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_event
ADD CONSTRAINT psp_fraud_event_fk4 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fraud_value
ADD CONSTRAINT psp_fraudvalue_fk1 FOREIGN KEY (fraud_rule_fk, realm_id) 
REFERENCES pspadm.psp_fraud_rule (fraud_rule_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fset_filing_detail
ADD CONSTRAINT psp_fset_filing_detail_fk1 FOREIGN KEY (response_file_fk, realm_id) 
REFERENCES pspadm.psp_fset_file (fset_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_fset_filing_detail
ADD CONSTRAINT psp_fset_filing_detail_fk2 FOREIGN KEY (parent_file_fk, realm_id) 
REFERENCES pspadm.psp_fset_file (fset_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_gems_ledger_posting_rule
ADD CONSTRAINT psp_gems_ledger_posting_ru_fk1 FOREIGN KEY (ledger_account_fk, realm_id) 
REFERENCES pspadm.psp_ledger_account (ledger_account_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_gems_monthly_balance
ADD CONSTRAINT psp_gems_monthly_balance_fk1 FOREIGN KEY (gems_ledger_posting_rule_fk, realm_id) 
REFERENCES pspadm.psp_gems_ledger_posting_rule (gems_ledger_posting_rule_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_gems_monthly_balance
ADD CONSTRAINT psp_gems_monthly_balance_fk2 FOREIGN KEY (gems_upload_batch_fk, realm_id) 
REFERENCES pspadm.psp_gems_upload_batch (gems_upload_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_hours_worked_exception
ADD CONSTRAINT psp_hours_worked_exception_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_individual
ADD CONSTRAINT psp_individual_fk1 FOREIGN KEY (mailing_address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_intuit_ba_bt_ft
ADD CONSTRAINT psp_intuit_babatch_type_fi_fk1 FOREIGN KEY (intuit_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_intuit_bank_account (intuit_bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_intuit_bank_acc_txn_type
ADD CONSTRAINT psp_intuit_bank_account_tr_fk1 FOREIGN KEY (intuit_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_intuit_bank_account (intuit_bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_intuit_bank_acc_txn_type
ADD CONSTRAINT psp_intuit_bank_account_tr_fk2 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_intuit_bank_account
ADD CONSTRAINT psp_intuit_bank_account_fk1 FOREIGN KEY (bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_intuit_shipper_info
ADD CONSTRAINT psp_intuit_shipper_info_fk1 FOREIGN KEY (shipper_address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_law
ADD CONSTRAINT psp_law_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_law_rate_range
ADD CONSTRAINT psp_law_rate_range_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_law_rate_value
ADD CONSTRAINT psp_law_rate_value_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ledger_account_action
ADD CONSTRAINT psp_ledger_account_action_fk1 FOREIGN KEY (action_event_fk, realm_id) 
REFERENCES pspadm.psp_action_event (code, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ledger_account_action
ADD CONSTRAINT psp_ledger_account_action_fk2 FOREIGN KEY (ledger_account_fk, realm_id) 
REFERENCES pspadm.psp_ledger_account (ledger_account_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ledger_balance
ADD CONSTRAINT psp_ledgerbalance_fk1 FOREIGN KEY (ledger_account_fk, realm_id) 
REFERENCES pspadm.psp_ledger_account (ledger_account_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ledger_balance
ADD CONSTRAINT psp_ledgerbalance_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ledger_operation
ADD CONSTRAINT psp_ledger_operation_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_ledger_operation
ADD CONSTRAINT psp_ledger_operation_fk2 FOREIGN KEY (ledger_operation_job_fk, realm_id) 
REFERENCES pspadm.psp_ledger_operation_job (ledger_operation_job_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liab_check_billing_assoc
ADD CONSTRAINT psp_liability_check_billin_fk1 FOREIGN KEY (billing_detail_fk, realm_id) 
REFERENCES pspadm.psp_billing_detail (billing_detail_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liab_check_billing_assoc
ADD CONSTRAINT psp_liability_check_billin_fk2 FOREIGN KEY (liability_check_fk, realm_id) 
REFERENCES pspadm.psp_liability_check (liability_check_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_adjustment
ADD CONSTRAINT psp_liability_adjustment_fk1 FOREIGN KEY (comp_adjust_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_adjustment
ADD CONSTRAINT psp_liability_adjustment_fk2 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_adjustment
ADD CONSTRAINT psp_liability_adjustment_fk3 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_adjustment
ADD CONSTRAINT psp_liability_adjustment_fk4 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_adjustment
ADD CONSTRAINT psp_liability_adjustment_fk6 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_adjustment
ADD CONSTRAINT psp_liability_adjustment_fk7 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_check
ADD CONSTRAINT psp_liability_check_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_check
ADD CONSTRAINT psp_liability_check_fk2 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_check_line
ADD CONSTRAINT psp_liability_check_line_fk1 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_check_line
ADD CONSTRAINT psp_liability_check_line_fk2 FOREIGN KEY (liability_check_fk, realm_id) 
REFERENCES pspadm.psp_liability_check (liability_check_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_liability_check_line
ADD CONSTRAINT psp_liability_check_line_fk4 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_limit_value
ADD CONSTRAINT psp_limit_value_fk1 FOREIGN KEY (limit_rule_fk, realm_id) 
REFERENCES pspadm.psp_limit_rule (limit_rule_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_manual_requirement
ADD CONSTRAINT psp_manual_requirement_fk1 FOREIGN KEY (manual_requirement_seq, realm_id) 
REFERENCES pspadm.psp_payment_method_requirement (payment_method_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_money_movement_transaction
ADD CONSTRAINT psp_money_movement_transac_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_money_movement_transaction
ADD CONSTRAINT psp_money_movement_transac_fk3 FOREIGN KEY (offload_batch_fk, realm_id) 
REFERENCES pspadm.psp_offload_batch (offload_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_money_movement_transaction
ADD CONSTRAINT psp_money_movement_transac_fk5 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_money_movement_transaction
ADD CONSTRAINT psp_money_movement_transac_fk6 FOREIGN KEY (payment_frequency_fk, realm_id) 
REFERENCES pspadm.psp_pmt_template_frequency (payment_template_frequency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_nachafile
ADD CONSTRAINT psp_nachafile_fk1 FOREIGN KEY (offload_batch_fk, realm_id) 
REFERENCES pspadm.psp_offload_batch (offload_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offer_price
ADD CONSTRAINT psp_offer_price_fk1 FOREIGN KEY (offer_fk, realm_id) 
REFERENCES pspadm.psp_offer (offer_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offer_svcchg_assoc
ADD CONSTRAINT psp_offer_svcchg_fk_offer FOREIGN KEY (offer_fk, realm_id) 
REFERENCES pspadm.psp_offer (offer_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offer_svcchg_assoc
ADD CONSTRAINT psp_offer_svcchg_fk_svcchg FOREIGN KEY (offering_service_charge_fk, realm_id) 
REFERENCES pspadm.psp_offering_svcchg (offering_svcchg_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offering
ADD CONSTRAINT psp_offering_fk1 FOREIGN KEY (limit_rule_fk, realm_id) 
REFERENCES pspadm.psp_limit_rule (limit_rule_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offering
ADD CONSTRAINT psp_offering_fk2 FOREIGN KEY (fraud_rule_fk, realm_id) 
REFERENCES pspadm.psp_fraud_rule (fraud_rule_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offering_svcchg
ADD CONSTRAINT psp_offering_svcchg_fk FOREIGN KEY (offering_svcchg_grp_fk, realm_id) 
REFERENCES pspadm.psp_offering_svcchg_grp (offering_svcchg_grp_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offering_svcchg_grp
ADD CONSTRAINT psp_offering_svcchg_grp_fk FOREIGN KEY (offering_fk, realm_id) 
REFERENCES pspadm.psp_offering (offering_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_offload_batch
ADD CONSTRAINT psp_offload_batch_fk1 FOREIGN KEY (offload_group_fk, realm_id) 
REFERENCES pspadm.psp_offload_group (offload_group_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_on_hold_reason
ADD CONSTRAINT psp_on_hold_reason_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pay_item
ADD CONSTRAINT psp_pay_item_fk1 FOREIGN KEY (liability_adjustment_fk, realm_id) 
REFERENCES pspadm.psp_liability_adjustment (liability_adjustment_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pay_item
ADD CONSTRAINT psp_pay_item_fk2 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT psp_paycheck_fk1 FOREIGN KEY (d_d_employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT psp_paycheck_fk2 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT psp_paycheck_fk3 FOREIGN KEY (comp_adjust_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT psp_paycheck_fk4 FOREIGN KEY (source_employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT psp_paycheck_fk5 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_split
ADD CONSTRAINT psp_paycheck_split_fk1 FOREIGN KEY (employee_bank_account_fk, realm_id) 
REFERENCES pspadm.psp_employee_bank_account (employee_bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_split
ADD CONSTRAINT psp_paycheck_split_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_usage
ADD CONSTRAINT psp_paycheck_usage_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_usage
ADD CONSTRAINT psp_paycheckusage_fk1 FOREIGN KEY (bill_fk, realm_id) 
REFERENCES pspadm.psp_bill (bill_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_usage
ADD CONSTRAINT psp_paycheckusage_fk2 FOREIGN KEY (employee_usage_fk, realm_id) 
REFERENCES pspadm.psp_employee_usage (employee_usage_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_usage_hist
ADD CONSTRAINT psp_paycheck_usage_hist_fk1 FOREIGN KEY (employee_usage_fk, realm_id) 
REFERENCES pspadm.psp_employee_usage (employee_usage_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paycheck_usage_hist
ADD CONSTRAINT psp_paycheck_usage_hist_fk2 FOREIGN KEY (paycheck_usage_fk, realm_id) 
REFERENCES pspadm.psp_paycheck_usage (paycheck_usage_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payee
ADD CONSTRAINT psp_payee_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payee
ADD CONSTRAINT psp_payee_fk2 FOREIGN KEY (mailing_address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payee_bank_account
ADD CONSTRAINT psp_payee_bank_account_fk1 FOREIGN KEY (payee_fk, realm_id) 
REFERENCES pspadm.psp_payee (payee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payee_bank_account
ADD CONSTRAINT psp_payee_bank_account_fk2 FOREIGN KEY (bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payment_batch_assoc
ADD CONSTRAINT psp_payment_batch_assoc_fk1 FOREIGN KEY (agency_check_batch_fk, realm_id) 
REFERENCES pspadm.psp_agency_check_batch (agency_check_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payment_method_requirement
ADD CONSTRAINT psp_paymentmethodrequireme_fk1 FOREIGN KEY (pmt_template_pmt_method_fk, realm_id) 
REFERENCES pspadm.psp_pmt_template_paymentmethod (pmt_template_paymentmethod_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payment_requirement
ADD CONSTRAINT psp_payment_requirement_fk1 FOREIGN KEY (payment_requirement_seq, realm_id) 
REFERENCES pspadm.psp_payment_method_requirement (payment_method_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payment_template
ADD CONSTRAINT psp_payment_template_fk1 FOREIGN KEY (agency_fk, realm_id) 
REFERENCES pspadm.psp_agency (agency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payment_template_agency_id
ADD CONSTRAINT psp_payment_template_agenc_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payroll_item_taxable_to
ADD CONSTRAINT psp_payroll_item_taxable_t_fk1 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payroll_item_taxable_to
ADD CONSTRAINT psp_payroll_item_taxable_t_fk2 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT psp_payroll_run_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payroll_run_action
ADD CONSTRAINT psp_payroll_run_action_fk1 FOREIGN KEY (action_event_fk, realm_id) 
REFERENCES pspadm.psp_action_event (code, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_payroll_subtype
ADD CONSTRAINT psp_payroll_subtype_fk1 FOREIGN KEY (offering_fk, realm_id) 
REFERENCES pspadm.psp_offering (offering_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paystub
ADD CONSTRAINT psp_paystub_fk1 FOREIGN KEY (pstub_employer_info_fk, realm_id) 
REFERENCES pspadm.psp_pstub_employer_info (pstub_employer_info_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_paystub
ADD CONSTRAINT psp_paystub_fk2 FOREIGN KEY (pstub_employee_info_fk, realm_id) 
REFERENCES pspadm.psp_pstub_employee_info (pstub_employee_info_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmt_template_bankaccount
ADD CONSTRAINT psp_payment_template_bank__fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmt_template_bankaccount
ADD CONSTRAINT psp_payment_template_bank__fk2 FOREIGN KEY (bank_account_fk, realm_id) 
REFERENCES pspadm.psp_bank_account (bank_account_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmt_template_frequency
ADD CONSTRAINT psp_payment_template_frequ_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmt_template_paymentmethod
ADD CONSTRAINT psp_payment_template_payme_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmttemplate_chkinfo_assoc
ADD CONSTRAINT psp_payment_template_check_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmttemplate_chkinfo_assoc
ADD CONSTRAINT psp_payment_template_check_fk2 FOREIGN KEY (pmttemplate_printedchkinfo_fk, realm_id) 
REFERENCES pspadm.psp_pmttemplate_printedchkinfo (pmttemplate_printedchkinfo_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pmttemplate_printedchkinfo
ADD CONSTRAINT psp_payment_template_print_fk1 FOREIGN KEY (address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_posting_rule
ADD CONSTRAINT psp_posting_rule_fk1 FOREIGN KEY (ledger_account_fk, realm_id) 
REFERENCES pspadm.psp_ledger_account (ledger_account_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_posting_rule
ADD CONSTRAINT psp_posting_rule_fk2 FOREIGN KEY (transaction_state_fk, realm_id) 
REFERENCES pspadm.psp_transaction_state (transaction_state_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_posting_rule
ADD CONSTRAINT psp_posting_rule_fk3 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_prior_payment_submission
ADD CONSTRAINT psp_prior_payment_submissi_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_property_audit
ADD CONSTRAINT psp_propertyaudit_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_dditem
ADD CONSTRAINT psp_pstub_dditem_fk1 FOREIGN KEY (paystub_fk, realm_id) 
REFERENCES pspadm.psp_paystub (paystub_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_employee_info
ADD CONSTRAINT psp_pstub_employee_info_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_employee_info
ADD CONSTRAINT psp_pstub_employee_info_fk2 FOREIGN KEY (pstub_address_fk, realm_id) 
REFERENCES pspadm.psp_pstub_address (pstub_address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_employee_preference
ADD CONSTRAINT psp_pstub_employee_prefere_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_employer_info
ADD CONSTRAINT psp_pstub_employer_info_fk1 FOREIGN KEY (pstub_address_fk, realm_id) 
REFERENCES pspadm.psp_pstub_address (pstub_address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_msg
ADD CONSTRAINT psp_pstub_msg_fk1 FOREIGN KEY (paystub_fk, realm_id) 
REFERENCES pspadm.psp_paystub (paystub_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_paid_timeoff_item
ADD CONSTRAINT psp_pstub_paid_timeoff_ite_fk1 FOREIGN KEY (paystub_fk, realm_id) 
REFERENCES pspadm.psp_paystub (paystub_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_pay_item
ADD CONSTRAINT psp_pstub_pay_item_fk1 FOREIGN KEY (paystub_fk, realm_id) 
REFERENCES pspadm.psp_paystub (paystub_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_pay_item
ADD CONSTRAINT psp_pstub_pay_item_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_pstub_state_tax_info
ADD CONSTRAINT psp_pstub_state_tax_info_fk1 FOREIGN KEY (pstub_employer_info_fk, realm_id) 
REFERENCES pspadm.psp_pstub_employer_info (pstub_employer_info_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_employee_info
ADD CONSTRAINT psp_qbdt_employee_info_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_employee_info
ADD CONSTRAINT psp_qbdt_employee_info_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_paycheck_info
ADD CONSTRAINT psp_qbdt_paycheck_info_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payline_info
ADD CONSTRAINT psp_qbdt_payline_info_fk1 FOREIGN KEY (employer_contribution_fk, realm_id) 
REFERENCES pspadm.psp_employer_contribution (employer_contribution_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payline_info
ADD CONSTRAINT psp_qbdt_payline_info_fk2 FOREIGN KEY (compensation_fk, realm_id) 
REFERENCES pspadm.psp_compensation (compensation_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payline_info
ADD CONSTRAINT psp_qbdt_payline_info_fk3 FOREIGN KEY (deduction_fk, realm_id) 
REFERENCES pspadm.psp_deduction (deduction_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payline_info
ADD CONSTRAINT psp_qbdt_payline_info_fk4 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD CONSTRAINT psp_qbdt_payroll_item_info_fk1 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD CONSTRAINT psp_qbdt_payroll_item_info_fk2 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD CONSTRAINT psp_qbdt_payroll_item_info_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_trans_line
ADD CONSTRAINT psp_qbdt_payroll_transacti_fk2 FOREIGN KEY (qbdt_payroll_transaction_fk, realm_id) 
REFERENCES pspadm.psp_qbdt_payroll_transaction (qbdt_payroll_transaction_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_trans_line
ADD CONSTRAINT psp_qbdt_payroll_transline_fk1 FOREIGN KEY (company_payroll_item_fk, realm_id) 
REFERENCES pspadm.psp_company_payroll_item (company_payroll_item_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD CONSTRAINT psp_qbdt_payroll_trans_fk1 FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD CONSTRAINT psp_qbdt_payroll_trans_fk2 FOREIGN KEY (prior_payment_submission_fk, realm_id) 
REFERENCES pspadm.psp_prior_payment_submission (prior_payment_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD CONSTRAINT psp_qbdt_payroll_transacti_fk1 FOREIGN KEY (comp_adjust_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD CONSTRAINT psp_qbdt_payroll_transacti_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk1 FOREIGN KEY (liability_check_fk, realm_id) 
REFERENCES pspadm.psp_liability_check (liability_check_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk2 FOREIGN KEY (liability_check_line_fk, realm_id) 
REFERENCES pspadm.psp_liability_check_line (liability_check_line_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk5 FOREIGN KEY (comp_adjust_submission_fk, realm_id) 
REFERENCES pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk6 FOREIGN KEY (liability_adjustment_fk, realm_id) 
REFERENCES pspadm.psp_liability_adjustment (liability_adjustment_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk7 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk8 FOREIGN KEY (qbdt_payroll_transaction_fk, realm_id) 
REFERENCES pspadm.psp_qbdt_payroll_transaction (qbdt_payroll_transaction_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdt_transaction_info_fk9 FOREIGN KEY (qbdt_payroll_trans_line_fk, realm_id) 
REFERENCES pspadm.psp_qbdt_payroll_trans_line (qbdt_payroll_trans_line_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD CONSTRAINT psp_qbdttransactioninfo_fk1 FOREIGN KEY (prior_payment_submission_fk, realm_id) 
REFERENCES pspadm.psp_prior_payment_submission (prior_payment_submission_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_qbdt_unprocessed_request
ADD CONSTRAINT psp_qbdt_unprocessed_reque_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_quickbooks_info
ADD CONSTRAINT psp_quickbooks_info_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_racompany_service_info
ADD CONSTRAINT psp_racompany_service_info_fk1 FOREIGN KEY (racompany_service_info_seq, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_rafenrollment
ADD CONSTRAINT psp_rafenrollment_fk1 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_rafenrollment_detail
ADD CONSTRAINT psp_rafenrollment_detail_fk1 FOREIGN KEY (r_a_f_enrollment_fk, realm_id) 
REFERENCES pspadm.psp_rafenrollment (rafenrollment_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_rafenrollment_detail
ADD CONSTRAINT psp_rafenrollment_detail_fk2 FOREIGN KEY (enrollment_file_fk, realm_id) 
REFERENCES pspadm.psp_rafenrollment_file (rafenrollment_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_rafenrollment_detail
ADD CONSTRAINT psp_rafenrollment_detail_fk3 FOREIGN KEY (delete_file_fk, realm_id) 
REFERENCES pspadm.psp_rafenrollment_file (rafenrollment_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_rate_ledger_operation
ADD CONSTRAINT psp_rate_ledger_operation_fk1 FOREIGN KEY (rate_ledger_operation_seq, realm_id) 
REFERENCES pspadm.psp_ledger_operation (ledger_operation_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_reporting_agent
ADD CONSTRAINT psp_reporting_agent_fk1 FOREIGN KEY (address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_role_sub_status
ADD CONSTRAINT psp_role_sub_status_fk1 FOREIGN KEY (auth_role_fk, realm_id) 
REFERENCES pspadm.psp_auth_role (auth_role_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_role_sub_status
ADD CONSTRAINT psp_role_sub_status_fk2 FOREIGN KEY (service_sub_status_fk, realm_id) 
REFERENCES pspadm.psp_service_sub_status (service_sub_status_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_second_offload
ADD CONSTRAINT psp_second_offload_fk1 FOREIGN KEY (offload_group_fk, realm_id) 
REFERENCES pspadm.psp_offload_group (offload_group_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_serv_stat_txn_sku_type
ADD CONSTRAINT psp_serv_stat_txn_sku_type_fk1 FOREIGN KEY (service_sub_status_fk, realm_id) 
REFERENCES pspadm.psp_service_sub_status (service_sub_status_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_serv_stat_txn_sku_type
ADD CONSTRAINT psp_serv_stat_txn_sku_type_fk2 FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_service_sub_status
ADD CONSTRAINT psp_service_sub_status_fk1 FOREIGN KEY (service_status_fk, realm_id) 
REFERENCES pspadm.psp_service_status (service_status_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_smsmigration
ADD CONSTRAINT psp_smsmigration_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_source_system_law_assoc
ADD CONSTRAINT psp_source_system_law_asso_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_source_system_law_assoc
ADD CONSTRAINT psp_source_system_law_asso_fk2 FOREIGN KEY (source_system_fk, realm_id) 
REFERENCES pspadm.psp_source_system (source_system_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_source_system_transmission
ADD CONSTRAINT psp_source_system_transmis_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_sourcesys_printedchk_info
ADD CONSTRAINT psp_source_system_printed__fk1 FOREIGN KEY (address_fk, realm_id) 
REFERENCES pspadm.psp_address (address_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_state_edi_tax_file
ADD CONSTRAINT psp_state_edi_tax_file_fk1 FOREIGN KEY (state_edi_tax_file_seq, realm_id) 
REFERENCES pspadm.psp_edi_tax_file (edi_tax_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_state_edi_tax_file
ADD CONSTRAINT psp_state_edi_tax_file_fk2 FOREIGN KEY (ack_file_fk, realm_id) 
REFERENCES pspadm.psp_state_edi_tax_file (state_edi_tax_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_state_report_assoc
ADD CONSTRAINT psp_strpt_tmpfreq_fk_pmttmpfrq FOREIGN KEY (payment_template_frequency_fk, realm_id) 
REFERENCES pspadm.psp_pmt_template_frequency (payment_template_frequency_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_state_report_assoc
ADD CONSTRAINT psp_strpt_tmpfreq_fk_strptout FOREIGN KEY (state_report_output_fk, realm_id) 
REFERENCES pspadm.psp_state_report_output (state_report_output_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_suicredits_job
ADD CONSTRAINT psp_suicredits_job_fk1 FOREIGN KEY (payment_template_fk, realm_id) 
REFERENCES pspadm.psp_payment_template (payment_template_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcchgprice
ADD CONSTRAINT psp_offeringservicechargep_fk1 FOREIGN KEY (offering_service_charge_fk, realm_id) 
REFERENCES pspadm.psp_offering_svcchg (offering_svcchg_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcstat_srcsys_assoc
ADD CONSTRAINT psp_svcstat_srcsys_fk_srcsys FOREIGN KEY (source_system_fk, realm_id) 
REFERENCES pspadm.psp_source_system (source_system_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcstat_srcsys_assoc
ADD CONSTRAINT psp_svcstat_srcsys_fk_svcstat FOREIGN KEY (service_sub_status_fk, realm_id) 
REFERENCES pspadm.psp_service_sub_status (service_sub_status_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcstat_svc_assoc
ADD CONSTRAINT psp_svcstat_svc_fk_svc FOREIGN KEY (service_fk, realm_id) 
REFERENCES pspadm.psp_service (service_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcstat_svc_assoc
ADD CONSTRAINT psp_svcstat_svc_fk_svcstat FOREIGN KEY (service_sub_status_fk, realm_id) 
REFERENCES pspadm.psp_service_sub_status (service_sub_status_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcstat_syscap_assoc
ADD CONSTRAINT psp_svcstat_cap_fk_cap FOREIGN KEY (system_capability_fk, realm_id) 
REFERENCES pspadm.psp_system_capability (system_capability_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_svcstat_syscap_assoc
ADD CONSTRAINT psp_svcstat_cap_fk_svcstat FOREIGN KEY (service_sub_status_fk, realm_id) 
REFERENCES pspadm.psp_service_sub_status (service_sub_status_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_system_payment_requirement
ADD CONSTRAINT psp_system_payment_require_fk1 FOREIGN KEY (system_payment_requirement_seq, realm_id) 
REFERENCES pspadm.psp_payment_requirement (payment_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_system_requirement
ADD CONSTRAINT psp_system_requirement_fk1 FOREIGN KEY (system_requirement_seq, realm_id) 
REFERENCES pspadm.psp_payment_method_requirement (payment_method_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax
ADD CONSTRAINT psp_tax_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax
ADD CONSTRAINT psp_tax_fk3 FOREIGN KEY (company_law_fk, realm_id) 
REFERENCES pspadm.psp_company_law (company_law_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax
ADD CONSTRAINT psp_tax_fk5 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax_company_service_info
ADD CONSTRAINT psp_tax_company_service_in_fk1 FOREIGN KEY (tax_company_service_info_seq, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax_credits9061
ADD CONSTRAINT psp_tax_credits9061_fk1 FOREIGN KEY (tax_credits_application_fk, realm_id) 
REFERENCES pspadm.psp_tax_credits_application (tax_credits_application_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax_penalty_interest
ADD CONSTRAINT psp_tax_penalty_interest_fk1 FOREIGN KEY (company_agency_fk, realm_id) 
REFERENCES pspadm.psp_company_agency (company_agency_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax_table_misc_data
ADD CONSTRAINT psp_tax_table_misc_data_fk1 FOREIGN KEY (employee_tax_fk, realm_id) 
REFERENCES pspadm.psp_employee_tax (employee_tax_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tax_table_misc_data
ADD CONSTRAINT psp_tax_table_misc_data_fk2 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_threshold_requirement
ADD CONSTRAINT psp_threshold_requirement_fk1 FOREIGN KEY (threshold_requirement_seq, realm_id) 
REFERENCES pspadm.psp_payment_requirement (payment_requirement_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tp401k_batch_employee
ADD CONSTRAINT "PSP_TP401K_BATCH_EE_FK2 " FOREIGN KEY (employee_fk, realm_id) 
REFERENCES pspadm.psp_employee (employee_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tp401k_batch_employee
ADD CONSTRAINT psp_tp401k_batch_ee_fk1 FOREIGN KEY (third_party401k_batch_fk, realm_id) 
REFERENCES pspadm.psp_third_party401k_batch (third_party401k_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tp401k_batch_paycheck
ADD CONSTRAINT psp_third_party401k_batch__fk1 FOREIGN KEY (third_party401k_batch_fk, realm_id) 
REFERENCES pspadm.psp_third_party401k_batch (third_party401k_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tp401k_paycheck_pending
ADD CONSTRAINT psp_tp401k_pend_state_pchk_fk FOREIGN KEY (third_party401k_paycheck_fk, realm_id) 
REFERENCES pspadm.psp_tp401k_paycheck (tp401k_paycheck_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tp401k_paycheck_state
ADD CONSTRAINT psp_tp401k_pchk_state_pchk_fk FOREIGN KEY (third_party401k_paycheck_fk, realm_id) 
REFERENCES pspadm.psp_tp401k_paycheck (tp401k_paycheck_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_tp401kcompany_service_info
ADD CONSTRAINT psp_third_party401k_compan_fk1 FOREIGN KEY (tp401kcompany_service_info_seq, realm_id) 
REFERENCES pspadm.psp_company_service (company_service_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_transaction_offload_batch
ADD CONSTRAINT psp_transaction_offload_ba_fk2 FOREIGN KEY (offload_batch_fk, realm_id) 
REFERENCES pspadm.psp_offload_batch (offload_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_transaction_response
ADD CONSTRAINT psp_transaction_response_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_transaction_return
ADD CONSTRAINT psp_transaction_return_fk2 FOREIGN KEY (return_batch_fk, realm_id) 
REFERENCES pspadm.psp_transaction_return_batch (transaction_return_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_transaction_return
ADD CONSTRAINT psp_transaction_return_fk3 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_transmission_payroll_run
ADD CONSTRAINT psp_transmissionpayrollrun_fk2 FOREIGN KEY (payroll_run_fk, realm_id) 
REFERENCES pspadm.psp_payroll_run (payroll_run_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_txntype_service_assoc
ADD CONSTRAINT psp_txntype_svc_fk_svc FOREIGN KEY (service_fk, realm_id) 
REFERENCES pspadm.psp_service (service_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_txntype_service_assoc
ADD CONSTRAINT psp_txntype_svc_fk_txntype FOREIGN KEY (transaction_type_fk, realm_id) 
REFERENCES pspadm.psp_transaction_type (transaction_type_cd, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_usage_period
ADD CONSTRAINT psp_usageperiod_fk1 FOREIGN KEY (company_usage_fk, realm_id) 
REFERENCES pspadm.psp_company_usage (company_usage_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_user_setting
ADD CONSTRAINT psp_user_setting_fk1 FOREIGN KEY (user_preference_fk, realm_id) 
REFERENCES pspadm.psp_user_preference (key, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_user_setting
ADD CONSTRAINT psp_usersetting_fk1 FOREIGN KEY (auth_user_fk, realm_id) 
REFERENCES pspadm.psp_auth_user (auth_user_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_vmp_employee_info
ADD CONSTRAINT psp_vmp_employee_info_fk1 FOREIGN KEY (company_fk, realm_id) 
REFERENCES pspadm.psp_company (company_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_voided_check
ADD CONSTRAINT psp_voided_check_fk1 FOREIGN KEY (accounting_report_file_fk, realm_id) 
REFERENCES pspadm.psp_accounting_report_file (accounting_report_file_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_voided_check
ADD CONSTRAINT psp_voided_check_fk3 FOREIGN KEY (agency_check_batch_fk, realm_id) 
REFERENCES pspadm.psp_agency_check_batch (agency_check_batch_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_wage_limit
ADD CONSTRAINT psp_wage_limit_fk1 FOREIGN KEY (law_fk, realm_id) 
REFERENCES pspadm.psp_law (law_id, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_wc_paycheck_pending
ADD CONSTRAINT psp_wc_pend_state_pchk_fk FOREIGN KEY (workers_comp_paycheck_fk, realm_id) 
REFERENCES pspadm.psp_wc_paycheck (wc_paycheck_seq, realm_id)
ON DELETE NO ACTION;



ALTER TABLE pspadm.psp_wc_paycheck_state
ADD CONSTRAINT psp_wc_pchk_state_pchk_fk FOREIGN KEY (workers_comp_paycheck_fk, realm_id) 
REFERENCES pspadm.psp_wc_paycheck (wc_paycheck_seq, realm_id)
ON DELETE NO ACTION;




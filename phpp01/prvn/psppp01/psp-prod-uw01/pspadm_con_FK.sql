\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

--------------------------------------------------------
--  ref constraints for table psp_achenrollment
--------------------------------------------------------

alter table pspadm.psp_achenrollment add constraint psp_achenrollment_fk2 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_achenrollment_detail
--------------------------------------------------------

alter table pspadm.psp_achenrollment_detail add constraint psp_achenrollment_detail_fk1 foreign key (a_c_h_enrollment_fk) references pspadm.psp_achenrollment (achenrollment_seq) ON DELETE NO ACTION;
alter table pspadm.psp_achenrollment_detail add constraint psp_achenrollment_detail_fk2 foreign key (response_file_fk) references pspadm.psp_achenrollment_file (achenrollment_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_achenrollment_detail add constraint psp_achenrollment_detail_fk3 foreign key (request_file_fk) references pspadm.psp_achenrollment_file (achenrollment_file_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_additional_filing_amount
--------------------------------------------------------

alter table pspadm.psp_additional_filing_amount add constraint psp_additional_filing_amou_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_ade_law_map
--------------------------------------------------------

alter table pspadm.psp_ade_law_map add constraint psp_ade_law_map_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_ade_law_map add constraint psp_ade_law_map_fk2 foreign key (ade_law_map_fk) references pspadm.psp_ade_law_map (ade_law_map_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_agency_check_batch
--------------------------------------------------------

alter table pspadm.psp_agency_check_batch add constraint psp_agency_check_batch_fk2 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
alter table pspadm.psp_agency_check_batch add constraint psp_agency_check_batch_fk1 foreign key (agency_check_batch_seq) references pspadm.psp_check_print_batch (check_print_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_agency_id_requirement
--------------------------------------------------------

alter table pspadm.psp_agency_id_requirement add constraint psp_agency_id_requirement_fk2 foreign key (payment_template_agency_id_fk) references pspadm.psp_payment_template_agency_id (payment_template_agency_id_seq) ON DELETE NO ACTION;
alter table pspadm.psp_agency_id_requirement add constraint psp_agency_id_requirement_fk1 foreign key (agency_id_requirement_seq) references pspadm.psp_payment_method_requirement (payment_method_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_agency_rate_request
--------------------------------------------------------

alter table pspadm.psp_agency_rate_request add constraint psp_agency_rate_request_fk1 foreign key (agency_fk) references pspadm.psp_agency (agency_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_annual_billing_item
--------------------------------------------------------

alter table pspadm.psp_annual_billing_item add constraint psp_annualbillingitem_fk1 foreign key (annual_billing_batch_fk) references pspadm.psp_annual_billing_batch (annual_billing_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_annual_billing_item add constraint psp_annualbillingitem_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_assisted_bundle_bill
--------------------------------------------------------

alter table pspadm.psp_assisted_bundle_bill add constraint psp_assistedbundlebill_fk1 foreign key (asst_bundle_comp_usage_fk) references pspadm.psp_asst_bundle_comp_usage (asst_bundle_comp_usage_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_asst_bundle_bill_detail
--------------------------------------------------------

alter table pspadm.psp_asst_bundle_bill_detail add constraint psp_asstbundlebilldetail_fk1 foreign key (assisted_bundle_bill_fk) references pspadm.psp_assisted_bundle_bill (assisted_bundle_bill_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_atfdata_extract_file
--------------------------------------------------------

alter table pspadm.psp_atfdata_extract_file add constraint psp_atfdata_extract_file_fk1 foreign key (a_t_f_data_extract_batch_fk) references pspadm.psp_atfdata_extract_batch (atfdata_extract_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_atfpayments_to_process
--------------------------------------------------------

alter table pspadm.psp_atfpayments_to_process add constraint psp_atfpayments_to_process_fk1 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_atfpayments_to_process add constraint psp_atfpayments_to_process_fk2 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_atfpayments_to_process add constraint psp_atfpayments_to_process_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_atfpayrolls_to_process
--------------------------------------------------------

alter table pspadm.psp_atfpayrolls_to_process add constraint psp_atfpayrolls_to_process_fk1 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_authrole_operation_assoc
--------------------------------------------------------

alter table pspadm.psp_authrole_operation_assoc add constraint psp_role_operation_fk_role foreign key (auth_role_fk) references pspadm.psp_auth_role (auth_role_seq) ON DELETE NO ACTION;
alter table pspadm.psp_authrole_operation_assoc add constraint psp_authrole_opt_fk_opt foreign key (auth_operation_fk) references pspadm.psp_auth_operation (operation_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_auth_role
--------------------------------------------------------

alter table pspadm.psp_auth_role add constraint psp_auth_role_fk1 foreign key (auth_domain_fk) references pspadm.psp_auth_domain (domain_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_auth_user_auth_role__assoc
--------------------------------------------------------

alter table pspadm.psp_auth_user_auth_role__assoc add constraint psp_userrole_fk_user foreign key (auth_user_fk) references pspadm.psp_auth_user (auth_user_seq) ON DELETE NO ACTION;
alter table pspadm.psp_auth_user_auth_role__assoc add constraint psp_userrole_fk_role foreign key (auth_role_fk) references pspadm.psp_auth_role (auth_role_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_batch_job_parameter
--------------------------------------------------------

alter table pspadm.psp_batch_job_parameter add constraint psp_batch_job_parameter_fk1 foreign key (batch_job_setup_fk) references pspadm.psp_batch_job_setup (job_type) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_bill
--------------------------------------------------------

alter table pspadm.psp_bill add constraint psp_bill_fk1 foreign key (company_usage_fk) references pspadm.psp_company_usage (company_usage_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_billing_detail
--------------------------------------------------------

alter table pspadm.psp_billing_detail add constraint psp_billing_detail_fk2 foreign key (offering_svcchg_price_fk) references pspadm.psp_svcchgprice (svcchgprice_seq) ON DELETE NO ACTION;
alter table pspadm.psp_billing_detail add constraint psp_billing_detail_fk1 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_bill_payment
--------------------------------------------------------

alter table pspadm.psp_bill_payment add constraint psp_bill_payment_fk1 foreign key (payee_fk) references pspadm.psp_payee (payee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_bill_payment add constraint psp_bill_payment_fk2 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_bill_payment_split
--------------------------------------------------------

alter table pspadm.psp_bill_payment_split add constraint psp_bill_payment_split_fk1 foreign key (bill_payment_fk) references pspadm.psp_bill_payment (bill_payment_seq) ON DELETE NO ACTION;
alter table pspadm.psp_bill_payment_split add constraint psp_bill_payment_split_fk2 foreign key (payee_bank_account_fk) references pspadm.psp_payee_bank_account (payee_bank_account_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_bpcompany_service_info
--------------------------------------------------------

alter table pspadm.psp_bpcompany_service_info add constraint psp_bpcompany_service_info_fk1 foreign key (bpcompany_service_info_seq) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_cdcompany_service_info
--------------------------------------------------------

alter table pspadm.psp_cdcompany_service_info add constraint psp_cdcompany_service_info_fk1 foreign key (cdcompany_service_info_seq) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_check_print_batch
--------------------------------------------------------

alter table pspadm.psp_check_print_batch add constraint psp_check_print_batch_fk1 foreign key (recon_plus_file_fk) references pspadm.psp_accounting_report_file (accounting_report_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_check_print_batch add constraint psp_check_print_batch_fk2 foreign key (positive_pay_file_fk) references pspadm.psp_accounting_report_file (accounting_report_file_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_check_print_paycheck
--------------------------------------------------------

alter table pspadm.psp_check_print_paycheck add constraint psp_checkprintpaycheck_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_check_print_paycheck add constraint psp_check_print_paycheck_fk1 foreign key (company_paycheck_batch_fk) references pspadm.psp_company_paycheck_batch (company_paycheck_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_check_print_signature
--------------------------------------------------------

alter table pspadm.psp_check_print_signature add constraint psp_check_print_signature_fk1 foreign key (sourcesys_printedchk_info_fk) references pspadm.psp_sourcesys_printedchk_info (sourcesys_printedchk_info_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company
--------------------------------------------------------

alter table pspadm.psp_company add constraint psp_company_fk8 foreign key (compliance_address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company add constraint psp_company_fk5 foreign key (offload_group_fk) references pspadm.psp_offload_group (offload_group_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company add constraint psp_company_fk7 foreign key (annual_billing_batch_fk) references pspadm.psp_annual_billing_batch (annual_billing_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company add constraint psp_company_fk2 foreign key (payroll_frequency_fk) references pspadm.psp_payroll_frequency (payroll_freq_cd) ON DELETE NO ACTION;
alter table pspadm.psp_company add constraint psp_company_fk3 foreign key (mailing_address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company add constraint psp_company_fk4 foreign key (legal_address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company add constraint psp_company_fk6 foreign key (funding_model_fk) references pspadm.psp_funding_model (funding_model_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_companyagency_frmtemplate
--------------------------------------------------------

alter table pspadm.psp_companyagency_frmtemplate add constraint psp_company_agency_form_te_fk1 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
alter table pspadm.psp_companyagency_frmtemplate add constraint psp_company_agency_form_te_fk2 foreign key (form_template_fk) references pspadm.psp_form_template (form_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_companyagency_pmttemplate
--------------------------------------------------------

alter table pspadm.psp_companyagency_pmttemplate add constraint psp_company_agency_payment_fk1 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
alter table pspadm.psp_companyagency_pmttemplate add constraint psp_company_agency_payment_fk2 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_additional_info
--------------------------------------------------------

alter table pspadm.psp_company_additional_info add constraint psp_company_additional_inf_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_additional_info add constraint psp_company_additional_inf_fk2 foreign key (industry_type_fk) references pspadm.psp_industry_type (industry_type_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_additional_info add constraint psp_company_additional_inf_fk3 foreign key (ownership_type_fk) references pspadm.psp_ownership_type (ownership_type_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_agency
--------------------------------------------------------

alter table pspadm.psp_company_agency add constraint psp_company_agency_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_agency add constraint psp_company_agency_fk1 foreign key (agency_fk) references pspadm.psp_agency (agency_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_bank_account
--------------------------------------------------------

alter table pspadm.psp_company_bank_account add constraint psp_company_bank_account_fk1 foreign key (bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_bank_account add constraint psp_company_bank_account_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_daily_liability
--------------------------------------------------------

alter table pspadm.psp_company_daily_liability add constraint psp_company_daily_liabilit_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_company_daily_liability add constraint psp_company_daily_liabilit_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_event
--------------------------------------------------------

alter table pspadm.psp_company_event add constraint psp_company_event_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_company_event_detail
--------------------------------------------------------

alter table pspadm.psp_company_event_detail add constraint psp_company_event_detail_fk1 foreign key (company_fk,company_event_fk) references pspadm.psp_company_event (company_fk,company_event_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_company_event_detail add constraint psp_company_event_detail_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_event_email
--------------------------------------------------------

alter table pspadm.psp_company_event_email add constraint psp_company_event_email_fk1 foreign key (company_fk,company_event_fk) references pspadm.psp_company_event (company_fk,company_event_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_company_event_email_param
--------------------------------------------------------

alter table pspadm.psp_company_event_email_param add constraint psp_company_event_email_pa_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_company_event_email_param add constraint psp_company_event_email_pa_fk1 foreign key (company_event_email_fk) references pspadm.psp_company_event_email (company_event_email_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_filing_amount
--------------------------------------------------------

alter table pspadm.psp_company_filing_amount add constraint psp_companyfilingamount_fk1 foreign key (company_agency_pmt_template_fk) references pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_law
--------------------------------------------------------

alter table pspadm.psp_company_law add constraint psp_company_law_fk4 foreign key (additional_company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_law add constraint psp_company_law_fk1 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_law add constraint psp_company_law_fk2 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_law_rate
--------------------------------------------------------

alter table pspadm.psp_company_law_rate add constraint psp_company_law_rate_fk1 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_note
--------------------------------------------------------

alter table pspadm.psp_company_note add constraint psp_companynote_fk1 foreign key (company_fk,company_event_fk) references pspadm.psp_company_event (company_fk,company_event_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_company_note add constraint psp_company_note_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_offer
--------------------------------------------------------

alter table pspadm.psp_company_offer add constraint psp_company_offer_fk1 foreign key (offer_fk) references pspadm.psp_offer (offer_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_offer add constraint psp_company_offer_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_offering
--------------------------------------------------------

alter table pspadm.psp_company_offering add constraint psp_company_offering_fk1 foreign key (offering_fk) references pspadm.psp_offering (offering_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_offering add constraint psp_company_offering_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_paycheck_batch
--------------------------------------------------------

alter table pspadm.psp_company_paycheck_batch add constraint psp_company_paycheck_batch_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_paycheck_batch add constraint psp_company_paycheck_batch_fk1 foreign key (company_paycheck_batch_seq) references pspadm.psp_check_print_batch (check_print_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_payroll_item
--------------------------------------------------------

alter table pspadm.psp_company_payroll_item add constraint psp_company_payroll_item_fk4 foreign key (additional_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_payroll_item add constraint psp_company_payroll_item_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_payroll_item add constraint psp_company_payroll_item_fk2 foreign key (payroll_item_fk) references pspadm.psp_payroll_item (payroll_item_code) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_pin
--------------------------------------------------------

alter table pspadm.psp_company_pin add constraint psp_company_pin_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_rate_request
--------------------------------------------------------

alter table pspadm.psp_company_rate_request add constraint psp_company_rate_request_fk1 foreign key (agency_rate_request_fk) references pspadm.psp_agency_rate_request (agency_rate_request_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_rate_request add constraint psp_company_rate_request_fk2 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_service
--------------------------------------------------------

alter table pspadm.psp_company_service add constraint psp_company_service_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_service add constraint psp_company_service_fk2 foreign key (service_fk) references pspadm.psp_service (service_cd) ON DELETE NO ACTION;
alter table pspadm.psp_company_service add constraint psp_company_service_fk3 foreign key (funding_model_fk) references pspadm.psp_funding_model (funding_model_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_service_bank_acct
--------------------------------------------------------

alter table pspadm.psp_company_service_bank_acct add constraint psp_company_service_bank_a_fk1 foreign key (company_service_fk) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_service_bank_acct add constraint psp_company_service_bank_a_fk2 foreign key (company_bank_account_fk) references pspadm.psp_company_bank_account (company_bank_account_seq) ON DELETE NO ACTION;
alter table pspadm.psp_company_service_bank_acct add constraint psp_company_service_bank_a_fk3 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_company_tfssubmission
--------------------------------------------------------

alter table pspadm.psp_company_tfssubmission add constraint psp_company_tfssubmission_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_compensation
--------------------------------------------------------

alter table pspadm.psp_compensation add constraint psp_compensation_fk4 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_compensation add constraint psp_compensation_fk2 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_compensation add constraint psp_compensation_fk1 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_comp_adjust_submission
--------------------------------------------------------

alter table pspadm.psp_comp_adjust_submission add constraint psp_company_adjustment_sub_fk4 foreign key (original_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_comp_adjust_submission add constraint psp_company_adjustment_sub_fk3 foreign key (void_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_comp_adjust_submission add constraint psp_company_adjustment_sub_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_comp_pmttemplate_pmtmethod
--------------------------------------------------------

alter table pspadm.psp_comp_pmttemplate_pmtmethod add constraint psp_companypaymenttemplate_fk1 foreign key (company_agency_pmt_template_fk) references pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_comp_pmt_template_agencyid
--------------------------------------------------------

alter table pspadm.psp_comp_pmt_template_agencyid add constraint psp_companypaymenttemplate_fk2 foreign key (company_agency_pmt_template_fk) references pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_contact
--------------------------------------------------------

alter table pspadm.psp_contact add constraint psp_contact_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_contact add constraint psp_contact_fk1 foreign key (contact_seq) references pspadm.psp_individual (individual_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_ddcompany_service_info
--------------------------------------------------------

alter table pspadm.psp_ddcompany_service_info add constraint psp_ddcompany_service_info_fk1 foreign key (ddcompany_service_info_seq) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_deduction
--------------------------------------------------------

alter table pspadm.psp_deduction add constraint psp_deduction_fk2 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_deduction add constraint psp_deduction_fk4 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_deduction add constraint psp_deduction_fk1 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_deposit_frequency_file_rec
--------------------------------------------------------

alter table pspadm.psp_deposit_frequency_file_rec add constraint psp_depositfrequencyfilere_fk1 foreign key (deposit_frequency_file_fk) references pspadm.psp_deposit_frequency_file (deposit_frequency_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_deposit_frequency_file_rec add constraint psp_deposit_frequency_file_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_deposit_frequency_req
--------------------------------------------------------

alter table pspadm.psp_deposit_frequency_req add constraint psp_deposit_frequency_requ_fk1 foreign key (deposit_frequency_req_seq) references pspadm.psp_payment_requirement (payment_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_dep_freq_ledger_operation
--------------------------------------------------------

alter table pspadm.psp_dep_freq_ledger_operation add constraint psp_deposit_frequency_ledg_fk1 foreign key (dep_freq_ledger_operation_seq) references pspadm.psp_ledger_operation (ledger_operation_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_dicrfile
--------------------------------------------------------

alter table pspadm.psp_dicrfile add constraint psp_dicrfile_fk1 foreign key (n_a_c_h_a_file_fk) references pspadm.psp_nachafile (nachafile_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_disburse_advice
--------------------------------------------------------

alter table pspadm.psp_disburse_advice add constraint psp_disburse_advice_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_disburse_advice_tax_liab
--------------------------------------------------------

alter table pspadm.psp_disburse_advice_tax_liab add constraint psp_disburse_advice_tax_li_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_disburse_advice_tax_liab add constraint psp_disburse_advice_tax_li_fk1 foreign key (disburse_advice_fk) references pspadm.psp_disburse_advice (disburse_advice_seq) ON DELETE NO ACTION;
alter table pspadm.psp_disburse_advice_tax_liab add constraint psp_disburse_advice_tax_li_fk2 foreign key (company_fk,tips_liability_fk) references pspadm.psp_disburse_advice_tax_liab (company_fk,disburse_advice_tax_liab_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_edi_payment_detail
--------------------------------------------------------

alter table pspadm.psp_edi_payment_detail add constraint psp_edi_payment_detail_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_edi_payment_detail add constraint psp_edi_payment_detail_fk1 foreign key (parent_file_fk) references pspadm.psp_state_edi_tax_file (state_edi_tax_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_edi_payment_detail add constraint psp_edi_payment_detail_fk2 foreign key (response_file_fk) references pspadm.psp_state_edi_tax_file (state_edi_tax_file_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_ee_payrollitem_qtrtotals
--------------------------------------------------------

alter table pspadm.psp_ee_payrollitem_qtrtotals add constraint psp_employee_payroll_item__fk1 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_ee_payrollitem_qtrtotals add constraint psp_employee_payroll_item__fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_effective_deposit_freq
--------------------------------------------------------

alter table pspadm.psp_effective_deposit_freq add constraint psp_effective_deposit_freq_fk1 foreign key (company_agency_pmt_template_fk) references pspadm.psp_companyagency_pmttemplate (companyagency_pmttemplate_seq) ON DELETE NO ACTION;
alter table pspadm.psp_effective_deposit_freq add constraint psp_effective_deposit_freq_fk2 foreign key (payment_template_frequency_fk) references pspadm.psp_pmt_template_frequency (payment_template_frequency_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_eftps_enrollment
--------------------------------------------------------

alter table pspadm.psp_eftps_enrollment add constraint psp_eftps_enrollment_fk1 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_eftps_enrollment_detail
--------------------------------------------------------

alter table pspadm.psp_eftps_enrollment_detail add constraint psp_eftps_enrollment_detai_fk2 foreign key (parent_file_fk) references pspadm.psp_eftps_file (eftps_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_eftps_enrollment_detail add constraint psp_eftps_enrollment_detai_fk3 foreign key (response_file_fk) references pspadm.psp_eftps_file (eftps_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_eftps_enrollment_detail add constraint psp_eftps_enrollment_detai_fk1 foreign key (eftps_enrollment_fk) references pspadm.psp_eftps_enrollment (eftps_enrollment_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_eftps_file
--------------------------------------------------------

alter table pspadm.psp_eftps_file add constraint psp_eftps_file_fk2 foreign key (eftps_file_seq) references pspadm.psp_edi_tax_file (edi_tax_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_eftps_file add constraint psp_eftps_file_fk1 foreign key (ack_file_fk) references pspadm.psp_eftps_file (eftps_file_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_eftps_payment_detail
--------------------------------------------------------

alter table pspadm.psp_eftps_payment_detail add constraint psp_eftps_payment_detail_fk1 foreign key (parent_file_fk) references pspadm.psp_eftps_file (eftps_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_eftps_payment_detail add constraint psp_eftps_payment_detail_fk2 foreign key (return_file_fk) references pspadm.psp_eftps_file (eftps_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_eftps_payment_detail add constraint psp_eftps_payment_detail_fk3 foreign key (response_file_fk) references pspadm.psp_eftps_file (eftps_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_eftps_payment_detail add constraint psp_eftps_payment_detail_fk4 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_employee
--------------------------------------------------------

alter table pspadm.psp_employee add constraint psp_employee_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee add constraint psp_employee_fk1 foreign key (employee_seq) references pspadm.psp_individual (individual_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_accrual
--------------------------------------------------------

alter table pspadm.psp_employee_accrual add constraint psp_employee_accrual_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_bank_account
--------------------------------------------------------

alter table pspadm.psp_employee_bank_account add constraint psp_employee_bank_account_fk1 foreign key (bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_bank_account add constraint psp_employee_bank_account_fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_custom_field
--------------------------------------------------------

alter table pspadm.psp_employee_custom_field add constraint psp_employee_custom_field_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_law_qtr_totals
--------------------------------------------------------

alter table pspadm.psp_employee_law_qtr_totals add constraint psp_employee_law_qtr_total_fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_law_qtr_totals add constraint psp_employee_law_qtr_total_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_law_qtr_totals add constraint psp_employee_law_qtr_total_fk6 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_law_qtr_totals add constraint psp_employee_law_qtr_total_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_payroll_item
--------------------------------------------------------

alter table pspadm.psp_employee_payroll_item add constraint psp_employee_payroll_item_fk1 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_payroll_item add constraint psp_employee_payroll_item_fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_tax
--------------------------------------------------------

alter table pspadm.psp_employee_tax add constraint psp_employee_tax_fk1 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_tax add constraint psp_employee_tax_fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_usage
--------------------------------------------------------

alter table pspadm.psp_employee_usage add constraint psp_employeeusage_fk1 foreign key (usage_period_fk) references pspadm.psp_usage_period (usage_period_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_w2_totals
--------------------------------------------------------

alter table pspadm.psp_employee_w2_totals add constraint psp_employee_w2_totals_fk1 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_w2_totals add constraint psp_employee_w2_totals_fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employee_w2_totals add constraint psp_employee_w2_totals_fk3 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_employee_w2_totals add constraint psp_employee_w2_totals_fk6 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employee_wage_plan
--------------------------------------------------------

alter table pspadm.psp_employee_wage_plan add constraint psp_employee_wage_plan_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_employer_contribution
--------------------------------------------------------

alter table pspadm.psp_employer_contribution add constraint psp_employer_contribution_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employer_contribution add constraint psp_employercontribution_fk1 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_employer_contribution add constraint psp_employercontribution_fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_employer_preference
--------------------------------------------------------

alter table pspadm.psp_employer_preference add constraint psp_employer_preference_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_emp_totals_payroll_run
--------------------------------------------------------

alter table pspadm.psp_emp_totals_payroll_run add constraint psp_emp_totals_payroll_run_fk1 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_emp_totals_payroll_run add constraint psp_emp_totals_payroll_run_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_entitlement
--------------------------------------------------------

alter table pspadm.psp_entitlement add constraint psp_entitlement_fk1 foreign key (entitlement_code_fk) references pspadm.psp_entitlement_code (entitlement_code_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_entitlement_code_offering
--------------------------------------------------------

alter table pspadm.psp_entitlement_code_offering add constraint psp_entitlement_code_offer_fk1 foreign key (entitlement_code_fk) references pspadm.psp_entitlement_code (entitlement_code_seq) ON DELETE NO ACTION;
alter table pspadm.psp_entitlement_code_offering add constraint psp_entitlement_code_offer_fk2 foreign key (offering_fk) references pspadm.psp_offering (offering_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_entitlement_unit
--------------------------------------------------------

alter table pspadm.psp_entitlement_unit add constraint psp_entitlement_unit_fk1 foreign key (entitlement_fk) references pspadm.psp_entitlement (entitlement_seq) ON DELETE NO ACTION;
alter table pspadm.psp_entitlement_unit add constraint psp_entitlementunit_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_entity_change
--------------------------------------------------------

alter table pspadm.psp_entity_change add constraint psp_entitychange_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_entry_detail_record
--------------------------------------------------------

alter table pspadm.psp_entry_detail_record add constraint psp_entry_detail_record_fk4 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_entry_detail_record add constraint psp_entry_detail_record_fk2 foreign key (n_a_c_h_a_file_fk) references pspadm.psp_nachafile (nachafile_seq) ON DELETE NO ACTION;
alter table pspadm.psp_entry_detail_record add constraint psp_entry_detail_record_fk1 foreign key (intuit_bank_account_fk) references pspadm.psp_intuit_bank_account (intuit_bank_account_seq) ON DELETE NO ACTION;
alter table pspadm.psp_entry_detail_record add constraint psp_entry_detail_record_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_event_as400_sync
--------------------------------------------------------

alter table pspadm.psp_event_as400_sync add constraint psp_event_as400_sync_fk1 foreign key (company_fk,company_event_fk) references pspadm.psp_company_event (company_fk,company_event_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_evttp_srcsys_assoc
--------------------------------------------------------

alter table pspadm.psp_evttp_srcsys_assoc add constraint psp_evttp_srcsys_fk_evttp foreign key (interesting_event_types_fk) references pspadm.psp_event_type (event_type_cd) ON DELETE NO ACTION;
alter table pspadm.psp_evttp_srcsys_assoc add constraint psp_evttp_srcsys_fk_srcsys foreign key (source_system_fk) references pspadm.psp_source_system (source_system_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_failed_payroll_run
--------------------------------------------------------

alter table pspadm.psp_failed_payroll_run add constraint psp_failed_payroll_run_fk1 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fee
--------------------------------------------------------

alter table pspadm.psp_fee add constraint psp_fee_fk1 foreign key (source_system_fk) references pspadm.psp_source_system (source_system_cd) ON DELETE NO ACTION;
alter table pspadm.psp_fee add constraint psp_fee_fk2 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_financial_transaction
--------------------------------------------------------

alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk16 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk13 foreign key (bill_payment_split_fk) references pspadm.psp_bill_payment_split (bill_payment_split_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk14 foreign key (tax_penalty_interest_fk) references pspadm.psp_tax_penalty_interest (tax_penalty_interest_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financialtransaction_fk3 foreign key (company_fk,original_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk8 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk11 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk6 foreign key (company_fk,paycheck_split_fk) references pspadm.psp_paycheck_split (company_fk,paycheck_split_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financialtransaction_fk2 foreign key (company_fk,relatable_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk9 foreign key (current_transaction_state_fk) references pspadm.psp_transaction_state (transaction_state_cd) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk7 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk5 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk4 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk3 foreign key (debit_bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk2 foreign key (credit_bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk10 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financialtransaction_fk1 foreign key (billing_detail_fk) references pspadm.psp_billing_detail (billing_detail_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_financial_trans_state
--------------------------------------------------------

alter table pspadm.psp_financial_trans_state add constraint psp_financialtransactionst_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_trans_state add constraint psp_financialtransactionst_fk2 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_trans_state add constraint psp_financial_transaction__fk7 foreign key (transaction_response_fk) references pspadm.psp_transaction_response (transaction_response_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_trans_state add constraint psp_financial_transaction__fk6 foreign key (transaction_state_fk) references pspadm.psp_transaction_state (transaction_state_cd) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_trans_state add constraint psp_financial_transaction__fk4 foreign key (gems_upload_batch_fk) references pspadm.psp_gems_upload_batch (gems_upload_batch_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_trans_state add constraint psp_financial_transaction__fk5 foreign key (company_fk,financial_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_financial_txn_action
--------------------------------------------------------

alter table pspadm.psp_financial_txn_action add constraint psp_financial_transaction__fk1 foreign key (action_event_fk) references pspadm.psp_action_event (code) ON DELETE NO ACTION;
alter table pspadm.psp_financial_txn_action add constraint psp_financial_transaction__fk2 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION;
alter table pspadm.psp_financial_txn_action add constraint psp_financial_transaction__fk3 foreign key (transaction_state_fk) references pspadm.psp_transaction_state (transaction_state_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_forecast_detail
--------------------------------------------------------

alter table pspadm.psp_forecast_detail add constraint psp_forecastdetail_fk1 foreign key (forecast_fk) references pspadm.psp_forecast (forecast_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_form_template
--------------------------------------------------------

alter table pspadm.psp_form_template add constraint psp_form_template_fk2 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
alter table pspadm.psp_form_template add constraint psp_form_template_fk1 foreign key (agency_fk) references pspadm.psp_agency (agency_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fraud_address
--------------------------------------------------------

alter table pspadm.psp_fraud_address add constraint psp_fraudaddress_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fraud_bank_account
--------------------------------------------------------

alter table pspadm.psp_fraud_bank_account add constraint psp_fraudbankaccount_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fraud_company
--------------------------------------------------------

alter table pspadm.psp_fraud_company add constraint psp_fraudcompany_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fraud_contact
--------------------------------------------------------

alter table pspadm.psp_fraud_contact add constraint psp_fraudcontact_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fraud_event
--------------------------------------------------------

alter table pspadm.psp_fraud_event add constraint psp_fraud_event_fk1 foreign key (company_fk,company_event_fk) references pspadm.psp_company_event (company_fk,company_event_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_fraud_event add constraint psp_fraud_event_fk2 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
alter table pspadm.psp_fraud_event add constraint psp_fraud_event_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_fraud_event add constraint psp_fraud_event_fk4 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fraud_value
--------------------------------------------------------

alter table pspadm.psp_fraud_value add constraint psp_fraudvalue_fk1 foreign key (fraud_rule_fk) references pspadm.psp_fraud_rule (fraud_rule_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_fset_filing_detail
--------------------------------------------------------

alter table pspadm.psp_fset_filing_detail add constraint psp_fset_filing_detail_fk1 foreign key (response_file_fk) references pspadm.psp_fset_file (fset_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_fset_filing_detail add constraint psp_fset_filing_detail_fk2 foreign key (parent_file_fk) references pspadm.psp_fset_file (fset_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_fset_filing_detail add constraint psp_fset_filing_detail_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_gems_ledger_posting_rule
--------------------------------------------------------

alter table pspadm.psp_gems_ledger_posting_rule add constraint psp_gems_ledger_posting_ru_fk1 foreign key (ledger_account_fk) references pspadm.psp_ledger_account (ledger_account_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_gems_monthly_balance
--------------------------------------------------------

alter table pspadm.psp_gems_monthly_balance add constraint psp_gems_monthly_balance_fk1 foreign key (gems_ledger_posting_rule_fk) references pspadm.psp_gems_ledger_posting_rule (gems_ledger_posting_rule_seq) ON DELETE NO ACTION;
alter table pspadm.psp_gems_monthly_balance add constraint psp_gems_monthly_balance_fk2 foreign key (gems_upload_batch_fk) references pspadm.psp_gems_upload_batch (gems_upload_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_hours_worked_exception
--------------------------------------------------------

alter table pspadm.psp_hours_worked_exception add constraint psp_hours_worked_exception_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_individual
--------------------------------------------------------

alter table pspadm.psp_individual add constraint psp_individual_fk1 foreign key (mailing_address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_intuit_bank_account
--------------------------------------------------------

alter table pspadm.psp_intuit_bank_account add constraint psp_intuit_bank_account_fk1 foreign key (bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_intuit_bank_acc_txn_type
--------------------------------------------------------

alter table pspadm.psp_intuit_bank_acc_txn_type add constraint psp_intuit_bank_account_tr_fk1 foreign key (intuit_bank_account_fk) references pspadm.psp_intuit_bank_account (intuit_bank_account_seq) ON DELETE NO ACTION;
alter table pspadm.psp_intuit_bank_acc_txn_type add constraint psp_intuit_bank_account_tr_fk2 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_intuit_ba_bt_ft
--------------------------------------------------------

alter table pspadm.psp_intuit_ba_bt_ft add constraint psp_intuit_babatch_type_fi_fk1 foreign key (intuit_bank_account_fk) references pspadm.psp_intuit_bank_account (intuit_bank_account_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_intuit_shipper_info
--------------------------------------------------------

alter table pspadm.psp_intuit_shipper_info add constraint psp_intuit_shipper_info_fk1 foreign key (shipper_address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_law
--------------------------------------------------------

alter table pspadm.psp_law add constraint psp_law_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_law_rate_range
--------------------------------------------------------

alter table pspadm.psp_law_rate_range add constraint psp_law_rate_range_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_law_rate_value
--------------------------------------------------------

alter table pspadm.psp_law_rate_value add constraint psp_law_rate_value_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_ledger_account_action
--------------------------------------------------------

alter table pspadm.psp_ledger_account_action add constraint psp_ledger_account_action_fk1 foreign key (action_event_fk) references pspadm.psp_action_event (code) ON DELETE NO ACTION;
alter table pspadm.psp_ledger_account_action add constraint psp_ledger_account_action_fk2 foreign key (ledger_account_fk) references pspadm.psp_ledger_account (ledger_account_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_ledger_balance
--------------------------------------------------------

alter table pspadm.psp_ledger_balance add constraint psp_ledgerbalance_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_ledger_balance add constraint psp_ledgerbalance_fk1 foreign key (ledger_account_fk) references pspadm.psp_ledger_account (ledger_account_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_ledger_operation
--------------------------------------------------------

alter table pspadm.psp_ledger_operation add constraint psp_ledger_operation_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_ledger_operation add constraint psp_ledger_operation_fk2 foreign key (ledger_operation_job_fk) references pspadm.psp_ledger_operation_job (ledger_operation_job_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_liability_adjustment
--------------------------------------------------------

alter table pspadm.psp_liability_adjustment add constraint psp_liability_adjustment_fk4 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_adjustment add constraint psp_liability_adjustment_fk7 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_adjustment add constraint psp_liability_adjustment_fk6 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_adjustment add constraint psp_liability_adjustment_fk1 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_adjustment add constraint psp_liability_adjustment_fk2 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_liability_adjustment add constraint psp_liability_adjustment_fk3 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_liability_check
--------------------------------------------------------

alter table pspadm.psp_liability_check add constraint psp_liability_check_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_check add constraint psp_liability_check_fk2 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_liability_check_line
--------------------------------------------------------

alter table pspadm.psp_liability_check_line add constraint psp_liability_check_line_fk1 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_check_line add constraint psp_liability_check_line_fk2 foreign key (liability_check_fk) references pspadm.psp_liability_check (liability_check_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liability_check_line add constraint psp_liability_check_line_fk4 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_liab_check_billing_assoc
--------------------------------------------------------

alter table pspadm.psp_liab_check_billing_assoc add constraint psp_liability_check_billin_fk1 foreign key (billing_detail_fk) references pspadm.psp_billing_detail (billing_detail_seq) ON DELETE NO ACTION;
alter table pspadm.psp_liab_check_billing_assoc add constraint psp_liability_check_billin_fk2 foreign key (liability_check_fk) references pspadm.psp_liability_check (liability_check_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_limit_value
--------------------------------------------------------

alter table pspadm.psp_limit_value add constraint psp_limit_value_fk1 foreign key (limit_rule_fk) references pspadm.psp_limit_rule (limit_rule_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_manual_requirement
--------------------------------------------------------

alter table pspadm.psp_manual_requirement add constraint psp_manual_requirement_fk1 foreign key (manual_requirement_seq) references pspadm.psp_payment_method_requirement (payment_method_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_money_movement_transaction
--------------------------------------------------------

alter table pspadm.psp_money_movement_transaction add constraint psp_moneymovementtransacti_fk1 foreign key (company_fk,original_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk6 foreign key (payment_frequency_fk) references pspadm.psp_pmt_template_frequency (payment_template_frequency_id) ON DELETE NO ACTION;
alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk3 foreign key (offload_batch_fk) references pspadm.psp_offload_batch (offload_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk5 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_nachafile
--------------------------------------------------------

alter table pspadm.psp_nachafile add constraint psp_nachafile_fk1 foreign key (offload_batch_fk) references pspadm.psp_offload_batch (offload_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_offering
--------------------------------------------------------

alter table pspadm.psp_offering add constraint psp_offering_fk1 foreign key (limit_rule_fk) references pspadm.psp_limit_rule (limit_rule_seq) ON DELETE NO ACTION;
alter table pspadm.psp_offering add constraint psp_offering_fk2 foreign key (fraud_rule_fk) references pspadm.psp_fraud_rule (fraud_rule_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_offering_svcchg
--------------------------------------------------------

alter table pspadm.psp_offering_svcchg add constraint psp_offering_svcchg_fk foreign key (offering_svcchg_grp_fk) references pspadm.psp_offering_svcchg_grp (offering_svcchg_grp_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_offering_svcchg_grp
--------------------------------------------------------

alter table pspadm.psp_offering_svcchg_grp add constraint psp_offering_svcchg_grp_fk foreign key (offering_fk) references pspadm.psp_offering (offering_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_offer_price
--------------------------------------------------------

alter table pspadm.psp_offer_price add constraint psp_offer_price_fk1 foreign key (offer_fk) references pspadm.psp_offer (offer_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_offer_svcchg_assoc
--------------------------------------------------------

alter table pspadm.psp_offer_svcchg_assoc add constraint psp_offer_svcchg_fk_svcchg foreign key (offering_service_charge_fk) references pspadm.psp_offering_svcchg (offering_svcchg_seq) ON DELETE NO ACTION;
alter table pspadm.psp_offer_svcchg_assoc add constraint psp_offer_svcchg_fk_offer foreign key (offer_fk) references pspadm.psp_offer (offer_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_offload_batch
--------------------------------------------------------

alter table pspadm.psp_offload_batch add constraint psp_offload_batch_fk1 foreign key (offload_group_fk) references pspadm.psp_offload_group (offload_group_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_on_hold_reason
--------------------------------------------------------

alter table pspadm.psp_on_hold_reason add constraint psp_on_hold_reason_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_paycheck
--------------------------------------------------------

alter table pspadm.psp_paycheck add constraint psp_paycheck_fk4 foreign key (source_employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk5 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk2 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk1 foreign key (d_d_employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk3 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_paycheck_split
--------------------------------------------------------

alter table pspadm.psp_paycheck_split add constraint psp_paycheck_split_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck_split add constraint psp_paycheck_split_fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_paycheck_split add constraint psp_paycheck_split_fk1 foreign key (employee_bank_account_fk) references pspadm.psp_employee_bank_account (employee_bank_account_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_paycheck_usage
--------------------------------------------------------

alter table pspadm.psp_paycheck_usage add constraint psp_paycheck_usage_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_paycheck_usage add constraint psp_paycheckusage_fk1 foreign key (bill_fk) references pspadm.psp_bill (bill_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck_usage add constraint psp_paycheckusage_fk2 foreign key (employee_usage_fk) references pspadm.psp_employee_usage (employee_usage_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_paycheck_usage_hist
--------------------------------------------------------

alter table pspadm.psp_paycheck_usage_hist add constraint psp_paycheck_usage_hist_fk1 foreign key (employee_usage_fk) references pspadm.psp_employee_usage (employee_usage_seq) ON DELETE NO ACTION;

--------------------------------------------------------
--  ref constraints for table psp_payee
--------------------------------------------------------

alter table pspadm.psp_payee add constraint psp_payee_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_payee add constraint psp_payee_fk2 foreign key (mailing_address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payee_bank_account
--------------------------------------------------------

alter table pspadm.psp_payee_bank_account add constraint psp_payee_bank_account_fk1 foreign key (payee_fk) references pspadm.psp_payee (payee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_payee_bank_account add constraint psp_payee_bank_account_fk2 foreign key (bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payment_batch_assoc
--------------------------------------------------------

alter table pspadm.psp_payment_batch_assoc add constraint psp_payment_batch_assoc_fk1 foreign key (agency_check_batch_fk) references pspadm.psp_agency_check_batch (agency_check_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_payment_batch_assoc add constraint psp_payment_batch_assoc_fk2 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_payment_method_requirement
--------------------------------------------------------

alter table pspadm.psp_payment_method_requirement add constraint psp_paymentmethodrequireme_fk1 foreign key (pmt_template_pmt_method_fk) references pspadm.psp_pmt_template_paymentmethod (pmt_template_paymentmethod_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payment_requirement
--------------------------------------------------------

alter table pspadm.psp_payment_requirement add constraint psp_payment_requirement_fk1 foreign key (payment_requirement_seq) references pspadm.psp_payment_method_requirement (payment_method_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payment_template
--------------------------------------------------------

alter table pspadm.psp_payment_template add constraint psp_payment_template_fk1 foreign key (agency_fk) references pspadm.psp_agency (agency_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payment_template_agency_id
--------------------------------------------------------

alter table pspadm.psp_payment_template_agency_id add constraint psp_payment_template_agenc_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payroll_item_taxable_to
--------------------------------------------------------

alter table pspadm.psp_payroll_item_taxable_to add constraint psp_payroll_item_taxable_t_fk1 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
alter table pspadm.psp_payroll_item_taxable_to add constraint psp_payroll_item_taxable_t_fk2 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payroll_run
--------------------------------------------------------

alter table pspadm.psp_payroll_run add constraint psp_payroll_run_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payroll_run_action
--------------------------------------------------------

alter table pspadm.psp_payroll_run_action add constraint psp_payroll_run_action_fk1 foreign key (action_event_fk) references pspadm.psp_action_event (code) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_payroll_subtype
--------------------------------------------------------

alter table pspadm.psp_payroll_subtype add constraint psp_payroll_subtype_fk1 foreign key (offering_fk) references pspadm.psp_offering (offering_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_paystub
--------------------------------------------------------

alter table pspadm.psp_paystub add constraint psp_paystub_fk1 foreign key (pstub_employer_info_fk) references pspadm.psp_pstub_employer_info (pstub_employer_info_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paystub add constraint psp_paystub_fk2 foreign key (company_fk,pstub_employee_info_fk) references pspadm.psp_pstub_employee_info (company_fk,pstub_employee_info_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paystub add constraint psp_paystub_fk3 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_pay_item
--------------------------------------------------------

alter table pspadm.psp_pay_item add constraint psp_pay_item_fk1 foreign key (liability_adjustment_fk) references pspadm.psp_liability_adjustment (liability_adjustment_seq) ON DELETE NO ACTION;
alter table pspadm.psp_pay_item add constraint psp_pay_item_fk2 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pmttemplate_chkinfo_assoc
--------------------------------------------------------

alter table pspadm.psp_pmttemplate_chkinfo_assoc add constraint psp_payment_template_check_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
alter table pspadm.psp_pmttemplate_chkinfo_assoc add constraint psp_payment_template_check_fk2 foreign key (pmttemplate_printedchkinfo_fk) references pspadm.psp_pmttemplate_printedchkinfo (pmttemplate_printedchkinfo_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pmttemplate_printedchkinfo
--------------------------------------------------------

alter table pspadm.psp_pmttemplate_printedchkinfo add constraint psp_payment_template_print_fk1 foreign key (address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pmt_template_bankaccount
--------------------------------------------------------

alter table pspadm.psp_pmt_template_bankaccount add constraint psp_payment_template_bank__fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
alter table pspadm.psp_pmt_template_bankaccount add constraint psp_payment_template_bank__fk2 foreign key (bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pmt_template_frequency
--------------------------------------------------------

alter table pspadm.psp_pmt_template_frequency add constraint psp_payment_template_frequ_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pmt_template_paymentmethod
--------------------------------------------------------

alter table pspadm.psp_pmt_template_paymentmethod add constraint psp_payment_template_payme_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_posting_rule
--------------------------------------------------------

alter table pspadm.psp_posting_rule add constraint psp_posting_rule_fk1 foreign key (ledger_account_fk) references pspadm.psp_ledger_account (ledger_account_cd) ON DELETE NO ACTION;
alter table pspadm.psp_posting_rule add constraint psp_posting_rule_fk2 foreign key (transaction_state_fk) references pspadm.psp_transaction_state (transaction_state_cd) ON DELETE NO ACTION;
alter table pspadm.psp_posting_rule add constraint psp_posting_rule_fk3 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_prior_payment_submission
--------------------------------------------------------

alter table pspadm.psp_prior_payment_submission add constraint psp_prior_payment_submissi_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_property_audit
--------------------------------------------------------

alter table pspadm.psp_property_audit add constraint psp_propertyaudit_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_dditem
--------------------------------------------------------

alter table pspadm.psp_pstub_dditem add constraint psp_pstub_dditem_fk1 foreign key (company_fk,paystub_fk) references pspadm.psp_paystub (company_fk,paystub_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_employee_info
--------------------------------------------------------

alter table pspadm.psp_pstub_employee_info add constraint psp_pstub_employee_info_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_pstub_employee_info add constraint psp_pstub_employee_info_fk2 foreign key (pstub_address_fk) references pspadm.psp_pstub_address (pstub_address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_employee_preference
--------------------------------------------------------

alter table pspadm.psp_pstub_employee_preference add constraint psp_pstub_employee_prefere_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_employer_info
--------------------------------------------------------

alter table pspadm.psp_pstub_employer_info add constraint psp_pstub_employer_info_fk1 foreign key (pstub_address_fk) references pspadm.psp_pstub_address (pstub_address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_msg
--------------------------------------------------------

alter table pspadm.psp_pstub_msg add constraint psp_pstub_msg_fk1 foreign key (company_fk,paystub_fk) references pspadm.psp_paystub (company_fk,paystub_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_paid_timeoff_item
--------------------------------------------------------

alter table pspadm.psp_pstub_paid_timeoff_item add constraint psp_pstub_paid_timeoff_ite_fk1 foreign key (company_fk,paystub_fk) references pspadm.psp_paystub (company_fk,paystub_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_pstub_pay_item
--------------------------------------------------------

alter table pspadm.psp_pstub_pay_item add constraint psp_pstub_pay_item_fk1 foreign key (company_fk,paystub_fk) references pspadm.psp_paystub (company_fk,paystub_seq) ON DELETE NO ACTION;
alter table pspadm.psp_pstub_pay_item add constraint psp_pstub_pay_item_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_pstub_state_tax_info
--------------------------------------------------------

alter table pspadm.psp_pstub_state_tax_info add constraint psp_pstub_state_tax_info_fk1 foreign key (pstub_employer_info_fk) references pspadm.psp_pstub_employer_info (pstub_employer_info_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_employee_info
--------------------------------------------------------

alter table pspadm.psp_qbdt_employee_info add constraint psp_qbdt_employee_info_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_employee_info add constraint psp_qbdt_employee_info_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_paycheck_info
--------------------------------------------------------

alter table pspadm.psp_qbdt_paycheck_info add constraint psp_qbdt_paycheck_info_fk1 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_qbdt_paycheck_info add constraint psp_qbdt_paycheck_info_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_payline_info
--------------------------------------------------------

alter table pspadm.psp_qbdt_payline_info add constraint psp_qbdt_payline_info_fk1 foreign key (employer_contribution_fk) references pspadm.psp_employer_contribution (employer_contribution_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payline_info add constraint psp_qbdt_payline_info_fk2 foreign key (company_fk,compensation_fk) references pspadm.psp_compensation (company_fk,compensation_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payline_info add constraint psp_qbdt_payline_info_fk3 foreign key (company_fk,deduction_fk) references pspadm.psp_deduction (company_fk,deduction_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payline_info add constraint psp_qbdt_payline_info_fk4 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_payroll_item_info
--------------------------------------------------------

alter table pspadm.psp_qbdt_payroll_item_info add constraint psp_qbdt_payroll_item_info_fk1 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payroll_item_info add constraint psp_qbdt_payroll_item_info_fk2 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payroll_item_info add constraint psp_qbdt_payroll_item_info_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_payroll_transaction
--------------------------------------------------------

alter table pspadm.psp_qbdt_payroll_transaction add constraint psp_qbdt_payroll_transacti_fk1 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payroll_transaction add constraint psp_qbdt_payroll_trans_fk2 foreign key (prior_payment_submission_fk) references pspadm.psp_prior_payment_submission (prior_payment_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payroll_transaction add constraint psp_qbdt_payroll_trans_fk1 foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payroll_transaction add constraint psp_qbdt_payroll_transacti_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_payroll_trans_line
--------------------------------------------------------

alter table pspadm.psp_qbdt_payroll_trans_line add constraint psp_qbdt_payroll_transline_fk1 foreign key (company_payroll_item_fk) references pspadm.psp_company_payroll_item (company_payroll_item_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_payroll_trans_line add constraint psp_qbdt_payroll_transacti_fk2 foreign key (qbdt_payroll_transaction_fk) references pspadm.psp_qbdt_payroll_transaction (qbdt_payroll_transaction_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_transaction_info
--------------------------------------------------------

alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk1 foreign key (liability_check_fk) references pspadm.psp_liability_check (liability_check_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk2 foreign key (liability_check_line_fk) references pspadm.psp_liability_check_line (liability_check_line_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk5 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk6 foreign key (liability_adjustment_fk) references pspadm.psp_liability_adjustment (liability_adjustment_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk4 foreign key (company_fk,financial_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdttransactioninfo_fk1 foreign key (prior_payment_submission_fk) references pspadm.psp_prior_payment_submission (prior_payment_submission_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk7 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk8 foreign key (qbdt_payroll_transaction_fk) references pspadm.psp_qbdt_payroll_transaction (qbdt_payroll_transaction_seq) ON DELETE NO ACTION;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk9 foreign key (qbdt_payroll_trans_line_fk) references pspadm.psp_qbdt_payroll_trans_line (qbdt_payroll_trans_line_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_qbdt_unprocessed_request
--------------------------------------------------------

alter table pspadm.psp_qbdt_unprocessed_request add constraint psp_qbdt_unprocessed_reque_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_quickbooks_info
--------------------------------------------------------

alter table pspadm.psp_quickbooks_info add constraint psp_quickbooks_info_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_racompany_service_info
--------------------------------------------------------

alter table pspadm.psp_racompany_service_info add constraint psp_racompany_service_info_fk1 foreign key (racompany_service_info_seq) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_rafenrollment
--------------------------------------------------------

alter table pspadm.psp_rafenrollment add constraint psp_rafenrollment_fk1 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_rafenrollment_detail
--------------------------------------------------------

alter table pspadm.psp_rafenrollment_detail add constraint psp_rafenrollment_detail_fk1 foreign key (r_a_f_enrollment_fk) references pspadm.psp_rafenrollment (rafenrollment_seq) ON DELETE NO ACTION;
alter table pspadm.psp_rafenrollment_detail add constraint psp_rafenrollment_detail_fk2 foreign key (enrollment_file_fk) references pspadm.psp_rafenrollment_file (rafenrollment_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_rafenrollment_detail add constraint psp_rafenrollment_detail_fk3 foreign key (delete_file_fk) references pspadm.psp_rafenrollment_file (rafenrollment_file_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_rate_ledger_operation
--------------------------------------------------------

alter table pspadm.psp_rate_ledger_operation add constraint psp_rate_ledger_operation_fk1 foreign key (rate_ledger_operation_seq) references pspadm.psp_ledger_operation (ledger_operation_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_reporting_agent
--------------------------------------------------------

alter table pspadm.psp_reporting_agent add constraint psp_reporting_agent_fk1 foreign key (address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_role_sub_status
--------------------------------------------------------

alter table pspadm.psp_role_sub_status add constraint psp_role_sub_status_fk1 foreign key (auth_role_fk) references pspadm.psp_auth_role (auth_role_seq) ON DELETE NO ACTION;
alter table pspadm.psp_role_sub_status add constraint psp_role_sub_status_fk2 foreign key (service_sub_status_fk) references pspadm.psp_service_sub_status (service_sub_status_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_second_offload
--------------------------------------------------------

alter table pspadm.psp_second_offload add constraint psp_second_offload_fk1 foreign key (offload_group_fk) references pspadm.psp_offload_group (offload_group_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_service_sub_status
--------------------------------------------------------

alter table pspadm.psp_service_sub_status add constraint psp_service_sub_status_fk1 foreign key (service_status_fk) references pspadm.psp_service_status (service_status_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_serv_stat_txn_sku_type
--------------------------------------------------------

alter table pspadm.psp_serv_stat_txn_sku_type add constraint psp_serv_stat_txn_sku_type_fk1 foreign key (service_sub_status_fk) references pspadm.psp_service_sub_status (service_sub_status_cd) ON DELETE NO ACTION;
alter table pspadm.psp_serv_stat_txn_sku_type add constraint psp_serv_stat_txn_sku_type_fk2 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_smsmigration
--------------------------------------------------------

alter table pspadm.psp_smsmigration add constraint psp_smsmigration_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_sourcesys_printedchk_info
--------------------------------------------------------

alter table pspadm.psp_sourcesys_printedchk_info add constraint psp_source_system_printed__fk1 foreign key (address_fk) references pspadm.psp_address (address_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_source_system_law_assoc
--------------------------------------------------------

alter table pspadm.psp_source_system_law_assoc add constraint psp_source_system_law_asso_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_source_system_law_assoc add constraint psp_source_system_law_asso_fk2 foreign key (source_system_fk) references pspadm.psp_source_system (source_system_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_state_edi_tax_file
--------------------------------------------------------

alter table pspadm.psp_state_edi_tax_file add constraint psp_state_edi_tax_file_fk2 foreign key (ack_file_fk) references pspadm.psp_state_edi_tax_file (state_edi_tax_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_state_edi_tax_file add constraint psp_state_edi_tax_file_fk1 foreign key (state_edi_tax_file_seq) references pspadm.psp_edi_tax_file (edi_tax_file_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_state_report_assoc
--------------------------------------------------------

alter table pspadm.psp_state_report_assoc add constraint psp_strpt_tmpfreq_fk_strptout foreign key (state_report_output_fk) references pspadm.psp_state_report_output (state_report_output_seq) ON DELETE NO ACTION;
alter table pspadm.psp_state_report_assoc add constraint psp_strpt_tmpfreq_fk_pmttmpfrq foreign key (payment_template_frequency_fk) references pspadm.psp_pmt_template_frequency (payment_template_frequency_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_suicredits_job
--------------------------------------------------------

alter table pspadm.psp_suicredits_job add constraint psp_suicredits_job_fk1 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_svcchgprice
--------------------------------------------------------

alter table pspadm.psp_svcchgprice add constraint psp_offeringservicechargep_fk1 foreign key (offering_service_charge_fk) references pspadm.psp_offering_svcchg (offering_svcchg_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_svcstat_srcsys_assoc
--------------------------------------------------------

alter table pspadm.psp_svcstat_srcsys_assoc add constraint psp_svcstat_srcsys_fk_svcstat foreign key (service_sub_status_fk) references pspadm.psp_service_sub_status (service_sub_status_cd) ON DELETE NO ACTION;
alter table pspadm.psp_svcstat_srcsys_assoc add constraint psp_svcstat_srcsys_fk_srcsys foreign key (source_system_fk) references pspadm.psp_source_system (source_system_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_svcstat_svc_assoc
--------------------------------------------------------

alter table pspadm.psp_svcstat_svc_assoc add constraint psp_svcstat_svc_fk_svcstat foreign key (service_sub_status_fk) references pspadm.psp_service_sub_status (service_sub_status_cd) ON DELETE NO ACTION;
alter table pspadm.psp_svcstat_svc_assoc add constraint psp_svcstat_svc_fk_svc foreign key (service_fk) references pspadm.psp_service (service_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_svcstat_syscap_assoc
--------------------------------------------------------

alter table pspadm.psp_svcstat_syscap_assoc add constraint psp_svcstat_cap_fk_svcstat foreign key (service_sub_status_fk) references pspadm.psp_service_sub_status (service_sub_status_cd) ON DELETE NO ACTION;
alter table pspadm.psp_svcstat_syscap_assoc add constraint psp_svcstat_cap_fk_cap foreign key (system_capability_fk) references pspadm.psp_system_capability (system_capability_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_system_payment_requirement
--------------------------------------------------------

alter table pspadm.psp_system_payment_requirement add constraint psp_system_payment_require_fk1 foreign key (system_payment_requirement_seq) references pspadm.psp_payment_requirement (payment_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_system_requirement
--------------------------------------------------------

alter table pspadm.psp_system_requirement add constraint psp_system_requirement_fk1 foreign key (system_requirement_seq) references pspadm.psp_payment_method_requirement (payment_method_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tax
--------------------------------------------------------

alter table pspadm.psp_tax add constraint psp_tax_fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_tax add constraint psp_tax_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
alter table pspadm.psp_tax add constraint psp_tax_fk5 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_tax add constraint psp_tax_fk3 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tax_company_service_info
--------------------------------------------------------

alter table pspadm.psp_tax_company_service_info add constraint psp_tax_company_service_in_fk1 foreign key (tax_company_service_info_seq) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tax_credits9061
--------------------------------------------------------

alter table pspadm.psp_tax_credits9061 add constraint psp_tax_credits9061_fk1 foreign key (tax_credits_application_fk) references pspadm.psp_tax_credits_application (tax_credits_application_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tax_payment_on_hold_reason
--------------------------------------------------------

alter table pspadm.psp_tax_payment_on_hold_reason add constraint psp_taxpaymentonholdreason_fk1 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_tax_penalty_interest
--------------------------------------------------------

alter table pspadm.psp_tax_penalty_interest add constraint psp_tax_penalty_interest_fk1 foreign key (company_agency_fk) references pspadm.psp_company_agency (company_agency_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tax_table_misc_data
--------------------------------------------------------

alter table pspadm.psp_tax_table_misc_data add constraint psp_tax_table_misc_data_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_tax_table_misc_data add constraint psp_tax_table_misc_data_fk1 foreign key (employee_tax_fk) references pspadm.psp_employee_tax (employee_tax_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_threshold_requirement
--------------------------------------------------------

alter table pspadm.psp_threshold_requirement add constraint psp_threshold_requirement_fk1 foreign key (threshold_requirement_seq) references pspadm.psp_payment_requirement (payment_requirement_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tp401kcompany_service_info
--------------------------------------------------------

alter table pspadm.psp_tp401kcompany_service_info add constraint psp_third_party401k_compan_fk1 foreign key (tp401kcompany_service_info_seq) references pspadm.psp_company_service (company_service_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tp401k_batch_employee
--------------------------------------------------------

alter table pspadm.psp_tp401k_batch_employee add constraint psp_tp401k_batch_ee_fk1 foreign key (third_party401k_batch_fk) references pspadm.psp_third_party401k_batch (third_party401k_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_tp401k_batch_employee add constraint psp_tp401k_batch_ee_fk2  foreign key (employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tp401k_batch_paycheck
--------------------------------------------------------

alter table pspadm.psp_tp401k_batch_paycheck add constraint psp_third_party401k_batch__fk1 foreign key (third_party401k_batch_fk) references pspadm.psp_third_party401k_batch (third_party401k_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_tp401k_batch_paycheck add constraint psp_third_party401k_batch__fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_tp401k_paycheck
--------------------------------------------------------

alter table pspadm.psp_tp401k_paycheck add constraint psp_tp401k_pchk_pchk_fk foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_tp401k_paycheck_pending
--------------------------------------------------------

alter table pspadm.psp_tp401k_paycheck_pending add constraint psp_tp401k_pend_state_pchk_fk foreign key (third_party401k_paycheck_fk) references pspadm.psp_tp401k_paycheck (tp401k_paycheck_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_tp401k_paycheck_state
--------------------------------------------------------

alter table pspadm.psp_tp401k_paycheck_state add constraint psp_tp401k_pchk_state_pchk_fk foreign key (third_party401k_paycheck_fk) references pspadm.psp_tp401k_paycheck (tp401k_paycheck_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_transaction_offload_batch
--------------------------------------------------------

alter table pspadm.psp_transaction_offload_batch add constraint psp_transaction_offload_ba_fk1 foreign key (company_fk,financial_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_transaction_offload_batch add constraint psp_transaction_offload_ba_fk2 foreign key (offload_batch_fk) references pspadm.psp_offload_batch (offload_batch_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_transaction_response
--------------------------------------------------------

alter table pspadm.psp_transaction_response add constraint psp_transaction_response_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_transaction_return
--------------------------------------------------------

alter table pspadm.psp_transaction_return add constraint psp_transaction_return_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_transaction_return add constraint psp_transaction_return_fk2 foreign key (return_batch_fk) references pspadm.psp_transaction_return_batch (transaction_return_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_transaction_return add constraint psp_transaction_return_fk1 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_transmission_payroll_run
--------------------------------------------------------

alter table pspadm.psp_transmission_payroll_run add constraint psp_transmissionpayrollrun_fk2 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_txntype_service_assoc
--------------------------------------------------------

alter table pspadm.psp_txntype_service_assoc add constraint psp_txntype_svc_fk_txntype foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION;
alter table pspadm.psp_txntype_service_assoc add constraint psp_txntype_svc_fk_svc foreign key (service_fk) references pspadm.psp_service (service_cd) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_usage_period
--------------------------------------------------------

alter table pspadm.psp_usage_period add constraint psp_usageperiod_fk1 foreign key (company_usage_fk) references pspadm.psp_company_usage (company_usage_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_user_setting
--------------------------------------------------------

alter table pspadm.psp_user_setting add constraint psp_user_setting_fk1 foreign key (user_preference_fk) references pspadm.psp_user_preference (key) ON DELETE NO ACTION;
alter table pspadm.psp_user_setting add constraint psp_usersetting_fk1 foreign key (auth_user_fk) references pspadm.psp_auth_user (auth_user_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_vmp_employee_info
--------------------------------------------------------

alter table pspadm.psp_vmp_employee_info add constraint psp_vmp_employee_info_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_voided_check
--------------------------------------------------------

alter table pspadm.psp_voided_check add constraint psp_voided_check_fk1 foreign key (accounting_report_file_fk) references pspadm.psp_accounting_report_file (accounting_report_file_seq) ON DELETE NO ACTION;
alter table pspadm.psp_voided_check add constraint psp_voided_check_fk3 foreign key (agency_check_batch_fk) references pspadm.psp_agency_check_batch (agency_check_batch_seq) ON DELETE NO ACTION;
alter table pspadm.psp_voided_check add constraint psp_voided_check_fk2 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_wage_limit
--------------------------------------------------------

alter table pspadm.psp_wage_limit add constraint psp_wage_limit_fk1 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_wc_company
--------------------------------------------------------

alter table pspadm.psp_wc_company add constraint psp_wc_company_fk1 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_wc_paycheck
--------------------------------------------------------

alter table pspadm.psp_wc_paycheck add constraint psp_wc_pchk_pchk_fk foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
--------------------------------------------------------
--  ref constraints for table psp_wc_paycheck_pending
--------------------------------------------------------

alter table pspadm.psp_wc_paycheck_pending add constraint psp_wc_pend_state_pchk_fk foreign key (workers_comp_paycheck_fk) references pspadm.psp_wc_paycheck (wc_paycheck_seq) ON DELETE NO ACTION;
--------------------------------------------------------
--  ref constraints for table psp_wc_paycheck_state
--------------------------------------------------------

alter table pspadm.psp_wc_paycheck_state add constraint psp_wc_pchk_state_pchk_fk foreign key (workers_comp_paycheck_fk) references pspadm.psp_wc_paycheck (wc_paycheck_seq) ON DELETE NO ACTION;
 
SELECT CURRENT_TIMESTAMP;
  

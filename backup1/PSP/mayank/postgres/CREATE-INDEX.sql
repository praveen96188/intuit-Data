-- ------------ Write CREATE-INDEX-stage scripts -----------

CREATE INDEX psp_achenrollment_fk2
ON pspadm.psp_achenrollment
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_achenrollment_detail_fk1
ON pspadm.psp_achenrollment_detail
USING BTREE (a_c_h_enrollment_fk ASC, realm_id ASC);



CREATE INDEX psp_achenrollment_detail_fk2
ON pspadm.psp_achenrollment_detail
USING BTREE (response_file_fk ASC, realm_id ASC);



CREATE INDEX psp_achenrollment_detail_fk3
ON pspadm.psp_achenrollment_detail
USING BTREE (request_file_fk ASC, realm_id ASC);



CREATE INDEX psp_additional_filing_amou_fk1
ON pspadm.psp_additional_filing_amount
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_ade_law_map_fk1
ON pspadm.psp_ade_law_map
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_ade_law_map_fk2
ON pspadm.psp_ade_law_map
USING BTREE (ade_law_map_fk ASC, realm_id ASC);



CREATE INDEX psp_agency_check_batch_fk2
ON pspadm.psp_agency_check_batch
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_agency_id_requirement_fk2
ON pspadm.psp_agency_id_requirement
USING BTREE (payment_template_agency_id_fk ASC, realm_id ASC);



CREATE INDEX psp_agency_rate_request_fk1
ON pspadm.psp_agency_rate_request
USING BTREE (agency_fk ASC, realm_id ASC);



CREATE INDEX psp_annualbillingitem_fk1
ON pspadm.psp_annual_billing_item
USING BTREE (annual_billing_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_annualbillingitem_fk2
ON pspadm.psp_annual_billing_item
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_assistedbundlebill_fk1
ON pspadm.psp_assisted_bundle_bill
USING BTREE (asst_bundle_comp_usage_fk ASC, realm_id ASC);



CREATE INDEX psp_asstbundlebilldetail_fk1
ON pspadm.psp_asst_bundle_bill_detail
USING BTREE (assisted_bundle_bill_fk ASC, realm_id ASC);



CREATE INDEX psp_atfdata_extract_file_fk1
ON pspadm.psp_atfdata_extract_file
USING BTREE (a_t_f_data_extract_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_atfpayments_to_process_fk1
ON pspadm.psp_atfpayments_to_process
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_atfpayments_to_process_fk2
ON pspadm.psp_atfpayments_to_process
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_atfpayments_to_process_fk3
ON pspadm.psp_atfpayments_to_process
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_atfpayrolls_to_process_fk1
ON pspadm.psp_atfpayrolls_to_process
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_atfpayrolls_to_process_i1
ON pspadm.psp_atfpayrolls_to_process
USING BTREE (modified_date ASC);



CREATE INDEX psp_auth_role_fk1
ON pspadm.psp_auth_role
USING BTREE (auth_domain_fk ASC, realm_id ASC);



CREATE INDEX psp_auth_user_corpid
ON pspadm.psp_auth_user
USING BTREE (corp_id ASC);



CREATE INDEX psp_userrole_fk_role
ON pspadm.psp_auth_user_auth_role__assoc
USING BTREE (auth_role_fk ASC, realm_id ASC);



CREATE INDEX psp_userrole_fk_user
ON pspadm.psp_auth_user_auth_role__assoc
USING BTREE (auth_user_fk ASC, realm_id ASC);



CREATE INDEX psp_authrole_opt_fk_opt
ON pspadm.psp_authrole_operation_assoc
USING BTREE (auth_operation_fk ASC, realm_id ASC);



CREATE INDEX psp_role_operation_fk_role
ON pspadm.psp_authrole_operation_assoc
USING BTREE (auth_role_fk ASC, realm_id ASC);



CREATE INDEX psp_bank_account_enc_i1
ON pspadm.psp_bank_account
USING BTREE (account_number_enc ASC);



CREATE INDEX psp_bank_account_i1
ON pspadm.psp_bank_account
USING BTREE (routing_number ASC, account_number_pt_bk ASC);



CREATE INDEX psp_bank_account_i2
ON pspadm.psp_bank_account
USING BTREE (account_number_pt_bk ASC);



CREATE INDEX psp_bank_account_mooddate
ON pspadm.psp_bank_account
USING BTREE (modified_date ASC);



CREATE INDEX psp_batch_job_audit_log_i1
ON pspadm.psp_batch_job_audit_log
USING BTREE (created_date ASC);



CREATE INDEX psp_batch_job_audit_log_idx1
ON pspadm.psp_batch_job_audit_log
USING BTREE (job_namespace ASC, is_verified ASC);



CREATE INDEX psp_batch_job_parameter_fk1
ON pspadm.psp_batch_job_parameter
USING BTREE (batch_job_setup_fk ASC, realm_id ASC);



CREATE INDEX psp_bill_fk1
ON pspadm.psp_bill
USING BTREE (company_usage_fk ASC, realm_id ASC);



CREATE UNIQUE INDEX psp_bill_u1
ON pspadm.psp_bill
USING BTREE (company_usage_fk ASC, bill_date ASC);



CREATE INDEX psp_bill_payment_fk1
ON pspadm.psp_bill_payment
USING BTREE (payee_fk ASC, realm_id ASC);



CREATE INDEX psp_bill_payment_fk2
ON pspadm.psp_bill_payment
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_bill_payment_split_fk1
ON pspadm.psp_bill_payment_split
USING BTREE (bill_payment_fk ASC, realm_id ASC);



CREATE INDEX psp_bill_payment_split_fk2
ON pspadm.psp_bill_payment_split
USING BTREE (payee_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_billing_detail_fk1
ON pspadm.psp_billing_detail
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_billing_detail_fk2
ON pspadm.psp_billing_detail
USING BTREE (offering_svcchg_price_fk ASC, realm_id ASC);



CREATE INDEX psp_check_print_batch_fk1
ON pspadm.psp_check_print_batch
USING BTREE (recon_plus_file_fk ASC, realm_id ASC);



CREATE INDEX psp_check_print_batch_fk2
ON pspadm.psp_check_print_batch
USING BTREE (positive_pay_file_fk ASC, realm_id ASC);



CREATE INDEX psp_check_print_paycheck_fk1
ON pspadm.psp_check_print_paycheck
USING BTREE (company_paycheck_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_checkprintpaycheck_fk2
ON pspadm.psp_check_print_paycheck
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_check_print_signature_fk1
ON pspadm.psp_check_print_signature
USING BTREE (sourcesys_printedchk_info_fk ASC, realm_id ASC);



CREATE INDEX psp_company_adjustment_sub_fk1
ON pspadm.psp_comp_adjust_submission
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_adjustment_sub_fk3
ON pspadm.psp_comp_adjust_submission
USING BTREE (void_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_company_adjustment_sub_fk4
ON pspadm.psp_comp_adjust_submission
USING BTREE (original_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_companypaymenttemplate_fk2
ON pspadm.psp_comp_pmt_template_agencyid
USING BTREE (company_agency_pmt_template_fk ASC, realm_id ASC);



CREATE INDEX psp_companypaymenttemplate_fk1
ON pspadm.psp_comp_pmttemplate_pmtmethod
USING BTREE (company_agency_pmt_template_fk ASC, realm_id ASC);



CREATE INDEX psp_comp_iam_realmid_idx
ON pspadm.psp_company
USING BTREE (i_a_m_realm_id ASC);



CREATE INDEX psp_company_fedtaxidenc_i1
ON pspadm.psp_company
USING BTREE (fed_tax_id_enc ASC);



CREATE INDEX psp_company_fk2
ON pspadm.psp_company
USING BTREE (payroll_frequency_fk ASC, realm_id ASC);



CREATE INDEX psp_company_fk3
ON pspadm.psp_company
USING BTREE (mailing_address_fk ASC, realm_id ASC);



CREATE INDEX psp_company_fk4
ON pspadm.psp_company
USING BTREE (legal_address_fk ASC, realm_id ASC);



CREATE INDEX psp_company_fk5
ON pspadm.psp_company
USING BTREE (funding_model_fk ASC, realm_id ASC);



CREATE INDEX psp_company_fk7
ON pspadm.psp_company
USING BTREE (annual_billing_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_company_fk8
ON pspadm.psp_company
USING BTREE (offload_group_fk ASC, realm_id ASC);



CREATE INDEX psp_company_i1
ON pspadm.psp_company
USING BTREE (LOWER(source_company_id) ASC);



CREATE INDEX psp_company_i2
ON pspadm.psp_company
USING BTREE (fed_tax_id_pt_bk ASC);



CREATE INDEX psp_company_i5
ON pspadm.psp_company
USING BTREE (LOWER(legal_name) ASC);



CREATE INDEX psp_company_additional_inf_fk1
ON pspadm.psp_company_additional_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_additional_inf_fk2
ON pspadm.psp_company_additional_info
USING BTREE (industry_type_fk ASC, realm_id ASC);



CREATE INDEX psp_company_agency_fk1
ON pspadm.psp_company_agency
USING BTREE (agency_fk ASC, realm_id ASC);



CREATE INDEX psp_company_agency_fk2
ON pspadm.psp_company_agency
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_bank_account_fk1
ON pspadm.psp_company_bank_account
USING BTREE (bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_company_bank_account_fk2
ON pspadm.psp_company_bank_account
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_bank_account_u1
ON pspadm.psp_company_bank_account
USING BTREE (source_bank_account_id ASC, company_fk ASC);



CREATE INDEX psp_company_consent_i1
ON pspadm.psp_company_consent
USING BTREE (fein_pt_bk ASC);



CREATE INDEX psp_company_daily_liabilit_fk1
ON pspadm.psp_company_daily_liability
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_company_daily_liabilit_fk2
ON pspadm.psp_company_daily_liability
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_event_fk1
ON pspadm.psp_company_event
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_event_i1
ON pspadm.psp_company_event
USING BTREE (company_fk ASC, event_type_cd ASC);



CREATE INDEX psp_company_event_i2
ON pspadm.psp_company_event
USING BTREE (event_time_stamp ASC);



CREATE INDEX psp_company_event_i3
ON pspadm.psp_company_event
USING BTREE (company_fk ASC, event_time_stamp ASC);



CREATE INDEX psp_company_event_detail_fk1
ON pspadm.psp_company_event_detail
USING BTREE (company_event_fk ASC, realm_id ASC);



CREATE INDEX psp_company_event_detail_fk2
ON pspadm.psp_company_event_detail
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_event_detail_i1
ON pspadm.psp_company_event_detail
USING BTREE (event_detail_type_cd ASC, value ASC, company_fk ASC);



CREATE INDEX psp_company_event_email_fk1
ON pspadm.psp_company_event_email
USING BTREE (company_event_fk ASC, realm_id ASC);



CREATE INDEX psp_company_event_email_pa_fk1
ON pspadm.psp_company_event_email_param
USING BTREE (company_event_email_fk ASC, realm_id ASC);



CREATE INDEX psp_companyfilingamount_fk1
ON pspadm.psp_company_filing_amount
USING BTREE (company_agency_pmt_template_fk ASC, realm_id ASC);



CREATE INDEX psp_company_law_fk1
ON pspadm.psp_company_law
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_company_law_fk2
ON pspadm.psp_company_law
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_company_law_fk4
ON pspadm.psp_company_law
USING BTREE (additional_company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_company_law_rate_fk1
ON pspadm.psp_company_law_rate
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_company_note_fk1
ON pspadm.psp_company_note
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_companynote_fk1
ON pspadm.psp_company_note
USING BTREE (company_event_fk ASC, realm_id ASC);



CREATE INDEX psp_company_offer_fk1
ON pspadm.psp_company_offer
USING BTREE (offer_fk ASC, realm_id ASC);



CREATE INDEX psp_company_offer_fk2
ON pspadm.psp_company_offer
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_offering_fk1
ON pspadm.psp_company_offering
USING BTREE (offering_fk ASC, realm_id ASC);



CREATE INDEX psp_company_offering_fk2
ON pspadm.psp_company_offering
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_paycheck_batch_fk2
ON pspadm.psp_company_paycheck_batch
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_payroll_item_fk1
ON pspadm.psp_company_payroll_item
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_payroll_item_fk2
ON pspadm.psp_company_payroll_item
USING BTREE (payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_company_payroll_item_fk4
ON pspadm.psp_company_payroll_item
USING BTREE (additional_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_company_pin_fk1
ON pspadm.psp_company_pin
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_rate_request_fk1
ON pspadm.psp_company_rate_request
USING BTREE (agency_rate_request_fk ASC, realm_id ASC);



CREATE INDEX psp_company_rate_request_fk2
ON pspadm.psp_company_rate_request
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_company_service_fk1
ON pspadm.psp_company_service
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_company_service_fk2
ON pspadm.psp_company_service
USING BTREE (service_fk ASC, realm_id ASC);



CREATE INDEX psp_company_service_fk3
ON pspadm.psp_company_service
USING BTREE (funding_model_fk ASC, realm_id ASC);



CREATE INDEX psp_company_service_idx1
ON pspadm.psp_company_service
USING BTREE (status_cd ASC);



CREATE INDEX psp_company_service_bank_a_fk1
ON pspadm.psp_company_service_bank_acct
USING BTREE (company_service_fk ASC, realm_id ASC);



CREATE INDEX psp_company_service_bank_a_fk2
ON pspadm.psp_company_service_bank_acct
USING BTREE (company_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_company_service_bank_a_fk3
ON pspadm.psp_company_service_bank_acct
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_company_tfssubmission_fk1
ON pspadm.psp_company_tfssubmission
USING BTREE (company_fk ASC, realm_id ASC);



CREATE UNIQUE INDEX psp_company_usage_u1
ON pspadm.psp_company_usage
USING BTREE (source_company_id ASC, source_system_cd ASC, license_id ASC, entitlement_id ASC);



CREATE INDEX psp_company_agency_form_te_fk1
ON pspadm.psp_companyagency_frmtemplate
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_company_agency_form_te_fk2
ON pspadm.psp_companyagency_frmtemplate
USING BTREE (form_template_fk ASC, realm_id ASC);



CREATE INDEX psp_company_agency_payment_fk1
ON pspadm.psp_companyagency_pmttemplate
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_company_agency_payment_fk2
ON pspadm.psp_companyagency_pmttemplate
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_compensation_fk1
ON pspadm.psp_compensation
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_compensation_fk2
ON pspadm.psp_compensation
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_compensation_fk4
ON pspadm.psp_compensation
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_contact_fk2
ON pspadm.psp_contact
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_ddcompany_service_info_fk2
ON pspadm.psp_ddcompany_service_info
USING BTREE (offload_group_fk ASC, realm_id ASC);



CREATE INDEX psp_deduction_fk1
ON pspadm.psp_deduction
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_deduction_fk2
ON pspadm.psp_deduction
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_deduction_fk4
ON pspadm.psp_deduction
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_deposit_frequency_file_fk1
ON pspadm.psp_deposit_frequency_file_rec
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_depositfrequencyfilere_fk1
ON pspadm.psp_deposit_frequency_file_rec
USING BTREE (deposit_frequency_file_fk ASC, realm_id ASC);



CREATE INDEX psp_dicrfile_fk1
ON pspadm.psp_dicrfile
USING BTREE (n_a_c_h_a_file_fk ASC, realm_id ASC);



CREATE INDEX psp_disburse_advice_fk1
ON pspadm.psp_disburse_advice
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_disburse_advice_tax_li_fk1
ON pspadm.psp_disburse_advice_tax_liab
USING BTREE (disburse_advice_fk ASC, realm_id ASC);



CREATE INDEX psp_disburse_advice_tax_li_fk2
ON pspadm.psp_disburse_advice_tax_liab
USING BTREE (tips_liability_fk ASC, realm_id ASC);



CREATE INDEX psp_edi_payment_detail_fk1
ON pspadm.psp_edi_payment_detail
USING BTREE (parent_file_fk ASC, realm_id ASC);



CREATE INDEX psp_edi_payment_detail_fk2
ON pspadm.psp_edi_payment_detail
USING BTREE (response_file_fk ASC, realm_id ASC);



CREATE INDEX psp_edi_payment_detail_fk3
ON pspadm.psp_edi_payment_detail
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_payroll_item__fk1
ON pspadm.psp_ee_payrollitem_qtrtotals
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_payroll_item__fk2
ON pspadm.psp_ee_payrollitem_qtrtotals
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_effective_deposit_freq_fk1
ON pspadm.psp_effective_deposit_freq
USING BTREE (company_agency_pmt_template_fk ASC, realm_id ASC);



CREATE INDEX psp_effective_deposit_freq_fk2
ON pspadm.psp_effective_deposit_freq
USING BTREE (payment_template_frequency_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_enrollment_fk1
ON pspadm.psp_eftps_enrollment
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_enrollment_detai_fk1
ON pspadm.psp_eftps_enrollment_detail
USING BTREE (eftps_enrollment_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_enrollment_detai_fk2
ON pspadm.psp_eftps_enrollment_detail
USING BTREE (parent_file_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_enrollment_detai_fk3
ON pspadm.psp_eftps_enrollment_detail
USING BTREE (response_file_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_file_fk1
ON pspadm.psp_eftps_file
USING BTREE (ack_file_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_fk1
ON pspadm.psp_eftps_payment_detail
USING BTREE (parent_file_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_fk2
ON pspadm.psp_eftps_payment_detail
USING BTREE (return_file_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_fk3
ON pspadm.psp_eftps_payment_detail
USING BTREE (response_file_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_fk4
ON pspadm.psp_eftps_payment_detail
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_i1
ON pspadm.psp_eftps_payment_detail
USING BTREE (parent_file_fk ASC, status_cd ASC, money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_i2
ON pspadm.psp_eftps_payment_detail
USING BTREE (transaction_set_id ASC, transaction_id ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_i3
ON pspadm.psp_eftps_payment_detail
USING BTREE (eft_transaction_id ASC, realm_id ASC);



CREATE INDEX psp_eftps_payment_detail_i4
ON pspadm.psp_eftps_payment_detail
USING BTREE (transaction_id ASC, agency_payment_id ASC, realm_id ASC);



CREATE INDEX psp_emp_totals_payroll_run_fk1
ON pspadm.psp_emp_totals_payroll_run
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_emp_totals_payroll_run_fk2
ON pspadm.psp_emp_totals_payroll_run
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_emp_cons_realmid_idx
ON pspadm.psp_employee
USING BTREE (consumer_realm_id ASC);



CREATE INDEX psp_employee_fk2
ON pspadm.psp_employee
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_tax_id
ON pspadm.psp_employee
USING BTREE (tax_id_pt_bk ASC);



CREATE INDEX psp_employee_taxidenc_i1
ON pspadm.psp_employee
USING BTREE (tax_id_enc ASC);



CREATE INDEX psp_employee_accrual_fk1
ON pspadm.psp_employee_accrual
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_bank_account_fk1
ON pspadm.psp_employee_bank_account
USING BTREE (bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_bank_account_fk2
ON pspadm.psp_employee_bank_account
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_bank_account_i1
ON pspadm.psp_employee_bank_account
USING BTREE (modified_date ASC);



CREATE INDEX psp_employee_bank_account_u1
ON pspadm.psp_employee_bank_account
USING BTREE (source_bank_account_id ASC, employee_fk ASC);



CREATE INDEX psp_employee_custom_field_fk1
ON pspadm.psp_employee_custom_field
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_law_qtr_tot_cel
ON pspadm.psp_employee_law_qtr_totals
USING BTREE (company_fk ASC, employee_fk ASC, law_fk ASC);



CREATE INDEX psp_employee_payroll_item_fk1
ON pspadm.psp_employee_payroll_item
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_payroll_item_fk2
ON pspadm.psp_employee_payroll_item
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_tax_fk1
ON pspadm.psp_employee_tax
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_tax_fk2
ON pspadm.psp_employee_tax
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employeeusage_fk1
ON pspadm.psp_employee_usage
USING BTREE (usage_period_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_w2_totals_fk1
ON pspadm.psp_employee_w2_totals
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_w2_totals_fk2
ON pspadm.psp_employee_w2_totals
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_w2_totals_fk3
ON pspadm.psp_employee_w2_totals
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_w2_totals_fk4
ON pspadm.psp_employee_w2_totals
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_w2_totals_fk6
ON pspadm.psp_employee_w2_totals
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_employee_wage_plan_fk1
ON pspadm.psp_employee_wage_plan
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_employer_contribution_fk1
ON pspadm.psp_employer_contribution
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_employercontribution_fk1
ON pspadm.psp_employer_contribution
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_employercontribution_fk2
ON pspadm.psp_employer_contribution
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_employer_preference_fk1
ON pspadm.psp_employer_preference
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_entitlement_fk1
ON pspadm.psp_entitlement
USING BTREE (entitlement_code_fk ASC, realm_id ASC);



CREATE INDEX psp_entitlement_idx2
ON pspadm.psp_entitlement
USING BTREE (customer_id ASC);



CREATE INDEX psp_entitlement_subsno
ON pspadm.psp_entitlement
USING BTREE (subscription_number ASC);



CREATE UNIQUE INDEX psp_entitlement_u1
ON pspadm.psp_entitlement
USING BTREE (license_number ASC, entitlement_offering_code ASC);



CREATE INDEX psp_entitlement_code_offer_fk1
ON pspadm.psp_entitlement_code_offering
USING BTREE (entitlement_code_fk ASC, realm_id ASC);



CREATE INDEX psp_entitlement_code_offer_fk2
ON pspadm.psp_entitlement_code_offering
USING BTREE (offering_fk ASC, realm_id ASC);



CREATE INDEX psp_enmt_fedtaxidenc_i1
ON pspadm.psp_entitlement_unit
USING BTREE (fed_tax_id_enc ASC);



CREATE INDEX psp_entitlement_unit_fk1
ON pspadm.psp_entitlement_unit
USING BTREE (entitlement_fk ASC, realm_id ASC);



CREATE INDEX psp_entitlement_unit_i1
ON pspadm.psp_entitlement_unit
USING BTREE (entitlement_unit_status ASC, created_date ASC);



CREATE INDEX psp_entitlement_unit_i2
ON pspadm.psp_entitlement_unit
USING BTREE (fed_tax_id_pt_bk ASC);



CREATE INDEX psp_entitlement_unit_idx2
ON pspadm.psp_entitlement_unit
USING BTREE (service_key ASC);



CREATE INDEX psp_entitlementunit_fk1
ON pspadm.psp_entitlement_unit
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_entitychange_fk1
ON pspadm.psp_entity_change
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_entity_update_u1
ON pspadm.psp_entity_update
USING BTREE (created_date ASC);



CREATE INDEX psp_entry_detail_record_fk1
ON pspadm.psp_entry_detail_record
USING BTREE (intuit_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_entry_detail_record_fk2
ON pspadm.psp_entry_detail_record
USING BTREE (n_a_c_h_a_file_fk ASC, intuit_bank_account_fk ASC);



CREATE INDEX psp_entry_detail_record_fk3
ON pspadm.psp_entry_detail_record
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_entry_detail_record_fk4
ON pspadm.psp_entry_detail_record
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_entry_detail_record_i1
ON pspadm.psp_entry_detail_record
USING BTREE (trace_number ASC);



CREATE INDEX psp_entry_detail_record_i2
ON pspadm.psp_entry_detail_record
USING BTREE (initiation_date ASC, n_a_c_h_a_file_type ASC);



CREATE INDEX psp_event_as400_sync_fk1
ON pspadm.psp_event_as400_sync
USING BTREE (company_event_fk ASC, realm_id ASC);



CREATE INDEX psp_evttp_srcsys_fk_evttp
ON pspadm.psp_evttp_srcsys_assoc
USING BTREE (interesting_event_types_fk ASC, realm_id ASC);



CREATE INDEX psp_evttp_srcsys_fk_srcsys
ON pspadm.psp_evttp_srcsys_assoc
USING BTREE (source_system_fk ASC, realm_id ASC);



CREATE INDEX psp_failed_payroll_run_fk1
ON pspadm.psp_failed_payroll_run
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_fee_fk1
ON pspadm.psp_fee
USING BTREE (source_system_fk ASC, realm_id ASC);



CREATE INDEX psp_fee_fk2
ON pspadm.psp_fee
USING BTREE (transaction_type_fk ASC, realm_id ASC);



CREATE INDEX fn_state_eff_date
ON pspadm.psp_financial_trans_state
USING BTREE (DATE(transaction_state_eff_date) ASC);



CREATE INDEX psp_financial_transaction__fk4
ON pspadm.psp_financial_trans_state
USING BTREE (gems_upload_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction__fk5
ON pspadm.psp_financial_trans_state
USING BTREE (financial_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction__fk6
ON pspadm.psp_financial_trans_state
USING BTREE (transaction_state_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction__fk7
ON pspadm.psp_financial_trans_state
USING BTREE (transaction_response_fk ASC, realm_id ASC);



CREATE INDEX psp_financialtransactionst_fk1
ON pspadm.psp_financial_trans_state
USING BTREE (company_fk ASC, transaction_type_fk ASC, transaction_state_eff_date ASC);



CREATE INDEX psp_financialtransactionst_fk2
ON pspadm.psp_financial_trans_state
USING BTREE (transaction_type_fk ASC, transaction_state_eff_date ASC);



CREATE INDEX psp_financial_transaction_fk10
ON pspadm.psp_financial_transaction
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk13
ON pspadm.psp_financial_transaction
USING BTREE (bill_payment_split_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk14
ON pspadm.psp_financial_transaction
USING BTREE (tax_penalty_interest_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk16
ON pspadm.psp_financial_transaction
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk2
ON pspadm.psp_financial_transaction
USING BTREE (credit_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk3
ON pspadm.psp_financial_transaction
USING BTREE (debit_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk5
ON pspadm.psp_financial_transaction
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk6
ON pspadm.psp_financial_transaction
USING BTREE (paycheck_split_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_fk9
ON pspadm.psp_financial_transaction
USING BTREE (current_transaction_state_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction_i3
ON pspadm.psp_financial_transaction
USING BTREE (transaction_type_fk ASC, company_fk ASC);



CREATE INDEX psp_financial_transaction_i4
ON pspadm.psp_financial_transaction
USING BTREE (law_fk ASC, transaction_type_fk ASC, company_fk ASC);



CREATE INDEX psp_financial_transaction_i5
ON pspadm.psp_financial_transaction
USING BTREE (company_fk ASC, current_transaction_state_fk ASC);



CREATE INDEX psp_financialtransaction_fk1
ON pspadm.psp_financial_transaction
USING BTREE (billing_detail_fk ASC, realm_id ASC);



CREATE INDEX psp_financialtransaction_fk2
ON pspadm.psp_financial_transaction
USING BTREE (original_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_financialtransaction_fk3
ON pspadm.psp_financial_transaction
USING BTREE (relatable_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction__fk1
ON pspadm.psp_financial_txn_action
USING BTREE (action_event_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction__fk2
ON pspadm.psp_financial_txn_action
USING BTREE (transaction_type_fk ASC, realm_id ASC);



CREATE INDEX psp_financial_transaction__fk3
ON pspadm.psp_financial_txn_action
USING BTREE (transaction_state_fk ASC, realm_id ASC);



CREATE INDEX psp_fin_txn_hold_fk_fin_txn
ON pspadm.psp_fintxn_onholdreason_assoc
USING BTREE (financial_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_fin_txn_hold_fk_hold
ON pspadm.psp_fintxn_onholdreason_assoc
USING BTREE (on_hold_reason_fk ASC, realm_id ASC);



CREATE INDEX psp_forecastdetail_fk1
ON pspadm.psp_forecast_detail
USING BTREE (forecast_fk ASC, realm_id ASC);



CREATE INDEX psp_form_template_fk1
ON pspadm.psp_form_template
USING BTREE (agency_fk ASC, realm_id ASC);



CREATE INDEX psp_form_template_fk2
ON pspadm.psp_form_template
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_fraudaddress_fk1
ON pspadm.psp_fraud_address
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_fraud_account_enc_i1
ON pspadm.psp_fraud_bank_account
USING BTREE (account_number_enc ASC);



CREATE INDEX psp_fraudbankaccount_acct_rtg
ON pspadm.psp_fraud_bank_account
USING BTREE (account_number_pt_bk ASC, routing_number ASC);



CREATE INDEX psp_fraudbankaccount_fk1
ON pspadm.psp_fraud_bank_account
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_fraudcompany_fk1
ON pspadm.psp_fraud_company
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_fraudcontact_fk1
ON pspadm.psp_fraud_contact
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_fraud_event_fk1
ON pspadm.psp_fraud_event
USING BTREE (company_event_fk ASC, realm_id ASC);



CREATE INDEX psp_fraud_event_fk2
ON pspadm.psp_fraud_event
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_fraud_event_fk3
ON pspadm.psp_fraud_event
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_fraud_event_fk4
ON pspadm.psp_fraud_event
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_fraud_event_i1
ON pspadm.psp_fraud_event
USING BTREE (event_status_cd ASC, event_time_stamp ASC);



CREATE INDEX psp_fraudvalue_fk1
ON pspadm.psp_fraud_value
USING BTREE (fraud_rule_fk ASC, realm_id ASC);



CREATE INDEX psp_fset_filing_detail_fk1
ON pspadm.psp_fset_filing_detail
USING BTREE (response_file_fk ASC, realm_id ASC);



CREATE INDEX psp_fset_filing_detail_fk2
ON pspadm.psp_fset_filing_detail
USING BTREE (parent_file_fk ASC, realm_id ASC);



CREATE INDEX psp_fset_filing_detail_fk3
ON pspadm.psp_fset_filing_detail
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_gems_ledger_posting_ru_fk1
ON pspadm.psp_gems_ledger_posting_rule
USING BTREE (ledger_account_fk ASC, realm_id ASC);



CREATE INDEX psp_gems_monthly_balance_fk1
ON pspadm.psp_gems_monthly_balance
USING BTREE (gems_ledger_posting_rule_fk ASC, realm_id ASC);



CREATE INDEX psp_gems_monthly_balance_fk2
ON pspadm.psp_gems_monthly_balance
USING BTREE (gems_upload_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_hours_worked_exception_fk1
ON pspadm.psp_hours_worked_exception
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_individual_fk1
ON pspadm.psp_individual
USING BTREE (mailing_address_fk ASC, realm_id ASC);



CREATE INDEX psp_individual_idx1
ON pspadm.psp_individual
USING BTREE (LOWER(first_name) ASC, LOWER(last_name) ASC);



CREATE INDEX psp_intuit_babatch_type_fi_fk1
ON pspadm.psp_intuit_ba_bt_ft
USING BTREE (intuit_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_intuit_bank_account_tr_fk1
ON pspadm.psp_intuit_bank_acc_txn_type
USING BTREE (intuit_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_intuit_bank_account_tr_fk2
ON pspadm.psp_intuit_bank_acc_txn_type
USING BTREE (transaction_type_fk ASC, realm_id ASC);



CREATE INDEX psp_intuit_bank_account_fk1
ON pspadm.psp_intuit_bank_account
USING BTREE (bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_intuit_shipper_info_fk1
ON pspadm.psp_intuit_shipper_info
USING BTREE (shipper_address_fk ASC, realm_id ASC);



CREATE INDEX psp_law_fk1
ON pspadm.psp_law
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_law_rate_range_fk1
ON pspadm.psp_law_rate_range
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_law_rate_value_fk1
ON pspadm.psp_law_rate_value
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_ledger_account_action_fk1
ON pspadm.psp_ledger_account_action
USING BTREE (action_event_fk ASC, realm_id ASC);



CREATE INDEX psp_ledger_account_action_fk2
ON pspadm.psp_ledger_account_action
USING BTREE (ledger_account_fk ASC, realm_id ASC);



CREATE INDEX psp_ledger_balance_i1
ON pspadm.psp_ledger_balance
USING BTREE (balance_date ASC);



CREATE INDEX psp_ledger_balance_u1
ON pspadm.psp_ledger_balance
USING BTREE (DATE(balance_date) ASC);



CREATE INDEX psp_ledger_balance_u2
ON pspadm.psp_ledger_balance
USING BTREE (company_fk ASC, ledger_account_fk ASC, DATE(balance_date) ASC);



CREATE INDEX psp_ledgerbalance_fk1
ON pspadm.psp_ledger_balance
USING BTREE (ledger_account_fk ASC, realm_id ASC);



CREATE INDEX psp_ledgerbalance_fk2
ON pspadm.psp_ledger_balance
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_ledger_operation_fk1
ON pspadm.psp_ledger_operation
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_ledger_operation_fk2
ON pspadm.psp_ledger_operation
USING BTREE (ledger_operation_job_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_billin_fk1
ON pspadm.psp_liab_check_billing_assoc
USING BTREE (billing_detail_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_billin_fk2
ON pspadm.psp_liab_check_billing_assoc
USING BTREE (liability_check_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_adjustment_fk1
ON pspadm.psp_liability_adjustment
USING BTREE (comp_adjust_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_adjustment_fk2
ON pspadm.psp_liability_adjustment
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_adjustment_fk3
ON pspadm.psp_liability_adjustment
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_adjustment_fk4
ON pspadm.psp_liability_adjustment
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_adjustment_fk6
ON pspadm.psp_liability_adjustment
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_adjustment_fk7
ON pspadm.psp_liability_adjustment
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_fk1
ON pspadm.psp_liability_check
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_fk2
ON pspadm.psp_liability_check
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_line_fk1
ON pspadm.psp_liability_check_line
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_line_fk2
ON pspadm.psp_liability_check_line
USING BTREE (liability_check_fk ASC, realm_id ASC);



CREATE INDEX psp_liability_check_line_fk4
ON pspadm.psp_liability_check_line
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_limit_value_fk1
ON pspadm.psp_limit_value
USING BTREE (limit_rule_fk ASC, realm_id ASC);



CREATE INDEX psp_mm_transaction_i1
ON pspadm.psp_money_movement_transaction
USING BTREE (company_fk ASC, status ASC, initiation_date ASC, due_date ASC, money_movement_payment_method ASC);



CREATE INDEX psp_mm_transaction_i10
ON pspadm.psp_money_movement_transaction
USING BTREE (payment_template_fk ASC, tax_payment_status ASC, payment_period_end ASC);



CREATE INDEX psp_mm_transaction_i2
ON pspadm.psp_money_movement_transaction
USING BTREE (initiation_date ASC, money_movement_payment_method ASC);



CREATE INDEX psp_mm_transaction_i3
ON pspadm.psp_money_movement_transaction
USING BTREE (tax_payment_status ASC, company_fk ASC);



CREATE INDEX psp_mm_transaction_i4
ON pspadm.psp_money_movement_transaction
USING BTREE (payment_template_fk ASC, company_fk ASC, status ASC);



CREATE INDEX psp_mm_transaction_i5
ON pspadm.psp_money_movement_transaction
USING BTREE (tax_payment_status ASC, money_movement_payment_method ASC, payment_period_end ASC);



CREATE INDEX psp_money_movement_transac_fk1
ON pspadm.psp_money_movement_transaction
USING BTREE (deposit_frequency_fk ASC, realm_id ASC);



CREATE INDEX psp_money_movement_transac_fk2
ON pspadm.psp_money_movement_transaction
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_money_movement_transac_fk3
ON pspadm.psp_money_movement_transaction
USING BTREE (offload_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_money_movement_transac_fk6
ON pspadm.psp_money_movement_transaction
USING BTREE (payment_frequency_fk ASC, realm_id ASC);



CREATE INDEX psp_moneymovementtransacti_fk1
ON pspadm.psp_money_movement_transaction
USING BTREE (original_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_nachafile_fk1
ON pspadm.psp_nachafile
USING BTREE (offload_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_offer_price_fk1
ON pspadm.psp_offer_price
USING BTREE (offer_fk ASC, realm_id ASC);



CREATE INDEX psp_offer_svcchg_fk_offer
ON pspadm.psp_offer_svcchg_assoc
USING BTREE (offer_fk ASC, realm_id ASC);



CREATE INDEX psp_offer_svcchg_fk_svcchg
ON pspadm.psp_offer_svcchg_assoc
USING BTREE (offering_service_charge_fk ASC, realm_id ASC);



CREATE INDEX psp_offering_fk1
ON pspadm.psp_offering
USING BTREE (limit_rule_fk ASC, realm_id ASC);



CREATE INDEX psp_offering_fk2
ON pspadm.psp_offering
USING BTREE (fraud_rule_fk ASC, realm_id ASC);



CREATE INDEX psp_offering_svcchg_fk
ON pspadm.psp_offering_svcchg
USING BTREE (offering_svcchg_grp_fk ASC, realm_id ASC);



CREATE INDEX psp_offering_svcchg_grp_fk
ON pspadm.psp_offering_svcchg_grp
USING BTREE (offering_fk ASC, realm_id ASC);



CREATE INDEX psp_offload_batch_fk1
ON pspadm.psp_offload_batch
USING BTREE (offload_group_fk ASC, realm_id ASC);



CREATE INDEX psp_on_hold_reason_fk1
ON pspadm.psp_on_hold_reason
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_pay_item_fk1
ON pspadm.psp_pay_item
USING BTREE (liability_adjustment_fk ASC, realm_id ASC);



CREATE INDEX psp_pay_item_fk2
ON pspadm.psp_pay_item
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_fk1
ON pspadm.psp_paycheck
USING BTREE (d_d_employee_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_fk2
ON pspadm.psp_paycheck
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_fk3
ON pspadm.psp_paycheck
USING BTREE (comp_adjust_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_fk4
ON pspadm.psp_paycheck
USING BTREE (source_employee_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_i1
ON pspadm.psp_paycheck
USING BTREE (created_date ASC);



CREATE INDEX psp_paycheck_u1
ON pspadm.psp_paycheck
USING BTREE (source_paycheck_id ASC, payroll_run_fk ASC);



CREATE INDEX psp_paycheck_split_fk1
ON pspadm.psp_paycheck_split
USING BTREE (employee_bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_split_fk2
ON pspadm.psp_paycheck_split
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_split_fk3
ON pspadm.psp_paycheck_split
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_split_i1
ON pspadm.psp_paycheck_split
USING BTREE (created_date ASC);



CREATE UNIQUE INDEX psp_paycheck_split_u1
ON pspadm.psp_paycheck_split
USING BTREE (source_dd_txn_id ASC, paycheck_fk ASC);



CREATE INDEX psp_paycheckusage_fk1
ON pspadm.psp_paycheck_usage
USING BTREE (bill_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheckusage_fk2
ON pspadm.psp_paycheck_usage
USING BTREE (employee_usage_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_usage_hist_fk1
ON pspadm.psp_paycheck_usage_hist
USING BTREE (employee_usage_fk ASC, realm_id ASC);



CREATE INDEX psp_paycheck_usage_hist_fk2
ON pspadm.psp_paycheck_usage_hist
USING BTREE (paycheck_usage_fk ASC, realm_id ASC);



CREATE INDEX psp_payee_fk1
ON pspadm.psp_payee
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_payee_fk2
ON pspadm.psp_payee
USING BTREE (mailing_address_fk ASC, realm_id ASC);



CREATE INDEX psp_payee_bank_account_fk1
ON pspadm.psp_payee_bank_account
USING BTREE (payee_fk ASC, realm_id ASC);



CREATE INDEX psp_payee_bank_account_fk2
ON pspadm.psp_payee_bank_account
USING BTREE (bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_batch_assoc_fk1
ON pspadm.psp_payment_batch_assoc
USING BTREE (agency_check_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_batch_assoc_fk2
ON pspadm.psp_payment_batch_assoc
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_paymentmethodrequireme_fk1
ON pspadm.psp_payment_method_requirement
USING BTREE (pmt_template_pmt_method_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_fk1
ON pspadm.psp_payment_template
USING BTREE (agency_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_agenc_fk1
ON pspadm.psp_payment_template_agency_id
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_payroll_item_taxable_t_fk1
ON pspadm.psp_payroll_item_taxable_to
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_payroll_item_taxable_t_fk2
ON pspadm.psp_payroll_item_taxable_to
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_payroll_run_eecalctoken
ON pspadm.psp_payroll_run
USING BTREE (e_e_calculation_token ASC);



CREATE INDEX psp_payroll_run_i1
ON pspadm.psp_payroll_run
USING BTREE (company_fk ASC, paycheck_date ASC);



CREATE INDEX psp_payroll_run_i2
ON pspadm.psp_payroll_run
USING BTREE (payroll_run_status ASC);



CREATE INDEX psp_payroll_run_i3
ON pspadm.psp_payroll_run
USING BTREE (company_fk ASC, paycheck_settlement_date ASC);



CREATE INDEX psp_payroll_run_i4
ON pspadm.psp_payroll_run
USING BTREE (usage_billing_token ASC);



CREATE UNIQUE INDEX psp_payroll_run_u1
ON pspadm.psp_payroll_run
USING BTREE (company_fk ASC, source_pay_run_id ASC);



CREATE INDEX psp_pr_procesed_by_frd_btchjob
ON pspadm.psp_payroll_run
USING BTREE (processed_by_fraud_batch_job ASC);



CREATE INDEX psp_payroll_run_action_fk1
ON pspadm.psp_payroll_run_action
USING BTREE (action_event_fk ASC, realm_id ASC);



CREATE INDEX psp_payroll_subtype_fk1
ON pspadm.psp_payroll_subtype
USING BTREE (offering_fk ASC, realm_id ASC);



CREATE INDEX psp_paystub_fk1
ON pspadm.psp_paystub
USING BTREE (pstub_employer_info_fk ASC, realm_id ASC);



CREATE INDEX psp_paystub_fk2
ON pspadm.psp_paystub
USING BTREE (pstub_employee_info_fk ASC, realm_id ASC);



CREATE INDEX psp_paystub_fk3
ON pspadm.psp_paystub
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_bank__fk1
ON pspadm.psp_pmt_template_bankaccount
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_bank__fk2
ON pspadm.psp_pmt_template_bankaccount
USING BTREE (bank_account_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_frequ_fk1
ON pspadm.psp_pmt_template_frequency
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_payme_fk1
ON pspadm.psp_pmt_template_paymentmethod
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_check_fk1
ON pspadm.psp_pmttemplate_chkinfo_assoc
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_check_fk2
ON pspadm.psp_pmttemplate_chkinfo_assoc
USING BTREE (pmttemplate_printedchkinfo_fk ASC, realm_id ASC);



CREATE INDEX psp_payment_template_print_fk1
ON pspadm.psp_pmttemplate_printedchkinfo
USING BTREE (address_fk ASC, realm_id ASC);



CREATE INDEX psp_posting_rule_fk1
ON pspadm.psp_posting_rule
USING BTREE (ledger_account_fk ASC, realm_id ASC);



CREATE INDEX psp_posting_rule_fk2
ON pspadm.psp_posting_rule
USING BTREE (transaction_state_fk ASC, realm_id ASC);



CREATE INDEX psp_posting_rule_fk3
ON pspadm.psp_posting_rule
USING BTREE (transaction_type_fk ASC, realm_id ASC);



CREATE INDEX psp_prior_payment_submissi_fk1
ON pspadm.psp_prior_payment_submission
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_propertyaudit_fk1
ON pspadm.psp_property_audit
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_dditem_fk1
ON pspadm.psp_pstub_dditem
USING BTREE (paystub_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_employee_info_fk1
ON pspadm.psp_pstub_employee_info
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_employee_info_fk2
ON pspadm.psp_pstub_employee_info
USING BTREE (pstub_address_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_employee_prefere_fk1
ON pspadm.psp_pstub_employee_preference
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_employer_info_fk1
ON pspadm.psp_pstub_employer_info
USING BTREE (pstub_address_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_msg_fk1
ON pspadm.psp_pstub_msg
USING BTREE (paystub_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_paid_timeoff_ite_fk1
ON pspadm.psp_pstub_paid_timeoff_item
USING BTREE (paystub_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_pay_item_fk1
ON pspadm.psp_pstub_pay_item
USING BTREE (paystub_fk ASC, realm_id ASC);



CREATE INDEX psp_pstub_state_tax_info_fk1
ON pspadm.psp_pstub_state_tax_info
USING BTREE (pstub_employer_info_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_employee_info_fk1
ON pspadm.psp_qbdt_employee_info
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_employee_info_fk2
ON pspadm.psp_qbdt_employee_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_paycheck_info_fk1
ON pspadm.psp_qbdt_paycheck_info
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_paycheck_info_fk2
ON pspadm.psp_qbdt_paycheck_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdtpchkinfo_comp_fk_token
ON pspadm.psp_qbdt_paycheck_info
USING BTREE (company_fk ASC, token ASC);



CREATE INDEX psp_qbdt_payline_info_fk1
ON pspadm.psp_qbdt_payline_info
USING BTREE (employer_contribution_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payline_info_fk2
ON pspadm.psp_qbdt_payline_info
USING BTREE (compensation_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payline_info_fk3
ON pspadm.psp_qbdt_payline_info
USING BTREE (deduction_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payline_info_fk4
ON pspadm.psp_qbdt_payline_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_item_info_fk1
ON pspadm.psp_qbdt_payroll_item_info
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_item_info_fk2
ON pspadm.psp_qbdt_payroll_item_info
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_item_info_fk3
ON pspadm.psp_qbdt_payroll_item_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_transacti_fk2
ON pspadm.psp_qbdt_payroll_trans_line
USING BTREE (qbdt_payroll_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_transline_fk1
ON pspadm.psp_qbdt_payroll_trans_line
USING BTREE (company_payroll_item_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_trans_fk1
ON pspadm.psp_qbdt_payroll_transaction
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_trans_fk2
ON pspadm.psp_qbdt_payroll_transaction
USING BTREE (prior_payment_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_transacti_fk1
ON pspadm.psp_qbdt_payroll_transaction
USING BTREE (comp_adjust_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_payroll_transacti_fk3
ON pspadm.psp_qbdt_payroll_transaction
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk1
ON pspadm.psp_qbdt_transaction_info
USING BTREE (liability_check_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk2
ON pspadm.psp_qbdt_transaction_info
USING BTREE (liability_check_line_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk3
ON pspadm.psp_qbdt_transaction_info
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk4
ON pspadm.psp_qbdt_transaction_info
USING BTREE (financial_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk5
ON pspadm.psp_qbdt_transaction_info
USING BTREE (comp_adjust_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk6
ON pspadm.psp_qbdt_transaction_info
USING BTREE (liability_adjustment_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk7
ON pspadm.psp_qbdt_transaction_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk8
ON pspadm.psp_qbdt_transaction_info
USING BTREE (qbdt_payroll_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_transaction_info_fk9
ON pspadm.psp_qbdt_transaction_info
USING BTREE (qbdt_payroll_trans_line_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdttransactioninfo_fk1
ON pspadm.psp_qbdt_transaction_info
USING BTREE (prior_payment_submission_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_unprocessed_reque_fk1
ON pspadm.psp_qbdt_unprocessed_request
USING BTREE (source_system_transmission_id ASC, realm_id ASC);



CREATE INDEX psp_qbdt_unprocessed_reque_fk2
ON pspadm.psp_qbdt_unprocessed_request
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_qbdt_unprocessed_reque_i1
ON pspadm.psp_qbdt_unprocessed_request
USING BTREE (status ASC);



CREATE INDEX psp_quickbooks_info_fk1
ON pspadm.psp_quickbooks_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_rafenrollment_fk1
ON pspadm.psp_rafenrollment
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_rafenrollment_i1
ON pspadm.psp_rafenrollment
USING BTREE (status ASC);



CREATE INDEX psp_rafenrollment_detail_fk1
ON pspadm.psp_rafenrollment_detail
USING BTREE (r_a_f_enrollment_fk ASC, realm_id ASC);



CREATE INDEX psp_rafenrollment_detail_fk2
ON pspadm.psp_rafenrollment_detail
USING BTREE (enrollment_file_fk ASC, realm_id ASC);



CREATE INDEX psp_rafenrollment_detail_fk3
ON pspadm.psp_rafenrollment_detail
USING BTREE (delete_file_fk ASC, realm_id ASC);



CREATE INDEX psp_reporting_agent_fk1
ON pspadm.psp_reporting_agent
USING BTREE (address_fk ASC, realm_id ASC);



CREATE INDEX psp_role_sub_status_fk1
ON pspadm.psp_role_sub_status
USING BTREE (auth_role_fk ASC, realm_id ASC);



CREATE INDEX psp_role_sub_status_fk2
ON pspadm.psp_role_sub_status
USING BTREE (service_sub_status_fk ASC, realm_id ASC);



CREATE INDEX psp_second_offload_fk1
ON pspadm.psp_second_offload
USING BTREE (offload_group_fk ASC, realm_id ASC);



CREATE INDEX psp_serv_stat_txn_sku_type_fk1
ON pspadm.psp_serv_stat_txn_sku_type
USING BTREE (service_sub_status_fk ASC, realm_id ASC);



CREATE INDEX psp_serv_stat_txn_sku_type_fk2
ON pspadm.psp_serv_stat_txn_sku_type
USING BTREE (transaction_type_fk ASC, realm_id ASC);



CREATE INDEX psp_service_sub_status_fk1
ON pspadm.psp_service_sub_status
USING BTREE (service_status_fk ASC, realm_id ASC);



CREATE INDEX psp_smsmigration_fk1
ON pspadm.psp_smsmigration
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_source_system_law_asso_fk1
ON pspadm.psp_source_system_law_assoc
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_source_system_law_asso_fk2
ON pspadm.psp_source_system_law_assoc
USING BTREE (source_system_fk ASC, realm_id ASC);



CREATE INDEX psp_source_system_printed__fk1
ON pspadm.psp_sourcesys_printedchk_info
USING BTREE (address_fk ASC, realm_id ASC);



CREATE INDEX psp_state_edi_tax_file_fk2
ON pspadm.psp_state_edi_tax_file
USING BTREE (ack_file_fk ASC, realm_id ASC);



CREATE INDEX psp_strpt_tmpfreq_fk_pmttmpfrq
ON pspadm.psp_state_report_assoc
USING BTREE (payment_template_frequency_fk ASC, realm_id ASC);



CREATE INDEX psp_strpt_tmpfreq_fk_strptout
ON pspadm.psp_state_report_assoc
USING BTREE (state_report_output_fk ASC, realm_id ASC);



CREATE INDEX psp_suicredits_job_fk1
ON pspadm.psp_suicredits_job
USING BTREE (payment_template_fk ASC, realm_id ASC);



CREATE INDEX psp_offeringservicechargep_fk1
ON pspadm.psp_svcchgprice
USING BTREE (offering_service_charge_fk ASC, realm_id ASC);



CREATE INDEX psp_svcstat_srcsys_fk_srcsys
ON pspadm.psp_svcstat_srcsys_assoc
USING BTREE (source_system_fk ASC, realm_id ASC);



CREATE INDEX psp_svcstat_srcsys_fk_svcstat
ON pspadm.psp_svcstat_srcsys_assoc
USING BTREE (service_sub_status_fk ASC, realm_id ASC);



CREATE INDEX psp_svcstat_svc_fk_svc
ON pspadm.psp_svcstat_svc_assoc
USING BTREE (service_fk ASC, realm_id ASC);



CREATE INDEX psp_svcstat_svc_fk_svcstat
ON pspadm.psp_svcstat_svc_assoc
USING BTREE (service_sub_status_fk ASC, realm_id ASC);



CREATE INDEX psp_svcstat_cap_fk_cap
ON pspadm.psp_svcstat_syscap_assoc
USING BTREE (system_capability_fk ASC, realm_id ASC);



CREATE INDEX psp_svcstat_cap_fk_svcstat
ON pspadm.psp_svcstat_syscap_assoc
USING BTREE (service_sub_status_fk ASC, realm_id ASC);



CREATE UNIQUE INDEX psp_system_parameter_spcode_u1
ON pspadm.psp_system_parameter
USING BTREE (system_parameter_cd ASC);



CREATE INDEX psp_tax_fk1
ON pspadm.psp_tax
USING BTREE (law_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_fk2
ON pspadm.psp_tax
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_fk3
ON pspadm.psp_tax
USING BTREE (company_law_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_fk5
ON pspadm.psp_tax
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_credits9061_fk1
ON pspadm.psp_tax_credits9061
USING BTREE (tax_credits_application_fk ASC, realm_id ASC);



CREATE INDEX psp_taxpaymentonholdreason_fk1
ON pspadm.psp_tax_payment_on_hold_reason
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_penalty_interest_fk1
ON pspadm.psp_tax_penalty_interest
USING BTREE (company_agency_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_table_misc_data_fk1
ON pspadm.psp_tax_table_misc_data
USING BTREE (employee_tax_fk ASC, realm_id ASC);



CREATE INDEX psp_tax_table_misc_data_fk2
ON pspadm.psp_tax_table_misc_data
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX "PSP_TP401K_BATCH_EE_FK2 "
ON pspadm.psp_tp401k_batch_employee
USING BTREE (employee_fk ASC, realm_id ASC);



CREATE INDEX psp_tp401k_batch_ee_fk1
ON pspadm.psp_tp401k_batch_employee
USING BTREE (third_party401k_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_third_party401k_batch__fk1
ON pspadm.psp_tp401k_batch_paycheck
USING BTREE (third_party401k_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_third_party401k_batch__fk2
ON pspadm.psp_tp401k_batch_paycheck
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_tp401k_pchk_pchk_fk
ON pspadm.psp_tp401k_paycheck
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_tp401k_pend_state_pchk_fk
ON pspadm.psp_tp401k_paycheck_pending
USING BTREE (third_party401k_paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_tp401k_pchk_state_pchk_fk
ON pspadm.psp_tp401k_paycheck_state
USING BTREE (third_party401k_paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_transaction_offload_ba_fk1
ON pspadm.psp_transaction_offload_batch
USING BTREE (financial_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_transaction_offload_ba_fk2
ON pspadm.psp_transaction_offload_batch
USING BTREE (offload_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_transaction_response_fk1
ON pspadm.psp_transaction_response
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_transaction_return_fk1
ON pspadm.psp_transaction_return
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_transaction_return_fk2
ON pspadm.psp_transaction_return
USING BTREE (return_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_transaction_return_i1
ON pspadm.psp_transaction_return
USING BTREE (company_fk ASC, return_status_cd ASC, bank_return_cd ASC);



CREATE INDEX psp_transmissionpayrollrun_fk1
ON pspadm.psp_transmission_payroll_run
USING BTREE (source_system_transmission_id ASC, realm_id ASC);



CREATE INDEX psp_transmissionpayrollrun_fk2
ON pspadm.psp_transmission_payroll_run
USING BTREE (payroll_run_fk ASC, realm_id ASC);



CREATE INDEX psp_txntype_svc_fk_svc
ON pspadm.psp_txntype_service_assoc
USING BTREE (service_fk ASC, realm_id ASC);



CREATE INDEX psp_txntype_svc_fk_txntype
ON pspadm.psp_txntype_service_assoc
USING BTREE (transaction_type_fk ASC, realm_id ASC);



CREATE UNIQUE INDEX psp_usage_period_u1
ON pspadm.psp_usage_period
USING BTREE (company_usage_fk ASC, start_date ASC, end_date ASC);



CREATE INDEX psp_usageperiod_fk1
ON pspadm.psp_usage_period
USING BTREE (company_usage_fk ASC, realm_id ASC);



CREATE INDEX psp_user_setting_fk1
ON pspadm.psp_user_setting
USING BTREE (user_preference_fk ASC, realm_id ASC);



CREATE INDEX psp_usersetting_fk1
ON pspadm.psp_user_setting
USING BTREE (auth_user_fk ASC, realm_id ASC);



CREATE INDEX psp_vmp_employee_info_fk1
ON pspadm.psp_vmp_employee_info
USING BTREE (company_fk ASC, realm_id ASC);



CREATE INDEX psp_voided_check_fk1
ON pspadm.psp_voided_check
USING BTREE (accounting_report_file_fk ASC, realm_id ASC);



CREATE INDEX psp_voided_check_fk2
ON pspadm.psp_voided_check
USING BTREE (money_movement_transaction_fk ASC, realm_id ASC);



CREATE INDEX psp_voided_check_fk3
ON pspadm.psp_voided_check
USING BTREE (agency_check_batch_fk ASC, realm_id ASC);



CREATE INDEX psp_wc_pchk_pchk_fk
ON pspadm.psp_wc_paycheck
USING BTREE (paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_wc_pend_state_pchk_fk
ON pspadm.psp_wc_paycheck_pending
USING BTREE (workers_comp_paycheck_fk ASC, realm_id ASC);



CREATE INDEX psp_wc_pchk_state_pchk_fk
ON pspadm.psp_wc_paycheck_state
USING BTREE (workers_comp_paycheck_fk ASC, realm_id ASC);



CREATE INDEX sys_mtable_000077dbf_ind_1
ON pspadm.sys_export_schema_01
USING BTREE (object_schema ASC, object_name ASC, object_type ASC);



CREATE INDEX sys_mtable_000077dbf_ind_2
ON pspadm.sys_export_schema_01
USING BTREE (base_process_order ASC);



CREATE INDEX sys_mtable_00007c13c_ind_1
ON pspadm.sys_export_schema_02
USING BTREE (object_schema ASC, object_name ASC, object_type ASC);



CREATE INDEX sys_mtable_00007c13c_ind_2
ON pspadm.sys_export_schema_02
USING BTREE (base_process_order ASC);



CREATE UNIQUE INDEX tpsql_idx
ON pspadm.toad_plan_sql
USING BTREE (statement_id ASC);




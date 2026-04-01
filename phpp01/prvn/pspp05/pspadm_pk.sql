\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;
--CONSTRAINTS
ALTER TABLE pspadm.gg_heartbeat
ADD PRIMARY KEY (source);

ALTER TABLE pspadm.psp_accounting_report_file
ADD PRIMARY KEY (accounting_report_file_seq);


ALTER TABLE pspadm.psp_ach_transaction_code
ADD PRIMARY KEY (transaction_code);

ALTER TABLE pspadm.psp_achenrollment
ADD PRIMARY KEY (achenrollment_seq);

ALTER TABLE pspadm.psp_achenrollment_detail
ADD PRIMARY KEY (achenrollment_detail_seq);

ALTER TABLE pspadm.psp_achenrollment_file
ADD PRIMARY KEY (achenrollment_file_seq);

ALTER TABLE pspadm.psp_action_event
ADD PRIMARY KEY (code);

ALTER TABLE pspadm.psp_additional_filing_amount
ADD PRIMARY KEY (name);

ALTER TABLE pspadm.psp_address
ADD PRIMARY KEY (address_seq);

ALTER TABLE pspadm.psp_ade_law_map
ADD PRIMARY KEY (ade_law_map_id);

ALTER TABLE pspadm.psp_agency
ADD PRIMARY KEY (agency_id);

ALTER TABLE pspadm.psp_agency_check_batch
ADD PRIMARY KEY (agency_check_batch_seq);

ALTER TABLE pspadm.psp_agency_id_requirement
ADD PRIMARY KEY (agency_id_requirement_seq);

ALTER TABLE pspadm.psp_agency_rate_request
ADD PRIMARY KEY (agency_rate_request_seq);


ALTER TABLE pspadm.psp_annual_billing_batch
ADD PRIMARY KEY (annual_billing_batch_seq);

ALTER TABLE pspadm.psp_annual_billing_item
ADD PRIMARY KEY (annual_billing_item_seq);


ALTER TABLE pspadm.psp_applied_database_patch
ADD PRIMARY KEY (applied_database_patch_seq);

ALTER TABLE pspadm.psp_archive_record
ADD PRIMARY KEY (archive_record_seq);



ALTER TABLE pspadm.psp_assisted_bundle_bill
ADD PRIMARY KEY (assisted_bundle_bill_seq);

ALTER TABLE pspadm.psp_asst_bundle_bill_detail
ADD PRIMARY KEY (asst_bundle_bill_detail_seq);



ALTER TABLE pspadm.psp_asst_bundle_comp_usage
ADD PRIMARY KEY (asst_bundle_comp_usage_seq);


ALTER TABLE pspadm.psp_atfdata_extract_batch
ADD PRIMARY KEY (atfdata_extract_batch_seq);



ALTER TABLE pspadm.psp_atfdata_extract_file
ADD PRIMARY KEY (atfdata_extract_file_seq);

ALTER TABLE pspadm.psp_atfpayments_to_process
ADD PRIMARY KEY (atfpayments_to_process_seq);

ALTER TABLE pspadm.psp_atfpayrolls_to_process
ADD PRIMARY KEY (atfpayrolls_to_process_seq);

ALTER TABLE pspadm.psp_auth_domain
ADD PRIMARY KEY (domain_id);



ALTER TABLE pspadm.psp_auth_operation
ADD PRIMARY KEY (operation_id);

ALTER TABLE pspadm.psp_auth_role
ADD PRIMARY KEY (auth_role_seq);

ALTER TABLE pspadm.psp_auth_user
ADD PRIMARY KEY (auth_user_seq);

ALTER TABLE pspadm.psp_auth_user_auth_role__assoc
ADD PRIMARY KEY (auth_user_fk, auth_role_fk);

ALTER TABLE pspadm.psp_authrole_operation_assoc
ADD PRIMARY KEY (auth_role_fk, auth_operation_fk);



ALTER TABLE pspadm.psp_bank_account
ADD PRIMARY KEY (bank_account_seq);

ALTER TABLE pspadm.psp_bank_holiday
ADD PRIMARY KEY (bank_holiday_date);

ALTER TABLE pspadm.psp_batch_job_audit_log
ADD PRIMARY KEY (batch_job_audit_log_seq);

ALTER TABLE pspadm.psp_batch_job_parameter
ADD PRIMARY KEY (id);


ALTER TABLE pspadm.psp_batch_job_setup
ADD PRIMARY KEY (job_type);




ALTER TABLE pspadm.psp_batch_job_status
ADD PRIMARY KEY (batch_job_status_seq);

ALTER TABLE pspadm.psp_bill
ADD PRIMARY KEY (bill_seq);



ALTER TABLE pspadm.psp_bill_payment
ADD PRIMARY KEY (bill_payment_seq);

ALTER TABLE pspadm.psp_bill_payment_split
ADD PRIMARY KEY (bill_payment_split_seq);



ALTER TABLE pspadm.psp_billing_detail
ADD PRIMARY KEY (billing_detail_seq);

ALTER TABLE pspadm.psp_bpcompany_service_info
ADD PRIMARY KEY (bpcompany_service_info_seq);

ALTER TABLE pspadm.psp_cdcompany_service_info
ADD PRIMARY KEY (cdcompany_service_info_seq);


ALTER TABLE pspadm.psp_check_print_batch
ADD PRIMARY KEY (check_print_batch_seq);


ALTER TABLE pspadm.psp_check_print_paycheck
ADD PRIMARY KEY (check_print_paycheck_seq);

ALTER TABLE pspadm.psp_check_print_signature
ADD PRIMARY KEY (check_print_signature_seq);


ALTER TABLE pspadm.psp_collection_stage
ADD PRIMARY KEY (collection_stage_code);

ALTER TABLE pspadm.psp_comp_adjust_submission
ADD PRIMARY KEY (comp_adjust_submission_seq);

ALTER TABLE pspadm.psp_comp_pmt_template_agencyid
ADD PRIMARY KEY (comp_pmt_template_agencyid_seq);


ALTER TABLE pspadm.psp_comp_pmttemplate_pmtmethod
ADD PRIMARY KEY (comp_pmttemplate_pmtmethod_seq);



ALTER TABLE pspadm.psp_company
ADD PRIMARY KEY (company_seq);

ALTER TABLE pspadm.psp_company_additional_info
ADD PRIMARY KEY (company_additional_info_seq);

ALTER TABLE pspadm.psp_company_agency
ADD PRIMARY KEY (company_agency_seq);



ALTER TABLE pspadm.psp_company_bank_account
ADD PRIMARY KEY (company_bank_account_seq);

ALTER TABLE pspadm.psp_company_consent
ADD PRIMARY KEY (company_consent_seq);

ALTER TABLE pspadm.psp_company_daily_liability
ADD PRIMARY KEY (company_daily_liability_seq);

ALTER TABLE pspadm.psp_company_event
ADD PRIMARY KEY (company_fk, company_event_seq);


ALTER TABLE pspadm.psp_company_event_detail
ADD PRIMARY KEY (company_fk, company_event_detail_seq);


ALTER TABLE pspadm.psp_company_event_email
ADD PRIMARY KEY (company_event_email_seq);

ALTER TABLE pspadm.psp_company_event_email_param
ADD PRIMARY KEY (company_fk, company_event_email_param_seq);

ALTER TABLE pspadm.psp_company_filing_amount
ADD PRIMARY KEY (company_filing_amount_seq);


ALTER TABLE pspadm.psp_company_law
ADD PRIMARY KEY (company_law_seq);


ALTER TABLE pspadm.psp_company_law_rate
ADD PRIMARY KEY (company_law_rate_seq);

ALTER TABLE pspadm.psp_company_note
ADD PRIMARY KEY (company_note_seq);

ALTER TABLE pspadm.psp_company_offer
ADD PRIMARY KEY (company_offer_seq);

ALTER TABLE pspadm.psp_company_offering
ADD PRIMARY KEY (company_offering_seq);

ALTER TABLE pspadm.psp_company_paycheck_batch
ADD PRIMARY KEY (company_paycheck_batch_seq);



ALTER TABLE pspadm.psp_company_payroll_item
ADD PRIMARY KEY (company_payroll_item_seq);


ALTER TABLE pspadm.psp_company_pin
ADD PRIMARY KEY (company_pin_seq);


ALTER TABLE pspadm.psp_company_rate_request
ADD PRIMARY KEY (company_rate_request_seq);


ALTER TABLE pspadm.psp_company_service
ADD PRIMARY KEY (company_service_seq);

ALTER TABLE pspadm.psp_company_service_bank_acct
ADD PRIMARY KEY (company_service_bank_acct_seq);



ALTER TABLE pspadm.psp_company_tfssubmission
ADD PRIMARY KEY (company_tfssubmission_seq);


ALTER TABLE pspadm.psp_company_usage
ADD PRIMARY KEY (company_usage_seq);

ALTER TABLE pspadm.psp_companyagency_frmtemplate
ADD PRIMARY KEY (companyagency_frmtemplate_seq);

ALTER TABLE pspadm.psp_companyagency_pmttemplate
ADD PRIMARY KEY (companyagency_pmttemplate_seq);

ALTER TABLE pspadm.psp_compensation
ADD PRIMARY KEY (company_fk, compensation_seq);


ALTER TABLE pspadm.psp_contact
ADD PRIMARY KEY (contact_seq);

ALTER TABLE pspadm.psp_ddcompany_service_info
ADD PRIMARY KEY (ddcompany_service_info_seq);

ALTER TABLE pspadm.psp_deduction
ADD PRIMARY KEY (company_fk, deduction_seq);

ALTER TABLE pspadm.psp_deleted_record
ADD PRIMARY KEY (deleted_record_seq);

ALTER TABLE pspadm.psp_dep_freq_ledger_operation
ADD PRIMARY KEY (dep_freq_ledger_operation_seq);


ALTER TABLE pspadm.psp_deposit_frequency
ADD PRIMARY KEY (deposit_frequency_code);



ALTER TABLE pspadm.psp_deposit_frequency_file
ADD PRIMARY KEY (deposit_frequency_file_seq);



ALTER TABLE pspadm.psp_deposit_frequency_file_rec
ADD PRIMARY KEY (deposit_frequency_file_rec_seq);


ALTER TABLE pspadm.psp_deposit_frequency_req
ADD PRIMARY KEY (deposit_frequency_req_seq);


ALTER TABLE pspadm.psp_dicrfile
ADD PRIMARY KEY (dicrfile_seq);

ALTER TABLE pspadm.psp_disburse_advice
ADD PRIMARY KEY (disburse_advice_seq);

ALTER TABLE pspadm.psp_disburse_advice_tax_liab
ADD PRIMARY KEY (company_fk, disburse_advice_tax_liab_seq);


ALTER TABLE pspadm.psp_edi_payment_detail
ADD PRIMARY KEY (edi_payment_detail_seq);



ALTER TABLE pspadm.psp_edi_tax_file
ADD PRIMARY KEY (edi_tax_file_seq);

ALTER TABLE pspadm.psp_ee_payrollitem_qtrtotals
ADD PRIMARY KEY (ee_payrollitem_qtrtotals_seq);

ALTER TABLE pspadm.psp_effective_deposit_freq
ADD PRIMARY KEY (effective_deposit_freq_seq);


ALTER TABLE pspadm.psp_eftps_enrollment
ADD PRIMARY KEY (eftps_enrollment_seq);


ALTER TABLE pspadm.psp_eftps_enrollment_detail
ADD PRIMARY KEY (eftps_enrollment_detail_seq);


ALTER TABLE pspadm.psp_eftps_file
ADD PRIMARY KEY (eftps_file_seq);


ALTER TABLE pspadm.psp_eftps_payment_detail
ADD PRIMARY KEY (eftps_payment_detail_seq);


ALTER TABLE pspadm.psp_emp_totals_payroll_run
ADD PRIMARY KEY (emp_totals_payroll_run_seq);



ALTER TABLE pspadm.psp_employee
ADD PRIMARY KEY (employee_seq);


ALTER TABLE pspadm.psp_employee_accrual
ADD PRIMARY KEY (employee_accrual_seq);



ALTER TABLE pspadm.psp_employee_bank_account
ADD PRIMARY KEY (employee_bank_account_seq);

ALTER TABLE pspadm.psp_employee_custom_field
ADD PRIMARY KEY (employee_custom_field_seq);

ALTER TABLE pspadm.psp_employee_law_qtr_totals
ADD PRIMARY KEY (employee_law_qtr_totals_seq);



ALTER TABLE pspadm.psp_employee_payroll_item
ADD PRIMARY KEY (employee_payroll_item_seq);



ALTER TABLE pspadm.psp_employee_tax
ADD PRIMARY KEY (employee_tax_seq);

ALTER TABLE pspadm.psp_employee_usage
ADD PRIMARY KEY (employee_usage_seq);

ALTER TABLE pspadm.psp_employee_w2_totals
ADD PRIMARY KEY (employee_w2_totals_seq);


ALTER TABLE pspadm.psp_employee_wage_plan
ADD PRIMARY KEY (employee_wage_plan_seq);

ALTER TABLE pspadm.psp_employer_contribution
ADD PRIMARY KEY (employer_contribution_seq);

ALTER TABLE pspadm.psp_employer_preference
ADD PRIMARY KEY (employer_preference_seq);


ALTER TABLE pspadm.psp_entitlement
ADD PRIMARY KEY (entitlement_seq);



ALTER TABLE pspadm.psp_entitlement_code
ADD PRIMARY KEY (entitlement_code_seq);


ALTER TABLE pspadm.psp_entitlement_code_offering
ADD PRIMARY KEY (entitlement_code_offering_seq);

ALTER TABLE pspadm.psp_entitlement_unit
ADD PRIMARY KEY (entitlement_unit_seq);

ALTER TABLE pspadm.psp_entity_change
ADD PRIMARY KEY (entity_change_seq);


ALTER TABLE pspadm.psp_entity_update_hist
ADD PRIMARY KEY (entity_update_seq);

ALTER TABLE pspadm.psp_entry_detail_record
ADD CONSTRAINT sys_c008379 PRIMARY KEY (company_fk, entry_detail_record_seq);


ALTER TABLE pspadm.psp_event_as400_sync
ADD PRIMARY KEY (event_as400_sync_seq);

--psp_event_detail_type

ALTER TABLE pspadm.psp_event_detail_type
ADD PRIMARY KEY (event_detail_type_cd);


ALTER TABLE pspadm.psp_event_log
ADD PRIMARY KEY (event_log_seq);


ALTER TABLE pspadm.psp_event_type
ADD PRIMARY KEY (event_type_cd);

ALTER TABLE pspadm.psp_evttp_srcsys_assoc
ADD PRIMARY KEY (interesting_event_types_fk, source_system_fk);


ALTER TABLE pspadm.psp_failed_payroll_run
ADD PRIMARY KEY (failed_payroll_run_seq);


ALTER TABLE pspadm.psp_fee
ADD PRIMARY KEY (fee_seq);

ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT sys_c008683 PRIMARY KEY (company_fk, financial_trans_state_seq);

ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT sys_c008675 PRIMARY KEY (company_fk, financial_transaction_seq);


ALTER TABLE pspadm.psp_financial_txn_action
ADD PRIMARY KEY (financial_txn_action_seq);

ALTER TABLE pspadm.psp_fintxn_onholdreason_assoc
ADD PRIMARY KEY (financial_transaction_fk, on_hold_reason_fk);


ALTER TABLE pspadm.psp_forecast
ADD PRIMARY KEY (forecast_seq);

ALTER TABLE pspadm.psp_forecast_detail
ADD PRIMARY KEY (forecast_detail_seq);

ALTER TABLE pspadm.psp_form_template
ADD PRIMARY KEY (form_template_cd);

ALTER TABLE pspadm.psp_fraud_address
ADD PRIMARY KEY (fraud_address_seq);


ALTER TABLE pspadm.psp_fraud_bank_account
ADD PRIMARY KEY (fraud_bank_account_seq);

ALTER TABLE pspadm.psp_fraud_company
ADD PRIMARY KEY (fraud_company_seq);

ALTER TABLE pspadm.psp_fraud_contact
ADD PRIMARY KEY (fraud_contact_seq);


ALTER TABLE pspadm.psp_fraud_event
ADD PRIMARY KEY (fraud_event_seq);


ALTER TABLE pspadm.psp_fraud_rule
ADD PRIMARY KEY (fraud_rule_seq);


ALTER TABLE pspadm.psp_fraud_value
ADD PRIMARY KEY (fraud_value_seq);


ALTER TABLE pspadm.psp_fset_file
ADD PRIMARY KEY (fset_file_seq);



ALTER TABLE pspadm.psp_fset_filing_detail
ADD PRIMARY KEY (fset_filing_detail_seq);

ALTER TABLE pspadm.psp_funding_model
ADD PRIMARY KEY (funding_model_cd);



ALTER TABLE pspadm.psp_gems_ledger_posting_rule
ADD PRIMARY KEY (gems_ledger_posting_rule_seq);

ALTER TABLE pspadm.psp_gems_monthly_balance
ADD PRIMARY KEY (gems_monthly_balance_seq);


ALTER TABLE pspadm.psp_gems_upload_batch
ADD PRIMARY KEY (gems_upload_batch_seq);


ALTER TABLE pspadm.psp_hours_worked_exception
ADD PRIMARY KEY (hours_worked_exception_id);



ALTER TABLE pspadm.psp_individual
ADD PRIMARY KEY (individual_seq);



ALTER TABLE pspadm.psp_industry_type
ADD PRIMARY KEY (industry_type_seq);



ALTER TABLE pspadm.psp_intuit_ba_bt_ft
ADD PRIMARY KEY (intuit_ba_bt_ft_seq);


ALTER TABLE pspadm.psp_intuit_bank_acc_txn_type
ADD PRIMARY KEY (intuit_bank_acc_txn_type_seq);

ALTER TABLE pspadm.psp_intuit_bank_account
ADD PRIMARY KEY (intuit_bank_account_seq);

ALTER TABLE pspadm.psp_intuit_shipper_info
ADD PRIMARY KEY (intuit_shipper_info_seq);


ALTER TABLE pspadm.psp_iopsync_company
ADD PRIMARY KEY (iopsync_company_seq);


ALTER TABLE pspadm.psp_law
ADD PRIMARY KEY (law_id);

ALTER TABLE pspadm.psp_law_rate_range
ADD PRIMARY KEY (law_rate_range_id);

ALTER TABLE pspadm.psp_law_rate_value
ADD PRIMARY KEY (law_rate_value_id);



ALTER TABLE pspadm.psp_ledger_account
ADD PRIMARY KEY (ledger_account_cd);



ALTER TABLE pspadm.psp_ledger_account_action
ADD PRIMARY KEY (ledger_account_action_seq);

ALTER TABLE pspadm.psp_ledger_balance
ADD CONSTRAINT sys_c008432 PRIMARY KEY (company_fk, ledger_balance_seq);


ALTER TABLE pspadm.psp_ledger_operation
ADD PRIMARY KEY (ledger_operation_seq);


ALTER TABLE pspadm.psp_ledger_operation_job
ADD PRIMARY KEY (ledger_operation_job_seq);

ALTER TABLE pspadm.psp_liab_check_billing_assoc
ADD PRIMARY KEY (liab_check_billing_assoc_seq);

ALTER TABLE pspadm.psp_liability_adjustment
ADD PRIMARY KEY (liability_adjustment_seq);



ALTER TABLE pspadm.psp_liability_check
ADD PRIMARY KEY (liability_check_seq);

ALTER TABLE pspadm.psp_liability_check_line
ADD PRIMARY KEY (liability_check_line_seq);


ALTER TABLE pspadm.psp_limit_rule
ADD PRIMARY KEY (limit_rule_seq);


ALTER TABLE pspadm.psp_limit_value
ADD PRIMARY KEY (limit_value_seq);

ALTER TABLE pspadm.psp_manual_requirement
ADD PRIMARY KEY (manual_requirement_seq);



ALTER TABLE pspadm.psp_message_log
ADD PRIMARY KEY (message_log_seq);

ALTER TABLE pspadm.psp_money_movement_transaction
ADD CONSTRAINT sys_c008717 PRIMARY KEY (company_fk, money_movement_transaction_seq);


ALTER TABLE pspadm.psp_nachafile
ADD PRIMARY KEY (nachafile_seq);



ALTER TABLE pspadm.psp_offer
ADD PRIMARY KEY (offer_seq);


ALTER TABLE pspadm.psp_offer_price
ADD PRIMARY KEY (offer_price_seq);

ALTER TABLE pspadm.psp_offer_svcchg_assoc
ADD PRIMARY KEY (offer_fk, offering_service_charge_fk);


ALTER TABLE pspadm.psp_offering
ADD PRIMARY KEY (offering_seq);



ALTER TABLE pspadm.psp_offering_svcchg
ADD PRIMARY KEY (offering_svcchg_seq);


ALTER TABLE pspadm.psp_offering_svcchg_grp
ADD PRIMARY KEY (offering_svcchg_grp_seq);


ALTER TABLE pspadm.psp_offload_batch
ADD PRIMARY KEY (offload_batch_seq);

ALTER TABLE pspadm.psp_offload_group
ADD PRIMARY KEY (offload_group_seq);


ALTER TABLE pspadm.psp_on_hold_reason
ADD PRIMARY KEY (on_hold_reason_seq);

ALTER TABLE pspadm.psp_ownership_type
ADD PRIMARY KEY (ownership_type_seq);



ALTER TABLE pspadm.psp_pay_item
ADD PRIMARY KEY (pay_item_seq);

ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT sys_c008737 PRIMARY KEY (company_fk, paycheck_seq);

ALTER TABLE pspadm.psp_paycheck_split
ADD CONSTRAINT sys_c008745 PRIMARY KEY (company_fk, paycheck_split_seq);



ALTER TABLE pspadm.psp_paycheck_usage_hist
ADD PRIMARY KEY (paycheck_usage_hist_seq);

ALTER TABLE pspadm.psp_payee
ADD PRIMARY KEY (payee_seq);



ALTER TABLE pspadm.psp_payee_bank_account
ADD PRIMARY KEY (payee_bank_account_seq);

ALTER TABLE pspadm.psp_payment_batch_assoc
ADD PRIMARY KEY (payment_batch_assoc_seq);

ALTER TABLE pspadm.psp_payment_method_requirement
ADD PRIMARY KEY (payment_method_requirement_seq);

ALTER TABLE pspadm.psp_payment_requirement
ADD PRIMARY KEY (payment_requirement_seq);



ALTER TABLE pspadm.psp_payment_template
ADD PRIMARY KEY (payment_template_cd);

ALTER TABLE pspadm.psp_payment_template_agency_id
ADD PRIMARY KEY (payment_template_agency_id_seq);

ALTER TABLE pspadm.psp_payroll_fraud_batch
ADD PRIMARY KEY (payroll_fraud_batch_seq);

ALTER TABLE pspadm.psp_payroll_frequency
ADD PRIMARY KEY (payroll_freq_cd);



ALTER TABLE pspadm.psp_payroll_item
ADD PRIMARY KEY (payroll_item_code);

ALTER TABLE pspadm.psp_payroll_item_taxable_to
ADD PRIMARY KEY (payroll_item_taxable_to_seq);



ALTER TABLE pspadm.psp_payroll_run
ADD PRIMARY KEY (payroll_run_seq);


ALTER TABLE pspadm.psp_payroll_run_action
ADD PRIMARY KEY (payroll_run_action_seq);


ALTER TABLE pspadm.psp_payroll_subtype
ADD PRIMARY KEY (payroll_subtype_seq);

ALTER TABLE pspadm.psp_paystub
ADD PRIMARY KEY (company_fk, paystub_seq);

ALTER TABLE pspadm.psp_perf_sst
ADD PRIMARY KEY (time_pacific, transmission_type);


ALTER TABLE pspadm.psp_pmt_template_bankaccount
ADD PRIMARY KEY (pmt_template_bankaccount_seq);


ALTER TABLE pspadm.psp_pmt_template_frequency
ADD PRIMARY KEY (payment_template_frequency_id);


ALTER TABLE pspadm.psp_pmt_template_paymentmethod
ADD PRIMARY KEY (pmt_template_paymentmethod_seq);

ALTER TABLE pspadm.psp_pmttemplate_chkinfo_assoc
ADD PRIMARY KEY (pmttemplate_chkinfo_assoc_seq);

ALTER TABLE pspadm.psp_pmttemplate_printedchkinfo
ADD PRIMARY KEY (pmttemplate_printedchkinfo_seq);

ALTER TABLE pspadm.psp_posting_rule
ADD PRIMARY KEY (posting_rule_cd);

ALTER TABLE pspadm.psp_prior_payment_submission
ADD PRIMARY KEY (prior_payment_submission_seq);

ALTER TABLE pspadm.psp_property_audit
ADD PRIMARY KEY (company_fk, property_audit_seq);

ALTER TABLE pspadm.psp_pstub_address
ADD PRIMARY KEY (pstub_address_seq);

ALTER TABLE pspadm.psp_pstub_dditem
ADD PRIMARY KEY (pstub_dditem_seq);

ALTER TABLE pspadm.psp_pstub_employee_info
ADD PRIMARY KEY (company_fk, pstub_employee_info_seq);



ALTER TABLE pspadm.psp_pstub_employee_preference
ADD PRIMARY KEY (pstub_employee_preference_seq);

ALTER TABLE pspadm.psp_pstub_employer_info
ADD PRIMARY KEY (pstub_employer_info_seq);



ALTER TABLE pspadm.psp_pstub_msg
ADD PRIMARY KEY (pstub_msg_seq);

ALTER TABLE pspadm.psp_pstub_paid_timeoff_item
ADD PRIMARY KEY (company_fk, pstub_paid_timeoff_item_seq);

ALTER TABLE pspadm.psp_pstub_pay_item
ADD PRIMARY KEY (company_fk, pstub_pay_item_seq);

ALTER TABLE pspadm.psp_pstub_state_tax_info
ADD PRIMARY KEY (pstub_state_tax_info_seq);



ALTER TABLE pspadm.psp_qbdt_employee_info
ADD PRIMARY KEY (qbdt_employee_info_seq);

ALTER TABLE pspadm.psp_qbdt_paycheck_info
ADD PRIMARY KEY (company_fk, qbdt_paycheck_info_seq);

ALTER TABLE pspadm.psp_qbdt_payline_info
ADD PRIMARY KEY (company_fk, qbdt_payline_info_seq);




ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD PRIMARY KEY (qbdt_payroll_item_info_seq);

ALTER TABLE pspadm.psp_qbdt_payroll_trans_line
ADD PRIMARY KEY (qbdt_payroll_trans_line_seq);


ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD PRIMARY KEY (qbdt_payroll_transaction_seq);

ALTER TABLE pspadm.psp_qbdt_request_info
ADD PRIMARY KEY (qbdt_request_info_seq);

ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD PRIMARY KEY (company_fk, qbdt_transaction_info_seq);



ALTER TABLE pspadm.psp_qbdt_unprocessed_request
ADD PRIMARY KEY (qbdt_unprocessed_request_seq);

ALTER TABLE pspadm.psp_quickbooks_info
ADD PRIMARY KEY (quickbooks_info_seq);

ALTER TABLE pspadm.psp_racompany_service_info
ADD PRIMARY KEY (racompany_service_info_seq);


ALTER TABLE pspadm.psp_rafenrollment
ADD PRIMARY KEY (rafenrollment_seq);

ALTER TABLE pspadm.psp_rafenrollment_detail
ADD PRIMARY KEY (rafenrollment_detail_seq);


ALTER TABLE pspadm.psp_rafenrollment_file
ADD PRIMARY KEY (rafenrollment_file_seq);

ALTER TABLE pspadm.psp_rate_ledger_operation
ADD PRIMARY KEY (rate_ledger_operation_seq);

ALTER TABLE pspadm.psp_report_job_setup
ADD PRIMARY KEY (report_name);

ALTER TABLE pspadm.psp_reporting_agent
ADD PRIMARY KEY (reporting_agent_seq);


ALTER TABLE pspadm.psp_return_reason_desc
ADD PRIMARY KEY (reason_cd);



ALTER TABLE pspadm.psp_role_sub_status
ADD PRIMARY KEY (role_sub_status_seq);



ALTER TABLE pspadm.psp_rtbautomationbackup
ADD PRIMARY KEY (rtbautomationbackup_seq);

ALTER TABLE pspadm.psp_sap_method_call
ADD PRIMARY KEY (sap_method_call_seq);

ALTER TABLE pspadm.psp_saved_reports
ADD PRIMARY KEY (saved_reports_seq);

ALTER TABLE pspadm.psp_second_offload
ADD PRIMARY KEY (second_offload_seq);


ALTER TABLE pspadm.psp_serv_stat_txn_sku_type
ADD PRIMARY KEY (serv_stat_txn_sku_type_seq);


ALTER TABLE pspadm.psp_service
ADD PRIMARY KEY (service_cd);


ALTER TABLE pspadm.psp_service_status
ADD PRIMARY KEY (service_status_cd);


ALTER TABLE pspadm.psp_service_sub_status
ADD PRIMARY KEY (service_sub_status_cd);


ALTER TABLE pspadm.psp_smsmigration
ADD PRIMARY KEY (smsmigration_seq);


ALTER TABLE pspadm.psp_smssync_failure
ADD PRIMARY KEY (smssync_failure_seq);


ALTER TABLE pspadm.psp_source_payroll_parameter
ADD PRIMARY KEY (source_payroll_parameter_seq);


ALTER TABLE pspadm.psp_source_system
ADD PRIMARY KEY (source_system_cd);

ALTER TABLE pspadm.psp_source_system_law_assoc
ADD PRIMARY KEY (source_system_law_assoc_seq);


ALTER TABLE pspadm.psp_sourcesys_printedchk_info
ADD PRIMARY KEY (sourcesys_printedchk_info_seq);

ALTER TABLE pspadm.psp_sql_execution_log_entry
ADD PRIMARY KEY (sql_execution_log_entry_seq);

ALTER TABLE pspadm.psp_state_edi_tax_file
ADD PRIMARY KEY (state_edi_tax_file_seq);

ALTER TABLE pspadm.psp_state_report_assoc
ADD PRIMARY KEY (state_report_assoc_seq);


ALTER TABLE pspadm.psp_state_report_output
ADD PRIMARY KEY (state_report_output_seq);


ALTER TABLE pspadm.psp_suicredits_job
ADD PRIMARY KEY (suicredits_job_seq);

ALTER TABLE pspadm.psp_svcchgprice
ADD PRIMARY KEY (svcchgprice_seq);

ALTER TABLE pspadm.psp_svcstat_srcsys_assoc
ADD PRIMARY KEY (service_sub_status_fk, source_system_fk);

ALTER TABLE pspadm.psp_svcstat_svc_assoc
ADD PRIMARY KEY (service_sub_status_fk, service_fk);

ALTER TABLE pspadm.psp_svcstat_syscap_assoc
ADD PRIMARY KEY (service_sub_status_fk, system_capability_fk);


ALTER TABLE pspadm.psp_system_capability
ADD PRIMARY KEY (system_capability_cd);

ALTER TABLE pspadm.psp_system_parameter
ADD PRIMARY KEY (system_parameter_seq);


ALTER TABLE pspadm.psp_system_payment_requirement
ADD PRIMARY KEY (system_payment_requirement_seq);


ALTER TABLE pspadm.psp_system_requirement
ADD PRIMARY KEY (system_requirement_seq);

ALTER TABLE pspadm.psp_tax
ADD PRIMARY KEY (company_fk, tax_seq);

ALTER TABLE pspadm.psp_tax_account_audit
ADD CONSTRAINT psp_tax_acc_aud_pk PRIMARY KEY (audit_id);



ALTER TABLE pspadm.psp_tax_company_service_info
ADD PRIMARY KEY (tax_company_service_info_seq);

ALTER TABLE pspadm.psp_tax_credits9061
ADD PRIMARY KEY (tax_credits9061_seq);

ALTER TABLE pspadm.psp_tax_credits_application
ADD PRIMARY KEY (tax_credits_application_seq);



ALTER TABLE pspadm.psp_tax_payment_on_hold_reason
ADD PRIMARY KEY (tax_payment_on_hold_reason_seq);



ALTER TABLE pspadm.psp_tax_penalty_interest
ADD PRIMARY KEY (tax_penalty_interest_seq);

ALTER TABLE pspadm.psp_tax_table_misc_data
ADD PRIMARY KEY (tax_table_misc_data_seq);


ALTER TABLE pspadm.psp_third_party401k_batch
ADD PRIMARY KEY (third_party401k_batch_seq);

ALTER TABLE pspadm.psp_threshold_requirement
ADD PRIMARY KEY (threshold_requirement_seq);

ALTER TABLE pspadm.psp_tp401k_batch_employee
ADD PRIMARY KEY (tp401k_batch_employee_seq);

ALTER TABLE pspadm.psp_tp401k_batch_paycheck
ADD PRIMARY KEY (tp401k_batch_paycheck_seq);


ALTER TABLE pspadm.psp_tp401k_paycheck
ADD PRIMARY KEY (tp401k_paycheck_seq);


ALTER TABLE pspadm.psp_tp401k_paycheck_pending
ADD PRIMARY KEY (tp401k_paycheck_pending_seq);


ALTER TABLE pspadm.psp_tp401k_paycheck_state
ADD PRIMARY KEY (tp401k_paycheck_state_seq);


ALTER TABLE pspadm.psp_tp401k_signup_batch
ADD PRIMARY KEY (tp401k_signup_batch_seq);


ALTER TABLE pspadm.psp_tp401k_signup_queue
ADD PRIMARY KEY (tp401k_signup_queue_seq);

ALTER TABLE pspadm.psp_tp401kcompany_service_info
ADD PRIMARY KEY (tp401kcompany_service_info_seq);

ALTER TABLE pspadm.psp_transaction_offload_batch
ADD PRIMARY KEY (transaction_offload_batch_seq);

ALTER TABLE pspadm.psp_transaction_response
ADD PRIMARY KEY (transaction_response_seq);



ALTER TABLE pspadm.psp_transaction_return
ADD PRIMARY KEY (transaction_return_seq);


ALTER TABLE pspadm.psp_transaction_return_batch
ADD PRIMARY KEY (transaction_return_batch_seq);


ALTER TABLE pspadm.psp_transaction_state
ADD PRIMARY KEY (transaction_state_cd);


ALTER TABLE pspadm.psp_transaction_type
ADD PRIMARY KEY (transaction_type_cd);


ALTER TABLE pspadm.psp_transmission_payroll_run
ADD PRIMARY KEY (transmission_payroll_run_seq);

ALTER TABLE pspadm.psp_txntype_service_assoc
ADD PRIMARY KEY (transaction_type_fk, service_fk);

ALTER TABLE pspadm.psp_usage_period
ADD PRIMARY KEY (usage_period_seq);

ALTER TABLE pspadm.psp_user_preference
ADD PRIMARY KEY (key);

ALTER TABLE pspadm.psp_user_setting
ADD PRIMARY KEY (user_setting_seq);

ALTER TABLE pspadm.psp_vmp_employee_info
ADD PRIMARY KEY (vmp_employee_info_seq);

ALTER TABLE pspadm.psp_voided_check
ADD PRIMARY KEY (voided_check_seq);

ALTER TABLE pspadm.psp_wage_limit
ADD PRIMARY KEY (wage_limit_id);


ALTER TABLE pspadm.psp_wc_company
ADD PRIMARY KEY (wc_company_seq);


ALTER TABLE pspadm.psp_wc_paycheck
ADD PRIMARY KEY (wc_paycheck_seq);


ALTER TABLE pspadm.psp_wc_paycheck_pending
ADD PRIMARY KEY (wc_paycheck_pending_seq);


ALTER TABLE pspadm.psp_wc_paycheck_state
ADD PRIMARY KEY (wc_paycheck_state_seq);

ALTER TABLE pspadm.z_temp_diy_reset_company
ADD PRIMARY KEY (source_company_id);

--unique
ALTER TABLE pspadm.psp_pstub_employee_preference
ADD CONSTRAINT emp_pref_unq_indx UNIQUE (app_name, preference_name, employee_fk);

ALTER TABLE pspadm.psp_batch_job_status
ADD CONSTRAINT c_psp_batch_job_status1 UNIQUE (job_type);

--unique
ALTER TABLE pspadm.psp_employee
ADD CONSTRAINT psp_emp_comp_fk_src_emp_id UNIQUE (company_fk, source_employee_id);

SELECT CURRENT_TIMESTAMP;




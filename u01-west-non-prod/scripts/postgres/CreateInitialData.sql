set search_path to pspadm;




\i 'InitialData/populate_sourcesystem.sql'
\i 'InitialData/populate_payroll_item.sql'
\i 'InitialData/populate_ledgeraccount.sql'
\i 'InitialData/populate_transactiontype.sql'
\i 'InitialData/populate_transactionstate.sql'
\i 'InitialData/populate_postingrule.sql'
\i 'InitialData/populate_bankaccount.sql'
\i 'InitialData/populate_intuitbankaccount.sql'
\i 'InitialData/populate_intuitbankaccnttxntype.sql'
\i 'InitialData/populate_bankholiday.sql'
\i 'InitialData/populate_industry_type.sql'
\i 'InitialData/populate_ownership_type.sql'
\i 'InitialData/populate_eventtype.sql'
\i 'InitialData/populate_fundingmodel.sql'
\i 'InitialData/populate_payrollfrequency.sql'
\i 'InitialData/populate_systemparameter.sql'
\i 'InitialData/populate_offloadgroup.sql'
\i 'InitialData/populate_service.sql'
\i 'InitialData/populate_sourcepayrollparameter.sql'
\i 'InitialData/populate_fee.sql'
\i 'InitialData/populate_action_event.sql'
\i 'InitialData/populate_financial_transaction_action.sql'
\i 'InitialData/populate_payroll_run_action.sql'
\i 'InitialData/populate_ledger_account_action.sql'
\i 'InitialData/populate_intuitbabatchfiletype.sql'
\i 'InitialData/populate_limit_rule.sql'
\i 'InitialData/populate_fraud_rule.sql'
\i 'InitialData/populate_offering.sql'
\i 'InitialData/populate_offering_svcchg_grp.sql'
\i 'InitialData/populate_offering_svcchg.sql'
\i 'InitialData/populate_svcchg_price.sql'
\i 'InitialData/populate_offer.sql'      
\i 'InitialData/populate_offer_svcchg_assoc.sql'    
\i 'InitialData/populate_offer_price.sql'         
\i 'InitialData/populate_entitlement.sql'
\i 'InitialData/populate_entitlement_offering.sql'
\i 'InitialData/populate_service_status.sql'
\i 'InitialData/populate_txntypesrvstatskuofld.sql'
\i 'InitialData/populate_system_capability.sql'
\i 'InitialData/populate_service_status_capability.sql'
\i 'InitialData/populate_service_status_sps.sql'
\i 'InitialData/populate_service_status_service.sql'
\i 'InitialData/populate_gems_ledger_postingrule.sql'
\i 'InitialData/populate_auth_domain.sql'
\i 'InitialData/populate_auth_operation.sql'
\i 'InitialData/populate_collection_stage.sql'
\i 'InitialData/populate_auth_role.sql'
\i 'InitialData/populate_role_substatus.sql'
\i 'InitialData/populate_return_reason_desc.sql'
\i 'InitialData/populate_role_operation_assoc.sql'
\i 'InitialData/populate_report_job_setup.sql'
\i 'InitialData/populate_user_preference.sql'
\i 'InitialData/populate_batch_job_setup.sql'
\i 'InitialData/populate_batch_job_parameter.sql'
\i 'InitialData/populate_ach_transaction_code.sql'
\i 'InitialData/populate_address.sql'
\i 'InitialData/populate_reporting_agent.sql'
\i 'InitialData/populate_agency.sql'
--\i 'InitialData/populate_payroll_subtype.sql' its not present and used in oracle
--\i 'InitialData/populate_txntypesrvstatofld.sql' its not present and used in oracle
--\i 'InitialData/rebuild_psid_seqs.sql' its not present and used in oracle
--\i 'InitialData/separator.sql' its just a separtor which is used in oracle

\i 'InitialData/AgencyRules/populate_psp_agency.sql'
--\i 'InitialData/populate_agency_checklist_item.sql'      missing
--\i 'InitialData/populate_service_checklist_item.sql'       missing
--\i 'InitialData/populate_agency_status.sql'        missing
\i 'InitialData/populate_company.sql'

\i 'InitialData/populate_offloadbatch_and_nachafile.sql'

\i 'InitialData/AgencyRules/populate_psp_payment_template.sql'
\i 'InitialData/AgencyRules/populate_psp_law.sql'
\i 'InitialData/AgencyRules/populate_psp_wage_limit.sql'

\i 'InitialData/populate_transactiontype_svc_assoc.sql'

\i 'InitialData/AgencyRules/populate_psp_pmt_template_frequency.sql'

\i 'InitialData/update_agency.sql'
\i 'InitialData/update_payment_template.sql'
\i 'InitialData/update_pmt_template_frequency.sql'

\i 'InitialData/AgencyRules/populate_psp_form_template.sql'

\i 'InitialData/update_form_template.sql'
\i 'InitialData/update_law.sql'
\i 'InitialData/populate_sourcesystemlawassoc.sql'
\i 'InitialData/populate_depositfrequencycode.sql'
\i 'InitialData/populate_intuitshipperinfo.sql'
\i 'InitialData/populate_law_rate_range.sql'
\i 'InitialData/populate_law_rate_value.sql'
\i 'InitialData/populate_ade_law_map.sql'
\i 'InitialData/update_batch_job_status.sql'
\i 'InitialData/populate_paymenttemplate_bankaccount.sql'
\i 'InitialData/populate_paymenttemplate_agencyid.sql'
\i 'InitialData/populate_paymenttemplate_paymentmethod.sql'
\i 'InitialData/populate_paymentmethod_requirement.sql'
\i 'InitialData/populate_source_system_printed_check_info.sql'
\i 'InitialData/populate_payment_template_printed_check_info.sql'
\i 'InitialData/populate_additional_filing_amounts.sql'
\i 'InitialData/populate_hours_worked_exception.sql'









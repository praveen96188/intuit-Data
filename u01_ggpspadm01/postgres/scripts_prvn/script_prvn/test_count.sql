set serveroutput on;
DECLARE
    table_list SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST('psp_law', 'psp_tax', 'psp_bill', 'psp_offer', 'psp_payee', 'psp_agency', 'psp_address', 'psp_company', 'psp_contact', 'psp_service', 'psp_employee', 'psp_offering', 'psp_paycheck', 'psp_auth_role', 'psp_auth_user', 'psp_fset_file', 'psp_fraud_rule', 'psp_individual', 'psp_limit_rule', 'psp_company_law', 'psp_entitlement', 'psp_payroll_run', 'psp_bank_account', 'psp_bill_payment', 'psp_compensation', 'psp_posting_rule', 'psp_usage_period', 'psp_voided_check', 'psp_company_event', 'psp_company_offer', 'psp_company_usage', 'psp_offload_batch', 'psp_offload_group', 'psp_auth_operation', 'psp_billing_detail', 'psp_company_agency', 'psp_deleted_record', 'psp_employee_usage', 'psp_ledger_account', 'psp_ledger_balance', 'psp_on_hold_reason', 'psp_paycheck_split', 'psp_paycheck_usage', 'psp_property_audit', 'psp_service_status', 'psp_company_service', 'psp_offering_svcchg', 'psp_payroll_subtype', 'psp_quickbooks_info', 'psp_company_law_rate', 'psp_company_offering', 'psp_employee_accrual', 'psp_entitlement_code', 'psp_entitlement_unit', 'psp_payment_template', 'psp_transaction_type', 'psp_gems_upload_batch', 'psp_payroll_frequency', 'psp_svcstat_svc_assoc', 'psp_transaction_state', 'psp_vmp_employee_info', 'psp_bill_payment_split', 'psp_fset_filing_detail', 'psp_payee_bank_account', 'psp_qbdt_paycheck_info', 'psp_service_sub_status', 'psp_transaction_return', 'psp_batch_job_audit_log', 'psp_entry_detail_record', 'psp_offering_svcchg_grp', 'psp_company_bank_account', 'psp_company_event_detail', 'psp_company_payroll_item', 'psp_eftps_payment_detail', 'psp_gems_monthly_balance', 'psp_liability_adjustment', 'psp_transaction_response', 'psp_employee_bank_account', 'psp_financial_trans_state', 'psp_financial_transaction', 'psp_comp_adjust_submission', 'psp_ddcompany_service_info', 'psp_effective_deposit_freq', 'psp_pmt_template_frequency', 'psp_authrole_operation_assoc', 'psp_gems_ledger_posting_rule', 'psp_qbdt_unprocessed_request', 'psp_tax_company_service_info', 'psp_auth_user_auth_role_assoc', 'psp_companyagency_pmttemplate', 'psp_entitlement_code_offering', 'psp_comp_pmt_template_agencyid', 'psp_comp_pmttemplate_pmtmethod', 'psp_money_movement_transaction', 'psp_pmt_template_paymentmethod', 'psp_pmttemplate_printedchkinfo', 'psp_tax_payment_on_hold_reason', 'psp_company_additional_info ', 'psp_ownership_type ', 'psp_deduction', 'psp_employee_payroll_item', 'psp_bpcompany_service_info');
    v_tab_name VARCHAR2(30);
    v_count NUMBER;
BEGIN
    DBMS_OUTPUT.ENABLE(NULL);
    FOR i IN 1..table_list.COUNT
        LOOP
            v_tab_name := table_list(i);
            EXECUTE IMMEDIATE 'SELECT /*+ PARALLEL(16) */ COUNT(*) FROM PSPADM.' || v_tab_name INTO v_count;
            DBMS_OUTPUT.PUT_LINE(v_tab_name || ',' || v_count);
        END LOOP;
END;
/


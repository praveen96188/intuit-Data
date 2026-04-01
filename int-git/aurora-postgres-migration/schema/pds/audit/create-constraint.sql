-- ------------ Write CREATE-CONSTRAINT-stage scripts -----------

ALTER TABLE ibobadm_pds.psp_hcm401k_company_policy
ADD PRIMARY KEY (hcm401k_company_policy_seq, realm_id);

ALTER TABLE ibobadm_pds.psp_hcm401k_company_qbdt_pitem
ADD CONSTRAINT c_psp_hcm401k_company_qbdt0 CHECK (hcm401k_contributor IN ('Employer', 'Employee'));

ALTER TABLE ibobadm_pds.psp_hcm401k_company_qbdt_pitem
ADD PRIMARY KEY (hcm401k_company_qbdt_pitem_seq, realm_id);

ALTER TABLE ibobadm_pds.psp_hcm401k_employee_deduction
ADD CONSTRAINT c_psp_hcm401k_employee_ded0 CHECK (hcm401k_amount_type IN ('Dollar', 'Percentage'));

ALTER TABLE ibobadm_pds.psp_hcm401k_employee_deduction
ADD CONSTRAINT c_psp_hcm401k_employee_ded1 CHECK (hcm401k_deduction_contributor IN ('Employer', 'Employee'));

ALTER TABLE ibobadm_pds.psp_hcm401k_employee_deduction
ADD PRIMARY KEY (hcm401k_employee_deduction_seq, realm_id);

ALTER TABLE ibobadm_pds.psp_hcm401k_policy
ADD CONSTRAINT c_psp_hcm401k_policy0 CHECK (deduction_item_policy IN ('TppoCus401K', 'TppoCusRoth401K', 'TppoCus401KCatchup', 'TdepCusLoanRepayment'));

ALTER TABLE ibobadm_pds.psp_hcm401k_policy
ADD CONSTRAINT c_psp_hcm401k_policy1 CHECK (deduction_item_provider IN ('Guideline'));

ALTER TABLE ibobadm_pds.psp_hcm401k_policy
ADD PRIMARY KEY (hcm401k_policy_seq, realm_id);

ALTER TABLE ibobadm_pds.psp_qbdt_request_info
ADD PRIMARY KEY (qbdt_request_info_seq, realm_id);

ALTER TABLE ibobadm_pds.psp_sap_method_call
ADD PRIMARY KEY (sap_method_call_seq, realm_id);

ALTER TABLE ibobadm_pds.psp_source_system_transmission
ADD CONSTRAINT c_psp_source_system_transm0 CHECK (from_source_system IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO'));

ALTER TABLE ibobadm_pds.psp_source_system_transmission
ADD CONSTRAINT c_psp_source_system_transm1 CHECK (type IN ('QueryEntitlement', 'ActivateFeature', 'QueryAccount', 'AuthenticatePIN', 'SetFeatureStatus', 'QueryPayrollStatus', 'CreateAccount', 'UpdateAccount', 'MigrateAccount', 'ValidateBankAccount', 'CreatePIN', 'ChangePIN', 'Sync', 'PayrollSubmission', 'ZeroPayroll', 'UpdateBankAccount', 'EntitlementExtensionUpdate', 'AgreementUpdate', 'AccountUpdate', 'ContactUpdate', 'Unknown', 'CUEVENT', 'CUINFOMOD', 'EntitlementExtensionNew', 'PayrollStatusQuery', 'ResetPIN', 'WS401KSubmitEmployees', 'WS401KSubmitPayroll', 'WSBillPaySendPaymentsToPayees', 'WSBillPayVoidPayments', 'WSBillPayQueryPaymentStatus', 'WS401KUpdateCompany', 'BalanceFile', 'AddService', 'ValidateSubscription', 'UpdateBillingDetails', 'EntitlementUnitActivation', 'EntitlementUnitDeactivation', 'EntitlementCancel', 'TaxExemptionLookup', 'TaxExemptionUpdate', 'MigrateEntitlement', 'UsageSync', 'UsageSend', 'QueryCustomerAsset', 'DeactivateService'));

ALTER TABLE ibobadm_pds.psp_source_system_transmission
ADD CONSTRAINT c_psp_source_system_transm2 CHECK (to_source_system IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO'));

ALTER TABLE ibobadm_pds.psp_source_system_transmission
ADD PRIMARY KEY (source_system_transmission_seq, realm_id);


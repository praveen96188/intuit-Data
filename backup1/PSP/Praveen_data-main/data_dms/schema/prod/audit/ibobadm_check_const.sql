set search_path=ibobadm;

ALTER TABLE ibobadm.psp_hcm401k_company_qbdt_pitem
ADD CONSTRAINT c_psp_hcm401k_company_qbdt0 CHECK (hcm401k_contributor IN ('Employer', 'Employee'));

ALTER TABLE ibobadm.psp_hcm401k_employee_deduction
ADD CONSTRAINT c_psp_hcm401k_employee_ded0 CHECK (hcm401k_amount_type IN ('Dollar', 'Percentage'));

ALTER TABLE ibobadm.psp_hcm401k_employee_deduction
ADD CONSTRAINT c_psp_hcm401k_employee_ded1 CHECK (hcm401k_deduction_contributor IN ('Employer', 'Employee'));

ALTER TABLE ibobadm.psp_hcm401k_policy
ADD CONSTRAINT c_psp_hcm401k_policy0 CHECK (deduction_item_policy IN ('TppoCus401K', 'TppoCusRoth401K', 'TppoCus401KCatchup', 'TdepCusLoanRepayment'));

ALTER TABLE ibobadm.psp_hcm401k_policy
ADD CONSTRAINT c_psp_hcm401k_policy1 CHECK (deduction_item_provider IN ('Guideline'));

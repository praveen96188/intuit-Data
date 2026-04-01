set search_path=ibobadm;

ALTER TABLE ibobadm.psp_hcm401k_company_policy
ADD CONSTRAINT psp_hcm401k_company_policy_fk1 FOREIGN KEY (hcm401k_policy_fk, realm_id) 
REFERENCES ibobadm.psp_hcm401k_policy (hcm401k_policy_seq, realm_id)
ON DELETE NO ACTION;

ALTER TABLE ibobadm.psp_hcm401k_company_qbdt_pitem
ADD CONSTRAINT psp_hcm401k_company_qbdt_p_fk1 FOREIGN KEY (hcm401k_company_policy_fk, realm_id) 
REFERENCES ibobadm.psp_hcm401k_company_policy (hcm401k_company_policy_seq, realm_id)
ON DELETE NO ACTION;

ALTER TABLE ibobadm.psp_hcm401k_employee_deduction
ADD CONSTRAINT psp_hcm401k_employee_deduc_fk1 FOREIGN KEY (hcm401k_company_policy_fk, realm_id) 
REFERENCES ibobadm.psp_hcm401k_company_policy (hcm401k_company_policy_seq, realm_id)
ON DELETE NO ACTION;

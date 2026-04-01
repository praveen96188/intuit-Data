-- ------------ Write CREATE-FOREIGN-KEY-CONSTRAINT-stage scripts -----------

ALTER TABLE ibobadm_pds.psp_hcm401k_company_policy
ADD CONSTRAINT psp_hcm401k_company_policy_fk1 FOREIGN KEY (hcm401k_policy_fk, realm_id) 
REFERENCES ibobadm_pds.psp_hcm401k_policy (hcm401k_policy_seq, realm_id)
ON DELETE NO ACTION;

ALTER TABLE ibobadm_pds.psp_hcm401k_company_qbdt_pitem
ADD CONSTRAINT psp_hcm401k_company_qbdt_p_fk1 FOREIGN KEY (hcm401k_company_policy_fk, realm_id) 
REFERENCES ibobadm_pds.psp_hcm401k_company_policy (hcm401k_company_policy_seq, realm_id)
ON DELETE NO ACTION;

ALTER TABLE ibobadm_pds.psp_hcm401k_employee_deduction
ADD CONSTRAINT psp_hcm401k_employee_deduc_fk1 FOREIGN KEY (hcm401k_company_policy_fk, realm_id) 
REFERENCES ibobadm_pds.psp_hcm401k_company_policy (hcm401k_company_policy_seq, realm_id)
ON DELETE NO ACTION;

/*
ALTER TABLE ibobadm_pds.psp_qbdt_request_info
ADD CONSTRAINT psp_qbdt_request_info_fk1 FOREIGN KEY (source_system_transmission_fk, realm_id) 
REFERENCES ibobadm_pds.psp_source_system_transmission (source_system_transmission_seq, realm_id)
ON DELETE NO ACTION;
*/


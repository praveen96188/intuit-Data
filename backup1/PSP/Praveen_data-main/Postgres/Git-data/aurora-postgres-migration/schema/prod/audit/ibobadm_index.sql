set search_path=ibobadm;

CREATE INDEX psp_hcm401k_company_policy_fk1
ON ibobadm.psp_hcm401k_company_policy
USING BTREE (hcm401k_policy_fk ASC, realm_id ASC);

CREATE INDEX psp_hcm401k_company_qbdt_p_fk1
ON ibobadm.psp_hcm401k_company_qbdt_pitem
USING BTREE (hcm401k_company_policy_fk ASC, realm_id ASC);

CREATE INDEX psp_hcm401k_employee_deduc_fk1
ON ibobadm.psp_hcm401k_employee_deduction
USING BTREE (hcm401k_company_policy_fk ASC, realm_id ASC);

CREATE INDEX psp_qbdt_request_info_crdt
ON ibobadm.psp_qbdt_request_info
USING BTREE (created_date ASC);

CREATE INDEX psp_qbdt_request_info_fk1
ON ibobadm.psp_qbdt_request_info
USING BTREE (source_system_transmission_fk ASC, realm_id ASC);

CREATE INDEX psp_sap_meth_call_idx1
ON ibobadm.psp_sap_method_call
USING BTREE (created_date ASC, service_name ASC, method_name ASC);



-- ------------ Write CREATE-INDEX-stage scripts -----------

CREATE INDEX psp_hcm401k_company_policy_fk1
ON ibobadm_pds.psp_hcm401k_company_policy
USING BTREE (hcm401k_policy_fk ASC, realm_id ASC);

CREATE INDEX psp_hcm401k_company_qbdt_p_fk1
ON ibobadm_pds.psp_hcm401k_company_qbdt_pitem
USING BTREE (hcm401k_company_policy_fk ASC, realm_id ASC);

CREATE INDEX psp_hcm401k_employee_deduc_fk1
ON ibobadm_pds.psp_hcm401k_employee_deduction
USING BTREE (hcm401k_company_policy_fk ASC, realm_id ASC);

CREATE INDEX psp_qbdt_request_info_fk1
ON ibobadm_pds.psp_qbdt_request_info
USING BTREE (source_system_transmission_fk ASC, realm_id ASC);

CREATE INDEX psp_source_system_transmis_i1
ON ibobadm_pds.psp_source_system_transmission
USING BTREE (from_source_system ASC, created_date ASC);

CREATE INDEX psp_source_system_transmis_i2
ON ibobadm_pds.psp_source_system_transmission
USING BTREE (created_date ASC);

CREATE INDEX psp_source_system_transmis_i4
ON ibobadm_pds.psp_source_system_transmission
USING BTREE (company_id ASC, created_date ASC);

CREATE INDEX psp_source_system_transmis_i5
ON ibobadm_pds.psp_source_system_transmission
USING BTREE (i_p_address ASC);

CREATE INDEX psp_src_sys_tran_id
ON ibobadm_pds.psp_source_system_transmission
USING BTREE (transmission_identifier ASC);


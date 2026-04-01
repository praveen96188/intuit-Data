-- ------------ Write CREATE-INDEX-stage scripts -----------

CREATE INDEX psp_qbdt_request_info_crdt
ON ibobadm.psp_qbdt_request_info
USING BTREE (created_date ASC);



CREATE INDEX psp_qbdt_request_info_fk1
ON ibobadm.psp_qbdt_request_info
USING BTREE (source_system_transmission_fk ASC, realm_id ASC);



CREATE INDEX psp_sap_method_call_idx1
ON ibobadm.psp_sap_method_call
USING BTREE (created_date ASC, service_name ASC, method_name ASC);



CREATE INDEX psp_source_system_transmis_i1
ON ibobadm.psp_source_system_transmission
USING BTREE (from_source_system ASC, created_date ASC);



CREATE INDEX psp_source_system_transmis_i2
ON ibobadm.psp_source_system_transmission
USING BTREE (created_date ASC);



CREATE INDEX psp_source_system_transmis_i4
ON ibobadm.psp_source_system_transmission
USING BTREE (company_id ASC, created_date ASC);



CREATE INDEX psp_src_sys_tran_id
ON ibobadm.psp_source_system_transmission
USING BTREE (transmission_identifier ASC);




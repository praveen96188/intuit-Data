-- ------------ Write CREATE-CONSTRAINT-stage scripts -----------

ALTER TABLE ibobadm.gg_heartbeat
ADD PRIMARY KEY (source);



ALTER TABLE ibobadm.gg_heartbeat_smc
ADD PRIMARY KEY (source);



ALTER TABLE ibobadm.gg_heartbeat_sst
ADD PRIMARY KEY (source);



ALTER TABLE ibobadm.psp_qbdt_request_info
ADD PRIMARY KEY (qbdt_request_info_seq, realm_id);



ALTER TABLE ibobadm.psp_sap_method_call
ADD PRIMARY KEY (sap_method_call_seq, realm_id);




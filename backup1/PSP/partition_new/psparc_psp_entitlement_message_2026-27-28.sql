----------------------------------------psparc.psp_entitlement_message-----------------------------------------------------------------------------------------------------------
 
--DDL--

\timing
set search_path to psparc;


ALTER TABLE psparc.psp_entitlement_message DETACH PARTITION psparc.psp_entitlement_message_2026;

ALTER TABLE psparc.psp_entitlement_message ATTACH PARTITION psparc.psp_entitlement_message_2026
FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2027-01-01 00:00:00');




CREATE TABLE psparc.psp_entitlement_message_2027
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2028-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2028
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2028-01-01 00:00:00') TO (MAXVALUE);

--CONSTRAINTS--
#ALTER TABLE psparc.psp_entitlement_message_2026 ADD CONSTRAINT psp_entitlement_message_2026_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2027 ADD CONSTRAINT psparc.psp_entitlement_message_2027_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2028 ADD CONSTRAINT psparc.psp_entitlement_message_2028_pk PRIMARY KEY (entitlement_message_seq, realm_id);

--Index RENAME--

#ALTER INDEX psparc.psp_entitlement_message_2026_modified_date_idx RENAME TO idx_ent_msg_mod_date_2026;
#ALTER INDEX psparc.psp_entitlement_message_2026_license_number_entitlement_off_idx RENAME TO psp_entitlement_msg_lcno_eofcd_2026;
#ALTER INDEX psparc.psp_entitlement_message_2026_message_timestamp_event_reason_idx RENAME TO psp_entitlement_msg_msgts_er_2026;
#ALTER INDEX psparc.psp_entitlement_message_2026_order_number_license_number_idx RENAME TO psp_entitlement_msg_orno_lcno_2026;
#ALTER INDEX psparc.psp_entitlement_message_2026_token_idx RENAME TO psp_entitlement_message_u1_2026;

ALTER INDEX psparc.psp_entitlement_message_2027_modified_date_idx RENAME TO psparc.idx_ent_msg_mod_date_2027;
ALTER INDEX psparc.psp_entitlement_message_2027_license_number_entitlement_off_idx RENAME TO psparc.psp_entitlement_msg_lcno_eofcd_2027;
ALTER INDEX psparc.psp_entitlement_message_2027_message_timestamp_event_reason_idx RENAME TO psparc.psp_entitlement_msg_msgts_er_2027;
ALTER INDEX psparc.psp_entitlement_message_2027_order_number_license_number_idx RENAME TO psparc.psp_entitlement_msg_orno_lcno_2027;
ALTER INDEX psparc.psp_entitlement_message_2027_token_idx RENAME TO psparc.psp_entitlement_message_u1_2027;

ALTER INDEX psparc.psp_entitlement_message_2028_modified_date_idx RENAME TO psparc.idx_ent_msg_mod_date_2028;
ALTER INDEX psparc.psp_entitlement_message_2028_license_number_entitlement_off_idx RENAME TO psparc.psp_entitlement_msg_lcno_eofcd_2028;
ALTER INDEX psparc.psp_entitlement_message_2028_message_timestamp_event_reason_idx RENAME TO psparc.psp_entitlement_msg_msgts_er_2028;
ALTER INDEX psparc.psp_entitlement_message_2028_order_number_license_number_idx RENAME TO psparc.psp_entitlement_msg_orno_lcno_2028;
ALTER INDEX psparc.psp_entitlement_message_2028_token_idx RENAME TO psparc.psp_entitlement_message_u1_2028;

--attach--

#alter index psparc.idx_ent_msg_mod_date attach partition psparc.idx_ent_msg_mod_date_2026;
#alter index psparc.psp_entitlement_msg_orno_lcno attach partition  psparc.psp_entitlement_msg_orno_lcno_2026 ;
#alter index psparc.psp_entitlement_msg_msgts_er attach partition psparc.psp_entitlement_msg_msgts_er_2026;
#alter index psparc.psp_entitlement_msg_lcno_eofcd attach partition psparc.psp_entitlement_msg_lcno_eofcd_2026 ;
#alter index psparc.psp_entitlement_message_u1 attach partition psparc.psp_entitlement_message_u1_2026;

alter index psparc.idx_ent_msg_mod_date attach partition psparc.idx_ent_msg_mod_date_2027;
alter index psparc.psp_entitlement_msg_orno_lcno attach partition  psparc.psp_entitlement_msg_orno_lcno_2027 ;
alter index psparc.psp_entitlement_msg_msgts_er attach partition psparc.psp_entitlement_msg_msgts_er_2027;
alter index psparc.psp_entitlement_msg_lcno_eofcd attach partition psparc.psp_entitlement_msg_lcno_eofcd_2027 ;
alter index psparc.psp_entitlement_message_u1 attach partition psparc.psp_entitlement_message_u1_2027;

alter index psparc.idx_ent_msg_mod_date attach partition psparc.idx_ent_msg_mod_date_2028;
alter index psparc.psp_entitlement_msg_orno_lcno attach partition  psparc.psp_entitlement_msg_orno_lcno_2028;
alter index psparc.psp_entitlement_msg_msgts_er attach partition psparc.psp_entitlement_msg_msgts_er_2028;
alter index psparc.psp_entitlement_msg_lcno_eofcd attach partition psparc.psp_entitlement_msg_lcno_eofcd_2028 ;
alter index psparc.psp_entitlement_message_u1 attach partition psparc.psp_entitlement_message_u1_2028;
______________________________________________________________________________________________________________________
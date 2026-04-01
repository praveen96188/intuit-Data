-- ------------ Write DROP-CONSTRAINT-stage scripts -----------

ALTER TABLE ibobadm.gg_heartbeat DROP CONSTRAINT sys_c009049;



ALTER TABLE ibobadm.gg_heartbeat_smc DROP CONSTRAINT sys_c005254;



ALTER TABLE ibobadm.gg_heartbeat_sst DROP CONSTRAINT sys_c005249;



ALTER TABLE ibobadm.psp_qbdt_request_info DROP CONSTRAINT sys_c005239;



ALTER TABLE ibobadm.psp_sap_method_call DROP CONSTRAINT sys_c005244;



-- ------------ Write DROP-INDEX-stage scripts -----------

DROP INDEX IF EXISTS ibobadm.psp_qbdt_request_info_crdt;



DROP INDEX IF EXISTS ibobadm.psp_qbdt_request_info_fk1;



DROP INDEX IF EXISTS ibobadm.psp_sap_method_call_idx1;



DROP INDEX IF EXISTS ibobadm.psp_source_system_transmis_i1;



DROP INDEX IF EXISTS ibobadm.psp_source_system_transmis_i2;



DROP INDEX IF EXISTS ibobadm.psp_source_system_transmis_i4;



DROP INDEX IF EXISTS ibobadm.psp_src_sys_tran_id;



-- ------------ Write DROP-SUBPARTITION-stage scripts -----------

DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_qbdt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_as400;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_cris;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_dflt;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_ews;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_null;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_psp;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_qbdt;



-- ------------ Write DROP-PARTITION-stage scripts -----------

DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_2008;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_9999;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m012010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m022010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m032010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m042010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m052010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m062010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m072010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m082010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m092010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m102010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m112010;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122009;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission_srcsystrns_m122010;



-- ------------ Write DROP-TABLE-stage scripts -----------

DROP TABLE IF EXISTS ibobadm.gg_heartbeat;



DROP TABLE IF EXISTS ibobadm.gg_heartbeat_smc;



DROP TABLE IF EXISTS ibobadm.gg_heartbeat_sst;



DROP TABLE IF EXISTS ibobadm.psp_qbdt_request_info;



DROP TABLE IF EXISTS ibobadm.psp_sap_method_call;



DROP TABLE IF EXISTS ibobadm.psp_source_system_transmission;



DROP TABLE IF EXISTS ibobadm.test1;



DROP TABLE IF EXISTS ibobadm.test2;



-- ------------ Write DROP-DATABASE-stage scripts -----------

-- ------------ Write CREATE-DATABASE-stage scripts -----------

CREATE SCHEMA IF NOT EXISTS ibobadm;



-- ------------ Write CREATE-TABLE-stage scripts -----------

CREATE TABLE ibobadm.gg_heartbeat(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.gg_heartbeat_smc(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.gg_heartbeat_sst(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.psp_qbdt_request_info(
    qbdt_request_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    employee_add_count NUMERIC(10,0),
    employee_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_count NUMERIC(10,0),
    employee_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_add_count NUMERIC(10,0),
    paycheck_update_count NUMERIC(10,0),
    payroll_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_count NUMERIC(10,0),
    payroll_item_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_count NUMERIC(10,0),
    payroll_item_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_count NUMERIC(10,0),
    payroll_transaction_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_count NUMERIC(10,0),
    payroll_trans_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_delete_count NUMERIC(10,0),
    payroll_item_delete_count NUMERIC(10,0),
    payroll_trans_delete_count NUMERIC(10,0),
    delete_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    delete_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    source_system_transmission_fk CHARACTER VARYING(255),
    paycheck_delete_count NUMERIC(10,0)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.psp_sap_method_call(
    sap_method_call_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    screen_path CHARACTER VARYING(400),
    service_name CHARACTER VARYING(255),
    method_name CHARACTER VARYING(255),
    parameters CHARACTER VARYING(4000),
    result_size NUMERIC(19,0) DEFAULT - 1,
    elapsed_millis NUMERIC(19,0) DEFAULT - 1,
    exception_trace CHARACTER VARYING(4000),
    security_principal CHARACTER VARYING(255),
    session_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.psp_source_system_transmission(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.test1(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.test2(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



-- ------------ Write CREATE-PARTITION-stage scripts -----------

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-01-01 00:00:00') TO (MAXVALUE)
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-02-01 00:00:00') TO ('2009-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-04-01 00:00:00') TO ('2009-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-06-01 00:00:00') TO ('2009-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



-- ------------ Write CREATE-SUBPARTITION-stage scripts -----------

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('QBDT');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('AS400');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('CRIS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        DEFAULT;



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('EWS');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN (NULL);



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('PSP');



CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('QBDT');



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




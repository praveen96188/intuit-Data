
create table prvn.qbdt_request_info_backup as select  /*+ Set(max_parallel_workers_per_gather 4)*/ * from ibobadm.psp_qbdt_request_info;

CREATE INDEX qbdt_request_info_backup_crdt
ON prvn.qbdt_request_info_backup
USING BTREE (created_date );

CREATE INDEX qbdt_request_info_backup_fk1
ON prvn.qbdt_request_info_backup
USING BTREE (source_system_transmission_fk , realm_id );

CREATE INDEX  qbdt_request_info_backup_fk2 ON prvn.qbdt_request_info_backup USING BTREE
(COMPANY_FK, REALM_ID);

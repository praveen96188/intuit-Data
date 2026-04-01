create table prvn.qbdt_request_info_backup_data as select  /*+ Set(max_parallel_workers_per_gather 4)*/ * from ibobadm.psp_qbdt_request_info;

CREATE INDEX qbdt_request_info_backup_data_crdt
ON prvn.qbdt_request_info_backup_data
USING BTREE (created_date );

CREATE INDEX qbdt_request_info_backup_data_fk1
ON prvn.qbdt_request_info_backup_data
USING BTREE (source_system_transmission_fk , realm_id );

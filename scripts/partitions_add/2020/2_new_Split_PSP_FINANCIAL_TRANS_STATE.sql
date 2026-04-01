set lines 300
set echo on timing on feedback on 
set pagesize 500
SET MARKUP HTML ON ENTMAP ON SPOOL ON PREFORMAT OFF ;
alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('02/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M012020 tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('03/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M022020 tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('04/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M032020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('05/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M042020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('06/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M052020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('07/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M062020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('08/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M072020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('09/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M082020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('10/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M092020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('11/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M102020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('12/01/2020', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M112020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_FINANCIAL_TRANS_STATE
split partition FINANCIAL_TXN_STATE_9999 at (to_date('01/01/2021', 'MM/DD/YYYY'))
into
(partition FINANCIAL_TXN_STATE_M122020  tablespace PSP_LOB01,
 partition FINANCIAL_TXN_STATE_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_FINANCIAL_TRANS_STATE' and partition_name like 'FINANCIAL_TXN_STATE_M%2020' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_FINANCIAL_TRANS_STATE' and partition_name like 'FINANCIAL_TXN_STATE_M%2020' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_FINANCIAL_TRANS_STATE') and partition_name like 'FINANCIAL_TXN_STATE_M%2020' order by partition_position, index_name;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_FINANCIAL_TRANS_STATE' and partition_name like 'FINANCIAL_TXN_STATE_9999%' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_FINANCIAL_TRANS_STATE' and partition_name = 'FINANCIAL_TXN_STATE_9999' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_FINANCIAL_TRANS_STATE') and partition_name = 'FINANCIAL_TXN_STATE_9999' order by partition_position, index_name;

select owner, object_name, object_type, status from dba_objects where object_name not like 'BIN%' and owner like 'PSP%' and status != 'VALID' order by 1,3,2;


spool off

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('02/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M012023 ,
 partition SRCSYSTRNS_9999 
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('03/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M022023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('04/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M032023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('05/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M042023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('06/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M052023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('07/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M062023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('08/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M072023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('09/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M082023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('10/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M092023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('11/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M102023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('12/01/2023', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M112023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;

alter table ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION
split partition SRCSYSTRNS_9999 at (to_date('01/01/2024', 'MM/DD/YYYY'))
into
(partition SRCSYSTRNS_M122023  tablespace PSP_LOB01,
 partition SRCSYSTRNS_9999  tablespace PSP_LOB01
LOB (REQUEST_DOCUMENT, RESPONSE_DOCUMENT) STORE AS (TABLESPACE PSP_LOB01)
 )
UPDATE INDEXES;


select index_name, index_type, tablespace_name, partitioned, status from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' order by partitioned, index_name;
select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'ibobadm' and segment_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' order by partition_name;
select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'ibobadm' and segment_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2023%' order by partition_name;
select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'ibobadm' and segment_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2011%' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2023%' order by partition_position;
select table_name, subpartition_name, high_value, tablespace_name from dba_tab_subpartitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2023%' order by partition_name, subpartition_position;
select index_name, partition_name, high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'ibobadm' and index_name in (select index_name from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION') and partition_name like 'SRCSYSTRNS_M%2023%' order by partition_position;
select index_name, subpartition_name, high_value, tablespace_name, status from dba_ind_subpartitions where index_owner = 'ibobadm' and index_name in (select index_name from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION') and partition_name like 'SRCSYSTRNS_M%2023%' order by partition_name, subpartition_position;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2011%' order by partition_position;
select table_name, subpartition_name, high_value, tablespace_name from dba_tab_subpartitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2011%' order by partition_name, subpartition_position;
select index_name, partition_name, high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'ibobadm' and index_name in (select index_name from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION') and partition_name like 'SRCSYSTRNS_M%2011%' order by partition_position;
select index_name, subpartition_name, high_value, tablespace_name, status from dba_ind_subpartitions where index_owner = 'ibobadm' and index_name in (select index_name from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION') and partition_name like 'SRCSYSTRNS_M%2011%' order by partition_name, subpartition_position;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'ibobadm' and segment_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_9999%' order by partition_name;
select table_name, partition_name,high_value, tablespace_name from dba_tab_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name = 'SRCSYSTRNS_9999' order by partition_position;
select table_name, subpartition_name, high_value, tablespace_name from dba_tab_subpartitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name = 'SRCSYSTRNS_9999' order by partition_name, subpartition_position;
select index_name, partition_name, high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'ibobadm' and index_name in (select index_name from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION') and partition_name = 'SRCSYSTRNS_9999' order by partition_position;
select index_name, subpartition_name, high_value, tablespace_name, status from dba_ind_subpartitions where index_owner = 'ibobadm' and index_name in (select index_name from dba_indexes where owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION') and partition_name = 'SRCSYSTRNS_9999' order by partition_name, subpartition_position;

SELECT owner, table_name "Table", column_name "Column", segment_name "Segment", tablespace_name "TBS", index_name "Index", PARTITIONED FROM dba_lobs WHERE owner = 'ibobadm' and table_name not like 'BIN%';
select table_name, column_name, lob_name, partition_name, lob_partition_name, lob_indpart_name from dba_lob_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2023%' order by partition_position;
select table_name, column_name, lob_name, lob_partition_name, subpartition_name, lob_subpartition_name, LOB_INDSUBPART_NAME from dba_lob_subpartitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and  lob_partition_name in (select lob_partition_name from dba_lob_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2023%') order by lob_partition_name, subpartition_name;
select table_name, column_name, lob_name, partition_name, lob_partition_name, lob_indpart_name from dba_lob_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2011%' order by partition_position;
select table_name, column_name, lob_name, lob_partition_name, subpartition_name, lob_subpartition_name, LOB_INDSUBPART_NAME from dba_lob_subpartitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and  lob_partition_name in (select lob_partition_name from dba_lob_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_M%2011%') order by lob_partition_name, subpartition_name;


select table_name, column_name, lob_name, partition_name, lob_partition_name, lob_indpart_name from dba_lob_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_9999%' order by partition_position;
select table_name, column_name, lob_name, lob_partition_name, subpartition_name, lob_subpartition_name, LOB_INDSUBPART_NAME from dba_lob_subpartitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and  lob_partition_name in (select lob_partition_name from dba_lob_partitions where table_owner = 'ibobadm' and table_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION' and partition_name like 'SRCSYSTRNS_9999%') order by lob_partition_name, subpartition_name;

select owner, object_name, object_type, status from dba_objects where object_name not like 'BIN%' and owner like 'PSP%' and status != 'VALID' order by 1,3,2

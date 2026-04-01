spool new_Split_PSP_ENTRY_DETAIL_RECORD.html
set lines 300
set echo on timing on feedback on
set pagesize 500
SET MARKUP HTML ON ENTMAP ON SPOOL ON PREFORMAT OFF ;


alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('02/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M012021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('03/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M022021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;


alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('04/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M032021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('05/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M042021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('06/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M052021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('07/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M062021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('08/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M072021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('09/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M082021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('10/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M092021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('11/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M102021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('12/01/2021', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M112021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_ENTRY_DETAIL_RECORD
split partition ENTRY_DETAIL_RCD_9999 at (to_date('01/01/2022', 'MM/DD/YYYY'))
into
(partition ENTRY_DETAIL_RCD_M122021 tablespace PSP_LOB01,
 partition ENTRY_DETAIL_RCD_9999 tablespace PSP_LOB01
 )
UPDATE INDEXES;



select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_ENTRY_DETAIL_RECORD' and partition_name like 'ENTRY_DETAIL_RCD_M%2021' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_ENTRY_DETAIL_RECORD' and partition_name like 'ENTRY_DETAIL_RCD_M%2021' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_ENTRY_DETAIL_RECORD') and partition_name like 'ENTRY_DETAIL_RCD_M%2021' order by partition_position, index_name;

select TABLE_NAME,PARTITIONING_TYPE,PARTITION_COUNT,STATUS from dba_part_tables where owner='PSPADM';

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_ENTRY_DETAIL_RECORD' and partition_name like 'ENTRY_DETAIL_RCD_M%2011' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_ENTRY_DETAIL_RECORD' and partition_name like 'ENTRY_DETAIL_RCD_M%2011' order by partition_position;
select index_name, partition_name, high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_ENTRY_DETAIL_RECORD') and partition_name like 'ENTRY_DETAIL_RCD_M%2011' order by partition_position, index_name;


select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_ENTRY_DETAIL_RECORD' and partition_name like 'ENTRY_DETAIL_RCD_9999%' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_ENTRY_DETAIL_RECORD' and partition_name = 'ENTRY_DETAIL_RCD_9999' order by partition_position;
select index_name, partition_name, high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_ENTRY_DETAIL_RECORD') and partition_name = 'ENTRY_DETAIL_RCD_9999' order by partition_position, index_name;

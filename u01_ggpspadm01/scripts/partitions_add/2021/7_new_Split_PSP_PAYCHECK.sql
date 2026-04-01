alter table pspadm.PSP_PAYCHECK
split partition PAYCHECK_9999 at (to_date('07/01/2021', 'MM/DD/YYYY'))
into
(partition PAYCHECK_BA12021  tablespace PSP_LOB01,
 partition PAYCHECK_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_PAYCHECK
split partition PAYCHECK_9999 at (to_date('01/01/2022', 'MM/DD/YYYY'))
into
(partition PAYCHECK_BA22021  tablespace PSP_LOB01,
 partition PAYCHECK_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_PAYCHECK' and partition_name like 'PAYCHECK_BA%2021' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_PAYCHECK' and partition_name like 'PAYCHECK_BA%2021' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_PAYCHECK') and partition_name like 'PAYCHECK_BA%2021' order by partition_position, index_name;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_PAYCHECK' and partition_name like 'PAYCHECK_BA%2011' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_PAYCHECK' and partition_name like 'PAYCHECK_BA%2011' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_PAYCHECK') and partition_name like 'PAYCHECK_BA%2011' order by partition_position, index_name;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_PAYCHECK' and partition_name like 'PAYCHECK_9999%' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_PAYCHECK' and partition_name = 'PAYCHECK_9999' order by partition_position;
select index_name, partition_name, high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_PAYCHECK') and partition_name = 'PAYCHECK_9999' order by partition_position, index_name;

select owner, object_name, object_type, status from dba_objects where object_name not like 'BIN%' and owner like 'PSP%' and status != 'VALID' order by 1,3,2;

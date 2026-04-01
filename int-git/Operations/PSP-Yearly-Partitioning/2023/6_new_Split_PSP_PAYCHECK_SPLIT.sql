alter table pspadm.PSP_PAYCHECK_SPLIT
split partition PAYCHKSPLIT_9999 at (to_date('07/01/2023', 'MM/DD/YYYY'))
into
(partition PAYCHKSPLIT_BA12023 tablespace PSP_LOB01,
 partition PAYCHKSPLIT_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

alter table pspadm.PSP_PAYCHECK_SPLIT
split partition PAYCHKSPLIT_9999 at (to_date('01/01/2024', 'MM/DD/YYYY'))
into
(partition PAYCHKSPLIT_BA22023  tablespace PSP_LOB01,
 partition PAYCHKSPLIT_9999  tablespace PSP_LOB01
 )
UPDATE INDEXES;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_PAYCHECK_SPLIT' and partition_name like 'PAYCHKSPLIT_BA%2023' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_PAYCHECK_SPLIT' and partition_name like 'PAYCHKSPLIT_BA%2023' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_PAYCHECK_SPLIT') and partition_name like 'PAYCHKSPLIT_BA%2023' order by partition_position, index_name;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_PAYCHECK_SPLIT' and partition_name like 'PAYCHKSPLIT_BA%2011' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_PAYCHECK_SPLIT' and partition_name like 'PAYCHKSPLIT_BA%2011' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_PAYCHECK_SPLIT') and partition_name like 'PAYCHKSPLIT_BA%2011' order by partition_position, index_name;

select segment_name, segment_type, partition_name, tablespace_name from dba_segments where owner = 'PSPADM' and segment_name = 'PSP_PAYCHECK_SPLIT' and partition_name like 'PAYCHKSPLIT_9999%' order by partition_name;
select table_name, partition_name, high_value, tablespace_name from dba_tab_partitions where table_owner = 'PSPADM' and table_name = 'PSP_PAYCHECK_SPLIT' and partition_name = 'PAYCHKSPLIT_9999' order by partition_position;
select index_name, partition_name,  high_value, tablespace_name, status from dba_ind_partitions where index_owner = 'PSPADM' and index_name in (select index_name from dba_indexes where owner = 'PSPADM' and table_name = 'PSP_PAYCHECK_SPLIT') and partition_name = 'PAYCHKSPLIT_9999' order by partition_position, index_name;

select owner, object_name, object_type, status from dba_objects where object_name not like 'BIN%' and owner like 'PSP%' and status != 'VALID' order by 1,3,2;

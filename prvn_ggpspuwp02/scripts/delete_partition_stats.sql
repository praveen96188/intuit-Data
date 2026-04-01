col spoolname new_value spoolname
select 'delete_partition_stats_'||SYS_CONTEXT('USERENV', 'DB_NAME')||'_'||to_char(sysdate,'YYMONDD_HH24.MI.SS') spoolname from dual;
spool '&spoolname' 

set pages 9999
col owner format a20
col table_owner format a20
col table_name format a40
col partition_name format a20

prompt === Table stats before cleanup ===

select owner, table_name, num_rows, last_analyzed, sample_size 
from dba_tables
where owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1')
and last_analyzed is not null;

prompt === Partition stats before cleanup ===

select table_owner, table_name, num_rows, last_analyzed, sample_size, partition_name
from dba_tab_partitions
where table_owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1')
and last_analyzed is not null
order by table_name, partition_name;

begin
  for i in (
      select table_owner, table_name, num_rows, last_analyzed, sample_size, partition_name
      from dba_tab_partitions
      where table_owner = 'QBO_DATA'
      and table_name IN ('TXHEADERS_1', 'TXDETAILS_1')
      and last_analyzed is not null
)
  loop
     DBMS_STATS.DELETE_TABLE_STATS ( ownname => i.table_owner, tabname => i.table_name,  partname => i.partition_name);
  end loop;
end;
/


prompt === Table stats after cleanup ===

select owner, table_name, num_rows, last_analyzed, sample_size 
from dba_tables
where owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1');

prompt === Partition stats after cleanup ===

select table_owner, table_name, num_rows, last_analyzed, sample_size, partition_name
from dba_tab_partitions
where table_owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1')
and last_analyzed is not null
order by table_name, partition_name;



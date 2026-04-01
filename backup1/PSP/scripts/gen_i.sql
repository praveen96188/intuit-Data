
SET LONG 50000 LONGCHUNKSIZE 50000 PAGESIZE 0 LINESIZE 1000 FEEDBACK OFF VERIFY OFF TRIMSPOOL ON

BEGIN
   DBMS_METADATA.set_transform_param (DBMS_METADATA.session_transform, 'SQLTERMINATOR', true);
   DBMS_METADATA.set_transform_param (DBMS_METADATA.session_transform, 'PRETTY', true);
   DBMS_METADATA.set_transform_param (DBMS_METADATA.session_transform, 'SEGMENT_ATTRIBUTES', false);
   DBMS_METADATA.set_transform_param (DBMS_METADATA.session_transform, 'STORAGE', false);
END;
/

SELECT DBMS_METADATA.get_ddl ('INDEX', index_name, owner)
FROM   dba_indexes
WHERE  owner      = UPPER('pspadm')
AND    table_name in (select table_name from dba_tables where lower(table_name) not like '%tablecounts%'
  and lower(table_name) not like '%explain%'
  and lower(table_name) not like '%backup%'
  and lower(table_name) not like 'tmp_%'
  and lower(table_name) not like 'temp_%'
  and lower(table_name) not like 'tt%'
  and lower(table_name) not like 'sys_%'
  and lower(table_name) not like 'sql_%'
  and lower(table_name) not like 'ht_%'
  and lower(table_name) not like '%april%'
  and lower(table_name) not like '%alert%'
  and lower(table_name) not like '%bkp%'
  and lower(table_name) not like '%$%'
  and lower(table_name) not like '%test%'
  and lower(table_name) not like '%empty%'
  and lower(table_name) not like 'arch%'
  and lower(table_name) not like '%hist%'
  and lower(table_name) not like '%toad_plan%'
  and lower(table_name) not like 'gg_he%'
  and lower(table_name) not like 'psp_posting_rule_mar7'
  and lower(table_name) not like 'psp_transaction_type_mar7'
  and lower(table_name) not like 'stats_11dec'
  and lower(table_name) not like 'pet_feb27'
  and lower(table_name) not like 'distinct%'
  and lower(table_name) not like 'cmp%'
  and lower(table_name) not like 'z_%'
  and lower(table_name) not like 'as400%') order by table_name;


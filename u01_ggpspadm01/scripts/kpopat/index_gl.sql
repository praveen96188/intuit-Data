set pages 100
select INDEX_NAME,INDEX_type ,GLOBAL_STATS from dba_indexes where index_name='&index_name';

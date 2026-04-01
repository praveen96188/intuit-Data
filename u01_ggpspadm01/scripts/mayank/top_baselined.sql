 select sql_handle||','||SQL_ID||','||listagg(cluster_id, '#') within group (order by sql_id)  from QBO_CLUSTER_SQL_BASELINE where trunc(CREATE_DATE) = trunc(sysdate) group by sql_handle||','||SQL_ID
/


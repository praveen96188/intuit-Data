col sql_fulltext for a300
set long 100000
select sql_fulltext from v$sql where sql_id='&sql_id' and rownum < 2;

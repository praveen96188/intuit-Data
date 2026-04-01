
set serveroutput on size unlimited
set lines 400 pages 999 trimspool on
col db_name format a12
col table_name format a40
col column_name format a49
col data_type format a12
col low_value format a40
col high_value format a40
col density format 99.999999
col low_value format a20
col high_value format a20
col method_opt_pref format a100
alter session set nls_date_format = 'yyyy-mm-dd hh24:mi:ss';

break on table_name dup skip 1

select c.table_name, c.column_name, c.data_type, c.num_distinct,  c.num_nulls, round(c.num_nulls/t.num_rows,2)*100 as pct_null, c.density, c.sample_size, t.last_analyzed, c.histogram, c.num_buckets,
decode(data_type,
                'VARCHAR2',to_char(raw_to_varchar2(low_value)),
                'DATE',to_char(raw_to_date(low_value), 'yyyy-mm-dd'),
                'NUMBER',to_char(raw_to_num(low_value))
        ) low_value,
        decode(data_type,
                'VARCHAR2',to_char(raw_to_varchar2(high_value)),
                'DATE',to_char(raw_to_date(high_value), 'yyyy-mm-dd'),
                'NUMBER',to_char(raw_to_num(high_value))
        ) high_value
from dba_tab_columns c, dba_tables t
where c.owner = t.owner
  and c.table_name = t.table_name
  and c.owner = 'QBO_DATA'
  and t.table_name IN ( 'TXHEADERS_1', 'TXDETAILS_1')
order by c.table_name desc, c.column_id;

select table_name, histogram, count(*)
from dba_tab_columns
where owner = 'QBO_DATA'
  and table_name IN ( 'TXHEADERS_1', 'TXDETAILS_1')
group by table_name, histogram
order by 1 desc, 2;

select table_name, dbms_stats.get_prefs(ownname=>'QBO_DATA',tabname=>t.table_name,pname=>'METHOD_OPT') method_opt_pref
from dba_tables t
where t.owner = 'QBO_DATA'
and t.table_name IN ( 'TXHEADERS_1', 'TXDETAILS_1');


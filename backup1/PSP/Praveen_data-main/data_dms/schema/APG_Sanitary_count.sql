
--Invalid object check

SELECT pg_class.relname as Object_name 
FROM pg_class, pg_index 
WHERE pg_index.indisvalid = false 
AND pg_index.indexrelid = pg_class.oid;


--objects count
SELECT
 n.nspname as schema_name
 ,CASE c.relkind
    WHEN 'r' THEN 'table'
    WHEN 'v' THEN 'view'
    WHEN 'i' THEN 'index'
    WHEN 'S' THEN 'sequence'
    WHEN 'm' THEN 'materialized view'
    WHEN 'p' THEN 'partitioned table'
 END as object_type
 ,count(1) as object_count
FROM pg_catalog.pg_class c
LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind IN ('r','v','i','S','m','p') and n.nspname='pspadm'
GROUP BY  n.nspname,
 CASE c.relkind
    WHEN 'r' THEN 'table'
    WHEN 'v' THEN 'view'
    WHEN 'i' THEN 'index'
    WHEN 'S' THEN 'sequence'
    WHEN 'm' THEN 'materialized view'
    WHEN 'p' THEN 'partitioned table'
 END
ORDER BY n.nspname,
 CASE c.relkind
    WHEN 'r' THEN 'table'
    WHEN 'v' THEN 'view'
    WHEN 'i' THEN 'index'
    WHEN 'S' THEN 'sequence'
    WHEN 'm' THEN 'materialized view'
    WHEN 'p' THEN 'partitioned table'
 END;


--Partitioned tables with their respective partition count

SELECT
    nmsp_parent.nspname AS schemaname,
    parent.relname      AS partition_main_table,
    count(child.relname ) AS  partition_child_table_count   
FROM pg_inherits
    JOIN pg_class parent            ON pg_inherits.inhparent = parent.oid
    JOIN pg_class child             ON pg_inherits.inhrelid   = child.oid
    JOIN pg_namespace nmsp_parent   ON nmsp_parent.oid  = parent.relnamespace
    JOIN pg_namespace nmsp_child    ON nmsp_child.oid   = child.relnamespace
WHERE parent.relname in (select relname  from pg_class where relkind = 'p') 
group by schemaname,partition_main_table 
order by partition_child_table_count ASC;


--Tables with their primary key columns

select dt.table_schema,
       dt.table_name,
       dt.constraint_name as Primary_Key,
       STRING_AGG(dt.column_name,',') as Primary_key_Column
       from (
select distinct tab.table_schema,
       tab.table_name,
       tco.constraint_name,
       kcu.column_name
from information_schema.tables tab
left join information_schema.table_constraints tco
          on tco.table_schema = tab.table_schema
          and tco.table_name = tab.table_name
          and tco.constraint_type = 'PRIMARY KEY'
left join information_schema.key_column_usage kcu 
          on kcu.constraint_name = tco.constraint_name
          and kcu.constraint_schema = tco.constraint_schema
          and kcu.constraint_name = tco.constraint_name
where tab.table_schema='pspadm'
     and tab.table_type = 'BASE TABLE'
     and tco.constraint_name is not null) as dt
group by dt.table_schema,
         dt.table_name,
         dt.constraint_name 
order by dt.table_schema,
         dt.table_name;

--tables with their index names and their respective index columns 

with
 ind_cols as (
select
    n.nspname as schema_name,
    t.relname as table_name,
    i.relname as index_name,
    a.attname as column_name,
    1 + array_position(ix.indkey, a.attnum) as column_position
from
     pg_catalog.pg_class t
join pg_catalog.pg_attribute a on t.oid    =      a.attrelid 
join pg_catalog.pg_index ix    on t.oid    =     ix.indrelid
join pg_catalog.pg_class i     on a.attnum = any(ix.indkey)
                              and i.oid    =     ix.indexrelid
join pg_catalog.pg_namespace n on n.oid    =      t.relnamespace
where t.relkind = 'r'
order by
    t.relname,
    i.relname,
    array_position(ix.indkey, a.attnum)
)
select schema_name,table_name,index_name,String_agg(column_name,',') as index_column_name ,String_agg(column_position::character varying,',') as index_column_position
from ind_cols
where schema_name = 'pspadm'
  and table_name  in (select tablename  from pg_tables where schemaname='pspadm' order by tablename asc)
  group by schema_name,
           table_name,
           index_name
  order by schema_name,
            table_name;

-- table with their FK constraint names and their respective constraint columns
select dt.table_schema,
       dt.table_name,
       dt.constraint_name as Foreign_Key,
       STRING_AGG(dt.column_name,',') as Foreign_Key_column_name
       from (
select distinct tab.table_schema,
       tab.table_name,
       tco.constraint_name,
       kcu.column_name
from information_schema.tables tab
left join information_schema.table_constraints tco
          on tco.table_schema = tab.table_schema
          and tco.table_name = tab.table_name
          and tco.constraint_type = 'FOREIGN KEY'
left join information_schema.key_column_usage kcu 
          on kcu.constraint_name = tco.constraint_name
          and kcu.constraint_schema = tco.constraint_schema
          and kcu.constraint_name = tco.constraint_name
where tab.table_schema='pspadm'
     and tab.table_type = 'BASE TABLE'
     and tco.constraint_name is not null) as dt
group by dt.table_schema,
         dt.table_name,
         dt.constraint_name 
order by dt.table_schema,
         dt.table_name;





--create schema
create schema backup_oos;

--create OOS metadata table
create table backup_oos.oos_metadata (
  op_type character varying(10),
  status character varying(20),
  table_name character varying(255),
  pk_seq character varying(255),
  company_fk character varying(255),
  pk_cols character varying(255)
);

--create index
create index idx_tab_name on backup_oos.oos_metadata (table_name);

--create table having Primary Key cols
create table backup_oos.pspadm_tables_pk_cols as select tab.table_name,
       string_agg(kcu.column_name, ', ') as key_columns
from information_schema.tables tab
         left join information_schema.table_constraints tco
                   on tco.table_schema = tab.table_schema
                       and tco.table_name = tab.table_name
                       and tco.constraint_type = 'PRIMARY KEY'
         left join information_schema.key_column_usage kcu
                   on kcu.constraint_name = tco.constraint_name
                       and kcu.constraint_schema = tco.constraint_schema
                       and kcu.constraint_name = tco.constraint_name
where tab.table_schema ='pspadm'
group by tab.table_name
order by tab.table_name;

--create index
create index idx_pspadm_tables_pk_cols_table_name on backup_oos.pspadm_tables_pk_cols(table_name);

--update OOS metadata table with primary key cols of tables
update backup_oos.oos_metadata x                                                                   
 set pk_cols=
 (select key_columns from backup_oos.pspadm_tables_pk_cols tab
where x.table_name=tab.table_name);
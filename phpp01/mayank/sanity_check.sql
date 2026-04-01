SET PAGESIZE 0
SET HEAD on ECHO OFF
SET MARKUP HTML ON
set HEADING on
set FEEDBACK on
spool SANITY_CHECK.html

HEAD 'Total object types and its count'
select owner,OBJECT_TYPE,count(*)
from DBA_OBJECTS
where OWNER in ('PSPADM')
group by OBJECT_TYPE, owner
order by 1, 2;

PROMPT 'Invalid Objects'
select owner, object_type, object_name from dba_objects where status <> 'VALID';

PROMPT 'Total Tables' 
select owner, count(*) from dba_tables where owner in ('PSPADM') group by owner order by 1;

PROMPT 'Partitioned Tables and its partition count'
select table_name, count(*) from dba_tab_partitions where table_owner = 'PSPADM' group by table_name order by TABLE_NAME;

PROMPT 'All Primary Key Columns' 
select table_name, CONSTRAINT_NAME, listagg(COLUMN_NAME, ',') within group (order by COLUMN_NAME) as cols
from DBA_CONS_COLUMNS
where CONSTRAINT_NAME in
      (select constraint_name from DBA_CONSTRAINTS where constraint_type = 'P' and owner = 'PSPADM')
  and owner = 'PSPADM'
group by table_name, CONSTRAINT_NAME
order by TABLE_NAME;

PROMPT 'All Primary Key Indexes'
select i.table_name, i.INDEX_NAME
from DBA_INDEXES i, DBA_CONSTRAINTS c
where i.OWNER='PSPADM'
  and c.CONSTRAINT_TYPE='P'
  and i.OWNER = c.OWNER
  and i.TABLE_NAME(+) = c.TABLE_NAME
  and i.INDEX_NAME(+)=c.CONSTRAINT_NAME
order by i.table_name, i.INDEX_NAME;

PROMPT 'All Index types and its count'
select index_type, count(*) from dba_indexes where owner in ('PSPADM') group by index_type order by 1;

PROMPT 'All constraint types and its count'
select constraint_type, count(*) from dba_constraints where owner in ('PSPADM') group by constraint_type order by 1;

PROMPT 'Enabled Triggers Count'
select status, count(*) from dba_triggers where owner in ('PSPADM') group by status;

PROMPT 'Partitioned PK indexes and its partition count'
select index_name, count(*)
from dba_ind_partitions
where index_owner = 'PSPADM' and INDEX_NAME like '%_PK'
group by INDEX_NAME
order by INDEX_NAME;

PROMPT 'All FK constraint Names, FK constraint columns and respective Primary table, primary table columns'
SELECT
    c.child_tab_owner           AS schema_name,
    c.table_name,
    c.constraint_name           AS fk_cons_name,
    cc.fk_column AS fk_cols,
    p.table_name AS prim_table, pc.ref_column AS pk_cols
FROM ( SELECT owner child_tab_owner, table_name, constraint_name, r_constraint_name FROM DBA_CONSTRAINTS WHERE owner = upper('pspadm') AND constraint_type = 'R') c,
     ( SELECT owner parent_tab_owner, table_name, constraint_name FROM DBA_CONSTRAINTS WHERE owner = upper('pspadm') AND constraint_type IN('P', 'U') ) p,
     ( SELECT owner, table_name, constraint_name, listagg(column_name, ', ') WITHIN group(ORDER BY position) fk_column
       FROM DBA_CONS_COLUMNS WHERE owner = upper('pspadm') GROUP BY owner, table_name, constraint_name ) cc,
     ( SELECT owner, table_name, constraint_name, listagg(column_name, ', ') WITHIN group(ORDER BY position) ref_column
       FROM DBA_CONS_COLUMNS WHERE owner = upper('pspadm') GROUP BY owner, table_name, constraint_name ) pc
WHERE c.r_constraint_name = p.constraint_name
  AND c.table_name = cc.table_name AND c.constraint_name = cc.constraint_name AND c.child_tab_owner = cc.owner
  AND p.table_name = pc.table_name AND p.constraint_name = pc.constraint_name AND p.parent_tab_owner = pc.owner
order by TABLE_NAME;

PROMPT 'Only FK indexes'
select ic.table_name,ic.index_name,listagg(ic.COLUMN_NAME,',') within group(order by ic.COLUMN_POSITION) ind_cols
from dba_ind_columns ic,
     (
         select ucc.table_name,ucc.CONSTRAINT_NAME,listagg(ucc.COLUMN_NAME, ',') within group ( order by ucc.POSITION) fk_cols
         from dba_cons_columns ucc, dba_constraints c
         where c.constraint_name = ucc.constraint_name
	       and c.table_name = ucc.table_name
           and c.constraint_type ='R'
           and c.OWNER='PSPADM'
         group by ucc.table_name, ucc.CONSTRAINT_NAME
     ) a
where ic.table_name = a.table_name
  and ic.INDEX_NAME=a.CONSTRAINT_NAME
   and ic.TABLE_OWNER='PSPADM'
group by ic.table_name,ic.index_name
order by ic.table_name,ic.index_name;

spool off


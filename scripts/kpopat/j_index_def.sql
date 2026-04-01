
rem
rem	Script:		index_defs.sql
rem	Author:		Jonathan Lewis
rem

set pagesize 60
set linesize 280
set trimspool off

 
column t_rows      format 99,999,999,999
column i_rows      format 99,999,999,999
column table_name  format a32
column index_name  format a32
column column_name format a32
column func        format a32
 
break   on table_name skip 1 on index_name skip 1 on uniqueness on pref_len -
        on t_rows on i_rows on distinct_keys on blevel on leaf_blocks

spool index_defs
 
select
	tab.table_name,
        ind.index_name,
        ind.uniqueness,
        ind.prefix_length       pref_len,
        tab.num_rows            t_rows,
        ind.num_rows            i_rows,
        ind.distinct_keys,
        ind.blevel,
        ind.leaf_blocks,
        inc.column_name,
        case inc.descend
            when 'DESC'
                then 'D'
                else null
        end                     descend,
        tbc.nullable,
        tbc.num_distinct,
        tbc.num_nulls,
        inx.column_expression   func
from
        dba_tables          tab,
        dba_indexes         ind,
        dba_ind_columns     inc,
        dba_tab_cols        tbc,
        dba_ind_expressions inx
where
        tab.owner        = 'QBO_DATA'
-- and     tab.table_name   = 'YYYYYYYY'
/*                  */
and     ind.table_owner  = tab.owner
and     ind.table_name   = tab.table_name
/*                  */
and     inc.table_owner  = ind.table_owner
and     inc.table_name   = ind.table_name
and     inc.index_owner  = ind.owner
and     inc.index_name   = ind.index_name
/*                  */
and     tbc.owner        = inc.table_owner
and     tbc.table_name   = inc.table_name
and     tbc.column_name  = inc.column_name
/*                  */
and     inx.table_owner(+)     = inc.table_owner
and     inx.table_name(+)      = inc.table_name
and     inx.index_owner(+)     = inc.index_owner
and     inx.index_name(+)      = inc.index_name
and     inx.column_position(+) = inc.column_position
order by
	tab.table_name,
        ind.index_name,
        inc.column_position
;

spool off



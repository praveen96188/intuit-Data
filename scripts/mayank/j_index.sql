rem
rem	Script:		indexes.sql
rem	Author:		Jonathan Lewis
rem	Dated:		Mar 2004
rem	Purpose:	Indicate index quality
rem
rem	Last tested  
rem		10.2.0.3
rem		10.1.0.4
rem		 9.2.0.6
rem		 8.1.7.4
rem	Not tested
rem		11.1.0.6
rem
rem	Notes:
rem	For a system which has reasonably accurate statistics,
rem	this script may indicate indexes which are a waste of 
rem	space - either because columns could be dropped, order
rem	re-arranged, or indexes are redundant.
rem
rem	It reports
rem		Table number of rows
rem		Indexes for each table: number of entries, number of distinct keys
rem		Columns (in order) for each index: number of distince values
rem
rem	Has to be run by a privileged user.
rem	It does NOT cater for partitioned objects or IOTs
rem

start setenv
set timing off

define m_schema = &m_schema_name

set pagesize 999
set linesize 255
set trimspool on

column table_name       format a30
column index_name       format a30
column uniqueness       format a6       heading "Unique"
column column_name      format a20

column t_num_rows       format 999,999,999
column i_num_rows       format 999,999,999
column distinct_keys    format 999,999,999
column num_distinct     format 999,999,999


break	on table_name skip 1 - 
	on t_num_rows - 
	on index_name skip 1 -
	on uniqueness - 
	on i_num_rows -
	on distinct_keys

spool indexes

select  /*+ ordered use_hash(ix) use_hash(ic) use_hash(tc) */
        ta.table_name, ta.num_rows t_num_rows,
        ix.index_name, ix.uniqueness, ix.num_rows i_num_rows, ix.distinct_keys,
        ic.column_name,
        tc.num_distinct
from
        (
        select /*+ no_merge */
                table_name, 
		num_rows,  
		last_analyzed
        from    dba_tables
        where   owner = upper('&m_schema')
        )       ta,
        (
        select /*+ no_merge */
                table_name, index_name, 
		decode(uniqueness,'UNIQUE','Yes',null) uniqueness, 
		num_rows, distinct_keys, last_analyzed
        from
                dba_indexes
        where
                table_owner = upper('&m_schema')
        )       ix,
        (
        select /*+ no_merge */
                table_name, index_name, 
		column_name, 
		column_position
        from
                dba_ind_columns
        where
                table_owner = upper('&m_schema')
        )       ic,
        (
        select /*+ no_merge */
                table_name, column_name, 
		num_distinct
        from
                dba_tab_cols
        where
                owner = upper('&m_schema')
        )       tc
where
        ix.table_name = ta.table_name
and     ic.table_name = ix.table_name
and     ic.index_name = ix.index_name
and     tc.table_name = ic.table_name
and     tc.column_name = ic.column_name
order by
        ta.table_name,
        ix.index_name,
        ic.column_position
;

spool off


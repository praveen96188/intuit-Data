create or replace package QBO_STATS_PKG
as
    procedure SET_TXHEADERS_PREF;
    procedure SET_TXDETAILS_PREF;
    -- procedure SET_TX_DATE(p_low_date date DEFAULT to_date('2014-01-11', 'yyyy-mm-dd'), p_high_date date DEFAULT trunc(sysdate) + 90);
    procedure SET_DATE_RANGE (p_owner VARCHAR2, p_table_name VARCHAR2, p_column_name VARCHAR2, p_low_date date DEFAULT to_date('2014-01-11', 'yyyy-mm-dd'), p_high_date date DEFAULT trunc(sysdate) + 90);
    procedure SET_DATE_RANGE_DESC (p_owner varchar2, p_table_name varchar2, p_column_name varchar2, p_low_date date, p_high_date date);
    function GET_DESC_COLUMN_NAME (p_owner varchar2, p_table_name varchar2, p_index_name varchar2,  p_column_position number) RETURN varchar2;
end;
/
show err

create or replace package body QBO_STATS_PKG
as

-- sets table preferences for TxHeaders, should always contain current settings

procedure SET_TXHEADERS_PREF
is
begin
   -- DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXHEADERS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id for columns size 1 tx_date');
   DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXHEADERS_1', 'METHOD_OPT', 'for all columns size 1 for all indexed columns size auto for columns size 1 tx_date for columns size 10 AUDIT_ID BILL_ID BOOKKEEPING_ACTIVITY_ID DD_CHECK_ID DEPT_ID DISCOUNT_ID FILED_TAX_RETURN_ID INVOICE_ID RECUR_DATA_ID SHIPPED_FROM_ADDRESS_ID SHIPPING_ADDRESS_ID SOURCE_REIMB_TXN_ID STATEMENT_ID TAX_AGENCY_ID');

end;

-- sets table preferences for TxDetails, should always contain current settings

procedure SET_TXDETAILS_PREF
is
begin
  -- DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXDETAILS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id deposit_id for columns size 1 tx_date');
  DBMS_STATS.SET_TABLE_PREFS ('QBO_DATA', 'TXDETAILS_1', 'METHOD_OPT', 'for all columns size 1 for all indexed columns size auto for columns size 1 tx_date for columns size 10 AUDIT_ID DEPOSIT_ID DEPT_ID DISCOUNT_ID EMPLOYEE_ID INVOICE_ID KLASS_ID OFX_TXN_ID OTHER_KLASS_ID PITEM_ID REIMB_TXN_ID SOURCE_TXN_ID STATEMENT_ID TAX_CODE_ID TAX_RATE_ID WITHHOLDING_RATE_ID WORK_JURISDICTION_ID');

end;

-- sets low/high values for TX_DATE on TxHeaders + TxDetails
-- will be replaced by generic procedure soon
-- DEPRECATED
--  procedure SET_TX_DATE (p_low_date date, p_high_date date)
--  as
--      srec            dbms_stats.statrec;
--      m_distcnt       number;
--      m_density       number;
--      m_nullcnt       number;
--      m_avgclen       number;
--      d_array         dbms_stats.datearray;
--  begin
--  
--  -- step #1: delete histogram
--  
--  dbms_output.put_line ('Fixing TxHeaders ...');
--  
--  dbms_output.put_line ('Deleting histogram ...');
--  
--   DBMS_STATS.DELETE_COLUMN_STATS (
--      ownname        => 'QBO_DATA', 
--      tabname        => 'TXHEADERS_1', 
--      colname        => 'TX_DATE', 
--      col_stat_type  => 'HISTOGRAM'
--  );
--  
--  -- step #2: set table prefs to disable histogram on tx_date creation during stats collection
--  
--   -- DBMS_STATS.SET_TABLE_PREFS ('QBO_DATA', 'TXHEADERS_1', 'METHOD_OPT', 'for all columns size auto for columns invoice_id size 10 for columns tx_date size 1');
--   SET_TXHEADERS_PREF;
--  
--  
--  -- step #3: get column statistics
--  
--  dbms_output.put_line ('Getting column stats ...');
--  
--      dbms_stats.get_column_stats(
--          ownname     => 'QBO_DATA',
--          tabname     => 'TXHEADERS_1',
--          colname     => 'TX_DATE',
--          distcnt     => m_distcnt,
--          density     => m_density,
--          nullcnt     => m_nullcnt,
--          srec        => srec,
--          avgclen     => m_avgclen
--      ); 
--  
--  -- step #4: set column statistics
--  
--      srec.bkvals := null;
--      d_array :=  dbms_stats.datearray( p_low_date, p_high_date);
--      srec.epc := 2;
--      dbms_stats.prepare_column_values(srec, d_array);
--      m_distcnt := p_high_date - p_low_date;
--      m_density := 1/m_distcnt;
--  
--  dbms_output.put_line ('Setting column stats ...');
--   
--      dbms_stats.set_column_stats(
--          ownname     => 'QBO_DATA',
--          tabname     => 'TXHEADERS_1',
--          colname     => 'TX_DATE',
--          distcnt     => m_distcnt,
--          density     => m_density,
--          nullcnt     => m_nullcnt,
--          srec        => srec,
--          avgclen     => m_avgclen
--      ); 
--  
--  dbms_output.put_line ('Fixing TxDetails ...');
--  
--  dbms_output.put_line ('Deleting histogram ...');
--  
--   DBMS_STATS.DELETE_COLUMN_STATS (
--      ownname        => 'QBO_DATA', 
--      tabname        => 'TXDETAILS_1', 
--      colname        => 'TX_DATE', 
--      col_stat_type  => 'HISTOGRAM'
--  );
--  
--  -- set table prefs to disable histogram creation during stats collection
--  
--   DBMS_STATS.SET_TABLE_PREFS ('QBO_DATA', 'TXDETAILS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id deposit_id for columns  size 1 tx_date ');
--  
--  -- step #2: get column statistics
--  
--  dbms_output.put_line ('Getting column stats ...');
--  
--      dbms_stats.get_column_stats(
--          ownname     => 'QBO_DATA',
--          tabname     => 'TXDETAILS_1',
--          colname     => 'TX_DATE',
--          distcnt     => m_distcnt,
--          density     => m_density,
--          nullcnt     => m_nullcnt,
--          srec        => srec,
--          avgclen     => m_avgclen
--      ); 
--  
--  -- step #3: set column statistics
--  
--      srec.bkvals := null;
--      srec.epc := 2;
--      dbms_stats.prepare_column_values(srec, d_array);
--      m_distcnt := p_high_date - p_low_date;
--      m_density := 1/m_distcnt;
--  
--  dbms_output.put_line ('Setting column stats ...');
--   
--      dbms_stats.set_column_stats(
--          ownname     => 'QBO_DATA',
--          tabname     => 'TXDETAILS_1',
--          colname     => 'TX_DATE',
--          distcnt     => m_distcnt,
--          density     => m_density,
--          nullcnt     => m_nullcnt,
--          srec        => srec,
--          avgclen     => m_avgclen
--      ); 
--   
--   
--  exception
--      when others then
--          raise;      -- should handle div/0
--   
--  end;

-- Generic procedure to set low/high values for date columns on any table/column

procedure SET_DATE_RANGE (p_owner varchar2, p_table_name varchar2, p_column_name varchar2, p_low_date date, p_high_date date)
as
    srec            dbms_stats.statrec;
    m_distcnt       number;
    m_density       number;
    m_nullcnt       number;
    m_avgclen       number;
    d_array         dbms_stats.datearray;
begin

-- step #1: delete histogram

    dbms_output.put_line ('Fixing ' || p_owner ||'.'|| p_table_name || '.' || p_column_name || '...');

    dbms_output.put_line ('Deleting histogram ...');

    DBMS_STATS.DELETE_COLUMN_STATS (
      ownname        => p_owner, 
      tabname        => p_table_name, 
      colname        => p_column_name, 
      col_stat_type  => 'HISTOGRAM'
    );


-- step #2: get column statistics

    dbms_output.put_line ('Getting column stats ...');

    dbms_stats.get_column_stats(
        ownname     => p_owner,
        tabname     => p_table_name,
        colname     => p_column_name,
        distcnt     => m_distcnt,
        density     => m_density,
        nullcnt     => m_nullcnt,
        srec        => srec,
        avgclen     => m_avgclen
    ); 

-- step #3: set column statistics

    srec.bkvals := null;
    d_array :=  dbms_stats.datearray( p_low_date, p_high_date);
    srec.epc := 2;
    dbms_stats.prepare_column_values(srec, d_array);
    m_distcnt := p_high_date - p_low_date;
    m_density := 1/m_distcnt;

    dbms_output.put_line ('Setting column stats ...');
 
    dbms_stats.set_column_stats(
        ownname     => p_owner,
        tabname     => p_table_name,
        colname     => p_column_name,
        distcnt     => m_distcnt,
        density     => m_density,
        nullcnt     => m_nullcnt,
        srec        => srec,
        avgclen     => m_avgclen
    ); 

    SET_DATE_RANGE_DESC (p_owner, p_table_name, p_column_name, p_low_date, p_high_date);
 
exception
    when others then
        raise;      -- should handle div/0
 
end;

procedure SET_DATE_RANGE_DESC (p_owner varchar2, p_table_name varchar2, p_column_name varchar2, p_low_date date, p_high_date date)
as
    srec            dbms_stats.statrec;
    m_distcnt       number;
    m_density       number;
    m_nullcnt       number;
    m_avgclen       number;
    d_array         dbms_stats.datearray;
    r_array			dbms_stats.rawarray;
    r1			raw(32);

    m_desc_column_name varchar2(100);
begin

    dbms_output.put_line('Setting low/high values for descending column ...');

    -- 1. Find descending colum name

    begin
	    select column_name
        into m_desc_column_name
        from (
	        select
                ind.index_name, ind.column_name, descend, exp.column_expression, 
			        qbo_stats_pkg.GET_DESC_COLUMN_NAME (ind.table_owner, ind.table_name, ind.index_name,  ind.column_position) as desc_col
	        from
		        (
		        select  /*+ no_merge */
			        *
		        from
			        all_ind_columns
		        where
		    	        table_owner = p_owner
		        and     table_name = p_table_name
		        and     index_owner = p_owner
		        )       ind,
		        (
		        select /*+ no_merge */
			        *
		        from
			        all_ind_expressions
		        where
		    	        table_owner = p_owner
		        and     table_name = p_table_name
		        and     index_owner = p_owner
		    )       exp
	    where
		    exp.table_owner = ind.table_owner
	    and     exp.table_name = ind.table_name
	    and     exp.index_owner = ind.index_owner
	    and     exp.index_name = ind.index_name
	    and     exp.column_position = ind.column_position
	    )
	    where desc_col like '%' || p_column_name || '%'
            and rownum = 1 ;
	exception
         when no_data_found then 
		dbms_output.put_line('No descending column for '||p_table_name ||'.'|| p_column_name);
		return; -- nothing to do
    end;
   
    dbms_output.put_line('col=' || p_column_name ||', desc col='|| m_desc_column_name);

    -- 2. Set low/high dates

	d_array := dbms_stats.datearray( p_low_date, p_high_date);

	--
	--	The raw array should have the date set
	--	in the opposite order to the date array
	--

	r_array := dbms_stats.rawarray(null,null);

	select 
		sys_op_descend(p_high_date)
	into
		r1
	from
		dual
	;

	r_array(1) := r1;

	select 
		sys_op_descend(p_low_date)
	into
		r1
	from
		dual
	;

	r_array(2) := r1;
	srec.epc := 2;
        m_distcnt := p_high_date - p_low_date;
        m_density := 1/m_distcnt;
	m_nullcnt := 0;	

	m_avgclen := 10;
        dbms_stats.prepare_column_values(srec, r_array);

	dbms_stats.set_column_stats(
		ownname		=> p_owner,
		tabname		=> p_table_name,
		colname		=> m_desc_column_name,
		distcnt		=> m_distcnt,
		density		=> m_density,
		nullcnt		=> m_nullcnt,
		srec		=> srec,
		avgclen		=> m_avgclen
	); 

end;


function GET_DESC_COLUMN_NAME (p_owner varchar2, p_table_name varchar2, p_index_name varchar2,  p_column_position number) RETURN varchar2
as
   m_col_expression long;
begin
    begin
        select /*+ no_merge */
            column_expression
        into m_col_expression
        from
            all_ind_expressions
        where
                table_owner = p_owner
        and     table_name = p_table_name
        and     index_owner = p_owner
        and     index_name = p_index_name
        and     column_position  = p_column_position;

        exception 
		when no_data_found then return null;
        end;
        return substr(m_col_expression,1,100);
end;

end;
/

show err

grant execute on QBO_STATS_PKG to public;


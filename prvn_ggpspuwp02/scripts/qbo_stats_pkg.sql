create or replace package QBO_STATS_PKG
as
  procedure SET_TXHEADERS_PREF;
  procedure SET_TXDETAILS_PREF;
  procedure SET_TX_DATE(p_low_date date DEFAULT to_date('2014-01-11', 'yyyy-mm-dd'), p_high_date date DEFAULT trunc(sysdate) + 90);
  procedure SET_DATE_RANGE (p_owner VARCHAR2, p_table_name VARCHAR2, p_column_name VARCHAR2, p_low_date date DEFAULT to_date('2014-01-11', 'yyyy-mm-dd'), p_high_date date DEFAULT trunc(sysdate) + 90);
end;
/
show err

create or replace package body QBO_STATS_PKG
as

-- sets table preferences for TxHeaders, should always contain current settings

procedure SET_TXHEADERS_PREF
is
begin
   DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXHEADERS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id for columns size 1 tx_date');
end;

-- sets table preferences for TxDetails, should always contain current settings

procedure SET_TXDETAILS_PREF
is
begin
  DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXDETAILS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id deposit_id for columns size 1 tx_date');
end;

-- sets low/high values for TX_DATE on TxHeaders + TxDetails
-- will be replaced by generic procedure soon

procedure SET_TX_DATE (p_low_date date, p_high_date date)
as
    srec            dbms_stats.statrec;
    m_distcnt       number;
    m_density       number;
    m_nullcnt       number;
    m_avgclen       number;
    d_array         dbms_stats.datearray;
begin

-- step #1: delete histogram

dbms_output.put_line ('Fixing TxHeaders ...');

dbms_output.put_line ('Deleting histogram ...');

 DBMS_STATS.DELETE_COLUMN_STATS (
    ownname        => 'QBO_DATA', 
    tabname        => 'TXHEADERS_1', 
    colname        => 'TX_DATE', 
    col_stat_type  => 'HISTOGRAM'
);

-- step #2: set table prefs to disable histogram on tx_date creation during stats collection

 DBMS_STATS.SET_TABLE_PREFS ('QBO_DATA', 'TXHEADERS_1', 'METHOD_OPT', 'for all columns size auto for columns invoice_id size 10 for columns tx_date size 1');

-- step #3: get column statistics

dbms_output.put_line ('Getting column stats ...');

    dbms_stats.get_column_stats(
        ownname     => 'QBO_DATA',
        tabname     => 'TXHEADERS_1',
        colname     => 'TX_DATE',
        distcnt     => m_distcnt,
        density     => m_density,
        nullcnt     => m_nullcnt,
        srec        => srec,
        avgclen     => m_avgclen
    ); 

-- step #4: set column statistics

    srec.bkvals := null;
    d_array :=  dbms_stats.datearray( p_low_date, p_high_date);
    srec.epc := 2;
    dbms_stats.prepare_column_values(srec, d_array);
    m_distcnt := p_high_date - p_low_date;
    m_density := 1/m_distcnt;

dbms_output.put_line ('Setting column stats ...');
 
    dbms_stats.set_column_stats(
        ownname     => 'QBO_DATA',
        tabname     => 'TXHEADERS_1',
        colname     => 'TX_DATE',
        distcnt     => m_distcnt,
        density     => m_density,
        nullcnt     => m_nullcnt,
        srec        => srec,
        avgclen     => m_avgclen
    ); 

dbms_output.put_line ('Fixing TxDetails ...');

dbms_output.put_line ('Deleting histogram ...');

 DBMS_STATS.DELETE_COLUMN_STATS (
    ownname        => 'QBO_DATA', 
    tabname        => 'TXDETAILS_1', 
    colname        => 'TX_DATE', 
    col_stat_type  => 'HISTOGRAM'
);

-- set table prefs to disable histogram creation during stats collection

 DBMS_STATS.SET_TABLE_PREFS ('QBO_DATA', 'TXDETAILS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id deposit_id for columns  size 1 tx_date ');

-- step #2: get column statistics

dbms_output.put_line ('Getting column stats ...');

    dbms_stats.get_column_stats(
        ownname     => 'QBO_DATA',
        tabname     => 'TXDETAILS_1',
        colname     => 'TX_DATE',
        distcnt     => m_distcnt,
        density     => m_density,
        nullcnt     => m_nullcnt,
        srec        => srec,
        avgclen     => m_avgclen
    ); 

-- step #3: set column statistics

    srec.bkvals := null;
    srec.epc := 2;
    dbms_stats.prepare_column_values(srec, d_array);
    m_distcnt := p_high_date - p_low_date;
    m_density := 1/m_distcnt;

dbms_output.put_line ('Setting column stats ...');
 
    dbms_stats.set_column_stats(
        ownname     => 'QBO_DATA',
        tabname     => 'TXDETAILS_1',
        colname     => 'TX_DATE',
        distcnt     => m_distcnt,
        density     => m_density,
        nullcnt     => m_nullcnt,
        srec        => srec,
        avgclen     => m_avgclen
    ); 
 
 
exception
    when others then
        raise;      -- should handle div/0
 
end;

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

 
exception
    when others then
        raise;      -- should handle div/0
 
end;

end;
/

show err

grant execute on QBO_STATS_PKG to public;

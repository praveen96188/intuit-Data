set feedback off;
set pagesize 0;
set verify off;
accept to_time prompt "Enter the end time: "
select min(SNAP_ID) from dba_hist_snapshot where to_char(END_INTERVAL_TIME,'YYMMDDHH24MI')='&to_time';
exit;


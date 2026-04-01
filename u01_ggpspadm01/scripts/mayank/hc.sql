spool hc.log
set trimspool on
set lines 300
set pages 3000
col event for a30 trunc
col machine for a15 trunc
col username for a22
col osuser for a11
col module for a15 trunc
col tablespace format a15
col CLIENT_IDENTIFIER for a20
col company_id for a20
col jvm for a20 trunc
col LOGONTIME for a12 trunc
col Status for a1 trunc
col sql_id for a15
column sid_serial for a13
set long 10000
SET TRIMSPOOL ON
set pages 0
select ' ' from dual;
select lpad(rpad(' SQL_ID/SQLTEXT with > 5 active sessions ',105,'-*'),150,'-*') from dual;
set pages 1000
select s.sql_id, count(1) count
  from v$session s
 where s.status='ACTIVE' and s.SQL_ID is not null
 group by s.sql_id
 having count(1) > 5
 order by 2 desc;
select sql_id, sql_fulltext
  from v$sql
 where sql_id in (select s.sql_id 
                    from v$session s 
                   where s.status='ACTIVE' 
                     and s.SQL_ID is not null 
                   group by s.sql_id
                    having count(1) > 5)
   and rownum < 2 ;
set pages 0
select lpad(rpad(' Cluster Wise JVMs/Total connections to RPTDB ',105,'-*'),150,'-*') from dual;
set pages 1000
select substr(username,1,instr(username,'_')-1) "Cluster",
       count(distinct username) "JVM's",count(*) "Total Connections"
  from v$session where username like 'QBO%' group by substr(username,1,instr(username,'_')-1) order by 1 ;
set pages 0 
select lpad(rpad(' Wait event with > 5 active sessions waiting on it ',105,'-*'),150,'-*') from dual; 
set pages 1000
select  event, count(1) count
  from gv$session
 where event not in (select name from V$EVENT_NAME where wait_class = 'Idle') 
 group by event
 having count(1) > 5; 
set pages 0
select lpad(rpad(' Sessions responsible for blocking multiple other sessions ',105,'-*'),150,'-*') from dual;  
set pages 1000
select blocking_session, count(1) count
  from gv$session
  where blocking_session is not null
  having count(1) > 10
 group by blocking_session;
set pages 0 
select lpad(rpad(' Company id with > 10 active sessions ',105,'-*'),150,'-*') from dual;  
set pages 1000
select client_identifier company_id, count(1)
  from gv$session
 where client_identifier is not null  
 group by client_identifier
 having count(1) > 10
 order by 2 desc; 
select client_identifier company_id, machine jvm, count(1) 
  from v$session
 where client_identifier in (select client_identifier
                               from v$session
                              where client_identifier is not null
                              group by client_identifier
                             having count(1) > 10)
 group by client_identifier, machine; 
set pages 0 
select lpad(rpad(' App/Web/DB machine having > 5 sessions ',105,'-*'),150,'-*') from dual;  
set pages 1000
select username, count(1)
  from gv$session 
 group by username
 having count(1) > 5
 order by 2 desc;
set pages 0 
select lpad(rpad(' All ACTIVE sessions details - wait event, blocking session... ',105,'-*'),150,'-*') from dual;   
set pages 1000
select username, 
       sql_id, 
       (sysdate-SQL_EXEC_START)*24*60*60 "RUNTIME(Secs)",  
       event, 
       seconds_in_wait,
       client_identifier,
       module,client_info,
       machine, 
       to_char(LOGON_TIME,'DD-MON HH24:MI') LOGONTIME, 
       blocking_session,
       lpad((sid || ',' || lpad(serial#,5)),11) sid_serial,
       substr(status,1,1) Status,
       s.sql_id, to_char(s.sql_exec_start, 'yyyy-mm-dd hh24:mi:ss') sql_exec_start,  s.prev_sql_id, to_char(s.prev_exec_start, 'yyyy-mm-dd hh24:mi:ss') prev_exec_start
  from gv$session s
  where (status = 'ACTIVE' or client_identifier is not null)
--  and sql_id is not null
    and username is not null
    and type != 'BACKGROUND'
order by 3 desc nulls last;
set pages 0
select lpad(rpad(' Top N redo generating sessions ',105,'-*'),150,'-*') from dual;
set pages 10000
set lines 300 
--col machine for a35
--col username for a10
column sid_serial for a13;
select b.username,
      machine,
      b.osuser,
      b.status,
      a.redo_mb,
      lpad((b.sid || ',' || lpad(b.serial#,5)),11) sid_serial 
from (select n.inst_id, sid,
            round(value/1024/1024/1024,2) redo_mb
        from gv$statname n, gv$sesstat s
        where n.inst_id=s.inst_id
              and n.name = 'redo size'
              and s.statistic# = n.statistic#
        order by value desc
    ) a,
    gv$session b
where b.inst_id=a.inst_id
  and a.sid = b.sid
and  rownum <= 10;
set pages 0
select lpad(rpad(' Redo Generation History ',105,'-*'),150,'-*') from dual;
set pages 10000
SELECT to_date(first_time) DAY,
to_char(sum(decode(to_char(first_time,'HH24'),'00',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "00",
to_char(sum(decode(to_char(first_time,'HH24'),'01',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "01",
to_char(sum(decode(to_char(first_time,'HH24'),'02',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "02",
to_char(sum(decode(to_char(first_time,'HH24'),'03',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "03",
to_char(sum(decode(to_char(first_time,'HH24'),'04',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "04",
to_char(sum(decode(to_char(first_time,'HH24'),'05',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "05",
to_char(sum(decode(to_char(first_time,'HH24'),'06',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "06",
to_char(sum(decode(to_char(first_time,'HH24'),'07',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "07",
to_char(sum(decode(to_char(first_time,'HH24'),'08',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "08",
to_char(sum(decode(to_char(first_time,'HH24'),'09',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "09",
to_char(sum(decode(to_char(first_time,'HH24'),'10',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "10",
to_char(sum(decode(to_char(first_time,'HH24'),'11',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "11",
to_char(sum(decode(to_char(first_time,'HH24'),'12',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "12",
to_char(sum(decode(to_char(first_time,'HH24'),'13',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "13",
to_char(sum(decode(to_char(first_time,'HH24'),'14',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "14",
to_char(sum(decode(to_char(first_time,'HH24'),'15',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "15",
to_char(sum(decode(to_char(first_time,'HH24'),'16',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "16",
to_char(sum(decode(to_char(first_time,'HH24'),'17',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "17",
to_char(sum(decode(to_char(first_time,'HH24'),'18',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "18",
to_char(sum(decode(to_char(first_time,'HH24'),'19',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "19",
to_char(sum(decode(to_char(first_time,'HH24'),'20',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "20",
to_char(sum(decode(to_char(first_time,'HH24'),'21',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "21",
to_char(sum(decode(to_char(first_time,'HH24'),'22',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "22",
to_char(sum(decode(to_char(first_time,'HH24'),'23',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "23"
from
v$archived_log
where dest_id=1 and to_date(first_time) > sysdate -30
group by
to_char(first_time,'YYYY-MON-DD'), to_date(first_time)
order by to_date(first_time);

set pages 0
select lpad(rpad(' Undo Utilization Status ',105,'-*'),150,'-*') from dual;
set pages 10000

select status,count(1) "Undo Blocks",sum(bytes)/1024/1024/1024 "Size(GB)" from dba_undo_extents group by status;

set pages 0
select lpad(rpad(' Sessions sorting data on disk ',105,'-*'),150,'-*') from dual;
set pages 10000
SELECT tablespace,
       s.username,
       s.sql_id sql_id,
       substr(s.module,1,15) module,
       client_identifier,
       sum((u.blocks * (select value from v$parameter where name='db_block_size')))/1024/1024/1024 "Size(GB)",
       lpad((s.sid || ',' || lpad(s.serial#,5)),11) sid_serial, 
       p.spid os_process
FROM gv$session s, gv$sort_usage u, gv$process p
WHERE s.saddr=u.session_addr
and p.addr = s.paddr
and u.extents > 10
and u.inst_id = s.inst_id
and TABLESPACE like '%TEMP%'
group by tablespace,
         s.username,
         s.sql_id,
         substr(s.module,1,15),
         client_identifier,
         lpad((s.sid || ',' || lpad(s.serial#,5)),11),
         p.spid;
set pages 0
select lpad(rpad(' Database/System Metrics ',105,'-*'),150,'-*') from dual;
set pages 10000
col "Metric" for a45
col "Metric Unit" for a30
select m.metric_name "Metric" ,
       round(m.value,2)    "Current Value",
       round(s.average,2) "Average Value",
       round(s.MAXVAL,2) "Max Value",
       m.metric_unit "Metric Unit"
 FROM v$sysmetric       m,
      v$sysmetric_summary s
WHERE m.group_id=s.group_id 
  AND m.METRIC_ID=s.METRIC_ID
  AND s.average > 0
  AND s.metric_name IN ('Redo Allocation Hit Ratio',        
            'Redo Generated Per Sec',            
                        'Logons Per Sec',                
                        'Logical Reads Per Sec',            
                    --    'Redo Writes Per Sec',            
                        'Host CPU Utilization (%)',         
                        'Network Traffic Volume Per Sec',        
                    --    'Enqueue Timeouts Per Sec',         
                    --    'Enqueue Waits Per Sec',            
                    --    'Enqueue Requests Per Sec',         
                        'Consistent Read Gets Per Sec',        
                        'Physical Read Total IO Requests Per Sec',    
                        'Physical Write Total IO Requests Per Sec', 
                        'Current Logons Count',            
                        'SQL Service Response Time',        
                        'Database Wait Time Ratio',         
                        'Database CPU Time Ratio',            
                        'Shared Pool Free %',            
                        'PGA Cache Hit %',                
                        'Session Limit %',                
                        'Database Time Per Sec',            
                    --    'Physical Write IO Requests Per Sec',    
                        'Physical Write Total Bytes Per Sec',    
                    --    'Physical Read IO Requests Per Sec',    
                        'Physical Read Total Bytes Per Sec',    
                        'Current OS Load',                
                        'Session Count',                
                        'Average Synchronous Single-Block Read Latency',
                        'I/O Megabytes per Second',         
                        'Average Active Sessions',                                
                        'Total PGA Allocated') 
 ORDER BY 1    ;

set pages 0
select lpad(rpad(' OS level stats ',105,'-*'),150,'-*') from dual;
set pages 1000
col value for 9999999999999.9
select stat_name, value from gv$osstat  where STAT_NAME like '%FREE%' or STAT_NAME like '%TIME%' or STAT_NAME like 'LOAD';

/*
set pages 0
select lpad(rpad(' Blocking sessions tree ',105,'-*'),150,'-*') from dual;
set pages 10000
column sess format a8 word_wrapped
column id1   format 99999999
column id2   format 99999999
column req       format 999
column type  format a4
column "Module" format a30 word_Wrapped
SELECT lpad('-->',DECODE(b.request,0,0,5),' ')||b.sid sess
        , b.id1
        , b.id2
        , b.lmode
       ,  b.request req, b.type --, wait_sessf(sid) "Module"
FROM V$LOCK b,v$session a
  WHERE b.id1 IN (SELECT id1 FROM gV$LOCK WHERE lmode = 0) and a.sid=b.sid -- and a.inst_id=b.inst_id
  ORDER BY b.id1,b.request;
*/
spool off;

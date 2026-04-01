col module format a4 trunc
break on qc_sid skip 1
col qc_sid format 99999
col INST_NAME format a9
col INST_ID format 99 heading I#
col SID format 99999 
col spid format 99999
col server_group format 99 heading SG
col server_set format 99 heading SS
col DEGREE format 999 heading DEG
col req_degree format 999 heading RDG
col event format a30 trunc
col username for a10 trunc
set lin 1000
set pages 1000
SELECT   px.inst_id, DECODE(px.qcinst_id, NULL, s.SID, px.qcsid) qc_sid,
         DECODE(
            px.qcinst_id,
            NULL, s.username,
            LOWER(SUBSTR(s.program, LENGTH(s.program)
                   - 4, 4) )
         ) username,
         DECODE(px.qcinst_id, NULL, 'QC', 'Slave') qc_slave,
         i.instance_name inst_name, px.SID, p.spid, px.server_group,
     px.server_set, px.DEGREE, px.req_degree, w.seconds_in_wait,w.event,s.module, /*s.action,*/ s.sql_id, trunc((SYSDATE-s.SQL_EXEC_START)*24*60) RunningSince
    FROM gv$session s, gv$px_session px, gv$process p, gv$session_wait w,
         gv$instance i
   WHERE px.inst_id = i.inst_id(+) AND px.SID = s.SID(+)
         AND px.serial# = s.serial# AND s.inst_id(+) = px.inst_id
         AND s.SID = w.SID(+) AND s.inst_id = w.inst_id(+) AND s.paddr = p.addr(+)
         AND s.inst_id = p.inst_id(+)
         AND(   px.qcsid
             || px.inst_id) IN(
               (SELECT    px2.qcsid
                       || px2.inst_id
                  FROM gv$px_session px2
                 WHERE px2.qcsid NOT IN(SELECT SID
                                          FROM v$session
                                         WHERE audsid = USERENV('sessionid') ) ) )
ORDER BY qc_sid, qc_slave,inst_name
/


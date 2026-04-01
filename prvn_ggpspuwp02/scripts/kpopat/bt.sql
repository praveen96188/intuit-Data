column sess format a8 word_wrapped
column id1   format 99999999
column id2   format 99999999
column req       format 999
column type  format a4
column "Module" format a30 word_Wrapped
SELECT b.inst_id,lpad('-->',DECODE(b.request,0,0,5),' ')||b.sid sess
       , b.id1
       , b.id2
       , b.lmode
      ,  b.inst_id
      ,  b.request req, b.type --, wait_sessf(sid) "Module"
FROM gV$LOCK b,gv$session a
 WHERE b.id1 IN (SELECT id1 FROM gV$LOCK WHERE lmode = 0) and a.sid=b.sid and a.inst_id=b.inst_id
 ORDER BY b.id1,b.request;


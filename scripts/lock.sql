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
 select listagg(WHICH_SCHEMA||'-'||CURRENT_VERSION, ',') within group (order by WHICH_SCHEMA) pcols1  from versions_1;


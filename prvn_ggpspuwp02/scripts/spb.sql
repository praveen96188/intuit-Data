set pages 0
set lines 1000
set head off
select * from dba_sql_plan_baselines 
where trunc(CREATED) = trunc(sysdate)
;
 set head on

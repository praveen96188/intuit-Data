--set pages 0
set lines 3000
set head on
select * from dba_sql_plan_baselines 
where trunc(CREATED) = trunc(sysdate)
;
 set head on

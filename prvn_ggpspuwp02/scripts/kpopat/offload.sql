set serverout on
declare
v_sql  varchar2(2048);
v_hr number;
v_min number;
v_company_cluster_id number;
BEGIN
--
SELECT SUBSTR (instance_name, 7, 2) AS company_cluster_id
  INTO v_company_cluster_id
  FROM dba_hist_database_instance
 WHERE ROWNUM = 1;
-- 
v_hr  := floor(((v_company_cluster_id-1)/10)/2);
v_min := substr(v_company_cluster_id,-1,1)*5;
--
v_sql := 'BEGIN '||
    'DBMS_SCHEDULER.CREATE_JOB(job_name    => ''INTUADMIN.OFFLOAD_DICT_IN_REDO'','||
                          'job_type        => ''PLSQL_BLOCK'','||
                          'JOB_ACTION      => ''DBMS_LOGMNR_D.BUILD( options => DBMS_LOGMNR_D.STORE_IN_REDO_LOGS);'','||
                          'start_date      => SYSTIMESTAMP,'||
                          'repeat_interval => ''freq=daily; byhour='||v_hr||'; byminute='||v_min||''','||
                          'end_date        => NULL,'||
                          'enabled         => TRUE,'||
                          'comments        => ''Regularly creates a dictionary to allow extract to go back in time'');
END;';
execute immediate v_sql;
--dbms_output.put_line(v_sql);
END;
/

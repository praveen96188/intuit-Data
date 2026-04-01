
DECLARE
  v_count          PLS_INTEGER := 0;
  v_iteration_cnt  PLS_INTEGER := 0;
  v_count_audit_log PLS_INTEGER := 0;
  v_start_time timestamp(6); 
  v_end_time timestamp(6);
  v_time_now timestamp(6);
  
BEGIN
	
	
	SELECT SYSTIMESTAMP INTO v_start_time FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Started at: ' || v_start_time);
	SELECT COUNT(*) INTO v_count_audit_log FROM PSP_BATCH_JOB_AUDIT_LOG;
	DBMS_OUTPUT.PUT_LINE ('Audit log count before purge: ' || v_count_audit_log);
	
	
	SELECT COUNT(*)
	  INTO v_count
	  FROM PSP_BATCH_JOB_AUDIT_LOG
	 WHERE Created_Date < SYSTIMESTAMP-5;
	
	DBMS_OUTPUT.PUT_LINE ('Total rows to delete : ' || v_count);
	 
	
	
	v_count := 0;
	
	LOOP   
	
    DELETE FROM PSP_BATCH_JOB_AUDIT_LOG
     WHERE Created_Date < SYSTIMESTAMP-5
		   AND ROWNUM < 20001;

    v_count := v_count + SQL%ROWCOUNT;		   
		
		EXIT WHEN SQL%ROWCOUNT = 0;
		
		COMMIT;
		
    v_iteration_cnt := v_iteration_cnt + 1;           
		
	END LOOP;
	
	COMMIT;
	
	DBMS_OUTPUT.PUT_LINE ('Total rows deleted : '      || v_count);
    DBMS_OUTPUT.PUT_LINE ('Total update iterations : ' || v_iteration_cnt);

	SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Shrink start time : '      || v_time_now);
	
	EXECUTE IMMEDIATE 'ALTER TABLE PSP_BATCH_JOB_AUDIT_LOG ENABLE ROW MOVEMENT';
	EXECUTE IMMEDIATE 'ALTER TABLE PSP_BATCH_JOB_AUDIT_LOG        SHRINK SPACE COMPACT';



	SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Shrink TIME : '      || v_time_now);

	EXECUTE IMMEDIATE 'ALTER TABLE PSP_BATCH_JOB_AUDIT_LOG        SHRINK SPACE';


	
	FOR rec IN (
	  SELECT distinct 'ALTER INDEX ' ||  a.index_name || ' COALESCE' sql_stmt
		  FROM USER_INDEXES a
	   WHERE table_name = Upper('PSP_BATCH_JOB_AUDIT_LOG')
 	) 
 	LOOP
 	    DBMS_OUTPUT.PUT_LINE(rec.sql_stmt);
 	    EXECUTE IMMEDIATE rec.sql_stmt;
    END LOOP;

SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
DBMS_OUTPUT.PUT_LINE ('Shrink end time : '      || v_time_now);
	
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;



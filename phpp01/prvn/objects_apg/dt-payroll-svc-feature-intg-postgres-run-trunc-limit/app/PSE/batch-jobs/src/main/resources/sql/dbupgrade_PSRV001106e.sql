
DECLARE
  v_count           PLS_INTEGER := 0;
  v_iteration_cnt   PLS_INTEGER := 0;
  v_event_log_count PLS_INTEGER := 0;
  v_time_now        timestamp(6);
  v_start_time 		timestamp(6);
  
BEGIN
	SELECT SYSTIMESTAMP INTO v_start_time FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Started at: ' || v_start_time);
	SELECT COUNT(*) INTO v_event_log_count FROM PSE_EVENT_LOG;
	DBMS_OUTPUT.PUT_LINE ('Event log count before purge: ' || v_event_log_count);
	
	
	
	SELECT COUNT(*)
	  INTO v_count
	  FROM PSE_EVENT_LOG
	 WHERE Z_INS_DTTM < SYSTIMESTAMP-30;
	
	DBMS_OUTPUT.PUT_LINE ('Total rows to update : ' || v_count);
	 
	
	
	v_count := 0;
	
	LOOP   
	
    DELETE FROM PSE_EVENT_LOG
     WHERE Z_INS_DTTM < SYSTIMESTAMP-30
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
	
	EXECUTE IMMEDIATE 'ALTER TABLE PSE_EVENT_LOG SHRINK SPACE COMPACT';
	
	SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Shrink1 time : '      || v_time_now);
	
	EXECUTE IMMEDIATE 'ALTER TABLE PSE_EVENT_LOG SHRINK SPACE';
	SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Shrink End time : '      || v_time_now);
	
	
    FOR rec IN (
	  SELECT distinct 'ALTER INDEX ' ||  a.index_name || ' COALESCE' sql_stmt
		  FROM USER_INDEXES a
	   WHERE table_name = Upper('PSE_EVENT_LOG')
 	) 
 	LOOP
 	    DBMS_OUTPUT.PUT_LINE(rec.sql_stmt);
 	    EXECUTE IMMEDIATE rec.sql_stmt;
    END LOOP;

	SELECT COUNT(*) INTO v_event_log_count FROM PSE_EVENT_LOG;
	DBMS_OUTPUT.PUT_LINE ('Event log count After purge: ' || v_event_log_count);
	
	SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('End time of this procedure : '      || v_time_now);
	
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;


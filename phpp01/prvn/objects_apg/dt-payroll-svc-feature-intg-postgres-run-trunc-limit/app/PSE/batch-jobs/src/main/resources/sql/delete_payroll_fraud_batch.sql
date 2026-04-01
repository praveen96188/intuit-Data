
DECLARE
  v_count          PLS_INTEGER := 0;

  v_time_now timestamp(6);
BEGIN


	SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Started at: ' || v_time_now);
	

DELETE FROM PSP_PAYROLL_FRAUD_BATCH 
where created_Date < sysdate-2
;
commit;

SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL;
	DBMS_OUTPUT.PUT_LINE ('Ended at: ' || v_time_now);


END;

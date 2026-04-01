
CREATE OR REPLACE PROCEDURE prc_payroll_fraudbatch_purge_dbupgrade_plsql_jobs_processor(
	v_count      OUT     INT,
	v_iteration_cnt OUT INT,
	v_count_audit_log OUT INT,
	v_count_delete OUT INT,
	v_start_time OUT TIMESTAMP WITH TIME ZONE,
	v_error OUT VARCHAR2
)

IS


BEGIN

v_count_delete := 0;
v_iteration_cnt := 0;
SELECT SYSTIMESTAMP INTO v_start_time FROM DUAL;

SELECT COUNT(*) INTO v_count_audit_log FROM PSP_BATCH_JOB_AUDIT_LOG;


SELECT COUNT(*)
INTO v_count
FROM PSP_BATCH_JOB_AUDIT_LOG
WHERE Created_Date < SYSTIMESTAMP-5;


LOOP

DELETE FROM PSP_BATCH_JOB_AUDIT_LOG
WHERE Created_Date < SYSTIMESTAMP-5
	AND ROWNUM < 20001;

v_count_delete := v_count_delete + SQL%ROWCOUNT;

EXIT WHEN SQL%ROWCOUNT = 0;

COMMIT;

v_iteration_cnt := v_iteration_cnt + 1;

END LOOP;

COMMIT;


EXCEPTION
  WHEN OTHERS THEN
    v_error := SQLERRM;
END prc_payroll_fraudbatch_purge_dbupgrade_plsql_jobs_processor;
/

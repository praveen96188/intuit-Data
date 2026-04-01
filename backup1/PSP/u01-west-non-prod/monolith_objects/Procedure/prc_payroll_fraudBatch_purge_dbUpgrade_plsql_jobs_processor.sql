CREATE OR REPLACE PROCEDURE prc_payroll_fraudbatch_purge_dbupgrade_plsql_jobs_processor(
	v_count      OUT     INT,
	v_iteration_cnt OUT INT,
	v_count_audit_log OUT INT,
	v_count_delete OUT INT,
	v_start_time OUT TIMESTAMP,
	v_error OUT VARCHAR
)
	LANGUAGE plpgsql AS
$$
DECLARE

	total_rows NUMERIC;

BEGIN

	v_count_delete := 0;
	v_iteration_cnt := 0;

	SELECT CURRENT_TIMESTAMP INTO v_start_time;


	SELECT COUNT(*) INTO v_count_audit_log FROM PSP_BATCH_JOB_AUDIT_LOG;


	SELECT COUNT(*)
				 INTO v_count
	FROM PSP_BATCH_JOB_AUDIT_LOG
	WHERE Created_Date < CURRENT_TIMESTAMP - INTERVAL '5 DAYS';


	LOOP


		DELETE FROM PSP_BATCH_JOB_AUDIT_LOG where BATCH_JOB_AUDIT_LOG_SEQ in (
			select BATCH_JOB_AUDIT_LOG_SEQ from  PSP_BATCH_JOB_AUDIT_LOG where Created_Date < CURRENT_TIMESTAMP - INTERVAL '5 DAYS' limit 20000);

		GET DIAGNOSTICS total_rows := ROW_COUNT;
		v_count_delete := v_count_delete + total_rows;

		EXIT WHEN total_rows = 0;

		v_iteration_cnt := v_iteration_cnt + 1;

	END LOOP;

EXCEPTION
	WHEN OTHERS THEN
		GET STACKED DIAGNOSTICS
			v_error := MESSAGE_TEXT;
END;

$$;
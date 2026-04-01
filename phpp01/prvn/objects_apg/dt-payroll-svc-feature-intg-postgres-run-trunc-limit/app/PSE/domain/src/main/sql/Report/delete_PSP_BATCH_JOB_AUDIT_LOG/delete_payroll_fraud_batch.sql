-- This data is "logging" data for the fraud batch job. We are seeing some performance impact with large volume.
SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     100
SET DEFINE       OFF

spool delete_payroll_fraud_batch.log

SELECT USER AS LOGIN_ID FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS START_TIME FROM DUAL;


PROMPT .
PROMPT Purge the PSP_PAYROLL_FRAUD_BATCH data except for the past 2 days ...

DELETE FROM PSPADM.PSP_PAYROLL_FRAUD_BATCH 
where created_Date < sysdate-2
;
commit;

PROMPT .
PROMPT Shrink PSP_PAYROLL_FRAUD_BATCH and generate new statistics ...

-- Done once in a separate script
--
-- ALTER TABLE PSPADM.PSP_PAYROLL_FRAUD_BATCH ENABLE ROW MOVEMENT;
--
-- Allows us to shrink the table after deletes

ALTER TABLE PSPADM.PSP_PAYROLL_FRAUD_BATCH SHRINK SPACE;
EXEC DBMS_STATS.gather_table_stats('PSPADM', 'PSP_PAYROLL_FRAUD_BATCH', estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE);

PROMPT .
PROMPT Shrink PK index on PSP_PAYROLL_FRAUD_BATCH and generate new statistics ...

ALTER INDEX PSPADM.SYS_C00108801 SHRINK SPACE COMPACT;
ALTER INDEX PSPADM.SYS_C00108801 SHRINK SPACE;
EXEC DBMS_STATS.gather_index_stats('PSPADM', 'SYS_C00108801', estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE);

PROMPT .
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS END_TIME FROM DUAL;
PROMPT Done.

spool off

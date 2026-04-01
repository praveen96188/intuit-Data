-- PSRV001063 
-- Removing the trigger from PSP_FINANCIAL_TRANSACTION to capture audit to current STATE.
-- This script will remove the corresponding data that was populated by the trigger.
-- Will also organize the table and index
--
-- CODE is fixed this script will FIX the bad data.
-- RUN AS PSPADM user

SET SERVEROUTPUT ON;

SPOOL dbupgrade_PSRV001063.log

PROMPT Total number of records in PSP_PROPERTY AUDIT

SELECT COUNT(*) from psp_property_audit ;

PROMPT count number of transactions to DELETE

SELECT COUNT(*) from psp_property_audit where class_name = 'FinancialTransaction';


-- delete 10k records at a time


BEGIN
	LOOP   
		DELETE FROM psp_property_audit
		 WHERE class_name = 'FinancialTransaction'
		 AND ROWNUM < 10000;
		
		IF sql%rowcount = 0 THEN
		   exit;
		END IF;
		COMMIT;
	END LOOP;
	COMMIT;
END;
/


PROMPT Total number of records in PSP_PROPERTY_AUDIT AFTER delete

SELECT COUNT(*) from psp_property_audit ;

PROMPT audits for financial transaction should be zero

SELECT COUNT(*) from psp_property_audit where class_name = 'FinancialTransaction';

PROMPT Reorg the table

ALTER TABLE psp_property_audit ENABLE ROW MOVEMENT;

ALTER TABLE psp_property_audit shrink space compact;

ALTER TABLE psp_property_audit shrink space;


PROMPT Rebuilt the indexes on the table

BEGIN
	FOR rec in (SELECT distinct 'ALTER INDEX ' ||  a.index_name || ' REBUILD' sql_stmt
		      FROM   user_indexes a
	             WHERE  table_name = Upper('psp_property_audit')
 	    ) 
 	LOOP
 	    DBMS_OUTPUT.PUT_LINE(rec.sql_stmt);
 	    EXECUTE IMMEDIATE rec.sql_stmt;
        END LOOP;
END;
/

PROMPT Creating the new index

CREATE INDEX PSP_PROPERTY_AUDIT_I1 ON PSP_PROPERTY_AUDIT (OBJECT_IDENTIFIER) LOGGING TABLESPACE PSP_IDX01 NOPARALLEL;

PROMPT Done. Gather STATS on PSP_Property_Audit table and indexes

SPOOL OFF; 
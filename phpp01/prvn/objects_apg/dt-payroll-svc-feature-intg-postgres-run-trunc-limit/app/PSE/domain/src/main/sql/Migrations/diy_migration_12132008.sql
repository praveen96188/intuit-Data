-- UPDATED: 12.07.2008 EMR
--
-- this script identifieds DD companies on the AS400 that will be 
-- targeted to be migrated to PSP on Saturday Dec 13 2008.  The 
-- following criteria will be met, as published by Andy Walker and
-- Use Case PSPDDMGU39: Conditions for Migration - DIY, authored by
-- Linda F.
--   a. DIY+DD
--   b. account is active, not MI or UP activation status, not on hold
--   c. Migrate any remaining terminated accounts
--   d. Migrate the remainder of active companies (>= 60k) who have submitted at least 1 payroll since 10.1.2007
--   e. Increase limit of paychecks per payroll from 100 to 150
--   f. Relax restriction on weekly payroll processors and allow all payroll frequencies to be migrated
--   g. Do not allow multi-EIN companies 
--   h. Do not allow pending payrolls
--   i. Do not allow companies to be migrated that have historically sent non-zero EE ID's 
--
-- Also this is a two step queuing process: first the as400 queue is built, then
-- the PSP migration queue is initialized.  We combined steps based upon our first
-- migration.
--
-- This must be run as DIYMIGADM user.

/* 
   NOTES:
     - handle 70k or end of list          DONE
     - dbms output every 5k               DONE
     - dbms output before terminated      DONE
     - look for REMOVE tag                DONE
     - remove requirement i.? 2 selects   NO
*/


SET serveroutput on
SET define       off

SPOOL diy_migration_12132008.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


prompt Now deleting the temp tables should they exist from a prior run
drop table Z_TEMP_MULTIEIN;
drop table Z_TEMP_IQACHSEL;
drop table Z_TEMP_PENDINGACH;
drop table Z_TEMP_EMPID_NOT_ZERO;
drop table Z_TEMP_IQCLIENT;


prompt Now loading Multi EIN temp table
CREATE TABLE Z_TEMP_MULTIEIN AS (SELECT CLI_FEIN FROM DIYMIGADM.DIY_IQCLIENT GROUP BY CLI_FEIN HAVING count(*) > 1);


prompt Now loading companies who submitted a payroll in the past year.
-- 12.07.2008 EMR   
-- changed the date from 10.1.2007 to 1 year prior
-- 12.01.2008 EMR
-- removed restriction about weekly payrolls.  all frequencies allowed.
-- however had to add a business rule to limit companies to just the past year of activity.
-- CREATE TABLE Z_TEMP_IQACHSEL AS (SELECT ACH_USERID FROM DIYMIGADM.DIY_IQACH4 WHERE ACH_DTPAYCHKS > 20081021 GROUP BY ACH_USERID HAVING count(*) > 1);
CREATE TABLE Z_TEMP_IQACHSEL AS (
  SELECT ACH_USERID,
         RECENT_PR_DATE
    FROM (
          SELECT ACH_USERID,
                 MAX(ACH_DTPAYCHKS) AS RECENT_PR_DATE
            FROM DIY_IQACH
           GROUP
              BY ACH_USERID
         )
   WHERE RECENT_PR_DATE > 20071212
);


prompt Now loading pending payrolls or more than 100 paychecks in ACH table
CREATE TABLE Z_TEMP_PENDINGACH AS (SELECT DISTINCT ACH_USERID FROM DIYMIGADM.DIY_IQACH4 WHERE ACH_DTPAYCHKS > 20071001 and (ACH_OFFLOADED = 0 or ach_paychk_count > 150));


prompt Now loading companies that have the QB bug where employee id equals 1 but should equal 0
-- 12.01.2008 EMR
-- removed      AND a.CLI_LAST_EMP      = 0 so that any customer who has a paycheck id greater than 0
-- is not migrated.  this now includes DIY DD with the QB bug and Assisted to DIY.
CREATE TABLE Z_TEMP_EMPID_NOT_ZERO AS (
  SELECT DISTINCT a.CLI_USERID AS CLI_USERID
    FROM DIY_IQCLIENT a,
         DIY_IQACHDD  b
   WHERE a.CLI_USERID        = b.ACHD_USERID
     AND a.CLI_TAXSERVICE    = 'N'
     AND a.CLI_STATUS        = 'A'
     AND a.CLI_ACTIV_STATUS <> 'MI'
     AND b.ACHD_EMPID        > 0
);


prompt Now loading IQCLIENT selection table
CREATE TABLE Z_TEMP_IQCLIENT AS (SELECT CLI_USERID, CLI_FEIN FROM DIYMIGADM.DIY_IQCLIENT WHERE         
 	CLI_STATUS = 'A' AND CLI_ACTIV_STATUS <> 'MI' and CLI_ACTIV_STATUS <> 'UP' and CLI_ON_HOLD = 'N' and CLI_TAXSERVICE = 'N');


prompt deleting all existing records in PSPMIGQUE.
DELETE FROM DIYMIGADM.DIY_MIG_QUEUE;
COMMIT;


prompt The total number of companies that match migration criteria is ...
SELECT COUNT(DISTINCT Z_TEMP_IQCLIENT.CLI_USERID) AS TOTAL_TO_MIGRATE
  FROM Z_TEMP_IQCLIENT, 
       Z_TEMP_IQACHSEL 
 WHERE Z_TEMP_IQCLIENT.CLI_USERID = Z_TEMP_IQACHSEL.ACH_USERID              
   AND Z_TEMP_IQCLIENT.CLI_FEIN   not in (SELECT Z_TEMP_MULTIEIN.CLI_FEIN         FROM Z_TEMP_MULTIEIN       WHERE Z_TEMP_MULTIEIN.CLI_FEIN         = Z_TEMP_IQCLIENT.CLI_FEIN)                                     
   AND Z_TEMP_IQCLIENT.CLI_USERID not in (SELECT Z_TEMP_PENDINGACH.ACH_USERID     FROM Z_TEMP_PENDINGACH     WHERE Z_TEMP_PENDINGACH.ACH_USERID     = Z_TEMP_IQCLIENT.CLI_USERID)
   AND Z_TEMP_IQCLIENT.CLI_USERID not in (SELECT Z_TEMP_EMPID_NOT_ZERO.CLI_USERID FROM Z_TEMP_EMPID_NOT_ZERO WHERE Z_TEMP_EMPID_NOT_ZERO.CLI_USERID = Z_TEMP_IQCLIENT.CLI_USERID);


prompt Now initializing the AS400 migration queue
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

DECLARE
  i                  number;
  nbrOfCompToMigrate number;

BEGIN
	
	 -- 12.07.2008 EMR
	 -- run the ALL remaining companies that qualify
   -- REMOVE, USE ONLY FOR TESTING	 
	 -- nbrOfCompToMigrate := 10;

   DBMS_OUTPUT.PUT_LINE ('Now loading any remaining termianted companies.');
   
   -- 12.07.2008 EMR   
   -- added migration of any additional terminated companies.
	 INSERT INTO DIYMIGADM.DIY_MIG_QUEUE 
	   SELECT CLI_USERID, 'R', 20081213, 0 
	     FROM DIYMIGADM.DIY_IQCLIENT
		  WHERE CLI_STATUS = 'TI' AND CLI_ACTIV_STATUS <> 'MI' and (CLI_TAXSERVICE = 'Y' or CLI_TAXSERVICE = 'N');
	 
	 DBMS_OUTPUT.PUT_LINE ('The number of terminated companies = ' || SQL%ROWCOUNT);

   
   DBMS_OUTPUT.PUT_LINE ('Now loading active companies into the AS400 migration queue.');
		  
	 i := 0;
	 
	 FOR rec IN (
	   SELECT DISTINCT Z_TEMP_IQCLIENT.CLI_USERID
	     FROM Z_TEMP_IQCLIENT, 
	          Z_TEMP_IQACHSEL 
	    WHERE Z_TEMP_IQCLIENT.CLI_USERID = Z_TEMP_IQACHSEL.ACH_USERID              
	      AND Z_TEMP_IQCLIENT.CLI_FEIN   not in (SELECT Z_TEMP_MULTIEIN.CLI_FEIN         FROM Z_TEMP_MULTIEIN       WHERE Z_TEMP_MULTIEIN.CLI_FEIN         = Z_TEMP_IQCLIENT.CLI_FEIN)                                     
			  AND Z_TEMP_IQCLIENT.CLI_USERID not in (SELECT Z_TEMP_PENDINGACH.ACH_USERID     FROM Z_TEMP_PENDINGACH     WHERE Z_TEMP_PENDINGACH.ACH_USERID     = Z_TEMP_IQCLIENT.CLI_USERID)
			  AND Z_TEMP_IQCLIENT.CLI_USERID not in (SELECT Z_TEMP_EMPID_NOT_ZERO.CLI_USERID FROM Z_TEMP_EMPID_NOT_ZERO WHERE Z_TEMP_EMPID_NOT_ZERO.CLI_USERID = Z_TEMP_IQCLIENT.CLI_USERID)
	 ) 
	 LOOP
	 
	 	 INSERT INTO DIYMIGADM.DIY_MIG_QUEUE values (rec.cli_userid, 'R', 20081213, 0);
	 	 
		 i := i + 1;
	
	   -- 12.07.2008 EMR
	   -- added incremental notification and commit per every 5k
     IF (MOD(i, 5000) = 0) THEN     
       DBMS_OUTPUT.PUT_LINE ('The following number of companies have been loaded - ' || i);
       COMMIT;
     END IF;
	 	 
	 	 -- 12.07.2008 EMR
	 	 -- the upper limit was removed.  bring over as many active companies as possible.
     -- REMOVE, USE ONLY FOR TESTING
		 -- EXIT WHEN i >= nbrOfCompToMigrate;
		 
	 END LOOP;

   DBMS_OUTPUT.PUT_LINE ('The AS400 migration queue has been loaded with total companies = ' || i);
	 COMMIT;	 
	 
END;
/

SHOW ERRORS

prompt Now deleting the temp tables
drop table Z_TEMP_MULTIEIN;
drop table Z_TEMP_IQACHSEL;
drop table Z_TEMP_PENDINGACH;
drop table Z_TEMP_EMPID_NOT_ZERO;
drop table Z_TEMP_IQCLIENT;




-- 11.18.2008 EMR   
-- COMBINED TWO STEPS INTO THIS SCRIPT

-- 12.07.2008 EMR
-- this last step using this index went from 11 min to 1 min, for example.
PROMPT Create index to tune the loading of the PSP migration queue.
CREATE INDEX XCAK_COMP_MIG_COMPSRCID ON COMPANY_MIGRATION
(SOURCE_DB_COMPANY_ID)
LOGGING
NOPARALLEL
COMPUTE STATISTICS;


prompt Now initializing the PSP migration queue
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

DECLARE 
  P_MIGRATION_PHASE_ID  VARCHAR2(10);
  P_RETURN_CD           NUMBER;
  P_RETURN_MSG          VARCHAR2(200);
BEGIN 
  P_MIGRATION_PHASE_ID := '4';
  P_RETURN_CD          := NULL;
  P_RETURN_MSG         := NULL;

  PK_DIYDDTOPSP_CONTROLLER.INITIALIZEMIGRATIONQUEUE (          
    P_MIGRATION_PHASE_ID, 
    P_RETURN_CD, 
    P_RETURN_MSG
  );

  DBMS_OUTPUT.PUT_LINE ('RC=' || P_RETURN_CD);
  DBMS_OUTPUT.PUT_LINE ('RM=' || P_RETURN_MSG);
  
  COMMIT; 

END;
/

SHOW ERRORS


SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

prompt Done Dude

spool off

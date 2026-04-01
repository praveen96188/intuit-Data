-- UPDATED: 11.18.2008 EMR
--
-- this script identifieds DD companies on the AS400 that will be 
-- targeted to be migrated to PSP on Friday Nov 21 2008.  The 
-- following criteria will be met, as published by Andy Walker and
-- Use Case PSPDDMGU39: Conditions for Migration - DIY, authored by
-- Linda F.
--   a. no terminated companies
--   b. DIY+DD
--   c. account is active, not MI or UP activation status, not on hold, not multi-EIN
--   d. have run a payroll in the last month, and are weekly
--   e. limit to 5000
--
-- Also this is a two step queuing process: first the as400 queue is built, then
-- the PSP migration queue is initialized.  We combined steps based upon our first
-- migration.
--
-- This must be run as DIYMIGADM user.

set serveroutput on
set define off

spool diy_migration_11212008.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;

prompt Now loading Multi EIN temp table
CREATE TABLE Z_TEMP_MULTIEIN AS (SELECT CLI_FEIN FROM DIYMIGADM.DIY_IQCLIENT GROUP BY CLI_FEIN HAVING count(*) > 1);

prompt Now loading IQACHSEL
-- 11.18.2008 EMR   
-- modified count and date to accomdate either weekly or bi-weekly relative to Fridays migration date.
CREATE TABLE Z_TEMP_IQACHSEL AS (SELECT ACH_USERID FROM DIYMIGADM.DIY_IQACH4 WHERE ACH_DTPAYCHKS > 20081021 GROUP BY ACH_USERID HAVING count(*) > 1);

prompt Now loading Pending or more than 10 pay checks in ACH table
CREATE TABLE Z_TEMP_PENDINGACH AS (SELECT DISTINCT ACH_USERID FROM DIYMIGADM.DIY_IQACH4 WHERE ACH_DTPAYCHKS > 20071001 and (ACH_OFFLOADED = 0 or ach_paychk_count > 10));

prompt Now loading IQCLIENT selection table
CREATE TABLE Z_TEMP_IQCLIENT AS (SELECT CLI_USERID, CLI_FEIN FROM DIYMIGADM.DIY_IQCLIENT WHERE         
 	CLI_STATUS = 'A' AND CLI_ACTIV_STATUS <> 'MI' and CLI_ACTIV_STATUS <> 'UP' and CLI_ON_HOLD = 'N' and CLI_TAXSERVICE = 'N');

prompt deleting all existing records in PSPMIGQUE
delete from DIYMIGADM.DIY_MIG_QUEUE;
commit;

prompt Now initializing the AS400 migration queue
DECLARE
  i                  number;
  nbrOfCompToMigrate number;
  --v_temp_date number;

BEGIN
	
	 -- 11.18.2008 EMR
	 -- modified this from the first migration of 500 to now 5000
	 nbrOfCompToMigrate := 5000;
   -- v_temp_date := TO_NUMBER(TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS'));

   -- 11.18.2008 EMR   
   -- REMOVED MIGRATION OF TERMINATED COMPANIES	
		  
	 i:=1;
	 
	 for rec in (
	   SELECT distinct Z_TEMP_IQCLIENT.CLI_USERID
	     FROM Z_TEMP_IQCLIENT, Z_TEMP_IQACHSEL 
	    WHERE Z_TEMP_IQCLIENT.CLI_USERID = Z_TEMP_IQACHSEL.ACH_USERID              
			  AND Z_TEMP_IQCLIENT.CLI_FEIN   not in (SELECT Z_TEMP_MULTIEIN.CLI_FEIN     FROM Z_TEMP_MULTIEIN   WHERE Z_TEMP_MULTIEIN.CLI_FEIN = Z_TEMP_IQCLIENT.CLI_FEIN)                                     
			  AND Z_TEMP_IQCLIENT.CLI_USERID not in (select Z_TEMP_PENDINGACH.ACH_USERID FROM Z_TEMP_PENDINGACH WHERE Z_TEMP_PENDINGACH.ACH_USERID = Z_TEMP_IQCLIENT.CLI_USERID)) 
	 loop
	 	 insert into DIYMIGADM.DIY_MIG_QUEUE values (rec.cli_userid, 'R', 20081121, 0);
		 exit when i >= nbrOfCompToMigrate;
		 i:= i + 1;
	 end loop;

   -- 11.18.2008 EMR   
   -- REMOVED THREE STATIC COMPANIES - STEVE AND TRACEY
	  	 
	 commit;	 
	 
END;
/

SHOW ERRORS

prompt Now deleting the temp tables
drop table Z_TEMP_MULTIEIN;
drop table Z_TEMP_IQACHSEL;
drop table Z_TEMP_PENDINGACH;
drop table Z_TEMP_IQCLIENT;


-- 11.18.2008 EMR   
-- COMBINED TWO STEPS INTO THIS SCRIPT
prompt Now initializing the PSP migration queue

DECLARE 
  P_MIGRATION_PHASE_ID  VARCHAR2(10);
  P_RETURN_CD           NUMBER;
  P_RETURN_MSG          VARCHAR2(200);
BEGIN 
  P_MIGRATION_PHASE_ID := '2';
  P_RETURN_CD := NULL;
  P_RETURN_MSG := NULL;

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


prompt Done Dude

spool off

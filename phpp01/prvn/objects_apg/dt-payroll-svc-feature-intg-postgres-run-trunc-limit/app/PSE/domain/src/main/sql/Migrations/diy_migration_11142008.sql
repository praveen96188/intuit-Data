set serveroutput on
set define off

spool diy_migration_11142008.log

prompt Now loading Multi EIN temp table
CREATE TABLE Z_TEMP_MULTIEIN AS (SELECT CLI_FEIN FROM DIYMIGADM.DIY_IQCLIENT GROUP BY CLI_FEIN HAVING count(*) > 1);

prompt Now loading IQACHSEL
CREATE TABLE Z_TEMP_IQACHSEL AS (SELECT ACH_USERID FROM DIYMIGADM.DIY_IQACH4 WHERE ACH_DTPAYCHKS > 20081001 GROUP BY ACH_USERID HAVING count(*) > 5);

prompt Now loading Pending or more than 10 pay checks in ACH table
CREATE TABLE Z_TEMP_PENDINGACH AS (SELECT DISTINCT ACH_USERID FROM DIYMIGADM.DIY_IQACH4 WHERE ACH_DTPAYCHKS > 20071001 and (ACH_OFFLOADED = 0 or ach_paychk_count > 10));

prompt Now loading IQCLIENT selection table
CREATE TABLE Z_TEMP_IQCLIENT AS (SELECT CLI_USERID, CLI_FEIN FROM DIYMIGADM.DIY_IQCLIENT WHERE         
 	CLI_STATUS = 'A' AND CLI_ACTIV_STATUS <> 'MI' and CLI_ACTIV_STATUS <> 'UP' and CLI_ON_HOLD = 'N' and CLI_TAXSERVICE = 'N');

prompt deleting all existing records in PSPMIGQUE
delete from DIYMIGADM.DIY_MIG_QUEUE;
commit;

prompt Now running PL SQL
declare
  i                  number;
  nbrOfCompToMigrate number;
  --v_temp_date number;

begin
	
	 nbrOfCompToMigrate := 500;
   -- v_temp_date := TO_NUMBER(TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS'));
	 INSERT INTO DIYMIGADM.DIY_MIG_QUEUE 
	   SELECT CLI_USERID, 'R', 20081114, 0 FROM DIYMIGADM.DIY_IQCLIENT
		  WHERE CLI_STATUS = 'TI' AND CLI_ACTIV_STATUS <> 'MI' and CLI_TAXSERVICE = 'N';
	 i:=1;
	 for rec in (
	 SELECT distinct Z_TEMP_IQCLIENT.CLI_USERID FROM Z_TEMP_IQCLIENT, Z_TEMP_IQACHSEL WHERE         
	 	 	 	Z_TEMP_IQCLIENT.CLI_USERID = Z_TEMP_IQACHSEL.ACH_USERID              
				and  Z_TEMP_IQCLIENT.CLI_FEIN not in (SELECT Z_TEMP_MULTIEIN.CLI_FEIN from Z_TEMP_MULTIEIN where Z_TEMP_MULTIEIN.CLI_FEIN  = Z_TEMP_IQCLIENT.CLI_FEIN)                                     
				and Z_TEMP_IQCLIENT.CLI_USERID not in (select Z_TEMP_PENDINGACH.ACH_USERID from Z_TEMP_PENDINGACH where Z_TEMP_PENDINGACH.ACH_USERID = Z_TEMP_IQCLIENT.CLI_USERID)) 
	 
	 loop
	 	 insert into DIYMIGADM.DIY_MIG_QUEUE values (rec.cli_userid, 'R', 20081114, 0);
		 exit when i >= nbrOfCompToMigrate;
		 i:= i + 1;
	 end loop;

	 begin
	 	 insert into DIYMIGADM.DIY_MIG_QUEUE values (652007780, 'R', 20081114, 0);
	   dbms_output.put_line('Inserted specific company for Steve: 652007780');		 
	 exception
	   when others then
	      dbms_output.put_line('652007780 already in PSPMIGQUE');		 
	 end;
	 
	 begin
	 	 insert into DIYMIGADM.DIY_MIG_QUEUE values (606020749, 'R', 20081114, 0);
	   dbms_output.put_line('Inserted specific company for Tracey: 606020749');		 
	 exception
	   when others then
	      dbms_output.put_line('606020749 already in PSPMIGQUE');		 
	 end;

	 begin
	 	 insert into DIYMIGADM.DIY_MIG_QUEUE values (632004976, 'R', 20081114, 0);
	   dbms_output.put_line('Inserted specific company for Tracey: 632004976');		 
	 exception
	   when others then
	      dbms_output.put_line('632004976 already in PSPMIGQUE');		 
	 end;
	  	 
	 commit;	 
	 
end;
/

prompt Now deleting the temp tables
drop table Z_TEMP_MULTIEIN;
drop table Z_TEMP_IQACHSEL;
drop table Z_TEMP_PENDINGACH;
drop table Z_TEMP_IQCLIENT;

prompt Done Dude

spool off

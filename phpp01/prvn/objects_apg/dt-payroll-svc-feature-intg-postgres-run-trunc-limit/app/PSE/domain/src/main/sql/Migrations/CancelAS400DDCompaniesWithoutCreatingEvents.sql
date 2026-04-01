-- This script is to cancel he DD companies on AS400.
-- All the DD companies which were migrated to PSP and active will be just cancelled on AS400.
-- All the other DD companies which were NOT migrated will also be cancelled on AS400 however, we take a backup so that we can
-- create evetns for those later.

-- This must be run as DIYMIGADM user.

SET serveroutput on
SET define       off

SPOOL cancelAS400DDCompanies.log

SELECT USER FROM DUAL;
prompt Start time
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;

CREATE TABLE Z_DIY_EVENTS_TO_BE_CREATED AS (
	   		 							 	  select 	cli_userid, cli_status, cli_activ_status, cli_on_hold, cli_agree_num, cli_taxservice   
											  from 		DIY_IQCLIENT 
											  where (cli_status = 'A' or cli_status = 'PP' or cli_status = 'PB' or cli_status = ' ' or cli_status = 'PE') and cli_activ_status <> 'MI' AND cli_activ_status <> 'UP' AND cli_taxservice = 'N'
									       );

prompt Number of events need to be created in the future
select count(*) from Z_DIY_EVENTS_TO_BE_CREATED; 											  

PROMPT State of statuses before update
SELECT CLI_TAXSERVICE, CLI_STATUS, CLI_ACTIV_STATUS, COUNT(*) FROM DIY_IQCLIENT 
 		 GROUP BY CLI_TAXSERVICE, CLI_STATUS, CLI_ACTIV_STATUS ORDER BY 1, 2, 3;


DECLARE
  alreadyCanMigCompanies number;
  beforeMigratedCompanies number;
  afterMigratedCompanies number;    
  beforeNonMigratedCompanies number;
  afterNonMigratedCompanies number;
  alreadyCanNonMigratedComp number;  
BEGIN

	 -- First for all active Migrated companies, just set the cli_status to 'CC'.
	 
	 select count(*) into alreadyCanMigCompanies from DIY_IQCLIENT where cli_activ_status = 'MI' AND cli_status = 'CC' AND cli_taxservice = 'N'; 							
	 
	 select count(*) into beforeMigratedCompanies from DIY_IQCLIENT where cli_activ_status = 'MI' AND cli_status = 'A' AND cli_taxservice = 'N'; 							
	 DBMS_OUTPUT.PUT_LINE('Number of companies to be Cancelled for Migrated Companies: ' || beforeMigratedCompanies);
	 	 
	 update DIY_IQCLIENT set cli_status = 'CC' where cli_activ_status = 'MI' AND cli_status = 'A' AND cli_taxservice = 'N';
	 
	 select count(*) into afterMigratedCompanies from DIY_IQCLIENT where cli_activ_status = 'MI' AND cli_status = 'CC' AND cli_taxservice = 'N';
	 DBMS_OUTPUT.PUT_LINE('Number of companies CANCELLED for Migrated Companies:: ' || (afterMigratedCompanies - alreadyCanMigCompanies) );
	 
	 if (afterMigratedCompanies != beforeMigratedCompanies + alreadyCanMigCompanies) then
	 	  Raise_Application_Error(-20051, 'Migrated Counts do NOT Match', false);
	 end if;	 	 

 	 select count(*) into alreadyCanNonMigratedComp from DIY_IQCLIENT where cli_status = 'CC' and cli_activ_status <> 'MI' AND cli_activ_status <> 'UP' AND cli_taxservice = 'N';
	 
 	 select count(*) into beforeNonMigratedCompanies from DIY_IQCLIENT where (cli_status = 'A' or cli_status = 'PP' or cli_status = 'PB' or cli_status = ' ' or cli_status = 'PE') and cli_activ_status <> 'MI' AND cli_activ_status <> 'UP' AND cli_taxservice = 'N';

	 DBMS_OUTPUT.PUT_LINE('Number of companies to be Cancelled for non-Migrated Companies: ' || beforeNonMigratedCompanies);

	 update DIY_IQCLIENT set cli_status = 'CC', cli_activ_status = 'CC' where (cli_status = 'A' or cli_status = 'PP' or cli_status = 'PB' or cli_status = ' ' or cli_status = 'PE') and cli_activ_status <> 'MI' AND cli_activ_status <> 'UP' AND cli_taxservice = 'N';

 	 select count(*) into afterNonMigratedCompanies from DIY_IQCLIENT where (cli_status = 'CC' and cli_activ_status <> 'MI' AND cli_activ_status <> 'UP' AND cli_taxservice = 'N');
	 DBMS_OUTPUT.PUT_LINE('Number of companies CANCELLED for non-Migrated Companies: ' || (afterNonMigratedCompanies - alreadyCanNonMigratedComp) );
	 
	 if (afterNonMigratedCompanies != alreadyCanNonMigratedComp + beforeNonMigratedCompanies) then
 	 	  Raise_Application_Error(-20051, 'Non-Migration Counts do NOT Match', false);
	 end if;
	 
	 commit;

	 
	EXCEPTION  
		   WHEN OTHERS THEN
		   		rollback;
		   		DBMS_OUTPUT.PUT_LINE('General Error' || sqlerrm);
END;
/

PROMPT State of statuses after update 
SELECT CLI_TAXSERVICE, CLI_STATUS, CLI_ACTIV_STATUS, COUNT(*) FROM DIY_IQCLIENT 
 		 GROUP BY CLI_TAXSERVICE, CLI_STATUS, CLI_ACTIV_STATUS ORDER BY 1, 2, 3;

rollback;
		 
prompt End time
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;

prompt DONE DUDE!!!

spool off

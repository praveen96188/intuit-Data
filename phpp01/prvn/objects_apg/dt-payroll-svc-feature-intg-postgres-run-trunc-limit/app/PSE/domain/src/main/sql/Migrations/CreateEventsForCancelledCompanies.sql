-- This script is to create events in AS400 so that it will flow to CRIS.

-- This must be run as DIYMIGADM user.

SET serveroutput on
SET define       off

SPOOL CreateEventsForCancelledCompanies.log

SELECT USER FROM DUAL;
prompt Start time
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;

CREATE TABLE Z_DIY_EVENTS_TO_BE_CREATED_NOW AS ( select * from Z_DIY_EVENTS_TO_BE_CREATED where MODIFIED_DATE is null and rownum <= 1000);
prompt Number of events to be created
select count(*) from Z_DIY_EVENTS_TO_BE_CREATED_NOW;

DECLARE
  psbtoken number(9);
  currTimestamp number(15);
  sysGuid varchar2(36);
  onHold varchar2(100);
  agreementNum varchar2(100);

  beginCreatedEvents number;
  afterCreatedEvents number;    
BEGIN

 	 select count(*) into beginCreatedEvents from Z_DIY_EVENTS_TO_BE_CREATED where MODIFIED_DATE is not null;
	 
	 FOR rec IN ( select * from Z_DIY_EVENTS_TO_BE_CREATED_NOW) 
 
	 LOOP

	 	 SELECT PSB_HIGH_TOKEN INTO psbtoken FROM DIY_PSCLIENT WHERE PSB_USERID = 980000002;
	 	 psbtoken := psbtoken + 2; 
	 	 
	 	 update DIY_IQCLIENT set cli_psb_token = psbtoken where cli_userid = rec.cli_userid;

		 select to_number(to_char(current_timestamp, 'YYYYMMDDHHMMSS')) into currTimestamp from dual;
		 
		 select sys_guid() into sysGuid from dual;
		 sysGuid := SUBSTR(sysGuid, 1, 8) || '-' || SUBSTR(sysGuid, 9, 4) || '-' || SUBSTR(sysGuid, 13, 4) || '-' || SUBSTR(sysGuid, 17, 4) || '-' || SUBSTR(sysGuid, 21);
		  
		 agreementNum := nvl(trim(rec.cli_agree_num),' ');
		 onHold := nvl(trim(rec.cli_on_hold),' ');
		 
		 begin
		    insert into DIY_IQEVENT ( EVE_USERID, EVE_TIMESTAMP, EVE_CODE, EVE_PSB_TOKEN, EVE_STATUS, EVE_ACTIV_STATUS, EVE_ON_HOLD, EVE_TRNUID, EVE_AGREE_NUM, EVE_REASONS)
		 		 values (
		 			 	    rec.cli_userid,
		 			 	    currTimestamp,   		   		
							'CANC', 
							psbtoken,							
							'CC',      		
							'CC', 	 
							onHold,
							sysGuid, 
							agreementNum,
							'SERVICE');
		commit;
		
		UPDATE Z_DIY_EVENTS_TO_BE_CREATED 
			   set MODIFIED_DATE = sysdate,
			       DB_PSBTOKEN = psbtoken
		where cli_userid = rec.cli_userid;
		commit;
		
		EXCEPTION  
				   WHEN OTHERS THEN
				   		rollback;
				         DBMS_OUTPUT.PUT_LINE('Error in Insert' || sqlerrm);
				   		 DBMS_OUTPUT.PUT_LINE('rec.cli_userid' || rec.cli_userid);
				   		 DBMS_OUTPUT.PUT_LINE('currTimestamp' || currTimestamp);
				   		 DBMS_OUTPUT.PUT_LINE('psbtoken' || psbtoken);
				   		 DBMS_OUTPUT.PUT_LINE('onHold' || onHold);
				   		 DBMS_OUTPUT.PUT_LINE('sysGuid' || sysGuid);
				   		 DBMS_OUTPUT.PUT_LINE('agreementNum' || agreementNum);
				  exit;
	    end;						 
				   		
		
	 END LOOP;
	 commit;

 	 select count(*) into afterCreatedEvents from Z_DIY_EVENTS_TO_BE_CREATED where MODIFIED_DATE is not null;

	 DBMS_OUTPUT.PUT_LINE('Number of events created: ' || (afterCreatedEvents - beginCreatedEvents));
	 
	EXCEPTION  
		   WHEN OTHERS THEN
		   		rollback;
		   		DBMS_OUTPUT.PUT_LINE('General Error' || sqlerrm);
END;
/

drop table Z_DIY_EVENTS_TO_BE_CREATED_NOW;
commit;

prompt End time
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;

prompt DONE DUDE!!!

spool off
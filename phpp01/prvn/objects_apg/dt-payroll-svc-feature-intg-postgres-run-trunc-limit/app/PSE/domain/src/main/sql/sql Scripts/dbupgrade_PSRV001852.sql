-- TEAMTRACK NUM: PSRV001852
-- CREATED  DATE: 08.18.2010
-- MODIFIED DATE: 08.18.2010
-- AUTHOR       : TT
-- MODIFIER     : 
--
-- PURPOSE: 
--   The purpose of this script is to do Price change to take effect 8/23/10 to increase DD price from 1.05 to 1.25 per.
--   add new offer FY11GF1.05:to Grandfather customers who purchased between 5/27 and 8/22 so that they get 6 more months of DD at 1.05 per
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF


SPOOL dbupgrade_PSRV001852.log

SELECT 'User = ' || USER
  FROM DUAL;
  
SELECT 'Start Time = ' || TO_CHAR (SYSDATE, 'MM.DD.YYYY HH24:MI')
  FROM DUAL;


SELECT offer_Cd, end_date FROM psp_offer
WHERE offer_cd in ('FY11GF1.05')
/




		Insert into PSP_OFFER (
		  OFFER_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, 
		  OFFER_CD,  NAME,  DESCRIPTION, 
		  IS_APPROVED,  DISCOUNT_TYPE, DISCOUNT_AMOUNT,  DISCOUNT_PERCENT, 
		  BEGIN_EVENT, END_EVENT, END_DATE, 
		  DURATION_DAYS, USAGES_ALLOWED, EFFECTIVE_DATE
		)
		Values (
		  '8e1b5b7f-a38d-c389-e040-10ac2743337b', 0, 'MANUAL_082310', SYSTIMESTAMP, 'MANUAL_082310', SYSTIMESTAMP,  -1, 
		  'FY11GF1.05','FY11GF1.05', 'Grandfather customers who purchased between 5/27 and 8/22 so that they get 6 more months of DD at 1.05', 
		  1, 'AlternatePrice',  0, 
		  0, 'RedemptionEvent', 'DateEvent', 
		  TO_TIMESTAMP('2/23/2011 12:00:01.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
		  0, 0, TO_TIMESTAMP('08/23/2010 7:00:01.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM')
		) ;


		Insert into PSP_OFFER_PRICE
		   (OFFER_PRICE_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, 
		    MODIFIED_DATE, REALM_ID, FEE_TYPE, ALTERNATE_UNIT_PRICE, OFFER_FK)
		 Values
		   ('8e1be0f9-9e35-cd09-e040-10ac27434104', 0, 'MANUAL_082310', TO_TIMESTAMP('7/29/2010 4:18:25.000000 PM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 'MANUAL_082310', 
		    TO_TIMESTAMP('8/2/2010 12:02:06.000000 PM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), -1, 'PerPaycheck', 1.05, '8e1b5b7f-a38d-c389-e040-10ac2743337b');

 
INSERT INTO PSP_OFFER_SVCCHG_ASSOC (OFFER_FK, OFFERING_SERVICE_CHARGE_FK) 
VALUES ( '8e1b5b7f-a38d-c389-e040-10ac2743337b', '70995ef3-0001-9373-e040-11ac3bda020f');

INSERT INTO PSP_OFFER_SVCCHG_ASSOC (OFFER_FK, OFFERING_SERVICE_CHARGE_FK) 
VALUES ('8e1b5b7f-a38d-c389-e040-10ac2743337b', '70995ef3-0002-9373-e040-11ac3bda020f');

INSERT INTO PSP_OFFER_SVCCHG_ASSOC (OFFER_FK, OFFERING_SERVICE_CHARGE_FK) 
VALUES ( '8e1b5b7f-a38d-c389-e040-10ac2743337b', '70995ef3-0007-9373-e040-11ac3bda020f');


COMMIT;

BEGIN

	FOR REC IN (SELECT company_seq FROM PSP_COMPANY a, psp_company_service b 
			WHERE 
				a.company_seq = b.company_fk
			   and b.service_fk='DirectDeposit' and b.status_cd='ActiveCurrent' 
			   and sign_up_date BETWEEN TO_TIMESTAMP('5/27/2010 07:00:00.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM')
					and TO_TIMESTAMP('8/23/2010 10:00:00.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM')
		   ) LOOP
	BEGIN
	
	
	Insert into PSP_COMPANY_OFFER
	   (COMPANY_OFFER_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, 
	    MODIFIED_DATE, REALM_ID, BEGIN_DATE, END_DATE, USAGES_REMAINING, 
	    OFFER_FK, COMPANY_FK)
	 Values
	   (fn_format_sysguid(sys_guid()), 0, 'MANUAL_082310', sysdate, 'MANUAL_082310', 
	    sysdate, -1, TO_TIMESTAMP('8/22/2010 7:00:00.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), TO_TIMESTAMP('02/23/2011 12:00:00.000000 PM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 0, 
	    '8e1b5b7f-a38d-c389-e040-10ac2743337b', rec.company_seq);

	END;
	END LOOP;
END;
/

commit;

UPDATE PSP_SVCCHGPRICE
SET price = 1.25
where price=1.05;

PROMPT should update 3 records..

commit;


 
 SELECT 'END Time = ' || TO_CHAR (SYSDATE, 'MM.DD.YYYY HH24:MI')
   FROM DUAL;
   
spool off;


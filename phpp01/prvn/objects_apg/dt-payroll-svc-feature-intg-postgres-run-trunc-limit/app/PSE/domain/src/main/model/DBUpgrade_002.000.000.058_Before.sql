DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_COMPANY_DATAFILE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_COMPANY_DATAFILE" CASCADE CONSTRAINTS';
	END IF;
	

END;
/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_COMPANY_DATAFILE 
(
	COMPANY_SEQ,
	OFFERING_FK	
) AS SELECT COMPANY_SEQ,OFFERING_FK FROM PSP_COMPANY
/


alter table PSP_COMPANY disable constraint PSP_COMPANY_FK1
/

alter table PSP_COMPANY_OFFER disable constraint PSP_COMPANY_OFFER_FK1
/

alter table psp_payroll_subtype disable constraint PSP_PAYROLL_SUBTYPE_FK1
/

delete from  PSP_OFFER_SVCCHG_ASSOC 
/

delete from  PSP_OFFER_PRICE
/

delete from PSP_OFFER
/

delete from  PSP_SVCCHGPRICE
/

delete from PSP_OFFERING_SVCCHG
/

delete from  PSP_OFFERING_SVCCHG_GRP
/

delete from  PSP_OFFERING
/








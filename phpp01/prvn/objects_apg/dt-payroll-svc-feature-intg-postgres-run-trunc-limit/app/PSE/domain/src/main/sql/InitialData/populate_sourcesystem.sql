DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_SOURCE_SYSTEM';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_SOURCE_SYSTEM" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_SOURCE_SYSTEM
(
	"SOURCE_SYSTEM_CD" VARCHAR2(255 CHAR) NOT NULL,
	"VERSION" NUMBER(19,0) NOT NULL, 
	"DESCRIPTION" VARCHAR2(4000 CHAR),  
	"NAME" VARCHAR2(4000 CHAR) 
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'QBOE', 'QuickBooks Online Edition', 'A WEB APPLICATION OFFERED BY INTUIT TO PERFORM VARIOUS ACCOUNTING AND PAYROLL FUNCTIONS THROUGH A WEB BROWSER.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'QBDT', 'QuickBooks Desktop', 'THE QUICKBOOKS DESKTOP THICK CLIENT.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'AS400', 'AS400', 'AS400.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'CRIS', 'CRIS', 'Siebel Platform used to manage agreements and calls from customers.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'EWS', 'Engineering Web Service.', 'Web interface that enables customers to select purchase, activate and manage (update) their payroll services through QB.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'PSP', 'Payroll Services Platform', 'Payroll Services Platform.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'GEMINI', 'Gemini', 'Gemini.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES ( 
'IOP', 'IOP', 'IOP.', 0)
/

INSERT INTO TEMP_PSP_SOURCE_SYSTEM (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION) VALUES (
'ADE', 'ADE', 'Agency Data Exchange.', 0)
/

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_SOURCE_SYSTEM
   (SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION)
SELECT 
    SOURCE_SYSTEM_CD, NAME, DESCRIPTION, VERSION
FROM 
   TEMP_PSP_SOURCE_SYSTEM tt 
WHERE 
   tt.SOURCE_SYSTEM_CD NOT IN (SELECT SOURCE_SYSTEM_CD FROM PSP_SOURCE_SYSTEM)

/

DELETE FROM PSP_SOURCE_SYSTEM
WHERE SOURCE_SYSTEM_CD NOT IN (SELECT SOURCE_SYSTEM_CD FROM TEMP_PSP_SOURCE_SYSTEM) 

/

UPDATE PSP_SOURCE_SYSTEM rt
SET (rt.NAME, rt.DESCRIPTION, rt.VERSION) =
(SELECT  tt.NAME, tt.DESCRIPTION, tt.VERSION
 FROM TEMP_PSP_SOURCE_SYSTEM tt WHERE tt.SOURCE_SYSTEM_CD = rt.SOURCE_SYSTEM_CD)
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_SOURCE_SYSTEM

/
COMMIT
 

 

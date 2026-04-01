DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_INTUIT_SHIPPER_INFO'; 

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_INTUIT_SHIPPER_INFO" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_INTUIT_SHIPPER_INFO
(
	PRIMARY KEY(INTUIT_SHIPPER_INFO_SEQ),
	"INTUIT_SHIPPER_INFO_SEQ" VARCHAR2(255 CHAR) NOT NULL,
	"VERSION" NUMBER(19,0) NOT NULL, 
	"CREATED_DATE" TIMESTAMP NOT NULL,  
	"MODIFIED_DATE" TIMESTAMP NOT NULL,  
	"REALM_FK" NUMBER(19,0) DEFAULT(-1) NOT NULL,   
	"SHIPPER_NAME" VARCHAR2(4000 CHAR),   
	"SHIPPER_ADDRESS_FK" VARCHAR2(255 CHAR)   
) 

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_INTUIT_SHIPPER_INFO (INTUIT_SHIPPER_INFO_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, SHIPPER_NAME, SHIPPER_ADDRESS_FK)
VALUES ('35cf8b0f-a09d-46d9-a474-60e3bb5e4481', 0, SYSDATE, SYSDATE, 'INTUIT PAYROLL SERVICES', 'a1544b12-7095-4e77-89c3-7253cdeb7ac6')
/

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_INTUIT_SHIPPER_INFO
   (INTUIT_SHIPPER_INFO_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, SHIPPER_NAME, SHIPPER_ADDRESS_FK )
SELECT 
    INTUIT_SHIPPER_INFO_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, SHIPPER_NAME, SHIPPER_ADDRESS_FK
FROM 
   TEMP_INTUIT_SHIPPER_INFO tt 
WHERE 
   tt.INTUIT_SHIPPER_INFO_SEQ NOT IN (SELECT INTUIT_SHIPPER_INFO_SEQ FROM PSP_INTUIT_SHIPPER_INFO)

/

DELETE FROM PSP_INTUIT_SHIPPER_INFO
WHERE INTUIT_SHIPPER_INFO_SEQ NOT IN (SELECT INTUIT_SHIPPER_INFO_SEQ FROM TEMP_INTUIT_SHIPPER_INFO) 
/

UPDATE PSP_INTUIT_SHIPPER_INFO rt
SET ( rt.CREATED_DATE, rt.MODIFIED_DATE, rt.SHIPPER_NAME, rt.SHIPPER_ADDRESS_FK, rt.VERSION) =
(SELECT  tt.CREATED_DATE, tt.MODIFIED_DATE, tt.SHIPPER_NAME, tt.SHIPPER_ADDRESS_FK, tt.VERSION
 FROM TEMP_INTUIT_SHIPPER_INFO tt WHERE tt.INTUIT_SHIPPER_INFO_SEQ = rt.INTUIT_SHIPPER_INFO_SEQ)
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_INTUIT_SHIPPER_INFO

/
COMMIT
 

 

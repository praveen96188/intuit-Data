DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_COLLECTION_STAGE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_COLLECTION_STAGE" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_COLLECTION_STAGE
(
  COLLECTION_STAGE_CODE         VARCHAR2(30 CHAR)                NOT NULL,
  VERSION      		      NUMBER(19)                       NOT NULL,
  REALM_ID                    NUMBER(19)                       DEFAULT -1                    NOT NULL,
  DESCRIPTION  VARCHAR2(100 CHAR)
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_COLLECTION_STAGE (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) VALUES ( 
'FirstCollectionAttempt',0,'1st Collection Attempt')
/
INSERT INTO TEMP_COLLECTION_STAGE (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) VALUES ( 
'SecondCollectionAttempt',0,'2nd Collection Attempt')
/
INSERT INTO TEMP_COLLECTION_STAGE (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) VALUES ( 
'TerminationExpected',0,'Termination Expected')
/

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_COLLECTION_STAGE
   (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) 
SELECT 
   COLLECTION_STAGE_CODE, VERSION, DESCRIPTION
FROM 
   TEMP_COLLECTION_STAGE tcs
WHERE 
   tcs.COLLECTION_STAGE_CODE NOT IN (SELECT COLLECTION_STAGE_CODE FROM PSP_COLLECTION_STAGE)

/

DELETE FROM PSP_COLLECTION_STAGE cs
WHERE cs.COLLECTION_STAGE_CODE NOT IN (SELECT COLLECTION_STAGE_CODE FROM TEMP_COLLECTION_STAGE) 

/

UPDATE PSP_COLLECTION_STAGE cs
SET  (cs.COLLECTION_STAGE_CODE, cs.VERSION, cs.DESCRIPTION) =
(SELECT  tcs.COLLECTION_STAGE_CODE, tcs.VERSION, tcs.DESCRIPTION
 FROM TEMP_COLLECTION_STAGE tcs WHERE tcs.COLLECTION_STAGE_CODE =cs.COLLECTION_STAGE_CODE)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_COLLECTION_STAGE

/
COMMIT
 

 

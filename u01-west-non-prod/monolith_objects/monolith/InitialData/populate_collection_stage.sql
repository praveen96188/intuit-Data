DROP TABLE IF EXISTS TEMP_COLLECTION_STAGE CASCADE;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TEMP_COLLECTION_STAGE (LIKE PSP_COLLECTION_STAGE INCLUDING ALL) ;
--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_COLLECTION_STAGE (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) VALUES ( 
'FirstCollectionAttempt',0,'1st Collection Attempt')
;
INSERT INTO TEMP_COLLECTION_STAGE (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) VALUES ( 
'SecondCollectionAttempt',0,'2nd Collection Attempt')
;
INSERT INTO TEMP_COLLECTION_STAGE (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) VALUES ( 
'TerminationExpected',0,'Termination Expected')
;

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

;

DELETE FROM PSP_COLLECTION_STAGE cs
WHERE cs.COLLECTION_STAGE_CODE NOT IN (SELECT COLLECTION_STAGE_CODE FROM TEMP_COLLECTION_STAGE) 

;

UPDATE PSP_COLLECTION_STAGE cs
SET  (COLLECTION_STAGE_CODE, VERSION, DESCRIPTION) =
(SELECT  tcs.COLLECTION_STAGE_CODE, tcs.VERSION, tcs.DESCRIPTION
 FROM TEMP_COLLECTION_STAGE tcs WHERE tcs.COLLECTION_STAGE_CODE =cs.COLLECTION_STAGE_CODE)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_COLLECTION_STAGE

;
COMMIT
 

 

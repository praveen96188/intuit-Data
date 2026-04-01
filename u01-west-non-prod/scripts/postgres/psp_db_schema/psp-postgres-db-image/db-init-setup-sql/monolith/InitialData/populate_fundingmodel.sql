DROP TABLE IF EXISTS TEMP_PSP_FUNDING_MODEL CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_FUNDING_MODEL (LIKE PSP_FUNDING_MODEL INCLUDING ALL)  

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_FUNDING_MODEL ( FUNDING_MODEL_CD, VERSION, NAME, DESCRIPTION, NUMBER_OF_FUNDING_DAYS) VALUES
( '2D', 0, '2 Day Funding Model', 'Funds required for DD TXNs associated with a payroll are debited from ERs acct 2 days prior to date they are to be deposited in EEs acct.', 2)
; 
INSERT INTO TEMP_PSP_FUNDING_MODEL ( FUNDING_MODEL_CD, VERSION, NAME, DESCRIPTION, NUMBER_OF_FUNDING_DAYS) VALUES ( 
'5D', 0, '5 Day Funding Model', 'Funds required for DD TXNs associated with a payroll are debited from ERs acct 5 days prior to date they are to be deposited in EEs acct.'
, 5)
;
INSERT INTO TEMP_PSP_FUNDING_MODEL ( FUNDING_MODEL_CD, VERSION, NAME, DESCRIPTION, NUMBER_OF_FUNDING_DAYS) VALUES (
'1D', 0, 'Next Day Funding Model', 'Next Day Funding Model', 1)
;
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_FUNDING_MODEL
   (FUNDING_MODEL_CD, VERSION, NAME, DESCRIPTION, NUMBER_OF_FUNDING_DAYS )
SELECT 
    FUNDING_MODEL_CD, VERSION, NAME, DESCRIPTION, NUMBER_OF_FUNDING_DAYS
FROM 
   TEMP_PSP_FUNDING_MODEL tt 
WHERE 
   tt.FUNDING_MODEL_CD NOT IN (SELECT FUNDING_MODEL_CD FROM PSP_FUNDING_MODEL)

;

DELETE FROM PSP_FUNDING_MODEL
WHERE FUNDING_MODEL_CD NOT IN (SELECT FUNDING_MODEL_CD FROM TEMP_PSP_FUNDING_MODEL) 

;

UPDATE PSP_FUNDING_MODEL rt
SET (VERSION, NAME, DESCRIPTION, NUMBER_OF_FUNDING_DAYS) =
(SELECT tt.VERSION, tt.NAME, tt.DESCRIPTION, tt.NUMBER_OF_FUNDING_DAYS
 FROM TEMP_PSP_FUNDING_MODEL tt WHERE tt.FUNDING_MODEL_CD = rt.FUNDING_MODEL_CD)
;



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_FUNDING_MODEL

;
COMMIT
 
 

 

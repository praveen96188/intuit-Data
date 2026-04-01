DROP TABLE IF EXISTS TEMP_INTUIT_SHIPPER_INFO CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_INTUIT_SHIPPER_INFO (LIKE PSP_INTUIT_SHIPPER_INFO INCLUDING ALL)

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_INTUIT_SHIPPER_INFO (INTUIT_SHIPPER_INFO_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, SHIPPER_NAME, SHIPPER_ADDRESS_FK)
VALUES ('35cf8b0f-a09d-46d9-a474-60e3bb5e4481', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INTUIT PAYROLL SERVICES', 'a1544b12-7095-4e77-89c3-7253cdeb7ac6')
;

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

;

DELETE FROM PSP_INTUIT_SHIPPER_INFO
WHERE INTUIT_SHIPPER_INFO_SEQ NOT IN (SELECT INTUIT_SHIPPER_INFO_SEQ FROM TEMP_INTUIT_SHIPPER_INFO) 
;

UPDATE PSP_INTUIT_SHIPPER_INFO rt
SET ( CREATED_DATE, MODIFIED_DATE, SHIPPER_NAME, SHIPPER_ADDRESS_FK, VERSION) =
(SELECT  tt.CREATED_DATE, tt.MODIFIED_DATE, tt.SHIPPER_NAME, tt.SHIPPER_ADDRESS_FK, tt.VERSION
 FROM TEMP_INTUIT_SHIPPER_INFO tt WHERE tt.INTUIT_SHIPPER_INFO_SEQ = rt.INTUIT_SHIPPER_INFO_SEQ)
;



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_INTUIT_SHIPPER_INFO

;
COMMIT
 

 

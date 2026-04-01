DROP TABLE IF EXISTS TEMP_FEE CASCADE;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TEMP_FEE (LIKE PSP_FEE INCLUDING ALL) ;
--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_FEE (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK) VALUES (
'5f748cc5-87e4-4c44-a591-13bbf6127ba1', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 50, 'AMOUNT OF THE FEE CHARGED TO A COMPANY FOR A REVERSAL OF A PAYROLL', 'ReverseFee', 'Reversal Fee Amount', 'QBOE', 'EmployerFeeDebit')
;
INSERT INTO TEMP_FEE  (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK) VALUES ( 
'804b5891-4fa2-4e88-a697-bb21fd11b09c', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100, 'AMOUNT OF THE FEE CHARGED TO A COMPANY FOR A NON-SUFFICENT FUNDS (NSF)', 'NSFFee', 'NSF Fee Amount', 'QBOE', 'EmployerFeeDebit')
;
INSERT INTO TEMP_FEE  (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK) VALUES (
'804b5891-4fa2-4e88-a797-bb21fd11b09c', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 'AMOUNT OF THE FEE CHARGED TO A COMPANY FOR A NON-SUFFICENT FUNDS (NSF) WHILE BILLING FOR FEES', 'FeeOnlyNSFFee', 'Fee Only NSF Fee Amount', 'QBOE', 'EmployerFeeDebit')
;

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_FEE
   (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK)
SELECT 
    FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK
FROM 
   TEMP_FEE f 
WHERE 
   f.FEE_CD NOT IN (SELECT FEE_CD FROM PSP_FEE)

;

DELETE FROM PSP_FEE
WHERE FEE_CD NOT IN (SELECT FEE_CD FROM TEMP_FEE) 

;

UPDATE PSP_FEE rf
SET  (VERSION, CREATED_DATE, MODIFIED_DATE, REALM_ID, AMOUNT, DESCRIPTION, FEE_CD, NAME, SOURCE_SYSTEM_FK, TRANSACTION_TYPE_FK) =
(SELECT  tf.VERSION, tf.CREATED_DATE, tf.MODIFIED_DATE, tf.REALM_ID, tf.AMOUNT,tf.DESCRIPTION, tf.FEE_CD, tf.NAME, tf.SOURCE_SYSTEM_FK, tf.TRANSACTION_TYPE_FK
 FROM TEMP_FEE tf WHERE tf.FEE_CD = rf.FEE_CD)
;



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_FEE

;
COMMIT
 

 

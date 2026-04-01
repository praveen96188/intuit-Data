DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_FEE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_FEE" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_FEE
(
  FEE_SEQ            VARCHAR2(255 CHAR)       NOT NULL,
  VERSION              NUMBER(19)               NOT NULL,
  CREATED_DATE         TIMESTAMP(6)             NOT NULL,
  MODIFIED_DATE        TIMESTAMP(6)             NOT NULL,
  REALM_ID             NUMBER(19)               DEFAULT -1                    NOT NULL,
  AMOUNT               NUMBER(19),
  DESCRIPTION          VARCHAR2(4000 CHAR),
  FEE_CD               VARCHAR2(4000 CHAR),
  NAME                 VARCHAR2(4000 CHAR),
  SOURCE_SYSTEM_FK     VARCHAR2(255 CHAR)       NOT NULL,
  TRANSACTION_TYPE_FK  VARCHAR2(255 CHAR)       NOT NULL
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_FEE (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK) VALUES ( 
'5f748cc5-87e4-4c44-a591-13bbf6127ba1', 0, SYSDATE, SYSDATE, 50, 'AMOUNT OF THE FEE CHARGED TO A COMPANY FOR A REVERSAL OF A PAYROLL', 'ReverseFee', 'Reversal Fee Amount', 'QBOE', 'EmployerFeeDebit')
/
INSERT INTO TEMP_FEE  (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK) VALUES ( 
'804b5891-4fa2-4e88-a697-bb21fd11b09c', 0, SYSDATE, SYSDATE, 100, 'AMOUNT OF THE FEE CHARGED TO A COMPANY FOR A NON-SUFFICENT FUNDS (NSF)', 'NSFFee', 'NSF Fee Amount', 'QBOE', 'EmployerFeeDebit')
/
INSERT INTO TEMP_FEE  (FEE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, AMOUNT,DESCRIPTION, FEE_CD, NAME,SOURCE_SYSTEM_FK,TRANSACTION_TYPE_FK) VALUES (
'804b5891-4fa2-4e88-a797-bb21fd11b09c', 0, SYSDATE, SYSDATE, 25, 'AMOUNT OF THE FEE CHARGED TO A COMPANY FOR A NON-SUFFICENT FUNDS (NSF) WHILE BILLING FOR FEES', 'FeeOnlyNSFFee', 'Fee Only NSF Fee Amount', 'QBOE', 'EmployerFeeDebit')
/

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

/

DELETE FROM PSP_FEE
WHERE FEE_CD NOT IN (SELECT FEE_CD FROM TEMP_FEE) 

/

UPDATE PSP_FEE rf
SET  (rf.VERSION, rf.CREATED_DATE, rf.MODIFIED_DATE, rf.REALM_ID, rf.AMOUNT,rf.DESCRIPTION, rf.FEE_CD, rf.NAME, rf.SOURCE_SYSTEM_FK, rf.TRANSACTION_TYPE_FK) =
(SELECT  tf.VERSION, tf.CREATED_DATE, tf.MODIFIED_DATE, tf.REALM_ID, tf.AMOUNT,tf.DESCRIPTION, tf.FEE_CD, tf.NAME, tf.SOURCE_SYSTEM_FK, tf.TRANSACTION_TYPE_FK
 FROM TEMP_FEE tf WHERE tf.FEE_CD = rf.FEE_CD)
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_FEE

/
COMMIT
 

 

DROP TABLE IF EXISTS TEMP_PSP_TRANSACTION_STATE CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_TRANSACTION_STATE (LIKE PSP_TRANSACTION_STATE INCLUDING ALL)

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_TRANSACTION_STATE ( TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'Created', 'Pending', 'An event has occured where a transaction is created.  The first state of a transaction.  May or many not have some affect on the ledger accounts.'
,0)
;
INSERT INTO TEMP_PSP_TRANSACTION_STATE ( TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'Executed', 'Executed', 'A txn that finished its intended task.  ach txns, passed info to mme. non-ach txns, no ach to mme, but memo entries made ledger to reflect non-ach activity.).'
,0)
;
INSERT INTO TEMP_PSP_TRANSACTION_STATE ( TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'Cancelled', 'Canceled', 'A transaction that was created but not gonig to be completed.  Caused by the Intuit rep or the customer.  Must occur before the transaction turns to executed.'
,0)
;
INSERT INTO TEMP_PSP_TRANSACTION_STATE ( TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'Returned', 'Returned', 'An ACH txn that went to the bank was either rejected or returned by the bank.'
,0)
;
INSERT INTO TEMP_PSP_TRANSACTION_STATE ( TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'Completed', 'Completed', 'Only for EE DD Reversal Debit.  Indicates sufficient time has passed to ensure the EE Debit will not be returned.',0)
;
INSERT INTO TEMP_PSP_TRANSACTION_STATE ( TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION  ) VALUES ( 
'Voided', 'Voided', 'For non-ACH transactions only, reverse all ledger entries entered by the created and executed states.',0)
;

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_TRANSACTION_STATE
   (TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION )
SELECT 
    TRANSACTION_STATE_CD, NAME, DESCRIPTION, VERSION
FROM 
   TEMP_PSP_TRANSACTION_STATE tt 
WHERE 
   tt.TRANSACTION_STATE_CD NOT IN (SELECT TRANSACTION_STATE_CD FROM PSP_TRANSACTION_STATE)

;

DELETE FROM PSP_TRANSACTION_STATE
WHERE TRANSACTION_STATE_CD NOT IN (SELECT TRANSACTION_STATE_CD FROM TEMP_PSP_TRANSACTION_STATE) 

;


UPDATE PSP_TRANSACTION_STATE rt
SET ( NAME, DESCRIPTION, VERSION) =
(SELECT   tt.NAME, tt.DESCRIPTION, tt.VERSION
 FROM TEMP_PSP_TRANSACTION_STATE tt WHERE tt.TRANSACTION_STATE_CD = rt.TRANSACTION_STATE_CD)
;



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_TRANSACTION_STATE

;
COMMIT
 

 

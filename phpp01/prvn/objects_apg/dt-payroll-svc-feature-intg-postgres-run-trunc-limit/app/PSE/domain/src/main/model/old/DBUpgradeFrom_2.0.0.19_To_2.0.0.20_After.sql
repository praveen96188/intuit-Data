--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_2.0.0.19_To_2.0.0.20.sql
--
-- Developers can hand code logic here for data migration purposes
-- Script to update the Paycheck status based on the associated financial transaction state
DECLARE
v_financial_trans_state VARCHAR2(255); 
BEGIN
 
   FOR REC IN (
                  SELECT * FROM PSP_PAYCHECK WHERE STATUS IS NULL
              )
   LOOP
       UPDATE PSP_PAYCHECK SET STATUS = 'Inactive' WHERE PAYCHECK_SEQ = REC.PAYCHECK_SEQ;
       
       FOR PAYCHECK_SPLIT_REC IN (
                                   SELECT * FROM PSP_PAYCHECK_SPLIT WHERE PAYCHECK_FK = REC.PAYCHECK_SEQ
                                )
       LOOP
                SELECT CURRENT_TRANSACTION_STATE_FK INTO v_financial_trans_state FROM PSP_FINANCIAL_TRANSACTION WHERE PAYCHECK_SPLIT_FK = PAYCHECK_SPLIT_REC.PAYCHECK_SPLIT_SEQ;  
          
                IF (v_financial_trans_state <> 'Cancelled') THEN
                    UPDATE PSP_PAYCHECK SET STATUS = 'Active' WHERE PAYCHECK_SEQ = REC.PAYCHECK_SEQ;
                    EXIT;  
                END IF;   
           
       END LOOP;
              
   END LOOP;
END;

/
commit;
SHOW ERRORS;

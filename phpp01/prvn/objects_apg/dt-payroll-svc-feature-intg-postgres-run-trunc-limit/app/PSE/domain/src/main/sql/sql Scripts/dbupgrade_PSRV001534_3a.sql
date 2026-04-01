
DECLARE
v_financial_trans_state VARCHAR2(255); 
BEGIN
 
   FOR REC IN ( SELECT PAYCHECK_SPLIT_FK FROM psp_financial_Transaction
   					WHERE paycheck_split_fk is not null
    					 AND  CURRENT_TRANSACTION_STATE_FK='Cancelled'
    	       )
   LOOP
       UPDATE PSP_PAYCHECK SET STATUS = 'Inactive' 
       WHERE PAYCHECK_SEQ = 
       		( SELECT PAYCHECK_FK FROM PSP_PAYCHECK_SPLIT WHERE PAYCHECK_SPLIT_SEQ = REC.PAYCHECK_SPLIT_FK)
       	;
   END LOOP;
   	
   COMMIT;

END;
/

 
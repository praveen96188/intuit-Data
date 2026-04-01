CREATE TABLE  TEMP1_PAYCHECK
AS SELECT PAYCHECK_FK, SUM(PAYCHECK_SPLIT_AMOUNT) net_amt
    FROM PSP_PAYCHECK_SPLIT a, PSP_PAYCHECK b
    WHERE 
        paycheck_fk=paycheck_seq
       and net_amount is null
       and 
        EXISTS (
            SELECT 'T' FROM PSP_FINANCIAL_TRANSACTION
                WHERE PAYCHECK_SPLIT_FK = a.PAYCHECK_SPLIT_SEQ                  
               )      
    GROUP BY PAYCHECK_FK 
/


DECLARE

TYPE varchar_array    	IS TABLE OF VARCHAR2(255) INDEX BY BINARY_INTEGER;
TYPE num_array   	IS TABLE OF NUMBER(15,2)  INDEX BY BINARY_INTEGER;

dml_errors EXCEPTION;
PRAGMA EXCEPTION_INIT(dml_errors, -24381);

CURSOR c_upd IS
SELECT paycheck_fk, net_amt FROM TEMP1_PAYCHECK;

v_net_amount num_array;
v_paycheck_Seq varchar_array;


BEGIN



  OPEN c_upd;

    LOOP
        FETCH c_upd 
        BULK COLLECT INTO	
            v_paycheck_seq, 
            v_net_amount
        LIMIT 5000;
            

    	IF v_paycheck_seq.COUNT = 0 THEN
            EXIT;
        END IF;


	BEGIN        

            FORALL i IN v_paycheck_seq.FIRST..v_paycheck_seq.LAST SAVE EXCEPTIONS
                UPDATE  PSP_PAYCHECK
                    SET NET_AMOUNT = v_net_amount(i)
                WHERE paycheck_SEQ = v_paycheck_Seq(i)    ;

            EXCEPTION
            WHEN dml_errors THEN
                 FOR j IN 1..SQL%BULK_EXCEPTIONS.COUNT
                 LOOP
                     DBMS_OUTPUT.PUT_LINE('ERROR '|| SQLERRM(-SQL%BULK_EXCEPTIONS(j).ERROR_CODE));                 
                 END LOOP;
        END;
        
        COMMIT;
        
    END LOOP;
    
  CLOSE c_upd;
  
END;  
/
  
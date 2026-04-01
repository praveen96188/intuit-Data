-- PSRV001017
-- The offload code that inserts the transaction_effective_date from “Executed” state is truncating the date without converting it to PST !! 
-- for example the transaction offloaded on 11/25/08 5:05 PST that is 11/26/08 0:05 GMT and truncating it will get to 11/26/08 incorrect value, is should be truncating PST date not GMT date.
-- CODE is fixed this script will FIX the bad data.
-- RUN AS PSPADM user

SET SERVEROUTPUT ON;

SPOOL dbupgrade_PSRV001017.log

PROMPT count number of transactions to update

SELECT COUNT(*), min(transaction_State_eff_date), max(transaction_State_eff_date) FROM psp_financial_Trans_State   
 WHERE creator_id ='ACHTransactionOffloadBatchJob'
   AND Transaction_State_fk ='Executed';

PROMPT count number of ledger entries to delete
   
SELECT COUNT(*) FROM psp_ledger_balance
 WHERE trunc(created_Date) >= trunc(to_Date('11/07/08','mm/dd/yy') );

PROMPT DROP backup table ofr rel 1.1 if exists

DROP TABLE z_financial_Trans_State_rel11;

DROP TABLE z_ledger_balance_rel11;   

PROMPT CREATE backup table for rel 1.1

CREATE TABLE z_financial_Trans_State_rel11 as
select * from psp_financial_Trans_State;

CREATE TABLE z_ledger_balance_rel11 as
select * from psp_ledger_balance;


PROMPT Updating psp_financial_trans_state, chaning state_eff_date to previous day
-- Shall we update modified user id to 'DBA Script' ?? don't know the impact on DDM
-- 

UPDATE psp_financial_Trans_State
   SET transaction_state_eff_date = transaction_state_Eff_Date - 1  
 WHERE creator_id ='ACHTransactionOffloadBatchJob'
   AND Transaction_State_fk ='Executed';
COMMIT;   

PROMPT After update

SELECT COUNT(*), min(transaction_State_eff_date), max(transaction_State_eff_date) FROM psp_financial_Trans_State   
 WHERE creator_id ='ACHTransactionOffloadBatchJob'
   AND Transaction_State_fk ='Executed';
   
PROMPT report on count by day   BEFORE
select trunc(balance_date), count(*) from psp_ledger_balance
WHERE trunc(created_Date) >= trunc(to_Date('11/07/08','mm/dd/yy') )
group by trunc(balance_date)
order by 1;

PROMPT delete 10k records at a time from psp_ledger_balance
PROMPT using created data instead of ledger balance date, because its okay to use it 

BEGIN
	LOOP   
		DELETE FROM psp_ledger_balance
		WHERE trunc(created_Date) >= trunc(to_Date('11/07/08','mm/dd/yy') )
		 AND ROWNUM < 10000;
		
		IF sql%rowcount = 0 THEN
		   exit;
		END IF;
		COMMIT;
	END LOOP;
	COMMIT;
END;
/

PROMPT count number of ledger entries, should be zero

SELECT COUNT(*) FROM psp_ledger_balance
 WHERE trunc(created_Date) >= trunc(to_Date('11/07/08','mm/dd/yy') );

PROMPT recalulate ledger balance
-- Make sure this does not overlap dates, meaning should finish running prior to midnight, else we will have to do something ...
 
EXEC PRC_UPDATE_LEDGER_BALANCE; 


PROMPT report on count by day  AFTER
select trunc(balance_date), count(*) from psp_ledger_balance
WHERE trunc(created_Date) >= trunc(to_Date('11/07/08','mm/dd/yy') )
group by trunc(balance_date)
order by 1;

PROMPT count number of ledger entry recreated...

SELECT COUNT(*) FROM psp_ledger_balance
 WHERE trunc(created_Date) >= trunc(to_Date('11/07/08','mm/dd/yy') );
 
PROMPT Done

SPOOL OFF; 
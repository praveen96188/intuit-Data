\timing 
vacuum (FREEZE, analyze, verbose) pspadm.psp_ledger_balance_p5;  
SELECT pg_sleep(60);
vacuum (FREEZE, analyze, verbose) pspadm.psp_pstub_pay_item_p10; 
SELECT pg_sleep(60);
vacuum (FREEZE, analyze, verbose) pspadm.psp_pstub_pay_item_p9;  
SELECT pg_sleep(60);

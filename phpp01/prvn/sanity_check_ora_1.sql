spool sanity_check_ora.log

select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION a;
select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_FINANCIAL_TRANSACTION a;
select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_PAYCHECK a;
select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_PAYCHECK_SPLIT a;
select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_PSTUB_PAY_ITEM a;
select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_TAX a;


spool off ;

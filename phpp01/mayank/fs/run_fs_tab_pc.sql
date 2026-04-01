set termout off timing on
spool run_fs_tab_pc
select /*+ FULL(PAYCHECK_BA12022) parallel(PAYCHECK_BA12022 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_BA12022)PAYCHECK_BA12022 ;
select /*+ FULL(PAYCHECK_BA22022) parallel(PAYCHECK_BA22022 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_BA22022)PAYCHECK_BA22022 ;
select /*+ FULL(PAYCHECK_BA12020) parallel(PAYCHECK_BA12020 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_BA12020)PAYCHECK_BA12020 ;
select /*+ FULL(PAYCHECK_BA22020) parallel(PAYCHECK_BA22020 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_BA22020)PAYCHECK_BA22020 ;
select /*+ FULL(PAYCHECK_BA12021) parallel(PAYCHECK_BA12021 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_BA12021)PAYCHECK_BA12021 ;
select /*+ FULL(PAYCHECK_BA22021) parallel(PAYCHECK_BA22021 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_BA22021)PAYCHECK_BA22021 ;
select /*+ FULL(PAYCHECK_9999) parallel(PAYCHECK_9999 ,4) */count(*) from pspadm.PSP_PAYCHECK partition (PAYCHECK_9999)PAYCHECK_9999 ;
spool off

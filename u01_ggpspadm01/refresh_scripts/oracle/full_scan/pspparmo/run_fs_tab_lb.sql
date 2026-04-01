set termout off timing on
spool run_fs_tab_lb
select /*+ FULL(LDGRBAL_BA12023) parallel(LDGRBAL_BA12023 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA12023)LDGRBAL_BA12023 ;
select /*+ FULL(LDGRBAL_BA22023) parallel(LDGRBAL_BA22023 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA22023)LDGRBAL_BA22023 ;
select /*+ FULL(LDGRBAL_BA12022) parallel(LDGRBAL_BA12022 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA12022)LDGRBAL_BA12022 ;
select /*+ FULL(LDGRBAL_BA22022) parallel(LDGRBAL_BA22022 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA22022)LDGRBAL_BA22022 ;
select /*+ FULL(LDGRBAL_BA12020) parallel(LDGRBAL_BA12020 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA12020)LDGRBAL_BA12020 ;
select /*+ FULL(LDGRBAL_BA22020) parallel(LDGRBAL_BA22020 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA22020)LDGRBAL_BA22020 ;
select /*+ FULL(LDGRBAL_BA12021) parallel(LDGRBAL_BA12021 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA12021)LDGRBAL_BA12021 ;
select /*+ FULL(LDGRBAL_BA22021) parallel(LDGRBAL_BA22021 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_BA22021)LDGRBAL_BA22021 ;
select /*+ FULL(LDGRBAL_9999) parallel(LDGRBAL_9999 ,4) */count(*) from pspadm.PSP_LEDGER_BALANCE partition (LDGRBAL_9999)LDGRBAL_9999 ;
spool off

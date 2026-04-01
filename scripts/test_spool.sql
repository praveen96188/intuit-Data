col spoolname new_value spoolname
select 'txdetails_histograms_'||SYS_CONTEXT('USERENV', 'DB_NAME')||'_'||to_char(sysdate,'YYMONDD_HH24.MI.SS')||'.log' 
spool '&spoolname'
select sysdate from dual;
spool off

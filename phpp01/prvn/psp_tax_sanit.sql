set lines 300 echo on timing on echo on feedback on trimspool on
spool psp_tax

select /*+ parallel(a,8) */ count(*) from PSPADM.PSP_TAX a;

spool off;

set echo on feedback on timing on
set serveroutput on
spool stats_psp_pstub_pay_item_fk2_fk_indx
-- collect index stats
exec dbms_stats.gather_index_stats('PSPADM','PSP_PSTUB_PAY_ITEM_FK2');
spool off


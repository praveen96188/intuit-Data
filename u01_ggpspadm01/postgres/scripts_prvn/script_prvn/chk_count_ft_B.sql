set echo on feedback on timing on
spool chk_count_ft_B
select /*+parallel(16) */count(1) from pspadm.PSP_FINANCIAL_TRANSACTION;
exit;

set time on timing on echo on feedback on
SELECT  /*+ PARALLEL(8) */
  modified_date - created_date, SOURCE_SYSTEM_TRANSMISSION_SEQ
FROM IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION sst
where created_date > sysdate - 365
and modified_date - created_date > interval '2' day;

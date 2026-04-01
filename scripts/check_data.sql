set echo on feedback on
-- Bank Holidays
select count(*) from PSPADM.PSP_BANK_HOLIDAY;
select trunc(BANK_HOLIDAY_DATE, 'Y'),count(*) from PSPADM.PSP_BANK_HOLIDAY group by  trunc(BANK_HOLIDAY_DATE, 'Y') order by 1 desc;
--Offload Batch
select max(OFFLOAD_DATE), count(*) from pspadm.PSP_OFFLOAD_BATCH;
select trunc(OFFLOAD_DATE, 'Y'), count(*) from pspadm.PSP_OFFLOAD_BATCH group by  trunc(OFFLOAD_DATE, 'Y') order by 1 desc;
-- NACHA File
select max(OFFLOAD_DATE), count(*) from pspadm.psp_nachafile NCF, PSPADM.PSP_OFFLOAD_BATCH ob where ncf.OFFLOAD_BATCH_FK = ob.OFFLOAD_BATCH_SEQ;
select trunc(OFFLOAD_DATE, 'Y'), count(*) from pspadm.psp_nachafile NCF, PSPADM.PSP_OFFLOAD_BATCH ob where ncf.OFFLOAD_BATCH_FK = ob.OFFLOAD_BATCH_SEQ
group by trunc(OFFLOAD_DATE, 'Y') ORDER BY 1 desc;


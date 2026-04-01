select to_char(CREATED_DATE,'MON-YY'),count(1) from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where COMPANY_ID='&comp_id' group by to_char(CREATED_DATE,'MON-YY') order by to_char(CREATED_DATE,'MON-YY');

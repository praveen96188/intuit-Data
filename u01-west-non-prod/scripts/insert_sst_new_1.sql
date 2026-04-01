set echo on timing on 
spool insert_sst_new_1
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2007','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2008','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2008','dd-mm-yyyy') and CREATED_DATE<to_date('01-02-2009','dd-mm-yyyy');
commit;

spool off

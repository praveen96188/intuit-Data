set echo on timing on 
spool insert_sst_new
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-02-2009','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2013','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2013','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2015','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2015','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2016','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2016','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2017','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2017','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2018','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2018','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2019','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2019','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2020','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2020','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2021','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2021','dd-mm-yyyy') and CREATED_DATE<to_date('01-01-2023','dd-mm-yyyy');
commit;
insert into IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION_NEW select * from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION where CREATED_DATE>=to_date('01-01-2023','dd-mm-yyyy') ;
commit;

spool off

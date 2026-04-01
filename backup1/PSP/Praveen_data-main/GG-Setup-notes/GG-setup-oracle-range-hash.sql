Setup and configure replication from Range RDS to Hash RDS
Setup Environment 
Configure GG Replication from Range Partitioning to Hash Partitioning
Create New GG Hub to setup replication for Hash RDS
./create_gg_hub_ha_new.sh psp prod psphpp01 us-west-2 vpc-2 no m4.10xlarge 1000


Create  and start Pump to send trails to HASH GG hub
Login to current production GG hub and run below commands

GGSCI (ggpspuwp01.sbg-psp-prod.a.intuit.com) 2> obey ./diroby/create_ppsphp01.oby
 
GGSCI (ggpspuwp01.sbg-psp-prod.a.intuit.com) 3> add extract ppsphp01, EXTTRAILSOURCE ./dirdat/pspuwp01/tr
 
EXTRACT added.
 
 
GGSCI (ggpspuwp01.sbg-psp-prod.a.intuit.com) 4> add rmttrail ./dirdat/pspuwp01/tr, extract ppsphp01, MEGABYTES 500
 
RMTTRAIL added.
 
GGSCI (ggpspuwp01.sbg-psp-prod.a.intuit.com) 5> info all
 
Program     Status      Group       Lag at Chkpt  Time Since Chkpt
 
MANAGER     RUNNING                                          
EXTRACT     RUNNING     EPSPWP01    00:00:02      00:00:00   
EXTRACT     RUNNING     EUIPWP01    00:00:03      00:00:03   
EXTRACT     ABENDED     PPSPEP01    00:00:00      00:04:40   
EXTRACT     RUNNING     PPSPEP05    00:00:03      00:00:08   
EXTRACT     STOPPED     PPSPHP01    00:00:00      00:00:05   
EXTRACT     RUNNING     PUIPWP01    00:00:03      00:00:09


Restore latest snapshot of Range RDS via console.
--get SCN
SQL> sho parameter db_name
 
NAME             TYPE    VALUE
------------------------------------ ----------- ------------------------------
db_name              string  PITHASH1
SQL> select name from v$database;
 
NAME
---------
PITHASH1
 
SQL> select incarnation#, resetlogs_change#, resetlogs_time, prior_resetlogs_change#, prior_resetlogs_time, status, resetlogs_id, prior_incarnation# from v$database_incarnation;
 
                  INCARNATION#              RESETLOGS_CHANGE# RESETLOGS        PRIOR_RESETLOGS_CHANGE# PRIOR_RES STATUS                    RESETLOGS_ID             PRIOR_INCARNATION#
------------------------------ ------------------------------ --------- ------------------------------ --------- ------- ------------------------------ ------------------------------
                             1                  8388795358333 14-AUG-20                  8388795357611 14-AUG-20 PARENT                      1048411759                              0
                             2                  8397003383021 07-APR-23                  8388795358333 14-AUG-20 PARENT                      1133501356                              1
                             3                  8397003385952 07-APR-23                  8397003383021 07-APR-23 CURRENT                     1133501971                              2
 
SQL> select current_scn from v$database;
 
                   CURRENT_SCN
------------------------------
                 8397003404258


Increase RDS storage for PITHASH1 to have ~14TB free for export backup & Take full database export in above restored RDS

Create New HASH RDS
./create_rds.sh prodx clusterdb psphpp02 us-west-2 vpc-2
 
--Change Master user password
aws --profile sbg-psp-prod --region us-west-2 rds modify-db-instance --db-instance-identifier psphpp02 --master-user-password xxxxxxx
Initiai Sync
Export data from PITHASH1 db
create directories in PITHASH1 db & Hash rds
EXEC rdsadmin.rdsadmin_util.create_directory(p_directory_name => 'DATA_PUMP_DIR1');
EXEC rdsadmin.rdsadmin_util.create_directory(p_directory_name => 'DATA_PUMP_DIR2');
EXEC rdsadmin.rdsadmin_util.create_directory(p_directory_name => 'DATA_PUMP_DIR3');
select * from DBA_DIRECTORIES where DIRECTORY_NAME like 'DATA_PUMP_DIR%';
create database link in PITHASH1 db
drop database link to_target;
create database link to_target connect to intuadmin identified by "xxxxxx" using '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST= psphpp02.sbg-psp-prod.a.intuit.com)(PORT=1521))(CONNECT_DATA=(SID=psphpp02)))';
select INSTANCE_NAME, HOST_NAME from v$instance@to_target;
export metadata

nohup expdp userid=intuadmin/xxxxx@pithash1 directory=DATA_PUMP_DIR dumpfile=exp_pspadm_metadata.dmp SCHEMAS=PSPADM content=METADATA_ONLY logfile=DATA_PUMP_DIR:exp_pspadm_metadata.log 2>&1 &
export data
nohup expdp userid=intuadmin/"xxxx"@pithash1 dumpfile=DATA_PUMP_DIR:exp_data_%U.dmp,DATA_PUMP_DIR1:exp_data1_%U.dmp,DATA_PUMP_DIR2:exp_data2_%U.dmp,DATA_PUMP_DIR3:exp_data3_%U.dmp parallel=48 schemas=PSPADM filesize=150G content=data_only logfile=DATA_PUMP_DIR:exp_data.log 2>&1 &
 
--export dump size : 13.893 TB (~14 TB) (Time Taken: ~11 hrs)
Copy dump files to target Hash db
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p1.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p2.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p3.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p4.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p5.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p6.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p7.sql &
nohup ./pithash1_run_on_target.sh copy_dump_files_to_target_p8.sql &
 
--Overall time taken ~8 hrs
Import data to HASH RDS db
Change DB parameter group to have new undo retention as 900 (15 mins)

Recreate redo logs
./run_on_target.sh add_redos.sql
 
--ensure no errors
grep ORA- add_redos.lst
create tablespaces on Hash RDS
nohup ./run_on_target.sh create_tablespaces.sql &
 
--ensure no errors
grep ORA- create_tablespaces.lst
 
--Temprarily change TEMP and UNDO tablespace size
alter tablespace temp resize 5T;
alter tablespace UNDO_T1 resize 4T;
Create schema owners

./run_on_target.sh create_schema_owners.sql
Create tables with PK

./run_sql.sh pspadm_hash_Partitioned_tables_ddl.sql
./run_sql.sh tables.sql
./run_sql.sh create_pk.sql
import data

nohup impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_data_%U.dmp,DATA_PUMP_DIR1:exp_data1_%U.dmp,DATA_PUMP_DIR2:exp_data2_%U.dmp,DATA_PUMP_DIR3:exp_data3_%U.dmp parallel=16 schemas=PSPADM exclude=TABLE:\"IN \(\'PSP_FINANCIAL_TRANS_STATE\',\'PSP_PSTUB_PAY_ITEM\',\'PSP_ENTRY_DETAIL_RECORD\',\'PSP_TAX\',\'PSP_COMPANY_EVENT_EMAIL_PARAM\',\'PSP_QBDT_PAYLINE_INFO\',\'PSP_COMPANY_EVENT_DETAIL\',\'PSP_ENTITY_UPDATE_HIST\',\'PSP_SOURCE_SYSTEM_TRANSMISSION\',\'T_PPPI_OLD\',\'T_PCEEP_OLD\',\'T_PDATL_OLD\',\'T_PPU_OLD\',\'T_PPPI_OLD\',\'T_PPU\'\)\" content=data_only EXCLUDE=STATISTICS TRANSFORM=DISABLE_ARCHIVE_LOGGING:Y logfile=DATA_PUMP_DIR:imp_data.log 2>&1 &
--Time taken ~15 hrs
 
nohup impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_data_%U.dmp,DATA_PUMP_DIR1:exp_data1_%U.dmp,DATA_PUMP_DIR2:exp_data2_%U.dmp,DATA_PUMP_DIR3:exp_data3_%U.dmp parallel=16 TABLES=PSPADM.PSP_FINANCIAL_TRANS_STATE,PSPADM.PSP_PSTUB_PAY_ITEM content=data_only EXCLUDE=STATISTICS TRANSFORM=DISABLE_ARCHIVE_LOGGING:Y logfile=DATA_PUMP_DIR:imp_psp_tables1.log 2>&1 &
--Time taken 16 hrs 30 mins
 
nohup impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_data_%U.dmp,DATA_PUMP_DIR1:exp_data1_%U.dmp,DATA_PUMP_DIR2:exp_data2_%U.dmp,DATA_PUMP_DIR3:exp_data3_%U.dmp parallel=16 TABLES=PSPADM.PSP_ENTRY_DETAIL_RECORD,PSPADM.PSP_TAX content=data_only EXCLUDE=STATISTICS TRANSFORM=DISABLE_ARCHIVE_LOGGING:Y logfile=DATA_PUMP_DIR:imp_psp_tables2.log 2>&1 &
 --Time taken ~11 hrs 45 mins
 
nohup impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_data_%U.dmp,DATA_PUMP_DIR1:exp_data1_%U.dmp,DATA_PUMP_DIR2:exp_data2_%U.dmp,DATA_PUMP_DIR3:exp_data3_%U.dmp parallel=16 TABLES=PSPADM.PSP_COMPANY_EVENT_EMAIL_PARAM,PSPADM.PSP_QBDT_PAYLINE_INFO,PSPADM.PSP_COMPANY_EVENT_DETAIL content=data_only EXCLUDE=STATISTICS TRANSFORM=DISABLE_ARCHIVE_LOGGING:Y logfile=DATA_PUMP_DIR:imp_psp_tables3.log 2>&1 &
 --Time taken ~5 hrs 20 mins
 
nohup impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_data_%U.dmp,DATA_PUMP_DIR1:exp_data1_%U.dmp,DATA_PUMP_DIR2:exp_data2_%U.dmp,DATA_PUMP_DIR3:exp_data3_%U.dmp parallel=16 TABLES=PSPADM.PSP_FINANCIAL_TRANSACTION,PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION content=data_only EXCLUDE=STATISTICS TRANSFORM=DISABLE_ARCHIVE_LOGGING:Y logfile=DATA_PUMP_DIR:imp_psp_tables4.log 2>&1 &
 --Time taken ~4 hrs 30 mins
 
--Ensure No Errors in all import logs
run row count sanity (in case of import failures seen in import log)
collect stats

nohup ./run_sql.sh collect_stats.sql &
--Time taken ~6 hrs 30 mins
cleanup dump files
nohup ./run_sql.sh cleanup_exp_dump_files.sql &
Create other Indexes
./run_sql.sh create_sec_fk_indexes_1.sql
./run_sql.sh create_sec_fk_indexes_2.sql
./run_sql.sh create_sec_fk_indexes_3.sql
./run_sql.sh create_sec_fk_indexes_4.sql
 
--Time Taken ~27 hrs
(Optional) Below is optional in case there is any error while index creation
CREATE INDEX PSPADM.PSP_PSTUB_PAY_ITEM_FK1 ON PSPADM.PSP_PSTUB_PAY_ITEM (COMPANY_FK,PAYSTUB_FK) local online parallel (degree 4) TABLESPACE PSP_IDX01;
 
--got below error for few indexes, so created indexes offline as below(without using online keyword)
ORA-00604: error occurred at recursive SQL level 1
ORA-01450: maximum key length (3215) exceeded
--offline rebuild
CREATE INDEX PSPADM.PSP_AGENCY_AGENCYIDENC_I1 ON PSPADM.PSP_AGENCY (AGENCY_ID_ENC) parallel (degree 4) TABLESPACE PSP_IDX02;
CREATE INDEX PSPADM.PSP_BANK_ACCOUNT_ENC_I1 ON PSPADM.PSP_BANK_ACCOUNT (ACCOUNT_NUMBER_ENC) parallel (degree 4) TABLESPACE PSP_IDX01;
CREATE INDEX PSPADM.PSP_COMPANY_FEDTAXIDENC_I1 ON PSPADM.PSP_COMPANY (FED_TAX_ID_ENC) parallel (degree 4) TABLESPACE PSP_IDX02;
 
CREATE INDEX PSPADM.PSP_EFTPS_PAYMENT_DETAIL_I3 ON PSPADM.PSP_EFTPS_PAYMENT_DETAIL (EFT_TRANSACTION_ID)  parallel (degree 4) TABLESPACE PSP_IDX02;
CREATE INDEX PSPADM.PSP_EFTPS_PAYMENT_DETAIL_I4 ON PSPADM.PSP_EFTPS_PAYMENT_DETAIL (TRANSACTION_ID,AGENCY_PAYMENT_ID)  parallel (degree 4) TABLESPACE PSP_IDX02;
CREATE INDEX PSPADM.PSP_EMPLOYEE_TAXIDENC_I1 ON PSPADM.PSP_EMPLOYEE (TAX_ID_ENC)  parallel (degree 4) TABLESPACE PSP_IDX01;
CREATE INDEX PSPADM.PSP_ENMT_FEDTAXIDENC_I1 ON PSPADM.PSP_ENTITLEMENT_UNIT (FED_TAX_ID_ENC)  parallel (degree 4) TABLESPACE PSP_IDX01;
 
CREATE INDEX PSPADM.PSP_FRAUD_ACCOUNT_ENC_I1 ON PSPADM.PSP_FRAUD_BANK_ACCOUNT (ACCOUNT_NUMBER_ENC)  parallel (degree 4) TABLESPACE PSP_IDX01;
disable index parallel degree
./run_sql.sh disable_index_parallel.sql &
create FK constraints
./run_sql.sh ref_constraints.sql
 
NOTE:
No FK between PSP_FINTXN_ONHOLDREASON_ASSOC & PSP_FINANCIAL_TRANSACTION
No FK between PSP_PAYCHECK_USAGE_HIST & PSP_PAYCHECK_USAG


Import other objects

nohup impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_pspadm_metadata.dmp schemas=PSPADM EXCLUDE=TABLE,INDEX,REF_CONSTRAINT,STATISTICS content=METADATA_ONLY TRANSFORM=DISABLE_ARCHIVE_LOGGING:Y logfile=DATA_PUMP_DIR:imp_pspadm_metadata.log 2>&1 &
 
impdp userid=intuadmin/"xxxxxx"@psphpp02 dumpfile=DATA_PUMP_DIR:exp_pspadm_metadata.dmp include=trigger logfile=DATA_PUMP_DIR:imp_pspadm_triggers.log
create check and not null constraints via generated script from source(production)
./run_sql.sh create_notnull_cc.sql
 
--Ensure no errors
create users
./run_sql.sh create_users.sql
 
--below should not return any PSPADM objects errors
grep -B 3 ORA-00942 CREATE_USERS.lst|egrep -v '"PSPBOADMIN"."|"APPBUGFIX"."|"MCHOUBEY"."|"SYS"."|"DIYMIGADM"."|"PSPIDPSBACKUP"."|"SKUMAR71"."|"PSPADM"."PSP_SOURCE_SYSTEM_TRANSMISSION"|"PSPLOG"."|"PSPFLUXADM"."|"PSPTEMP"."'
lock app users
alter user pspapp account lock;
alter user pspadm account lock;


check for invalid objects.

select owner, object_type, object_name from dba_objects where status <> 'VALID';
Run Sanity check on source and target HASH rds db

@sanity_chk.sql
Final step for Goldengate replication from Range to Hash RDS
login to HASH RDS
Setup  supplemental log
SELECT SUPPLEMENTAL_LOG_DATA_MIN, FORCE_LOGGING FROM V$DATABASE;
exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
exec rdsadmin.rdsadmin_util.force_logging();
exec rdsadmin.rdsadmin_util.switch_logfile;
exec rdsadmin.rdsadmin_util.set_configuration('archivelog retention hours',72);
commit;
Grant permission to GGT and GGS
grant create session to GGT;
grant DBA to GGT;
exec rdsadmin.rdsadmin_dbms_goldengate_auth.grant_admin_privilege(grantee=>'GGT',grant_select_privileges=>true, do_grants=>TRUE);
grant exempt access policy to GGT;
 
GRANT SELECT ANY DICTIONARY TO GGS;
host sed '8,25d' $GG_HOME/sequence.sql > $GG_HOME/rds_sequence.sql
@$GG_HOME/rds_sequence.sql ggs
GRANT EXECUTE ON GGS.updateSequence to GGT;
GRANT EXECUTE ON GGS.replicateSequence to GGT;
drop table GGT.CHECKPOINT;
drop table GGT.CHECKPOINT_LOX;
Create credentials and checkpoint.  
ALTER CREDENTIALSTORE ADD USER ggs@psphpp02 PASSWORD "xxxxx" ALIAS ggsource_psphpp02;
ALTER CREDENTIALSTORE ADD USER ggt@psphpp02 PASSWORD "xxxxx" ALIAS ggtarget_psphpp02;
obey ./diroby/dblogin_s_psphpp02.oby
obey ./diroby/dblogin_t_psphpp02.oby
add checkpointtable
add schema trandata
obey ./diroby/dblogin_s.oby
add schematrandata PSPADM
Setup and start oracle range to oracle hash replicat process
obey ./diroby/create_rpspwp01.oby
 
GGSCI (ggpsphpp01.sbg-psp-prod.a.intuit.com) 2> obey ./diroby/create_rpspwp01.oby
 
GGSCI (ggpsphpp01.sbg-psp-prod.a.intuit.com) 3> dblogin USERIDALIAS ggtarget
 
Successfully logged into database.
 
GGSCI (ggpsphpp01.sbg-psp-prod.a.intuit.com as ggt@PSPHPP01) 4> add replicat rpspwp01, INTEGRATED, EXTTRAIL ./dirdat/pspuwp01/tr
 
REPLICAT (Integrated) added.
 
GGSCI (ggpsphpp01.sbg-psp-prod.a.intuit.com as ggt@PSPHPP01) 11> register replicat rpspwp01 database
 
2023-03-31 06:29:18  INFO    OGG-02528  REPLICAT RPSPWP01 successfully registered with database as inbound server OGG$RPSPWP01.
Get correct SCN from PITHASH1 db and modify below. Start replication atcsn
start rpspwp01 atcsn 8397003383021


Setup GoldenGate replication from Hash RDS (B) to Range RDS (C)
Shut down application & ensure no application connections
On Hash GG hub, create extract and pump [to Range RDS (C)]

obey ./diroby/create_epsphp02.oby
obey ./diroby/dblogin_s_psphpp02.oby
register extract epsphp02
 
start epsphp02
info epsphp02
 
obey ./diroby/create_ppsprp01.oby
 
start ppsprp01
info ppsprp01
Verify trails are coming to Range GG hub
ls $GG_HOME/dirdat/psphpp02/


Take Snapshot of Production (A)
Once snapshot completed, Start applications back 
Restore snapshot as new Range RDS (C)
Final step for goldengate replication from Hash db(B) to Range db(C)
login to Range db (C)
SELECT SUPPLEMENTAL_LOG_DATA_MIN, FORCE_LOGGING FROM V$DATABASE;
exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
exec rdsadmin.rdsadmin_util.force_logging();
exec rdsadmin.rdsadmin_util.switch_logfile;
exec rdsadmin.rdsadmin_util.set_configuration('archivelog retention hours',72);
commit;
Grant permission to GGT and GGS

grant create session to GGT;
grant DBA to GGT;
exec rdsadmin.rdsadmin_dbms_goldengate_auth.grant_admin_privilege(grantee=>'GGT',grant_select_privileges=>true, do_grants=>TRUE);
grant exempt access policy to GGT;
GRANT SELECT ANY DICTIONARY TO GGS;
run rds_sequences.sql on Range db (C)
host sed '8,25d' $GG_HOME/sequence.sql > $GG_HOME/rds_sequence.sql
@$GG_HOME/rds_sequence.sql ggs
GRANT EXECUTE ON GGS.updateSequence to GGT;
GRANT EXECUTE ON GGS.replicateSequence to GGT;
drop table GGT.CHECKPOINT;
drop table GGT.CHECKPOINT_LOX;
create credentialstore on Range (C) gg hub
ALTER CREDENTIALSTORE ADD USER ggs@psprpp01 PASSWORD "WGgRgK7d#(7eX" ALIAS ggsource;
ALTER CREDENTIALSTORE ADD USER ggt@psprpp01 PASSWORD "WGgRgK7d#(7eX" ALIAS ggtarget;
obey ./diroby/dblogin_s.oby
obey ./diroby/dblogin_t.oby
add checkpointtable
add schematrandata

obey ./diroby/dblogin_s.oby
add schematrandata PSPADM
On Range GG hub, create replicat
obey ./diroby/create_rpsphp02.oby
 
register replicat rpsphp02 database
On Range GG Hub, start replicat
start rpsphp02
On Hash GG hub, Run following commands to flush sequences
GGSCI (ggpsppf501.sbg-psp-ppd.a.intuit.com) 5> dblogin useridalias ggsource_hash_pds
Successfully logged into database.
 
GGSCI (ggpsppf501.sbg-psp-ppd.a.intuit.com as ggs@PPSPHP01) 6> flush sequence pspadm.*
 
2023-05-18 01:09:31  INFO    OGG-15311  Successfully flushed 76 sequence(s) pspadm.*.
Setup GG monitoring on Range GG hub
verify sequence bi-directional replication
1. On Range (current live database)
SQL> create sequence pspadm.seq_test;
 
Sequence created.
 
2. SQL> select SEQUENCE_OWNER, SEQUENCE_NAME, LAST_NUMBER from dba_sequences where SEQUENCE_OWNER = 'PSPADM' and SEQUENCE_NAME = 'SEQ_TEST';
 
SEQUENCE_OWNER       SEQUENCE_NAME        LAST_NUMBER
-------------------- -------------------- -----------
PSPADM               SEQ_TEST                       1
 
3. Test the sequence
 
a) Run it on Range db(A)
SQL> select pspadm.seq_test.nextval from dual;
 
   NEXTVAL
----------
         1
 
b) Wait for 10 secs. Run it on Hash db (B)
SQL> select SEQUENCE_OWNER, SEQUENCE_NAME, LAST_NUMBER from dba_sequences where SEQUENCE_OWNER = 'PSPADM' and SEQUENCE_NAME = 'SEQ_TEST';
 
SEQUENCE_OWNER       SEQUENCE_NAME        LAST_NUMBER
-------------------- -------------------- -----------
PSPADM               SEQ_TEST                      22
 
c) Wait for 10 secs. Run it on Range db (C)
SQL> select SEQUENCE_OWNER, SEQUENCE_NAME, LAST_NUMBER from dba_sequences where SEQUENCE_OWNER = 'PSPADM' and SEQUENCE_NAME = 'SEQ_TEST';
 
SEQUENCE_OWNER       SEQUENCE_NAME        LAST_NUMBER
-------------------- -------------------- -----------
PSPADM               SEQ_TEST                      43
Prod



1. On Range (current Production)
SQL> select SEQUENCE_OWNER, SEQUENCE_NAME, LAST_NUMBER from dba_sequences where SEQUENCE_OWNER = 'PSPADM' and SEQUENCE_NAME = 'TEST';
 
SEQUENCE_OWNER       SEQUENCE_NAME        LAST_NUMBER
-------------------- -------------------- -----------
PSPADM               TEST                         777
 
2. Test sequence
 
SQL> select pspadm.test.currval from dual
 
   CURRVAL
----------
       778
 
b) Wait for 10 secs. Run it on Hash db (B)
SQL> select pspadm.test.nextval from dual;
 
   NEXTVAL
----------
       798
 
c) Wait for 10 secs. Run it on Range db (C)
SQL> select pspadm.test.nextval from dual;
 
   NEXTVAL
----------
       820



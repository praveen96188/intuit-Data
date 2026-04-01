--vacuum age check
SELECT datname, age(datfrozenxid) FROM pg_database;

--vacuum tables list
SELECT c.oid::regclass as table_name,
       greatest(age(c.relfrozenxid),age(t.relfrozenxid)) as age
FROM pg_class c
         LEFT JOIN pg_class t ON c.reltoastrelid = t.oid
WHERE c.relkind IN ('r', 'm')  order by 2 desc; 

--timing
\timing
SELECT pg_sleep(60);


aws rds add-option-to-option-group --profile sbg-psp-ppd --region us-east-2 \
--option-group-name replica-19-7c5992eb-96c0-4e03-9e69-61730c9446de \
--options '[{"OptionSettings":[{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_CLIENT","Value":"SHA512,SHA384,SHA256,SHA1,MD5"},{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_SERVER","Value":"SHA1,MD5"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]' \
--apply-immediately


eiamCli aws_creds -a 152430470825 -r PowerUser -p default

eiamCli aws_creds -a 893547637742 -r PowerUser -p sbg-psp-prod

eiamCli aws_creds -a 893547637742 -r PowerUser -p default


/Users/.../.AWS/credentials
change aws_access_key_id,aws_secret_access_key and aws_session_token for Prod and non prod.

Non-Prod:
[sbg-psp-ppd]
aws_access_key_id=
aws_secret_access_key=
aws_session_token=

eiamCli aws_creds -a 893547637742 -r PowerUser -p sbg-psp-prod
--ami update
./update_gg_hub_ami.sh sbg-psp-prod us-west-2 pspapg01 |tee update_gg_hub_ami_pspapg01.log




--cros region
nohup ./copy_rds_cross_region.sh e2e clusterdb pspe2euw pspe2eme us-west-2 us-east-2 vpc-2 db.r5.2xlarge > copy_rds_cross_region_pspe2eme_09-05-2023.log &

--create database
./create_rds.sh prodx clusterdb PSPHPP01 us-west-2 vpc-2 false

--refresh database 
 <AppEnv> should be qa, e2e, stg, sbx, prf, prod
#              <DBType> should be clusterdb or reportdb
#              <Source DBName> is name of Source RDS to be restored
#              <Target DBName> is name of the Restored RDS
# old_staging_db

./oracle-staging-refresh.sh e2e clusterdb psppf501 psptempdb psppfib1








aws rds describe-db-cluster-snapshots --db-cluster-identifier psp-shared-aud \
    --query "DBClusterSnapshots[*].DBClusterSnapshotIdentifier"

ips rds restore-cluster -c ppsp-temp-db.json -d deploy.json -e temp -g primary-temp -i --new-cluster-id ppsp-temp-db  --snapshot-id rds:psp-shared-aud-2023-07-05-05-11



ips rds restore-cluster -c ppsp-temp-db.json -d deploy.json -e temp -g primary-temp1 -i db1 \
    --new-cluster-id ppsp-temp-db1 \
    --snapshot-id rds:psp-shared-aud-2023-07-05-05-11

pspehp01 =
  (description =
    (address_list =
      (address = (protocol = tcp)(host = pspehp01.sbg-psp-prod.a.intuit.com)(port = 1521))
    )
    (connect_data =
      (sid = pspehp01)
    )
  )

pspuep05 =
  (description =
    (address_list =
      (address = (protocol = tcp)(host = pspuep05.sbg-psp-prod.a.intuit.com)(port = 1521))
    )
    (connect_data =
      (sid = pspuep05)
    )
  )



2023-06-27T13:13:15 [SOURCE_UNLOAD   ]W:  Oracle error code is '1555' ORA-01555: snapshot too old: rollback segment number 13 with name "_SYSSMU13_3436483065$" too small   (oracle_endpoint_unload.c:235)

2023-06-27T13:13:15 [SOURCE_UNLOAD   ]E:  ORA-01555: snapshot too old: rollback segment number 13 with name "_SYSSMU13_3436483065$" too small  [1020436]  (oracle_endpoint_unload.c:235)



intuadmin/"ZrFMqe#xc91kgf"@'pspuep05.cerpnqmbpq9a.us-east-2.rds.amazonaws.com:1521/pspuep05'



Source: pspuep05

create dblink:

create database link to_pspehp01 connect to intuadmin identified by "PIgRgK7d#(2XZ" using '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST= pspehp01.sbg-psp-prod.a.intuit.com)(PORT=1521))(CONNECT_DATA=(SID=pspehp01)))';
select INSTANCE_NAME, HOST_NAME from v$instance@to_pspehp01;




export:
nohup expdp userid=intuadmin/"ZrFMqe#xc91kgf"@pspuep05 directory=DATA_PUMP_DIR dumpfile=exp_pspadm_pspuep05_metadata.dmp SCHEMAS=PSPADM content=METADATA_ONLY logfile=DATA_PUMP_DIR:exp_pspadm_pspuep05_metadata.log 2>&1 &

check Dump file is created or not :

SELECT * FROM TABLE(rdsadmin.rds_file_util.listdir('DATA_PUMP_DIR')) ORDER BY MTIME;

create copy file:exp_pspadm_pspuep05_metadata.sql
 
spool copy_exp_pspadm_pspuep05_metadata
set echo on
set feed on
set time on
set timi on

BEGIN
    DBMS_FILE_TRANSFER.PUT_FILE(
      source_directory_object       => 'DATA_PUMP_DIR',
      source_file_name              => 'exp_pspadm_pspuep05_metadata.dmp',
      destination_directory_object  => 'DATA_PUMP_DIR',
      destination_file_name         => 'exp_pspadm_pspuep05_metadata.dmp',
      destination_database          => 'to_pspehp01'
    );
END;
/

---pspuep05_run_on_target.sh
sqlplus intuadmin/"ZrFMqe#xc91kgf"@'pspuep05.sbg-psp-prod.a.intuit.com:1521/pspuep05' << EOF
set echo on
set feed on
set time on
set timi on

select INSTANCE_NAME, HOST_NAME from v\$instance;

@$1 $2
exit

EOF

copy from source to target:

./pspuep05_run_on_target.sh exp_pspadm_pspuep05_metadata.sql



check file copied or not in target:

SELECT * FROM TABLE(rdsadmin.rds_file_util.listdir('DATA_PUMP_DIR')) ORDER BY MTIME;


import on target:
nohup impdp userid=intuadmin/"PIgRgK7d#(2XZ"@pspehp01 dumpfile=DATA_PUMP_DIR:exp_pspadm_pspuep05_metadata.dmp schemas=PSPADM EXCLUDE=TABLE,INDEX,REF_CONSTRAINT,STATISTICS content=METADATA_ONLY logfile=DATA_PUMP_DIR:imp_pspadm_pspuep05_metadata.log 2>&1 &

impdp userid=intuadmin/"PIgRgK7d#(2XZ"@pspehp01 dumpfile=DATA_PUMP_DIR:exp_pspadm_pspuep05_metadata.dmp include=trigger logfile=DATA_PUMP_DIR:imp_pspadm_triggers.log



select USERNAME from DBA_USERS where USERNAME  like '%AK%';


select USERNAME from DBA_USERS where USERNAME in ('RN5',
'KSUR',
'CBHAT',
'PRAVEENKUMARH635',
'VRAJVANSHI',
'AAGARWAL25','VJOSHI4');


intuadmin/"KVadPpU3q#(7eC"@'psprpp01.sbg-psp-prod.a.intuit.com:1521/psprpp01'
intuadmin/"KVadPpU3q#(7eC"@'psphpp02.sbg-psp-prod.a.intuit.com:1521/psphpp02'


PRAVEENKUMARH635: https://tincan.intuit.com#90275aab93d40303b3b8d094dc28d20333f49f7767f5b06cf0cc82a5df0213dd
Same password for A,B,C
CBHAT : https://tincan.intuit.com#6cce89e3ae2e683b3b5315d21a8e5ea4b2c49769a561f5f0b74977befe40757e
Same password for A,B,C
AAGARWAL25 : https://tincan.intuit.com#62aa34b5666adc66c09f06a99d531dd1bffd92c4d88b9ff8f6a52891d7a26c20
Same password for A,B,C
VJOSHI4 : https://tincan.intuit.com#3d6123199db73d7b4159adbe5abeeb9a000e5dcf45e94c5a901255813abaf205
Same password for A,B,C
VRAJVANSHI : https://tincan.intuit.com#1476774f09d54081453c98b8741a32fc845ee7397b23f817d14c493ba4ad93cb
Same password for A,B,C
RN5 : https://tincan.intuit.com#c1c764ae14b72dee2af0d2eb5deb37bb90550217437bd774da9dd73a5920e858
Same password for A,B,C
KSUR : https://tincan.intuit.com#3e8d2bc029ff29153026d326be7e0b779df04ae07a9ef1e3f70175cd3e461d27
Same password for A,B,C
SBHAGAT : https://tincan.intuit.com#c4b82c92df6585a3027d8cb4df6eebb59dc4cc70299e3a93c68872ef8449ba0e
Same password for A,B,C
AJOHNSON43 : https://tincan.intuit.com#a2ce324242dad3d07132eb54363769ab7419528193f8b3348c8bd36c5f85aeda
Same password for A,B,C
KMUTHURANGAM : https://tincan.intuit.com#2d3876a7f8f446ad59a0f81461980d853184b44f68b2e442616747cbdead75be

Same password for A,B,C
hrajoria : https://tincan.intuit.com#a4c15af8527d90e431d0d09fc9b5212206f4f6057666a55f72ebfcf66fa30ad7


Alter user PRAVEENKUMARH635  identified by "G^jW7ijCbfhv1";
Alter user CBHAT identified by "G^jW7ijCbfhv2";
Alter user AAGARWAL25 identified by "G^jW7ijCbfh3";
Alter user VJOSHI4 identified by "G^jW7ijCbfhv4";
Alter user VRAJVANSHI identified by "G^jW7ijCbfhv5";
Alter user RN5 identified by "G^jW7ijCbfhv6";
Alter user KSUR identified by "G^jW7ijCbfhv7";
Alter user SBHAGAT identified by "G^jW7ijCbfhv8";
Alter user AJOHNSON43 identified by "G^jW7ijCbfhv9";
Alter user KMUTHURANGAM identified by "G^jW7ijCbfhv10";
Alter user hrajoria identified by "G^jW7ijCbfhv11";

Alter user jbansal identified by "G^jW7ijCbfhv12";
Alter user akumari3 identified by "G^jW7ijCbfhv13";







psphdg01.cjls0bohfgpq.us-west-2.rds.amazonaws.com



. /l/orcl

stty -echo
echo "Please enter the database master user intuadmin password:"
read sys_password
stty echo

echo $sys_password > .p
function run_sql
{
  sys_password=`cat .p`
  rm -f .p
  sqlplus /nolog <<EOF
    connect intuadmin/$sys_password@'psphdg01.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521/PSPHPP02'
    set echo on
    set feed on
    set time on
    set timi on

    select INSTANCE_NAME, HOST_NAME from v\$instance;

    @$1 $2
    exit
EOF
}
export -f run_sql
nohup bash -c "run_sql $1 $2" &





Oracle Range (A):
Hostname: pspadg01.sbg-psp-prod.a.intuit.com
port:1521
Service:pspuwp01

Oracle Hash(B):
Host: psphdg01.sbg-psp-prod.a.intuit.com
port:1521
Service:psphpp02

Oracle Range(C):
Host: psprpp01.sbg-psp-prod.a.intuit.com
port: 1521
Service:psprpp01


select employee_payroll_item_seq , item_limit, 'pg' as source from pspadm.PSP_EMPLOYEE_PAYROLL_ITEM  where created_date between  '2023-08-20 02:00:00'  and '2023-08-20 03:00:00' 
union  
select employee_payroll_item_seq , item_limit , 'or' as source from imp_psphpp02_pspadm.PSP_EMPLOYEE_PAYROLL_ITEM where employee_payroll_item_seq in (select employee_payroll_item_seq  from pspadm.PSP_EMPLOYEE_PAYROLL_ITEM where created_date between  '2023-08-20 02:00:00'  and '2023-08-20 03:00:00' ) order by 1 ;




DMS Rep Cluster:

cluster: ppsp-pds-uw01
DBname: ppdspg01
Instance: db.r6g.4xlarge

GG Rep Cluster:

Cluster: ppsp-pds-uw02
DBname: ppdspg02
Instance: db.r6g.4xlarge



select employee_payroll_item_seq , item_limit, ‘pg’ as source from pspadm.PSP_EMPLOYEE_PAYROLL_ITEM  where created_date between  ‘2023-08-20 02:00:00’  and ‘2023-08-20 03:00:00’ union  
select employee_payroll_item_seq , item_limit , ‘or’ as source from imp_psphpp02_pspadm.PSP_EMPLOYEE_PAYROLL_ITEM where employee_payroll_item_seq in (select employee_payroll_item_seq  from pspadm.PSP_EMPLOYEE_PAYROLL_ITEM where created_date between  ‘2023-08-20 02:00:00’  and ‘2023-08-20 03:00:00’ ) order by 1 ;




--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database ppdspg01 from public;

-- switch to psppp001 database
\c ppdspg01 pspadm_owner

create role pspadm_readwrite_role;
-- grant permission to connect the database
grant connect on database ppdspg01 to pspadm_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on tables to pspadm_readwrite_role;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant usage on sequences to pspadm_readwrite_role;


\c psppp001 postgres

-- create read-write application user/role with permission to login and grant needful permissions #--
create user pspapp with password 'XXXXXXX';
-- grant read write role to user
grant pspadm_readwrite_role to pspapp;
--set search path for pspapp
alter user pspapp set search_path to pspadm;



\c psppp001 postgres

create role pspadm_readwrite_role;
-- grant permission to connect the database
grant connect on database psppp001 to pspadm_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on tables to pspadm_readwrite_role;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant usage on sequences to pspadm_readwrite_role;


us-east: 
Non-Prod PDS APG Database.
Host: ppsp-pds-ue01.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com
port: 1521
DB: ppdspg01















DROP SEQUENCE seq_atf_batch_id_nbr;
DROP SEQUENCE  psid_ne;
DROP SEQUENCE  psid_default;
DROP SEQUENCE  seq_trace_nbr;
DROP SEQUENCE  psid_me;
DROP SEQUENCE  psid_mn;
DROP SEQUENCE  psid_mt;
DROP SEQUENCE  psid_nm;
DROP SEQUENCE  seq_asst_usage_billing_token;
DROP SEQUENCE  seq_qbdt_source_company_id;
DROP SEQUENCE  psid_ga;
DROP SEQUENCE  psid_la;
DROP SEQUENCE  seq_ee_pitem_calc_token;
DROP SEQUENCE  psid_hi;
DROP SEQUENCE  psid_ia;
DROP SEQUENCE  psid_ky;
DROP SEQUENCE  psid_tn;
DROP SEQUENCE  psid_wv;
DROP SEQUENCE  psid_md;
DROP SEQUENCE  psid_mi;
DROP SEQUENCE  psid_ak;
DROP SEQUENCE  psid_ca;
DROP SEQUENCE  psid_ks;
DROP SEQUENCE  psid_ok;
DROP SEQUENCE  psid_co;
DROP SEQUENCE  psid_tx;
DROP SEQUENCE  seq_trace_number;
DROP SEQUENCE  seq_401k_upload_batch_id;
DROP SEQUENCE  seq_401k_signup_batch_id;
DROP SEQUENCE  seq_eftps_segment_sequence;
DROP SEQUENCE  psid_il;
DROP SEQUENCE  seq_txn_token_nbr;
DROP SEQUENCE  psid_nh;
DROP SEQUENCE  seq_eftps_payment_sequence;
DROP SEQUENCE  psid_al;
DROP SEQUENCE  psid_sc;
DROP SEQUENCE  psid_ar;
DROP SEQUENCE  psid_ms;
DROP SEQUENCE  psid_nd;
DROP SEQUENCE  psid_ri;
DROP SEQUENCE  psid_wi;
DROP SEQUENCE  psid_de;
DROP SEQUENCE  psid_sd;
DROP SEQUENCE  psid_va;
DROP SEQUENCE  psid_in;
DROP SEQUENCE  psid_ut;
DROP SEQUENCE  seq_transaction_number;
DROP SEQUENCE  seq_ach_file_ctr;
DROP SEQUENCE  psid_ma;
DROP SEQUENCE  psid_wy;
DROP SEQUENCE  psid_az;
DROP SEQUENCE  psid_mo;
DROP SEQUENCE  psid_nv;
DROP SEQUENCE  seq_usage_billing_token;
DROP SEQUENCE  seq_gems_upload_batch_id;
DROP SEQUENCE  seq_eftps_file_sequence;
DROP SEQUENCE  psid_dc;
DROP SEQUENCE  psid_nj;
DROP SEQUENCE  psid_nc;
DROP SEQUENCE  psid_pa;
DROP SEQUENCE  psid_id;
DROP SEQUENCE  psid_or;
DROP SEQUENCE  seq_ee_calculation_token;
DROP SEQUENCE  psid_ny;
DROP SEQUENCE  psid_oh;
DROP SEQUENCE  psid_fl;
DROP SEQUENCE  psid_vt;
DROP SEQUENCE  seq_subscription_number;
DROP SEQUENCE  psid_ct;
DROP SEQUENCE  psid_wa;

--staging



ALTER SEQUENCE PSPADM.SEQ_TAX_ACC_AUD RESTART WITH 30987;
ALTER SEQUENCE PSPADM.PSID_NE RESTART WITH 431001989;
ALTER SEQUENCE PSPADM.PSID_DEFAULT RESTART WITH 999004502;
ALTER SEQUENCE PSPADM.SEQ_TRACE_NBR RESTART WITH 16368000000000;
ALTER SEQUENCE PSPADM.PSID_ME RESTART WITH 323001627;
ALTER SEQUENCE PSPADM.PSID_MN RESTART WITH 427007155;
ALTER SEQUENCE PSPADM.SEQ_PSID RESTART WITH 99900509;
ALTER SEQUENCE PSPADM.PSID_MT RESTART WITH 530002078;
ALTER SEQUENCE PSPADM.PSID_NM RESTART WITH 535003031;
ALTER SEQUENCE PSPADM.SEQ_ASST_USAGE_BILLING_TOKEN RESTART WITH 179140;
ALTER SEQUENCE PSPADM.SEQ_QBDT_SOURCE_COMPANY_ID RESTART WITH 108939105;

ALTER SEQUENCE PSPADM.PSID_GA RESTART WITH 313013621;
ALTER SEQUENCE PSPADM.PSID_LA RESTART WITH 422005520;
ALTER SEQUENCE PSPADM.SEQ_EE_PITEM_CALC_TOKEN RESTART WITH 1280140;
ALTER SEQUENCE PSPADM.PSID_HI RESTART WITH 715002684;
ALTER SEQUENCE PSPADM.TEST1 RESTART WITH 88;
ALTER SEQUENCE PSPADM.PSID_IA RESTART WITH 419002575;
ALTER SEQUENCE PSPADM.PSID_KY RESTART WITH 321003104;
ALTER SEQUENCE PSPADM.PSID_TN RESTART WITH 347000675;
ALTER SEQUENCE PSPADM.PSID_WV RESTART WITH 353001550;
ALTER SEQUENCE PSPADM.PSID_MD RESTART WITH 324010083;
ALTER SEQUENCE PSPADM.PSID_MI RESTART WITH 326009953;

ALTER SEQUENCE PSPADM.PSID_AK RESTART WITH 702002044;
ALTER SEQUENCE PSPADM.PSID_CA RESTART WITH 606121845;
ALTER SEQUENCE PSPADM.PSID_KS RESTART WITH 420003206;
ALTER SEQUENCE PSPADM.PSID_OK RESTART WITH 440003956;
ALTER SEQUENCE PSPADM.PSID_CO RESTART WITH 508013558;
ALTER SEQUENCE PSPADM.PSID_TX RESTART WITH 448038323;
ALTER SEQUENCE PSPADM.SEQ_TRACE_NUMBER RESTART WITH 2128832172500;
ALTER SEQUENCE PSPADM.SEQ_401K_UPLOAD_BATCH_ID RESTART WITH 1316;
ALTER SEQUENCE PSPADM.SEQ_401K_SIGNUP_BATCH_ID RESTART WITH 553;
ALTER SEQUENCE PSPADM.SEQ_EFTPS_SEGMENT_SEQUENCE RESTART WITH 390470;
ALTER SEQUENCE PSPADM.PSID_IL RESTART WITH 417015233;

ALTER SEQUENCE PSPADM.SEQ_TXN_TOKEN_NBR RESTART WITH 7488235;
ALTER SEQUENCE PSPADM.PSID_NH RESTART WITH 333002484;
ALTER SEQUENCE PSPADM.SEQ_EFTPS_PAYMENT_SEQUENCE RESTART WITH 19549921;
ALTER SEQUENCE PSPADM.ENTITLEMENT_SEQ RESTART WITH 463003;
ALTER SEQUENCE PSPADM.PSID_AL RESTART WITH 401004974;
ALTER SEQUENCE PSPADM.PSID_SC RESTART WITH 345005437;
ALTER SEQUENCE PSPADM.PSID_AR RESTART WITH 405002406;
ALTER SEQUENCE PSPADM.PSID_MS RESTART WITH 428002332;
ALTER SEQUENCE PSPADM.PSID_ND RESTART WITH 438000801;
ALTER SEQUENCE PSPADM.PSID_RI RESTART WITH 344000983;
ALTER SEQUENCE PSPADM.PSID_WI RESTART WITH 454005836;

ALTER SEQUENCE PSPADM.SEQ_ATF_BATCH_ID_NBR RESTART WITH 9913;
ALTER SEQUENCE PSPADM.PSID_DE RESTART WITH 310001529;
ALTER SEQUENCE PSPADM.PSID_SD RESTART WITH 446000917;
ALTER SEQUENCE PSPADM.PSID_VA RESTART WITH 351014116;
ALTER SEQUENCE PSPADM.PSID_IN RESTART WITH 318003515;
ALTER SEQUENCE PSPADM.PSID_UT RESTART WITH 549006041;
ALTER SEQUENCE PSPADM.SEQ_TRANSACTION_NUMBER RESTART WITH 470705031;
ALTER SEQUENCE PSPADM.SEQ_ACH_FILE_CTR RESTART WITH 21;
ALTER SEQUENCE PSPADM.PSID_MA RESTART WITH 325008176;
ALTER SEQUENCE PSPADM.PSID_WY RESTART WITH 555001154;
ALTER SEQUENCE PSPADM.PSID_AZ RESTART WITH 504012052;

ALTER SEQUENCE PSPADM.PSID_MO RESTART WITH 429006082;
ALTER SEQUENCE PSPADM.PSID_NV RESTART WITH 632006669;
ALTER SEQUENCE PSPADM.SEQ_USAGE_BILLING_TOKEN RESTART WITH 136457460;
ALTER SEQUENCE PSPADM.SEQ_GEMS_UPLOAD_BATCH_ID RESTART WITH 39345;
ALTER SEQUENCE PSPADM.SEQ_EFTPS_FILE_SEQUENCE RESTART WITH 187084;
ALTER SEQUENCE PSPADM.PSID_DC RESTART WITH 311001625;
ALTER SEQUENCE PSPADM.PSID_NJ RESTART WITH 334009277;
ALTER SEQUENCE PSPADM.PSID_NC RESTART WITH 337012626;
ALTER SEQUENCE PSPADM.PSID_PA RESTART WITH 342010761;
ALTER SEQUENCE PSPADM.SEQ_TEMP_COMPANY RESTART WITH 3352566;
ALTER SEQUENCE PSPADM.PSID_ID RESTART WITH 516003590;

ALTER SEQUENCE PSPADM.PSID_OR RESTART WITH 641008497;
ALTER SEQUENCE PSPADM.SEQ_EE_CALCULATION_TOKEN RESTART WITH 21213987;
ALTER SEQUENCE PSPADM.PSID_NY RESTART WITH 336017872;
ALTER SEQUENCE PSPADM.PSID_OH RESTART WITH 339008670;
ALTER SEQUENCE PSPADM.PSID_FL RESTART WITH 312032596;
ALTER SEQUENCE PSPADM.PSID_VT RESTART WITH 350001384;
ALTER SEQUENCE PSPADM.TEST RESTART WITH 818;
ALTER SEQUENCE PSPADM.SEQ_SUBSCRIPTION_NUMBER RESTART WITH 9850215;
ALTER SEQUENCE PSPADM.PSID_CT RESTART WITH 309005571;
ALTER SEQUENCE PSPADM.PSID_WA RESTART WITH 652013951;


--non-prod


ALTER SEQUENCE PSPADM.ENTITLEMENT_SEQ RESTART WITH 463003;
ALTER SEQUENCE PSPADM.PSID_AK RESTART WITH 702002044;
ALTER SEQUENCE PSPADM.PSID_AL RESTART WITH 401004970;
ALTER SEQUENCE PSPADM.PSID_AR RESTART WITH 405002398;
ALTER SEQUENCE PSPADM.PSID_AZ RESTART WITH 504012033;
ALTER SEQUENCE PSPADM.PSID_CA RESTART WITH 606121774;
ALTER SEQUENCE PSPADM.PSID_CO RESTART WITH 508013539;
ALTER SEQUENCE PSPADM.PSID_CT RESTART WITH 309005568;
ALTER SEQUENCE PSPADM.PSID_DC RESTART WITH 311001623;
ALTER SEQUENCE PSPADM.PSID_DE RESTART WITH 310001526;
ALTER SEQUENCE PSPADM.PSID_DEFAULT RESTART WITH 999108348;

ALTER SEQUENCE PSPADM.PSID_FL RESTART WITH 312032558;
ALTER SEQUENCE PSPADM.PSID_GA RESTART WITH 313013612;
ALTER SEQUENCE PSPADM.PSID_HI RESTART WITH 715002683;
ALTER SEQUENCE PSPADM.PSID_IA RESTART WITH 419002572;
ALTER SEQUENCE PSPADM.PSID_ID RESTART WITH 516003583;
ALTER SEQUENCE PSPADM.PSID_IL RESTART WITH 417015224;
ALTER SEQUENCE PSPADM.PSID_IN RESTART WITH 318003515;
ALTER SEQUENCE PSPADM.PSID_KS RESTART WITH 420003203;
ALTER SEQUENCE PSPADM.PSID_KY RESTART WITH 321003102;
ALTER SEQUENCE PSPADM.PSID_LA RESTART WITH 422005505;
ALTER SEQUENCE PSPADM.PSID_MA RESTART WITH 325008174;

ALTER SEQUENCE PSPADM.PSID_MD RESTART WITH 324010075;
ALTER SEQUENCE PSPADM.PSID_ME RESTART WITH 323001625;
ALTER SEQUENCE PSPADM.PSID_MI RESTART WITH 326009940;
ALTER SEQUENCE PSPADM.PSID_MN RESTART WITH 427007151;
ALTER SEQUENCE PSPADM.PSID_MO RESTART WITH 429006072;
ALTER SEQUENCE PSPADM.PSID_MS RESTART WITH 428002331;
ALTER SEQUENCE PSPADM.PSID_MT RESTART WITH 530002073;
ALTER SEQUENCE PSPADM.PSID_NC RESTART WITH 337012612;
ALTER SEQUENCE PSPADM.PSID_ND RESTART WITH 438000801;
ALTER SEQUENCE PSPADM.PSID_NE RESTART WITH 431001983;
ALTER SEQUENCE PSPADM.PSID_NH RESTART WITH 333002480;

ALTER SEQUENCE PSPADM.PSID_NJ RESTART WITH 334009268;
ALTER SEQUENCE PSPADM.PSID_NM RESTART WITH 535003025;
ALTER SEQUENCE PSPADM.PSID_NV RESTART WITH 632006663;
ALTER SEQUENCE PSPADM.PSID_NY RESTART WITH 336017856;
ALTER SEQUENCE PSPADM.PSID_OH RESTART WITH 339008656;
ALTER SEQUENCE PSPADM.PSID_OK RESTART WITH 440003949;
ALTER SEQUENCE PSPADM.PSID_OR RESTART WITH 641008491;
ALTER SEQUENCE PSPADM.PSID_PA RESTART WITH 342010744;
ALTER SEQUENCE PSPADM.PSID_RI RESTART WITH 344000982;
ALTER SEQUENCE PSPADM.PSID_SC RESTART WITH 345005430;
ALTER SEQUENCE PSPADM.PSID_SD RESTART WITH 446000912;

ALTER SEQUENCE PSPADM.PSID_TN RESTART WITH 347000666;
ALTER SEQUENCE PSPADM.PSID_TX RESTART WITH 448038274;
ALTER SEQUENCE PSPADM.PSID_UT RESTART WITH 549006038;
ALTER SEQUENCE PSPADM.PSID_VA RESTART WITH 351014098;
ALTER SEQUENCE PSPADM.PSID_VT RESTART WITH 350001384;
ALTER SEQUENCE PSPADM.PSID_WA RESTART WITH 652013932;
ALTER SEQUENCE PSPADM.PSID_WI RESTART WITH 454005833;
ALTER SEQUENCE PSPADM.PSID_WV RESTART WITH 353001548;
ALTER SEQUENCE PSPADM.PSID_WY RESTART WITH 555001152;
ALTER SEQUENCE PSPADM.SEQ_401K_SIGNUP_BATCH_ID RESTART WITH 553;
ALTER SEQUENCE PSPADM.SEQ_401K_UPLOAD_BATCH_ID RESTART WITH 1316;

ALTER SEQUENCE PSPADM.SEQ_ACH_FILE_CTR RESTART WITH 21;
ALTER SEQUENCE PSPADM.SEQ_ASST_USAGE_BILLING_TOKEN RESTART WITH 129968;
ALTER SEQUENCE PSPADM.SEQ_ATF_BATCH_ID_NBR RESTART WITH 9911;
ALTER SEQUENCE PSPADM.SEQ_EE_CALCULATION_TOKEN RESTART WITH 20573919;
ALTER SEQUENCE PSPADM.SEQ_EE_PITEM_CALC_TOKEN RESTART WITH 1280140;
ALTER SEQUENCE PSPADM.SEQ_EFTPS_FILE_SEQUENCE RESTART WITH 175993;
ALTER SEQUENCE PSPADM.SEQ_EFTPS_PAYMENT_SEQUENCE RESTART WITH 18952751;
ALTER SEQUENCE PSPADM.SEQ_EFTPS_SEGMENT_SEQUENCE RESTART WITH 376372;
ALTER SEQUENCE PSPADM.SEQ_GEMS_UPLOAD_BATCH_ID RESTART WITH 37907;
ALTER SEQUENCE PSPADM.SEQ_PSID RESTART WITH 99900509;
ALTER SEQUENCE PSPADM.SEQ_QBDT_SOURCE_COMPANY_ID RESTART WITH 109023959;

ALTER SEQUENCE PSPADM.SEQ_SUBSCRIPTION_NUMBER RESTART WITH 9987618;
ALTER SEQUENCE PSPADM.SEQ_TAX_ACC_AUD RESTART WITH 28621;
ALTER SEQUENCE PSPADM.SEQ_TEMP_COMPANY RESTART WITH 3352566;
ALTER SEQUENCE PSPADM.SEQ_TEST RESTART WITH 22;
ALTER SEQUENCE PSPADM.SEQ_TRACE_NBR RESTART WITH 16468000000000;
ALTER SEQUENCE PSPADM.SEQ_TRACE_NUMBER RESTART WITH 2128685950204;
ALTER SEQUENCE PSPADM.SEQ_TRANSACTION_NUMBER RESTART WITH 415875773;
ALTER SEQUENCE PSPADM.SEQ_TXN_TOKEN_NBR RESTART WITH 7214474;
ALTER SEQUENCE PSPADM.SEQ_USAGE_BILLING_TOKEN RESTART WITH 128343924;
ALTER SEQUENCE PSPADM.TEST RESTART WITH 777;
ALTER SEQUENCE PSPADM.TEST1 RESTART WITH 88;








--old 
SQLNET.ENCRYPTION_SERVER  REQUIRED
SQLNET.ENCRYPTION_CLIENT  REQUESTED

--new
SQLNET.ENCRYPTION_SERVER  REQUIRED
SQLNET.ENCRYPTION_CLIENT  REQUIRED


aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-west-2   --option-group-name  oracle-ee-19-qa-clusterdb-vpc-2 --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUESTED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately

aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-west-2   --option-group-name   oracle-ee-19-e2e-clusterdb-vpc-2 --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUESTED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately

aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-west-2   --option-group-name   oracle-ee-19-prf-clusterdb-vpc-2 --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUESTED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately

aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-west-2   --option-group-name  oracle-ee-19-qa-clusterdb-vpc-2 --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUESTED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately

aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-west-2   --option-group-name  oracle-ee-19-qa-clusterdb-vpc-2 --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUESTED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately








oracle-ee-19-e2e-clusterdb-psp-west2
oracle-ee-19-e2e-clusterdb-vpc-2
oracle-ee-19-prf-clusterdb-vpc-2
--oracle-ee-19-qa-clusterdb-vpc-2
--oracle-ee-19-qa-clusterdb-vpc-2-tz


aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-east-2   --option-group-name  oracle-ee-19-e2e-clusterdb-vpc-2 --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately




aws rds add-option-to-option-group   --profile sbg-psp-ppd --region us-east-2   --option-group-name  dummy --options '[{"OptionSettings":[{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]'   --apply-immediately









aws rds add-option-to-option-group --profile sbg-psp-ppd --region us-west-2 \
--option-group-name oracle-ee-19-prf-clusterdb-vpc-2 \
--options '[{"OptionSettings":[{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_CLIENT","Value":"SHA512,SHA384,SHA256,SHA1,MD5"},{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_SERVER","Value":"SHA1,MD5"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]' \
--apply-immediately

aws rds add-option-to-option-group --profile sbg-psp-ppd --region us-west-2 \
--option-group-name oracle-ee-19-e2e-clusterdb-vpc-2 \
--options '[{"OptionSettings":[{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_CLIENT","Value":"SHA512,SHA384,SHA256,SHA1,MD5"},{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_SERVER","Value":"SHA1,MD5"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]' \
--apply-immediately


aws rds add-option-to-option-group --profile sbg-psp-ppd --region us-west-2 \
--option-group-name oracle-ee-19-e2e-clusterdb-psp-west2 \
--options '[{"OptionSettings":[{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_CLIENT","Value":"SHA512,SHA384,SHA256,SHA1,MD5"},{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_SERVER","Value":"SHA1,MD5"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]' \
--apply-immediately



aws rds add-option-to-option-group --profile sbg-psp-ppd --region us-east-2 \
--option-group-name dummy \
--options '[{"OptionSettings":[{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_CLIENT","Value":"SHA512,SHA384,SHA256,SHA1,MD5"},{"Name":"SQLNET.CRYPTO_CHECKSUM_TYPES_SERVER","Value":"SHA1,MD5"},{"Name":"SQLNET.ENCRYPTION_CLIENT","Value":"REQUIRED"},{"Name":"SQLNET.ENCRYPTION_SERVER","Value":"REQUIRED"}],"OptionName":"NATIVE_NETWORK_ENCRYPTION"}]' \
--apply-immediately


CREATE INDEX PSP_INDIVIDUAL_FIDX ON PSP_INDIVIDUAL (LOWER(EMAIL));
CREATE INDEX PSP_COMPANY_FIDX ON PSP_COMPANY (LOWER(LEGAL_NAME));



exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');

CREATE INDEX PSPADM.IDX_INDIVIDUAL_EMAIL ON PSPADM.PSP_INDIVIDUAL (EMAIL)  online parallel (degree 16) TABLESPACE PSP_IDX02;
alter index PSPADM.PSP_INDIVIDUAL_FIDX NOPARALLEL;

CREATE INDEX PSPADM.IDX_COMPANY_LEGAL_NAME ON PSPADM.PSP_COMPANY (LEGAL_NAME)  online parallel (degree 16) TABLESPACE PSP_IDX02;
alter index PSPADM.PSP_COMPANY_FIDX NOPARALLEL;

exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');


select dbms_metadata.get_ddl('TABLE', 'PSP_COMPANY')
from dba_table;


select di.table_name,di.index_name,di.index_type,di.status from dba_indexes di
where di.owner = 'PSPADM' and di.index_type like '%FUNCTION-BASED%'; 

SELECT DBMS_METADATA.get_ddl ('TABLE','PSP_COMPANY' , 'PSPADM')
FROM all_tables;
WHERE owner = UPPER('&1');


--PDS east
ppdspg01=> set search_path to pspadm;
SET
ppdspg01=> CREATE EXTENSION dblink schema pspadm;
CREATE EXTENSION
ppdspg01=>  GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_pds2_pspapp;
GRANT
ppdspg01=> GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_pds2_pspapp;
GRANT
ppdspg01=> GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_pds2_pspapp;
GRANT
ppdspg01=> GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO psp_pds2_pspapp;
GRANT
ppdspg01=> GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO psp_pds2_pspapp;
GRANT
ppdspg01=> GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO psp_pds2_pspapp;
GRANT
ppdspg01=> CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr 'ppsp-pds-ue01.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com',port '5432', dbname 'ppdspg01');
CREATE SERVER
ppdspg01=> CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'psp_pds2_pspapp', password 'Pzu#JA7M55$%');
CREATE USER MAPPING
--Staging
pitparmo=> set search_path to pspadm;
SET
pitparmo=> CREATE EXTENSION dblink schema pspadm;
CREATE EXTENSION
pitparmo=> GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_prl_app;
GRANT
pitparmo=> GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_prl_app;
GRANT
pitparmo=> GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO psp_prl_app;
GRANT
pitparmo=> GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO psp_prl_app;
GRANT
pitparmo=> GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO psp_prl_app;
GRANT
pitparmo=> CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr 'ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com',port '6543', dbname 'pitparmo');
CREATE SERVER
pitparmo=> CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'psp_prl_app', password 'ViqB#N4uLG)a');
CREATE USER MAPPING



  ppdspg01-ppsphp01-pspadm-task1  dms-replication-instance1 arn:aws:dms:us-west-2:152430470825:task:TS5IIDUH6UNOZB45IPEGK3AQV755M7CZIDQBIEQ
  ppdspg01-ppsphp01-pspadm-task2  dms-replication-instance1 arn:aws:dms:us-west-2:152430470825:task:2U72LSA2HSG3XWDDDC7FVBDYHCYI4OQSH7MMM6A
  ppdspg01-ppsphp01-pspadm-task3  dms-replication-instance1 arn:aws:dms:us-west-2:152430470825:task:7O5L5E7EHKYVDOGQ7AXL4DIZB2NNRRAJETF337Q
  ppdspg01-ppsphp01-pspadm-task4  dms-replication-instance2 arn:aws:dms:us-west-2:152430470825:task:B3JZIBPBTGCFDCINW5R2VJB4LAP3K3MZZKXOQQY
  ppdspg01-ppsphp01-pspadm-task5  dms-replication-instance2 arn:aws:dms:us-west-2:152430470825:task:JFCIZHKL5IMAMXVZX5WJQ6Z6IYO4I36EPECWVRY
  ppdspg01-ppsphp01-pspadm-task6  dms-replication-instance2 arn:aws:dms:us-west-2:152430470825:task:KES7GXOLDUD5K3S7XBC4ZFH47MLHNPVZGRZGNTQ
  ppdspg01-ppsphp01-pspadm-task7  dms-replication-instance-sys  arn:aws:dms:us-west-2:152430470825:task:HYEHME6ZLFSSZ7VBUQ2K7IXFTAUDIUWCBFFIETI
  ppdspg01-ppsphp01-pspadm-task8  dms-replication-instance-sys  arn:aws:dms:us-west-2:152430470825:task:AH2SN2W652FN2SAWRDWOJEA73B7LTI3IBXEIERQ







  
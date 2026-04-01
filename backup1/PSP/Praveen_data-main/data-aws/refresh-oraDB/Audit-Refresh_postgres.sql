psql -h psp-par-aud-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 6543 -U postgres
-- HV32pts0PUD=4lWpicaRyz^og-GKz1

SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin')
group by db,usename,machine;



alter database prodapgib rename to parapgib;
 
--connect to staging database as below
\c parapgib
 
create user ibob_prl_pspapp password 'Vp3zu#JA7M5)a';
 
--if user already exists then execute below to allow login and change password
alter user ibob_prl_pspapp login password 'Vp3zu#JA7M5)a';
 
grant connect on database parapgib to ibob_prl_pspapp;
grant usage on schema ibobadm to ibob_prl_pspapp;
grant all on all tables in schema ibobadm  to ibob_prl_pspapp;
grant all on all sequences in schema ibobadm to ibob_prl_pspapp;
grant all on all functions in schema ibobadm to ibob_prl_pspapp;
alter default privileges in schema ibobadm grant all on tables to ibob_prl_pspapp;
alter default privileges in schema ibobadm grant all on sequences to ibob_prl_pspapp;
alter default privileges in schema ibobadm grant all on functions to ibob_prl_pspapp;
alter user ibob_prl_pspapp set search_path to ibobadm;


alter user ibobadm_owner with NOLOGIN;
alter user ibob_prod_pspapp with NOLOGIN;
alter user ibobadm_readonly with NOLOGIN;


ALTER role ibob_prl_pspapp SET max_parallel_workers_per_gather TO 16;


select rolname,rolcanlogin
from pg_roles
where rolname in ('ibobadm_owner','ibob_prod_pspapp','ibobadm_readonly');



alter user ADSC_RO login password 'Pd#T5lf5u';

psql -h psp-par-aud.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 6543 -U postgres -d parapgib


ssh -t ip-10-77-34-55.us-east-2.compute.internal  -J ec2-3-130-125-83.us-east-2.compute.amazonaws.com "sudo su - oracle";

INTU_LICENSING

INp3zu#JA7M5)p


INTU_LICENSING/"INp3zu#JA7M5)p"@'psphpp02.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521/psphpp02'

Step 1: add RDS instance name to EC2
intuit:licensing:rdsDBIdentifier 


--step 2 create user on RDS
create user INTU_LICENSING identified by "INp3zu#JA7M5)p" profile APPLICATION_HIGH_RISK_PROFILE;
grant create session to INTU_LICENSING;
execute rdsadmin.rdsadmin_util.grant_sys_object('V_$OPTION', 'INTU_LICENSING','SELECT',false);
 
execute rdsadmin.rdsadmin_util.grant_sys_object('V_$VERSION', 'INTU_LICENSING','SELECT',false);
 
execute rdsadmin.rdsadmin_util.grant_sys_object('DBA_FEATURE_USAGE_STATISTICS', 'INTU_LICENSING','SELECT',false);

--step 3:
Create a AWS Secrets Manager Entry for your RDS / EC2 Database

oracle_licensing_pspvd1pt


oracle_licensing_psphpp02

oracle secrete for RDS database pspvd1pt for oracle  license data gathering

we have completed all steps for both the JIRA’s. https://jira.intuit.com/browse/QBWG-166474 and https://jira.intuit.com/browse/QBWG-166473
Most of the db instances that are shown in the violations are replicas of each other. have posted same in #acp-ticketing-help  channel but no one responded till now.
I can see in violations only replica db instances only.



we have followed remediation steps and completed all.
Most of the db instances that are shown in the violations are replicas of each other.can you please help me to mark replica db instances to completed.

Replica db instances:
arn:aws:rds:us-east-2:152430470825:db:ppspsp01 
arn:aws:rds:us-west-2:152430470825:db:ppsphpdg 

have posted same in #acp-ticketing-help  channel but no one responded till now.

pspe2eue,psphpue,pspe2eme,ppspsp01

pspsys02--ppspo2p1
    
pspsys01--ppspo2p2
    
psppfib1
    
--psppf501--ibob
    
ppsphp05
    
--ppsphp01---501 instance
    
ppsphpdg
    
parsysmo

we have done for below primary db instances:
arn:aws:rds:us-west-2:893547637742:db:psphpp02
arn:aws:rds:us-west-2:893547637742:db:pspvd1pt 



we have followed remediation steps and completed all .
Most of the db instances that are shown in the violations are replicas of each other.can you please help me to mark replica db instances to completed. 

List of Replica db instances:
arn:aws:rds:us-west-2:893547637742:db:psphpp05
arn:aws:rds:us-west-2:893547637742:db:psphdg01 
arn:aws:rds:us-west-2:893547637742:db:pspparhdg
arn:aws:rds:us-west-2:893547637742:db:psphdg02
arn:aws:rds:us-west-2:893547637742:db:pspparhs
arn:aws:rds:us-east-2:893547637742:db:psphsp02  
 



arn:aws:rds:us-east-2:152430470825:db:pspe2eue 
arn:aws:rds:us-west-2:152430470825:db:parsysmo 
arn:aws:rds:us-west-2:152430470825:db:psppfib1 
arn:aws:rds:us-west-2:152430470825:db:pspsys02
arn:aws:rds:us-west-2:152430470825:db:psppf501
arn:aws:rds:us-west-2:152430470825:db:pspsys01
arn:aws:rds:us-west-2:152430470825:db:ppsphp05 
arn:aws:rds:us-east-2:152430470825:db:ppspsp01
arn:aws:rds:us-west-2:152430470825:db:ppsphp01
arn:aws:rds:us-west-2:152430470825:db:ppsphpdg 
arn:aws:rds:us-east-2:152430470825:db:psphpue
arn:aws:rds:us-east-2:152430470825:db:pspe2eme 



SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin','postgres')
group by db,usename,machine;


select * from pspadm.gg_heartbeat;

us-west:

psql -h ppsp-pds-db.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -U postgres -p 5432 -d pdsibobdb
-- .4xSM0vHYTlP^nftK0tvv1Y.j5kZBg

psql -h ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 -U postgres -d ppdspg02
-- changeme




--us-east
psql -h ppsp-pds-uw02dr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com -U postgres -p 5432 -d ppdspg02

psql -h ppsp-pds-dbdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com -U postgres -p 5432 -d pdsibobdb








Failover priority correction needed for Monolith DB


Install pg_proctab on audit DB

prodapgib=> CREATE EXTENSION pg_proctab;
CREATE EXTENSION
prodapgib=> SELECT * FROM pg_proctab();






ssh -o ServerAliveInterval=45 -i ~/.ssh/id_rsa -L 127.0.0.1:11538:psphdg01.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521 pnarlagalla@ec2-52-38-226-158.us-west-2.compute.amazonaws.com



select count(*) from PSPADM.psp_company_event ce inner join PSPADM.psp_company_event_detail ced
                                                     on ced.company_event_fk= ce.company_event_seq
                                                         and ced.company_fk= ced.company_fk
where event_type_cd='OFXServiceActivated' and status_cd='Active'
  and ced.event_detail_type_cd='ServiceCode'
  and ced.value='DirectDeposit'
  and ce.event_time_stamp > to_date('2022-12-20', 'yyyy-mm-dd') 
  and EVENT_TIME_STAMP < to_date('2023-01-07', 'yyyy-mm-dd') ;



staging Postgres Primary (ppsp-stg-pitparmo) and audit (psp-par-aud)  will not be available for 10-15min as part of DB refresh.


select count(*) from PSPADM.psp_company_event ce inner join PSPADM.psp_company_event_detail ced
                                                     on ced.company_event_fk= ce.company_event_seq
                                                         and ced.company_fk= ced.company_fk
where event_type_cd='OFXServiceActivated' and status_cd='Active'
  and ced.event_detail_type_cd='ServiceCode'
  and ced.value='DirectDeposit'
  and ce.event_time_stamp < to_date('2023-12-20', 'yyyy-mm-dd') 
  and EVENT_TIME_STAMP > to_date('2024-01-07', 'yyyy-mm-dd') ;

\timing;

vacuum (analyze, verbose) pspadm.psp_nachafile                                    ;
vacuum (analyze, verbose) pspadm.psp_rafenrollment                                ;
vacuum (analyze, verbose) pspadm.psp_auth_user                                    ;
vacuum (analyze, verbose) pg_shdepend                                      ;
vacuum (analyze, verbose) pspadm.psp_sql_execution_log_entry                      ;
vacuum (analyze, verbose) pspadm.psp_gems_upload_batch                            ;
vacuum (analyze, verbose) pspadm.psp_source_system_transmission_m112014_from_qbdt ;
vacuum (analyze, verbose) pspadm.psp_company_agency                               ;
vacuum (analyze, verbose) pspadm.psp_bill                                         ;
vacuum (analyze, verbose) pspadm.psp_company_law                                  ;
vacuum (analyze, verbose) pspadm.psp_on_hold_reason                               ;
vacuum (analyze, verbose) pspadm.psp_entitlement_unit                             ;
vacuum (analyze, verbose) pspadm.psp_bank_account                                 ;
vacuum (analyze, verbose) pspadm.psp_company_law_rate                             ;
vacuum (analyze, verbose) pspadm.psp_tax_payment_on_hold_reason                   ;
vacuum (analyze, verbose) pspadm.psp_qbdt_payroll_item_info                       ;
vacuum (analyze, verbose) pspadm.psp_company_payroll_item                         ;
vacuum (analyze, verbose) pspadm.psp_company_pin                                  ;
vacuum (analyze, verbose) pspadm.psp_source_system_transmission_m082014_from_qbdt ;
vacuum (analyze, verbose) pspadm.psp_tax_company_service_info                     ;
vacuum (analyze, verbose) pspadm.psp_auth_user_auth_role__assoc                   ;
vacuum (analyze, verbose) pspadm.psp_ledger_operation                             ;
vacuum (analyze, verbose) pspadm.psp_ddcompany_service_info                       ;
vacuum (analyze, verbose) pspadm.psp_comp_pmttemplate_pmtmethod                   ;
vacuum (analyze, verbose) pspadm.psp_ledger_operation_job                         ;
vacuum (analyze, verbose) pspadm.psp_payroll_item_taxable_to                      ;
vacuum (analyze, verbose) pspadm.psp_entitlement                                  ;
vacuum (analyze, verbose) pspadm.psp_address                                      ;
vacuum (analyze, verbose) pspadm.psp_employee_wage_plan                           ;
vacuum (analyze, verbose) pspadm.psp_fraud_bank_account                           ;
vacuum (analyze, verbose) pspadm.psp_companyagency_pmttemplate                    ;
vacuum (analyze, verbose) pspadm.psp_source_system_transmission_m062014_from_qbdt ;
vacuum (analyze, verbose) pspadm.psp_employee_accrual                             ;
vacuum (analyze, verbose) pg_depend                                        ;
vacuum (analyze, verbose) pg_class                                         ;
vacuum (analyze, verbose) pspadm.psp_source_system_transmission_m092018_from_ews  ;
vacuum (analyze, verbose) pspadm.psp_failed_payroll_run                           ;
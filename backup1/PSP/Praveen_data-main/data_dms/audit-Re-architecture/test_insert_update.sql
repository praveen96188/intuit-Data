select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where usename like '%data_capture_role%');



SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid() and usename not in ('postgres','rdsadmin','dms_apg_src','dms_apg_tgt')
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by usename;


                                                                                                                                  


psp_hcm401k_company_qbdt_pitem
psp_hcm401k_company_policy
psp_hcm401k_employee_deduction  
psp_hcm401k_policy





insert into psp_hcm401k_policy (hcm401k_policy_seq,version,created_date,modified_date,realm_id) values('test1',1,current_timestamp,current_timestamp,-1);
update psp_hcm401k_policy set version=2 where hcm401k_policy_seq='test1';

insert into psp_hcm401k_company_policy (hcm401k_company_policy_seq,version,created_date,modified_date,realm_id,hcm401k_policy_fk) values('test1',1,current_timestamp,current_timestamp,-1,'test1');
update psp_hcm401k_company_policy set version=2 where hcm401k_company_policy_seq='test1';

insert into psp_hcm401k_company_qbdt_pitem (hcm401k_company_qbdt_pitem_seq,version,created_date,modified_date,realm_id,hcm401k_company_policy_fk) values ('test1',1,current_timestamp,current_timestamp,-1,'test1');
update psp_hcm401k_company_qbdt_pitem set version=2 where hcm401k_company_qbdt_pitem_seq='test1';


insert into psp_hcm401k_employee_deduction (hcm401k_employee_deduction_seq,version,created_date,modified_date,realm_id,hcm401k_company_policy_fk) values ('test1',1,current_timestamp,current_timestamp,-1,'test1');
update psp_hcm401k_employee_deduction set version=2 where hcm401k_employee_deduction_seq='test1';

delete from  psp_hcm401k_employee_deduction where hcm401k_employee_deduction_seq='test1';
delete from  psp_hcm401k_company_qbdt_pitem where hcm401k_company_qbdt_pitem_seq='test1';
delete from  psp_hcm401k_company_policy where hcm401k_company_policy_seq='test1';
delete from  psp_hcm401k_policy where hcm401k_policy_seq='test1';

insert into psp_qbdt_request_info (qbdt_request_info_seq,version,realm_id,company_fk) values ('test2',1,-1,'test3');
update psp_qbdt_request_info set version=2 where qbdt_request_info_seq='test1';
delete from psp_qbdt_request_info where qbdt_request_info_seq='test1';




truncate table pspadm.psp_hcm401k_employee_deduction;
truncate table pspadm.psp_hcm401k_company_qbdt_pitem;
truncate table pspadm.psp_hcm401k_company_policy cascade;
truncate table pspadm.psp_hcm401k_policy cascade;

insert into ibobadm.psp_qbdt_request_info (qbdt_request_info_seq,version,realm_id,company_fk, created_date) values ('test5',1,-1,'test6', current_timestamp);
update psp_qbdt_request_info set version=2 where qbdt_request_info_seq='test1';
delete from psp_qbdt_request_info where qbdt_request_info_seq='test1';

select count(*) from psp_hcm401k_company_qbdt_pitem;
select count(*) from psp_hcm401k_company_policy;
select count(*) from psp_hcm401k_employee_deduction;  
select count(*) from psp_hcm401k_policy;
select count(*) from psp_qbdt_request_info; 




*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh prodapgib-pspapg02-ibobadm-pspadm-hcm-fullload-cdc 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh prodapgib-pspapg02-ibobadm-pspadm-qri-fullload-cdc 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh prodapgib-pspapg02-ibobadm-pspadm-hcm-val-cdc 1>/dev/null 2>&1

postgresql://ppsp-sys-mon.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432/psyspg01


ALTER TABLE ibobadm.gg_heartbeat ADD PRIMARY KEY (source);


select count(*)
              FROM pg_stat_activity
              where query like '%QbdtEmployeeInfo:findEmployeesWithGreaterToken:47%' and state='active';

alter user psp_payroll_dm  with NOLOGIN;   
alter user pspapp   with NOLOGIN;              
alter user pspbatch_ro_user  with NOLOGIN; 
alter user pspbatch_rw_user with NOLOGIN;          
alter user pspread  with NOLOGIN;           
alter user psprjf   with NOLOGIN;



After enabled  the feature flag yesterday ,
token query running from more than 200+ connections and free local storage slowly comingdown
query running from last 20hours.


SELECT datname,Usename,application_name,client_hostname, pid, state, age(clock_timestamp(), query_start) AS age ,query
FROM pg_stat_activity
WHERE state = 'active'  
ORDER BY age DESC;


SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid() and usename not in ('postgres','rdsadmin','ggs','postgresi')
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by count(*) desc;


SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'pitparmo' and pg_stat_activity.usename='psp_prl_app'
  AND pid <> pg_backend_pid();


select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where query like '%QbdtEmployeeInfo:findEmployeesWithGreaterToken:47%')  AND usename  in ('psp_prl_app') ;


select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where usename like '%dms_apg_user%');

select count(*)
              FROM pg_stat_activity
              where query like '%QbdtEmployeeInfo:findEmployeesWithGreaterToken:47%' and state='active';











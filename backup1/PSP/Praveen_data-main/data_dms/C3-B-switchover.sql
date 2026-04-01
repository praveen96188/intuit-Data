psql -h ppsp-pds-uw01.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 -U postgres -d ppdspg01

 --pspadm_owner
 --Pp3zu#JA7M5

--lock app users
alter user pspadm_owner with NOLOGIN;
alter user pspapp with NOLOGIN;
alter user psprjf with NOLOGIN;
--verify
select rolname,rolcanlogin from pg_roles where rolname in ('pspapp','pspadm_owner','psprjf');

--connections
SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin','postgres','dms_apg_src')
group by db,usename,machine;


select rolname,rolcanlogin from pg_roles where rolname in ('pspadm_owner');

intuadmin/"KVadPpU3q#(7eC"@'psphpp02.sbg-psp-prod.a.intuit.com:1521/psphpp02'


alter user pspadm_owner with LOGIN;

\i Disable_triggers_onC2.sql

\i verify_Trigger_status_onC2.sql


\i Post_Oracle_Sequece_reset.sql

alter user pspadm_owner with NOLOGIN;


@Seq_reset_onB.sql

select rolname,rolcanlogin from pg_roles where rolname in ('pspadm_owner');


SELECT * FROM pg_class, pg_index WHERE pg_index.indisvalid = false AND pg_index.indexrelid = pg_class.oid;



select pid, 
       usename, 
       pg_blocking_pids(pid) as blocked_by, 
       query as blocked_query
from pg_stat_activity
where cardinality(pg_blocking_pids(pid)) > 0;


 select pid, usename,datname,query from pg_stat_activity where pid=13122;

SELECT pg_terminate_backend(19166);


SELECT 
    pg_terminate_backend(25263) 
FROM 
    pg_stat_activity 
WHERE 
    -- don't kill my own connection!
    25263 <> pg_backend_pid()
    -- don't kill the connections to other databases
    AND datname = 'database_name'







--active sessions from application
SELECT current_timestamp,
       datname, pid,leader_pid, usesysid, usename, application_name, backend_type,
       coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,
       wait_event_type, wait_event, query, query_start,
       1000 * EXTRACT(EPOCH FROM (clock_timestamp()-query_start)) as duration
from pg_stat_activity
where state in ('active','idle in transaction') and pid != pg_backend_pid()
and usename not in ('ggs','ggt','dms_apg_src','postgres')
order by query_start desc;














--disable triggers in C3
\timing
set search_path=pspadm;
alter table pspadm.psp_address DISABLE TRIGGER psp_address_at ;
alter table pspadm.psp_bpcompany_service_info DISABLE TRIGGER psp_bpcompany_service_info_at ;
alter table pspadm.psp_comp_pmttemplate_pmtmethod DISABLE TRIGGER psp_cmpmtmplt_pmtmtd_at ;
alter table pspadm.psp_company DISABLE TRIGGER psp_company_at ;
alter table pspadm.psp_company_agency DISABLE TRIGGER psp_company_agency_at ;
alter table pspadm.psp_company_bank_account DISABLE TRIGGER psp_company_bank_account_at ;
alter table pspadm.psp_company_event DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p0 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p1 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p2 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p3 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p4 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p5 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p6 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_event_p7 DISABLE TRIGGER tr_upd_company_event_timestamp ;
alter table pspadm.psp_company_law DISABLE TRIGGER psp_company_law_at ;
alter table pspadm.psp_company_offer DISABLE TRIGGER psp_company_offer_at ;
alter table pspadm.psp_company_payroll_item DISABLE TRIGGER psp_company_payroll_item_at ;
alter table pspadm.psp_company_service DISABLE TRIGGER psp_company_service_at ;
alter table pspadm.psp_companyagency_pmttemplate DISABLE TRIGGER psp_cmpnyagency_pmttplt_at ;
alter table pspadm.psp_contact DISABLE TRIGGER psp_contact_at ;
alter table pspadm.psp_ddcompany_service_info DISABLE TRIGGER psp_ddcompany_service_info_at ;
alter table pspadm.psp_employee DISABLE TRIGGER psp_employee_at ;
alter table pspadm.psp_entity_change DISABLE TRIGGER psp_entity_change_at ;
alter table pspadm.psp_individual DISABLE TRIGGER psp_individual_at ;
alter table pspadm.psp_money_movement_transaction DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p0 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p1 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p2 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p3 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p4 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p5 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p6 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_money_movement_transaction_p7 DISABLE TRIGGER psp_money_mvmt_trans_at ;
alter table pspadm.psp_property_audit DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p0 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p1 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p2 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p3 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p4 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p5 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p6 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_property_audit_p7 DISABLE TRIGGER tr_upd_dd_limits ;
alter table pspadm.psp_quickbooks_info DISABLE TRIGGER psp_quickbooks_info_at ;
alter table pspadm.psp_tax_company_service_info DISABLE TRIGGER psp_tax_cs_info_at ;
alter table pspadm.psp_tax_penalty_interest DISABLE TRIGGER psp_tax_penalty_interest_at ;


 select last_value+1 from pspadm.psid_ak;
 select last_value+1 from pspadm.psid_al;
 select last_value+1 from pspadm.psid_ar;
 select last_value+1 from pspadm.psid_az;
 select last_value+1 from pspadm.psid_ca;
 select last_value+1 from pspadm.psid_co;
 select last_value+1 from pspadm.psid_ct;
 select last_value+1 from pspadm.psid_dc;
 select last_value+1 from pspadm.psid_de;
 select last_value+1 from pspadm.psid_default;
 select last_value+1 from pspadm.psid_fl;
 select last_value+1 from pspadm.psid_ga;
 select last_value+1 from pspadm.psid_hi;
 select last_value+1 from pspadm.psid_ia;
 select last_value+1 from pspadm.psid_id;
 select last_value+1 from pspadm.psid_il;
 select last_value+1 from pspadm.psid_in;
 select last_value+1 from pspadm.psid_ks;
 select last_value+1 from pspadm.psid_ky;
 select last_value+1 from pspadm.psid_la;
 select last_value+1 from pspadm.psid_ma;
 select last_value+1 from pspadm.psid_md;
 select last_value+1 from pspadm.psid_me;
 select last_value+1 from pspadm.psid_mi;
 select last_value+1 from pspadm.psid_mn;
 select last_value+1 from pspadm.seq_atf_batch_id_nbr;
 select last_value+1 from pspadm.psid_mo;
 select last_value+1 from pspadm.psid_ms;
 select last_value+1 from pspadm.psid_mt;
 select last_value+1 from pspadm.psid_nc;
 select last_value+1 from pspadm.psid_nd;
 select last_value+1 from pspadm.psid_ne;
 select last_value+1 from pspadm.psid_nh;
 select last_value+1 from pspadm.psid_nj;
 select last_value+1 from pspadm.psid_nm;
 select last_value+1 from pspadm.psid_nv;
 select last_value+1 from pspadm.psid_ny;
 select last_value+1 from pspadm.psid_oh;
 select last_value+1 from pspadm.psid_ok;
 select last_value+1 from pspadm.psid_or;
 select last_value+1 from pspadm.psid_pa;
 select last_value+1 from pspadm.psid_ri;
 select last_value+1 from pspadm.psid_sc;
 select last_value+1 from pspadm.psid_sd;
 select last_value+1 from pspadm.psid_tn;
 select last_value+1 from pspadm.psid_tx;
 select last_value+1 from pspadm.psid_ut;
 select last_value+1 from pspadm.psid_va;
 select last_value+1 from pspadm.psid_vt;
 select last_value+1 from pspadm.psid_wa;
 select last_value+1 from pspadm.psid_wi;
 select last_value+1 from pspadm.psid_wv;
 select last_value+1 from pspadm.psid_wy;
 select last_value+1 from pspadm.seq_401k_signup_batch_id;
 select last_value+1 from pspadm.seq_401k_upload_batch_id;
 select last_value+1 from pspadm.seq_ach_file_ctr;
 select last_value+1 from pspadm.seq_asst_usage_billing_token;
 select last_value+1 from pspadm.seq_ee_calculation_token;
 select last_value+1 from pspadm.seq_ee_pitem_calc_token;
 select last_value+1 from pspadm.seq_eftps_file_sequence;
 select last_value+1 from pspadm.seq_eftps_payment_sequence;
 select last_value+1 from pspadm.seq_eftps_segment_sequence;
 select last_value+1 from pspadm.seq_gems_upload_batch_id;
 select last_value+1 from pspadm.seq_qbdt_source_company_id;
 select last_value+1 from pspadm.seq_subscription_number;
 select last_value+1 from pspadm.seq_trace_nbr;
 select last_value+1 from pspadm.seq_trace_number;
 select last_value+1 from pspadm.seq_transaction_number;
 select last_value+1 from pspadm.seq_txn_token_nbr;
 select last_value+1 from pspadm.seq_usage_billing_token;


--create DMS replication tasks on B-C3 
--enable triggers and FK on B
select count(*) from DBA_TRIGGERS where owner='PSPADM'  and status='ENABLED';
select COUNT(*) from dba_constraints where owner='PSPADM' and constraint_type='R' and status='ENABLED';

select 'ALTER TRIGGER PSPADM.' || TRIGGER_NAME || 'ENABLE;' from DBA_TRIGGERS where owner='PSPADM'  and status='DISABLED';
select 'alter table ' ||  owner || '.' || table_name || ' enable constraint ' || constraint_name || ';' from dba_constraints  where owner='PSPADM' and constraint_type in ('R');




INTUADMIN/"changeme"@'ppsphp01.sbg-psp-ppd.a.intuit.com:1521/ppsphp01'



spool RESET_SEQUENCES_on_B
set echo on feedback on timing on
-- Setting Tag 2 to disable DDL replication on C2
exec dbms_streams.set_tag (hextoraw(2));



--unlock users on B

Alter user pspadm account unlock;
Alter user pspapp account unlock;

--check connections
set lines 300
col username for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('RDSADMIN','INTUADMIN','SYS','SYSTEM') and type = 'USER'  group by service_name,username,machine order by username,machine;






--Non-prod
ALTER SEQUENCE PSPADM.SEQ_TAX_ACC_AUD RESTART WITH 31178;
psql:alter_seq_hash_post.sql:3: ERROR:  relation "pspadm.seq_tax_acc_aud" does not exist

ALTER SEQUENCE PSPADM.SEQ_PSID RESTART WITH 99900510;
psql:alter_seq_hash_post.sql:9: ERROR:  relation "pspadm.seq_psid" does not exist

ALTER SEQUENCE PSPADM.TEST1 RESTART WITH 89;
psql:alter_seq_hash_post.sql:18: ERROR:  relation "pspadm.test1" does not exist

ALTER SEQUENCE PSPADM.ENTITLEMENT_SEQ RESTART WITH 463004;
psql:alter_seq_hash_post.sql:39: ERROR:  relation "pspadm.entitlement_seq" does not exist


ALTER SEQUENCE PSPADM.SEQ_TEMP_COMPANY RESTART WITH 3352567;
psql:alter_seq_hash_post.sql:67: ERROR:  relation "pspadm.seq_temp_company" does not exist

ALTER SEQUENCE PSPADM.TEST RESTART WITH 819;
psql:alter_seq_hash_post.sql:75: ERROR:  relation "pspadm.test" does not exist


--new reset from 10000
ALTER SEQUENCE PSPADM.SEQ_ATF_BATCH_ID_NBR RESTART WITH 9924;
psql:alter_seq_hash_post.sql:47: ERROR:  RESTART value (9924) cannot be less than MINVALUE (10000)


ALTER SEQUENCE PSPADM.SEQ_ATF_BATCH_ID_NBR RESTART WITH 10000;




C2:
Host: ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port:5432
service:pspapg02
pspadm_owner: p3zu#JA7M5aa
postgres : WpicaRyz^og-GKz1



\i pspadm_check_constraints.sql
\i create_type.sql
\i CreateProcedures.sql
\i CreateFunctions.sql


--pending
\i CreateSequence.sql
@Sequence_Select_From_B.sql

vi seq_alter_seq_onC2.sql

\i seq_alter_seq_onC2.sql

\i CreateTrigger.sql






SET LINESIZE 32000;
SET PAGESIZE 40000;
SET LONG 50000;
SELECT 'ALTER SEQUENCE '||sequence_owner||'.'||sequence_name||' RESTART WITH '||(last_number + 1)||';' as Sequence_reset
FROM all_sequences
WHERE sequence_owner = 'PSPADM';





--Triggers
select distinct trigger_name from information_schema.triggers where trigger_schema='pspadm';

--sequences
SELECT sequence_schema, sequence_name 
FROM information_schema.sequences 
 ORDER BY sequence_name ;
 
 --count 70

SELECT  nspname, proname 
FROM    pg_catalog.pg_namespace  
JOIN    pg_catalog.pg_proc  
ON      pronamespace = pg_namespace.oid 
WHERE   nspname = 'pspadm'
ORDER BY Proname;
--count 62


--lock app users
alter user pspadm_owner with LOGIN;
alter user pspapp with LOGIN;
alter user psprjf with LOGIN;
alter user pspbatch_rw_user with LOGIN;
alter user pspbatch_ro_user with LOGIN;

--verify
select rolname,rolcanlogin from pg_roles where rolname in ('pspapp','pspadm_owner','psprjf','pspbatch_rw_user','pspbatch_ro_user','pspread');

--connections
SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin','postgres','dms_apg_src')
group by db,usename,machine;



--oracle

intuadmin/"KVadPpU3q#(7eC"@'psphpp02.sbg-psp-prod.a.intuit.com:1521/psphpp02'

Alter user pspadm account lock;
Alter user pspapp account lock;

--check connections
set lines 300
col username for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('RDSADMIN','INTUADMIN','SYS','SYSTEM') and type = 'USER'  group by service_name,username,machine order by username,machine;

SELECT 'ALTER SEQUENCE '||sequence_owner||'.'||sequence_name||' RESTART WITH '||(last_number + 1)||';'
FROM all_sequences
WHERE sequence_owner = 'PSPADM';








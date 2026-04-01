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




--staging monolith
working:
psp_prl_app 
pspbatch_rw_user
psp_prl_read
pspbatch_ro_user

Lock:
pspadm_owner
psprjf 

no users:
perf_test: Perf#123
pspval: pspval#123




password incorrect:
data_capture_role: e#xc91kggPQ@34
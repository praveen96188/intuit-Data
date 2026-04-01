\conninfo
select current_user;
select current_database();

UPDATE cron.job SET database = 'pitparmo' WHERE jobid in (select t.jobid from cron.job t);
alter user drcadm_owner with nologin;
alter user pspadm_readonly_role with nologin;
alter user pspadm_readwrite_role with nologin;
alter user pspapp with nologin;
alter user pspread with nologin;
alter user sys_drc_app with nologin;
alter user testusr with nologin;
alter user pspadm_owner with login;

exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
CREATE INDEX PSPADM.PSP_PAYCHECK_i3 ON PSPADM.PSP_PAYCHECK (PAYCHECK_SEQ) local online parallel (degree 8) TABLESPACE PSP_IDX01;
alter index PSPADM.PSP_PAYCHECK_i3 NOPARALLEL;
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');

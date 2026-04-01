--
-- This script will execute all the automatically generated diff scripts
--
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.02.01 to 2.23.02.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

alter session set current_schema=pspadm;

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.023.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.023.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.023.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('aff8dfda-ef3f-4608-9829-c5201d17f5db',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d14ca32d-607c-4d7a-bfea-c41cfca80563',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql


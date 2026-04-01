--
-- This script will execute all the automatically generated diff scripts
--
--------------------------------------------------------------------------------------
-- Upgrading from 1.5 to 2.0.0.0
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.035' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.035' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.035_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.035.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.035_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a657f32a-c933-4273-a7ca-baee35ecd06d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.035','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7dc4dc98-0091-448f-b5b6-c81b6063099b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.035','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.35 to 2.0.0.36
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.036' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.036' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.036_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.036.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.036_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dd88d31f-930a-4ea1-b668-3877cb48563e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.036','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4cd44acf-33e0-41b9-b322-688ed0fcd002',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.036','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.36 to 2.0.0.37
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.037' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.037' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.037_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.037.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.037_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dff8bfc6-c0e2-4c84-9411-32fb6fc1daf0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.037','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('40ffb223-9576-43ce-886f-f6177b7fb213',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.037','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.37 to 2.0.0.38
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.038' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.038' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.038_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.038.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.038_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('017b4400-1562-4dc5-a85b-08d49689a796',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.038','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ccbd3726-d065-44f5-b017-a15fc2354441',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.038','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.38 to 2.0.0.39
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.039' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.039' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.039_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.039.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.039_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8e0f650d-5d56-4fd1-9763-54bd3ef04628',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.039','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1caa6e7f-9734-4237-b35d-d1c1ab3f6f95',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.039','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.39 to 2.0.0.40
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.040' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.040' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.040_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.040.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.040_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8d43b47d-7dfd-47e9-bb75-a56003345e7b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.040','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6bd8578d-0011-4a2b-9c70-54c4899372de',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.040','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.41 to 2.0.0.41
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.041' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.041' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.041_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.041.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.041_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e4bbaf90-9745-418d-b3e3-d37926445e6c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.041','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('235a2628-8782-40a6-8852-5a642a528204',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.041','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.42 to 2.0.0.42
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.042' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.042' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.042_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.042.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.042_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b0667106-1af9-43cb-94ed-d87a519128de',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.042','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fecaca51-d590-4cca-b593-0841c96cfab2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.042','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.43 to 2.0.0.43
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.043' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.043' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.043_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.043.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.043_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('361f1470-3055-4b9a-892b-6d42cc2b4afb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.043','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9fe78298-e02e-413c-93b6-8f4297db8bd3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.043','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.44 to 2.0.0.44
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.044' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.044' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.044_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.044.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.044_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4768f0b0-00c5-46ef-8f4f-72dbc9c1142c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.044','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7de1851e-a10d-49da-8ca4-7f42f4a5349b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.044','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.45 to 2.0.0.45
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.045' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.045' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.045_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.045.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.045_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cea3b208-d277-43ee-b7d8-235790f07082',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.045','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e5838c8a-f295-4347-910f-3bd29a0a2370',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.045','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.46 to 2.0.0.46
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.046' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.046' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.046_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.046.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.046_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e0a6c264-6630-42a1-bfb1-75e9a6ccc716',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.046','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2d3b4b52-9538-42d6-aec8-0c5a3e051c76',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.046','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.47 to 2.0.0.47
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.047' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.047' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.047_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.047.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.047_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('30db7f08-e16b-4e23-b1d8-9d6b87ce5534',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.047','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('36d55bea-d1da-4f4d-b54e-7d816a06a05e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.047','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.48 to 2.0.0.48
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.048' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.048' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.048_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.048.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.048_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('891c23ac-10d6-4d43-908b-db2cbb04310d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.048','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('19399f36-8370-443e-90fd-c8ffe433e4ac',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.048','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.49 to 2.0.0.49
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.049' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.049' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.049_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.049.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.049_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d8fbc82e-1fc0-4c5f-a423-9c7f42a3cad6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.049','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d0b0b9a6-7c3a-494a-822f-cb8ab3c6d406',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.049','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.50 to 2.0.0.50
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.050' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.050' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.050_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.050.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.050_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4b6ed7d1-4da1-416a-9823-c2e53b001342',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.050','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bca24acc-11c6-4839-8d2b-1cbc0b3d409a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.050','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.51 to 2.0.0.51
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.051' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.051' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.051_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.051.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.051_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('43c1d497-531d-4475-b4be-a3e10925f76e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.051','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2d16edfb-b08d-40f6-aae7-54c303cfca24',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.051','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.52 to 2.0.0.52
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.052' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.052' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.052_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.052.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.052_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b239fe5e-c28e-4e69-bcc0-d141322753a9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.052','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e918b658-5505-4568-8387-86359d200147',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.052','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.53 to 2.0.0.53
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.053' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.053' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.053_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.053.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.053_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('40cc4dbe-b6ad-4f9c-a22f-cc83ad716f07',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.053','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a2234e06-de73-49d4-bf4c-c81d0d2f4927',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.053','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.54 to 2.0.0.54
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.054' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.054' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.054_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.054.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.054_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a5e9008f-f1ba-4db9-846d-bbf4db508e11',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.054','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2e247fdb-2cfa-4e13-b89a-c80d0b65a704',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.054','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.55 to 2.0.0.55
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.055' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.055' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.055_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.055.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.055_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('839389ab-0565-4518-b335-11468247b1f7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.055','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8c0fc301-2a8d-4107-88fc-d1ed22743b41',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.055','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.56 to 2.0.0.56
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.056' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.056' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.056_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.056.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.056_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('032c4bbf-5da1-4786-9893-818c89fb431f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.056','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4ff537b8-af0a-498c-a0a9-cebbbe0965ba',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.056','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.57 to 2.0.0.57
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.057' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.057' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.057_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.057.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.057_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3e8335a2-1d55-4c7b-9f9d-340c8f78c0db',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.057','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('88b96c95-c7f6-4065-9956-b366298e3590',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.057','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.58 to 2.0.0.58
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.058' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.058' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.058_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.058.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.058_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ce652a4b-af49-4094-b93f-b7dd92d0ccb6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.058','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3d8c64b5-ea82-48f0-aa9c-5eff4c9b41cc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.058','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.59 to 2.0.0.59
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.059' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.059' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.059_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.059.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.059_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cb49a831-bc30-442e-8a5b-46deddbbb06b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.059','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('50979c2c-ee14-4264-b4b0-bc1d2fa49990',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.059','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.60 to 2.0.0.60
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.060' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.060' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.060_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.060.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.060_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('96fa4fb5-73b1-4e2a-8bfb-e00307a6384a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.060','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e603f48c-0e61-4ab3-809f-005c2a76b632',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.060','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.61 to 2.0.0.61
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.061' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.061' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.061_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.061.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.061_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('655b3568-9db8-412c-8c84-4adb1ee7902d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.061','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('23818b61-f6df-4ea8-8ae8-1949a2649fa7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.061','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.62 to 2.0.0.62
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.062' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.062' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.062_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.062.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.062_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ed99914e-a940-4fc0-9623-8b1e1dc4e488',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.062','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c7089000-3061-432d-b18e-59932e14d14c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.062','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.63 to 2.0.0.63
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.063' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.063' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.063_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.063.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.063_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9bb57c86-0ae8-43f9-98aa-30447e555bef',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.063','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ec1488b6-34b9-442c-bad0-ea5dd349ae16',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.063','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.64 to 2.0.0.64
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.064' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.064' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.064_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.064.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.064_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('23abc6a5-5efd-4492-b6e2-0a8f94f4a28e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.064','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7fdab7b3-9b96-41c8-8da4-941ef7a1a0a5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.064','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.0.65 to 2.0.0.65
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.065' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.065' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.065_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.000.065.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.000.065_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bafbfd82-c3a8-454b-bd92-9df78390f99c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.065','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('39fb7e2b-943c-4c21-b177-e9144ac3dd01',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.000.065','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.0 to 2.1.0.0
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.000' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.000' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.000_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.000.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.000_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ddc4accc-63af-4cc9-a2bb-2e927393eaee',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.000','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('214b4039-2d85-42c0-8ba0-36a7270094b9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.000','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.1 to 2.1.0.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('380a944a-4032-49d4-9fb5-44c824637925',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b3b17ad1-6415-4d4e-9329-0e9d1f1aa1e0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.2 to 2.1.0.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a27f0a8b-0de7-44bd-8ee7-7f20813d6595',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('abf0c663-34bb-49d8-889e-a14cfc78cd89',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.3 to 2.1.0.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b5e6d5ae-30d6-4786-8817-062f2ee2a582',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('aaaeda2c-0a27-426b-98f7-e3875f58b26e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.4 to 2.1.0.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ffe0ef6e-6d41-404e-9248-1c6d0a02c87e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('59ac94a3-63e9-499c-a8be-364cb415324e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.5 to 2.1.0.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b9ebc8e6-f0f2-49bc-8be5-606cb2afef4d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ff2d2dbb-552a-4e01-b7ee-71a28e3c5d0d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.6 to 2.1.0.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4135622a-6967-4cf9-943c-2662f8740f76',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('96343e6a-280c-4bcf-aeea-e7c11fde8ba5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.80 to 2.0.10.80
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.080' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.080' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.080_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.080.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.080_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d9288ec3-248c-461f-981d-a547405dcb00',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.080','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0787479a-fd06-4a4a-a151-c4e75303eb03',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.080','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.81 to 2.0.10.81
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.081' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.081' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.081_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.081.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.081_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('39496f83-eda7-4e8b-81d8-2ee2628caace',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.081','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('01f7cf73-5652-4719-8101-4fd769c9e598',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.081','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.82 to 2.0.10.82
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.082' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.082' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.082_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.082.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.082_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('45896995-5d35-4463-9852-4f6c735ddc51',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.082','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('46263699-4060-4ae2-a39a-c7e90cf24e2a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.082','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.88 to 2.0.10.88
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.088' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.088' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.088_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.088.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.088_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('36dcef1b-841f-4bb7-8651-f1f66140a9e0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.088','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ebd68aaf-f35e-4036-8b8c-015a693c8065',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.088','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.1 to 2.0.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8b5c66bf-313c-4933-a313-2bd7ca18beeb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e4cd09a6-740f-40ba-a6d7-d2bae1d1be91',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.7 to 2.1.0.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0570c648-247d-408f-871c-b3a38f58076a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b40da4ab-ec06-4a9e-8634-6017aeacceab',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.87 to 2.0.10.87
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.087' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.087' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.087_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.087.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.087_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8fe007bd-5a69-40d1-abd2-e2b1f809c12a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.087','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c3aae57b-5a3d-43cd-9376-883ca551dd94',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.087','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.88 to 2.0.10.88
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.088' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.088' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.088_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.088.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.088_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('36dcef1b-841f-4bb7-8651-f1f66140a9e0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.088','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ebd68aaf-f35e-4036-8b8c-015a693c8065',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.088','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.89 to 2.0.10.89
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.089' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.089' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.089_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.089.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.089_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('45dd0dd4-3b56-4232-b7a7-73b6eb452a0d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.089','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('62bde31a-7914-494b-b7c1-7a60f46dd5e4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.089','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.90 to 2.0.10.90
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.090' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.090' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.090_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.090.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.090_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('755305da-7ef7-48f5-8764-0bc8b7753b9b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.090','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f78e09c0-1dce-44f2-ba2d-80f50f7cb919',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.090','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.91 to 2.0.10.91
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.091' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.091' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.091_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.091.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.091_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('365b192b-c688-411f-9c9b-1e675d12c623',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.091','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8be4769f-62d8-448a-b29e-3ca9a02c09a4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.091','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.1 to 2.0.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8b5c66bf-313c-4933-a313-2bd7ca18beeb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e4cd09a6-740f-40ba-a6d7-d2bae1d1be91',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.2 to 2.0.11.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7227d032-9de0-43d6-88c9-1a42bba970bb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('81fdbecf-04fa-440a-949a-baad2c9fcc96',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.3 to 2.0.11.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8046d8bb-dd90-4638-85ad-6bde2bfd6bbb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('24e8ede8-2063-45bd-aac1-3ca0ff6db39e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.8 to 2.1.0.8
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.008' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.008' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.008_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.008.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.008_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0f8c8925-3af4-4ccd-9dfb-7446c31d7ec9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.008','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('75b63d5f-c65b-45c7-8cb3-ddf0afd3f4f3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.008','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.9 to 2.1.0.9
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.009' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.009' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.009_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.009.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.009_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('652efd7e-67b8-44c7-93c1-48dec70691b5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.009','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6106609b-c13a-4f9c-a719-1a7354f4f265',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.009','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.4 to 2.0.11.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('55d8da9a-963b-4300-84d0-1fbe5ee03566',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6c5c7c75-67db-418a-aff9-2b9ffd18b35b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.5 to 2.0.11.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('53fe1beb-425c-4b8d-9e45-166f9f441ebc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('581ac763-2a33-46e9-aa4d-8bd17a2b217e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.6 to 2.0.11.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d37aa160-e013-4694-a542-26b7ba0dc919',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('af573770-614d-4b35-8623-4470b81e7e66',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.7 to 2.0.11.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8416930b-2da0-43b5-bea1-233c54fc8255',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f15b61f8-c300-4cee-8e5d-4890ff442e55',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.10 to 2.1.0.10
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.010' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.010' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.010_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.010.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.010_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dcee16d7-7b2d-4863-84b1-adfbd7f69adc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.010','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('424ab4e4-2a7f-4ef3-8933-ee6b644ddaa5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.010','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.11 to 2.1.0.11
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.011' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.011' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.011_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.011.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.011_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('83cbd800-d362-4863-943e-e4701020d3fa',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.011','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c5fad07a-f83f-45ea-822a-74b050097b03',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.011','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.12 to 2.1.0.12
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.012' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.012' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.012_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.012.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.012_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3024503e-4378-4134-af5a-695be4a523b0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.012','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('aa4ba990-f9fb-4a91-a173-19c3ebb55df8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.012','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.090' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.090' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.090_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.090.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.090_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('755305da-7ef7-48f5-8764-0bc8b7753b9b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.090','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f78e09c0-1dce-44f2-ba2d-80f50f7cb919',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.090','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.10.91 to 2.0.10.91
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.091' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.010.091' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.010.091_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.010.091.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.010.091_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('365b192b-c688-411f-9c9b-1e675d12c623',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.091','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8be4769f-62d8-448a-b29e-3ca9a02c09a4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.010.091','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.1 to 2.0.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8b5c66bf-313c-4933-a313-2bd7ca18beeb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e4cd09a6-740f-40ba-a6d7-d2bae1d1be91',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.2 to 2.0.11.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7227d032-9de0-43d6-88c9-1a42bba970bb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('81fdbecf-04fa-440a-949a-baad2c9fcc96',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.3 to 2.0.11.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8046d8bb-dd90-4638-85ad-6bde2bfd6bbb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('24e8ede8-2063-45bd-aac1-3ca0ff6db39e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.4 to 2.0.11.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('55d8da9a-963b-4300-84d0-1fbe5ee03566',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6c5c7c75-67db-418a-aff9-2b9ffd18b35b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.5 to 2.0.11.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('53fe1beb-425c-4b8d-9e45-166f9f441ebc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('581ac763-2a33-46e9-aa4d-8bd17a2b217e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.6 to 2.0.11.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d37aa160-e013-4694-a542-26b7ba0dc919',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('af573770-614d-4b35-8623-4470b81e7e66',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.7 to 2.0.11.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8416930b-2da0-43b5-bea1-233c54fc8255',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f15b61f8-c300-4cee-8e5d-4890ff442e55',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.11.9 to 2.0.11.9
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.009' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.011.009' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.011.009_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.011.009.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.011.009_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5f577ad4-c5d7-42d7-87c9-265af977cf2e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.009','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0eed88fc-edad-48c1-91ce-eae82f296b5f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.011.009','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.0 to 2.0.12.0
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.000' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.000' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.000_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.000.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.000_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a22ed9e3-ed06-4dd5-98d1-57cac3bfa4c9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.000','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e8e436b4-09ff-4080-ba7d-e81c26db4dd3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.000','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.1 to 2.0.12.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9609b59a-5efa-4e41-bed9-81a35034be48',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3af8d3a6-1ec8-42b5-b042-fdc1675f7d36',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.2 to 2.0.12.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a1c0ae0a-58ac-48e3-a095-aa522cc86bb3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('293d7094-cf98-42ba-b5bb-27f8f14074eb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.3 to 2.0.12.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7a68be02-ca92-467e-ac9e-94db990466f0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c8a7d8fc-a653-46cf-a1be-fcff51bcfe8c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.4 to 2.0.12.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5f577a27-c5d7-42d7-87c9-265af977cf27',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0eed8827-edad-48c1-91ce-eae82f296b27',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.5 to 2.0.12.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('46bb08aa-7c3d-4384-bd1d-8a44b254284c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d7f19b65-b0ac-48fe-82c8-f9a9fc5bd441',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.6 to 2.0.12.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8099c7d9-a52c-40ec-a232-f1c5b523a8d9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e4e46ca0-e2fc-4a1b-87e7-b9bc9ca0597d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.7 to 2.0.12.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('377a7bb9-81d0-41d7-bd54-56390537166b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b836e0e6-9c79-4850-bb9a-b6cf1288121f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.8 to 2.0.12.8
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.008' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.008' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.008_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.008.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.008_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f7dd7375-5d68-4a13-829a-74296c16424f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.008','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ec2d5635-9b7e-4522-baf2-ab73a5046b99',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.008','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.12.9 to 2.0.12.9
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.009' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.012.009' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.012.009_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.012.009.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.012.009_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('996c12b5-92a6-424a-a0a6-28c40d65b25e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.009','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b5bbf087-82de-46ed-b78d-c89bfe5ad23c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.012.009','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.0 to 2.0.13.0
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.000' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.000' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.000_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.000.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.000_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a21443e7-0d63-4e76-bd11-ec67a73a01c4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.000','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cd43de72-dbe6-44c3-8ff9-0ae3609499c1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.000','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.1 to 2.0.13.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5268cb0a-4f9c-4a26-a279-a7313ac8e96d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ca1d4db7-7f3f-4836-8553-12fe8cc7c878',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.2 to 2.0.13.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f2f80786-1517-4415-a215-5b523fedd6f7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2e78b3a7-0daf-49b7-82ba-b65902933744',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.3 to 2.0.13.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f741e27d-465c-4b20-b6da-765dcb2733d6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d4fa09bf-4b73-48d0-bd88-20c1389f56fd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.4 to 2.0.13.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b22c69fc-7fcd-437f-bfcb-910b50d1a11e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7e5743bd-008f-4045-82c8-039466e6cded',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.4 to 2.0.13.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b22c69fc-7fcd-437f-bfcb-910b50d1a11f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7e5743bd-008f-4045-82c8-039466e6cdef',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.5 to 2.0.13.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4e6915e4-08a9-4864-ae20-0e674d70735c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('45405444-dba1-40bf-a7fd-49513d1c2e0e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.0.13.6 to 2.0.13.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.013.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.013.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.000.013.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.000.013.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('911ce493-f3b5-41e5-af1e-10d9dcc2c3d6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e7462f85-26cb-484f-9795-504cc977d47e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.000.013.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.13 to 2.1.0.13
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.013' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.013' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.013_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.013.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.013_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('681c966a-004e-4c84-b779-fcb72a630ae8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.013','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('82297928-3cd0-4254-ada7-4e380342bdc2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.013','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.14 to 2.1.0.14
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.014' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.014' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.014_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.014.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.014_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6646231a-2986-4e1a-9e68-015e1ba8977b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.014','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('acf922fd-83bf-46a9-8a41-e644d1746046',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.014','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.15 to 2.1.0.15
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.015' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.015' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.015_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.015.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.015_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('927b264d-3ee6-49a6-8a38-be6efbac7613',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.015','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3902acf7-a8fc-433c-84b6-10be5069869a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.015','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.0.16 to 2.1.0.16
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.016' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.000.016' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.000.016_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.000.016.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.000.016_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8e80a99e-91ff-4155-ba19-6a47987af8a3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.016','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('439d47ec-bd56-4726-8bdb-a9bcbef0c47b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.000.016','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.1 to 2.1.1.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f70a0991-3c8c-4a34-b74e-6e6726b63c8e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ea8196c1-c194-4206-a766-97051a6ddbb9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.2 to 2.1.1.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1e36fc94-9c31-4c00-88b4-efe495ce53fe',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6b3830e2-a8e4-4dbf-a0fd-6146872b76e2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.3 to 2.1.1.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c4fef1b4-299d-4a04-9e9b-5a7ba72cf593',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2bc62cdc-ddd8-4208-a577-291500942bd9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.4 to 2.1.1.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('95c32325-f2e0-4c61-ae9f-4709d20eb879',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a54fad64-5ffe-4d5d-96fa-008a62de9fe4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.5 to 2.1.1.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('135c1460-856c-4eb5-a19d-e4ce03593a2e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('997ab74f-4454-4d3c-a772-35c9a5d0b3d9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.6 to 2.1.1.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4b63ddce-a962-4797-9e35-2727db338c65',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b061c3f3-9f97-419b-a7ef-5d05fffb7bee',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.7 to 2.1.1.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('baa177a6-1bf2-4bc6-808d-a6cbc5daa0e8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('07f3b7fc-48d7-49b7-9de4-f802028275dd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.8 to 2.1.1.8
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.008' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.008' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.008_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.008.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.008_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('82bd4ffd-2b6e-44d0-8a5f-ea10e7094867',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.008','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('198f7839-58d1-4828-8ff2-a4a983bbbc1d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.008','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.9 to 2.1.1.9
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.009' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.009' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.009_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.009.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.009_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2d0a6ddf-c41d-4d86-a7ce-8adc50c2fdf5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.009','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c2557575-f4e8-4eb0-8afc-659a577f4573',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.009','SchemaUpgrade') ;
COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.10 to 2.1.1.10
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.010' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.010' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.010_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.010.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.010_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dbebe107-f270-4149-b59b-2a87c24b4dbe',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.010','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b6450f04-4bcf-48cc-ab5c-fa18093aa462',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.010','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.11 to 2.1.1.11
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.011' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.011' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.011_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.011.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.011_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('34786525-8972-4a66-8f7e-8f56d2d96a3e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.011','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c78cac55-76cc-4eb1-b08c-bed1206d958f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.011','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.12 to 2.1.1.12
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.012' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.012' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.012_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.012.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.012_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b0ba4ab9-8a3f-4297-9801-01464a3dd43a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.012','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cbb74c09-c5df-4bcf-8bbf-c533463e530c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.012','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.13 to 2.1.1.13
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.013' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.013' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.013_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.013.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.013_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e0d3a2ae-71d8-44d1-8bc6-a9255d849fa1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.013','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3f814a95-14ab-4400-adcb-408665f795d9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.013','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.14 to 2.1.1.14
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.014' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.014' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.014_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.014.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.014_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e7c02334-6656-48f4-8181-a5cde4456d6a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.014','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('554b1596-a978-46b6-9ccd-e45eb43dc5dc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.014','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.15 to 2.1.1.15
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.015' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.015' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.015_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.015.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.015_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4154b983-0fac-4d23-bcd0-3a654c6fd3d1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.015','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2a697a57-b51f-4e59-8fef-545a6dd55dd3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.015','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.16 to 2.1.1.16
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.016' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.016' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.016_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.016.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.016_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('10a193fe-68aa-4a9f-a73a-02362974d112',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.016','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e1dec39e-9971-447a-82d3-8afc2d97715f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.016','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.17 to 2.1.1.17
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.017' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.017' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.017_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.017.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.017_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1a15bb40-367a-48ca-b900-8ccda79555ae',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.017','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c0aaecee-d525-425b-b91b-93b9922cf17c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.017','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.18 to 2.1.1.18
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.018' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.018' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.018_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.018.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.018_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dabf0b6c-fdcd-467d-835c-2f9b53100845',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.018','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cbc87377-86ba-4a2d-9044-43aead2c1153',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.018','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.19 to 2.1.1.19
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.019' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.019' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.019_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.019.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.019_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('987e76d7-e637-42c5-9a34-96645799f054',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.019','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6cac9be8-68f1-44aa-a079-17910b4c1eea',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.019','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.20 to 2.1.1.20
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.020' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.020' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.020_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.020.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.020_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ca2d216e-3f53-4f0e-9656-0358422a785b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.020','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('74f646f2-2ea2-467d-8b79-16667b2a4bc6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.020','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.21 to 2.1.1.21
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.021' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.021' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.021_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.021.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.021_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c4955c89-c3e0-40a4-a320-d28c91d71e2b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.021','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f7da3146-271a-40b9-a2b3-d3d72f9cb288',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.021','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.22 to 2.1.1.22
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.022' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.022' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.022_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.022.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.022_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fa6f491e-b420-4a20-a55d-d978fda85cf8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.022','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('274e87c7-3869-4d06-92af-04ad12319334',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.022','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.23 to 2.1.1.23
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.023' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.023' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.023_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.023.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.023_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b341971d-b36d-4a65-a734-b5bb5093c864',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.023','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('03a5fd12-0efa-4321-aee6-2ad6fc8d4bbe',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.023','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.24 to 2.1.1.24
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.024' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.024' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.024_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.024.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.024_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('54b3e984-f113-4acb-b500-e76854459021',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.024','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8bfa446e-be1d-44ad-92c7-f03d944c5cba',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.024','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.1.1.25 to 2.1.1.25
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.025' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.001.001.025' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.001.001.025_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.001.001.025.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.001.001.025_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dbf050b2-807e-44a2-af44-e92b3c527721',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.025','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('aa9d8060-8b7c-424c-becb-1c3066e85d04',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.001.001.025','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.10.1 to 2.12.10.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.010.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.010.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.010.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.010.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.010.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4ef526d5-569e-4d35-b0e8-989cd0b287a0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.010.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8aaf482d-8f28-49da-9b36-d9fa4ff0eba4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.010.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.11.1 to 2.12.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2fdb9de1-00d1-4f97-aba8-8cfdce0fd139',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6eeb87e8-671c-4279-b723-4b56e9fdd61f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.11.2 to 2.12.11.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5abc2eb8-a5b0-4ca6-8c8f-c5cd06957469',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('328514b3-4fd3-431a-bd76-c17fffe4af25',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.11.3 to 2.12.11.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.011.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.011.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.011.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dd7fe87d-63b1-4c8d-9bfe-16cd4140594f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('928067e7-967e-48a1-865c-824baa3e9533',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.11.4 to 2.12.11.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.011.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.011.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.011.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c3c14fb4-3d2a-49d6-8e49-2a923051b532',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3c312035-540e-4b8a-bf22-6d6d1fd2a272',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.11.5 to 2.12.11.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.011.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.011.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.011.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.011.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4c50a301-56c7-4ff5-83f5-629492bd8a3e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('69a18fb7-3606-47f7-97e7-87a97ce2caba',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.011.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.12.0 to 2.12.12.0
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.000' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.000' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.012.000_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.012.000.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.012.000_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('49219ad8-c69e-4d9b-aa5b-673ec1816ac5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.000','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('da43ca26-4b95-4d13-9a8e-9da3e92c5575',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.000','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.12.1 to 2.12.12.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b69f39af-f549-463e-a1d1-b615fb110ddc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b59b821d-7949-4b41-94fc-aad1cbb89f88',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.12.2 to 2.12.12.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.012.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.012.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.012.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f73b6b8a-0d3f-4f52-934b-ac1800b62475',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('35d7e2bf-91dd-42bf-a144-5aaffeac47b6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.12.12.3 to 2.12.12.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.012.012.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.012.012.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.012.012.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.012.012.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0c8dfe0e-baaf-487d-9cec-d59389321a40',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('11181b2e-ad27-486a-af8c-2272ce933215',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.012.012.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.13.1.1 to 2.13.1.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.001.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.001.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.001.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.001.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.001.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('af4a901e-9b03-4751-a1d4-4da16795c5c6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.001.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('80e76dfd-8be4-4a31-8f91-002ad59cd2fd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.001.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.13.1.2 to 2.13.1.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.001.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.001.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.001.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.001.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.001.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('899e5dfe-e0f7-49f0-9d50-ecd4ed32c1aa',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.001.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('31e4fea9-a7e8-4f18-afd6-32e6c23e5b69',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.001.002','SchemaUpgrade') ;
COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.1 to 2.13.2.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b8ba4e95-873b-472e-bfc4-0ac16caccbd2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ccff8223-7b52-4ee3-8122-7f6fb6f6076b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.2 to 2.13.2.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e6b63d8c-db47-4260-9247-0f63268cb363',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('12423061-a494-4721-8c4a-ad21706c8eee',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.3 to 2.13.2.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('180a9dd6-fd84-4c99-bdc4-d2f9987c1f5e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0429b1a2-9870-4def-bf82-ccd130d8b673',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.4 to 2.13.2.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('467be415-9e73-476d-95da-10bfc8f268bd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6cfe5ff0-71dc-42e5-8ef6-48388549c856',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.5 to 2.13.2.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6b262f15-2f16-4333-a9fe-76c1388128b8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('71b874f4-58f9-4fe5-ad21-a8b8fa274220',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.6 to 2.13.2.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dfd7fb65-a761-4bf5-9c1c-d025356c2f61',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('de8f9316-ad6e-42f5-9e48-4265c75cab3e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.7 to 2.13.2.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5b63241a-c3e9-438c-9c14-3900c51a7237',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f3b77b51-9b86-4a6b-a230-82779d42979f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.2.8 to 2.13.2.8
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.008' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.002.008' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.002.008_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.002.008.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.002.008_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1d142ff2-2438-44c9-bbbf-364fc71e87b8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.008','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fb495c4b-6aec-4886-89f7-8b38fb43c2e7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.002.008','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.1 to 2.13.3.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1a94f723-4b6c-48c2-8fa1-46e11452cd8d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1dc81dfd-fcb2-4b5e-be65-058beaf2cbd2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.2 to 2.13.3.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('10810ac8-9f44-4fa0-a5ef-938f555afaf6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('96742cc9-e1e5-4463-b1a1-a870c24a1a88',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.3 to 2.13.3.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0f4c4954-c6ff-4780-8ba7-d4c0192c41e0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4851ef15-3a0e-4701-8c07-c3e38d60073e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.4 to 2.13.3.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e41218ac-9f3a-443c-aa27-763741bfc684',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('786f2705-4dce-4264-b42a-e56e2b7e401b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.5 to 2.13.3.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('60ab2662-a74d-4d7d-8840-d4e576f8d951',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c09cf713-60b7-41fb-900f-1f473fd8e29c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.6 to 2.13.3.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('00e4c16f-e570-4530-a28d-a5a2af95514c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e8e256a5-24be-4b3a-94c8-191a3ba7341d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.7 to 2.13.3.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('57b6b849-86e1-4a33-8020-bccf300e1606',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('180c8ed2-7013-493c-a3e7-9495ccccbdaa',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.3.8 to 2.13.3.8
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.008' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.003.008' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.003.008_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.003.008.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.003.008_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('110d18f6-2de2-4065-a4f6-c8465ddaad99',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.008','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b6371a65-1bc2-449f-b923-3eb12e55657b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.003.008','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.1 to 2.13.4.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('592a08b5-cb90-4903-a10c-0f61fc05907f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dbf17b66-abeb-4c2f-adff-a55f68ec6a05',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.2 to 2.13.4.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e3f017aa-9a0d-48b1-917f-d77902498a4d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5306c7b7-a065-4835-90cd-40e4f0ac7162',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.3 to 2.13.4.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('169fc55e-2d40-45f7-a11b-2b5967822f34',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('880de38e-0920-493b-9501-b9dc5d23af9f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.4 to 2.13.4.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2d0a6fc9-7f26-4c19-8b10-44c38a6a8f5b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b10604be-b9b8-4feb-9850-d0895f093e9d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.5 to 2.13.4.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1254d7a7-99c2-4ef5-a002-bf9d510908ba',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('adb81d63-414d-4400-836b-2361e4bc994a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.6 to 2.13.4.6
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('62417381-55f8-4b46-ae3e-9f0a402bead7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('251bed83-f03a-4af2-952d-8da7353b5c8c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.7 to 2.13.4.7
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.007' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.007' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.007_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.007.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.007_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('104e633b-bd3f-46c6-99cf-31c86a738da4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.007','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ed0493a6-73f3-4ae7-bac7-b973a02bbbde',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.007','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.8 to 2.13.4.8
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.008' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.008' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.008_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.008.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.008_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('995b2a04-e4e0-408b-9700-ceec5ac92e91',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.008','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b4a4c4ae-70a1-46ff-ba3c-8506993e432d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.008','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.9 to 2.13.4.9
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.009' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.009' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.009_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.009.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.009_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0fcbe2ab-62d4-4f38-8958-103976f41516',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.009','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('500047d8-ddc2-48c8-be2d-30aca48e4b40',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.009','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.10 to 2.13.4.10
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.010' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.010' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.010_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.010.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.010_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2b94c7b8-1fe2-4154-ad5d-4201caa4295f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.010','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('30a20d16-efd3-4792-9381-d657e7e0bf22',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.010','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.4.11 to 2.13.4.11
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.011' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.004.011' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.004.011_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.004.011.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.004.011_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ddb14037-611c-4357-8399-c3603bf2b5a0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.011','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5df7597a-dedc-4c63-a9c2-5eae81c0bd61',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.004.011','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.5.1 to 2.13.5.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.005.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.005.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.005.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.005.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.005.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a38e9531-0047-493d-980c-489066968583',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.005.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('239261bc-87fc-46c4-9286-bc53213a0930',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.005.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.5.2 to 2.13.5.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.005.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.005.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.005.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.005.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.005.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9ad8da26-4b45-4b2f-b292-67015b817f06',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.005.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b58c304c-e9ba-4169-91c4-f6bced5c210e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.005.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.6.1 to 2.13.6.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0fef2105-9596-4acd-8f75-3a350c1856d3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('baf9666f-0648-4a79-a680-4274167fff3e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.6.2 to 2.13.6.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.006.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.006.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.006.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6cce3f10-93eb-40bf-af13-9cd5a4e68790',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('57a64bce-666e-4d26-bfe0-cbf85d85cb4b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.6.3 to 2.13.6.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.006.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.006.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.006.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e7941b59-192c-4c75-a26f-cb0ec3cdfedd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4671d170-037a-4bf1-9066-751086dd56a8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.6.4 to 2.13.6.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.006.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.006.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.006.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ccca7c04-2dc8-409e-9a97-d804f046a93b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('138a2314-7fb0-44ca-b976-bdedde64d0c6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.6.5 to 2.13.6.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.006.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.006.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.006.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.006.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('17ac9011-f39c-424e-8c60-44c0208524f1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2f778759-f59d-411f-a022-c407def1cc41',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.006.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.7.1 to 2.13.7.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.007.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.007.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.007.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a26a53aa-148e-4551-84bb-a9ce5938a165',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f108eb58-4d23-4d38-a68c-08b742ee3c39',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.7.2 to 2.13.7.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.007.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.007.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.007.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('899d6add-a49d-4df3-88bf-9291eec4f5bb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ef6a19e8-54cc-4586-99e4-c29815633b98',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.7.3 to 2.13.7.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.007.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.007.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.007.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c19ad2c2-1581-4451-b339-8b3fb9f4cac9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4f66ac86-a9b9-4567-a4e6-65866d6724d3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.7.4 to 2.13.7.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.007.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.007.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.007.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ddda4352-6065-4c0d-a661-a214f75d227c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('464cdf09-a27e-47b0-b529-4b2dc981a054',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.7.5 to 2.13.7.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.007.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.007.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.007.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.007.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('95471548-e6a1-48a6-a258-c6e68ce8387e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ee0e3b76-0646-4372-aac0-5f54c552f69c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.007.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.8.1 to 2.13.8.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.008.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.008.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.008.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('73e21ab3-fe60-406b-82f1-3850ba533c66',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('929430be-e511-499b-a3f1-d721c638cb6b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.8.2 to 2.13.8.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.008.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.008.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.008.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5703d643-302f-41b4-98a1-598dcc40e230',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3bb29767-772d-4c3f-96f3-ff67adfca4a4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.8.3 to 2.13.8.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.008.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.008.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.008.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('48a82323-4d15-4c33-9420-86326f1b187e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f2262d6a-fb91-4b5f-9a0d-05c8257347c1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------

-- Upgrading from 2.13.8.4 to 2.13.8.4

---------------------------------------------------------------------------------------

spool off

set termout off

set heading off



spool Upgrade.sql



declare

 rec_count number;

 upgraderec_count number;

 sql_str   varchar2(100);

begin

select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 

select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.008.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 

select 

decode(rec_count,0,'@DBUpgrade_002.013.008.004_Before.sql',' ') 

 into sql_str from dual;

dbms_output.put_line(sql_str);



select

 decode(upgraderec_count,0,'@DBUpgrade_002.013.008.004.sql',' ') 

 into sql_str from dual;

dbms_output.put_line(sql_str);



select

 decode(rec_count,0,'@DBUpgrade_002.013.008.004_After.sql',' ') 

 into sql_str from dual;

dbms_output.put_line(sql_str);



if(rec_count = 0) then

insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)

values ('50da4a6e-6b1f-4210-af81-56b15bee7ddb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.004','DataMigration') ;



COMMIT;

end if;

if(upgraderec_count = 0) then

insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)

values ('58350942-892e-4da1-bc1f-fce2ee7d00de',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.008.004','SchemaUpgrade') ;



COMMIT;

end if;

end;

/



spool off

set termout on

set heading on



spool InstallDB.log append



@Upgrade.sql

--------------------------------------------------------------------------------------
-- Upgrading from 2.13.9.1 to 2.13.9.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bc5e7e15-ca59-4ec7-891a-ca8a88370e4b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('40a7c80b-0e29-4fab-986e-5477db97dc23',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.9.2 to 2.13.9.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.009.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.009.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.009.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.009.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.009.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fabda354-814c-452e-8e38-2f4371221f79',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.009.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d1dff1c7-1c7c-499b-bd7d-eb4f80213a49',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.009.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.10.1 to 2.13.10.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.010.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.010.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.010.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6450e9c2-499a-4323-a920-18a44ffc9e3e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d2db3ac8-3e58-4c01-8e61-87675c3f1e8a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.10.2 to 2.13.10.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.010.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.010.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.010.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3b27a368-d8af-41f5-a85e-0db34fada5dc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0745f8d4-f18c-436f-851e-c164ff59dc48',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.10.3 to 2.13.10.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.010.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.010.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.010.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8a6438d8-4b98-43f2-8d7b-0b8bc1cb1b24',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e3b3cc16-7d1e-4c96-b927-68224097f577',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.10.4 to 2.13.10.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.010.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.010.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.010.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.010.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c56867c8-5c31-4442-a463-49e468956a91',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3f0ddabd-43c6-4809-85a5-2e62e2e262d1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.010.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.11.1 to 2.13.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f10d5471-c0fc-407b-9103-86984418c894',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e3f4a730-bdb4-4dea-b713-fcc15e9871b9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.11.2 to 2.13.11.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1550fb85-4892-449f-9595-825c85849d78',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('29121bf8-ce9a-42cf-9f77-72a59ae4eb38',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.11.3 to 2.13.11.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.011.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.011.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.011.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4c2e18f9-376d-42b6-ae2b-8a43bb0a474b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ff4caa12-c139-4f9c-8c9f-78ddffe2df42',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.11.4 to 2.13.11.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.011.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.011.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.011.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f42a40bc-67bb-470f-8120-17b323072937',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c83bd4de-3f05-48b2-8464-518c52866c7a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.11.5 to 2.13.11.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.011.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.011.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.011.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.011.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('676686ef-af5d-4dd7-8773-de22007dc5de',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4594dd6a-33bf-42fa-b7e3-7001ad65aec4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.011.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.12.1 to 2.13.12.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b626ca97-2c1f-4168-814f-5beef2e067b0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('96563165-245e-4269-9417-19ffc24d0a63',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.13.12.2 to 2.13.12.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.012.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.013.012.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.013.012.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.013.012.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.013.012.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('33bafacc-78be-44a7-a858-0ae053575634',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.012.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('41884069-4798-47a0-81ef-2d2e33d87df2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.013.012.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.14.2.1 to 2.14.2.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.014.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.014.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.014.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('74bb8aef-6eec-44bc-a178-ffe7c6de17f9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7f1bf920-5705-4df4-a683-2746b8752214',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.14.3.1 to 2.14.3.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.014.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.014.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.014.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('aa65ca10-d1f2-4a63-b13e-21bc9762db64',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e5cffa76-4842-4044-9815-5edf1ce1eecb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.14.3.2 to 2.14.3.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.003.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.003.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.014.003.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.014.003.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.014.003.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9079bcd8-c572-4272-964f-aa68f6f6decb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.003.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5943b8a3-6109-48b7-8d26-96de5eb4ea12',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.003.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.14.4.1 to 2.14.4.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.014.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.014.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.014.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a941e42e-00ea-4480-840e-fb4e8a22a20c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f21f8e9e-ab57-49f5-830d-b22560748ce7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.14.8.1 to 2.14.8.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.008.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.008.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.014.008.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.014.008.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.014.008.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3010a559-6aed-4f23-9f85-e9acad12411d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.008.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9d6c8d56-c8c7-46c4-b35b-52767a9ffe8a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.008.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.14.9.1 to 2.14.9.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.014.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.014.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.014.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.014.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('48c3a213-3bfa-4bd1-b9b1-c91a0c74d1b0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('282f2fa1-711b-48f3-b8e2-56e005239ec4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.014.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.1.1 to 2.15.1.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.001.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.001.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.001.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.001.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.001.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cbeb3600-22fc-419f-8567-d93c67afc270',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.001.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('676749c6-cc27-46fc-b6c7-fafc89a80573',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.001.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.3.1 to 2.15.3.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7d09c923-c3da-4af7-8026-c503e855caff',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('acb183bc-9224-4189-b20d-4abf5c3e106f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.6.1 to 2.15.6.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5c7bf6b4-6b11-4820-b10b-5e3e3cccacb7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bd240372-7b52-466f-976d-a50295e0e9db',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.6.2 to 2.15.6.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.006.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.006.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.006.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.006.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.006.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5238a651-6fdd-46b9-beee-29bd3684abf9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.006.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c1ed2bc4-7342-489a-92fe-ddbb48a550ca',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.006.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.7.1 to 2.15.7.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.007.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.007.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.007.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.007.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.007.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4a6512d9-cbdc-4c95-81ff-e719b42dc645',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.007.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('afb803da-2aaf-4c2d-9a7d-8c1a6990b8b8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.007.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.7.2 to 2.15.7.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.007.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.007.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.007.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.007.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.007.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('44809b45-0349-4ea9-bbaf-88ee5b622dfe',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.007.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dd04939f-2fd8-4a41-ad24-6c27bbde43b0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.007.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.9.1 to 2.15.9.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5944676a-1417-49c5-9094-b7542853187b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a8dd454b-9b71-4b96-99ea-c8678eeb3cc1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.9.2 to 2.15.9.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.009.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.009.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.009.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.009.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.009.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('71573a19-034c-407f-8a60-2d89b5b412d9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.009.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9a250380-477a-49f4-93bb-39a4320ebc89',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.009.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.15.9.3 to 2.15.9.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.009.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.015.009.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.015.009.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.015.009.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.015.009.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a65de022-6191-4fd7-a463-26b19d69895f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.009.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4726a785-d388-4b57-b071-341937fe7b97',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.015.009.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.6.2 to 2.16.6.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.006.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.006.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.006.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.006.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.006.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9b0afe44-54a1-4376-ac58-c2510c1812fd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.006.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f55b8981-0fe6-4b8c-9458-0036564b187d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.006.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.6.3 to 2.16.6.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.006.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.006.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.006.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.006.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.006.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4aeacbcf-74ec-4876-8b11-9fcb1bfbc110',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.006.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('dd6b1d73-d9e5-4922-b679-853645603da1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.006.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.7.1 to 2.16.7.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.007.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.007.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.007.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.007.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.007.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0355daf2-8fe5-46d1-9bc2-5fb1e8aaee3b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.007.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fbd11791-e9e5-4644-8d5a-f1c966d6f930',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.007.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.8.2 to 2.16.8.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.008.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.008.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.008.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.008.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.008.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1a99e3b2-e182-4816-b8de-27da860f0415',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.008.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3c5d1d6c-a061-4a8f-aec2-f15307dcda20',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.008.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.8.3 to 2.16.8.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.008.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.008.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.008.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.008.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.008.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('15f92098-fc47-462c-a6ff-95f2a5f1976e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.008.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a0fa9f00-6100-4b98-8d4e-50de982b8198',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.008.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.9.1 to 2.16.9.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('af3ddfab-f9e5-4c33-a0ac-57d859c26341',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5b49b83d-6655-4934-8ad9-4458be8d3bc0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.9.2 to 2.16.9.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.009.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.009.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.009.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.009.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.009.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('61ce200b-cb32-47bd-86dd-6626edf46769',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.009.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d07b579c-e1db-4191-89ed-471c847fcef4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.009.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.9.3 to 2.16.9.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.009.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.009.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.009.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.009.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.009.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d448a0cd-b5b5-4ec4-a42a-cba8a570e39f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.009.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('15c6c6a7-db61-4184-bdc3-59cf1ea6d050',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.009.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.10.1 to 2.16.10.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.010.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.010.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.010.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.010.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.010.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('efc80a61-c57b-492f-b6f1-f950c8c96e73',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.010.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c79dc7c2-970c-45a7-a24e-e0360731bac5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.010.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.10.2 to 2.16.10.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.010.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.010.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.010.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.010.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.010.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('77ea2c8a-92c2-4d03-bffb-11b7db6a1327',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.010.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fafb8e0c-20ab-45f9-b818-5fd248d652d3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.010.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.11.1 to 2.16.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f044459b-26ae-4124-bf65-a469e1e7e0f4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('517168fe-a6f6-42e6-93cd-df0ae666198f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.11.2 to 2.16.11.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3f845ea2-a852-42aa-aec9-357caba7bd7e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a0322f68-1887-435f-afa3-fb118ae896d6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.11.3 to 2.16.11.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.011.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.011.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.011.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.011.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.011.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b9eb0ecb-40bb-475f-a6cc-9b8fcd98f165',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.011.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2d893bb8-a276-44e4-a5a5-341368a35fc9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.011.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.16.12.1 to 2.16.12.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.016.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.016.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.016.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.016.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d32deb9f-b554-47c3-8534-646324ce9186',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('735c7b91-eefd-432d-b391-661e143f1e84',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.016.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.1.1 to 2.17.1.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.001.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.001.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.001.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.001.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.001.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9e509ff0-943c-4511-ad97-bc21fc517461',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.001.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3d482f8e-ca08-46c4-b907-3e61f3182921',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.001.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.1.2 to 2.17.1.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.001.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.001.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.001.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.001.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.001.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a15a5fe0-3079-43ef-81a1-929cf0cdbeaf',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.001.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('0bc0d697-1b13-4eb1-9bb1-55a684f1f6e1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.001.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.2.1 to 2.17.2.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('115f61a2-e097-44c5-9af3-8b6a54d28c8c',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fef672dd-aebc-4c9e-b4aa-cf58253c78fc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.3.1 to 2.17.3.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c061a82f-3ad4-447c-91b9-954d654b40b3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1adc4c4c-0620-4842-a626-eef714345cf6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.6.1 to 2.17.6.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b97a0aa6-1a14-4746-91a9-99e738c83c39',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e4c4300c-b3f2-4cfc-8c1c-fcb1d1dcc05f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.8.1 to 2.17.8.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.008.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.008.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.008.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.008.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.008.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('eef3017d-ab4f-4e09-91cf-ab69c3eed676',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.008.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4de227e2-634c-4ef5-88c3-51e9a9cf8d87',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.008.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.9.1 to 2.17.9.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d484265b-6183-4e92-ba06-20d76f1858af',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9de65a55-edb3-42bf-beb2-5a3a2d733bc5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.11.1 to 2.17.11.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c25e0aef-76a1-452a-9ad5-80ee991a63fe',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b7a1ba3c-af3a-42c0-8f85-276c5c914d88',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.11.2 to 2.17.11.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e92f6b4b-9be1-4253-a03e-e6adf8ba22d2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('25303457-7a31-4157-84ba-9b1391b477d3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.17.12.1 to 2.17.12.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.017.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.017.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.017.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.017.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('91e3f1a7-f478-4971-9180-5d0ddc079ce7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f9265810-d52a-4d05-b5a2-31a703e9de81',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.017.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.02.1 to 2.18.02.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7e2a8ff4-8075-4522-ab9b-6e2890bf8e3a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1c9e279c-d57a-4b80-8611-4c1db24a62fc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.02.2 to 2.18.02.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.002.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.002.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.002.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.002.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.002.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('50fe461e-b94d-480f-9050-b272af2c4c7d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.002.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b44d7b8a-11e0-485b-8ec0-efa0a33cc0b1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.002.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append



@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.03.1 to 2.18.03.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2437ee3c-f92d-4e26-9c08-738ad61202e7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3030da60-abbe-415f-baed-661584a2aa9a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.03.2 to 2.18.03.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.003.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.003.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.003.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.003.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.003.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e3867fe2-796b-4331-ab77-f9041be339be',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.003.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ddd2a494-5194-4a90-95be-01788fd3fc58',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.003.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.03.3 to 2.18.03.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.003.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.003.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.003.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.003.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.003.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fc523072-47c4-4c66-8cf5-a230d9da3826',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.003.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('725c5cb8-f149-4d8e-bce7-b78a43f2baee',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.003.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.04.1 to 2.18.04.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('26fbf602-8ffd-41de-b550-2cb76e12a656',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7b0d5e1b-2c4d-4ef4-bcae-f5ea4763d203',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.04.2 to 2.18.04.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.004.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.004.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.004.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d7c29d6f-347f-4aa8-9da2-3e62ba2d3868',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('48df8631-69f4-40b1-94fb-4c613256e785',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.04.3 to 2.18.04.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.004.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.004.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.004.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('80c33260-ac30-4b81-a47c-b5c5fdecd784',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f5082e9a-528a-494d-b047-96b6e9432ffc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.04.4 to 2.18.04.4
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.004.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.004.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.004.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a3a4f919-0f25-4e65-83f8-e9bba9016cdd',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('42ffcf70-9425-4735-a47f-6d813cd8e0e2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.04.5 to 2.18.04.5
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.004.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.004.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.004.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.004.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bc9271f2-38e6-4168-b368-e209a4478eea',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ee711196-c33c-44d6-b89c-330e92ea3ca7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.004.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.05.1 to 2.18.05.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.005.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.005.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.005.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.005.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.005.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('661c3787-84cf-4e65-a25b-5cabd781cc7a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.005.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4be18fcf-18df-4ae9-ba78-9008befa2aaf',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.005.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.05.2 to 2.18.05.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.005.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.005.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.005.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.005.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.005.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a566fac1-35ee-48f5-bc7b-a55df54aaec5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.005.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fd08888d-e47b-482d-8926-070de5d563f0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.005.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.05.3 to 2.18.05.3
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.005.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.005.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.005.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.005.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.005.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6029e9f9-9346-43f7-b550-98338815ecf6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.005.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('776d0b9c-d5d6-4b89-9cee-bf2ee6f8533d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.005.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.08.1 to 2.18.08.1
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.008.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.008.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.008.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.008.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.008.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6ed96e50-aadc-4550-ad35-c449fcfcea6e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.008.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ed3d5516-bd56-4965-aea7-2a80ba4e217a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.008.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.08.2 to 2.18.08.2
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.008.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.008.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.008.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.008.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.008.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3e5b50d5-cb45-4937-8014-f9150b8829d5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.008.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c45bc286-7051-42b2-8856-61258687e9d1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.008.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.09.01 to 2.18.09.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('767e254e-1c0b-47a3-b10d-a4e246c93875',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fe7ab221-34c6-4712-9047-6515bd853048',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.09.02 to 2.18.09.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.009.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.009.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.009.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.009.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.009.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cdb5d5ab-c13e-4b66-b0b1-283936871df3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.009.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('98520e75-5578-4f45-ae80-8130c1f57d16',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.009.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.11.01 to 2.18.11.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a27d3c7d-3ac4-41e8-930d-e269de0e96e0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ec0c7258-f404-4a31-ba8b-a72b0994920d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.12.01 to 2.18.12.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a8133258-f770-4e48-ad4c-f0439aaa89a0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b59a87ef-98a1-4c24-9156-5c389bba8fd3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.12.02 to 2.18.12.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.012.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.012.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.012.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.012.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.012.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9fcb4ca4-0e8a-45e5-95d2-16ffdcc6c43a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.012.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('016fa4a4-6c66-46e8-9239-5047b95ac7d7',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.012.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.18.12.03 to 2.18.12.03
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.012.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.018.012.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.018.012.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.018.012.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.018.012.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b2b12952-8d5f-4599-9929-06d11de20634',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.012.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('08663acb-d9dd-4c3f-9f70-6b7a8b91bb63',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.018.012.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.3.01 to 2.19.3.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f703e60d-2c11-46b9-9f6a-f004ba253d0f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ce29aa7c-55fb-4e10-b329-a2f967b2937d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.4.01 to 2.19.4.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6879bb5a-024c-43ed-b4c7-6eb104a24779',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a4412e1c-d1b0-4cac-9e79-bb0677635cdb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.5.01 to 2.19.5.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.005.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.005.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.005.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.005.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.005.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('01435804-f90e-4979-a6dd-aa9236625dcb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.005.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e6ae3e1c-ed05-4d79-b05b-238f5146a034',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.005.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.5.02 to 2.19.5.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.005.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.005.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.005.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.005.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.005.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('92114b9f-3c90-4368-866c-f2c913a58384',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.005.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('02103504-abe5-44f5-a8fe-23f9c66893d4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.005.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.5.03 to 2.19.5.03
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.005.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.005.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.005.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.005.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.005.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d5f0a368-c0d8-4bc5-aa0b-414212c2e1c5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.005.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('40258dc8-5108-4f20-8fc9-aefbb0554393',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.005.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.6.01 to 2.19.6.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1fe57af7-67cd-453e-b3ed-5ec1933c8077',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('21d503ed-fa3a-4397-a8df-2ac67e929b69',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.7.01 to 2.19.7.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.007.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.007.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.007.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.007.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.007.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bf249850-8f42-406b-92b6-e62e7c6cd2ac',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.007.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c451df60-2561-4fa6-bd65-a82f2be9b23f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.007.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.8.01 to 2.19.8.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.008.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.008.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.008.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.008.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.008.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('faf4bd33-7814-462f-9869-5bb8063605ec',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.008.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('60760d5b-1770-4e76-8d9d-fb65bdd7e63d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.008.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.10.01 to 2.19.10.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.010.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.010.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.010.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.010.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.010.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a27031ad-b16d-41ac-b4e8-f7e486c61892',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.010.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c38a44d2-e124-495b-a120-bc10c3bea96e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.010.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.10.02 to 2.19.10.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.010.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.010.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.010.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.010.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.010.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ae7373a0-8388-47df-bb62-ca9f6ed61738',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.010.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6545f918-a99e-4236-bf5e-d222b794c6b0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.010.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.11.01 to 2.19.11.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d7498727-e6ee-447a-b1ae-c2b659c99f55',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ce078b70-b5d6-4d79-a3db-93fba0cb7825',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.19.11.02 to 2.19.11.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.011.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.019.011.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.019.011.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.019.011.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.019.011.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3bcc94de-6e27-4c84-9e3a-03dfbaede67d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.011.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('013354e1-9bef-4466-ab0f-9d709df6401b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.019.011.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.02.01 to 2.20.02.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7613f65b-ef29-4d51-b751-cef2f70072cb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1f1d8025-93cd-4d91-b001-8e4f2ba1b9c0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.02.02 to 2.20.02.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.002.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.002.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.002.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.002.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.002.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f0ffe210-12d3-41c3-80c9-14b935f1a2f1',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.002.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e67a8016-5f80-4e82-a7db-2c9a8a0c14cc',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.002.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.03.01 to 2.20.03.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4df6e333-b12f-4033-9621-2dcd3c8ce745',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e03bf0ee-751e-4051-81bd-e86db510fc5d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.03.02 to 2.20.03.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.003.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.003.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.003.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.003.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.003.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9e1ce355-06e8-42c7-933d-27fb4da5d1b6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.003.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1f9566a9-e87a-4db0-b84d-2c88a6738113',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.003.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.03.03 to 2.20.03.03
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.003.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.003.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.003.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.003.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.003.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2dfa1518-6ebc-42f7-9ad2-cae6d277082b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.003.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ed5bb14d-e937-458b-a6d4-db5097242b85',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.003.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.03.03 to 2.20.04.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration';
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade';
select 
decode(rec_count,0,'@DBUpgrade_002.020.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('2dfa1518-6ebc-42f7-9ad2-cae6d277082f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ed5bb14d-e937-458b-a6d4-db5097242b8f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.04.02 to 2.20.04.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.004.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.004.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.004.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1a96fb83-dc4c-45fd-8795-47424994248d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ca2632f8-34e7-49ed-8e7b-8b7e44d71843',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.04.03 to 2.20.04.03
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.003' and DATABASE_PATCH_TYPE_CD='DataMigration';
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade';
select
decode(rec_count,0,'@DBUpgrade_002.020.004.003_Before.sql',' ')
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.004.003.sql',' ')
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.004.003_After.sql',' ')
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('519b3e37-4e02-40b9-a1cc-ae66cfe4819d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('06a5ac43-06cb-402b-94d8-a996f0308fe0',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.04.04 to 2.20.04.04
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.004' and DATABASE_PATCH_TYPE_CD='DataMigration';
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.004.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade';
select
decode(rec_count,0,'@DBUpgrade_002.020.004.004_Before.sql',' ')
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.004.004.sql',' ')
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.004.004_After.sql',' ')
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('1deda7bc-0378-4804-96ea-ccb736d5688b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('31c6d7e4-1adb-49d1-a478-d8f8f7711edb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.004.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.05.01 to 2.20.05.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.005.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.005.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.005.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.005.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.005.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('cfe80ea4-83f2-4108-8b6b-814a59c0dd62',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.005.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('44d85a01-2b1a-483d-b802-e19c56406340',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.005.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.06.01 to 2.20.06.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a453ca7c-8b58-447d-85ed-073548bf8a85',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ff5f816d-83ed-4de0-9d94-f937c06ff707',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.08.02 to 2.20.08.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.008.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.008.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.008.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.008.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.008.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('6ae46ab3-3524-4fbd-b335-a123bfdaf839',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.008.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('83c6ca12-e311-459c-8598-946393af232e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.008.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.10.01 to 2.20.10.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.010.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.010.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.010.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.010.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.010.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4d77e462-c75b-4f36-bd44-47ede843c6fb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.010.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d9c9f39b-b915-4ead-b21c-60abaf8ca732',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.010.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.20.10.02 to 2.20.10.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.010.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.020.010.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.020.010.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.020.010.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.020.010.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bf584e07-749f-4207-897b-04126a16b848',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.010.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('7a6894eb-c626-4acc-84c6-1b6a897c61f9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.020.010.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.01.01 to 2.21.01.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.001.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.001.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.001.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.001.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.001.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c53c1447-50f6-4fef-a19f-78eea0ef3688',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.001.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4e289f36-3e1b-41b1-aa1a-83107783ea40',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.001.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.02.01 to 2.21.02.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d4591a8e-d3df-43d4-a6fe-476c7b601b0e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4ee5f89c-38b0-4590-9453-ba58b82a41b8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.02.02 to 2.21.02.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.002.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.002.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.002.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.002.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.002.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ac6e5217-a5f2-4031-8ce7-ccd382b2513b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.002.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('93cf9e13-49cd-4e8f-99c9-e38c78d7ea93',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.002.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.03.01 to 2.21.03.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('f8d19b19-982c-4930-bb82-56a16459ddbf',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('97b409e2-ba18-49d8-9ee2-1050c6ad6f40',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.03.02 to 2.21.03.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.003.002' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.003.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.003.002_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.003.002.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.003.002_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b4872473-c1ae-46b6-9127-99dd7b494791',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.003.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('86826802-82e5-4943-beb7-2a654a355cbb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.003.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.04.01 to 2.21.04.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('65708991-6d39-421c-99c7-70aca6370353',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('647da41b-7bf2-48cd-9873-1257f8defa06',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.07.01 to 2.21.07.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.007.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.007.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.007.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.007.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.007.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c7ae2d0c-ac51-4a90-917b-2dcf645c18f3',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.007.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('57aec3b9-4af4-42e1-a6a8-6e6ca242c64f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.007.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.09.01 to 2.21.09.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('d034d4e7-f22d-4598-a2f6-2f68c66ae1d9',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ada3995a-f2d0-4634-bdc3-8fcb732d3a22',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.10.01 to 2.21.10.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.010.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.010.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.010.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.010.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.010.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8352fdfe-abc2-4c55-8d5d-8fb5e4f89c2e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.010.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ac1359fe-05e7-4d40-889a-2e734db54cc2',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.010.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.11.01 to 2.21.11.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('507da3c6-cb92-44cb-a70c-b81d3dfa3fba',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('36c59a0d-0c2f-4501-bbd9-b068ac708556',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.21.12.01 to 2.21.12.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.012.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.021.012.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.021.012.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.021.012.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.021.012.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('9be510a2-a013-4a00-8ef7-aa3f939639fb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.012.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4953a328-617b-40b6-883d-1d02d95dfca6',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.021.012.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.02.01 to 2.22.02.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.002.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.002.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.002.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('899f448d-80fd-4039-be92-48ecd836fc66',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.002.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('71e8d724-80d5-4067-9226-14540f5a7167',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.002.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.02.02 to 2.22.02.02
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.002.002' and DATABASE_PATCH_TYPE_CD='DataMigration';
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.002.002' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade';
select
    decode(rec_count,0,'@DBUpgrade_002.022.002.002_Before.sql',' ')
into sql_str from dual;
dbms_output.put_line(sql_str);

select
    decode(upgraderec_count,0,'@DBUpgrade_002.022.002.002.sql',' ')
into sql_str from dual;
dbms_output.put_line(sql_str);

select
    decode(rec_count,0,'@DBUpgrade_002.022.002.002_After.sql',' ')
into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4f69f4f8-45ed-42f4-be73-99d4f56a878a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.002.002','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('eec0d37e-09a8-4d5c-bfce-e45d3a5fe374',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.002.002','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.03.01 to 2.22.03.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('bb148e34-905c-44be-a4a7-cf44d90a031e',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('23deb578-2c9f-4845-9359-59a663de6284',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.06.01 to 2.22.06.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('8731fad0-8975-4250-a6de-b62d51eb25ca',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e6263f44-0158-48eb-a5ad-2acd248fd325',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.08.01 to 2.22.08.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.008.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.008.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.008.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('12b3ac6b-93fb-4748-93bd-b572a5e5a427',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b39dc77e-4ccb-4b95-993b-a4066928a36b',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.08.03 to 2.22.08.03
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.003' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.003' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.008.003_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.008.003.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.008.003_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('915c14aa-388e-4338-a067-16627edb9d2d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.003','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('946d6166-74ca-49a1-8263-8194235174c4',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.003','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.08.04 to 2.22.08.04
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.004' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.004' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.008.004_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.008.004.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.008.004_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('78e69290-0672-4b28-a6a1-80ac7b6c4021',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.004','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('63d1517b-8d0d-495c-9fdb-19065931fd22',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.004','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.08.05 to 2.22.08.05
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.005' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.005' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.008.005_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.008.005.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.008.005_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fc7408ad-69f6-4ba9-b335-6812f24a5c93',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.005','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('70c6961a-500a-4f13-8dca-7a08b817d345',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.005','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.08.06 to 2.22.08.06
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.006' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.008.006' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.008.006_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.008.006.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.008.006_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('ebf4cf4a-da04-4025-ac8a-2372de96c8de',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.006','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('31c13da3-a768-4c95-acbd-964fe78f3826',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.008.006','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.09.01 to 2.22.09.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.009.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.009.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.009.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.009.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.009.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('e72827d0-f471-4442-9447-9dda1507990f',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.009.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('5c9e5368-28c6-4b11-b9db-51790cfb87f8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.009.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.22.11.01 to 2.22.11.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.011.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.022.011.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.022.011.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.022.011.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.022.011.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('c1852f42-7399-47b2-851f-a2f572743475',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.011.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('4cda558f-c3c8-4123-828f-e90d410c87b8',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.022.011.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.02.01 to 2.23.02.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

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
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.03.01 to 2.23.03.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.003.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.003.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.023.003.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.023.003.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.023.003.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('56e45a81-0083-4eda-b71f-e810c7f6eef5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.003.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('01d358b7-d11c-4c9e-bcfd-46efe6a2124a',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.003.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.04.01 to 2.23.04.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.004.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.004.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.023.004.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.023.004.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.023.004.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('a6fab435-4fff-4cc2-bd40-57d8e2eff6bb',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.004.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('3fc1656d-a601-4927-830a-29a67bc76e1d',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.004.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.05.01 to 2.23.05.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.005.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.005.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.023.005.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.023.005.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.023.005.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('997df445-0f55-43e6-99af-91a573154536',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.005.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('b209d658-131e-4861-ac49-ecd34ce42d22',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.005.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.06.01 to 2.23.06.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.006.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.006.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.023.006.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.023.006.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.023.006.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('49f76ad2-f815-4b79-93d7-5852786c5115',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.006.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('644c1c02-3ac7-423e-a927-04bb371f5aec',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.006.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql
--------------------------------------------------------------------------------------
-- Upgrading from 2.23.07.01 to 2.23.07.01
---------------------------------------------------------------------------------------
spool off
set termout off
set heading off

spool Upgrade.sql

declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.007.001' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.007.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.023.007.001_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(upgraderec_count,0,'@DBUpgrade_002.023.007.001.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

select
 decode(rec_count,0,'@DBUpgrade_002.023.007.001_After.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);

if(rec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('06bec208-c925-4408-b288-e74adf7f91aa',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.007.001','DataMigration') ;

COMMIT;
end if;
if(upgraderec_count = 0) then
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
values ('fbf138f0-d26c-4b6d-9a9f-63d60edd57b5',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.007.001','SchemaUpgrade') ;

COMMIT;
end if;
end;
/

spool off
set termout on
set heading on

spool InstallDB.log append

@Upgrade.sql

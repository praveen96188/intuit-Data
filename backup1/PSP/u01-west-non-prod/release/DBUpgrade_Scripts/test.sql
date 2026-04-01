set echo on feedback on 
set serveroutput on
declare
 rec_count number;
 upgraderec_count number;
 sql_str   varchar2(100);
begin
select count(*) into rec_count from PSPADM.PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = 'test.002.000.000.035' and DATABASE_PATCH_TYPE_CD='DataMigration'; 
select count(*) into upgraderec_count from PSPADM.PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.000.000.035' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; 
select 
decode(rec_count,0,'@DBUpgrade_002.000.000.035_Before.sql',' ') 
 into sql_str from dual;
dbms_output.put_line(sql_str);
end;
/


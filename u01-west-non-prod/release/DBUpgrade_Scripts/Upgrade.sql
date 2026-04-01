SQL> 
SQL> alter session set current_schema=pspadm;

Session altered.

Elapsed: 00:00:00.00
SQL> 
SQL> declare
  2   rec_count number;
  3   upgraderec_count number;
  4   sql_str	varchar2(100);
  5  begin
  6  select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.002.001' and DATABASE_PATCH_TYPE_CD='DataMigration';
  7  select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '002.023.002.001' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade';
  8  select
  9  decode(rec_count,0,'@DBUpgrade_002.023.002.001_Before.sql',' ')
 10   into sql_str from dual;
 11  dbms_output.put_line(sql_str);
 12  
 13  select
 14   decode(upgraderec_count,0,'@DBUpgrade_002.023.002.001.sql',' ')
 15   into sql_str from dual;
 16  dbms_output.put_line(sql_str);
 17  
 18  select
 19   decode(rec_count,0,'@DBUpgrade_002.023.002.001_After.sql',' ')
 20   into sql_str from dual;
 21  dbms_output.put_line(sql_str);
 22  
 23  if(rec_count = 0) then
 24  insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
 25  values ('aff8dfda-ef3f-4608-9829-c5201d17f5db',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.002.001','DataMigration') ;
 26  
 27  COMMIT;
 28  end if;
 29  if(upgraderec_count = 0) then
 30  insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
 31  values ('d14ca32d-607c-4d7a-bfea-c41cfca80563',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '002.023.002.001','SchemaUpgrade') ;
 32  
 33  COMMIT;
 34  end if;
 35  end;
 36  /
@DBUpgrade_002.023.002.001_Before.sql                                           
@DBUpgrade_002.023.002.001.sql                                                  
@DBUpgrade_002.023.002.001_After.sql                                            

PL/SQL procedure successfully completed.

Elapsed: 00:00:00.01
SQL> 
SQL> spool off

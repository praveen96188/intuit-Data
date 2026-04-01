--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\PSP\v1-maint\PSE\Domain\src\main\model\DBUpgrade_001.009.010.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
PROMPT Before Insert;

insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
select UNIQUE_ID,0,'System',CREATED_DATE,MODIFIED_DATE, lpad(substr(SCHEMA_VERSION,0,instr(SCHEMA_VERSION,'.',1,1)-1),3,'000') || '.' ||  lpad(substr(SCHEMA_VERSION,instr(SCHEMA_VERSION,'.',1,1)+1,(instr(SCHEMA_VERSION,'.',1,2)) - (instr(SCHEMA_VERSION,'.',1,1))-1 ),3,'000') || '.' ||  lpad(substr(SCHEMA_VERSION,instr(SCHEMA_VERSION,'.',1,2)+1,(instr(SCHEMA_VERSION,'.',1,3)) - (instr(SCHEMA_VERSION,'.',1,2))-1 ),3,'000')|| '.' ||  lpad(substr(SCHEMA_VERSION,instr(SCHEMA_VERSION,'.',1,3)+1),3,'000'), 'SchemaUpgrade' from spcfmodelinfo where MODEL_ALIAS = 'PSP'
/
insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
select '1a4c1b54-dc53-49a1-8676-d31a23423721',0,'System',CREATED_DATE,MODIFIED_DATE, lpad(substr(SCHEMA_VERSION,0,instr(SCHEMA_VERSION,'.',1,1)-1),3,'000') || '.' ||  lpad(substr(SCHEMA_VERSION,instr(SCHEMA_VERSION,'.',1,1)+1,(instr(SCHEMA_VERSION,'.',1,2)) - (instr(SCHEMA_VERSION,'.',1,1))-1 ),3,'000') || '.' ||  lpad(substr(SCHEMA_VERSION,instr(SCHEMA_VERSION,'.',1,2)+1,(instr(SCHEMA_VERSION,'.',1,3)) - (instr(SCHEMA_VERSION,'.',1,2))-1 ),3,'000')|| '.' ||  lpad(substr(SCHEMA_VERSION,instr(SCHEMA_VERSION,'.',1,3)+1),3,'000'), 'DataMigration' from spcfmodelinfo where MODEL_ALIAS = 'PSP'
/
SHOW ERRORS;

COMMIT;	
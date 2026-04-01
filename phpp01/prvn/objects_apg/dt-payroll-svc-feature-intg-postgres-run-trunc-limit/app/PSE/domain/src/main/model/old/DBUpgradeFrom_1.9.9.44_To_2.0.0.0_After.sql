--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.44_To_2.0.0.0.sql
--
-- Developers can hand code logic here for data migration purposes
--
PROMPT Before update 

SELECT COUNT(*) FROM PSP_COMPANY WHERE OFFLOAD_GROUP_FK is null;

-- Populate offload group fk in company
BEGIN
   FOR REC IN (
                  SELECT DD.OFFLOAD_GROUP_FK as OFFLOAD_GROUP,COMPANY_SEQ  from PSP_COMPANY_SERVICE cs, PSP_DDCOMPANY_SERVICE_INFO dd, PSP_COMPANY comp
                  WHERE CS.COMPANY_FK = COMP.COMPANY_SEQ
                  and CS.COMPANY_SERVICE_SEQ = DD.DDCOMPANY_SERVICE_INFO_SEQ
              )
   LOOP   
       UPDATE PSP_COMPANY set OFFLOAD_GROUP_FK=REC.OFFLOAD_GROUP where
       PSP_COMPANY.COMPANY_SEQ=REC.COMPANY_SEQ;
   END LOOP;
END;

/
SHOW ERRORS;

COMMIT;

PROMPT After update 

SELECT COUNT(*) FROM PSP_COMPANY WHERE OFFLOAD_GROUP_FK is null;

--Drop offload group fk from PSP_DDCOMPANY_SERVICE_INFO
ALTER TABLE PSP_DDCOMPANY_SERVICE_INFO DROP COLUMN OFFLOAD_GROUP_FK;

-- make contraint not null after population
ALTER TABLE PSP_COMPANY 
MODIFY (OFFLOAD_GROUP_FK NOT NULL); 
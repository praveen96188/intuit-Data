--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.000.000.035.sql
--
-- Developers can hand code logic here for data migration purposes
--


PROMPT UPDATNG PSP_INDIVIDUAL

PROMPT Count contact
select count(*) from PSP_CONTACT where title_suffix is not null
/

UPDATE PSP_INDIVIDUAL a 
SET  SUFFIX = (SELECT TITLE_SUFFIX FROM PSP_CONTACT
                   WHERE contact_Seq=a.individual_seq and TITLE_SUFFIX is not null) 
WHERE SUFFIX IS NULL
/

COMMIT;

PROMPT Count individual

select count(*) from PSP_INDIVIDUAL where SUFFIX is not null
/

 
PROMPT Before update PSP_COMPANY_EVENT_DETAIL - OldDDServiceStatus

select count(*) from psp_company_event_detail where EVENT_DETAIL_TYPE_CD ='OldDDServiceStatus';

update psp_company_event_detail 
set EVENT_DETAIL_TYPE_CD ='OldServiceStatus' ,      MODIFIED_DATE=sysdate
where EVENT_DETAIL_TYPE_CD ='OldDDServiceStatus';

COMMIT;

PROMPT After update PSP_COMPANY_EVENT_DETAIL - OldDDServiceStatus

PROMPT Before update PSP_COMPANY_EVENT_DETAIL - NewDDServiceStatus

select count(*) from psp_company_event_detail where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus';

update psp_company_event_detail set EVENT_DETAIL_TYPE_CD ='NewServiceStatus' ,      MODIFIED_DATE=sysdate
where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus';

COMMIT;

PROMPT After update PSP_COMPANY_EVENT_DETAIL - NewDDServiceStatus

select count(*) from psp_company_event_detail where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus'
/


PROMPT After update PSP_TRANSACTION_TYPE - Association_Type

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

-- make contraint not null after population
ALTER TABLE PSP_COMPANY 
MODIFY (OFFLOAD_GROUP_FK NOT NULL); 

Prompt Column OFFLOAD_GROUP_FK;

ALTER TABLE PSP_DDCOMPANY_SERVICE_INFO
MODIFY(OFFLOAD_GROUP_FK NULL);




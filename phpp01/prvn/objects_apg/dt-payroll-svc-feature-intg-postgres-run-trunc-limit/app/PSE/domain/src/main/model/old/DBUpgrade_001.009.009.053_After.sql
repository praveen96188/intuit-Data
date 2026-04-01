--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\rel-1.3\PSE\Domain\src\main\model\DBUpgrade_001.009.009.053.sql
--
-- Developers can hand code logic here for data migration purposes
--

Prompt Before Update;
SELECT COUNT(*) FROM PSP_APPLIED_DATABASE_PATCH WHERE DATABASE_PATCH_TYPE_CD is null;

BEGIN
  FOR rec IN (SELECT DISTINCT a.company_offer_Seq FROM psp_company_offer a, psp_offer b
				     WHERE a.offer_fk = b.offer_Seq
				     AND  a.company_fk IN (SELECT company_seq from PSP_COMPANY
				     			    WHERE trunc(sign_up_date) > trunc(to_date('07/01/2009','mm/dd/yyyy')))
				     AND b.offer_cd in ('P60708','P61460','P61461','P60241')
				   )
   LOOP
     BEGIN

			UPDATE PSP_COMPANY_OFFER
			   SET MODIFIER_ID =  'MANUAL_08312009', 
			    MODIFIED_DATE  = SYSTIMESTAMP, 
			    BEGIN_DATE = TO_TIMESTAMP('8/31/2009 7:00:00.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'),
			    END_DATE =  TO_TIMESTAMP('12/31/2009 12:00:00.000000 PM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
			    OFFER_FK =  '71712062-380c-f20f-e040-10ac154336f5' 
			  WHERE company_offer_Seq = rec.company_offer_Seq ;
     END;
   END LOOP;


    FOR rec_company IN (SELECT company_seq FROM psp_company
				     WHERE trunc(sign_up_date) > trunc(to_date('07/01/2009','mm/dd/yyyy'))
				     AND  company_seq NOT IN (SELECT company_fk from PSP_COMPANY_OFFER)
				   )
    LOOP
	  BEGIN

			Insert into PSP_COMPANY_OFFER
			   (COMPANY_OFFER_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, 
			    MODIFIED_DATE, REALM_ID, BEGIN_DATE, END_DATE, USAGES_REMAINING, 
			    OFFER_FK, COMPANY_FK)
			 Values
			   (FN_FORMAT_SYSGUID(SYS_GUID()), 0, 'MANUAL_08312009', SYSTIMESTAMP, 'MANUAL_08312009', 
			   SYSTIMESTAMP, -1, TO_TIMESTAMP('8/31/2009 7:00:00.000000 AM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), TO_TIMESTAMP('12/31/2009 12:00:00.000000 PM','fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 0, 
			    '71712062-3812-f20f-e040-10ac154336f5', rec_company.company_seq);
	  END;
    END LOOP;
   FOR REC IN (
                  SELECT APPLIED_DATABASE_PATCH_SEQ as PATCH_SEQ,DATABASE_PATCH_VERSION AS PATCH_VERSION FROM PSP_APPLIED_DATABASE_PATCH WHERE DATABASE_PATCH_TYPE_CD is null
              )
   LOOP 
    insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)
    values (FN_FORMAT_SYSGUID(SYS_GUID()),0,'System',SYSTIMESTAMP, SYSTIMESTAMP, REC.PATCH_VERSION,'DataMigration') ;
    
    UPDATE PSP_APPLIED_DATABASE_PATCH SET DATABASE_PATCH_TYPE_CD = 'SchemaUpgrade' where APPLIED_DATABASE_PATCH_SEQ = REC.PATCH_SEQ;   
   END LOOP;
END;
	   
/
SHOW ERRORS;

COMMIT;	   

Prompt After Update;
SELECT COUNT(*) FROM PSP_APPLIED_DATABASE_PATCH WHERE DATABASE_PATCH_TYPE_CD is null;

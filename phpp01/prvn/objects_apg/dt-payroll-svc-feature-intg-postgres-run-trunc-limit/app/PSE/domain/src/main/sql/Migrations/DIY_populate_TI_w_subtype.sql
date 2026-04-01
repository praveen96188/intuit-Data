-- CREATED: 01.20.2008
--  AUTHOR: EMR
-- PURPOSE:
--   The intent of this script is to populate DIY migrated companies that are
--   terminated, and had the Assisted service at the time of termination, with
--   a subtype of Assisted.  This is needed due to a dependency with EWS adapter.
--
--   This script should be run as PSPADM.


SET SERVEROUTPUT ON
SET HEADING      ON 
SET DEFINE       OFF

SPOOL DIY_populate_TI_w_subtype.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;


PROMPT The number of DIY Assisted terminated companies without a subtype before the update.

-- how to isolate Assisted.  are they the only ones with null.  Do all DD only have 
-- subtype filled in.

SELECT COUNT(*) 
  FROM psp_company         c,
       psp_company_service cs  
 WHERE cs.company_fk                = c.company_seq
   AND c.source_system_cd           = 'QBDT'
   AND cs.status_cd                 = 'Terminated'
   AND c.AGREE_INFO_AGREE_SUB_TYPE IS NULL
   AND c.MIGRATION_STATUS           = 'MigratedFromAS400';


PROMPT Update the subtype for DIY Assisted terminated companies.

UPDATE PSP_COMPANY c
   SET AGREE_INFO_AGREE_SUB_TYPE = 'Assisted'
 WHERE EXISTS (
         SELECT 'T' 
           FROM psp_company_service cs  
          WHERE cs.company_fk                = c.company_seq
            AND c.source_system_cd           = 'QBDT'
            AND cs.status_cd                 = 'Terminated'
            AND c.AGREE_INFO_AGREE_SUB_TYPE IS NULL
            AND c.MIGRATION_STATUS           = 'MigratedFromAS400'
       );


PROMPT The number of DIY Assisted terminated companies without a subtype after the update.

SELECT COUNT(*) 
  FROM psp_company         c,
       psp_company_service cs  
 WHERE cs.company_fk                = c.company_seq
   AND c.source_system_cd           = 'QBDT'
   AND cs.status_cd                 = 'Terminated'
   AND c.AGREE_INFO_AGREE_SUB_TYPE IS NULL
   AND c.MIGRATION_STATUS           = 'MigratedFromAS400';


PROMPT Reminder to manually commit or rollback.

SPOOL OFF

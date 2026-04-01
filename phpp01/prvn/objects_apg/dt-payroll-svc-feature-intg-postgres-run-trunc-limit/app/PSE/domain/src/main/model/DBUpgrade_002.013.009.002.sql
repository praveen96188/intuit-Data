--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table PSP_EMP_TOTALS_PAYROLL_RUN;
-- This table keeps track of new/changes of assisted payroll runs. If any "Pending" payroll runs, EE Totals calculation job needs to recalculate totals for that quarter and company.
-- EE Totals calculation job use this table to identify company and quarter that needs to be recalculated for employee quarterly totals.
CREATE TABLE PSP_EMP_TOTALS_PAYROLL_RUN
(
  EMP_TOTALS_PAYROLL_RUN_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  VERSION                     NUMBER(19)        NOT NULL,
  CREATOR_ID                  VARCHAR2(30 CHAR),
  CREATED_DATE                TIMESTAMP(6)      NOT NULL,
  MODIFIER_ID                 VARCHAR2(30 CHAR),
  MODIFIED_DATE               TIMESTAMP(6)      NOT NULL,
  REALM_ID                    NUMBER(19)        DEFAULT -1                    NOT NULL,
  QUARTER_START_DATE          TIMESTAMP(6),
  STATUS                      VARCHAR2(255 CHAR),
  PAYROLL_RUN_FK              VARCHAR2(255 CHAR) NOT NULL,
  COMPANY_FK                  VARCHAR2(255 CHAR) NOT NULL
)
NOPARALLEL;

declare 
    v_db_name varchar2(100);
begin
    select name into v_db_name from v$database;
    if(v_db_name in ('PSPQA01', 'PSPQA06','PSPTRN01','PSPDV03','PSPDEV01','PSPPP01','XE','ORCL'))
    then
       dbms_output.put_line('Prompt Index PSP_EMP_TOTALS_PAYROLL_RUN_FK1');
        
       execute immediate ' CREATE INDEX PSP_EMP_TOTALS_PAYROLL_RUN_FK1 ON PSP_EMP_TOTALS_PAYROLL_RUN
        (PAYROLL_RUN_FK, REALM_ID)
        NOPARALLEL';

        dbms_output.put_line('Prompt Index PSP_EMP_TOTALS_PAYROLL_RUN_FK2');
       
       execute immediate ' CREATE INDEX PSP_EMP_TOTALS_PAYROLL_RUN_FK2 ON PSP_EMP_TOTALS_PAYROLL_RUN
        (COMPANY_FK, REALM_ID)
        NOPARALLEL' ;

        execute immediate' ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
         ADD CONSTRAINT C_PSP_EMP_TOTALS_PAYROLL_R0
          CHECK (STATUS IN(''Pending'', ''Processed''))';

        execute immediate' ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
         ADD PRIMARY KEY
          (EMP_TOTALS_PAYROLL_RUN_SEQ, REALM_ID)
          USING INDEX';

        execute immediate ' ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
         ADD CONSTRAINT PSP_EMP_TOTALS_PAYROLL_RUN_FK1 
          FOREIGN KEY (PAYROLL_RUN_FK, REALM_ID) 
          REFERENCES PSP_PAYROLL_RUN (PAYROLL_RUN_SEQ,REALM_ID)';

        execute immediate ' ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
         ADD CONSTRAINT PSP_EMP_TOTALS_PAYROLL_RUN_FK2 
          FOREIGN KEY (COMPANY_FK, REALM_ID) 
          REFERENCES PSP_COMPANY (COMPANY_SEQ,REALM_ID)';
    ELSE 
        dbms_output.put_line( 'This is not a QA/DEV env - post_deploy_2013R9.sql needs to be run manually after the automatic deploy');
    END IF;
END;
/


PROMPT finished DBUpgrade_002.013.009.002.sql
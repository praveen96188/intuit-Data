set lines 300 echo on timing on term on echo on feedback on trimspool on num 30 serveroutput on
spool monolith_chk_max_clob_size
select /*+ parallel(1,4) */max(
    dbms_lob.getlength(FILE_CONTENT)
  ) / 1024 / 1024 AS default_getlength_Size_MB, 
  max(
    dbms_lob.getlength(FILE_CONTENT_ENC)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_ACHENROLLMENT_FILE;

select 
 max(
    dbms_lob.getlength(SIGNATURE)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_CHECK_PRINT_SIGNATURE;

select /*+ parallel(1,4) */max(
    dbms_lob.getlength(MESSAGE)
  ) / 1024 / 1024 AS default_getlength_Size_MB, 
--  max(
--    dbms_lob.getlength(MESSAGE_BKP)
--  ) / 1024 / 1024 AS default_getlength_Size_MB,
  max(
    dbms_lob.getlength(MESSAGE_ENC)
  ) / 1024 / 1024 AS default_getlength_Size_MB  
from 
  pspadm.PSP_ENTITLEMENT_MESSAGE;

select 
 max(
    dbms_lob.getlength(CHANGED_ATTRIBUTES)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_ENTITY_UPDATE;

select /*+ parallel(1,4) */max(
    dbms_lob.getlength(ORIGINAL_FILE)
  ) / 1024 / 1024 AS default_getlength_Size_MB, 
  max(
    dbms_lob.getlength(PROCESSED_FILE)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_LEDGER_OPERATION_JOB;

select /*+ parallel(1,4) */max(
    dbms_lob.getlength(REQUEST_LOG)
  ) / 1024 / 1024 AS default_getlength_Size_MB, 
  max(
    dbms_lob.getlength(RESPONSE_LOG)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_MESSAGE_LOG;

select 
 max(
    dbms_lob.getlength(RTB_BACKUP)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_RTBAUTOMATIONBACKUP;

select 
 max(
    dbms_lob.getlength(QUERY)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_SAVED_REPORTS;

select
 max(
    dbms_lob.getlength(VALIDATION_ERROR_RESULT)
  ) / 1024 / 1024 AS default_getlength_Size_MB
from
  pspadm.PSP_SMSMIGRATION;

select /*+ parallel(1,4) */max(
    dbms_lob.getlength(BANK_LOGO)
  ) / 1024 / 1024 AS default_getlength_Size_MB, 
  max(
    dbms_lob.getlength(SOURCE_SYSTEM_LOGO)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_SOURCESYS_PRINTEDCHK_INFO;

select 
 max(
    dbms_lob.getlength(S_Q_L)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_SQL_EXECUTION_LOG_ENTRY;

select 
 max(
    dbms_lob.getlength(REPORT_OUTPUT)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_STATE_REPORT_OUTPUT;

select 
 max(
    dbms_lob.getlength(PROCESSED_FILE)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_SUICREDITS_JOB;

select 
 max(
    dbms_lob.getlength(FORM9061)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_TAX_CREDITS9061;

select /*+ parallel(1,4) */max(
    dbms_lob.getlength(SIGNED_DOCUMENT)
  ) / 1024 / 1024 AS default_getlength_Size_MB, 
  max(
    dbms_lob.getlength(UNSIGNED_DOCUMENT)
  ) / 1024 / 1024 AS default_getlength_Size_MB 
from 
  pspadm.PSP_TAX_CREDITS_APPLICATION;





CREATE OR REPLACE PACKAGE PK_IOPDDTOPSP_VALIDATION AS
/******************************************************************************
   NAME:       PK_IOPDDTOPSP_VALIDATION
   UPDATED:    09.17.2008 10:00 AM   
   PURPOSE:    Validate company data once it is migrated from PSE to PSP.
               Might also be used to validate DIY data migration.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        04.01.2008  EMR              created validation framework.
   1.1        05.02.2008  EMR              added monitoring api
   1.2        05.08.2008  EMR              added purge api to erase logs
   1.3        06.10.2008  EMR              changed payroll status compare UC 66
   1.4        06.10.2008  EMR              changed IOP to QBOE to match PSP
   1.5        07.02.2008  EMR              remapped txn types, pse to psp
   2.0        07.08.2008  EMR              adapted to common framework         
   2.0.1      07.17.2008  EMR              adapted to common framework   
   2.0.2      09.17.2008  EMR              added support for psp on hold   
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- PACKAGE TYPE DEFINITIONS
  -- ------------------------------------------------------------------------

  pc_raise_app_err_cd              CONSTANT NUMBER       := -20052;

  pc_validation_mode_comp          CONSTANT VARCHAR2(20) := 'C';
  pc_validation_mode_summary       CONSTANT VARCHAR2(20) := 'S';
  
  pc_Event_Type_Error              CONSTANT VARCHAR2(10) := 'ERR';  -- bad event
  pc_Event_Type_Warn               CONSTANT VARCHAR2(10) := 'WARN'; -- informational event  
  pc_Event_Type_Info               CONSTANT VARCHAR2(10) := 'INFO'; -- informational event
  pc_Event_Type_Summary            CONSTANT VARCHAR2(10) := 'SUM';  -- summary      
  

  -- ------------------------------------------------------------------------
  -- PRIVATE PACKAGE FUNCTIONS
  -- ------------------------------------------------------------------------
  
  -- these are used for mapping PSP codes back to PSE codes

  FUNCTION FN_GET_PSP_DD_STATUS (
    p_PSE_COMPANY_DB_ID            IN  NUMBER)
	RETURN VARCHAR2;


  FUNCTION FN_GET_PSP_ONHOLD_COUNT (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
	RETURN NUMBER;    

  
  FUNCTION FN_GET_PSE_LEDGER_ACCT (
    p_PSP_LEDGER_ACCT_CD           IN  VARCHAR2)
	RETURN LEDGER.LEDGER_ACCT_CD%TYPE;
  
  
  FUNCTION FN_GET_PSP_PAYROLL_STATUS (
    p_PSE_PAYROLL_STATUS_CD        IN  VARCHAR2)
	RETURN VARCHAR2;

	
  FUNCTION FN_GET_PSP_TXN_TYPE (
    p_PSE_TXN_TYPE_CD              IN  TXN_TYPE_VAL.TXN_TYPE_CD%TYPE)
	RETURN VARCHAR2;
	

  FUNCTION FN_GET_PSE_TXN_STATE (
    p_PSP_TXN_STATE_CD             IN  VARCHAR2)
	RETURN TXN_STATE_VAL.TXN_STATE_CD%TYPE;


  FUNCTION FN_GET_PSE_EVENT_TYPE (
    p_PSP_EVENT_TYPE_CD             IN  VARCHAR2)
	RETURN EVENT_TYPE_VAL.EVENT_TYPE_CD%TYPE;
  

  -- ------------------------------------------------------------------------
  -- PUBLIC APIS
  -- ------------------------------------------------------------------------

  -- This public api validates just 1 company
  PROCEDURE PR_Validate_IOP_Company (
    p_PSE_COMPANY_GSEQ             IN  NUMBER,
	p_PSP_COMPANY_GUID             IN  VARCHAR2,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  -- This public api validates all companies and is expected to run at the end
  -- of migration.
  PROCEDURE PR_Validate_IOP_Migration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
 
 
END PK_IOPDDTOPSP_VALIDATION; 
/


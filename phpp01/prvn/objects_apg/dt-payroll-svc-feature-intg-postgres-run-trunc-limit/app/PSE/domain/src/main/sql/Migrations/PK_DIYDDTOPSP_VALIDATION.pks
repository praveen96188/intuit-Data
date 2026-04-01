CREATE OR REPLACE PACKAGE PK_DIYDDTOPSP_VALIDATION AS
/******************************************************************************
   NAME:       PK_DIYDDTOPSP_VALIDATION
   UPDATED:    07.23.2008 11:00 AM   
   PURPOSE:    Validate company data once it is migrated from the AS400 to PSP.
               

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        07.14.2008  EMR              created validation framework.
   1.1        07.17.2008  EMR              added support for company validation
   1.1.1      07.18.2008  EMR              used new range for raise app err   
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- PACKAGE TYPE DEFINITIONS
  -- ------------------------------------------------------------------------

  pc_raise_app_err_cd              CONSTANT NUMBER       := -20055;

  pc_validation_mode_comp          CONSTANT VARCHAR2(20) := 'C';
  pc_validation_mode_summary       CONSTANT VARCHAR2(20) := 'S';
  
  pc_Event_Type_Error              CONSTANT VARCHAR2(10) := 'ERR';  -- bad event
  pc_Event_Type_Warn               CONSTANT VARCHAR2(10) := 'WARN'; -- informational event  
  pc_Event_Type_Info               CONSTANT VARCHAR2(10) := 'INFO'; -- informational event
  pc_Event_Type_Summary            CONSTANT VARCHAR2(10) := 'SUM';  -- summary      
  

  -- ------------------------------------------------------------------------
  -- PRIVATE PACKAGE FUNCTIONS
  -- ------------------------------------------------------------------------

  FUNCTION FN_GET_PSP_ER_LIMIT (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
    RETURN NUMBER;
    
  FUNCTION FN_GET_PSP_EE_LIMIT (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
    RETURN NUMBER;


  -- ------------------------------------------------------------------------
  -- PUBLIC APIS
  -- ------------------------------------------------------------------------

  -- This public api validates just 1 company
  PROCEDURE PR_Validate_DIY_Company (
    p_DIY_COMPANY_USERID           IN  NUMBER,     -- SOURCE
    p_PSP_COMPANY_GUID             IN  VARCHAR2,   -- TARGET
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  -- This public api validates all companies and is expected to run at the end
  -- of migration.
  PROCEDURE PR_Validate_DIY_Migration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
 
 
END PK_DIYDDTOPSP_VALIDATION; 
/


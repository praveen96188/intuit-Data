CREATE OR REPLACE PACKAGE PK_IOPDDTOPSP_CONTROLLER AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_CONTROLLER
   UPDATED: 07.18.2008 02:30 PM
   PURPOSE: Provide an interface for Java to manage the migration state
            of DIY companies being transferred from the AS/400 to PSP.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.13.2008  EMR              Created this package.
   1.1        06.27.2008  EMR              added additional controller apis
                                           for e2e migration.
   2.0        07.08.2008  EMR              adapted framework for IOP
   2.0.1      07.15.2008  EMR              changed api type to match company 
                                           table
   2.0.2      07.15.2008  EMR              used new range for raise app err                                           
   2.0.3      07.18.2008  EMR              changed comp validate signature   
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- PACKAGE TYPE DEFINITIONS
  -- ------------------------------------------------------------------------

  pc_purge_flag                    CONSTANT VARCHAR2(10) := 'MIGPURGE';
  pc_raise_app_err_cd              CONSTANT NUMBER       := -20051;  


  -- ------------------------------------------------------------------------
  -- PUBLIC APIS
  -- ------------------------------------------------------------------------

  PROCEDURE GetCompanyToMigrate (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_IOP_SRC_COMPANY_ID           OUT COMPANY.SOURCE_COMPANY_ID%TYPE
  );


  PROCEDURE ValidateMigratedCompany (
    p_IOP_SRC_COMPANY_ID           IN  COMPANY.SOURCE_COMPANY_ID%TYPE,
	p_PSP_COMPANY_GUID             IN  VARCHAR2,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  PROCEDURE SetCompanyMigrationToComplete (
    p_IOP_SRC_COMPANY_ID           IN  COMPANY.SOURCE_COMPANY_ID%TYPE,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  PROCEDURE SetCompanyMigrationToError (
    p_IOP_SRC_COMPANY_ID           IN  COMPANY.SOURCE_COMPANY_ID%TYPE,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  PROCEDURE ValidateEntireMigration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  -- fun with sql and pivot charts  
  PROCEDURE GetMigrationSummary (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  PROCEDURE InitializeMigrationQueue (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
  

  -- purge any previous migration log entries
  PROCEDURE PurgeMigrationLogs (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
  

END PK_IOPDDTOPSP_CONTROLLER; 
/


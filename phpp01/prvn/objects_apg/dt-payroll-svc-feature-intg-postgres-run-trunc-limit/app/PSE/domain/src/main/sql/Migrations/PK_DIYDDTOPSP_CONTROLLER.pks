CREATE OR REPLACE PACKAGE PK_DIYDDTOPSP_CONTROLLER AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_CONTROLLER
   UPDATED: 07.18.2008 04:00 PM  
   PURPOSE: Provide an interface for Java to manage the migration state
            of DIY companies being transferred from the AS/400 to PSP.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.13.2008  EMR              Created this package.
   1.1        06.27.2008  EMR              added additional controller apis
                                             for e2e migration.
   2.0        07.11.2008  EMR              syncronized with PSE controller
   2.0.1      07.16.2008  EMR              modified initialize for phasing                                    
   2.0.2      07.18.2008  EMR              used new range for raise app err   
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- PACKAGE TYPE DEFINITIONS
  -- ------------------------------------------------------------------------

  pc_raise_app_err_cd              CONSTANT NUMBER       := -20051;  

  gc_validation_mode_comp          CONSTANT VARCHAR2(20) := 'C';
  gc_validation_mode_summary       CONSTANT VARCHAR2(20) := 'S';


  -- ------------------------------------------------------------------------
  -- PUBLIC APIS
  -- ------------------------------------------------------------------------

  PROCEDURE GetCompanyToMigrate (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_DIY_SRC_COMPANY_ID           OUT NUMBER         -- IQCLIENT.CLI_USERID 
  );


  PROCEDURE SetCompanyToSyncing (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );

  PROCEDURE ValidateMigratedCompany (
    p_DIY_COMPANY_USERID           IN  NUMBER,        -- SOURCE ID: CLI_USERID
	p_PSP_COMPANY_GUID             IN  VARCHAR2,      -- TARGET ID: COMPANY_SEQ
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
    
  
  PROCEDURE SetCompanyMigrationToComplete (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
  

  PROCEDURE SetCompanyMigrationToError (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  PROCEDURE ValidateEntireMigration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );


  PROCEDURE GetMigrationSummary (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );
  

  PROCEDURE InitializeMigrationQueue (
    p_MIGRATION_PHASE_ID           IN  COMPANY_MIGRATION.MIGRATION_PHASE_ID%TYPE,  
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  );

END PK_DIYDDTOPSP_CONTROLLER; 
/


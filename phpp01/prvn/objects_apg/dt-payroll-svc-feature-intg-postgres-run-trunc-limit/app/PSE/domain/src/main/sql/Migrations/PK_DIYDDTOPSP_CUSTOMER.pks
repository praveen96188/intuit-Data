CREATE OR REPLACE PACKAGE PK_DIYDDTOPSP_CUSTOMER AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_CUSTOMER
   UPDATED: 06.16.2008  02:00 PM   
   PURPOSE: Provide an interface for Java into the AS/400 to retrieve
            DIY Direct Deposit data.

            The following subject areas will be retrieved with this package:
			   - company
			   - company contact
			   - company direct deposit service settings
			   - company bank account
			   - company events
			   - company migration metadata

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.13.2008  EMR              Created this package.
   1.1        06.23.2008  EMR              Enhanced company to v1.1
   1.2        06.23.2008  EMR              Split contact ref cursor 
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- Package type definitions
  -- ------------------------------------------------------------------------

  pc_raise_app_err_cd              CONSTANT NUMBER       := -20052;

  TYPE refcur_company             IS REF CURSOR;
  TYPE refcur_comp_contact        IS REF CURSOR;
  TYPE refcur_comp_princ_contact  IS REF CURSOR;  
  TYPE refcur_comp_bankacct       IS REF CURSOR;
  TYPE refcur_comp_events         IS REF CURSOR;


  -- ------------------------------------------------------------------------
  -- Public APIs
  -- ------------------------------------------------------------------------

  -- PASS 1: prove interface between PSP and AS/400

  PROCEDURE migrateCompanyInfo (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMPANY_REFCUR               OUT refcur_company
  );

  PROCEDURE migrateContacts (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMP_CONTACT_REFCUR          OUT refcur_comp_contact,
    p_COMP_PRINC_CONTACT_REFCUR    OUT refcur_comp_princ_contact    
  );

  PROCEDURE migrateDDService (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMPANY_REFCUR               OUT refcur_company
  );


  -- PASS 2: allow PSP pay submit

  PROCEDURE migrateCompanyBankAccounts (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMP_BANKACCT_REFCUR         OUT refcur_comp_bankacct
  );


  PROCEDURE migrateCompanyEvents (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMP_STRIKE_COUNT            OUT NUMBER,    
    p_COMP_EVENTS_REFCUR           OUT refcur_comp_events
  );
  
  
END PK_DIYDDTOPSP_CUSTOMER; 
/


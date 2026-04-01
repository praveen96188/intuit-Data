CREATE OR REPLACE PACKAGE PK_TEST_COMPANY AS
/******************************************************************************
   NAME:       PK_TEST_COMPANY
   UPDATED: 07.15.2008 11:00 AM   
   PURPOSE:    test cases for all company apis.  uses ref cursors.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        03.03.2008  EMR              created test cases for pass 1 and 2
   1.1        06.24.2008  EMR              created test cases for slice 8
   1.2        07.15.2008  EMR              added support to loop all data
******************************************************************************/

  PROCEDURE TestAllApis;
   

  -- round 1 in March
  
  PROCEDURE GetCompany (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
  
  PROCEDURE GetContacts (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
  
  PROCEDURE GetDDInfo (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
  
  PROCEDURE GetBankAcct (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
  
  
  -- round 2 in June for slice 8
  
  PROCEDURE GetEvents (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
  
  PROCEDURE GetEmployees (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
  
  PROCEDURE GetEEBankAcct (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_DIY_SOURCE_EMP_ID            IN  VARCHAR2
  );
  
  PROCEDURE GetPayRuns (
    p_DIY_COMPANY_ID               IN  NUMBER
  );
    
  PROCEDURE GetPaychecks (
    p_DIY_COMPANY_ID               IN  NUMBER,  
    p_PAYROLL_RUN_ID               IN  VARCHAR2
  );
  
  PROCEDURE GetPaycheckSplits (
    p_DIY_COMPANY_ID               IN  NUMBER,  
	p_SOURCE_PAYCHECK_ID           IN  VARCHAR2
  );
  
  PROCEDURE GetTxns (
    p_DIY_COMPANY_ID               IN  NUMBER,  
    p_PAYROLL_RUN_ID               IN  VARCHAR2
  );
 

END PK_TEST_COMPANY; 
/


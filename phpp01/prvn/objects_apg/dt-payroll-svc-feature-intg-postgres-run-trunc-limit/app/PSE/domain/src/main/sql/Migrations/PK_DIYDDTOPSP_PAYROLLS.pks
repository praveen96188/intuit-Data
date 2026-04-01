CREATE OR REPLACE PACKAGE PK_DIYDDTOPSP_PAYROLLS AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_PAYROLLS
   UPDATED: 07.29.2008  10:00 AM  
   PURPOSE: Provide an interface for Java into the AS/400 to retrieve
            DIY Direct Deposit data.

			The following subject areas will be retrieved with this package:
			  - payroll runs (future paychecks only, no history).
              - paychecks and splits
              - payroll financial transactions
               
            Please note that the following items will be derived so that
            the appropriate objects are built in PSP:
              - employees
              - employee bank accounts

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        03.28.2008  EMR              added future payrolls v0.1
   1.1        06.16.2008  EMR              added future payrolls v0.2
   1.2        06.27.2008  EMR              added support for txns
   1.3        07.28.2008  EMR              changed payroll source ids
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- Package type definitions
  -- ------------------------------------------------------------------------

  pc_raise_app_err_cd              CONSTANT NUMBER       := -20053;

  TYPE refcur_employees            IS REF CURSOR;
  TYPE refcur_emp_bank_accts       IS REF CURSOR;

  TYPE refcur_comp_payruns         IS REF CURSOR;
  TYPE refcur_comp_paychecks       IS REF CURSOR;
  TYPE refcur_paycheck_splits      IS REF CURSOR;
  TYPE refcur_pyrl_ee_txns         IS REF CURSOR;    
  TYPE refcur_pyrl_er_txns         IS REF CURSOR;
  TYPE refcur_pyrl_fee_txns        IS REF CURSOR;
  TYPE refcur_pyrl_tax_txns        IS REF CURSOR;    


  -- ------------------------------------------------------------------------
  -- Public APIs
  -- ------------------------------------------------------------------------

  PROCEDURE migrateEmployees (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_EMPLOYEE_LIST                OUT refcur_employees
  );


  PROCEDURE migrateEmployeeBankAccounts (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_DIY_SOURCE_EMP_ID            IN  VARCHAR2,    
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_EMP_BANK_ACCT_LIST           OUT refcur_emp_bank_accts
  );
  

  PROCEDURE migratePayrollRuns (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYROLL_RUN_LIST             OUT refcur_comp_payruns
  );


  PROCEDURE migratePaychecks (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
	p_PAYROLL_RUN_ID               IN  VARCHAR2,     -- LIABILITY CHECK NUM
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYCHECK_LIST                OUT refcur_comp_paychecks
  );


  PROCEDURE migratePaycheckSplits (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
	p_SOURCE_PAYCHECK_ID           IN  VARCHAR2,     -- CHECK ID IN XREF
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYCHECK_SPLIT_LIST          OUT refcur_paycheck_splits
  );  
  

  PROCEDURE migratePayrollTransactions (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
	p_PAYROLL_RUN_ID               IN  VARCHAR2,     -- LIABILITY CHECK NUM
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYROLL_EE_TXN_LIST          OUT refcur_pyrl_ee_txns,
    p_PAYROLL_ER_TXN_LIST          OUT refcur_pyrl_er_txns,   
    p_PAYROLL_FEE_TXN_LIST         OUT refcur_pyrl_fee_txns,
    p_PAYROLL_TAX_TXN_LIST         OUT refcur_pyrl_tax_txns        
  );
  
  
END PK_DIYDDTOPSP_PAYROLLS; 
/


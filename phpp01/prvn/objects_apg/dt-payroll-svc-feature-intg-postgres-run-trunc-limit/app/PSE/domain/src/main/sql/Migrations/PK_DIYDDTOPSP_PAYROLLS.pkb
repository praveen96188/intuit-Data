CREATE OR REPLACE PACKAGE BODY PK_DIYDDTOPSP_PAYROLLS AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_PAYROLLS
   UPDATED: 10.14.2008  09:00 AM  
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
   1.2        06.24.2008  EMR              added support for txns
   1.2.1      06.25.2008  EMR              fixed employee api with Steve
   1.2.2      06.27.2008  EMR              finally added txns
   1.2.3      07.01.2008  EMR              add support for void, recall, strip
   1.2.4      07.09.2008  EMR              modified ee bank eff date      
   1.2.5      07.23.2008  EMR              aligned AS clauses, looks pretty now   
   1.2.6      07.29.2008  EMR              fixed payroll source codes
   1.2.7      07.30.2008  EMR              made payrun amt a positive number   
   1.2.8      09.18.2008  EMR              tuned get employees cursor
   1.2.9      09.26.2008  EMR              further tuned get employees sql
   1.2.10     10.07.2008  EMR              more employee and employee bank
                                             account tuning.
   1.2.11     10.13.2008  EMR              reverted ee apis back to v1 queries                                             
   1.3.0      10.14.2008  EMR              changed all payroll queries to use
                                             local cache.  major revision.
                                             - create local cache in get ee
                                             - all payroll queries reference
                                               local cache.
                                             - cache is purged after validation.
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- Public APIs
  -- ------------------------------------------------------------------------

  -- -----------------------
  -- derive employee information from paychecks 
  -- -----------------------

  PROCEDURE migrateEmployees (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_EMPLOYEE_LIST                OUT refcur_employees
  )
  IS
    cur_employee_list              refcur_employees;

  BEGIN
  
    -- IOP query for reference
    --
    --    select e.EMPLOYEE_GSEQ, 
    --           e.STATUS_CD, 
    --           e.SOURCE_EMPLOYEE_ID, 
    --           e.TAX_ID, 
    --           i.FIRST_NAME, 
    --           i.MIDDLE_NAME, 
    --           i.LAST_NAME  
    --      from employee   e, 
    --           individual i 
    --     where e.EMPLOYEE_GSEQ = i.INDIVIDUAL_GSEQ 
    --       and e.COMPANY_GSEQ  = :1

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- DESIGN NOTES
    --   the ware clause below should get the same list of future
    --   payrolls as the payrun api below.
    --   Also because the as400 stores name as a whole we use that as 
    --   the temporary source id.
    
    -- TODO:
    --   Is gseq needed.  YES.  using simple gseq generator.
    
    -- 07.01.2008 found out there are four payrun types: null for normal,
    --   S for stripped, H for ach holds and T for test.  Only normal goes.
    
    -- 09.18.2008 EMR
    --   joins and dblinks can cause performance issues and the query below
    --   ran in a few minutes.  Reading the DRDA best practices doc outlined
    --   an approach that lead to the new version, which can run in
    --   milliseconds.
    
    -- 10.07.2008 EMR
    --   running against QA data v2 works fine.  but running against production
    --   data with millions of paychecks, well v2 was less than stellar.
    --   thus v3 was born.
    
    -- 10.14.2008 EMR
    --   after carefull analysis it was discovered that DB2 on the AS400 does
    --   not do joins well over a large table - eg 20 million rows.  Thus
    --   a local cache is built in Oracle of the simple tables which runs
    --   fast.  Then all payroll queries will use that local cache.  Validation
    --   will also use the same cache.  Thus from 30 minutes to 10 seconds.
    --   As a result I have gutted all previous queries exception the original
    --   merely as a reference.  See perforce for previous versions.
    
    /* V1
       this query is slow because functions the as400 do not recognize by db2
       return all data to Oracle to do the entire query.  Thus network slowness. 
    
    OPEN cur_employee_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_EMPLOYEE_GSEQ,
              EMP_STATUS_CD,
              EMP_SOURCE_EMPLOYEE_ID,
              EMP_TAX_ID,
              EMP_FIRST_NAME, 
              EMP_MIDDLE_NAME, 
              EMP_LAST_NAME
         FROM (     
               SELECT DISTINCT
                      PK_DIYDDTOPSP_CONST.gc_PSP_Employee_Status_CD  AS EMP_STATUS_CD,
                      TRIM(ACHD_EMPNAME)                             AS EMP_SOURCE_EMPLOYEE_ID,
                      PK_DIYDDTOPSP_CONST.gc_Null_Str                AS EMP_TAX_ID,
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_FIRST_CD,
                        TRIM(ACHD_EMPNAME))                          AS EMP_FIRST_NAME, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_MIDDLE_CD,
                        TRIM(ACHD_EMPNAME))                          AS EMP_MIDDLE_NAME, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_LAST_CD,
                        TRIM(ACHD_EMPNAME))                          AS EMP_LAST_NAME
                 FROM DIY_IQACHDD
                WHERE ACHD_USERID        = p_DIY_COMPANY_ID
                  AND ACHD_TRACE_NUMBER IN (
                        SELECT ACH_TRACE_NUMBER
                          FROM DIY_IQACH
                         WHERE ACH_USERID        = p_DIY_COMPANY_ID
                           AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
                           AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
                           AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal
                      )
              );
    */

    /* V2     
    
    OPEN cur_employee_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_EMPLOYEE_GSEQ,
              EMP_STATUS_CD,
              EMP_SOURCE_EMPLOYEE_ID,
              EMP_TAX_ID,
              EMP_FIRST_NAME, 
              EMP_MIDDLE_NAME, 
              EMP_LAST_NAME
         FROM (
               SELECT DISTINCT
                      PK_DIYDDTOPSP_CONST.gc_PSP_Employee_Status_CD  AS EMP_STATUS_CD,
                      TRIM(EMP_SOURCE_EMPLOYEE_ID)                   As EMP_SOURCE_EMPLOYEE_ID,
                      PK_DIYDDTOPSP_CONST.gc_Null_Str                AS EMP_TAX_ID,
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_FIRST_CD,
                        TRIM(EMP_FIRST_NAME))                        AS EMP_FIRST_NAME,
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_MIDDLE_CD,
                        TRIM(EMP_MIDDLE_NAME))                       AS EMP_MIDDLE_NAME, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_LAST_CD,
                        TRIM(EMP_LAST_NAME))                         AS EMP_LAST_NAME
                 FROM ( 
                       SELECT /*+ NO_MERGE * /
                              ACHD_EMPNAME  AS EMP_SOURCE_EMPLOYEE_ID,
                              ACHD_EMPNAME  AS EMP_FIRST_NAME, 
                              ACHD_EMPNAME  AS EMP_MIDDLE_NAME, 
                              ACHD_EMPNAME  AS EMP_LAST_NAME
                         FROM DIY_IQACHDD
                        WHERE ACHD_USERID        = p_DIY_COMPANY_ID
                          AND ACHD_TRACE_NUMBER IN (
                                SELECT ACH_TRACE_NUMBER
                                  FROM DIY_IQACH
                                 WHERE ACH_USERID        = p_DIY_COMPANY_ID
                                   AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
                                   AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
                                   AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal
                              )
                      )
              );
    */
    
    --
    -- BUILD A LOCAL PAYROLL CACHE
    -- 
    
    -- commit in util
    
    PK_DIYDDTOPSP_UTILS.PR_PURGE_PAYROLL_CACHE (
      p_DIY_COMPANY_ID
    );

    PK_DIYDDTOPSP_UTILS.PR_CREATE_PAYROLL_CACHE (
      p_DIY_COMPANY_ID
    );


    --
    -- RETURN EMPLOYEES
    --
     
    OPEN cur_employee_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_EMPLOYEE_GSEQ,
              EMP_STATUS_CD,
              EMP_SOURCE_EMPLOYEE_ID,
              EMP_TAX_ID,
              EMP_FIRST_NAME, 
              EMP_MIDDLE_NAME, 
              EMP_LAST_NAME
         FROM (
               SELECT DISTINCT
                      PK_DIYDDTOPSP_CONST.gc_PSP_Employee_Status_CD  AS EMP_STATUS_CD,
                      TRIM(EMP_SOURCE_EMPLOYEE_ID)                   As EMP_SOURCE_EMPLOYEE_ID,
                      PK_DIYDDTOPSP_CONST.gc_Null_Str                AS EMP_TAX_ID,
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_FIRST_CD,
                        TRIM(EMP_FIRST_NAME))                        AS EMP_FIRST_NAME,
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_MIDDLE_CD,
                        TRIM(EMP_MIDDLE_NAME))                       AS EMP_MIDDLE_NAME, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmployeeName (
                        PK_DIYDDTOPSP_CONST.GC_NAME_LAST_CD,
                        TRIM(EMP_LAST_NAME))                         AS EMP_LAST_NAME
                 FROM ( 
                       SELECT ACHD_EMPNAME  AS EMP_SOURCE_EMPLOYEE_ID,
                              ACHD_EMPNAME  AS EMP_FIRST_NAME, 
                              ACHD_EMPNAME  AS EMP_MIDDLE_NAME, 
                              ACHD_EMPNAME  AS EMP_LAST_NAME
                         FROM TEMP_CACHE_IQACHDD
                        WHERE ACHD_USERID = p_DIY_COMPANY_ID
                      )
              );

    p_EMPLOYEE_LIST := cur_employee_list;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := SUBSTR ( ('Error in migrateEmployees API retrieving employees. ' || SQLERRM), 1, 500);
  END;


  PROCEDURE migrateEmployeeBankAccounts (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_DIY_SOURCE_EMP_ID            IN  VARCHAR2,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_EMP_BANK_ACCT_LIST           OUT refcur_emp_bank_accts
  )
  IS
    cur_emp_bank_acct_list         refcur_emp_bank_accts;

  BEGIN
  
    -- IOP query for reference
    --
    -- select ba.BANK_ACCT_GSEQ, 
    --        ba.ACCT_NUM, 
    --        ba.ACCT_TYPE_CD, 
    --        ba.BANK_NAME, 
    --        ba.ROUTING_NUM,
    --        eba.EMP_BANK_ACCT_ASSOC_GSEQ, 
    --        eba.SOURCE_BANK_ACCT_ID,     
    --        eba.STATUS_CD, 
    --        eba.EFF_DTTM, 
    --        eba.EXP_DTTM 
    --   from employee e, 
    --        emp_bank_acct_assoc eba, 
    --        bank_acct ba 
    --  where eba.BANK_ACCT_GSEQ   = ba.BANK_ACCT_GSEQ 
    --    and eba.EMPLOYEE_GSEQ    = e.EMPLOYEE_GSEQ  
    --    and e.COMPANY_GSEQ       = :1 
    --    and e.SOURCE_EMPLOYEE_ID = :2 
    --  order 
    --     by eba.EMP_BANK_ACCT_ASSOC_GSEQ

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- DESIGN NOTES
    --   The WHERE clause below should get the same list of future payrolls as 
    --   the payrun api below.  Also the employee source id is simply the 
    --   employee name.
    --
    --   Also the source bank id is simply a combination of routing number and
    --   employee bank account number.
    
    -- TODO:
    --   does employee bank acct and assoc need gseq.  A: not really
    --   how to handle split accounts - eg order by
    
    -- 07.01.2008 found out there are four payrun types: null for normal,
    --   S for stripped, H for ach holds and T for test.  Only normal goes.

    -- 10.07.2008 EMR
    --   running against QA data v2 works fine.  but running against production
    --   data with millions of paychecks, well v2 was less than stellar.
    --   thus v3 was born.
    
    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.
    

    /* V1 
        
    OPEN cur_emp_bank_acct_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_BANK_ACCT_GSEQ, 
              EMP_ACCT_NUM, 
              EMP_ACCT_TYPE_CD, 
              EMP_BANK_NAME, 
              EMP_ROUTING_NUM,
              PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_BANK_ACCT_ASSOC_GSEQ, 
              EMP_SOURCE_BANK_ACCT_ID,
              EMP_BANK_ACCT_STATUS_CD, 
              EMP_BANK_ACCT_EFF_DTTM, 
              EMP_BANK_ACCT_EXP_DTTM
         FROM ( 
               SELECT DISTINCT
                      TRIM(ACHD_ACCTID)                              AS EMP_ACCT_NUM, 
                      DECODE (
                        TRIM(ACHD_ACCTTYPE), 
                        PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
                        PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,              
                        PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav,
                        PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
                      )                                              AS EMP_ACCT_TYPE_CD, 
                      PK_DIYDDTOPSP_CONST.GC_NULL_STR                AS EMP_BANK_NAME, 
                      TRIM(ACHD_BANKID)                              AS EMP_ROUTING_NUM,
                      PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                        TRIM(ACHD_BANKID), 
                        TRIM(ACHD_ACCTID))                           AS EMP_SOURCE_BANK_ACCT_ID,
                      PK_DIYDDTOPSP_CONST.gc_PSP_BankAcct_Status_CD  AS EMP_BANK_ACCT_STATUS_CD, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmpBankEffDttm (
                        p_DIY_COMPANY_ID)                            AS EMP_BANK_ACCT_EFF_DTTM, 
                      NULL                                           AS EMP_BANK_ACCT_EXP_DTTM
                 FROM DIY_IQACHDD
                WHERE ACHD_USERID        = p_DIY_COMPANY_ID
                  AND TRIM(ACHD_EMPNAME) = p_DIY_SOURCE_EMP_ID  -- get agreement
                  AND ACHD_TRACE_NUMBER IN (
                        SELECT ACH_TRACE_NUMBER
                          FROM DIY_IQACH
                         WHERE ACH_USERID        = p_DIY_COMPANY_ID
                           AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
                           AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
                           AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal
                      )
              );
    */

    /* V2
    
    OPEN cur_emp_bank_acct_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_BANK_ACCT_GSEQ, 
              EMP_ACCT_NUM, 
              EMP_ACCT_TYPE_CD, 
              EMP_BANK_NAME, 
              EMP_ROUTING_NUM,
              PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_BANK_ACCT_ASSOC_GSEQ, 
              EMP_SOURCE_BANK_ACCT_ID,
              EMP_BANK_ACCT_STATUS_CD, 
              EMP_BANK_ACCT_EFF_DTTM, 
              EMP_BANK_ACCT_EXP_DTTM
         FROM ( 
               SELECT DISTINCT
                      TRIM(ACHD_ACCTID)                              AS EMP_ACCT_NUM, 
                      DECODE (
                        TRIM(ACHD_ACCTTYPE), 
                        PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
                        PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,              
                        PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav,
                        PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
                      )                                              AS EMP_ACCT_TYPE_CD, 
                      PK_DIYDDTOPSP_CONST.GC_NULL_STR                AS EMP_BANK_NAME, 
                      TRIM(ACHD_BANKID)                              AS EMP_ROUTING_NUM,
                      PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                        TRIM(ACHD_BANKID), 
                        TRIM(ACHD_ACCTID))                           AS EMP_SOURCE_BANK_ACCT_ID,
                      PK_DIYDDTOPSP_CONST.gc_PSP_BankAcct_Status_CD  AS EMP_BANK_ACCT_STATUS_CD, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmpBankEffDttm (
                        p_DIY_COMPANY_ID)                            AS EMP_BANK_ACCT_EFF_DTTM, 
                      NULL                                           AS EMP_BANK_ACCT_EXP_DTTM
                 FROM (
                       SELECT /*+ NO_MERGE * /
                              a.ACHD_ACCTID,
                              a.ACHD_ACCTTYPE,
                              a.ACHD_BANKID
                         FROM DIY_IQACHDD  a,
                              DIY_IQACH    b
                        WHERE a.ACHD_USERID       = b.ACH_USERID
                          AND a.ACHD_TRACE_NUMBER = b.ACH_TRACE_NUMBER
                          AND b.ACH_USERID        = p_DIY_COMPANY_ID
                          AND TRIM(b.ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
                          AND b.ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
                          AND TRIM(b.ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal
                          AND TRIM(a.ACHD_EMPNAME) = p_DIY_SOURCE_EMP_ID
                      )
              );
    */
    
    OPEN cur_emp_bank_acct_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_BANK_ACCT_GSEQ, 
              EMP_ACCT_NUM, 
              EMP_ACCT_TYPE_CD, 
              EMP_BANK_NAME, 
              EMP_ROUTING_NUM,
              PK_DIYDDTOPSP_UTILS.FNGetNextGseq   AS EMP_BANK_ACCT_ASSOC_GSEQ, 
              EMP_SOURCE_BANK_ACCT_ID,
              EMP_BANK_ACCT_STATUS_CD, 
              EMP_BANK_ACCT_EFF_DTTM, 
              EMP_BANK_ACCT_EXP_DTTM
         FROM ( 
               SELECT DISTINCT
                      TRIM(ACHD_ACCTID)                              AS EMP_ACCT_NUM, 
                      DECODE (
                        TRIM(ACHD_ACCTTYPE), 
                        PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
                        PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,              
                        PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav,
                        PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
                      )                                              AS EMP_ACCT_TYPE_CD, 
                      PK_DIYDDTOPSP_CONST.GC_NULL_STR                AS EMP_BANK_NAME, 
                      TRIM(ACHD_BANKID)                              AS EMP_ROUTING_NUM,
                      PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                        TRIM(ACHD_BANKID), 
                        TRIM(ACHD_ACCTID))                           AS EMP_SOURCE_BANK_ACCT_ID,
                      PK_DIYDDTOPSP_CONST.gc_PSP_BankAcct_Status_CD  AS EMP_BANK_ACCT_STATUS_CD, 
                      PK_DIYDDTOPSP_UTILS.FNGetEmpBankEffDttm (
                        p_DIY_COMPANY_ID)                            AS EMP_BANK_ACCT_EFF_DTTM, 
                      NULL                                           AS EMP_BANK_ACCT_EXP_DTTM    
                 FROM (
                       SELECT ACHD_ACCTID,
                              ACHD_ACCTTYPE,
                              ACHD_BANKID
                         FROM TEMP_CACHE_IQACHDD
                        WHERE ACHD_USERID        = p_DIY_COMPANY_ID
                          AND TRIM(ACHD_EMPNAME) = p_DIY_SOURCE_EMP_ID
                      )
              );

    p_EMP_BANK_ACCT_LIST := cur_emp_bank_acct_list;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := SUBSTR ( ('Error in migrateEmployeeBankAccounts API retrieving employee accounts. ' || SQLERRM), 1, 500);
  END;


  PROCEDURE migratePayrollRuns (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYROLL_RUN_LIST             OUT refcur_comp_payruns
  )
  IS
    cur_payroll_run_list           refcur_comp_payruns;

  BEGIN

    -- IOP query for reference
    --
    -- select pr.PAYROLL_RUN_GSEQ, 
    --        pr.PAYCHECK_DEPOSIT_DATE, 
    --        pr.PAYROLL_NET_AMT, 
    --        pr.PAYROLL_RUN_DATE,  
    --        pr.PAYROLL_STATUS_CD, 
    --        pr.SOURCE_PAY_RUN_ID, 
    --        pr.STATUS_EFF_DATE, 
    --        cba.COMP_BANK_ACCT_ASSOC_GSEQ  
    --   from payroll_run           pr, 
    --        comp_bank_acct_assoc  cba 
    --  where pr.COMP_BANK_ACCT_ASSOC_GSEQ (+) =     
    --        cba.COMP_BANK_ACCT_ASSOC_GSEQ 
    --    and pr.COMPANY_GSEQ (+) = 
    --        cba.COMPANY_GSEQ  
    --    and pr.company_gseq     = :1 
    --  order 
    --     by pr.PAYROLL_RUN_GSEQ


    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- TODO
    --   Are gseqs needed - payrun and company bank acct assoc.
    --   Where is token mapped - company or payrun.
    --   Is as400 net with or without fees.
    --   Are there taxes in as400 and are then needed on psp.
    
    -- 07.01.2008 found out there are four payrun types: null for normal,
    --   S for stripped, H for ach holds and T for test.  Only normal goes.
    
    -- 07.29.2008 there is a table that maps the QB ofx to the as400 tables
    --   for payroll runs - DXCHKXREF. Thus the ach_trace_number is just a
    --   DB2 table key and will not be used as the pay run source id.  The
    --   value used is simply a sequential number that increments each time
    --   a payroll is sent, and is stored on the client table as
    --   CLI_LAST_PAYTAX.
    
    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.
    
    /* V1
    
    OPEN cur_payroll_run_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq AS PR_PAYROLL_RUN_GSEQ,
              PR_SOURCE_PAY_RUN_ID, 
              PR_PAYCHECK_DEPOSIT_DATE, 
              PR_PAYROLL_NET_AMT, 
              PR_PAYROLL_RUN_DATE,  
              PR_PAYROLL_STATUS_CD, 
              PR_STATUS_EFF_DATE, 
              SRC_COMP_BANK_ACCT_ID
         FROM (
               SELECT TO_CHAR(ACH_LIABCHK)                         AS PR_SOURCE_PAY_RUN_ID, 
                      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDDHH24MISS')   AS PR_PAYCHECK_DEPOSIT_DATE, 
                      ABS(ACH_DDAMT)                               AS PR_PAYROLL_NET_AMT, 
                      TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS')   AS PR_PAYROLL_RUN_DATE,  
                      PK_DIYDDTOPSP_CONST.gc_PSP_Payrun_Status_CD  AS PR_PAYROLL_STATUS_CD, 
                      TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS')   AS PR_STATUS_EFF_DATE, 
                      PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID    AS SRC_COMP_BANK_ACCT_ID
                 FROM DIY_IQACH
                WHERE ACH_USERID        = p_DIY_COMPANY_ID
                  AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
                  AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
                  AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal
                ORDER
                   BY ACH_LIABCHK
              );
              
    */
    
    OPEN cur_payroll_run_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq AS PR_PAYROLL_RUN_GSEQ,
              PR_SOURCE_PAY_RUN_ID, 
              PR_PAYCHECK_DEPOSIT_DATE, 
              PR_PAYROLL_NET_AMT, 
              PR_PAYROLL_RUN_DATE,  
              PR_PAYROLL_STATUS_CD, 
              PR_STATUS_EFF_DATE, 
              SRC_COMP_BANK_ACCT_ID
         FROM (
               SELECT TO_CHAR(ACH_LIABCHK)                         AS PR_SOURCE_PAY_RUN_ID, 
                      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDDHH24MISS')   AS PR_PAYCHECK_DEPOSIT_DATE, 
                      ABS(ACH_DDAMT)                               AS PR_PAYROLL_NET_AMT, 
                      TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS')   AS PR_PAYROLL_RUN_DATE,  
                      PK_DIYDDTOPSP_CONST.gc_PSP_Payrun_Status_CD  AS PR_PAYROLL_STATUS_CD, 
                      TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS')   AS PR_STATUS_EFF_DATE, 
                      PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID    AS SRC_COMP_BANK_ACCT_ID
                 FROM TEMP_CACHE_IQACH
                WHERE ACH_USERID        = p_DIY_COMPANY_ID
                ORDER
                   BY ACH_LIABCHK
              );

               
    p_PAYROLL_RUN_LIST := cur_payroll_run_list;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := SUBSTR ( ('Error in migratePayrollRuns API retrieving payroll runs. ' || SQLERRM), 1, 500);
  END;


  PROCEDURE migratePaychecks (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
	p_PAYROLL_RUN_ID               IN  VARCHAR2,     -- LIABILITY CHECK NUM
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYCHECK_LIST                OUT refcur_comp_paychecks
  )
  IS
    v_as400_ach_trace_num          NUMBER;
    v_temp_ach_liabchk             NUMBER;

    cur_paycheck_list              refcur_comp_paychecks;

  BEGIN

    -- IOP query for reference
    --
    -- select p.PAYCHECK_GSEQ, 
    --        p.SOURCE_PAYCHECK_ID, 
    --        e.SOURCE_EMPLOYEE_ID 
    --   from paycheck p, 
    --        employee e 
    --  where e.EMPLOYEE_GSEQ    = p.EMPLOYEE_GSEQ 
    --    and e.COMPANY_GSEQ     = p.COMPANY_GSEQ 
    --    and p.company_gseq     = :1 
    --    and p.payroll_run_gseq = :2


    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
    

    -- translate the liability check, aka payroll source id, into the as400
    -- database key.  also remember TO_NUMBER is post processed.
    
    --v_temp_ach_liabchk := TO_NUMBER(p_PAYROLL_RUN_ID);
    
    SELECT ACH_TRACE_NUMBER
      INTO v_as400_ach_trace_num
      FROM TEMP_CACHE_IQACH
     WHERE ACH_USERID  = p_DIY_COMPANY_ID
       AND ACH_LIABCHK = TO_NUMBER(p_PAYROLL_RUN_ID);
    

    -- 07.29.2008 there is a table that maps the QB ofx to the as400 tables
    --   for paychecks - DXCHKXREF. Thus the ach_trace_number and subnumber are
    --   is just a DB2 table key and will not be used as the pay run source id.
    --   The value used is simply a sequential number that increments each time
    --   a payroll is sent, and is stored on the client table as
    --   CLI_LAST_PAYCHK.
    
    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.

    /* V1
       this was the old method until how splits was discovered in the 
       xref table.
       
    OPEN cur_paycheck_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq             AS PC_PAYCHECK_GSEQ, 
              PK_DIYDDTOPSP_UTILS.FNGetPaycheckSourceID (
                p_DIY_COMPANY_ID,
                ACHD_TRACE_NUMBER,
                ACHD_SUBNUM)                                AS PC_SOURCE_PAYCHECK_ID,
              TRIM(ACHD_EMPNAME)                            AS PC_SOURCE_EMPLOYEE_ID
         FROM DIY_IQACHDD
        WHERE ACHD_USERID       = p_DIY_COMPANY_ID
          AND ACHD_TRACE_NUMBER = v_as400_ach_trace_num
        ORDER
           BY ACHD_SUBNUM ASC;
    */
    
    /* V2
    
    OPEN cur_paycheck_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq             AS PC_PAYCHECK_GSEQ,
              TO_CHAR(a.XREF_PAYCHKID)                      AS PC_SOURCE_PAYCHECK_ID,
              TRIM(b.ACHD_EMPNAME)                          AS PC_SOURCE_EMPLOYEE_ID
         FROM DIY_DXCHKXREF a,
              DIY_IQACHDD   b
        WHERE a.XREF_USERID       = b.ACHD_USERID
          AND a.XREF_TRACE_NUMBER = b.ACHD_TRACE_NUMBER
          AND a.XREF_DD_NUMBER    = b.ACHD_SUBNUM
          AND a.XREF_USERID       = p_DIY_COMPANY_ID    
          AND a.XREF_TRACE_NUMBER = v_as400_ach_trace_num
        ORDER
           BY a.XREF_PAYCHKID ASC;
           
    */ 

    OPEN cur_paycheck_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq    AS PC_PAYCHECK_GSEQ,
              TO_CHAR(a.XREF_PAYCHKID)             AS PC_SOURCE_PAYCHECK_ID,
              TRIM(b.ACHD_EMPNAME)                 AS PC_SOURCE_EMPLOYEE_ID
         FROM TEMP_CACHE_DXCHKXREF a,
              TEMP_CACHE_IQACHDD   b
        WHERE a.XREF_USERID       = b.ACHD_USERID
          AND a.XREF_TRACE_NUMBER = b.ACHD_TRACE_NUMBER
          AND a.XREF_DD_NUMBER    = b.ACHD_SUBNUM
          AND a.XREF_USERID       = p_DIY_COMPANY_ID    
          AND a.XREF_TRACE_NUMBER = v_as400_ach_trace_num
        ORDER
           BY a.XREF_PAYCHKID ASC; 


    p_PAYCHECK_LIST := cur_paycheck_list;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := SUBSTR ( ('Error in migratePaychecks API retrieving paychecks. ' || SQLERRM), 1, 500);
  END;


  PROCEDURE migratePaycheckSplits (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
	p_SOURCE_PAYCHECK_ID           IN  VARCHAR2,     -- CHECK ID IN XREF
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYCHECK_SPLIT_LIST          OUT refcur_paycheck_splits
  )
  IS
    v_as400_payrun_id               NUMBER;
    v_as400_paycheck_num1           NUMBER;
    v_as400_paycheck_num2           NUMBER;
    v_as400_paycheck_num3           NUMBER;
    v_as400_paycheck_num4           NUMBER;            

    cur_paycheck_split_list        refcur_paycheck_splits;

  BEGIN

    -- IOP query for reference
    --
    -- select ps.paycheck_gseq, 
    --        ps.SOURCE_DD_TXN_ID, 
    --        ps.PYCK_SPLIT_AMT, 
    --        eba.SOURCE_BANK_ACCT_ID, 
    --        ba.ACCT_NUM, 
    --        ba.ACCT_TYPE_CD, 
    --        ba.BANK_NAME, 
    --        ba.ROUTING_NUM 
    --   from paycheck_split ps, 
    --        emp_bank_acct_assoc eba, 
    --        bank_acct ba 
    --  where ps.EMP_BANK_ACCT_ASSOC_GSEQ = eba.EMP_BANK_ACCT_ASSOC_GSEQ    
    --    and ps.COMPANY_GSEQ             = eba.COMPANY_GSEQ 
    --    and ba.BANK_ACCT_GSEQ           = eba.BANK_ACCT_GSEQ 
    --    and ps.company_gseq             = :1 
    --    and ps.paycheck_gseq            = :2


    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- NOTE:
    --   the source paycheck id is the real check number as found in the ofx
    --   cross reference table.  This must be converted into the as400 table
    --   keys.  Just found out paycheck splits show up in the XREF table.
    
    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.

    PK_DIYDDTOPSP_UTILS.PRGetAS400PaycheckID (
      p_DIY_COMPANY_ID,
      TO_NUMBER(p_SOURCE_PAYCHECK_ID),
      v_as400_payrun_id,
      v_as400_paycheck_num1,
      v_as400_paycheck_num2,
      v_as400_paycheck_num3,
      v_as400_paycheck_num4
    );    
    
    /* V1 
    
    OPEN cur_paycheck_split_list
	 FOR
       SELECT 1                                AS PCS_PAYCHECK_GSEQ,   -- fix, ach num + subnum
              PK_DIYDDTOPSP_UTILS.FNGetDDTxnSourceID (
                ACHD_TRACE_NUMBER,
                ACHD_SUBNUM)                   AS PCS_SOURCE_DD_TXN_ID,
              ACHD_AMT                         AS PCS_PYCK_SPLIT_AMT,
              PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                TRIM(ACHD_BANKID), 
                TRIM(ACHD_ACCTID))             AS PCS_SOURCE_BANK_ACCT_ID,
              TRIM(ACHD_ACCTID)                AS PCS_ACCT_NUM,
              DECODE (
                TRIM(ACHD_ACCTTYPE), 
                PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
                PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,              
                PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav, 
                PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
              )                                AS PCS_ACCT_TYPE_CD,
              PK_DIYDDTOPSP_CONST.GC_NULL_STR  AS PCS_BANK_NAME, 
              TRIM(ACHD_BANKID)                AS PCS_ROUTING_NUM  
         FROM DIY_IQACHDD
        WHERE ACHD_USERID        = p_DIY_COMPANY_ID
          AND ACHD_TRACE_NUMBER  = v_as400_payrun_id
          AND ACHD_SUBNUM       IN (
                v_as400_paycheck_num1,
                v_as400_paycheck_num2,
                v_as400_paycheck_num3,
                v_as400_paycheck_num4
              );
              
    */
    
    OPEN cur_paycheck_split_list
	 FOR
       SELECT 1                                AS PCS_PAYCHECK_GSEQ,   -- fix, ach num + subnum
              PK_DIYDDTOPSP_UTILS.FNGetDDTxnSourceID (
                ACHD_TRACE_NUMBER,
                ACHD_SUBNUM)                   AS PCS_SOURCE_DD_TXN_ID,
              ACHD_AMT                         AS PCS_PYCK_SPLIT_AMT,
              PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                TRIM(ACHD_BANKID), 
                TRIM(ACHD_ACCTID))             AS PCS_SOURCE_BANK_ACCT_ID,
              TRIM(ACHD_ACCTID)                AS PCS_ACCT_NUM,
              DECODE (
                TRIM(ACHD_ACCTTYPE), 
                PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
                PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,              
                PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav, 
                PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
              )                                AS PCS_ACCT_TYPE_CD,
              PK_DIYDDTOPSP_CONST.GC_NULL_STR  AS PCS_BANK_NAME, 
              TRIM(ACHD_BANKID)                AS PCS_ROUTING_NUM  
         FROM TEMP_CACHE_IQACHDD
        WHERE ACHD_USERID        = p_DIY_COMPANY_ID
          AND ACHD_TRACE_NUMBER  = v_as400_payrun_id
          AND ACHD_SUBNUM       IN (
                v_as400_paycheck_num1,
                v_as400_paycheck_num2,
                v_as400_paycheck_num3,
                v_as400_paycheck_num4
              );
                  
    p_PAYCHECK_SPLIT_LIST := cur_paycheck_split_list;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := SUBSTR ( ('Error in migratePaycheckSplits API retrieving paycheck splits. ' || SQLERRM), 1, 500);
  END;


  PROCEDURE migratePayrollTransactions (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
	p_PAYROLL_RUN_ID               IN  VARCHAR2,     -- LIABILITY CHECK NUM
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_PAYROLL_EE_TXN_LIST          OUT refcur_pyrl_ee_txns,
    p_PAYROLL_ER_TXN_LIST          OUT refcur_pyrl_er_txns,   
    p_PAYROLL_FEE_TXN_LIST         OUT refcur_pyrl_fee_txns,
    p_PAYROLL_TAX_TXN_LIST         OUT refcur_pyrl_tax_txns        
  )
  IS
    v_as400_ach_trace_num          NUMBER;
    v_temp_ach_liabchk             NUMBER;
    
    cur_payroll_ee_txn_list        refcur_pyrl_ee_txns;
    cur_payroll_er_txn_list        refcur_pyrl_er_txns;
    cur_payroll_fee_txn_list       refcur_pyrl_fee_txns;
    cur_payroll_tax_txn_list       refcur_pyrl_tax_txns;            
    
  BEGIN
  
    -- IOP query for reference
    --
    -- SELECT FT.FINANCIAL_TXN_GSEQ,
    -- 	      FT.Z_INS_DTTM,
    -- 	      FT.TXN_TYPE_CD, 
    -- 	      FT.CURRENT_TXN_STATE_CD, 
    -- 	      FT.CR_BANK_ACCT_TYPE_CD, 
    -- 	      FT.DB_BANK_ACCT_TYPE_CD, 
    -- 	      FT.FINANCIAL_TXN_AMT, 
    -- 	      FT.SETTLEMENT_TYPE_CD, 
    -- 	      FT.SETTLEMENT_DATE,
    --   	  PS.SOURCE_DD_TXN_ID, 
    -- 	      FTA.PARENT_TXN_GSEQ      AS ORIGINAL_FINANCIAL_TXN_GSEQ, 
    -- 	      CRBA.ACCT_NUM            AS CR_ACCT_NUM, 
    -- 	      CRBA.ACCT_TYPE_CD        AS CR_ACCT_TYPE_CD, 
    -- 	      CRBA.BANK_NAME           AS CR_BANK_NAME, 
    -- 	      CRBA.ROUTING_NUM         AS CR_ROUTING_NUM, 
    -- 	      DBBA.ACCT_NUM            AS DB_ACCT_NUM, 
    -- 	      DBBA.ACCT_TYPE_CD        AS DB_ACCT_TYPE_CD, 
    -- 	      DBBA.BANK_NAME           AS DB_BANK_NAME, 
    -- 	      DBBA.ROUTING_NUM         AS DB_ROUTING_NUM, 
    -- 	      (
    -- 	       CASE 
    --        )                        AS SOURCE_BANK_ACCT_ID
    --   FROM FINANCIAL_TXN                 FT 
    --        LEFT JOIN BANK_ACCT           CRBA
    --        LEFT JOIN BANK_ACCT           DBBA 
    --        LEFT JOIN PAYCHECK_SPLIT      PS  
    --        LEFT JOIN FINANCIAL_TXN_ASSOC FTA 
    --  WHERE ft.COMPANY_GSEQ     = :1
    --    AND ft.PAYROLL_RUN_GSEQ = :2
    --  ORDER 
    --     BY FT.FINANCIAL_TXN_GSEQ;

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';


    -- translate the liability check, aka payroll source id, into the as400
    -- database key.  also remember TO_NUMBER is post processed.
    
    --v_temp_ach_liabchk := TO_NUMBER(p_PAYROLL_RUN_ID);
    
    SELECT ACH_TRACE_NUMBER
      INTO v_as400_ach_trace_num
      FROM TEMP_CACHE_IQACH
     WHERE ACH_USERID  = p_DIY_COMPANY_ID
       AND ACH_LIABCHK = TO_NUMBER(p_PAYROLL_RUN_ID);
    	

    -- DESIGN NOTE:
    --   Steve and I agreed to return four reference cursors, one for
    --   each txn type in regards to future paryolls.  They include
    --   employee credit, employer debit, fees and taxes.
    --
    --   07.01.2008 
    --   there are four payroll run or iqach record types - ' ', S, T, H.  
    --   When a payroll is stripped, S, the amounts stay but nothing
    --   is offloaded, thus these will not be moved.  Neither will T or H.
    --   the blank looking one is normal and will be moved.  Also a void or
    --   recall will simply zero out they paychecks, which will be moved as
    --   canceled.
    --
    --   07.29.2008
    --   the DXCHKXREF table contains the cross reference between the ofx
    --   check id and payroll runs in the as400 tables.  The liability check
    --   number associated with the payroll run is the source payrun and
    --   should be unique in all cases.
    --
    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.
     
    
    -- EMPLOYEE CREDIT TRANSACTION LIST
    --   it is assumed that this transaction will take from Intuit and give
    --   to the employee - how nice of us.  the migration java will fill in
    --   the intuit bank info, and lookup in the java cache the ee bank info.
  
    /* V1 
    
    OPEN cur_payroll_ee_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_EE_Credit_Txn_CD     AS FT_TXN_TYPE_CD,
              PK_DIYDDTOPSP_UTILS.FNGetTxnStatusCD ( 
     	        ACHD_AMT)                                     AS FT_CURRENT_TXN_STATE_CD, 
     	      ACHD_AMT                                        AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACHD_DTPAYCHKS, 'YYYYMMDD')             AS FT_SETTLEMENT_DATE,
       	      PK_DIYDDTOPSP_UTILS.FNGetDDTxnSourceID (
                ACHD_TRACE_NUMBER,
                ACHD_SUBNUM)                                  AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                TRIM(ACHD_BANKID), 
                TRIM(ACHD_ACCTID))                            AS EE_SOURCE_BANK_ACCT_ID       
         FROM DIY_IQACHDD
        WHERE ACHD_USERID       = p_DIY_COMPANY_ID
          AND ACHD_TRACE_NUMBER = v_as400_ach_trace_num;
          
    */ 

    OPEN cur_payroll_ee_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_EE_Credit_Txn_CD     AS FT_TXN_TYPE_CD,
              PK_DIYDDTOPSP_UTILS.FNGetTxnStatusCD ( 
     	        ACHD_AMT)                                     AS FT_CURRENT_TXN_STATE_CD, 
     	      ACHD_AMT                                        AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACHD_DTPAYCHKS, 'YYYYMMDD')             AS FT_SETTLEMENT_DATE,
       	      PK_DIYDDTOPSP_UTILS.FNGetDDTxnSourceID (
                ACHD_TRACE_NUMBER,
                ACHD_SUBNUM)                                  AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                TRIM(ACHD_BANKID), 
                TRIM(ACHD_ACCTID))                            AS EE_SOURCE_BANK_ACCT_ID       
         FROM TEMP_CACHE_IQACHDD
        WHERE ACHD_USERID       = p_DIY_COMPANY_ID
          AND ACHD_TRACE_NUMBER = v_as400_ach_trace_num;


    -- EMPLOYER DEBIT TRANSACTION
    --   it is assumed that this transaction will take from the employer and give
    --   to the Intuit - how nice of us.  the migration java will fill in
    --   the intuit bank info, and lookup in the java cache the ee bank info.
    
    -- note: although i created a function to sum the checks, the as400
    --   denormalized the field and put the check total on the payrun, or 
    --   iqach.  to calculate txn status, i used the payrun field for
    --   convenience.
    --
    --   was ...
    -- 	      PK_DIYDDTOPSP_UTILS.FNGetERDebitAmt (
    --          p_DIY_COMPANY_ID,
    --          p_PAYROLL_RUN_ID)                     AS FT_FINANCIAL_TXN_AMT, 
    
    /* V1
    
    OPEN cur_payroll_er_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_ER_Debit_Txn_CD      AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_UTILS.FNGetTxnStatusCD ( 
     	        ABS(ACH_DDAMT))                               AS FT_CURRENT_TXN_STATE_CD, 
     	      ABS(ACH_DDAMT)                                  AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM DIY_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num;
          
    */

    OPEN cur_payroll_er_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_ER_Debit_Txn_CD      AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_UTILS.FNGetTxnStatusCD ( 
     	        ABS(ACH_DDAMT))                               AS FT_CURRENT_TXN_STATE_CD, 
     	      ABS(ACH_DDAMT)                                  AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM TEMP_CACHE_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num;


    -- EMPLOYER FEE AND SALES TAX DEBIT TRANSACTIONS
    --   the fee and tax are denormalized on the AS400.  Once again the fee and  
    --   tax is taken from the employer and given to Intuit, and there the
    --   money will stay, until we buy everyone iPhones.  thus it is assumed that
    --   the java will fill in all the account info.
    --
    --   the first fee is the dd fee of .99 per paycheck.  the second one
    --   is the transmission fee, once per payroll for 3 whole dollars.

    /* V1 
    
    OPEN cur_payroll_fee_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq              AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_FeeDD_Debit_Txn_CD   AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD      AS FT_CURRENT_TXN_STATE_CD, 
     	      ACH_DD_FEE                                      AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM DIY_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num
       UNION ALL
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq              AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_FeeTR_Debit_Txn_CD   AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD      AS FT_CURRENT_TXN_STATE_CD, 
     	      ACH_EMPLOYEE_FEE                                AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM DIY_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num;


    OPEN cur_payroll_tax_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Tax_Debit_Txn_CD     AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD      AS FT_CURRENT_TXN_STATE_CD, 
     	      ACH_SALES_TAX                                   AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM DIY_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num;
          
    */          

    OPEN cur_payroll_fee_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq              AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_FeeDD_Debit_Txn_CD   AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD      AS FT_CURRENT_TXN_STATE_CD, 
     	      ACH_DD_FEE                                      AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM TEMP_CACHE_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num
       UNION ALL
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq              AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_FeeTR_Debit_Txn_CD   AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD      AS FT_CURRENT_TXN_STATE_CD, 
     	      ACH_EMPLOYEE_FEE                                AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM TEMP_CACHE_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num;


    OPEN cur_payroll_tax_txn_list
	 FOR
       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS FT_FINANCIAL_TXN_GSEQ,
     	      PK_DIYDDTOPSP_UTILS.FNGetPayrunInsDttm (
                p_DIY_COMPANY_ID,
                v_as400_ach_trace_num)                        AS FT_Z_INS_DTTM,
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Tax_Debit_Txn_CD     AS FT_TXN_TYPE_CD, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD      AS FT_CURRENT_TXN_STATE_CD, 
     	      ACH_SALES_TAX                                   AS FT_FINANCIAL_TXN_AMT, 
     	      PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Sttlmnt_Type_CD  AS FT_SETTLEMENT_TYPE_CD, 
     	      TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDD') - 1          AS FT_SETTLEMENT_DATE,
       	      TO_CHAR(
                PK_DIYDDTOPSP_UTILS.FNGetNextGseq)            AS PS_SOURCE_DD_TXN_ID, 
              PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID       AS ER_SOURCE_BANK_ACCT_ID       
         FROM TEMP_CACHE_IQACH
        WHERE ACH_USERID       = p_DIY_COMPANY_ID
          AND ACH_TRACE_NUMBER = v_as400_ach_trace_num;
           

    p_PAYROLL_EE_TXN_LIST  := cur_payroll_ee_txn_list;
    p_PAYROLL_ER_TXN_LIST  := cur_payroll_er_txn_list;   
    p_PAYROLL_FEE_TXN_LIST := cur_payroll_fee_txn_list;
    p_PAYROLL_TAX_TXN_LIST := cur_payroll_tax_txn_list;        


  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      --p_RETURN_MSG := SUBSTR ( ('Error in migratePayrollTransactions API retrieving payroll transactions. ' || SQLERRM), 1, 500);
      DBMS_OUTPUT.PUT_LINE (SQLERRM);
  END;


END PK_DIYDDTOPSP_PAYROLLS; 
/


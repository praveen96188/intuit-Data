CREATE OR REPLACE PACKAGE BODY PK_DIYDDTOPSP_VALIDATION AS
/******************************************************************************
   NAME:       PK_DIYDDTOPSP_VALIDATION
   UPDATED:    03.19.2009 04:01 PM   
   PURPOSE:    Validate company data once it is migrated from the AS400 to PSP.
               

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        07/14/2008  EMR              created validation framework.
   1.1        07/16/2008  EMR              added company validation
   1.1.1      07.18.2008  EMR              used new range for raise app err      
   1.1.2      07.23.2008  EMR              bug fixes for validation
                                             to_char as400 num for comp val
   1.1.3      07.30.2008  EMR              removed PRAGMA AUTONOMOUS TXN, do
                                             not use. mess up the gateway.  
   1.1.4      07.30.2008  EMR              fixed payroll and dd info compare                                                                                        
   1.1.5      07.31.2008  EMR              added global validation
   1.1.6      09.26.2008  EMR              tuned AS400_EE_BA_TOTAL
   1.1.7      09.30.2008  EMR              recoded pin compare
   1.1.7.1    10.01.2008  EMR              fixed group by for pin compare
   1.1.8      10.01.2008  EMR              fixed fein compare
   1.1.9      10.03.2008  EMR              had to change pin compare again
   1.1.10     10.03.2008  EMR              turn off some validation for termed
                                             companies due to potential bad data.
   1.1.11     10.10.2008  EMR              tuned payroll validation                                             
   1.1.12     10.15.2008  EMR              commented out pin validation.
   1.1.13     10.16.2008  EMR              refined company validation for ...
                                              PR_VALIDATE_DIY_ACTIVE_DD_INFO
                                              PR_VALIDATE_PAYROLLS
   1.1.14     10.21.2008  EMR              changed pin compare to simply a count
   1.2.0      10.21.2008  EMR              new global validation
   1.2.1      10.22.2008  EMR              added new temp cache purge utils.
   1.2.2      10.23.2008  EMR              moved contact validation from 
                                             global to company compare.
   1.2.3      03.19.2009  EMR              added type to employee bank compare                                             
******************************************************************************/


  -- ------------------------------------------------------------------------
  -- PRIVATE UTILITY FUNCTIONS
  -- ------------------------------------------------------------------------

  FUNCTION FN_AS400_COMP_EXISTS (
    p_DIY_COMPANY_DB_ID            IN  NUMBER)
    RETURN BOOLEAN
  IS
    b_company_exists               BOOLEAN := FALSE;
    v_temp_count                   NUMBER  := 0;
    
  BEGIN

    SELECT COUNT(*)
      INTO v_temp_count
      FROM DIY_IQCLIENT
     WHERE CLI_USERID = p_DIY_COMPANY_DB_ID;
    
    -- only 1 company should be found
    IF (v_temp_count = 1) THEN
      b_company_exists := TRUE;
    ELSE
      b_company_exists := FALSE;
    END IF; 

      
    RETURN b_company_exists;

  EXCEPTION
    WHEN OTHERS THEN 
      RAISE;
  END;

  
  FUNCTION FN_PSP_COMP_EXISTS (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
    RETURN BOOLEAN
  IS
    b_company_exists               BOOLEAN := FALSE;
    v_temp_count                   NUMBER  := 0;
    
  BEGIN

    SELECT COUNT(*)
      INTO v_temp_count
      FROM PSP_COMPANY
     WHERE COMPANY_SEQ = p_PSP_COMPANY_DB_ID;
    
    -- only 1 company should be found
    IF (v_temp_count = 1) THEN
      b_company_exists := TRUE;
    ELSE
      b_company_exists := FALSE;
    END IF; 

      
    RETURN b_company_exists;

  EXCEPTION
    WHEN OTHERS THEN 
      RAISE;
  END;


  FUNCTION FN_Is_Company_Termed (
    p_DIY_COMPANY_DB_ID            IN  NUMBER)
    RETURN BOOLEAN
  IS
    v_temp_status                  VARCHAR2(100);
    v_tf_answer                    BOOLEAN;
    
  BEGIN

    v_temp_status := NULL;
    v_tf_answer   := FALSE;
    
    SELECT TRIM(CLI_STATUS)
      INTO v_temp_status
      FROM DIY_IQCLIENT
     WHERE CLI_USERID = p_DIY_COMPANY_DB_ID;
    
    -- good news is that a migration DIY company is only Active or Terminated.
    IF (v_temp_status = PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Termed) THEN
      v_tf_answer := TRUE;
    ELSE
      v_tf_answer := FALSE;
    END IF; 

      
    RETURN v_tf_answer;

  EXCEPTION
    WHEN OTHERS THEN 
      RAISE;
  END;


  FUNCTION FN_Company_Has_Payrolls (
    p_DIY_COMPANY_DB_ID            IN  NUMBER)
    RETURN BOOLEAN
  IS
    v_temp_count                   NUMBER;
    v_tf_answer                    BOOLEAN;
    
  BEGIN

    v_temp_count := 0;
    v_tf_answer  := FALSE;
    
    -- 10.14.2008 EMR
    --   changed to use local payroll cache instead of AS400
    
    /* V1
    
    SELECT COUNT(*)
      INTO v_temp_count
      FROM DIY_IQACH
     WHERE ACH_USERID        = p_DIY_COMPANY_DB_ID
       AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
       AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
       AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal;

    */

    SELECT COUNT(*)
      INTO v_temp_count
      FROM TEMP_CACHE_IQACH
     WHERE ACH_USERID = p_DIY_COMPANY_DB_ID;
           
    
    -- 94 percent of the companies sampled do not have payrolls
    IF (v_temp_count = 0) THEN
      v_tf_answer := FALSE;
    ELSE
      v_tf_answer := TRUE;
    END IF; 

      
    RETURN v_tf_answer;

  EXCEPTION
    WHEN OTHERS THEN 
      RAISE;
  END;


  FUNCTION FN_GET_PSP_ER_LIMIT (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
    RETURN NUMBER
  IS
    v_temp_limit                   NUMBER;
    
  BEGIN

    SELECT NVL (
             Override_Company_Limit_Amount, 
             PK_DIYDDTOPSP_CONST.gc_DIY_LimitAmt_Company
           )
      INTO v_temp_limit
      FROM PSP_DDCOMPANY_SERVICE_INFO a,
           PSP_COMPANY_SERVICE        b
     WHERE a.DDCOMPANY_SERVICE_INFO_SEQ = b.COMPANY_SERVICE_SEQ
       AND a.REALM_ID                   = b.REALM_ID
       AND b.Company_FK                 = p_PSP_COMPANY_DB_ID;

    RETURN v_temp_limit;

  EXCEPTION
    WHEN OTHERS THEN 
      RAISE;
  END;
  

  FUNCTION FN_GET_PSP_EE_LIMIT (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
    RETURN NUMBER
  IS
    v_temp_limit                   NUMBER;
    
  BEGIN

    SELECT NVL (
             Override_Employee_Limit_Amount, 
             PK_DIYDDTOPSP_CONST.gc_DIY_LimitAmt_Employee
           )
      INTO v_temp_limit
      FROM PSP_DDCOMPANY_SERVICE_INFO a,
           PSP_COMPANY_SERVICE        b
     WHERE a.DDCOMPANY_SERVICE_INFO_SEQ = b.COMPANY_SERVICE_SEQ
       AND a.REALM_ID                   = b.REALM_ID
       AND b.Company_FK                 = p_PSP_COMPANY_DB_ID;

    RETURN v_temp_limit;

  EXCEPTION
    WHEN OTHERS THEN 
      RAISE;
  END;


  PROCEDURE PR_SET_COMP_MIGRATION_EVENT (
    p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,
    p_EVENT_LOG_TYPE_CD            IN  MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE, 
    p_EVENT_PARAM_CD               IN  MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE,
    p_EVENT_PARAM_VALUE            IN  MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE
  )
  IS
 

  BEGIN
  
    INSERT  /*+ APPEND */ INTO MIGRATION_EVENT_LOG (
      MIGRATION_EVENT_LOG_GSEQ, 
      COMPANY_MIGRATION_GSEQ, 
      EVENT_LOG_TYPE_CD, 
      EVENT_PARAM_CD, 
      EVENT_PARAM_VALUE
    ) 
    VALUES (
      SEQ_MIGRATION_EVENT_LOG.NEXTVAL,
      p_COMPANY_MIGRATION_GSEQ,
      p_EVENT_LOG_TYPE_CD, 
      p_EVENT_PARAM_CD,
      p_EVENT_PARAM_VALUE
    );

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


  -- ------------------------------------------------------------------------
  -- THE SPECIFIC COMPARITOR ROUTINES
  -- ------------------------------------------------------------------------


  --
  -- THESE ARE THE COMPANY SPECIFIC VALIDATIONS
  --

  PROCEDURE PR_VALIDATE_COMPANY_INFO (
    p_DIY_COMPANY_USERID           IN  NUMBER,
    p_PSP_COMPANY_GUID             IN  VARCHAR2, 
    p_COMPANY_MIGRATION_GSEQ       IN  COMPANY_MIGRATION.COMPANY_MIGRATION_GSEQ%TYPE,
    p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_match_count                  NUMBER;

  BEGIN
  
    -- initialize to happy path
	b_data_matches    := TRUE;
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'CompInfoCompare';    
	v_event_msg       := 'Company basic data matches.';
	v_match_count     := 0;
    
	SELECT COUNT(*)
	  INTO v_match_count
      FROM (
            SELECT LPAD(TO_CHAR(CLI_FEIN), 9, '0') AS FEIN,
            	   TRIM(CLI_LEGALNAME)             AS LEGALNAME, 
                   TO_CHAR(CLI_USERID)             AS PSID 
              FROM DIY_IQCLIENT
             WHERE CLI_USERID = p_DIY_COMPANY_USERID
            MINUS
            SELECT FED_TAX_ID,
            	   LEGAL_NAME, 
                   SOURCE_COMPANY_ID 
              FROM PSP_COMPANY
             WHERE COMPANY_SEQ = p_PSP_COMPANY_GUID
	       );

    -- if the two companies match then minus should yield a result
	-- of 0 meaning there are no records that don't match. Any
	-- other results mean one of the fields do not match. And if they
    -- do not match then something happened during the migration and oh, my,
    -- that is not a good thing and must be reported to Steve immediately.
	
    IF (v_match_count <> 0) THEN

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Company basic data does not match. AS400 COMP ID: ' || 
	                       p_DIY_COMPANY_USERID                   ||
						   ' PSP COMP ID: '                       ||
						   p_PSP_COMPANY_GUID                     ||
						   '.';
	  
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);

	
    -- 10.23.2008 EMR
    --   moved the contact compare to company level for better performance.
    
    SELECT (DIY_PRINC_CONTACT_CNT + DIY_CONTACT_CNT) - PSP_CONTACT_CNT
	  INTO v_match_count
      FROM (
            SELECT (
                    SELECT COUNT(*)
                      FROM DIY_IQPRINC_CONTACT
                     WHERE CLIP_USERID = p_DIY_COMPANY_USERID
                   ) AS DIY_PRINC_CONTACT_CNT,
                   (
                    SELECT COUNT(*)
                      FROM DIY_IQCONTACT
                     WHERE CLIC_USERID = p_DIY_COMPANY_USERID
                   ) AS DIY_CONTACT_CNT,
                   (
                    SELECT COUNT(*)
                      FROM PSP_CONTACT
                     WHERE COMPANY_FK = p_PSP_COMPANY_GUID  
                   ) AS PSP_CONTACT_CNT       
              FROM DUAL
           );
               
    IF (v_match_count = 0) THEN
    
      v_temp_event_type := pc_Event_Type_Info;    
	  v_event_msg       := 'Company contact count matches.';
          
    ELSE

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Company contact count does not match. AS400 COMP ID: ' || 
	                       p_DIY_COMPANY_USERID                   ||
						   ' PSP COMP ID: '                       ||
						   p_PSP_COMPANY_GUID                     ||
						   '.';
	  
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);

    
    -- tell the caller how things turn out
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


  PROCEDURE PR_VALIDATE_PAYROLLS (
    p_DIY_COMPANY_USERID           IN  NUMBER,
    p_PSP_COMPANY_GUID             IN  VARCHAR2, 
    p_COMPANY_MIGRATION_GSEQ       IN  COMPANY_MIGRATION.COMPANY_MIGRATION_GSEQ%TYPE,
    p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_match_count                  NUMBER;

    v_AS400_EE_TOTAL               NUMBER;
    v_AS400_EE_BA_TOTAL            NUMBER;
    v_AS400_PAYRUN_TOTAL           NUMBER;
    v_AS400_PAYCHECK_TOTAL         NUMBER;
    v_AS400_PYCHCK_EE_TXN_TOTAL    NUMBER;
    
    v_PSP_EE_TOTAL                 NUMBER;
    v_PSP_EE_BA_TOTAL              NUMBER;
    v_PSP_PAYRUN_TOTAL             NUMBER;
    v_PSP_PAYCHECK_TOTAL           NUMBER;
    v_PSP_TXN_ER_DEBIT_TOTAL       NUMBER;
    v_PSP_TXN_EE_CREDIT_TOTAL      NUMBER;    

    v_payrun_detail_cnt            NUMBER;    

  BEGIN
  
    -- initialize to happy path
	b_data_matches    := TRUE;
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'PayrollCompare';    
	v_event_msg       := 'Data matches';
	v_match_count     := 0;
    
    -- 10.16.2008 EMR
    --   modified paychecks to filter out what appears to be canceled checks.
    --   these appear as 0 in xref and do not show up in iqachdd.
    --   AS400_PAYCHECK_TOTAL

    -- 03.19.2009 EMR
    --   modified paychecks to filter out what appears to be canceled checks.

    SELECT (
            SELECT COUNT(*)
              FROM (
                    SELECT DISTINCT
                           TRIM(ACHD_EMPNAME) AS EMP_SOURCE_EMPLOYEE_ID
                      FROM TEMP_CACHE_IQACHDD
                     WHERE ACHD_USERID = p_DIY_COMPANY_USERID
                   )
           ) AS AS400_EE_TOTAL,
           (
            SELECT COUNT(*)
              FROM (
                    SELECT DISTINCT
                           EMP_SOURCE_EMPLOYEE_ID,
                           PK_DIYDDTOPSP_UTILS.FNGetEmpSrcBankID (
                             DIY_BANKID, 
                             DIY_ACCTID
                           ) AS EMP_SOURCE_BANK_ACCT_ID,
                           DIY_ACCT_TYPE
                      FROM (
                            SELECT TRIM(ACHD_EMPNAME)  AS EMP_SOURCE_EMPLOYEE_ID,
                                   TRIM(ACHD_BANKID)   AS DIY_BANKID, 
                                   TRIM(ACHD_ACCTID)   AS DIY_ACCTID,
                                   TRIM(ACHD_ACCTTYPE) AS DIY_ACCT_TYPE
                              FROM TEMP_CACHE_IQACHDD
                             WHERE ACHD_USERID = p_DIY_COMPANY_USERID
                           )
                   )
           ) AS AS400_EE_BA_TOTAL,
           (
            SELECT COUNT(*)
              FROM (
                    SELECT ACH_TRACE_NUMBER
                      FROM TEMP_CACHE_IQACH
                     WHERE ACH_USERID        = p_DIY_COMPANY_USERID
                   )
           ) AS AS400_PAYRUN_TOTAL,
           (
            SELECT COUNT(*)
              FROM (
                    SELECT XREF_PAYCHKID
                      FROM TEMP_CACHE_DXCHKXREF
                     WHERE XREF_USERID     = p_DIY_COMPANY_USERID
                       AND XREF_DD_NUMBER <> 0                     
                   )
           ) AS AS400_PAYCHECK_TOTAL,
           (
            SELECT COUNT(*)
              FROM (
                    SELECT ACHD_TRACE_NUMBER,
                           ACHD_SUBNUM
                      FROM TEMP_CACHE_IQACHDD
                     WHERE ACHD_USERID = p_DIY_COMPANY_USERID
                   )
           ) AS AS400_PAYCHECK_EE_TXN_TOTAL,
           (
            SELECT COUNT(*)
              FROM PSP_EMPLOYEE
             WHERE Company_FK = p_PSP_COMPANY_GUID
           ) AS PSP_EE_TOTAL,
           (
            SELECT COUNT(*)
              FROM PSP_EMPLOYEE_BANK_ACCOUNT a,
                   PSP_EMPLOYEE              b
             WHERE a.Employee_FK = b.Employee_Seq
               AND b.Company_FK  = p_PSP_COMPANY_GUID
           ) AS PSP_EE_BA_TOTAL,
           (
            SELECT COUNT(*)
              FROM PSP_PAYROLL_RUN
             WHERE Company_FK = p_PSP_COMPANY_GUID
           ) AS PSP_PAYRUN_TOTAL,
           (
            SELECT COUNT(*)
              FROM PSP_PAYCHECK    a,
                   PSP_PAYROLL_RUN b
             WHERE a.Payroll_Run_FK = b.Payroll_Run_Seq
               AND b.Company_FK     = p_PSP_COMPANY_GUID
           ) AS PSP_PAYCHECK_TOTAL,
           (
            SELECT COUNT(*)
              FROM PSP_FINANCIAL_TRANSACTION
             WHERE Company_FK          = p_PSP_COMPANY_GUID
               AND Transaction_Type_FK = PK_DIYDDTOPSP_CONST.gc_PSP_ER_Debit_Txn_CD
           ) AS PSP_TXN_ER_DEBIT_TOTAL,
           (
            SELECT COUNT(*)
              FROM PSP_FINANCIAL_TRANSACTION
             WHERE Company_FK          = p_PSP_COMPANY_GUID
               AND Transaction_Type_FK = PK_DIYDDTOPSP_CONST.gc_PSP_EE_Credit_Txn_CD
           ) AS PSP_TXN_EE_CREDIT_TOTAL
	  INTO
           v_AS400_EE_TOTAL,
           v_AS400_EE_BA_TOTAL,
           v_AS400_PAYRUN_TOTAL,
           v_AS400_PAYCHECK_TOTAL,
           v_AS400_PYCHCK_EE_TXN_TOTAL,
           v_PSP_EE_TOTAL,
           v_PSP_EE_BA_TOTAL,
           v_PSP_PAYRUN_TOTAL,
           v_PSP_PAYCHECK_TOTAL,
           v_PSP_TXN_ER_DEBIT_TOTAL,
           v_PSP_TXN_EE_CREDIT_TOTAL
      FROM DUAL;


    SELECT COUNT(*)
      INTO v_payrun_detail_cnt    
      FROM (
            SELECT TO_CHAR(ACH_LIABCHK)                              AS PR_SOURCE_PAY_RUN_ID, 
                   TRUNC(TO_DATE(ACH_DTPAYCHKS, 'YYYYMMDDHH24MISS')) AS PR_PAYCHECK_DEPOSIT_DATE, 
                   PK_DIYDDTOPSP_CONST.gc_PSP_Payrun_Status_CD       AS PR_PAYROLL_STATUS_CD,
                   ABS(ACH_DDAMT)                                    AS PR_PAYROLL_NET_AMT 
              FROM TEMP_CACHE_IQACH
             WHERE ACH_USERID        = p_DIY_COMPANY_USERID
             MINUS
            SELECT SOURCE_PAY_RUN_ID,
				   TRUNC(PAYCHECK_DATE),
                   PAYROLL_RUN_STATUS,  
                   PAYROLL_NET_AMOUNT
              FROM PSP_PAYROLL_RUN
             WHERE COMPANY_FK = p_PSP_COMPANY_GUID
           );

	-- once all the totals have been returned then compare and log either 
    -- pass or fail for each.  What was not compared but coould be is:
    -- finanical transaction details, mainly amount, state and settlement date;
    -- fee transactions.
	   
    IF (v_AS400_EE_TOTAL = v_PSP_EE_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Employee totals match. Total = ' || v_PSP_EE_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Employee totals DO NOT match. AS400 = ' ||
	                       v_AS400_EE_TOTAL || ', PSP = ' || v_PSP_EE_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);
    

    IF (v_AS400_EE_BA_TOTAL = v_PSP_EE_BA_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Employee bank account totals match. Total = ' || v_PSP_EE_BA_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Employee bank account totals DO NOT match. AS400 = ' ||
	                       v_AS400_EE_BA_TOTAL || ', PSP = ' || v_PSP_EE_BA_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (v_AS400_PAYRUN_TOTAL = v_PSP_PAYRUN_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Payrun totals match. Total = ' || v_PSP_PAYRUN_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Payrun totals DO NOT match. AS400 = ' ||
	                       v_AS400_PAYRUN_TOTAL || ', PSP = ' || v_PSP_PAYRUN_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);
    

    IF (v_AS400_PAYCHECK_TOTAL = v_PSP_PAYCHECK_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Paycheck totals match. Total = ' || v_PSP_PAYCHECK_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Paycheck totals DO NOT match. AS400 = ' ||
	                       v_AS400_PAYCHECK_TOTAL || ', PSP = ' || v_PSP_PAYCHECK_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);

    
    -- the mapping is one er debit txn per payroll run
    IF (v_AS400_PAYRUN_TOTAL = v_PSP_TXN_ER_DEBIT_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Employer debit transaction totals match. Total = ' || v_PSP_TXN_ER_DEBIT_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Employer debit transaction totals DO NOT match. AS400 = ' ||
	                       v_AS400_PAYRUN_TOTAL || ', PSP = ' || v_PSP_TXN_ER_DEBIT_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);

   -- the xref table has the checks and the achdd table has all the ee transactions.
    IF (v_AS400_PYCHCK_EE_TXN_TOTAL = v_PSP_TXN_EE_CREDIT_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Employee credit transaction totals match. Total = ' || v_PSP_TXN_EE_CREDIT_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Employee credit transaction totals DO NOT match. AS400 = ' ||
	                       v_AS400_PAYCHECK_TOTAL || ', PSP = ' || v_PSP_TXN_EE_CREDIT_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);


    -- a 0 here means that the payruns in both databases match
    IF (v_payrun_detail_cnt = 0) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Payrun details match.';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Payrun details DO NOT match. ' ||
	                       v_payrun_detail_cnt             ||
                           ' rows different.';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (p_COMPANY_MIGRATION_GSEQ, v_temp_event_type, v_event_cd, v_event_msg);
  
    
    -- tell the caller how things turned out  
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


  PROCEDURE PR_VALIDATE_DIY_DD_INFO (
    p_DIY_COMPANY_USERID           IN  NUMBER,
    p_PSP_COMPANY_GUID             IN  VARCHAR2, 
    p_COMPANY_MIGRATION_GSEQ       IN  COMPANY_MIGRATION.COMPANY_MIGRATION_GSEQ%TYPE,
    p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_match_count                  NUMBER;

  BEGIN
  
    -- initialize to happy path
	b_data_matches    := TRUE;   -- once set here never use true again below
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'DIYDDInfoCompare';    
	v_event_msg       := 'Company bank account data matches';
	v_match_count     := 0;

    -- 09.30.2008 EMR
    -- moved pin compare to its own select due to the way they changed
    -- the pin model in psp.
    --
    -- removed this function call
    --   PK_DIYDDTOPSP_UTILS.FNGetEncryptedPin (
    --     p_DIY_COMPANY_USERID,
    --     1)                            AS AS400_QB_PIN1,
    
    -- 10.03.2008 EMR
    -- The DD info was split because only part of it applies to active 
    -- companies.  Here is the breakdown of what is migrated ...
    --
    -- termed: company info, dd service info, contacts
    -- active: company info, dd service info, contacts, events, pins, 
    --         company bank account, payrolls including employees.
    --
    -- this api applies to both terminated and active customers.
    
	SELECT COUNT(*)
	  INTO v_match_count
      FROM (
            SELECT PK_DIYDDTOPSP_UTILS.FNGetEROverrideLimitAmt (
	                 p_DIY_COMPANY_USERID)         AS OVERRIDECOMPANYLIMITAMOUNT,
			       PK_DIYDDTOPSP_UTILS.FNGetEEOverrideLimitAmt (
	                 p_DIY_COMPANY_USERID)         AS OVERRIDEEMPLOYEELIMITAMOUNT,
                   CLI_HIGH_TOKEN                  AS AS400_QB_NEXTQBSYNCTOKEN,
                   TRIM(CLI_APPKEY)                AS AS400_QB_SERVICEKEY,
                   TRIM(CLI_ID)                    AS CRISROWID,
                   TRIM(CLI_AGREE_NUM)             AS AS400_QB_AGREEMENTNUM,
                   TRIM(CLI_REGNUM)                AS AS400_QB_REGISTRATIONNUM,
                   TRIM(CLI_APPVER)                AS AS400_QB_VERSIONNUM,
                   TRIM(CLI_APPID)                 AS AS400_QB_APPLICATIONID,
                   TO_CHAR(CLI_LAST_PAYTAX + 1)    AS AS400_NEXT_PAYROLLTXNID,
                   TO_CHAR(CLI_LAST_PAYCHK + 1)    AS AS400_NEXT_PAYCHECKID
              FROM DIY_IQCLIENT
             WHERE CLI_USERID = p_DIY_COMPANY_USERID
            MINUS
            SELECT FN_GET_PSP_ER_LIMIT (p_PSP_COMPANY_GUID) AS PSP_ER_Limit,
                   FN_GET_PSP_EE_LIMIT (p_PSP_COMPANY_GUID) AS PSP_EE_Limit,
                   Current_Token,
                   Agreement_Info_Service_Key,
                   Agree_Info_Source_ID,
                   Agree_Info_Sub_Nbr,
                   Quickbooks_Info_License_Number,
                   QB_Info_App_Version,
                   Quickbooks_Info_Application_ID,
                   Next_Payroll_Transaction_ID,
                   Next_Paycheck_ID
              FROM PSP_COMPANY
             WHERE Company_SEQ = p_PSP_COMPANY_GUID
          );

    IF (v_match_count = 0) THEN
    
      v_temp_event_type := pc_Event_Type_Info;    
	  v_event_msg       := 'Company DD service data matches';
          
    ELSE

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Company DD service data does NOT match. AS400 COMP ID: ' || 
	                       p_DIY_COMPANY_USERID                   ||
						   ' PSP COMP ID: '                       ||
						   p_PSP_COMPANY_GUID                     ||
						   '.';
	  
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);
    
    -- tell the caller how things turned out
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;
  

  PROCEDURE PR_VALIDATE_DIY_ACTIVE_DD_INFO (
    p_DIY_COMPANY_USERID           IN  NUMBER,
    p_PSP_COMPANY_GUID             IN  VARCHAR2, 
    p_COMPANY_MIGRATION_GSEQ       IN  COMPANY_MIGRATION.COMPANY_MIGRATION_GSEQ%TYPE,
    p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_match_count                  NUMBER;

  BEGIN
  
    -- initialize to happy path
	b_data_matches    := TRUE;   -- once set here never use true again below
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'DIYDDInfoCompare';    
	v_event_msg       := 'Company bank account data matches';
	v_match_count     := 0;

    -- 10.03.2008 EMR
    -- The DD info was split because only part of it applies to active 
    -- companies.  Here is the breakdown of what is migrated ...
    --
    -- termed: company info, dd service info, contacts
    -- active: company info, dd service info, contacts, events, pins, 
    --         company bank account, payrolls including employees.
   
    -- check the company bank account info first, then check additional
    -- dd info.  please note that once the data matches boolean goes to false
    -- it should stay false, but match count can be reset.
    
    -- 10.16.2008 EMR
    --   added default for bank name because there are lots of null ones.
    --   removed this ...
    --     TRIM(CLI_BANKNAME)              AS BANK_NAME,

	SELECT COUNT(*)
	  INTO v_match_count
      FROM (
            SELECT TRIM(CLI_ACCTID)                AS ACCOUNTNUMBER,
                   DECODE (
			         TRIM(CLI_ACCTTYPE),
                     PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
                     PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,
                     PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav,
                     PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
			       )                               AS ACCOUNT_TYPE_CD,
       	           DECODE (
                     TRIM(CLI_BANKNAME),
                     PK_DIYDDTOPSP_CONST.gc_Null_Str, 
                     PK_DIYDDTOPSP_CONST.gc_Default_Bank_Name,
                     TRIM(CLI_BANKNAME)
                   )                               AS BANKNAME,
                   TRIM(CLI_BANKID)                AS ROUTING_NUMBER
              FROM DIY_IQCLIENT
             WHERE CLI_USERID = p_DIY_COMPANY_USERID
            MINUS
            SELECT a.ACCOUNT_NUMBER, 
                   a.ACCOUNT_TYPE_CD, 
                   a.BANK_NAME, 
                   a.ROUTING_NUMBER
              FROM PSP_BANK_ACCOUNT         a,
                   PSP_COMPANY_BANK_ACCOUNT b
             WHERE a.BANK_ACCOUNT_SEQ = b.BANK_ACCOUNT_FK
               AND b.COMPANY_FK       = p_PSP_COMPANY_GUID
           );

    IF (v_match_count = 0) THEN
    
      v_temp_event_type := pc_Event_Type_Info;    
	  v_event_msg       := 'Company bank account data matches';
          
    ELSE

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Company bank account data does NOT match. AS400 COMP ID: ' || 
	                       p_DIY_COMPANY_USERID  ||
						   ' PSP COMP ID: '      ||
						   p_PSP_COMPANY_GUID    ||
						   '.';
	  
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);


    -- 09.30.2008 EMR
    -- moved pin compare to its own select due to the way they changed
    -- the pin model in psp.
    --
    -- removed this function call
    --   PK_DIYDDTOPSP_UTILS.FNGetEncryptedPin (
    --     p_DIY_COMPANY_USERID,
    --     1)                            AS AS400_QB_PIN1,
    
	SELECT COUNT(*)
	  INTO v_match_count
      FROM (
            SELECT CLI_STRIKE_COUNT                AS AS400_STRIKE_COUNT
              FROM DIY_IQCLIENT
             WHERE CLI_USERID = p_DIY_COMPANY_USERID
            MINUS
            SELECT (
                    SELECT COUNT(*)
                      FROM PSP_COMPANY_EVENT 
                     WHERE Company_FK    = p_PSP_COMPANY_GUID
                       AND Event_Type_CD = PK_DIYDDTOPSP_CONST.gc_PSP_Event_Strike_CD
                   )                                        AS PSP_Strike_Count
              FROM PSP_COMPANY
             WHERE Company_SEQ = p_PSP_COMPANY_GUID
          );

    IF (v_match_count = 0) THEN
    
      v_temp_event_type := pc_Event_Type_Info;    
	  v_event_msg       := 'Company DD service data matches (strike count)';
          
    ELSE

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Company DD service data (strike count) does NOT match. AS400 COMP ID: ' || 
	                       p_DIY_COMPANY_USERID                   ||
						   ' PSP COMP ID: '                       ||
						   p_PSP_COMPANY_GUID                     ||
						   '.';
	  
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);


    -- 10.03.2008 EMR
    -- had to change the rows to columns conversion because PSP has no order
    -- and in testing all three psp records inserted at the same fractional
    -- time.  thus the pin compare could not match up due to lack of order.
    -- althrough v2 works, it runs slower than v1.
    
    -- 10.21.2008 EMR
    -- change pin validation from comparing content to simply comparing row
    -- count.  its not glamerous but it is effective and better than nothing.
    -- java is extracting the pins due to character conversion issues with the
    -- gateway.  the java had to implement a configuration change and this
    -- compare is a stop gap measure to ensure pins floated over.

    /* V1
    
	SELECT COUNT(*)
	  INTO v_match_count
      FROM (
            SELECT MAX(DECODE(INROWNUM, 1, PSP_PIN, NULL)) AS PSP_PIN1,
                   MAX(DECODE(INROWNUM, 2, PSP_PIN, NULL)) AS PSP_PIN2, 
                   MAX(DECODE(INROWNUM, 3, PSP_PIN, NULL)) AS PSP_PIN3
              FROM (
                    SELECT ROWNUM AS INROWNUM,
                           COMPANY_FK,
                           P_I_N_VALUE AS PSP_PIN
                      FROM PSP_COMPANY_PIN
                     WHERE COMPANY_FK = p_PSP_COMPANY_GUID
                   )
             GROUP
                BY COMPANY_FK
             MINUS
            SELECT TRIM(PIN_1) AS AS400_PIN1, 
                   TRIM(PIN_2) AS AS400_PIN2, 
                   TRIM(PIN_3) AS AS400_PIN3
                   FROM DIY_DXPIN 
             WHERE PIN_USERID = p_DIY_COMPANY_USERID
           );
           
    */
      
    /* V2
    
    SELECT COUNT(*)
      INTO v_match_count
      FROM (
            SELECT P_I_N_VALUE AS PSP_PIN
              FROM PSP_COMPANY_PIN
             WHERE COMPANY_FK = p_PSP_COMPANY_GUID
            MINUS
            (
             SELECT TRIM(PIN_1) AS AS400_PIN1 
               FROM DIY_DXPIN 
              WHERE PIN_USERID = p_DIY_COMPANY_USERID
             UNION ALL
             SELECT TRIM(PIN_2) AS AS400_PIN2 
               FROM DIY_DXPIN 
              WHERE PIN_USERID = p_DIY_COMPANY_USERID
             UNION ALL
             SELECT TRIM(PIN_3) AS AS400_PIN3
               FROM DIY_DXPIN 
              WHERE PIN_USERID = p_DIY_COMPANY_USERID
            )
           );
           
    */

    SELECT COUNT(*)
      INTO v_match_count
      FROM (
            SELECT COUNT(*) AS PSP_PIN_CNT
              FROM (
                    SELECT P_I_N_VALUE AS PSP_PIN
                      FROM PSP_COMPANY_PIN
                     WHERE COMPANY_FK = p_PSP_COMPANY_GUID
                       AND TRIM(P_I_N_VALUE) IS NOT NULL
                   )
            MINUS
            SELECT NVL (
                     (
                      SELECT PIN1_CNT + PIN2_CNT + PIN3_CNT AS DIY_PIN_CNT
                        FROM (
                              SELECT DECODE(TRIM(PIN_1), '', 0, 1) AS PIN1_CNT,
                                     DECODE(TRIM(PIN_2), '', 0, 1) AS PIN2_CNT,
                                     DECODE(TRIM(PIN_3), '', 0, 1) AS PIN3_CNT
                                FROM DIY_DXPIN 
                               WHERE PIN_USERID = p_DIY_COMPANY_USERID
                             ) 
                     ), 0
                   ) AS DIY_PIN_CNT 
              FROM DUAL
           );
    
    IF (v_match_count = 0) THEN
    
      v_temp_event_type := pc_Event_Type_Info;    
	  v_event_msg       := 'Company PIN count matches';
          
    ELSE

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Company PIN count does NOT match. AS400 COMP ID: ' || 
	                       p_DIY_COMPANY_USERID                   ||
						   ' PSP COMP ID: '                       ||
						   p_PSP_COMPANY_GUID                     ||
						   '.';
	  
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);

    
    -- tell the caller how things turned out
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


  --
  -- THIS IS THE GRAND FINALE
  --
  
  PROCEDURE PR_VALIDATE_PSE_MIGRATION (
	p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
    
    v_MigQ_ER_Total                NUMBER;
    v_MigQ_Payrun_Total            NUMBER;
    v_MigQ_Paycheck_Total          NUMBER;
    v_MigQ_Contact_Princ_Total     NUMBER;
    v_MigQ_Contact_Admin_Total     NUMBER;    

    v_PSP_ER_Total                 NUMBER;
    v_PSP_Payrun_Total             NUMBER;
    v_PSP_Paycheck_Total           NUMBER;
    v_PSP_Contact_Total            NUMBER;

  BEGIN
  
    -- initialize to happy path
	b_data_matches    := TRUE;
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'GlobalCompare';    
	v_event_msg       := 'Scheduled and actual migrated data matches';
  
  
    --
    -- compute the totals for all interested metrics according to use case 47
    --
    
    -- 10.21.2008 EMR
    --   due to the way DB2 on the AS400 does joins the paycheck query is
    --   taking way way to long.  thus needed a fresh new approach towards
    --   comparing paychecks.  payrolls arent as fast as they could be but
    --   work.
    
    -- 10.23.2008 EMR
    --   had to move the contact compare from global to company validation.
    --   The reason is that these types of queries, where a big list is passed
    --   in, perform very bad in DB2 on the AS400.  I guess its just because
    --   its not Oracle.
    
    /* this was moved to company validation for performance reasons.
    
           (
            SELECT COUNT(*)
              FROM DIY_IQPRINC_CONTACT
             WHERE CLIP_USERID IN (
                     SELECT TO_NUMBER (SOURCE_DB_COMPANY_ID)
                       FROM COMPANY_MIGRATION 
                      WHERE MIGRATION_STATE_CD            = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
                        AND TRUNC (MIGRATION_ACTUAL_DATE) > TRUNC (SYSDATE - 1)
                   )
           ) AS Contacts_Principle_To_Migrate,
           (
            SELECT COUNT(*)
              FROM DIY_IQCONTACT
             WHERE CLIC_USERID IN (
                     SELECT TO_NUMBER (SOURCE_DB_COMPANY_ID)
                       FROM COMPANY_MIGRATION 
                      WHERE MIGRATION_STATE_CD            = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
                        AND TRUNC (MIGRATION_ACTUAL_DATE) > TRUNC (SYSDATE - 1)
                   )
           ) AS Contacts_Admin_To_Migrate,
           (
            SELECT COUNT(*)
              FROM PSP_CONTACT       a,
                   COMPANY_MIGRATION b
             WHERE a.COMPANY_FK                   = b.TARGET_DB_COMPANY_ID  
               AND b.MIGRATION_STATE_CD           = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
               AND TRUNC(b.MIGRATION_ACTUAL_DATE) > TRUNC (SYSDATE - 1)
           ) AS Contacts_Migrated
           
    */
      
    SELECT (
            SELECT COUNT(*)
              FROM COMPANY_MIGRATION a
             WHERE MIGRATION_STATE_CD           = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
               AND TRUNC(MIGRATION_ACTUAL_DATE) > TRUNC(SYSDATE - 1)
           ) AS Companies_To_Migrate,
           (
            SELECT COUNT(*)
              FROM PSP_COMPANY       a,
                   COMPANY_MIGRATION b
             WHERE a.COMPANY_SEQ                  = b.TARGET_DB_COMPANY_ID  
               AND b.MIGRATION_STATE_CD           = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
               AND TRUNC(b.MIGRATION_ACTUAL_DATE) > TRUNC(SYSDATE - 1)
           ) As Companies_Migrated,
           (
            SELECT COUNT (*)
              FROM TEMP_CACHE_IQACH
             WHERE ACH_USERID IN (
                     SELECT TO_NUMBER (SOURCE_DB_COMPANY_ID)
                       FROM COMPANY_MIGRATION 
                      WHERE MIGRATION_STATE_CD            = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
                        AND TRUNC (MIGRATION_ACTUAL_DATE) > TRUNC(SYSDATE - 1)
                   )
           ) AS Payruns_To_Migrate,
           (               
            SELECT COUNT(*)
              FROM PSP_PAYROLL_RUN   a,
                   COMPANY_MIGRATION b
             WHERE a.COMPANY_FK                   = b.TARGET_DB_COMPANY_ID  
               AND b.MIGRATION_STATE_CD           = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
               AND TRUNC(b.MIGRATION_ACTUAL_DATE) > TRUNC(SYSDATE - 1)
           ) AS Payruns_Migrated,
           ( 
            SELECT COUNT (*)
              FROM TEMP_CACHE_DXCHKXREF
             WHERE (XREF_USERID, XREF_TRACE_NUMBER) IN (
                     SELECT ACH_USERID, ACH_TRACE_NUMBER
                       FROM TEMP_CACHE_IQACH
                      WHERE ACH_USERID IN (
                              SELECT TO_NUMBER (SOURCE_DB_COMPANY_ID)
                                FROM COMPANY_MIGRATION
                               WHERE MIGRATION_STATE_CD            = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
                                 AND TRUNC (MIGRATION_ACTUAL_DATE) > TRUNC (SYSDATE - 1)
                            )
                   )
           ) AS Paychecks_To_Migrated,
           (
            SELECT COUNT(*)
              FROM PSP_PAYCHECK      a,
                   COMPANY_MIGRATION b,
                   PSP_PAYROLL_RUN   c
             WHERE a.PAYROLL_RUN_FK               = c.PAYROLL_RUN_SEQ
               AND c.COMPANY_FK                   = b.TARGET_DB_COMPANY_ID  
               AND b.MIGRATION_STATE_CD           = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD
               AND TRUNC(b.MIGRATION_ACTUAL_DATE) > TRUNC (SYSDATE - 1)
           ) AS Paychecks_Migrated,
           (
            0
           ) AS Contacts_Principle_To_Migrate,
           (
            0
           ) AS Contacts_Admin_To_Migrate,
           (
            0
           ) AS Contacts_Migrated
      INTO v_MigQ_ER_Total,
           v_PSP_ER_Total,
           v_MigQ_Payrun_Total,
           v_PSP_Payrun_Total,
           v_MigQ_Paycheck_Total,
           v_PSP_Paycheck_Total,
           v_MigQ_Contact_Princ_Total,
           v_MigQ_Contact_Admin_Total,
           v_PSP_Contact_Total
      FROM DUAL;


    --
    -- evaluate the totals
    --
    
    IF (v_MigQ_ER_Total = v_PSP_ER_Total) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Companies migrated totals match.  Total = ' || v_MigQ_ER_Total;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Companies migrated totals DO NOT match. Scheduled to migrate = ' ||
	                       v_MigQ_ER_Total || ', PSP = ' || v_PSP_ER_Total;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_MigQ_Payrun_Total = v_PSP_Payrun_Total) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Payrolls migrated totals match.  Total = ' || v_MigQ_Payrun_Total;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Payrolls migrated totals DO NOT match. Scheduled to migrate = ' ||
	                       v_MigQ_Payrun_Total || ', PSP = ' || v_PSP_Payrun_Total;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);
    

    IF (v_MigQ_Paycheck_Total = v_PSP_Paycheck_Total) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Paychecks migrated totals match.  Total = ' || v_MigQ_Paycheck_Total;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Paychecks migrated totals DO NOT match. Scheduled to migrate = ' ||
	                       v_MigQ_Paycheck_Total || ', PSP = ' || v_PSP_Paycheck_Total;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    /* moved contact validation to the company level

    IF ((v_MigQ_Contact_Princ_Total + v_MigQ_Contact_Admin_Total) = v_PSP_Contact_Total) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Contacts migrated totals match.  Total = ' || v_MigQ_Paycheck_Total;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Contacts migrated totals DO NOT match. Scheduled to migrate = ' ||
	                       v_MigQ_Contact_Princ_Total + v_MigQ_Contact_Admin_Total || ', PSP = ' || v_PSP_Contact_Total;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);
    
    */

  
    --
	-- REPORT THE FINDINGS
	--	  
	
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;


  


  -- ------------------------------------------------------------------------
  -- MAIN CONTROLLER TO DO THE VALIDATION
  -- ------------------------------------------------------------------------

  PROCEDURE PR_Compare_Controller (
    p_VALIDATION_MODE              IN  VARCHAR2,     -- C company, A all
    p_DIY_COMPANY_USERID           IN  NUMBER,
    p_PSP_COMPANY_GUID             IN  VARCHAR2,     -- optional, psp guid
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2    
  )
  IS
    b_company_exists               BOOLEAN;
    b_data_matches                 BOOLEAN;
    b_data_mismatch_found          BOOLEAN;
    v_temp_rc_msg                  VARCHAR2(500);
    v_company_migration_gseq       NUMBER;
  
  BEGIN

    -- hope for the best, at least to start
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';    
  
  
    -- 
    -- CAN EITHER VALIDATE ONE COMPANY OR ALL COMPANIES
    -- 

    -- this is for one company to compare pse to psp
    
    IF (p_VALIDATION_MODE = pc_validation_mode_comp) THEN
    
      -- BOTH AS400 AND PSP COMPANIES MUST EXIST TO DO THE VALIDATION
      
      b_company_exists := FN_AS400_COMP_EXISTS (p_DIY_COMPANY_USERID);
      
      IF (NOT b_company_exists) THEN
       
        RAISE_APPLICATION_ERROR (
          pc_raise_app_err_cd,
          'AS400 DIY COMPANY CANNOT BE FOUND.  ' || p_DIY_COMPANY_USERID || '.',
          FALSE);
          
      END IF;

      b_company_exists := FN_PSP_COMP_EXISTS (p_PSP_COMPANY_GUID);
      
      IF (NOT b_company_exists) THEN
       
        RAISE_APPLICATION_ERROR (
          pc_raise_app_err_cd,
          'PSP COMPANY CANNOT BE FOUND.  ' || p_PSP_COMPANY_GUID || '.',
          FALSE);
          
      END IF; 

      -- 
      -- COMPARE DIY COMPANY TO PSP COMPANY
      -- 
      
      -- 10.03.2008 EMR
      -- some validation will be omitted for termianted companies because 
      -- those companies, which can be old, tend to have bad data at lower
      -- levels.
      
      -- 10.22.2008 EMR
      -- modified the temp cache purge approach to benefit global validation.
      
      
      -- initialize to happy path
      b_data_matches        := TRUE;
      b_data_mismatch_found := FALSE;
      v_temp_rc_msg         := 'Mismatches found in the following: ';

      -- get migration gseq
      -- the as400 is actually a number but is stored in the migration
      -- queue as a varchar
      SELECT Company_Migration_Gseq
        INTO v_company_migration_gseq
        FROM COMPANY_MIGRATION
       WHERE Source_DB_Company_ID = TO_CHAR(p_DIY_COMPANY_USERID);
        
     
      -- company compare (1.)
      PR_VALIDATE_COMPANY_INFO (
        p_DIY_COMPANY_USERID, 
        p_PSP_COMPANY_GUID, 
        v_company_migration_gseq,
        b_data_matches
      );
            
      IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE; 
        v_temp_rc_msg         := v_temp_rc_msg || 'company info, ';              
      END IF;


      -- diy dd info (3.)
      PR_VALIDATE_DIY_DD_INFO (
        p_DIY_COMPANY_USERID, 
        p_PSP_COMPANY_GUID, 
        v_company_migration_gseq,
        b_data_matches
      );
            
      IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE;       
        v_temp_rc_msg         := v_temp_rc_msg || 'DIY DD info.';        
      END IF;
      
      
      -- when the company is terminated we do not migrate any of this info
      IF (NOT FN_Is_Company_Termed (p_DIY_COMPANY_USERID)) THEN

        -- diy dd info (3.)
        PR_VALIDATE_DIY_ACTIVE_DD_INFO (
          p_DIY_COMPANY_USERID, 
          p_PSP_COMPANY_GUID, 
          v_company_migration_gseq,
          b_data_matches
        );
            
        IF (NOT b_data_matches) THEN
          b_data_mismatch_found := TRUE;       
          v_temp_rc_msg         := v_temp_rc_msg || 'DIY DD info.';        
        END IF;
        

        -- only validate if the company has payrolls
        IF (FN_Company_Has_Payrolls (p_DIY_COMPANY_USERID)) THEN

          -- payroll compare (2.), if applicable
          PR_VALIDATE_PAYROLLS (
            p_DIY_COMPANY_USERID, 
            p_PSP_COMPANY_GUID, 
            v_company_migration_gseq,
            b_data_matches
          );

          IF (NOT b_data_matches) THEN
            b_data_mismatch_found := TRUE;       
            v_temp_rc_msg         := v_temp_rc_msg || 'payrolls,';        
          END IF;
          
          --
          -- PURGE THE LOCAL PAYROLL CACHE
          --
    
          -- 10.22.2008 EMR
          -- only purge the paychecks
          
          --PK_DIYDDTOPSP_UTILS.PR_PURGE_PAYROLL_CACHE (
          --  p_DIY_COMPANY_USERID
          --);
          
          PK_DIYDDTOPSP_UTILS.PR_PURGE_PAYCHECK_CACHE (
            p_DIY_COMPANY_USERID
          );
          

        END IF;

      END IF;

      --
      -- report the results back the the calling api
      --

      IF (b_data_mismatch_found) THEN

        p_RETURN_CD  := -1;
        p_RETURN_MSG := v_temp_rc_msg;

      END IF;
       
      
    END IF;

    
    --
    -- GLOBAL COMPARE AT END OF MIGRATION
    --
    
    IF (p_VALIDATION_MODE = pc_validation_mode_summary) THEN

      PR_VALIDATE_PSE_MIGRATION (
        b_data_matches);

      IF (NOT b_data_matches) THEN
        p_RETURN_CD  := -1;
        p_RETURN_MSG := 'The DIY companies scheduled to migrate and those actually migrated to PSP have global data mismatches.';
      END IF;
      
    END IF;
    
  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;
  

  -- ------------------------------------------------------------------------
  -- PUBLIC APIs
  -- ------------------------------------------------------------------------

  PROCEDURE PR_Validate_DIY_Company (
    p_DIY_COMPANY_USERID           IN  NUMBER,     -- SOURCE
    p_PSP_COMPANY_GUID             IN  VARCHAR2,   -- TARGET
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    v_temp_RC                      NUMBER;
    v_temp_RC_MSG                  VARCHAR2(500);

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    PR_Compare_Controller (
      pc_validation_mode_comp,
      p_DIY_COMPANY_USERID,
      p_PSP_COMPANY_GUID,
      v_temp_RC,
      v_temp_RC_MSG
    );

    IF (v_temp_RC <> 0) THEN

      p_RETURN_CD  := -1;
      p_RETURN_MSG := SUBSTR (
        (
         'Data inconsistencies were found. ' || v_temp_RC_MSG
        ),
        1,
        500
      );

    END IF;
    
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
        (
         'Unexpected error found while validating company. ' ||
         SQLERRM
        ),
        1,
        500
      );
  END;


  PROCEDURE PR_Validate_DIY_Migration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    v_temp_RC                      NUMBER;
    v_temp_RC_MSG                  VARCHAR2(500);

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    PR_Compare_Controller (
      pc_validation_mode_summary,
      NULL,   -- p_DIY_COMPANY_USERID  -- SOURCE
      NULL,   -- p_PSP_COMPANY_GUID    -- TARGET
      v_temp_RC,
      v_temp_RC_MSG
    );
    
    IF (v_temp_RC <> 0) THEN

      p_RETURN_CD  := -1;
      p_RETURN_MSG := SUBSTR (
        (
         'Data inconsistencies were found.' || v_temp_RC_MSG
        ),
        1,
        500
      );

    END IF;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
        (
         'Unexpected error while validating IOP migrated data. ' ||
         SQLERRM
        ),
        1,
        500
      );
  END;

  
END PK_DIYDDTOPSP_VALIDATION; 
/


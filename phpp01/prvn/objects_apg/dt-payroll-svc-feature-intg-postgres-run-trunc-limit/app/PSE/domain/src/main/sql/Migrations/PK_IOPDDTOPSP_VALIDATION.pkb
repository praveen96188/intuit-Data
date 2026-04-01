CREATE OR REPLACE PACKAGE BODY PK_IOPDDTOPSP_VALIDATION AS
/******************************************************************************
   NAME:       PK_IOPDDTOPSP_VALIDATION
   UPDATED:    10.15.2008 01:00 PM   
   PURPOSE:    Validate company data once it is migrated from PSE to PSP.
               Might also be used to validate DIY data migration.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        04.01.2008  EMR              created validation framework.
   1.1        05.02.2008  EMR              added monitoring api
   1.2        05.08.2008  EMR              added purge api to erase logs
   1.3        05.22.2008  EMR              changed dblink pspmigration to 
                                           pspmigration.world
   1.4        06.06.2008  EMR              fixed pay run date utc compare
   1.5        06.10.2008  EMR              remapped payroll run status for UC 66
   1.6        06.30.2008  EMR              updated because PSP updated
                                           - unique ids changed to table_seq
                                           - event type mapping changed
                                           - events in general now match pse
                                           - some val tables now _CD and 
                                             constraints
   1.6.1      07.01.2008  EMR              changed fee accts on ledger
   1.6.2      07.02.2008  EMR              remapped txn types, pse to psp
   2.0        07.08.2008  EMR              adapted to common framework
   2.0.1      07.10.2008  EMR              modified ledger compare for fees                                            
   2.0.2      07.17.2008  EMR              used new range for raise app err
   2.0.3      08.29.2008  EMR              changed settlement type check
   2.0.4      09.17.2008  EMR              changed DD status compare - hold
   2.0.5      09.26.2008  EMR              changed DD status compare 
                                             - suspend, pending term
   2.0.6      09.29.2008  EMR              fixed global event compare to handle
                                             new event types in PSP
   2.0.7      10.07.2008  EMR              fixed daylight savings for fall 2008
   2.0.8      10.10.2008  EMR              modified company count validation
                                             to omit company 8350
   2.0.8.1    10.15.2008  EMR              had to modify some global compares
                                             for 8350.                       
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- CURSORS: 
  -- ------------------------------------------------------------------------
  
  -- return a list of companies ready to migrate from DIY to PSP
  
  CURSOR cur_employee_mismatch (
    p_PSE_COMPANY_DB_ID            IN  NUMBER,  
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2
  )
  IS
    SELECT c.SOURCE_EMPLOYEE_ID  AS EE_SRC_ID,
           d.FIRST_NAME          AS EE_F_NAME,
           d.MIDDLE_NAME         AS EE_M_NAME,
           d.LAST_NAME           AS EE_L_NAME,
           c.STATUS_CD           AS EE_STATUS,
           a.SOURCE_BANK_ACCT_ID AS EE_SRC_BA, 
    	   a.STATUS_CD           AS EE_BA_STATUS, 
           b.ACCT_TYPE_CD        AS EE_BA_TYPE, 
           b.BANK_NAME           AS EE_BA_NAME, 
           b.ROUTING_NUM         AS EE_BA_RTN, 
    	   b.ACCT_NUM	         AS EE_BA_ACCT_NUM 
      FROM EMP_BANK_ACCT_ASSOC a,
       	   BANK_ACCT           b,
           EMPLOYEE            c,
           INDIVIDUAL          d
     WHERE a.BANK_ACCT_GSEQ = b.BANK_ACCT_GSEQ
       AND a.COMPANY_GSEQ   = c.COMPANY_GSEQ
       AND a.EMPLOYEE_GSEQ  = c.EMPLOYEE_GSEQ
       AND c.EMPLOYEE_GSEQ  = d.INDIVIDUAL_GSEQ
       AND c.COMPANY_GSEQ   = p_PSE_COMPANY_DB_ID
    MINUS
    SELECT c.SOURCE_EMPLOYEE_ID     AS EE_SRC_ID,
           d.FIRST_NAME             AS EE_F_NAME,
           d.MIDDLE_NAME            AS EE_M_NAME,
           d.LAST_NAME              AS EE_L_NAME,
           DECODE (
    	     c.STATUS_CD,
    		 'Active',              'ACTV',
    		 'PendingVerification', 'PNDVER',
    		 'Inactive',            'INACTV'
    	   )                        AS EE_STATUS,
           a.SOURCE_BANK_ACCOUNT_ID AS EE_SRC_BA,
           DECODE (
    	     a.STATUS_CD,
    		 'Active',              'ACTV',
    		 'PendingVerification', 'PNDVER',
    		 'Inactive',            'INACTV'
    	   )                        AS EE_BA_STATUS,
           DECODE (
    	     b.ACCOUNT_TYPE_CD,
    		 'Checking', 'C',
    		 'Savings',  'S'
    	   )                        AS EE_BA_TYPE,
    	   b.BANK_NAME              AS EE_BA_NAME,	   
    	   b.ROUTING_NUMBER         AS EE_BA_RTN,
           b.ACCOUNT_NUMBER         AS EE_BA_ACCT_NUM	   	    
      FROM PSP_EMPLOYEE_BANK_ACCOUNT@PSPMIGRATION.WORLD a,	   
           PSP_BANK_ACCOUNT@PSPMIGRATION.WORLD	        b,
           PSP_EMPLOYEE@PSPMIGRATION.WORLD              c,
           PSP_INDIVIDUAL@PSPMIGRATION.WORLD            d
     WHERE a.BANK_ACCOUNT_FK = b.BANK_ACCOUNT_SEQ
       AND a.EMPLOYEE_FK     = c.EMPLOYEE_SEQ
       AND c.EMPLOYEE_SEQ    = d.INDIVIDUAL_SEQ
       AND c.COMPANY_FK      = p_PSP_COMPANY_DB_ID;

	   
  -- ------------------------------------------------------------------------
  -- PRIVATE UTILITY FUNCTIONS
  -- ------------------------------------------------------------------------

  FUNCTION FN_PSE_COMP_EXISTS (
    p_PSE_COMPANY_DB_ID            IN  NUMBER)
	RETURN BOOLEAN
  IS
    b_company_exists               BOOLEAN := FALSE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

	SELECT COUNT(*)
	  INTO v_temp_count
	  FROM COMPANY
	 WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID;
	
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
	  FROM PSP_COMPANY@PSPMIGRATION.WORLD
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


  FUNCTION FN_GET_MIGRATION_Q_PK (
    p_PSE_COMPANY_GSEQ             IN  NUMBER)
	RETURN COMPANY_MIGRATION.COMPANY_MIGRATION_GSEQ%TYPE
  IS
    v_temp_pk                      COMPANY_MIGRATION.COMPANY_MIGRATION_GSEQ%TYPE;
	
  BEGIN

	SELECT Company_Migration_Gseq
	  INTO v_temp_pk
	  FROM COMPANY_MIGRATION
	 WHERE Source_DB_Company_ID = (
       SELECT Source_Company_ID
         FROM COMPANY
        WHERE Company_Gseq = p_PSE_COMPANY_GSEQ);
	
    RETURN v_temp_pk;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;


  FUNCTION FN_GET_PSP_DD_STATUS (
    p_PSE_COMPANY_DB_ID            IN  NUMBER)
	RETURN VARCHAR2
  IS
    v_psp_status_cd                VARCHAR2(255); -- PSP_COMPANY_SERVICE.STATUS_CD
	
	v_comp_status                  COMPANY.STATUS_CD%TYPE;
	v_dd_status                    DD_COMPANY_INFO.DD_COMP_STATUS_CD%TYPE;
	b_any_payrolls                 VARCHAR2(10);
	b_no_payrolls_acct_exists      VARCHAR2(10);
	b_no_payrolls_or_accts         VARCHAR2(10);
	
  BEGIN
  
    v_psp_status_cd := 'CHANGE';
	
	SELECT a.STATUS_CD,
	       b.DD_COMP_STATUS_CD,
		   DECODE (
             NVL (
               (
			    SELECT 1
                  FROM COMPANY c
                 WHERE c.COMPANY_GSEQ = a.COMPANY_GSEQ
                  AND EXISTS (
                      SELECT 1
          		        FROM PAYROLL_RUN d
          		       WHERE c.COMPANY_GSEQ = d.COMPANY_GSEQ)
			   ),
			   0
			 ),
		     1, PK_IOPDDTOPSP_CONST.gc_TRUE,
		     0, PK_IOPDDTOPSP_CONST.gc_FALSE
	       ) AS PSE_PAYROLL_EXISTS,
		   DECODE (
             NVL (
               (
			    SELECT 1
                  FROM COMPANY c
                 WHERE c.COMPANY_GSEQ = a.COMPANY_GSEQ
                  AND NOT EXISTS (
                        SELECT 1
          		          FROM PAYROLL_RUN d
          		         WHERE c.COMPANY_GSEQ = d.COMPANY_GSEQ
				      )
				  AND EXISTS (
                        SELECT 1 
	                      FROM COMP_BANK_ACCT_ASSOC e
		                 WHERE c.COMPANY_GSEQ =  e.COMPANY_GSEQ
		                   AND e.STATUS_CD    = 'ACTV'
	                  )
			   ),
			   0
			 ),
		     1, PK_IOPDDTOPSP_CONST.gc_TRUE,
		     0, PK_IOPDDTOPSP_CONST.gc_FALSE
	       ) AS PSE_NO_PAYROLL_ACCT_EXISTS,
		   DECODE (
             NVL (
               (
			    SELECT 1
                  FROM COMPANY c
                 WHERE c.COMPANY_GSEQ = a.COMPANY_GSEQ
                  AND NOT EXISTS (
                        SELECT 1
          		          FROM PAYROLL_RUN d
          		         WHERE c.COMPANY_GSEQ = d.COMPANY_GSEQ
				      )
				  AND NOT EXISTS (
                        SELECT 1 
	                      FROM COMP_BANK_ACCT_ASSOC e
		                 WHERE c.COMPANY_GSEQ =  e.COMPANY_GSEQ
		                   AND e.STATUS_CD    = 'ACTV'
	                  )
			   ),
			   0
			 ),
		     1, PK_IOPDDTOPSP_CONST.gc_TRUE,
		     0, PK_IOPDDTOPSP_CONST.gc_FALSE
	       ) AS PSE_NO_PAYROLL_NO_ACCT
	  INTO v_comp_status,
	       v_dd_status,
	       b_any_payrolls,
	       b_no_payrolls_acct_exists,
	       b_no_payrolls_or_accts
	  FROM COMPANY         a,
	       DD_COMPANY_INFO b
	 WHERE a.COMPANY_GSEQ = b.COMPANY_GSEQ
	   AND a.COMPANY_GSEQ = p_PSE_COMPANY_DB_ID;


    -- PSE: statuses not used
    --      'AchRejectOther', 
    --      'AchRejectR1R9', 
    --      'ActiveSeasonal', 
    --      'AuditCorrections', 
    --      'Fraud', 
    --      'IntuitCollections', 
    --      'MissingPaperwork', 
    --      'NoticeOfChange', 
    --      'PendingBalanceFile', 
    --      'PendingCancellation', 
    --      'PendingPinCreation', 
    --      'RiskCollections', 
    --      'SuspendedDirectDeposit', 

    -- please note that the order of checking
	-- active active is very important, in terms of
	-- whether payrolls or accounts exists.  thus
	-- keep the order below.
    
    -- 09.17.2008 EMR
    -- modified this case statement, and why, well because PSP changed
    -- the status model.  Now various statuses - pendign activation, 
    -- pending termination, suspend, hold - have all become on hold reasons.
    -- thus the statuses are the same and a select count is needed to determine
    -- if a psp company is on hold ... nice.
    
    /* V1
		   
	SELECT CASE 
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'PNDACTVN' THEN
               'FraudReview'
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'PNDTERMN' THEN
               'PendingTermination'
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'SSPND'    THEN
               'DirectDepositLimit'
	       WHEN v_comp_status  = 'INACTV'   AND
		        v_dd_status    = 'CNCLD'    THEN
               'Cancelled'
	       WHEN v_comp_status  = 'INACTV'   AND
		        v_dd_status    = 'TERMD'    THEN
               'Terminated'
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'HOLD'     THEN
               'RiskAssessment'
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'ACTV'     AND   
				b_any_payrolls = PK_IOPDDTOPSP_CONST.gc_TRUE   THEN
               'ActiveCurrent'
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'ACTV'     AND   
				b_no_payrolls_or_accts    = PK_IOPDDTOPSP_CONST.gc_TRUE   THEN
               'PendingBankVerification'
	       WHEN v_comp_status  = 'ACTV'     AND
		        v_dd_status    = 'ACTV'     AND   
				b_no_payrolls_acct_exists = PK_IOPDDTOPSP_CONST.gc_TRUE   THEN
               'PendingFirstPayroll'
		   ELSE 
		       'StatusNotFound'
           END			   
	  INTO v_psp_status_cd
	  FROM DUAL;
    
    */

	SELECT CASE
	       WHEN v_comp_status  = 'INACTV'    AND
		        v_dd_status    = 'CNCLD'     THEN
               'Cancelled'
	       WHEN v_comp_status  = 'INACTV'    AND
		        v_dd_status    = 'TERMD'     THEN
               'Terminated'
	       WHEN v_comp_status  = 'ACTV'      AND
		        (
                 v_dd_status    = 'ACTV'     OR 
                 v_dd_status    = 'PNDACTVN' OR
                 v_dd_status    = 'PNDTERMN' OR
                 v_dd_status    = 'SSPND'    OR
                 v_dd_status    = 'HOLD'
                )                            AND
				b_any_payrolls = PK_IOPDDTOPSP_CONST.gc_TRUE   THEN
               'ActiveCurrent'
	       WHEN v_comp_status  = 'ACTV'      AND
		        (
                 v_dd_status    = 'ACTV'     OR 
                 v_dd_status    = 'PNDACTVN' OR
                 v_dd_status    = 'PNDTERMN' OR
                 v_dd_status    = 'SSPND'    OR
                 v_dd_status    = 'HOLD'
                )                            AND
				b_no_payrolls_or_accts    = PK_IOPDDTOPSP_CONST.gc_TRUE   THEN
               'PendingBankVerification'
	       WHEN v_comp_status  = 'ACTV'      AND
		        (
                 v_dd_status    = 'ACTV'     OR 
                 v_dd_status    = 'PNDACTVN' OR
                 v_dd_status    = 'PNDTERMN' OR
                 v_dd_status    = 'SSPND'    OR
                 v_dd_status    = 'HOLD'
                )                            AND
				b_no_payrolls_acct_exists = PK_IOPDDTOPSP_CONST.gc_TRUE   THEN
               'PendingFirstPayroll'
		   ELSE
		       'StatusNotFound'
           END
	  INTO v_psp_status_cd
	  FROM DUAL;

    RETURN v_psp_status_cd;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;


  FUNCTION FN_GET_PSP_ONHOLD_COUNT (
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2)
	RETURN NUMBER
  IS
    v_onhold_cnt                   NUMBER;
	
  BEGIN

    SELECT COUNT(*) 
      INTO v_onhold_cnt
      FROM PSP_ON_HOLD_REASON@PSPMIGRATION.WORLD
     WHERE COMPANY_FK = p_PSP_COMPANY_DB_ID
       AND EXPIRATION_DATE IS NULL;
	  
    RETURN v_onhold_cnt;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;

  
  FUNCTION FN_GET_PSE_LEDGER_ACCT (
    p_PSP_LEDGER_ACCT_CD           IN  VARCHAR2)
	RETURN LEDGER.LEDGER_ACCT_CD%TYPE
  IS
    v_pse_ledger_acct              LEDGER.LEDGER_ACCT_CD%TYPE;
	
  BEGIN

    -- not in PSE, new in psp
	--   SalesAndUseTax
    
    -- 07.10.2008 changed names of fee accounts
    --  was ...
    --         'FeeCash',            'FC',    -- Fee Cash
    --         'FeeReceivable',      'FR'	  -- Fee Receivable
  
    SELECT DECODE (
	         p_PSP_LEDGER_ACCT_CD,
             'BadDebt',             'BD',	-- Bad Debt
             'DDCurrentCash',       'DDCC',	-- DD Current Cash
             'DDCurrentLiability',  'DDCL',	-- DD Current Liability
             'DDFutureLiability',   'DDFL',	-- DD Future Liability
             'DDFutureReceivable',  'DDFR',	-- DD Future Receivable
             'EEReturnCash',        'EERC',	-- EE Return Cash
             'EEReturnLiablility',  'EERL',	-- EE Return Liability 
             'ERReturnCash',        'ERRC',	-- ER Return Cash
             'ERReturnReceivable',  'ERRR',	-- ER Return Receivable
             'FeeCashRevenue',      'FC',   -- Fee Cash
             'FeeIncome',           'FI',	-- Fee Income
             'FeeCashBalanceSheet', 'FR'    -- Fee Receivable
           )
	  INTO v_pse_ledger_acct
	  FROM DUAL;
	  
    RETURN v_pse_ledger_acct;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;

/*  
  FUNCTION FN_GET_PSE_PAYROLL_STATUS (
    p_PSP_PAYROLL_STATUS_CD        IN  VARCHAR2)
	RETURN TXN_STATE_VAL.TXN_STATE_CD%TYPE
  IS
    v_pse_payroll_status           PAYROLL_RUN.PAYROLL_STATUS_CD%TYPE;
	
  BEGIN
  
                 if ("DBRTNRDBPE")	PendingRedebit;
        	else if ("DBRTNRDBOF")	RedebitOffloaded;
        	else if ("NSF")		    DebitReturned;
        	else if ("NSFREDBPND")	PendingAutoRedebit;
        	else if ("NSFREDBOFF")	AutoRedebitOffloaded;
        	else if ("NSFTWICE")	ReturnedTwice;

        	else if ("WRITTENOFF")	WrittenOff;
        	else if ("PENDING")	    Pending;
        	else if ("OFFLDDEBIT")	OffloadedDebit;
        	else if ("OFFLDALL")	OffloadedAll;
        	else if ("NSFCANCEL")	NSFCanceled;
        	else if ("DBRETURNED")	DebitReturned;
        	else if ("DBRTNCANCL")	DebitReturnedCanceled;
        	else if ("CANCELED")	Canceled;
        	else if ("COMPLETE")	Complete
  

    SELECT DECODE (
	         p_PSP_PAYROLL_STATUS_CD,
             'Complete',                      'COMPLETE', 
             'Canceled',                      'CANCELED', 
             'DebitReturnedCanceled',         'DBRTNCANCL', 
             'DebitReturned',                 'DBRETURNED', <-- twice
             'NSFTwice',                      'NSFTWICE',   <-- changed
             'NSFRedebitOffloaded',           'NSFREDBOFF', <-- changed
             'NSFRedebitPending',             'NSFREDBPND', <-- changed
             'NSFCanceled',                   'NSFCANCEL', 
             'NSF',                           'NSF',        <-- changed, twice
             'OffloadedAll',                  'OFFLDALL', 
             'OffloadedDebit',                'OFFLDDEBIT', 
             'Pending',                       'PENDING',
             'DebitReturnedRedebitPending',   'DBRTNRDBPE', <-- changed
             'DebitReturnedRedebitOffloaded', 'DBRTNRDBOF', <-- changed
             'WrittenOff',                    'WRITTENOFF'
           )
	  INTO v_pse_payroll_status
	  FROM DUAL;
	  
    RETURN v_pse_payroll_status;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
*/
  
  
  FUNCTION FN_GET_PSP_PAYROLL_STATUS (
    p_PSE_PAYROLL_STATUS_CD        IN  VARCHAR2)
	RETURN VARCHAR2
  IS
    v_psp_payroll_status           VARCHAR2(100);  -- cant use type because psp constraint not table
	
  BEGIN

    -- the following were changed in UC 66
	-- 06.10.2008  
    --           if ("DBRTNRDBPE")	PendingRedebit;
    --      else if ("DBRTNRDBOF")	RedebitOffloaded;
    --      else if ("NSF")		    DebitReturned;
    --      else if ("NSFREDBPND")	PendingAutoRedebit;
    --      else if ("NSFREDBOFF")	AutoRedebitOffloaded;
    --      else if ("NSFTWICE")	ReturnedTwice;

    SELECT DECODE (
	         p_PSE_PAYROLL_STATUS_CD,
             'COMPLETE',   'Complete',                       
             'CANCELED',   'Canceled',                       
             'DBRTNCANCL', 'DebitReturnedCanceled',          
             'DBRETURNED', 'DebitReturned',
             'NSFTWICE',   'ReturnedTwice',
             'NSFREDBOFF', 'AutoRedebitOffloaded',
             'NSFREDBPND', 'PendingAutoRedebit',
             'NSFCANCEL',  'NSFCanceled',                    
             'NSF',        'DebitReturned',
             'OFFLDALL',   'OffloadedAll',                   
             'OFFLDDEBIT', 'OffloadedDebit',                 
             'PENDING',    'Pending',                       
             'DBRTNRDBPE', 'PendingRedebit',
             'DBRTNRDBOF', 'RedebitOffloaded',
             'WRITTENOFF', 'WrittenOff'                    
           )
	  INTO v_psp_payroll_status
	  FROM DUAL;
	  
    RETURN v_psp_payroll_status;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  

  FUNCTION FN_GET_PSP_TXN_TYPE (
    p_PSE_TXN_TYPE_CD              IN  TXN_TYPE_VAL.TXN_TYPE_CD%TYPE)
	RETURN VARCHAR2
  IS
    v_psp_txn_type                 VARCHAR2(100);
	
  BEGIN

    -- in PSE but dropped in PSP
    --   ERFUNDS	ER Funds
    --   ERPREFUND	Employer Prefund
    
    -- not in PSE, new in psp
    --   DdFraud
    --   DdServiceFee
    --   DdServiceFeeRefundCredit
    --   ServiceSalesAndUseTax
    --   ServiceSalesAndUseTaxRefundCredit
    --   UntimelyReturnPostWriteOff
    --   UntimelyReturnPreWriteOff

    --   BadDebtRecoveryFee
    --   BadDebtRecoverySalesAndUseTax
    --   EmployerFeeDebit
    --   EmployerFeeRedebit
    --   EmployerWriteOffFee
    --   EmployerWriteOffSalesAndUseTax
    --   IntuitFeeTransfer
    --   ServiceSalesAndUseTaxRedebit
    --   ServiceSalesAndUseTaxReturnedRefundCredit
    
    -- 07.02.2008 changed mapping from psp to pse, to pse to psp.  why you ask?
    --   because psp was simplified so we had to map M :: 1.
  
    SELECT DECODE (
	         p_PSE_TXN_TYPE_CD,
             'EEDDCR',     'EmployeeDdCredit',                          -- EE DD Credit          
             'EEDDRVDB',   'EmployeeDdReversalDebit',                   -- EE DD Reversal Debit      
             'EEESCR',     'EmployeeEscalationCredit',                  -- Employee Escalation Credit      
             'ER2PRFCR',   'EmployerDoublePaymentRefundCredit',         -- Employer Double Payment Refund Credit      
             'ERDDDB',     'EmployerDdDebit',                           -- ER DD Debit      
             'ERDDREDB',   'EmployerDdRedebit',                         -- ER DD Redebit      
             'ERDDRFCR',   'EmployerDdRefundCredit',                    -- ER DD Refund Credit      
             'ERDDRJRFCR', 'EmployerDdRejectRefundCredit',              -- ER DD Reject Refund Credit      
             'ERDDRTRFCR', 'EmployerDdReturnedRefundCredit',            -- ER DD Returned Refund Credit      
             'ERDDRVRFCR', 'EmployerDdReversalRefundCredit',            -- ER DD Reversal Refund Credit      
             'ERESCR',     'EmployerEscalationCredit',                  -- Employer Escalation Credit      
             'ERVERDB',    'EmployerVerificationDebit',                 -- ER Verification Debit
             'ERWO',       'EmployerWriteOff',                          -- ER Write Off
             'INT5DRTXFR', 'Intuit5DayReturnTransfer',                  -- Intuit 5day Return Transfer
             'INTEERTXFR', 'IntuitEmployeeReturnTransfer',              -- Intuit EE Return Transfer
             'INTERVRRTX', 'IntuitEmployerVerificationReturnTransfer',  -- Intuit ER Verification Return Transfer
             'WODEBTRCVR', 'BadDebtRecovery',                           -- Bad Debt Recovery
             -- 07.02.2008 remapped per UC 31 and 73
             'ERFERTDB',   'EmployerFeeDebit',                          -- ER Return Fee Debit      
             'ERFERTREDB', 'EmployerFeeRedebit',                        -- ER Return Fee Redebit      
             'ERFERTRFCR', 'EmployerFeeRefundCredit',                   -- ER Return Fee Refund Credit      
             'ERFERVDB',   'EmployerFeeDebit',                          -- ER Reversal Fee Debit
             'ERFERVREDB', 'EmployerFeeRedebit',                        -- ER Reversal Fee Redebit
             'ERFERVRFCR', 'EmployerFeeRefundCredit',                   -- ER Reversal Fee Refund Credit
             'INTERFERTX', 'IntuitFeeTransfer',                         -- Intuit ER Return Fee Transfer
             'INTERFERVX', 'IntuitFeeTransfer'                         -- Intuit ER Reversal Fee Transfer
           )
	  INTO v_psp_txn_type
	  FROM DUAL;
	  
    RETURN v_psp_txn_type;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;

  
  FUNCTION FN_GET_PSE_TXN_STATE (
    p_PSP_TXN_STATE_CD             IN  VARCHAR2)
	RETURN TXN_STATE_VAL.TXN_STATE_CD%TYPE
  IS
    v_pse_txn_state                TXN_STATE_VAL.TXN_STATE_CD%TYPE;
	
  BEGIN

    SELECT DECODE (
	         p_PSP_TXN_STATE_CD,
             'Cancelled', 'CLD', 
             'Completed', 'CP',
             'Created',   'CR',   
             'Executed',  'EX', 
             'Returned',  'RTN',  
             'Voided',    'VOID'   
           )
	  INTO v_pse_txn_state
	  FROM DUAL;
	  
    RETURN v_pse_txn_state;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;


  FUNCTION FN_GET_PSE_EVENT_TYPE (
    p_PSP_EVENT_TYPE_CD             IN  VARCHAR2)
	RETURN EVENT_TYPE_VAL.EVENT_TYPE_CD%TYPE
  IS
    v_pse_event_type                EVENT_TYPE_VAL.EVENT_TYPE_CD%TYPE;
	
  BEGIN

    -- new in PSP and does not exist in PSE
    --   BankAccountVerified
    --   EINChanged
    --   PINCreated
    --   PINUpdated
    --   TransmissionError
    --   LegalNameChanged
    --   LegalAddressChanged
    --   CompanyBankAccountChange 
    --   FeeCreated
    --   CoaFeeAccountChange
    --   CoaSalesTaxAccountChange    
    
    --   this was changed in the latest round of psp changes to event model
    --     'DDIncreasePayrollLimit',         'DDINCPRLMT',  -- Increase Payroll Limit
  
    SELECT DECODE (
	         p_PSP_EVENT_TYPE_CD,
             'ACHReturn',                      'ACHRETRN',	  -- ACH Return
             'CompanyBankAccountStatusChange', 'CBASTATCHG',  -- Company Bank Account Status Change
             'ServiceStatusChange',            'DDSTATCHG',	  -- Direct Deposit Company Status Change
             'DDIncreasePayrollLimit',         'DDINCPRLMT',  -- Increase Payroll Limit
             'LimitViolation',                 'DDOVRPRLMT',  -- Payroll Limit Violation
             'PayrollCancelled',               'PYRLCANCLD',  -- Payroll Cancelled
             'ReversalOK',                     'REVERSALOK',  -- Reversal Successful
             'ReversalRequested',              'REVERSALRQ',  -- Reversal Requested
             'Strike',                         'STRIKE'	      -- Strike
           )
	  INTO v_pse_event_type
	  FROM DUAL;
	  
    RETURN v_pse_event_type;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  
  
  --
  -- these are for global validation
  --
  
  FUNCTION FN_PAYROLL_STATUSES_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN
  
    SELECT COUNT(*)
	  INTO v_temp_count
      FROM (
            SELECT PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_PAYROLL_STATUS (
			         PAYROLL_STATUS_CD) AS PSP_PAYROLL_RUN_STATUS,
                   COUNT(*) AS PSE_PAYRUN_STAT_COUNT
              FROM PAYROLL_RUN
             GROUP
                BY PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_PAYROLL_STATUS (
			         PAYROLL_STATUS_CD)
            MINUS
			SELECT PAYROLL_RUN_STATUS,
			       COUNT(*) AS PSP_PAYRUN_STAT_COUNT
              FROM PSP_PAYROLL_RUN@PSPMIGRATION.WORLD
             GROUP
                BY PAYROLL_RUN_STATUS
      	   );

    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;


  FUNCTION FN_TRANSACTIONS_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

    SELECT COUNT(*)
	  INTO v_temp_count
      FROM ( 
            SELECT TRUNC(a.SETTLEMENT_DATE), 
                   b.SOURCE_COMPANY_ID,
                   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_TXN_TYPE (
                     a.TXN_TYPE_CD), 
                   a.CURRENT_TXN_STATE_CD, 
            	   COUNT(*) AS PSE_TXN_COUNT
              FROM FINANCIAL_TXN  a,
                   COMPANY        b
             WHERE a.COMPANY_GSEQ = b.COMPANY_GSEQ
             GROUP
                BY PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_TXN_TYPE (
                     a.TXN_TYPE_CD),
            	   b.SOURCE_COMPANY_ID, 
                   a.CURRENT_TXN_STATE_CD, 
            	   TRUNC(a.SETTLEMENT_DATE)
            MINUS
            SELECT TRUNC(a.SETTLEMENT_DATE),
                   b.SOURCE_COMPANY_ID,
                   a.TRANSACTION_TYPE_FK,
                   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSE_TXN_STATE (
				     a.CURRENT_TRANSACTION_STATE_FK),
            	   COUNT(*) AS PSP_TXN_COUNT
              FROM PSP_FINANCIAL_TRANSACTION@PSPMIGRATION.WORLD a,
                   PSP_COMPANY@PSPMIGRATION.WORLD               b
             WHERE a.COMPANY_FK = b.COMPANY_SEQ  
             GROUP
                BY a.TRANSACTION_TYPE_FK,
            	   b.SOURCE_COMPANY_ID,
                   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSE_TXN_STATE (
				     a.CURRENT_TRANSACTION_STATE_FK),
                   TRUNC(a.SETTLEMENT_DATE)
    	   );
  
    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  

  FUNCTION FN_AUDIT_HISTORY_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

    -- 10.15.2008 EMR
    --   omit bogus PSE company that will not be migrated.

    SELECT (
            SELECT COUNT(*) 
              FROM AUDIT_COLUMN
             WHERE COMPANY_GSEQ <> PK_IOPDDTOPSP_CONST.gc_Bogus_Company_8350 
	       ) -
	       (
            SELECT COUNT(*) 
              FROM PSP_PROPERTY_AUDIT@PSPMIGRATION.WORLD
	       ) AS AUDIT_COMPARE_CNT
	  INTO v_temp_count	
      FROM DUAL;	

    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  

  FUNCTION FN_COMPANY_EVENTS_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

    -- 09.29.2008 EMR
    -- modified PSP side to remove new event types generated during migration
    -- that do not exist in PSE.

    SELECT COUNT(*)
	  INTO v_temp_count
      FROM (  
            SELECT event_type_cd AS PSE_EVENT_TYPE, 
                   COUNT(*)      AS PSE_EVENT_TYPE_CNT
              FROM COMP_EVENT
             GROUP 
                BY event_type_cd
            MINUS
            SELECT PK_IOPDDTOPSP_VALIDATION.FN_GET_PSE_EVENT_TYPE (
    		         event_type_cd) AS PSP_EVENT_TYPE,
                   COUNT(*)         AS PSP_EVENT_TYPE_CNT
              FROM PSP_COMPANY_EVENT@PSPMIGRATION.WORLD
             WHERE EVENT_TYPE_CD NOT IN (
                     PK_IOPDDTOPSP_CONST.gc_PSP_Event_ManualNote, 
                     PK_IOPDDTOPSP_CONST.gc_PSP_Event_FirstPayroll
                   )
             GROUP 
                BY event_type_cd
    	   );
		
    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  
  
  FUNCTION FN_COMPANY_PAYROLLS_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

    SELECT COUNT(*)
	  INTO v_temp_count
      FROM (
            SELECT b.SOURCE_COMPANY_ID,
            	   COUNT(*) AS PSE_PAYRUN_COUNT
              FROM PAYROLL_RUN a,
                   COMPANY     b	
             WHERE a.COMPANY_GSEQ = b.COMPANY_GSEQ
             GROUP
                BY b.SOURCE_COMPANY_ID
            MINUS	
            SELECT b.SOURCE_COMPANY_ID, 
                   COUNT(*) AS PSP_PAYRUN_COUNT
              FROM PSP_PAYROLL_RUN@PSPMIGRATION.WORLD a,
                   PSP_COMPANY@PSPMIGRATION.WORLD     b
             WHERE a.COMPANY_FK = b.COMPANY_SEQ
             GROUP 
                BY b.SOURCE_COMPANY_ID
    	   );
  
    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  
  
  FUNCTION FN_COMPANY_PAYCHECKS_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

    SELECT COUNT(*)
	  INTO v_temp_count
      FROM (
            SELECT b.SOURCE_COMPANY_ID,
                   a.SOURCE_PAY_RUN_ID,
            	   COUNT(*) AS PSE_CHECK_COUNT
              FROM PAYROLL_RUN a,
                   COMPANY     b,
            	   PAYCHECK    c	
             WHERE a.COMPANY_GSEQ     = b.COMPANY_GSEQ
               AND a.COMPANY_GSEQ     = c.COMPANY_GSEQ
               AND a.PAYROLL_RUN_GSEQ = c.PAYROLL_RUN_GSEQ
             GROUP
                BY b.SOURCE_COMPANY_ID,
            	   a.SOURCE_PAY_RUN_ID
            MINUS
            SELECT b.SOURCE_COMPANY_ID, 
                   a.SOURCE_PAY_RUN_ID,
                   COUNT(*) AS PSP_PAYCHECK_COUNT
              FROM PSP_PAYROLL_RUN@PSPMIGRATION.WORLD a,
                   PSP_COMPANY@PSPMIGRATION.WORLD     b,
            	   PSP_PAYCHECK@PSPMIGRATION.WORLD    c
             WHERE a.COMPANY_FK       = b.COMPANY_SEQ
               AND a.PAYROLL_RUN_SEQ  = c.PAYROLL_RUN_FK
             GROUP 
                BY b.SOURCE_COMPANY_ID,
            	   a.SOURCE_PAY_RUN_ID
    	   );
  
    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  
  
  FUNCTION FN_COMPANY_CONTACTS_MATCH 
	RETURN BOOLEAN
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_count                   NUMBER  := 0;
	
  BEGIN

    -- 10.15.2008 EMR
    --   omit bogus PSE company that will not be migrated.
    

    SELECT COUNT(*)
	  INTO v_temp_count
      FROM (
            SELECT b.SOURCE_COMPANY_ID,
            	   COUNT(*) AS PSE_CONTACT_COUNT
              FROM CONTACT  a,
                   COMPANY  b	
             WHERE a.COMPANY_GSEQ = b.COMPANY_GSEQ
               AND a.COMPANY_GSEQ <> PK_IOPDDTOPSP_CONST.gc_Bogus_Company_8350
             GROUP
                BY b.SOURCE_COMPANY_ID
            MINUS
            SELECT b.SOURCE_COMPANY_ID, 
                   COUNT(*) AS PSP_CONTACT_COUNT
              FROM PSP_CONTACT@PSPMIGRATION.WORLD a,
                   PSP_COMPANY@PSPMIGRATION.WORLD b
             WHERE a.COMPANY_FK = b.COMPANY_SEQ
             GROUP 
                BY b.SOURCE_COMPANY_ID
    	   );
  
    IF (v_temp_count <> 0) THEN
	  b_data_matches := FALSE;
	END IF;		   

    RETURN b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  
  
  -- ------------------------------------------------------------------------
  -- PRIVATE LOGGING UTILITIES
  -- ------------------------------------------------------------------------

  PROCEDURE PR_SET_COMP_VALIDATE_START (  
    p_PSE_COMPANY_DB_ID            IN  NUMBER,
	p_COMPANY_MIGRATION_GSEQ       OUT NUMBER
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION; 
    v_Legal_Name                   COMPANY.LEGAL_NAME%TYPE;
	
  BEGIN

    SELECT Legal_Name
      INTO v_Legal_Name
  	  FROM COMPANY
     WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID;
     
    INSERT INTO COMPANY_MIGRATION (
      COMPANY_MIGRATION_GSEQ, 
  	  MIGRATION_STATE_CD, 
  	  SOURCE_DB_COMPANY_ID, 
      COMPANY_LEGAL_NAME, 
  	  DD_SERVICE_CD, 
  	  MIGRATION_PHASE_ID, 
      MIGRATION_SCHEDULED_DATE 
    ) 
    VALUES (
      SEQ_COMPANY_MIGRATION.NEXTVAL,
      PK_IOPDDTOPSP_CONST.gc_Validating_StateCD,
      TO_CHAR(p_PSE_COMPANY_DB_ID),
      v_Legal_Name,
      PK_IOPDDTOPSP_CONST.gc_DD_SERVICE_IOP,
      '1',
  	  SYSDATE
    )
    RETURNING COMPANY_MIGRATION_GSEQ INTO p_COMPANY_MIGRATION_GSEQ;
	
	COMMIT;
    
  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  

  PROCEDURE PR_SET_COMP_VALIDATE_END (  
	p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,
	p_VALIDATION_END_STATE_CD      IN  VARCHAR2
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION; 
	
  BEGIN

    UPDATE COMPANY_MIGRATION
	   SET MIGRATION_STATE_CD     = p_VALIDATION_END_STATE_CD
	 WHERE Company_Migration_Gseq = p_COMPANY_MIGRATION_GSEQ;
    
	COMMIT;
    
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
    PRAGMA                         AUTONOMOUS_TRANSACTION; 

  BEGIN
  
    INSERT INTO MIGRATION_EVENT_LOG (
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
  -- PRIVATE COMPANY VALIDATION FUNCTIONS
  -- ------------------------------------------------------------------------
  
  PROCEDURE PR_VALIDATE_LEDGER (
    p_PSE_COMPANY_DB_ID            IN  NUMBER,  
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2,
	p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,
	p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
    
    v_psp_fcb_bal                  NUMBER;  -- fee cash balance sheet
    v_psp_fcr_bal                  NUMBER;  -- fee cash revenue
    v_psp_fi_bal                   NUMBER;  -- fee income
    
    v_pse_fr_bal                   NUMBER;  -- fee receivable
    v_pse_fc_bal                   NUMBER;  -- fee cash
    v_pse_fi_bal                   NUMBER;  -- fee income
	
    v_match_acct_count             NUMBER;

  BEGIN

    -- FIX  : add query from steve and remove these lines 
  
  
    -- INITIALIZE
    b_data_matches     := TRUE;
    v_temp_event_type  := pc_Event_Type_Info;
    v_event_cd         := 'CompLedgerCompare';    
	v_event_msg        := 'Data Matches';
    v_match_acct_count := 0;


	-- because there was a ledger account removed from PSP that
	-- exists in PSE, reverse the compare.  Thus compute PSP
	-- then identify the balances that don't match PSE.
	--
	-- Also the method for determining signs has changed so we
	-- decided to just compare absolute values.  This should 
	-- work fine because they balance the books in PSE everyday
	-- anyway.
    
    -- 07.10.2008 EMR 
    --   the ledger was changed by Erika and Tushar in regards to fees.
    --   the psp concept of fee cash and fee revenue have changed.
    --   all other ledger accounts match one for one.
	
	SELECT COUNT(*)
	  INTO v_match_acct_count
	  FROM (
            SELECT PK_IOPDDTOPSP_VALIDATION.FN_GET_PSE_LEDGER_ACCT (
			         LEDGER_ACCOUNT_FK) AS PSP_LEDGER_ACCT,
                   ABS(BALANCE_AMOUNT) AS PSP_LEDGER_NET_BAL
              FROM (
                    SELECT LEDGER_ACCOUNT_FK, 
                           BALANCE_DATE, 
                    	   BALANCE_AMOUNT,
                    	   ROW_NUMBER () OVER (
                    	     PARTITION BY LEDGER_ACCOUNT_FK
                    		 ORDER BY BALANCE_DATE DESC
                    	   ) RANK_BAL_BY_DT
                      FROM PSP_LEDGER_BALANCE@PSPMIGRATION.WORLD
                     WHERE COMPANY_FK = p_PSP_COMPANY_DB_ID
                       AND LEDGER_ACCOUNT_FK NOT IN (
                             'FeeCashBalanceSheet',
                             'FeeCashRevenue',
                             'FeeIncome'
                           )
                   )
             WHERE RANK_BAL_BY_DT = 1
			MINUS 
            SELECT LEDGER_ACCT_CD,
                   ABS ( SUM (
                     DECODE (
            	       CR_DB_IND,
            		   'C', LEDGER_AMT,
            		   'D', LEDGER_AMT * -1
            	     )
                   ) ) AS PSE_LEDGER_NET_BAL
              FROM LEDGER
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
               AND LEDGER_ACCT_CD NOT IN (
                     'FC',
                     'FR',
                     'FI'
                   )
             GROUP 
                BY LEDGER_ACCT_CD
		   ); 


    -- 07.11.2008 EMR
    --   oh, here is the story.  all ledgers accts but the fee accts are one
    --   to one.  the fee accounts have the following formula to compare.
    --   any questions about accounting see Tushar or Erika or read a book.
    
    -- PSE       PSP
    -- --------  ----------
    -- FI - FR = FCR; 
    -- FC      = FCR + FCB;
    -- FI - FR = FI

    -- PSE
    -- ---
    -- FC - credit * -1, debit *  1
    -- FR - credit * -1, debit *  1
    -- FI - credit *  1, deibt * -1

    -- PSP
    SELECT NVL(MAX(DECODE(PSP_LEDGER_ACCT, 'FeeCashBalanceSheet', PSP_LEDGER_NET_BAL)), 0) AS PSP_LA_FCB,
           NVL(MAX(DECODE(PSP_LEDGER_ACCT, 'FeeCashRevenue',      PSP_LEDGER_NET_BAL)), 0) AS PSP_LA_FCR,
           NVL(MAX(DECODE(PSP_LEDGER_ACCT, 'FeeIncome',           PSP_LEDGER_NET_BAL)), 0) AS PSP_LA_FI
      INTO v_psp_fcb_bal,
           v_psp_fcr_bal,
           v_psp_fi_bal
      FROM (
            SELECT LEDGER_ACCOUNT_FK  AS PSP_LEDGER_ACCT,
                   BALANCE_AMOUNT     AS PSP_LEDGER_NET_BAL
              FROM (
                    SELECT LEDGER_ACCOUNT_FK, 
                           BALANCE_DATE, 
                    	   BALANCE_AMOUNT,
                    	   ROW_NUMBER () OVER (
                    	     PARTITION BY LEDGER_ACCOUNT_FK
                    		 ORDER BY BALANCE_DATE DESC
                    	   ) RANK_BAL_BY_DT
                      FROM PSP_LEDGER_BALANCE@PSPMIGRATION.WORLD
                     WHERE COMPANY_FK = p_PSP_COMPANY_DB_ID
                       AND LEDGER_ACCOUNT_FK IN (
                             'FeeCashBalanceSheet',
                             'FeeCashRevenue',
                             'FeeIncome'
                           )
                   )
             WHERE RANK_BAL_BY_DT = 1
             );  

    -- PSE             
    SELECT NVL(MAX(DECODE(LEDGER_ACCT_CD, 'FR', PSE_LEDGER_NET_BAL)), 0) AS PSE_LA_FR,
           NVL(MAX(DECODE(LEDGER_ACCT_CD, 'FC', PSE_LEDGER_NET_BAL)), 0) AS PSE_LA_FC,
           NVL(MAX(DECODE(LEDGER_ACCT_CD, 'FI', PSE_LEDGER_NET_BAL)), 0) AS PSE_LA_FI
      INTO v_pse_fr_bal,
           v_pse_fc_bal,
           v_pse_fi_bal
      FROM (
            SELECT LEDGER_ACCT_CD,
                   SUM (
                     DECODE (
                       LEDGER_ACCT_CD,
                       'FC',
                       DECODE (
                         CR_DB_IND,
                         'C', LEDGER_AMT * -1,
                         'D', LEDGER_AMT *  1
                       ),
                       'FR',
                       DECODE (
                         CR_DB_IND,
                         'C', LEDGER_AMT * -1,
                         'D', LEDGER_AMT *  1
                       ),
                       'FI',
                       DECODE (
                         CR_DB_IND,
                         'C', LEDGER_AMT *  1,
                         'D', LEDGER_AMT * -1
                       )
                     )
                   )  AS PSE_LEDGER_NET_BAL
              FROM LEDGER
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
               AND LEDGER_ACCT_CD IN (
                     'FC',
                     'FR',
                     'FI'
                   )
             GROUP 
                BY LEDGER_ACCT_CD
            );

    IF (v_pse_fi_bal - v_pse_fr_bal = v_psp_fcr_bal)  AND
       (v_pse_fc_bal = v_psp_fcr_bal + v_psp_fcb_bal) AND
       (v_pse_fi_bal - v_pse_fr_bal = v_psp_fi_bal  ) AND
       (v_match_acct_count = 0)                       THEN
    
      -- although redundant, it reiterates the fact that all ledger accounts
      -- match, which is a good thing.   
      v_match_acct_count := 0;
       
    ELSE
    
      -- this just indicates that the fee accounts might be a problem.
      -- good for debugging.
      v_match_acct_count := v_match_acct_count + 3;
      
    END IF;  

		   
    IF (v_match_acct_count <> 0) THEN
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Data does not match. PSE COMP ID: ' || 
	                       p_PSE_COMPANY_DB_ID                  ||
						   ' PSP COMP ID: '                     ||
						   p_PSP_COMPANY_DB_ID                  ||
						   ', for the following data: '         ||
						   'company ledger. ('                  ||
						   v_match_acct_count                   ||
						   ').';
	END IF;	   
	

    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);
	
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;

  
  PROCEDURE PR_VALIDATE_COMP_BANK_ACCT (
    p_PSE_COMPANY_DB_ID            IN  NUMBER,  
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2,
	p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,	
	p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;

    v_match_acct_count            NUMBER;
	
  BEGIN

    -- FIX  : account num encrypted
    -- FIXED: acct status cd, account type	
  
  
    -- initialize
    b_data_matches     := TRUE;
    v_temp_event_type  := pc_Event_Type_Info;
    v_event_cd         := 'CompBankAcctCompare';    
	v_event_msg        := 'Data Matches';
    v_match_acct_count := 0;


    SELECT COUNT(*)
	  INTO v_match_acct_count
      FROM (
            SELECT a.SOURCE_BANK_ACCT_ID, 
            	   a.STATUS_CD, 
            	   a.VERIFY_RETRY_CNT, 
            	   a.TOT_RETRY_CNT, 
                   b.ACCT_TYPE_CD, 
                   b.BANK_NAME, 
                   b.ROUTING_NUM, 
            	   b.ACCT_NUM	    
              FROM COMP_BANK_ACCT_ASSOC a,
               	   BANK_ACCT            b
             WHERE a.BANK_ACCT_GSEQ = b.BANK_ACCT_GSEQ
               AND a.COMPANY_GSEQ   = p_PSE_COMPANY_DB_ID
            MINUS
            SELECT a.SOURCE_BANK_ACCOUNT_ID,
                   DECODE (
            	     a.STATUS_CD,
            		 'Active',              'ACTV',
            		 'PendingVerification', 'PNDVER',
            		 'Inactive',            'INACTV'
            	   ),
                   a.VERIFY_RETRY_COUNT, 
            	   a.TOTAL_RETRY_COUNT, 
                   DECODE (
            	     b.ACCOUNT_TYPE_CD,
            		 'Checking', 'C',
            		 'Savings',  'S'
            	   ),
            	   b.BANK_NAME,	   
            	   b.ROUTING_NUMBER,
                   b.ACCOUNT_NUMBER	   	    
              FROM PSP_COMPANY_BANK_ACCOUNT@PSPMIGRATION.WORLD a,	   
                   PSP_BANK_ACCOUNT@PSPMIGRATION.WORLD	       b
             WHERE a.BANK_ACCOUNT_FK = b.BANK_ACCOUNT_SEQ
               AND a.COMPANY_FK      = p_PSP_COMPANY_DB_ID
           );

	IF (v_match_acct_count > 0) THEN
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Data does not match. PSE COMP ID: ' || 
	                       p_PSE_COMPANY_DB_ID                  ||
						   ' PSP COMP ID: '                     ||
						   p_PSP_COMPANY_DB_ID                  ||
						   ', for the following data: '         ||
						   'company bank account.';
	END IF;
	
	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);
	
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;

  
  PROCEDURE PR_VALIDATE_COMPANY_INFO (
    p_PSE_COMPANY_DB_ID            IN  NUMBER,  
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2,
	p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,	
	p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_match_count                  NUMBER;
	rt_pse_company                 COMPANY%ROWTYPE;
	rt_psp_company                 PSP_COMPANY@PSPMIGRATION.WORLD%ROWTYPE;
	
  BEGIN

	-- FIX  : fein encrypted
	
	
    -- initialize to happy path
	b_data_matches    := TRUE;
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'CompInfoCompare';    
	v_event_msg       := 'Data matches';
	v_match_count     := 0;
	
	
	SELECT COUNT(*)
	  INTO v_match_count
      FROM (
            SELECT FED_TAX_ID,
            	   LEGAL_NAME, 
                   DBA_NAME, 
                   SOURCE_COMPANY_ID, 
                   SOURCE_SYSTEM_CD 
              FROM COMPANY
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
            MINUS
            SELECT FED_TAX_ID,
            	   LEGAL_NAME, 
                   DBA_NAME, 
                   SOURCE_COMPANY_ID, 
            	   SOURCE_SYSTEM_CD
              FROM PSP_COMPANY@PSPMIGRATION.WORLD
             WHERE COMPANY_SEQ = p_PSP_COMPANY_DB_ID
	       );

    -- if the two companies match then minus should yield a result
	-- of 0 meaning there are no records that don't match. Any
	-- other results mean one of the fields do not match.
	
    IF (v_match_count <> 0) THEN

	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Data does not match. PSE COMP ID: ' || 
	                       p_PSE_COMPANY_DB_ID                  ||
						   ' PSP COMP ID: '                     ||
						   p_PSP_COMPANY_DB_ID                  ||
						   ', for the following fields: ';

      -- hope they match and they should but if they don't
	  -- then do a deeper dive into which fields exactly don't
	  -- match.  more painful to do but better for debugging.
	  						   
	  SELECT * INTO rt_pse_company FROM COMPANY
       WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID;
	  
	  SELECT * INTO rt_psp_company FROM PSP_COMPANY@PSPMIGRATION.WORLD
       WHERE COMPANY_SEQ = p_PSP_COMPANY_DB_ID;
	   
	  IF (rt_pse_company.fed_tax_id <> rt_psp_company.fed_tax_id) THEN
	    v_event_msg := v_event_msg || 'fed tax id, ';
	  END IF;

	  IF (rt_pse_company.legal_name <> rt_psp_company.legal_name) THEN
	    v_event_msg := v_event_msg || 'legal name, ';
	  END IF;
	  
	  IF (rt_pse_company.dba_name <> rt_psp_company.dba_name) THEN
	    v_event_msg := v_event_msg || 'dba name, ';
	  END IF;
	  
	  IF (rt_pse_company.source_company_id <> rt_psp_company.source_company_id) THEN
	    v_event_msg := v_event_msg || 'source company id, ';
	  END IF;
	  
	  IF (rt_pse_company.source_system_cd <> rt_psp_company.source_system_cd) THEN
	    v_event_msg := v_event_msg || 'source system cd.';
	  END IF;
	  
	END IF;

	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);
	
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;

  
  PROCEDURE PR_VALIDATE_DD_INFO (
    p_PSE_COMPANY_DB_ID            IN  NUMBER,  
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2,
	p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,	
	p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_temp_mismatch_msg            MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;

    v_match_ddsvc_count            NUMBER;
    v_match_ee_count               NUMBER;
    v_match_token_count            NUMBER;
    v_match_payrun_count           NUMBER;
    v_match_paychk_count           NUMBER;
    v_match_txn_count              NUMBER;						
	
  BEGIN

	-- FIX  : dd status code
	-- FIXED: payroll status, tnx type, txn state	


    -- initialize
	b_data_matches       := TRUE;
    v_temp_event_type    := pc_Event_Type_Info;
    v_event_cd           := 'CompDDInfoCompare';    
	v_event_msg          := 'Data Matches';
    v_temp_mismatch_msg  := '';	

    v_match_ddsvc_count  := 0;
    v_match_ee_count     := 0;
    v_match_token_count  := 0;
    v_match_payrun_count := 0;
    v_match_paychk_count := 0;
    v_match_txn_count    := 0;						
	
   
    -- dd service
    
    -- 09.17.2008 EMR
    -- the function for pse dd status was modified.  Also the hold status
    -- has changed drastically in psp so the compare was modified for this
    -- status.  No on hold reasons in psp means it is not on hold.
    
	SELECT COUNT(*)
	  INTO v_match_ddsvc_count
	  FROM (   
            SELECT a.FUNDING_MODEL_CD                AS FUNDING_MODEL,
            	   NVL(a.OVERRIDE_COMP_LIMIT_AMT, 0) AS ER_LIMIT, 
                   NVL(a.OVERRIDE_EMP_LIMIT_AMT,  0) AS EE_LIMIT,
            	   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_DD_STATUS (
                     p_PSE_COMPANY_DB_ID)            AS SERVICE_STATUS,
                   DECODE (
                     a.DD_COMP_STATUS_CD, 
                     PK_IOPDDTOPSP_CONST.gc_PSE_Status_Hold,      1,
                     PK_IOPDDTOPSP_CONST.gc_PSE_Status_Suspend,   1,
                     PK_IOPDDTOPSP_CONST.gc_PSE_Status_PendgTerm, 1, 
                     0
                   )                                 AS PSE_ONHOLD_STATUS
              FROM DD_COMPANY_INFO a
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
            MINUS
            SELECT a.FUNDING_MODEL_FK                       AS FUNDING_MODEL,
                   NVL(c.OVERRIDE_COMPANY_LIMIT_AMOUNT,  0) AS ER_LIMIT, 
                   NVL(c.OVERRIDE_EMPLOYEE_LIMIT_AMOUNT, 0) AS EE_LIMIT,
                   b.STATUS_CD                              AS SERVICE_STATUS,
                   DECODE (
                     PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_ONHOLD_COUNT(p_PSP_COMPANY_DB_ID),
                     0, 0,
                     1
                   )                                        AS PSP_ONHOLD_STATUS 
              FROM PSP_COMPANY@PSPMIGRATION.WORLD                 a,
                   PSP_COMPANY_SERVICE@PSPMIGRATION.WORLD         b,
                   PSP_DDCOMPANY_SERVICE_INFO@PSPMIGRATION.WORLD  c
             WHERE a.COMPANY_SEQ         = b.COMPANY_FK
               AND b.COMPANY_SERVICE_SEQ = c.DDCOMPANY_SERVICE_INFO_SEQ 
               AND a.COMPANY_SEQ         = p_PSP_COMPANY_DB_ID
	       );
   
    -- employees   
    SELECT (
            SELECT COUNT(*)
              FROM EMPLOYEE
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
    	   ) - 
    	   (
    	    SELECT COUNT(*) 
              FROM PSP_EMPLOYEE@PSPMIGRATION.WORLD
             WHERE COMPANY_FK = p_PSP_COMPANY_DB_ID
    	   ) AS EE_COMPARE_CNT
	  INTO v_match_ee_count
      FROM DUAL;
   
    -- token
    SELECT COUNT(*)
	  INTO v_match_token_count
      FROM (
            SELECT TXN_TOKEN_NBR, 
                   SOURCE_REQUEST_ID 
              FROM TXN_RESPONSE
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
            MINUS
            SELECT TRANSACTION_TOKEN_NUMBER,
                   SOURCE_REQUEST_ID 
              FROM PSP_TRANSACTION_RESPONSE@PSPMIGRATION.WORLD
             WHERE COMPANY_FK = p_PSP_COMPANY_DB_ID
           );

    -- payrolls
	-- this includes payruns, paychecks and transactions
	
	-- payroll run date uses UTC time in psp.  thus it needs
	-- to be converted to daylight savings first, then redone
	-- with the utc offset.  if it sounds confusing just look
	-- at the code below it's pretty straight forward.
	
	-- NEW_TIME GMT to PST does not accomodate daylight savings time.
	-- so an hour needs to be added. 06.09.2008
	
	-- UC 66 changed payroll status code mapping. 06.10.2008
	
    SELECT COUNT(*)
	  INTO v_match_payrun_count 
      FROM (
            SELECT SOURCE_PAY_RUN_ID, 
                   PAYROLL_RUN_DATE,
                   TRUNC(PAYCHECK_DEPOSIT_DATE),
                   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_PAYROLL_STATUS (
			         PAYROLL_STATUS_CD) AS PSP_PAYROLL_RUN_STATUS,				   	   
                   PAYROLL_NET_AMT
              FROM PAYROLL_RUN
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
            MINUS	   
            SELECT SOURCE_PAY_RUN_ID,
			       (
				    CASE
					  WHEN PAYROLL_RUN_DATE < TO_DATE ('11042007 0300','MMDDYYYY HH24MI')
					  THEN NEW_TIME(
                        PAYROLL_RUN_DATE, 
                        PK_IOPDDTOPSP_CONST.gc_GMT_timezone, 
                        PK_IOPDDTOPSP_CONST.gc_PST_timezone) + (1/24)
					  WHEN PAYROLL_RUN_DATE BETWEEN TO_DATE ('11042007 0300','MMDDYYYY HH24MI') AND
					                                TO_DATE ('03092008 0400','MMDDYYYY HH24MI')
					  THEN NEW_TIME(
                        PAYROLL_RUN_DATE, 
                        PK_IOPDDTOPSP_CONST.gc_GMT_timezone, 
                        PK_IOPDDTOPSP_CONST.gc_PST_timezone)
					  WHEN PAYROLL_RUN_DATE BETWEEN TO_DATE ('03092008 0400','MMDDYYYY HH24MI') AND
					                                TO_DATE ('11022008 0300','MMDDYYYY HH24MI')
					  THEN NEW_TIME(
                        PAYROLL_RUN_DATE, 
                        PK_IOPDDTOPSP_CONST.gc_GMT_timezone, 
                        PK_IOPDDTOPSP_CONST.gc_PST_timezone) + (1/24)
					  WHEN PAYROLL_RUN_DATE BETWEEN TO_DATE ('11022008 0300','MMDDYYYY HH24MI') AND
					                                TO_DATE ('03092009 0400','MMDDYYYY HH24MI')
					  THEN NEW_TIME(
                        PAYROLL_RUN_DATE, 
                        PK_IOPDDTOPSP_CONST.gc_GMT_timezone, 
                        PK_IOPDDTOPSP_CONST.gc_PST_timezone)
				    END
				   ) AS PSP_PAYROLL_RUN_DATE,     
            	   TRUNC(PAYCHECK_DATE),
                   PAYROLL_RUN_STATUS,  
                   PAYROLL_NET_AMOUNT
              FROM PSP_PAYROLL_RUN@PSPMIGRATION.WORLD
             WHERE COMPANY_FK = p_PSP_COMPANY_DB_ID
    	   );

	   	   
    SELECT (
            SELECT COUNT(*)
              FROM PAYCHECK
             WHERE COMPANY_GSEQ = p_PSE_COMPANY_DB_ID
    	   ) -
    	   (
    	    SELECT COUNT(*)
              FROM PSP_PAYCHECK@PSPMIGRATION.WORLD    a,
                   PSP_PAYROLL_RUN@PSPMIGRATION.WORLD b
             WHERE a.PAYROLL_RUN_FK = b.PAYROLL_RUN_SEQ 
               AND b.COMPANY_FK     = p_PSP_COMPANY_DB_ID
    	   ) AS CHECK_COMPARE_CNT
	  INTO v_match_paychk_count
      FROM DUAL;
   

    SELECT COUNT(*)
	  INTO v_match_txn_count
      FROM (
            SELECT b.SOURCE_PAY_RUN_ID,
                   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSP_TXN_TYPE (
                     a.TXN_TYPE_CD),
                   a.SETTLEMENT_TYPE_CD,	   
            	   a.CURRENT_TXN_STATE_CD,
                   TRUNC(a.SETTLEMENT_DATE) 
              FROM FINANCIAL_TXN a,
                   PAYROLL_RUN   b	
             WHERE a.PAYROLL_RUN_GSEQ = b.PAYROLL_RUN_GSEQ
               AND a.COMPANY_GSEQ     = b.COMPANY_GSEQ 
               AND a.COMPANY_GSEQ     = p_PSE_COMPANY_DB_ID   
            MINUS
            SELECT b.SOURCE_PAY_RUN_ID,
                   a.TRANSACTION_TYPE_FK,
                   UPPER (
                     DECODE (
                       a.SETTLEMENT_TYPE_CD,
                       PK_IOPDDTOPSP_CONST.gc_PSP_Settlement_Check, PK_IOPDDTOPSP_CONST.gc_PSE_Settlement_Check,
                       a.SETTLEMENT_TYPE_CD
                     )
                   ),	   
            	   PK_IOPDDTOPSP_VALIDATION.FN_GET_PSE_TXN_STATE (
				     a.CURRENT_TRANSACTION_STATE_FK),
                   TRUNC(a.SETTLEMENT_DATE) 
              FROM PSP_FINANCIAL_TRANSACTION@PSPMIGRATION.WORLD a,
                   PSP_PAYROLL_RUN@PSPMIGRATION.WORLD	        b	
             WHERE a.PAYROLL_RUN_FK = b.PAYROLL_RUN_SEQ
               AND a.COMPANY_FK     = b.COMPANY_FK 
               AND a.COMPANY_FK     = p_PSP_COMPANY_DB_ID
    	   );


    -- DESIGN NOTE
	--   all the counts above should equal zero.  if any don't
	--   then log it as an error.
	
    IF (v_match_ddsvc_count  > 0 ) THEN
	  b_data_matches      := FALSE;
      v_temp_mismatch_msg := 'DD Service Info, ';	  
	END IF;
	
    IF (v_match_ee_count > 0) THEN
	  b_data_matches      := FALSE;
      v_temp_mismatch_msg := v_temp_mismatch_msg || 'Employee count, ';	  
	END IF;
	
    IF (v_match_token_count > 0) THEN
	  b_data_matches      := FALSE;
      v_temp_mismatch_msg := v_temp_mismatch_msg || 'Token number, ';	  
	END IF;
	
    IF (v_match_payrun_count > 0) THEN
	  b_data_matches      := FALSE;
      v_temp_mismatch_msg := v_temp_mismatch_msg || 'Payroll Run, ';	  
	END IF;
	
    IF (v_match_paychk_count > 0) THEN
	  b_data_matches      := FALSE;
      v_temp_mismatch_msg := v_temp_mismatch_msg || 'Paycheck count, ';	  
	END IF;
	
    IF (v_match_txn_count > 0) THEN
	  b_data_matches      := FALSE;
      v_temp_mismatch_msg := v_temp_mismatch_msg || 'Financial Transactions.';	  
	END IF;						

	-- reformat the logging message
    IF (NOT b_data_matches) THEN
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Data does not match. PSE COMP ID: ' || 
	                       p_PSE_COMPANY_DB_ID                  ||
						   ' PSP COMP ID: '                     ||
						   p_PSP_COMPANY_DB_ID                  ||
						   ', for the following data: '         ||
						   v_temp_mismatch_msg;
	END IF;	 

	
    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);
	
    p_DATA_MATCHES_IND := b_data_matches;

  EXCEPTION
    WHEN OTHERS THEN 
	  RAISE;
  END;
  
  
  PROCEDURE PR_VALIDATE_EMPLOYEES (
    p_PSE_COMPANY_DB_ID            IN  NUMBER,  
    p_PSP_COMPANY_DB_ID            IN  VARCHAR2,
	p_COMPANY_MIGRATION_GSEQ       IN  NUMBER,	
	p_DATA_MATCHES_IND             OUT BOOLEAN
  )
  IS
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	v_temp_mismatch_msg            MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	
	rt_emp_mismatch_list           cur_employee_mismatch%ROWTYPE;
	
  BEGIN

    -- initialize
  	b_data_matches    := TRUE;
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'CompEmployeesCompare';    
	v_event_msg       := 'Data Matches';
    v_temp_mismatch_msg  := '';	
	  
    IF (cur_employee_mismatch%ISOPEN) THEN
	  CLOSE cur_employee_mismatch;
	END IF;
	
    OPEN cur_employee_mismatch (
      p_PSE_COMPANY_DB_ID,  
      p_PSP_COMPANY_DB_ID
	);
	  
    FETCH cur_employee_mismatch
	 INTO rt_emp_mismatch_list;

	-- return the company ready to migrate
    IF (cur_employee_mismatch%FOUND) THEN
	
	  FOR i IN 1..cur_employee_mismatch%ROWCOUNT
	  LOOP
	    v_temp_mismatch_msg := v_temp_mismatch_msg            ||
		                       rt_emp_mismatch_list.EE_SRC_ID ||
							   ', ';
	  END LOOP;
	
  	  b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;
	  v_event_msg       := 'Data does not match. PSE COMP ID: ' || 
	                       p_PSE_COMPANY_DB_ID                  ||
						   ' PSP COMP ID: '                     ||
						   p_PSP_COMPANY_DB_ID                  ||
						   ', for the following '               ||
						   cur_employee_mismatch%ROWCOUNT       ||
						   ' employees (source ids): '          ||
						   v_temp_mismatch_msg;
	
	END IF;
	
    CLOSE cur_employee_mismatch;

    PR_SET_COMP_MIGRATION_EVENT (
	  p_COMPANY_MIGRATION_GSEQ,
	  v_temp_event_type, 
      v_event_cd,
	  v_event_msg
	);
	
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
    b_data_matches                 BOOLEAN := TRUE;
	v_temp_event_type              MIGRATION_EVENT_LOG.EVENT_LOG_TYPE_CD%TYPE;	
	v_event_cd                     MIGRATION_EVENT_LOG.EVENT_PARAM_CD%TYPE;
	v_event_msg                    MIGRATION_EVENT_LOG.EVENT_PARAM_VALUE%TYPE;
	
    v_PSE_ER_TOTAL                 NUMBER;
    v_PSE_ER_BA_TOTAL              NUMBER;
    v_PSE_EE_TOTAL                 NUMBER;
    v_PSE_EE_BA_TOTAL              NUMBER;
    v_PSE_PAYRUN_TOTAL             NUMBER;
    v_PSE_PAYCHECK_TOTAL           NUMBER;
    v_PSE_TXN_TOTAL                NUMBER;
    v_PSE_TXN_STATE_TOTAL          NUMBER;
    v_PSE_OFFLOAD_TOTAL            NUMBER;
    v_PSE_TXN_OFFLOAD_TOTAL        NUMBER;
    v_PSE_TXN_RESP_TOTAL           NUMBER;
    v_PSE_RTN_BATCH_TOTAL          NUMBER;
    v_PSE_RTN_TXN_TOTAL            NUMBER;
    v_PSE_COMP_NOTE_TOTAL          NUMBER;
    v_PSE_COMP_EVENT_TOTAL         NUMBER;
    v_PSE_ADDRESS_TOTAL            NUMBER;
    v_PSE_INDIVIDUAL_TOTAL         NUMBER;
    v_PSE_BANKACCT_TOTAL           NUMBER;
    
    v_PSP_ER_TOTAL                 NUMBER;
    v_PSP_ER_BA_TOTAL              NUMBER;
    v_PSP_EE_TOTAL                 NUMBER;
    v_PSP_EE_BA_TOTAL              NUMBER;
    v_PSP_PAYRUN_TOTAL             NUMBER;
    v_PSP_PAYCHECK_TOTAL           NUMBER;
    v_PSP_TXN_TOTAL                NUMBER;
    v_PSP_TXN_STATE_TOTAL          NUMBER;
    v_PSP_OFFLOAD_TOTAL            NUMBER;
    v_PSP_TXN_OFFLOAD_TOTAL        NUMBER;
    v_PSP_TXN_RESP_TOTAL           NUMBER;
    v_PSP_RTN_BATCH_TOTAL          NUMBER;
    v_PSP_RTN_TXN_TOTAL            NUMBER;
    v_PSP_COMP_NOTE_TOTAL          NUMBER;
    v_PSP_COMP_EVENT_TOTAL         NUMBER;
    v_PSP_ADDRESS_TOTAL            NUMBER;
    v_PSP_INDIVIDUAL_TOTAL         NUMBER;
    v_PSP_BANKACCT_TOTAL	       NUMBER;
	
  BEGIN

    -- FIX  : 	
	-- FIXED: payroll status cd, txn type cd, txn state cd,
    --        audit grouping, events grouping
	
	-- NOTE: remember to turn off audit triggers in PSP
	
  
    -- initialize to happy path
	b_data_matches    := TRUE;
    v_temp_event_type := pc_Event_Type_Info;
    v_event_cd        := 'FinalGlobalCompare';    
	v_event_msg       := 'Data Matches';

	
	--
	-- SIMPLE COUNT COMPARES
	--
	
	-- sorry for the long query but it is sometimes better to 
	-- throw everything at once at the great Oracle
    
    -- 09.29.2008 EMR
    --   modified event compare to factor out new PSP events generated during
    --   migration that are not in PSE.
    
    -- 10.10.2008 EMR
    --   modified count company to omit the one production company that created
    --   a bogus record due to a toplink issue in July 2008. 
	
    SELECT (
            SELECT COUNT(*)
    		  FROM COMPANY
             WHERE COMPANY_GSEQ <> PK_IOPDDTOPSP_CONST.gc_Bogus_Company_8350
           ) AS PSE_ER_TOTAL,
           (
     	    SELECT COUNT(*)
       		  FROM COMP_BANK_ACCT_ASSOC
       	   ) AS PSE_ER_BA_TOTAL,
       	   (
       	    SELECT COUNT(*)
       	 	  FROM EMPLOYEE
       	   ) AS PSE_EE_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM EMP_BANK_ACCT_ASSOC
       	   ) AS PSE_EE_BA_TOTAL,
       	   (
       	    SELECT COUNT(*) 
       		  FROM PAYROLL_RUN
       	   ) AS PSE_PAYRUN_TOTAL,
       	   (
       	    SELECT COUNT(*) 
       		  FROM PAYCHECK
       	   ) AS PSE_PAYCHECK_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM FINANCIAL_TXN
       	   ) AS PSE_TXN_TOTAL,
           (
       	    SELECT COUNT(*)
       		  FROM FINANCIAL_TXN_STATE_ASSOC
       	   ) AS PSE_TXN_STATE_TOTAL,	   
       	   (
       	    SELECT COUNT(*) 
       	      FROM OFFLOAD_BATCH
       	   ) AS PSE_OFFLOAD_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM TXN_OFFLOAD_BATCH_ASSOC
       	   ) AS PSE_TXN_OFFLOAD_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM TXN_RESPONSE
       	   ) AS PSE_TXN_RESP_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM RETURN_BATCH
       	   ) AS PSE_RTN_BATCH_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM TXN_RETURN
       	   ) AS PSE_RTN_TXN_TOTAL,
           (
       	    SELECT COUNT(*)
       		  FROM COMP_NOTE
       	   ) AS PSE_COMP_NOTE_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM COMP_EVENT
       	   ) AS PSE_COMP_EVENT_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM ADDRESS
       	   ) AS PSE_ADDRESS_TOTAL,	   
       	   (
       	    SELECT COUNT(*)
       		  FROM INDIVIDUAL
       	   ) AS PSE_INDIVIDUAL_TOTAL,	   
       	   (
       	    SELECT COUNT(*)
       		  FROM BANK_ACCT
       	   ) AS PSE_BANKACCT_TOTAL,	   
           (
            SELECT COUNT(*)
       		  FROM PSP_COMPANY@PSPMIGRATION.WORLD
       	   ) AS PSP_ER_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_COMPANY_BANK_ACCOUNT@PSPMIGRATION.WORLD
       	   ) AS PSP_ER_BA_TOTAL,
       	   (
       	    SELECT COUNT(*)
       	      FROM PSP_EMPLOYEE@PSPMIGRATION.WORLD
       	   ) AS PSP_EE_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_EMPLOYEE_BANK_ACCOUNT@PSPMIGRATION.WORLD
       	   ) AS PSP_EE_BA_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_PAYROLL_RUN@PSPMIGRATION.WORLD
       	   ) AS PSP_PAYRUN_TOTAL,
       	   (
       	    SELECT COUNT(*)
       	      FROM PSP_PAYCHECK@PSPMIGRATION.WORLD
       	   ) AS PSP_PAYCHECK_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_FINANCIAL_TRANSACTION@PSPMIGRATION.WORLD
       	   ) AS PSP_TXN_TOTAL,
           (
       	    SELECT COUNT(*)
       		   FROM PSP_FINANCIAL_TRANS_STATE@PSPMIGRATION.WORLD
       	   ) AS PSP_TXN_STATE_TOTAL,	   
       	   (
       	    SELECT COUNT(*) 
       		  FROM PSP_OFFLOAD_BATCH@PSPMIGRATION.WORLD
       	   ) AS PSP_OFFLOAD_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_TRANSACTION_OFFLOAD_BATCH@PSPMIGRATION.WORLD
       	   ) AS PSP_TXN_OFFLOAD_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_TRANSACTION_RESPONSE@PSPMIGRATION.WORLD
       	   ) AS PSP_TXN_RESP_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_TRANSACTION_RETURN_BATCH@PSPMIGRATION.WORLD
       	   ) AS PSP_RTN_BATCH_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_TRANSACTION_RETURN@PSPMIGRATION.WORLD
       	   ) AS PSP_RTN_TXN_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_COMPANY_NOTE@PSPMIGRATION.WORLD
       	   ) AS PSP_COMP_NOTE_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_COMPANY_EVENT@PSPMIGRATION.WORLD
              WHERE EVENT_TYPE_CD NOT IN (
                      PK_IOPDDTOPSP_CONST.gc_PSP_Event_ManualNote, 
                      PK_IOPDDTOPSP_CONST.gc_PSP_Event_FirstPayroll
                    )
       	   ) AS PSP_COMP_EVENT_TOTAL,
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_ADDRESS@PSPMIGRATION.WORLD
       	   ) AS PSP_ADDRESS_TOTAL,	   
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_INDIVIDUAL@PSPMIGRATION.WORLD
       	   ) AS PSP_INDIVIDUAL_TOTAL,	   
       	   (
       	    SELECT COUNT(*)
       		  FROM PSP_BANK_ACCOUNT@PSPMIGRATION.WORLD
       	   ) AS PSP_BANKACCT_TOTAL
	  INTO
           v_PSE_ER_TOTAL,           -- PSE
           v_PSE_ER_BA_TOTAL,
           v_PSE_EE_TOTAL,
           v_PSE_EE_BA_TOTAL,
           v_PSE_PAYRUN_TOTAL,
           v_PSE_PAYCHECK_TOTAL,
           v_PSE_TXN_TOTAL,
           v_PSE_TXN_STATE_TOTAL,	   
           v_PSE_OFFLOAD_TOTAL,
           v_PSE_TXN_OFFLOAD_TOTAL,
           v_PSE_TXN_RESP_TOTAL,
           v_PSE_RTN_BATCH_TOTAL,
           v_PSE_RTN_TXN_TOTAL,
           v_PSE_COMP_NOTE_TOTAL,
           v_PSE_COMP_EVENT_TOTAL,
           v_PSE_ADDRESS_TOTAL,	   
           v_PSE_INDIVIDUAL_TOTAL,	   
           v_PSE_BANKACCT_TOTAL,	   
           v_PSP_ER_TOTAL,           -- PSP
           v_PSP_ER_BA_TOTAL,
           v_PSP_EE_TOTAL,
           v_PSP_EE_BA_TOTAL,
           v_PSP_PAYRUN_TOTAL,
           v_PSP_PAYCHECK_TOTAL,
           v_PSP_TXN_TOTAL,
           v_PSP_TXN_STATE_TOTAL,	   
           v_PSP_OFFLOAD_TOTAL,
           v_PSP_TXN_OFFLOAD_TOTAL,
           v_PSP_TXN_RESP_TOTAL,
           v_PSP_RTN_BATCH_TOTAL,
           v_PSP_RTN_TXN_TOTAL,
           v_PSP_COMP_NOTE_TOTAL,
           v_PSP_COMP_EVENT_TOTAL,
           v_PSP_ADDRESS_TOTAL,	   
           v_PSP_INDIVIDUAL_TOTAL,	   
           v_PSP_BANKACCT_TOTAL	   
      FROM DUAL;


	-- once all the totals have been returned then
	-- compare and log either pass or fail for each
	   
    IF (v_PSE_ER_TOTAL = v_PSP_ER_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company totals match. Total = ' || v_PSP_ER_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company totals DO NOT match. PSE = ' ||
	                       v_PSE_ER_TOTAL || ', PSP = ' || v_PSP_ER_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);
	
	
    IF (v_PSE_ER_BA_TOTAL = v_PSP_ER_BA_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company bank account totals match. Total = ' || v_PSP_ER_BA_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company bank account totals DO NOT match. PSE = ' ||
	                       v_PSE_ER_BA_TOTAL || ', PSP = ' || v_PSP_ER_BA_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);
	
	
    IF (v_PSE_EE_TOTAL = v_PSP_EE_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Employee totals match. Total = ' || v_PSP_EE_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Employee totals DO NOT match. PSE = ' ||
	                       v_PSE_EE_TOTAL || ', PSP = ' || v_PSP_EE_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

	
    IF (v_PSE_EE_BA_TOTAL = v_PSP_EE_BA_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Employee bank account totals match. Total = ' || v_PSP_EE_BA_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Employee bank account totals DO NOT match. PSE = ' ||
	                       v_PSE_EE_BA_TOTAL || ', PSP = ' || v_PSP_EE_BA_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

	
    IF (v_PSE_PAYRUN_TOTAL = v_PSP_PAYRUN_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Payroll totals match. Total = ' || v_PSP_PAYRUN_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Payroll totals DO NOT match. PSE = ' ||
	                       v_PSE_PAYRUN_TOTAL || ', PSP = ' || v_PSP_PAYRUN_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_PAYCHECK_TOTAL = v_PSP_PAYCHECK_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Paycheck totals match. Total = ' || v_PSP_PAYCHECK_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Paycheck totals DO NOT match. PSE = ' ||
	                       v_PSE_PAYCHECK_TOTAL || ', PSP = ' || v_PSP_PAYCHECK_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_TXN_TOTAL = v_PSP_TXN_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Transaction totals match. Total = ' || v_PSP_TXN_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Transaction totals DO NOT match. PSE = ' ||
	                       v_PSE_TXN_TOTAL || ', PSP = ' || v_PSP_TXN_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_TXN_STATE_TOTAL = v_PSP_TXN_STATE_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Transaction state totals match. Total = ' || v_PSP_TXN_STATE_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Transaction state totals DO NOT match. PSE = ' ||
	                       v_PSE_TXN_STATE_TOTAL || ', PSP = ' || v_PSP_TXN_STATE_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_OFFLOAD_TOTAL = v_PSP_OFFLOAD_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Batch offload totals match. Total = ' || v_PSP_OFFLOAD_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Batch offload totals DO NOT match. PSE = ' ||
	                       v_PSE_OFFLOAD_TOTAL || ', PSP = ' || v_PSP_OFFLOAD_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_TXN_OFFLOAD_TOTAL = v_PSP_TXN_OFFLOAD_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Batch offload txn totals match. Total = ' || v_PSP_TXN_OFFLOAD_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Batch offload txn totals DO NOT match. PSE = ' ||
	                       v_PSE_TXN_OFFLOAD_TOTAL || ', PSP = ' || v_PSP_TXN_OFFLOAD_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_TXN_RESP_TOTAL = v_PSP_TXN_RESP_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Txn response totals match. Total = ' || v_PSP_TXN_RESP_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Txn response totals DO NOT match. PSE = ' ||
	                       v_PSE_TXN_RESP_TOTAL || ', PSP = ' || v_PSP_TXN_RESP_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_RTN_BATCH_TOTAL = v_PSP_RTN_BATCH_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Batch return totals match. Total = ' || v_PSP_RTN_BATCH_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Batch return totals DO NOT match. PSE = ' ||
	                       v_PSE_RTN_BATCH_TOTAL || ', PSP = ' || v_PSP_RTN_BATCH_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_RTN_TXN_TOTAL = v_PSP_RTN_TXN_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Batch return txn totals match. Total = ' || v_PSP_RTN_TXN_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Batch return txn totals DO NOT match. PSE = ' ||
	                       v_PSE_RTN_TXN_TOTAL || ', PSP = ' || v_PSP_RTN_TXN_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_COMP_NOTE_TOTAL = v_PSP_COMP_NOTE_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company note totals match. Total = ' || v_PSP_COMP_NOTE_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company note totals DO NOT match. PSE = ' ||
	                       v_PSE_COMP_NOTE_TOTAL || ', PSP = ' || v_PSP_COMP_NOTE_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_COMP_EVENT_TOTAL = v_PSP_COMP_EVENT_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company event totals match. Total = ' || v_PSP_COMP_EVENT_TOTAL;
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company event totals DO NOT match. PSE = ' ||
	                       v_PSE_COMP_EVENT_TOTAL || ', PSP = ' || v_PSP_COMP_EVENT_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_ADDRESS_TOTAL = v_PSP_ADDRESS_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Address totals match. Total = ' || v_PSP_ADDRESS_TOTAL;
    ELSE
	  -- DO NOT stop migration due to orphan records
      v_temp_event_type := pc_Event_Type_Warn;	  
	  v_event_msg       := 'Address totals DO NOT match. PSE = ' ||
	                       v_PSE_ADDRESS_TOTAL || ', PSP = ' || v_PSP_ADDRESS_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_INDIVIDUAL_TOTAL = v_PSP_INDIVIDUAL_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Individual totals match. Total = ' || v_PSP_INDIVIDUAL_TOTAL;
    ELSE
	  -- DO NOT stop migration due to orphan records
      v_temp_event_type := pc_Event_Type_Warn;	  
	  v_event_msg       := 'Individual totals DO NOT match. PSE = ' ||
	                       v_PSE_INDIVIDUAL_TOTAL || ', PSP = ' || v_PSP_INDIVIDUAL_TOTAL;
	END IF;
	
    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


    IF (v_PSE_BANKACCT_TOTAL = v_PSP_BANKACCT_TOTAL) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Bank Account totals match. Total = ' || v_PSP_BANKACCT_TOTAL;
    ELSE
	  -- DO NOT stop migration due to orphan records
      v_temp_event_type := pc_Event_Type_Warn;	  
	  v_event_msg       := 'Bank Account totals DO NOT match. PSE = ' ||
	                       v_PSE_BANKACCT_TOTAL || ', PSP = ' || v_PSP_BANKACCT_TOTAL;
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);
	
	
	--
	-- GROUPING COMPARES
	--

    IF (FN_PAYROLL_STATUSES_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Payroll statuses match. (f.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Payroll statuses DO NOT match.';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (FN_TRANSACTIONS_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Transactions match. (g. i. j. k. m. p.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Transactions DO NOT match. (g. i. j. k. m. p.)';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (FN_AUDIT_HISTORY_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Audit History matches. (o.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Audit History DOES NOT matches. (o.)';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (FN_COMPANY_EVENTS_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company events match. (r.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company events DO NOT match. (r.)';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (FN_COMPANY_PAYROLLS_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company payrolls match. (s.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company payrolls DO NOT match. (s.)';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (FN_COMPANY_PAYCHECKS_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company paychecks match. (t.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company paychecks DO NOT match. (t.)';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);

    
    IF (FN_COMPANY_CONTACTS_MATCH) THEN
      v_temp_event_type := pc_Event_Type_Info;	  
	  v_event_msg       := 'Company contacts match. (u.)';
    ELSE
      b_data_matches    := FALSE;
      v_temp_event_type := pc_Event_Type_Error;	  
	  v_event_msg       := 'Company contacts DO NOT match. (u.)';
	END IF;

    PR_SET_COMP_MIGRATION_EVENT (NULL, v_temp_event_type, v_event_cd, v_event_msg);


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
    p_PSE_COMPANY_GSEQ             IN  NUMBER,       -- optional, pse gseq
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
	
	  -- BOTH PSE AND PSP COMPANIES MUST EXIST TO DO THE VALIDATION
	  
	  b_company_exists := FN_PSE_COMP_EXISTS (p_PSE_COMPANY_GSEQ);
	  
	  IF (NOT b_company_exists) THEN
	   
	    RAISE_APPLICATION_ERROR (
		  pc_raise_app_err_cd,
		  'PSE COMPANY CANNOT BE FOUND.  ' || p_PSE_COMPANY_GSEQ || '.',
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
	  -- COMPARE PSE COMPANY TO PSP COMPANY
	  -- 
	  
	  -- initialize to happy path
	  b_data_matches        := TRUE;
      b_data_mismatch_found := FALSE;
	  v_temp_rc_msg         := 'Mismatches found in the following: ';
	
      /* 
         07.08.2008 emr
         removed because now done by controller
      
      PR_SET_COMP_VALIDATE_START (  
        p_PSE_COMPANY_DB_ID,
	    v_company_migration_gseq
	  );
      
	  */
      
      v_company_migration_gseq := FN_GET_MIGRATION_Q_PK (p_PSE_COMPANY_GSEQ);   	  

      -- ledger compare (1.)	  
	  PR_VALIDATE_LEDGER (
	    p_PSE_COMPANY_GSEQ, 
		p_PSP_COMPANY_GUID, 
		v_company_migration_gseq,
		b_data_matches
	  );
	  	  
	  IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE;
  	    v_temp_rc_msg         := v_temp_rc_msg || 'ledger, '; 	  
	  END IF;

	  
	  -- company bank account compare (2.)
	  PR_VALIDATE_COMP_BANK_ACCT (
	    p_PSE_COMPANY_GSEQ, 
		p_PSP_COMPANY_GUID, 
		v_company_migration_gseq,
		b_data_matches
	  );
	  	  
	  IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE; 	  
  	    v_temp_rc_msg         := v_temp_rc_msg || 'company bank account, ';		
	  END IF;

	  
	  -- company compare (3.)
	  PR_VALIDATE_COMPANY_INFO (
	    p_PSE_COMPANY_GSEQ, 
		p_PSP_COMPANY_GUID, 
		v_company_migration_gseq,
		b_data_matches
	  );
	  	  
	  IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE; 
  	    v_temp_rc_msg         := v_temp_rc_msg || 'company info, ';			  
	  END IF;

	  
	  -- company dd service compare (4.)
	  PR_VALIDATE_DD_INFO (
	    p_PSE_COMPANY_GSEQ, 
		p_PSP_COMPANY_GUID, 
		v_company_migration_gseq,
		b_data_matches
	  );
	  	  
	  IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE; 	  
  	    v_temp_rc_msg         := v_temp_rc_msg || 'dd info, ';		
	  END IF;

	  
	  -- employee compare (5.)
	  PR_VALIDATE_EMPLOYEES (
	    p_PSE_COMPANY_GSEQ, 
		p_PSP_COMPANY_GUID, 
		v_company_migration_gseq,
		b_data_matches
	  );
	  	  
	  IF (NOT b_data_matches) THEN
        b_data_mismatch_found := TRUE; 	  
  	    v_temp_rc_msg         := v_temp_rc_msg || 'employees.';		
	  END IF;
	  
	  
	  --
	  -- report the results back the the calling api
	  --

      /* 
         07.08.2008 emr
         removed because now done by controller
	  
	  IF (NOT b_data_mismatch_found) THEN

        PR_SET_COMP_VALIDATE_END (  
	      v_company_migration_gseq,
		  PK_IOPDDTOPSP_CONST.gc_Complete_StateCD
	    );
	  
	  ELSE

        PR_SET_COMP_VALIDATE_END (  
	      v_company_migration_gseq,
		  PK_IOPDDTOPSP_CONST.gc_Error_StateCD
	    );
		
        p_RETURN_CD  := -1;
        p_RETURN_MSG := v_temp_rc_msg;

	  END IF;

      */

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
        p_RETURN_MSG := 'PSE and PSP databases have global data mismatches.';
	  END IF;
	  
	END IF;
	
  EXCEPTION
    WHEN OTHERS THEN
	  RAISE;
  END;
  

  -- ------------------------------------------------------------------------
  -- PUBLIC APIs
  -- ------------------------------------------------------------------------

  PROCEDURE PR_Validate_IOP_Company (
    p_PSE_COMPANY_GSEQ             IN  NUMBER,
	p_PSP_COMPANY_GUID             IN  VARCHAR2,
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
      p_PSE_COMPANY_GSEQ,
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


  PROCEDURE PR_Validate_IOP_Migration (
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
      NULL,  -- p_PSE_COMPANY_DB_ID
	  NULL,  -- p_PSP_COMPANY_DB_ID
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

  
END PK_IOPDDTOPSP_VALIDATION; 
/


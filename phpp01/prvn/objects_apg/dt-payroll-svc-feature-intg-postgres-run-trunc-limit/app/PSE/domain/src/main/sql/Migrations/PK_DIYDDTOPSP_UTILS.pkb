CREATE OR REPLACE PACKAGE BODY PK_DIYDDTOPSP_UTILS AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_UTILS
   UPDATED: 11.26.2008  09:00 AM  
   PURPOSE: A library of common functions.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.26.2008  EMR              Created functions for customer
   1.1        06.19.2008  EMR              Added support for events, employees,
                                           and payrolls.
   1.2        06.23.2008  EMR              Add CompanyInfo v1.1
   1.3        06.25.2008  EMR              added generic sequence generator
   1.3.1      06.26.2008  EMR              fixed emp bank acct eff date   
   1.3.2      06.26.2008  EMR              added functions for paycheck split   
   1.3.3      06.27.2008  EMR              added functions for txns
   1.3.3      07.01.2008  EMR              aligned psp enums
   1.3.4      07.09.2008  EMR              modified ee bank eff date for distinct
   1.3.5      07.15.2008  EMR              provided default to utils as follows:
                                             FNGetCompBankAcctEffDt  = event BNKV, BNKA, migration date
                                             FNGetDDCompStatus       = ok to error if not term or active
                                             FNGetDDCompStatusEffDt  = event ACTV, SNUP, migration date
                                             FNGetEEOverrideLimitAmt = 15,000
                                             FNGetEROverrideLimitAmt = 40,000
                                             FNGetEmpBankEffDttm     = take oldest payroll run
                                             FNGetPSPMasterSKU       = default GC_PSP_SKU_STD3  
                                             FNGetPSPSubtype         = if not found, QB Payroll Standard  
                                             FNGetSignUpDt           = enroll date, then SNUP event, migration date
   1.4.5      07.18.2008  EMR              used new range for raise app err                                            
   1.4.6      07.23.2008  EMR              added function for comp email   
   1.4.7      07.29.2008  EMR              fixed paycheck source id
                                             FNGetPaycheckSourceID   
                                             PRGetSrcPaycheckID
   1.4.8      07.29.2008  EMR              remapped subtype to match psp enum
   1.4.9      07.31.2008  EMR              added count of diy payrolls to client   
   1.4.10     08.05.2008  EMR              fixed signup date   
   1.4.11     08.06.2008  EMR              added support for three princ contacts
   1.4.12     08.07.2008  EMR              strip spaces from email ids   
   1.4.13     08.07.2008  EMR              added contact email and name formatter
   1.4.14     08.08.2008  EMR              added more email formatting
   1.4.15     08.13.2008  EMR              changed source table for token
   1.4.16     09.30.2008  EMR              convert reason code
   1.4.17     10.08.2008  EMR              changed comp bank account date function
   1.4.18     10.09.2008  EMR              fixed FNGetSalesTaxChartName to handle
                                             no records in child table
   1.4.19     10.09.2008  EMR              changed FNGetEmpSrcBankID params from
                                             number to varchar
   1.4.20     10.14.2008  EMR              added new local cache for payrolls                                             
   1.4.20.1   10.14.2008  EMR              modified PRGetAS400PaycheckID to work
                                             with local payroll cache.
   1.4.21     10.15.2008  EMR              modified FNGetPSPSubtype to pass back
                                             Assisted value so Java could error.
   1.4.22     10.16.2008  EMR              modified FNGetPSPSubtype to deal with 
                                             assisted and terminated companies.
   1.4.23     10.22.2008  EMR              added new purge temp cache utilities
                                             to accomodate global validation.
   1.4.24     11.26.2008  EMR              modified FNGetPSPMasterSKU due to 
                                             wrong order of CASE statement. 
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- Private Functions
  -- ------------------------------------------------------------------------

  --
  -- simple sequence generator
  --
  FUNCTION FNGetNextGseq
  RETURN NUMBER
  IS
    v_temp_gseq                    NUMBER;
  BEGIN

    SELECT SEQ_GSEQ_FOR_JAVA.NEXTVAL
      INTO v_temp_gseq
      FROM DUAL;

    RETURN v_temp_gseq;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot generate gseq for java. ' || SQLERRM,
		FALSE);
  END;


  --
  -- Retrieve and convert the company status
  --
  FUNCTION FNGetCompStatus (
    p_AS400_STATUS_CD              IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_status                  VARCHAR2(100);

  BEGIN

    -- only companies thare are terminated or active will be migrated.
	-- all others can stay put, maybe forever.

    SELECT DECODE (
             p_AS400_STATUS_CD,
             PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Termed,
             PK_DIYDDTOPSP_CONST.gc_PSP_ER_Status_Termed,
             PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Active,
             PK_DIYDDTOPSP_CONST.gc_PSP_ER_Status_Active
           )
      INTO v_temp_status
      FROM DUAL;

    RETURN v_temp_status;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot map company status. ' || p_AS400_STATUS_CD || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- Retrieve the date the company status was effective
  --
  FUNCTION FNGetCompStatusEffDt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN DATE
  IS
    v_temp_status_effdt            DATE;

  BEGIN

    -- sign up event represents when the company was first added to the as400.
	-- note there could be many records of this type so ensure only the lates
	-- is returned.

    SELECT COMP_STATUS_EFFDT
      INTO v_temp_status_effdt
      FROM (
            SELECT ROWNUM AS inRowNum,
                   COMP_STATUS_EFFDT
              FROM (
                    SELECT TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS COMP_STATUS_EFFDT
                      FROM DIY_IQEVENT
                     WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_active
                       AND EVE_USERID = p_DIY_COMPANY_ID
		             ORDER 
                        BY EVE_TIMESTAMP DESC
                   )
	       )
     WHERE inRowNum = 1;

    RETURN v_temp_status_effdt;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve company status effective date. ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- get the company email id using the contacts.
  --
  FUNCTION FNGetCompanyEmailID (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN VARCHAR2
  IS
    v_temp_comp_emailid            VARCHAR2(100);
    b_found_emailid                BOOLEAN;

  BEGIN
  
    -- DESIGN NOTE
    --   so when we started testing with real prod data we discovered that
    --   email is optional in DIY.  so we try and try and try but if all
    --   else fails then use a default email id that will not go anywhere.
    --   first use admin contact then walk through principle contacts.
  
    b_found_emailid := FALSE;

    BEGIN
    
	  SELECT PK_DIYDDTOPSP_UTILS.FNFormatEmailID (
                CLIC_EMAIL) 
	    INTO v_temp_comp_emailid
        FROM DIY_IQCONTACT
       WHERE CLIC_USERID = p_DIY_COMPANY_ID;
      
      IF (v_temp_comp_emailid = PK_DIYDDTOPSP_CONST.GC_DIY_NO_EMAIL) THEN
        b_found_emailid := FALSE;
      ELSE 
        b_found_emailid := TRUE;
      END IF;
        
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        b_found_emailid := FALSE;
      WHEN OTHERS THEN
        RAISE;
    END;


    IF (NOT b_found_emailid) THEN
    
      BEGIN
              
        SELECT PK_DIYDDTOPSP_UTILS.FNFormatEmailID (
                CLIP_EMAIL) 
	      INTO v_temp_comp_emailid
          FROM DIY_IQPRINC_CONTACT
         WHERE CLIP_USERID = p_DIY_COMPANY_ID
           AND CLIP_NUMBER = 1;
       
        IF (v_temp_comp_emailid = PK_DIYDDTOPSP_CONST.GC_DIY_NO_EMAIL) THEN
          b_found_emailid := FALSE;
        ELSE 
          b_found_emailid := TRUE;
        END IF;

        
      EXCEPTION
        WHEN NO_DATA_FOUND THEN
          b_found_emailid := FALSE;
        WHEN OTHERS THEN
          RAISE;
      END;
      
    END IF;
    

    IF (NOT b_found_emailid) THEN
    
      BEGIN
              
        SELECT PK_DIYDDTOPSP_UTILS.FNFormatEmailID (
                 CLIP_EMAIL) 
	      INTO v_temp_comp_emailid
          FROM DIY_IQPRINC_CONTACT
         WHERE CLIP_USERID = p_DIY_COMPANY_ID
           AND CLIP_NUMBER = 2;
       
        IF (v_temp_comp_emailid = PK_DIYDDTOPSP_CONST.GC_DIY_NO_EMAIL) THEN
          b_found_emailid := FALSE;
        ELSE 
          b_found_emailid := TRUE;
        END IF;

        
      EXCEPTION
        WHEN NO_DATA_FOUND THEN
          b_found_emailid := FALSE;
        WHEN OTHERS THEN
          RAISE;
      END;
      
    END IF;

    
    IF (NOT b_found_emailid) THEN
      v_temp_comp_emailid := PK_DIYDDTOPSP_CONST.GC_DIY_NO_EMAIL;
    END IF;


    RETURN v_temp_comp_emailid;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve primary principal email id for ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- strip everything but numbers for things like phone and zip
  --
  FUNCTION FNGetOnlyNumbers (
    p_CHAR_ITEM                    IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_char_item               VARCHAR2(100);

  BEGIN

    -- strip out alpha and symbols leave just numbers
	-- translate match set must equal the number of translate replace list

	SELECT REPLACE(TRANSLATE (p_CHAR_ITEM, '-(). ', '     '), ' ', NULL)
	  INTO v_temp_char_item
      FROM DUAL;

    RETURN v_temp_char_item;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot strip non numerics from value ' || p_CHAR_ITEM || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetPSPContactRoleCD (
    p_AS400_PRINCIPAL_NUMBER       IN  NUMBER
  ) RETURN VARCHAR2
  IS
    v_temp_role                  VARCHAR2(100);

  BEGIN

    -- only companies thare are terminated or active will be migrated.
	-- all others can stay put, maybe forever.

    SELECT DECODE (
             p_AS400_PRINCIPAL_NUMBER,
             1,
             PK_DIYDDTOPSP_CONST.gc_ContactRole_PriPrinc,
             2,
             PK_DIYDDTOPSP_CONST.gc_ContactRole_SecPrinc,
             3,
             PK_DIYDDTOPSP_CONST.gc_ContactRole_SecPrinc,
             PK_DIYDDTOPSP_CONST.gc_ContactRole_SecPrinc
           )
      INTO v_temp_role
      FROM DUAL;

    RETURN v_temp_role;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot map company status. ' || p_AS400_PRINCIPAL_NUMBER || '. ' || SQLERRM,
		FALSE);
  END;
  

  --
  -- if the customer has a different limit than the overall default
  --
  FUNCTION FNGetEROverrideLimitAmt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN NUMBER
  IS
    v_temp_amount                  NUMBER;
  BEGIN

	BEGIN

	  -- amount6 is company and amount14 is employee
	  -- ensure only the latest record is returned

      SELECT COMP_DD_LIMIT
        INTO v_temp_amount
        FROM (
              SELECT ROWNUM AS inRowNum,
                     ADDL_AMT AS COMP_DD_LIMIT
                FROM DIY_DXADDLINFO
               WHERE ADDL_USERID  = p_DIY_COMPANY_ID
			     AND ADDL_RECTYPE = PK_DIYDDTOPSP_CONST.gc_DIY_LimitType_Company
			 )
	   WHERE inRowNum = 1;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_amount := PK_DIYDDTOPSP_CONST.gc_DIY_LimitAmt_Company;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    RETURN v_temp_amount;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve company limit override amount. ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- if all employees have a different limit than the overall default
  --
  FUNCTION FNGetEEOverrideLimitAmt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN NUMBER
  IS
    v_temp_amount                  NUMBER;
  BEGIN

	BEGIN

	  -- amount6 is company and amount14 is employee
	  -- ensure only the latest record is returned

      SELECT EMP_DD_LIMIT
        INTO v_temp_amount
        FROM (
              SELECT ROWNUM   AS inRowNum,
                     ADDL_AMT AS EMP_DD_LIMIT
                FROM DIY_DXADDLINFO
               WHERE ADDL_USERID  = p_DIY_COMPANY_ID
			     AND ADDL_RECTYPE = PK_DIYDDTOPSP_CONST.gc_DIY_LimitType_Employee
			 )
	   WHERE inRowNum = 1;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_amount := PK_DIYDDTOPSP_CONST.gc_DIY_LimitAmt_Employee;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    RETURN v_temp_amount;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve employee limit override amount. ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- Retrieve and convert the company direct deposit status
  --
  FUNCTION FNGetDDCompStatus (
    p_AS400_STATUS_CD              IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_status                  VARCHAR2(100);

  BEGIN

    -- only companies thare are terminated or active will be migrated.
	-- all others can stay put, maybe forever.

    SELECT DECODE (
             p_AS400_STATUS_CD,
             PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Termed,
             PK_DIYDDTOPSP_CONST.gc_PSP_ER_Status_Termed,
             PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Active,
             PK_DIYDDTOPSP_CONST.gc_PSP_ER_Status_Active,
             NULL
           )
      INTO v_temp_status
      FROM DUAL;
      
    IF (v_temp_status IS NULL) THEN

	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Company is not in Terminated or Active status. ',
		FALSE);
    
    END IF;

    RETURN v_temp_status;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve company DD status. ' || p_AS400_STATUS_CD || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- Retrieve the date the company direct deposit status was effective
  --
  FUNCTION FNGetDDCompStatusEffDt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN DATE
  IS
    v_temp_status_effdt            DATE;
    b_found_record                 BOOLEAN;    

  BEGIN

    b_found_record := FALSE;

    -- active, first payroll (spay) and payroll (parl) events all happen together
	-- so just take active.
	-- note there could be many records of this type so ensure only the lates
	-- is returned.

	BEGIN

      SELECT DD_STATUS_EFFDT
        INTO v_temp_status_effdt
        FROM (
              SELECT ROWNUM  AS inRowNum,
                     DD_STATUS_EFFDT
                FROM (
                      SELECT TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS DD_STATUS_EFFDT
                        FROM DIY_IQEVENT
                       WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_active
                         AND EVE_USERID = p_DIY_COMPANY_ID
		               ORDER 
                          BY EVE_TIMESTAMP DESC
                     )
	         )
       WHERE inRowNum = 1;

      b_found_record := TRUE;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        b_found_record := FALSE;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    -- in most cases the one above will be used.  but just in case always have
    -- a second options.  options are a good thing.
    IF (NOT b_found_record) THEN

	  BEGIN

        SELECT DD_STATUS_EFFDT
          INTO v_temp_status_effdt
          FROM (
                SELECT ROWNUM  AS inRowNum,
                       DD_STATUS_EFFDT
                  FROM (
                        SELECT TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS DD_STATUS_EFFDT
                          FROM DIY_IQEVENT
                         WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_signup
                           AND EVE_USERID = p_DIY_COMPANY_ID
		                 ORDER 
                            BY EVE_TIMESTAMP DESC
                       )
	           )
         WHERE inRowNum = 1;

        b_found_record := TRUE;
      
	  EXCEPTION
	    WHEN NO_DATA_FOUND THEN
	      b_found_record := FALSE;
	    WHEN OTHERS THEN
	      RAISE;
      END;
    
    END IF;
    
    -- ok 0 for 2.  that means reach to the back of the rack and use the good
    -- old date of migration.  that's what Linda told me to do.
    IF (NOT b_found_record) THEN
      v_temp_status_effdt := SYSDATE;
    END IF;


    RETURN v_temp_status_effdt;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve DD status effective date. ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;
 

  --
  -- Retrieve the date the company bank account was effective
  --
  FUNCTION FNGetCompBankAcctEffDt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN DATE
  IS
    v_temp_status_effdt            DATE;
    b_found_record                 BOOLEAN;

  BEGIN
  
    b_found_record := FALSE;

    -- bank account verified is the event that indicates when a bank account
	-- became active, or effective in PSP terms.
	-- note there could be many records of this type so ensure only the lates
	-- is returned.
    
    -- 10.08.2008 EMR
    --   modified this whole block so that we guarantee a date is returned.
    --   currently there are some events missing.

    /* V1 - modified due to these events missing.  need a fool proof method.
    
	BEGIN

      SELECT BANK_ACCT_STATUS_EFFDT
        INTO v_temp_status_effdt
        FROM (
              SELECT ROWNUM                                     AS inRowNum,
                     BANK_ACCT_STATUS_EFFDT
                FROM (
                      SELECT TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS BANK_ACCT_STATUS_EFFDT
                        FROM DIY_IQEVENT
                       WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_bnkacct_vfy
                         AND EVE_USERID = p_DIY_COMPANY_ID
		               ORDER 
                          BY EVE_TIMESTAMP DESC
                     )
	         )
       WHERE inRowNum = 1;
       
      b_found_record := TRUE;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
	    b_found_record := FALSE;
	  WHEN OTHERS THEN
	    RAISE;
	END;
    
    -- in most cases the one above will be used.  but just in case always have
    -- a second options.  options are a good thing.
    IF (NOT b_found_record) THEN

	  BEGIN

        SELECT BANK_ACCT_STATUS_EFFDT
          INTO v_temp_status_effdt
          FROM (
                SELECT ROWNUM                                     AS inRowNum,
                       BANK_ACCT_STATUS_EFFDT
                  FROM (
                        SELECT TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS BANK_ACCT_STATUS_EFFDT
                          FROM DIY_IQEVENT
                         WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_bnkacct_actv
                           AND EVE_USERID = p_DIY_COMPANY_ID
		                 ORDER 
                            BY EVE_TIMESTAMP DESC
                       )
	           )
         WHERE inRowNum = 1;
       
        b_found_record := TRUE;

	  EXCEPTION
	    WHEN NO_DATA_FOUND THEN
	      b_found_record := FALSE;
	    WHEN OTHERS THEN
	      RAISE;
      END;
    
    END IF;
    
    */

    BEGIN

      SELECT BANK_ACCT_STATUS_EFFDT
        INTO v_temp_status_effdt
        FROM (
              SELECT ROWNUM AS outRowNum,
                     DECODE (
                       DIY_EVENT_CODE,
                       PK_DIYDDTOPSP_CONST.gc_event_cd_bnkacct_vfy,  1,
                       PK_DIYDDTOPSP_CONST.gc_event_cd_bnkacct_actv, 2,
                       PK_DIYDDTOPSP_CONST.gc_event_cd_signup,       3
                     ) AS DIY_EVENT_ORDER,
                     BANK_ACCT_STATUS_EFFDT
                FROM (
                      SELECT ROWNUM                                              AS inRowNum,
                             DIY_EVENT_CODE,
                             TO_DATE(BANK_ACCT_STATUS_EFFDT, 'YYYYMMDDHH24MISS') AS BANK_ACCT_STATUS_EFFDT
                        FROM (
                              SELECT EVE_CODE      AS DIY_EVENT_CODE,
                                     EVE_TIMESTAMP AS BANK_ACCT_STATUS_EFFDT
                                FROM DIY_IQEVENT
                               WHERE EVE_CODE   IN (
                                       PK_DIYDDTOPSP_CONST.gc_event_cd_bnkacct_vfy, 
                                       PK_DIYDDTOPSP_CONST.gc_event_cd_bnkacct_actv,
                                       PK_DIYDDTOPSP_CONST.gc_event_cd_signup
                                     )
                                 AND EVE_USERID = p_DIY_COMPANY_ID
                               ORDER 
                                  BY EVE_TIMESTAMP DESC
                             )
                     )
               WHERE inRowNum = 1
               ORDER 
                  BY DIY_EVENT_ORDER
             )
      WHERE outRowNum = 1;

      b_found_record := TRUE;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
	    b_found_record := FALSE;
	  WHEN OTHERS THEN
	    RAISE;
    END;
       

    -- ok 0 for 2.  that means reach to the back of the rack and use the good
    -- old date of migration.  
    IF (NOT b_found_record) THEN
      v_temp_status_effdt := SYSDATE - 180;
    END IF;


    RETURN v_temp_status_effdt;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve company bank account effective date. ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;
  
  
  --
  -- Get either the first, last or middle name of the employee
  --
  FUNCTION FNGetEmployeeName (
    p_NAME_PART_CD                 IN   VARCHAR2,
    p_NAME                         IN   VARCHAR2
  )	RETURN VARCHAR2
  IS
    v_temp_name                    VARCHAR2(100);
    v_trimmed_name                 VARCHAR2(100);

  BEGIN
  
    v_temp_name    := PK_DIYDDTOPSP_CONST.GC_NULL_STR;
    v_trimmed_name := TRIM(p_NAME);
    
    -- DESIGN NOTES
    --   the assumption is the pattern is either 'first last' or 'first m last'
    --   thus there are two spaces.  In the case of no middle initial or name 
    --   then that would default to a null value.
  
    IF (p_NAME_PART_CD = PK_DIYDDTOPSP_CONST.gc_Name_First_CD) THEN
    
      SELECT SUBSTR(v_trimmed_name, 
                    1, 
                    INSTR(v_trimmed_name, ' ', 1, 1) - 1 
                   )
        INTO v_temp_name
        FROM dual;
      
    END IF;
    
    IF (p_NAME_PART_CD = PK_DIYDDTOPSP_CONST.gc_Name_Middle_CD) THEN
    
      SELECT NVL(
                 SUBSTR(v_trimmed_name, 
                        INSTR(v_trimmed_name,  ' ', 1, 1) + 1,
                        (INSTR(v_trimmed_name,  ' ', -1, 1) - INSTR(v_trimmed_name,  ' ', 1, 1)) - 1 
                       ), 
                 ''
                )
        INTO v_temp_name
        FROM dual;
      
    END IF;

    IF (p_NAME_PART_CD = PK_DIYDDTOPSP_CONST.gc_Name_Last_CD) THEN
    
      SELECT SUBSTR(v_trimmed_name, 
                    INSTR(v_trimmed_name,  ' ', -1, 1) + 1,
                    LENGTH(v_trimmed_name) 
                   )
        INTO v_temp_name
        FROM dual;
      
    END IF;
    
    RETURN v_temp_name;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE;
  END;



  --
  -- Generate a employee source bank id
  --

  /* V1 10.09.2008 EMR
  FUNCTION FNGetEmpSrcBankID (
    p_EE_RTN_NUM                   IN   NUMBER,
    p_EE_ACCT_NUM                  IN   NUMBER
  )	RETURN VARCHAR2
  */
  
  FUNCTION FNGetEmpSrcBankID (
    p_EE_RTN_NUM                   IN   VARCHAR2,
    p_EE_ACCT_NUM                  IN   VARCHAR2
  )	RETURN VARCHAR2

  IS
    v_temp_id                    VARCHAR2(100);

  BEGIN
  
    -- 10.09.2008 EMR
    --   these are actually characters, was a typo thinking _num
    --v_temp_id := TO_CHAR(p_EE_RTN_NUM) || '-' || TO_CHAR(p_EE_ACCT_NUM);
    
    v_temp_id := p_EE_RTN_NUM || '-' || p_EE_ACCT_NUM;
    
    RETURN v_temp_id;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE;
  END;

  
  --
  -- Obtain a generated bank effective date 
  --
  FUNCTION FNGetEmpBankEffDttm (
    p_COMPANY_DB_ID                IN   NUMBER
  ) RETURN DATE
  IS
    v_temp_dttm                    DATE;

  BEGIN

    -- to accomodate multiple payrolls use the first payroll that came in
    -- which has yet to be offloaded.
  
    SELECT MIN(TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS'))
      INTO v_temp_dttm
      FROM DIY_IQACH
     WHERE ACH_USERID        = p_COMPANY_DB_ID
       AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
       AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
       AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal;
       
    RETURN v_temp_dttm;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE;
  END;


  --
  -- Generate a source paycheck id 
  --
  FUNCTION FNGetPaycheckSourceID (
    p_COMPANY_DB_ID                IN   NUMBER,  
    p_PAYRUN_DB_ID                 IN   NUMBER,
    p_PAYCHECK_OFFSET_NUM          IN   NUMBER
  ) RETURN VARCHAR2
  IS
    v_temp_source_id               VARCHAR2(100);

  BEGIN

    -- 07.29.2008
    -- this was the old algorithm.  the table now referenced has
    -- the appropriate cross reference keys.
      
    -- v_temp_source_id := TO_CHAR(p_PAYRUN_DB_ID)           ||
    --                     PK_DIYDDTOPSP_CONST.gc_SourceID_Delimiter_CD ||
    --                     TO_CHAR(p_PAYCHECK_OFFSET_NUM);
  
    SELECT TO_CHAR(XREF_PAYCHKID)
      INTO v_temp_source_id
      FROM DIY_DXCHKXREF
     WHERE XREF_USERID       = p_COMPANY_DB_ID
       AND XREF_TRACE_NUMBER = p_PAYRUN_DB_ID
       AND XREF_DD_NUMBER    = p_PAYCHECK_OFFSET_NUM;
       
    RETURN v_temp_source_id;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE;
  END;
  

  --
  -- Generate a source transaction id 
  --
  FUNCTION FNGetDDTxnSourceID (
    p_PAYRUN_DB_ID                 IN   NUMBER,
    p_PAYCHECK_OFFSET_NUM          IN   NUMBER
  ) RETURN VARCHAR2
  IS
    v_temp_source_id               VARCHAR2(100);

  BEGIN
  
    v_temp_source_id := TO_CHAR(p_PAYRUN_DB_ID)           ||
                        PK_DIYDDTOPSP_CONST.gc_SourceID_Delimiter_CD ||
                        TO_CHAR(p_PAYCHECK_OFFSET_NUM);
                        
    RETURN v_temp_source_id;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE;
  END;
  

  --
  -- map as400 pricing to psp pricing
  --
  FUNCTION FNGetPSPMasterSKU (
    p_DIY_PRICE_CODE               IN  VARCHAR2,
    p_DIY_OFFER_CODE               IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_sku                     VARCHAR2(100);

  BEGIN

    -- 11.26.2008 EMR
    -- although the order of the WHENs mimics the requirements document, the
    -- execution order has to be changed to filter out the three standards that
    -- should get standard 3 instead of standard.
    
    /* V1
    SELECT CASE
             WHEN p_DIY_PRICE_CODE = 'DIYDD-FREE' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDDSTD-3' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDD-STD' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD
             WHEN p_DIY_OFFER_CODE = 'FREEDD1YR' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_OFFER_CODE = 'P56152' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_OFFER_CODE = 'P14215' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             ELSE PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
           END
      INTO v_temp_sku
      FROM DUAL;
    */ 

    SELECT CASE
             WHEN p_DIY_PRICE_CODE = 'DIYDD-FREE' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDDSTD-3' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDD-STD' AND
                  p_DIY_OFFER_CODE = 'FREEDD1YR' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDD-STD' AND
                  p_DIY_OFFER_CODE = 'P56152' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDD-STD' AND
                  p_DIY_OFFER_CODE = 'P14215' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
             WHEN p_DIY_PRICE_CODE = 'DIYDD-STD' 
             THEN PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD
             ELSE PK_DIYDDTOPSP_CONST.GC_PSP_SKU_STD3
           END
      INTO v_temp_sku
      FROM DUAL;

    RETURN v_temp_SKU;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve PSP SKU for. ' || p_DIY_PRICE_CODE || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetPSPOfferCode (
    p_DIY_OFFER_CODE               IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_offer                   VARCHAR2(100);

  BEGIN

    -- these are the only offer codes that should be available in PSP
  
    SELECT CASE
             WHEN p_DIY_OFFER_CODE = 'WAIVE ALL' 
             THEN p_DIY_OFFER_CODE
             WHEN p_DIY_OFFER_CODE = 'P57213' 
             THEN p_DIY_OFFER_CODE
             WHEN p_DIY_OFFER_CODE = 'P57553' 
             THEN p_DIY_OFFER_CODE
             WHEN p_DIY_OFFER_CODE = 'P58359' 
             THEN p_DIY_OFFER_CODE
             WHEN p_DIY_OFFER_CODE = 'P59258' 
             THEN p_DIY_OFFER_CODE
             WHEN p_DIY_OFFER_CODE = 'P59710' 
             THEN p_DIY_OFFER_CODE
             ELSE PK_DIYDDTOPSP_CONST.GC_NULL_STR
           END
      INTO v_temp_offer
      FROM DUAL; 

    RETURN v_temp_offer;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve DIY offer code for. ' || p_DIY_OFFER_CODE || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetPSPOfferExpDt (
    p_DIY_OFFER_CODE               IN  VARCHAR2,
    p_DIY_OFFER_EXP_DT             IN  VARCHAR2
  ) RETURN DATE
  IS
    v_temp_offer_exp               VARCHAR2(100);

  BEGIN

    -- these are the only offer codes that should be available in PSP
  
    SELECT CASE
             WHEN p_DIY_OFFER_CODE = 'WAIVE ALL' 
             THEN p_DIY_OFFER_EXP_DT
             WHEN p_DIY_OFFER_CODE = 'P57213' 
             THEN p_DIY_OFFER_EXP_DT
             WHEN p_DIY_OFFER_CODE = 'P57553' 
             THEN p_DIY_OFFER_EXP_DT
             WHEN p_DIY_OFFER_CODE = 'P58359' 
             THEN p_DIY_OFFER_EXP_DT
             WHEN p_DIY_OFFER_CODE = 'P59258' 
             THEN p_DIY_OFFER_EXP_DT
             WHEN p_DIY_OFFER_CODE = 'P59710' 
             THEN p_DIY_OFFER_EXP_DT
             ELSE PK_DIYDDTOPSP_CONST.GC_DEFAULT_OFFER_EXP_DT
           END
      INTO v_temp_offer_exp
      FROM DUAL;
    
    -- when testing with real prod data the date was set to 0.
    IF (v_temp_offer_exp = 0) THEN
      v_temp_offer_exp := PK_DIYDDTOPSP_CONST.GC_DEFAULT_OFFER_EXP_DT;
    END IF;
      
    RETURN TO_DATE(v_temp_offer_exp, 'YYYYMMDD');

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve DIY offer code for. ' || p_DIY_OFFER_CODE || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- derive key startup dates
  --
  FUNCTION FNGetFirstPayrollDt (
     p_COMPANY_DB_ID                IN   NUMBER
  ) RETURN DATE
  IS
    v_temp_first_date               DATE;

  BEGIN

    -- use the first occurance of the event type ACTV
  
    BEGIN 
    
      SELECT TO_DATE(FirstPayDate,  'YYYYMMDDHH24MISS')
        INTO v_temp_first_date
        FROM (
              SELECT ROWNUM        AS inRowNum,
                     FirstPayDate
                FROM (
                      SELECT EVE_TIMESTAMP AS FirstPayDate
                        FROM DIY_IQEVENT
                       WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_active
                         AND EVE_USERID = p_COMPANY_DB_ID
                       ORDER 
                          BY EVE_TIMESTAMP
                     )
             )
       WHERE inRowNum = 1;
       
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_temp_first_date := NULL;
      WHEN OTHERS THEN
        RAISE;
    END; 

    RETURN v_temp_first_date;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve first payroll date for ' || p_COMPANY_DB_ID || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetSignUpDt (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400  
    p_SIGNUP_DATE                  IN  NUMBER
  ) RETURN DATE
  IS
    v_temp_signup_date             VARCHAR2(100);
    b_found_record                 BOOLEAN;

  BEGIN

    b_found_record := FALSE;
    
    -- just format it from character to oracle date
    -- added a default search criteria just in case.  garuantee a date 
    -- is returned.
  
    BEGIN
    
      -- found out some enroll dates are not set.
      
      IF (p_SIGNUP_DATE <> 0) THEN
      
        SELECT TO_DATE(p_SIGNUP_DATE, 'YYYYMMDDHH24MISS')
          INTO v_temp_signup_date
          FROM DUAL;
        
        b_found_record := TRUE;
      
      ELSE
        b_found_record := FALSE;
      END IF;        
        
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        b_found_record := FALSE;  
      WHEN OTHERS THEN
        b_found_record := FALSE;
    END;

    
    IF (NOT b_found_record) THEN

	  BEGIN

        SELECT DIY_DD_SIGNUP_DATE
          INTO v_temp_signup_date
          FROM (
                SELECT ROWNUM                                     AS inRowNum,
                       DIY_DD_SIGNUP_DATE
                  FROM (
                        SELECT TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS DIY_DD_SIGNUP_DATE
                          FROM DIY_IQEVENT
                         WHERE EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_event_cd_signup
                           AND EVE_USERID = p_DIY_COMPANY_ID
		                 ORDER 
                            BY EVE_TIMESTAMP DESC
                       )
	           )
         WHERE inRowNum = 1;
       
        b_found_record := TRUE;

	  EXCEPTION
	    WHEN NO_DATA_FOUND THEN
	      b_found_record := FALSE;
	    WHEN OTHERS THEN
	      RAISE;
      END;
    
    END IF;
    
    -- this is the default in case all other attempts did not yield a date.
    IF (NOT b_found_record) THEN
    
      v_temp_signup_date := SYSDATE;
    
    END IF;
    
       
    RETURN v_temp_signup_date;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot convert sigup date ' || p_SIGNUP_DATE || '. ' || SQLERRM,
		FALSE);
  END;
  

  --
  -- obtain chart of accounts names
  --
  FUNCTION FNGetFeeChartName (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN VARCHAR2
  IS
    v_temp_name                    VARCHAR2(100);
  BEGIN

	BEGIN

      SELECT ChartAcctFeeName
        INTO v_temp_name
        FROM (
              SELECT ROWNUM     AS inRowNum,
                     ADDL_VALUE AS ChartAcctFeeName
                FROM DIY_DXADDLINFO
               WHERE ADDL_USERID  = p_DIY_COMPANY_ID
			     AND ADDL_RECTYPE = PK_DIYDDTOPSP_CONST.gc_ChartAcct_Fee_Type
			 )
	   WHERE inRowNum = 1;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_name := PK_DIYDDTOPSP_CONST.gc_Default_ChartAcct_Fee;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    RETURN v_temp_name;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve fee chart of account name for ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;
  

  FUNCTION FNGetSalesTaxChartName (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN VARCHAR2
  IS
    v_temp_name                    VARCHAR2(100);
  BEGIN

	BEGIN

      -- 10.09.2008 EMR
      --   added default in the decode when records are not found in child
      --   table.

      SELECT ChartAcctTaxName
        INTO v_temp_name
        FROM (
              SELECT ROWNUM     AS inRowNum,
                     DECODE (
                       TRIM(ADDL_VALUE),
                       PK_DIYDDTOPSP_CONST.gc_Null_Str,
                       PK_DIYDDTOPSP_CONST.gc_ChartAcct_Tax_Type,
                       ADDL_VALUE
                     ) AS ChartAcctTaxName
                FROM DIY_DXADDLINFO
               WHERE ADDL_USERID  = p_DIY_COMPANY_ID
			     AND ADDL_RECTYPE = PK_DIYDDTOPSP_CONST.gc_ChartAcct_Tax_Type
			 )
	   WHERE inRowNum = 1;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_name := PK_DIYDDTOPSP_CONST.gc_Default_ChartAcct_Tax;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    RETURN v_temp_name;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve sales tax chart of account name for ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;
  

  FUNCTION FNGetQBLastSyncToken (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN NUMBER
  IS
    v_temp_token                   NUMBER;
  BEGIN

	BEGIN

      SELECT LOG_TOKEN_OUT
        INTO v_temp_token
        FROM (
              SELECT ROWNUM AS inRowNum,
                     LOG_TOKEN_OUT
                FROM (
                      SELECT LOG_TOKEN_OUT
                        FROM DIY_SKLOG
                       WHERE LOG_USERID = p_DIY_COMPANY_ID 
                       ORDER 
                          BY LOG_DATE DESC, 
                             LOG_TIME DESC
                     )
             )
      WHERE inRowNum = 1;

	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_token := PK_DIYDDTOPSP_CONST.gc_Default_LastSync_Token;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    RETURN v_temp_token;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve QBDT last sync token number for ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- get encrypted pins
  --
  FUNCTION FNGetEncryptedPin (
    p_DIY_COMPANY_ID               IN  NUMBER,        -- DB KEY ON AS400
    p_PIN_NUM                      IN  NUMBER
  ) RETURN VARCHAR2
  IS
    v_temp_pin                     VARCHAR2(100);
    
  BEGIN

	BEGIN

      IF (p_PIN_NUM = 1) THEN
      
        SELECT TRIM(PIN_1)
          INTO v_temp_pin
          FROM DIY_DXPIN
         WHERE PIN_USERID = p_DIY_COMPANY_ID;
      
      END IF;
      
	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_pin := PK_DIYDDTOPSP_CONST.GC_NULL_STR;
	  WHEN OTHERS THEN
	    RAISE;
	END;
    
	BEGIN

      IF (p_PIN_NUM = 2) THEN
      
        SELECT TRIM(PIN_2)
          INTO v_temp_pin
          FROM DIY_DXPIN
         WHERE PIN_USERID = p_DIY_COMPANY_ID;
      
      END IF;
      
	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_pin := PK_DIYDDTOPSP_CONST.GC_NULL_STR;
	  WHEN OTHERS THEN
	    RAISE;
	END;
    
	BEGIN

      IF (p_PIN_NUM = 3) THEN
      
        SELECT TRIM(PIN_3)
          INTO v_temp_pin
          FROM DIY_DXPIN
         WHERE PIN_USERID = p_DIY_COMPANY_ID;
      
      END IF;
      
	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
        v_temp_pin := PK_DIYDDTOPSP_CONST.GC_NULL_STR;
	  WHEN OTHERS THEN
	    RAISE;
	END;


    RETURN v_temp_pin;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot retrieve pin ' || p_PIN_NUM || ' for ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- something to do with the type of QB service
  --
  FUNCTION FNGetPSPSubtype (
    p_DIY_COMPANY_ID               IN  VARCHAR2,
    p_AS400_STATUS_CD              IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_subtype                 VARCHAR2(100);

  BEGIN

    -- most of the subtypes are the same.  just replace blanks.
    -- also assisted subtypes should not be migrated
    
    -- 07.29.2008 EMR
    -- changed the mapping because psp uses short enums not the long
    -- wordy strings like the AS400.
    
    -- 10.16.2008 EMR
    --   modified mapping to handle both assisted and terminated
    --   companies.
    --
    --   STATUS     TAX        SUBTYPE        ACTION
    --   ------     ---        -----------    -----------------------
    --     TI        N         NULL           SET TO NULL
    --     TI        N         DIY            CONVERT TO PSP
    --     TI        N         ASSISTED       SET TO NULL
    --     TI        Y         NULL           SET TO NULL
    --     TI        Y         DIY            N/A
    --     TI        Y         ASSISTED       SET TO NULL
    --     A         N         NULL           SET TO STANDARD
    --     A         N         DIY            CONVERT TO PSP
    --     A         N         ASSISTED       THROW EXCEPTION, CLEAN UP BY REP
    --     A         Y         NULL           FILTERED IN MIGRATION QUEUE
    --     A         Y         DIY            FILTERED IN MIGRATION QUEUE
    --     A         Y         ASSISTED       FILTERED IN MIGRATION QUEUE
    
    BEGIN
    
      IF (p_AS400_STATUS_CD = PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Termed) THEN

        SELECT DECODE (
                 UPPER(TRIM(SUBT_SUBTYPE)),
                 '',                               PK_DIYDDTOPSP_CONST.gc_Null_Str,
                 'QB PAYROLL BASIC LIMITED',       'BasicLimited',
                 'QB PAYROLL BASIC UNLIMITED',     'BasicUnlimited',
                 'QB PAYROLL ENHANCED',            'Enhanced',
                 'QB PAYROLL ENHANCED ACCOUNTANT', 'EnhancedAccountant',
                 'QB PAYROLL ENHANCED UNLIMITED',  'EnhancedUnlimited',
                 'QB PAYROLL NEW BASIC UNLIMITED', 'NewBasicUnlimited',
                 'QB PAYROLL STANDARD',            'Standard',
                 'QB PAYROLL BASIC 0-3 EMP',       'Basic0to3Emp',
                 'QB PAYROLL ENHANCED 0-3 EMP',    'Enhanced0to3Emp',
                 'QB PAYROLL PAP ENH ACCT',        'PAPEnhAcct',
                 PK_DIYDDTOPSP_CONST.gc_Null_Str
               )
          INTO v_temp_subtype
          FROM DIY_IRSUBTYPE
         WHERE SUBT_USERID = p_DIY_COMPANY_ID;
      
      ELSIF (p_AS400_STATUS_CD = PK_DIYDDTOPSP_CONST.gc_DIY_ER_Status_Active) THEN 
    
        SELECT DECODE (
                 UPPER(TRIM(SUBT_SUBTYPE)),
                 '',                               'Standard',
                 'QB PAYROLL BASIC LIMITED',       'BasicLimited',
                 'QB PAYROLL BASIC UNLIMITED',     'BasicUnlimited',
                 'QB PAYROLL ENHANCED',            'Enhanced',
                 'QB PAYROLL ENHANCED ACCOUNTANT', 'EnhancedAccountant',
                 'QB PAYROLL ENHANCED UNLIMITED',  'EnhancedUnlimited',
                 'QB PAYROLL NEW BASIC UNLIMITED', 'NewBasicUnlimited',
                 'QB PAYROLL STANDARD',            'Standard',
                 'QB PAYROLL BASIC 0-3 EMP',       'Basic0to3Emp',
                 'QB PAYROLL ENHANCED 0-3 EMP',    'Enhanced0to3Emp',
                 'QB PAYROLL PAP ENH ACCT',        'PAPEnhAcct',
                 TRIM(SUBT_SUBTYPE)
               )
          INTO v_temp_subtype
          FROM DIY_IRSUBTYPE
         WHERE SUBT_USERID = p_DIY_COMPANY_ID;

      END IF;
       
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_temp_subtype := PK_DIYDDTOPSP_CONST.gc_Default_Subtype;
      WHEN OTHERS THEN
        RAISE;
    END; 

    RETURN v_temp_subtype;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot map AS400 subtype to PSP subtype for ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;
  

  --
  -- source paycheck id is generated now have to split into as400 pieces
  --
  PROCEDURE PRGetAS400PaycheckID (
    p_COMPANY_DB_ID                IN  NUMBER,  
    p_SOURCE_PAYCHECK_ID           IN  NUMBER,
    p_AS400_PAYRUN_ID              OUT NUMBER,
    p_AS400_PAYCHECK_ID1           OUT NUMBER,
    p_AS400_PAYCHECK_ID2           OUT NUMBER,
    p_AS400_PAYCHECK_ID3           OUT NUMBER,
    p_AS400_PAYCHECK_ID4           OUT NUMBER           
  ) 
  IS

  BEGIN

    -- NOTE:
    --   the first part of the source check id is the ach trace number
    --   and the second part is the achd sub number.
    --   both parts were concatenated to make a unique source check id.
    
    -- 07.29.2008
    --   this was the old algorithm until we found the magical cross ref
    --   table that links ofx paycheck ids to as400 tables.  
    
    --p_AS400_PAYRUN_ID := 
    --  TO_NUMBER (
    --     SUBSTR (
    --      p_SOURCE_PAYCHECK_ID,
    --      1,
    --      INSTR (
    --        p_SOURCE_PAYCHECK_ID, 
    --        PK_DIYDDTOPSP_CONST.gc_SourceID_Delimiter_CD,
    --        1,
    --        1
    --      ) - 1
    --    )
    --  );

    --p_AS400_PAYCHECK_ID := 
    --  TO_NUMBER (
    --     SUBSTR (
    --      p_SOURCE_PAYCHECK_ID,
    --      INSTR (
    --        p_SOURCE_PAYCHECK_ID, 
    --        PK_DIYDDTOPSP_CONST.gc_SourceID_Delimiter_CD,
    --        -1,
    --        1
    --      ) + 1,
    --      LENGTH (p_SOURCE_PAYCHECK_ID)       
    --    )
    --  );
    
    -- NOTE: 07.29.2008
    --   there is one paycheck and it can be split into two accounts.  The
    --   DXCHKXREF contains the split numbers and it appears as seperate rows
    --   in the IQACHDD check table.

    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.
    
    /* V1
    
    SELECT XREF_TRACE_NUMBER,
           XREF_DD_NUMBER,
           XREF_DD_NUMBER2,
           XREF_DD_NUMBER3,
           XREF_DD_NUMBER4
      INTO p_AS400_PAYRUN_ID,
           p_AS400_PAYCHECK_ID1,
           p_AS400_PAYCHECK_ID2,
           p_AS400_PAYCHECK_ID3,
           p_AS400_PAYCHECK_ID4
      FROM DIY_DXCHKXREF
     WHERE XREF_USERID   = p_COMPANY_DB_ID 
       AND XREF_PAYCHKID = p_SOURCE_PAYCHECK_ID;
       
    */
    
    SELECT XREF_TRACE_NUMBER,
           XREF_DD_NUMBER,
           XREF_DD_NUMBER2,
           XREF_DD_NUMBER3,
           XREF_DD_NUMBER4
      INTO p_AS400_PAYRUN_ID,
           p_AS400_PAYCHECK_ID1,
           p_AS400_PAYCHECK_ID2,
           p_AS400_PAYCHECK_ID3,
           p_AS400_PAYCHECK_ID4
      FROM TEMP_CACHE_DXCHKXREF
     WHERE XREF_USERID   = p_COMPANY_DB_ID 
       AND XREF_PAYCHKID = p_SOURCE_PAYCHECK_ID;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot split source paycheck id for ' || p_SOURCE_PAYCHECK_ID || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- generate data for payroll transactions
  --
  FUNCTION FNGetPayrunInsDttm (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_PAYROLL_RUN_ID               IN  NUMBER
  ) RETURN DATE
  IS
    v_temp_date                    DATE;

  BEGIN


    -- 10.14.2008 EMR
    --   using local payroll cache instead of going to AS400 to avoid joins
    --   in DB2.

    -- simply using the timestamp of when the payrun was submitted
  
    /* V1
    
    SELECT TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS')
      INTO v_temp_date
      FROM DIY_IQACH
     WHERE ACH_USERID       = p_DIY_COMPANY_ID
       AND ACH_TRACE_NUMBER = p_PAYROLL_RUN_ID;
    
    */
    
    SELECT TO_DATE(ACH_TIMESTAMP, 'YYYYMMDDHH24MISS')
      INTO v_temp_date
      FROM TEMP_CACHE_IQACH
     WHERE ACH_USERID       = p_DIY_COMPANY_ID
       AND ACH_TRACE_NUMBER = p_PAYROLL_RUN_ID;
 
    RETURN v_temp_date;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot obtain date for payrun ' || p_PAYROLL_RUN_ID ||
        ' for company ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetERDebitAmt (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_PAYROLL_RUN_ID               IN  NUMBER
  ) RETURN NUMBER
  IS
    v_temp_amt                     NUMBER;

  BEGIN

    SELECT SUM(ACHD_AMT)
      INTO v_temp_amt
      FROM DIY_IQACHDD
     WHERE ACHD_USERID       = p_DIY_COMPANY_ID
       AND ACHD_TRACE_NUMBER = p_PAYROLL_RUN_ID; 

    RETURN v_temp_amt;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot obtain employer debit amount for ' || p_PAYROLL_RUN_ID ||
        ' for company ' || p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;
  

  FUNCTION FNGetTxnStatusCD (
    p_TXN_AMT                      IN  NUMBER
  ) RETURN VARCHAR2
  IS
    v_temp_status                  VARCHAR2(100);

  BEGIN

    SELECT DECODE (
             p_TXN_AMT,
             0,
             PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cancel_State_CD,
             PK_DIYDDTOPSP_CONST.gc_PSP_Txn_Cr_State_CD
           )
      INTO v_temp_status
      FROM DUAL;

    RETURN v_temp_status;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot map transaction status for the amount ' || p_TXN_AMT || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetPriorPayrollCnt (
    p_DIY_COMPANY_ID               IN  NUMBER
  ) RETURN NUMBER
  IS
    v_prior_payrun_count            NUMBER;

  BEGIN

    -- count only successful prior payroll runs on the as400.  successfull
    -- simply means they were offloaded.
    
    SELECT COUNT(*)
      INTO v_prior_payrun_count
      FROM DIY_IQACH
     WHERE ACH_USERID         = p_DIY_COMPANY_ID
       AND TRIM(ACH_SOURCE)   = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
       AND ACH_OFFLOADED     <> PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
       AND TRIM(ACH_RECTYPE)  = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal;

    RETURN v_prior_payrun_count;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot obtain count for prior payroll runs for company ' || 
        p_DIY_COMPANY_ID || '. ' || SQLERRM,
		FALSE);
  END;


  FUNCTION FNGetPSPStrikeReason (
    p_AS400_STRIKE_REASON          IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_reason                  VARCHAR2(100);

  BEGIN

    SELECT DECODE(
             TRIM(p_AS400_STRIKE_REASON),
             PK_DIYDDTOPSP_CONST.gc_DIY_StrkRsn_AchRej, PK_DIYDDTOPSP_CONST.gc_PSP_StrkRsn_AchRej,
             PK_DIYDDTOPSP_CONST.gc_DIY_StrkRsn_Manual, PK_DIYDDTOPSP_CONST.gc_PSP_StrkRsn_Manual,
             PK_DIYDDTOPSP_CONST.gc_Null_Str
           )     
      INTO v_temp_reason
      FROM DUAL;

    RETURN v_temp_reason;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot convert AS400 strike reason. ' || p_AS400_STRIKE_REASON || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- THESE ARE SPECIAL FORMATTING FUNCTIONS TO ACCOMODATE DATA ANOMOLIES
  --

  FUNCTION FNFormatEmailID (
    p_EMAILID                      IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_email                   VARCHAR2(200);

  BEGIN

    -- some email ids are not present and some have spaces.  PSP will 
    -- choke if that happens.
    
    SELECT DECODE (
             TRIM(p_EMAILID),
             '',  PK_DIYDDTOPSP_CONST.GC_DIY_NO_EMAIL,
             REPLACE(TRIM(p_EMAILID), ' ', '')
           )
      INTO v_temp_email
      FROM DUAL;
    
    
    -- so as we do more testing with DIY we found that old email ids have
    -- no email suffix, like .com.  Mostly termed companies.
      
    IF (INSTR(v_temp_email, '@') = 0) OR
       (INSTR(v_temp_email, '.') = 0) THEN
       
       v_temp_email := PK_DIYDDTOPSP_CONST.GC_DIY_NO_EMAIL;
       
    END IF;

    RETURN v_temp_email;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot format email id. ' || p_EMAILID || '. ' || SQLERRM,
		FALSE);
  END;
  
  
  FUNCTION FNFormatContactName (
    p_CONTACT_NAME                 IN  VARCHAR2
  ) RETURN VARCHAR2
  IS
    v_temp_name                    VARCHAR2(200);

  BEGIN

    -- some names in DIY are blank and that will make PSP choke.  Due to 
    -- old old data.
    
    SELECT DECODE(
             TRIM(p_CONTACT_NAME),
             '', PK_DIYDDTOPSP_CONST.GC_DIY_NO_NAME,
             TRIM(p_CONTACT_NAME)
           )     
      INTO v_temp_name
      FROM DUAL;

    RETURN v_temp_name;

  EXCEPTION
    WHEN OTHERS THEN
	  RAISE_APPLICATION_ERROR (
	    pc_raise_app_err_cd,
		'Cannot format contact name. ' || p_CONTACT_NAME || '. ' || SQLERRM,
		FALSE);
  END;


  --
  -- THESE ARE SPECIAL FUNCTIONS TO CONTROL LOCAL CACHING FOR PERFORMANCE BOOST.
  --

  PROCEDURE PR_CREATE_PAYROLL_CACHE (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER        -- IQCLIENT.CLI_USERID
  )
  IS
    v_temp_date                    NUMBER;
    
  BEGIN

    -- 10.14.2008 EMR
    --   well, where do I start.  It appears that DB2 does not do joins well
    --   and for that our payroll was taking up to 30 minutes.  so we cache
    --   locally in oracle the core tables, and then join against those.
    --   morale of the story is leave the joins to oracle.
    
    INSERT INTO TEMP_CACHE_IQACH (
      ACH_USERID,
      ACH_TRACE_NUMBER,
      ACH_LIABCHK,
      ACH_DTPAYCHKS,
      ACH_DDAMT,
      ACH_TIMESTAMP,
      ACH_DD_FEE,
      ACH_EMPLOYEE_FEE,
      ACH_SALES_TAX
    )
    SELECT ACH_USERID,
           ACH_TRACE_NUMBER,
           ACH_LIABCHK,
           ACH_DTPAYCHKS,
           ACH_DDAMT,
           ACH_TIMESTAMP,
           ACH_DD_FEE,
           ACH_EMPLOYEE_FEE,
           ACH_SALES_TAX
      FROM DIY_IQACH
     WHERE ACH_USERID        = p_DIY_SRC_COMPANY_ID
       AND TRIM(ACH_SOURCE)  = PK_DIYDDTOPSP_CONST.gc_DIY_Ach_Source_QBooks
       AND ACH_OFFLOADED     = PK_DIYDDTOPSP_CONST.gc_DIY_No_Offload_Ind
       AND TRIM(ACH_RECTYPE) = PK_DIYDDTOPSP_CONST.gc_DIY_RecType_Normal;
       
    FOR i IN (
      SELECT ACH_TRACE_NUMBER
        FROM TEMP_CACHE_IQACH
       WHERE ACH_USERID = p_DIY_SRC_COMPANY_ID
    )
    LOOP
      
      INSERT INTO TEMP_CACHE_IQACHDD (
        ACHD_USERID, 
        ACHD_TRACE_NUMBER, 
        ACHD_SUBNUM, 
        ACHD_EMPNAME, 
        ACHD_ACCTID, 
        ACHD_ACCTTYPE, 
        ACHD_BANKID, 
        ACHD_DTPAYCHKS, 
        ACHD_AMT
      )
      SELECT ACHD_USERID, 
             ACHD_TRACE_NUMBER, 
             ACHD_SUBNUM, 
             TRIM(ACHD_EMPNAME), 
             TRIM(ACHD_ACCTID), 
             TRIM(ACHD_ACCTTYPE), 
             TRIM(ACHD_BANKID), 
             ACHD_DTPAYCHKS, 
             ACHD_AMT
        FROM DIY_IQACHDD 
       WHERE ACHD_USERID       = p_DIY_SRC_COMPANY_ID
         AND ACHD_TRACE_NUMBER = i.ACH_TRACE_NUMBER;

         
      INSERT INTO TEMP_CACHE_DXCHKXREF (
        XREF_USERID, 
        XREF_TRACE_NUMBER, 
        XREF_PAYCHKID, 
        XREF_DD_NUMBER, 
        XREF_DD_NUMBER2, 
        XREF_DD_NUMBER3, 
        XREF_DD_NUMBER4
      )
      SELECT XREF_USERID, 
             XREF_TRACE_NUMBER, 
             XREF_PAYCHKID, 
             XREF_DD_NUMBER, 
             XREF_DD_NUMBER2, 
             XREF_DD_NUMBER3, 
             XREF_DD_NUMBER4
        FROM DIY_DXCHKXREF
       WHERE XREF_USERID       = p_DIY_SRC_COMPANY_ID
         AND XREF_TRACE_NUMBER = i.ACH_TRACE_NUMBER;
         
    END LOOP;
    
    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      RAISE;
  END;
  
  
  PROCEDURE PR_PURGE_PAYROLL_CACHE (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER        -- IQCLIENT.CLI_USERID
  )
  IS
    v_temp_date                    NUMBER;
    
  BEGIN

    DELETE FROM TEMP_CACHE_DXCHKXREF
     WHERE XREF_USERID = p_DIY_SRC_COMPANY_ID;
    
    DELETE FROM TEMP_CACHE_IQACHDD
     WHERE ACHD_USERID = p_DIY_SRC_COMPANY_ID;

    DELETE FROM TEMP_CACHE_IQACH
     WHERE ACH_USERID  = p_DIY_SRC_COMPANY_ID;
    
    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      RAISE;
  END;
  
  
  PROCEDURE PR_PURGE_PAYCHECK_CACHE (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER        -- IQCLIENT.CLI_USERID
  )
  IS
    v_temp_date                    NUMBER;
    
  BEGIN

    -- 10.22.2008 EMR 
    -- This util was added to aid in the global validation.  By retaining
    -- the payroll cache, we avoid a costly join in DB2 on the AS400.

    DELETE FROM TEMP_CACHE_IQACHDD
     WHERE ACHD_USERID = p_DIY_SRC_COMPANY_ID;

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      RAISE;
  END;
  

  PROCEDURE PR_PURGE_ALL_TEMP_CACHE
  IS
    v_temp_date                    NUMBER;
    
  BEGIN

    DELETE FROM TEMP_CACHE_DXCHKXREF;
    
    DELETE FROM TEMP_CACHE_IQACHDD;

    DELETE FROM TEMP_CACHE_IQACH;
    
    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      RAISE;
  END;


END PK_DIYDDTOPSP_UTILS; 
/


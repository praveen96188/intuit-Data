CREATE OR REPLACE PACKAGE BODY PK_DIYDDTOPSP_CUSTOMER AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_CUSTOMER
   UPDATED: 10.16.2008  04:00 PM
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
   1.0        02.13.2008  EMR              Created this package body.
   1.1        06.18.2008  EMR              Added company events.
   1.2        06.23.2008  EMR              Added company v1.1 for DIY fields
   1.2.1      06.26.2008  EMR              added TRIM for as400 char fields
   1.2.2      07.01.2008  EMR              aligned psp enum values
   1.2.3      07.15.2008  EMR              modified signup date for defaults   
   1.2.4      07.23.2008  EMR              fixed events - removed if check   
   1.2.5      07.23.2008  EMR              changed company email id   
   1.2.6      07.31.2008  EMR              added count of diy payrolls to client   
   1.2.7      08.05.2008  EMR              fixing data anomolies ...
                                             fein
                                             notification email
                                             contact names   
   1.2.8      08.06.2008  EMR              fixing data anomolies ...                                             
                                             strip spaces in contact email
   1.2.9      08.07.2008  EMR              formatting contact name and email
   1.2.10     08.13.2008  EMR              added new way to extract QB token                                            
   1.2.11     09.30.2008  EMR              added strike reason and description
   1.2.12     10.01.2008  EMR              added cli high token back
   1.2.13     10.02.2008  EMR              modified sort order for strikes
   1.2.14     10.14.2008  EMR              added default company bank name.
   1.2.15     10.16.2008  EMR              modified acct subtype mapping to 
                                             deal properly with Assisted.   
******************************************************************************/

/*

  ENSURE
  ---------------------------------------------
  - cursors are closed in java

*/

  -- ------------------------------------------------------------------------
  -- Public APIs
  -- ------------------------------------------------------------------------

  -- -----------------------
  -- get basic company info
  -- -----------------------

  PROCEDURE migrateCompanyInfo (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMPANY_REFCUR               OUT refcur_company
  )
  IS
    v_company_refcur               refcur_company;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- fields that dont exist in diy and get nulls:
	--   payroll frequency cd
	--   notification email
	--   address line 3
	
	-- fields that dont exist but get defaults
	--   funding model
    
    -- 06.23.2008 added DIY fields on the as400 per UC20
    
    -- 07.01.2008 removed company status, only exists on dd service.
    --          PK_DIYDDTOPSP_UTILS.FNGetCompStatus (
    --		    TRIM(CLI_STATUS))            AS COMPANYSTATUS,
    --          PK_DIYDDTOPSP_UTILS.FNGetCompStatusEffDt (
    --		    p_DIY_COMPANY_ID)            AS STATUSEFFDATE,
    
    -- 08.05.2008 EMR
    --   changed FEIN to always be 9 digits so that it passes the PSP validation.
    
    -- 08.13.2008 changed method for getting QB token.  was ...
    --            check with Satish about his use of this table.
    --            CLI_HIGH_TOKEN      AS NEXTQBSYNCTOKEN,
    
    -- 10.01.2008 added this field back because of performance problems with
    --            SKLOG.  Discussed approach with Joe and Linda and if
    --            tokens are out of sync with QB, PSP automatically resets.
    --            CLI_HIGH_TOKEN      AS NEXTQBSYNCTOKEN,       added back
    --            PK_DIYDDTOPSP_UTILS.FNGetQBLastSyncToken (    removed
    --              p_DIY_COMPANY_ID)
    
    -- 10.16.2008 EMR
    --   modified account subtype to use tax service and status to determine
    --   the appropriate handling of terminated Assisted accounts.                       
    
    OPEN v_company_refcur
	 FOR
	   SELECT LPAD(TO_CHAR(CLI_FEIN), 9, '0')           AS FEIN,
              TRIM(CLI_LEGALNAME)                       AS LEGALNAME,
              TRIM(CLI_DBA)                             AS DBANAME,
              PK_DIYDDTOPSP_CONST.gc_Null_Str           AS PAYROLLFREQUENCYCD,
              PK_DIYDDTOPSP_UTILS.FNGetCompanyEmailID (              
                p_DIY_COMPANY_ID)                       AS NOTIFICATIONEMAIL,
              PK_DIYDDTOPSP_CONST.gc_FundingModel_2day  AS FUNDINGMODELCD,
			  PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
			    CLI_BUS_PHONE)                          AS PHONE,
        	  TRIM(CLI_ADDR1)                           AS LEGALADDRESSLINE1,
              TRIM(CLI_ADDR2)                           AS LEGALADDRESSLINE2,
              PK_DIYDDTOPSP_CONST.gc_Null_Str           AS LEGALADDRESSLINE3,
        	  TRIM(CLI_CITY)                            AS LEGALADDRESSCITY,
        	  TRIM(CLI_STATE)                           AS LEGALADDRESSSTATE,
			  PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
			    CLI_POSTALCODE)                         AS LEGALADDRESSZIP,
        	  PK_DIYDDTOPSP_CONST.gc_Null_Str           AS LEGALADDRESSZIPPLUS4,
              TRIM(CLI_REGNUM)                          AS QBREGISTRATIONNUM,
              TRIM(CLI_APPVER)                          AS QBVERSIONNUM,
              TRIM(CLI_APPID)                           AS QBAPPLICATIONID,
              TRIM(CLI_AGREE_NUM)                       AS QBAGREEMENTNUM,
              TRIM(CLI_ID)                              AS CRISROWID,
              TRIM(CLI_APPKEY)                          AS QBSERVICEKEY,
              PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                TRIM(CLI_PRICE_CODE),
                TRIM(CLI_OFFER_CODE))                   AS PSPMASTERSKU,
              PK_DIYDDTOPSP_UTILS.FNGetPSPOfferCode (
                TRIM(CLI_OFFER_CODE))                   AS PSPOFFERCODE,
              PK_DIYDDTOPSP_UTILS.FNGetPSPOfferExpDt (
                TRIM(CLI_OFFER_CODE),
                CLI_OFFER_EXPIRES)                      AS PSPOFFEREXPDTTM,
              PK_DIYDDTOPSP_UTILS.FNGetFirstPayrollDt (
                p_DIY_COMPANY_ID)                       AS DIYFIRSTPAYROLLDT,
              PK_DIYDDTOPSP_UTILS.FNGetSignUpDt (
                p_DIY_COMPANY_ID,
                CLI_ENROLLDATE)                         AS DIYSIGNUPDT,
              TRIM(CLI_TAXVER)                          AS DIYTAXTABLENUM,
              PK_DIYDDTOPSP_UTILS.FNGetFeeChartName (
                p_DIY_COMPANY_ID)                       AS QBCHARTFEENAME,
              PK_DIYDDTOPSP_UTILS.FNGetSalesTaxChartName (
                p_DIY_COMPANY_ID)                       AS QBCHARTSALESTAXNAME,
              TRIM(CLI_SALES_TAX_EXEMPT)                AS SALESTAXSTATUS,
              CLI_LAST_PAYTAX + 1                       AS NEXTPAYROLLTXNID,
              CLI_LAST_PAYCHK + 1                       AS NEXTPAYCHECKID,
              CLI_HIGH_TOKEN                            AS NEXTQBSYNCTOKEN,
              PK_DIYDDTOPSP_UTILS.FNGetEncryptedPin (
                p_DIY_COMPANY_ID,
                1)                                      AS QBPIN1,
              PK_DIYDDTOPSP_UTILS.FNGetEncryptedPin (
                p_DIY_COMPANY_ID,
                2)                                      AS QBPIN2,
              PK_DIYDDTOPSP_UTILS.FNGetEncryptedPin (
                p_DIY_COMPANY_ID,
                3)                                      AS QBPIN3,
              PK_DIYDDTOPSP_UTILS.FNGetPSPSubtype (
                p_DIY_COMPANY_ID,
                TRIM(CLI_STATUS))                       AS ACCTSUBTYPE,
              PK_DIYDDTOPSP_UTILS.FNGetPriorPayrollCnt (
                p_DIY_COMPANY_ID)                       AS AS400PAYROLLCOUNT
         FROM DIY_IQCLIENT
        WHERE CLI_USERID = p_DIY_COMPANY_ID;

	p_COMPANY_REFCUR := v_company_refcur;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error in GetCompanyMDTO getting company data. ' || SQLERRM), 1, 500);
  END;


  -- -----------------------
  -- get company contacts
  -- -----------------------

  PROCEDURE migrateContacts (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMP_CONTACT_REFCUR          OUT refcur_comp_contact,
    p_COMP_PRINC_CONTACT_REFCUR    OUT refcur_comp_princ_contact    
  )
  IS
    v_comp_contact_refcur          refcur_comp_contact;
    v_comp_princ_contact_refcur    refcur_comp_princ_contact;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- fields that dont exist in diy and get nulls:
	--   gender
	--
	-- fields that dont exist but get defaults
	--   contact preference
	--   authorized signer
	--   contact role
    
    -- 08.05.2008 EMR
    -- while doing tests with prod data it was discovered that contact names
    -- could be missing.  to accomodate we return the word empty.
    
    -- 08.07.2008 EMR
    -- abstracted formatting of contact email id and name to util lib.
    
    -- payroll admin
    OPEN v_comp_contact_refcur
	 FOR
       SELECT PK_DIYDDTOPSP_CONST.gc_Contact_By_Phone   AS CONTACT_COMM_PREF,
              PK_DIYDDTOPSP_UTILS.FNFormatEmailID (
                CLIC_EMAIL)                             AS CONTACT_EMAIL,
              PK_DIYDDTOPSP_UTILS.FNFormatContactName (  
                CLIC_FIRSTNAME)                         AS CONTACT_FIRST_NAME,
              TRIM(CLIC_MIDDLENAME)                     AS CONTACT_MIDDLE_NAME,
              PK_DIYDDTOPSP_UTILS.FNFormatContactName (  
                CLIC_LASTNAME)                          AS CONTACT_LAST_NAME,
              PK_DIYDDTOPSP_CONST.gc_Null_Str           AS CONTACT_GENDER_CD,
              PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
                CLIC_DAYPHONE)                          AS CONTACT_PHONE,
              PK_DIYDDTOPSP_CONST.gc_No_Ind             AS CONTACT_AUTH_SIGN_YN,
              PK_DIYDDTOPSP_CONST.gc_ContactRole_Admin  AS CONTACT_ROLE_CD,
              TRIM(CLIC_TITLE)                          AS CONTACT_NAME_TITLE,
              TRIM(CLIC_BUS_TITLE)                      AS CONTACT_JOB_TITLE,
              PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
                CLIC_FAX)                               AS CONTACT_FAX_NUM
         FROM DIY_IQCONTACT
        WHERE CLIC_USERID = p_DIY_COMPANY_ID;


    -- primary and secondary principle contact
    OPEN v_comp_princ_contact_refcur
	 FOR
       SELECT PK_DIYDDTOPSP_CONST.gc_Contact_By_Phone   AS CONTACT_COMM_PREF,
              PK_DIYDDTOPSP_UTILS.FNFormatEmailID (
                CLIP_EMAIL)                             AS CONTACT_EMAIL,
              PK_DIYDDTOPSP_UTILS.FNFormatContactName (  
                CLIP_FIRSTNAME)                         AS CONTACT_FIRST_NAME,
              TRIM(CLIP_MIDDLENAME)                     AS CONTACT_MIDDLE_NAME,
              PK_DIYDDTOPSP_UTILS.FNFormatContactName (  
                CLIP_LASTNAME)                          AS CONTACT_LAST_NAME,
              PK_DIYDDTOPSP_CONST.gc_Null_Str           AS CONTACT_GENDER_CD,
              PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
                CLIP_DAYPHONE)                          AS CONTACT_PHONE,
              PK_DIYDDTOPSP_CONST.gc_Yes_Ind            AS CONTACT_AUTH_SIGN_YN,
              PK_DIYDDTOPSP_UTILS.FNGetPSPContactRoleCD (
                CLIP_NUMBER)                            AS CONTACT_ROLE_CD,
              TRIM(CLIP_TITLE)                          AS CONTACT_NAME_TITLE,
              TRIM(CLIP_BUS_TITLE)                      AS CONTACT_JOB_TITLE,
              PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
                CLIP_FAX)                               AS CONTACT_FAX_NUM
         FROM DIY_IQPRINC_CONTACT
        WHERE CLIP_USERID = p_DIY_COMPANY_ID;

    /* 

    NOTE: this was the original version until the ref cursor stopped working.
          at first we thought it was due to union all.  then we found out
          that the dblink must be public.  but we already switched to two
          ref cursors. 
     
    OPEN v_comp_contact_refcur
	 FOR
       SELECT inCONTACT_COMM_PREF           AS CONTACT_COMM_PREF,
              RTRIM(inCONTACT_EMAIL)        AS CONTACT_EMAIL,
              RTRIM(inCONTACT_FIRST_NAME)   AS CONTACT_FIRST_NAME,
              RTRIM(inCONTACT_MIDDLE_NAME)  AS CONTACT_MIDDLE_NAME,
              RTRIM(inCONTACT_LAST_NAME)    AS CONTACT_LAST_NAME,
              inCONTACT_GENDER_CD           AS CONTACT_GENDER_CD,
              inCONTACT_PHONE               AS CONTACT_PHONE,
              inCONTACT_AUTH_SIGN_YN        AS CONTACT_AUTH_SIGN_YN,
              inCONTACT_ROLE_CD             AS CONTACT_ROLE_CD
         FROM (
        	   SELECT CLIP_NUMBER                    AS inCONTACT_ORDER,
        	          PK_DIYDDTOPSP_CONST.gc_Contact_By_Phone   AS inCONTACT_COMM_PREF,
                      CLIP_EMAIL                     AS inCONTACT_EMAIL,
                      CLIP_FIRSTNAME                 AS inCONTACT_FIRST_NAME,
                      CLIP_MIDDLENAME                AS inCONTACT_MIDDLE_NAME,
                      CLIP_LASTNAME                  AS inCONTACT_LAST_NAME,
                      PK_DIYDDTOPSP_CONST.gc_Null_Str           AS inCONTACT_GENDER_CD,
					  PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
	                    CLIP_DAYPHONE)               AS inCONTACT_PHONE,
                      PK_DIYDDTOPSP_CONST.gc_Yes_Ind            AS inCONTACT_AUTH_SIGN_YN,
                      PK_DIYDDTOPSP_CONST.gc_ContactRole_Owner  AS inCONTACT_ROLE_CD
                 FROM DIY_IQPRINC_CONTACT
                WHERE CLIP_USERID = p_DIY_COMPANY_ID
        	    UNION ALL
        	   SELECT 10                             AS inCONTACT_ORDER,
        	          PK_DIYDDTOPSP_CONST.gc_Contact_By_Phone   AS inCONTACT_COMM_PREF,
                      CLIC_EMAIL                     AS inCONTACT_EMAIL,
                      CLIC_FIRSTNAME                 AS inCONTACT_FIRST_NAME,
                      CLIC_MIDDLENAME                AS inCONTACT_MIDDLE_NAME,
                      CLIC_LASTNAME                  AS inCONTACT_LAST_NAME,
                      PK_DIYDDTOPSP_CONST.gc_Null_Str           AS inCONTACT_GENDER_CD,
					  PK_DIYDDTOPSP_UTILS.FNGetOnlyNumbers (
			            CLIC_DAYPHONE)               AS inCONTACT_PHONE,
                      PK_DIYDDTOPSP_CONST.gc_No_Ind             AS inCONTACT_AUTH_SIGN_YN,
                      PK_DIYDDTOPSP_CONST.gc_ContactRole_Admin  AS inCONTACT_ROLE_CD
                 FROM DIY_IQCONTACT
                WHERE CLIC_USERID = p_DIY_COMPANY_ID
              )
        ORDER BY inCONTACT_ORDER;
        
    */

	p_COMP_CONTACT_REFCUR       := v_comp_contact_refcur;
	p_COMP_PRINC_CONTACT_REFCUR := v_comp_princ_contact_refcur;    

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error in migrateContacts getting company contact data. ' || SQLERRM), 1, 500);
  END;


  -- ------------------------------------------------------------------
  -- get company direct deposit setup information
  -- ------------------------------------------------------------------

  PROCEDURE migrateDDService (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMPANY_REFCUR               OUT refcur_company
  )
  IS
    v_company_refcur               refcur_company;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- fields that dont exist in diy and get nulls:
	--   override ee limit
	--
	-- fields that dont exist but get defaults
	--   offload group
	--   consecutive limit violation

    OPEN v_company_refcur
	 FOR
	   SELECT PK_DIYDDTOPSP_UTILS.FNGetEROverrideLimitAmt (
	            p_DIY_COMPANY_ID)                      AS OVERRIDECOMPANYLIMITAMOUNT,
			  PK_DIYDDTOPSP_UTILS.FNGetEEOverrideLimitAmt (
	            p_DIY_COMPANY_ID)                      AS OVERRIDEEMPLOYEELIMITAMOUNT,
              PK_DIYDDTOPSP_CONST.gc_OffloadGroup_Std  AS OFFLOADGROUPCD,
			  PK_DIYDDTOPSP_UTILS.FNGetDDCompStatus (
			    TRIM(CLI_STATUS))                      AS DDSERVICESTATUS,
			  PK_DIYDDTOPSP_UTILS.FNGetDDCompStatusEffDt (
			    p_DIY_COMPANY_ID)                      AS STATUSEFFECTIVEDATE,
              PK_DIYDDTOPSP_CONST.gc_Zero_Count        AS CONSEQLIMITVIOLATIONCOUNT
         FROM DIY_IQCLIENT
        WHERE CLI_USERID = p_DIY_COMPANY_ID;

	p_COMPANY_REFCUR := v_company_refcur;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error in GetDDCompanyMDTO getting company data. ' || SQLERRM), 1, 500);
  END;


  -- ------------------------------------------------------------------
  -- get company bank account information
  -- ------------------------------------------------------------------

  PROCEDURE migrateCompanyBankAccounts (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMP_BANKACCT_REFCUR         OUT refcur_comp_bankacct
  )
  IS
    v_comp_bankacct_refcur         refcur_comp_bankacct;
	v_temp_eff_date                DATE;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

	-- initialize
	v_temp_eff_date := PK_DIYDDTOPSP_UTILS.FNGetCompBankAcctEffDt (
			             p_DIY_COMPANY_ID);

    -- fields that dont exist in diy and get nulls:
	--   expiration date
	--
	-- fields that dont exist but get defaults
	--   source id
	--   status
	--   retry counts
    
    -- 10.14.2008 EMR
    --   added default bank name.  was ...
    --   TRIM(CLI_BANKNAME)                         AS BANKNAME,

    OPEN v_comp_bankacct_refcur
	 FOR
       SELECT PK_DIYDDTOPSP_CONST.gc_Default_Src_CBA_ID  AS SRC_COMP_BANK_ACCT_ID,
	          TRIM(CLI_ACCTID)                           AS ACCOUNTNUMBER,
              DECODE (
			    TRIM(CLI_ACCTTYPE),
				PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Chk,
				PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Chk,
				PK_DIYDDTOPSP_CONST.gc_DIY_BankAcctType_Sav,
				PK_DIYDDTOPSP_CONST.gc_PSP_BankAcctType_Sav
			  )                                          AS ACCOUNTTYPECD,
       	      DECODE (
                TRIM(CLI_BANKNAME),
                PK_DIYDDTOPSP_CONST.gc_Null_Str, 
                PK_DIYDDTOPSP_CONST.gc_Default_Bank_Name,
                TRIM(CLI_BANKNAME)
              )                                          AS BANKNAME,
              TRIM(CLI_BANKID)                           AS ROUTINGNUMBER,
			  v_temp_eff_date                            AS BAEFFECTIVEDATE,
			  PK_DIYDDTOPSP_CONST.gc_No_Date             AS BAEXPIRATIONDATE,
			  v_temp_eff_date                            AS CBAEFFECTIVEDATE,
			  PK_DIYDDTOPSP_CONST.gc_No_Date             AS CBAEXPIRATIONDATE,
			  PK_DIYDDTOPSP_CONST.gc_Bank_Status_Active  AS BANKACCOUNTSTATUS,
			  v_temp_eff_date                            AS BASTATUSEFFECTIVEDATE,
			  PK_DIYDDTOPSP_CONST.gc_Zero_Count          AS VERIFYRETRYCOUNT,
			  PK_DIYDDTOPSP_CONST.gc_Zero_Count          AS TOTALRETRYCOUNT,
              TRIM(CLI_ACCTNAME)                         AS QBCHARTBANKACCTNAME
         FROM DIY_IQCLIENT
        WHERE CLI_USERID = p_DIY_COMPANY_ID;

    p_COMP_BANKACCT_REFCUR := v_comp_bankacct_refcur;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error in GetCompanyBankAcctMDTO getting company bank account data. ' || SQLERRM), 1, 500);
  END;


  -- ------------------------------------------------------------------
  -- get company events
  -- ------------------------------------------------------------------

  PROCEDURE migrateCompanyEvents (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_COMP_STRIKE_COUNT            OUT NUMBER,
    p_COMP_EVENTS_REFCUR           OUT refcur_comp_events
  )
  IS
    v_comp_events_refcur           refcur_comp_events;
    v_strike_count                 NUMBER;

  BEGIN

    -- DESIGN NOTE
    --  
    --   currently there is only 1 event to migrate - strikes.
    --   need to determine what details to retrieve.
    --   anticipate no details for event counter
    --
    --   so now a word about strikes.  There is a counter that stores the
    --   active number of stikes, then the details are in a child table. 
    --   need to anticipate counter greater than 0 but no child records.   

    -- IOP query for reference
    --
    -- SELECT COMP_EVENT_GSEQ, 
    --        EVENT_TYPE_CD, 
    --        STATUS_CD, 
    --        STATUS_EFF_DTTM, 
    --        EVENT_TIMESTAMP, 
    --   FROM COMP_EVENT 
    --  WHERE COMPANY_GSEQ = :1 
    --  ORDER 
    --     BY COMP_EVENT_GSEQ 


    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

	-- initialize
    v_strike_count := 0;
    
    SELECT CLI_STRIKE_COUNT
      INTO v_strike_count
      FROM DIY_IQCLIENT
     WHERE CLI_USERID = p_DIY_COMPANY_ID;

    p_COMP_STRIKE_COUNT := v_strike_count;

    -- 09.30.3008 EMR
    -- added strike reason and description to match new event model in PSP.
    -- also rewrote query for better performance with the gateway.

    /* V1
    
    OPEN v_comp_events_refcur
     FOR
       SELECT EV_COMP_EVENT_GSEQ,
              EV_EVENT_TYPE_CD,
              EV_STATUS_CD,
              EV_STATUS_EFF_DTTM,
              EV_EVENT_TIMESTAMP
         FROM (
               SELECT ROWNUM                  AS inRowNum,
                      EV_COMP_EVENT_GSEQ,
                      EV_EVENT_TYPE_CD,
                      EV_STATUS_CD,
                      EV_STATUS_EFF_DTTM,
                      EV_EVENT_TIMESTAMP,
                      EV_EVENT_STRIKE_REASON
                 FROM (
                       SELECT PK_DIYDDTOPSP_UTILS.FNGetNextGseq          AS EV_COMP_EVENT_GSEQ,
                              PK_DIYDDTOPSP_CONST.gc_PSP_Event_Strike_CD AS EV_EVENT_TYPE_CD,
                              PK_DIYDDTOPSP_CONST.gc_PSP_Event_Status_CD AS EV_STATUS_CD,
                              TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS EV_STATUS_EFF_DTTM,
                              TO_DATE(EVE_TIMESTAMP, 'YYYYMMDDHH24MISS') AS EV_EVENT_TIMESTAMP,
                              TRIM(EVE_REASONS)                          AS EV_EVENT_STRIKE_REASON
                         FROM DIY_IQEVENT
                        WHERE EVE_USERID = p_DIY_COMPANY_ID
                          AND EVE_CODE   = PK_DIYDDTOPSP_CONST.gc_DIY_EVENT_TYPE_STRIKE
                        ORDER
                           BY EVE_TIMESTAMP ASC
                      )
              )
        WHERE inRowNum <= v_strike_count;
        
    */

    -- 10.02.2008 EMR
    -- changed the sort order to grab the latest strikes not the oldest ones.

    OPEN v_comp_events_refcur
     FOR
       SELECT EV_COMP_EVENT_GSEQ,
              EV_EVENT_TYPE_CD,
              EV_STATUS_CD,
              EV_STATUS_EFF_DTTM,
              EV_EVENT_TIMESTAMP,
              EV_EVENT_STRIKE_REASON,
              EV_EVENT_STRIKE_REASON_DESCR
         FROM (
               SELECT ROWNUM                                          AS inRowNum,
                      PK_DIYDDTOPSP_UTILS.FNGetNextGseq               AS EV_COMP_EVENT_GSEQ,
                      PK_DIYDDTOPSP_CONST.gc_PSP_Event_Strike_CD      AS EV_EVENT_TYPE_CD,
                      PK_DIYDDTOPSP_CONST.gc_PSP_Event_Status_CD      AS EV_STATUS_CD,
                      TO_DATE(EV_STATUS_EFF_DTTM, 'YYYYMMDDHH24MISS') AS EV_STATUS_EFF_DTTM,
                      TO_DATE(EV_EVENT_TIMESTAMP, 'YYYYMMDDHH24MISS') AS EV_EVENT_TIMESTAMP,
                      PK_DIYDDTOPSP_UTILS.FNGetPSPStrikeReason (
                        EV_EVENT_STRIKE_REASON)                       AS EV_EVENT_STRIKE_REASON,
                      PK_DIYDDTOPSP_CONST.gc_DIY_Event_Strike_Descr   AS EV_EVENT_STRIKE_REASON_DESCR  
                 FROM (
                       SELECT /*+ NO_MERGE */
                              EVE_TIMESTAMP     AS EV_STATUS_EFF_DTTM,
                              EVE_TIMESTAMP     AS EV_EVENT_TIMESTAMP,
                              TRIM(EVE_REASONS) AS EV_EVENT_STRIKE_REASON
                         FROM DIY_IQEVENT
                        WHERE EVE_USERID         = p_DIY_COMPANY_ID
                          AND EVE_CODE           = PK_DIYDDTOPSP_CONST.gc_DIY_EVENT_TYPE_STRIKE
                          AND TRIM(EVE_REASONS) <> PK_DIYDDTOPSP_CONST.gc_DIY_StrkRsn_Del
                        ORDER
                           BY EVE_TIMESTAMP DESC
                      )
              )
        WHERE inRowNum <= v_strike_count;
    
    p_COMP_EVENTS_REFCUR := v_comp_events_refcur;
        
  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := SUBSTR ( ('Error in migrateCompanyEvents getting company event data. ' || SQLERRM), 1, 500);
  END;


END PK_DIYDDTOPSP_CUSTOMER; 
/


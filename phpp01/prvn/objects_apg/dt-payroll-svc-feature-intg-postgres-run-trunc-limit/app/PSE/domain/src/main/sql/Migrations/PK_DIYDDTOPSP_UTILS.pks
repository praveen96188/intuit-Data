CREATE OR REPLACE PACKAGE PK_DIYDDTOPSP_UTILS AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_UTILS
   UPDATED: 10.22.2008  10:00 AM  
   PURPOSE: A library of common functions.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.26.2008  EMR              Created functions for customer
   1.1        06.19.2008  EMR              Added support for events and 
                                             employees.
   1.2        06.23.2008  EMR              Add CompanyInfo v1.1
   1.3        06.24.2008  EMR              modified company status
   1.4        06.25.2008  EMR              added generic sequence generator
   1.4.1      06.26.2008  EMR              added functions for paycheck split
   1.4.2      06.27.2008  EMR              added functions for txns                                                 
   1.4.3      07.01.2008  EMR              aligned psp enums
   1.4.4      07.09.2008  EMR              modified ee bank eff date   
   1.4.5      07.18.2008  EMR              used new range for raise app err
   1.4.6      07.23.2008  EMR              added function for comp email      
   1.4.7      07.29.2008  EMR              fixed paycheck source id   
   1.4.8      07.31.2008  EMR              added count of diy payrolls to client   
   1.4.9      08.07.2008  EMR              added formatting apis   
   1.4.10     08.13.2008  EMR              changed source table for token
   1.4.11     09.30.2008  EMR              added conversion of strike reason
   1.4.12     10.09.2008  EMR              changed FNGetEmpSrcBankID params from
                                             number to varchar
   1.4.12     10.14.2008  EMR              added new local cache for payrolls                                             
   1.4.13     10.16.2008  EMR              modified FNGetPSPSubtype to include
                                             status code.
   1.4.14     10.22.2008  EMR              added new temp cache purge utils to
                                             help global validation performance.
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- PACKAGE TYPE DEFINITIONS
  -- ------------------------------------------------------------------------

  pc_raise_app_err_cd              CONSTANT NUMBER       := -20054;  


  FUNCTION FNGetNextGseq
  RETURN NUMBER;
  

  FUNCTION FNGetCompStatus (
    p_AS400_STATUS_CD              IN  VARCHAR2
  ) RETURN VARCHAR2;


  FUNCTION FNGetCompStatusEffDt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN DATE;


  FUNCTION FNGetCompanyEmailID (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN VARCHAR2;


  FUNCTION FNGetOnlyNumbers (
    p_CHAR_ITEM                    IN  VARCHAR2
  ) RETURN VARCHAR2;
  

  FUNCTION FNGetPSPContactRoleCD (
    p_AS400_PRINCIPAL_NUMBER       IN  NUMBER
  ) RETURN VARCHAR2;  


  FUNCTION FNGetEROverrideLimitAmt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN NUMBER;


  FUNCTION FNGetEEOverrideLimitAmt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN NUMBER;


  FUNCTION FNGetDDCompStatus (
    p_AS400_STATUS_CD              IN  VARCHAR2
  ) RETURN VARCHAR2;


  FUNCTION FNGetDDCompStatusEffDt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN DATE;


  FUNCTION FNGetCompBankAcctEffDt (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN DATE;


  FUNCTION FNGetEmployeeName (
    p_NAME_PART_CD                 IN   VARCHAR2,
    p_NAME                         IN   VARCHAR2
  )	RETURN VARCHAR2;
  

  /* V1 10.09.2008 EMR
  FUNCTION FNGetEmpSrcBankID (
    p_EE_RTN_NUM                   IN   NUMBER,
    p_EE_ACCT_NUM                  IN   NUMBER
  )	RETURN VARCHAR2;
  */
  
  FUNCTION FNGetEmpSrcBankID (
    p_EE_RTN_NUM                   IN   VARCHAR2,
    p_EE_ACCT_NUM                  IN   VARCHAR2
  )	RETURN VARCHAR2;
  

  FUNCTION FNGetEmpBankEffDttm (
    p_COMPANY_DB_ID                IN   NUMBER
  ) RETURN DATE;


  FUNCTION FNGetPaycheckSourceID (
    p_COMPANY_DB_ID                IN   NUMBER,  
    p_PAYRUN_DB_ID                 IN   NUMBER,
    p_PAYCHECK_OFFSET_NUM          IN   NUMBER
  ) RETURN VARCHAR2;
  

  FUNCTION FNGetDDTxnSourceID (
    p_PAYRUN_DB_ID                 IN   NUMBER,
    p_PAYCHECK_OFFSET_NUM          IN   NUMBER
  ) RETURN VARCHAR2;


  FUNCTION FNGetPSPMasterSKU (
    p_DIY_PRICE_CODE               IN  VARCHAR2,
    p_DIY_OFFER_CODE               IN  VARCHAR2
  ) RETURN VARCHAR2;
  
  
  FUNCTION FNGetPSPOfferCode (
    p_DIY_OFFER_CODE               IN  VARCHAR2
  ) RETURN VARCHAR2;
  

  FUNCTION FNGetPSPOfferExpDt (
    p_DIY_OFFER_CODE               IN  VARCHAR2,
    p_DIY_OFFER_EXP_DT             IN  VARCHAR2
  ) RETURN DATE;
 

  FUNCTION FNGetFirstPayrollDt (
     p_COMPANY_DB_ID               IN   NUMBER
  ) RETURN DATE;
   

  FUNCTION FNGetSignUpDt (
    p_DIY_COMPANY_ID               IN  NUMBER,       -- DB KEY ON AS400  
    p_SIGNUP_DATE                  IN  NUMBER
  ) RETURN DATE;


  FUNCTION FNGetFeeChartName (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN VARCHAR2;
  
  
  FUNCTION FNGetSalesTaxChartName (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN VARCHAR2;
  
  
  FUNCTION FNGetQBLastSyncToken (
    p_DIY_COMPANY_ID               IN  NUMBER        -- DB KEY ON AS400
  ) RETURN NUMBER;  


  FUNCTION FNGetEncryptedPin (
    p_DIY_COMPANY_ID               IN  NUMBER,        -- DB KEY ON AS400
    p_PIN_NUM                      IN  NUMBER
  ) RETURN VARCHAR2;    


  FUNCTION FNGetPSPSubtype (
    p_DIY_COMPANY_ID               IN  VARCHAR2,
    p_AS400_STATUS_CD              IN  VARCHAR2
  ) RETURN VARCHAR2;


  PROCEDURE PRGetAS400PaycheckID (
    p_COMPANY_DB_ID                IN  NUMBER,  
    p_SOURCE_PAYCHECK_ID           IN  NUMBER,
    p_AS400_PAYRUN_ID              OUT NUMBER,
    p_AS400_PAYCHECK_ID1           OUT NUMBER,
    p_AS400_PAYCHECK_ID2           OUT NUMBER,
    p_AS400_PAYCHECK_ID3           OUT NUMBER,
    p_AS400_PAYCHECK_ID4           OUT NUMBER           
  ); 


  FUNCTION FNGetPayrunInsDttm (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_PAYROLL_RUN_ID               IN  NUMBER
  ) RETURN DATE;
  

  FUNCTION FNGetERDebitAmt (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_PAYROLL_RUN_ID               IN  NUMBER
  ) RETURN NUMBER;
  

  FUNCTION FNGetTxnStatusCD (
    p_TXN_AMT                      IN  NUMBER
  ) RETURN VARCHAR2;
  

  FUNCTION FNGetPriorPayrollCnt (
    p_DIY_COMPANY_ID               IN  NUMBER
  ) RETURN NUMBER;


  FUNCTION FNGetPSPStrikeReason (
    p_AS400_STRIKE_REASON          IN  VARCHAR2
  ) RETURN VARCHAR2;
  
  
  --
  -- THESE ARE SPECIAL FORMATTING FUNCTIONS TO ACCOMODATE DATA ANOMOLIES
  --

  FUNCTION FNFormatEmailID (
    p_EMAILID                      IN  VARCHAR2
  ) RETURN VARCHAR2;
  
  
  FUNCTION FNFormatContactName (
    p_CONTACT_NAME                 IN  VARCHAR2
  ) RETURN VARCHAR2;


  --
  -- THESE ARE SPECIAL FUNCTIONS TO CONTROL LOCAL CACHING FOR PERFORMANCE BOOST.
  --

  PROCEDURE PR_CREATE_PAYROLL_CACHE (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER        -- IQCLIENT.CLI_USERID
  );
  
  PROCEDURE PR_PURGE_PAYROLL_CACHE (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER        -- IQCLIENT.CLI_USERID
  );  

  PROCEDURE PR_PURGE_PAYCHECK_CACHE (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER        -- IQCLIENT.CLI_USERID
  );
  
  PROCEDURE PR_PURGE_ALL_TEMP_CACHE;
  

END PK_DIYDDTOPSP_UTILS; 
/


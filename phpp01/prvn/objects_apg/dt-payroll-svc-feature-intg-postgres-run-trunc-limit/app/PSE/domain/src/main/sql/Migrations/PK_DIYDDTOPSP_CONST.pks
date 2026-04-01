CREATE OR REPLACE PACKAGE PK_DIYDDTOPSP_CONST AS
/******************************************************************************
   NAME:    PK_CONST
   UPDATED: 10.14.2008  03:00 PM  
   PURPOSE:	global constants

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.25.2008  EMR			   Created migration state engine
   1.1        06.19.2008  EMR              Added support for events and 
                                           employees.
   1.2        07.01.2008  EMR              aligned psp enums
   1.2.1      07.11.2008  EMR              renamed to make 1 mig package
   1.2.2      07.11.2008  EMR              add general constants from PSE
   1.2.3      07.15.2008  EMR              added new bank active code
   1.2.4      07.18.2008  EMR              added as400 mig table status codes   
   1.2.5      07.23.2008  EMR              added PSP email constant   
   1.2.6      07.29.2008  EMR              fixed subtype mapping   
   1.2.7      07.29.2008  EMR              added defaults for missing data
                                             notification email
                                             contact name   
   1.2.8      08.13.2008  EMR              added default for default token                                             
   1.2.9      09.10.2008  EMR              changed default email id to null
   1.2.10     09.30.2008  EMR              added strike reason description
   1.2.11     09.30.2008  EMR              filter out deleted strikes
   1.2.12     10.14.2008  EMR              added default company bank name
******************************************************************************/

  -- ------------------------------------------------------------------------
  -- Migration State Engine
  -- ------------------------------------------------------------------------
  gc_Idle_StateCD             CONSTANT VARCHAR2(10) := 'IDL';  -- not ready to migrate
  gc_Ready_StateCD            CONSTANT VARCHAR2(10) := 'RDY';  -- ready to migrate
  gc_Migrating_StateCD        CONSTANT VARCHAR2(10) := 'MIG';  -- moving company
  gc_Validating_StateCD       CONSTANT VARCHAR2(10) := 'VAL';  -- validating what moved
  gc_Syncing_StateCD          CONSTANT VARCHAR2(10) := 'SYNC';  -- one last sync w as400  
  gc_Complete_StateCD         CONSTANT VARCHAR2(10) := 'CP' ;  -- successful migration
  gc_Error_StateCD            CONSTANT VARCHAR2(10) := 'ERR';  -- unsuccessful migration

  
  -- ------------------------------------------------------------------------
  -- Other Control Constants
  -- ------------------------------------------------------------------------
  
  gc_DD_SERVICE_IOP           CONSTANT VARCHAR2(10) := 'QBOE';
  gc_DD_SERVICE_DIY           CONSTANT VARCHAR2(10) := 'DIY';
  gc_TRUE                     CONSTANT VARCHAR2(10) := 'TRUE';
  gc_FALSE                    CONSTANT VARCHAR2(10) := 'FALSE';
  
  gc_GMT_timezone             CONSTANT VARCHAR2(10) := 'GMT';
  gc_PST_timezone             CONSTANT VARCHAR2(10) := 'PST';  -- standard time
  gc_PDT_timezone             CONSTANT VARCHAR2(10) := 'PDT';  -- daylight savings time  


  -- ------------------------------------------------------------------------
  -- Global Constants
  -- ------------------------------------------------------------------------
  --                                                    measuring stick
  --                                                             1         2         3
  --                                                    123456789012345678901234567890

  -- internal package code sets
  gc_Name_First_CD            CONSTANT VARCHAR2(20) := 'F';
  gc_Name_Middle_CD           CONSTANT VARCHAR2(20) := 'M';
  gc_Name_Last_CD             CONSTANT VARCHAR2(20) := 'L';
  gc_SourceID_Delimiter_CD    CONSTANT VARCHAR2(20) := '-';   

  -- these must match the ENUMs in PSP

  gc_Null_Str                 CONSTANT VARCHAR2(20) := '';
  gc_Yes_Ind                  CONSTANT VARCHAR2(20) := 'Y';
  gc_No_Ind                   CONSTANT VARCHAR2(20) := 'N';
  gc_OffloadGroup_Std         CONSTANT VARCHAR2(20) := 'STD';
  gc_Zero_Count               CONSTANT NUMBER       := 0;
  gc_Null_Count               CONSTANT NUMBER       := NULL;
  gc_Default_Src_CBA_ID       CONSTANT VARCHAR2(20) := '1';  -- company bank account
  gc_No_Date                  CONSTANT DATE         := NULL;

  gc_FundingModel_2day        CONSTANT VARCHAR2(30) := '2D';
  gc_Contact_By_Phone         CONSTANT VARCHAR2(30) := 'Phone';
  gc_ContactRole_Admin        CONSTANT VARCHAR2(30) := 'PayrollAdmin';
  gc_ContactRole_PriPrinc     CONSTANT VARCHAR2(30) := 'PrimaryPrincipal';
  gc_ContactRole_SecPrinc     CONSTANT VARCHAR2(30) := 'SecondaryPrincipal';
  gc_Bank_Status_Active       CONSTANT VARCHAR2(30) := 'Active';  
  gc_PSP_BankAcctType_Chk     CONSTANT VARCHAR2(30) := 'Checking';
  gc_PSP_BankAcctType_Sav     CONSTANT VARCHAR2(30) := 'Savings';
  gc_PSP_Event_Strike_CD      CONSTANT VARCHAR2(30) := 'Strike';
  gc_PSP_Event_Status_CD      CONSTANT VARCHAR2(30) := 'Active';
  gc_PSP_StrkRsn_AchRej       CONSTANT VARCHAR2(30) := 'DebitReturned';
  gc_PSP_StrkRsn_Manual       CONSTANT VARCHAR2(30) := 'Manual';
  gc_PSP_Employee_Status_CD   CONSTANT VARCHAR2(30) := 'Active'; 
  gc_PSP_BankAcct_Status_CD   CONSTANT VARCHAR2(30) := 'Active';
  gc_PSP_Payrun_Status_CD     CONSTANT VARCHAR2(30) := 'Pending';
  gc_PSP_SKU_STD3             CONSTANT VARCHAR2(30) := 'DIYDDSTD-3';
  gc_PSP_SKU_STD              CONSTANT VARCHAR2(30) := 'DIYDD-STD';
  gc_Default_Offer_Exp_Dt     CONSTANT VARCHAR2(30) := '20090630';
  gc_Default_ChartAcct_Fee    CONSTANT VARCHAR2(30) := 'Payroll Expenses';  
  gc_Default_ChartAcct_Tax    CONSTANT VARCHAR2(30) := 'Payroll Expenses';
  gc_Default_Subtype          CONSTANT VARCHAR2(30) := 'Standard';  -- match psp enum
  gc_Default_LastSync_Token   CONSTANT NUMBER       := 0;  -- the beginning of time
  gc_Default_Bank_Name        CONSTANT VARCHAR2(30) := 'No Bank Name Provided';  
  
  gc_PSP_ER_Status_Termed     CONSTANT VARCHAR2(30) := 'Terminated';
  gc_PSP_ER_Status_Active     CONSTANT VARCHAR2(30) := 'ActiveCurrent';
  
  gc_PSP_ER_Debit_Txn_CD      CONSTANT VARCHAR2(30) := 'EmployerDdDebit';
  gc_PSP_EE_Credit_Txn_CD     CONSTANT VARCHAR2(30) := 'EmployeeDdCredit';
  gc_PSP_FeeDD_Debit_Txn_CD   CONSTANT VARCHAR2(30) := 'EmployerFeeDebitDD';
  gc_PSP_FeeTR_Debit_Txn_CD   CONSTANT VARCHAR2(30) := 'EmployerFeeDebitTransmission';
  gc_PSP_Tax_Debit_Txn_CD     CONSTANT VARCHAR2(30) := 'ServiceSalesAndUseTax';
  gc_PSP_Txn_Cr_State_CD      CONSTANT VARCHAR2(30) := 'Pending';
  gc_PSP_Txn_Cancel_State_CD  CONSTANT VARCHAR2(30) := 'Canceled';  
  gc_PSP_Txn_Sttlmnt_Type_CD  CONSTANT VARCHAR2(30) := 'ACH';          


  -- enums in DIY on the AS400

  gc_DIY_BankAcctType_Chk     CONSTANT VARCHAR2(20) := 'CHECKING';
  gc_DIY_BankAcctType_Sav     CONSTANT VARCHAR2(20) := 'SAVINGS';
  gc_DIY_LimitType_Company    CONSTANT VARCHAR2(20) := 'DDLIMIT6';
  gc_DIY_LimitType_Employee   CONSTANT VARCHAR2(20) := 'DDLIMIT14';
  gc_DIY_LimitAmt_Company     CONSTANT NUMBER       := 40000;
  gc_DIY_LimitAmt_Employee    CONSTANT NUMBER       := 15000;  

  gc_DIY_Ach_Source_Manual    CONSTANT VARCHAR2(20) := 'M';    -- manual txn from rep
  gc_DIY_Ach_Source_QBooks    CONSTANT VARCHAR2(20) := 'Q';    -- quickbooks
  gc_DIY_No_Offload_Ind       CONSTANT NUMBER       := 0;      -- offloaded is a date
  gc_DIY_EVENT_TYPE_STRIKE    CONSTANT VARCHAR2(20) := 'STRK'; -- strike
  gc_DIY_Event_Strike_Descr   CONSTANT VARCHAR2(99) := 'MIGRATED FROM AS400';
  gc_DIY_StrkRsn_Del          CONSTANT VARCHAR2(20) := '*DEL'; -- deleted strike
  gc_DIY_StrkRsn_AchRej       CONSTANT VARCHAR2(30) := 'ACHREJECT';
  gc_DIY_StrkRsn_Manual       CONSTANT VARCHAR2(30) := 'MANUALSTRK';
  gc_ChartAcct_Fee_Type       CONSTANT VARCHAR2(20) := 'FEEACCTNAME';
  gc_ChartAcct_Tax_Type       CONSTANT VARCHAR2(20) := 'SALESTAXACCTNAME';
  gc_DIY_ER_Status_Termed     CONSTANT VARCHAR2(20) := 'TI';   -- terminated  
  gc_DIY_ER_Status_Active     CONSTANT VARCHAR2(20) := 'A';    -- active
  gc_DIY_RecType_Normal       CONSTANT VARCHAR2(20) := ' ';    -- normal
  gc_DIY_RecType_Stripped     CONSTANT VARCHAR2(20) := 'S';    -- payroll stripped
  gc_DIY_RecType_Hold         CONSTANT VARCHAR2(20) := 'H';    -- ach hold
  gc_DIY_RecType_Test         CONSTANT VARCHAR2(20) := 'T';    -- not used in prod
  
  gc_DIY_No_Name              CONSTANT VARCHAR2(50) := '<EMPTY>';  -- when no name is found
  gc_DIY_No_Email             CONSTANT VARCHAR2(50) := NULL;       -- when no name is found
  
  gc_PSPMIGQUE_Ready_CD       CONSTANT VARCHAR2(20) := 'R';    -- ready to migrate
  gc_PSPMIGQUE_Complete_CD    CONSTANT VARCHAR2(20) := 'C';    -- completed migration
  gc_PSPMIGQUE_Error_CD       CONSTANT VARCHAR2(20) := 'E';    -- error with migration


  -- these must match the iqevent code set on the AS400

  gc_event_cd_signup          CONSTANT VARCHAR2(20) := 'SNUP';
  gc_event_cd_bnkacct_vfy     CONSTANT VARCHAR2(20) := 'BNKV';
  gc_event_cd_bnkacct_actv    CONSTANT VARCHAR2(20) := 'BNKA';  
  gc_event_cd_active          CONSTANT VARCHAR2(20) := 'ACTV';


END PK_DIYDDTOPSP_CONST; 
/


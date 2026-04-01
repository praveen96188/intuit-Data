CREATE OR REPLACE PACKAGE BODY PK_TEST_DIY_MIGRATION_APIS AS
/******************************************************************************
   NAME:    PK_TEST_DIY_MIGRATION_APIS
   UPDATED: 11.26.2008 09:00 AM
   PURPOSE: test cases for all company apis.  uses ref cursors.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        03.03.2008  EMR              created test cases for pass 1 and 2
   1.1        06.24.2008  EMR              updated for slice 8 work
   1.1.1      06.25.2008  EMR              fixes for slice 8 work   
   1.1.2      07.15.2008  EMR              added support for looping all data   
   1.2        09.15.2008  EMR              changed package name
   1.2.1      09.15.2008  EMR              added time markers for test all apis
   1.2.2      10.14.2008  EMR              modified to use local payroll cache
   1.2.3      11.26.2008  EMR              added test api for Master Price SKU   
******************************************************************************/

  PROCEDURE ResetCompany (
    p_COMPANY_ID                   IN VARCHAR2
  )
  IS
  BEGIN

    UPDATE COMPANY_MIGRATION
	   SET Migration_State_CD   = PK_DIYDDTOPSP_CONST.gc_Ready_StateCD
	 WHERE SOURCE_DB_COMPANY_ID = p_COMPANY_ID;

	COMMIT;

  END;


  PROCEDURE TestAllApis (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    v_DIY_COMPANY_ID               NUMBER;  
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(1000);
    
    p_DIY_SOURCE_EMP_ID            VARCHAR2(100);
    p_SOURCE_PAYCHECK_ID           VARCHAR2(100);

  BEGIN

    IF (p_DIY_COMPANY_ID IS NOT NULL) THEN
      v_DIY_COMPANY_ID := p_DIY_COMPANY_ID;
    ELSE
      PK_DIYDDTOPSP_CONTROLLER.GETCOMPANYTOMIGRATE (
	    P_RETURN_CD,
	    P_RETURN_MSG,
	    v_DIY_COMPANY_ID
	  );
    END IF;
    
    PK_DIYDDTOPSP_UTILS.PR_PURGE_PAYROLL_CACHE (
      v_DIY_COMPANY_ID
    );
    
    COMMIT;
    

    DBMS_OUTPUT.PUT_LINE ('DIY Company ID : ' || v_DIY_COMPANY_ID);
    

    DBMS_OUTPUT.PUT_LINE ('Get Company Info : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));
     
    GetCompany (v_DIY_COMPANY_ID);

    
    DBMS_OUTPUT.PUT_LINE ('Get Contacts : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));

	GetContacts (v_DIY_COMPANY_ID);


    DBMS_OUTPUT.PUT_LINE ('Get DD Service Info : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));
    
	GetDDInfo (v_DIY_COMPANY_ID);


    DBMS_OUTPUT.PUT_LINE ('Get Company Bank Account : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));

	GetBankAcct (v_DIY_COMPANY_ID);
    

    DBMS_OUTPUT.PUT_LINE ('Get Events : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));    

	GetEvents (v_DIY_COMPANY_ID);


    DBMS_OUTPUT.PUT_LINE ('Get Employees : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));
    
	GetEmployees (v_DIY_COMPANY_ID);
    
    
    DBMS_OUTPUT.PUT_LINE ('Get Employee Bank Accounts : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));

    FOR i IN (
      SELECT DISTINCT
             TRIM(ACHD_EMPNAME) AS EMP_SRC_ID
        FROM TEMP_CACHE_IQACHDD
       WHERE ACHD_USERID        = v_DIY_COMPANY_ID
    )
    LOOP

      GetEEBankAcct (v_DIY_COMPANY_ID, i.EMP_SRC_ID);

    END LOOP;


    DBMS_OUTPUT.PUT_LINE ('Get Payruns : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));
    
	GetPayruns (v_DIY_COMPANY_ID);


    DBMS_OUTPUT.PUT_LINE ('Get Paychecks : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));

    FOR i IN (
      SELECT TO_CHAR(ACH_LIABCHK)  AS PAYROLL_RUN_SRC_ID
        FROM TEMP_CACHE_IQACH
       WHERE ACH_USERID        = v_DIY_COMPANY_ID
    )
    LOOP

      GetPaychecks (v_DIY_COMPANY_ID, i.PAYROLL_RUN_SRC_ID);

    END LOOP;


    DBMS_OUTPUT.PUT_LINE ('Get Paycheck Splits : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));
    
    FOR i IN (

       SELECT TO_CHAR(a.XREF_PAYCHKID)                      AS PC_SOURCE_PAYCHECK_ID
         FROM TEMP_CACHE_DXCHKXREF a,
              TEMP_CACHE_IQACHDD   b
        WHERE a.XREF_USERID        = b.ACHD_USERID
          AND a.XREF_TRACE_NUMBER  = b.ACHD_TRACE_NUMBER
          AND a.XREF_DD_NUMBER     = b.ACHD_SUBNUM
          AND a.XREF_USERID        = v_DIY_COMPANY_ID
        ORDER
           BY a.XREF_PAYCHKID ASC
    )
    LOOP

      GetPaycheckSplits (v_DIY_COMPANY_ID, i.PC_SOURCE_PAYCHECK_ID);

    END LOOP;


    DBMS_OUTPUT.PUT_LINE ('Get Transactions : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));
    
    FOR i IN (
      SELECT TO_CHAR(ACH_LIABCHK)  AS PAYROLL_RUN_SRC_ID
        FROM TEMP_CACHE_IQACH
       WHERE ACH_USERID        = v_DIY_COMPANY_ID
    )
    LOOP

      GetTxns (v_DIY_COMPANY_ID, i.PAYROLL_RUN_SRC_ID);

    END LOOP;

    DBMS_OUTPUT.PUT_LINE ('Done : ' || TO_CHAR(SYSDATE, 'HH24:MI:SS'));

    IF (p_DIY_COMPANY_ID IS NOT NULL) THEN
      ResetCompany (v_DIY_COMPANY_ID);
    END IF;
    
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE(SQLERRM);          
  END;


  -- 
  -- GETTERS TO GET THE AS400 DATA
  --

  PROCEDURE GetCompany (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD              NUMBER;
    P_RETURN_MSG             VARCHAR2(1000);

    P_COMPANY_REFCUR         PK_DIYDDTOPSP_CUSTOMER.refcur_company;

	FEIN                     VARCHAR2(100);
    LEGALNAME                VARCHAR2(100);
    DBANAME                  VARCHAR2(100);
    PAYROLLFREQUENCYCD       VARCHAR2(100);
    NOTIFICATIONEMAIL        VARCHAR2(100);
    FUNDINGMODELCD           VARCHAR2(100);
    COMPANYSTATUS            VARCHAR2(100);
    STATUSEFFDATE            DATE;
	PHONE                    VARCHAR2(100);
    LEGALADDRESSLINE1        VARCHAR2(100);
    LEGALADDRESSLINE2        VARCHAR2(100);
    LEGALADDRESSLINE3        VARCHAR2(100);
    LEGALADDRESSCITY         VARCHAR2(100);
    LEGALADDRESSSTATE        VARCHAR2(100);
	LEGALADDRESSZIP          VARCHAR2(100);
    LEGALADDRESSZIPPLUS4     VARCHAR2(100);
    
    QBREGISTRATIONNUM        VARCHAR2(100);
    QBVERSIONNUM             VARCHAR2(100);
    QBAPPLICATIONID          VARCHAR2(100);
    QBCHARTBANKACCTNAME      VARCHAR2(100);
    QBAGREEMENTNUM           VARCHAR2(100);
    CRISROWID                VARCHAR2(100);
    QBSERVICEKEY             VARCHAR2(100);
    PSPMASTERSKU             VARCHAR2(100);
    PSPOFFERCODE             VARCHAR2(100);
    PSPOFFEREXPDTTM          DATE;
    DIYFIRSTPAYROLLDT        DATE;
    DIYSIGNUPDT              DATE;
    DIYTAXTABLENUM           VARCHAR2(100);
    QBCHARTFEENAME           VARCHAR2(100);
    QBCHARTSALESTAXNAME      VARCHAR2(100);
    SALESTAXSTATUS           VARCHAR2(100);
    NEXTPAYROLLTXNID         NUMBER;
    NEXTPAYCHECKID           NUMBER;
    NEXTQBSYNCTOKEN          NUMBER;
    QBPIN1                   VARCHAR2(100);
    QBPIN2                   VARCHAR2(100);
    QBPIN3                   VARCHAR2(100);
    ACCTSUBTYPE              VARCHAR2(100);
    PRIORPAYROLLCNT          NUMBER;

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;
      -- P_COMPANY_REFCUR := NULL;  Modify the code to initialize this parameter
      
      IF (P_COMPANY_REFCUR%ISOPEN) THEN
        CLOSE P_COMPANY_REFCUR;
      END IF;

      PK_DIYDDTOPSP_CUSTOMER.migrateCompanyInfo (
  	    P_DIY_COMPANY_ID,
  	    P_RETURN_CD,
  	    P_RETURN_MSG,
  	    P_COMPANY_REFCUR
  	  );

  	  FETCH P_COMPANY_REFCUR INTO
        FEIN,
        LEGALNAME,
        DBANAME,
        PAYROLLFREQUENCYCD,
        NOTIFICATIONEMAIL,
        FUNDINGMODELCD,
        PHONE,
        LEGALADDRESSLINE1,
        LEGALADDRESSLINE2,
        LEGALADDRESSLINE3,
        LEGALADDRESSCITY,
        LEGALADDRESSSTATE,
        LEGALADDRESSZIP,
        LEGALADDRESSZIPPLUS4,
        QBREGISTRATIONNUM,
        QBVERSIONNUM,
        QBAPPLICATIONID,
        QBAGREEMENTNUM,
        CRISROWID,
        QBSERVICEKEY,
        PSPMASTERSKU,
        PSPOFFERCODE,
        PSPOFFEREXPDTTM,
        DIYFIRSTPAYROLLDT,
        DIYSIGNUPDT,
        DIYTAXTABLENUM,
        QBCHARTFEENAME,
        QBCHARTSALESTAXNAME,
        SALESTAXSTATUS,
        NEXTPAYROLLTXNID,
        NEXTPAYCHECKID,
        NEXTQBSYNCTOKEN,
        QBPIN1,
        QBPIN2,
        QBPIN3,
        ACCTSUBTYPE,
        PRIORPAYROLLCNT;


      DBMS_OUTPUT.PUT_LINE('********************************');
  	  DBMS_OUTPUT.PUT_LINE('* Company                       ');
  	  DBMS_OUTPUT.PUT_LINE('********************************');

      DBMS_OUTPUT.PUT_LINE('  FEIN : ' || FEIN                 || ' - ' || LENGTH(FEIN));
      DBMS_OUTPUT.PUT_LINE(' LNAME : ' || LEGALNAME            || ' - ' || LENGTH(LEGALNAME));
      DBMS_OUTPUT.PUT_LINE(' DNAME : ' || DBANAME              || ' - ' || LENGTH(DBANAME));
      DBMS_OUTPUT.PUT_LINE(' PYFRQ : ' || PAYROLLFREQUENCYCD);
      DBMS_OUTPUT.PUT_LINE('NEMAIL : ' || NOTIFICATIONEMAIL);
      DBMS_OUTPUT.PUT_LINE('FUNDMD : ' || FUNDINGMODELCD);
      DBMS_OUTPUT.PUT_LINE(' PHONE : ' || PHONE);
      DBMS_OUTPUT.PUT_LINE('LADDR1 : ' || LEGALADDRESSLINE1    || ' - ' || LENGTH(LEGALADDRESSLINE1));
      DBMS_OUTPUT.PUT_LINE('LADDR2 : ' || LEGALADDRESSLINE2    || ' - ' || LENGTH(LEGALADDRESSLINE2));
      DBMS_OUTPUT.PUT_LINE('LADDR3 : ' || LEGALADDRESSLINE3);
      DBMS_OUTPUT.PUT_LINE(' LCITY : ' || LEGALADDRESSCITY     || ' - ' || LENGTH(LEGALADDRESSCITY));
      DBMS_OUTPUT.PUT_LINE('LSTATE : ' || LEGALADDRESSSTATE    || ' - ' || LENGTH(LEGALADDRESSSTATE));
      DBMS_OUTPUT.PUT_LINE('  LZIP : ' || LEGALADDRESSZIP      || ' - ' || LENGTH(LEGALADDRESSZIP));
      DBMS_OUTPUT.PUT_LINE(' LZIP4 : ' || LEGALADDRESSZIPPLUS4);
                            
      DBMS_OUTPUT.PUT_LINE(' QBREG : ' || QBREGISTRATIONNUM    || ' - ' || LENGTH(QBREGISTRATIONNUM));
      DBMS_OUTPUT.PUT_LINE(' QBVER : ' || QBVERSIONNUM         || ' - ' || LENGTH(QBVERSIONNUM));
      DBMS_OUTPUT.PUT_LINE(' QBAPP : ' || QBAPPLICATIONID      || ' - ' || LENGTH(QBAPPLICATIONID));
      DBMS_OUTPUT.PUT_LINE('AGGNUM : ' || QBAGREEMENTNUM       || ' - ' || LENGTH(QBAGREEMENTNUM));
      DBMS_OUTPUT.PUT_LINE(' CRROW : ' || CRISROWID            || ' - ' || LENGTH(CRISROWID));
      DBMS_OUTPUT.PUT_LINE('SVCKEY : ' || QBSERVICEKEY         || ' - ' || LENGTH(QBSERVICEKEY));
      DBMS_OUTPUT.PUT_LINE('SUBTYP : ' || ACCTSUBTYPE          || ' - ' || LENGTH(ACCTSUBTYPE));
      DBMS_OUTPUT.PUT_LINE('MSTSKU : ' || PSPMASTERSKU         || ' - ' || LENGTH(PSPMASTERSKU));
      DBMS_OUTPUT.PUT_LINE('OFFRCD : ' || PSPOFFERCODE         || ' - ' || LENGTH(PSPOFFERCODE));
      DBMS_OUTPUT.PUT_LINE('OFFEXP : ' || PSPOFFEREXPDTTM      || ' - ' || LENGTH(PSPOFFEREXPDTTM));
      DBMS_OUTPUT.PUT_LINE('FSTPAY : ' || DIYFIRSTPAYROLLDT    || ' - ' || LENGTH(DIYFIRSTPAYROLLDT));
      DBMS_OUTPUT.PUT_LINE('SIGNUP : ' || DIYSIGNUPDT          || ' - ' || LENGTH(DIYSIGNUPDT));
      DBMS_OUTPUT.PUT_LINE('TXTNUM : ' || DIYTAXTABLENUM       || ' - ' || LENGTH(DIYTAXTABLENUM));
      DBMS_OUTPUT.PUT_LINE('CHTFEE : ' || QBCHARTFEENAME       || ' - ' || LENGTH(QBCHARTFEENAME));
      DBMS_OUTPUT.PUT_LINE('CHTTAX : ' || QBCHARTSALESTAXNAME  || ' - ' || LENGTH(QBCHARTSALESTAXNAME));
      DBMS_OUTPUT.PUT_LINE('SALETX : ' || SALESTAXSTATUS       || ' - ' || LENGTH(SALESTAXSTATUS));
      DBMS_OUTPUT.PUT_LINE('NXTPYR : ' || NEXTPAYROLLTXNID     || ' - ' || LENGTH(NEXTPAYROLLTXNID));
      DBMS_OUTPUT.PUT_LINE('NXTCHK : ' || NEXTPAYCHECKID       || ' - ' || LENGTH(NEXTPAYCHECKID));
      DBMS_OUTPUT.PUT_LINE('NXTTKN : ' || NEXTQBSYNCTOKEN      || ' - ' || LENGTH(NEXTQBSYNCTOKEN));
      DBMS_OUTPUT.PUT_LINE('QBPIN1 : ' || QBPIN1               || ' - ' || LENGTH(QBPIN1));
      DBMS_OUTPUT.PUT_LINE('QBPIN2 : ' || QBPIN2               || ' - ' || LENGTH(QBPIN2));
      DBMS_OUTPUT.PUT_LINE('QBPIN3 : ' || QBPIN3               || ' - ' || LENGTH(QBPIN3));
      DBMS_OUTPUT.PUT_LINE('PYRLCT : ' || PRIORPAYROLLCNT);      
      
      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

  	  CLOSE P_COMPANY_REFCUR;

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;

  EXCEPTION
    WHEN OTHERS THEN
      CLOSE P_COMPANY_REFCUR;
	  DBMS_OUTPUT.PUT_LINE (SQLERRM);
  END;


  PROCEDURE GetContacts (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    P_COMP_CONTACT_REFCUR          PK_DIYDDTOPSP_CUSTOMER.refcur_comp_contact;
    P_COMP_PRINC_CONTACT_REFCUR    PK_DIYDDTOPSP_CUSTOMER.refcur_comp_princ_contact;    

    CONTACT_COMM_PREF              VARCHAR2(100);
    CONTACT_EMAIL                  VARCHAR2(100);
    CONTACT_FIRST_NAME             VARCHAR2(100);
    CONTACT_MIDDLE_NAME            VARCHAR2(100);
    CONTACT_LAST_NAME              VARCHAR2(100);
    CONTACT_GENDER_CD              VARCHAR2(100);
    CONTACT_PHONE                  VARCHAR2(100);
    CONTACT_AUTH_SIGN_YN           VARCHAR2(100);
    CONTACT_ROLE_CD                VARCHAR2(100);

    CONTACT_NAME_TITLE             VARCHAR2(100);
    CONTACT_JOB_TITLE              VARCHAR2(100);
    CONTACT_FAX_NUM                VARCHAR2(100);

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;
      -- P_COMP_CONTACT_REFCUR := NULL;  Modify the code to initialize this parameter

      PK_DIYDDTOPSP_CUSTOMER.migrateContacts (
  	    P_DIY_COMPANY_ID,
  	    P_RETURN_CD,
  	    P_RETURN_MSG,
  	    P_COMP_CONTACT_REFCUR,
        P_COMP_PRINC_CONTACT_REFCUR        
  	  );

      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* Contacts                      ');
  	  DBMS_OUTPUT.PUT_LINE('********************************');


  	  DBMS_OUTPUT.PUT_LINE('-> Payroll Admin           ');

  	  LOOP

  	    FETCH P_COMP_CONTACT_REFCUR INTO
          CONTACT_COMM_PREF,
          CONTACT_EMAIL,
          CONTACT_FIRST_NAME,
          CONTACT_MIDDLE_NAME,
          CONTACT_LAST_NAME,
          CONTACT_GENDER_CD,
          CONTACT_PHONE,
          CONTACT_AUTH_SIGN_YN,
          CONTACT_ROLE_CD,
          CONTACT_NAME_TITLE,
          CONTACT_JOB_TITLE,
          CONTACT_FAX_NUM;
        
        EXIT WHEN P_COMP_CONTACT_REFCUR%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('COMPRF : ' || CONTACT_COMM_PREF    || ' - ' || LENGTH(CONTACT_COMM_PREF));
        DBMS_OUTPUT.PUT_LINE('EMAIL  : ' || CONTACT_EMAIL        || ' - ' || LENGTH(CONTACT_EMAIL));
        DBMS_OUTPUT.PUT_LINE('F NAME : ' || CONTACT_FIRST_NAME   || ' - ' || LENGTH(CONTACT_FIRST_NAME));
        DBMS_OUTPUT.PUT_LINE('M NAME : ' || CONTACT_MIDDLE_NAME  || ' - ' || LENGTH(CONTACT_MIDDLE_NAME));
        DBMS_OUTPUT.PUT_LINE('L NAME : ' || CONTACT_LAST_NAME    || ' - ' || LENGTH(CONTACT_LAST_NAME));
        DBMS_OUTPUT.PUT_LINE('GENDER : ' || CONTACT_GENDER_CD    || ' - ' || LENGTH(CONTACT_GENDER_CD));
        DBMS_OUTPUT.PUT_LINE('PHONE  : ' || CONTACT_PHONE        || ' - ' || LENGTH(CONTACT_PHONE));
        DBMS_OUTPUT.PUT_LINE('AUTHYN : ' || CONTACT_AUTH_SIGN_YN || ' - ' || LENGTH(CONTACT_AUTH_SIGN_YN));
        DBMS_OUTPUT.PUT_LINE('  ROLE : ' || CONTACT_ROLE_CD      || ' - ' || LENGTH(CONTACT_ROLE_CD));
                              
        DBMS_OUTPUT.PUT_LINE('NTITLE : ' || CONTACT_NAME_TITLE   || ' - ' || LENGTH(CONTACT_NAME_TITLE));
        DBMS_OUTPUT.PUT_LINE('JTITLE : ' || CONTACT_JOB_TITLE    || ' - ' || LENGTH(CONTACT_JOB_TITLE));
        DBMS_OUTPUT.PUT_LINE('   FAX : ' || CONTACT_FAX_NUM      || ' - ' || LENGTH(CONTACT_FAX_NUM));
        
  	  END LOOP;


      DBMS_OUTPUT.PUT_LINE('-> Primary and Secondary      ');

  	  LOOP

        FETCH P_COMP_PRINC_CONTACT_REFCUR INTO
          CONTACT_COMM_PREF,
          CONTACT_EMAIL,
          CONTACT_FIRST_NAME,
          CONTACT_MIDDLE_NAME,
          CONTACT_LAST_NAME,
          CONTACT_GENDER_CD,
          CONTACT_PHONE,
          CONTACT_AUTH_SIGN_YN,
          CONTACT_ROLE_CD,
          CONTACT_NAME_TITLE,
          CONTACT_JOB_TITLE,
          CONTACT_FAX_NUM;
        
        EXIT WHEN P_COMP_CONTACT_REFCUR%NOTFOUND;        

        DBMS_OUTPUT.PUT_LINE('COMPRF : ' || CONTACT_COMM_PREF    || ' - ' || LENGTH(CONTACT_COMM_PREF));
        DBMS_OUTPUT.PUT_LINE('EMAIL  : ' || CONTACT_EMAIL        || ' - ' || LENGTH(CONTACT_EMAIL));
        DBMS_OUTPUT.PUT_LINE('F NAME : ' || CONTACT_FIRST_NAME   || ' - ' || LENGTH(CONTACT_FIRST_NAME));
        DBMS_OUTPUT.PUT_LINE('M NAME : ' || CONTACT_MIDDLE_NAME  || ' - ' || LENGTH(CONTACT_MIDDLE_NAME));
        DBMS_OUTPUT.PUT_LINE('L NAME : ' || CONTACT_LAST_NAME    || ' - ' || LENGTH(CONTACT_LAST_NAME));
        DBMS_OUTPUT.PUT_LINE('GENDER : ' || CONTACT_GENDER_CD    || ' - ' || LENGTH(CONTACT_GENDER_CD));
        DBMS_OUTPUT.PUT_LINE('PHONE  : ' || CONTACT_PHONE        || ' - ' || LENGTH(CONTACT_PHONE));
        DBMS_OUTPUT.PUT_LINE('AUTHYN : ' || CONTACT_AUTH_SIGN_YN || ' - ' || LENGTH(CONTACT_AUTH_SIGN_YN));
        DBMS_OUTPUT.PUT_LINE('  ROLE : ' || CONTACT_ROLE_CD      || ' - ' || LENGTH(CONTACT_ROLE_CD));
                         
        DBMS_OUTPUT.PUT_LINE('NTITLE : ' || CONTACT_NAME_TITLE   || ' - ' || LENGTH(CONTACT_NAME_TITLE));
        DBMS_OUTPUT.PUT_LINE('JTITLE : ' || CONTACT_JOB_TITLE    || ' - ' || LENGTH(CONTACT_JOB_TITLE));
        DBMS_OUTPUT.PUT_LINE('   FAX : ' || CONTACT_FAX_NUM      || ' - ' || LENGTH(CONTACT_FAX_NUM));

  	  END LOOP;
      

      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

  	  CLOSE P_COMP_CONTACT_REFCUR;
      CLOSE P_COMP_PRINC_CONTACT_REFCUR;
      
	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
    END IF;

  END;


  PROCEDURE GetDDInfo (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_COMPANY_REFCUR               PK_DIYDDTOPSP_CUSTOMER.refcur_company;

    OVERRIDECOMPANYLIMITAMOUNT     NUMBER;
    OVERRIDEEMPLOYEELIMITAMOUNT    NUMBER;
    OFFLOADGROUPCD                 VARCHAR2(100);
    DDSERVICESTATUS                VARCHAR2(100);
    STATUSEFFECTIVEDATE            DATE;
    CONSEQLIMITVIOLATIONCOUNT      NUMBER;

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;
      -- P_COMPANY_REFCUR := NULL;  Modify the code to initialize this parameter

      PK_DIYDDTOPSP_CUSTOMER.migrateDDService (
	    P_DIY_COMPANY_ID,
	    P_RETURN_CD,
	    P_RETURN_MSG,
	    P_COMPANY_REFCUR
	  );

	  FETCH P_COMPANY_REFCUR INTO
        OVERRIDECOMPANYLIMITAMOUNT,
        OVERRIDEEMPLOYEELIMITAMOUNT,
        OFFLOADGROUPCD,
        DDSERVICESTATUS,
        STATUSEFFECTIVEDATE,
        CONSEQLIMITVIOLATIONCOUNT;

      DBMS_OUTPUT.PUT_LINE('********************************');
  	  DBMS_OUTPUT.PUT_LINE('* DD Info                       ');
  	  DBMS_OUTPUT.PUT_LINE('********************************');

      DBMS_OUTPUT.PUT_LINE('ER LMT : ' || OVERRIDECOMPANYLIMITAMOUNT);
  	  DBMS_OUTPUT.PUT_LINE('EE LMT : ' || OVERRIDEEMPLOYEELIMITAMOUNT);
      DBMS_OUTPUT.PUT_LINE('OFFGRP : ' || OFFLOADGROUPCD);
      DBMS_OUTPUT.PUT_LINE('DDSTAT : ' || DDSERVICESTATUS);
  	  DBMS_OUTPUT.PUT_LINE('STATDT : ' || TO_CHAR(STATUSEFFECTIVEDATE, 'MM.DD.YYYY'));
      DBMS_OUTPUT.PUT_LINE('VIOLCT : ' || CONSEQLIMITVIOLATIONCOUNT);

      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

  	  CLOSE P_COMPANY_REFCUR;

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;

  EXCEPTION
    WHEN OTHERS THEN
	  DBMS_OUTPUT.PUT_LINE (SQLERRM);

  END;


  PROCEDURE GetBankAcct (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    P_COMP_BANKACCT_REFCUR         PK_DIYDDTOPSP_CUSTOMER.refcur_company;

    COMPANYBANKACCOUNTID           VARCHAR2(100);
    ACCOUNTNUMBER                  VARCHAR2(100);
    ACCOUNTTYPECD                  VARCHAR2(100);
    BANKNAME                       VARCHAR2(100);
    ROUTINGNUMBER                  VARCHAR2(100);
    BAEFFECTIVEDATE                DATE;
    BAEXPIRATIONDATE               DATE;
    CBAEFFECTIVEDATE               DATE;
    CBAEXPIRATIONDATE              DATE;
    BANKACCOUNTSTATUS              VARCHAR2(100);
    STATUSEFFECTIVEDATE            DATE;
    VERIFYRETRYCOUNT               NUMBER;
    TOTALRETRYCOUNT                NUMBER;
    QBCHARTBANKACCTNAME            VARCHAR2(100);

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;
      -- P_COMP_BANKACCT_REFCUR := NULL;  Modify the code to initialize this parameter

      PK_DIYDDTOPSP_CUSTOMER.migrateCompanyBankAccounts (
	    P_DIY_COMPANY_ID,
	    P_RETURN_CD,
	    P_RETURN_MSG,
	    P_COMP_BANKACCT_REFCUR
	  );

	  FETCH P_COMP_BANKACCT_REFCUR INTO
        COMPANYBANKACCOUNTID,
        ACCOUNTNUMBER,
        ACCOUNTTYPECD,
        BANKNAME,
        ROUTINGNUMBER,
        BAEFFECTIVEDATE,
        BAEXPIRATIONDATE,
        CBAEFFECTIVEDATE,
        CBAEXPIRATIONDATE,
        BANKACCOUNTSTATUS,
        STATUSEFFECTIVEDATE,
        VERIFYRETRYCOUNT,
        TOTALRETRYCOUNT,
        QBCHARTBANKACCTNAME;

      DBMS_OUTPUT.PUT_LINE('********************************');
  	  DBMS_OUTPUT.PUT_LINE('* Bank Account                  ');
  	  DBMS_OUTPUT.PUT_LINE('********************************');

      DBMS_OUTPUT.PUT_LINE('ERACCT : ' || COMPANYBANKACCOUNTID || ' - ' || LENGTH(COMPANYBANKACCOUNTID));
      DBMS_OUTPUT.PUT_LINE('ACCNUM : ' || ACCOUNTNUMBER        || ' - ' || LENGTH(ACCOUNTNUMBER));
      DBMS_OUTPUT.PUT_LINE('ACCTYP : ' || ACCOUNTTYPECD        || ' - ' || LENGTH(ACCOUNTTYPECD));
      DBMS_OUTPUT.PUT_LINE(' BNKNM : ' || BANKNAME             || ' - ' || LENGTH(BANKNAME));
      DBMS_OUTPUT.PUT_LINE('RTNNUM : ' || ROUTINGNUMBER        || ' - ' || LENGTH(ROUTINGNUMBER));
      DBMS_OUTPUT.PUT_LINE(' BAEFF : ' || TO_CHAR(BAEFFECTIVEDATE, 'MM.DD.YYYY'));
      DBMS_OUTPUT.PUT_LINE(' BAEXP : ' || BAEXPIRATIONDATE);
      DBMS_OUTPUT.PUT_LINE('CBAEFF : ' || TO_CHAR(CBAEFFECTIVEDATE, 'MM.DD.YYYY'));
      DBMS_OUTPUT.PUT_LINE('CBAEXP : ' || CBAEXPIRATIONDATE);
      DBMS_OUTPUT.PUT_LINE('BASTAT : ' || BANKACCOUNTSTATUS);
      DBMS_OUTPUT.PUT_LINE('STAEFF : ' || TO_CHAR(STATUSEFFECTIVEDATE, 'MM.DD.YYYY'));
      DBMS_OUTPUT.PUT_LINE('VRTYCT : ' || VERIFYRETRYCOUNT);
      DBMS_OUTPUT.PUT_LINE('TTRYCT : ' || TOTALRETRYCOUNT);
      DBMS_OUTPUT.PUT_LINE('CHTBNK : ' || QBCHARTBANKACCTNAME  || ' - ' || LENGTH(QBCHARTBANKACCTNAME));

      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

  	  CLOSE P_COMP_BANKACCT_REFCUR;

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;

  END;


  PROCEDURE GetEvents (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_COMP_STRIKE_COUNT            NUMBER;
    p_COMP_EVENTS_REFCUR           PK_DIYDDTOPSP_CUSTOMER.refcur_comp_events;

    EMP_EMPLOYEE_GSEQ              NUMBER;
    
    EV_COMP_EVENT_GSEQ             NUMBER;
    EV_EVENT_TYPE_CD               VARCHAR2(100);
    EV_STATUS_CD                   VARCHAR2(100);
    EV_STATUS_EFF_DTTM             DATE;
    EV_EVENT_TIMESTAMP             DATE;
    EV_EVENT_STRIKE_REASON         VARCHAR2(100);
    EV_EVENT_STRIKE_REASON_DESCR   VARCHAR2(100);

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;

      PK_DIYDDTOPSP_CUSTOMER.migrateCompanyEvents (
        p_DIY_COMPANY_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_COMP_STRIKE_COUNT,
        p_COMP_EVENTS_REFCUR
	  );


      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* COMPANY STRIKES               ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      DBMS_OUTPUT.PUT_LINE ('Strike Count = ' || p_COMP_STRIKE_COUNT);

      IF (p_COMP_STRIKE_COUNT > 0) THEN

          LOOP
          
            FETCH p_COMP_EVENTS_REFCUR INTO
              EV_COMP_EVENT_GSEQ,
              EV_EVENT_TYPE_CD,
              EV_STATUS_CD,
              EV_STATUS_EFF_DTTM,
              EV_EVENT_TIMESTAMP,
              EV_EVENT_STRIKE_REASON,
              EV_EVENT_STRIKE_REASON_DESCR;
            
            EXIT WHEN p_COMP_EVENTS_REFCUR%NOTFOUND;

            DBMS_OUTPUT.PUT_LINE('EVGSEQ : ' || EV_COMP_EVENT_GSEQ);
            DBMS_OUTPUT.PUT_LINE('EVTYPE : ' || EV_EVENT_TYPE_CD   || ' - ' || LENGTH(EV_EVENT_TYPE_CD));
            DBMS_OUTPUT.PUT_LINE('EVSTAT : ' || EV_STATUS_CD       || ' - ' || LENGTH(EV_STATUS_CD));
            DBMS_OUTPUT.PUT_LINE('STAEFF : ' || EV_STATUS_EFF_DTTM);
            DBMS_OUTPUT.PUT_LINE('TMSTMP : ' || EV_EVENT_TIMESTAMP);
          
          END LOOP;
          
  	      CLOSE p_COMP_EVENTS_REFCUR;
      
      END IF;

      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;

  END;

  
  PROCEDURE GetEmployees (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    TYPE refcur_employees          IS REF CURSOR;
    p_EMPLOYEE_LIST                refcur_employees;

    EMP_EMPLOYEE_GSEQ              NUMBER;
    EMP_STATUS_CD                  VARCHAR2(1000);
    EMP_SOURCE_EMPLOYEE_ID         VARCHAR2(1000);
    EMP_TAX_ID                     VARCHAR2(1000);
    EMP_FIRST_NAME                 VARCHAR2(1000);
    EMP_MIDDLE_NAME                VARCHAR2(1000);
    EMP_LAST_NAME                  VARCHAR2(1000);

  BEGIN

    IF p_EMPLOYEE_LIST%ISOPEN THEN
      CLOSE p_EMPLOYEE_LIST;
    END IF;

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;

      PK_DIYDDTOPSP_PAYROLLS.migrateEmployees (
        p_DIY_COMPANY_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_EMPLOYEE_LIST
	  );


      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* EMPLOYEES                     ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      LOOP
      
        FETCH p_EMPLOYEE_LIST INTO
          EMP_EMPLOYEE_GSEQ,
          EMP_STATUS_CD,
          EMP_SOURCE_EMPLOYEE_ID,
          EMP_TAX_ID,
          EMP_FIRST_NAME,
          EMP_MIDDLE_NAME,
          EMP_LAST_NAME;
          
        EXIT WHEN p_EMPLOYEE_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('EEGSEQ : ' || EMP_EMPLOYEE_GSEQ);
        DBMS_OUTPUT.PUT_LINE('EESTAT : ' || EMP_STATUS_CD          || ' - ' || LENGTH(EMP_STATUS_CD));
        DBMS_OUTPUT.PUT_LINE(' SRCID : ' || EMP_SOURCE_EMPLOYEE_ID || ' - ' || LENGTH(EMP_SOURCE_EMPLOYEE_ID));
        DBMS_OUTPUT.PUT_LINE(' TAXID : ' || EMP_TAX_ID             || ' - ' || LENGTH(EMP_TAX_ID));
        DBMS_OUTPUT.PUT_LINE('F NAME : ' || EMP_FIRST_NAME         || ' - ' || LENGTH(EMP_FIRST_NAME));
        DBMS_OUTPUT.PUT_LINE('M NAME : ' || EMP_MIDDLE_NAME        || ' - ' || LENGTH(EMP_MIDDLE_NAME));
        DBMS_OUTPUT.PUT_LINE('L NAME : ' || EMP_LAST_NAME          || ' - ' || LENGTH(EMP_LAST_NAME));
      
      END LOOP;

      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

  	  CLOSE p_EMPLOYEE_LIST;

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
      CLOSE p_EMPLOYEE_LIST;
	END IF;
  
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE (SQLERRM);
  END;


  PROCEDURE GetEEBankAcct (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_DIY_SOURCE_EMP_ID            IN  VARCHAR2
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_EMP_BANK_ACCT_LIST           PK_DIYDDTOPSP_PAYROLLS.refcur_emp_bank_accts;    

    EMP_BANK_ACCT_GSEQ             NUMBER;  
    EMP_ACCT_NUM                   VARCHAR2(100); 
    EMP_ACCT_TYPE_CD               VARCHAR2(100); 
    EMP_BANK_NAME                  VARCHAR2(100); 
    EMP_ROUTING_NUM                VARCHAR2(100);
    EMP_BANK_ACCT_ASSOC_GSEQ       NUMBER;  
    EMP_SOURCE_BANK_ACCT_ID        VARCHAR2(100);
    EMP_BANK_ACCT_STATUS_CD        VARCHAR2(100); 
    EMP_BANK_ACCT_EFF_DTTM         DATE; 
    EMP_BANK_ACCT_EXP_DTTM         DATE;

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;
      
      PK_DIYDDTOPSP_PAYROLLS.migrateEmployeeBankAccounts (
        p_DIY_COMPANY_ID,
        p_DIY_SOURCE_EMP_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_EMP_BANK_ACCT_LIST
      );

      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* EMPLOYEE BANK ACCOUNTS        ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      LOOP

        FETCH p_EMP_BANK_ACCT_LIST INTO
          EMP_BANK_ACCT_GSEQ, 
          EMP_ACCT_NUM, 
          EMP_ACCT_TYPE_CD, 
          EMP_BANK_NAME, 
          EMP_ROUTING_NUM,
          EMP_BANK_ACCT_ASSOC_GSEQ, 
          EMP_SOURCE_BANK_ACCT_ID,
          EMP_BANK_ACCT_STATUS_CD, 
          EMP_BANK_ACCT_EFF_DTTM,
          EMP_BANK_ACCT_EXP_DTTM; 
            
        EXIT WHEN p_EMP_BANK_ACCT_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('BKGSEQ : ' || EMP_BANK_ACCT_GSEQ);
        DBMS_OUTPUT.PUT_LINE('ACCNUM : ' || EMP_ACCT_NUM);
        DBMS_OUTPUT.PUT_LINE('ACCTYP : ' || EMP_ACCT_TYPE_CD         || ' - ' || LENGTH(EMP_ACCT_TYPE_CD));
        DBMS_OUTPUT.PUT_LINE('BKNAME : ' || EMP_BANK_NAME            || ' - ' || LENGTH(EMP_BANK_NAME));
        DBMS_OUTPUT.PUT_LINE('RTNNUM : ' || EMP_ROUTING_NUM          || ' - ' || LENGTH(EMP_ROUTING_NUM));
        DBMS_OUTPUT.PUT_LINE('ASGSEQ : ' || EMP_BANK_ACCT_ASSOC_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' SRCID : ' || EMP_SOURCE_BANK_ACCT_ID  || ' - ' || LENGTH(EMP_SOURCE_BANK_ACCT_ID));
        DBMS_OUTPUT.PUT_LINE('BASTAT : ' || EMP_BANK_ACCT_STATUS_CD  || ' - ' || LENGTH(EMP_BANK_ACCT_STATUS_CD));
        DBMS_OUTPUT.PUT_LINE(' BAEFF : ' || TO_CHAR(EMP_BANK_ACCT_EFF_DTTM, 'MMDDYYYY HH24:MI:SS'));        
          
      END LOOP;

      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);
          
  	  CLOSE p_EMP_BANK_ACCT_LIST;

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;

  END;

  
  PROCEDURE GetPayRuns (
    p_DIY_COMPANY_ID               IN  NUMBER
  )
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_PAYROLL_RUN_LIST             PK_DIYDDTOPSP_PAYROLLS.refcur_comp_payruns;

    PR_PAYROLL_RUN_GSEQ            NUMBER;
    PR_SOURCE_PAY_RUN_ID           VARCHAR2(100);
    PR_PAYCHECK_DEPOSIT_DATE       DATE;
    PR_PAYROLL_NET_AMT             NUMBER;
    PR_PAYROLL_RUN_DATE            DATE;
    PR_PAYROLL_STATUS_CD           VARCHAR2(100);
    PR_STATUS_EFF_DATE             DATE;
    CBA_COMP_BANK_ACCT_ASSOC_GSEQ  NUMBER;

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;

      PK_DIYDDTOPSP_PAYROLLS.migratePayrollRuns (
        p_DIY_COMPANY_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_PAYROLL_RUN_LIST
      );


      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* PAYROLLS                    ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      LOOP
          
        FETCH p_PAYROLL_RUN_LIST INTO
          PR_PAYROLL_RUN_GSEQ,
          PR_SOURCE_PAY_RUN_ID, 
          PR_PAYCHECK_DEPOSIT_DATE, 
          PR_PAYROLL_NET_AMT, 
          PR_PAYROLL_RUN_DATE,  
          PR_PAYROLL_STATUS_CD, 
          PR_STATUS_EFF_DATE, 
          CBA_COMP_BANK_ACCT_ASSOC_GSEQ;
            
        EXIT WHEN p_PAYROLL_RUN_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('PRGSEQ : ' || PR_PAYROLL_RUN_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' PRSRC : ' || PR_SOURCE_PAY_RUN_ID   || ' - ' || LENGTH(PR_SOURCE_PAY_RUN_ID));
        DBMS_OUTPUT.PUT_LINE(' DEPDT : ' || PR_PAYCHECK_DEPOSIT_DATE);
        DBMS_OUTPUT.PUT_LINE('NETAMT : ' || PR_PAYROLL_NET_AMT);
        DBMS_OUTPUT.PUT_LINE(' RUNDT : ' || PR_PAYROLL_RUN_DATE);
        DBMS_OUTPUT.PUT_LINE('PRSTAT : ' || PR_PAYROLL_STATUS_CD   || ' - ' || LENGTH(PR_PAYROLL_STATUS_CD));        
        DBMS_OUTPUT.PUT_LINE('STAEFF : ' || PR_STATUS_EFF_DATE);
        DBMS_OUTPUT.PUT_LINE('ASGSEQ : ' || CBA_COMP_BANK_ACCT_ASSOC_GSEQ);        
          
      END LOOP;
          
  	  CLOSE p_PAYROLL_RUN_LIST;
      
      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;

  END;


  PROCEDURE GetPaychecks (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_PAYROLL_RUN_ID               IN  VARCHAR2
  )    
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_PAYCHECK_LIST                PK_DIYDDTOPSP_PAYROLLS.refcur_comp_paychecks;

    PC_PAYCHECK_GSEQ               NUMBER;
    PC_SOURCE_PAYCHECK_ID          VARCHAR2(100);
    PC_SOURCE_EMPLOYEE_ID          VARCHAR2(100);

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;
      
  	  IF (p_PAYCHECK_LIST%ISOPEN) THEN 
        CLOSE p_PAYCHECK_LIST;      
      END IF;

      PK_DIYDDTOPSP_PAYROLLS.migratePaychecks (
        p_DIY_COMPANY_ID,
	    p_PAYROLL_RUN_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_PAYCHECK_LIST
      );


      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* PAYCHECKS                     ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      LOOP
          
        FETCH p_PAYCHECK_LIST INTO
          PC_PAYCHECK_GSEQ, 
          PC_SOURCE_PAYCHECK_ID,
          PC_SOURCE_EMPLOYEE_ID;
            
        EXIT WHEN p_PAYCHECK_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('PCGSEQ : ' || PC_PAYCHECK_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' PCSRC : ' || PC_SOURCE_PAYCHECK_ID   || ' - ' || LENGTH(PC_SOURCE_PAYCHECK_ID));
        DBMS_OUTPUT.PUT_LINE(' EESRC : ' || PC_SOURCE_EMPLOYEE_ID   || ' - ' || LENGTH(PC_SOURCE_EMPLOYEE_ID));        
          
      END LOOP;
          
  	  CLOSE p_PAYCHECK_LIST;
      
      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;
    
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE (SQLERRM);
  END;


  PROCEDURE GetPaycheckSplits (
    p_DIY_COMPANY_ID               IN  NUMBER,
	p_SOURCE_PAYCHECK_ID           IN  VARCHAR2
  )    
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_PAYCHECK_SPLIT_LIST          PK_DIYDDTOPSP_PAYROLLS.refcur_paycheck_splits;
    
    PCS_PAYCHECK_GSEQ              NUMBER;
    PCS_SOURCE_DD_TXN_ID           VARCHAR2(100);
    PCS_PYCK_SPLIT_AMT             NUMBER;
    PCS_SOURCE_BANK_ACCT_ID        VARCHAR2(100);
    PCS_ACCT_NUM                   VARCHAR2(100);
    PCS_ACCT_TYPE_CD               VARCHAR2(100);
    PCS_BANK_NAME                  VARCHAR2(100);
    PCS_ROUTING_NUM                VARCHAR2(100);

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;

      PK_DIYDDTOPSP_PAYROLLS.migratePaycheckSplits (
        p_DIY_COMPANY_ID,
	    p_SOURCE_PAYCHECK_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_PAYCHECK_SPLIT_LIST
      );


      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* PAYCHECK SPLITS               ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      LOOP
          
        FETCH p_PAYCHECK_SPLIT_LIST INTO
          PCS_PAYCHECK_GSEQ,
          PCS_SOURCE_DD_TXN_ID,
          PCS_PYCK_SPLIT_AMT,
          PCS_SOURCE_BANK_ACCT_ID,
          PCS_ACCT_NUM,
          PCS_ACCT_TYPE_CD,
          PCS_BANK_NAME, 
          PCS_ROUTING_NUM;  
            
        EXIT WHEN p_PAYCHECK_SPLIT_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('PCGSEQ : ' || PCS_PAYCHECK_GSEQ);
        DBMS_OUTPUT.PUT_LINE('TXNSRC : ' || PCS_SOURCE_DD_TXN_ID    || ' - ' || LENGTH(PCS_SOURCE_DD_TXN_ID));
        DBMS_OUTPUT.PUT_LINE('SPLAMT : ' || PCS_PYCK_SPLIT_AMT);        
        DBMS_OUTPUT.PUT_LINE(' BASRC : ' || PCS_SOURCE_BANK_ACCT_ID || ' - ' || LENGTH(PCS_SOURCE_BANK_ACCT_ID));
        DBMS_OUTPUT.PUT_LINE('ACCNUM : ' || PCS_ACCT_NUM);        
        DBMS_OUTPUT.PUT_LINE('ACCTYP : ' || PCS_ACCT_TYPE_CD        || ' - ' || LENGTH(PCS_ACCT_TYPE_CD));
        DBMS_OUTPUT.PUT_LINE('BKNAME : ' || PCS_BANK_NAME           || ' - ' || LENGTH(PCS_BANK_NAME));
        DBMS_OUTPUT.PUT_LINE('RTNNUM : ' || PCS_ROUTING_NUM);                
          
      END LOOP;
          
  	  CLOSE p_PAYCHECK_SPLIT_LIST;
      
      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;


  END;


  PROCEDURE GetTxns (
    p_DIY_COMPANY_ID               IN  NUMBER,
    p_PAYROLL_RUN_ID               IN  VARCHAR2
  )    
  IS
    P_RETURN_CD                    NUMBER;
    P_RETURN_MSG                   VARCHAR2(500);

    p_PAYROLL_EE_TXN_LIST          PK_DIYDDTOPSP_PAYROLLS.refcur_pyrl_ee_txns;
    p_PAYROLL_ER_TXN_LIST          PK_DIYDDTOPSP_PAYROLLS.refcur_pyrl_er_txns;   
    p_PAYROLL_FEE_TXN_LIST         PK_DIYDDTOPSP_PAYROLLS.refcur_pyrl_fee_txns;
    p_PAYROLL_TAX_TXN_LIST         PK_DIYDDTOPSP_PAYROLLS.refcur_pyrl_tax_txns;
    
    FT_FINANCIAL_TXN_GSEQ    NUMBER;
    FT_Z_INS_DTTM            DATE;
    FT_TXN_TYPE_CD           VARCHAR2(100);
    FT_CURRENT_TXN_STATE_CD  VARCHAR2(100); 
    FT_FINANCIAL_TXN_AMT     NUMBER; 
    FT_SETTLEMENT_TYPE_CD    VARCHAR2(100); 
    FT_SETTLEMENT_DATE       DATE;
    PS_SOURCE_DD_TXN_ID      VARCHAR2(100); 
    SOURCE_BANK_ACCT_ID      VARCHAR2(100);

  BEGIN

    IF (P_DIY_COMPANY_ID IS NOT NULL) THEN

      P_RETURN_CD      := NULL;
      P_RETURN_MSG     := NULL;

      PK_DIYDDTOPSP_PAYROLLS.migratePayrollTransactions (
        p_DIY_COMPANY_ID,
	    p_PAYROLL_RUN_ID,
        p_RETURN_CD,
        p_RETURN_MSG,
        p_PAYROLL_EE_TXN_LIST,
        p_PAYROLL_ER_TXN_LIST,   
        p_PAYROLL_FEE_TXN_LIST,
        p_PAYROLL_TAX_TXN_LIST        
      );


      DBMS_OUTPUT.PUT_LINE('********************************');
      DBMS_OUTPUT.PUT_LINE('* FINANCIAL TRANSACTIONS        ');
      DBMS_OUTPUT.PUT_LINE('********************************');

      DBMS_OUTPUT.PUT_LINE('--> ee credit');      

      LOOP
          
        FETCH p_PAYROLL_EE_TXN_LIST INTO
          FT_FINANCIAL_TXN_GSEQ,
          FT_Z_INS_DTTM,
          FT_TXN_TYPE_CD,
          FT_CURRENT_TXN_STATE_CD, 
          FT_FINANCIAL_TXN_AMT, 
          FT_SETTLEMENT_TYPE_CD, 
          FT_SETTLEMENT_DATE,
          PS_SOURCE_DD_TXN_ID, 
          SOURCE_BANK_ACCT_ID;    
            
        EXIT WHEN p_PAYROLL_EE_TXN_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('FTGSEQ : ' || FT_FINANCIAL_TXN_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' INSDT : ' || FT_Z_INS_DTTM);
        DBMS_OUTPUT.PUT_LINE('TXNTYP : ' || FT_TXN_TYPE_CD          || ' - ' || LENGTH(FT_TXN_TYPE_CD));        
        DBMS_OUTPUT.PUT_LINE(' TXNST : ' || FT_CURRENT_TXN_STATE_CD || ' - ' || LENGTH(FT_CURRENT_TXN_STATE_CD));
        DBMS_OUTPUT.PUT_LINE('   AMT : ' || FT_FINANCIAL_TXN_AMT);
        DBMS_OUTPUT.PUT_LINE('STLTYP : ' || FT_SETTLEMENT_TYPE_CD   || ' - ' || LENGTH(FT_SETTLEMENT_TYPE_CD));
        DBMS_OUTPUT.PUT_LINE(' STLDT : ' || FT_SETTLEMENT_DATE);
        DBMS_OUTPUT.PUT_LINE('SRCTXN : ' || PS_SOURCE_DD_TXN_ID     || ' - ' || LENGTH(PS_SOURCE_DD_TXN_ID));
        DBMS_OUTPUT.PUT_LINE('SRCACC : ' || SOURCE_BANK_ACCT_ID     || ' - ' || LENGTH(SOURCE_BANK_ACCT_ID));
          
      END LOOP;


      DBMS_OUTPUT.PUT_LINE('--> er debit');      

      LOOP
          
        FETCH p_PAYROLL_ER_TXN_LIST INTO
          FT_FINANCIAL_TXN_GSEQ,
          FT_Z_INS_DTTM,
          FT_TXN_TYPE_CD,
          FT_CURRENT_TXN_STATE_CD, 
          FT_FINANCIAL_TXN_AMT, 
          FT_SETTLEMENT_TYPE_CD, 
          FT_SETTLEMENT_DATE,
          PS_SOURCE_DD_TXN_ID, 
          SOURCE_BANK_ACCT_ID;    
            
        EXIT WHEN p_PAYROLL_ER_TXN_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('FTGSEQ : ' || FT_FINANCIAL_TXN_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' INSDT : ' || FT_Z_INS_DTTM);
        DBMS_OUTPUT.PUT_LINE('TXNTYP : ' || FT_TXN_TYPE_CD          || ' - ' || LENGTH(FT_TXN_TYPE_CD));        
        DBMS_OUTPUT.PUT_LINE(' TXNST : ' || FT_CURRENT_TXN_STATE_CD || ' - ' || LENGTH(FT_CURRENT_TXN_STATE_CD));
        DBMS_OUTPUT.PUT_LINE('   AMT : ' || FT_FINANCIAL_TXN_AMT);
        DBMS_OUTPUT.PUT_LINE('STLTYP : ' || FT_SETTLEMENT_TYPE_CD   || ' - ' || LENGTH(FT_SETTLEMENT_TYPE_CD));
        DBMS_OUTPUT.PUT_LINE(' STLDT : ' || FT_SETTLEMENT_DATE);
        DBMS_OUTPUT.PUT_LINE('SRCTXN : ' || PS_SOURCE_DD_TXN_ID     || ' - ' || LENGTH(PS_SOURCE_DD_TXN_ID));
        DBMS_OUTPUT.PUT_LINE('SRCACC : ' || SOURCE_BANK_ACCT_ID     || ' - ' || LENGTH(SOURCE_BANK_ACCT_ID));
          
      END LOOP;


      DBMS_OUTPUT.PUT_LINE('--> er dd fees');      

      LOOP
          
        FETCH p_PAYROLL_FEE_TXN_LIST INTO
          FT_FINANCIAL_TXN_GSEQ,
          FT_Z_INS_DTTM,
          FT_TXN_TYPE_CD,
          FT_CURRENT_TXN_STATE_CD, 
          FT_FINANCIAL_TXN_AMT, 
          FT_SETTLEMENT_TYPE_CD, 
          FT_SETTLEMENT_DATE,
          PS_SOURCE_DD_TXN_ID, 
          SOURCE_BANK_ACCT_ID;    
            
        EXIT WHEN p_PAYROLL_FEE_TXN_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('FTGSEQ : ' || FT_FINANCIAL_TXN_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' INSDT : ' || FT_Z_INS_DTTM);
        DBMS_OUTPUT.PUT_LINE('TXNTYP : ' || FT_TXN_TYPE_CD          || ' - ' || LENGTH(FT_TXN_TYPE_CD));        
        DBMS_OUTPUT.PUT_LINE(' TXNST : ' || FT_CURRENT_TXN_STATE_CD || ' - ' || LENGTH(FT_CURRENT_TXN_STATE_CD));
        DBMS_OUTPUT.PUT_LINE('   AMT : ' || FT_FINANCIAL_TXN_AMT);
        DBMS_OUTPUT.PUT_LINE('STLTYP : ' || FT_SETTLEMENT_TYPE_CD   || ' - ' || LENGTH(FT_SETTLEMENT_TYPE_CD));
        DBMS_OUTPUT.PUT_LINE(' STLDT : ' || FT_SETTLEMENT_DATE);
        DBMS_OUTPUT.PUT_LINE('SRCTXN : ' || PS_SOURCE_DD_TXN_ID     || ' - ' || LENGTH(PS_SOURCE_DD_TXN_ID));
        DBMS_OUTPUT.PUT_LINE('SRCACC : ' || SOURCE_BANK_ACCT_ID     || ' - ' || LENGTH(SOURCE_BANK_ACCT_ID));
          
      END LOOP;


      DBMS_OUTPUT.PUT_LINE('--> er dd sales tax');      

      LOOP
          
        FETCH p_PAYROLL_TAX_TXN_LIST INTO
          FT_FINANCIAL_TXN_GSEQ,
          FT_Z_INS_DTTM,
          FT_TXN_TYPE_CD,
          FT_CURRENT_TXN_STATE_CD, 
          FT_FINANCIAL_TXN_AMT, 
          FT_SETTLEMENT_TYPE_CD, 
          FT_SETTLEMENT_DATE,
          PS_SOURCE_DD_TXN_ID, 
          SOURCE_BANK_ACCT_ID;    
            
        EXIT WHEN p_PAYROLL_TAX_TXN_LIST%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('FTGSEQ : ' || FT_FINANCIAL_TXN_GSEQ);
        DBMS_OUTPUT.PUT_LINE(' INSDT : ' || FT_Z_INS_DTTM);
        DBMS_OUTPUT.PUT_LINE('TXNTYP : ' || FT_TXN_TYPE_CD          || ' - ' || LENGTH(FT_TXN_TYPE_CD));        
        DBMS_OUTPUT.PUT_LINE(' TXNST : ' || FT_CURRENT_TXN_STATE_CD || ' - ' || LENGTH(FT_CURRENT_TXN_STATE_CD));
        DBMS_OUTPUT.PUT_LINE('   AMT : ' || FT_FINANCIAL_TXN_AMT);
        DBMS_OUTPUT.PUT_LINE('STLTYP : ' || FT_SETTLEMENT_TYPE_CD   || ' - ' || LENGTH(FT_SETTLEMENT_TYPE_CD));
        DBMS_OUTPUT.PUT_LINE(' STLDT : ' || FT_SETTLEMENT_DATE);
        DBMS_OUTPUT.PUT_LINE('SRCTXN : ' || PS_SOURCE_DD_TXN_ID     || ' - ' || LENGTH(PS_SOURCE_DD_TXN_ID));
        DBMS_OUTPUT.PUT_LINE('SRCACC : ' || SOURCE_BANK_ACCT_ID     || ' - ' || LENGTH(SOURCE_BANK_ACCT_ID));
          
      END LOOP;

          
  	  CLOSE p_PAYROLL_EE_TXN_LIST;
  	  CLOSE p_PAYROLL_ER_TXN_LIST;
  	  CLOSE p_PAYROLL_FEE_TXN_LIST;
  	  CLOSE p_PAYROLL_TAX_TXN_LIST;            

      
      DBMS_OUTPUT.PUT_LINE ('RC =' || P_RETURN_CD);
      DBMS_OUTPUT.PUT_LINE ('MSG=' || P_RETURN_MSG);

	ELSE
	  DBMS_OUTPUT.PUT_LINE ('Sorry, no companies found to migrate.');
	END IF;


  END;


  --
  -- OTHER TEST CASES
  --
  
  PROCEDURE TestCase_Util_MasterSKU
  IS
    v_temp_price_code              VARCHAR2(100);
    v_temp_offer_code              VARCHAR2(100);
    v_should_master_sku            VARCHAR2(100);
    v_is_master_sku                VARCHAR2(100);
    
  BEGIN

    -- FROM USE CASE 20, THE PRICE CODE MAPPING.
 
    -- DIYDD-FREE   Any         DIYDDSTD-3	   
    -- DIYDDSTD-3   Any         DIYDDSTD-3	   
    -- DIYDD-STD    Any         DIYDD-STD	   
    -- DIYDD-STD    FREEDD1YR   DIYDDSTD-3	   
    -- DIYDD-STD    P56152      DIYDDSTD-3	   
    -- DIYDD-STD    P14215      DIYDDSTD-3	 


    v_temp_price_code   := 'DIYDD-FREE';
    v_temp_offer_code   := '';            -- equals any
    v_should_master_sku := 'DIYDDSTD-3';    
    v_is_master_sku     := PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                           v_temp_price_code,
                           v_temp_offer_code
                         );
    
    SELECT DECODE (
             v_temp_offer_code,
             '', '<ANY>', 
             v_temp_offer_code
           )
    INTO v_temp_offer_code
    FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE ('----------------------------------');                     
    DBMS_OUTPUT.PUT_LINE ('Price Code          = ' || v_temp_price_code);
    DBMS_OUTPUT.PUT_LINE ('Offer Code          = ' || v_temp_offer_code);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (should) = ' || v_should_master_sku);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (is    ) = ' || v_is_master_sku);


    v_temp_price_code   := 'DIYDDSTD-3';
    v_temp_offer_code   := '';            -- equals any
    v_should_master_sku := 'DIYDDSTD-3';    
    v_is_master_sku     := PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                           v_temp_price_code,
                           v_temp_offer_code
                         );

    SELECT DECODE (
             v_temp_offer_code,
             '', '<ANY>', 
             v_temp_offer_code
           )
    INTO v_temp_offer_code
    FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE ('----------------------------------');                     
    DBMS_OUTPUT.PUT_LINE ('Price Code          = ' || v_temp_price_code);
    DBMS_OUTPUT.PUT_LINE ('Offer Code          = ' || v_temp_offer_code);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (should) = ' || v_should_master_sku);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (is    ) = ' || v_is_master_sku);    
        

    v_temp_price_code   := 'DIYDD-STD';
    v_temp_offer_code   := '';            -- equals any
    v_should_master_sku := 'DIYDD-STD';    
    v_is_master_sku     := PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                           v_temp_price_code,
                           v_temp_offer_code
                         );

    SELECT DECODE (
             v_temp_offer_code,
             '', '<ANY, other than FREEDD1YR, P56152, P14215>', 
             v_temp_offer_code
           )
    INTO v_temp_offer_code
    FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE ('----------------------------------');                     
    DBMS_OUTPUT.PUT_LINE ('Price Code          = ' || v_temp_price_code);
    DBMS_OUTPUT.PUT_LINE ('Offer Code          = ' || v_temp_offer_code);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (should) = ' || v_should_master_sku);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (is    ) = ' || v_is_master_sku);


    v_temp_price_code   := 'DIYDD-STD';
    v_temp_offer_code   := 'FREEDD1YR';
    v_should_master_sku := 'DIYDDSTD-3';    
    v_is_master_sku     := PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                           v_temp_price_code,
                           v_temp_offer_code
                         );

    SELECT DECODE (
             v_temp_offer_code,
             '', '<ANY>', 
             v_temp_offer_code
           )
    INTO v_temp_offer_code
    FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE ('----------------------------------');                     
    DBMS_OUTPUT.PUT_LINE ('Price Code          = ' || v_temp_price_code);
    DBMS_OUTPUT.PUT_LINE ('Offer Code          = ' || v_temp_offer_code);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (should) = ' || v_should_master_sku);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (is    ) = ' || v_is_master_sku);    


    v_temp_price_code   := 'DIYDD-STD';
    v_temp_offer_code   := 'P56152';
    v_should_master_sku := 'DIYDDSTD-3';    
    v_is_master_sku     := PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                           v_temp_price_code,
                           v_temp_offer_code
                         );

    SELECT DECODE (
             v_temp_offer_code,
             '', '<ANY>', 
             v_temp_offer_code
           )
    INTO v_temp_offer_code
    FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE ('----------------------------------');                     
    DBMS_OUTPUT.PUT_LINE ('Price Code          = ' || v_temp_price_code);
    DBMS_OUTPUT.PUT_LINE ('Offer Code          = ' || v_temp_offer_code);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (should) = ' || v_should_master_sku);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (is    ) = ' || v_is_master_sku);    


    v_temp_price_code   := 'DIYDD-STD';
    v_temp_offer_code   := 'P14215';
    v_should_master_sku := 'DIYDDSTD-3';    
    v_is_master_sku     := PK_DIYDDTOPSP_UTILS.FNGetPSPMasterSKU (
                           v_temp_price_code,
                           v_temp_offer_code
                         );

    SELECT DECODE (
             v_temp_offer_code,
             '', '<ANY>', 
             v_temp_offer_code
           )
    INTO v_temp_offer_code
    FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE ('----------------------------------');                     
    DBMS_OUTPUT.PUT_LINE ('Price Code          = ' || v_temp_price_code);
    DBMS_OUTPUT.PUT_LINE ('Offer Code          = ' || v_temp_offer_code);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (should) = ' || v_should_master_sku);
    DBMS_OUTPUT.PUT_LINE ('Master Sku (is    ) = ' || v_is_master_sku);    
        
    
  END;
    

END PK_TEST_DIY_MIGRATION_APIS; 
/


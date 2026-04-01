--------------------------------------------------------
--  DDL for Function ACQUIRE_REALM_WRITE_LOCK
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."ACQUIRE_REALM_WRITE_LOCK" (lockHandle varchar2) RETURN integer
AS
    lockStatus number;
BEGIN
    lockStatus := dbms_lock.request(
        lockhandle => lockHandle,
        lockmode => dbms_lock.x_mode,
        timeout => dbms_lock.maxwait,
        release_on_commit => true
    );
    RETURN lockStatus;
END;

/

  GRANT EXECUTE ON "PSPADM"."ACQUIRE_REALM_WRITE_LOCK" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function ALLOCATE_REALM_LOCK
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."ALLOCATE_REALM_LOCK" (lockName varchar2) RETURN varchar2
AS
    PRAGMA AUTONOMOUS_TRANSACTION;
    lockHandle varchar2(128);
BEGIN
    dbms_lock.allocate_unique(
        lockname => lockName,
        lockhandle => lockHandle
     );
    RETURN lockHandle;
END;

/

  GRANT EXECUTE ON "PSPADM"."ALLOCATE_REALM_LOCK" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_FORMAT_SYSGUID
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_FORMAT_SYSGUID" (pSysGuid IN RAW) RETURN VARCHAR2
DETERMINISTIC
AS
fmtGuid VARCHAR2(200);
BEGIN
   SELECT SUBSTR(pSysGuid, 1, 8) || '-' || SUBSTR(pSysGuid, 9, 4) || '-' || SUBSTR(pSysGuid, 13, 4) || '-' || SUBSTR(pSysGuid, 17, 4) || '-' || SUBSTR(pSysGuid, 21)
   INTO fmtGuid FROM DUAL;
   SELECT LOWER(fmtGuid) INTO fmtGuid FROM DUAL;
   RETURN fmtGuid;
END FN_FORMAT_SYSGUID; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_FORMAT_SYSGUID" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_GET_EDR_AMOUNT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_EDR_AMOUNT" (p_date DATE) RETURN NUMBER
AS


/******************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
******************************************************************************/

    v_EDR_AMOUNT number(15,2);
BEGIN

                SELECT
                      SUM(amount) INTO     v_EDR_AMOUNT
                 FROM PSP_ENTRY_DETAIL_RECORD rec0
                      INNER JOIN PSP_NACHAFILE nf ON NF.NACHAFILE_SEQ = REC0.N_A_C_H_A_FILE_FK
                      INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = NF.OFFLOAD_BATCH_FK
                WHERE
                    ob.STATUS_CD =  'InProcess'
                    AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
                    AND OB.OFFLOAD_DATE = SYS_EXTRACT_UTC(TO_TIMESTAMP(p_date))
                    AND rec0.initiation_date = SYS_EXTRACT_UTC(TO_TIMESTAMP(p_date));
   RETURN     v_EDR_AMOUNT;
END FN_GET_EDR_AMOUNT;

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_EDR_AMOUNT" TO "PSPAPP_ROLE";
  GRANT EXECUTE ON "PSPADM"."FN_GET_EDR_AMOUNT" TO "PSPAPP";
--------------------------------------------------------
--  DDL for Function FN_GET_EDR_COUNT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_EDR_COUNT" (p_date VARCHAR2) RETURN NUMBER
AS


/******************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
   1.1          03.10.2010  Jeff Jones       Removed Sys_Extract_UTC function and
                                           changed input parm to VARCHAR2
******************************************************************************/

    Nbr_EDR_RECORDS number(15);
BEGIN

                SELECT
                        count(*) INTO     Nbr_EDR_RECORDS
                 FROM PSP_ENTRY_DETAIL_RECORD rec0
                      INNER JOIN PSP_NACHAFILE nf ON NF.NACHAFILE_SEQ = REC0.N_A_C_H_A_FILE_FK
                      INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = NF.OFFLOAD_BATCH_FK
                WHERE
                    ob.STATUS_CD =  'InProcess'
                    AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
                    AND OB.OFFLOAD_DATE = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"')
                    AND rec0.initiation_date = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"');
   RETURN     Nbr_EDR_RECORDS;
END FN_GET_EDR_COUNT;

 /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_EDR_COUNT" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_GET_ENV
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_ENV" RETURN VARCHAR2
AS

/******************************************************************************
   PURPOSE: Get the current environment
******************************************************************************/

  ENV VARCHAR2(200);
BEGIN
  SELECT (CASE
              WHEN UPPER(sys_context('userenv', 'service_name')) in ('PSPPROD', 'PSPDR') THEN 'PROD'
              WHEN UPPER(sys_context('userenv', 'db_name')) in
                   ('PSPUWP01','PSPUWP02','PSPUEP01','PSPUEP02', 'PSPPP001', 'PSPSP001', 'PSPWP001', 'PSPEP001', 'PSPTS005', 'PSPTSIB5', 'PSPUE005', 'PSPUEIB5')
                  THEN 'PROD'
              ELSE 'NONPROD' END) INTO ENV
    from dual;
  RETURN ENV;
END FN_GET_ENV; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_ENV" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_GET_LAST_DAY_OF_QUARTER
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_LAST_DAY_OF_QUARTER" (p_input TIMESTAMP) RETURN TIMESTAMP
AS
  ts TIMESTAMP;
  v_tz_offset VARCHAR2 (100 Char);
BEGIN

   SELECT SYSTEM_PARAMETER_VALUE
    INTO v_tz_offset
    FROM PSP_SYSTEM_PARAMETER
       WHERE SYSTEM_PARAMETER_CD='PSP_DATE_TIMEZONE_OFFSET';

   -- Find the last day of the quarter for the given date and subtract the PSP TZ offset.
   SELECT sys_extract_utc(to_timestamp(add_months(trunc(p_input,'Q'),3) - 1) - NUMTODSINTERVAL(((TO_NUMBER(v_tz_offset) * 3600 * 1000))/1000, 'SECOND'))
    INTO ts
    FROM DUAL;

   RETURN ts;
END FN_GET_LAST_DAY_OF_QUARTER; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_LAST_DAY_OF_QUARTER" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_GET_LEDGER_BALANCE
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_LEDGER_BALANCE" (
   p_company   VARCHAR2,
   p_ledger    VARCHAR2
)
   RETURN NUMBER
AS
/******************************************************************************
   PURPOSE: Return current ledger balance calculated from the most recent psp_ledger_balance and
   any financial transaction states that have been made since then (i.e. today)

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        05.04.2010  David            Created
   1.1        09.07.2010  David/Tushar     Removed trunc in second query since
                                           it was pulling too many partitions
   1.2        09.20.2010  David/Tushar     Changed second part to use max date from all
                                           ledger accounts since it was using all
                                           partitions when there was no data in balance table
******************************************************************************/
   v_lb_date       TIMESTAMP;
   v_lb_balance    NUMBER;
   v_fts_balance   NUMBER;
BEGIN
   BEGIN
      SELECT balance_date, balance_amount
        INTO v_lb_date, v_lb_balance
        FROM psp_ledger_balance lb
       WHERE lb.company_fk = p_company
         AND lb.ledger_account_fk = p_ledger
         AND trunc(lb.balance_date) =
                (SELECT trunc(MAX (lb2.balance_date))
                   FROM psp_ledger_balance lb2
                  WHERE lb2.company_fk = p_company
                    AND lb2.ledger_account_fk = p_ledger);
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         NULL;
   END;


   SELECT SUM (  ft.financial_transaction_amount
               * DECODE (pr.credit_debit_ind,
                         'C', DECODE (la.balance_calculation_rule,
                                      'CreditAddsToBalance', 1,
                                      -1
                                     ),
                         'D', DECODE (la.balance_calculation_rule,
                                      'DebitAddsToBalance', 1,
                                      -1
                                     )
                        )
              )
     INTO v_fts_balance
     FROM psp_financial_trans_state fts,
          psp_financial_transaction ft,
          psp_posting_rule pr,
          psp_ledger_account la
    WHERE fts.financial_transaction_fk = ft.financial_transaction_seq
      AND pr.transaction_state_fk = fts.transaction_state_fk
      AND pr.transaction_type_fk = ft.transaction_type_fk
      AND la.ledger_account_cd = pr.ledger_account_fk
      AND fts.transaction_state_eff_date > (select nvl(trunc(max(balance_date)) + 1, to_date('01/01/1970', 'MM/DD/YYYY')) from psp_ledger_balance)
      AND fts.company_fk = p_company
      AND pr.ledger_account_fk = p_ledger;


   RETURN (NVL (v_lb_balance, 0) + NVL (v_fts_balance, 0));

END fn_get_ledger_balance; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO "PSPAPP_ROLE";
  GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO "PSPAPP";
  GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO "PSPREPORTS";
  GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO "PSPREAD_AWS";
  GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO "KMUTHURANGAM";
--------------------------------------------------------
--  DDL for Function FN_GET_MMT_COUNT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_MMT_COUNT" (p_date DATE) RETURN NUMBER
AS

/******************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
******************************************************************************/

    Nbr_MMT_RECORDS number(15);
BEGIN

        SELECT
        	COUNT (*) INTO Nbr_MMT_RECORDS
          FROM psp_money_movement_transaction mmt
               INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = mmt.OFFLOAD_BATCH_FK
         WHERE
           ob.STATUS_CD =  'InProcess'
           AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
           AND OB.OFFLOAD_DATE = SYS_EXTRACT_UTC (TO_TIMESTAMP (p_date))
           AND mmt.initiation_date = SYS_EXTRACT_UTC (TO_TIMESTAMP (p_date))
           AND mmt.money_movement_payment_method = 'ACHDirectDeposit';

   RETURN     Nbr_MMT_RECORDS;

END FN_GET_MMT_COUNT; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_MMT_COUNT" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_GET_PSID
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_PSID" (p_state IN VARCHAR)
    RETURN psp_company.source_company_id%type
AS
    psid psp_company.source_company_id%type;
BEGIN

    if (p_state = 'AL') then select PSID_AL.nextval into psid from dual;
    elsif (p_state = 'AK') then select PSID_AK.nextval into psid from dual;
    elsif (p_state = 'AZ') then select PSID_AZ.nextval into psid from dual;
    elsif (p_state = 'AR') then select PSID_AR.nextval into psid from dual;
    elsif (p_state = 'CA') then select PSID_CA.nextval into psid from dual;
    elsif (p_state = 'CO') then select PSID_CO.nextval into psid from dual;
    elsif (p_state = 'CT') then select PSID_CT.nextval into psid from dual;
    elsif (p_state = 'DE') then select PSID_DE.nextval into psid from dual;
    elsif (p_state = 'DC') then select PSID_DC.nextval into psid from dual;
    elsif (p_state = 'FL') then select PSID_FL.nextval into psid from dual;
    elsif (p_state = 'GA') then select PSID_GA.nextval into psid from dual;
    elsif (p_state = 'HI') then select PSID_HI.nextval into psid from dual;
    elsif (p_state = 'ID') then select PSID_ID.nextval into psid from dual;
    elsif (p_state = 'IL') then select PSID_IL.nextval into psid from dual;
    elsif (p_state = 'IN') then select PSID_IN.nextval into psid from dual;
    elsif (p_state = 'IA') then select PSID_IA.nextval into psid from dual;
    elsif (p_state = 'KS') then select PSID_KS.nextval into psid from dual;
    elsif (p_state = 'KY') then select PSID_KY.nextval into psid from dual;
    elsif (p_state = 'LA') then select PSID_LA.nextval into psid from dual;
    elsif (p_state = 'ME') then select PSID_ME.nextval into psid from dual;
    elsif (p_state = 'MD') then select PSID_MD.nextval into psid from dual;
    elsif (p_state = 'MA') then select PSID_MA.nextval into psid from dual;
    elsif (p_state = 'MI') then select PSID_MI.nextval into psid from dual;
    elsif (p_state = 'MN') then select PSID_MN.nextval into psid from dual;
    elsif (p_state = 'MS') then select PSID_MS.nextval into psid from dual;
    elsif (p_state = 'MO') then select PSID_MO.nextval into psid from dual;
    elsif (p_state = 'MT') then select PSID_MT.nextval into psid from dual;
    elsif (p_state = 'NE') then select PSID_NE.nextval into psid from dual;
    elsif (p_state = 'NV') then select PSID_NV.nextval into psid from dual;
    elsif (p_state = 'NH') then select PSID_NH.nextval into psid from dual;
    elsif (p_state = 'NJ') then select PSID_NJ.nextval into psid from dual;
    elsif (p_state = 'NM') then select PSID_NM.nextval into psid from dual;
    elsif (p_state = 'NY') then select PSID_NY.nextval into psid from dual;
    elsif (p_state = 'NC') then select PSID_NC.nextval into psid from dual;
    elsif (p_state = 'ND') then select PSID_ND.nextval into psid from dual;
    elsif (p_state = 'OH') then select PSID_OH.nextval into psid from dual;
    elsif (p_state = 'OK') then select PSID_OK.nextval into psid from dual;
    elsif (p_state = 'OR') then select PSID_OR.nextval into psid from dual;
    elsif (p_state = 'PA') then select PSID_PA.nextval into psid from dual;
    elsif (p_state = 'RI') then select PSID_RI.nextval into psid from dual;
    elsif (p_state = 'SC') then select PSID_SC.nextval into psid from dual;
    elsif (p_state = 'SD') then select PSID_SD.nextval into psid from dual;
    elsif (p_state = 'TN') then select PSID_TN.nextval into psid from dual;
    elsif (p_state = 'TX') then select PSID_TX.nextval into psid from dual;
    elsif (p_state = 'UT') then select PSID_UT.nextval into psid from dual;
    elsif (p_state = 'VT') then select PSID_VT.nextval into psid from dual;
    elsif (p_state = 'VA') then select PSID_VA.nextval into psid from dual;
    elsif (p_state = 'WA') then select PSID_WA.nextval into psid from dual;
    elsif (p_state = 'WV') then select PSID_WV.nextval into psid from dual;
    elsif (p_state = 'WI') then select PSID_WI.nextval into psid from dual;
    elsif (p_state = 'WY') then select PSID_WY.nextval into psid from dual;
    else select PSID_DEFAULT.nextval into psid from dual;
    end if;

   RETURN psid;

END FN_GET_PSID; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_PSID" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function FN_GET_PSP_TIMESTAMP
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."FN_GET_PSP_TIMESTAMP" (forTrigger IN NUMBER DEFAULT 1) RETURN TIMESTAMP
AS
ts TIMESTAMP;
v_offset VARCHAR2 (100 Char);
v_tz_offset VARCHAR2 (100 Char);
BEGIN

   SELECT SYSTEM_PARAMETER_VALUE
     INTO v_offset
	 FROM PSP_SYSTEM_PARAMETER
	WHERE SYSTEM_PARAMETER_CD='PSP_DATE_OFFSET';

   v_tz_offset:='+00.00';

   If (forTrigger=0) THEN
       SELECT SYSTEM_PARAMETER_VALUE
	INTO v_tz_offset
	FROM PSP_SYSTEM_PARAMETER
       WHERE SYSTEM_PARAMETER_CD='PSP_DATE_TIMEZONE_OFFSET';
   END IF;

   SELECT SYSTIMESTAMP + NUMTODSINTERVAL((TO_NUMBER(v_offset) + (TO_NUMBER(v_tz_offset) * 3600 * 1000))/1000, 'SECOND')
    INTO ts
    FROM DUAL;

   RETURN ts;
END FN_GET_PSP_TIMESTAMP; /* GOLDENGATE_DDL_REPLICATION */

/

  GRANT EXECUTE ON "PSPADM"."FN_GET_PSP_TIMESTAMP" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function TEMP_GET_LEDGER_BALANCE
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."TEMP_GET_LEDGER_BALANCE" (
   p_company   VARCHAR2,
   p_ledger    VARCHAR2
)
   RETURN NUMBER
AS
/******************************************************************************
   PURPOSE: Return current ledger balance calculated from the most recent psp_ledger_balance and
   any financial transaction states that have been made since then (i.e. today)

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        05.04.2010  David            Created
   1.1        09.07.2010  David/Tushar     Removed trunc in second query since
                                           it was pulling too many partitions
******************************************************************************/
   v_lb_date       TIMESTAMP;
   v_lb_balance    NUMBER;
   v_fts_balance   NUMBER;
BEGIN
   BEGIN
      SELECT balance_date, balance_amount
        INTO v_lb_date, v_lb_balance
        FROM psp_ledger_balance lb
       WHERE lb.company_fk = p_company
         AND lb.ledger_account_fk = p_ledger
         AND trunc(lb.balance_date) =
                (SELECT trunc(MAX (lb2.balance_date))
                   FROM psp_ledger_balance lb2
                  WHERE lb2.company_fk = p_company
                    AND lb2.ledger_account_fk = p_ledger);
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         NULL;
   END;

   SELECT SUM (  ft.financial_transaction_amount
               * DECODE (pr.credit_debit_ind,
                         'C', DECODE (la.balance_calculation_rule,
                                      'CreditAddsToBalance', 1,
                                      -1
                                     ),
                         'D', DECODE (la.balance_calculation_rule,
                                      'DebitAddsToBalance', 1,
                                      -1
                                     )
                        )
              )
     INTO v_fts_balance
     FROM psp_financial_trans_state fts,
          psp_financial_transaction ft,
          psp_posting_rule pr,
          psp_ledger_account la
    WHERE fts.financial_transaction_fk = ft.financial_transaction_seq
      AND pr.transaction_state_fk = fts.transaction_state_fk
      AND pr.transaction_type_fk = ft.transaction_type_fk
      AND la.ledger_account_cd = pr.ledger_account_fk
      AND fts.transaction_state_eff_date > (select trunc(max(balance_date)) + 1 from psp_ledger_balance) 
      AND ft.company_fk = p_company
      AND pr.ledger_account_fk = p_ledger;

   RETURN (NVL (v_lb_balance, 0) + NVL (v_fts_balance, 0));
END temp_get_ledger_balance; 

/

  GRANT EXECUTE ON "PSPADM"."TEMP_GET_LEDGER_BALANCE" TO "PSPAPP_ROLE";
--------------------------------------------------------
--  DDL for Function TIME_DIFF
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "PSPADM"."TIME_DIFF" (
DATE_1 IN DATE, DATE_2 IN DATE) RETURN NUMBER IS

NDATE_1   NUMBER;
NDATE_2   NUMBER;
NSECOND_1 NUMBER(5,0);
NSECOND_2 NUMBER(5,0);

BEGIN
  -- Get Julian date number from first date (DATE_1)
  NDATE_1 := TO_NUMBER(TO_CHAR(DATE_1, 'J'));

  -- Get Julian date number from second date (DATE_2)
  NDATE_2 := TO_NUMBER(TO_CHAR(DATE_2, 'J'));

  -- Get seconds since midnight from first date (DATE_1)
  NSECOND_1 := TO_NUMBER(TO_CHAR(DATE_1, 'SSSSS'));

  -- Get seconds since midnight from second date (DATE_2)
  NSECOND_2 := TO_NUMBER(TO_CHAR(DATE_2, 'SSSSS'));

  RETURN (((NDATE_2 - NDATE_1) * 86400)+(NSECOND_2 - NSECOND_1));
END time_diff;

/

  GRANT EXECUTE ON "PSPADM"."TIME_DIFF" TO "PSPAPP_ROLE";
  GRANT EXECUTE ON "PSPADM"."TIME_DIFF" TO "PSPREAD";
  GRANT EXECUTE ON "PSPADM"."TIME_DIFF" TO "PSPREAD_AWS";

-- ------------ Write CREATE-FUNCTION-stage scripts -----------

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_format_sysguid(IN psysguid BYTEA)
RETURNS TEXT
AS
$BODY$
DECLARE
    fmtGuid CHARACTER VARYING(200);
BEGIN
    SELECT
        CONCAT_WS('', aws_oracle_ext.substr(pSysGuid, 1, 8), '-', aws_oracle_ext.substr(pSysGuid, 9, 4), '-', aws_oracle_ext.substr(pSysGuid, 13, 4), '-', aws_oracle_ext.substr(pSysGuid, 17, 4), '-', aws_oracle_ext.substr(pSysGuid, 21))
        INTO STRICT fmtGuid;
    SELECT
        LOWER(fmtGuid)
        INTO STRICT fmtGuid;
    RETURN fmtGuid;
END;
$BODY$
LANGUAGE  plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_edr_count(IN p_date TEXT)
RETURNS DOUBLE PRECISION
AS
$BODY$
/*
*****************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
   1.1          03.10.2010  Jeff Jones       Removed Sys_Extract_UTC function and
                                           changed input parm to VARCHAR2
*****************************************************************************
*/
DECLARE
    Nbr_EDR_RECORDS NUMERIC(15);
BEGIN
    SELECT
        COUNT(*)
        INTO STRICT Nbr_EDR_RECORDS
        FROM psp_entry_detail_record AS rec0
        INNER JOIN psp_nachafile AS nf
            ON nf.nachafile_seq = rec0.n_a_c_h_a_file_fk
        INNER JOIN psp_offload_batch AS ob
            ON ob.offload_batch_seq = nf.offload_batch_fk
        WHERE ob.status_cd = 'InProcess' AND ob.offload_group_fk = (SELECT
            offload_group_seq
            FROM psp_offload_group
            WHERE offload_group_cd = 'STD'
            LIMIT 1) AND ob.offload_date = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"') AND rec0.initiation_date = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"');
    RETURN Nbr_EDR_RECORDS;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_env()
RETURNS TEXT
AS
$BODY$
/*
*****************************************************************************
   PURPOSE: Get the current environment
*****************************************************************************
*/
DECLARE
    ENV CHARACTER VARYING(200);
BEGIN
    SELECT
        (CASE
            WHEN aws_oracle_ext.SYS_CONTEXT('userenv', 'service_name') IN ('pspprod', 'pspdr') THEN 'PROD'
            ELSE 'NONPROD'
        END)
        INTO STRICT ENV;
    RETURN ENV;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_last_day_of_quarter(IN p_input TIMESTAMP WITHOUT TIME ZONE)
RETURNS TIMESTAMP
AS
$BODY$
DECLARE
    ts TIMESTAMP(6) WITHOUT TIME ZONE;
    v_tz_offset CHARACTER VARYING(100);
BEGIN
    SELECT
        system_parameter_value
        INTO STRICT v_tz_offset
        FROM psp_system_parameter
        WHERE system_parameter_cd = 'PSP_DATE_TIMEZONE_OFFSET';
    /* Find the last day of the quarter for the given date and subtract the PSP TZ offset. */
    
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT sys_extract_utc(to_timestamp(add_months(trunc(p_input,'Q'),3) - 1) - NUMTODSINTERVAL(((TO_NUMBER(v_tz_offset) * 3600 * 1000))/1000, 'SECOND'))
        INTO ts
        FROM DUAL
    */
    RETURN ts;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_ledger_balance(IN p_company TEXT, IN p_ledger TEXT)
AS
$BODY$
/*
*****************************************************************************
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
*****************************************************************************
*/
DECLARE
    v_lb_date TIMESTAMP(6) WITHOUT TIME ZONE;
    v_lb_balance DOUBLE PRECISION;
    v_fts_balance DOUBLE PRECISION;
BEGIN
    BEGIN
        SELECT
            balance_date, balance_amount
            INTO STRICT v_lb_date, v_lb_balance
            FROM psp_ledger_balance AS lb
            WHERE lb.company_fk = p_company AND lb.ledger_account_fk = p_ledger AND TRUNC(lb.balance_date::TIMESTAMP WITHOUT TIME ZONE) = (SELECT
                TRUNC(MAX(lb2.balance_date)::TIMESTAMP WITHOUT TIME ZONE)
                FROM psp_ledger_balance AS lb2
                WHERE lb2.company_fk = p_company AND lb2.ledger_account_fk = p_ledger);
        EXCEPTION
            WHEN no_data_found THEN
                NULL;
    END;
    SELECT
        SUM(ft.financial_transaction_amount *
        CASE pr.credit_debit_ind
            WHEN 'C' THEN
            CASE la.balance_calculation_rule
                WHEN 'CreditAddsToBalance' THEN 1
                ELSE - 1
            END
            WHEN 'D' THEN
            CASE la.balance_calculation_rule
                WHEN 'DebitAddsToBalance' THEN 1
                ELSE - 1
            END
        END)
        INTO STRICT v_fts_balance
        FROM psp_financial_trans_state AS fts, psp_financial_transaction AS ft, psp_posting_rule AS pr, psp_ledger_account AS la
        WHERE fts.financial_transaction_fk = ft.financial_transaction_seq AND pr.transaction_state_fk = fts.transaction_state_fk AND pr.transaction_type_fk = ft.transaction_type_fk AND la.ledger_account_cd = pr.ledger_account_fk AND fts.transaction_state_eff_date > (SELECT
            COALESCE(TRUNC(MAX(balance_date)::TIMESTAMP WITHOUT TIME ZONE) + (1::NUMERIC || ' days')::INTERVAL, (SELECT
                aws_oracle_ext.TO_DATE('01/01/1970', 'MM/DD/YYYY')))
            FROM psp_ledger_balance) AND fts.company_fk = p_company AND pr.ledger_account_fk = p_ledger;
    RETURN (COALESCE(v_lb_balance, 0) + COALESCE(v_fts_balance, 0));
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_mmt_count(IN p_date TIMESTAMP WITHOUT TIME ZONE)
RETURNS DOUBLE PRECISION
AS
$BODY$
/*
*****************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
*****************************************************************************
*/
DECLARE
    Nbr_MMT_RECORDS NUMERIC(15);
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT
            	COUNT (*) INTO Nbr_MMT_RECORDS
              FROM psp_money_movement_transaction mmt
                   INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = mmt.OFFLOAD_BATCH_FK
             WHERE
               ob.STATUS_CD =  'InProcess'
               AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
               AND OB.OFFLOAD_DATE = SYS_EXTRACT_UTC (TO_TIMESTAMP (p_date))
               AND mmt.initiation_date = SYS_EXTRACT_UTC (TO_TIMESTAMP (p_date))
               AND mmt.money_movement_payment_method = 'ACHDirectDeposit'
    */
    RETURN Nbr_MMT_RECORDS;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_psid(IN p_state TEXT)
RETURNS CHARACTER VARYING
AS
$BODY$
DECLARE
    psid ibobadm_pds.psp_company.source_company_id%TYPE;
BEGIN
    IF (p_state = 'AL') THEN
        SELECT
            nextval('ibobadm_pds.psid_al')
            INTO STRICT psid;
    ELSIF (p_state = 'AK') THEN
        SELECT
            nextval('ibobadm_pds.psid_ak')
            INTO STRICT psid;
    ELSIF (p_state = 'AZ') THEN
        SELECT
            nextval('ibobadm_pds.psid_az')
            INTO STRICT psid;
    ELSIF (p_state = 'AR') THEN
        SELECT
            nextval('ibobadm_pds.psid_ar')
            INTO STRICT psid;
    ELSIF (p_state = 'CA') THEN
        SELECT
            nextval('ibobadm_pds.psid_ca')
            INTO STRICT psid;
    ELSIF (p_state = 'CO') THEN
        SELECT
            nextval('ibobadm_pds.psid_co')
            INTO STRICT psid;
    ELSIF (p_state = 'CT') THEN
        SELECT
            nextval('ibobadm_pds.psid_ct')
            INTO STRICT psid;
    ELSIF (p_state = 'DE') THEN
        SELECT
            nextval('ibobadm_pds.psid_de')
            INTO STRICT psid;
    ELSIF (p_state = 'DC') THEN
        SELECT
            nextval('ibobadm_pds.psid_dc')
            INTO STRICT psid;
    ELSIF (p_state = 'FL') THEN
        SELECT
            nextval('ibobadm_pds.psid_fl')
            INTO STRICT psid;
    ELSIF (p_state = 'GA') THEN
        SELECT
            nextval('ibobadm_pds.psid_ga')
            INTO STRICT psid;
    ELSIF (p_state = 'HI') THEN
        SELECT
            nextval('ibobadm_pds.psid_hi')
            INTO STRICT psid;
    ELSIF (p_state = 'ID') THEN
        SELECT
            nextval('ibobadm_pds.psid_id')
            INTO STRICT psid;
    ELSIF (p_state = 'IL') THEN
        SELECT
            nextval('ibobadm_pds.psid_il')
            INTO STRICT psid;
    ELSIF (p_state = 'IN') THEN
        SELECT
            nextval('ibobadm_pds.psid_in')
            INTO STRICT psid;
    ELSIF (p_state = 'IA') THEN
        SELECT
            nextval('ibobadm_pds.psid_ia')
            INTO STRICT psid;
    ELSIF (p_state = 'KS') THEN
        SELECT
            nextval('ibobadm_pds.psid_ks')
            INTO STRICT psid;
    ELSIF (p_state = 'KY') THEN
        SELECT
            nextval('ibobadm_pds.psid_ky')
            INTO STRICT psid;
    ELSIF (p_state = 'LA') THEN
        SELECT
            nextval('ibobadm_pds.psid_la')
            INTO STRICT psid;
    ELSIF (p_state = 'ME') THEN
        SELECT
            nextval('ibobadm_pds.psid_me')
            INTO STRICT psid;
    ELSIF (p_state = 'MD') THEN
        SELECT
            nextval('ibobadm_pds.psid_md')
            INTO STRICT psid;
    ELSIF (p_state = 'MA') THEN
        SELECT
            nextval('ibobadm_pds.psid_ma')
            INTO STRICT psid;
    ELSIF (p_state = 'MI') THEN
        SELECT
            nextval('ibobadm_pds.psid_mi')
            INTO STRICT psid;
    ELSIF (p_state = 'MN') THEN
        SELECT
            nextval('ibobadm_pds.psid_mn')
            INTO STRICT psid;
    ELSIF (p_state = 'MS') THEN
        SELECT
            nextval('ibobadm_pds.psid_ms')
            INTO STRICT psid;
    ELSIF (p_state = 'MO') THEN
        SELECT
            nextval('ibobadm_pds.psid_mo')
            INTO STRICT psid;
    ELSIF (p_state = 'MT') THEN
        SELECT
            nextval('ibobadm_pds.psid_mt')
            INTO STRICT psid;
    ELSIF (p_state = 'NE') THEN
        SELECT
            nextval('ibobadm_pds.psid_ne')
            INTO STRICT psid;
    ELSIF (p_state = 'NV') THEN
        SELECT
            nextval('ibobadm_pds.psid_nv')
            INTO STRICT psid;
    ELSIF (p_state = 'NH') THEN
        SELECT
            nextval('ibobadm_pds.psid_nh')
            INTO STRICT psid;
    ELSIF (p_state = 'NJ') THEN
        SELECT
            nextval('ibobadm_pds.psid_nj')
            INTO STRICT psid;
    ELSIF (p_state = 'NM') THEN
        SELECT
            nextval('ibobadm_pds.psid_nm')
            INTO STRICT psid;
    ELSIF (p_state = 'NY') THEN
        SELECT
            nextval('ibobadm_pds.psid_ny')
            INTO STRICT psid;
    ELSIF (p_state = 'NC') THEN
        SELECT
            nextval('ibobadm_pds.psid_nc')
            INTO STRICT psid;
    ELSIF (p_state = 'ND') THEN
        SELECT
            nextval('ibobadm_pds.psid_nd')
            INTO STRICT psid;
    ELSIF (p_state = 'OH') THEN
        SELECT
            nextval('ibobadm_pds.psid_oh')
            INTO STRICT psid;
    ELSIF (p_state = 'OK') THEN
        SELECT
            nextval('ibobadm_pds.psid_ok')
            INTO STRICT psid;
    ELSIF (p_state = 'OR') THEN
        SELECT
            nextval('ibobadm_pds.psid_or')
            INTO STRICT psid;
    ELSIF (p_state = 'PA') THEN
        SELECT
            nextval('ibobadm_pds.psid_pa')
            INTO STRICT psid;
    ELSIF (p_state = 'RI') THEN
        SELECT
            nextval('ibobadm_pds.psid_ri')
            INTO STRICT psid;
    ELSIF (p_state = 'SC') THEN
        SELECT
            nextval('ibobadm_pds.psid_sc')
            INTO STRICT psid;
    ELSIF (p_state = 'SD') THEN
        SELECT
            nextval('ibobadm_pds.psid_sd')
            INTO STRICT psid;
    ELSIF (p_state = 'TN') THEN
        SELECT
            nextval('ibobadm_pds.psid_tn')
            INTO STRICT psid;
    ELSIF (p_state = 'TX') THEN
        SELECT
            nextval('ibobadm_pds.psid_tx')
            INTO STRICT psid;
    ELSIF (p_state = 'UT') THEN
        SELECT
            nextval('ibobadm_pds.psid_ut')
            INTO STRICT psid;
    ELSIF (p_state = 'VT') THEN
        SELECT
            nextval('ibobadm_pds.psid_vt')
            INTO STRICT psid;
    ELSIF (p_state = 'VA') THEN
        SELECT
            nextval('ibobadm_pds.psid_va')
            INTO STRICT psid;
    ELSIF (p_state = 'WA') THEN
        SELECT
            nextval('ibobadm_pds.psid_wa')
            INTO STRICT psid;
    ELSIF (p_state = 'WV') THEN
        SELECT
            nextval('ibobadm_pds.psid_wv')
            INTO STRICT psid;
    ELSIF (p_state = 'WI') THEN
        SELECT
            nextval('ibobadm_pds.psid_wi')
            INTO STRICT psid;
    ELSIF (p_state = 'WY') THEN
        SELECT
            nextval('ibobadm_pds.psid_wy')
            INTO STRICT psid;
    ELSE
        SELECT
            nextval('ibobadm_pds.psid_default')
            INTO STRICT psid;
    END IF;
    RETURN psid;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_get_psp_timestamp(IN fortrigger DOUBLE PRECISION DEFAULT 1)
RETURNS TIMESTAMP
AS
$BODY$
DECLARE
    ts TIMESTAMP(6) WITHOUT TIME ZONE;
    v_offset CHARACTER VARYING(100);
    v_tz_offset CHARACTER VARYING(100);
BEGIN
    SELECT
        system_parameter_value
        INTO STRICT v_offset
        FROM psp_system_parameter
        WHERE system_parameter_cd = 'PSP_DATE_OFFSET';
    v_tz_offset := '+00.00';

    IF (forTrigger = 0) THEN
        SELECT
            system_parameter_value
            INTO STRICT v_tz_offset
            FROM psp_system_parameter
            WHERE system_parameter_cd = 'PSP_DATE_TIMEZONE_OFFSET';
    END IF;
    SELECT
        aws_oracle_ext.systimestamp() + concat_ws(' ', ((aws_oracle_ext.TO_NUMBER(v_offset) + (aws_oracle_ext.TO_NUMBER(v_tz_offset) * 3600 * 1000))::NUMERIC / 1000::NUMERIC)::TEXT, 'SECOND')::INTERVAL
        INTO STRICT ts;
    RETURN ts;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.fn_gmt_local_midnight(IN indate TIMESTAMP WITHOUT TIME ZONE)
RETURNS TIMESTAMP WITHOUT TIME ZONE
AS
$BODY$
DECLARE
    in_dst BOOLEAN;
    date_dst BOOLEAN;
BEGIN
    in_dst := aws_oracle_ext.sessiontimezone() = '-07:00';

    IF EXTRACT (YEAR FROM TO_TIMESTAMP(inDate, 'DD-MON-YY HH24:MI:SS.US')) >= 2007 THEN
        date_dst := inDate > aws_oracle_ext.NEXT_DAY(aws_oracle_ext.ADD_MONTHS(aws_oracle_ext.TRUNC(inDate, 'YY'), 2) - (1::NUMERIC || ' days')::INTERVAL, 'SUNDAY') + (7::NUMERIC || ' days')::INTERVAL AND inDate <= aws_oracle_ext.NEXT_DAY(aws_oracle_ext.ADD_MONTHS(aws_oracle_ext.TRUNC(inDate, 'YY'), 10) - (1::NUMERIC || ' days')::INTERVAL, 'SUNDAY');
    ELSE
        date_dst := inDate > aws_oracle_ext.NEXT_DAY(aws_oracle_ext.ADD_MONTHS(aws_oracle_ext.TRUNC(inDate, 'YY'), 3) - (1::NUMERIC || ' days')::INTERVAL, 'SUNDAY') AND inDate <= aws_oracle_ext.NEXT_DAY(aws_oracle_ext.ADD_MONTHS(aws_oracle_ext.TRUNC(inDate, 'YY'), 10) - (1::NUMERIC || ' days')::INTERVAL, 'SUNDAY') - (7::NUMERIC || ' days')::INTERVAL;
    END IF;
    RETURN NULL;
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the TO_TIMESTAMP_TZ function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    return sys_extract_utc(to_timestamp_tz(inDate)) + case when in_dst and not date_dst then 1/24 when not in_dst and date_dst then -1/24 else 0 end
    */
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.pk_gems_accounts_receivable$init()
RETURNS void
AS
$BODY$
BEGIN

IF aws_oracle_ext.packageinitialize(proutinename => 'ibobadm_pds.pk_gems_accounts_receivable') THEN


PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.pk_gems_accounts_receivable', pvariable => 'g_gems_upload_batch_key', pval => NULL::CHARACTER VARYING(100));

PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.pk_gems_accounts_receivable', pvariable => 'g_batch_id', pval => NULL::DOUBLE PRECISION);

PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.pk_gems_accounts_receivable', pvariable => 'g_psp_date', pval => NULL::TIMESTAMP(6) WITHOUT TIME ZONE);

PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.pk_gems_accounts_receivable', pvariable => 'g_lookback_days', pval => NULL::DOUBLE PRECISION);
ELSE
  RETURN;
END IF;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.prc_offload$init()
RETURNS void
AS
$BODY$
BEGIN
    IF aws_oracle_ext.packageinitialize(proutinename => 'ibobadm_pds.prc_offload')
    THEN
        perform aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.prc_offload', pvariable => 'v_return_cd', pval => NULL::DOUBLE PRECISION);
        perform aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.prc_offload', pvariable => 'v_error_desc', pval => NULL::CHARACTER VARYING(100));
        perform aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.prc_offload', pvariable => 'v_utc_date', pval => NULL::TIMESTAMP(6) WITHOUT TIME ZONE);
        perform aws_oracle_ext.setglobalvariable(proutinename => 'ibobadm_pds.prc_offload', pvariable => 'v_psp_date', pval => NULL::TIMESTAMP(6) WITHOUT TIME ZONE);
    ELSE
        RETURN;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE OR REPLACE FUNCTION ibobadm_pds.prc_remove_company$parentrec(IN tablename TEXT DEFAULT NULL, IN uniqueid TEXT DEFAULT NULL)
RETURNS ibobadm_pds.prc_remove_company$parentrec
AS
$BODY$
SELECT
    ROW (tablename, uniqueid)::ibobadm_pds.prc_remove_company$parentrec;
$BODY$
LANGUAGE  sql;


-- ------------ Write CREATE-FUNCTION-stage scripts -----------

CREATE OR REPLACE FUNCTION pspadm.empincomp(IN x TEXT)
RETURNS TEXT
AS
$BODY$
DECLARE
    lv_temp CHARACTER VARYING(100);
BEGIN
    SELECT
        employee_seq
        INTO STRICT lv_temp
        FROM pspadm.psp_employee
        WHERE company_fk = x;
    RETURN lv_temp;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_format_sysguid(IN psysguid BYTEA)
RETURNS TEXT
AS
$BODY$
DECLARE
    fmtGuid CHARACTER VARYING(200);
BEGIN
    SELECT
        CONCAT_WS('', aws_oracle_ext.substr(psysguid, 1, 8), '-', aws_oracle_ext.substr(psysguid, 9, 4), '-', aws_oracle_ext.substr(psysguid, 13, 4), '-', aws_oracle_ext.substr(psysguid, 17, 4), '-', aws_oracle_ext.substr(psysguid, 21))
        INTO STRICT fmtGuid;
    SELECT
        LOWER(fmtGuid)
        INTO STRICT fmtGuid;
    RETURN fmtGuid;
END;
$BODY$
LANGUAGE  plpgsql IMMUTABLE;



CREATE OR REPLACE FUNCTION pspadm.fn_get_edr_amount(IN p_date TIMESTAMP WITHOUT TIME ZONE)
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
    v_EDR_AMOUNT NUMERIC(15, 2);
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT
                          SUM(amount) INTO     v_EDR_AMOUNT
                     FROM PSP_ENTRY_DETAIL_RECORD rec0
                          INNER JOIN PSP_NACHAFILE nf ON NF.NACHAFILE_SEQ = REC0.N_A_C_H_A_FILE_FK
                          INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = NF.OFFLOAD_BATCH_FK
                    WHERE
                        ob.STATUS_CD =  'InProcess'
                        AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
                        AND OB.OFFLOAD_DATE = SYS_EXTRACT_UTC(TO_TIMESTAMP(p_date))
                        AND rec0.initiation_date = SYS_EXTRACT_UTC(TO_TIMESTAMP(p_date))
    */
    RETURN v_EDR_AMOUNT;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_get_edr_count(IN p_date TEXT)
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
        FROM pspadm.psp_entry_detail_record AS rec0
        INNER JOIN pspadm.psp_nachafile AS nf
            ON nf.nachafile_seq = rec0.n_a_c_h_a_file_fk
        INNER JOIN pspadm.psp_offload_batch AS ob
            ON ob.offload_batch_seq = nf.offload_batch_fk
        WHERE ob.status_cd = 'InProcess' AND ob.offload_group_fk = (SELECT
            offload_group_seq
            FROM pspadm.psp_offload_group
            WHERE offload_group_cd = 'STD'
            LIMIT 1) AND ob.offload_date = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"') AND rec0.initiation_date = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"');
    RETURN Nbr_EDR_RECORDS;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_get_env()
RETURNS TEXT
AS
$BODY$
/*
*****************************************************************************
   PURPOSE: Get the current environment
*****************************************************************************
*/
DECLARE
    env CHARACTER VARYING(200);
BEGIN
    SELECT
        (CASE
            WHEN UPPER(aws_oracle_ext.SYS_CONTEXT('userenv', 'service_name')) IN ('PSPPROD', 'PSPDR') THEN 'PROD'
            WHEN UPPER(aws_oracle_ext.SYS_CONTEXT('userenv', 'db_name')) IN ('PSPUWP01', 'PSPUWP02', 'PSPUEP01', 'PSPUEP02', 'PSPPP001', 'PSPSP001', 'PSPWP001', 'PSPEP001', 'PSPTS005', 'PSPTSIB5', 'PSPUE005', 'PSPUEIB5') THEN 'PROD'
            ELSE 'NONPROD'
        END)
        INTO STRICT env;
    RETURN env;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_get_last_day_of_quarter(IN p_input TIMESTAMP WITHOUT TIME ZONE)
RETURNS TIMESTAMP WITHOUT TIME ZONE
AS
$BODY$
DECLARE
    ts TIMESTAMP(6) WITHOUT TIME ZONE;
    v_tz_offset CHARACTER VARYING(100);
BEGIN
    SELECT
        system_parameter_value
        INTO STRICT v_tz_offset
        FROM pspadm.psp_system_parameter
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



CREATE OR REPLACE FUNCTION pspadm.fn_get_ledger_balance(IN p_company TEXT, IN p_ledger TEXT)
RETURNS DOUBLE PRECISION
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
            FROM pspadm.psp_ledger_balance AS lb
            WHERE lb.company_fk = p_company AND lb.ledger_account_fk = p_ledger AND DATE(lb.balance_date) = (SELECT
                DATE(MAX(lb2.balance_date))
                FROM pspadm.psp_ledger_balance AS lb2
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
        FROM pspadm.psp_financial_trans_state AS fts, pspadm.psp_financial_transaction AS ft, pspadm.psp_posting_rule AS pr, pspadm.psp_ledger_account AS la
        WHERE fts.financial_transaction_fk = ft.financial_transaction_seq AND pr.transaction_state_fk = fts.transaction_state_fk AND pr.transaction_type_fk = ft.transaction_type_fk AND la.ledger_account_cd = pr.ledger_account_fk AND fts.transaction_state_eff_date > (SELECT
            COALESCE(DATE(MAX(balance_date)) + (1::NUMERIC || ' days')::INTERVAL, (SELECT
                aws_oracle_ext.TO_DATE('01/01/1970', 'MM/DD/YYYY')))
            FROM pspadm.psp_ledger_balance) AND fts.company_fk = p_company AND pr.ledger_account_fk = p_ledger;
    RETURN (COALESCE(v_lb_balance, 0) + COALESCE(v_fts_balance, 0));
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_get_mmt_count(IN p_date TIMESTAMP WITHOUT TIME ZONE)
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



CREATE OR REPLACE FUNCTION pspadm.fn_get_psid(IN p_state TEXT)
RETURNS TEXT
AS
$BODY$
DECLARE
    psid PSPADM.PSP_COMPANY.source_company_id%TYPE;
BEGIN
    IF (p_state = 'AL') THEN
        SELECT
            nextval('pspadm.psid_al')
            INTO STRICT psid;
    ELSIF (p_state = 'AK') THEN
        SELECT
            nextval('pspadm.psid_ak')
            INTO STRICT psid;
    ELSIF (p_state = 'AZ') THEN
        SELECT
            nextval('pspadm.psid_az')
            INTO STRICT psid;
    ELSIF (p_state = 'AR') THEN
        SELECT
            nextval('pspadm.psid_ar')
            INTO STRICT psid;
    ELSIF (p_state = 'CA') THEN
        SELECT
            nextval('pspadm.psid_ca')
            INTO STRICT psid;
    ELSIF (p_state = 'CO') THEN
        SELECT
            nextval('pspadm.psid_co')
            INTO STRICT psid;
    ELSIF (p_state = 'CT') THEN
        SELECT
            nextval('pspadm.psid_ct')
            INTO STRICT psid;
    ELSIF (p_state = 'DE') THEN
        SELECT
            nextval('pspadm.psid_de')
            INTO STRICT psid;
    ELSIF (p_state = 'DC') THEN
        SELECT
            nextval('pspadm.psid_dc')
            INTO STRICT psid;
    ELSIF (p_state = 'FL') THEN
        SELECT
            nextval('pspadm.psid_fl')
            INTO STRICT psid;
    ELSIF (p_state = 'GA') THEN
        SELECT
            nextval('pspadm.psid_ga')
            INTO STRICT psid;
    ELSIF (p_state = 'HI') THEN
        SELECT
            nextval('pspadm.psid_hi')
            INTO STRICT psid;
    ELSIF (p_state = 'ID') THEN
        SELECT
            nextval('pspadm.psid_id')
            INTO STRICT psid;
    ELSIF (p_state = 'IL') THEN
        SELECT
            nextval('pspadm.psid_il')
            INTO STRICT psid;
    ELSIF (p_state = 'IN') THEN
        SELECT
            nextval('pspadm.psid_in')
            INTO STRICT psid;
    ELSIF (p_state = 'IA') THEN
        SELECT
            nextval('pspadm.psid_ia')
            INTO STRICT psid;
    ELSIF (p_state = 'KS') THEN
        SELECT
            nextval('pspadm.psid_ks')
            INTO STRICT psid;
    ELSIF (p_state = 'KY') THEN
        SELECT
            nextval('pspadm.psid_ky')
            INTO STRICT psid;
    ELSIF (p_state = 'LA') THEN
        SELECT
            nextval('pspadm.psid_la')
            INTO STRICT psid;
    ELSIF (p_state = 'ME') THEN
        SELECT
            nextval('pspadm.psid_me')
            INTO STRICT psid;
    ELSIF (p_state = 'MD') THEN
        SELECT
            nextval('pspadm.psid_md')
            INTO STRICT psid;
    ELSIF (p_state = 'MA') THEN
        SELECT
            nextval('pspadm.psid_ma')
            INTO STRICT psid;
    ELSIF (p_state = 'MI') THEN
        SELECT
            nextval('pspadm.psid_mi')
            INTO STRICT psid;
    ELSIF (p_state = 'MN') THEN
        SELECT
            nextval('pspadm.psid_mn')
            INTO STRICT psid;
    ELSIF (p_state = 'MS') THEN
        SELECT
            nextval('pspadm.psid_ms')
            INTO STRICT psid;
    ELSIF (p_state = 'MO') THEN
        SELECT
            nextval('pspadm.psid_mo')
            INTO STRICT psid;
    ELSIF (p_state = 'MT') THEN
        SELECT
            nextval('pspadm.psid_mt')
            INTO STRICT psid;
    ELSIF (p_state = 'NE') THEN
        SELECT
            nextval('pspadm.psid_ne')
            INTO STRICT psid;
    ELSIF (p_state = 'NV') THEN
        SELECT
            nextval('pspadm.psid_nv')
            INTO STRICT psid;
    ELSIF (p_state = 'NH') THEN
        SELECT
            nextval('pspadm.psid_nh')
            INTO STRICT psid;
    ELSIF (p_state = 'NJ') THEN
        SELECT
            nextval('pspadm.psid_nj')
            INTO STRICT psid;
    ELSIF (p_state = 'NM') THEN
        SELECT
            nextval('pspadm.psid_nm')
            INTO STRICT psid;
    ELSIF (p_state = 'NY') THEN
        SELECT
            nextval('pspadm.psid_ny')
            INTO STRICT psid;
    ELSIF (p_state = 'NC') THEN
        SELECT
            nextval('pspadm.psid_nc')
            INTO STRICT psid;
    ELSIF (p_state = 'ND') THEN
        SELECT
            nextval('pspadm.psid_nd')
            INTO STRICT psid;
    ELSIF (p_state = 'OH') THEN
        SELECT
            nextval('pspadm.psid_oh')
            INTO STRICT psid;
    ELSIF (p_state = 'OK') THEN
        SELECT
            nextval('pspadm.psid_ok')
            INTO STRICT psid;
    ELSIF (p_state = 'OR') THEN
        SELECT
            nextval('pspadm.psid_or')
            INTO STRICT psid;
    ELSIF (p_state = 'PA') THEN
        SELECT
            nextval('pspadm.psid_pa')
            INTO STRICT psid;
    ELSIF (p_state = 'RI') THEN
        SELECT
            nextval('pspadm.psid_ri')
            INTO STRICT psid;
    ELSIF (p_state = 'SC') THEN
        SELECT
            nextval('pspadm.psid_sc')
            INTO STRICT psid;
    ELSIF (p_state = 'SD') THEN
        SELECT
            nextval('pspadm.psid_sd')
            INTO STRICT psid;
    ELSIF (p_state = 'TN') THEN
        SELECT
            nextval('pspadm.psid_tn')
            INTO STRICT psid;
    ELSIF (p_state = 'TX') THEN
        SELECT
            nextval('pspadm.psid_tx')
            INTO STRICT psid;
    ELSIF (p_state = 'UT') THEN
        SELECT
            nextval('pspadm.psid_ut')
            INTO STRICT psid;
    ELSIF (p_state = 'VT') THEN
        SELECT
            nextval('pspadm.psid_vt')
            INTO STRICT psid;
    ELSIF (p_state = 'VA') THEN
        SELECT
            nextval('pspadm.psid_va')
            INTO STRICT psid;
    ELSIF (p_state = 'WA') THEN
        SELECT
            nextval('pspadm.psid_wa')
            INTO STRICT psid;
    ELSIF (p_state = 'WV') THEN
        SELECT
            nextval('pspadm.psid_wv')
            INTO STRICT psid;
    ELSIF (p_state = 'WI') THEN
        SELECT
            nextval('pspadm.psid_wi')
            INTO STRICT psid;
    ELSIF (p_state = 'WY') THEN
        SELECT
            nextval('pspadm.psid_wy')
            INTO STRICT psid;
    ELSE
        SELECT
            nextval('pspadm.psid_default')
            INTO STRICT psid;
    END IF;
    RETURN psid;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_get_psp_timestamp(IN fortrigger DOUBLE PRECISION DEFAULT 1)
RETURNS TIMESTAMP WITHOUT TIME ZONE
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
        FROM pspadm.psp_system_parameter
        WHERE system_parameter_cd = 'PSP_DATE_OFFSET';
    v_tz_offset := '+00.00';

    IF (fortrigger = 0) THEN
        SELECT
            system_parameter_value
            INTO STRICT v_tz_offset
            FROM pspadm.psp_system_parameter
            WHERE system_parameter_cd = 'PSP_DATE_TIMEZONE_OFFSET';
    END IF;
    SELECT
        aws_oracle_ext.systimestamp() + concat_ws(' ', ((aws_oracle_ext.TO_NUMBER(v_offset) + (aws_oracle_ext.TO_NUMBER(v_tz_offset) * 3600 * 1000))::NUMERIC / 1000::NUMERIC)::TEXT, 'SECOND')::INTERVAL
        INTO STRICT ts;
    RETURN ts;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.fn_get_week_of_year(IN p_input TIMESTAMP WITHOUT TIME ZONE)
RETURNS DOUBLE PRECISION
AS
$BODY$
DECLARE
    week DOUBLE PRECISION;
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.TRUNC(DATE,VARCHAR2) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    select ( (trunc(p_input,'day') - trunc(trunc(p_input,'yyyy'),'day')) / 7 ) +
       case when to_char(trunc(sysdate,'YYYY'),'day') = 'sun' then 1 else 0 end into week from dual
    */
    RETURN week + 1;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.get_or_pending_agency_id()
RETURNS CHARACTER VARYING
AS
$BODY$
DECLARE
    source_company_id CHARACTER VARYING(50) := NULL;
BEGIN
    SELECT
        c.source_company_id
        INTO STRICT source_company_id, mmt.money_movement_transaction_seq, mmt.agency_taxpayer_id_enc
        FROM pspadm.psp_money_movement_transaction AS mmt
        JOIN pspadm.psp_company AS c
            ON mmt.company_fk = c.company_seq
        JOIN pspadm.psp_company_service AS cs
            ON cs.company_fk = c.company_seq
        WHERE mmt.tax_payment_status IN ('ReadyToSend', 'OnHold') AND mmt.payment_template_fk IN ('OR-OTCUI-PAYMENT', 'OR-OTCWH-PAYMENT', 'OR-STTV-PAYMENT') AND cs.status_cd IN ('ActiveCurrent', 'ActiveSeasonal');
    RETURN source_company_id;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_agency_check_batch()
RETURNS TABLE (agency_check_batch_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_agency_check_batch$tmp(
    agency_check_batch_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_agency_check_batch$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_agency_id_requirement()
RETURNS TABLE (agency_id_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_agency_id_requirement$tmp(
    agency_id_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_agency_id_requirement$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_bpcompany_service_info()
RETURNS TABLE (bpcompany_service_info_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_bpcompany_service_info$tmp(
    bpcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_bpcompany_service_info$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_cdcompany_service_info()
RETURNS TABLE (cdcompany_service_info_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_cdcompany_service_info$tmp(
    cdcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_cdcompany_service_info$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_check_print_batch()
RETURNS TABLE (check_print_batch_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_check_print_batch$tmp(
    check_print_batch_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_check_print_batch$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_company_paycheck_batch()
RETURNS TABLE (company_paycheck_batch_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_company_paycheck_batch$tmp(
    company_paycheck_batch_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_company_paycheck_batch$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_company_service()
RETURNS TABLE (company_service_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_company_service$tmp(
    company_service_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_company_service$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_contact()
RETURNS TABLE (contact_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_contact$tmp(
    contact_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_contact$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_ddcompany_service_info()
RETURNS TABLE (ddcompany_service_info_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_ddcompany_service_info$tmp(
    ddcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_ddcompany_service_info$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_dep_freq_ledger_operati()
RETURNS TABLE (dep_freq_ledger_operation_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_dep_freq_ledger_operati$tmp(
    dep_freq_ledger_operation_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_dep_freq_ledger_operati$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_deposit_frequency_req()
RETURNS TABLE (deposit_frequency_req_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_deposit_frequency_req$tmp(
    deposit_frequency_req_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_deposit_frequency_req$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_edi_tax_file()
RETURNS TABLE (edi_tax_file_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_edi_tax_file$tmp(
    edi_tax_file_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_edi_tax_file$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_eftps_file()
RETURNS TABLE (eftps_file_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_eftps_file$tmp(
    eftps_file_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_eftps_file$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_employee()
RETURNS TABLE (employee_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_employee$tmp(
    employee_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_employee$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_individual()
RETURNS TABLE (individual_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_individual$tmp(
    individual_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_individual$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_ledger_operation()
RETURNS TABLE (ledger_operation_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_ledger_operation$tmp(
    ledger_operation_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_ledger_operation$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_manual_requirement()
RETURNS TABLE (manual_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_manual_requirement$tmp(
    manual_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_manual_requirement$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_payment_method_requirem()
RETURNS TABLE (payment_method_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_payment_method_requirem$tmp(
    payment_method_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_payment_method_requirem$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_payment_requirement()
RETURNS TABLE (payment_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_payment_requirement$tmp(
    payment_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_payment_requirement$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_racompany_service_info()
RETURNS TABLE (racompany_service_info_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_racompany_service_info$tmp(
    racompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_racompany_service_info$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_rate_ledger_operation()
RETURNS TABLE (rate_ledger_operation_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_rate_ledger_operation$tmp(
    rate_ledger_operation_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_rate_ledger_operation$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_state_edi_tax_file()
RETURNS TABLE (state_edi_tax_file_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_state_edi_tax_file$tmp(
    state_edi_tax_file_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_state_edi_tax_file$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_system_payment_requirem()
RETURNS TABLE (system_payment_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_system_payment_requirem$tmp(
    system_payment_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_system_payment_requirem$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_system_requirement()
RETURNS TABLE (system_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_system_requirement$tmp(
    system_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_system_requirement$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_tax_company_service_inf()
RETURNS TABLE (tax_company_service_info_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_tax_company_service_inf$tmp(
    tax_company_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_tax_company_service_inf$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_threshold_requirement()
RETURNS TABLE (threshold_requirement_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_threshold_requirement$tmp(
    threshold_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_threshold_requirement$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_tp401kcompany_service_i()
RETURNS TABLE (tp401kcompany_service_info_seq CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_tp401kcompany_service_i$tmp(
    tp401kcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM ht_psp_tp401kcompany_service_i$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.pk_gems_accounts_receivable$init()
RETURNS void
AS
$BODY$
BEGIN

IF aws_oracle_ext.packageinitialize(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE') THEN


PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_gems_upload_batch_key', pval => NULL::CHARACTER VARYING(100));

PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_batch_id', pval => NULL::DOUBLE PRECISION);

PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_psp_date', pval => NULL::TIMESTAMP(6) WITHOUT TIME ZONE);

PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', pval => NULL::DOUBLE PRECISION);
ELSE
  RETURN;
END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.prc_offload$init()
RETURNS void
AS
$BODY$
BEGIN
    IF aws_oracle_ext.packageinitialize(proutinename => 'pspadm.prc_offload')
    THEN
        perform aws_oracle_ext.setglobalvariable(proutinename => 'pspadm.prc_offload', pvariable => 'v_return_cd', pval => NULL::DOUBLE PRECISION);
        perform aws_oracle_ext.setglobalvariable(proutinename => 'pspadm.prc_offload', pvariable => 'v_error_desc', pval => NULL::CHARACTER VARYING(100));
        perform aws_oracle_ext.setglobalvariable(proutinename => 'pspadm.prc_offload', pvariable => 'v_utc_date', pval => NULL::TIMESTAMP(6) WITHOUT TIME ZONE);
        perform aws_oracle_ext.setglobalvariable(proutinename => 'pspadm.prc_offload', pvariable => 'v_psp_date', pval => NULL::TIMESTAMP(6) WITHOUT TIME ZONE);
    ELSE
        RETURN;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.prc_remove_company$parentrec(IN tablename TEXT DEFAULT NULL, IN uniqueid TEXT DEFAULT NULL)
RETURNS pspadm.prc_remove_company$parentrec
AS
$BODY$
SELECT
    ROW (tablename, uniqueid)::pspadm.prc_remove_company$parentrec;
$BODY$
LANGUAGE  sql;



CREATE OR REPLACE FUNCTION pspadm.prc_remove_company2$parentrec(IN tablename TEXT DEFAULT NULL, IN uniqueid TEXT DEFAULT NULL)
RETURNS pspadm.prc_remove_company2$parentrec
AS
$BODY$
SELECT
    ROW (tablename, uniqueid)::pspadm.prc_remove_company2$parentrec;
$BODY$
LANGUAGE  sql;



CREATE OR REPLACE FUNCTION pspadm.prc_remove_record_test$parentrec(IN tablename TEXT DEFAULT NULL, IN uniqueid TEXT DEFAULT NULL)
RETURNS pspadm.prc_remove_record_test$parentrec
AS
$BODY$
SELECT
    ROW (tablename, uniqueid)::pspadm.prc_remove_record_test$parentrec;
$BODY$
LANGUAGE  sql;



CREATE OR REPLACE FUNCTION pspadm.psp_financial_transaction(IN financial_transaction_seq TEXT DEFAULT NULL, IN version DOUBLE PRECISION DEFAULT NULL, IN creator_id TEXT DEFAULT NULL, IN created_date TIMESTAMP DEFAULT NULL, IN modifier_id TEXT DEFAULT NULL, IN modified_date TIMESTAMP DEFAULT NULL, IN realm_id DOUBLE PRECISION DEFAULT NULL, IN financial_transaction_amount DOUBLE PRECISION DEFAULT NULL, IN settlement_date TIMESTAMP DEFAULT NULL, IN settlement_type_cd TEXT DEFAULT NULL, IN credit_bank_account_type TEXT DEFAULT NULL, IN debit_bank_account_type TEXT DEFAULT NULL, IN on_hold DOUBLE PRECISION DEFAULT NULL, IN sku TEXT DEFAULT NULL, IN sku_quantity DOUBLE PRECISION DEFAULT NULL, IN billing_detail_fk TEXT DEFAULT NULL, IN credit_bank_account_fk TEXT DEFAULT NULL, IN debit_bank_account_fk TEXT DEFAULT NULL, IN company_fk TEXT DEFAULT NULL, IN payroll_run_fk TEXT DEFAULT NULL, IN paycheck_split_fk TEXT DEFAULT NULL, IN transaction_type_fk TEXT DEFAULT NULL, IN law_fk TEXT DEFAULT NULL, IN current_transaction_state_fk TEXT DEFAULT NULL, IN original_transaction_fk TEXT DEFAULT NULL, IN money_movement_transaction_fk TEXT DEFAULT NULL, IN original_settlement_date TIMESTAMP DEFAULT NULL, IN refund_type TEXT DEFAULT NULL, IN comp_adjust_submission_fk TEXT DEFAULT NULL, IN tax_penalty_interest_fk TEXT DEFAULT NULL, IN relatable_transaction_fk TEXT DEFAULT NULL, IN bill_payment_split_fk TEXT DEFAULT NULL, IN company_law_fk TEXT DEFAULT NULL, IN status TEXT DEFAULT NULL)
RETURNS pspadm.psp_financial_transaction
AS
$BODY$
SELECT
    ROW (financial_transaction_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, financial_transaction_amount, settlement_date, settlement_type_cd, credit_bank_account_type, debit_bank_account_type, on_hold, sku, sku_quantity, billing_detail_fk, credit_bank_account_fk, debit_bank_account_fk, company_fk, payroll_run_fk, paycheck_split_fk, transaction_type_fk, law_fk, current_transaction_state_fk, original_transaction_fk, money_movement_transaction_fk, original_settlement_date, refund_type, comp_adjust_submission_fk, tax_penalty_interest_fk, relatable_transaction_fk, bill_payment_split_fk, company_law_fk, status)::pspadm.psp_financial_transaction;
$BODY$
LANGUAGE  sql;



CREATE OR REPLACE FUNCTION pspadm.quest_sl_temp_explain1()
RETURNS TABLE (statement_id CHARACTER VARYING, plan_id DOUBLE PRECISION, timestamp TIMESTAMP WITHOUT TIME ZONE, remarks CHARACTER VARYING, operation CHARACTER VARYING, options CHARACTER VARYING, object_node CHARACTER VARYING, object_owner CHARACTER VARYING, object_name CHARACTER VARYING, object_alias CHARACTER VARYING, object_instance NUMERIC, object_type CHARACTER VARYING, optimizer CHARACTER VARYING, search_columns DOUBLE PRECISION, id NUMERIC, parent_id NUMERIC, depth NUMERIC, position NUMERIC, cost NUMERIC, cardinality NUMERIC, bytes NUMERIC, other_tag CHARACTER VARYING, partition_start CHARACTER VARYING, partition_stop CHARACTER VARYING, partition_id NUMERIC, other TEXT, distribution CHARACTER VARYING, cpu_cost NUMERIC, io_cost NUMERIC, temp_space NUMERIC, access_predicates CHARACTER VARYING, filter_predicates CHARACTER VARYING, projection CHARACTER VARYING, time NUMERIC, qblock_name CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE quest_sl_temp_explain1$tmp(
    statement_id CHARACTER VARYING(30),
    plan_id DOUBLE PRECISION,
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    remarks CHARACTER VARYING(4000),
    operation CHARACTER VARYING(30),
    options CHARACTER VARYING(255),
    object_node CHARACTER VARYING(128),
    object_owner CHARACTER VARYING(30),
    object_name CHARACTER VARYING(30),
    object_alias CHARACTER VARYING(65),
    object_instance NUMERIC(38,0),
    object_type CHARACTER VARYING(30),
    optimizer CHARACTER VARYING(255),
    search_columns DOUBLE PRECISION,
    id NUMERIC(38,0),
    parent_id NUMERIC(38,0),
    depth NUMERIC(38,0),
    position NUMERIC(38,0),
    cost NUMERIC(38,0),
    cardinality NUMERIC(38,0),
    bytes NUMERIC(38,0),
    other_tag CHARACTER VARYING(255),
    partition_start CHARACTER VARYING(255),
    partition_stop CHARACTER VARYING(255),
    partition_id NUMERIC(38,0),
    other TEXT,
    distribution CHARACTER VARYING(30),
    cpu_cost NUMERIC(38,0),
    io_cost NUMERIC(38,0),
    temp_space NUMERIC(38,0),
    access_predicates CHARACTER VARYING(4000),
    filter_predicates CHARACTER VARYING(4000),
    projection CHARACTER VARYING(4000),
    time NUMERIC(20,2),
    qblock_name CHARACTER VARYING(30)
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT PRESERVE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM quest_sl_temp_explain1$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.quest_sl_temp_explain2()
RETURNS TABLE (statement_id CHARACTER VARYING, plan_id DOUBLE PRECISION, timestamp TIMESTAMP WITHOUT TIME ZONE, remarks CHARACTER VARYING, operation CHARACTER VARYING, options CHARACTER VARYING, object_node CHARACTER VARYING, object_owner CHARACTER VARYING, object_name CHARACTER VARYING, object_alias CHARACTER VARYING, object_instance NUMERIC, object_type CHARACTER VARYING, optimizer CHARACTER VARYING, search_columns DOUBLE PRECISION, id NUMERIC, parent_id NUMERIC, depth NUMERIC, position NUMERIC, cost NUMERIC, cardinality NUMERIC, bytes NUMERIC, other_tag CHARACTER VARYING, partition_start CHARACTER VARYING, partition_stop CHARACTER VARYING, partition_id NUMERIC, other TEXT, other_xml TEXT, distribution CHARACTER VARYING, cpu_cost NUMERIC, io_cost NUMERIC, temp_space NUMERIC, access_predicates CHARACTER VARYING, filter_predicates CHARACTER VARYING, projection CHARACTER VARYING, time NUMERIC, qblock_name CHARACTER VARYING)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE quest_sl_temp_explain2$tmp(
    statement_id CHARACTER VARYING(30),
    plan_id DOUBLE PRECISION,
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    remarks CHARACTER VARYING(4000),
    operation CHARACTER VARYING(30),
    options CHARACTER VARYING(255),
    object_node CHARACTER VARYING(128),
    object_owner CHARACTER VARYING(30),
    object_name CHARACTER VARYING(30),
    object_alias CHARACTER VARYING(65),
    object_instance NUMERIC(38,0),
    object_type CHARACTER VARYING(30),
    optimizer CHARACTER VARYING(255),
    search_columns DOUBLE PRECISION,
    id NUMERIC(38,0),
    parent_id NUMERIC(38,0),
    depth NUMERIC(38,0),
    position NUMERIC(38,0),
    cost NUMERIC(38,0),
    cardinality NUMERIC(38,0),
    bytes NUMERIC(38,0),
    other_tag CHARACTER VARYING(255),
    partition_start CHARACTER VARYING(255),
    partition_stop CHARACTER VARYING(255),
    partition_id NUMERIC(38,0),
    other TEXT,
    other_xml TEXT,
    distribution CHARACTER VARYING(30),
    cpu_cost NUMERIC(38,0),
    io_cost NUMERIC(38,0),
    temp_space NUMERIC(38,0),
    access_predicates CHARACTER VARYING(4000),
    filter_predicates CHARACTER VARYING(4000),
    projection CHARACTER VARYING(4000),
    time NUMERIC(20,2),
    qblock_name CHARACTER VARYING(30)
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT PRESERVE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM quest_sl_temp_explain2$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.sox_verify_function(IN username TEXT, IN password TEXT, IN old_password TEXT)
RETURNS BOOLEAN
AS
$BODY$
DECLARE
    n BOOLEAN;
    m NUMERIC(38);
    differ NUMERIC(38);
    isdigit BOOLEAN;
    ischar BOOLEAN;
    ispunct BOOLEAN;
    iscap BOOLEAN;
    digitarray CHARACTER VARYING(32);
    punctarray CHARACTER VARYING(25);
    capchararray CHARACTER VARYING(26);
    lowchararray CHARACTER VARYING(26);
BEGIN
    /* digitarray:= '0123456789'; */
    <<findchar>>
    BEGIN
        capchararray := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        lowchararray := 'abcdefghijklmnopqrstuvwxyz';
        digitarray := '1234567890!"#$%&()``*+,-/:;<=>?_';
        /* Check if the password is same as the username */
        
        /*
        [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.NLS_LOWER(VARCHAR2) function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.NLS_LOWER(VARCHAR2) function. Use suitable function or create user defined function.]
        IF NLS_LOWER(password) = NLS_LOWER(username) THEN
             raise_application_error(-20001, 'Password same as or similar to user');
           END IF
        */
        /* Check for the minimum length of the password */
        IF LENGTH(password) < 8 THEN
            RAISE USING hint = -20002, message = 'Password length less than 8', detail = 'User-defined exception';
        END IF;
        /* Check if the password contains at least one low case letter, */
        /* one capital case letter and one non-alphabetic character */
        /* 1. Check for the non-alphabetic character */
        isdigit := FALSE;
        m := LENGTH(password);

        FOR i IN 1..LENGTH(digitarray) LOOP
            FOR j IN 1..m LOOP
                IF aws_oracle_ext.substr(password, j, 1) = aws_oracle_ext.substr(digitarray, i, 1) THEN
                    isdigit := TRUE;
                    EXIT findchar;
                END IF;
            END LOOP;
        END LOOP;

        IF isdigit = FALSE THEN
            RAISE USING hint = -20003, message = E'Password should contain at least one \\
        non-alphabetic character, one low case and one upper case character', detail = 'User-defined exception';
        END IF;
        /* 2. Check for the low case character */
    END;

    <<findcap>>
    BEGIN
        ischar := FALSE;

        FOR i IN 1..LENGTH(lowchararray) LOOP
            FOR j IN 1..m LOOP
                IF aws_oracle_ext.substr(password, j, 1) = aws_oracle_ext.substr(lowchararray, i, 1) THEN
                    ischar := TRUE;
                    EXIT findcap;
                END IF;
            END LOOP;
        END LOOP;

        IF ischar = FALSE THEN
            RAISE USING hint = -20003, message = E'Password should contain at least one \\
              non-alphabetic, one low case and one upper case character', detail = 'User-defined exception';
        END IF;
        /* 3. Check for the capital character */
    END;

    <<endsearch>>
    BEGIN
        iscap := FALSE;

        FOR i IN 1..LENGTH(capchararray) LOOP
            FOR j IN 1..m LOOP
                IF aws_oracle_ext.substr(password, j, 1) = aws_oracle_ext.substr(capchararray, i, 1) THEN
                    iscap := TRUE;
                    EXIT endsearch;
                END IF;
            END LOOP;
        END LOOP;

        IF iscap = FALSE THEN
            RAISE USING hint = -20003, message = E'Password should contain at least one \\
              non-alphabetic, one low case and one upper case character', detail = 'User-defined exception';
        END IF;
    END;

    IF old_password IS NOT NULL THEN
        differ := LENGTH(old_password) - LENGTH(password);

        IF ABS(differ) < 3 THEN
            IF LENGTH(password) < LENGTH(old_password) THEN
                m := LENGTH(password);
            ELSE
                m := LENGTH(old_password);
            END IF;
            differ := ABS(differ);

            FOR i IN 1..m LOOP
                IF aws_oracle_ext.substr(password, i, 1) != aws_oracle_ext.substr(old_password, i, 1) THEN
                    differ := differ + 1;
                END IF;
            END LOOP;

            IF differ < 3 THEN
                RAISE USING hint = -20004, message = E'Password should differ by at \\
         least 3 characters', detail = 'User-defined exception';
            END IF;
        END IF;
    END IF;
    /* Everything is fine; return TRUE ; */
    RETURN (TRUE);
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.squirrel_get_error_offset(IN query TEXT)
RETURNS DOUBLE PRECISION
AS
$BODY$
DECLARE
    l_theCursor NUMERIC(38) := aws_oracle_ext.dbms_sql$open_cursor();
    l_status NUMERIC(38);
BEGIN
    BEGIN
        PERFORM aws_oracle_ext.dbms_sql$parse((l_theCursor)::INTEGER, (query)::TEXT);
        EXCEPTION
            WHEN others THEN
                BEGIN
                    /*
                    [5340 - Severity CRITICAL - PostgreSQL doesn't support the SYS.DBMS_SQL.LAST_ERROR_POSITION() function. Use suitable function or create user defined function.]
                    l_status := dbms_sql.last_error_position
                    */
                END;
    END;
    l_thecursor := aws_oracle_ext.dbms_sql$close_cursor((l_theCursor)::NUMERIC);
    RETURN l_status;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.sum(IN x DOUBLE PRECISION, IN y DOUBLE PRECISION)
RETURNS DOUBLE PRECISION
AS
$BODY$
DECLARE
    c DOUBLE PRECISION;
BEGIN
    c := x + y;
    RETURN c;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.sum1(IN x DOUBLE PRECISION, IN y DOUBLE PRECISION)
RETURNS DOUBLE PRECISION
AS
$BODY$
DECLARE
    c DOUBLE PRECISION;
BEGIN
    c := x + y;
    RETURN c;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.sys_temp_fbt()
RETURNS TABLE (schema CHARACTER VARYING, object_name CHARACTER VARYING, "OBJECT#" DOUBLE PRECISION, rid CHARACTER VARYING, action CHARACTER)
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE sys_temp_fbt$tmp(
    schema CHARACTER VARYING(32),
    object_name CHARACTER VARYING(32),
    "OBJECT#" DOUBLE PRECISION,
    rid CHARACTER VARYING(8000),
    action CHARACTER(1)
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT PRESERVE ROWS;


EXCEPTION WHEN OTHERS THEN END;

RETURN QUERY SELECT * FROM sys_temp_fbt$tmp;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_agency_check_batch_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_agency_check_batch$tmp(
    agency_check_batch_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_agency_check_batch$tmp(agency_check_batch_seq)
 VALUES (new.agency_check_batch_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_agency_check_batch$tmp
 SET agency_check_batch_seq=new.agency_check_batch_seq 
WHERE agency_check_batch_seq=old.agency_check_batch_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_agency_check_batch$tmp 
WHERE agency_check_batch_seq=old.agency_check_batch_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_agency_id_requirement_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_agency_id_requirement$tmp(
    agency_id_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_agency_id_requirement$tmp(agency_id_requirement_seq)
 VALUES (new.agency_id_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_agency_id_requirement$tmp
 SET agency_id_requirement_seq=new.agency_id_requirement_seq 
WHERE agency_id_requirement_seq=old.agency_id_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_agency_id_requirement$tmp 
WHERE agency_id_requirement_seq=old.agency_id_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_bpcompany_service_info_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_bpcompany_service_info$tmp(
    bpcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_bpcompany_service_info$tmp(bpcompany_service_info_seq)
 VALUES (new.bpcompany_service_info_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_bpcompany_service_info$tmp
 SET bpcompany_service_info_seq=new.bpcompany_service_info_seq 
WHERE bpcompany_service_info_seq=old.bpcompany_service_info_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_bpcompany_service_info$tmp 
WHERE bpcompany_service_info_seq=old.bpcompany_service_info_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_cdcompany_service_info_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_cdcompany_service_info$tmp(
    cdcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_cdcompany_service_info$tmp(cdcompany_service_info_seq)
 VALUES (new.cdcompany_service_info_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_cdcompany_service_info$tmp
 SET cdcompany_service_info_seq=new.cdcompany_service_info_seq 
WHERE cdcompany_service_info_seq=old.cdcompany_service_info_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_cdcompany_service_info$tmp 
WHERE cdcompany_service_info_seq=old.cdcompany_service_info_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_check_print_batch_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_check_print_batch$tmp(
    check_print_batch_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_check_print_batch$tmp(check_print_batch_seq)
 VALUES (new.check_print_batch_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_check_print_batch$tmp
 SET check_print_batch_seq=new.check_print_batch_seq 
WHERE check_print_batch_seq=old.check_print_batch_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_check_print_batch$tmp 
WHERE check_print_batch_seq=old.check_print_batch_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_company_paycheck_batch_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_company_paycheck_batch$tmp(
    company_paycheck_batch_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_company_paycheck_batch$tmp(company_paycheck_batch_seq)
 VALUES (new.company_paycheck_batch_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_company_paycheck_batch$tmp
 SET company_paycheck_batch_seq=new.company_paycheck_batch_seq 
WHERE company_paycheck_batch_seq=old.company_paycheck_batch_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_company_paycheck_batch$tmp 
WHERE company_paycheck_batch_seq=old.company_paycheck_batch_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_company_service_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_company_service$tmp(
    company_service_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_company_service$tmp(company_service_seq)
 VALUES (new.company_service_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_company_service$tmp
 SET company_service_seq=new.company_service_seq 
WHERE company_service_seq=old.company_service_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_company_service$tmp 
WHERE company_service_seq=old.company_service_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_contact_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_contact$tmp(
    contact_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_contact$tmp(contact_seq)
 VALUES (new.contact_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_contact$tmp
 SET contact_seq=new.contact_seq 
WHERE contact_seq=old.contact_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_contact$tmp 
WHERE contact_seq=old.contact_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_ddcompany_service_info_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_ddcompany_service_info$tmp(
    ddcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_ddcompany_service_info$tmp(ddcompany_service_info_seq)
 VALUES (new.ddcompany_service_info_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_ddcompany_service_info$tmp
 SET ddcompany_service_info_seq=new.ddcompany_service_info_seq 
WHERE ddcompany_service_info_seq=old.ddcompany_service_info_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_ddcompany_service_info$tmp 
WHERE ddcompany_service_info_seq=old.ddcompany_service_info_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_dep_freq_ledger_operati_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_dep_freq_ledger_operati$tmp(
    dep_freq_ledger_operation_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_dep_freq_ledger_operati$tmp(dep_freq_ledger_operation_seq)
 VALUES (new.dep_freq_ledger_operation_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_dep_freq_ledger_operati$tmp
 SET dep_freq_ledger_operation_seq=new.dep_freq_ledger_operation_seq 
WHERE dep_freq_ledger_operation_seq=old.dep_freq_ledger_operation_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_dep_freq_ledger_operati$tmp 
WHERE dep_freq_ledger_operation_seq=old.dep_freq_ledger_operation_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_deposit_frequency_req_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_deposit_frequency_req$tmp(
    deposit_frequency_req_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_deposit_frequency_req$tmp(deposit_frequency_req_seq)
 VALUES (new.deposit_frequency_req_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_deposit_frequency_req$tmp
 SET deposit_frequency_req_seq=new.deposit_frequency_req_seq 
WHERE deposit_frequency_req_seq=old.deposit_frequency_req_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_deposit_frequency_req$tmp 
WHERE deposit_frequency_req_seq=old.deposit_frequency_req_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_edi_tax_file_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_edi_tax_file$tmp(
    edi_tax_file_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_edi_tax_file$tmp(edi_tax_file_seq)
 VALUES (new.edi_tax_file_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_edi_tax_file$tmp
 SET edi_tax_file_seq=new.edi_tax_file_seq 
WHERE edi_tax_file_seq=old.edi_tax_file_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_edi_tax_file$tmp 
WHERE edi_tax_file_seq=old.edi_tax_file_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_eftps_file_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_eftps_file$tmp(
    eftps_file_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_eftps_file$tmp(eftps_file_seq)
 VALUES (new.eftps_file_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_eftps_file$tmp
 SET eftps_file_seq=new.eftps_file_seq 
WHERE eftps_file_seq=old.eftps_file_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_eftps_file$tmp 
WHERE eftps_file_seq=old.eftps_file_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_employee_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_employee$tmp(
    employee_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_employee$tmp(employee_seq)
 VALUES (new.employee_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_employee$tmp
 SET employee_seq=new.employee_seq 
WHERE employee_seq=old.employee_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_employee$tmp 
WHERE employee_seq=old.employee_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_individual_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_individual$tmp(
    individual_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_individual$tmp(individual_seq)
 VALUES (new.individual_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_individual$tmp
 SET individual_seq=new.individual_seq 
WHERE individual_seq=old.individual_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_individual$tmp 
WHERE individual_seq=old.individual_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_ledger_operation_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_ledger_operation$tmp(
    ledger_operation_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_ledger_operation$tmp(ledger_operation_seq)
 VALUES (new.ledger_operation_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_ledger_operation$tmp
 SET ledger_operation_seq=new.ledger_operation_seq 
WHERE ledger_operation_seq=old.ledger_operation_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_ledger_operation$tmp 
WHERE ledger_operation_seq=old.ledger_operation_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_manual_requirement_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_manual_requirement$tmp(
    manual_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_manual_requirement$tmp(manual_requirement_seq)
 VALUES (new.manual_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_manual_requirement$tmp
 SET manual_requirement_seq=new.manual_requirement_seq 
WHERE manual_requirement_seq=old.manual_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_manual_requirement$tmp 
WHERE manual_requirement_seq=old.manual_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_payment_method_requirem_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_payment_method_requirem$tmp(
    payment_method_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_payment_method_requirem$tmp(payment_method_requirement_seq)
 VALUES (new.payment_method_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_payment_method_requirem$tmp
 SET payment_method_requirement_seq=new.payment_method_requirement_seq 
WHERE payment_method_requirement_seq=old.payment_method_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_payment_method_requirem$tmp 
WHERE payment_method_requirement_seq=old.payment_method_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_payment_requirement_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_payment_requirement$tmp(
    payment_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_payment_requirement$tmp(payment_requirement_seq)
 VALUES (new.payment_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_payment_requirement$tmp
 SET payment_requirement_seq=new.payment_requirement_seq 
WHERE payment_requirement_seq=old.payment_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_payment_requirement$tmp 
WHERE payment_requirement_seq=old.payment_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_racompany_service_info_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_racompany_service_info$tmp(
    racompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_racompany_service_info$tmp(racompany_service_info_seq)
 VALUES (new.racompany_service_info_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_racompany_service_info$tmp
 SET racompany_service_info_seq=new.racompany_service_info_seq 
WHERE racompany_service_info_seq=old.racompany_service_info_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_racompany_service_info$tmp 
WHERE racompany_service_info_seq=old.racompany_service_info_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_rate_ledger_operation_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_rate_ledger_operation$tmp(
    rate_ledger_operation_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_rate_ledger_operation$tmp(rate_ledger_operation_seq)
 VALUES (new.rate_ledger_operation_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_rate_ledger_operation$tmp
 SET rate_ledger_operation_seq=new.rate_ledger_operation_seq 
WHERE rate_ledger_operation_seq=old.rate_ledger_operation_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_rate_ledger_operation$tmp 
WHERE rate_ledger_operation_seq=old.rate_ledger_operation_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_state_edi_tax_file_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_state_edi_tax_file$tmp(
    state_edi_tax_file_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_state_edi_tax_file$tmp(state_edi_tax_file_seq)
 VALUES (new.state_edi_tax_file_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_state_edi_tax_file$tmp
 SET state_edi_tax_file_seq=new.state_edi_tax_file_seq 
WHERE state_edi_tax_file_seq=old.state_edi_tax_file_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_state_edi_tax_file$tmp 
WHERE state_edi_tax_file_seq=old.state_edi_tax_file_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_system_payment_requirem_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_system_payment_requirem$tmp(
    system_payment_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_system_payment_requirem$tmp(system_payment_requirement_seq)
 VALUES (new.system_payment_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_system_payment_requirem$tmp
 SET system_payment_requirement_seq=new.system_payment_requirement_seq 
WHERE system_payment_requirement_seq=old.system_payment_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_system_payment_requirem$tmp 
WHERE system_payment_requirement_seq=old.system_payment_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_system_requirement_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_system_requirement$tmp(
    system_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_system_requirement$tmp(system_requirement_seq)
 VALUES (new.system_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_system_requirement$tmp
 SET system_requirement_seq=new.system_requirement_seq 
WHERE system_requirement_seq=old.system_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_system_requirement$tmp 
WHERE system_requirement_seq=old.system_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_tax_company_service_inf_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_tax_company_service_inf$tmp(
    tax_company_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_tax_company_service_inf$tmp(tax_company_service_info_seq)
 VALUES (new.tax_company_service_info_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_tax_company_service_inf$tmp
 SET tax_company_service_info_seq=new.tax_company_service_info_seq 
WHERE tax_company_service_info_seq=old.tax_company_service_info_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_tax_company_service_inf$tmp 
WHERE tax_company_service_info_seq=old.tax_company_service_info_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_threshold_requirement_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_threshold_requirement$tmp(
    threshold_requirement_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_threshold_requirement$tmp(threshold_requirement_seq)
 VALUES (new.threshold_requirement_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_threshold_requirement$tmp
 SET threshold_requirement_seq=new.threshold_requirement_seq 
WHERE threshold_requirement_seq=old.threshold_requirement_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_threshold_requirement$tmp 
WHERE threshold_requirement_seq=old.threshold_requirement_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.ht_psp_tp401kcompany_service_i_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE ht_psp_tp401kcompany_service_i$tmp(
    tp401kcompany_service_info_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT DELETE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO ht_psp_tp401kcompany_service_i$tmp(tp401kcompany_service_info_seq)
 VALUES (new.tp401kcompany_service_info_seq) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE ht_psp_tp401kcompany_service_i$tmp
 SET tp401kcompany_service_info_seq=new.tp401kcompany_service_info_seq 
WHERE tp401kcompany_service_info_seq=old.tp401kcompany_service_info_seq RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM ht_psp_tp401kcompany_service_i$tmp 
WHERE tp401kcompany_service_info_seq=old.tp401kcompany_service_info_seq; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_address_at$psp_address()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.address_line1 <> old.address_line1) AND new.address_line1 IS NOT NULL AND old.address_line1 IS NOT NULL) OR (new.address_line1 IS NULL AND old.address_line1 IS NOT NULL) OR (new.address_line1 IS NOT NULL AND old.address_line1 IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'AddressLine1', aws_oracle_ext.TO_CHAR(old.address_line1), aws_oracle_ext.TO_CHAR(new.address_line1), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.address_line2 <> old.address_line2) AND new.address_line2 IS NOT NULL AND old.address_line2 IS NOT NULL) OR (new.address_line2 IS NULL AND old.address_line2 IS NOT NULL) OR (new.address_line2 IS NOT NULL AND old.address_line2 IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'AddressLine2', aws_oracle_ext.TO_CHAR(old.address_line2), aws_oracle_ext.TO_CHAR(new.address_line2), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.address_line3 <> old.address_line3) AND new.address_line3 IS NOT NULL AND old.address_line3 IS NOT NULL) OR (new.address_line3 IS NULL AND old.address_line3 IS NOT NULL) OR (new.address_line3 IS NOT NULL AND old.address_line3 IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'AddressLine3', aws_oracle_ext.TO_CHAR(old.address_line3), aws_oracle_ext.TO_CHAR(new.address_line3), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.city <> old.city) AND new.city IS NOT NULL AND old.city IS NOT NULL) OR (new.city IS NULL AND old.city IS NOT NULL) OR (new.city IS NOT NULL AND old.city IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'City', aws_oracle_ext.TO_CHAR(old.city), aws_oracle_ext.TO_CHAR(new.city), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.country <> old.country) AND new.country IS NOT NULL AND old.country IS NOT NULL) OR (new.country IS NULL AND old.country IS NOT NULL) OR (new.country IS NOT NULL AND old.country IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'Country', aws_oracle_ext.TO_CHAR(old.country), aws_oracle_ext.TO_CHAR(new.country), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.state <> old.state) AND new.state IS NOT NULL AND old.state IS NOT NULL) OR (new.state IS NULL AND old.state IS NOT NULL) OR (new.state IS NOT NULL AND old.state IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'State', aws_oracle_ext.TO_CHAR(old.state), aws_oracle_ext.TO_CHAR(new.state), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.zip_code <> old.zip_code) AND new.zip_code IS NOT NULL AND old.zip_code IS NOT NULL) OR (new.zip_code IS NULL AND old.zip_code IS NOT NULL) OR (new.zip_code IS NOT NULL AND old.zip_code IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'ZipCode', aws_oracle_ext.TO_CHAR(old.zip_code), aws_oracle_ext.TO_CHAR(new.zip_code), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.zip_code_extension <> old.zip_code_extension) AND new.zip_code_extension IS NOT NULL AND old.zip_code_extension IS NOT NULL) OR (new.zip_code_extension IS NULL AND old.zip_code_extension IS NOT NULL) OR (new.zip_code_extension IS NOT NULL AND old.zip_code_extension IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_seq
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company AS t1
                    JOIN pspadm.psp_employee AS ee
                        ON t1.company_seq = ee.company_fk
                    JOIN pspadm.psp_individual AS individual
                        ON ee.employee_seq = individual.individual_seq
                    WHERE (t1.mailing_address_fk = new.address_seq OR t1.legal_address_fk = new.address_seq OR individual.mailing_address_fk = new.address_seq)
                    LIMIT 1;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Address', 'ZipCodeExtension', aws_oracle_ext.TO_CHAR(old.zip_code_extension), aws_oracle_ext.TO_CHAR(new.zip_code_extension), new.address_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_bpcompany_service_info_at$psp_bpcompany_service_info()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        creator_id
        INTO STRICT v_creator_id
        FROM pspadm.psp_company_service
        WHERE company_service_seq = new.bpcompany_service_info_seq;
    SELECT
        modifier_id
        INTO STRICT v_modifier_id
        FROM pspadm.psp_company_service
        WHERE company_service_seq = new.bpcompany_service_info_seq;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.override_company_limit_amount <> old.override_company_limit_amount) AND new.override_company_limit_amount IS NOT NULL AND old.override_company_limit_amount IS NOT NULL) OR (new.override_company_limit_amount IS NULL AND old.override_company_limit_amount IS NOT NULL) OR (new.override_company_limit_amount IS NOT NULL AND old.override_company_limit_amount IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_service AS t1
                    WHERE t1.company_service_seq = new.bpcompany_service_info_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'BPCompanyServiceInfo', 'OverrideCompanyLimitAmount', aws_oracle_ext.TO_CHAR(old.override_company_limit_amount), aws_oracle_ext.TO_CHAR(new.override_company_limit_amount), new.bpcompany_service_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.override_payee_limit_amount <> old.override_payee_limit_amount) AND new.override_payee_limit_amount IS NOT NULL AND old.override_payee_limit_amount IS NOT NULL) OR (new.override_payee_limit_amount IS NULL AND old.override_payee_limit_amount IS NOT NULL) OR (new.override_payee_limit_amount IS NOT NULL AND old.override_payee_limit_amount IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_service AS t1
                    WHERE t1.company_service_seq = new.bpcompany_service_info_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'BPCompanyServiceInfo', 'OverridePayeeLimitAmount', aws_oracle_ext.TO_CHAR(old.override_payee_limit_amount), aws_oracle_ext.TO_CHAR(new.override_payee_limit_amount), new.bpcompany_service_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_cmpmtmplt_pmtmtd_at$psp_comp_pmttemplate_pmtmethod()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.agent_enabled <> old.agent_enabled) AND new.agent_enabled IS NOT NULL AND old.agent_enabled IS NOT NULL) OR (new.agent_enabled IS NULL AND old.agent_enabled IS NOT NULL) OR (new.agent_enabled IS NOT NULL AND old.agent_enabled IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    JOIN pspadm.psp_companyagency_pmttemplate AS capt
                        ON t1.company_agency_seq = capt.company_agency_fk
                    WHERE capt.companyagency_pmttemplate_seq = new.company_agency_pmt_template_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyPaymentTemplatePaymentMethod', 'AgentEnabled', aws_oracle_ext.TO_CHAR(old.agent_enabled), aws_oracle_ext.TO_CHAR(new.agent_enabled), new.comp_pmttemplate_pmtmethod_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.enabled <> old.enabled) AND new.enabled IS NOT NULL AND old.enabled IS NOT NULL) OR (new.enabled IS NULL AND old.enabled IS NOT NULL) OR (new.enabled IS NOT NULL AND old.enabled IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    JOIN pspadm.psp_companyagency_pmttemplate AS capt
                        ON t1.company_agency_seq = capt.company_agency_fk
                    WHERE capt.companyagency_pmttemplate_seq = new.company_agency_pmt_template_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyPaymentTemplatePaymentMethod', 'Enabled', aws_oracle_ext.TO_CHAR(old.enabled), aws_oracle_ext.TO_CHAR(new.enabled), new.comp_pmttemplate_pmtmethod_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_cmpnyagency_pmttplt_at$psp_companyagency_pmttemplate()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.agency_taxpayer_id_enc <> old.agency_taxpayer_id_enc) AND new.agency_taxpayer_id_enc IS NOT NULL AND old.agency_taxpayer_id_enc IS NOT NULL) OR (new.agency_taxpayer_id_enc IS NULL AND old.agency_taxpayer_id_enc IS NOT NULL) OR (new.agency_taxpayer_id_enc IS NOT NULL AND old.agency_taxpayer_id_enc IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    WHERE t1.company_agency_seq = new.company_agency_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyAgencyPaymentTemplate', 'AgencyTaxpayerIdEnc', aws_oracle_ext.TO_CHAR(old.agency_taxpayer_id_enc), aws_oracle_ext.TO_CHAR(new.agency_taxpayer_id_enc), new.companyagency_pmttemplate_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_agency_at$psp_company_agency()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.er_fica_deferral_enabled <> old.er_fica_deferral_enabled) AND new.er_fica_deferral_enabled IS NOT NULL AND old.er_fica_deferral_enabled IS NOT NULL) OR (new.er_fica_deferral_enabled IS NULL AND old.er_fica_deferral_enabled IS NOT NULL) OR (new.er_fica_deferral_enabled IS NOT NULL AND old.er_fica_deferral_enabled IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyAgency', 'ErFicaDeferralEnabled', aws_oracle_ext.TO_CHAR(old.er_fica_deferral_enabled), aws_oracle_ext.TO_CHAR(new.er_fica_deferral_enabled), new.company_agency_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_at$psp_company()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.funding_model_fk <> old.funding_model_fk) AND new.funding_model_fk IS NOT NULL AND old.funding_model_fk IS NOT NULL) OR (new.funding_model_fk IS NULL AND old.funding_model_fk IS NOT NULL) OR (new.funding_model_fk IS NOT NULL AND old.funding_model_fk IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_seq
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Company', 'FundingModel', aws_oracle_ext.TO_CHAR(old.funding_model_fk), aws_oracle_ext.TO_CHAR(new.funding_model_fk), new.company_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.notification_email <> old.notification_email) AND new.notification_email IS NOT NULL AND old.notification_email IS NOT NULL) OR (new.notification_email IS NULL AND old.notification_email IS NOT NULL) OR (new.notification_email IS NOT NULL AND old.notification_email IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_seq
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Company', 'NotificationEmail', aws_oracle_ext.TO_CHAR(old.notification_email), aws_oracle_ext.TO_CHAR(new.notification_email), new.company_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.fed_tax_id_enc <> old.fed_tax_id_enc) AND new.fed_tax_id_enc IS NOT NULL AND old.fed_tax_id_enc IS NOT NULL) OR (new.fed_tax_id_enc IS NULL AND old.fed_tax_id_enc IS NOT NULL) OR (new.fed_tax_id_enc IS NOT NULL AND old.fed_tax_id_enc IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_seq
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Company', 'FedTaxIdEnc', aws_oracle_ext.TO_CHAR(old.fed_tax_id_enc), aws_oracle_ext.TO_CHAR(new.fed_tax_id_enc), new.company_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.legal_name <> old.legal_name) AND new.legal_name IS NOT NULL AND old.legal_name IS NOT NULL) OR (new.legal_name IS NULL AND old.legal_name IS NOT NULL) OR (new.legal_name IS NOT NULL AND old.legal_name IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_seq
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Company', 'LegalName', aws_oracle_ext.TO_CHAR(old.legal_name), aws_oracle_ext.TO_CHAR(new.legal_name), new.company_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_bank_account_at$psp_company_bank_account()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.status_cd <> old.status_cd) AND new.status_cd IS NOT NULL AND old.status_cd IS NOT NULL) OR (new.status_cd IS NULL AND old.status_cd IS NOT NULL) OR (new.status_cd IS NOT NULL AND old.status_cd IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyBankAccount', 'StatusCd', aws_oracle_ext.TO_CHAR(old.status_cd), aws_oracle_ext.TO_CHAR(new.status_cd), new.company_bank_account_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.status_effective_date <> old.status_effective_date) AND new.status_effective_date IS NOT NULL AND old.status_effective_date IS NOT NULL) OR (new.status_effective_date IS NULL AND old.status_effective_date IS NOT NULL) OR (new.status_effective_date IS NOT NULL AND old.status_effective_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyBankAccount', 'StatusEffectiveDate', aws_oracle_ext.TO_CHAR(old.status_effective_date), aws_oracle_ext.TO_CHAR(new.status_effective_date), new.company_bank_account_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.source_bank_account_name <> old.source_bank_account_name) AND new.source_bank_account_name IS NOT NULL AND old.source_bank_account_name IS NOT NULL) OR (new.source_bank_account_name IS NULL AND old.source_bank_account_name IS NOT NULL) OR (new.source_bank_account_name IS NOT NULL AND old.source_bank_account_name IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyBankAccount', 'SourceBankAccountName', aws_oracle_ext.TO_CHAR(old.source_bank_account_name), aws_oracle_ext.TO_CHAR(new.source_bank_account_name), new.company_bank_account_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_law_at$psp_company_law()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.exemption_status <> old.exemption_status) AND new.exemption_status IS NOT NULL AND old.exemption_status IS NOT NULL) OR (new.exemption_status IS NULL AND old.exemption_status IS NOT NULL) OR (new.exemption_status IS NOT NULL AND old.exemption_status IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    WHERE t1.company_agency_seq = new.company_agency_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyLaw', 'ExemptionStatus', aws_oracle_ext.TO_CHAR(old.exemption_status), aws_oracle_ext.TO_CHAR(new.exemption_status), new.company_law_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.filing_status <> old.filing_status) AND new.filing_status IS NOT NULL AND old.filing_status IS NOT NULL) OR (new.filing_status IS NULL AND old.filing_status IS NOT NULL) OR (new.filing_status IS NOT NULL AND old.filing_status IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    WHERE t1.company_agency_seq = new.company_agency_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyLaw', 'FilingStatus', aws_oracle_ext.TO_CHAR(old.filing_status), aws_oracle_ext.TO_CHAR(new.filing_status), new.company_law_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.reimbursable_status <> old.reimbursable_status) AND new.reimbursable_status IS NOT NULL AND old.reimbursable_status IS NOT NULL) OR (new.reimbursable_status IS NULL AND old.reimbursable_status IS NOT NULL) OR (new.reimbursable_status IS NOT NULL AND old.reimbursable_status IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    WHERE t1.company_agency_seq = new.company_agency_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyLaw', 'ReimbursableStatus', aws_oracle_ext.TO_CHAR(old.reimbursable_status), aws_oracle_ext.TO_CHAR(new.reimbursable_status), new.company_law_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_offer_at$psp_company_offer()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.offer_fk <> old.offer_fk) AND new.offer_fk IS NOT NULL AND old.offer_fk IS NOT NULL) OR (new.offer_fk IS NULL AND old.offer_fk IS NOT NULL) OR (new.offer_fk IS NOT NULL AND old.offer_fk IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyOffer', 'Offer', aws_oracle_ext.TO_CHAR(old.offer_fk), aws_oracle_ext.TO_CHAR(new.offer_fk), new.company_offer_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_payroll_item_at$psp_company_payroll_item()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.payroll_item_fk <> old.payroll_item_fk) AND new.payroll_item_fk IS NOT NULL AND old.payroll_item_fk IS NOT NULL) OR (new.payroll_item_fk IS NULL AND old.payroll_item_fk IS NOT NULL) OR (new.payroll_item_fk IS NOT NULL AND old.payroll_item_fk IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyPayrollItem', 'PayrollItem', aws_oracle_ext.TO_CHAR(old.payroll_item_fk), aws_oracle_ext.TO_CHAR(new.payroll_item_fk), new.company_payroll_item_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.source_description <> old.source_description) AND new.source_description IS NOT NULL AND old.source_description IS NOT NULL) OR (new.source_description IS NULL AND old.source_description IS NOT NULL) OR (new.source_description IS NOT NULL AND old.source_description IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyPayrollItem', 'SourceDescription', aws_oracle_ext.TO_CHAR(old.source_description), aws_oracle_ext.TO_CHAR(new.source_description), new.company_payroll_item_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.source_payroll_item_id <> old.source_payroll_item_id) AND new.source_payroll_item_id IS NOT NULL AND old.source_payroll_item_id IS NOT NULL) OR (new.source_payroll_item_id IS NULL AND old.source_payroll_item_id IS NOT NULL) OR (new.source_payroll_item_id IS NOT NULL AND old.source_payroll_item_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyPayrollItem', 'SourcePayrollItemId', aws_oracle_ext.TO_CHAR(old.source_payroll_item_id), aws_oracle_ext.TO_CHAR(new.source_payroll_item_id), new.company_payroll_item_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_company_service_at$psp_company_service()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.status_cd <> old.status_cd) AND new.status_cd IS NOT NULL AND old.status_cd IS NOT NULL) OR (new.status_cd IS NULL AND old.status_cd IS NOT NULL) OR (new.status_cd IS NOT NULL AND old.status_cd IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyService', 'StatusCd', aws_oracle_ext.TO_CHAR(old.status_cd), aws_oracle_ext.TO_CHAR(new.status_cd), new.company_service_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.status_effective_date <> old.status_effective_date) AND new.status_effective_date IS NOT NULL AND old.status_effective_date IS NOT NULL) OR (new.status_effective_date IS NULL AND old.status_effective_date IS NOT NULL) OR (new.status_effective_date IS NOT NULL AND old.status_effective_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'CompanyService', 'StatusEffectiveDate', aws_oracle_ext.TO_CHAR(old.status_effective_date), aws_oracle_ext.TO_CHAR(new.status_effective_date), new.company_service_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_contact_at$psp_contact()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        creator_id
        INTO STRICT v_creator_id
        FROM pspadm.psp_individual
        WHERE individual_seq = new.contact_seq;
    SELECT
        modifier_id
        INTO STRICT v_modifier_id
        FROM pspadm.psp_individual
        WHERE individual_seq = new.contact_seq;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.auth_signer_yn_ind <> old.auth_signer_yn_ind) AND new.auth_signer_yn_ind IS NOT NULL AND old.auth_signer_yn_ind IS NOT NULL) OR (new.auth_signer_yn_ind IS NULL AND old.auth_signer_yn_ind IS NOT NULL) OR (new.auth_signer_yn_ind IS NOT NULL AND old.auth_signer_yn_ind IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Contact', 'AuthSignerYnInd', aws_oracle_ext.TO_CHAR(old.auth_signer_yn_ind), aws_oracle_ext.TO_CHAR(new.auth_signer_yn_ind), new.contact_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.contact_role_cd <> old.contact_role_cd) AND new.contact_role_cd IS NOT NULL AND old.contact_role_cd IS NOT NULL) OR (new.contact_role_cd IS NULL AND old.contact_role_cd IS NOT NULL) OR (new.contact_role_cd IS NOT NULL AND old.contact_role_cd IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Contact', 'ContactRoleCd', aws_oracle_ext.TO_CHAR(old.contact_role_cd), aws_oracle_ext.TO_CHAR(new.contact_role_cd), new.contact_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.fax <> old.fax) AND new.fax IS NOT NULL AND old.fax IS NOT NULL) OR (new.fax IS NULL AND old.fax IS NOT NULL) OR (new.fax IS NOT NULL AND old.fax IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Contact', 'Fax', aws_oracle_ext.TO_CHAR(old.fax), aws_oracle_ext.TO_CHAR(new.fax), new.contact_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.job_title <> old.job_title) AND new.job_title IS NOT NULL AND old.job_title IS NOT NULL) OR (new.job_title IS NULL AND old.job_title IS NOT NULL) OR (new.job_title IS NOT NULL AND old.job_title IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Contact', 'JobTitle', aws_oracle_ext.TO_CHAR(old.job_title), aws_oracle_ext.TO_CHAR(new.job_title), new.contact_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.source_contact_id <> old.source_contact_id) AND new.source_contact_id IS NOT NULL AND old.source_contact_id IS NOT NULL) OR (new.source_contact_id IS NULL AND old.source_contact_id IS NOT NULL) OR (new.source_contact_id IS NOT NULL AND old.source_contact_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Contact', 'SourceContactId', aws_oracle_ext.TO_CHAR(old.source_contact_id), aws_oracle_ext.TO_CHAR(new.source_contact_id), new.contact_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.title <> old.title) AND new.title IS NOT NULL AND old.title IS NOT NULL) OR (new.title IS NULL AND old.title IS NOT NULL) OR (new.title IS NOT NULL AND old.title IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Contact', 'Title', aws_oracle_ext.TO_CHAR(old.title), aws_oracle_ext.TO_CHAR(new.title), new.contact_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_ddcompany_service_info_at$psp_ddcompany_service_info()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        creator_id
        INTO STRICT v_creator_id
        FROM pspadm.psp_company_service
        WHERE company_service_seq = new.ddcompany_service_info_seq;
    SELECT
        modifier_id
        INTO STRICT v_modifier_id
        FROM pspadm.psp_company_service
        WHERE company_service_seq = new.ddcompany_service_info_seq;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.override_company_limit_amount <> old.override_company_limit_amount) AND new.override_company_limit_amount IS NOT NULL AND old.override_company_limit_amount IS NOT NULL) OR (new.override_company_limit_amount IS NULL AND old.override_company_limit_amount IS NOT NULL) OR (new.override_company_limit_amount IS NOT NULL AND old.override_company_limit_amount IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_service AS t1
                    WHERE t1.company_service_seq = new.ddcompany_service_info_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'DDCompanyServiceInfo', 'OverrideCompanyLimitAmount', aws_oracle_ext.TO_CHAR(old.override_company_limit_amount), aws_oracle_ext.TO_CHAR(new.override_company_limit_amount), new.ddcompany_service_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.override_employee_limit_amount <> old.override_employee_limit_amount) AND new.override_employee_limit_amount IS NOT NULL AND old.override_employee_limit_amount IS NOT NULL) OR (new.override_employee_limit_amount IS NULL AND old.override_employee_limit_amount IS NOT NULL) OR (new.override_employee_limit_amount IS NOT NULL AND old.override_employee_limit_amount IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_service AS t1
                    WHERE t1.company_service_seq = new.ddcompany_service_info_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'DDCompanyServiceInfo', 'OverrideEmployeeLimitAmount', aws_oracle_ext.TO_CHAR(old.override_employee_limit_amount), aws_oracle_ext.TO_CHAR(new.override_employee_limit_amount), new.ddcompany_service_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_employee_at$psp_employee()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        creator_id
        INTO STRICT v_creator_id
        FROM pspadm.psp_individual
        WHERE individual_seq = new.employee_seq;
    SELECT
        modifier_id
        INTO STRICT v_modifier_id
        FROM pspadm.psp_individual
        WHERE individual_seq = new.employee_seq;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.birth_date_enc <> old.birth_date_enc) AND new.birth_date_enc IS NOT NULL AND old.birth_date_enc IS NOT NULL) OR (new.birth_date_enc IS NULL AND old.birth_date_enc IS NOT NULL) OR (new.birth_date_enc IS NOT NULL AND old.birth_date_enc IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'BirthDateEnc', aws_oracle_ext.TO_CHAR(old.birth_date_enc), aws_oracle_ext.TO_CHAR(new.birth_date_enc), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.fed_allowances <> old.fed_allowances) AND new.fed_allowances IS NOT NULL AND old.fed_allowances IS NOT NULL) OR (new.fed_allowances IS NULL AND old.fed_allowances IS NOT NULL) OR (new.fed_allowances IS NOT NULL AND old.fed_allowances IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'FedAllowances', aws_oracle_ext.TO_CHAR(old.fed_allowances), aws_oracle_ext.TO_CHAR(new.fed_allowances), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.fed_filing_status <> old.fed_filing_status) AND new.fed_filing_status IS NOT NULL AND old.fed_filing_status IS NOT NULL) OR (new.fed_filing_status IS NULL AND old.fed_filing_status IS NOT NULL) OR (new.fed_filing_status IS NOT NULL AND old.fed_filing_status IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'FedFilingStatus', aws_oracle_ext.TO_CHAR(old.fed_filing_status), aws_oracle_ext.TO_CHAR(new.fed_filing_status), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.hire_date <> old.hire_date) AND new.hire_date IS NOT NULL AND old.hire_date IS NOT NULL) OR (new.hire_date IS NULL AND old.hire_date IS NOT NULL) OR (new.hire_date IS NOT NULL AND old.hire_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'HireDate', aws_oracle_ext.TO_CHAR(old.hire_date), aws_oracle_ext.TO_CHAR(new.hire_date), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.re_hire_date <> old.re_hire_date) AND new.re_hire_date IS NOT NULL AND old.re_hire_date IS NOT NULL) OR (new.re_hire_date IS NULL AND old.re_hire_date IS NOT NULL) OR (new.re_hire_date IS NOT NULL AND old.re_hire_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'ReHireDate', aws_oracle_ext.TO_CHAR(old.re_hire_date), aws_oracle_ext.TO_CHAR(new.re_hire_date), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.termination_date <> old.termination_date) AND new.termination_date IS NOT NULL AND old.termination_date IS NOT NULL) OR (new.termination_date IS NULL AND old.termination_date IS NOT NULL) OR (new.termination_date IS NOT NULL AND old.termination_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'TerminationDate', aws_oracle_ext.TO_CHAR(old.termination_date), aws_oracle_ext.TO_CHAR(new.termination_date), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.work_state <> old.work_state) AND new.work_state IS NOT NULL AND old.work_state IS NOT NULL) OR (new.work_state IS NULL AND old.work_state IS NOT NULL) OR (new.work_state IS NOT NULL AND old.work_state IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'WorkState', aws_oracle_ext.TO_CHAR(old.work_state), aws_oracle_ext.TO_CHAR(new.work_state), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.live_state <> old.live_state) AND new.live_state IS NOT NULL AND old.live_state IS NOT NULL) OR (new.live_state IS NULL AND old.live_state IS NOT NULL) OR (new.live_state IS NOT NULL AND old.live_state IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'LiveState', aws_oracle_ext.TO_CHAR(old.live_state), aws_oracle_ext.TO_CHAR(new.live_state), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.source_employee_id <> old.source_employee_id) AND new.source_employee_id IS NOT NULL AND old.source_employee_id IS NOT NULL) OR (new.source_employee_id IS NULL AND old.source_employee_id IS NOT NULL) OR (new.source_employee_id IS NOT NULL AND old.source_employee_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'SourceEmployeeId', aws_oracle_ext.TO_CHAR(old.source_employee_id), aws_oracle_ext.TO_CHAR(new.source_employee_id), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.status_cd <> old.status_cd) AND new.status_cd IS NOT NULL AND old.status_cd IS NOT NULL) OR (new.status_cd IS NULL AND old.status_cd IS NOT NULL) OR (new.status_cd IS NOT NULL AND old.status_cd IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'StatusCd', aws_oracle_ext.TO_CHAR(old.status_cd), aws_oracle_ext.TO_CHAR(new.status_cd), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tax_id_enc <> old.tax_id_enc) AND new.tax_id_enc IS NOT NULL AND old.tax_id_enc IS NOT NULL) OR (new.tax_id_enc IS NULL AND old.tax_id_enc IS NOT NULL) OR (new.tax_id_enc IS NOT NULL AND old.tax_id_enc IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'TaxIdEnc', aws_oracle_ext.TO_CHAR(old.tax_id_enc), aws_oracle_ext.TO_CHAR(new.tax_id_enc), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tp_401k_info_is_hce <> old.tp_401k_info_is_hce) AND new.tp_401k_info_is_hce IS NOT NULL AND old.tp_401k_info_is_hce IS NOT NULL) OR (new.tp_401k_info_is_hce IS NULL AND old.tp_401k_info_is_hce IS NOT NULL) OR (new.tp_401k_info_is_hce IS NOT NULL AND old.tp_401k_info_is_hce IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'ThirdParty401kInfo.IsHighlyCompensated', aws_oracle_ext.TO_CHAR(old.tp_401k_info_is_hce), aws_oracle_ext.TO_CHAR(new.tp_401k_info_is_hce), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tp_401k_info_owner_percent <> old.tp_401k_info_owner_percent) AND new.tp_401k_info_owner_percent IS NOT NULL AND old.tp_401k_info_owner_percent IS NOT NULL) OR (new.tp_401k_info_owner_percent IS NULL AND old.tp_401k_info_owner_percent IS NOT NULL) OR (new.tp_401k_info_owner_percent IS NOT NULL AND old.tp_401k_info_owner_percent IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'ThirdParty401kInfo.OwnershipPercentage', aws_oracle_ext.TO_CHAR(old.tp_401k_info_owner_percent), aws_oracle_ext.TO_CHAR(new.tp_401k_info_owner_percent), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tp_401k_info_is_family_member <> old.tp_401k_info_is_family_member) AND new.tp_401k_info_is_family_member IS NOT NULL AND old.tp_401k_info_is_family_member IS NOT NULL) OR (new.tp_401k_info_is_family_member IS NULL AND old.tp_401k_info_is_family_member IS NOT NULL) OR (new.tp_401k_info_is_family_member IS NOT NULL AND old.tp_401k_info_is_family_member IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'ThirdParty401kInfo.IsFamilyMember', aws_oracle_ext.TO_CHAR(old.tp_401k_info_is_family_member), aws_oracle_ext.TO_CHAR(new.tp_401k_info_is_family_member), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tp_401k_info_last_upload_date <> old.tp_401k_info_last_upload_date) AND new.tp_401k_info_last_upload_date IS NOT NULL AND old.tp_401k_info_last_upload_date IS NOT NULL) OR (new.tp_401k_info_last_upload_date IS NULL AND old.tp_401k_info_last_upload_date IS NOT NULL) OR (new.tp_401k_info_last_upload_date IS NOT NULL AND old.tp_401k_info_last_upload_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Employee', 'ThirdParty401kInfo.Last401kUploadDate', aws_oracle_ext.TO_CHAR(old.tp_401k_info_last_upload_date), aws_oracle_ext.TO_CHAR(new.tp_401k_info_last_upload_date), new.employee_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_entity_change_at$psp_entity_change()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.agent_id <> old.agent_id) AND new.agent_id IS NOT NULL AND old.agent_id IS NOT NULL) OR (new.agent_id IS NULL AND old.agent_id IS NOT NULL) OR (new.agent_id IS NOT NULL AND old.agent_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'EntityChange', 'AgentId', aws_oracle_ext.TO_CHAR(old.agent_id), aws_oracle_ext.TO_CHAR(new.agent_id), new.entity_change_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.effective_date <> old.effective_date) AND new.effective_date IS NOT NULL AND old.effective_date IS NOT NULL) OR (new.effective_date IS NULL AND old.effective_date IS NOT NULL) OR (new.effective_date IS NOT NULL AND old.effective_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'EntityChange', 'EffectiveDate', aws_oracle_ext.TO_CHAR(old.effective_date), aws_oracle_ext.TO_CHAR(new.effective_date), new.entity_change_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.new_ein_enc <> old.new_ein_enc) AND new.new_ein_enc IS NOT NULL AND old.new_ein_enc IS NOT NULL) OR (new.new_ein_enc IS NULL AND old.new_ein_enc IS NOT NULL) OR (new.new_ein_enc IS NOT NULL AND old.new_ein_enc IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'EntityChange', 'NewEinEnc', aws_oracle_ext.TO_CHAR(old.new_ein_enc), aws_oracle_ext.TO_CHAR(new.new_ein_enc), new.entity_change_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.old_ein_enc <> old.old_ein_enc) AND new.old_ein_enc IS NOT NULL AND old.old_ein_enc IS NOT NULL) OR (new.old_ein_enc IS NULL AND old.old_ein_enc IS NOT NULL) OR (new.old_ein_enc IS NOT NULL AND old.old_ein_enc IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'EntityChange', 'OldEinEnc', aws_oracle_ext.TO_CHAR(old.old_ein_enc), aws_oracle_ext.TO_CHAR(new.old_ein_enc), new.entity_change_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_individual_at$psp_individual()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.first_name <> old.first_name) AND new.first_name IS NOT NULL AND old.first_name IS NOT NULL) OR (new.first_name IS NULL AND old.first_name IS NOT NULL) OR (new.first_name IS NOT NULL AND old.first_name IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_employee AS t1
                    WHERE t1.employee_seq = new.individual_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Individual', 'FirstName', aws_oracle_ext.TO_CHAR(old.first_name), aws_oracle_ext.TO_CHAR(new.first_name), new.individual_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.middle_name <> old.middle_name) AND new.middle_name IS NOT NULL AND old.middle_name IS NOT NULL) OR (new.middle_name IS NULL AND old.middle_name IS NOT NULL) OR (new.middle_name IS NOT NULL AND old.middle_name IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_employee AS t1
                    WHERE t1.employee_seq = new.individual_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Individual', 'MiddleName', aws_oracle_ext.TO_CHAR(old.middle_name), aws_oracle_ext.TO_CHAR(new.middle_name), new.individual_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.last_name <> old.last_name) AND new.last_name IS NOT NULL AND old.last_name IS NOT NULL) OR (new.last_name IS NULL AND old.last_name IS NOT NULL) OR (new.last_name IS NOT NULL AND old.last_name IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_employee AS t1
                    WHERE t1.employee_seq = new.individual_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Individual', 'LastName', aws_oracle_ext.TO_CHAR(old.last_name), aws_oracle_ext.TO_CHAR(new.last_name), new.individual_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.phone <> old.phone) AND new.phone IS NOT NULL AND old.phone IS NOT NULL) OR (new.phone IS NULL AND old.phone IS NOT NULL) OR (new.phone IS NOT NULL AND old.phone IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_employee AS t1
                    WHERE t1.employee_seq = new.individual_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Individual', 'Phone', aws_oracle_ext.TO_CHAR(old.phone), aws_oracle_ext.TO_CHAR(new.phone), new.individual_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.email <> old.email) AND new.email IS NOT NULL AND old.email IS NOT NULL) OR (new.email IS NULL AND old.email IS NOT NULL) OR (new.email IS NOT NULL AND old.email IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_employee AS t1
                    WHERE t1.employee_seq = new.individual_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'Individual', 'Email', aws_oracle_ext.TO_CHAR(old.email), aws_oracle_ext.TO_CHAR(new.email), new.individual_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_money_mvmt_trans_at$psp_money_movement_transaction()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.agency_taxpayer_id_enc <> old.agency_taxpayer_id_enc) AND new.agency_taxpayer_id_enc IS NOT NULL AND old.agency_taxpayer_id_enc IS NOT NULL) OR (new.agency_taxpayer_id_enc IS NULL AND old.agency_taxpayer_id_enc IS NOT NULL) OR (new.agency_taxpayer_id_enc IS NOT NULL AND old.agency_taxpayer_id_enc IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'MoneyMovementTransaction', 'AgencyTaxpayerIdEnc', aws_oracle_ext.TO_CHAR(old.agency_taxpayer_id_enc), aws_oracle_ext.TO_CHAR(new.agency_taxpayer_id_enc), new.money_movement_transaction_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.initiation_date <> old.initiation_date) AND new.initiation_date IS NOT NULL AND old.initiation_date IS NOT NULL) OR (new.initiation_date IS NULL AND old.initiation_date IS NOT NULL) OR (new.initiation_date IS NOT NULL AND old.initiation_date IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'MoneyMovementTransaction', 'InitiationDate', aws_oracle_ext.TO_CHAR(old.initiation_date), aws_oracle_ext.TO_CHAR(new.initiation_date), new.money_movement_transaction_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.manual_payment_status <> old.manual_payment_status) AND new.manual_payment_status IS NOT NULL AND old.manual_payment_status IS NOT NULL) OR (new.manual_payment_status IS NULL AND old.manual_payment_status IS NOT NULL) OR (new.manual_payment_status IS NOT NULL AND old.manual_payment_status IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'MoneyMovementTransaction', 'ManualPaymentStatus', aws_oracle_ext.TO_CHAR(old.manual_payment_status), aws_oracle_ext.TO_CHAR(new.manual_payment_status), new.money_movement_transaction_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.money_movement_payment_method <> old.money_movement_payment_method) AND new.money_movement_payment_method IS NOT NULL AND old.money_movement_payment_method IS NOT NULL) OR (new.money_movement_payment_method IS NULL AND old.money_movement_payment_method IS NOT NULL) OR (new.money_movement_payment_method IS NOT NULL AND old.money_movement_payment_method IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'MoneyMovementTransaction', 'MoneyMovementPaymentMethod', aws_oracle_ext.TO_CHAR(old.money_movement_payment_method), aws_oracle_ext.TO_CHAR(new.money_movement_payment_method), new.money_movement_transaction_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tax_payment_status <> old.tax_payment_status) AND new.tax_payment_status IS NOT NULL AND old.tax_payment_status IS NOT NULL) OR (new.tax_payment_status IS NULL AND old.tax_payment_status IS NOT NULL) OR (new.tax_payment_status IS NOT NULL AND old.tax_payment_status IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'MoneyMovementTransaction', 'TaxPaymentStatus', aws_oracle_ext.TO_CHAR(old.tax_payment_status), aws_oracle_ext.TO_CHAR(new.tax_payment_status), new.money_movement_transaction_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.reference_number <> old.reference_number) AND new.reference_number IS NOT NULL AND old.reference_number IS NOT NULL) OR (new.reference_number IS NULL AND old.reference_number IS NOT NULL) OR (new.reference_number IS NOT NULL AND old.reference_number IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'MoneyMovementTransaction', 'ReferenceNumber', aws_oracle_ext.TO_CHAR(old.reference_number), aws_oracle_ext.TO_CHAR(new.reference_number), new.money_movement_transaction_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_quickbooks_info_at$psp_quickbooks_info()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.application_id <> old.application_id) AND new.application_id IS NOT NULL AND old.application_id IS NOT NULL) OR (new.application_id IS NULL AND old.application_id IS NOT NULL) OR (new.application_id IS NOT NULL AND old.application_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'ApplicationId', aws_oracle_ext.TO_CHAR(old.application_id), aws_oracle_ext.TO_CHAR(new.application_id), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.application_version <> old.application_version) AND new.application_version IS NOT NULL AND old.application_version IS NOT NULL) OR (new.application_version IS NULL AND old.application_version IS NOT NULL) OR (new.application_version IS NOT NULL AND old.application_version IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'ApplicationVersion', aws_oracle_ext.TO_CHAR(old.application_version), aws_oracle_ext.TO_CHAR(new.application_version), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.a_s400_payroll_count <> old.a_s400_payroll_count) AND new.a_s400_payroll_count IS NOT NULL AND old.a_s400_payroll_count IS NOT NULL) OR (new.a_s400_payroll_count IS NULL AND old.a_s400_payroll_count IS NOT NULL) OR (new.a_s400_payroll_count IS NOT NULL AND old.a_s400_payroll_count IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'AS400PayrollCount', aws_oracle_ext.TO_CHAR(old.a_s400_payroll_count), aws_oracle_ext.TO_CHAR(new.a_s400_payroll_count), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.coa_fee_account_name <> old.coa_fee_account_name) AND new.coa_fee_account_name IS NOT NULL AND old.coa_fee_account_name IS NOT NULL) OR (new.coa_fee_account_name IS NULL AND old.coa_fee_account_name IS NOT NULL) OR (new.coa_fee_account_name IS NOT NULL AND old.coa_fee_account_name IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'CoaFeeAccountName', aws_oracle_ext.TO_CHAR(old.coa_fee_account_name), aws_oracle_ext.TO_CHAR(new.coa_fee_account_name), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.coa_sales_tax_account_name <> old.coa_sales_tax_account_name) AND new.coa_sales_tax_account_name IS NOT NULL AND old.coa_sales_tax_account_name IS NOT NULL) OR (new.coa_sales_tax_account_name IS NULL AND old.coa_sales_tax_account_name IS NOT NULL) OR (new.coa_sales_tax_account_name IS NOT NULL AND old.coa_sales_tax_account_name IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'CoaSalesTaxAccountName', aws_oracle_ext.TO_CHAR(old.coa_sales_tax_account_name), aws_oracle_ext.TO_CHAR(new.coa_sales_tax_account_name), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.license_number <> old.license_number) AND new.license_number IS NOT NULL AND old.license_number IS NOT NULL) OR (new.license_number IS NULL AND old.license_number IS NOT NULL) OR (new.license_number IS NOT NULL AND old.license_number IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'LicenseNumber', aws_oracle_ext.TO_CHAR(old.license_number), aws_oracle_ext.TO_CHAR(new.license_number), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.tax_table_id <> old.tax_table_id) AND new.tax_table_id IS NOT NULL AND old.tax_table_id IS NOT NULL) OR (new.tax_table_id IS NULL AND old.tax_table_id IS NOT NULL) OR (new.tax_table_id IS NOT NULL AND old.tax_table_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'TaxTableId', aws_oracle_ext.TO_CHAR(old.tax_table_id), aws_oracle_ext.TO_CHAR(new.tax_table_id), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.file_id <> old.file_id) AND new.file_id IS NOT NULL AND old.file_id IS NOT NULL) OR (new.file_id IS NULL AND old.file_id IS NOT NULL) OR (new.file_id IS NOT NULL AND old.file_id IS NULL)) THEN
            BEGIN
                SELECT
                    new.company_fk
                    INTO STRICT v_company_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'QuickbooksInfo', 'FileId', aws_oracle_ext.TO_CHAR(old.file_id), aws_oracle_ext.TO_CHAR(new.file_id), new.quickbooks_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_tax_cs_info_at$psp_tax_company_service_info()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        creator_id
        INTO STRICT v_creator_id
        FROM pspadm.psp_company_service
        WHERE company_service_seq = new.tax_company_service_info_seq;
    SELECT
        modifier_id
        INTO STRICT v_modifier_id
        FROM pspadm.psp_company_service
        WHERE company_service_seq = new.tax_company_service_info_seq;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.w2_delivery_preference_cd <> old.w2_delivery_preference_cd) AND new.w2_delivery_preference_cd IS NOT NULL AND old.w2_delivery_preference_cd IS NOT NULL) OR (new.w2_delivery_preference_cd IS NULL AND old.w2_delivery_preference_cd IS NOT NULL) OR (new.w2_delivery_preference_cd IS NOT NULL AND old.w2_delivery_preference_cd IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_service AS t1
                    WHERE t1.company_service_seq = new.tax_company_service_info_seq;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'TaxCompanyServiceInfo', 'W2DeliveryPreferenceCd', aws_oracle_ext.TO_CHAR(old.w2_delivery_preference_cd), aws_oracle_ext.TO_CHAR(new.w2_delivery_preference_cd), new.tax_company_service_info_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.psp_tax_penalty_interest_at$psp_tax_penalty_interest()
RETURNS trigger
AS
$BODY$
DECLARE
    v_company_fk CHARACTER VARYING(255);
    v_creator_id CHARACTER VARYING(30);
    v_modifier_id CHARACTER VARYING(30);
    v_psp_utc_systimestamp TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    SELECT
        new.creator_id
        INTO STRICT v_creator_id;
    SELECT
        new.modifier_id
        INTO STRICT v_modifier_id;

    IF (TG_OP = 'UPDATE') THEN
        IF (((new.payment_method <> old.payment_method) AND new.payment_method IS NOT NULL AND old.payment_method IS NOT NULL) OR (new.payment_method IS NULL AND old.payment_method IS NOT NULL) OR (new.payment_method IS NOT NULL AND old.payment_method IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    WHERE t1.company_agency_seq = new.company_agency_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'TaxPenaltyInterest', 'PaymentMethod', aws_oracle_ext.TO_CHAR(old.payment_method), aws_oracle_ext.TO_CHAR(new.payment_method), new.tax_penalty_interest_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;

        IF (((new.amount <> old.amount) AND new.amount IS NOT NULL AND old.amount IS NOT NULL) OR (new.amount IS NULL AND old.amount IS NOT NULL) OR (new.amount IS NOT NULL AND old.amount IS NULL)) THEN
            BEGIN
                SELECT
                    t1.company_fk
                    INTO STRICT v_company_fk
                    FROM pspadm.psp_company_agency AS t1
                    WHERE t1.company_agency_seq = new.company_agency_fk;
                EXCEPTION
                    WHEN no_data_found THEN
                        v_company_fk := NULL;
                    WHEN others THEN
                        RAISE;
            END;

            IF v_company_fk IS NOT NULL THEN
                /*
                [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
                SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL
                */
                INSERT INTO pspadm.psp_property_audit (property_audit_seq, version, created_date, modified_date, class_name, property_name, old_property_value, new_property_value, object_identifier, user_id, audit_date, company_fk)
                VALUES (aws_oracle_ext.sys_guid(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, 'TaxPenaltyInterest', 'Amount', aws_oracle_ext.TO_CHAR(old.amount), aws_oracle_ext.TO_CHAR(new.amount), new.tax_penalty_interest_seq, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
            END IF;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.quest_sl_temp_explain1_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE quest_sl_temp_explain1$tmp(
    statement_id CHARACTER VARYING(30),
    plan_id DOUBLE PRECISION,
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    remarks CHARACTER VARYING(4000),
    operation CHARACTER VARYING(30),
    options CHARACTER VARYING(255),
    object_node CHARACTER VARYING(128),
    object_owner CHARACTER VARYING(30),
    object_name CHARACTER VARYING(30),
    object_alias CHARACTER VARYING(65),
    object_instance NUMERIC(38,0),
    object_type CHARACTER VARYING(30),
    optimizer CHARACTER VARYING(255),
    search_columns DOUBLE PRECISION,
    id NUMERIC(38,0),
    parent_id NUMERIC(38,0),
    depth NUMERIC(38,0),
    position NUMERIC(38,0),
    cost NUMERIC(38,0),
    cardinality NUMERIC(38,0),
    bytes NUMERIC(38,0),
    other_tag CHARACTER VARYING(255),
    partition_start CHARACTER VARYING(255),
    partition_stop CHARACTER VARYING(255),
    partition_id NUMERIC(38,0),
    other TEXT,
    distribution CHARACTER VARYING(30),
    cpu_cost NUMERIC(38,0),
    io_cost NUMERIC(38,0),
    temp_space NUMERIC(38,0),
    access_predicates CHARACTER VARYING(4000),
    filter_predicates CHARACTER VARYING(4000),
    projection CHARACTER VARYING(4000),
    time NUMERIC(20,2),
    qblock_name CHARACTER VARYING(30)
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT PRESERVE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO quest_sl_temp_explain1$tmp(statement_id,plan_id,timestamp,remarks,operation,options,object_node,object_owner,object_name,object_alias,object_instance,object_type,optimizer,search_columns,id,parent_id,depth,position,cost,cardinality,bytes,other_tag,partition_start,partition_stop,partition_id,other,distribution,cpu_cost,io_cost,temp_space,access_predicates,filter_predicates,projection,time,qblock_name)
 VALUES (new.statement_id,new.plan_id,new.timestamp,new.remarks,new.operation,new.options,new.object_node,new.object_owner,new.object_name,new.object_alias,new.object_instance,new.object_type,new.optimizer,new.search_columns,new.id,new.parent_id,new.depth,new.position,new.cost,new.cardinality,new.bytes,new.other_tag,new.partition_start,new.partition_stop,new.partition_id,new.other,new.distribution,new.cpu_cost,new.io_cost,new.temp_space,new.access_predicates,new.filter_predicates,new.projection,new.time,new.qblock_name) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE quest_sl_temp_explain1$tmp
 SET statement_id=new.statement_id, plan_id=new.plan_id, timestamp=new.timestamp, remarks=new.remarks, operation=new.operation, options=new.options, object_node=new.object_node, object_owner=new.object_owner, object_name=new.object_name, object_alias=new.object_alias, object_instance=new.object_instance, object_type=new.object_type, optimizer=new.optimizer, search_columns=new.search_columns, id=new.id, parent_id=new.parent_id, depth=new.depth, position=new.position, cost=new.cost, cardinality=new.cardinality, bytes=new.bytes, other_tag=new.other_tag, partition_start=new.partition_start, partition_stop=new.partition_stop, partition_id=new.partition_id, other=new.other, distribution=new.distribution, cpu_cost=new.cpu_cost, io_cost=new.io_cost, temp_space=new.temp_space, access_predicates=new.access_predicates, filter_predicates=new.filter_predicates, projection=new.projection, time=new.time, qblock_name=new.qblock_name 
WHERE statement_id=old.statement_id AND plan_id=old.plan_id AND timestamp=old.timestamp AND remarks=old.remarks AND operation=old.operation AND options=old.options AND object_node=old.object_node AND object_owner=old.object_owner AND object_name=old.object_name AND object_alias=old.object_alias AND object_instance=old.object_instance AND object_type=old.object_type AND optimizer=old.optimizer AND search_columns=old.search_columns AND id=old.id AND parent_id=old.parent_id AND depth=old.depth AND position=old.position AND cost=old.cost AND cardinality=old.cardinality AND bytes=old.bytes AND other_tag=old.other_tag AND partition_start=old.partition_start AND partition_stop=old.partition_stop AND partition_id=old.partition_id AND other=old.other AND distribution=old.distribution AND cpu_cost=old.cpu_cost AND io_cost=old.io_cost AND temp_space=old.temp_space AND access_predicates=old.access_predicates AND filter_predicates=old.filter_predicates AND projection=old.projection AND time=old.time AND qblock_name=old.qblock_name RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM quest_sl_temp_explain1$tmp 
WHERE statement_id=old.statement_id AND plan_id=old.plan_id AND timestamp=old.timestamp AND remarks=old.remarks AND operation=old.operation AND options=old.options AND object_node=old.object_node AND object_owner=old.object_owner AND object_name=old.object_name AND object_alias=old.object_alias AND object_instance=old.object_instance AND object_type=old.object_type AND optimizer=old.optimizer AND search_columns=old.search_columns AND id=old.id AND parent_id=old.parent_id AND depth=old.depth AND position=old.position AND cost=old.cost AND cardinality=old.cardinality AND bytes=old.bytes AND other_tag=old.other_tag AND partition_start=old.partition_start AND partition_stop=old.partition_stop AND partition_id=old.partition_id AND other=old.other AND distribution=old.distribution AND cpu_cost=old.cpu_cost AND io_cost=old.io_cost AND temp_space=old.temp_space AND access_predicates=old.access_predicates AND filter_predicates=old.filter_predicates AND projection=old.projection AND time=old.time AND qblock_name=old.qblock_name; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.quest_sl_temp_explain2_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE quest_sl_temp_explain2$tmp(
    statement_id CHARACTER VARYING(30),
    plan_id DOUBLE PRECISION,
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    remarks CHARACTER VARYING(4000),
    operation CHARACTER VARYING(30),
    options CHARACTER VARYING(255),
    object_node CHARACTER VARYING(128),
    object_owner CHARACTER VARYING(30),
    object_name CHARACTER VARYING(30),
    object_alias CHARACTER VARYING(65),
    object_instance NUMERIC(38,0),
    object_type CHARACTER VARYING(30),
    optimizer CHARACTER VARYING(255),
    search_columns DOUBLE PRECISION,
    id NUMERIC(38,0),
    parent_id NUMERIC(38,0),
    depth NUMERIC(38,0),
    position NUMERIC(38,0),
    cost NUMERIC(38,0),
    cardinality NUMERIC(38,0),
    bytes NUMERIC(38,0),
    other_tag CHARACTER VARYING(255),
    partition_start CHARACTER VARYING(255),
    partition_stop CHARACTER VARYING(255),
    partition_id NUMERIC(38,0),
    other TEXT,
    other_xml TEXT,
    distribution CHARACTER VARYING(30),
    cpu_cost NUMERIC(38,0),
    io_cost NUMERIC(38,0),
    temp_space NUMERIC(38,0),
    access_predicates CHARACTER VARYING(4000),
    filter_predicates CHARACTER VARYING(4000),
    projection CHARACTER VARYING(4000),
    time NUMERIC(20,2),
    qblock_name CHARACTER VARYING(30)
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT PRESERVE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO quest_sl_temp_explain2$tmp(statement_id,plan_id,timestamp,remarks,operation,options,object_node,object_owner,object_name,object_alias,object_instance,object_type,optimizer,search_columns,id,parent_id,depth,position,cost,cardinality,bytes,other_tag,partition_start,partition_stop,partition_id,other,other_xml,distribution,cpu_cost,io_cost,temp_space,access_predicates,filter_predicates,projection,time,qblock_name)
 VALUES (new.statement_id,new.plan_id,new.timestamp,new.remarks,new.operation,new.options,new.object_node,new.object_owner,new.object_name,new.object_alias,new.object_instance,new.object_type,new.optimizer,new.search_columns,new.id,new.parent_id,new.depth,new.position,new.cost,new.cardinality,new.bytes,new.other_tag,new.partition_start,new.partition_stop,new.partition_id,new.other,new.other_xml,new.distribution,new.cpu_cost,new.io_cost,new.temp_space,new.access_predicates,new.filter_predicates,new.projection,new.time,new.qblock_name) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE quest_sl_temp_explain2$tmp
 SET statement_id=new.statement_id, plan_id=new.plan_id, timestamp=new.timestamp, remarks=new.remarks, operation=new.operation, options=new.options, object_node=new.object_node, object_owner=new.object_owner, object_name=new.object_name, object_alias=new.object_alias, object_instance=new.object_instance, object_type=new.object_type, optimizer=new.optimizer, search_columns=new.search_columns, id=new.id, parent_id=new.parent_id, depth=new.depth, position=new.position, cost=new.cost, cardinality=new.cardinality, bytes=new.bytes, other_tag=new.other_tag, partition_start=new.partition_start, partition_stop=new.partition_stop, partition_id=new.partition_id, other=new.other, other_xml=new.other_xml, distribution=new.distribution, cpu_cost=new.cpu_cost, io_cost=new.io_cost, temp_space=new.temp_space, access_predicates=new.access_predicates, filter_predicates=new.filter_predicates, projection=new.projection, time=new.time, qblock_name=new.qblock_name 
WHERE statement_id=old.statement_id AND plan_id=old.plan_id AND timestamp=old.timestamp AND remarks=old.remarks AND operation=old.operation AND options=old.options AND object_node=old.object_node AND object_owner=old.object_owner AND object_name=old.object_name AND object_alias=old.object_alias AND object_instance=old.object_instance AND object_type=old.object_type AND optimizer=old.optimizer AND search_columns=old.search_columns AND id=old.id AND parent_id=old.parent_id AND depth=old.depth AND position=old.position AND cost=old.cost AND cardinality=old.cardinality AND bytes=old.bytes AND other_tag=old.other_tag AND partition_start=old.partition_start AND partition_stop=old.partition_stop AND partition_id=old.partition_id AND other=old.other AND other_xml=old.other_xml AND distribution=old.distribution AND cpu_cost=old.cpu_cost AND io_cost=old.io_cost AND temp_space=old.temp_space AND access_predicates=old.access_predicates AND filter_predicates=old.filter_predicates AND projection=old.projection AND time=old.time AND qblock_name=old.qblock_name RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM quest_sl_temp_explain2$tmp 
WHERE statement_id=old.statement_id AND plan_id=old.plan_id AND timestamp=old.timestamp AND remarks=old.remarks AND operation=old.operation AND options=old.options AND object_node=old.object_node AND object_owner=old.object_owner AND object_name=old.object_name AND object_alias=old.object_alias AND object_instance=old.object_instance AND object_type=old.object_type AND optimizer=old.optimizer AND search_columns=old.search_columns AND id=old.id AND parent_id=old.parent_id AND depth=old.depth AND position=old.position AND cost=old.cost AND cardinality=old.cardinality AND bytes=old.bytes AND other_tag=old.other_tag AND partition_start=old.partition_start AND partition_stop=old.partition_stop AND partition_id=old.partition_id AND other=old.other AND other_xml=old.other_xml AND distribution=old.distribution AND cpu_cost=old.cpu_cost AND io_cost=old.io_cost AND temp_space=old.temp_space AND access_predicates=old.access_predicates AND filter_predicates=old.filter_predicates AND projection=old.projection AND time=old.time AND qblock_name=old.qblock_name; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.sys_temp_fbt_iud()
RETURNS trigger
AS
$BODY$
BEGIN
BEGIN
CREATE TEMPORARY TABLE sys_temp_fbt$tmp(
    schema CHARACTER VARYING(32),
    object_name CHARACTER VARYING(32),
    "OBJECT#" DOUBLE PRECISION,
    rid CHARACTER VARYING(8000),
    action CHARACTER(1)
)
        WITH (
        OIDS=FALSE
        )
ON COMMIT PRESERVE ROWS;


EXCEPTION WHEN OTHERS THEN END;

IF tg_op = 'INSERT' THEN 
INSERT INTO sys_temp_fbt$tmp(schema,object_name,OBJECT#,rid,action)
 VALUES (new.schema,new.object_name,new.OBJECT#,new.rid,new.action) RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'UPDATE' THEN 
UPDATE sys_temp_fbt$tmp
 SET schema=new.schema, object_name=new.object_name, OBJECT#=new.OBJECT#, rid=new.rid, action=new.action 
WHERE schema=old.schema AND object_name=old.object_name AND OBJECT#=old.OBJECT# AND rid=old.rid AND action=old.action RETURNING * INTO NEW;
RETURN new;
ELSIF tg_op = 'DELETE' THEN 
DELETE FROM sys_temp_fbt$tmp 
WHERE schema=old.schema AND object_name=old.object_name AND OBJECT#=old.OBJECT# AND rid=old.rid AND action=old.action; 
RETURN old;
END IF;
END
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.tr_ins_company_event_timestamp$psp_company_event()
RETURNS trigger
AS
$BODY$
BEGIN
    IF (new.event_type_cd <> 'Strike' AND new.creator_id <> 'QBOEMigrationBatchJob' AND new.creator_id <> 'AS400MigrationBatchJob') THEN
        BEGIN
            /*
            [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
            SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO :NEW.EVENT_TIME_STAMP FROM DUAL
            */
        END;
    END IF;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.tr_upd_company_event_timestamp$psp_company_event()
RETURNS trigger
AS
$BODY$
BEGIN
    IF (new.event_type_cd <> 'Strike') THEN
        new.event_time_stamp := old.event_time_stamp;
    END IF;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.tr_upd_dd_limits$psp_property_audit()
RETURNS trigger
AS
$BODY$
DECLARE
    v_default_company_limit CHARACTER VARYING(255);
    v_default_employee_limit CHARACTER VARYING(255);
BEGIN
    IF new.class_name = 'DDCompanyServiceInfo' AND new.property_name = 'OverrideCompanyLimitAmount' THEN
        SELECT
            value
            INTO STRICT v_default_company_limit
            FROM pspadm.psp_limit_value AS lv
            JOIN pspadm.psp_limit_rule
                ON lv.limit_rule_fk = limit_rule_seq
            JOIN pspadm.psp_offering AS o
                ON limit_rule_seq = o.limit_rule_fk
            JOIN pspadm.psp_company_offering AS co
                ON o.offering_seq = co.offering_fk
            WHERE co.company_fk = new.company_fk AND o.service_code = 'DirectDeposit' AND lv.name = 'DefaultCompanyLimit' AND co.created_date = (SELECT
                MAX(ico.created_date)
                FROM pspadm.psp_company_offering AS ico
                JOIN pspadm.psp_offering AS io
                    ON io.offering_seq = ico.offering_fk
                WHERE ico.company_fk = new.company_fk AND io.service_code = 'DirectDeposit');

        IF new.old_property_value IS NULL THEN
            new.old_property_value := v_default_company_limit;
        END IF;

        IF new.new_property_value IS NULL THEN
            new.new_property_value := v_default_company_limit;
        END IF;
    END IF;

    IF new.class_name = 'DDCompanyServiceInfo' AND new.property_name = 'OverrideEmployeeLimitAmount' THEN
        SELECT
            value
            INTO STRICT v_default_employee_limit
            FROM pspadm.psp_limit_value AS lv
            JOIN pspadm.psp_limit_rule
                ON lv.limit_rule_fk = limit_rule_seq
            JOIN pspadm.psp_offering AS o
                ON limit_rule_seq = o.limit_rule_fk
            JOIN pspadm.psp_company_offering AS co
                ON o.offering_seq = co.offering_fk
            WHERE co.company_fk = new.company_fk AND o.service_code = 'DirectDeposit' AND lv.name = 'DefaultEmployeeLimit' AND co.created_date = (SELECT
                MAX(ico.created_date)
                FROM pspadm.psp_company_offering AS ico
                JOIN pspadm.psp_offering AS io
                    ON io.offering_seq = ico.offering_fk
                WHERE ico.company_fk = new.company_fk AND io.service_code = 'DirectDeposit');

        IF new.old_property_value IS NULL THEN
            new.old_property_value := v_default_employee_limit;
        END IF;

        IF new.new_property_value IS NULL THEN
            new.new_property_value := v_default_employee_limit;
        END IF;
    END IF;

    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        RETURN NEW;
    END IF;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION pspadm.trc_ledger_balance_calc_ai$psp_financial_trans_state()
RETURNS trigger
AS
$BODY$
DECLARE
    rt_financial_txn pspadm.psp_financial_transaction := ROW (NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)::pspadm.psp_financial_transaction;
    v_previous_balance PSPADM.PSP_LEDGER_BALANCE.BALANCE_AMOUNT%TYPE;
    c_credit_cd CONSTANT PSPADM.PSP_POSTING_RULE.CREDIT_DEBIT_IND%TYPE := 'C';
    c_debit_cd CONSTANT PSPADM.PSP_POSTING_RULE.CREDIT_DEBIT_IND%TYPE := 'D';
    c_add_credit_cd CONSTANT PSPADM.PSP_LEDGER_ACCOUNT.BALANCE_CALCULATION_RULE%TYPE := 'CreditAddsToBalance';
    c_add_debit_cd CONSTANT PSPADM.PSP_LEDGER_ACCOUNT.BALANCE_CALCULATION_RULE%TYPE := 'DebitAddsToBalance';
    c_version_default CONSTANT PSPADM.PSP_LEDGER_BALANCE.VERSION%TYPE := 0;
    c_realmid_default CONSTANT PSPADM.PSP_LEDGER_BALANCE.REALM_ID%TYPE := - 1;
    aws$rec RECORD;
    rec_ledger_info RECORD;
    sql$rowcount BIGINT;
BEGIN
    /* HISTORY */
    /* 04.25.2008   EMR   Created trigger */
    /* DESIGN NOTES: */
    /* */
    /* ledger balance is per day per company per ledger account */
    /* */
    /* for each ledger affected by the posting rules */
    /* using txn state and txn type */
    /* */
    /* update ledger balance matching */
    /* company */
    /* ledger account */
    /* trunc(date) */
    /* */
    /* if row not found then */
    /* insert new row */
    /* key information is at the transaction level: */
    /* tnx type, txn amount, company id */
    /* */
    /* In optimizing the ach offload process, Tushar found out that this trigger */
    /* is expensive. Because we do not really use psp_ledger_balance in real time */
    /* we decided to move the ledger balance update to a daily batch job */
    /* However, we still need to keep this in place for Migration purposes */
    /* Ideally, this would be transformed into a sproc that the migration code calls */
    /* */
    IF (new.creator_id != 'QBOEMigrationBatchJob' AND new.creator_id != 'AS400MigrationBatchJob') THEN
        RETURN NEW;
    END IF;
    /* */
    /* All logic below only runs for migration */
    /* */
    SELECT
        *
        INTO STRICT aws$rec
        FROM pspadm.psp_financial_transaction
        WHERE financial_transaction_seq = new.financial_transaction_fk;
    rt_financial_txn := aws$rec;
    /* get a list of ledger accounts that need to be updated for this */
    /* new transaction state according to the posting rules and also */
    /* determine whether to add or subtract the amount */

    FOR rec_ledger_info IN
    SELECT
        a.ledger_account_cd AS ledger_acct_cd,
        CASE b.credit_debit_ind
            WHEN c_debit_cd THEN
            CASE a.balance_calculation_rule
                WHEN c_add_debit_cd THEN 1
                WHEN c_add_credit_cd THEN - 1
            END
            WHEN c_credit_cd THEN
            CASE a.balance_calculation_rule
                WHEN c_add_debit_cd THEN - 1
                WHEN c_add_credit_cd THEN 1
            END
        END AS ledger_amt_multiplier
        FROM pspadm.psp_ledger_account AS a, pspadm.psp_posting_rule AS b
        WHERE a.ledger_account_cd = b.ledger_account_fk AND b.transaction_state_fk = new.transaction_state_fk AND b.transaction_type_fk = rt_financial_txn.transaction_type_fk
    LOOP
        /* for each ledger account add or subtract the new transaction amount */
        UPDATE pspadm.psp_ledger_balance
        SET version = new.version + 1, modified_date = new.transaction_state_eff_date, balance_amount = balance_amount + (rt_financial_txn.financial_transaction_amount * rec_ledger_info.ledger_amt_multiplier)
            WHERE company_fk = rt_financial_txn.company_fk AND ledger_account_fk = rec_ledger_info.ledger_acct_cd AND DATE(balance_date) = DATE(new.transaction_state_eff_date);
        GET DIAGNOSTICS sql$rowcount = ROW_COUNT;

        IF (sql$rowcount = 0) THEN
            /* compute the current balance, meaning take the balance */
            /* from a previous day and start there.  Because the balance */
            /* is keyed by date, the balance could be for a previous day */
            /* or might not exist at all. */
            BEGIN
                SELECT
                    balance_amount
                    INTO STRICT v_previous_balance
                    FROM pspadm.psp_ledger_balance
                    WHERE company_fk = rt_financial_txn.company_fk AND ledger_account_fk = rec_ledger_info.ledger_acct_cd AND DATE(balance_date) = (SELECT
                        MAX(DATE(balance_date))
                        FROM pspadm.psp_ledger_balance
                        WHERE company_fk = rt_financial_txn.company_fk AND ledger_account_fk = rec_ledger_info.ledger_acct_cd);
                EXCEPTION
                    WHEN no_data_found THEN
                        v_previous_balance := 0;
                    WHEN others THEN
                        RAISE;
            END;
            /* insert a new balance record for the date the tnx state changed. */
            /* please note that if two inserts arrive at the same time one */
            /* will win and the other will be blocked and will eventually get */
            /* the standard typical duplicate pk.  At that time then an update */
            /* will be needed. */

            BEGIN
                BEGIN
                    /*
                    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
                    INSERT INTO PSP_LEDGER_BALANCE (
                              LEDGER_BALANCE_SEQ,
                                  VERSION,
                                  CREATED_DATE,
                              MODIFIED_DATE,
                                  REALM_ID,
                                  BALANCE_AMOUNT,
                              BALANCE_DATE,
                                  LEDGER_ACCOUNT_FK,
                                  COMPANY_FK
                                )
                            VALUES (
                                FN_FORMAT_SYSGUID(SYS_GUID()),
                              c_version_default,
                              SYS_EXTRACT_UTC(SYSTIMESTAMP),
                              SYS_EXTRACT_UTC(SYSTIMESTAMP),
                              c_realmid_default,
                                  v_previous_balance +
                              (
                                   rt_financial_txn.FINANCIAL_TRANSACTION_AMOUNT *
                                   rec_ledger_info.LEDGER_AMT_MULTIPLIER
                                  ),
                              :NEW.TRANSACTION_STATE_EFF_DATE,
                              rec_ledger_info.LEDGER_ACCT_CD,
                              rt_financial_txn.COMPANY_FK
                              )
                    */
                END;
                EXCEPTION
                    WHEN unique_violation THEN
                        UPDATE pspadm.psp_ledger_balance
                        SET version = new.version + 1, modified_date = new.transaction_state_eff_date, balance_amount = balance_amount + (rt_financial_txn.financial_transaction_amount * rec_ledger_info.ledger_amt_multiplier)
                            WHERE company_fk = rt_financial_txn.company_fk AND ledger_account_fk = rec_ledger_info.ledger_acct_cd AND DATE(balance_date) = DATE(new.transaction_state_eff_date);
                    WHEN others THEN
                        RAISE;
            END;
        END IF;
    END LOOP;
    RETURN NEW;
    EXCEPTION
        WHEN others THEN
            RAISE USING hint = -20501, message = FALSE, detail = 'User-defined exception';
            RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;




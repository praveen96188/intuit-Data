-- ------------ Write CREATE-PROCEDURE-stage scripts -----------

CREATE PROCEDURE pspadm.pk_gems_accounts_receivable$prc_assoc_fin_txn_states(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
BEGIN
    PERFORM PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE$Init();
    UPDATE pspadm.psp_financial_trans_state AS fts
    SET gems_upload_batch_fk = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_gems_upload_batch_key', ptp => NULL::CHARACTER VARYING(100)), version = version + 1, modifier_id = p_user_id, modified_date = p_app_server_date
        WHERE fts.gems_upload_batch_fk IS NULL AND DATE(fts.transaction_state_eff_date) >= DATE(aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE)) - (aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', ptp => NULL::DOUBLE PRECISION)::NUMERIC || ' days')::INTERVAL AND fts.transaction_state_eff_date >= DATE(aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE)) - (aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', ptp => NULL::DOUBLE PRECISION)::NUMERIC || ' days')::INTERVAL AND fts.transaction_state_fk IN ('Executed', 'Voided', 'Completed') AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_financial_transaction AS ft, pspadm.psp_posting_rule AS pr, pspadm.psp_ledger_account AS la
            WHERE ft.financial_transaction_seq = fts.financial_transaction_fk AND pr.transaction_state_fk = fts.transaction_state_fk AND pr.ledger_account_fk = la.ledger_account_cd AND pr.transaction_type_fk = ft.transaction_type_fk AND la.reporting_frequency = 'Daily' AND la.ledger_account_type IN ('Income', 'SUTax') AND ft.sku IS NOT NULL);
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_gems_accounts_receivable$prc_calc_reveivables(INOUT p_cur_gems_daily_upload REFCURSOR)
AS 
$BODY$
/* result set to return to client */
DECLARE
    p_cur_gems_daily_upload$ATTRIBUTES aws_oracle_data.TCursorAttributes;
BEGIN
    PERFORM PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE$Init();
    p_cur_gems_daily_upload := NULL;
    OPEN p_cur_gems_daily_upload FOR
    SELECT
        aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_batch_id', ptp => NULL::DOUBLE PRECISION) AS batch_id, ft.sku, SUM(CASE la.ledger_account_type
            WHEN 'Income' THEN ft.sku_quantity *
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
            END
            ELSE 0::CHARACTER VARYING
        END) AS sku_quantity, SUM(CASE la.ledger_account_type
            WHEN 'Income' THEN ft.financial_transaction_amount *
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
            END
            ELSE 0::CHARACTER VARYING
        END) AS income_amt, SUM(CASE la.ledger_account_type
            WHEN 'SUTax' THEN ft.financial_transaction_amount *
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
            END
            ELSE 0::CHARACTER VARYING
        END) AS tax_amt
        FROM pspadm.psp_financial_transaction AS ft, pspadm.psp_financial_trans_state AS fts, pspadm.psp_posting_rule AS pr, pspadm.psp_ledger_account AS la
        WHERE ft.financial_transaction_seq = fts.financial_transaction_fk AND fts.transaction_state_eff_date >= DATE(aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE)) - (aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', ptp => NULL::DOUBLE PRECISION)::NUMERIC || ' days')::INTERVAL AND
        /* AND ft.current_transaction_state_fk = fts.transaction_state_fk */
        fts.transaction_state_fk = pr.transaction_state_fk AND fts.gems_upload_batch_fk = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_gems_upload_batch_key', ptp => NULL::CHARACTER VARYING(100)) AND pr.ledger_account_fk = la.ledger_account_cd AND pr.transaction_type_fk = ft.transaction_type_fk AND ft.sku IS NOT NULL AND la.reporting_frequency = 'Daily' AND la.ledger_account_type IN ('Income', 'SUTax')
        GROUP BY ft.sku
        ORDER BY ft.sku;
    p_cur_gems_daily_upload$ATTRIBUTES := ROW (TRUE, 0, NULL, NULL);
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_gems_accounts_receivable$prc_create_upload_batch(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
BEGIN
    PERFORM PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE$Init();
    INSERT INTO pspadm.psp_gems_upload_batch (gems_upload_batch_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, batch_id, batch_type, upload_status, status_effective_date)
    VALUES (aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_gems_upload_batch_key', ptp => NULL::CHARACTER VARYING(100)), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, - 1, aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_batch_id', ptp => NULL::DOUBLE PRECISION), 'Daily', 'InProcess', aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE));
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_gems_accounts_receivable$prc_main(INOUT p_cur_gems_daily_upload REFCURSOR, IN p_old_upload_batch_id DOUBLE PRECISION, IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* result set to return to client */
/* existing gems upload batch to supercede (0 if want new) */
/* for audit purposes */
/* UTC Date */
DECLARE
    PK_GEMS_ACCOUNTS_RECEIVABLE$g_psp_date TIMESTAMP WITHOUT TIME ZONE;
    PK_GEMS_ACCOUNTS_RECEIVABLE$g_lookback_days DOUBLE PRECISION;
    PK_GEMS_ACCOUNTS_RECEIVABLE$g_gems_upload_batch_key TEXT;
    PK_GEMS_ACCOUNTS_RECEIVABLE$g_batch_id DOUBLE PRECISION;
BEGIN
    PERFORM PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE$Init();
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp)
            INTO g_psp_date
            FROM DUAL
    */
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_psp_date', pval => PK_GEMS_ACCOUNTS_RECEIVABLE$g_psp_date);

    BEGIN
        SELECT
            aws_oracle_ext.TO_NUMBER(COALESCE(system_parameter_value, '5'))
            INTO STRICT pk_gems_accounts_receivable$g_lookback_days
            FROM pspadm.psp_system_parameter
            WHERE system_parameter_cd = 'GEMS_ACCOUNTS_RECEIVABLE_LOOKBACK_DAYS';
        PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', pval => PK_GEMS_ACCOUNTS_RECEIVABLE$g_lookback_days);
        EXCEPTION
            WHEN no_data_found THEN
                PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', pval => 5);
            WHEN others THEN
                PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_lookback_days', pval => 5);
    END;
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid())
        INTO STRICT pk_gems_accounts_receivable$g_gems_upload_batch_key;
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_gems_upload_batch_key', pval => PK_GEMS_ACCOUNTS_RECEIVABLE$g_gems_upload_batch_key);
    SELECT
        nextval('pspadm.seq_gems_upload_batch_id')
        INTO STRICT pk_gems_accounts_receivable$g_batch_id;
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.PK_GEMS_ACCOUNTS_RECEIVABLE', pvariable => 'g_batch_id', pval => PK_GEMS_ACCOUNTS_RECEIVABLE$g_batch_id);
    CALL pspadm.pk_gems_accounts_receivable$prc_supercede_upload_batch(p_old_upload_batch_id, p_user_id, p_app_server_date);
    CALL pspadm.pk_gems_accounts_receivable$prc_create_upload_batch(p_user_id, p_app_server_date);
    CALL pspadm.pk_gems_accounts_receivable$prc_assoc_fin_txn_states(p_user_id, p_app_server_date);
    CALL pspadm.pk_gems_accounts_receivable$prc_calc_reveivables(p_cur_gems_daily_upload);
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_gems_accounts_receivable$prc_supercede_upload_batch(IN p_old_upload_batch_id DOUBLE PRECISION, IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* existing gems upload batch to supercede (0 if none) */
/* for audit purposes */
/* UTC Date */
DECLARE
    v_old_batch_key CHARACTER VARYING(100);
BEGIN
    IF p_old_upload_batch_id != 0 THEN
        BEGIN
            SELECT
                gems_upload_batch_seq
                INTO STRICT v_old_batch_key
                FROM pspadm.psp_gems_upload_batch
                WHERE batch_id = p_old_upload_batch_id;
            UPDATE pspadm.psp_gems_upload_batch
            SET version = version + 1, modifier_id = p_user_id, modified_date = p_app_server_date, upload_status = 'Superceded', status_effective_date = p_app_server_date
                WHERE gems_upload_batch_seq = v_old_batch_key;
            UPDATE pspadm.psp_financial_trans_state
            SET gems_upload_batch_fk = NULL
                WHERE gems_upload_batch_fk = v_old_batch_key;
            /* potentional of select returning zero data, do nothing... */
            EXCEPTION
                WHEN no_data_found THEN
                    NULL;
        END;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_payroll_item_totals$prc_comp_qtr_payroll_item_tot(IN p_company_seq TEXT, IN p_qtr_start_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* Company seq */
/* Quarter start date in UTC */
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
/* current system UTC date and time */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp) INTO v_utc_date FROM DUAL
    */
    /* dbms_output.put_line('Merging PRC_COMP_QTR_PAYROLLITEM_TOT, starting merge  - Started at ' || to_char(systimestamp, 'hh24:mi:ss')); */
    /* Deleting records for voided payrolls */
    DELETE FROM pspadm.psp_ee_payrollitem_qtrtotals AS eepayrollitemqtrtotals
        WHERE eepayrollitemqtrtotals.quarter = (SELECT
            aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'Q'))) AND eepayrollitemqtrtotals.year = (SELECT
            aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'YYYY'))) AND EXISTS (SELECT
            '1'
            FROM pspadm.psp_company_payroll_item AS pcpi
            WHERE pcpi.company_payroll_item_seq = eepayrollitemqtrtotals.company_payroll_item_fk AND pcpi.company_fk = p_company_seq);
    INSERT INTO pspadm.psp_ee_payrollitem_qtrtotals (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date, modified_date, realm_id, quarter, year, amount, taxable_wages, total_wages, tips_taxable_wages_amount, company_payroll_item_fk, employee_fk)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'PK_PAYROLL_ITEM_TOTALS', v_utc_date, v_utc_date, - 1, aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'Q')), aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'YYYY')), src.amount, 0, 0, 0, src.company_payroll_item_seq, src.source_employee_fk
        FROM (SELECT
            company_payroll_item_seq, source_employee_fk, SUM(amount) AS amount
            FROM (SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pcomp.compensation_amount) AS amount
                FROM pspadm.psp_compensation AS pcomp
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pcomp.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE DATE(DATE_TRUNC('QUARTER', pr.paycheck_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.company_fk = p_company_seq AND
                /* :companySeq */
                pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pded.deduction_amount) AS amount
                FROM pspadm.psp_deduction AS pded
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pded.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pded.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE DATE(DATE_TRUNC('QUARTER', pr.paycheck_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.company_fk = p_company_seq AND
                /* :companySeq */
                pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pec.contribution_amount) AS amount
                FROM pspadm.psp_employer_contribution AS pec
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pec.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pec.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE DATE(DATE_TRUNC('QUARTER', pr.paycheck_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.company_fk = p_company_seq AND
                /* :companySeq */
                pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, ptrans.employee_fk AS source_employee_fk, SUM(ptransline.amount *
                CASE pitem.payroll_item_type
                    WHEN 'Deduction' THEN - 1
                    WHEN 'Compensation' THEN 1
                    WHEN 'EmployerContribution' THEN 1
                    ELSE 1::CHARACTER VARYING
                END) AS amount
                FROM pspadm.psp_qbdt_payroll_trans_line AS ptransline
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                JOIN pspadm.psp_qbdt_payroll_transaction AS ptrans
                    ON ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                JOIN pspadm.psp_payroll_item AS pitem
                    ON pitem.payroll_item_code = pcpi.payroll_item_fk
                WHERE DATE(DATE_TRUNC('QUARTER', ptrans.period_end_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                ptrans.employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND ptrans.is_voided = 0 AND ptrans.company_fk = p_company_seq AND
                /* :companySeq */
                pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, ptrans.employee_fk) AS var_sbq
            GROUP BY company_payroll_item_seq, source_employee_fk) AS src;
/* dbms_output.put_line('Finished merging PRC_COMP_QTR_PAYROLLITEM_TOT  - ' || to_char(systimestamp, 'hh24:mi:ss')); */
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_payroll_item_totals$prc_qtr_payroll_item_tot(IN p_qtr_start_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* Quarter start date in UTC */
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
/* current system UTC date and time */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp) INTO v_utc_date FROM DUAL
    */
    /* dbms_output.put_line('Merging PRC_QTR_PAYROLL_ITEM_TOT, starting merge  - Started at ' || to_char(systimestamp, 'hh24:mi:ss')); */
    /* Deleting records for voided payrolls */
    DELETE FROM pspadm.psp_ee_payrollitem_qtrtotals AS eepayrollitemqtrtotals
        WHERE eepayrollitemqtrtotals.quarter = (SELECT
            aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'Q'))) AND eepayrollitemqtrtotals.year = (SELECT
            aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'YYYY')));
    INSERT INTO pspadm.psp_ee_payrollitem_qtrtotals (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date, modified_date, realm_id, quarter, year, amount, taxable_wages, total_wages, tips_taxable_wages_amount, company_payroll_item_fk, employee_fk)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'PK_PAYROLL_ITEM_TOTALS-INS', v_utc_date, v_utc_date, - 1, aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'Q')), aws_oracle_ext.TO_NUMBER(aws_oracle_ext.TO_CHAR(p_qtr_start_date, 'YYYY')), src.amount, 0, 0, 0, src.company_payroll_item_seq, src.source_employee_fk
        FROM (SELECT
            company_payroll_item_seq, source_employee_fk, SUM(amount) AS amount
            FROM (SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pcomp.compensation_amount) AS amount
                FROM pspadm.psp_compensation AS pcomp
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pcomp.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE DATE(DATE_TRUNC('QUARTER', pr.paycheck_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pded.deduction_amount) AS amount
                FROM pspadm.psp_deduction AS pded
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pded.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pded.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE DATE(DATE_TRUNC('QUARTER', pr.paycheck_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pec.contribution_amount) AS amount
                FROM pspadm.psp_employer_contribution AS pec
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pec.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pec.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE DATE(DATE_TRUNC('QUARTER', pr.paycheck_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, ptrans.employee_fk AS source_employee_fk, SUM(ptransline.amount *
                CASE pitem.payroll_item_type
                    WHEN 'Deduction' THEN - 1
                    WHEN 'Compensation' THEN 1
                    WHEN 'EmployerContribution' THEN 1
                    ELSE 1::CHARACTER VARYING
                END) AS amount
                FROM pspadm.psp_qbdt_payroll_trans_line AS ptransline
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                JOIN pspadm.psp_qbdt_payroll_transaction AS ptrans
                    ON ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                JOIN pspadm.psp_payroll_item AS pitem
                    ON pitem.payroll_item_code = pcpi.payroll_item_fk
                WHERE DATE(DATE_TRUNC('QUARTER', ptrans.period_end_date)) = DATE(p_qtr_start_date) AND
                /* :qtrStartDate */
                ptrans.employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND ptrans.is_voided = 0 AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, ptrans.employee_fk) AS var_sbq
            GROUP BY company_payroll_item_seq, source_employee_fk) AS src;
/* dbms_output.put_line('Finished merging PRC_QTR_PAYROLL_ITEM_TOT  - ' || to_char(systimestamp, 'hh24:mi:ss')); */
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.pk_payroll_item_totals$prc_year_payroll_item_tot(IN p_year TEXT)
AS 
$BODY$
/* Quarter start date in UTC */
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
/* current system UTC date and time */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp) INTO v_utc_date FROM DUAL
    */
    /* dbms_output.put_line('Merging PRC_YEAR_PAYROLL_ITEM_TOT, starting merge  - Started at ' || to_char(systimestamp, 'hh24:mi:ss')); */
    /* Deleting records for voided payrolls */
    DELETE FROM pspadm.psp_ee_payrollitem_qtrtotals AS eepayrollitemqtrtotals
        WHERE eepayrollitemqtrtotals.year = (SELECT
            aws_oracle_ext.TO_NUMBER(p_year));
    INSERT INTO pspadm.psp_ee_payrollitem_qtrtotals (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date, modified_date, realm_id, quarter, year, amount, taxable_wages, total_wages, tips_taxable_wages_amount, company_payroll_item_fk, employee_fk)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'PK_PAYROLL_ITEM_TOTALS-INS', v_utc_date, v_utc_date, - 1, aws_oracle_ext.TO_NUMBER(src.qtr), aws_oracle_ext.TO_NUMBER(p_year), src.amount, 0, 0, 0, src.company_payroll_item_seq, src.source_employee_fk
        FROM (SELECT
            company_payroll_item_seq, source_employee_fk, SUM(amount) AS amount, qtr
            FROM (SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pcomp.compensation_amount) AS amount, aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'Q') AS qtr
                FROM pspadm.psp_compensation AS pcomp
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pcomp.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'YYYY') = p_year AND
                /* :year */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk, aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'Q')
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pded.deduction_amount) AS amount, aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'Q') AS qtr
                FROM pspadm.psp_deduction AS pded
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pded.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pded.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'YYYY') = p_year AND
                /* :year */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pr.payroll_run_status != 'Superseded' AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk, aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'Q')
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, pcheck.source_employee_fk AS source_employee_fk, SUM(pec.contribution_amount) AS amount, aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'Q') AS qtr
                FROM pspadm.psp_employer_contribution AS pec
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = pec.company_payroll_item_fk
                JOIN pspadm.psp_paycheck AS pcheck
                    ON pec.paycheck_fk = pcheck.paycheck_seq
                JOIN pspadm.psp_payroll_run AS pr
                    ON pr.payroll_run_seq = pcheck.payroll_run_fk
                WHERE aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'YYYY') = p_year AND
                /* :year */
                pcheck.source_employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND pcheck.status = 'Active' AND pcheck.source_paycheck_id NOT LIKE '-%' AND pr.payroll_run_status != 'Superseded' AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, pcheck.source_employee_fk, aws_oracle_ext.TO_CHAR(pr.paycheck_date, 'Q')
            UNION ALL
            SELECT
                pcpi.company_payroll_item_seq AS company_payroll_item_seq, ptrans.employee_fk AS source_employee_fk, SUM(ptransline.amount *
                CASE pitem.payroll_item_type
                    WHEN 'Deduction' THEN - 1
                    WHEN 'Compensation' THEN 1
                    WHEN 'EmployerContribution' THEN 1
                    ELSE 1::CHARACTER VARYING
                END) AS amount, aws_oracle_ext.TO_CHAR(ptrans.period_end_date, 'Q') AS qtr
                FROM pspadm.psp_qbdt_payroll_trans_line AS ptransline
                JOIN pspadm.psp_company_payroll_item AS pcpi
                    ON pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                JOIN pspadm.psp_qbdt_payroll_transaction AS ptrans
                    ON ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                JOIN pspadm.psp_payroll_item AS pitem
                    ON pitem.payroll_item_code = pcpi.payroll_item_fk
                WHERE aws_oracle_ext.TO_CHAR(ptrans.period_end_date, 'YYYY') = p_year AND
                /* :year */
                ptrans.employee_fk IS NOT NULL AND pcpi.is_archived = 0 AND ptrans.is_voided = 0 AND pcpi.tax_form_line IN ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION', 'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B', 'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B', 'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS', 'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5', 'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19', 'TTT22', 'TTT23', 'TTT24', 'TTT25', 'TTT26', 'TTT27', 'TTT28', 'TTT29')
                /* (:taxFormLines) */
                GROUP BY pcpi.company_payroll_item_seq, ptrans.employee_fk, aws_oracle_ext.TO_CHAR(ptrans.period_end_date, 'Q')) AS var_sbq
            GROUP BY company_payroll_item_seq, source_employee_fk, qtr) AS src;
/* dbms_output.put_line('Finished merging PRC_YEAR_PAYROLL_ITEM_TOT  - ' || to_char(systimestamp, 'hh24:mi:ss')); */
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_achtransactionprocessor(IN p_source_system_cd TEXT, IN p_user_id TEXT, IN p_processing_date TIMESTAMP WITHOUT TIME ZONE, IN p_application_server_date TIMESTAMP WITHOUT TIME ZONE, INOUT p_number_of_records NUMERIC)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
DECLARE
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp)
          INTO v_psp_date
          FROM DUAL
    */
    /* PSRV001109: Changed the way we're selecting financial transctions for state change. */
    /* Previously: We were selecting by ft.settlement_date <= p_processing_date, which was causing full table/partition scans that took hours to complete. */
    /* Currently:  We are now selecting by [ft.settlement_date between (p_processing_date - 45) and p_processing_date], i.e a 45 day look-back window. */
    /* KP */
    /* Create Completed Transaction States for all transactions in the Offload Batch */
    INSERT INTO pspadm.psp_financial_trans_state (financial_trans_state_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, transaction_state_eff_date, insert_user_id, gems_upload_batch_fk, financial_transaction_fk, transaction_state_fk, transaction_response_fk, company_fk, transaction_type_fk)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 0, p_user_id, p_application_server_date, p_user_id, p_application_server_date, - 1, DATE(v_psp_date) + (.5::NUMERIC || ' days')::INTERVAL, NULL, NULL, ft.financial_transaction_seq, 'Completed', NULL, ft.company_fk, ft.transaction_type_fk
        FROM pspadm.psp_financial_transaction AS ft
        INNER JOIN pspadm.psp_company AS c
            ON c.company_seq = ft.company_fk
        WHERE c.source_system_cd = p_source_system_cd AND ft.current_transaction_state_fk = 'Executed' AND ft.settlement_type_cd IN ('ACH', 'ApplyForward', 'EFTPSDirectDebit', 'EDI') AND ft.settlement_date BETWEEN (p_processing_date - (45::NUMERIC || ' days')::INTERVAL) AND p_processing_date AND ft.transaction_type_fk IN ('EmployeeDdCredit', 'ServiceSalesAndUseTax', 'EmployerTaxReturnedCredit', 'EmployerTaxRefundCredit', 'EmployerTaxReturnedRefundCredit', 'EmployerTaxCreditReturnedTransfer', 'EmployerTaxCreditApplied', 'EmployerTaxOverpaymentApplied', 'AgencyTaxOverpayment', 'EmployerTaxDirectDebit', 'EmployerTaxDirectOverpaymentApplied', 'AgencyDirectOverpayment', 'AgencyDirectDebit', 'AgencyDirectCredit', 'AgencyTaxRecredit', 'AgencyTaxRedebit', 'AgencyTaxCredit', 'AgencyTaxDebit', 'AgencyTaxOverpaymentApplied');
    /* Set the status of Financial Transactions to Completed for all transactions in the Offload Batch */
    UPDATE pspadm.psp_financial_transaction AS ft
    SET current_transaction_state_fk = 'Completed', version = version + 1, modifier_id = p_user_id, modified_date = p_application_server_date
        WHERE ft.current_transaction_state_fk = 'Executed' AND ft.settlement_type_cd IN ('ACH', 'ApplyForward', 'EFTPSDirectDebit', 'EDI') AND ft.settlement_date BETWEEN (p_processing_date - (45::NUMERIC || ' days')::INTERVAL) AND p_processing_date AND ft.transaction_type_fk IN ('EmployeeDdCredit', 'ServiceSalesAndUseTax', 'EmployerTaxReturnedCredit', 'EmployerTaxRefundCredit', 'EmployerTaxReturnedRefundCredit', 'EmployerTaxCreditReturnedTransfer', 'EmployerTaxCreditApplied', 'EmployerTaxOverpaymentApplied', 'AgencyTaxOverpayment', 'EmployerTaxDirectDebit', 'EmployerTaxDirectOverpaymentApplied', 'AgencyDirectOverpayment', 'AgencyDirectDebit', 'AgencyDirectCredit', 'AgencyTaxRecredit', 'AgencyTaxRedebit', 'AgencyTaxCredit', 'AgencyTaxDebit', 'AgencyTaxOverpaymentApplied') AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_company AS c
            WHERE c.company_seq = ft.company_fk AND c.source_system_cd = p_source_system_cd);
    GET DIAGNOSTICS p_number_of_records = ROW_COUNT;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_calculate_ledger_balance(IN p_ledger_update_date TIMESTAMP WITHOUT TIME ZONE DEFAULT aws_oracle_ext.SYSDATE() - (1::NUMERIC || ' days')::INTERVAL)
AS 
$BODY$
/*
*****************************************************************************
   UPDATED: 10.03.2008
   PURPOSE: Updates ledger balance for the passed date.
            If the date is null, it will use sysdate -1
   LOGIC: 1. Insert previous ledger balance for the company (with max date).
             insert only ledger account for companies that were updated. By
             updated means we have record in "financial_transaction_state"
          2. Update all the balances for the updated txns (via merge)
          3. For new ledger account for company, Insert data (via merge)

   ASSUMPTIONS:
          1. PSP_FINANCIAL_TRANS_STATE.transaction_state_eff_date is stored as UTC. However, it is stored in a way
             that trunc(transaction_state_eff_date) = trunc(transaction_state_eff_date as PDT)
             In other words, the time component is stored in a way that the UTC date is always the same as the PDT date
             (for instance, 01:00:00 PM)
          2. PSP_LEDGER_BALANCE.balance_date has the same characteristics (but this sproc is the one that guarantees
             that)


    TODO:
        1. Performance test in LT
        2. Functional test


   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        10.03.2008  Tushar           Created
   1.1        10.11.2008  Allen            Added UTC/PDT logic to dates
   1.2        06.02.2010  Allen            Changed the insert
   1.3        02.14.2012  Zack             Updated for performance improvement
   1.4        10.15.2012  Anand            Updated for reporting types
*****************************************************************************
*/
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* the time-corrected ledger_update_date is used throughout the code instead of p_ledger_update_date */
    /* current system UTC date and time */
    v_ledger_update_date TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL
    */
    /* p_ledger_update_date is PDT, but must also have the time component in a way */
    /* that truncate(p_ledger_update_date) = truncate(p_ledger_update_date as UTC) */
    SELECT
        aws_oracle_ext.TRUNC(p_ledger_update_date) + (.5::NUMERIC || ' days')::INTERVAL
        INTO STRICT v_ledger_update_date;
    /* this sets the time to 12:00:00 PM */
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Merging PSP_LEDGER_BALANCE, starting merge  - ', aws_oracle_ext.TO_CHAR(v_ledger_update_date, 'yyyy-mm-dd'), ' - Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    INSERT INTO pspadm.psp_ledger_balance (tgt.ledger_balance_seq, version, creator_id, created_date, modified_date, realm_id, balance_amount, balance_date, ledger_account_fk, company_fk, reporting_type)
    SELECT
        fts.company_fk, DATE(fts.transaction_state_eff_date) AS newbal_date, SUM((SELECT
            ft.financial_transaction_amount
            FROM pspadm.psp_financial_transaction AS ft
            WHERE ft.financial_transaction_seq = fts.financial_transaction_fk) *
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
        END) AS amount, pr.ledger_account_fk, po.reporting_type
        FROM pspadm.psp_financial_trans_state AS fts, pspadm.psp_posting_rule AS pr, pspadm.psp_ledger_account AS la, pspadm.psp_company AS pc, pspadm.psp_company_offering AS pco, pspadm.psp_offering AS po
        WHERE pr.transaction_state_fk = fts.transaction_state_fk AND pr.transaction_type_fk = fts.transaction_type_fk AND la.ledger_account_cd = pr.ledger_account_fk AND pc.company_seq = fts.company_fk AND pc.company_seq = pco.company_fk AND pco.offering_fk = po.offering_seq AND
        /* AND PO.REPORTING_TYPE IN ('DirectDeposit','Tax') */
        po.service_code = 'DirectDeposit' AND DATE(fts.transaction_state_eff_date) = DATE(v_ledger_update_date) AND fts.transaction_state_eff_date BETWEEN v_ledger_update_date - (1::NUMERIC || ' days')::INTERVAL AND v_ledger_update_date + (1::NUMERIC || ' days')::INTERVAL
        GROUP BY fts.company_fk, DATE(fts.transaction_state_eff_date), pr.ledger_account_fk, po.reporting_type
    ON CONFLICT (ledger_account_fk, company_fk, reporting_type) DO UPDATE SET balance_amount = tgt.balance_amount + amount, excluded.modified_date = excluded.v_utc_date, excluded.version = excluded.version + 1, excluded.modifier_id = 'LEDGERBALANCEBATCHJOBMERGEUPD';
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished merging PSP_LEDGER_BALANCE  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    COMMIT;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_calculate_w2_totals(IN p_processing_year NUMERIC, IN p_company_id TEXT)
AS 
$BODY$
/*
*****************************************************************************
   CREATED: 11/8/2012
   PURPOSE: Calculate W2 Employee Totals for a given year
            If the date is null, it will use sysdate -1 year

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        11.08.2012  Marcela Villani  Created
*****************************************************************************
*/
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
/* current system UTC date and time */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL
    */
    IF p_company_id IS NULL THEN
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Calculating Law Annual totals - Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        /* Delete and insert PSP_COMPANY_TFS_SUBMISSION */
        DELETE FROM pspadm.psp_company_tfssubmission
            WHERE year = p_processing_year;
        INSERT INTO pspadm.psp_company_tfssubmission (company_tfssubmission_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, submission_status, status_effective_date, company_fk, year)
        SELECT
            pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date, 'W2ANNUALCALCULATIONINS', v_utc_date, - 1, 'Pending', v_utc_date, company_fk, p_processing_year
            FROM (SELECT DISTINCT
                company_fk
                FROM (SELECT DISTINCT
                    company_fk AS company_fk
                    FROM pspadm.psp_employee_law_qtr_totals
                    WHERE year = p_processing_year
                UNION
                SELECT DISTINCT
                    company_fk AS company_fk
                    FROM pspadm.psp_ee_payrollitem_qtrtotals AS pit
                    INNER JOIN pspadm.psp_company_payroll_item AS cpi
                        ON pit.company_payroll_item_fk = cpi.company_payroll_item_seq
                    WHERE pit.year = p_processing_year) AS var_sbq) AS var_sbq_2;
        INSERT INTO pspadm.psp_employee_w2_totals (tgt.employee_w2_totals_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, year, taxable_wages, amount, tips_taxable_wages_amount, total_wages, company_payroll_item_fk, employee_fk, law_fk, company_fk, company_law_fk)
        SELECT
            company_fk, employee_fk, company_law_fk, law_fk, SUM(taxable_wages) AS taxable_wages, SUM(tax_amount) AS amount, SUM(tips_taxable_wages_amount) AS tips_taxable_wages_amount, SUM(total_wages) AS total_wages
            FROM pspadm.psp_employee_law_qtr_totals
            WHERE year = p_processing_year
            GROUP BY company_fk, employee_fk, company_law_fk, law_fk
        ON CONFLICT (year, company_law_fk, company_fk, employee_fk) DO UPDATE SET amount = excluded.amount, taxable_wages = excluded.taxable_wages, tips_taxable_wages_amount = excluded.tips_taxable_wages_amount, total_wages = excluded.total_wages, excluded.modified_date = excluded.v_utc_date, excluded.version = excluded.version + 1, excluded.modifier_id = 'W2ANNUALCALCULATIONUPD';
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished Calculating Law Annual totals  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Calculating Payroll Item Annual totals - Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        INSERT INTO pspadm.psp_employee_w2_totals (tgt.employee_w2_totals_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, year, taxable_wages, amount, tips_taxable_wages_amount, total_wages, company_payroll_item_fk, employee_fk, law_fk, company_fk, company_law_fk)
        SELECT
            cpi.company_fk AS company_fk, employee_fk, company_payroll_item_fk, SUM(taxable_wages) AS taxable_wages, SUM(amount) AS amount, SUM(tips_taxable_wages_amount) AS tips_taxable_wages_amount, SUM(total_wages) AS total_wages
            FROM pspadm.psp_ee_payrollitem_qtrtotals AS pit
            INNER JOIN pspadm.psp_company_payroll_item AS cpi
                ON pit.company_payroll_item_fk = cpi.company_payroll_item_seq
            WHERE year = p_processing_year
            GROUP BY cpi.company_fk, employee_fk, company_payroll_item_fk
        ON CONFLICT (year, company_payroll_item_fk, company_fk, employee_fk) DO UPDATE SET amount = excluded.amount, taxable_wages = excluded.taxable_wages, tips_taxable_wages_amount = excluded.tips_taxable_wages_amount, total_wages = excluded.total_wages, excluded.modified_date = excluded.v_utc_date, excluded.version = excluded.version + 1, excluded.modifier_id = 'W2ANNUALCALCULATIONUPD';
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished Calculating Payroll Item Annual totals  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        COMMIT;
    ELSE
        /* Calculate for one company */
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Calculating Law Annual totals - Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        /* Delete and insert PSP_COMPANY_TFS_SUBMISSION */
        DELETE FROM pspadm.psp_company_tfssubmission
            WHERE year = p_processing_year AND company_fk = p_company_id;
        INSERT INTO pspadm.psp_company_tfssubmission (company_tfssubmission_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, submission_status, status_effective_date, company_fk, year)
        VALUES (pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date, 'W2ANNUALCALCULATIONINS', v_utc_date, - 1, 'Pending', v_utc_date, p_company_id, p_processing_year);
        INSERT INTO pspadm.psp_employee_w2_totals (tgt.employee_w2_totals_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, year, taxable_wages, amount, tips_taxable_wages_amount, total_wages, company_payroll_item_fk, employee_fk, law_fk, company_fk, company_law_fk)
        SELECT
            company_fk, employee_fk, company_law_fk, law_fk, SUM(taxable_wages) AS taxable_wages, SUM(tax_amount) AS amount, SUM(tips_taxable_wages_amount) AS tips_taxable_wages_amount, SUM(total_wages) AS total_wages
            FROM pspadm.psp_employee_law_qtr_totals
            WHERE year = p_processing_year AND company_fk = p_company_id
            GROUP BY company_fk, employee_fk, company_law_fk, law_fk
        ON CONFLICT (year, company_law_fk, company_fk, employee_fk) DO UPDATE SET amount = excluded.amount, taxable_wages = excluded.taxable_wages, tips_taxable_wages_amount = excluded.tips_taxable_wages_amount, total_wages = excluded.total_wages, excluded.modified_date = excluded.v_utc_date, excluded.version = excluded.version + 1, excluded.modifier_id = 'W2ANNUALCALCULATIONUPD';
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished Calculating Law Annual totals  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Calculating Payroll Item Annual totals - Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
        INSERT INTO pspadm.psp_employee_w2_totals (tgt.employee_w2_totals_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, year, taxable_wages, amount, tips_taxable_wages_amount, total_wages, company_payroll_item_fk, employee_fk, law_fk, company_fk, company_law_fk)
        SELECT
            cpi.company_fk AS company_fk, employee_fk, company_payroll_item_fk, SUM(taxable_wages) AS taxable_wages, SUM(amount) AS amount, SUM(tips_taxable_wages_amount) AS tips_taxable_wages_amount, SUM(total_wages) AS total_wages
            FROM pspadm.psp_ee_payrollitem_qtrtotals AS pit
            INNER JOIN pspadm.psp_company_payroll_item AS cpi
                ON pit.company_payroll_item_fk = cpi.company_payroll_item_seq
            WHERE year = p_processing_year AND company_fk = p_company_id
            GROUP BY cpi.company_fk, employee_fk, company_payroll_item_fk
        ON CONFLICT (year, company_payroll_item_fk, company_fk, employee_fk) DO UPDATE SET amount = excluded.amount, taxable_wages = excluded.taxable_wages, tips_taxable_wages_amount = excluded.tips_taxable_wages_amount, total_wages = excluded.total_wages, excluded.modified_date = excluded.v_utc_date, excluded.version = excluded.version + 1, excluded.modifier_id = 'W2ANNUALCALCULATIONUPD';
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished Calculating Payroll Item Annual totals  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    END IF;
    COMMIT;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_eftps_payments_events(IN p_calling_procedure TEXT, IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_payment_file_seq TEXT, IN p_payment_initiation_date TIMESTAMP WITHOUT TIME ZONE, IN p_tax_payment_status_1 TEXT, IN p_tax_payment_status_2 TEXT DEFAULT NULL)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* primary key of payment file */
DECLARE
    v_sql_stmt CHARACTER VARYING(32767);
    /* unused */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
/* error desc variable for logging */
BEGIN
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'company event started - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName           IN   VARCHAR2,
                   p_calling_procedure,             -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name           IN   VARCHAR2,
                   'PSP_COMPANY_EVENT',             --  p_ObjectName         IN   VARCHAR2,
                   'N/A',                           --p_UserName           IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Inserting COMPANY_EVENT')
    */
    INSERT INTO pspadm.psp_company_event (company_event_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, event_time_stamp, status_effective_date, status_cd, event_type_cd, event_token, source_id, note_last_updated_date, company_fk)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, - 1, p_app_server_date, p_app_server_date, 'Active', 'TaxPaymentStatusChanged', 0, mmt.money_movement_transaction_seq, NULL, mmt.company_fk
        FROM pspadm.psp_eftps_payment_detail AS efpd
        JOIN pspadm.psp_money_movement_transaction AS mmt
            ON mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
        WHERE efpd.parent_file_fk = p_payment_file_seq AND mmt.initiation_date = p_payment_initiation_date AND mmt.status = 'Executed' AND mmt.tax_payment_status IN (p_tax_payment_status_1, p_tax_payment_status_2);
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   p_calling_procedure,             -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_COMPANY_EVENT_DETAIL',      --  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Inserting COMPANY_EVENT_DETAIL')
    */
    /*
    [9996 - Severity CRITICAL - Transformer error occurred. Please submit report to developers.]
    v_sql_stmt:='INSERT /*+ APPEND */ ALL
        INTO psp_company_event_detail (COMPANY_EVENT_DETAIL_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
             VALUE, EVENT_DETAIL_TYPE_CD, EVENT_DETAIL_SUBTYPE, COMPANY_EVENT_FK, COMPANY_FK)
        VALUES (FN_FORMAT_SYSGUID(sys_guid()), 0, :b1, :b2, :b3, :b4, -1,
            money_movement_transaction_seq, ''MoneyMovementTransactionId'', null, company_event_seq, company_fk)
        INTO psp_company_event_detail (COMPANY_EVENT_DETAIL_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
             VALUE, EVENT_DETAIL_TYPE_CD, EVENT_DETAIL_SUBTYPE, COMPANY_EVENT_FK, COMPANY_FK)
        VALUES (FN_FORMAT_SYSGUID(sys_guid()), 0, :b5, :b6, :b7, :b8, -1,
            ''The tax payment status for '' || payment_template_fk || '' due on '' || to_char(payment_due_date, ''MM/DD/YYYY'') || '' via '' || money_movement_payment_method || '' has changed to '' || status_cd, ''GenericEventDetail'', null, company_event_seq, company_fk)
        SELECT
            mmt.money_movement_transaction_seq, mmt.payment_template_fk, mmt.money_movement_payment_method, efpd.status_cd, efpd.payment_due_date, ce.company_event_seq, mmt.company_fk
        FROM
            psp_eftps_payment_detail efpd
            JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
            JOIN psp_company_event ce on ce.company_fk = mmt.company_fk
        WHERE
            efpd.parent_file_fk = :b9
            AND mmt.initiation_date = :b10
            AND mmt.status = ''Executed''
            AND ce.event_type_cd = ''TaxPaymentStatusChanged''
            AND ce.source_id = mmt.money_movement_transaction_seq
            AND ce.event_time_stamp >= :b11
            AND ce.created_date = :b12'
    */
    EXECUTE v_sql_stmt USING p_user_id, p_app_server_date, p_user_id, p_app_server_date, p_user_id, p_app_server_date, p_user_id, p_app_server_date, p_payment_file_seq, p_payment_initiation_date, p_app_server_date, p_app_server_date;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'company event inserts finished ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_eftps_payments_mmt_status(IN p_calling_procedure TEXT, IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_payment_file_seq TEXT, IN p_payment_initiation_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* primary key of payment file */
/* unused */
DECLARE
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
/* error desc variable for logging */
BEGIN
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'update mmt status started  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   p_calling_procedure,             -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_MONEY_MOVEMENT_TRANSACTION',--  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Updating MONEY_MOVEMENT_TRANSACTION')
    */
    /* For each MMT we are going to update the tax_payment_status on below, */
    /* recalculate the ATF payments passing in the current and new payment status. */
    CALL pspadm.prc_recalc_atf_payments_eftps(p_calling_procedure, p_user_id, p_app_server_date, p_payment_file_seq, p_payment_initiation_date);
    /* update MMT TaxPaymentStatus to mirror EftpsPaymentDetail status updated by batch job, set status effective date */
    
    /*
    [5608 - Severity CRITICAL - Unable to convert the UPDATE statement with multiple-column subquery in SET clause. Perform a manual conversion.]
    UPDATE psp_money_movement_transaction mmt
      SET (mmt.modifier_id, mmt.modified_date, mmt.tax_payment_status, mmt.tax_pmtstatus_effectivedate) = (
        SELECT
          p_user_id, p_app_server_date, efpd.status_cd, p_app_server_date
      	FROM
          psp_eftps_payment_detail efpd
      	WHERE
          efpd.parent_file_fk = p_payment_file_seq
          AND efpd.money_movement_transaction_fk = mmt.money_movement_transaction_seq
          AND mmt.initiation_date = p_payment_initiation_date
       ), mmt.version = mmt.version + 1
      WHERE
        mmt.initiation_date = p_payment_initiation_date
        AND mmt.money_movement_transaction_seq in (
          SELECT
            money_movement_transaction_fk
          FROM
            psp_eftps_payment_detail efpd
          WHERE
            efpd.parent_file_fk = p_payment_file_seq
        )
    */
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'update mmt status finished  - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_eftps_payments_response(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_response_file_id DOUBLE PRECISION, IN p_complete_fin_txns DOUBLE PRECISION DEFAULT 0)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* aka file control number */
DECLARE
    v_payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE;
    v_payment_file_seq CHARACTER VARYING(36);
    /* unused */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
    aws$frmt_err_bcktrc CHARACTER VARYING(2000);
    aws$frmt_err_num CHARACTER VARYING(8);
    aws$frmt_err_stck CHARACTER VARYING(2000);
/* error desc variable for logging */
BEGIN
    /* find the payment file key to simplify payment detail join */
    /* find the initiation date to force all MMT searches onto correct partition */
    BEGIN
        SELECT
            mmt.initiation_date, ef_payment.eftps_file_seq
            INTO STRICT v_payment_initiation_date, v_payment_file_seq
            FROM pspadm.psp_eftps_file AS ef_response
            JOIN pspadm.psp_eftps_payment_detail AS efpd
                ON efpd.response_file_fk = ef_response.eftps_file_seq
            JOIN pspadm.psp_eftps_file AS ef_payment
                ON ef_payment.eftps_file_seq = efpd.parent_file_fk
            JOIN pspadm.psp_money_movement_transaction AS mmt
                ON mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
            JOIN pspadm.psp_edi_tax_file AS etf
                ON etf.edi_tax_file_seq = ef_response.eftps_file_seq
            WHERE etf.file_id = p_response_file_id
            LIMIT 1;
        EXCEPTION
            WHEN too_many_rows THEN
                RAISE USING hint = -20052, message = CONCAT_WS('', 'ERROR: MULTIPLE RESPONSE FILES FOUND FOR FILE_ID: ', p_response_file_id), detail = 'User-defined exception';
            WHEN no_data_found THEN
                /* AS400 files */
                RETURN;
            WHEN others THEN
                /* Output desired error message */
                RAISE DEBUG USING MESSAGE = '-20999: unexpected error -- error stack follows';
                /* Output actual line number of error source */
                GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
                RAISE DEBUG USING MESSAGE = aws$frmt_err_bcktrc::TEXT;
                /* Output the actual error number and message */
                GET STACKED DIAGNOSTICS aws$frmt_err_num = RETURNED_SQLSTATE,
                    aws$frmt_err_stck = MESSAGE_TEXT;
                aws$frmt_err_stck := CONCAT(aws$frmt_err_num, ': ', aws$frmt_err_stck);
                RAISE DEBUG USING MESSAGE = aws$frmt_err_stck::TEXT;
    END;
    CALL pspadm.prc_eftps_payments_mmt_status('PRC_EFTPS_PAYMENTS_RESPONSE'::TEXT, p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date);
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           -- p_ArchName           IN   VARCHAR2,
                   'PRC_EFTPS_PAYMENTS_RESPONSE',   -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_FINANCIAL_TRANS_STATE',     -- p_ObjectName         IN   VARCHAR2,
                   'N/A',                           -- p_UserName           IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Inserting FINANCIAL_TRANS_STATE')
    */
    /* update FTS */
    
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.NEW_TIME(DATE,VARCHAR2,VARCHAR2) function. Use suitable function or create user defined function.]
    INSERT /*+ APPEND */ INTO PSP_FINANCIAL_TRANS_STATE
       (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
         REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
         GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
         TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
       (SELECT
          FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
          new_time(p_app_server_date,'GMT','PDT'), NULL, NULL, ft.financial_transaction_seq, DECODE(efpd.status_cd, 'AcknowledgedByAgency', 'Completed', 'RejectedByAgency', 'Returned'), NULL, ft.company_fk, ft.transaction_type_fk
       FROM
          psp_eftps_payment_detail efpd
      	  JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
          JOIN psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
       WHERE
          efpd.parent_file_fk = v_payment_file_seq
          AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
          AND mmt.status = 'Executed'
          AND (
            (mmt.money_movement_payment_method = 'EFTPS' AND mmt.tax_payment_status in (DECODE(p_complete_fin_txns, 1, 'AcknowledgedByAgency', '<same-day>'), 'RejectedByAgency'))
            OR (mmt.money_movement_payment_method = 'EFTPSDirectDebit' AND mmt.tax_payment_status = 'RejectedByAgency')
          )
          AND mmt.initiation_date = v_payment_initiation_date
          AND ft.current_transaction_state_fk = 'Executed'
          AND trunc(ft.settlement_date) >= trunc(mmt.initiation_date)
          AND ft.settlement_date >= mmt.initiation_date
       )
    */
    
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           -- p_ArchName           IN   VARCHAR2,
                   'PRC_EFTPS_PAYMENTS_RESPONSE',   -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_FINANCIAL_TRANSACTION',     -- p_ObjectName         IN   VARCHAR2,
                   'N/A',                           -- p_UserName           IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Updating FINANCIAL_TRANSACTION')
    */
    
    /*
    [5608 - Severity CRITICAL - Unable to convert the UPDATE statement with multiple-column subquery in SET clause. Perform a manual conversion.]
    UPDATE psp_financial_transaction ft
      SET (current_transaction_state_fk, modifier_id, modified_date) = (
        SELECT
          DECODE(efpd.status_cd, 'AcknowledgedByAgency', 'Completed', 'RejectedByAgency', 'Returned'), p_user_id, p_app_server_date
        FROM
          psp_eftps_payment_detail efpd
      	  JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
        WHERE
          efpd.parent_file_fk = v_payment_file_seq
          AND ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
          AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
          AND mmt.money_movement_payment_method in ('EFTPS','EFTPSDirectDebit')
          AND mmt.status = 'Executed'
          AND mmt.tax_payment_status in ('AcknowledgedByAgency', 'RejectedByAgency')
          AND mmt.initiation_date = v_payment_initiation_date
        ), ft.version = ft.version + 1
      WHERE
        trunc(settlement_date) >= trunc(v_payment_initiation_date)
        AND settlement_date >= trunc(v_payment_initiation_date)
        AND current_transaction_state_fk = 'Executed'
        AND money_movement_transaction_fk in (
          SELECT
            money_movement_transaction_fk
          FROM
            psp_eftps_payment_detail efpd
            JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
          WHERE
            efpd.parent_file_fk = v_payment_file_seq
            AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
            AND mmt.status = 'Executed'
            AND (
              (mmt.tax_payment_status in (DECODE(p_complete_fin_txns, 1, 'AcknowledgedByAgency', '<same-day>'), 'RejectedByAgency') AND mmt.money_movement_payment_method = 'EFTPS')
              OR (efpd.status_cd = 'RejectedByAgency' AND mmt.money_movement_payment_method = 'EFTPSDirectDebit')
            )
            AND mmt.initiation_date = v_payment_initiation_date
        )
    */
    /* company events */
    CALL pspadm.prc_eftps_payments_events('PRC_EFTPS_PAYMENTS_RESPONSE'::TEXT, p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, 'AcknowledgedByAgency'::TEXT, 'RejectedByAgency'::TEXT);
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_eftps_payments_return(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_return_file_id DOUBLE PRECISION)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* aka file control number */
DECLARE
    v_payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE;
    v_payment_file_seq CHARACTER VARYING(36);
    /* unused */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
    /* error desc variable for logging */
    payment_file_cursor CURSOR FOR
    /* find the payment file key to simplify payment detail join */
    SELECT DISTINCT
        parent_file_fk
        FROM pspadm.psp_eftps_payment_detail
        WHERE return_file_fk = (SELECT
            edi_tax_file_seq
            FROM pspadm.psp_edi_tax_file AS ef
            WHERE ef.file_id = p_return_file_id);
    payment_file_cursor$ATTRIBUTES aws_oracle_data.TCursorAttributes := ROW (FALSE, NULL, NULL, NULL);
    aws$frmt_err_bcktrc CHARACTER VARYING(2000);
    aws$frmt_err_num CHARACTER VARYING(8);
    aws$frmt_err_stck CHARACTER VARYING(2000);
BEGIN
    FOR payment_detail_rec IN payment_file_cursor LOOP
        v_payment_file_seq := payment_detail_rec.parent_file_fk;

        BEGIN
            /* find the initiation date to force all MMT searches onto correct partition */
            SELECT
                initiation_date
                INTO STRICT v_payment_initiation_date
                FROM pspadm.psp_eftps_payment_detail AS epd
                JOIN pspadm.psp_money_movement_transaction AS mmt
                    ON mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
                WHERE epd.parent_file_fk = v_payment_file_seq
                LIMIT 1;
            EXCEPTION
                WHEN no_data_found THEN
                    /* as400 files */
                    RAISE DEBUG USING MESSAGE = CONCAT_WS('', '-20999: no data found when looking up initiation date for payment file: ', v_payment_file_seq, ' -- ');
                    GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
                    RAISE DEBUG USING MESSAGE = aws$frmt_err_bcktrc::TEXT;
                    GET STACKED DIAGNOSTICS aws$frmt_err_num = RETURNED_SQLSTATE,
                        aws$frmt_err_stck = MESSAGE_TEXT;
                    aws$frmt_err_stck := CONCAT(aws$frmt_err_num, ': ', aws$frmt_err_stck);
                    RAISE DEBUG USING MESSAGE = aws$frmt_err_stck::TEXT;
                    RAISE;
                WHEN others THEN
                    RAISE DEBUG USING MESSAGE = CONCAT_WS('', '-20999: unexpected error looking up initiation date for payment file: ', v_payment_file_seq, ' -- ');
                    GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
                    RAISE DEBUG USING MESSAGE = aws$frmt_err_bcktrc::TEXT;
                    GET STACKED DIAGNOSTICS aws$frmt_err_num = RETURNED_SQLSTATE,
                        aws$frmt_err_stck = MESSAGE_TEXT;
                    aws$frmt_err_stck := CONCAT(aws$frmt_err_num, ': ', aws$frmt_err_stck);
                    RAISE DEBUG USING MESSAGE = aws$frmt_err_stck::TEXT;
                    RAISE;
        END;
        CALL pspadm.prc_eftps_payments_mmt_status('PRC_EFTPS_PAYMENTS_RETURN'::TEXT, p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date);
        /*
        [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
        PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                   null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   'PRC_EFTPS_PAYMENTS_RETURN',     -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_FINANCIAL_TRANS_STATE',     --  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                   'Insert FINANCIAL_TRANS_STATE')
        */
        /* update FTs */
        
        /*
        [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.NEW_TIME(DATE,VARCHAR2,VARCHAR2) function. Use suitable function or create user defined function.]
        INSERT /*+ APPEND */ INTO PSP_FINANCIAL_TRANS_STATE
                 (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
                     CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                     REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
                     GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
                     TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
                 (SELECT
                        FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                        new_time(p_app_server_date,'GMT','PDT'), NULL, NULL, ft.financial_transaction_seq, 'Returned', NULL, ft.company_fk, ft.transaction_type_fk
                    FROM
                        psp_eftps_payment_detail efpd
                        JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                        JOIN psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                    WHERE
                        efpd.parent_file_fk = v_payment_file_seq
                        AND efpd.status_cd in ('ReturnedTaxNotPaid')
                        AND mmt.money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
                        AND mmt.status = 'Executed'
                        AND mmt.tax_payment_status in ('ReturnedTaxNotPaid')
                        AND mmt.initiation_date = v_payment_initiation_date
                        AND ft.current_transaction_state_fk in ('Executed','Completed')
                        AND trunc(ft.settlement_date) >= trunc(mmt.initiation_date)
                        AND ft.settlement_date >= mmt.initiation_date
                )
        */
        
        /*
        [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
        PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                      null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   'PRC_EFTPS_PAYMENTS_RETURN',     -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_FINANCIAL_TRANSACTION',     --  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                   'Updating FINANCIAL_TRANSACTION')
        */
        UPDATE pspadm.psp_financial_transaction
        SET current_transaction_state_fk = 'Returned', modifier_id = p_user_id, modified_date = p_app_server_date, version = version + 1
            WHERE DATE(settlement_date) >= DATE(v_payment_initiation_date) AND settlement_date >= DATE(v_payment_initiation_date) AND current_transaction_state_fk IN ('Executed', 'Completed') AND money_movement_transaction_fk IN (SELECT
                money_movement_transaction_fk
                FROM pspadm.psp_eftps_payment_detail AS efpd
                JOIN pspadm.psp_money_movement_transaction AS mmt
                    ON mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                WHERE efpd.parent_file_fk = v_payment_file_seq AND efpd.status_cd IN ('ReturnedTaxNotPaid') AND mmt.money_movement_payment_method IN ('EFTPS', 'EFTPSDirectDebit') AND mmt.status = 'Executed' AND mmt.tax_payment_status IN ('ReturnedTaxNotPaid') AND mmt.initiation_date = v_payment_initiation_date);
        CALL pspadm.prc_eftps_payments_events('PRC_EFTPS_PAYMENTS_RETURN'::TEXT, p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, 'ReturnedTaxPaid, ReturnedTaxNotPaid'::TEXT);
    END LOOP;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_eftps_payments_sent(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_payment_file_id DOUBLE PRECISION)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* aka file control number */
DECLARE
    v_payment_file_seq CHARACTER VARYING(36);
    v_payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* unused */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
    aws$frmt_err_bcktrc CHARACTER VARYING(2000);
    aws$frmt_err_num CHARACTER VARYING(8);
    aws$frmt_err_stck CHARACTER VARYING(2000);
/* error desc variable for logging */
BEGIN
    BEGIN
        SELECT
            eftps_file_seq, payment_initiation_date
            INTO STRICT v_payment_file_seq, v_payment_initiation_date
            FROM pspadm.psp_eftps_file AS ef
            JOIN pspadm.psp_eftps_payment_detail AS epd
                ON epd.parent_file_fk = ef.eftps_file_seq
            JOIN pspadm.psp_edi_tax_file AS etf
                ON etf.edi_tax_file_seq = ef.eftps_file_seq
            WHERE file_id = p_payment_file_id
            LIMIT 1;
        EXCEPTION
            WHEN too_many_rows THEN
                RAISE USING hint = -20052, message = CONCAT_WS('', 'ERROR: MULTIPLE FILES FOUND FOR FILE_ID: ', p_payment_file_id), detail = 'User-defined exception';
            WHEN no_data_found THEN
                /* AS400 files */
                RETURN;
            WHEN others THEN
                /* Output desired error message */
                RAISE DEBUG USING MESSAGE = '-20999: unexpected error -- error stack follows';
                /* Output actual line number of error source */
                GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
                RAISE DEBUG USING MESSAGE = aws$frmt_err_bcktrc::TEXT;
                /* Output the actual error number and message */
                GET STACKED DIAGNOSTICS aws$frmt_err_num = RETURNED_SQLSTATE,
                    aws$frmt_err_stck = MESSAGE_TEXT;
                aws$frmt_err_stck := CONCAT(aws$frmt_err_num, ': ', aws$frmt_err_stck);
                RAISE DEBUG USING MESSAGE = aws$frmt_err_stck::TEXT;
    END;
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   'PRC_EFTPS_PAYMENTS_SENT',       -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_FINANCIAL_TRANSACTION',     --  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Updating FINANCIAL_TRANSACTION')
    */
    /* update current financial transaction state for all transactions in eftps */
    /* the actual current_transaction_state_fk value (Executed, Returned, etc.) should be matched with what happened in Transaction States above */
    UPDATE pspadm.psp_financial_transaction
    SET current_transaction_state_fk = 'Executed', modifier_id = p_user_id, modified_date = p_app_server_date, version = version + 1
        WHERE DATE(settlement_date) >= DATE(p_app_server_date) AND settlement_date >= p_app_server_date AND current_transaction_state_fk = 'Created' AND money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_eftps_file AS ef
            JOIN pspadm.psp_eftps_payment_detail AS efpd
                ON efpd.parent_file_fk = ef.eftps_file_seq
            JOIN pspadm.psp_money_movement_transaction AS mmt
                ON mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
            JOIN pspadm.psp_edi_tax_file AS etf
                ON etf.edi_tax_file_seq = ef.eftps_file_seq
            WHERE etf.file_id = p_payment_file_id AND mmt.status = 'Executed' AND mmt.tax_payment_status = 'SentToAgency' AND mmt.initiation_date = v_payment_initiation_date);
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'financial transaction update finished - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   'PRC_EFTPS_PAYMENTS_SENT',       -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_FINANCIAL_TRANS_STATE',     --  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Insert FINANCIAL_TRANS_STATE')
    */
    /* create Transaction States for all transactions in eftps file */
    
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.NEW_TIME(DATE,VARCHAR2,VARCHAR2) function. Use suitable function or create user defined function.]
    INSERT /*+ APPEND */ INTO PSP_FINANCIAL_TRANS_STATE
       (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
         REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
         GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
         TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
         (SELECT FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                 new_time(p_app_server_date,'GMT','PDT'), NULL, NULL, FT.FINANCIAL_TRANSACTION_SEQ, 'Executed', NULL, ft.company_fk, ft.transaction_type_fk
            FROM psp_eftps_file ef
             JOIN psp_eftps_payment_detail efpd on efpd.parent_file_fk = ef.eftps_file_seq
             JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
             JOIN psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
             JOIN PSP_EDI_TAX_FILE etf on ETF.EDI_TAX_FILE_SEQ = EF.EFTPS_FILE_SEQ
            WHERE etf.file_id = p_payment_file_id
            AND mmt.status = 'Executed'
            AND mmt.tax_payment_status = 'SentToAgency'
            AND mmt.initiation_date = v_payment_initiation_date
            AND ft.current_transaction_state_fk = 'Executed'
            AND trunc(ft.settlement_date) >= trunc(mmt.initiation_date)
            AND ft.settlement_date >= mmt.initiation_date)
    */
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'financial transaction state insert finished - ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'hh24:mi:ss'));
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    UPDATE PSP_EDI_TAX_FILE
      SET
        STATUS_CD = 'PendingTransmission',
        MODIFIED_DATE = SYS_EXTRACT_UTC(SYSTIMESTAMP),
        MODIFIER_ID = p_user_id,
        VERSION = VERSION + 1
      WHERE SYSTEM_OWNER = 'PSP'
      AND FILE_ID = p_payment_file_id
    */
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_eftps_payments_sent_events(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_payment_file_id DOUBLE PRECISION, IN p_tax_payment_status TEXT)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* aka file control number */
DECLARE
    v_payment_file_seq CHARACTER VARYING(36);
    v_payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* unused */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
    aws$frmt_err_bcktrc CHARACTER VARYING(2000);
    aws$frmt_err_num CHARACTER VARYING(8);
    aws$frmt_err_stck CHARACTER VARYING(2000);
/* error desc variable for logging */
BEGIN
    BEGIN
        SELECT
            eftps_file_seq, payment_initiation_date
            INTO STRICT v_payment_file_seq, v_payment_initiation_date
            FROM pspadm.psp_eftps_file AS ef
            JOIN pspadm.psp_eftps_payment_detail AS epd
                ON epd.parent_file_fk = ef.eftps_file_seq
            JOIN pspadm.psp_edi_tax_file AS etf
                ON etf.edi_tax_file_seq = ef.eftps_file_seq
            WHERE file_id = p_payment_file_id
            LIMIT 1;
        EXCEPTION
            WHEN too_many_rows THEN
                RAISE USING hint = -20052, message = CONCAT_WS('', 'ERROR: MULTIPLE FILES FOUND FOR FILE_ID: ', p_payment_file_id), detail = 'User-defined exception';
            WHEN no_data_found THEN
                /* AS400 files */
                RETURN;
            WHEN others THEN
                /* Output desired error message */
                RAISE DEBUG USING MESSAGE = '-20999: unexpected error -- error stack follows';
                /* Output actual line number of error source */
                GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
                RAISE DEBUG USING MESSAGE = aws$frmt_err_bcktrc::TEXT;
                /* Output the actual error number and message */
                GET STACKED DIAGNOSTICS aws$frmt_err_num = RETURNED_SQLSTATE,
                    aws$frmt_err_stck = MESSAGE_TEXT;
                aws$frmt_err_stck := CONCAT(aws$frmt_err_num, ': ', aws$frmt_err_stck);
                RAISE DEBUG USING MESSAGE = aws$frmt_err_stck::TEXT;
    END;
    CALL pspadm.prc_eftps_payments_events('PRC_EFTPS_PAYMENTS_SENT'::TEXT, p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, p_tax_payment_status);
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload(IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE, IN p_file_type TEXT, IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */
/* DD or Tax */
/* For audit purposes */
/* UTC Date */
/* 11/20/09 added new hints */
/* these two variables are used in all SQL statements to populate date fields, */
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    /* current system date and time adjusted by PSPDate offset */
    /* current system UTC date and time */
    /* return code variable for logging */
    /* error desc variable for logging */
    sql$rowcount BIGINT;
    v_psp_date$1937460023 TIMESTAMP(6) WITHOUT TIME ZONE;
BEGIN
    PERFORM aws_oracle_ext.packageinitialize(proutinename => 'pspadm.prc_offload', pforce => TRUE);
    PERFORM aws_oracle_ext.packageinitialize(proutinename => 'pspadm.prc_offload', pforce => TRUE);
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_OFFLOAD_BATCH_ID', pval => p_offload_batch_id);
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_OFFLOAD_DATE', pval => p_offload_date);
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_USER_ID', pval => p_user_id);
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.TO_TIMESTAMP_TZ(VARCHAR2) function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC (to_timestamp_tz(concat(fn_get_psp_timestamp(0), ' US/Pacific')))
         INTO v_psp_date
         FROM DUAL
    */
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_psp_date', pval => v_psp_date$1937460023);
    PERFORM aws_oracle_ext.setglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_utc_date', pval => p_app_server_date);
    UPDATE pspadm.psp_offload_batch
    SET status_cd = 'Completed', status_effecive_date = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE), version = version + 1, modifier_id = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_USER_ID', ptp => NULL::VARCHAR(8000)), modified_date = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_utc_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE)
        WHERE offload_batch_seq = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_OFFLOAD_BATCH_ID', ptp => NULL::VARCHAR(8000));

    IF p_file_type = 'DD' THEN
        CALL pspadm.prc_offload$update_nacha_file_trace_number$959a4e53('CCD'::TEXT);
        CALL pspadm.prc_offload$update_nacha_file_trace_number$959a4e53('PPD'::TEXT);
    END IF;
    /* 'DD' File Type */

    IF p_file_type = 'Tax' THEN
        CALL pspadm.prc_offload$update_nacha_file_trace_number$959a4e53('CCDPlus'::TEXT);
    END IF;
    /* 'Tax' File Type */
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload$update_nacha_file_trace_number$959a4e53(IN p_nacha_file_type TEXT)
AS 
$BODY$
/* pspadm.prc_offload.update_nacha_file_trace_number */
DECLARE
    v_nacha_file_id CHARACTER VARYING(100);
    prc_offload$v_return_cd DOUBLE PRECISION;
    prc_offload$v_error_desc CHARACTER VARYING(100);
BEGIN
    prc_offload$v_return_cd := aws_oracle_ext.get_package_variable('pspadm', 'prc_offload', 'v_return_cd')::DOUBLE PRECISION;
    prc_offload$v_error_desc := aws_oracle_ext.get_package_variable('pspadm', 'prc_offload', 'v_error_desc')::CHARACTER VARYING(100);
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                        v_RETURN_CD,
                        v_ERROR_DESC,
                        'N/A',                          -- p_CompanyId          IN   VARCHAR2,
                        null,                         -- p_TypeCd             IN   VARCHAR2,
                        'PROD',                         -- p_DomainName         IN   VARCHAR2,
                        'PSP',                          --p_ArchName           IN   VARCHAR2,
                        'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
                        'N/A',                          -- p_HostName           IN   VARCHAR2,
                        'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
                        'PSP_ENTRY_DETAIL_RECORD',   --  p_ObjectName         IN   VARCHAR2,
                        'N/A',                          --p_UserName           IN   VARCHAR2,
                        to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
                        'Updating ENTRY_DETAIL_RECORD ' || p_nacha_file_type )
    */
    PERFORM aws_oracle_ext.set_package_variable('pspadm', 'prc_offload', 'v_error_desc', prc_offload$v_error_desc);
    PERFORM aws_oracle_ext.set_package_variable('pspadm', 'prc_offload', 'v_return_cd', prc_offload$v_return_cd);
    SELECT
        nachafile_seq
        INTO STRICT v_nacha_file_id
        FROM pspadm.psp_nachafile
        WHERE offload_batch_fk = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_OFFLOAD_BATCH_ID', ptp => NULL::VARCHAR(8000)) AND file_type = p_nacha_file_type;
    /* Update trace number for entry detail records associated with nacha file */
    /* nvl on record_data with some value so that the sort order is preserved */
    /* no need to do above, checked with Dawn.. */
    
    /*
    [5065 - Severity CRITICAL - PostgreSQL doesn't support the UPDATE statement for a subquery. Perform this operation on the underlying tables instead.]
    UPDATE
                 (SELECT  /*+
                          INDEX (rec0 PSP_ENTRY_DETAIL_RECORD_FK2)
                        */
                                rec0.entry_detail_record_seq, rec0.trace_number, rec0.n_a_c_h_a_file_fk,
                              rec0.record_data_enc, rec0.VERSION, rec0.modifier_id, rec0.modified_date
                  FROM psp_entry_detail_record rec0
                  WHERE rec0.n_a_c_h_a_file_fk = v_nacha_file_id
                        AND rec0.initiation_date = p_offload_date
                  ORDER BY rec0.legal_name,
                              rec0.company_fk,
                              rec0.n_a_c_h_a_batch_type,
                              rec0.settlement_date,
                              rec0.record_data_enc,
                              rec0.amount,
                              rec0.entry_detail_record_seq) src
                  SET trace_number = DECODE (NVL (record_data_enc, '0'), '0', NULL, seq_trace_number.NEXTVAL),
                      VERSION = VERSION + 1,
                      modifier_id = p_user_id,
                      modified_date = v_utc_date
    */
    GET DIAGNOSTICS sql$rowcount = ROW_COUNT;

    IF sql$rowcount != 0 THEN
        UPDATE pspadm.psp_nachafile
        SET status = 'Finalized', version = version + 1, modifier_id = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'P_USER_ID', ptp => NULL::VARCHAR(8000)), modified_date = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_utc_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE), finalization_date = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE), status_effective_date = aws_oracle_ext.getglobalvariable(proutinename => 'PSPADM.prc_offload', pvariable => 'v_psp_date', ptp => NULL::TIMESTAMP(6) WITHOUT TIME ZONE)
            WHERE nachafile_seq = v_nacha_file_id;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload_insert_fts(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */
/* these two variables are used in all SQL statements to populate date fields, */
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system date and time adjusted by PSPDate offset */
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
/* current system UTC date and time */
/* SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL; */
BEGIN
    /* DATE LOGIC TO BE REVIEWED ******** */
    /* I think we should use p_offload_date instead of v_psp_date .. */
    
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL
    */
    v_utc_date := p_app_server_date;
    /* Create Executed Transaction States for all transactions in the Offload Batch */
    
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.NEW_TIME(DATE,VARCHAR2,VARCHAR2) function. Use suitable function or create user defined function.]
    INSERT /*+ APPEND */ INTO PSP_FINANCIAL_TRANS_STATE
             (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
               CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
               REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
               GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
               TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
             (SELECT FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1,
                    trunc(new_time(v_psp_date,'GMT','PDT')) + .5, NULL, NULL, FT.FINANCIAL_TRANSACTION_SEQ, 'Executed', NULL, ft.company_fk, ft.transaction_type_fk
                 FROM PSP_FINANCIAL_TRANSACTION FT,
                      PSP_MONEY_MOVEMENT_TRANSACTION MMT
                 WHERE MMT.OFFLOAD_BATCH_FK = p_offload_batch_id
                   AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
                   AND FT.CURRENT_TRANSACTION_STATE_FK = 'Executed'
                   AND MMT.initiation_date =  p_offload_date
                   AND FT.SETTLEMENT_DATE >= MMT.INITIATION_DATE
                   AND trunc(FT.SETTLEMENT_DATE) >= trunc(MMT.INITIATION_DATE)
               )
    */
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload_upd_agency_status(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE, IN p_file_type TEXT)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */
/* DD or Tax */
/* these two variables are used in all SQL statements to populate date fields, */
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system date and time adjusted by PSPDate offset */
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    mmtrec RECORD;
/* current system UTC date and time */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL
    */
    v_utc_date := p_app_server_date;

    IF p_file_type = 'Tax' THEN
        /* For each MMT we are going to update the tax_payment_status on below, */
        /* recalculate the ATF payments passing in the new and existing payment status. */
        FOR mmtrec IN
        SELECT
            mmt.*
            FROM pspadm.psp_money_movement_transaction AS mmt
            WHERE tax_payment_status = 'SentToAgency' AND money_movement_payment_method = 'ACHCredit' AND status = 'Executed' AND mmt.offload_batch_fk = p_offload_batch_id
        LOOP
            CALL pspadm.prc_recalculate_atf_payments(p_user_id, v_utc_date, mmtrec.money_movement_transaction_seq, mmtrec.payment_template_fk, mmtrec.money_movement_payment_method, 'AcknowledgedByAgency'::TEXT, mmtrec.payment_period_end, mmtrec.initiation_date, mmtrec.company_fk);
        END LOOP;
        /* Set the Payment Status of ACHCRedit MMTs to AcknowledgedByAgency */
        UPDATE pspadm.psp_money_movement_transaction AS mmt
        SET tax_payment_status = 'AcknowledgedByAgency', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
            WHERE tax_payment_status = 'SentToAgency' AND money_movement_payment_method = 'ACHCredit' AND status = 'Executed' AND mmt.offload_batch_fk = p_offload_batch_id;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload_update_ft(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */
/* these two variables are used in all SQL statements to populate date fields, */
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system date and time adjusted by PSPDate offset */
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system UTC date and time */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
/* error desc variable for logging */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL
    */
    v_utc_date := p_app_server_date;
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
              v_RETURN_CD,
              v_ERROR_DESC,
              'N/A',                          -- p_CompanyId          IN   VARCHAR2,
              null,                         -- p_TypeCd             IN   VARCHAR2,
              'PROD',                         -- p_DomainName         IN   VARCHAR2,
              'PSP',                          --p_ArchName           IN   VARCHAR2,
              'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
              'N/A',                          -- p_HostName           IN   VARCHAR2,
              'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
              'PSP_FINANCIAL_TRANSACTION',   --  p_ObjectName         IN   VARCHAR2,
              'N/A',                          --p_UserName           IN   VARCHAR2,
              to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
              'Updating FINANCIAL_TRANSACTION')
    */
    /* Set the status of Financial Transactions to Executed for all transactions in the Offload Batch */
    UPDATE pspadm.psp_financial_transaction AS ft0
    SET current_transaction_state_fk = 'Executed', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE ft0.current_transaction_state_fk = 'Created' AND ft0.settlement_date >= p_offload_date AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1
            WHERE mmt1.money_movement_transaction_seq = ft0.money_movement_transaction_fk AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.mm_transaction_amount >= 0);
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload_update_mmt(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE, IN p_file_type TEXT)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */
/* DD or Tax */
/* these two variables are used in all SQL statements to populate date fields, */
/* the UTC date is used to populate SPCF audit fields created_date and modified_date */
DECLARE
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system date and time adjusted by PSPDate offset */
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system UTC date and time */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
/* error desc variable for logging */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL
    */
    v_utc_date := p_app_server_date;
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
            v_RETURN_CD,
            v_ERROR_DESC,
           'N/A',                          -- p_CompanyId          IN   VARCHAR2,
            null,                         -- p_TypeCd             IN   VARCHAR2,
           'PROD',                         -- p_DomainName         IN   VARCHAR2,
           'PSP',                          --p_ArchName           IN   VARCHAR2,
           'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
           'N/A',                          -- p_HostName           IN   VARCHAR2,
           'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
           'PSP_MONEY_MOVEMENT_TRANSACTION',   --  p_ObjectName         IN   VARCHAR2,
           'N/A',                          --p_UserName           IN   VARCHAR2,
           to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
           'Updating MONEY_MOVEMENT_TRANSACTION')
    */
    /* */
    /* Set MMTxns status to Executed */
    /* */
    IF p_file_type = 'Tax' THEN
        UPDATE pspadm.psp_money_movement_transaction AS mmt
        SET status = 'Executed', version = version + 1, modifier_id = p_user_id, tax_payment_status = 'SentToAgency', modified_date = v_utc_date
            WHERE mmt.offload_batch_fk = p_offload_batch_id AND mmt.initiation_date = p_offload_date AND mmt.mm_transaction_amount >= 0;
    ELSE
        UPDATE pspadm.psp_money_movement_transaction AS mmt
        SET status = 'Executed', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
            WHERE mmt.offload_batch_fk = p_offload_batch_id AND mmt.initiation_date = p_offload_date AND mmt.mm_transaction_amount >= 0;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload_update_payroll(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */

/*
*****************************************************************************
   UPDATED: 03.24.2010
   PURPOSE: Updates payroll status for the offload
   LOGIC  : PayrollRun status is updated during the offload, currently on busy day
           it takes around 10-20mins. The offloaded financial transaction statuses
           will already be committed as Executed, but the payroll statuses will
           not be updated by this SP.
           We are moving the same updates after the file is written and before the
           missed ach txn batch job runs. This will reduce the time for file generation.


   ASSUMPTIONS:
          1. This call should be added to the offloadAndPostOffload method in the
             batch job itself (as the fee event  creation currently is),
             so it will run via the  existing test ws method.
          2. No error handling in SP, it will be thrown back to calling program
          3. The agents will not be able to perform certain actions until this is done,
             may get core errors if they try to do other actions before the payroll
             status is updated. This is because SAP looks only at the payroll status
             to allow/disallow actions while core actually looks at the transaction
             cutoff time before it does any processing for transactions that
             will be cancelled/modified.Worst case it would return them a
             database locking error rather than a core error.
          4. COMMIT WILL BE DONE IN SP

    TODO:
        1. Performance test in LT
        2. Functional test


   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        03.24.2010  Tushar           Created

*****************************************************************************
*/
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system date and time adjusted by PSPDate offset */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
/* error desc variable for logging */
BEGIN
    v_utc_date := p_app_server_date;
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG(
            v_RETURN_CD,
            v_ERROR_DESC,
            'N/A',                          -- p_CompanyId          IN   VARCHAR2,
            null,                         -- p_TypeCd             IN   VARCHAR2,
            'PROD',                         -- p_DomainName         IN   VARCHAR2,
            'PSP',                          --p_ArchName           IN   VARCHAR2,
            'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
            'N/A',                          -- p_HostName           IN   VARCHAR2,
            'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
            'PSP_PAYROLL_RUN',   --  p_ObjectName         IN   VARCHAR2,
            'N/A',                          --p_UserName           IN   VARCHAR2,
            to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
            'Updating PAYROLL_RUN non-reversals')
    */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedDebit', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status != 'OffloadedAll' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Impound');
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'AutoRedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingAutoRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Redebit');
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'RedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Redebit');
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdCredit');
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                               v_RETURN_CD,
                               v_ERROR_DESC,
                               'N/A',                          -- p_CompanyId          IN   VARCHAR2,
                               null,                         -- p_TypeCd             IN   VARCHAR2,
                               'PROD',                         -- p_DomainName         IN   VARCHAR2,
                               'PSP',                          --p_ArchName           IN   VARCHAR2,
                               'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
                               'N/A',                          -- p_HostName           IN   VARCHAR2,
                               'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
                               'PSP_PAYROLL_RUN',   --  p_ObjectName         IN   VARCHAR2,
                               'N/A',                          --p_UserName           IN   VARCHAR2,
                               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
                    'Updating PAYROLL_RUN reversals')
    */
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(FROM_TZ(FN_GET_PSP_TIMESTAMP,'US/Pacific')) INTO v_psp_date FROM DUAL
    */
    INSERT INTO pspadm.psp_atfpayrolls_to_process
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 0, p_user_id, v_psp_date, p_user_id, v_psp_date, - 1, pr.payroll_run_seq
        FROM pspadm.psp_payroll_run AS pr
        WHERE (pr.payroll_run_status = 'OffloadedAll' OR pr.payroll_run_status = 'OffloadedDebit') AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND ft1.transaction_type_fk = 'EmployerTaxDebit');
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'ReversalsOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingReversals' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdReversalDebit');
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerTaxDebit', 'EmployerTaxCredit'));
    /* Update payrolls that only contain fees when the fees offload */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE payroll_run_status != 'AutoRedebitOffloaded' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerFeeDebit')) AND NOT EXISTS (SELECT
            'T'
            FROM pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND ft1.transaction_type_fk IN ('EmployerDdDebit', 'EmployerTaxDebit'));
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'Complete', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'OffloadedAll' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_financial_transaction AS ft2
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND ft2.payroll_run_fk = ft1.payroll_run_fk AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdCredit' AND ft2.current_transaction_state_fk = 'Completed' AND ft2.settlement_type_cd <> 'ACH' AND ft2.transaction_type_fk = 'EmployerDdDebit');
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
    	           v_RETURN_CD,
                       v_ERROR_DESC,
                       'N/A',                          -- p_CompanyId          IN   VARCHAR2,
                        null,                         -- p_TypeCd             IN   VARCHAR2,
                       'PROD',                         -- p_DomainName         IN   VARCHAR2,
                       'PSP',                          --p_ArchName           IN   VARCHAR2,
                       'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
                       'N/A',                          -- p_HostName           IN   VARCHAR2,
                       'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
                       'ALL DONE',   --  p_ObjectName         IN   VARCHAR2,
                       'N/A',                          --p_UserName           IN   VARCHAR2,
                       to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
                       'Writing Files')
    */
    COMMIT;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_offload_update_pr_test(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_offload_batch_id TEXT, IN p_offload_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* For audit purposes */
/* UTC Date */
/* psp_offload_batch.offload_batch_seq */
/* UTC Date */

/*
*****************************************************************************
   UPDATED: 03.24.2010
   PURPOSE: Updates payroll status for the offload
   LOGIC  : PayrollRun status is updated during the offload, currently on busy day
           it takes around 10-20mins. The offloaded financial transaction statuses
           will already be committed as Executed, but the payroll statuses will
           not be updated by this SP.
           We are moving the same updates after the file is written and before the
           missed ach txn batch job runs. This will reduce the time for file generation.


   ASSUMPTIONS:
          1. This call should be added to the offloadAndPostOffload method in the
             batch job itself (as the fee event  creation currently is),
             so it will run via the  existing test ws method.
          2. No error handling in SP, it will be thrown back to calling program
          3. The agents will not be able to perform certain actions until this is done,
             may get core errors if they try to do other actions before the payroll
             status is updated. This is because SAP looks only at the payroll status
             to allow/disallow actions while core actually looks at the transaction
             cutoff time before it does any processing for transactions that
             will be cancelled/modified.Worst case it would return them a
             database locking error rather than a core error.
          4. COMMIT WILL BE DONE IN SP

    TODO:
        1. Performance test in LT
        2. Functional test


   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        03.24.2010  Tushar           Created

*****************************************************************************
*/
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    v_psp_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system date and time adjusted by PSPDate offset */
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
    /* error desc variable for logging */
    v_pspo_offload_group CHARACTER VARYING(100);
    /* offload_group of PSPO offload group to identify phase of company */
    v_dds_offload_group CHARACTER VARYING(100);
    /* offload_group of DDS offload group to identify phase of company */
    v_std_offload_group CHARACTER VARYING(100);
/* offload_group of STD offload group to identify phase of company */
BEGIN
    v_utc_date := p_app_server_date;
    v_std_offload_group := '3b67b658-dc4e-012a-fc4f-005056c02727';
    v_pspo_offload_group := '3b672729-dc4e-012a-fc4f-005056c02727';
    v_dds_offload_group := '3b672730-dc4e-012a-fc4f-005056c02727';
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG(
            v_RETURN_CD,
            v_ERROR_DESC,
            'N/A',                          -- p_CompanyId          IN   VARCHAR2,
            null,                         -- p_TypeCd             IN   VARCHAR2,
            'PROD',                         -- p_DomainName         IN   VARCHAR2,
            'PSP',                          --p_ArchName           IN   VARCHAR2,
            'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
            'N/A',                          -- p_HostName           IN   VARCHAR2,
            'Offload Stored Proc - Test State',          -- Application_name           IN   VARCHAR2,
            'PSP_PAYROLL_RUN',   --  p_ObjectName         IN   VARCHAR2,
            'N/A',                          --p_UserName           IN   VARCHAR2,
            to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
            'Updating test state PAYROLL_RUN non-reversals')
    */
    /* Impound transactions for STD offloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedDebit', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status != 'OffloadedAll' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Impound' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* Employer DD Debit Offload for PSPO offloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET d_d_status = 'OffloadedDebit', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.d_d_status != 'OffloadedAll' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployerDdDebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_pspo_offload_group);
    /* Employer Tax Debit Offload for PSPO offloadGroup */
    
    /*
    [9996 - Severity CRITICAL - Transformer error occurred. Please submit report to developers.]
    UPDATE psp_payroll_run pr
             SET tax_and_fees_status = 'OffloadedAll',
                 VERSION = VERSION + 1,
                 modifier_id = p_user_id,
                 modified_date = v_utc_date
           WHERE pr.tax_and_fees_status != 'OffloadedAll'
            AND  EXISTS (
                    SELECT 'T'
                      FROM psp_money_movement_transaction mmt1, psp_financial_transaction ft1, psp_company comp
                     WHERE ft1.payroll_run_fk = pr.payroll_run_seq
                       AND mmt1.offload_batch_fk = p_offload_batch_id
                       AND mmt1.initiation_date = p_offload_date
                       AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
                       AND ft1.settlement_date >= mmt1.initiation_date
    				           AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
                       AND ft1.current_transaction_state_fk = 'Executed'
                       AND ft1.transaction_type_fk in 'EmployerTaxDebit'
                       AND ft1.company_fk = comp.company_seq
                       AND (comp.offload_group_fk = v_pspo_offload_group
                            OR comp.offload_group_fk = v_dds_offload_group))
    */
    /* Employer Fee Debit Offload for PSPO offloadGroup */
    
    /*
    [9996 - Severity CRITICAL - Transformer error occurred. Please submit report to developers.]
    UPDATE psp_payroll_run pr
         SET tax_and_fees_status = 'OffloadedAll',
           VERSION = VERSION + 1,
           modifier_id = p_user_id,
           modified_date = v_utc_date
         WHERE pr.tax_and_fees_status != 'OffloadedAll'
         AND NOT EXISTS (
             SELECT
               'T'
             FROM psp_financial_transaction ft1
             WHERE ft1.payroll_run_fk = pr.payroll_run_seq
                   AND ft1.transaction_type_fk = 'EmployerTaxDebit')
         AND  EXISTS (
             SELECT 'T'
             FROM psp_money_movement_transaction mmt1, psp_financial_transaction ft1, psp_company comp, PSP_OFFERING_SVCCHG osc
             WHERE ft1.payroll_run_fk = pr.payroll_run_seq
                   AND mmt1.offload_batch_fk = p_offload_batch_id
                   AND mmt1.initiation_date = p_offload_date
                   AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
                   AND ft1.settlement_date >= mmt1.initiation_date
                   AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
                   AND ft1.current_transaction_state_fk = 'Executed'
                   AND ft1.transaction_type_fk in 'EmployerFeeDebit'
                   AND ft1.company_fk = comp.company_seq
                   AND (comp.offload_group_fk = v_pspo_offload_group
                        OR comp.offload_group_fk = v_dds_offload_group)
                   AND ft1.SKU= osc.S_K_U
                   AND osc.SKU_TYPE = 'Payroll')
    */
    /* Redit for STD OffloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'AutoRedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingAutoRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Redebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* DD Redebit offloaded for PSPOOffloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET d_d_status = 'AutoRedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.d_d_status = 'PendingAutoRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployerDdRedebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_pspo_offload_group);
    /* Tax/Fee Redebit offloaded for PSPO offloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET tax_and_fees_status = 'AutoRedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.tax_and_fees_status = 'PendingAutoRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerTaxRedebit', 'EmployerFeeRedebit', 'ServiceSalesAndUseTaxRedebit') AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /* Consolidate PayrollRun status for PSPO offloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'AutoRedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE (pr.d_d_status IN ('AutoRedebitOffloaded') AND pr.tax_and_fees_status IN ('AutoRedebitOffloaded', 'Complete', 'None', 'OffloadedAll')) OR (pr.d_d_status IN ('Complete', 'None', 'OffloadedAll', 'OffloadedDebit') AND pr.tax_and_fees_status IN ('AutoRedebitOffloaded')) AND pr.payroll_run_status = 'PendingAutoRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Redebit' AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /* for STD OffloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'RedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Redebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* for PSPO offload group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET d_d_status = 'RedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.d_d_status = 'PendingRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployerDdRedebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_pspo_offload_group);
    /* PSPO offload Group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET tax_and_fees_status = 'RedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.tax_and_fees_status = 'PendingRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerTaxRedebit', 'EmployerFeeRedebit', 'ServiceSalesAndUseTaxRedebit') AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /* Consolidated Payroll Run for PSPO offload Group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'RedebitOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE (pr.d_d_status IN ('RedebitOffloaded') AND pr.tax_and_fees_status IN ('RedebitOffloaded', 'Complete', 'None', 'OffloadedAll')) OR (pr.d_d_status IN ('Complete', 'None', 'OffloadedDebit', 'OffloadedAll') AND pr.tax_and_fees_status IN ('RedebitOffloaded')) AND pr.payroll_run_status = 'PendingRedebit' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND tt1.association_type = 'Redebit' AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /* STD offload group DD Credit */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdCredit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* DDCRedit for PSPO offloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET d_d_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdCredit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_pspo_offload_group);
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                               v_RETURN_CD,
                               v_ERROR_DESC,
                               'N/A',                          -- p_CompanyId          IN   VARCHAR2,
                               null,                         -- p_TypeCd             IN   VARCHAR2,
                               'PROD',                         -- p_DomainName         IN   VARCHAR2,
                               'PSP',                          --p_ArchName           IN   VARCHAR2,
                               'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
                               'N/A',                          -- p_HostName           IN   VARCHAR2,
                               'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
                               'PSP_PAYROLL_RUN',   --  p_ObjectName         IN   VARCHAR2,
                               'N/A',                          --p_UserName           IN   VARCHAR2,
                               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
                    'Updating test state PAYROLL_RUN reversals')
    */
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(FROM_TZ(FN_GET_PSP_TIMESTAMP,'US/Pacific')) INTO v_psp_date FROM DUAL
    */
    /* STD offload Group for EEDDReversal */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'ReversalsOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingReversals' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdReversalDebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* for PSPO offload Group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'ReversalsOffloaded', d_d_status = 'ReversalsOffloaded', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'PendingReversals' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdReversalDebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* STD offload group Only Fee scenario */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE payroll_run_status != 'AutoRedebitOffloaded' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerFeeDebit') AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group) AND NOT EXISTS (SELECT
            'T'
            FROM pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND ft1.transaction_type_fk IN ('EmployerDdDebit', 'EmployerTaxDebit'));
    /* Update payrolls that only contain fees when the fees offload for PSPO offload group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET tax_and_fees_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE payroll_run_status != 'AutoRedebitOffloaded' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerFeeDebit') AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group)) AND NOT EXISTS (SELECT
            'T'
            FROM pspadm.psp_financial_transaction AS ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND ft1.transaction_type_fk IN ('EmployerDdDebit', 'EmployerTaxDebit'));
    /* Consolidated PAyrollRun for OffloadedAll PSPO offload group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE (pr.d_d_status IN ('OffloadedAll') AND pr.tax_and_fees_status IN ('OffloadedAll', 'Complete', 'None')) OR (pr.d_d_status IN ('Complete', 'None') AND pr.tax_and_fees_status IN ('OffloadedAll')) AND pr.payroll_run_status != 'Complete' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /* Consolidated PayrollRun OffloadedDebit for PSPOOffload Group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedDebit', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.d_d_status IN ('OffloadedDebit') AND pr.tax_and_fees_status IN ('OffloadedAll', 'Complete', 'None') AND pr.payroll_run_status != 'Complete' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND tt1.association_type = 'Impound' AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    INSERT INTO pspadm.psp_atfpayrolls_to_process
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 0, p_user_id, v_psp_date, p_user_id, v_psp_date, - 1, pr.payroll_run_seq
        FROM pspadm.psp_payroll_run AS pr
        WHERE (pr.payroll_run_status = 'OffloadedAll' OR pr.payroll_run_status = 'OffloadedDebit') AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_transaction_type AS tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = tt1.transaction_type_cd AND ft1.transaction_type_fk = 'EmployerTaxDebit');
    /* STD offload group ErTaxDebit, ErTaxCredit */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerTaxDebit', 'EmployerTaxCredit') AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* ErTaxCredit ErTAxDebit for PSPOOffload group */
    UPDATE pspadm.psp_payroll_run AS pr
    SET tax_and_fees_status = 'OffloadedAll', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk IN ('EmployerTaxDebit', 'EmployerTaxCredit') AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /* Marking PAyroll run complete for Non ACH Payroll STD OffloadGroup */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'Complete', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'OffloadedAll' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_financial_transaction AS ft2, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND ft2.payroll_run_fk = ft1.payroll_run_fk AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdCredit' AND ft2.current_transaction_state_fk = 'Completed' AND ft2.settlement_type_cd <> 'ACH' AND ft2.transaction_type_fk = 'EmployerDdDebit' AND ft1.company_fk = comp.company_seq AND comp.offload_group_fk = v_std_offload_group);
    /* Marking PAyrollRun Complete for Non ACH payrolls for PSPO/DDS OffloadGroup */
    /* we were earlier marking payroll_run_status as OffloadedAll without confirming TaxAndFee is offloaded. Doing same in PSPO and DDS */
    UPDATE pspadm.psp_payroll_run AS pr
    SET payroll_run_status = 'Complete', d_d_status = 'Complete', version = version + 1, modifier_id = p_user_id, modified_date = v_utc_date
        WHERE pr.payroll_run_status = 'OffloadedAll' AND EXISTS (SELECT
            'T'
            FROM pspadm.psp_money_movement_transaction AS mmt1, pspadm.psp_financial_transaction AS ft1, pspadm.psp_financial_transaction AS ft2, pspadm.psp_company AS comp
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq AND ft2.payroll_run_fk = ft1.payroll_run_fk AND mmt1.offload_batch_fk = p_offload_batch_id AND mmt1.initiation_date = p_offload_date AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk AND ft1.settlement_date >= mmt1.initiation_date AND DATE(ft1.settlement_date) >= DATE(mmt1.initiation_date) AND ft1.current_transaction_state_fk = 'Executed' AND ft1.transaction_type_fk = 'EmployeeDdCredit' AND ft2.current_transaction_state_fk = 'Completed' AND ft2.settlement_type_cd <> 'ACH' AND ft2.transaction_type_fk = 'EmployerDdDebit' AND ft1.company_fk = comp.company_seq AND (comp.offload_group_fk = v_pspo_offload_group OR comp.offload_group_fk = v_dds_offload_group));
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
    	           v_RETURN_CD,
                       v_ERROR_DESC,
                       'N/A',                          -- p_CompanyId          IN   VARCHAR2,
                        null,                         -- p_TypeCd             IN   VARCHAR2,
                       'PROD',                         -- p_DomainName         IN   VARCHAR2,
                       'PSP',                          --p_ArchName           IN   VARCHAR2,
                       'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
                       'N/A',                          -- p_HostName           IN   VARCHAR2,
                       'Offload Stored Proc - Test State',          -- Application_name           IN   VARCHAR2,
                       'ALL DONE',   --  p_ObjectName         IN   VARCHAR2,
                       'N/A',                          --p_UserName           IN   VARCHAR2,
                       to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
                       'Writing Files')
    */
    COMMIT;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_recalc_atf_payments_eftps(IN p_calling_procedure TEXT, IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_payment_file_seq TEXT, IN p_payment_initiation_date TIMESTAMP WITHOUT TIME ZONE)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
/* primary key of payment file */
/* unused */
DECLARE
    v_return_cd DOUBLE PRECISION;
    /* return code variable for logging */
    v_error_desc CHARACTER VARYING(100);
/* error desc variable for logging */
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    PRC_SET_PSP_EVENT_LOG (
                   v_RETURN_CD,
                   v_ERROR_DESC,
                   'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                    null,                           -- p_TypeCd             IN   VARCHAR2,
                   'PROD',                          -- p_DomainName         IN   VARCHAR2,
                   'PSP',                           --p_ArchName            IN   VARCHAR2,
                   p_calling_procedure,             -- p_CompName           IN   VARCHAR2,
                   'N/A',                           -- p_HostName           IN   VARCHAR2,
                   'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                   'PSP_ATFPAYMENTS_TO_PROCESS',    --  p_ObjectName        IN   VARCHAR2,
                   'N/A',                           --p_UserName            IN   VARCHAR2,
                   to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                  'Updating PSP_ATFPAYMENTS_TO_PROCESS')
    */
    /* remove existing ATF payment information associated w/MMTs */
    DELETE FROM pspadm.psp_atfpayments_to_process
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_fk
            FROM pspadm.psp_eftps_payment_detail AS epd
            WHERE epd.parent_file_fk = p_payment_file_seq AND epd.status_cd IN ('AcknowledgedByAgency', 'ReturnedTaxPaid', 'RejectedByAgency', 'ReturnedTaxNotPaid'));
    /* insert acknowledged payments by law */
    INSERT INTO pspadm.psp_atfpayments_to_process (atfpayments_to_process_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, payment_date, quarter_end_date, law_fk, money_movement_transaction_fk, company_fk, amount)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, p_user_id, p_app_server_date, p_user_id, p_app_server_date, - 1, settlement_date, quarter_end, law_fk, money_movement_transaction_seq, company_fk, amount
        FROM (SELECT
            mmt.company_fk, mmt.money_movement_transaction_seq, pspadm.fn_get_last_day_of_quarter(mmt.payment_period_end) AS quarter_end, ft.law_fk, PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ft.settlement_date) AS settlement_date, SUM(CASE
                WHEN current_transaction_state_fk IN ('Cancelled', 'Voided') THEN 0
                WHEN transaction_type_fk IN ('AgencyPostBALFHPDETaxPayment', 'AgencyPostBALFHPDETaxRefund') THEN 0
                WHEN transaction_type_fk IN ('AgencyTaxDebit', 'AgencyDirectDebit', 'AgencyHPDETaxRefund', 'AgencyTaxOverpaymentApplied') THEN (ft.financial_transaction_amount * - 1)
                WHEN transaction_type_fk IN ('AgencyTaxCredit', 'AgencyDirectCredit', 'AgencyHPDETaxPayment') THEN ft.financial_transaction_amount
                ELSE 0
            END) AS amount
            FROM pspadm.psp_eftps_payment_detail AS epd
            JOIN pspadm.psp_money_movement_transaction AS mmt
                ON mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
            JOIN pspadm.psp_financial_transaction AS ft
                ON ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
            WHERE epd.parent_file_fk = p_payment_file_seq AND epd.status_cd IN ('AcknowledgedByAgency', 'ReturnedTaxPaid') AND mmt.initiation_date = p_payment_initiation_date AND mmt.money_movement_payment_method IN ('EFTPS', 'EFTPSDirectDebit') AND ft.settlement_date >= p_payment_initiation_date AND ft.law_fk IS NOT NULL
            GROUP BY mmt.company_fk, mmt.money_movement_transaction_seq, pspadm.fn_get_last_day_of_quarter(mmt.payment_period_end), ft.law_fk) AS var_sbq;
    /* rejected payments */
    /* raffi: combine w/above?  case when epd.status_cd = 'RejectedByAgency' then 0 */
    INSERT INTO pspadm.psp_atfpayments_to_process (atfpayments_to_process_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, payment_date, quarter_end_date, law_fk, money_movement_transaction_fk, company_fk, amount)
    SELECT
        pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, p_user_id, p_app_server_date, p_user_id, p_app_server_date, - 1, settlement_date, quarter_end, law_fk, money_movement_transaction_seq, company_fk, amount
        FROM (SELECT
            mmt.company_fk, mmt.money_movement_transaction_seq, pspadm.fn_get_last_day_of_quarter(mmt.payment_period_end) AS quarter_end, ft.law_fk, PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ft.settlement_date) AS settlement_date, 0 AS amount
            FROM pspadm.psp_eftps_payment_detail AS epd
            JOIN pspadm.psp_money_movement_transaction AS mmt
                ON mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
            JOIN pspadm.psp_financial_transaction AS ft
                ON ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
            WHERE epd.parent_file_fk = p_payment_file_seq AND epd.status_cd IN ('RejectedByAgency', 'ReturnedTaxNotPaid') AND mmt.initiation_date = p_payment_initiation_date AND mmt.money_movement_payment_method IN ('EFTPS', 'EFTPSDirectDebit') AND ft.settlement_date >= p_payment_initiation_date AND ft.law_fk IS NOT NULL
            GROUP BY mmt.company_fk, mmt.money_movement_transaction_seq, pspadm.fn_get_last_day_of_quarter(mmt.payment_period_end), ft.law_fk) AS var_sbq_2;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_recalculate_atf_payments(IN p_user_id TEXT, IN p_app_server_date TIMESTAMP WITHOUT TIME ZONE, IN p_mmt_seq TEXT, IN p_payment_template_cd TEXT, IN p_payment_method TEXT, IN p_payment_status TEXT, IN p_pay_period_end TIMESTAMP WITHOUT TIME ZONE, IN p_initiation_date TIMESTAMP WITHOUT TIME ZONE, IN p_company_seq TEXT, IN p_bulk_loading BOOLEAN DEFAULT FALSE)
AS 
$BODY$
/* for audit purposes */
/* UTC Date */
DECLARE
    v_amount PSPADM.PSP_ATFPAYMENTS_TO_PROCESS.amount%TYPE;
    v_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE;
    v_treat_as_zero BOOLEAN;
    lawrec RECORD;
    ftrec RECORD;
/* Some method/status combinations will be treated as 0 regardless of FT amounts. */
BEGIN
    IF p_payment_method IN ('EFTPS', 'EFTPSDirectDebit', 'HPDE', 'HPDERefund', 'CheckPayment', 'ACHCredit', 'ACHDebit', 'EDI', 'SuperCheck') AND p_payment_status IN ('AcknowledgedByAgency', 'ReturnedTaxPaid', 'RejectedByAgency', 'ReturnedTaxNotPaid', 'None') THEN
        /* If we are performing an initial data load, this is unnecessary. */
        IF (p_bulk_loading = FALSE) THEN
            /* Delete any existing ATF Payment records for this MMT. */
            DELETE FROM pspadm.psp_atfpayments_to_process
                WHERE money_movement_transaction_fk = p_mmt_seq;
        END IF;
        /* For each law that is part of this MMT's payment template. */

        FOR lawrec IN
        SELECT
            pspadm.psp_law.law_id
            FROM pspadm.psp_law
            JOIN pspadm.psp_payment_template AS pt
                ON pt.payment_template_cd = pspadm.psp_law.payment_template_fk
            WHERE pt.payment_template_cd = p_payment_template_cd AND pt.support_start_date <= p_pay_period_end
        LOOP
            v_treat_as_zero := FALSE;
            v_amount := 0.0;
            v_settlement_date := NULL;

            IF p_payment_status IN ('RejectedByAgency', 'ReturnedTaxNotPaid') THEN
                v_treat_as_zero := TRUE;
            END IF;

            FOR ftrec IN
            SELECT
                ft.settlement_date, ft.transaction_type_fk, ft.financial_transaction_amount, ft.current_transaction_state_fk
                FROM pspadm.psp_financial_transaction AS ft
                WHERE ft.money_movement_transaction_fk = p_mmt_seq AND ft.law_fk = lawrec.law_id AND ft.settlement_date >= p_initiation_date AND DATE(ft.settlement_date) >= DATE(p_initiation_date)
            LOOP
                /* We may be setting this to the same value multiple times... */
                v_settlement_date := ftrec.settlement_date;
                /* Calculate this FT's impact on the total amount. */

                IF ftrec.current_transaction_state_fk IN ('Cancelled', 'Voided') THEN
                    v_treat_as_zero := TRUE;
                    EXIT;
                ELSIF ftrec.transaction_type_fk IN ('AgencyPostBALFHPDETaxPayment', 'AgencyPostBALFHPDETaxRefund') THEN
                    v_treat_as_zero := TRUE;
                    EXIT;
                /* Determine if this is an addition or subtraction. */
                ELSIF ftrec.transaction_type_fk IN ('AgencyTaxDebit', 'AgencyDirectDebit', 'AgencyHPDETaxRefund', 'AgencyTaxOverpaymentApplied') THEN
                    v_amount := v_amount - ftrec.financial_transaction_amount;
                ELSIF ftrec.transaction_type_fk IN ('AgencyTaxCredit', 'AgencyDirectCredit', 'AgencyHPDETaxPayment') THEN
                    v_amount := v_amount + ftrec.financial_transaction_amount;
                END IF;
            END LOOP;
            /* Only create an ATF record if we found at least 1 FT for this Law. */

            IF v_settlement_date IS NOT NULL THEN
                /* If we've determined that the amount should be zero, override the calculated amount. */
                IF v_treat_as_zero THEN
                    v_amount := 0;
                END IF;
                /* Insert the record while converting the pay_period_end date to the equivalent quarter end date. */
                INSERT INTO pspadm.psp_atfpayments_to_process (atfpayments_to_process_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, payment_date, quarter_end_date, law_fk, money_movement_transaction_fk, company_fk, amount)
                VALUES (pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, p_user_id, p_app_server_date, p_user_id, p_app_server_date, - 1, v_settlement_date, pspadm.fn_get_last_day_of_quarter(p_pay_period_end), lawrec.law_id, p_mmt_seq, p_company_seq, v_amount);
            END IF;
        END LOOP;
    END IF;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_remove_company(IN uniqueid TEXT, IN tablename TEXT)
AS 
$BODY$
DECLARE
    UniqueIdCollection CHARACTER VARYING(1020) [];
    ParentColumnNames CHARACTER VARYING(50) [];
    id_collection UniqueIdCollection%TYPE;
    c1 CURSOR FOR
    SELECT
        table_name, constraint_name, constraint_type, r_constraint_name
        FROM aws_oracle_ext.SYS_USER_CONSTRAINTS;
    c1$ATTRIBUTES aws_oracle_data.TCursorAttributes := ROW (FALSE, NULL, NULL, NULL);
    UserConstraints pspadm.prc_remove_company$c1 [];
    child_constraint_collection UserConstraints%TYPE;
    parent_constraint_collection UserConstraints%TYPE;
    ParentRecs pspadm.prc_remove_company$parentrec [];
    parent_recs ParentRecs%TYPE;
    ColumnNamesList CHARACTER VARYING(50) [];
    columnNames ColumnNamesList%TYPE;
    parent_id CHARACTER VARYING(1020);
    parent_columns ParentColumnNames%TYPE;
    delete_stmt_str CHARACTER VARYING(200);
    child_select_stmt_str CHARACTER VARYING(1000);
    parent_select_stmt_str CHARACTER VARYING(3000);
    select_stmt_str CHARACTER VARYING(300);
    /* delete_stmt_str VARCHAR2(300); */
    x DOUBLE PRECISION := 1;
BEGIN
    child_select_stmt_str := CONCAT_WS('', 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints', ' WHERE r_constraint_name IN (SELECT constraint_name FROM user_constraints ', 'WHERE table_name = ''', tablename, ''')', 'AND table_name NOT IN', '(''PSP_COMPANY'')');
    /* DBMS_OUTPUT.PUT_LINE(child_select_stmt_str); */
    
    /*
    [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
    EXECUTE IMMEDIATE child_select_stmt_str BULK COLLECT INTO child_constraint_collection
    */
    FOR i IN 1..COALESCE(array_length(child_constraint_collection, 1), 0) LOOP
        BEGIN
            /* DBMS_OUTPUT.PUT_LINE(child_constraint_collection(i).table_name); */
            /* DBMS_OUTPUT.PUT_LINE(child_constraint_collection(i).constraint_name); */
            SELECT
                ARRAY_AGG(blk.column_name)
                INTO columnNames
                FROM (SELECT
                    ucc.column_name AS column_name
                    FROM aws_oracle_ext.SYS_USER_CONS_COLUMNS AS ucc
                    WHERE ucc.constraint_name = child_constraint_collection[i].constraint_name AND ucc.column_name NOT IN ('REALM_ID')) AS blk
                WHERE ucc.constraint_name = child_constraint_collection[i].constraint_name AND ucc.column_name NOT IN ('REALM_ID');
            /* DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(1) ); */
            /* identify the rows to be deleted from child table */
            select_stmt_str := CONCAT_WS('', 'SELECT ', aws_oracle_ext.substr(child_constraint_collection[i].table_name, 5), '_SEQ FROM ', child_constraint_collection[i].table_name, ' WHERE ', columnNames[1], '=''', uniqueid, '''');

            FOR j IN 2..COALESCE(array_length(columnNames, 1), 0) LOOP
                /* DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(j) ); */
                select_stmt_str := CONCAT_WS('', select_stmt_str, ' OR ', columnNames[j], '=''', uniqueid, '''');
            END LOOP;
            /* DBMS_OUTPUT.PUT_LINE(select_stmt_str); */
            
            /*
            [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
            EXECUTE IMMEDIATE select_stmt_str BULK COLLECT INTO id_collection
            */
            /* DBMS_OUTPUT.PUT_LINE('Count: '||id_collection.count); */
            IF (COALESCE(array_length(id_collection, 1), 0) > 0) THEN
                FOR j IN 1..COALESCE(array_length(id_collection, 1), 0) LOOP
                    CALL pspadm.prc_remove_company(id_collection[j], child_constraint_collection[i].table_name);
                END LOOP;
            END IF;
            EXCEPTION
                WHEN no_data_found THEN
                    /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION'); */
                    NULL;
                WHEN others THEN
                    /* this is to delete the Association tables data */
                    delete_stmt_str := CONCAT_WS('', 'DELETE FROM ', child_constraint_collection[i].table_name, ' WHERE ', columnNames[1], '=''', uniqueid, '''');

                    FOR j IN 2..COALESCE(array_length(columnNames, 1), 0) LOOP
                        /* DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(j) ); */
                        delete_stmt_str := CONCAT_WS('', delete_stmt_str, ' OR ', columnNames[j], '=''', uniqueid, '''');
                    END LOOP;
                    EXECUTE delete_stmt_str;
        END;
    END LOOP;
    /* identify the rows to be deleted from the parent table, exclude the tables with static data */
    parent_select_stmt_str := CONCAT_WS('', 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints', ' WHERE constraint_name IN (SELECT r_constraint_name FROM user_constraints', ' WHERE table_name = ''', tablename, ''')', 'AND table_name NOT IN', '(''PSP_ACH_TRANSACTION_CODE'', ''PSP_ACTION_EVENT'', ''PSP_AGENCY'', ''PSP_AGENCY_CHECK_LIST_ITEM'', ''PSP_AGENCY_CHECK_LIST_STATUS'', ''PSP_AUTH_DOMAIN'', ''PSP_AUTH_OPERATION'', ''PSP_AUTH_ROLE'', ''PSP_AUTHROLE_OPERATION_ASSOC'', ''PSP_AUTH_USER'',', '''PSP_BANK_HOLIDAY'', ''PSP_COLLECTION_STAGE'', ''PSP_AGENCY_STATUS'', ''PSP_DEPOSIT_FREQUENCY_CODE'', ''PSP_EVENT_TYPE'', ''PSP_FEE'', ''PSP_FINANCIAL_TXN_ACTION'', ''PSP_FORM_TEMPLATE'', ''PSP_FUNDING_MODEL'',', '''PSP_GEMS_UPLOAD_BATCH'', ''PSP_GEMS_LEDGER_POSTING_RULE'', ''PSP_INTUIT_BANK_ACCOUNT'', ''PSP_INTUIT_BANK_ACC_TXN_TYPE'', ''PSP_INTUIT_BA_BT_FT'', ''PSP_LAW'', ''PSP_LEDGER_ACCOUNT'',', '''PSP_LEDGER_ACCOUNT_ACTION'', ''PSP_MONEY_MOVEMENT_TRANSACTION'', ''PSP_NACHAFILE'', ''PSP_OFFER'', ''PSP_OFFERING'', ''PSP_OFFERING_SVCCHG'', ''PSP_OFFERING_SVCCHG_GRP'', ''PSP_OFFERING_SVC_ASSOC'', ''PSP_OFFER_SVCCHG_ASSOC'', ''PSP_OFFLOAD_BATCH'', ''PSP_OFFLOAD_GROUP'', ''PSP_PAYMENT_TEMPLATE'', ''PSP_PMT_TEMPLATE_FREQUENCY'', ''PSP_PAYROLL_FREQUENCY'', ''PSP_PAYROLL_RUN_ACTION'',', '''PSP_POSTING_RULE'', ''PSP_REPORTING_AGENT'', ''PSP_SERVICE'', ''PSP_SERVICE_CHECK_LIST_ITEM'', ''PSP_SERVICE_CHECK_LIST_STATUS'', ''PSP_SERVICE_STATUS'', ''PSP_SVCSTAT_SRCSYS_ASSOC'', ''PSP_SVCSTAT_SVC_ASSOC'', ''PSP_SVCSTAT_SYSCAP_ASSOC'', ''PSP_SVCSTAT_TXNTYPE_ASSOC'', ''PSP_SYSTEM_CAPABILITY'',', '''PSP_SOURCE_PAYROLL_PARAMETER'', ''PSP_SOURCE_SYSTEM'', ''PSP_SYSTEM_PARAMETER'', ''PSP_TRANSACTION_RETURN_BATCH'',''PSP_TRANSACTION_STATE'',', '''PSP_TRANSACTION_TYPE'', ''PSP_COMPANY'', ''PSP_ENTITLEMENT_CODE'')');
    /* DBMS_OUTPUT.PUT_LINE(parent_select_stmt_str); */
    
    /*
    [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
    EXECUTE IMMEDIATE parent_select_stmt_str BULK COLLECT INTO parent_constraint_collection
    */
    /* DBMS_OUTPUT.PUT_LINE('EXECUTED parent select'); */
    FOR j IN 1..COALESCE(array_length(parent_constraint_collection, 1), 0) LOOP
        BEGIN
            /* identify the parent column names */
            SELECT
                ARRAY_AGG(blk.column_name)
                INTO parent_columns
                FROM (SELECT
                    ucc.column_name AS column_name
                    FROM aws_oracle_ext.SYS_USER_CONSTRAINTS AS uc
                    INNER JOIN aws_oracle_ext.SYS_USER_CONS_COLUMNS AS ucc
                        ON ucc.constraint_name = uc.constraint_name
                    WHERE uc.constraint_type = 'R' AND uc.table_name = tablename AND ucc.table_name = tablename AND uc.r_constraint_name = parent_constraint_collection[j].constraint_name AND ucc.column_name NOT IN ('REALM_ID')) AS blk
                WHERE uc.constraint_type = 'R' AND uc.table_name = tablename AND ucc.table_name = tablename AND uc.r_constraint_name = parent_constraint_collection[j].constraint_name AND ucc.column_name NOT IN ('REALM_ID');

            FOR k IN 1..COALESCE(array_length(parent_columns, 1), 0) LOOP
                /* DBMS_OUTPUT.PUT_LINE('Parent Column: ' || parent_columns(k)); */
                select_stmt_str := CONCAT_WS('', 'SELECT ', parent_columns[k], ' FROM ', tablename, ' WHERE ', aws_oracle_ext.substr(tablename, 5), '_SEQ = ''', uniqueid, '''');

                IF (parent_constraint_collection[j].table_name = 'PSP_BANK_ACCOUNT') THEN
                    select_stmt_str := CONCAT_WS('', select_stmt_str, ' AND ', parent_columns[k], ' NOT IN (SELECT BANK_ACCOUNT_FK FROM PSP_INTUIT_BANK_ACCOUNT)');
                END IF;
                /* DBMS_OUTPUT.PUT_LINE(select_stmt_str); */
                EXECUTE select_stmt_str INTO STRICT parent_id;
                /* DBMS_OUTPUT.PUT_LINE(parent_id); */
                /* DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count); */
                /* DBMS_OUTPUT.PUT_LINE('X: ' || x); */

                IF (parent_id IS NOT NULL) THEN
                    parent_recs := aws_oracle_ext.EXTEND(parent_recs);
                    /* DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count); */
                    parent_recs[x] := ROW (parent_constraint_collection[j].table_name, parent_recs[x].uniqueid);
                    parent_recs[x] := ROW (parent_recs[x].tablename, parent_id);
                    /* DBMS_OUTPUT.PUT_LINE('Assigning parent recs: ' || parent_recs(x).tablename); */
                    x := x + 1;
                END IF;
            END LOOP;
            EXCEPTION
                WHEN no_data_found THEN
                    /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION'); */
                    NULL;
                WHEN others THEN
                    /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ PARENT OTHERS EXCEPTION'); */
                    NULL;
        END;
    END LOOP;
    /* delete the current entry */
    delete_stmt_str := CONCAT_WS('', 'DELETE FROM ', tablename, ' WHERE ', aws_oracle_ext.substr(tablename, 5), '_SEQ=''', uniqueid, '''');
    /* DBMS_OUTPUT.PUT_LINE(delete_stmt_str); */
    EXECUTE delete_stmt_str;
    /* DBMS_OUTPUT.PUT_LINE('#### ' || parent_recs.count); */
    /* delete the entries from parent table */

    FOR l IN 1..COALESCE(array_length(parent_recs, 1), 0) LOOP
        CALL pspadm.prc_remove_company(parent_recs[l].uniqueid, parent_recs[l].tablename);
    END LOOP;
    EXCEPTION
        WHEN no_data_found THEN
            /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION'); */
            RETURN;
        WHEN others THEN
            /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ OUTER OTHERS EXCEPTION'); */
            RETURN;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_remove_company2(IN uniqueid TEXT, IN tablename TEXT)
AS 
$BODY$
DECLARE
    UniqueIdCollection CHARACTER VARYING(1020) [];
    ParentColumnNames CHARACTER VARYING(50) [];
    id_collection UniqueIdCollection%TYPE;
    c1 CURSOR FOR
    SELECT
        table_name, constraint_name, constraint_type, r_constraint_name
        FROM aws_oracle_ext.SYS_USER_CONSTRAINTS;
    c1$ATTRIBUTES aws_oracle_data.TCursorAttributes := ROW (FALSE, NULL, NULL, NULL);
    UserConstraints pspadm.prc_remove_company2$c1 [];
    child_constraint_collection UserConstraints%TYPE;
    parent_constraint_collection UserConstraints%TYPE;
    ParentRecs pspadm.prc_remove_company2$parentrec [];
    parent_recs ParentRecs%TYPE;
    ColumnNamesList CHARACTER VARYING(50) [];
    columnNames ColumnNamesList%TYPE;
    parent_id CHARACTER VARYING(1020);
    parent_columns ParentColumnNames%TYPE;
    delete_stmt_str CHARACTER VARYING(200);
    child_select_stmt_str CHARACTER VARYING(1000);
    parent_select_stmt_str CHARACTER VARYING(3000);
    select_stmt_str CHARACTER VARYING(300);
    /* delete_stmt_str VARCHAR2(300); */
    x DOUBLE PRECISION := 1;
BEGIN
    child_select_stmt_str := CONCAT_WS('', 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints', ' WHERE r_constraint_name IN (SELECT constraint_name FROM user_constraints ', 'WHERE table_name = ''', tablename, ''')', 'AND table_name NOT IN', '(''PSP_COMPANY'')');
    /* DBMS_OUTPUT.PUT_LINE(child_select_stmt_str); */
    
    /*
    [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
    EXECUTE IMMEDIATE child_select_stmt_str BULK COLLECT INTO child_constraint_collection
    */
    FOR i IN 1..COALESCE(array_length(child_constraint_collection, 1), 0) LOOP
        BEGIN
            /* DBMS_OUTPUT.PUT_LINE(child_constraint_collection(i).table_name); */
            /* DBMS_OUTPUT.PUT_LINE(child_constraint_collection(i).constraint_name); */
            SELECT
                ARRAY_AGG(blk.column_name)
                INTO columnNames
                FROM (SELECT
                    ucc.column_name AS column_name
                    FROM aws_oracle_ext.SYS_USER_CONS_COLUMNS AS ucc
                    WHERE ucc.constraint_name = child_constraint_collection[i].constraint_name AND ucc.column_name NOT IN ('REALM_ID')) AS blk
                WHERE ucc.constraint_name = child_constraint_collection[i].constraint_name AND ucc.column_name NOT IN ('REALM_ID');
            /* DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(1) ); */
            /* identify the rows to be deleted from child table */
            select_stmt_str := CONCAT_WS('', 'SELECT ', aws_oracle_ext.substr(child_constraint_collection[i].table_name, 5), '_SEQ FROM ', child_constraint_collection[i].table_name, ' WHERE ', columnNames[1], '=''', uniqueid, '''');

            FOR j IN 2..COALESCE(array_length(columnNames, 1), 0) LOOP
                /* DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(j) ); */
                select_stmt_str := CONCAT_WS('', select_stmt_str, ' OR ', columnNames[j], '=''', uniqueid, '''');
            END LOOP;
            /* DBMS_OUTPUT.PUT_LINE(select_stmt_str); */
            
            /*
            [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
            EXECUTE IMMEDIATE select_stmt_str BULK COLLECT INTO id_collection
            */
            /* DBMS_OUTPUT.PUT_LINE('Count: '||id_collection.count); */
            IF (COALESCE(array_length(id_collection, 1), 0) > 0) THEN
                FOR j IN 1..COALESCE(array_length(id_collection, 1), 0) LOOP
                    CALL pspadm.prc_remove_company(id_collection[j], child_constraint_collection[i].table_name);
                END LOOP;
            END IF;
            EXCEPTION
                WHEN no_data_found THEN
                    /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION'); */
                    NULL;
                WHEN others THEN
                    /* this is to delete the Association tables data */
                    delete_stmt_str := CONCAT_WS('', 'DELETE FROM ', child_constraint_collection[i].table_name, ' WHERE ', columnNames[1], '=''', uniqueid, '''');

                    FOR j IN 2..COALESCE(array_length(columnNames, 1), 0) LOOP
                        /* DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(j) ); */
                        delete_stmt_str := CONCAT_WS('', delete_stmt_str, ' OR ', columnNames[j], '=''', uniqueid, '''');
                    END LOOP;
                    EXECUTE delete_stmt_str;
        END;
    END LOOP;
    /* identify the rows to be deleted from the parent table, exclude the tables with static data */
    parent_select_stmt_str := CONCAT_WS('', 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints', ' WHERE constraint_name IN (SELECT r_constraint_name FROM user_constraints', ' WHERE table_name = ''', tablename, ''')', 'AND table_name NOT IN', '(''PSP_ACH_TRANSACTION_CODE'', ''PSP_ACTION_EVENT'', ''PSP_AGENCY'', ''PSP_AGENCY_CHECK_LIST_ITEM'', ''PSP_AGENCY_CHECK_LIST_STATUS'', ''PSP_AUTH_DOMAIN'', ''PSP_AUTH_OPERATION'', ''PSP_AUTH_ROLE'', ''PSP_AUTHROLE_OPERATION_ASSOC'', ''PSP_AUTH_USER'',', '''PSP_BANK_HOLIDAY'', ''PSP_COLLECTION_STAGE'', ''PSP_AGENCY_STAT_TXN_TYPE'', ''PSP_AGENCY_STATUS'', ''PSP_DEPOSIT_FREQUENCY_CODE'', ''PSP_EVENT_TYPE'', ''PSP_FEE'', ''PSP_FINANCIAL_TXN_ACTION'', ''PSP_FORM_TEMPLATE'', ''PSP_FUNDING_MODEL'',', '''PSP_GEMS_UPLOAD_BATCH'', ''PSP_GEMS_LEDGER_POSTING_RULE'', ''PSP_INTUIT_BANK_ACCOUNT'', ''PSP_INTUIT_BANK_ACC_TXN_TYPE'', ''PSP_INTUIT_BA_BT_FT'', ''PSP_LAW'', ''PSP_LEDGER_ACCOUNT'',', '''PSP_LEDGER_ACCOUNT_ACTION'', ''PSP_MONEY_MOVEMENT_TRANSACTION'', ''PSP_NACHAFILE'', ''PSP_OFFER'', ''PSP_OFFERING'', ''PSP_OFFERING_SVCCHG'', ''PSP_OFFERING_SVCCHG_GRP'', ''PSP_OFFERING_SVC_ASSOC'', ''PSP_OFFER_SVCCHG_ASSOC'', ''PSP_OFFLOAD_BATCH'', ''PSP_OFFLOAD_GROUP'', ''PSP_PAYMENT_TEMPLATE'', ''PSP_PMT_TEMPLATE_FREQUENCY'', ''PSP_PAYROLL_FREQUENCY'', ''PSP_PAYROLL_RUN_ACTION'',', '''PSP_POSTING_RULE'', ''PSP_REPORTING_AGENT'', ''PSP_SERVICE'', ''PSP_SERVICE_CHECK_LIST_ITEM'', ''PSP_SERVICE_CHECK_LIST_STATUS'', ''PSP_SERVICE_STATUS'', ''PSP_SVCSTAT_SRCSYS_ASSOC'', ''PSP_SVCSTAT_SVC_ASSOC'', ''PSP_SVCSTAT_SYSCAP_ASSOC'', ''PSP_SVCSTAT_TXNTYPE_ASSOC'', ''PSP_SYSTEM_CAPABILITY'',', '''PSP_SOURCE_PAYROLL_PARAMETER'', ''PSP_SOURCE_SYSTEM'', ''PSP_SYSTEM_PARAMETER'', ''PSP_TRANSACTION_RETURN_BATCH'',''PSP_TRANSACTION_STATE'',', '''PSP_TRANSACTION_TYPE'', ''PSP_COMPANY'')');
    /* DBMS_OUTPUT.PUT_LINE(parent_select_stmt_str); */
    
    /*
    [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
    EXECUTE IMMEDIATE parent_select_stmt_str BULK COLLECT INTO parent_constraint_collection
    */
    /* DBMS_OUTPUT.PUT_LINE('EXECUTED parent select'); */
    FOR j IN 1..COALESCE(array_length(parent_constraint_collection, 1), 0) LOOP
        BEGIN
            /* identify the parent column names */
            SELECT
                ARRAY_AGG(blk.column_name)
                INTO parent_columns
                FROM (SELECT
                    ucc.column_name AS column_name
                    FROM aws_oracle_ext.SYS_USER_CONSTRAINTS AS uc
                    INNER JOIN aws_oracle_ext.SYS_USER_CONS_COLUMNS AS ucc
                        ON ucc.constraint_name = uc.constraint_name
                    WHERE uc.constraint_type = 'R' AND uc.table_name = tablename AND ucc.table_name = tablename AND uc.r_constraint_name = parent_constraint_collection[j].constraint_name AND ucc.column_name NOT IN ('REALM_ID')) AS blk
                WHERE uc.constraint_type = 'R' AND uc.table_name = tablename AND ucc.table_name = tablename AND uc.r_constraint_name = parent_constraint_collection[j].constraint_name AND ucc.column_name NOT IN ('REALM_ID');

            FOR k IN 1..COALESCE(array_length(parent_columns, 1), 0) LOOP
                /* DBMS_OUTPUT.PUT_LINE('Parent Column: ' || parent_columns(k)); */
                select_stmt_str := CONCAT_WS('', 'SELECT ', parent_columns[k], ' FROM ', tablename, ' WHERE ', aws_oracle_ext.substr(tablename, 5), '_SEQ = ''', uniqueid, '''');

                IF (parent_constraint_collection[j].table_name = 'PSP_BANK_ACCOUNT') THEN
                    select_stmt_str := CONCAT_WS('', select_stmt_str, ' AND ', parent_columns[k], ' NOT IN (SELECT BANK_ACCOUNT_FK FROM PSP_INTUIT_BANK_ACCOUNT)');
                END IF;
                /* DBMS_OUTPUT.PUT_LINE(select_stmt_str); */
                EXECUTE select_stmt_str INTO STRICT parent_id;
                /* DBMS_OUTPUT.PUT_LINE(parent_id); */
                /* DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count); */
                /* DBMS_OUTPUT.PUT_LINE('X: ' || x); */

                IF (parent_id IS NOT NULL) THEN
                    parent_recs := aws_oracle_ext.EXTEND(parent_recs);
                    /* DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count); */
                    parent_recs[x] := ROW (parent_constraint_collection[j].table_name, parent_recs[x].uniqueid);
                    parent_recs[x] := ROW (parent_recs[x].tablename, parent_id);
                    /* DBMS_OUTPUT.PUT_LINE('Assigning parent recs: ' || parent_recs(x).tablename); */
                    x := x + 1;
                END IF;
            END LOOP;
            EXCEPTION
                WHEN no_data_found THEN
                    /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION'); */
                    NULL;
                WHEN others THEN
                    /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ PARENT OTHERS EXCEPTION'); */
                    NULL;
        END;
    END LOOP;
    /* delete the current entry */
    delete_stmt_str := CONCAT_WS('', 'DELETE FROM ', tablename, ' WHERE ', aws_oracle_ext.substr(tablename, 5), '_SEQ=''', uniqueid, '''');
    RAISE DEBUG USING MESSAGE = COALESCE(delete_stmt_str::TEXT, '');
    EXECUTE delete_stmt_str;
    /* DBMS_OUTPUT.PUT_LINE('#### ' || parent_recs.count); */
    /* delete the entries from parent table */

    FOR l IN 1..COALESCE(array_length(parent_recs, 1), 0) LOOP
        CALL pspadm.prc_remove_company2(parent_recs[l].uniqueid, parent_recs[l].tablename);
    END LOOP;
    EXCEPTION
        WHEN no_data_found THEN
            /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION'); */
            RETURN;
        WHEN others THEN
            /* DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ OUTER OTHERS EXCEPTION'); */
            RETURN;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_remove_company_fast(IN uniqueid TEXT)
AS 
$BODY$
BEGIN
    /* Company -> AnnualBillingItem */
    DELETE FROM pspadm.psp_annual_billing_item
        WHERE company_fk IN (uniqueid);
    /* Company -> ATFPaymentsToProcess */
    DELETE FROM pspadm.psp_atfpayments_to_process
        WHERE company_fk IN (uniqueid);
    /* Company -> CheckPrintPaycheck */
    DELETE FROM pspadm.psp_check_print_paycheck
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE original_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE original_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE original_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE original_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE original_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE original_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE original_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE qbdt_payroll_transaction_fk IN (SELECT
                qbdt_payroll_transaction_seq
                FROM pspadm.psp_qbdt_payroll_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE original_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE original_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction */
    DELETE FROM pspadm.psp_qbdt_payroll_transaction
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE void_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE original_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE original_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE comp_adjust_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE qbdt_payroll_transaction_fk IN (SELECT
                qbdt_payroll_transaction_seq
                FROM pspadm.psp_qbdt_payroll_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction */
    DELETE FROM pspadm.psp_qbdt_payroll_transaction
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE original_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE void_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE void_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE void_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE void_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE void_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE comp_adjust_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE void_submission_fk IN (SELECT
                            comp_adjust_submission_seq
                            FROM pspadm.psp_comp_adjust_submission
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE qbdt_payroll_transaction_fk IN (SELECT
                qbdt_payroll_transaction_seq
                FROM pspadm.psp_qbdt_payroll_transaction
                WHERE comp_adjust_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE void_submission_fk IN (SELECT
                        comp_adjust_submission_seq
                        FROM pspadm.psp_comp_adjust_submission
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE comp_adjust_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE void_submission_fk IN (SELECT
                    comp_adjust_submission_seq
                    FROM pspadm.psp_comp_adjust_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtPayrollTransaction */
    DELETE FROM pspadm.psp_qbdt_payroll_transaction
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE void_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE void_submission_fk IN (SELECT
                comp_adjust_submission_seq
                FROM pspadm.psp_comp_adjust_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAdjustmentSubmission -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE void_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE comp_adjust_submission_fk IN (SELECT
            comp_adjust_submission_seq
            FROM pspadm.psp_comp_adjust_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAdjustmentSubmission */
    DELETE FROM pspadm.psp_comp_adjust_submission
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyAgency -> ACHEnrollment -> ACHEnrollmentDetail */
    DELETE FROM pspadm.psp_achenrollment_detail
        WHERE a_c_h_enrollment_fk IN (SELECT
            achenrollment_seq
            FROM pspadm.psp_achenrollment
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> ACHEnrollment */
    DELETE FROM pspadm.psp_achenrollment
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> CompanyLaw -> PayrollItemTaxableTo */
    DELETE FROM pspadm.psp_payroll_item_taxable_to
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> EmployeeLawQtrTotals */
    DELETE FROM pspadm.psp_employee_law_qtr_totals
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> EmployeeW2Totals */
    DELETE FROM pspadm.psp_employee_w2_totals
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> LiabilityCheckLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_line_fk IN (SELECT
            liability_check_line_seq
            FROM pspadm.psp_liability_check_line
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> LiabilityCheckLine */
    DELETE FROM pspadm.psp_liability_check_line
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLawRate */
    DELETE FROM pspadm.psp_company_law_rate
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> PayrollItemTaxableTo */
    DELETE FROM pspadm.psp_payroll_item_taxable_to
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeLawQtrTotals */
    DELETE FROM pspadm.psp_employee_law_qtr_totals
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeW2Totals */
    DELETE FROM pspadm.psp_employee_w2_totals
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityCheckLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_line_fk IN (SELECT
            liability_check_line_seq
            FROM pspadm.psp_liability_check_line
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> LiabilityCheckLine */
    DELETE FROM pspadm.psp_liability_check_line
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> CompanyLawRate */
    DELETE FROM pspadm.psp_company_law_rate
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> CompanyLaw */
    DELETE FROM pspadm.psp_company_law
        WHERE additional_company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeTax -> TaxTableMiscData */
    DELETE FROM pspadm.psp_tax_table_misc_data
        WHERE employee_tax_fk IN (SELECT
            employee_tax_seq
            FROM pspadm.psp_employee_tax
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE additional_company_law_fk IN (SELECT
                    company_law_seq
                    FROM pspadm.psp_company_law
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> EmployeeTax */
    DELETE FROM pspadm.psp_employee_tax
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw -> QbdtPayrollItemInfo */
    DELETE FROM pspadm.psp_qbdt_payroll_item_info
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE additional_company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> CompanyLaw */
    DELETE FROM pspadm.psp_company_law
        WHERE additional_company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> EmployeeTax -> TaxTableMiscData */
    DELETE FROM pspadm.psp_tax_table_misc_data
        WHERE employee_tax_fk IN (SELECT
            employee_tax_seq
            FROM pspadm.psp_employee_tax
            WHERE company_law_fk IN (SELECT
                company_law_seq
                FROM pspadm.psp_company_law
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> CompanyLaw -> EmployeeTax */
    DELETE FROM pspadm.psp_employee_tax
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw -> QbdtPayrollItemInfo */
    DELETE FROM pspadm.psp_qbdt_payroll_item_info
        WHERE company_law_fk IN (SELECT
            company_law_seq
            FROM pspadm.psp_company_law
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyLaw */
    DELETE FROM pspadm.psp_company_law
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> CompanyAgencyFormTemplate */
    DELETE FROM pspadm.psp_companyagency_frmtemplate
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE tax_penalty_interest_fk IN (SELECT
                tax_penalty_interest_seq
                FROM pspadm.psp_tax_penalty_interest
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE tax_penalty_interest_fk IN (SELECT
                tax_penalty_interest_seq
                FROM pspadm.psp_tax_penalty_interest
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE tax_penalty_interest_fk IN (SELECT
                    tax_penalty_interest_seq
                    FROM pspadm.psp_tax_penalty_interest
                    WHERE company_agency_fk IN (SELECT
                        company_agency_seq
                        FROM pspadm.psp_company_agency
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE tax_penalty_interest_fk IN (SELECT
                tax_penalty_interest_seq
                FROM pspadm.psp_tax_penalty_interest
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE tax_penalty_interest_fk IN (SELECT
                tax_penalty_interest_seq
                FROM pspadm.psp_tax_penalty_interest
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE tax_penalty_interest_fk IN (SELECT
                tax_penalty_interest_seq
                FROM pspadm.psp_tax_penalty_interest
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE tax_penalty_interest_fk IN (SELECT
                tax_penalty_interest_seq
                FROM pspadm.psp_tax_penalty_interest
                WHERE company_agency_fk IN (SELECT
                    company_agency_seq
                    FROM pspadm.psp_company_agency
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyAgency -> TaxPenaltyInterest -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE tax_penalty_interest_fk IN (SELECT
            tax_penalty_interest_seq
            FROM pspadm.psp_tax_penalty_interest
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> TaxPenaltyInterest */
    DELETE FROM pspadm.psp_tax_penalty_interest
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> RAFEnrollment -> RAFEnrollmentDetail */
    DELETE FROM pspadm.psp_rafenrollment_detail
        WHERE r_a_f_enrollment_fk IN (SELECT
            rafenrollment_seq
            FROM pspadm.psp_rafenrollment
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> RAFEnrollment */
    DELETE FROM pspadm.psp_rafenrollment
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> EftpsEnrollment -> EftpsEnrollmentDetail */
    DELETE FROM pspadm.psp_eftps_enrollment_detail
        WHERE eftps_enrollment_fk IN (SELECT
            eftps_enrollment_seq
            FROM pspadm.psp_eftps_enrollment
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> EftpsEnrollment */
    DELETE FROM pspadm.psp_eftps_enrollment
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> CompanyRateRequest */
    DELETE FROM pspadm.psp_company_rate_request
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> CompanyFilingAmount */
    DELETE FROM pspadm.psp_company_filing_amount
        WHERE company_agency_pmt_template_fk IN (SELECT
            companyagency_pmttemplate_seq
            FROM pspadm.psp_companyagency_pmttemplate
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> CompanyPaymentTemplateAgencyId */
    DELETE FROM pspadm.psp_comp_pmt_template_agencyid
        WHERE company_agency_pmt_template_fk IN (SELECT
            companyagency_pmttemplate_seq
            FROM pspadm.psp_companyagency_pmttemplate
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> EffectiveDepositFrequency */
    DELETE FROM pspadm.psp_effective_deposit_freq
        WHERE company_agency_pmt_template_fk IN (SELECT
            companyagency_pmttemplate_seq
            FROM pspadm.psp_companyagency_pmttemplate
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyAgencyPaymentTemplate -> CompanyPaymentTemplatePaymentMethod */
    DELETE FROM pspadm.psp_comp_pmttemplate_pmtmethod
        WHERE company_agency_pmt_template_fk IN (SELECT
            companyagency_pmttemplate_seq
            FROM pspadm.psp_companyagency_pmttemplate
            WHERE company_agency_fk IN (SELECT
                company_agency_seq
                FROM pspadm.psp_company_agency
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyAgency -> CompanyAgencyPaymentTemplate */
    DELETE FROM pspadm.psp_companyagency_pmttemplate
        WHERE company_agency_fk IN (SELECT
            company_agency_seq
            FROM pspadm.psp_company_agency
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyAgency */
    DELETE FROM pspadm.psp_company_agency
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyDailyLiability */
    DELETE FROM pspadm.psp_company_daily_liability
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyEvent -> CompanyEventEmail -> CompanyEventEmailParam */
    DELETE FROM pspadm.psp_company_event_email_param
        WHERE company_event_email_fk IN (SELECT
            company_event_email_seq
            FROM pspadm.psp_company_event_email
            WHERE company_event_fk IN (SELECT
                company_event_seq
                FROM pspadm.psp_company_event
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyEvent -> CompanyEventEmail */
    DELETE FROM pspadm.psp_company_event_email
        WHERE company_event_fk IN (SELECT
            company_event_seq
            FROM pspadm.psp_company_event
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyEvent -> CompanyEventDetail */
    DELETE FROM pspadm.psp_company_event_detail
        WHERE company_event_fk IN (SELECT
            company_event_seq
            FROM pspadm.psp_company_event
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyEvent -> CompanyNote */
    DELETE FROM pspadm.psp_company_note
        WHERE company_event_fk IN (SELECT
            company_event_seq
            FROM pspadm.psp_company_event
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyEvent -> FraudEvent */
    DELETE FROM pspadm.psp_fraud_event
        WHERE company_event_fk IN (SELECT
            company_event_seq
            FROM pspadm.psp_company_event
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyEvent -> EventAs400Sync */
    DELETE FROM pspadm.psp_event_as400_sync
        WHERE company_event_fk IN (SELECT
            company_event_seq
            FROM pspadm.psp_company_event
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyEvent */
    DELETE FROM pspadm.psp_company_event
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyEventDetail */
    DELETE FROM pspadm.psp_company_event_detail
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyEventEmailParam */
    DELETE FROM pspadm.psp_company_event_email_param
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyOffer */
    DELETE FROM pspadm.psp_company_offer
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyOffering */
    DELETE FROM pspadm.psp_company_offering
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyPaycheckBatch -> CheckPrintPaycheck */
    DELETE FROM pspadm.psp_check_print_paycheck
        WHERE company_paycheck_batch_fk IN (SELECT
            company_paycheck_batch_seq
            FROM pspadm.psp_company_paycheck_batch
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPaycheckBatch */
    DELETE FROM pspadm.psp_company_paycheck_batch
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyPayrollItem -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> EmployeeW2Totals */
    DELETE FROM pspadm.psp_employee_w2_totals
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> LiabilityCheckLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_line_fk IN (SELECT
            liability_check_line_seq
            FROM pspadm.psp_liability_check_line
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> LiabilityCheckLine */
    DELETE FROM pspadm.psp_liability_check_line
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> PayrollItemTaxableTo */
    DELETE FROM pspadm.psp_payroll_item_taxable_to
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> EmployeePayrollItem */
    DELETE FROM pspadm.psp_employee_payroll_item
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> EmployeePayrollItemQtrTotals */
    DELETE FROM pspadm.psp_ee_payrollitem_qtrtotals
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE additional_payroll_item_fk IN (SELECT
                    company_payroll_item_seq
                    FROM pspadm.psp_company_payroll_item
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployeeW2Totals */
    DELETE FROM pspadm.psp_employee_w2_totals
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE additional_payroll_item_fk IN (SELECT
                    company_payroll_item_seq
                    FROM pspadm.psp_company_payroll_item
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> LiabilityCheckLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_line_fk IN (SELECT
            liability_check_line_seq
            FROM pspadm.psp_liability_check_line
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE additional_payroll_item_fk IN (SELECT
                    company_payroll_item_seq
                    FROM pspadm.psp_company_payroll_item
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> LiabilityCheckLine */
    DELETE FROM pspadm.psp_liability_check_line
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> PayrollItemTaxableTo */
    DELETE FROM pspadm.psp_payroll_item_taxable_to
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployeePayrollItem */
    DELETE FROM pspadm.psp_employee_payroll_item
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployeePayrollItemQtrTotals */
    DELETE FROM pspadm.psp_ee_payrollitem_qtrtotals
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> CompanyPayrollItem */
    DELETE FROM pspadm.psp_company_payroll_item
        WHERE additional_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE additional_payroll_item_fk IN (SELECT
                    company_payroll_item_seq
                    FROM pspadm.psp_company_payroll_item
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> QbdtPayrollItemInfo */
    DELETE FROM pspadm.psp_qbdt_payroll_item_info
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE additional_payroll_item_fk IN (SELECT
                    company_payroll_item_seq
                    FROM pspadm.psp_company_payroll_item
                    WHERE company_fk IN (uniqueid))));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE additional_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> CompanyPayrollItem */
    DELETE FROM pspadm.psp_company_payroll_item
        WHERE additional_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> QbdtPayrollItemInfo */
    DELETE FROM pspadm.psp_qbdt_payroll_item_info
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE company_payroll_item_fk IN (SELECT
                company_payroll_item_seq
                FROM pspadm.psp_company_payroll_item
                WHERE company_fk IN (uniqueid)));
    /* Company -> CompanyPayrollItem -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE company_payroll_item_fk IN (SELECT
            company_payroll_item_seq
            FROM pspadm.psp_company_payroll_item
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyPayrollItem */
    DELETE FROM pspadm.psp_company_payroll_item
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyPIN */
    DELETE FROM pspadm.psp_company_pin
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyTFSSubmission */
    DELETE FROM pspadm.psp_company_tfssubmission
        WHERE company_fk IN (uniqueid);
    /* Company -> DepositFrequencyFileRec */
    DELETE FROM pspadm.psp_deposit_frequency_file_rec
        WHERE company_fk IN (uniqueid);
    /* Company -> DisburseAdvice -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab */
    DELETE FROM pspadm.psp_disburse_advice_tax_liab
        WHERE tips_liability_fk IN (SELECT
            disburse_advice_tax_liab_seq
            FROM pspadm.psp_disburse_advice_tax_liab
            WHERE tips_liability_fk IN (SELECT
                disburse_advice_tax_liab_seq
                FROM pspadm.psp_disburse_advice_tax_liab
                WHERE disburse_advice_fk IN (SELECT
                    disburse_advice_seq
                    FROM pspadm.psp_disburse_advice
                    WHERE company_fk IN (uniqueid))));
    /* Company -> DisburseAdvice -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab */
    DELETE FROM pspadm.psp_disburse_advice_tax_liab
        WHERE tips_liability_fk IN (SELECT
            disburse_advice_tax_liab_seq
            FROM pspadm.psp_disburse_advice_tax_liab
            WHERE disburse_advice_fk IN (SELECT
                disburse_advice_seq
                FROM pspadm.psp_disburse_advice
                WHERE company_fk IN (uniqueid)));
    /* Company -> DisburseAdvice -> DisburseAdviceTaxLiab */
    DELETE FROM pspadm.psp_disburse_advice_tax_liab
        WHERE disburse_advice_fk IN (SELECT
            disburse_advice_seq
            FROM pspadm.psp_disburse_advice
            WHERE company_fk IN (uniqueid));
    /* Company -> DisburseAdvice */
    DELETE FROM pspadm.psp_disburse_advice
        WHERE company_fk IN (uniqueid);
    /* Company -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab */
    DELETE FROM pspadm.psp_disburse_advice_tax_liab
        WHERE tips_liability_fk IN (SELECT
            disburse_advice_tax_liab_seq
            FROM pspadm.psp_disburse_advice_tax_liab
            WHERE tips_liability_fk IN (SELECT
                disburse_advice_tax_liab_seq
                FROM pspadm.psp_disburse_advice_tax_liab
                WHERE company_fk IN (uniqueid)));
    /* Company -> DisburseAdviceTaxLiab -> DisburseAdviceTaxLiab */
    DELETE FROM pspadm.psp_disburse_advice_tax_liab
        WHERE tips_liability_fk IN (SELECT
            disburse_advice_tax_liab_seq
            FROM pspadm.psp_disburse_advice_tax_liab
            WHERE company_fk IN (uniqueid));
    /* Company -> DisburseAdviceTaxLiab */
    DELETE FROM pspadm.psp_disburse_advice_tax_liab
        WHERE company_fk IN (uniqueid);
    /* Company -> EmployeeLawQtrTotals */
    DELETE FROM pspadm.psp_employee_law_qtr_totals
        WHERE company_fk IN (uniqueid);
    /* Company -> EmployeeW2Totals */
    DELETE FROM pspadm.psp_employee_w2_totals
        WHERE company_fk IN (uniqueid);
    /* Company -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE company_fk IN (uniqueid));
    /* Company -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE company_fk IN (uniqueid);
    /* Company -> EmpTotalsPayrollRun */
    DELETE FROM pspadm.psp_emp_totals_payroll_run
        WHERE company_fk IN (uniqueid);
    /* Company -> EntitlementUnit */
    DELETE FROM pspadm.psp_entitlement_unit
        WHERE company_fk IN (uniqueid);
    /* Company -> EntityChange */
    DELETE FROM pspadm.psp_entity_change
        WHERE company_fk IN (uniqueid);
    /* Company -> EntryDetailRecord */
    DELETE FROM pspadm.psp_entry_detail_record
        WHERE company_fk IN (uniqueid);
    /* Company -> FraudAddress */
    DELETE FROM pspadm.psp_fraud_address
        WHERE company_fk IN (uniqueid);
    /* Company -> FraudBankAccount */
    DELETE FROM pspadm.psp_fraud_bank_account
        WHERE company_fk IN (uniqueid);
    /* Company -> FraudCompany */
    DELETE FROM pspadm.psp_fraud_company
        WHERE company_fk IN (uniqueid);
    /* Company -> FraudContact */
    DELETE FROM pspadm.psp_fraud_contact
        WHERE company_fk IN (uniqueid);
    /* Company -> FraudEvent */
    DELETE FROM pspadm.psp_fraud_event
        WHERE company_fk IN (uniqueid);
    /* Company -> LedgerBalance */
    DELETE FROM pspadm.psp_ledger_balance
        WHERE company_fk IN (uniqueid);
    /* Company -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE company_fk IN (uniqueid));
    /* Company -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE company_fk IN (uniqueid));
    /* Company -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE company_fk IN (uniqueid);
    /* Company -> LiabilityCheck -> LiabilityCheckBillingDetailAssoc */
    DELETE FROM pspadm.psp_liab_check_billing_assoc
        WHERE liability_check_fk IN (SELECT
            liability_check_seq
            FROM pspadm.psp_liability_check
            WHERE company_fk IN (uniqueid));
    /* Company -> LiabilityCheck -> LiabilityCheckLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_line_fk IN (SELECT
            liability_check_line_seq
            FROM pspadm.psp_liability_check_line
            WHERE liability_check_fk IN (SELECT
                liability_check_seq
                FROM pspadm.psp_liability_check
                WHERE company_fk IN (uniqueid)));
    /* Company -> LiabilityCheck -> LiabilityCheckLine */
    DELETE FROM pspadm.psp_liability_check_line
        WHERE liability_check_fk IN (SELECT
            liability_check_seq
            FROM pspadm.psp_liability_check
            WHERE company_fk IN (uniqueid));
    /* Company -> LiabilityCheck -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_fk IN (SELECT
            liability_check_seq
            FROM pspadm.psp_liability_check
            WHERE company_fk IN (uniqueid));
    /* Company -> LiabilityCheck */
    DELETE FROM pspadm.psp_liability_check
        WHERE company_fk IN (uniqueid);
    /* Company -> OnHoldReason -> FinancialTransaction */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE on_hold_reason_fk IN (SELECT
            on_hold_reason_seq
            FROM pspadm.psp_on_hold_reason
            WHERE company_fk IN (uniqueid));
    /* Company -> OnHoldReason */
    DELETE FROM pspadm.psp_on_hold_reason
        WHERE company_fk IN (uniqueid);
    /* Company -> PaycheckUsage -> PaycheckUsageHist */
    DELETE FROM pspadm.psp_paycheck_usage_hist
        WHERE paycheck_usage_fk IN (SELECT
            paycheck_usage_seq
            FROM pspadm.psp_paycheck_usage
            WHERE company_fk IN (uniqueid));
    /* Company -> PaycheckUsage */
    DELETE FROM pspadm.psp_paycheck_usage
        WHERE company_fk IN (uniqueid);
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE bill_payment_split_fk IN (SELECT
            bill_payment_split_seq
            FROM pspadm.psp_bill_payment_split
            WHERE bill_payment_fk IN (SELECT
                bill_payment_seq
                FROM pspadm.psp_bill_payment
                WHERE payee_fk IN (SELECT
                    payee_seq
                    FROM pspadm.psp_payee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Payee -> BillPayment -> BillPaymentSplit */
    DELETE FROM pspadm.psp_bill_payment_split
        WHERE bill_payment_fk IN (SELECT
            bill_payment_seq
            FROM pspadm.psp_bill_payment
            WHERE payee_fk IN (SELECT
                payee_seq
                FROM pspadm.psp_payee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Payee -> BillPayment */
    DELETE FROM pspadm.psp_bill_payment
        WHERE payee_fk IN (SELECT
            payee_seq
            FROM pspadm.psp_payee
            WHERE company_fk IN (uniqueid));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE payee_bank_account_fk IN (SELECT
                    payee_bank_account_seq
                    FROM pspadm.psp_payee_bank_account
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE payee_bank_account_fk IN (SELECT
                    payee_bank_account_seq
                    FROM pspadm.psp_payee_bank_account
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE payee_bank_account_fk IN (SELECT
                        payee_bank_account_seq
                        FROM pspadm.psp_payee_bank_account
                        WHERE payee_fk IN (SELECT
                            payee_seq
                            FROM pspadm.psp_payee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE payee_bank_account_fk IN (SELECT
                    payee_bank_account_seq
                    FROM pspadm.psp_payee_bank_account
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE payee_bank_account_fk IN (SELECT
                    payee_bank_account_seq
                    FROM pspadm.psp_payee_bank_account
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE payee_bank_account_fk IN (SELECT
                    payee_bank_account_seq
                    FROM pspadm.psp_payee_bank_account
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE payee_bank_account_fk IN (SELECT
                    payee_bank_account_seq
                    FROM pspadm.psp_payee_bank_account
                    WHERE payee_fk IN (SELECT
                        payee_seq
                        FROM pspadm.psp_payee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE bill_payment_split_fk IN (SELECT
            bill_payment_split_seq
            FROM pspadm.psp_bill_payment_split
            WHERE payee_bank_account_fk IN (SELECT
                payee_bank_account_seq
                FROM pspadm.psp_payee_bank_account
                WHERE payee_fk IN (SELECT
                    payee_seq
                    FROM pspadm.psp_payee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Payee -> PayeeBankAccount -> BillPaymentSplit */
    DELETE FROM pspadm.psp_bill_payment_split
        WHERE payee_bank_account_fk IN (SELECT
            payee_bank_account_seq
            FROM pspadm.psp_payee_bank_account
            WHERE payee_fk IN (SELECT
                payee_seq
                FROM pspadm.psp_payee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Payee -> PayeeBankAccount */
    DELETE FROM pspadm.psp_payee_bank_account
        WHERE payee_fk IN (SELECT
            payee_seq
            FROM pspadm.psp_payee
            WHERE company_fk IN (uniqueid));
    /* Company -> Payee */
    DELETE FROM pspadm.psp_payee
        WHERE company_fk IN (uniqueid);
    /* Company -> PriorPaymentSubmission -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE prior_payment_submission_fk IN (SELECT
            prior_payment_submission_seq
            FROM pspadm.psp_prior_payment_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> PriorPaymentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE qbdt_payroll_transaction_fk IN (SELECT
                qbdt_payroll_transaction_seq
                FROM pspadm.psp_qbdt_payroll_transaction
                WHERE prior_payment_submission_fk IN (SELECT
                    prior_payment_submission_seq
                    FROM pspadm.psp_prior_payment_submission
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PriorPaymentSubmission -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE prior_payment_submission_fk IN (SELECT
                prior_payment_submission_seq
                FROM pspadm.psp_prior_payment_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> PriorPaymentSubmission -> QbdtPayrollTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE prior_payment_submission_fk IN (SELECT
                prior_payment_submission_seq
                FROM pspadm.psp_prior_payment_submission
                WHERE company_fk IN (uniqueid)));
    /* Company -> PriorPaymentSubmission -> QbdtPayrollTransaction */
    DELETE FROM pspadm.psp_qbdt_payroll_transaction
        WHERE prior_payment_submission_fk IN (SELECT
            prior_payment_submission_seq
            FROM pspadm.psp_prior_payment_submission
            WHERE company_fk IN (uniqueid));
    /* Company -> PriorPaymentSubmission */
    DELETE FROM pspadm.psp_prior_payment_submission
        WHERE company_fk IN (uniqueid);
    /* Company -> PropertyAudit */
    DELETE FROM pspadm.psp_property_audit
        WHERE company_fk IN (uniqueid);
    /* Company -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtEmployeeInfo */
    DELETE FROM pspadm.psp_qbdt_employee_info
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtPayrollItemInfo */
    DELETE FROM pspadm.psp_qbdt_payroll_item_info
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE qbdt_payroll_transaction_fk IN (SELECT
                qbdt_payroll_transaction_seq
                FROM pspadm.psp_qbdt_payroll_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> QbdtPayrollTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> QbdtPayrollTransaction */
    DELETE FROM pspadm.psp_qbdt_payroll_transaction
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE company_fk IN (uniqueid);
    /* Company -> QbdtUnprocessedRequest */
    DELETE FROM pspadm.psp_qbdt_unprocessed_request
        WHERE company_fk IN (uniqueid);
    /* Company -> TaxTableMiscData */
    DELETE FROM pspadm.psp_tax_table_misc_data
        WHERE company_fk IN (uniqueid);
    /* Company -> VmpEmployeeInfo */
    DELETE FROM pspadm.psp_vmp_employee_info
        WHERE company_fk IN (uniqueid);
    /* Company -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE company_fk IN (uniqueid);
    /* Company -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE company_fk IN (uniqueid));
    /* Company -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE company_fk IN (uniqueid);
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE company_fk IN (uniqueid)));
    /* Company -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE company_fk IN (uniqueid)));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE company_fk IN (uniqueid)));
    /* Company -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE company_fk IN (uniqueid)));
    /* Company -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE company_fk IN (uniqueid)));
    /* Company -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE company_fk IN (uniqueid)));
    /* Company -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE company_fk IN (uniqueid));
    /* Company -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE company_fk IN (uniqueid);
    /* Company -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE company_fk IN (uniqueid);
    /* Company -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE company_fk IN (uniqueid)));
    /* Company -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE company_fk IN (uniqueid));
    /* Company -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE company_fk IN (uniqueid);
    /* Company -> PayrollRun -> ATFPayrollsToProcess */
    DELETE FROM pspadm.psp_atfpayrolls_to_process
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE billing_detail_fk IN (SELECT
                billing_detail_seq
                FROM pspadm.psp_billing_detail
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE billing_detail_fk IN (SELECT
                billing_detail_seq
                FROM pspadm.psp_billing_detail
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE billing_detail_fk IN (SELECT
                    billing_detail_seq
                    FROM pspadm.psp_billing_detail
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE billing_detail_fk IN (SELECT
                billing_detail_seq
                FROM pspadm.psp_billing_detail
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE billing_detail_fk IN (SELECT
                billing_detail_seq
                FROM pspadm.psp_billing_detail
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE billing_detail_fk IN (SELECT
                billing_detail_seq
                FROM pspadm.psp_billing_detail
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE billing_detail_fk IN (SELECT
                billing_detail_seq
                FROM pspadm.psp_billing_detail
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillingDetail -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE billing_detail_fk IN (SELECT
            billing_detail_seq
            FROM pspadm.psp_billing_detail
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> BillingDetail -> LiabilityCheckBillingDetailAssoc */
    DELETE FROM pspadm.psp_liab_check_billing_assoc
        WHERE billing_detail_fk IN (SELECT
            billing_detail_seq
            FROM pspadm.psp_billing_detail
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> BillingDetail */
    DELETE FROM pspadm.psp_billing_detail
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE bill_payment_split_fk IN (SELECT
                    bill_payment_split_seq
                    FROM pspadm.psp_bill_payment_split
                    WHERE bill_payment_fk IN (SELECT
                        bill_payment_seq
                        FROM pspadm.psp_bill_payment
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE bill_payment_split_fk IN (SELECT
                bill_payment_split_seq
                FROM pspadm.psp_bill_payment_split
                WHERE bill_payment_fk IN (SELECT
                    bill_payment_seq
                    FROM pspadm.psp_bill_payment
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE bill_payment_split_fk IN (SELECT
            bill_payment_split_seq
            FROM pspadm.psp_bill_payment_split
            WHERE bill_payment_fk IN (SELECT
                bill_payment_seq
                FROM pspadm.psp_bill_payment
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> BillPayment -> BillPaymentSplit */
    DELETE FROM pspadm.psp_bill_payment_split
        WHERE bill_payment_fk IN (SELECT
            bill_payment_seq
            FROM pspadm.psp_bill_payment
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> BillPayment */
    DELETE FROM pspadm.psp_bill_payment
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> CompanyServiceBankAccount */
    DELETE FROM pspadm.psp_company_service_bank_acct
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> EmpTotalsPayrollRun */
    DELETE FROM pspadm.psp_emp_totals_payroll_run
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> FraudEvent */
    DELETE FROM pspadm.psp_fraud_event
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> LiabilityCheck -> LiabilityCheckBillingDetailAssoc */
    DELETE FROM pspadm.psp_liab_check_billing_assoc
        WHERE liability_check_fk IN (SELECT
            liability_check_seq
            FROM pspadm.psp_liability_check
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> LiabilityCheck -> LiabilityCheckLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_line_fk IN (SELECT
            liability_check_line_seq
            FROM pspadm.psp_liability_check_line
            WHERE liability_check_fk IN (SELECT
                liability_check_seq
                FROM pspadm.psp_liability_check
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> LiabilityCheck -> LiabilityCheckLine */
    DELETE FROM pspadm.psp_liability_check_line
        WHERE liability_check_fk IN (SELECT
            liability_check_seq
            FROM pspadm.psp_liability_check
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> LiabilityCheck -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_check_fk IN (SELECT
            liability_check_seq
            FROM pspadm.psp_liability_check
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> LiabilityCheck */
    DELETE FROM pspadm.psp_liability_check
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> TransmissionPayrollRun */
    DELETE FROM pspadm.psp_transmission_payroll_run
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE payroll_run_fk IN (SELECT
                            payroll_run_seq
                            FROM pspadm.psp_payroll_run
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE payroll_run_fk IN (SELECT
                        payroll_run_seq
                        FROM pspadm.psp_payroll_run
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE payroll_run_fk IN (SELECT
                    payroll_run_seq
                    FROM pspadm.psp_payroll_run
                    WHERE company_fk IN (uniqueid))));
    /* Company -> PayrollRun -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE payroll_run_fk IN (SELECT
                payroll_run_seq
                FROM pspadm.psp_payroll_run
                WHERE company_fk IN (uniqueid)));
    /* Company -> PayrollRun -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun -> FailedPayrollRun */
    DELETE FROM pspadm.psp_failed_payroll_run
        WHERE payroll_run_fk IN (SELECT
            payroll_run_seq
            FROM pspadm.psp_payroll_run
            WHERE company_fk IN (uniqueid));
    /* Company -> PayrollRun */
    DELETE FROM pspadm.psp_payroll_run
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyNote */
    DELETE FROM pspadm.psp_company_note
        WHERE company_fk IN (uniqueid);
    /* Company -> Employee -> EmployeeAccrual */
    DELETE FROM pspadm.psp_employee_accrual
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeeCustomField */
    DELETE FROM pspadm.psp_employee_custom_field
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeeLawQtrTotals */
    DELETE FROM pspadm.psp_employee_law_qtr_totals
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeePayrollItem */
    DELETE FROM pspadm.psp_employee_payroll_item
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeePayrollItemQtrTotals */
    DELETE FROM pspadm.psp_ee_payrollitem_qtrtotals
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeeTax -> TaxTableMiscData */
    DELETE FROM pspadm.psp_tax_table_misc_data
        WHERE employee_tax_fk IN (SELECT
            employee_tax_seq
            FROM pspadm.psp_employee_tax
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> EmployeeTax */
    DELETE FROM pspadm.psp_employee_tax
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeeW2Totals */
    DELETE FROM pspadm.psp_employee_w2_totals
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeeWagePlan */
    DELETE FROM pspadm.psp_employee_wage_plan
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> FraudEvent */
    DELETE FROM pspadm.psp_fraud_event
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> LiabilityAdjustment -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> LiabilityAdjustment -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE liability_adjustment_fk IN (SELECT
            liability_adjustment_seq
            FROM pspadm.psp_liability_adjustment
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> LiabilityAdjustment */
    DELETE FROM pspadm.psp_liability_adjustment
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> PayItem */
    DELETE FROM pspadm.psp_pay_item
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE pstub_employee_info_fk IN (SELECT
                pstub_employee_info_seq
                FROM pspadm.psp_pstub_employee_info
                WHERE employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE pstub_employee_info_fk IN (SELECT
                pstub_employee_info_seq
                FROM pspadm.psp_pstub_employee_info
                WHERE employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE pstub_employee_info_fk IN (SELECT
                pstub_employee_info_seq
                FROM pspadm.psp_pstub_employee_info
                WHERE employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> PstubEmployeeInfo -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE pstub_employee_info_fk IN (SELECT
                pstub_employee_info_seq
                FROM pspadm.psp_pstub_employee_info
                WHERE employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> PstubEmployeeInfo -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE pstub_employee_info_fk IN (SELECT
            pstub_employee_info_seq
            FROM pspadm.psp_pstub_employee_info
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> PstubEmployeeInfo */
    DELETE FROM pspadm.psp_pstub_employee_info
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> ThirdParty401kBatchEmployee */
    DELETE FROM pspadm.psp_tp401k_batch_employee
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE d_d_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE d_d_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE d_d_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE d_d_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE d_d_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE d_d_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE d_d_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE d_d_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE d_d_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE d_d_employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE employee_bank_account_fk IN (SELECT
                    employee_bank_account_seq
                    FROM pspadm.psp_employee_bank_account
                    WHERE employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE employee_bank_account_fk IN (SELECT
                    employee_bank_account_seq
                    FROM pspadm.psp_employee_bank_account
                    WHERE employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE employee_bank_account_fk IN (SELECT
                        employee_bank_account_seq
                        FROM pspadm.psp_employee_bank_account
                        WHERE employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE employee_bank_account_fk IN (SELECT
                    employee_bank_account_seq
                    FROM pspadm.psp_employee_bank_account
                    WHERE employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE employee_bank_account_fk IN (SELECT
                    employee_bank_account_seq
                    FROM pspadm.psp_employee_bank_account
                    WHERE employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE employee_bank_account_fk IN (SELECT
                    employee_bank_account_seq
                    FROM pspadm.psp_employee_bank_account
                    WHERE employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE employee_bank_account_fk IN (SELECT
                    employee_bank_account_seq
                    FROM pspadm.psp_employee_bank_account
                    WHERE employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE employee_bank_account_fk IN (SELECT
                employee_bank_account_seq
                FROM pspadm.psp_employee_bank_account
                WHERE employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> EmployeeBankAccount -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE employee_bank_account_fk IN (SELECT
            employee_bank_account_seq
            FROM pspadm.psp_employee_bank_account
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> EmployeeBankAccount */
    DELETE FROM pspadm.psp_employee_bank_account
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> Paycheck -> EmployerContribution -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE employer_contribution_fk IN (SELECT
            employer_contribution_seq
            FROM pspadm.psp_employer_contribution
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> EmployerContribution */
    DELETE FROM pspadm.psp_employer_contribution
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubPayItem */
    DELETE FROM pspadm.psp_pstub_pay_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubDDItem */
    DELETE FROM pspadm.psp_pstub_dditem
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubMsg */
    DELETE FROM pspadm.psp_pstub_msg
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub -> PstubPaidTimeoffItem */
    DELETE FROM pspadm.psp_pstub_paid_timeoff_item
        WHERE paystub_fk IN (SELECT
            paystub_seq
            FROM pspadm.psp_paystub
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Paystub */
    DELETE FROM pspadm.psp_paystub
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> ThirdParty401kBatchPaycheck */
    DELETE FROM pspadm.psp_tp401k_batch_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Compensation -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE compensation_fk IN (SELECT
            compensation_seq
            FROM pspadm.psp_compensation
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Compensation */
    DELETE FROM pspadm.psp_compensation
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> Tax */
    DELETE FROM pspadm.psp_tax
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE source_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE source_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE paycheck_split_fk IN (SELECT
                    paycheck_split_seq
                    FROM pspadm.psp_paycheck_split
                    WHERE paycheck_fk IN (SELECT
                        paycheck_seq
                        FROM pspadm.psp_paycheck
                        WHERE source_employee_fk IN (SELECT
                            employee_seq
                            FROM pspadm.psp_employee
                            WHERE company_fk IN (uniqueid))))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE source_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE source_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE source_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE paycheck_split_fk IN (SELECT
                paycheck_split_seq
                FROM pspadm.psp_paycheck_split
                WHERE paycheck_fk IN (SELECT
                    paycheck_seq
                    FROM pspadm.psp_paycheck
                    WHERE source_employee_fk IN (SELECT
                        employee_seq
                        FROM pspadm.psp_employee
                        WHERE company_fk IN (uniqueid)))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE paycheck_split_fk IN (SELECT
            paycheck_split_seq
            FROM pspadm.psp_paycheck_split
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> PaycheckSplit */
    DELETE FROM pspadm.psp_paycheck_split
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> QbdtPaycheckInfo */
    DELETE FROM pspadm.psp_qbdt_paycheck_info
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckState */
    DELETE FROM pspadm.psp_tp401k_paycheck_state
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> ThirdParty401kPaycheck -> ThirdParty401kPaycheckPendingState */
    DELETE FROM pspadm.psp_tp401k_paycheck_pending
        WHERE third_party401k_paycheck_fk IN (SELECT
            tp401k_paycheck_seq
            FROM pspadm.psp_tp401k_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> ThirdParty401kPaycheck */
    DELETE FROM pspadm.psp_tp401k_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckState */
    DELETE FROM pspadm.psp_wc_paycheck_state
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> WorkersCompPaycheck -> WorkersCompPaycheckPendingState */
    DELETE FROM pspadm.psp_wc_paycheck_pending
        WHERE workers_comp_paycheck_fk IN (SELECT
            wc_paycheck_seq
            FROM pspadm.psp_wc_paycheck
            WHERE paycheck_fk IN (SELECT
                paycheck_seq
                FROM pspadm.psp_paycheck
                WHERE source_employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> Paycheck -> WorkersCompPaycheck */
    DELETE FROM pspadm.psp_wc_paycheck
        WHERE paycheck_fk IN (SELECT
            paycheck_seq
            FROM pspadm.psp_paycheck
            WHERE source_employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> Paycheck */
    DELETE FROM pspadm.psp_paycheck
        WHERE source_employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> PstubEmployeePreference */
    DELETE FROM pspadm.psp_pstub_employee_preference
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> QbdtEmployeeInfo */
    DELETE FROM pspadm.psp_qbdt_employee_info
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_trans_line_fk IN (SELECT
            qbdt_payroll_trans_line_seq
            FROM pspadm.psp_qbdt_payroll_trans_line
            WHERE qbdt_payroll_transaction_fk IN (SELECT
                qbdt_payroll_transaction_seq
                FROM pspadm.psp_qbdt_payroll_transaction
                WHERE employee_fk IN (SELECT
                    employee_seq
                    FROM pspadm.psp_employee
                    WHERE company_fk IN (uniqueid))));
    /* Company -> Employee -> QbdtPayrollTransaction -> QbdtPayrollTransactionLine */
    DELETE FROM pspadm.psp_qbdt_payroll_trans_line
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> QbdtPayrollTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE qbdt_payroll_transaction_fk IN (SELECT
            qbdt_payroll_transaction_seq
            FROM pspadm.psp_qbdt_payroll_transaction
            WHERE employee_fk IN (SELECT
                employee_seq
                FROM pspadm.psp_employee
                WHERE company_fk IN (uniqueid)));
    /* Company -> Employee -> QbdtPayrollTransaction */
    DELETE FROM pspadm.psp_qbdt_payroll_transaction
        WHERE employee_fk IN (SELECT
            employee_seq
            FROM pspadm.psp_employee
            WHERE company_fk IN (uniqueid));
    /* Company -> Employee */
    DELETE FROM pspadm.psp_employee
        WHERE company_fk IN (uniqueid);
    /* Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyBankAccount -> CompanyServiceBankAccount */
    DELETE FROM pspadm.psp_company_service_bank_acct
        WHERE company_bank_account_fk IN (SELECT
            company_bank_account_seq
            FROM pspadm.psp_company_bank_account
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyBankAccount */
    DELETE FROM pspadm.psp_company_bank_account
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyService -> CompanyServiceBankAccount */
    DELETE FROM pspadm.psp_company_service_bank_acct
        WHERE company_service_fk IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    /* Company -> CompanyService */
    DELETE FROM pspadm.psp_bpcompany_service_info
        WHERE bpcompany_service_info_seq IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    DELETE FROM pspadm.psp_cdcompany_service_info
        WHERE cdcompany_service_info_seq IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    DELETE FROM pspadm.psp_racompany_service_info
        WHERE racompany_service_info_seq IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    DELETE FROM pspadm.psp_tax_company_service_info
        WHERE tax_company_service_info_seq IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    DELETE FROM pspadm.psp_tp401kcompany_service_info
        WHERE tp401kcompany_service_info_seq IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    DELETE FROM pspadm.psp_ddcompany_service_info
        WHERE ddcompany_service_info_seq IN (SELECT
            company_service_seq
            FROM pspadm.psp_company_service
            WHERE company_fk IN (uniqueid));
    DELETE FROM pspadm.psp_company_service
        WHERE company_fk IN (uniqueid);
    /* Company -> Contact */
    DELETE FROM pspadm.psp_contact
        WHERE company_fk IN (uniqueid);
    /* Company -> TransactionResponse -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE transaction_response_fk IN (SELECT
            transaction_response_seq
            FROM pspadm.psp_transaction_response
            WHERE company_fk IN (uniqueid));
    /* Company -> TransactionResponse */
    DELETE FROM pspadm.psp_transaction_response
        WHERE company_fk IN (uniqueid);
    /* Company -> MoneyMovementTransaction -> ATFPaymentsToProcess */
    DELETE FROM pspadm.psp_atfpayments_to_process
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> EdiPaymentDetail */
    DELETE FROM pspadm.psp_edi_payment_detail
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> EftpsPaymentDetail */
    DELETE FROM pspadm.psp_eftps_payment_detail
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> EntryDetailRecord */
    DELETE FROM pspadm.psp_entry_detail_record
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> FsetFilingDetail */
    DELETE FROM pspadm.psp_fset_filing_detail
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> TaxPaymentOnHoldReason */
    DELETE FROM pspadm.psp_tax_payment_on_hold_reason
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE relatable_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE original_transaction_fk IN (SELECT
                financial_transaction_seq
                FROM pspadm.psp_financial_transaction
                WHERE money_movement_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> ATFPaymentsToProcess */
    DELETE FROM pspadm.psp_atfpayments_to_process
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> EdiPaymentDetail */
    DELETE FROM pspadm.psp_edi_payment_detail
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> EftpsPaymentDetail */
    DELETE FROM pspadm.psp_eftps_payment_detail
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> EntryDetailRecord */
    DELETE FROM pspadm.psp_entry_detail_record
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FsetFilingDetail */
    DELETE FROM pspadm.psp_fset_filing_detail
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> TaxPaymentOnHoldReason */
    DELETE FROM pspadm.psp_tax_payment_on_hold_reason
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE relatable_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE original_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> TransactionOffloadBatch */
    DELETE FROM pspadm.psp_transaction_offload_batch
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE original_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE original_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE original_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> FinancialTransactionState */
    DELETE FROM pspadm.psp_financial_trans_state
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE original_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> OnHoldReason */
    DELETE FROM pspadm.psp_fintxn_onholdreason_assoc
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE original_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE financial_transaction_fk IN (SELECT
            financial_transaction_seq
            FROM pspadm.psp_financial_transaction
            WHERE money_movement_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE original_transaction_fk IN (SELECT
                    money_movement_transaction_seq
                    FROM pspadm.psp_money_movement_transaction
                    WHERE company_fk IN (uniqueid))));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> FinancialTransaction */
    DELETE FROM pspadm.psp_financial_transaction
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> MoneyMovementTransaction */
    DELETE FROM pspadm.psp_money_movement_transaction
        WHERE original_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> TransactionReturn */
    DELETE FROM pspadm.psp_transaction_return
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> PaymentBatchAssoc */
    DELETE FROM pspadm.psp_payment_batch_assoc
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction -> VoidedCheck */
    DELETE FROM pspadm.psp_voided_check
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE original_transaction_fk IN (SELECT
                money_movement_transaction_seq
                FROM pspadm.psp_money_movement_transaction
                WHERE company_fk IN (uniqueid)));
    /* Company -> MoneyMovementTransaction -> MoneyMovementTransaction */
    DELETE FROM pspadm.psp_money_movement_transaction
        WHERE original_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> TransactionReturn */
    DELETE FROM pspadm.psp_transaction_return
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> PaymentBatchAssoc */
    DELETE FROM pspadm.psp_payment_batch_assoc
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> QbdtTransactionInfo */
    DELETE FROM pspadm.psp_qbdt_transaction_info
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction -> VoidedCheck */
    DELETE FROM pspadm.psp_voided_check
        WHERE money_movement_transaction_fk IN (SELECT
            money_movement_transaction_seq
            FROM pspadm.psp_money_movement_transaction
            WHERE company_fk IN (uniqueid));
    /* Company -> MoneyMovementTransaction */
    DELETE FROM pspadm.psp_money_movement_transaction
        WHERE company_fk IN (uniqueid);
    /* Company -> TransactionReturn */
    DELETE FROM pspadm.psp_transaction_return
        WHERE company_fk IN (uniqueid);
    /* Company -> Deduction -> QbdtPaylineInfo */
    DELETE FROM pspadm.psp_qbdt_payline_info
        WHERE deduction_fk IN (SELECT
            deduction_seq
            FROM pspadm.psp_deduction
            WHERE company_fk IN (uniqueid));
    /* Company -> Deduction */
    DELETE FROM pspadm.psp_deduction
        WHERE company_fk IN (uniqueid);
    /* Company -> CheckPrintSignature */
    DELETE FROM pspadm.psp_check_print_signature
        WHERE company_fk IN (uniqueid);
    /* Company -> CompanyAdditionalInfo */
    DELETE FROM pspadm.psp_company_additional_info
        WHERE company_fk IN (uniqueid);
    /* Company -> EmployerPreference */
    DELETE FROM pspadm.psp_employer_preference
        WHERE company_fk IN (uniqueid);
    /* Company -> QuickbooksInfo */
    DELETE FROM pspadm.psp_quickbooks_info
        WHERE company_fk IN (uniqueid);
    DELETE FROM pspadm.psp_company
        WHERE company_seq = uniqueid;
    COMMIT;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_remove_record_test(IN uniqueid TEXT, IN tablename TEXT)
AS 
$BODY$
DECLARE
    UniqueIdCollection CHARACTER VARYING(1020) [];
    ParentColumnNames CHARACTER VARYING(50) [];
    id_collection UniqueIdCollection%TYPE;
    c1 CURSOR FOR
    SELECT
        table_name, constraint_name, constraint_type, r_constraint_name
        FROM aws_oracle_ext.SYS_USER_CONSTRAINTS;
    c1$ATTRIBUTES aws_oracle_data.TCursorAttributes := ROW (FALSE, NULL, NULL, NULL);
    UserConstraints pspadm.prc_remove_record_test$c1 [];
    child_constraint_collection UserConstraints%TYPE;
    parent_constraint_collection UserConstraints%TYPE;
    ParentRecs pspadm.prc_remove_record_test$parentrec [];
    parent_recs ParentRecs%TYPE;
    ColumnNamesList CHARACTER VARYING(50) [];
    columnNames ColumnNamesList%TYPE;
    parent_id CHARACTER VARYING(1020);
    parent_columns ParentColumnNames%TYPE;
    delete_stmt_str CHARACTER VARYING(200);
    child_select_stmt_str CHARACTER VARYING(1000);
    parent_select_stmt_str CHARACTER VARYING(3000);
    select_stmt_str CHARACTER VARYING(300);
    /* delete_stmt_str VARCHAR2(300); */
    x DOUBLE PRECISION := 1;
BEGIN
    child_select_stmt_str := CONCAT_WS('', 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints', ' WHERE r_constraint_name IN (SELECT constraint_name FROM user_constraints ', 'WHERE table_name = ''', tablename, ''')', 'AND table_name NOT IN', '(''', tablename, ''')');
    /* DBMS_OUTPUT.PUT_LINE(child_select_stmt_str); */
    
    /*
    [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
    EXECUTE IMMEDIATE child_select_stmt_str BULK COLLECT INTO child_constraint_collection
    */
    FOR i IN 1..COALESCE(array_length(child_constraint_collection, 1), 0) LOOP
        BEGIN
            RAISE DEBUG USING MESSAGE = child_constraint_collection[i].table_name::TEXT;
            RAISE DEBUG USING MESSAGE = child_constraint_collection[i].constraint_name::TEXT;
            SELECT
                ARRAY_AGG(blk.column_name)
                INTO columnNames
                FROM (SELECT
                    ucc.column_name AS column_name
                    FROM aws_oracle_ext.SYS_USER_CONS_COLUMNS AS ucc
                    WHERE ucc.constraint_name = child_constraint_collection[i].constraint_name AND ucc.column_name NOT IN ('REALM_ID')) AS blk
                WHERE ucc.constraint_name = child_constraint_collection[i].constraint_name AND ucc.column_name NOT IN ('REALM_ID');
            RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'ColumnName:', columnNames[1]);
            /* identify the rows to be deleted from child table */
            select_stmt_str := CONCAT_WS('', 'SELECT ', aws_oracle_ext.substr(child_constraint_collection[i].table_name, 5), '_SEQ FROM ', child_constraint_collection[i].table_name, ' WHERE ', columnNames[1], '=''', uniqueid, '''');

            FOR j IN 2..COALESCE(array_length(columnNames, 1), 0) LOOP
                RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'ColumnName:', columnNames[j]);
                select_stmt_str := CONCAT_WS('', select_stmt_str, ' OR ', columnNames[j], '=''', uniqueid, '''');
            END LOOP;
            RAISE DEBUG USING MESSAGE = COALESCE(select_stmt_str::TEXT, '');
            /*
            [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
            EXECUTE IMMEDIATE select_stmt_str BULK COLLECT INTO id_collection
            */
            RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Count: ', COALESCE(array_length(id_collection, 1), 0));

            IF (COALESCE(array_length(id_collection, 1), 0) > 0) THEN
                FOR j IN 1..COALESCE(array_length(id_collection, 1), 0) LOOP
                    CALL pspadm.prc_remove_record_test(id_collection[j], child_constraint_collection[i].table_name);
                END LOOP;
            END IF;
            EXCEPTION
                WHEN no_data_found THEN
                    RAISE DEBUG USING MESSAGE = '@@@@@@@@@ EXCEPTION';
                    NULL;
                WHEN others THEN
                    /* this is to delete the Association tables data */
                    delete_stmt_str := CONCAT_WS('', 'DELETE FROM ', child_constraint_collection[i].table_name, ' WHERE ', columnNames[1], '=''', uniqueid, '''');

                    FOR j IN 2..COALESCE(array_length(columnNames, 1), 0) LOOP
                        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'ColumnName:', columnNames[j]);
                        delete_stmt_str := CONCAT_WS('', delete_stmt_str, ' OR ', columnNames[j], '=''', uniqueid, '''');
                    END LOOP;
                    EXECUTE delete_stmt_str;
        END;
    END LOOP;
    /* identify the rows to be deleted from the parent table, exclude the tables with static data */
    parent_select_stmt_str := CONCAT_WS('', 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints', ' WHERE constraint_name IN (SELECT r_constraint_name FROM user_constraints', ' WHERE table_name = ''', tablename, ''')', 'AND table_name NOT IN', '(''PSP_ACH_TRANSACTION_CODE'', ''PSP_ACTION_EVENT'', ''PSP_AGENCY'', ''PSP_AGENCY_CHECK_LIST_ITEM'', ''PSP_AGENCY_CHECK_LIST_STATUS'', ''PSP_AUTH_DOMAIN'', ''PSP_AUTH_OPERATION'', ''PSP_AUTH_ROLE'', ''PSP_AUTHROLE_OPERATION_ASSOC'', ''PSP_AUTH_USER'',', '''PSP_BANK_HOLIDAY'', ''PSP_COLLECTION_STAGE'', ''PSP_AGENCY_STAT_TXN_TYPE'', ''PSP_AGENCY_STATUS'', ''PSP_DEPOSIT_FREQUENCY_CODE'', ''PSP_EVENT_TYPE'', ''PSP_FEE'', ''PSP_FINANCIAL_TXN_ACTION'', ''PSP_FORM_TEMPLATE'', ''PSP_FUNDING_MODEL'',', '''PSP_GEMS_UPLOAD_BATCH'', ''PSP_GEMS_LEDGER_POSTING_RULE'', ''PSP_INTUIT_BANK_ACCOUNT'', ''PSP_INTUIT_BANK_ACC_TXN_TYPE'', ''PSP_INTUIT_BA_BT_FT'', ''PSP_LAW'', ''PSP_LEDGER_ACCOUNT'',', '''PSP_LEDGER_ACCOUNT_ACTION'', ''PSP_MONEY_MOVEMENT_TRANSACTION'', ''PSP_NACHAFILE'', ''PSP_OFFER'', ''PSP_OFFERING'', ''PSP_OFFERING_SVCCHG'', ''PSP_OFFERING_SVCCHG_GRP'', ''PSP_OFFERING_SVC_ASSOC'', ''PSP_OFFER_SVCCHG_ASSOC'', ''PSP_OFFLOAD_BATCH'', ''PSP_OFFLOAD_GROUP'', ''PSP_PAYMENT_TEMPLATE'', ''PSP_PMT_TEMPLATE_FREQUENCY'', ''PSP_PAYROLL_FREQUENCY'', ''PSP_PAYROLL_RUN_ACTION'',', '''PSP_POSTING_RULE'', ''PSP_REPORTING_AGENT'', ''PSP_SERVICE'', ''PSP_SERVICE_CHECK_LIST_ITEM'', ''PSP_SERVICE_CHECK_LIST_STATUS'', ''PSP_SERVICE_STATUS'', ''PSP_SVCSTAT_SRCSYS_ASSOC'', ''PSP_SVCSTAT_SVC_ASSOC'', ''PSP_SVCSTAT_SYSCAP_ASSOC'', ''PSP_SVCSTAT_TXNTYPE_ASSOC'', ''PSP_SYSTEM_CAPABILITY'',', '''PSP_SOURCE_PAYROLL_PARAMETER'', ''PSP_SOURCE_SYSTEM'', ''PSP_SYSTEM_PARAMETER'', ''PSP_TRANSACTION_RETURN_BATCH'',''PSP_TRANSACTION_STATE'',', '''PSP_TRANSACTION_TYPE'', ''PSP_COMPANY'', ''PSP_ENTITLEMENT_CODE'')');
    RAISE DEBUG USING MESSAGE = COALESCE(parent_select_stmt_str::TEXT, '');
    /*
    [5088 - Severity CRITICAL - PostgreSQL doesn't support the EXECUTE IMMEDIATE statement with BULK COLLECT. Perform a manual conversion.]
    EXECUTE IMMEDIATE parent_select_stmt_str BULK COLLECT INTO parent_constraint_collection
    */
    RAISE DEBUG USING MESSAGE = 'EXECUTED parent select';

    FOR j IN 1..COALESCE(array_length(parent_constraint_collection, 1), 0) LOOP
        BEGIN
            /* identify the parent column names */
            SELECT
                ARRAY_AGG(blk.column_name)
                INTO parent_columns
                FROM (SELECT
                    ucc.column_name AS column_name
                    FROM aws_oracle_ext.SYS_USER_CONSTRAINTS AS uc
                    INNER JOIN aws_oracle_ext.SYS_USER_CONS_COLUMNS AS ucc
                        ON ucc.constraint_name = uc.constraint_name
                    WHERE uc.constraint_type = 'R' AND uc.table_name = tablename AND ucc.table_name = tablename AND uc.r_constraint_name = parent_constraint_collection[j].constraint_name AND ucc.column_name NOT IN ('REALM_ID')) AS blk
                WHERE uc.constraint_type = 'R' AND uc.table_name = tablename AND ucc.table_name = tablename AND uc.r_constraint_name = parent_constraint_collection[j].constraint_name AND ucc.column_name NOT IN ('REALM_ID');

            FOR k IN 1..COALESCE(array_length(parent_columns, 1), 0) LOOP
                RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Parent Column: ', parent_columns[k]);
                select_stmt_str := CONCAT_WS('', 'SELECT ', parent_columns[k], ' FROM ', tablename, ' WHERE ', aws_oracle_ext.substr(tablename, 5), '_SEQ = ''', uniqueid, '''');

                IF (parent_constraint_collection[j].table_name = 'PSP_BANK_ACCOUNT') THEN
                    select_stmt_str := CONCAT_WS('', select_stmt_str, ' AND ', parent_columns[k], ' NOT IN (SELECT BANK_ACCOUNT_FK FROM PSP_INTUIT_BANK_ACCOUNT)');
                END IF;
                RAISE DEBUG USING MESSAGE = COALESCE(select_stmt_str::TEXT, '');
                EXECUTE select_stmt_str INTO STRICT parent_id;
                RAISE DEBUG USING MESSAGE = COALESCE(parent_id::TEXT, '');
                RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Count: ', COALESCE(array_length(parent_recs, 1), 0));
                RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'X: ', x);

                IF (parent_id IS NOT NULL) THEN
                    parent_recs := aws_oracle_ext.EXTEND(parent_recs);
                    /* DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count); */
                    parent_recs[x] := ROW (parent_constraint_collection[j].table_name, parent_recs[x].uniqueid);
                    parent_recs[x] := ROW (parent_recs[x].tablename, parent_id);
                    /* DBMS_OUTPUT.PUT_LINE('Assigning parent recs: ' || parent_recs(x).tablename); */
                    x := x + 1;
                END IF;
            END LOOP;
            EXCEPTION
                WHEN no_data_found THEN
                    RAISE DEBUG USING MESSAGE = '@@@@@@@@@ EXCEPTION';
                    NULL;
                WHEN others THEN
                    RAISE DEBUG USING MESSAGE = '@@@@@@@@@ PARENT OTHERS EXCEPTION';
                    NULL;
        END;
    END LOOP;
    /* delete the current entry */
    delete_stmt_str := CONCAT_WS('', 'DELETE FROM ', tablename, ' WHERE ', aws_oracle_ext.substr(tablename, 5), '_SEQ=''', uniqueid, '''');
    RAISE DEBUG USING MESSAGE = COALESCE(delete_stmt_str::TEXT, '');
    EXECUTE delete_stmt_str;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', '#### ', COALESCE(array_length(parent_recs, 1), 0));
    /* delete the entries from parent table */

    FOR l IN 1..COALESCE(array_length(parent_recs, 1), 0) LOOP
        CALL pspadm.prc_remove_record_test(parent_recs[l].uniqueid, parent_recs[l].tablename);
    END LOOP;
    EXCEPTION
        WHEN no_data_found THEN
            RAISE DEBUG USING MESSAGE = '@@@@@@@@@ EXCEPTION';
            RETURN;
        WHEN others THEN
            RAISE DEBUG USING MESSAGE = '@@@@@@@@@ OUTER OTHERS EXCEPTION';
            RETURN;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_set_psp_event_log(INOUT p_return_cd DOUBLE PRECISION, INOUT p_error_desc TEXT, IN p_companyid TEXT, IN p_typecd TEXT, IN p_domainname TEXT, IN p_archname TEXT, IN p_compname TEXT, IN p_hostname TEXT, IN p_appname TEXT, IN p_objectname TEXT, IN p_username TEXT, IN p_messagedttm TEXT, IN p_message TEXT)
AS 
$BODY$
DECLARE
    aws_params JSON := json_build_object('p_return_cd', p_return_cd, 'p_error_desc', p_error_desc, 'p_companyid', p_companyid, 'p_typecd', p_typecd, 'p_domainname', p_domainname, 'p_archname', p_archname, 'p_compname', p_compname, 'p_hostname', p_hostname, 'p_appname', p_appname, 'p_objectname', p_objectname, 'p_username', p_username, 'p_messagedttm', p_messagedttm, 'p_message', p_message);
    aws_session_env JSON;
BEGIN
    SELECT
        *
        FROM aws_oracle_ext.autonomous_exec('CALL pspadm.prc_set_psp_event_log$at('||quote_nullable(aws_params)||','||quote_nullable(aws_session_env)||')')
        INTO aws_params, aws_session_env;
    SELECT
        t.p_return_cd, t.p_error_desc
        INTO p_return_cd, p_error_desc
        FROM json_to_record(aws_params)
            AS t (p_return_cd DOUBLE PRECISION, p_error_desc TEXT);
END
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_set_psp_event_log$at(INOUT aws_params JSON, INOUT aws_session_env JSON)
AS 
$BODY$
/* formatted AS 'DD/MM/YYYY HH24:MI:SS' */
DECLARE
    v_temp_date PSPADM.PSP_EVENT_LOG.MESSAGE_DTTM%TYPE;
    p_return_cd DOUBLE PRECISION;
    p_error_desc TEXT;
    p_companyid TEXT;
    p_typecd TEXT;
    p_domainname TEXT;
    p_archname TEXT;
    p_compname TEXT;
    p_hostname TEXT;
    p_appname TEXT;
    p_objectname TEXT;
    p_username TEXT;
    p_messagedttm TEXT;
    p_message TEXT;
/*
*****************************************************************************
   NAME:       PR_SET_PSP_EVENT_LOG
   PURPOSE:    Creates log entry into PSP_EVENT_LOG Table, used for offload log
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09/18/2009  Tushar Thakker   1. Created


   INPUT:
   OUTPUT:
   RETURNED VALUE:
   CALLED BY:
   ASSUMPTIONS:
   LIMITATIONS:
   ALGORITHM : Autonomous transaction transaction for logging
   NOTES:
*****************************************************************************
*/
BEGIN
    /* initialize */
    SELECT
        t.p_return_cd, t.p_error_desc, t.p_companyid, t.p_typecd, t.p_domainname, t.p_archname, t.p_compname, t.p_hostname, t.p_appname, t.p_objectname, t.p_username, t.p_messagedttm, t.p_message
        INTO p_return_cd, p_error_desc, p_companyid, p_typecd, p_domainname, p_archname, p_compname, p_hostname, p_appname, p_objectname, p_username, p_messagedttm, p_message
        FROM json_to_record(aws_params)
            AS t (p_return_cd DOUBLE PRECISION, p_error_desc TEXT, p_companyid TEXT, p_typecd TEXT, p_domainname TEXT, p_archname TEXT, p_compname TEXT, p_hostname TEXT, p_appname TEXT, p_objectname TEXT, p_username TEXT, p_messagedttm TEXT, p_message TEXT);
    p_return_cd := 0;
    p_error_desc := NULL;
    /* this was the first version Rob came up with */
    /* v_temp_date  := TO_DATE (p_MessageDTTM, 'DD/MM/YYYY HH24:MI:SS'); */
    v_temp_date := aws_oracle_ext.TO_DATE(p_messagedttm::TEXT, 'YYYY-MM-DD"T"HH24:MI:SS');
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function.]
    INSERT INTO PSP_EVENT_LOG (
                EVENT_LOG_SEQ,
                VERSION ,
                CREATOR_ID ,
                CREATED_DATE ,
                MODIFIER_ID ,
                MODIFIED_DATE   ,
                REALM_ID ,
                COMPANY_ID,
                EVENT_LOG_TYPE_CD,
                DOMAIN_NAME,
                ARCHITECTURE_NAME,
                COMPONENT_NAME,
                HOST_NAME,
                APPLICATION_NAME,
                OBJECT_NAME,
                MESSAGE_DTTM,
                MESSAGE)
            VALUES (
                FN_FORMAT_SYSGUID(SYS_GUID()),
                0 ,
                'PRC_OFFLOAD' ,
                SYS_EXTRACT_UTC(SYSTIMESTAMP),
                null ,
                SYS_EXTRACT_UTC(SYSTIMESTAMP),
                -1 ,
                p_CompanyId,
                p_TypeCd,
                p_DomainName,
                p_ArchName,
                p_CompName,
                p_HostName,
                p_AppName,
                p_ObjectName,
                v_temp_date,
                p_Message)
    */
    /*
    [5035 - Severity CRITICAL - A transaction cannot be ended inside a block with exception handlers. Revise your code to try move transaction control on side of application.]
    COMMIT
    */
    prc_set_psp_event_log$at.aws_params := json_build_object('p_return_cd', p_return_cd, 'p_error_desc', p_error_desc);
    EXCEPTION
        WHEN others THEN
            p_return_cd := SQLSTATE;
            p_error_desc := aws_oracle_ext.substr((CONCAT_WS('', 'Error creating PSP Event Log. ', SQLERRM)), 1, 250);
            prc_set_psp_event_log$at.aws_params := json_build_object('p_return_cd', p_return_cd, 'p_error_desc', p_error_desc);
END;
$BODY$
LANGUAGE plpgsql;


COMMENT ON PROCEDURE pspadm.prc_set_psp_event_log$at(INOUT aws_params JSON, INOUT aws_session_env JSON)
     IS 'Belongs to: pspadm.prc_set_psp_event_log';



CREATE PROCEDURE pspadm.prc_upd_company_ledger_balance(IN p_company_fk TEXT DEFAULT NULL)
AS 
$BODY$
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system UTC date and time */
    v_end_date TIMESTAMP(0) WITHOUT TIME ZONE := pspadm.fn_get_psp_timestamp() - (1::NUMERIC || ' days')::INTERVAL;
    v_ledger_balance_max_date TIMESTAMP(0) WITHOUT TIME ZONE;
    financial_trans_state_cursor CURSOR FOR
    SELECT DISTINCT
        (DATE(transaction_state_eff_date)) AS transaction_state_eff_date
        FROM pspadm.psp_financial_trans_state
        WHERE company_fk = p_company_fk AND transaction_state_eff_date < v_end_date + (1::NUMERIC || ' days')::INTERVAL AND DATE(transaction_state_eff_date) > (SELECT
            aws_oracle_ext.TRUNC(COALESCE(MAX(balance_date), (SELECT
                aws_oracle_ext.TO_DATE('01-JAN-2005'))))
            FROM pspadm.psp_ledger_balance AS lb
            WHERE lb.company_fk = p_company_fk)
        ORDER BY aws_oracle_ext.TRUNC(transaction_state_eff_date);
    financial_trans_state_cursor$ATTRIBUTES aws_oracle_data.TCursorAttributes := ROW (FALSE, NULL, NULL, NULL);
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL
    */
    IF (p_company_fk IS NULL) THEN
        RETURN;
    END IF;
    /* get the max balance_date from psp_ledger_balance table calculate balances till that date */
    SELECT
        MAX(balance_date)
        INTO STRICT v_ledger_balance_max_date
        FROM pspadm.psp_ledger_balance;

    IF v_ledger_balance_max_date IS NOT NULL THEN
        v_end_date := v_ledger_balance_max_date;
    END IF;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'yyyy-mm-dd hh24:mi:ss'));

    FOR financial_trans_state_rec IN financial_trans_state_cursor LOOP
        INSERT INTO pspadm.psp_ledger_balance (ledger_balance_seq, version, creator_id, created_date, modified_date, realm_id, balance_amount, balance_date, ledger_account_fk, company_fk, reporting_type)
        SELECT
            pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'COMPANYLEDGERBALANCERECALCJOB', v_utc_date, v_utc_date, - 1, COALESCE((SELECT
                ilb.balance_amount
                FROM pspadm.psp_ledger_balance AS ilb
                WHERE ilb.ledger_account_fk = pr.ledger_account_fk AND ilb.company_fk = fts.company_fk AND ilb.reporting_type = po.reporting_type AND DATE(ilb.balance_date) = (SELECT
                    MAX(DATE(olb.balance_date))
                    FROM pspadm.psp_ledger_balance AS olb
                    WHERE olb.company_fk = ilb.company_fk AND olb.ledger_account_fk = ilb.ledger_account_fk AND olb.reporting_type = ilb.reporting_type)), 0) + SUM((SELECT
                ft.financial_transaction_amount
                FROM pspadm.psp_financial_transaction AS ft
                WHERE ft.financial_transaction_seq = fts.financial_transaction_fk) *
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
            END) AS amount, DATE(fts.transaction_state_eff_date) AS newbal_date, pr.ledger_account_fk, fts.company_fk, po.reporting_type
            FROM pspadm.psp_financial_trans_state AS fts, pspadm.psp_posting_rule AS pr, pspadm.psp_ledger_account AS la, pspadm.psp_company AS pc, pspadm.psp_company_offering AS pco, pspadm.psp_offering AS po
            WHERE pr.transaction_state_fk = fts.transaction_state_fk AND pr.transaction_type_fk = fts.transaction_type_fk AND la.ledger_account_cd = pr.ledger_account_fk AND pc.company_seq = fts.company_fk AND pc.company_seq = pco.company_fk AND pco.offering_fk = po.offering_seq AND
            /* AND PO.REPORTING_TYPE IN ('DirectDeposit','Tax') */
            po.service_code = 'DirectDeposit' AND DATE(fts.transaction_state_eff_date) = aws_oracle_ext.TRUNC(financial_trans_state_rec.transaction_state_eff_date) AND fts.transaction_state_eff_date BETWEEN financial_trans_state_rec.transaction_state_eff_date - (1::NUMERIC || ' days')::INTERVAL AND financial_trans_state_rec.transaction_state_eff_date + (1::NUMERIC || ' days')::INTERVAL AND fts.company_fk = p_company_fk
            GROUP BY fts.company_fk, DATE(fts.transaction_state_eff_date), pr.ledger_account_fk, po.reporting_type;
    END LOOP;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'yyyy-mm-dd hh24:mi:ss'));
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.prc_update_ledger_balance()
AS 
$BODY$
DECLARE
    v_start_date TIMESTAMP(0) WITHOUT TIME ZONE;
    v_end_date TIMESTAMP(0) WITHOUT TIME ZONE := pspadm.fn_get_psp_timestamp() - (1::NUMERIC || ' days')::INTERVAL;
    v_ledger_date TIMESTAMP(0) WITHOUT TIME ZONE;
BEGIN
    /* get the last date when ledger was updated. */
    SELECT
        MAX(balance_date) + (1::NUMERIC || ' days')::INTERVAL
        INTO STRICT v_start_date
        FROM pspadm.psp_ledger_balance;
    /* NULL means ledger was never updated */

    IF v_start_date IS NULL THEN
        SELECT
            MIN(transaction_state_eff_date)
            INTO STRICT v_start_date
            FROM pspadm.psp_financial_trans_state;
    END IF;
    /* NULL here means psp_financial_trans_state is empty */

    IF v_start_date IS NULL THEN
        RETURN;
    END IF;
    /* Start updating the ledger with the start date */
    v_ledger_date := v_start_date;

    FOR i IN 1..((EXTRACT (EPOCH FROM aws_oracle_ext.TRUNC(v_end_date) - aws_oracle_ext.TRUNC(v_start_date)) / 86400)::NUMERIC) + 1 LOOP
        CALL pspadm.prc_calculate_ledger_balance(v_ledger_date);
        v_ledger_date := v_start_date + (i::NUMERIC || ' days')::INTERVAL;
    END LOOP;
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.purge_qboe_data$prc_create_qboe_backup(IN v_purge_schema TEXT, IN v_bkp_schema TEXT)
AS 
$BODY$
DECLARE
    v_sql CHARACTER VARYING(1000);
    v_table_sql CHARACTER VARYING(1000);
    v_count DOUBLE PRECISION;
    v_table_name CHARACTER VARYING(100);
    v_bkp_table_name CHARACTER VARYING(100);
    v_is_backup_created NUMERIC(1) := 0;
    v_record_count DOUBLE PRECISION := 0;
    v_sqlerrm CHARACTER VARYING(1000) := NULL;
    table_exists NUMERIC(1);
    bkp_schema_table_exists NUMERIC(1);
    vcompany_seq CHARACTER VARYING(255) := 'a9a85930-ce78-4b5d-96b1-86e076cc482c';
    aws$frmt_err_bcktrc CHARACTER VARYING(2000);
/*
*****************************************************************************
   NAME:       PRC CREATE QBOE BACKUP
   PURPOSE:    Create backup tables required for Purging QBOE data

   REVISIONS:
   Ver        Date        Author                  Description
   ---------  ----------  -------------------    -----------------------------
   1.0        8/14/2018   Namrata Loharuka       1. Created this procedure.

   NOTES:



*****************************************************************************
*/
BEGIN
    /* - Count No. of Tables to loop through */
    EXECUTE CONCAT_WS('', 'SELECT COUNT(*) FROM  ', v_bkp_schema, '.QBOE_PURGING_METADATA_NEW ') INTO STRICT v_count;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Total Table Count: ', v_count);

    FOR i IN 1..v_count LOOP
        RAISE DEBUG USING MESSAGE = '***********************************************';
        EXECUTE CONCAT_WS('', 'SELECT table_name, bkp_table_name, table_query, 
        is_backup_created FROM ', v_bkp_schema, '.QBOE_PURGING_METADATA_NEW
         WHERE TABLE_ORDER = ', i) INTO STRICT v_table_name, v_bkp_table_name, v_table_sql, v_is_backup_created;
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Taking backup of ', v_table_name);
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', aws_oracle_ext.TO_CHAR(aws_oracle_ext.SYSDATE(), 'DD-MON-YYYY HH24:MI:SS'), ' : 
            Creating backup table ');
        v_sql := CONCAT_WS('', 'CREATE TABLE ', v_bkp_schema, '.', v_bkp_table_name, ' AS (select /*+ PARALLEL(8) */ * ', v_table_sql, ')');
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'generated sql', v_sql);
        EXECUTE v_sql;
        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Total record count ', v_record_count);
        /*
        [5035 - Severity CRITICAL - A transaction cannot be ended inside a block with exception handlers. Revise your code to try move transaction control on side of application.]
        COMMIT
        */
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', aws_oracle_ext.TO_CHAR(aws_oracle_ext.SYSDATE(), 'DD-MON-YYYY HH24:MI:SS'), ' :  Backup of ', v_table_name, ' completed with backup table name as ', v_bkp_table_name);
        RAISE DEBUG USING MESSAGE = '***********************************************';
    END LOOP;
    EXCEPTION
        WHEN no_data_found THEN
            GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
            v_sqlerrm := aws_oracle_ext.substr(CONCAT_WS('', SQLERRM, aws$frmt_err_bcktrc), 0, 999);
            RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'No Data Found excep-: ', v_sqlerrm);
            RAISE USING hint = -20002, message = v_sqlerrm, detail = 'User-defined exception';
        WHEN others THEN
            GET STACKED DIAGNOSTICS aws$frmt_err_bcktrc = PG_CONTEXT;
            v_sqlerrm := aws_oracle_ext.substr(CONCAT_WS('', SQLERRM, aws$frmt_err_bcktrc), 0, 999);
            RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Other Exception: ', v_sqlerrm);
            RAISE USING hint = -20003, message = v_sqlerrm, detail = 'User-defined exception';
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.tmp_prc_upd_comp_led_bal(IN p_company_fk TEXT DEFAULT NULL)
AS 
$BODY$
DECLARE
    v_utc_date TIMESTAMP(6) WITHOUT TIME ZONE;
    /* current system UTC date and time */
    v_end_date TIMESTAMP(0) WITHOUT TIME ZONE := pspadm.fn_get_psp_timestamp() - (1::NUMERIC || ' days')::INTERVAL;
    v_ledger_balance_max_date TIMESTAMP(0) WITHOUT TIME ZONE;
    financial_trans_state_cursor CURSOR FOR
    SELECT DISTINCT
        (DATE(transaction_state_eff_date)) AS transaction_state_eff_date
        FROM pspadm.psp_financial_trans_state
        WHERE company_fk = p_company_fk AND transaction_state_eff_date < v_end_date + (1::NUMERIC || ' days')::INTERVAL
        ORDER BY aws_oracle_ext.TRUNC(transaction_state_eff_date);
    financial_trans_state_cursor$ATTRIBUTES aws_oracle_data.TCursorAttributes := ROW (FALSE, NULL, NULL, NULL);
BEGIN
    /*
    [5340 - Severity CRITICAL - PostgreSQL doesn't support the STANDARD.SYS_EXTRACT_UTC(TIMESTAMP_TZ_UNCONSTRAINED) function. Use suitable function or create user defined function., 5578 - Severity CRITICAL - Unable to automatically transform the SELECT statement. Try rewriting the statement.]
    SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL
    */
    IF (p_company_fk IS NULL) THEN
        RETURN;
    END IF;
    /* get the max balance_date from psp_ledger_balance table calculate balances till that date */
    SELECT
        MAX(balance_date)
        INTO STRICT v_ledger_balance_max_date
        FROM pspadm.psp_ledger_balance;

    IF v_ledger_balance_max_date IS NOT NULL THEN
        v_end_date := v_ledger_balance_max_date;
    END IF;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Started at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'yyyy-mm-dd hh24:mi:ss'));

    FOR financial_trans_state_rec IN financial_trans_state_cursor LOOP
        INSERT INTO pspadm.psp_ledger_balance (ledger_balance_seq, version, creator_id, created_date, modified_date, realm_id, balance_amount, balance_date, ledger_account_fk, company_fk, reporting_type)
        SELECT
            pspadm.fn_format_sysguid(aws_oracle_ext.sys_guid()), 1, 'COMPANYLEDGERBALANCERECALCJOB', v_utc_date, v_utc_date, - 1, COALESCE((SELECT
                ilb.balance_amount
                FROM pspadm.psp_ledger_balance AS ilb
                WHERE ilb.ledger_account_fk = pr.ledger_account_fk AND ilb.company_fk = fts.company_fk AND DATE(ilb.balance_date) = (SELECT
                    MAX(DATE(olb.balance_date))
                    FROM pspadm.psp_ledger_balance AS olb
                    WHERE olb.company_fk = ilb.company_fk AND olb.ledger_account_fk = ilb.ledger_account_fk AND olb.reporting_type = ilb.reporting_type)), 0) + SUM((SELECT
                ft.financial_transaction_amount
                FROM pspadm.psp_financial_transaction AS ft
                WHERE ft.financial_transaction_seq = fts.financial_transaction_fk) *
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
            END) AS amount, DATE(fts.transaction_state_eff_date) AS newbal_date, pr.ledger_account_fk, fts.company_fk, po.reporting_type
            FROM pspadm.psp_financial_trans_state AS fts, pspadm.psp_posting_rule AS pr, pspadm.psp_ledger_account AS la, pspadm.psp_company AS pc, pspadm.psp_company_offering AS pco, pspadm.psp_offering AS po
            WHERE pr.transaction_state_fk = fts.transaction_state_fk AND pr.transaction_type_fk = fts.transaction_type_fk AND la.ledger_account_cd = pr.ledger_account_fk AND pc.company_seq = pco.company_fk AND pco.offering_fk = po.offering_seq AND
            /* AND PO.REPORTING_TYPE IN ('DirectDeposit','Tax') */
            po.service_code = 'DirectDeposit' AND DATE(fts.transaction_state_eff_date) = aws_oracle_ext.TRUNC(financial_trans_state_rec.transaction_state_eff_date) AND fts.transaction_state_eff_date BETWEEN financial_trans_state_rec.transaction_state_eff_date - (1::NUMERIC || ' days')::INTERVAL AND financial_trans_state_rec.transaction_state_eff_date + (1::NUMERIC || ' days')::INTERVAL AND fts.company_fk = p_company_fk
            GROUP BY fts.company_fk, DATE(fts.transaction_state_eff_date), pr.ledger_account_fk;
    END LOOP;
    RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Finished at ', aws_oracle_ext.TO_CHAR(aws_oracle_ext.systimestamp(), 'yyyy-mm-dd hh24:mi:ss'));
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.update_freestanding_column(IN columnname TEXT, IN columnvalue TEXT, IN guidlist TEXT)
AS 
$BODY$
BEGIN
    EXECUTE CONCAT_WS('', 'UPDATE spcfdynamicentitydata SET  ', columnname, ' =  ', columnvalue, ' WHERE type_id IN (', guidlist, ')');
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.update_nonfreestanding_column(IN columnname TEXT, IN columnvalue TEXT, IN guidlist TEXT)
AS 
$BODY$
BEGIN
    EXECUTE CONCAT_WS('', 'UPDATE spcfdynamicvalues SET  ', columnname, ' =  ', columnvalue, ' WHERE type_id IN (', guidlist, ')');
END;
$BODY$
LANGUAGE plpgsql;



CREATE PROCEDURE pspadm.your_test()
AS 
$BODY$
BEGIN
    DECLARE
        v_count INTEGER := 0;
        v_time_now TIMESTAMP(6) WITHOUT TIME ZONE;
    BEGIN
        SELECT
            aws_oracle_ext.systimestamp()
            INTO STRICT v_time_now;
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Started at: ', v_time_now);
        DELETE FROM pspadm.psp_payroll_fraud_batch
            WHERE created_date < (SELECT
                aws_oracle_ext.SYSDATE()) - (2::NUMERIC || ' days')::INTERVAL;
        COMMIT;
        /* EXECUTE IMMEDIATE 'ALTER TABLE PSP_PAYROLL_FRAUD_BATCH ENABLE ROW MOVEMENT'; */
        /* EXECUTE IMMEDIATE 'ALTER TABLE PSP_PAYROLL_FRAUD_BATCH SHRINK SPACE'; */
        /* DBMS_STATS.gather_table_stats('PSP_LOCAL', 'PSP_PAYROLL_FRAUD_BATCH', estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE); */
        /* SELECT SYSTIMESTAMP INTO v_time_now FROM DUAL; */
        RAISE DEBUG USING MESSAGE = CONCAT_WS('', 'Ended at: ', v_time_now);
    END;
END;
$BODY$
LANGUAGE plpgsql;




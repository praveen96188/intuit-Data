CREATE OR REPLACE PROCEDURE prc_offload_update_payroll
(
    p_user_id           IN VARCHAR,  -- For audit purposes
    p_app_server_date   IN TIMESTAMP, -- UTC Date
    p_offload_batch_id  IN VARCHAR,  -- psp_offload_batch.offload_batch_seq
    p_offload_date      IN TIMESTAMP  -- UTC Date
)

    LANGUAGE plpgsql AS
$$
DECLARE


/******************************************************************************
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

******************************************************************************/

    v_utc_date   TIMESTAMP;
    v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_return_cd text; -- return code variable for logging
    v_error_desc VARCHAR(100);-- error desc variable for logging
    payroll_list VARCHAR(255)[];

BEGIN


    v_utc_date := p_app_server_date;


    CALL PRC_SET_PSP_EVENT_LOG(
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
            to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
            'Updating PAYROLL_RUN non-reversals');

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE pr.payroll_run_status != 'OffloadedAll'
      AND EXISTS (
            SELECT 'T'
            FROM psp_money_movement_transaction mmt1,
                 psp_financial_transaction ft1,
                 psp_transaction_type tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day',ft1.settlement_date) >= date_trunc('day',mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND tt1.association_type = 'Impound');

    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'OffloadedDebit',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE pr.payroll_run_status = 'PendingAutoRedebit'
      AND EXISTS(
            SELECT 'T'
            FROM psp_money_movement_transaction mmt1,
                 psp_financial_transaction ft1,
                 psp_transaction_type tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day', ft1.settlement_date) >= date_trunc('day', mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND tt1.association_type = 'Redebit');

    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'AutoRedebitOffloaded',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE pr.payroll_run_status = 'PendingRedebit'
      AND EXISTS(
            SELECT 'T'
            FROM psp_money_movement_transaction mmt1,
                 psp_financial_transaction ft1,
                 psp_transaction_type tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day', ft1.settlement_date) >= date_trunc('day', mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND tt1.association_type = 'Redebit');

    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'RedebitOffloaded',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE EXISTS(
                  SELECT 'T'
                  FROM psp_money_movement_transaction mmt1,
                       psp_financial_transaction ft1
                  WHERE ft1.payroll_run_fk = pr.payroll_run_seq
                    AND mmt1.offload_batch_fk = p_offload_batch_id
                    AND mmt1.initiation_date = p_offload_date
                    AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
                    AND mmt1.company_fk = ft1.company_fk
                    AND ft1.settlement_date >= mmt1.initiation_date
                    AND date_trunc('day', ft1.settlement_date) >= date_trunc('day', mmt1.initiation_date)
                    AND ft1.current_transaction_state_fk = 'Executed'
                    AND ft1.transaction_type_fk = 'EmployeeDdCredit');

    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'OffloadedAll',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

    CALL PRC_SET_PSP_EVENT_LOG (
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
            to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
            'Updating PAYROLL_RUN reversals');

    SELECT (fn_get_psp_timestamp()::timestamptz) AT TIME ZONE 'UTC' INTO v_psp_date;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE (pr.payroll_run_status = 'OffloadedAll' or pr.payroll_run_status = 'OffloadedDebit')
      AND EXISTS(
        /*+
                IndexScan(mmt1 PSP_MONEY_MOVEMENT_TRANSAC_FK3)
                IndexScan(ft1 PSP_FINANCIAL_TRANSACTION_FK10)*/
            SELECT 'T'
            FROM psp_money_movement_transaction mmt1,
                 psp_financial_transaction ft1,
                 psp_transaction_type tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day', ft1.settlement_date) >= date_trunc('day', mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND ft1.transaction_type_fk = 'EmployerTaxDebit');

    INSERT INTO PSP_ATFPAYROLLS_TO_PROCESS
        (SELECT gen_random_uuid(),
                0, p_user_id, v_psp_date, p_user_id, v_psp_date, -1,
                UNNEST(payroll_list));

    payroll_list = NULL;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE pr.payroll_run_status = 'PendingReversals'
      AND EXISTS (
            SELECT 'T'
            FROM psp_money_movement_transaction mmt1, psp_financial_transaction ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day',ft1.settlement_date) >= date_trunc('day',mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = 'EmployeeDdReversalDebit');

    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'ReversalsOffloaded',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE EXISTS(
              /*+
                  IndexScan(mmt1 PSP_MONEY_MOVEMENT_TRANSAC_FK3)
                  IndexScan(ft1 PSP_FINANCIAL_TRANSACTION_FK10)
                */
                  SELECT 'T'
                  FROM psp_money_movement_transaction mmt1,
                       psp_financial_transaction ft1
                  WHERE ft1.payroll_run_fk = pr.payroll_run_seq
                    AND mmt1.offload_batch_fk = p_offload_batch_id
                    AND mmt1.initiation_date = p_offload_date
                    AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
                    AND mmt1.company_fk = ft1.company_fk
                    AND ft1.settlement_date >= mmt1.initiation_date
                    AND date_trunc('day', ft1.settlement_date) >= date_trunc('day', mmt1.initiation_date)
                    AND ft1.current_transaction_state_fk = 'Executed'
                    AND ft1.transaction_type_fk in ('EmployerTaxDebit', 'EmployerTaxCredit'));


--Update payrolls that only contain fees when the fees offload
    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'OffloadedAll',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE payroll_run_status != 'AutoRedebitOffloaded'
      AND EXISTS (
        /*+
           IndexScan(MMT1 PSP_MONEY_MOVEMENT_TRANSAC_FK3)
           IndexScan(FT1 PSP_FINANCIAL_TRANSACTION_FK10)
       */
            SELECT
                'T'
            FROM psp_money_movement_transaction mmt1,
                 psp_financial_transaction ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day',ft1.settlement_date) >= date_trunc('day',mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk in ('EmployerFeeDebit'))
      AND NOT EXISTS (
            SELECT
                'T'
            FROM psp_financial_transaction ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND ft1.transaction_type_fk in ('EmployerDdDebit', 'EmployerTaxDebit'));

    UPDATE psp_payroll_run pr
    SET payroll_run_status = 'OffloadedAll',
        VERSION            = VERSION + 1,
        modifier_id        = p_user_id,
        modified_date      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

/*+parallel(pr 8 soft) */
    SELECT ARRAY_AGG(pr.payroll_run_seq)
    INTO payroll_list
    from psp_payroll_run pr
    WHERE PR.PAYROLL_RUN_STATUS = 'OffloadedAll'
      AND EXISTS
        (
            SELECT 'T'
            FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
                 PSP_FINANCIAL_TRANSACTION FT1,
                 PSP_FINANCIAL_TRANSACTION FT2
            WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
              AND FT2.PAYROLL_RUN_FK = FT1.PAYROLL_RUN_FK
              AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
              AND MMT1.initiation_date = p_offload_date
              AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND date_trunc('day', ft1.settlement_date) >= date_trunc('day', mmt1.initiation_date)
              AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
              AND FT1.TRANSACTION_TYPE_FK = 'EmployeeDdCredit'
              AND FT2.CURRENT_TRANSACTION_STATE_FK = 'Completed'
              AND FT2.SETTLEMENT_TYPE_CD <> 'ACH'
              AND FT2.TRANSACTION_TYPE_FK = 'EmployerDdDebit'
        );

    UPDATE PSP_PAYROLL_RUN PR
    SET PAYROLL_RUN_STATUS = 'Complete',
        VERSION            = VERSION + 1,
        MODIFIER_ID        = p_user_id,
        MODIFIED_DATE      = v_utc_date
    WHERE pr.payroll_run_seq = ANY (payroll_list);

    payroll_list = NULL;

    CALL PRC_SET_PSP_EVENT_LOG (
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
            to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
            'Writing Files');
END;
$$;
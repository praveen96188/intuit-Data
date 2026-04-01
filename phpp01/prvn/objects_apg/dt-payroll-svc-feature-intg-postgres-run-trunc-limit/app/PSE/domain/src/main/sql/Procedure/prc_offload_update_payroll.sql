CREATE OR REPLACE PROCEDURE prc_offload_update_payroll 
   (
    p_user_id           IN VARCHAR2,  -- For audit purposes
    p_app_server_date   IN TIMESTAMP, -- UTC Date
    p_offload_batch_id  IN VARCHAR2,  -- psp_offload_batch.offload_batch_seq
    p_offload_date      IN TIMESTAMP  -- UTC Date
   )
   
IS


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

    TYPE payroll_list_type IS TABLE OF psp_payroll_run.payroll_run_seq%TYPE;
    v_utc_date   TIMESTAMP;
    v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_return_cd number; -- return code variable for logging
    v_error_desc varchar2(100);-- error desc variable for logging
    payroll_list payroll_list_type;
    
   BEGIN
   
   
      v_utc_date := p_app_server_date;
      
      
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
        'Updating PAYROLL_RUN non-reversals');


        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
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
                   AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
                   AND ft1.current_transaction_state_fk = 'Executed'
                   AND ft1.transaction_type_fk = tt1.transaction_type_cd
                   AND tt1.association_type = 'Impound');

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'OffloadedDebit',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE pr.payroll_run_status = 'PendingAutoRedebit'
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
              AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND tt1.association_type = 'Redebit');

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'AutoRedebitOffloaded',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE pr.payroll_run_status = 'PendingRedebit'
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
              AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND tt1.association_type = 'Redebit');

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'RedebitOffloaded',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE EXISTS (
            SELECT 'T'
            FROM psp_money_movement_transaction mmt1, psp_financial_transaction ft1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = 'EmployeeDdCredit');

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'OffloadedAll',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

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
                'Updating PAYROLL_RUN reversals');

      SELECT SYS_EXTRACT_UTC(FROM_TZ(FN_GET_PSP_TIMESTAMP,'US/Pacific')) INTO v_psp_date FROM DUAL;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE (pr.payroll_run_status = 'OffloadedAll' or pr.payroll_run_status = 'OffloadedDebit')
          AND EXISTS (
            SELECT
                'T'
            FROM psp_money_movement_transaction mmt1,
                 psp_financial_transaction ft1,
                 psp_transaction_type tt1
            WHERE ft1.payroll_run_fk = pr.payroll_run_seq
              AND mmt1.offload_batch_fk = p_offload_batch_id
              AND mmt1.initiation_date = p_offload_date
              AND mmt1.money_movement_transaction_seq = ft1.money_movement_transaction_fk
              AND mmt1.company_fk = ft1.company_fk
              AND ft1.settlement_date >= mmt1.initiation_date
              AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = tt1.transaction_type_cd
              AND ft1.transaction_type_fk = 'EmployerTaxDebit');

        FORALL indx IN 1 .. payroll_list.COUNT
            INSERT INTO PSP_ATFPAYROLLS_TO_PROCESS
                VALUES (FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, v_psp_date, p_user_id, v_psp_date, -1, payroll_list(indx));

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
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
              AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk = 'EmployeeDdReversalDebit');

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'ReversalsOffloaded',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE EXISTS (
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
              AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
              AND ft1.current_transaction_state_fk = 'Executed'
              AND ft1.transaction_type_fk in ('EmployerTaxDebit', 'EmployerTaxCredit'));

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'OffloadedAll',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE payroll_run_status != 'AutoRedebitOffloaded'
             AND EXISTS (
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
                   AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
                   AND ft1.current_transaction_state_fk = 'Executed'
                   AND ft1.transaction_type_fk in ('EmployerFeeDebit'))
           AND NOT EXISTS (
            SELECT
                      'T'
                      FROM psp_financial_transaction ft1
                     WHERE ft1.payroll_run_fk = pr.payroll_run_seq
                       AND ft1.transaction_type_fk in ('EmployerDdDebit', 'EmployerTaxDebit'));

        FORALL indx IN 1 .. payroll_list.COUNT
            UPDATE psp_payroll_run pr
            SET payroll_run_status = 'OffloadedAll',
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = v_utc_date
            WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;

        SELECT/*+parallel(8) */ pr.payroll_run_seq
                                    BULK COLLECT INTO payroll_list  from psp_payroll_run pr
        WHERE
                PR.PAYROLL_RUN_STATUS = 'OffloadedAll'
          AND
            EXISTS
                (
                    SELECT 'T'
                    FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
                         PSP_FINANCIAL_TRANSACTION FT1,
                         PSP_FINANCIAL_TRANSACTION FT2
                    WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
                      AND FT2.PAYROLL_RUN_FK = FT1.PAYROLL_RUN_FK
                      AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
                      AND MMT1.initiation_date =  p_offload_date
                      AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
                      AND mmt1.company_fk = ft1.company_fk
                      AND ft1.settlement_date >= mmt1.initiation_date
                      AND trunc(ft1.settlement_date) >= trunc(mmt1.initiation_date)
                      AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
                      AND FT1.TRANSACTION_TYPE_FK = 'EmployeeDdCredit'
                      AND FT2.CURRENT_TRANSACTION_STATE_FK = 'Completed'
                      AND FT2.SETTLEMENT_TYPE_CD <> 'ACH'
                      AND FT2.TRANSACTION_TYPE_FK = 'EmployerDdDebit'
                );


        FORALL indx IN 1 .. payroll_list.COUNT
                UPDATE psp_payroll_run pr
                SET PAYROLL_RUN_STATUS = 'Complete',
                    VERSION = VERSION + 1,
                    MODIFIER_ID = p_user_id,
                    MODIFIED_DATE = v_utc_date
                WHERE pr.payroll_run_seq = payroll_list(indx);

        payroll_list.DELETE;


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
                   'Writing Files');



    COMMIT;
END;
/

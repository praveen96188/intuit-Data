\set AUTOCOMMIT off
\echo :AUTOCOMMIT
set search_path to pspadm;
\timing

--explain (analyze,verbose) UPDATE
--    pspadm.psp_financial_transaction ft0
--SET current_transaction_state_fk = 'Executed',
--    VERSION = VERSION + 1
--WHERE ft0.settlement_date >= timestamp '2022-12-28 08:00:00'
--  AND EXISTS (
--        SELECT /*+ QB_NAME(SUBQ_MMT1) */
--            'T'
--        FROM pspadm.psp_money_movement_transaction mmt1
--        WHERE mmt1.money_movement_transaction_seq = ft0.money_movement_transaction_fk
--          AND mmt1.offload_batch_fk = 'b7c48409-b9c6-3ea4-e053-0100007fdf27'
--          AND mmt1.initiation_date = timestamp '2022-12-28 08:00:00'
--          AND mmt1.mm_transaction_amount>=0);
--
--rollback;

explain (analyze,verbose) INSERT /*+ APPEND */ INTO pspadm.PSP_FINANCIAL_TRANS_STATE
(FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
 CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
 REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
 GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
 TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
    (SELECT nextval('pspadm.perf_test'), 0, 'PerformanceTest', timestamp '2023-04-26 08:00:00', 'PerformanceTest', timestamp '2023-04-26 08:00:00', -1,
            timestamp '2023-04-26 08:00:00', NULL, NULL, FT.FINANCIAL_TRANSACTION_SEQ, 'Executed', NULL, ft.company_fk, ft.transaction_type_fk
     FROM pspadm.PSP_FINANCIAL_TRANSACTION FT,
          pspadm.PSP_MONEY_MOVEMENT_TRANSACTION MMT
     WHERE MMT.OFFLOAD_BATCH_FK = 'b7c48409-b9c6-3ea4-e053-0100007fdf27'
       AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
       AND FT.CURRENT_TRANSACTION_STATE_FK IN ('Executed', 'Completed')
       AND MMT.initiation_date =  timestamp '2022-12-28 08:00:00'
       AND FT.SETTLEMENT_DATE >= MMT.INITIATION_DATE
       AND date(FT.SETTLEMENT_DATE) >= date(MMT.INITIATION_DATE)
    );

rollback;


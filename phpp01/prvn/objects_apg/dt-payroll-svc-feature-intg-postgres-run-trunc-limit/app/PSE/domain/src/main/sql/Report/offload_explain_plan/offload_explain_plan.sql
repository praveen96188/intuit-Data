set pages 200
set lines 120
col object for a40
col operation for a40
col options for a30
--col position for 999,999
col cost for 999,999
col id for 99

alter session set nls_date_format='DD-MON-YY HH24:MI:SS';
select statement_id, count(*) from plan_table group by statement_id;

EXPLAIN PLAN 
    SET STATEMENT_ID = 'offload_plan_mmt' 
    INTO plan_table 
    FOR 
UPDATE /*+ USE_NL(@SUB_FT_COMP CP)
                   LEADING(MMT)
                   LEADING(@SUB_FT_COMP FT)
                   INDEX_RS(@SUB_FT_COMP FT PSP_FINANCIAL_TRANSACTION_FK10)
                   INDEX_RS (mmt PSP_MM_TRANSACTION_I2 )
                   NO_UNNEST(@SUB_FT_COMP) */
                 pspadm.psp_money_movement_transaction mmt
         SET offload_batch_fk = :p_offload_batch_id,
             status = 'Executed',
             VERSION = VERSION + 1,
             modifier_id = :p_user_id,
             modified_date = :v_utc_date
       WHERE mmt.offload_batch_fk IS NULL
         AND mmt.initiation_date = :p_offload_date
         AND mmt.money_movement_payment_method = 'ACHDirectDeposit'
         AND EXISTS (
                SELECT /*+ QB_NAME(SUB_FT_COMP)
                     INDEX_RS(FT PSP_FINANCIAL_TRANSACTION_FK10) */
                  'T'
                  FROM pspadm.psp_financial_transaction ft, pspadm.psp_company cp
                 WHERE ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                   AND ft.on_hold = 0
                   AND ft.settlement_date >= :p_offload_date                       -- can it be null?
                   AND ft.current_transaction_state_fk = 'Created'
                   AND ft.company_fk = cp.company_seq
                   AND cp.offload_group_fk = :v_offload_group_id);

EXPLAIN PLAN
    SET STATEMENT_ID = 'offload_plan_edr'
    INTO plan_table
    FOR
   UPDATE
             (SELECT  /*+
                      QB_NAME(MAIN_EDR)                     
                     LEADING (@SEL$798CEC2A rec0 mmt0)
                     USE_HASH(@SEL$798CEC2A @SUBQ_MMT mmt0)
                    INDEX_RS(@SEL$798CEC2A @SUBQ_MMT mmt0 PSP_MONEY_MOVEMENT_TRANSAC_FK3)
                    UNNEST(@SUBQ_MMT)
                    MERGE(@MAIN_EDR)
                    */
                            rec0.entry_detail_record_seq, rec0.trace_number, rec0.n_a_c_h_a_file_fk,
                          rec0.record_data, rec0.VERSION, rec0.modifier_id, rec0.modified_date
                     FROM pspadm.psp_entry_detail_record rec0
                    WHERE rec0.n_a_c_h_a_file_fk IS NULL
                      AND rec0.initiation_date = :p_offload_date
                      -- AND REC0.RECORD_DATA IS NOT NULL
                      AND rec0.n_a_c_h_a_file_type = :p_nacha_file_type
                      AND EXISTS (
                             SELECT /*+ qb_name(SUBQ_MMT) 
                                        USE_HASH(rec0 mmt0)
                                        INDEX_RS(@SEL$798CEC2A rec0 PSP_ENTRY_DETAIL_RECORD_I2 )*/
                                         'T'
                               FROM pspadm.psp_money_movement_transaction mmt0
                              WHERE rec0.money_movement_transaction_fk =
                                                                 mmt0.money_movement_transaction_seq
                                AND mmt0.offload_batch_fk = :p_offload_batch_id)
                 ORDER BY rec0.legal_name,
                          rec0.company_fk,
                          rec0.n_a_c_h_a_batch_type,
                          rec0.record_data,
                          rec0.amount,
                          rec0.entry_detail_record_seq) src
            SET trace_number = DECODE (NVL (record_data, '0'), '0', NULL, pspadm.seq_trace_number.NEXTVAL),
                n_a_c_h_a_file_fk = :v_nacha_file_id,
                VERSION = VERSION + 1,
                modifier_id = :p_user_id,
                modified_date = :v_utc_date;

EXPLAIN PLAN
    SET STATEMENT_ID = 'offload_plan_ft'
    INTO plan_table
    FOR
UPDATE
                /*+
                   USE_NL (FT0 MMT1)
                   LEADING (MMT1 FT0)
                   UNNEST(@SUBQ_MMT1)
                   INDEX_RS (@SUBQ_MMT1 MMT1 PSP_MONEY_MOVEMENT_TRANSAC_FK3)
                   INDEX_RS (FT0 PSP_FINANCIAL_TRANSACTION_FK10)
                */
           pspadm.psp_financial_transaction ft0
         SET current_transaction_state_fk = 'Executed',
             VERSION = VERSION + 1,
             modifier_id = :p_user_id,
             modified_date = :v_utc_date
       WHERE ft0.current_transaction_state_fk = 'Created'
         AND ft0.settlement_date >= :p_offload_date
         AND EXISTS (
                SELECT /*+ QB_NAME(SUBQ_MMT1)
                                 */
                    'T'
                  FROM pspadm.psp_money_movement_transaction mmt1
                 WHERE mmt1.money_movement_transaction_seq = ft0.money_movement_transaction_fk
                   AND mmt1.offload_batch_fk = :p_offload_batch_id
                   AND mmt1.initiation_date = :p_offload_date);

spool offload_explain_plan_mmt.log
select sysdate from dual;
prompt MMT:
SELECT     id, parent_id, cardinality "Rows",
substr (lpad(' ', level-1) || operation || ' (' || options || ')',1,40 )  operation, 
cost, object_owner||'.'||object_name object
FROM       plan_table
START WITH id = 0 AND statement_id = 'offload_plan_mmt'
CONNECT BY PRIOR id = parent_id AND statement_id = 'offload_plan_mmt';
spool off

spool offload_explain_plan_edr.log
select sysdate from dual;
prompt EDR:
SELECT     id, parent_id, cardinality "Rows",
substr (lpad(' ', level-1) || operation || ' (' || options || ')',1,40 )  operation,
cost, object_owner||'.'||object_name object
FROM       plan_table
START WITH id = 0 AND statement_id = 'offload_plan_edr'
CONNECT BY PRIOR id = parent_id AND statement_id = 'offload_plan_edr';
spool off

spool offload_explain_plan_ft.log
select sysdate from dual;
prompt FT:
SELECT     id, parent_id, cardinality "Rows",
substr (lpad(' ', level-1) || operation || ' (' || options || ')',1,40 )  operation,
cost, object_owner||'.'||object_name object
FROM       plan_table
START WITH id = 0 AND statement_id = 'offload_plan_ft'
CONNECT BY PRIOR id = parent_id AND statement_id = 'offload_plan_ft';
spool off

rollback;


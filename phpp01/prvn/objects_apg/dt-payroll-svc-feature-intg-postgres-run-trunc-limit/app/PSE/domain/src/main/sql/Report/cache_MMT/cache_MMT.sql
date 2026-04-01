SET TIME ON
SET TIMI ON
spool cache_MMT.log

PROMPT EDR RECORDS

                SELECT
                        count(*)
                 FROM pspadm.PSP_ENTRY_DETAIL_RECORD rec0
                WHERE
                    rec0.N_A_C_H_A_FILE_FK is null
                    AND rec0.initiation_date = SYS_EXTRACT_UTC(TO_TIMESTAMP(sysdate))
                   -- AND REC0.RECORD_DATA IS NOT NULL
                   -- AND rec0.N_A_C_H_A_FILE_TYPE = p_nacha_file_type
                   -- AND EXISTS  ( SELECT 'T'
                   --                   FROM pspadm.PSP_MONEY_MOVEMENT_TRANSACTION mmt0
                   --                  WHERE REC0.MONEY_MOVEMENT_TRANSACTION_FK = MMT0.MONEY_MOVEMENT_TRANSACTION_SEQ
                   --                    AND mmt0.offload_batch_fk is null)
/

PROMPT MMT RECORDS

  SELECT /*+ USE_NL(@SUB_FT_COMP CP)
                                 LEADING(MMT)
                                 LEADING(@SUB_FT_COMP FT)
                                 INDEX_RS(@SUB_FT_COMP FT PSP_FINANCIAL_TRANSACTION_FK10)                
                                 INDEX_RS (mmt PSP_MM_TRANSACTION_I2 ) 
                                 NO_UNNEST(@SUB_FT_COMP) */
                COUNT (*)  
          FROM pspadm.psp_money_movement_transaction mmt
         WHERE mmt.offload_batch_fk IS NULL
           AND mmt.initiation_date = SYS_EXTRACT_UTC(TO_TIMESTAMP(sysdate))
           AND mmt.money_movement_payment_method = 'ACHDirectDeposit'
           AND EXISTS (
                  SELECT /*+ QB_NAME(SUB_FT_COMP)
                                   INDEX_RS(FT PSP_FINANCIAL_TRANSACTION_FK10) */
                                'T'
                    FROM pspadm.psp_financial_transaction ft,
                         pspadm.psp_company_service cs,
                         pspadm.psp_ddcompany_service_info dd
                   WHERE ft.money_movement_transaction_fk =
                                                    mmt.money_movement_transaction_seq
                     AND ft.on_hold = 0
                     AND ft.settlement_date >=
                            SYS_EXTRACT_UTC(TO_TIMESTAMP(sysdate))
                     AND ft.current_transaction_state_fk = 'Created'
                     AND ft.company_fk = cs.company_fk
                     AND cs.company_service_seq = dd.ddcompany_service_info_seq
                     --AND dd.offload_group_fk = v_offload_group_id
                     AND cs.service_fk = 'DirectDeposit');

/

spool off


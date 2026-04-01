CREATE OR REPLACE PACKAGE BODY PK_GEMS_ACCOUNTS_RECEIVABLE
AS
   -- Simple procedure to supercede an existing gems upload batch
   -- (set batch state to 'Superceded' and disassociate all FTS records)
   -- (if p_gems_upload_batch_id == 0, then procedure takes no action)
   PROCEDURE prc_supercede_upload_batch (
      p_old_upload_batch_id   IN   NUMBER,   -- existing gems upload batch to supercede (0 if none)
      p_user_id               IN   VARCHAR2, -- for audit purposes
      p_app_server_date       IN   TIMESTAMP -- UTC Date
   )
   IS
      v_old_batch_key   VARCHAR2 (100);
   BEGIN
      IF p_old_upload_batch_id != 0
      THEN
         BEGIN
            SELECT gems_upload_batch_seq
              INTO v_old_batch_key
              FROM psp_gems_upload_batch
             WHERE batch_id = p_old_upload_batch_id;

            UPDATE psp_gems_upload_batch
               SET VERSION = VERSION + 1,
                   modifier_id = p_user_id,
                   modified_date = p_app_server_date,
                   upload_status = 'Superceded',
                   status_effective_date = p_app_server_date
             WHERE gems_upload_batch_seq = v_old_batch_key;

            UPDATE psp_financial_trans_state
               SET gems_upload_batch_fk = NULL
             WHERE gems_upload_batch_fk = v_old_batch_key;
         EXCEPTION
            -- potentional of select returning zero data, do nothing...
            WHEN NO_DATA_FOUND
            THEN
               NULL;
         END;
      END IF;
   END prc_supercede_upload_batch;

/*****************************************************************************/

   -- Create the new gems upload batch for this job
   PROCEDURE prc_create_upload_batch (
      p_user_id           IN   VARCHAR2, -- for audit purposes
      p_app_server_date   IN   TIMESTAMP -- UTC Date
   )
   IS
   BEGIN
      INSERT INTO psp_gems_upload_batch
                  (gems_upload_batch_seq, VERSION, creator_id, created_date, modifier_id,
                   modified_date, realm_id, batch_id, batch_type, upload_status,
                   status_effective_date
                  )
           VALUES (g_gems_upload_batch_key, 0, p_user_id, p_app_server_date, p_user_id,
                   p_app_server_date, -1, g_batch_id, 'Daily', 'InProcess',
                   g_psp_date
                  );
   END prc_create_upload_batch;

/*****************************************************************************/

   -- Update the financial transaction state records with the new gems
   -- upload batch key for the financial transactions being reported
   -- (uses data driven rules via the ledger account and posting rules tables)
   -- 1/17/2014 : Fixing the update to pick up non-ach txn's 
   PROCEDURE prc_assoc_fin_txn_states (
         p_user_id           IN   VARCHAR2, -- for audit purposes
         p_app_server_date   IN   TIMESTAMP -- UTC Date
      )
      IS
      BEGIN
         UPDATE psp_financial_trans_state fts
            SET fts.gems_upload_batch_fk = g_gems_upload_batch_key,
                VERSION = VERSION + 1,
                modifier_id = p_user_id,
                modified_date = p_app_server_date
          WHERE fts.gems_upload_batch_fk IS NULL
            AND TRUNC (fts.transaction_state_eff_date) >= trunc(g_psp_date) - g_lookback_days
            AND fts.transaction_state_eff_date >= trunc(g_psp_date) - g_lookback_days
            AND fts.transaction_state_fk in ('Executed','Voided','Completed')
            AND EXISTS (
                   SELECT 'T'
                     FROM psp_financial_transaction ft, psp_posting_rule pr, psp_ledger_account la
                    WHERE ft.financial_transaction_seq = fts.financial_transaction_fk
                      AND ft.company_fk = fts.company_fk
                      AND pr.transaction_state_fk = fts.transaction_state_fk
                      AND pr.ledger_account_fk = la.ledger_account_cd
                      AND pr.transaction_type_fk = ft.transaction_type_fk
                      AND la.reporting_frequency = 'Daily'
                      AND la.ledger_account_type IN ('Income', 'SUTax')
                      AND ft.sku IS NOT NULL);
                      
                                      
   END prc_assoc_fin_txn_states;

/*****************************************************************************/

   -- Calculate the GEMS A/R numbers
   PROCEDURE prc_calc_reveivables (
      p_cur_gems_daily_upload   OUT   sys_refcursor -- result set to return to client
   )
   IS
   BEGIN
      OPEN p_cur_gems_daily_upload FOR
         SELECT   g_batch_id batch_id, ft.sku,
                  SUM (DECODE (la.ledger_account_type,
                               'Income', ft.sku_quantity
                                * DECODE (pr.credit_debit_ind,
                                          'C', DECODE (la.balance_calculation_rule,
                                                       'CreditAddsToBalance', 1,
                                                       -1
                                                      ),
                                          'D', DECODE (la.balance_calculation_rule,
                                                       'DebitAddsToBalance', 1,
                                                       -1
                                                      )
                                         ),
                               0
                              )
                      ) sku_quantity,
                  SUM (DECODE (la.ledger_account_type,
                               'Income', ft.financial_transaction_amount
                                * DECODE (pr.credit_debit_ind,
                                          'C', DECODE (la.balance_calculation_rule,
                                                       'CreditAddsToBalance', 1,
                                                       -1
                                                      ),
                                          'D', DECODE (la.balance_calculation_rule,
                                                       'DebitAddsToBalance', 1,
                                                       -1
                                                      )
                                         ),
                               0
                              )
                      ) income_amt,
                  SUM (DECODE (la.ledger_account_type,
                               'SUTax', ft.financial_transaction_amount
                                * DECODE (pr.credit_debit_ind,
                                          'C', DECODE (la.balance_calculation_rule,
                                                       'CreditAddsToBalance', 1,
                                                       -1
                                                      ),
                                          'D', DECODE (la.balance_calculation_rule,
                                                       'DebitAddsToBalance', 1,
                                                       -1
                                                      )
                                         ),
                               0
                              )
                      ) tax_amt
             FROM psp_financial_transaction ft,
                  psp_financial_trans_state fts,
                  psp_posting_rule pr,
                  psp_ledger_account la
            WHERE ft.financial_transaction_seq = fts.financial_transaction_fk
              AND ft.company_fk = fts.company_fk
              AND fts.transaction_state_eff_date >= trunc(g_psp_date) - g_lookback_days
            --  AND ft.current_transaction_state_fk = fts.transaction_state_fk
              AND fts.transaction_state_fk = pr.transaction_state_fk
              AND fts.gems_upload_batch_fk = g_gems_upload_batch_key
              AND pr.ledger_account_fk = la.ledger_account_cd
              AND pr.transaction_type_fk = ft.transaction_type_fk
              AND ft.sku IS NOT NULL
              AND la.reporting_frequency = 'Daily'
              AND la.ledger_account_type IN ('Income', 'SUTax')
         GROUP BY ft.sku
         ORDER BY ft.sku;
   END prc_calc_reveivables;

/*****************************************************************************/

   -- Main procedure that will be called from the batch job
   -- 1. Supercede an existing gems upload batch (if any - 0 for none)
   -- 2. Create new gems upload batch.
   -- 3. Update all appropriate FTS records with new gems upload batch.
   -- 4. Generate daily gems accounts receivable numbers.
   PROCEDURE prc_main (
      p_cur_gems_daily_upload   OUT      sys_refcursor, -- result set to return to client
      p_old_upload_batch_id     IN       NUMBER,        -- existing gems upload batch to supercede (0 if want new)
      p_user_id                 IN       VARCHAR2,      -- for audit purposes
      p_app_server_date         IN       TIMESTAMP      -- UTC Date
   )
   IS
   BEGIN
      SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp)
        INTO g_psp_date
        FROM DUAL;

      BEGIN  
        SELECT TO_NUMBER(COALESCE(SYSTEM_PARAMETER_VALUE, '5'))
          INTO g_lookback_days
          FROM PSP_SYSTEM_PARAMETER
         WHERE SYSTEM_PARAMETER_CD = 'GEMS_ACCOUNTS_RECEIVABLE_LOOKBACK_DAYS';
       EXCEPTION
        WHEN NO_DATA_FOUND 
        THEN g_lookback_days := 5;
        WHEN OTHERS
        THEN g_lookback_days := 5;
       END;

      SELECT fn_format_sysguid (SYS_GUID ())
        INTO g_gems_upload_batch_key
        FROM DUAL;

      SELECT seq_gems_upload_batch_id.NEXTVAL
        INTO g_batch_id
        FROM DUAL;

      prc_supercede_upload_batch (p_old_upload_batch_id, p_user_id, p_app_server_date);
      prc_create_upload_batch (p_user_id, p_app_server_date);
      prc_assoc_fin_txn_states (p_user_id, p_app_server_date);
      prc_calc_reveivables (p_cur_gems_daily_upload);
   END prc_main;
END pk_gems_accounts_receivable;
/

CREATE OR REPLACE PACKAGE PK_GEMS_ACCOUNTS_RECEIVABLE
AS
/******************************************************************************
   NAME:       pk_gems_accounts_receivable
   PURPOSE:    To calculate GEMS DAILY A/R numbers for revenue and tax
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09/18/2009  Tushar Thakker   1. Created this package
   1.1        09/23/2009  Ken Paul         2. Added gems upload batch creation
                                              and recalculating a batch

   INPUT: GEMS upload batch id
   OUTPUT: Reference cursor with actual numbers that go in the file
           Output column names are: sku, sku_quantity, income_amt, tax_amt
   RETURNED VALUE:
   CALLED BY:
   ASSUMPTIONS:
   LIMITATIONS:
   ALGORITHM : Java batch program passes GemsUploadBatchId for the day's batch
               First update Financial_trans_state with gems_upload_batch_id,
               then sum all the revenue and tax numbers for the SKUs and return
               to java
   NOTES:
******************************************************************************/
   g_gems_upload_batch_key   VARCHAR2 (100); -- new guid for gems upload batch
   g_batch_id                NUMBER;         -- new gems upload batch id
   g_psp_date                TIMESTAMP;      -- current system date and time adjusted by PSPDate offset
   g_lookback_days           NUMBER;         -- number of days back from g_psp_date to look for candidate financial transaction states

   -- Simple procedure to supercede an existing gems upload batch
   -- (set batch state to 'Superceded' and disassociate all FTS records)
   PROCEDURE prc_supercede_upload_batch (
      p_old_upload_batch_id   IN   NUMBER,   -- existing gems upload batch to supercede (0 if none)
      p_user_id               IN   VARCHAR2, -- for audit purposes
      p_app_server_date       IN   TIMESTAMP -- UTC Date
   );

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
   );
END pk_gems_accounts_receivable;
/
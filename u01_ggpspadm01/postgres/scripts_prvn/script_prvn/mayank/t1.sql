explain plan for
SELECT *
FROM
  (SELECT /*+ FIRST_ROWS gather_plan_statistics */ a.*, rownum rnum
   FROM
     (SELECT txHeaders.tx_id AS id,
             txHeaders.tx_type_id AS txIdColumn,
             txHeaders.tx_date AS txDateColumn,
             txHeaders.due_date,
             NULL AS detailsDueDate,
             CASE
                 WHEN txHeaders.tx_type_id = 8 THEN txDetails.amount
                 WHEN txDetails.is_purchase = 1 THEN -txHeaders.amount
                 ELSE txHeaders.amount
             END AS totalamountcolumn,
             txHeaders.home_amount AS homeAmount,
             txHeaders.tax_amount AS taxAmount,
             txHeaders.net_amount AS netAmount,
             txHeaders.is_no_post AS isNoPost,
             txHeaders.ship_date AS shipDate,
             txHeaders.name_id AS nameId,
             txHeaders.doc_num AS doc_num,
             txHeaders.memo_text AS memo,
             CASE
                 WHEN txHeaders.tx_type_id IN (3,
                                               34,
                                               54) THEN (CASE
                                                             WHEN txHeaders.is_voided = 1 THEN 'void'
                                                             WHEN txHeaders.is_reversed = 1 THEN 'reversed'
                                                             ELSE 'paid'
                                                         END)
                 WHEN txHeaders.tx_type_id IN (1,
                                               31,
                                               48,
                                               50) THEN 'paid'
                 WHEN txHeaders.tx_type_id IN (25,
                                               13,
                                               29,
                                               30) THEN (CASE
                                                              WHEN txHeaders.invoice_id IS NOT NULL THEN 'ccbt_applied'
                                                              ELSE 'ccbt_open'
                                                          END)
                 WHEN txHeaders.tx_type_id = 4 THEN (CASE
                                                         WHEN txHeaders.is_ar_paid = 1
                                                              AND txHeaders.is_voided = 1 THEN 'void'
                                                         WHEN txHeaders.is_ar_paid = 1
                                                              AND txHeaders.is_reversed = 1 THEN 'reversed'
                                                         WHEN txHeaders.is_ar_paid = 1 THEN 'paid'
                                                         WHEN txHeaders.is_ar_paid = 2
                                                              AND txHeaders.due_date < trunc(CURRENT_DATE) THEN 'overdue'
                                                         WHEN txHeaders.open_balance < txHeaders.amount THEN 'partial'
                                                         ELSE 'inv_open'
                                                     END)
                 WHEN txHeaders.tx_type_id = 33 THEN (CASE
                                                          WHEN txHeaders.is_ar_paid = 1
                                                               AND txHeaders.is_voided = 1 THEN 'oth_void'
                                                          WHEN txHeaders.is_ar_paid = 1
                                                               AND txHeaders.is_reversed = 1 THEN 'oth_reversed'
                                                          WHEN txHeaders.is_ar_paid = 1 THEN 'oth_applied'
                                                          WHEN txHeaders.open_balance < txHeaders.amount THEN 'oth_partial'
                                                          ELSE 'oth_unapplied'
                                                      END)
                 WHEN txHeaders.tx_type_id = 12 THEN (CASE
                                                          WHEN txHeaders.is_ap_paid = 1
                                                               AND txHeaders.is_voided = 1 THEN 'oth_void'
                                                          WHEN txHeaders.is_ap_paid = 1
                                                               AND txHeaders.is_reversed = 1 THEN 'oth_reversed'
                                                          WHEN txHeaders.is_ap_paid = 1 THEN 'oth_applied'
                                                          WHEN txHeaders.open_balance > txHeaders.amount THEN 'oth_partial'
                                                          ELSE 'oth_unapplied'
                                                      END)
                 WHEN txHeaders.tx_type_id = 9 THEN (CASE
                                                         WHEN txHeaders.is_ap_paid = 1
                                                              AND txHeaders.is_voided = 1 THEN 'void'
                                                         WHEN txHeaders.is_ap_paid = 1
                                                              AND txHeaders.is_reversed = 1 THEN 'reversed'
                                                         WHEN txHeaders.is_ap_paid = 1 THEN 'paid'
                                                         WHEN txHeaders.is_ap_paid = 2
                                                              AND txHeaders.due_date < trunc(CURRENT_DATE) THEN 'overdue'
                                                         WHEN txHeaders.open_balance < txHeaders.amount THEN 'partial'
                                                         ELSE 'bill_open'
                                                     END)
                 WHEN txHeaders.tx_type_id = 35 THEN (CASE
                                                          WHEN txHeaders.invoice_id IS NOT NULL THEN 'est_applied'
                                                          WHEN txHeaders.est_status = 4 THEN 'est_rejected'
                                                          WHEN txHeaders.est_status = 3 THEN 'est_closed'
                                                          WHEN txHeaders.est_status = 2 THEN 'est_accepted'
                                                          WHEN txHeaders.est_status = 1
                                                               AND trunc(CURRENT_DATE) > txHeaders.expiration_date THEN 'est_expired'
                                                          WHEN txHeaders.est_status = 1 THEN 'est_open'
                                                          ELSE NULL
                                                      END)
                 WHEN txHeaders.tx_type_id = 46 THEN (CASE
                                                          WHEN txHeaders.bill_id IS NOT NULL THEN 'est_applied'
                                                          WHEN txHeaders.po_status = 2 THEN 'est_closed'
                                                          WHEN txHeaders.po_status = 1
                                                               AND trunc(CURRENT_DATE) > txHeaders.expiration_date THEN 'est_expired'
                                                          WHEN txHeaders.po_status = 1 THEN 'est_open'
                                                          ELSE NULL
                                                      END)
                 WHEN txHeaders.tx_type_id = 63 THEN (CASE
                                                           WHEN txHeaders.bill_id IS NOT NULL THEN 'pmt_applied'
                                                           ELSE 'open'
                                                       END)
                 WHEN txHeaders.tx_type_id IN (7,
                                               14,
                                               15) THEN (CASE
                                                             WHEN (txHeaders.is_ar_paid = 1
                                                                   OR txHeaders.is_ap_paid = 1)
                                                                  AND txHeaders.is_voided = 1 THEN 'pmt_void'
                                                             WHEN (txHeaders.is_ar_paid = 1
                                                                   OR txHeaders.is_ap_paid = 1)
                                                                  AND txHeaders.is_reversed = 1 THEN 'pmt_reversed'
                                                             WHEN (txHeaders.is_ar_paid = 1
                                                                   OR txHeaders.is_ap_paid = 1) THEN 'pmt_applied'
                                                             WHEN (txHeaders.is_ar_paid = 2
                                                                   OR txHeaders.is_ap_paid = 2)
                                                                  AND txHeaders.open_balance < txHeaders.amount THEN 'pmt_partial'
                                                             WHEN (txHeaders.is_ar_paid = 2
                                                                   OR txHeaders.is_ap_paid = 2) THEN 'pmt_unapplied'
                                                             ELSE NULL
                                                         END)
                 WHEN txHeaders.tx_type_id = 32 THEN (CASE
                                                          WHEN txHeaders.is_voided = 1 THEN 'void'
                                                          WHEN txHeaders.is_reversed = 1 THEN 'reversed'
                                                          WHEN txHeaders.pmt_disposition_status = 4 THEN 'paid_disputed'
                                                          WHEN txHeaders.pmt_disposition_status = 5 THEN 'pai
                                                                   d_deposit_hold'
                                                          WHEN txHeaders.pmt_disposition_status = 7 THEN 'paid_full_release'
                                                          WHEN txHeaders.pmt_disposition_status = 8 THEN 'paid_partial_release'
                                                          WHEN txHeaders.pmt_disposition_status = 9 THEN 'paid_no_release'
                                                          WHEN txHeaders.pmt_disposition_status = 6 THEN 'paid_deposit_failed'
                                                          WHEN txHeaders.pmt_disposition_status = 1 THEN 'paid_payment_failed'
                                                          WHEN txHeaders.pmt_disposition_status = 2 THEN 'paid_payment_failed'
                                                          WHEN txHeaders.pmt_disposition_status = 3 THEN 'paid_disputed'
                                                          WHEN txHeaders.pmt_disposition_status = 10 THEN 'paid_disputed_win'
                                                          WHEN txHeaders.pmt_disposition_status = 11 THEN 'paid_disputed_lose'
                                                          WHEN txHeaders.pmt_disposition_status = 12 THEN 'paid_deposit_resolved'
                                                          ELSE 'paid'
                                                      END)
                 ELSE (NULL)
             END AS TxnStatusColumn,
             NVL(txHeaders.open_balance, 0) AS openbalancecolumn,
             txHeaders.accepted_date,
             txHeaders.accepted_by,
             txHeaders.expiration_date,
             txHeaders.currency_type
      FROM TxHeaders_1 txHeaders,
           TxDetails_1 txDetails
      WHERE txHeaders.tx_id = txDetails.tx_id
        AND ((txDetails.tx_id, txDetails.sequence) IN
               (SELECT txDetails.tx_id,
                       CASE
                           WHEN txDetails.tx_type_id = 8 THEN txDetails.sequence
                           ELSE 0
                       END
                FROM TxDetails_1 txDetails
                WHERE txDetails.customer_id = 6067 ))
      ORDER BY TxHeaders.tx_date DESC) a
   WHERE rownum <= 150 )
WHERE rnum >= 1;

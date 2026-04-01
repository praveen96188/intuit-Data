spool CatalogDrivenDetailReport_customers.log
set timing on
set trimspool on
alter session set current_schema=qbo_data;

exec dbms_session.set_identifier(85766581);

select distinct tx_type_id from txheaders_1;

/* DetailReportGenerator.generate com.intuit.qbo.reports.CatalogDrivenDetailReport */
SELECT Customers.full_name,
       TO_CHAR(TxDetails.tx_date, 'MM/DD/YYYY'),
       ARAPCreditPmntChargeLinks.charge_tx_id,
       TxDetails.tx_type_id,
       TxDetails.tx_id,
       TxHeaders.memo_text,
       TxDetails.sequence,
       ARAPCreditPmntChargeLinks.amount,
       ARAPCreditPmntChargeLinks.payment_tx_id,
       ARAPCreditPmntChargeLinks.credit_tx_id,
       TxHeaders.doc_num,
       TxDetails.amount,
       TxDetails.open_balance
FROM TxDetails_1 TxDetails,
     Customers_1 Customers,
     ARAPCreditPmntChargeLinks_1 ARAPCreditPmntChargeLinks,
     TxHeaders_1 TxHeaders,
     Accounts_1 Accounts
WHERE ((TxHeaders.tx_type_id IN ( (SELECT /*+ cardinality(t 10) */ COLUMN_VALUE FROM table(qbo_utils.str2tbl('34,54,29,4,35,8,33,3,7,27')) t)))
       AND TxDetails.tx_date < to_date('05/30/2018 00:00:00','MM/DD/YYYY HH24:MI:SS')
       AND (TxDetails.sequence = 0
            AND TxDetails.tx_type_id IN (4, 7, 27, 33)
            OR (TxDetails.tx_type_id=8 AND Accounts.account_type_id=1))
       AND (TxDetails.tx_id NOT IN
              (SELECT TxDetails2.tx_id
               FROM TxDetails_1 TxDetails2
               WHERE TxDetails2.tx_type_id IN (4)
                 AND TxDetails2.tx_id NOT IN
                   (SELECT ARAPCreditPmntChargeLinks.charge_tx_id
                    FROM ARAPCreditPmntChargeLinks_1 ARAPCreditPmntChargeLinks)))
       AND TxDetails.is_no_post IS NULL
       AND TxDetails.customer_id IS NOT NULL)
  AND TxDetails.customer_id=Customers.name_id(+)
  AND TxDetails.tx_id=TxHeaders.tx_id
  AND TxHeaders.tx_id=ARAPCreditPmntChargeLinks.payment_tx_id(+)
  AND TxDetails.account_id=Accounts.account_id(+)
ORDER BY Customers.full_name,
         TxDetails.tx_date,
         ARAPCreditPmntChargeLinks.charge_tx_id
/ 
spool off 

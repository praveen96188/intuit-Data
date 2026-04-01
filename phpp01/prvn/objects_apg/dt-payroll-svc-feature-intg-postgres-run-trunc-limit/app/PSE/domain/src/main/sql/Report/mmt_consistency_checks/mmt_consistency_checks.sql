set pause off;
set feedback off;
set serveroutput on size 1000000;
set linesize 1000;
column status format a15;
column tax_payment_status format a15;
column initiation_date format a28;
column amount-mm_transaction_amount heading DIFFERENCE;
column amount heading "EDR AMOUNT";
column mm_transaction_amount heading "MMT AMOUNT";
column money_movement_transaction_seq format a36;

--
-- DD MMT is  associated with pending offload batch when it should not be
--
select 'DD MMT is  associated with pending offload batch when it should not be' AS Problem, mm_transaction_amount, mm_transaction_amount, 0, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
        inner join psp_offload_batch ob on ob.offload_batch_seq = mmt.offload_batch_fk and ob.status_cd = 'InProcess'
where money_movement_payment_method = 'ACHDirectDeposit' and
          status != 'Created' and 
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- DD MMT is not associated with offload batch when it should be
--
select 'DD MMT is not associated with offload batch when it should be' AS Problem, mm_transaction_amount, mm_transaction_amount, 0, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
where money_movement_payment_method = 'ACHDirectDeposit' and
          offload_batch_fk is null and
          status = 'Created' and 
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- Tax Pmt MMT is  associated with pending offload batch when it should not be
--
select 'Tax Pmt MMT is  associated with pending offload batch when it should not be' AS Problem, mm_transaction_amount, mm_transaction_amount, 0, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
        inner join psp_offload_batch ob on ob.offload_batch_seq = mmt.offload_batch_fk and ob.status_cd = 'InProcess'
where money_movement_payment_method = 'ACHCredit' and
          tax_payment_status not in ('ReadyToSend', 'Ignore') and 
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- Tax Pmt MMT is not associated with offload batch when it should be
--
select 'Tax Pmt MMT is not associated with offload batch when it should be' AS Problem, mm_transaction_amount, mm_transaction_amount, 0, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
where money_movement_payment_method = 'ACHCredit' and
          offload_batch_fk is null and
          tax_payment_status in ('ReadyToSend', 'Ignore') and 
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- EDR is associated with nacha file that does not match offload batch in MMT
--
select 'EDR is associated with nacha file that does not match offload batch in MMT' AS Problem, mm_transaction_amount, amount, amount - mm_transaction_amount, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
        inner join psp_offload_batch ob on ob.offload_batch_seq = mmt.offload_batch_fk    
        inner join psp_entry_detail_record edr on edr.initiation_date = mmt.initiation_date and edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
        inner join psp_nachafile nf on nf.nachafile_seq = edr.n_a_c_h_a_file_fk 
where money_movement_payment_method in ('ACHCredit', 'ACHDirectDeposit') and
           nf.offload_batch_fk != ob.offload_batch_seq  and  
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- EDR is not associated with nacha file when it should be
--
select 'EDR is not associated with nacha file when it should be' AS Problem, mm_transaction_amount, amount, amount - mm_transaction_amount, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
        inner join psp_offload_batch ob on ob.offload_batch_seq = mmt.offload_batch_fk    
        inner join psp_entry_detail_record edr on edr.initiation_date = mmt.initiation_date and edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
        left join psp_nachafile nf on nf.nachafile_seq = edr.n_a_c_h_a_file_fk 
where money_movement_payment_method in ('ACHCredit', 'ACHDirectDeposit') and
           nf.offload_batch_fk is null and
          tax_payment_status != 'Ignore' and
          mm_transaction_amount != 0 and
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- EDR is  associated with nacha file when it should not be
--
select 'EDR is  associated with nacha file when it should not be' AS Problem, mm_transaction_amount, amount, amount - mm_transaction_amount, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
        inner join psp_offload_batch ob on ob.offload_batch_seq = mmt.offload_batch_fk    
        inner join psp_entry_detail_record edr on edr.initiation_date = mmt.initiation_date and edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
        left join psp_nachafile nf on nf.nachafile_seq = edr.n_a_c_h_a_file_fk 
where money_movement_payment_method in ('ACHCredit', 'ACHDirectDeposit') and
           nf.offload_batch_fk is not null and
          tax_payment_status = 'Ignore' and  
          mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
UNION ALL
--
-- EDR amount is different than MMT amount
--
select 'EDR amount is different than MMT amount' AS Problem, mm_transaction_amount, amount, amount - mm_transaction_amount, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
         inner join psp_entry_detail_record edr on edr.initiation_date = mmt.initiation_date and edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
where money_movement_payment_method = 'ACHCredit' and
          mm_transaction_amount <> edr.amount and
          edr.txp_record_data is not null and
          mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2
UNION ALL
--
-- EDR initiation_date is different than MMT initiation_date
--
select 'EDR initiation_date is different than MMT initiation_date', mm_transaction_amount, amount, 0, edr.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq --mmt.*, edr.*
from psp_money_movement_transaction mmt
         left join psp_entry_detail_record edr on edr.initiation_date = mmt.initiation_date and edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
where money_movement_payment_method = 'ACHCredit' and
          edr.initiation_date is null and
          mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2
UNION ALL
--
-- MMT initiation date has incorrect time
--
select 'MMT initiation date has incorrect time', mm_transaction_amount, mm_transaction_amount, 0, mmt.initiation_date, mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq --mmt.*, edr.*
from psp_money_movement_transaction mmt
where mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2 and
         (extract(hour from initiation_date) < 7 or
           extract(hour from initiation_date) > 8 or
           extract(minute from initiation_date) != 0 or
           extract(second from initiation_date) != 0)
UNION ALL
--
--  Executed MMT was updated by another process
--
select  'Executed MMT was updated by another process', mm_transaction_amount,  mm_transaction_amount, 0, mmt.initiation_date,  mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq
from psp_money_movement_transaction mmt
where mmt.initiation_date between current_timestamp - 2 and current_timestamp + 1 and
      status = 'Executed'  and
      modifier_id not in ('EftpsFileCommBatchJob', 'PrintedCheckBatchJob', 'EftpsPaymentsBatchJob', 'AchOffloadBatchJob') and
      money_movement_payment_method NOT IN ('HPDE', 'PostBalfHPDERefund')
UNION ALL
--
-- DD MMT has OnHold status but no unexpired on hold reason
--
select 'DD MMT has OnHold status but no unexpired on hold reason', mm_transaction_amount,  mm_transaction_amount, 0, mmt.initiation_date,  mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq --mmt.*, edr.*
from psp_money_movement_transaction mmt
where mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2 and
      money_movement_payment_method = 'ACHDirectDeposit' and
      status = 'OnHold' and
       not exists (select 'T' from psp_on_hold_reason ohr where ohr.company_fk = mmt.company_fk and expiration_date is null)
UNION ALL
--
-- ACH MMT has created status but should be on hold
--
select 'ACH MMT has created status but should be on hold', mm_transaction_amount,  mm_transaction_amount, 0, mmt.initiation_date,  mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq --mmt.*, edr.*
from psp_money_movement_transaction mmt
where mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2 and
      money_movement_payment_method = 'ACHDirectDeposit' and
      status = 'Created' and
      not exists (select 'T' from psp_financial_transaction ft where ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq and on_hold = 0 and settlement_date > mmt.initiation_date - 20 and rownum < 2) and
      exists (select 'T' from psp_financial_transaction ft where ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq and settlement_date > mmt.initiation_date - 20 and rownum < 2)
UNION ALL
--
-- MMT has on hold status but should be created
--
select 'ACH MMT has on hold status but should be created', mm_transaction_amount,  mm_transaction_amount, 0, mmt.initiation_date,  mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq --mmt.*, edr.*
from psp_money_movement_transaction mmt
where mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2 and
      money_movement_payment_method = 'ACHDirectDeposit' and
      status = 'OnHold' and
      exists (select 'T' from psp_financial_transaction ft where ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq and on_hold = 0 and settlement_date > mmt.initiation_date - 20 and rownum < 2)
UNION ALL
--
-- MMT has on hold status but no tax payment on hold reason
--
select 'ACH MMT has on hold status but should be created', mm_transaction_amount,  mm_transaction_amount, 0, mmt.initiation_date,  mmt.initiation_date, status, tax_payment_status, mmt.money_movement_transaction_seq --mmt.*, edr.*
from psp_money_movement_transaction mmt
where mmt.initiation_date between current_timestamp - 2 and current_timestamp + 2 and
      money_movement_payment_method != 'ACHDirectDeposit' and
      tax_payment_status = 'OnHold' and
      not exists (select 'T' from psp_tax_payment_on_hold_reason tpor where tpor.money_movement_transaction_fk = mmt.money_movement_transaction_seq and expiration_date is null)
order by 5;


EXIT;

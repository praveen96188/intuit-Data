col "Init Date" format a12
col "Payment Method" format a20
col "Payment Template" format a20

set pagesize 3000

TTITLE CENTER 'State Tax Payments Forecast' SKIP 1 -
       CENTER _DATE SKIP 3

      
BREAK ON "Init Date" NODUP SKIP 1 -
      ON "Payment Template" NODUP SKIP 1


select
  to_char(trunc(initiation_date), 'DD-MON-YY') as "Init Date",
  payment_template_fk as "Payment Template", 
  count(*) as "Txn Count", 
  money_movement_payment_method as "Payment Method"
from
  pspadm.psp_money_movement_transaction mmt
where
  initiation_date >= trunc(systimestamp)
  and money_movement_payment_method in ('ACHCredit','CheckPayment')
group by
  initiation_date, payment_template_fk, money_movement_payment_method
order by 
  initiation_date, payment_template_fk, money_movement_payment_method;


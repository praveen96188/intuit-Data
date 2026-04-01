set linesize 220
set pagesize 1000
SET HEADING ON

col template head 'Template' format a18
col payment_method head 'Payment|Method' format a12
col PSID format a10
col fed_tax_id head 'FEIN' format a10
col money_movement_transaction_seq format a38
col initiation_date head 'Initiation|Date' format a11
col due_date head 'Due Date' format a11
col amount head 'Amount'
col tax_payment_status head 'Payment|Status' format a12
col agent_hold head 'Payment|Agent|Hold' format a9
col company_hold head 'Company|Hold' format a9
col comp_hold head 'Payment|Company|Hold' format a9
col enroll_hold head 'Payment|Enroll|Hold' format a9
col backdate_hold head 'Payment|BackDate|Hold' format a9
col money_movement_transaction_seq head 'MMT GUID'

TTITLE CENTER 'Missed Payments Report - High Priority Review' SKIP 1 -
       CENTER _DATE SKIP 3
    
BREAK ON due_date NODUP SKIP 1 -
      ON template NODUP SKIP 1 -
      ON tax_payment_status NODUP SKIP 1

spool missed_payment.log
-- list all payments that should be looked at immediately
select *
from (
  select 
    to_char(due_date, 'YYYY-MM-DD') as due_date,   
    payment_template_fk as template,
    tax_payment_status, 
    money_movement_payment_method as payment_method,
    source_company_id as PSID, 
    fed_tax_id, 
    mm_transaction_amount as amount, 
    (case (select count(ohr.on_hold_reason_seq) from pspadm.psp_on_hold_reason ohr where ohr.company_fk = c.company_seq and ohr.expiration_date is null) when 0 then 'N' else 'Y' end) as company_hold,
    (case when company_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as comp_hold,
    (case when agent_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as agent_hold,  
    (case when enroll_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as enroll_hold,
	(case when backdate_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as backdate_hold,
    money_movement_transaction_seq,
    to_char(initiation_date, 'YYYY-MM-DD') as initiation_date  
  from pspadm.psp_money_movement_transaction mmt
    join pspadm.psp_company c on company_fk = company_seq
    left join pspadm.psp_tax_payment_on_hold_reason enroll_hold on enroll_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and enroll_hold.on_hold_reason_cd = 'Enrollment' and enroll_hold.expiration_date is null
	left join pspadm.psp_tax_payment_on_hold_reason backdate_hold on backdate_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and backdate_hold.on_hold_reason_cd = 'BackDate' and backdate_hold.expiration_date is null
    left join pspadm.psp_tax_payment_on_hold_reason agent_hold on agent_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and agent_hold.on_hold_reason_cd = 'Agent' and agent_hold.expiration_date is null
    left join pspadm.psp_tax_payment_on_hold_reason company_hold on company_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and company_hold.on_hold_reason_cd = 'Company' and company_hold.expiration_date is null
  where initiation_date < trunc(systimestamp)
  and tax_payment_status in ('ReadyToSend','OnHold')
  )
where
  (tax_payment_status = 'ReadyToSend' and amount > 0)
  or (tax_payment_status = 'OnHold' and company_hold = 'N' and comp_hold = 'Y') 
  or (tax_payment_status = 'OnHold' and company_hold = 'N' and comp_hold = 'N' and agent_hold = 'N' and enroll_hold = 'N' and backdate_hold = 'N')
order by due_date, template, tax_payment_status desc, payment_method, amount desc;


TTITLE CENTER 'Missed Payments Report' SKIP 1 -
       CENTER _DATE SKIP 3


-- list all payments that should be fixed but are not immediate causes for concern
select *
from (
  select 
    to_char(due_date, 'YYYY-MM-DD') as due_date,   
    payment_template_fk as template,
    tax_payment_status, 
    money_movement_payment_method as payment_method,
    source_company_id as PSID, 
    fed_tax_id, 
    mm_transaction_amount as amount, 
    (case (select count(ohr.on_hold_reason_seq) from pspadm.psp_on_hold_reason ohr where ohr.company_fk = c.company_seq and ohr.expiration_date is null) when 0 then 'N' else 'Y' end) as company_hold,
    (case when company_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as comp_hold,
    (case when agent_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as agent_hold,  
    (case when enroll_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as enroll_hold,
	(case when backdate_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as backdate_hold,
    money_movement_transaction_seq,
    to_char(initiation_date, 'YYYY-MM-DD') as initiation_date  
  from pspadm.psp_money_movement_transaction mmt
    join pspadm.psp_company c on company_fk = company_seq
    left join pspadm.psp_tax_payment_on_hold_reason enroll_hold on enroll_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and enroll_hold.on_hold_reason_cd = 'Enrollment' and enroll_hold.expiration_date is null
	left join pspadm.psp_tax_payment_on_hold_reason backdate_hold on backdate_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and backdate_hold.on_hold_reason_cd = 'BackDate' and backdate_hold.expiration_date is null
    left join pspadm.psp_tax_payment_on_hold_reason agent_hold on agent_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and agent_hold.on_hold_reason_cd = 'Agent' and agent_hold.expiration_date is null
    left join pspadm.psp_tax_payment_on_hold_reason company_hold on company_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and company_hold.on_hold_reason_cd = 'Company' and company_hold.expiration_date is null
  where initiation_date < trunc(systimestamp)
  and tax_payment_status in ('ReadyToSend','OnHold')
  minus
  select *
  from (
    select 
      to_char(due_date, 'YYYY-MM-DD') as due_date,   
      payment_template_fk as template,
      tax_payment_status, 
      money_movement_payment_method as payment_method,
      source_company_id as PSID, 
      fed_tax_id, 
      mm_transaction_amount as amount, 
      (case (select count(ohr.on_hold_reason_seq) from pspadm.psp_on_hold_reason ohr where ohr.company_fk = c.company_seq and ohr.expiration_date is null) when 0 then 'N' else 'Y' end) as company_hold,
      (case when company_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as comp_hold,
      (case when agent_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as agent_hold,  
      (case when enroll_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as enroll_hold,
	  (case when backdate_hold.tax_payment_on_hold_reason_seq is null then 'N' else 'Y' end) as backdate_hold,
      money_movement_transaction_seq,
      to_char(initiation_date, 'YYYY-MM-DD') as initiation_date  
    from pspadm.psp_money_movement_transaction mmt
      join pspadm.psp_company c on company_fk = company_seq
      left join pspadm.psp_tax_payment_on_hold_reason enroll_hold on enroll_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and enroll_hold.on_hold_reason_cd = 'Enrollment' and enroll_hold.expiration_date is null
	  left join pspadm.psp_tax_payment_on_hold_reason backdate_hold on backdate_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and backdate_hold.on_hold_reason_cd = 'BackDate' and backdate_hold.expiration_date is null
      left join pspadm.psp_tax_payment_on_hold_reason agent_hold on agent_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and agent_hold.on_hold_reason_cd = 'Agent' and agent_hold.expiration_date is null
      left join pspadm.psp_tax_payment_on_hold_reason company_hold on company_hold.money_movement_transaction_fk = mmt.money_movement_transaction_seq and company_hold.on_hold_reason_cd = 'Company' and company_hold.expiration_date is null
    where initiation_date < trunc(systimestamp)
    and tax_payment_status in ('ReadyToSend','OnHold')
    )
  where
    (tax_payment_status = 'ReadyToSend' and amount > 0)
    or (tax_payment_status = 'OnHold' and company_hold = 'N' and comp_hold = 'Y') 
)
order by due_date, template, tax_payment_status desc, payment_method, amount desc;

spool off
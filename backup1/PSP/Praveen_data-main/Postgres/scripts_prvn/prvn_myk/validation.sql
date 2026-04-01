set search_path=pspadm;
\timing on
-- validate ledger run
-- count validation
select count(*) from psp_ledger_balance where date(balance_date) BETWEEN date'2023-04-03' AND  date'2023-04-10'; --> 1,948,547

-- deep validation (should be empty)
select ledger_balance_seq, version, creator_id,modifier_id, realm_id, balance_amount,
       balance_date,ledger_account_fk, company_fk, reporting_type from  psp_ledger_balance
where date(balance_date) BETWEEN date'2023-04-03' AND  date'2023-04-10'
except
select ledger_balance_seq, version, creator_id,modifier_id, realm_id, balance_amount,
       balance_date,ledger_account_fk, company_fk, reporting_type from ledger_balance_backup_test;

-- double check (This should also be empty)
select ledger_balance_seq, version, creator_id,modifier_id, realm_id, balance_amount,
       balance_date,ledger_account_fk, company_fk, reporting_type from ledger_balance_backup_test
except
select ledger_balance_seq, version, creator_id,modifier_id, realm_id, balance_amount,
       balance_date,ledger_account_fk, company_fk, reporting_type from  psp_ledger_balance
where date(balance_date) BETWEEN date'2023-04-03' AND  date'2023-04-10';


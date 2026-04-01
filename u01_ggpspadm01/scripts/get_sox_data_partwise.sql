\timing
set search_path to mchoubey;
drop table if exists cte_get_balance_pre_load_1;
create temp table cte_get_balance_pre_load_1 as
    (select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p0) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk);
insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p1) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;

insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p2) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;

insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p3) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;
insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p4) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;
insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p5) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;
insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p6) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;
insert into cte_get_balance_pre_load_1
select plb.balance_amount,
            plb.balance_date,
            plb.ledger_account_fk,
            plb.company_fk,
            pla.ledger_account_cd id
     from (select balance_amount, balance_date, ledger_account_fk, company_fk from pspadm.psp_ledger_balance_p7) plb
              join (select ledger_account_cd
                    from pspadm.psp_ledger_account
                    where ledger_account_cd not in ('DDFutureLiability', 'DDFutureReceivable', 'FeeCashRevenue', 'FeeIncome', 'TaxFutureReceivable',
                                     'TaxFutureLiability')) pla on pla.ledger_account_cd = plb.ledger_account_fk;


drop table if exists cte_get_balance_pre_load;
create temp table cte_get_balance_pre_load as
    (select balance_amount,
            balance_date,
            ledger_account_fk,
            company_fk,
            id,
            row_number() over (order by company_fk,ID,balance_date desc) rnk
     from cte_get_balance_pre_load_1);

drop table if exists cte_get_balance;
create temp table cte_get_balance as
    (SELECT cur.company_fk,
            coalesce(prv.company_fk, '0')   as prev_company_fk,
            cur.id                          AS ledger_account_cd,
            prv.id            as prev_ledger_account_cd,
            cur.balance_amount,
            coalesce(prv.balance_amount, 0) as prev_balance_amount,
            cur.balance_date,
            prv.balance_date                as prev_balance_date,
            cur.ledger_account_fk
     from (select * from cte_get_balance_pre_load) cur
              left join (select * from cte_get_balance_pre_load) prv on cur.rnk = prv.rnk + 1);

drop table if exists CTE_GET_FIRST_ROW_PREV_BALANCE;
create temp table CTE_GET_FIRST_ROW_PREV_BALANCE as
(SELECT *
 From (SELECT row_number() over (partition by company_fk,ledger_account_cd ORDER BY balance_date desc) as rn,
              company_fk,
              ledger_account_cd,
              balance_amount,
              balance_date,
              ledger_account_fk,
              prev_balance_amount,
              prev_balance_date
       FROM CTE_GET_BALANCE
       WHERE company_fk = prev_company_fk
         AND ledger_account_cd = prev_ledger_account_cd
         AND balance_amount != prev_balance_amount) cgb1
 WHERE rn in (1, 2));

drop table if exists CTE_GET_FIRST_ROW_CURRENT_BALANCE;
create temp table CTE_GET_FIRST_ROW_CURRENT_BALANCE as
    (select *
     from (SELECT row_number() over (partition by company_fk,ledger_account_cd ORDER BY balance_date desc) as rn,
                  company_fk,
                  ledger_account_cd,
                  balance_amount,
                  balance_date,
                  ledger_account_fk
           FROM CTE_GET_BALANCE
           WHERE (company_fk != prev_company_fk OR ledger_account_cd != prev_ledger_account_cd OR (
                       company_fk = prev_company_fk AND ledger_account_cd = prev_ledger_account_cd AND
                       balance_amount = prev_balance_amount))) cgb2
     where rn = 1);
drop table if exists sq_fact_psp_ledger_balance;
create temp table sq_fact_psp_ledger_balance as
(SELECT CB.company_fk,
       CB.ledger_account_cd,
       CB.balance_amount,
       CAST(CB.balance_date AS DATE) AS balance_date,
       CASE
           WHEN PB.rn = 1 THEN COALESCE(PB.balance_amount, 0)
           WHEN PB.rn = 2 THEN COALESCE(PB.prev_balance_amount, 0)
           END                       AS prev_balance_amount
FROM CTE_GET_FIRST_ROW_CURRENT_BALANCE CB
LEFT JOIN CTE_GET_FIRST_ROW_PREV_BALANCE PB ON CB.company_fk = PB.company_fk AND
                                               CB.ledger_account_cd = PB.ledger_account_cd
LEFT JOIN (SELECT count(*) c, company_fk, ledger_account_cd
           from CTE_GET_FIRST_ROW_PREV_BALANCE
           group by company_fk, ledger_account_cd) cnt ON CB.company_fk = cnt.company_fk AND
                                                          CB.ledger_account_cd = cnt.ledger_account_cd
 where (PB.rn in (select max(rn) FROM CTE_GET_FIRST_ROW_PREV_BALANCE))
          OR (cb.rn = 1 and coalesce(cnt.c, 0) != 2));


select min(concat(cast(ledger_account_cd as varchar), '_', company_fk, '_', to_char(balance_date, 'yyyymmdd'))) as min_id
     , max(concat(cast(ledger_account_cd as varchar), '_', company_fk, '_', to_char(balance_date, 'yyyymmdd'))) as max_id
     , min(balance_date)                                                                                      as min_balance_date
     , max(balance_date)                                                                                      as max_balance_date
     , count(1)                                                                                               as cnt
     , sum(balance_amount)                                                                                    as total_amount
from sq_fact_psp_ledger_balance;

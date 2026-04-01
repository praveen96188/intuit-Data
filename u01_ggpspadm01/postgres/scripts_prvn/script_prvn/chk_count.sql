\timing
select count(*) from pspadm.psp_money_movement_transaction_p0 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p1 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p2 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p3 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p4 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p5 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p6 where created_date < date '2024-02-12'
union all
select count(*) from pspadm.psp_money_movement_transaction_p7 where created_date < date '2024-02-12';


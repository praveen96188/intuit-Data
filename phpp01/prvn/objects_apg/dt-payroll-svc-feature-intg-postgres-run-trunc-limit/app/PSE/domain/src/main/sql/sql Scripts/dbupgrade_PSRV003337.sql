update psp_money_movement_transaction mmt
    set mmt.offload_batch_fk=null
        where initiation_date > date '2012-01-01'
              and (mmt.money_movement_payment_method='CheckPayment' or mmt.money_movement_payment_method is null)
              and mmt.offload_batch_fk is not null
              and status in ('OnHold', 'Created');

commit;

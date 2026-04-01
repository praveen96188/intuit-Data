CREATE OR REPLACE PROCEDURE PRC_UPDATE_LEDGER_BALANCE IS

    v_start_date date;
    v_end_date   date := FN_GET_PSP_TIMESTAMP -1 ;
    v_ledger_date date;
BEGIN

-- get the last date when ledger was updated.

  SELECT max(balance_date) +1
    INTO v_start_date
    FROM psp_ledger_balance ;

-- NULL means ledger was never updated

    IF v_start_date is null THEN
        SELECT min(Transaction_state_eff_date)
          INTO v_start_date
          FROM psp_financial_Trans_state;
    END IF;

-- NULL here means psp_financial_trans_state is empty
   IF v_start_date is null THEN
      RETURN;
   END IF;

-- Start updating the ledger with the start date

   v_ledger_date := v_start_date;

   FOR i in 1..(trunc(v_end_date) - trunc(v_start_date) ) +1
    LOOP
       PRC_CALCULATE_LEDGER_BALANCE(v_ledger_Date);
       v_ledger_date := v_start_date + i;
    END LOOP;

END PRC_UPDATE_LEDGER_BALANCE;
/

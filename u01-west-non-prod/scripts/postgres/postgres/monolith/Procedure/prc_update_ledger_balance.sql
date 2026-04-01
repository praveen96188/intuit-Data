CREATE OR REPLACE PROCEDURE PRC_UPDATE_LEDGER_BALANCE()
    LANGUAGE plpgsql AS
    $$
    DECLARE
        v_start_date date;
        v_end_date   date = FN_GET_PSP_TIMESTAMP() - interval '1 days';
        v_ledger_date date;
    BEGIN

        -- get the last date when ledger was updated.

        SELECT max(balance_date) + interval '1 days'
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

        FOR i in 1..(v_end_date::date - v_start_date::date ) + 1
            LOOP
                CALL PRC_CALCULATE_LEDGER_BALANCE(v_ledger_Date);
                v_ledger_date := v_start_date + i;
            END LOOP;
    END;
    $$
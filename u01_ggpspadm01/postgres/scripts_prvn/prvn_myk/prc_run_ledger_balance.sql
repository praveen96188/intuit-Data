--ledger balance run between specific dates
CREATE OR REPLACE PROCEDURE PRC_RUN_LEDGER_BALANCE(p_start_date date, p_end_date date)
    LANGUAGE plpgsql AS
$$
DECLARE
    v_ledger_date date;
BEGIN

    v_ledger_date := p_start_date;

    FOR i in 1..(p_end_date::date - p_start_date::date ) + 1
        LOOP
            --run ledger statement per day basis
            RAISE NOTICE 'Running ledger for date - %', v_ledger_date;
            CALL PRC_CALCULATE_LEDGER_BALANCE(v_ledger_Date); -- fixed proc
            v_ledger_date := p_start_date + i;
        END LOOP;
END;
$$;


-- delete data date wise
CREATE OR REPLACE PROCEDURE PRC_DELETE_LEDGER_BALANCE_DATA(p_start_date date, p_end_date date)
    LANGUAGE plpgsql AS
$$
DECLARE
    v_delete_date date;
BEGIN
    v_delete_date := p_start_date;

    FOR i in 1..(p_end_date::date - p_start_date::date ) + 1
        LOOP
            --delete statement per day basis
            RAISE NOTICE 'deleting for date - %', v_delete_date;
            delete from psp_ledger_balance where date(balance_date) = date(v_delete_date);
            v_delete_date := p_start_date + i;
        END LOOP;
END;
$$;


CREATE OR REPLACE PROCEDURE PRC_OFFLOAD_UPD_AGENCY_STATUS
   (
    p_user_id           IN VARCHAR,            -- For audit purposes
    p_app_server_date   IN TIMESTAMP,    -- UTC Date
    p_offload_batch_id  IN VARCHAR,       -- psp_offload_batch.offload_batch_seq
    p_offload_date      IN TIMESTAMP ,      -- UTC Date
    p_file_type          IN       VARCHAR     -- DD or Tax
   )
LANGUAGE plpgsql AS
  $$
  DECLARE

    -- these two variables are used in all SQL statements to populate date fields,
    -- the UTC date is used to populate SPCF audit fields created_date and modified_date
    v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_utc_date TIMESTAMP; -- current system UTC date and time

BEGIN

     SELECT timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)) INTO v_psp_date;

     v_utc_date := p_app_server_date;

       IF p_file_type = 'Tax'  THEN

          -- For each MMT we are going to update the tax_payment_status on below,
          -- recalculate the ATF payments passing in the new and existing payment status.

          DECLARE
            mmtRec psp_money_movement_transaction%rowtype;
             BEGIN
          FOR mmtRec in (select mmt.*
            from psp_money_movement_transaction mmt
            WHERE tax_payment_status = 'SentToAgency'
             and money_movement_payment_method = 'ACHCredit'
             and status = 'Executed'
             and mmt.offload_batch_fk=p_offload_batch_id)
          LOOP
            CALL prc_recalculate_atf_payments(p_user_id, v_utc_date, mmtRec.money_movement_transaction_seq, mmtRec.payment_template_fk,
                mmtRec.money_movement_payment_method, 'AcknowledgedByAgency', mmtRec.payment_period_end, mmtRec.initiation_date, mmtRec.company_fk);
          END LOOP;
        END;

          -- Set the Payment Status of ACHCRedit MMTs to AcknowledgedByAgency
          UPDATE psp_money_movement_transaction mmt
             SET tax_payment_status = 'AcknowledgedByAgency',
                 VERSION = VERSION + 1,
                 modifier_id = p_user_id,
                 modified_date = v_utc_date
           WHERE tax_payment_status = 'SentToAgency'
             and money_movement_payment_method = 'ACHCredit'
             and status = 'Executed'
             and mmt.offload_batch_fk=p_offload_batch_id;
       END IF;

END;
$$;
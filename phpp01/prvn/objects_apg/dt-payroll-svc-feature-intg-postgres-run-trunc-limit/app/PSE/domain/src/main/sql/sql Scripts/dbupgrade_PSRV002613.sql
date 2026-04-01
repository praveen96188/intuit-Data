BEGIN
      LOOP
            UPDATE PSPADM.PSP_TRANSACTION_RETURN t1
                  SET company_fk = (select company_fk from pspadm.psp_money_movement_transaction t2 where t2.money_movement_transaction_seq = t1.money_movement_transaction_fk)
            WHERE company_fk is null and ROWNUM < 5000;

            IF SQL%ROWCOUNT = 0
            THEN
                  EXIT;
            ELSE
                  COMMIT;
            END IF;
      END LOOP;

      COMMIT;
END;

/

CREATE INDEX PSPADM.PSP_TRANSACTION_RETURN_I1 ON PSPADM.PSP_TRANSACTION_RETURN
(COMPANY_FK, RETURN_STATUS_CD, BANK_RETURN_CD)
TABLESPACE PSP_IDX01
online
parallel 16;

alter index PSPADM.PSP_TRANSACTION_RETURN_I1 noparallel;

drop index PSPADM.PSP_TRANSACTION_RETURN_FK3;


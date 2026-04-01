set serveroutput on
set echo on timing on feedback on

DECLARE
    -- Declare variables for table name, constraint name and index name
    type array_t is varray (13) of varchar2(50);
    table_array array_t := array_t('PSP_EVENT_AS400_SYNC', 'PSP_TRANSACTION_OFFLOAD_BATCH', 'PSP_TAX_PAYMENT_ON_HOLD_REASON', 'PSP_PAYMENT_BATCH_ASSOC', 'PSP_FSET_FILING_DETAIL', 'PSP_VOIDED_CHECK', 'PSP_EFTPS_PAYMENT_DETAIL', 'PSP_EDI_PAYMENT_DETAIL', 'PSP_TP401K_BATCH_PAYCHECK', 'PSP_TP401K_PAYCHECK', 'PSP_WC_PAYCHECK', 'PSP_PAYSTUB', 'PSP_COMPANY_EVENT_EMAIL');
    index_constraint_array array_t := array_t('PSP_EVENT_AS400_SYNC_FK2', 'PSP_TRANSACTION_OFFLOAD_BA_FK3', 'PSP_TAX_PAYMENT_ON_HOLD_RE_FK2','PSP_PAYMENT_BATCH_ASSOC_FK3', 'PSP_FSET_FILING_DETAIL_FK4', 'PSP_VOIDED_CHECK_FK4', 'PSP_EFTPS_PAYMENT_DETAIL_FK5', 'PSP_EDI_PAYMENT_DETAIL_FK4', 'PSP_THIRD_PARTY401K_BATCH__FK3',  'PSP_THIRD_PARTY401K_PAYCHE_FK2', 'PSP_WORKERS_COMP_PAYCHECK_FK2', 'PSP_PAYSTUB_FK4', 'PSP_COMPANY_EVENT_EMAIL_FK2');
    target_column_name varchar2(50) := 'COMPANY_FK';
    constraint_exist varchar2(50);
    index_exist varchar2(50);
    column_exists integer := 0;
BEGIN
    for i in 1..table_array.count loop
            SELECT count(*)
            INTO constraint_exist
            FROM all_constraints
            WHERE table_name = table_array(i)
              AND CONSTRAINT_NAME = index_constraint_array(i);

            if constraint_exist > 0 then
                -- Drop the constraint
--                dbms_output.put_line('ALTER TABLE ' || table_array(i) || ' DROP CONSTRAINT ' || index_constraint_array(i));
            EXECUTE IMMEDIATE 'ALTER TABLE PSPADM.' || table_array(i) || ' DROP CONSTRAINT ' || index_constraint_array(i);
            end if;

            SELECT count(*)
            INTO index_exist
            FROM all_indexes
            WHERE table_name = table_array(i)
              AND index_name = index_constraint_array(i);

            if index_exist > 0 then
                -- Drop the index
--                dbms_output.put_line('DROP INDEX ' || index_constraint_array(i));
            EXECUTE IMMEDIATE 'DROP INDEX PSPADM.' || index_constraint_array(i);
            end if;

            Select count(*)
            into column_exists
            from all_tab_cols
            where column_name = target_column_name
              and table_name = table_array(i);

            if column_exists > 0 then
                -- Drop the column
--                dbms_output.put_line('ALTER TABLE ' || table_array(i) || ' DROP COLUMN ' || target_column_name);
            EXECUTE IMMEDIATE 'ALTER TABLE PSPADM.' || table_array(i) || ' DROP COLUMN ' || target_column_name;
            end if;
        end loop;
    COMMIT;
END;
/

--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
ALTER TABLE PSP_BILLING_DETAIL
 DROP CONSTRAINT PSP_BILLING_DETAIL_FK_TAX_TX;

ALTER TABLE PSP_BILLING_DETAIL
 DROP CONSTRAINT PSP_BILLING_DETAIL_FK3;

Prompt drop Index PSP_BILLING_DETAIL_FK_TAX_TX;
DROP INDEX PSP_BILLING_DETAIL_FK_TAX_TX;

Prompt drop Index PSP_BILLING_DETAIL_FK3;
DROP INDEX PSP_BILLING_DETAIL_FK3;

Prompt Column TAX_TRANSACTION_FK;
-- Column to be dropped is part of a multi-column constraint.
-- Oracle requires that the constraint be dropped first.
-- There may be another statement later in the script that tries to drop
-- the constraint again.  Errors produced by it can be ignored.
ALTER TABLE PSP_BILLING_DETAIL DROP CONSTRAINT PSP_BILLING_DETAIL_FK_TAX_TX;
ALTER TABLE PSP_BILLING_DETAIL DROP COLUMN TAX_TRANSACTION_FK;

Prompt Column FEE_TRANSACTION_FK;
-- Column to be dropped is part of a multi-column constraint.
-- Oracle requires that the constraint be dropped first.
-- There may be another statement later in the script that tries to drop
-- the constraint again.  Errors produced by it can be ignored.
ALTER TABLE PSP_BILLING_DETAIL DROP CONSTRAINT PSP_BILLING_DETAIL_FK3;
ALTER TABLE PSP_BILLING_DETAIL DROP COLUMN FEE_TRANSACTION_FK;

Prompt Column BILLING_DETAIL_FK;
ALTER TABLE PSP_FINANCIAL_TRANSACTION
 ADD (BILLING_DETAIL_FK  VARCHAR2(255 CHAR));

Prompt Index PSP_FINANCIALTRANSACTION_FK2;
--
-- PSP_FINANCIALTRANSACTION_FK2  (Index) 
--
CREATE INDEX PSP_FINANCIALTRANSACTION_FK2 ON PSP_FINANCIAL_TRANSACTION
(ORIGINAL_TRANSACTION_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_FINANCIALTRANSACTION_FK1;
Prompt drop Index PSP_FINANCIALTRANSACTION_FK1;
DROP INDEX PSP_FINANCIALTRANSACTION_FK1;
Prompt Index PSP_FINANCIALTRANSACTION_FK1;
--
-- PSP_FINANCIALTRANSACTION_FK1  (Index) 
--
CREATE INDEX PSP_FINANCIALTRANSACTION_FK1 ON PSP_FINANCIAL_TRANSACTION
(BILLING_DETAIL_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Constraint PSP_FINANCIALTRANSACTION_FK1;
ALTER TABLE PSP_FINANCIAL_TRANSACTION
 DROP CONSTRAINT PSP_FINANCIALTRANSACTION_FK1;
ALTER TABLE PSP_FINANCIAL_TRANSACTION
 ADD CONSTRAINT PSP_FINANCIALTRANSACTION_FK1 
 FOREIGN KEY (BILLING_DETAIL_FK, REALM_ID) 
 REFERENCES PSP_BILLING_DETAIL (BILLING_DETAIL_SEQ,REALM_ID);

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 ADD CONSTRAINT PSP_FINANCIALTRANSACTION_FK2 
 FOREIGN KEY (ORIGINAL_TRANSACTION_FK, REALM_ID) 
 REFERENCES PSP_FINANCIAL_TRANSACTION (FINANCIAL_TRANSACTION_SEQ,REALM_ID);

select 'finished DBUpgradeFrom_1.9.1.0_To_1.9.2.0.sql ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual
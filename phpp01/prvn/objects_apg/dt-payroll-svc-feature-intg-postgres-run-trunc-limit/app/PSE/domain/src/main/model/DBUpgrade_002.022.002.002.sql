--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL

Prompt Constraint C_PSP_SMSMIGRATION0;
ALTER TABLE PSP_SMSMIGRATION
DROP CONSTRAINT C_PSP_SMSMIGRATION0;

ALTER TABLE PSP_SMSMIGRATION
    ADD CONSTRAINT C_PSP_SMSMIGRATION0
        CHECK (MIGRATION_STATUS IN('ValidationInProgress', 'NeedsValidation', 'ValidationSuccess', 'ValidationError', 'ValidationInternalError', 'MigrationError', 'MigrationInProgress', 'MigrationComplete', 'DataCollectionComplete', 'MigrationReverted', 'MigrationOnHold')) enable novalidate;

PROMPT finished DBUpgrade_002.022.002.002.sql

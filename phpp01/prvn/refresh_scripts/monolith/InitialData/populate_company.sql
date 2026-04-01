DO $$
DECLARE

    company_exists NUMERIC;

BEGIN

    select count(COMPANY_SEQ) into company_exists
        from PSP_COMPANY
            where SOURCE_COMPANY_ID='111111111' and SOURCE_SYSTEM_CD = 'PSP';

    IF company_exists = 0 THEN

        INSERT INTO PSP_COMPANY (COMPANY_SEQ,
          CREATOR_ID,
          CREATED_DATE,
          MODIFIER_ID,
          MODIFIED_DATE,
          PHONE,
          DBA_NAME,
          FED_TAX_ID_ENC,
          LEGAL_NAME,
          SOURCE_COMPANY_ID,
          DEBUG_LOGGING,
          NOTIFICATION_EMAIL,
          NBR_OF_FAILED_LOGIN_ATTEMPTS,
          CURRENT_TOKEN,
          SOURCE_SYSTEM_CD,
          IS_FLAGGED_FOR_FRAUD,
          SIGN_UP_DATE,
          PRIVATE_KEY_ENC,
          PUBLIC_KEY,
          NBR_FAILED_AUTHENTICATIONS,
          CLOUD_CURRENT_TOKEN,
          OFFLOAD_GROUP_FK,
          FUNDING_MODEL_FK,
          VERSION)
        VALUES ('1bf77ce5-dcd6-4573-bad1-26dc361ecd43',
        'System',
         CURRENT_TIMESTAMP,
         'System',
         CURRENT_TIMESTAMP,
        '123-456-7890',
        'GlobalBookTransfer_DBA',
        '2gIAAAASAQAEAAAAAwIACHNJdHJLb2tn9WwffpGiUwRIhLK8ZCIULAt6dgn15sHr7g==',
        'Intuit-Book Transfer',
        '111111111',
        0,
        'Intuit@BookTransfer',
        0,
        0,
        'PSP',
        0,
        CURRENT_TIMESTAMP,
        NULL,
        NULL,
        0,
        0,
        (select OFFLOAD_GROUP_SEQ  from PSP_OFFLOAD_GROUP where OFFLOAD_GROUP_CD='STD'),
        '5D',
        0);

    END IF;
END $$;
COMMIT;
;


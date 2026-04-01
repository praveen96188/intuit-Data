-- The tax rep role was removed, there shouldn't be any users in prod with that role
delete from psp_auth_user where auth_role_fk = '675e8b0e-43f4-a631-5341-6ad6a5327356';
commit;

-- the agency tax payer id was moved from company agency to company payment template
DROP TRIGGER PSPADM.PSP_COMPANY_AGENCY_AT;
commit;

-- to support 2 day and 5 day funding model.

UPDATE PSP_SOURCE_PAYROLL_PARAMETER
   SET PARAMETER_VALUE = '1'
 WHERE SOURCE_SYSTEM_CD = 'QBDT'
       AND PARAMETER_CD = 'AllowMultipleFundingModels';

COMMIT;

INSERT INTO PSP_AUTH_ROLE ( AUTH_ROLE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, ROLE_ID,NAME,DESCRIPTION, AUTH_DOMAIN_FK,CREATOR_ID)
VALUES ('635e8b0e-43f4-a631-5341-56d6a5327856', 0, SYSDATE, SYSDATE, 'AccountingRM','AccountingRM','AccountingRM','DDUI','System');

update psp_auth_user 
set auth_role_fk = '635e8b0e-43f4-a631-5341-56d6a5327856' 
where auth_role_fk = '635e8b0e-43f4-a631-5341-56d6a5327855';

delete from psp_auth_user
where auth_role_fk = '635e8b0e-43f4-a631-5341-56d6a5327855';

commit;
CREATE OR REPLACE FUNCTION FN_GET_ENV RETURN VARCHAR2
AS

/******************************************************************************
   PURPOSE: Get the current environment
******************************************************************************/

  ENV VARCHAR2(200);
BEGIN
  SELECT (CASE
              WHEN UPPER(sys_context('userenv', 'service_name')) in ('PSPPROD', 'PSPDR') THEN 'PROD'
              WHEN UPPER(sys_context('userenv', 'db_name')) in
                   ('PSPUWP01','PSPUWP02','PSPUEP01','PSPUEP02', 'PSPPP001', 'PSPSP001', 'PSPWP001', 'PSPEP001', 'PSPTS005', 'PSPTSIB5', 'PSPUE005', 'PSPUEIB5')
                  THEN 'PROD'
              ELSE 'NONPROD' END) INTO ENV
    from dual;
  RETURN ENV;
END FN_GET_ENV;
/

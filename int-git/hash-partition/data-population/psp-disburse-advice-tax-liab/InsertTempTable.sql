insert into PSPADM.temp_company_disburse (COMPANY_FK)
select distinct COMPANY_FK from PSPADM.PSP_DISBURSE_ADVICE;
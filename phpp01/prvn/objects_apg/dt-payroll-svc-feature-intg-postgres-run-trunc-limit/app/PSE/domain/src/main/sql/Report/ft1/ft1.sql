Set pages 999;
Set linesize 200;
Set pagesize 999;
COLUMN PSID FORMAT 9999999999;
COLUMN LEGAL_NAME FORMAT A40;
COLUMN AMOUNT_RETURNED FORMAT $999999999.99;
COLUMN RETURN_DATE FORMAT A40;
COLUMN REDEBIT_CREATED FORMAT A2;
spool ft1.log
select pc.source_company_id PSID,pc.legal_name LEGAL_NAME,mmt.mm_transaction_amount AMOUNT_RETURNED,ptr.created_date RETURN_DATE,  decode((select count(*) from pspadm.psp_financial_transaction ft1 where ft1.original_transaction_fk=ft.financial_transaction_seq and ft1.TRANSACTION_TYPE_FK='EmployerFeeRedebit') ,0,'N','Y') REDEBIT_CREATED from pspadm.psp_company pc
inner join pspadm.psp_payroll_run pr on PC.COMPANY_seq=PR.COMPANY_fk
inner join pspadm.psp_financial_transaction ft on FT.PAYROLL_RUN_FK=PR.PAYROLL_RUN_SEQ
inner join pspadm.psp_money_movement_transaction mmt on MMT.MONEY_MOVEMENT_TRANSACTION_SEQ=FT.MONEY_MOVEMENT_TRANSACTION_FK
inner join pspadm.psp_transaction_return ptr on PTR.MONEY_MOVEMENT_TRANSACTION_FK=MMT.MONEY_MOVEMENT_TRANSACTION_SEQ
where  FT.TRANSACTION_TYPE_FK='EmployerFeeDebit' and ft.company_fk in (select company_fk from pspadm.psp_company_service where service_fk='BillPayment') 
and ptr.created_date>sysdate-1 and pr.payroll_run_type='BillPayment';
spool off

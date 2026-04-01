---- This is a post deploy script that needs to run after deploying 2.0.

-- Insert an ATF Payroll record for each payroll run that doesn't already have one.
-- This is due to a bug that was missing negative, liability adjustments.
insert into PSP_ATFPAYROLLS_TO_PROCESS (atfpayrolls_to_process_seq, version, creator_id, created_date, modifier_id, modified_date, payroll_run_fk)
(select sys_guid(), 0, 'NegativeAdjustmentMigration', systimestamp, 'NegativeAdjustmentMigration', systimestamp, run.payroll_run_seq
    from psp_payroll_run run
    join psp_company co on run.company_fk = co.company_seq
    where payroll_run_status = 'Complete'
       and payroll_run_type in ('CloudOnly','Adjustment')
       and payroll_run_date >= trunc(sysdate,'Y')    -- Just for 2012
       and not exists (select 1 from psp_atfpayrolls_to_process atf where ATF.PAYROLL_RUN_FK = run.payroll_run_seq)
       and exists (select 1 from psp_company_service serv where serv.company_fk = co.company_seq and serv.service_fk = 'Tax'));

commit;

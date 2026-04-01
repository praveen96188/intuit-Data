--Workflow :  - com.intuit.sbd.payroll.psp.jss.processors.RetryQbdtUnprocessedRequestProcessor$ResetQbdtFlagsStep.execute
--query name :  - findProcessingDisabledCompanies
--line number : - 2586
--Splunk link : -https://github.intuit.com/payroll-dtpayroll/dt-payroll-svc/blob/master/app/PSE/domain/src/main/java/com/intuit/sbd/payroll/psp/domain/Company.java#L2586 
--Time taken without baseline : - 
--sqlhash -Mayank to fill

/* findProcessingDisabledCompanies */ SELECT distinct co.* FROM psp_company co
        join psp_quickbooks_info qinfo on co.company_seq=qinfo.company_fk
        join (
        select created_date, company_fk, company_event_seq, row_number() over(partition by company_fk order by created_date desc)  rn
        from psp_company_event
        where event_type_cd=?
        ) ce on ce.company_fk=qinfo.company_fk
        and rn=?
        where qinfo.process_transmissions=?
        and ? * (to_date(TO_CHAR(sys_extract_utc(fn_get_psp_timestamp),?),?)
        -to_date(TO_CHAR( ce.created_date,?),?))
        BETWEEN ? and ?
        and (CASE WHEN (? = ?) THEN co.IS_DG_DISASSOCIATED ELSE ? END) = ?

--Time taken after baseline : - 
--Plan after baseline - 08xpu34mgks5a

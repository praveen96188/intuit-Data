--Workflow :  - com.intuit.sbd.payroll.psp.jss.processors.WorkersCompProcessor
--query name :  - findWCPaychecksForSplitHavingStatusDeleteOrEdit
--line number : - 161
--Splunk link : - https://github.intuit.com/payroll-dtpayroll/dt-payroll-svc/blob/master/app/PSE/batch-jobs/src/main/java/com/intuit/sbd/payroll/psp/jss/processors/WorkersCompProcessor.java#L161
--Time taken without baseline : 794.652825 secs(6 min)
--sqlhash -Mayank to fill

/* findWCPaychecksForSplitHavingStatusDeleteOrEdit */ select workerscom0_.WC_PAYCHECK_SEQ as wc_paycheck_seq1_292_, workerscom0_.VERSION as version2_292_, workerscom0_.CREATOR_ID as creator_id3_292_, workerscom0_.CREATED_DATE as created_date4_292_, workerscom0_.MODIFIER_ID as modifier_id5_292_, workerscom0_.MODIFIED_DATE as modified_date6_292_, workerscom0_.REALM_ID as realm_id7_292_, workerscom0_.PAYCHECK_VERSION as paycheck_version8_292_, workerscom0_.INITIATION_DATE as initiation_date9_292_, workerscom0_.CURRENT_STATE_CD as current_state_cd10_292_, workerscom0_.PAYCHECK_FK as paycheck_fk11_292_, workerscom0_.COMPANY_FK as company_fk12_292_ from PSP_WC_PAYCHECK workerscom0_ cross join PSP_PAYCHECK paycheck1_ cross join PSP_COMPANY company2_ cross join PSP_PAYROLL_RUN payrollrun3_ cross join PSP_WC_COMPANY wccompany4_ where  company2_.IS_DG_DISASSOCIATED=? and company2_.COMPANY_SEQ=wccompany4_.COMPANY_FK and workerscom0_.COMPANY_FK=paycheck1_.COMPANY_FK and paycheck1_.COMPANY_FK=company2_.COMPANY_SEQ and paycheck1_.PAYCHECK_SEQ=workerscom0_.PAYCHECK_FK and payrollrun3_.PAYROLL_RUN_SEQ=paycheck1_.PAYROLL_RUN_FK and payrollrun3_.PAYCHECK_DATE<=? and (workerscom0_.CURRENT_STATE_CD in (? , ?)) and (wccompany4_.SUBS_TYPE_CD in (?))

--Time taken after baseline : - 2.616741 secs
--Plan after baseline - fyrwd58y8rmq5

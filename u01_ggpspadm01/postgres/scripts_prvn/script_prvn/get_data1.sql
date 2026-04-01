set max_parallel_workers_per_gather=4;
SELECT (CASE
            WHEN PAYROLL_RUN_TYPE = 'Regular' THEN 'Money Movement - Employees'
            WHEN PAYROLL_RUN_TYPE = 'BillPayment' THEN 'Money Movement - Contractors'
            ELSE 'Others' END)                     ACH_MONEY_MOVEMENT_TYPE,
       SUM(AMOUNT),
       TO_CHAR(SUM(AMOUNT), '$999,999,999,999.00') FORMATTED_AMOUNT
FROM (SELECT DISTINCT EDR.ENTRY_DETAIL_RECORD_SEQ,
                                          EDR.AMOUNT,
                                          PR.PAYROLL_RUN_SEQ,
                                          PR.PAYROLL_RUN_TYPE
      FROM PSPADM.PSP_FINANCIAL_TRANSACTION FT,
           PSPADM.PSP_ENTRY_DETAIL_RECORD EDR,
           PSPADM.PSP_PAYROLL_RUN PR
      WHERE FT.MONEY_MOVEMENT_TRANSACTION_FK = EDR.MONEY_MOVEMENT_TRANSACTION_FK
        AND FT.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
        AND EDR.INITIATION_DATE > timestamp '2023-11-03 00:00:00'
        AND EDR.INTUIT_BANK_ACCOUNT_FK IS NULL
        AND EDR.RECORD_DATA_ENC IS NOT NULL
        and ft.company_fk = pr.company_fk
        and edr.company_fk = pr.company_fk) res
GROUP BY (CASE
              WHEN PAYROLL_RUN_TYPE = 'Regular' THEN 'Money Movement - Employees'
              WHEN PAYROLL_RUN_TYPE = 'BillPayment' THEN 'Money Movement - Contractors'
              ELSE 'Others' END);


/******************************************
     NAME:       PRC_QTR_PAYROLL_ITEM_TOT
     PURPOSE:    To calculate payroll item totals for W2 data, this is for all companies and one quarter
******************************************/

CREATE OR REPLACE PROCEDURE prc_payroll_item_totals_qtr_payroll_item_tot (
    p_qtr_start_date            IN   TIMESTAMP -- Quarter start date in UTC
)
    LANGUAGE plpgsql AS
$$

DECLARE
v_utc_date TIMESTAMP; -- current system UTC date and time
BEGIN

SELECT timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)) INTO v_utc_date;

-- Deleting records for voided payrolls
DELETE FROM psp_ee_payrollitem_qtrtotals eePayrollItemQtrTotals
WHERE
        eePayrollItemQtrTotals.quarter = cast(extract(QUARTER FROM p_qtr_start_date) as numeric)
  AND eePayrollItemQtrTotals.year = cast(extract(YEAR FROM p_qtr_start_date) as numeric);

INSERT INTO psp_ee_payrollitem_qtrtotals  (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date,
                                           modified_date, realm_id, quarter, year,
                                           amount, taxable_wages, total_wages, tips_taxable_wages_amount,
                                           company_payroll_item_fk, employee_fk)
select gen_random_uuid(), 1, 'PK_PAYROLL_ITEM_TOTALS-INS', v_utc_date,
       v_utc_date, -1, cast(extract(QUARTER FROM p_qtr_start_date) as numeric), cast(extract(YEAR FROM p_qtr_start_date) as numeric),
       src.amount, 0, 0, 0,
       src.company_payroll_item_seq, src.source_employee_fk
from  (select company_payroll_item_seq, source_employee_fk, sum(amount) amount
       from ((select pcpi.company_payroll_item_seq company_payroll_item_seq, pcheck.source_employee_fk source_employee_fk, sum(pcomp.compensation_amount) amount
              from psp_compensation pcomp
                       join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk and pcpi.company_fk = pcomp.company_fk
                       join psp_paycheck pcheck on pcomp.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pcomp.company_fk
                       join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
              where
                      date_trunc('quarter', pr.paycheck_date) = date_trunc('day',p_qtr_start_date) -- :qtrStartDate
                and pcheck.source_employee_fk is not null
                and pcpi.is_archived = 0
                and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                and pr.payroll_run_status != 'Superseded'
                    and pcpi.tax_form_line in ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION',
                                               'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B',
                                               'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B',
                                               'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS',
                                               'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5',
                                               'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17','TTT19',
                                               'TTT22','TTT23','TTT24','TTT25','TTT26','TTT27','TTT28','TTT29','TTT30','TTT31','TTT32','TTT33')  --(:taxFormLines)
              group by
                  pcpi.company_payroll_item_seq, pcheck.source_employee_fk)
             union all
             (select pcpi.company_payroll_item_seq company_payroll_item_seq , pcheck.source_employee_fk source_employee_fk, sum(pded.deduction_amount) amount
              from psp_deduction pded
                       join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pded.company_payroll_item_fk and pcpi.company_fk = pded.company_fk
                       join psp_paycheck pcheck on pded.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pded.company_fk
                       join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
              where
                      date_trunc('quarter', pr.paycheck_date) = date_trunc('day',p_qtr_start_date) -- :qtrStartDate
                and pcheck.source_employee_fk is not null
                and pcpi.is_archived = 0
                and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                and pr.payroll_run_status != 'Superseded'
                    and pcpi.tax_form_line in ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION',
                                               'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B',
                                               'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B',
                                               'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS',
                                               'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5',
                                               'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19',
                                               'TTT22','TTT23','TTT24','TTT25','TTT26','TTT27','TTT28','TTT29','TTT30','TTT31','TTT32','TTT33')  --(:taxFormLines)
              group by
                  pcpi.company_payroll_item_seq, pcheck.source_employee_fk)
             union all
             (select pcpi.company_payroll_item_seq company_payroll_item_seq , pcheck.source_employee_fk source_employee_fk, sum(pec.contribution_amount) amount
              from psp_employer_contribution pec
                       join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pec.company_payroll_item_fk and pcpi.company_fk = pec.company_fk
                       join psp_paycheck pcheck on pec.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pec.company_fk
                       join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
              where
                      date_trunc('quarter', pr.paycheck_date) = date_trunc('day',p_qtr_start_date) -- :qtrStartDate
                and pcheck.source_employee_fk is not null
                and pcpi.is_archived = 0
                and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                and pr.payroll_run_status != 'Superseded'
                    and pcpi.tax_form_line in ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION',
                                               'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B',
                                               'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B',
                                               'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS',
                                               'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5',
                                               'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17','TTT19',
                                               'TTT22','TTT23','TTT24','TTT25','TTT26','TTT27','TTT28','TTT29','TTT30','TTT31','TTT32','TTT33')  --(:taxFormLines)
              group by
                  pcpi.company_payroll_item_seq, pcheck.source_employee_fk)
             union all
             (select pcpi.company_payroll_item_seq company_payroll_item_seq , ptrans.employee_fk source_employee_fk, sum(ptransline.amount * (case when pitem.payroll_item_type = 'Deduction' then -1
                                                                                                                                                   when pitem.payroll_item_type = 'Compensation' then 1
                                                                                                                                                   when pitem.payroll_item_type = 'EmployerContribution' then 1
                                                                                                                                                   else 1 end)) amount
              from psp_qbdt_payroll_trans_line ptransline
                       join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                       join psp_qbdt_payroll_transaction ptrans on ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                       join psp_payroll_item pitem on pitem.payroll_item_code = pcpi.payroll_item_fk
              where
                      date_trunc('quarter', ptrans.period_end_date) = date_trunc('day',p_qtr_start_date) -- :qtrStartDate
                and ptrans.employee_fk is not null
                and pcpi.is_archived = 0
                and ptrans.is_voided = 0
                and pcpi.tax_form_line in ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION',
                                           'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B',
                                           'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B',
                                           'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS',
                                           'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5',
                                           'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19',
                                           'TTT22','TTT23','TTT24','TTT25','TTT26','TTT27','TTT28','TTT29','TTT30','TTT31','TTT32','TTT33')  --(:taxFormLines)
              group by
                  pcpi.company_payroll_item_seq, ptrans.employee_fk)) AS derivedTable
       group by
           company_payroll_item_seq, source_employee_fk) src;

END;

$$;
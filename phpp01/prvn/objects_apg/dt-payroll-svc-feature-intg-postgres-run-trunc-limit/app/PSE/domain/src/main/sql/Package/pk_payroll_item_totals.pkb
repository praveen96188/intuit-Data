CREATE OR REPLACE PACKAGE BODY PK_PAYROLL_ITEM_TOTALS
AS
   /******************************************
       NAME:       PK_PAYROLL_ITEM_TOTALS
       PURPOSE:    To calculate payroll item totals for W2 data
   ******************************************/

   /******************************************
       NAME:       PRC_COMP_QTR_PAYROLL_ITEM_TOT
       PURPOSE:    To calculate payroll item totals for W2 data, this is for one company and one quarter
   ******************************************/
   PROCEDURE PRC_COMP_QTR_PAYROLL_ITEM_TOT (
           p_company_seq               IN   VARCHAR2, -- Company seq
           p_qtr_start_date            IN   TIMESTAMP -- Quarter start date in UTC
       )
        IS
            v_utc_date TIMESTAMP; -- current system UTC date and time
        BEGIN

          SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp) INTO v_utc_date FROM DUAL;
          
          -- dbms_output.put_line('Merging PRC_COMP_QTR_PAYROLLITEM_TOT, starting merge  - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
          -- Deleting records for voided payrolls
          DELETE FROM psp_ee_payrollitem_qtrtotals eePayrollItemQtrTotals
                WHERE 
                    eePayrollItemQtrTotals.quarter = to_number(to_char(p_qtr_start_date, 'Q'))
                    AND eePayrollItemQtrTotals.year = to_number(to_char(p_qtr_start_date, 'YYYY'))
                    AND EXISTS (SELECT '1'  FROM psp_company_payroll_item pcpi
                                  WHERE pcpi.company_payroll_item_seq =  eePayrollItemQtrTotals.company_payroll_item_fk
                                     AND pcpi.company_fk = p_company_seq);

          INSERT INTO psp_ee_payrollitem_qtrtotals (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date,
                                                                                                 modified_date, realm_id, quarter, year,
                                                                                                 amount, taxable_wages, total_wages, tips_taxable_wages_amount,
                                                                                                 company_payroll_item_fk, employee_fk)
               select fn_format_sysguid (SYS_GUID ()), 1, 'PK_PAYROLL_ITEM_TOTALS', v_utc_date,
                                            v_utc_date, -1, to_number(to_char(p_qtr_start_date, 'Q')), to_number(to_char(p_qtr_start_date, 'YYYY')),
                                            src.amount, 0, 0, 0,
                                            src.company_payroll_item_seq, src.source_employee_fk
               from (select company_payroll_item_seq, source_employee_fk, sum(amount) amount
                         from ((select pcpi.company_payroll_item_seq company_payroll_item_seq, pcheck.source_employee_fk source_employee_fk, sum(pcomp.compensation_amount) amount
                          from psp_compensation pcomp
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk and pcpi.company_fk = pcomp.company_fk
                          join psp_paycheck pcheck on pcomp.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pcomp.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                           TRUNC (pr.paycheck_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
                          and pcheck.source_employee_fk is not null
                          and pcpi.is_archived = 0
                          and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                          and pr.company_fk = p_company_seq --:companySeq
                          and pcheck.company_fk = p_company_seq
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
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , pcheck.source_employee_fk source_employee_fk, sum(pded.deduction_amount) amount
                          from psp_deduction pded
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pded.company_payroll_item_fk and pcpi.company_fk = pded.company_fk
                          join psp_paycheck pcheck on pded.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pded.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                        TRUNC (pr.paycheck_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
                          and pcheck.source_employee_fk is not null
                          and pcpi.is_archived = 0
                          and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                          and pr.company_fk = p_company_seq --:companySeq
                          and pcheck.company_fk = p_company_seq
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
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , pcheck.source_employee_fk source_employee_fk, sum(pec.contribution_amount) amount
                          from psp_employer_contribution pec
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pec.company_payroll_item_fk and pcpi.company_fk = pec.company_fk
                          join psp_paycheck pcheck on pec.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pec.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                           TRUNC (pr.paycheck_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
                          and pcheck.source_employee_fk is not null
                          and pcpi.is_archived = 0
                          and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                          and pr.company_fk = p_company_seq --:companySeq
                          and pcheck.company_fk = p_company_seq
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
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , ptrans.employee_fk source_employee_fk, sum(ptransline.amount * decode(pitem.payroll_item_type, 'Deduction', -1, 'Compensation', 1, 'EmployerContribution', 1, 1)) amount
                          from psp_qbdt_payroll_trans_line ptransline
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                          join psp_qbdt_payroll_transaction ptrans on ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                          join psp_payroll_item pitem on pitem.payroll_item_code = pcpi.payroll_item_fk
                        where
                          TRUNC (ptrans.period_end_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
                          and ptrans.employee_fk is not null
                          and pcpi.is_archived = 0
                          and ptrans.is_voided = 0
                          and ptrans.company_fk = p_company_seq --:companySeq
                          and pcpi.tax_form_line in ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION',
                                                                                             'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B',
                                                                                             'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B',
                                                                                             'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS',
                                                                                             'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5',
                                                                                             'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19',
                                                                                             'TTT22','TTT23','TTT24','TTT25','TTT26','TTT27','TTT28','TTT29','TTT30','TTT31','TTT32','TTT33')  --(:taxFormLines)
                        group by
                          pcpi.company_payroll_item_seq, ptrans.employee_fk))
                        group by
                          company_payroll_item_seq, source_employee_fk) src;

          -- dbms_output.put_line('Finished merging PRC_COMP_QTR_PAYROLLITEM_TOT  - ' || to_char(systimestamp, 'hh24:mi:ss'));

   END PRC_COMP_QTR_PAYROLL_ITEM_TOT;
   

   /******************************************
        NAME:       PRC_COMP_QTR_PAYROLL_ITEM_TOT
        PURPOSE:    To calculate payroll item totals for W2 data, this is for all companies and one quarter
   ******************************************/

   PROCEDURE PRC_QTR_PAYROLL_ITEM_TOT (
           p_qtr_start_date            IN   TIMESTAMP -- Quarter start date in UTC
       )
        IS
            v_utc_date TIMESTAMP; -- current system UTC date and time
        BEGIN

          SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp) INTO v_utc_date FROM DUAL;
          
          -- dbms_output.put_line('Merging PRC_QTR_PAYROLL_ITEM_TOT, starting merge  - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
          -- Deleting records for voided payrolls
          DELETE FROM psp_ee_payrollitem_qtrtotals eePayrollItemQtrTotals
                WHERE 
                    eePayrollItemQtrTotals.quarter = to_number(to_char(p_qtr_start_date, 'Q'))
                    AND eePayrollItemQtrTotals.year = to_number(to_char(p_qtr_start_date, 'YYYY'));

          INSERT INTO psp_ee_payrollitem_qtrtotals  (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date,
                        modified_date, realm_id, quarter, year,
                        amount, taxable_wages, total_wages, tips_taxable_wages_amount,
                        company_payroll_item_fk, employee_fk)
                 select fn_format_sysguid (SYS_GUID ()), 1, 'PK_PAYROLL_ITEM_TOTALS-INS', v_utc_date,
                      v_utc_date, -1, to_number(to_char(p_qtr_start_date,'Q')), to_number(to_char(p_qtr_start_date, 'YYYY')),
                      src.amount, 0, 0, 0,
                      src.company_payroll_item_seq, src.source_employee_fk
                 from  (select company_payroll_item_seq, source_employee_fk, sum(amount) amount
                         from ((select pcpi.company_payroll_item_seq company_payroll_item_seq, pcheck.source_employee_fk source_employee_fk, sum(pcomp.compensation_amount) amount
                          from psp_compensation pcomp
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk and pcpi.company_fk = pcomp.company_fk
                          join psp_paycheck pcheck on pcomp.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pcomp.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                           TRUNC (pr.paycheck_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
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
                        TRUNC (pr.paycheck_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
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
                           TRUNC (pr.paycheck_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
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
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , ptrans.employee_fk source_employee_fk, sum(ptransline.amount * decode(pitem.payroll_item_type, 'Deduction', -1, 'Compensation', 1, 'EmployerContribution', 1, 1)) amount
                          from psp_qbdt_payroll_trans_line ptransline
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                          join psp_qbdt_payroll_transaction ptrans on ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                          join psp_payroll_item pitem on pitem.payroll_item_code = pcpi.payroll_item_fk
                        where
                          TRUNC (ptrans.period_end_date, 'Q') = trunc(p_qtr_start_date) -- :qtrStartDate
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
                          pcpi.company_payroll_item_seq, ptrans.employee_fk))
                        group by
                          company_payroll_item_seq, source_employee_fk) src;

          -- dbms_output.put_line('Finished merging PRC_QTR_PAYROLL_ITEM_TOT  - ' || to_char(systimestamp, 'hh24:mi:ss'));

   END PRC_QTR_PAYROLL_ITEM_TOT;

   /******************************************
        NAME:       PRC_COMP_QTR_PAYROLL_ITEM_TOT
        PURPOSE:    To calculate payroll item totals for W2 data, this is for all companies and one year (all 4 quarters)
   ******************************************/

   PROCEDURE PRC_YEAR_PAYROLL_ITEM_TOT (
            p_year               IN   VARCHAR2 -- Quarter start date in UTC
       )
        IS
            v_utc_date TIMESTAMP; -- current system UTC date and time
        BEGIN

          SELECT SYS_EXTRACT_UTC (fn_get_psp_timestamp) INTO v_utc_date FROM DUAL;
          
          -- dbms_output.put_line('Merging PRC_YEAR_PAYROLL_ITEM_TOT, starting merge  - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
          -- Deleting records for voided payrolls
          DELETE FROM psp_ee_payrollitem_qtrtotals eePayrollItemQtrTotals
                WHERE 
                    eePayrollItemQtrTotals.year = to_number(p_year);

        INSERT INTO psp_ee_payrollitem_qtrtotals  (ee_payrollitem_qtrtotals_seq, version, creator_id, created_date,
                        modified_date, realm_id, quarter, year,
                        amount, taxable_wages, total_wages, tips_taxable_wages_amount,
                        company_payroll_item_fk, employee_fk)
                 select fn_format_sysguid (SYS_GUID ()), 1, 'PK_PAYROLL_ITEM_TOTALS-INS', v_utc_date,
                      v_utc_date, -1, to_number(src.qtr), to_number(p_year),
                      src.amount, 0, 0, 0,
                      src.company_payroll_item_seq, src.source_employee_fk
                 from  (select company_payroll_item_seq, source_employee_fk, sum(amount) amount, qtr
                         from ((select pcpi.company_payroll_item_seq company_payroll_item_seq, pcheck.source_employee_fk source_employee_fk, sum(pcomp.compensation_amount) amount, to_char(pr.paycheck_date, 'Q') qtr
                          from psp_compensation pcomp
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pcomp.company_payroll_item_fk and pcpi.company_fk = pcomp.company_fk
                          join psp_paycheck pcheck on pcomp.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pcomp.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                         to_char(pr.paycheck_date, 'YYYY') = p_year -- :year
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
                          pcpi.company_payroll_item_seq, pcheck.source_employee_fk, to_char(pr.paycheck_date, 'Q'))
                         union all
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , pcheck.source_employee_fk source_employee_fk, sum(pded.deduction_amount) amount, to_char(pr.paycheck_date, 'Q') qtr
                          from psp_deduction pded
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pded.company_payroll_item_fk and pcpi.company_fk = pded.company_fk
                          join psp_paycheck pcheck on pded.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pded.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                       to_char (pr.paycheck_date, 'YYYY') = p_year -- :year
                          and pcheck.source_employee_fk is not null
                          and pcpi.is_archived = 0
                          and pr.payroll_run_status != 'Superseded'
                          and pcheck.status = 'Active' and pcheck.SOURCE_PAYCHECK_ID not like '-%'
                          and pcpi.tax_form_line in ('ALLOCTIPS', 'DPDNTCARE', 'DPDNTCARECO', 'NONQUALPLAN', 'SEC457', 'ADOPTION',
                                                                                             'GROUPTERMLIFE', 'MEDSAVING', 'NONTAXSICK', 'QUALMVEX', 'ROTH401K', 'ROTH403B',
                                                                                             'SIMPLE', 'Q125POP', 'Q401K', 'Q403B', 'Q408K', 'Q457B',
                                                                                             'TTT14', 'Q501C', 'TTT3', 'TTT7', 'TTT8', 'FRNGBNFTS',
                                                                                             'OTHER', 'OTHMVEXP', 'TTT1', 'TTT2', 'TTT4', 'TTT5',
                                                                                             'LTAX1', 'LTAX2', 'SECLOCAL', 'TTT11', 'TTT6', 'TTT9', 'TIPS', 'TTT10', 'TTT17', 'TTT19',
                                                                                             'TTT22','TTT23','TTT24','TTT25','TTT26','TTT27','TTT28','TTT29','TTT30','TTT31','TTT32','TTT33')  --(:taxFormLines)
                        group by
                          pcpi.company_payroll_item_seq, pcheck.source_employee_fk, to_char(pr.paycheck_date, 'Q'))
                          union all
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , pcheck.source_employee_fk source_employee_fk, sum(pec.contribution_amount) amount, to_char(pr.paycheck_date, 'Q') qtr
                          from psp_employer_contribution pec
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = pec.company_payroll_item_fk and pcpi.company_fk = pec.company_fk
                          join psp_paycheck pcheck on pec.paycheck_fk = pcheck.paycheck_seq and pcheck.company_fk = pec.company_fk
                          join psp_payroll_run pr on pr.payroll_run_seq = pcheck.payroll_run_fk
                        where
                           to_char (pr.paycheck_date, 'YYYY') = p_year -- :year
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
                          pcpi.company_payroll_item_seq, pcheck.source_employee_fk, to_char(pr.paycheck_date, 'Q'))
                        union all
                        (select pcpi.company_payroll_item_seq company_payroll_item_seq , ptrans.employee_fk source_employee_fk, sum(ptransline.amount * decode(pitem.payroll_item_type, 'Deduction', -1, 'Compensation', 1, 'EmployerContribution', 1, 1)) amount, to_char(ptrans.period_end_date, 'Q') qtr
                          from psp_qbdt_payroll_trans_line ptransline
                          join psp_company_payroll_item pcpi on pcpi.company_payroll_item_seq = ptransline.company_payroll_item_fk
                          join psp_qbdt_payroll_transaction ptrans on ptrans.qbdt_payroll_transaction_seq = ptransline.qbdt_payroll_transaction_fk
                          join psp_payroll_item pitem on pitem.payroll_item_code = pcpi.payroll_item_fk
                        where
                          to_char (ptrans.period_end_date, 'YYYY') = p_year-- :year
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
                          pcpi.company_payroll_item_seq, ptrans.employee_fk, to_char(ptrans.period_end_date, 'Q')))
                        group by
                          company_payroll_item_seq, source_employee_fk, qtr) src;

          -- dbms_output.put_line('Finished merging PRC_YEAR_PAYROLL_ITEM_TOT  - ' || to_char(systimestamp, 'hh24:mi:ss'));

   END PRC_YEAR_PAYROLL_ITEM_TOT;

END PK_PAYROLL_ITEM_TOTALS;
/

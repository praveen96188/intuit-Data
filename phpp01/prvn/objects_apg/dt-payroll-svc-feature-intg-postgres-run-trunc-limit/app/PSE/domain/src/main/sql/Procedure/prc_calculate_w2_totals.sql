CREATE OR REPLACE PROCEDURE PRC_CALCULATE_W2_TOTALS
        ( p_processing_year int,
           p_company_id varchar2 )
     
IS

/******************************************************************************
   CREATED: 11/8/2012
   PURPOSE: Calculate W2 Employee Totals for a given year
            If the date is null, it will use sysdate -1 year

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        11.08.2012  Marcela Villani  Created
******************************************************************************/

    -- the UTC date is used to populate SPCF audit fields created_date and modified_date
    v_utc_date TIMESTAMP; -- current system UTC date and time

BEGIN
     SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL;
     IF p_company_id is null
     THEN
          dbms_output.put_line('Calculating Law Annual totals - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
          -- Delete and insert PSP_COMPANY_TFS_SUBMISSION
          DELETE FROM PSP_COMPANY_TFSSUBMISSION WHERE year = p_processing_year;
          
          INSERT INTO PSP_COMPANY_TFSSUBMISSION (
             COMPANY_TFSSUBMISSION_SEQ, VERSION, CREATOR_ID, 
             CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, 
             REALM_ID, SUBMISSION_STATUS, STATUS_EFFECTIVE_DATE, 
             COMPANY_FK, YEAR) 
             SELECT fn_format_sysguid (SYS_GUID ()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date,
                         'W2ANNUALCALCULATIONINS', v_utc_date, -1, 'Pending', v_utc_date,
                         company_fk, p_processing_year
             FROM ( SELECT DISTINCT company_fk
                        FROM 
                        (SELECT distinct company_fk  as company_fk from PSP_EMPLOYEE_LAW_QTR_TOTALS WHERE year = p_processing_year 
                         UNION SELECT distinct company_fk as company_fk from PSP_EE_PAYROLLITEM_QTRTOTALS pit
                                    INNER JOIN PSP_COMPANY_PAYROLL_ITEM cpi on PIT.COMPANY_PAYROLL_ITEM_FK = CPI.COMPANY_PAYROLL_ITEM_SEQ
                                    WHERE PIT.YEAR = p_processing_year) );        
 
          
          MERGE INTO psp_employee_w2_totals tgt
             USING (
                     SELECT COMPANY_FK,  EMPLOYEE_FK, COMPANY_LAW_FK,   LAW_FK,
                     SUM(TAXABLE_WAGES) as taxable_wages,
                     SUM(TAX_AMOUNT) as amount,
                     SUM(TIPS_TAXABLE_WAGES_AMOUNT) as tips_taxable_wages_amount,
                     SUM(TOTAL_WAGES) as total_wages
                     FROM PSP_EMPLOYEE_LAW_QTR_TOTALS
                     WHERE Year =  p_processing_year
                     GROUP BY COMPANY_FK,  EMPLOYEE_FK, COMPANY_LAW_FK, LAW_FK
                    ) src
             ON ( tgt.company_fk = src.company_fk
             AND tgt.employee_fk = src.employee_fk
             AND tgt.company_law_fk = src.company_law_fk
             AND tgt.year =  p_processing_year)

             WHEN MATCHED THEN
                UPDATE
                   SET tgt.amount =  src.amount,
                       tgt.taxable_wages = src.taxable_wages,
                       tgt. tips_taxable_wages_amount = src.tips_taxable_wages_amount,
                       tgt.total_wages = src.total_wages,
                       modified_date = v_utc_date,
                       version = version+1,
                       modifier_id='W2ANNUALCALCULATIONUPD'
             WHEN NOT MATCHED THEN
                INSERT  (tgt.EMPLOYEE_W2_TOTALS_SEQ, VERSION, CREATOR_ID,
                         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                         REALM_ID, YEAR, TAXABLE_WAGES,
                         AMOUNT, TIPS_TAXABLE_WAGES_AMOUNT, TOTAL_WAGES,
                         COMPANY_PAYROLL_ITEM_FK, EMPLOYEE_FK, LAW_FK,
                         COMPANY_FK, COMPANY_LAW_FK)
                VALUES (fn_format_sysguid (SYS_GUID ()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date,
                        'W2ANNUALCALCULATIONINS', v_utc_date, -1,
                         p_processing_year, src.taxable_wages,
                        src.amount, src.tips_taxable_wages_amount,
                        src.total_wages,
                        null, src.employee_fk, src.law_fk,
                        src.company_fk, src.company_law_fk
                       );

          dbms_output.put_line('Finished Calculating Law Annual totals  - ' || to_char(systimestamp, 'hh24:mi:ss'));

          dbms_output.put_line('Calculating Payroll Item Annual totals - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
          MERGE INTO psp_employee_w2_totals tgt
             USING (
                     SELECT cpi.COMPANY_FK as company_fk,  EMPLOYEE_FK, COMPANY_PAYROLL_ITEM_FK,
                     SUM(TAXABLE_WAGES) as taxable_wages,
                     SUM(AMOUNT) as amount,
                     SUM(TIPS_TAXABLE_WAGES_AMOUNT) as tips_taxable_wages_amount,
                     SUM(TOTAL_WAGES) as total_wages
                     FROM PSP_EE_PAYROLLITEM_QTRTOTALS pit
                       INNER JOIN PSP_COMPANY_PAYROLL_ITEM cpi ON PIT.COMPANY_PAYROLL_ITEM_FK = CPI.COMPANY_PAYROLL_ITEM_SEQ
                     WHERE Year = p_processing_year
                     GROUP BY cpi.COMPANY_FK,  EMPLOYEE_FK, COMPANY_PAYROLL_ITEM_FK
                    ) src
             ON ( tgt.company_fk = src.company_fk
             AND tgt.employee_fk = src.employee_fk
             AND tgt.company_payroll_item_fk = src.company_payroll_item_fk
             AND tgt.year =  p_processing_year)

             WHEN MATCHED THEN
                UPDATE
                   SET tgt.amount =  src.amount,
                       tgt.taxable_wages = src.taxable_wages,
                       tgt. tips_taxable_wages_amount = src.tips_taxable_wages_amount,
                       tgt.total_wages = src.total_wages,
                       modified_date = v_utc_date,
                       version = version+1,
                       modifier_id='W2ANNUALCALCULATIONUPD'
             WHEN NOT MATCHED THEN
                INSERT  (tgt.EMPLOYEE_W2_TOTALS_SEQ, VERSION, CREATOR_ID,
                         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                         REALM_ID, YEAR, TAXABLE_WAGES,
                         AMOUNT, TIPS_TAXABLE_WAGES_AMOUNT, TOTAL_WAGES,
                         COMPANY_PAYROLL_ITEM_FK, EMPLOYEE_FK, LAW_FK,
                         COMPANY_FK, COMPANY_LAW_FK)
                VALUES (fn_format_sysguid (SYS_GUID ()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date,
                        'W2ANNUALCALCULATIONINS', v_utc_date, -1,
                         p_processing_year, src.taxable_wages,
                        src.amount, src.tips_taxable_wages_amount,
                        src.total_wages,
                        src.company_payroll_item_fk, src.employee_fk, null,
                        src.company_fk, null
                       );

          dbms_output.put_line('Finished Calculating Payroll Item Annual totals  - ' || to_char(systimestamp, 'hh24:mi:ss'));
          COMMIT; 
 ELSE  -- Calculate for one company
     
         dbms_output.put_line('Calculating Law Annual totals - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
         
             -- Delete and insert PSP_COMPANY_TFS_SUBMISSION
          DELETE FROM PSP_COMPANY_TFSSUBMISSION WHERE year = p_processing_year and company_fk = p_company_id;
          
          INSERT INTO PSP_COMPANY_TFSSUBMISSION (
             COMPANY_TFSSUBMISSION_SEQ, VERSION, CREATOR_ID, 
             CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, 
             REALM_ID, SUBMISSION_STATUS, STATUS_EFFECTIVE_DATE, 
             COMPANY_FK, YEAR) 
             VALUES ( fn_format_sysguid (SYS_GUID ()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date,
                          'W2ANNUALCALCULATIONINS', v_utc_date, -1, 'Pending', v_utc_date,
                          p_company_id, p_processing_year);
                          
          MERGE INTO psp_employee_w2_totals tgt
             USING (
                     SELECT COMPANY_FK,  EMPLOYEE_FK, COMPANY_LAW_FK,   LAW_FK,
                     SUM(TAXABLE_WAGES) as taxable_wages,
                     SUM(TAX_AMOUNT) as amount,
                     SUM(TIPS_TAXABLE_WAGES_AMOUNT) as tips_taxable_wages_amount,
                     SUM(TOTAL_WAGES) as total_wages
                     FROM PSP_EMPLOYEE_LAW_QTR_TOTALS
                     WHERE Year =  p_processing_year
                     AND company_fk = p_company_id
                     GROUP BY COMPANY_FK,  EMPLOYEE_FK, COMPANY_LAW_FK, LAW_FK
                    ) src
             ON ( tgt.company_fk = src.company_fk
             AND tgt.employee_fk = src.employee_fk
             AND tgt.company_law_fk = src.company_law_fk
             AND tgt.year =  p_processing_year)

             WHEN MATCHED THEN
                UPDATE
                   SET tgt.amount =  src.amount,
                       tgt.taxable_wages = src.taxable_wages,
                       tgt. tips_taxable_wages_amount = src.tips_taxable_wages_amount,
                       tgt.total_wages = src.total_wages,
                       modified_date = v_utc_date,
                       version = version+1,
                       modifier_id='W2ANNUALCALCULATIONUPD'
             WHEN NOT MATCHED THEN
                INSERT  (tgt.EMPLOYEE_W2_TOTALS_SEQ, VERSION, CREATOR_ID,
                         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                         REALM_ID, YEAR, TAXABLE_WAGES,
                         AMOUNT, TIPS_TAXABLE_WAGES_AMOUNT, TOTAL_WAGES,
                         COMPANY_PAYROLL_ITEM_FK, EMPLOYEE_FK, LAW_FK,
                         COMPANY_FK, COMPANY_LAW_FK)
                VALUES (fn_format_sysguid (SYS_GUID ()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date,
                        'W2ANNUALCALCULATIONINS', v_utc_date, -1,
                         p_processing_year, src.taxable_wages,
                        src.amount, src.tips_taxable_wages_amount,
                        src.total_wages,
                        null, src.employee_fk, src.law_fk,
                        src.company_fk, src.company_law_fk
                       );

          dbms_output.put_line('Finished Calculating Law Annual totals  - ' || to_char(systimestamp, 'hh24:mi:ss'));

          dbms_output.put_line('Calculating Payroll Item Annual totals - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
          MERGE INTO psp_employee_w2_totals tgt
             USING (
                     SELECT cpi.COMPANY_FK as company_fk,  EMPLOYEE_FK, COMPANY_PAYROLL_ITEM_FK,
                     SUM(TAXABLE_WAGES) as taxable_wages,
                     SUM(AMOUNT) as amount,
                     SUM(TIPS_TAXABLE_WAGES_AMOUNT) as tips_taxable_wages_amount,
                     SUM(TOTAL_WAGES) as total_wages
                     FROM PSP_EE_PAYROLLITEM_QTRTOTALS pit
                       INNER JOIN PSP_COMPANY_PAYROLL_ITEM cpi ON PIT.COMPANY_PAYROLL_ITEM_FK = CPI.COMPANY_PAYROLL_ITEM_SEQ
                     WHERE Year = p_processing_year
                      AND company_fk = p_company_id
                     GROUP BY cpi.COMPANY_FK,  EMPLOYEE_FK, COMPANY_PAYROLL_ITEM_FK
                    ) src
             ON ( tgt.company_fk = src.company_fk
             AND tgt.employee_fk = src.employee_fk
             AND tgt.company_payroll_item_fk = src.company_payroll_item_fk
             AND tgt.year =  p_processing_year)

             WHEN MATCHED THEN
                UPDATE
                   SET tgt.amount =  src.amount,
                       tgt.taxable_wages = src.taxable_wages,
                       tgt. tips_taxable_wages_amount = src.tips_taxable_wages_amount,
                       tgt.total_wages = src.total_wages,
                       modified_date = v_utc_date,
                       version = version+1,
                       modifier_id='W2ANNUALCALCULATIONUPD'
             WHEN NOT MATCHED THEN
                INSERT  (tgt.EMPLOYEE_W2_TOTALS_SEQ, VERSION, CREATOR_ID,
                         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                         REALM_ID, YEAR, TAXABLE_WAGES,
                         AMOUNT, TIPS_TAXABLE_WAGES_AMOUNT, TOTAL_WAGES,
                         COMPANY_PAYROLL_ITEM_FK, EMPLOYEE_FK, LAW_FK,
                         COMPANY_FK, COMPANY_LAW_FK)
                VALUES (fn_format_sysguid (SYS_GUID ()), 1, 'W2ANNUALCALCULATIONINS', v_utc_date,
                        'W2ANNUALCALCULATIONINS', v_utc_date, -1,
                         p_processing_year, src.taxable_wages,
                        src.amount, src.tips_taxable_wages_amount,
                        src.total_wages,
                        src.company_payroll_item_fk, src.employee_fk, null,
                        src.company_fk, null
                       );

          dbms_output.put_line('Finished Calculating Payroll Item Annual totals  - ' || to_char(systimestamp, 'hh24:mi:ss'));
         
    END IF;
 COMMIT;
  
END PRC_CALCULATE_W2_TOTALS;
/

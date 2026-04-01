CREATE OR REPLACE PACKAGE PK_PAYROLL_ITEM_TOTALS
AS

    PROCEDURE PRC_COMP_QTR_PAYROLL_ITEM_TOT (
        p_company_seq               IN   VARCHAR2, -- Company seq
        p_qtr_start_date            IN   TIMESTAMP -- Quarter start date
    );

    PROCEDURE PRC_QTR_PAYROLL_ITEM_TOT (
        p_qtr_start_date            IN   TIMESTAMP -- Quarter start date
    );

    PROCEDURE PRC_YEAR_PAYROLL_ITEM_TOT (
        p_year               IN   VARCHAR2 -- year (YYYY) to calculate payroll item calculations
    );

END PK_PAYROLL_ITEM_TOTALS;
/
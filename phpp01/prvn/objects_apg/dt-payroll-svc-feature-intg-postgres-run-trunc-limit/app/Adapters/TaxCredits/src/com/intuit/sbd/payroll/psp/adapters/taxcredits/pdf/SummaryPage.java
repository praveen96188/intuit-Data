package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 12, 2010
 * Time: 2:36:09 PM
 */
public class SummaryPage extends Form {
    // pdf file
    public static final String FILE_NAME = "resources/SummaryPage.pdf";

    public static final String COMPANY_ADDRESS = "company_address";
    public static final String COMPANY_CITY = "company_city";
    public static final String COMPANY_CONTACT_EMAIL = "company_contact_email";
    public static final String COMPANY_CONTACT_NAME = "company_contact_name";
    public static final String COMPANY_CONTACT_PHONE = "company_contact_phone";
    public static final String COMPANY_EIN = "company_ein";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_STATE = "company_state";
    public static final String COMPANY_ZIP = "company_zip";
    public static final String COMPANY_FISCAL_YEAR_START_DATE = "company_fiscal_year_start_date";
    public static final String COMPANY_OFFER = "company_offer";

    public static final String EMPLOYEE_ADDRESS = "employee_address";
    public static final String EMPLOYEE_CITY = "employee_city";
    public static final String EMPLOYEE_COUNTY = "employee_county";
    public static final String EMPLOYEE_DATE_COMPLETED = "employee_date_completed";
    public static final String EMPLOYEE_DATE_HIRED = "employee_date_hired";
    public static final String EMPLOYEE_DATE_OFFERED = "employee_date_offered";
    public static final String EMPLOYEE_DATE_STARTED = "employee_date_started";
    public static final String EMPLOYEE_DOB = "employee_dob";
    public static final String EMPLOYEE_FIRST_NAME = "employee_first_name";
    public static final String EMPLOYEE_LAST_NAME = "employee_last_name";
    public static final String EMPLOYEE_MIDDLE_NAME = "employee_middle_name";
    public static final String EMPLOYEE_PHONE = "employee_phone";
    public static final String EMPLOYEE_SSN = "employee_ssn";
    public static final String EMPLOYEE_STARTING_WAGE = "employee_starting_wage";
    public static final String EMPLOYEE_STATE = "employee_state";
    public static final String EMPLOYEE_TITLE = "employee_title";
    public static final String EMPLOYEE_WORK_STATE = "employee_work_state";
    public static final String EMPLOYEE_WORKED_FOR_EMPLOYER = "employee_worked_for_employer";
    public static final String EMPLOYEE_ZIP = "employee_zip";
    public static final String EMPLOYEE_QUALIFYING_CATEGORY = "employee_qualifying_category";

    public String getFileName() {
        return FILE_NAME;
    }
}

package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 14, 2010
 * Time: 6:04:41 PM
 */
public class Form9061 extends Form {
    // pdf file
    public static final String FILE_NAME = "resources/f9061.pdf";

    // form fields
    public static final String ADDRESS_LINE_1 = "address_line_1";
    public static final String ADDRESS_LINE_2 = "address_line_2";
    public static final String BENEFITS_CITY_STATE_13 = "benefits_city_state_13";
    public static final String BENEFITS_CITY_STATE_14 = "benefits_city_state_14";
    public static final String BENEFITS_CITY_STATE_16 = "benefits_city_state_16";
    public static final String CHECK_NO_12 = "check_no_12";
    public static final String CHECK_NO_13_1 = "check_no_13_1";
    public static final String CHECK_NO_13_2 = "check_no_13_2";
    public static final String CHECK_NO_13_3 = "check_no_13_3";
    public static final String CHECK_NO_13_4 = "check_no_13_4";
    public static final String CHECK_NO_13_5 = "check_no_13_5";
    public static final String CHECK_NO_14_1 = "check_no_14_1";
    public static final String CHECK_NO_14_2 = "check_no_14_2";
    public static final String CHECK_NO_15_1 = "check_no_15_1";
    public static final String CHECK_NO_15_2 = "check_no_15_2";
    public static final String CHECK_NO_15_3 = "check_no_15_3";
    public static final String CHECK_NO_16_1 = "check_no_16_1";
    public static final String CHECK_NO_16_2 = "check_no_16_2";
    public static final String CHECK_NO_16_3 = "check_no_16_3";
    public static final String CHECK_NO_16_4 = "check_no_16_4";
    public static final String CHECK_NO_17_1 = "check_no_17_1";
    public static final String CHECK_NO_18_1 = "check_no_18_1";
    public static final String CHECK_NO_18_2 = "check_no_18_2";
    public static final String CHECK_NO_19_1 = "check_no_19_1";
    public static final String CHECK_NO_20_1 = "check_no_20_1";
    public static final String CHECK_NO_20_2 = "check_no_20_2";
    public static final String CHECK_NO_20_3 = "check_no_20_3";
    public static final String CHECK_NO_20_4 = "check_no_20_4";
    public static final String CHECK_NO_21_1 = "check_no_21_1";
    public static final String CHECK_NO_21_2 = "check_no_21_2";
    public static final String CHECK_NO_21_3 = "check_no_21_3";
    public static final String CHECK_NO_21_4 = "check_no_21_4";
    public static final String CHECK_NO_8 = "check_no_8";
    public static final String CHECK_YES_12 = "check_yes_12";
    public static final String CHECK_YES_13_1 = "check_yes_13_1";
    public static final String CHECK_YES_13_2 = "check_yes_13_2";
    public static final String CHECK_YES_13_3 = "check_yes_13_3";
    public static final String CHECK_YES_13_4 = "check_yes_13_4";
    public static final String CHECK_YES_13_5 = "check_yes_13_5";
    public static final String CHECK_YES_14_1 = "check_yes_14_1";
    public static final String CHECK_YES_14_2 = "check_yes_14_2";
    public static final String CHECK_YES_15_1 = "check_yes_15_1";
    public static final String CHECK_YES_15_2 = "check_yes_15_2";
    public static final String CHECK_YES_15_3 = "check_yes_15_3";
    public static final String CHECK_YES_16_1 = "check_yes_16_1";
    public static final String CHECK_YES_16_2 = "check_yes_16_2";
    public static final String CHECK_YES_16_3 = "check_yes_16_3";
    public static final String CHECK_YES_16_4 = "check_yes_16_4";
    public static final String CHECK_YES_17_1 = "check_yes_17_1";
    public static final String CHECK_YES_18_1 = "check_yes_18_1";
    public static final String CHECK_YES_18_2 = "check_yes_18_2";
    public static final String CHECK_YES_19_1 = "check_yes_19_1";
    public static final String CHECK_YES_20_1 = "check_yes_20_1";
    public static final String CHECK_YES_20_2 = "check_yes_20_2";
    public static final String CHECK_YES_20_3 = "check_yes_20_3";
    public static final String CHECK_YES_20_4 = "check_yes_20_4";
    public static final String CHECK_YES_21_1 = "check_yes_21_1";
    public static final String CHECK_YES_21_2 = "check_yes_21_2";
    public static final String CHECK_YES_21_3 = "check_yes_21_3";
    public static final String CHECK_YES_21_4 = "check_yes_21_4";
    public static final String CHECK_YES_8 = "check_yes_8";
    public static final String CITY_STATE_ZIP = "city_state_zip";
    public static final String CONVICTION_DATE = "conviction_date";
    public static final String DOCUMENT_SOURCES = "document_sources";
    public static final String EIN = "ein";
    public static final String EMPLOYEE_NAME = "employee_name";
    public static final String EMPLOYER_NAME = "employer_name";
    public static final String EMPLOYMENT_START_DATE = "employment_start_date";
    public static final String FEDERAL_CONVICTION = "federal_conviction";
    public static final String LAST_DATE_OF_EMPLOYMENT = "last_date_of_employment";
    public static final String PHONE = "phone";
    public static final String POSITION = "position";
    public static final String PRIMARY_RECIPIENT_13 = "primary_recipient_13";
    public static final String PRIMARY_RECIPIENT_14 = "primary_recipient_14";
    public static final String PRIMARY_RECIPIENT_16 = "primary_recipient_16";
    public static final String RELEASE_DATE = "release_date";
    public static final String RURAL_RENEWAL_COUNTY = "rural_renewal_county";
    public static final String SSN = "ssn";
    public static final String STARTING_WAGE = "starting_wage";
    public static final String STATE_CONVICTION = "state_conviction";
    public static final String UNDER_AGE_40 = "under_age_40";

    public String getFileName() {
        return FILE_NAME;
    }

    public Form9061() {
        shouldFlattenFile = false;
    }
}

package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 22, 2010
 * Time: 12:42:41 PM
 */
public class CTJS182 extends Form {
    // pdf file
    public static final String FILE_NAME = "resources/wotc-js182.pdf";

    // form fields
    public static final String EMPLOYEE_NAME = "employee_name";
    public static final String EMPLOYER_ADDRESS_LINE_1 = "employer_address_line_1";
    public static final String EMPLOYER_ADDRESS_LINE_2 = "employer_address_line_2";
    public static final String EMPLOYER_CITY_STATE_ZIP = "employer_city_state_zip";
    public static final String EMPLOYER_NAME = "employer_name";
    public static final String SSN = "ssn";
    public static final String START_WORK_DATE = "start_work_date";

    public String getFileName() {
        return FILE_NAME;
    }
}

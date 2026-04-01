package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

/**
 * User: dweinberg
 * Date: Apr 6, 2010
 * Time: 10:56:01 AM
 */
public class YouthSelfAttestation extends Form {

    public static final String FILE_NAME = "resources/Youth_Self_Attestation_Form.pdf";

    public static final String DATE_OF_BIRTH = "Date of Birth";
    public static final String EMPLOYER_FEDERAL_ID_EIN_NUMBER = "Employer Federal ID EIN Number";
    public static final String EMPLOYER_NAME = "Employer Name";
    public static final String NEW_HIRE_NAME = "New Hire Name";
    public static final String SOCIAL_SECURITY_NUMBER = "Social Security Number";
    public static final String CHECK = "check";
    public static final String CHECK2 = "check2";
    public static final String CHECK3 = "check3";

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

}

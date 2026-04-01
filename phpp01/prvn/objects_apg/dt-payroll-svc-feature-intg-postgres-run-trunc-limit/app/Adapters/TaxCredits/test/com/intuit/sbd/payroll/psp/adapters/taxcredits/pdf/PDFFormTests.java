package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

import com.intuit.sbd.payroll.psp.Application;
import com.lowagie.text.pdf.PdfReader;
import org.junit.After;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 14, 2010
 * Time: 11:25:21 AM
 */
public class PDFFormTests {
    @Before
    public void runBeforeEachTest() {
        //Application.truncateTables();
    }

    @After
    public void runAfterEachTest() {

    }

    public static byte[] generateTest8850() throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(Form8850.CHECKBOX_1, Form.CHECKED);
        fieldValues.put(Form8850.CHECKBOX_2, Form.CHECKED);
        fieldValues.put(Form8850.CHECKBOX_3, Form.CHECKED);
        fieldValues.put(Form8850.CHECKBOX_4, Form.CHECKED);
        fieldValues.put(Form8850.CHECKBOX_5, Form.CHECKED);
        fieldValues.put(Form8850.FIRST_HIRE_2005, Form.CHECKED);

        fieldValues.put(Form8850.EMPLOYEE_NAME, "employee name");
        fieldValues.put(Form8850.SSN_1, "123");
        fieldValues.put(Form8850.SSN_2, "12");
        fieldValues.put(Form8850.SSN_3, "1234");
        fieldValues.put(Form8850.EMPLOYEE_ADDRESS, "123 street");
        fieldValues.put(Form8850.EMPLOYEE_CITY_STATE_ZIP, "Reno, NV 89511-1234");
        fieldValues.put(Form8850.EMPLOYEE_COUNTY, "Washoe");
        fieldValues.put(Form8850.EMPLOYEE_PHONE_1, "775");
        fieldValues.put(Form8850.EMPLOYEE_PHONE_2, "555");
        fieldValues.put(Form8850.EMPLOYEE_PHONE_3, "5555");
        fieldValues.put(Form8850.DOB_MONTH, "12");
        fieldValues.put(Form8850.DOB_DAY, "25");
        fieldValues.put(Form8850.DOB_YEAR, "2001");
        fieldValues.put(Form8850.KATRINA_ADDRESS, "123 street, New Orleans, LA, county");

        fieldValues.put(Form8850.EMPLOYER_NAME, "We employee you");
        fieldValues.put(Form8850.EMPLOYER_PHONE_1, "712");
        fieldValues.put(Form8850.EMPLOYER_PHONE_2, "789");
        fieldValues.put(Form8850.EMPLOYER_PHONE_3, "8974");
        fieldValues.put(Form8850.EIN_1, "12");
        fieldValues.put(Form8850.EIN_2, "1234567");
        fieldValues.put(Form8850.EMPLOYER_ADDRESS, "587 this is a street");
        fieldValues.put(Form8850.EMPLOYER_CITY_STATE_ZIP, "Salt Lake City, UT 84123-7891");

        fieldValues.put(Form8850.CONTACT_NAME, "Contact Me");
        fieldValues.put(Form8850.CONTACT_PHONE_1, "999");
        fieldValues.put(Form8850.CONTACT_PHONE_2, "875");
        fieldValues.put(Form8850.CONTACT_PHONE_3, "9999");
        fieldValues.put(Form8850.CONTACT_ADDRESS, "8791 street address");
        fieldValues.put(Form8850.CONTACT_CITY_STATE_ZIP, "Yo Mamma, WA, 98741");

        fieldValues.put(Form8850.GROUP_NUMBER, "6");

        fieldValues.put(Form8850.INFO_MONTH, "01");
        fieldValues.put(Form8850.INFO_DAY, "01");
        fieldValues.put(Form8850.INFO_YEAR, "2005");

        fieldValues.put(Form8850.OFFER_MONTH, "02");
        fieldValues.put(Form8850.OFFER_DAY, "02");
        fieldValues.put(Form8850.OFFER_YEAR, "2007");

        fieldValues.put(Form8850.HIRE_MONTH, "03");
        fieldValues.put(Form8850.HIRE_DAY, "03");
        fieldValues.put(Form8850.HIRE_YEAR, "2008");

        fieldValues.put(Form8850.START_MONTH, "04");
        fieldValues.put(Form8850.START_DAY, "04");
        fieldValues.put(Form8850.START_YEAR, "2009");

        fieldValues.put(Form8850.START_COUNTY, "Some County");
        fieldValues.put(Form8850.TITLE, "agent");

        Form8850 form8850 = new Form8850();

        return form8850.generateForm(fieldValues);
    }

    @Test
    public void testForm8850_Generation() throws Exception {
        assertFormsEqual("resources/test_f8850.pdf", generateTest8850());
    }

    public static byte[] generateTest9061() throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(Form9061.CHECK_NO_12, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_13_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_13_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_13_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_13_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_13_5, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_14_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_14_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_15_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_15_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_15_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_16_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_16_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_16_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_16_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_17_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_18_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_18_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_19_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_20_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_20_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_20_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_20_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_21_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_21_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_21_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_21_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_NO_8, Form.CHECKED);

        fieldValues.put(Form9061.CHECK_YES_12, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_13_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_13_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_13_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_13_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_13_5, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_14_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_14_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_15_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_15_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_15_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_16_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_16_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_16_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_16_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_17_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_18_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_18_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_19_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_20_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_20_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_20_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_20_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_21_1, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_21_2, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_21_3, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_21_4, Form.CHECKED);
        fieldValues.put(Form9061.CHECK_YES_8, Form.CHECKED);

        fieldValues.put(Form9061.FEDERAL_CONVICTION, Form.CHECKED);
        fieldValues.put(Form9061.STATE_CONVICTION, Form.CHECKED);

        fieldValues.put(Form9061.EMPLOYER_NAME, "We employee you");
        fieldValues.put(Form9061.ADDRESS_LINE_1, "123 street");
        fieldValues.put(Form9061.ADDRESS_LINE_2, "Suite 5487");
        fieldValues.put(Form9061.CITY_STATE_ZIP, "Reno, NV 89502-1234");
        fieldValues.put(Form9061.PHONE, "(775)123-4567");
        fieldValues.put(Form9061.EIN, "12-123456789");
        fieldValues.put(Form9061.EMPLOYEE_NAME, "John Wilks Booth");
        fieldValues.put(Form9061.SSN, "123-12-1234");
        fieldValues.put(Form9061.LAST_DATE_OF_EMPLOYMENT, "08/05/2008");
        fieldValues.put(Form9061.EMPLOYMENT_START_DATE, "12/14/2009");
        fieldValues.put(Form9061.STARTING_WAGE, "$10.00");
        fieldValues.put(Form9061.POSITION, "Window Cleaner");
        fieldValues.put(Form9061.UNDER_AGE_40, "09/18/1984");
        fieldValues.put(Form9061.PRIMARY_RECIPIENT_13, "Ted Smith");
        fieldValues.put(Form9061.BENEFITS_CITY_STATE_13, "Reno, NV");
        fieldValues.put(Form9061.PRIMARY_RECIPIENT_14, "Johnny Smith");
        fieldValues.put(Form9061.BENEFITS_CITY_STATE_14, "Sparks, NV");
        fieldValues.put(Form9061.PRIMARY_RECIPIENT_16, "Lizzy Smith");
        fieldValues.put(Form9061.BENEFITS_CITY_STATE_16, "Carson, NV");
        fieldValues.put(Form9061.CONVICTION_DATE, "05/06/2006");
        fieldValues.put(Form9061.RELEASE_DATE, "09/08/2007");
        fieldValues.put(Form9061.RURAL_RENEWAL_COUNTY, "Washoe");
        fieldValues.put(Form9061.DOCUMENT_SOURCES, "Some documents on the floor.");

        Form9061 form9061 = new Form9061();

        return form9061.generateForm(fieldValues);
    }

    @Test
    public void testForm9061_Generation() throws Exception {
        assertFormsEqual("resources/test_f9061.pdf", generateTest9061());
    }

    public static byte[] generateTest2848() throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(Form2848.CHECK_REP_1_NEW_ADDRESS, Form.CHECKED);
        fieldValues.put(Form2848.CHECK_REP_1_NEW_FAX, Form.CHECKED);
        fieldValues.put(Form2848.CHECK_REP_1_NEW_PHONE, Form.CHECKED);

        fieldValues.put(Form2848.CHECK_REP_2_NEW_ADDRESS, Form.CHECKED);
        fieldValues.put(Form2848.CHECK_REP_2_NEW_FAX, Form.CHECKED);
        fieldValues.put(Form2848.CHECK_REP_2_NEW_PHONE, Form.CHECKED);

        fieldValues.put(Form2848.CHECK_REP_3_NEW_ADDRESS, Form.CHECKED);
        fieldValues.put(Form2848.CHECK_REP_3_NEW_FAX, Form.CHECKED);
        fieldValues.put(Form2848.CHECK_REP_3_NEW_PHONE, Form.CHECKED);

        fieldValues.put(Form2848.CHECKBOX_LINE_4, Form.CHECKED);
        fieldValues.put(Form2848.CHECKBOX_LINE_7A, Form.CHECKED);
        fieldValues.put(Form2848.CHECKBOX_LINE_7B, Form.CHECKED);
        fieldValues.put(Form2848.CHECKBOX_LINE_8, Form.CHECKED);

        fieldValues.put(Form2848.EIN_1, "12");
        fieldValues.put(Form2848.EIN_2, "1234567");
        fieldValues.put(Form2848.LINE_5_1, "blah");
        fieldValues.put(Form2848.LINE_5_2, "blah blah");
        fieldValues.put(Form2848.LINE_5_3, "blah blah blah");
        fieldValues.put(Form2848.LINE_5_4, "blah blah blah blah");
        fieldValues.put(Form2848.LINE_6_INITAL, "DM");
        fieldValues.put(Form2848.LINE_6_REP, "Da' Man");
        fieldValues.put(Form2848.PHONE_1, "775");
        fieldValues.put(Form2848.PHONE_2, "555-5555");
        fieldValues.put(Form2848.PIN_1_1, "1");
        fieldValues.put(Form2848.PIN_1_2, "2");
        fieldValues.put(Form2848.PIN_1_3, "3");
        fieldValues.put(Form2848.PIN_1_4, "4");
        fieldValues.put(Form2848.PIN_1_5, "5");
        fieldValues.put(Form2848.PIN_2_1, "6");
        fieldValues.put(Form2848.PIN_2_2, "7");
        fieldValues.put(Form2848.PIN_2_3, "8");
        fieldValues.put(Form2848.PIN_2_4, "9");
        fieldValues.put(Form2848.PIN_2_5, "0");
        fieldValues.put(Form2848.PLAN_NUMBER, "A457861");
        fieldValues.put(Form2848.PRINT_NAME_OTHER, "Abba Zabba");
        fieldValues.put(Form2848.PRINTED_NAME_1, "Tax Payer 1");
        fieldValues.put(Form2848.PRINTED_NAME_2, "Tax Payer 2");
        fieldValues.put(Form2848.REP_1_ADDRESS, "Address Rep 1");
        fieldValues.put(Form2848.REP_2_ADDRESS, "Address Rep 2");
        fieldValues.put(Form2848.REP_3_ADDRESS, "Address Rep 3");
        fieldValues.put(Form2848.REP_1_CAF, "CAF Rep 1");
        fieldValues.put(Form2848.REP_2_CAF, "CAF Rep 2");
        fieldValues.put(Form2848.REP_3_CAF, "CAF Rep 3");
        fieldValues.put(Form2848.REP_1_CITY_STATE_ZIP, "City, State zip Rep 1");
        fieldValues.put(Form2848.REP_2_CITY_STATE_ZIP, "City, State zip Rep 2");
        fieldValues.put(Form2848.REP_3_CITY_STATE_ZIP, "City, State zip Rep 3");
        fieldValues.put(Form2848.REP_1_DESIGNATION, "A");
        fieldValues.put(Form2848.REP_2_DESIGNATION, "B");
        fieldValues.put(Form2848.REP_3_DESIGNATION, "C");
        fieldValues.put(Form2848.REP_1_FAX, "Rep 1 fax");
        fieldValues.put(Form2848.REP_2_FAX, "Rep 2 fax");
        fieldValues.put(Form2848.REP_3_FAX, "Rep 3 fax");
        fieldValues.put(Form2848.REP_1_JURISDICTION, "Rep 1 State");
        fieldValues.put(Form2848.REP_2_JURISDICTION, "Rep 2 State");
        fieldValues.put(Form2848.REP_3_JURISDICTION, "Rep 3 State");
        fieldValues.put(Form2848.REP_1_NAME, "Rep 1 name");
        fieldValues.put(Form2848.REP_2_NAME, "Rep 2 name");
        fieldValues.put(Form2848.REP_3_NAME, "Rep 3 name");
        fieldValues.put(Form2848.REP_1_PHONE, "Rep 1 phone");
        fieldValues.put(Form2848.REP_2_PHONE, "Rep 2 phone");
        fieldValues.put(Form2848.REP_3_PHONE, "Rep 3 phone");
        fieldValues.put(Form2848.SSN_1_1, "111");
        fieldValues.put(Form2848.SSN_1_2, "11");
        fieldValues.put(Form2848.SSN_1_3, "1111");
        fieldValues.put(Form2848.SSN_2_1, "222");
        fieldValues.put(Form2848.SSN_2_2, "22");
        fieldValues.put(Form2848.SSN_2_3, "2222");
        fieldValues.put(Form2848.TAX_FORM_1, "940");
        fieldValues.put(Form2848.TAX_FORM_2, "941");
        fieldValues.put(Form2848.TAX_FORM_3, "944");
        fieldValues.put(Form2848.TAX_OR_PENALTY_1, "FUTA");
        fieldValues.put(Form2848.TAX_OR_PENALTY_2, "SS");
        fieldValues.put(Form2848.TAX_OR_PENALTY_3, "MED");
        fieldValues.put(Form2848.TAX_PAYER_ADDRESS_LINE_1, "Tax payer address line 1");
        fieldValues.put(Form2848.TAX_PAYER_ADDRESS_LINE_2, "Tax payer address line 2");
        fieldValues.put(Form2848.TAX_PAYER_CITY_STATE_ZIP, "Tax payer city, state zip");
        fieldValues.put(Form2848.TAX_PAYER_NAMES, "Bob & Linda Taxpayer");
        fieldValues.put(Form2848.TITLE_1, "Rep 1 title");
        fieldValues.put(Form2848.TITLE_2, "Rep 2 title");
        fieldValues.put(Form2848.YEARS_OR_PERIODS_1, "2001");
        fieldValues.put(Form2848.YEARS_OR_PERIODS_2, "2002");
        fieldValues.put(Form2848.YEARS_OR_PERIODS_3, "2003");

        Form2848 form2848 = new Form2848();

        return form2848.generateForm(fieldValues);
    }

    @Test
    public void testForm2848_Generation() throws Exception {
        assertFormsEqual("resources/test_f2848.pdf", generateTest2848());        
    }

    public static byte[] generateTestCTJS182() throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(CTJS182.EMPLOYEE_NAME, "Luke Sky Walker");
        fieldValues.put(CTJS182.EMPLOYER_ADDRESS_LINE_1, "123 Sky Walker Lane");
        fieldValues.put(CTJS182.EMPLOYER_ADDRESS_LINE_2, "APT 1967");
        fieldValues.put(CTJS182.EMPLOYER_CITY_STATE_ZIP, "Reno, NV 89511");
        fieldValues.put(CTJS182.EMPLOYER_NAME, "Jedi Fondation");
        fieldValues.put(CTJS182.SSN, "555-55-5555");
        fieldValues.put(CTJS182.START_WORK_DATE, "12/25/2008");

        CTJS182 ctjs182 = new CTJS182();

        return ctjs182.generateForm(fieldValues);
    }

    @Test
    public void testCTJS182_Generation() throws Exception {
        assertFormsEqual("resources/test_wotc-js182.pdf", generateTestCTJS182());         
    }

    public static byte[] generateTestSummaryPage() throws Exception {
         Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(SummaryPage.COMPANY_ADDRESS, "123 Company Address St.");
        fieldValues.put(SummaryPage.COMPANY_CITY, "Company City");
        fieldValues.put(SummaryPage.COMPANY_CONTACT_EMAIL, "company_email@mycompanywebsite.com");
        fieldValues.put(SummaryPage.COMPANY_CONTACT_NAME, "Tater Salad");
        fieldValues.put(SummaryPage.COMPANY_CONTACT_PHONE, "(775) 555-1234 x12345");
        fieldValues.put(SummaryPage.COMPANY_EIN, "12-1234567");
        fieldValues.put(SummaryPage.COMPANY_NAME, "The greatest company in the world");
        fieldValues.put(SummaryPage.COMPANY_STATE, "NV");
        fieldValues.put(SummaryPage.COMPANY_ZIP, "89520-3002");
        fieldValues.put(SummaryPage.COMPANY_FISCAL_YEAR_START_DATE, "02/11");
        fieldValues.put(SummaryPage.COMPANY_OFFER, "TC600");

        fieldValues.put(SummaryPage.EMPLOYEE_ADDRESS, "123 Employee Address St.");
        fieldValues.put(SummaryPage.EMPLOYEE_CITY, "Employee City");
        fieldValues.put(SummaryPage.EMPLOYEE_COUNTY, "County is not Country");
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_COMPLETED, "01/01/2001");
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_HIRED, "02/02/2002");
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_OFFERED, "03/03/2003");
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_STARTED, "04/04/2004");
        fieldValues.put(SummaryPage.EMPLOYEE_DOB, "05/05/2005");
        fieldValues.put(SummaryPage.EMPLOYEE_FIRST_NAME, "Taylor");
        fieldValues.put(SummaryPage.EMPLOYEE_LAST_NAME, "Kitsch");
        fieldValues.put(SummaryPage.EMPLOYEE_MIDDLE_NAME, "M");
        fieldValues.put(SummaryPage.EMPLOYEE_PHONE, "(111) 111-1234 x12345");
        fieldValues.put(SummaryPage.EMPLOYEE_SSN, "111-11-1111");
        fieldValues.put(SummaryPage.EMPLOYEE_STARTING_WAGE, "$1 Billion");
        fieldValues.put(SummaryPage.EMPLOYEE_STATE, "CA");
        fieldValues.put(SummaryPage.EMPLOYEE_TITLE, "The guy that feeds the fish at the pet store");
        fieldValues.put(SummaryPage.EMPLOYEE_WORK_STATE, "CA");
        fieldValues.put(SummaryPage.EMPLOYEE_WORKED_FOR_EMPLOYER, "Previously? Yes");
        fieldValues.put(SummaryPage.EMPLOYEE_ZIP, "11111-9999");
        fieldValues.put(SummaryPage.EMPLOYEE_QUALIFYING_CATEGORY, "DisabledVeteran, DesignatedCommunityResident");

        SummaryPage summaryPage = new SummaryPage();

        return summaryPage.generateForm(fieldValues);
    }

    @Test
    public void testSummaryPage_Generation() throws Exception {
        assertFormsEqual("resources/test_SummaryPage.pdf", generateTestSummaryPage());
    }

    public static byte[] generateTestCoverPage() throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(CoverPage.POSTMARK_DATE, "May 30, 2010");

        CoverPage coverPage = new CoverPage();

        return coverPage.generateForm(fieldValues);
    }

    @Test
    public void testCoverPage_Generation() throws Exception {
        assertFormsEqual("resources/test_CoverPage.pdf", generateTestCoverPage());        
    }

    public static byte[] generateTestYouthSelfAttestation() throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(YouthSelfAttestation.NEW_HIRE_NAME, "Joe Employee");
        fieldValues.put(YouthSelfAttestation.SOCIAL_SECURITY_NUMBER, "12-345-6789");
        fieldValues.put(YouthSelfAttestation.DATE_OF_BIRTH, "10/10/1990");
        fieldValues.put(YouthSelfAttestation.EMPLOYER_NAME, "The second greatest company in the world");
        fieldValues.put(YouthSelfAttestation.EMPLOYER_FEDERAL_ID_EIN_NUMBER, "11-1234567");
        fieldValues.put(YouthSelfAttestation.CHECK, Form.CHECKED);
        fieldValues.put(YouthSelfAttestation.CHECK2, Form.CHECKED);
        fieldValues.put(YouthSelfAttestation.CHECK3, Form.CHECKED);

        YouthSelfAttestation youthSelfAttestation = new YouthSelfAttestation();

        return youthSelfAttestation.generateForm(fieldValues);
    }

    @Test
    public void testYouthSelfAttestation_Generation() throws Exception {
        assertFormsEqual("resources/test_Youth_Self_Attestation_Form.pdf", generateTestYouthSelfAttestation());
    }

    @Test
    public void testFormConcat() {
        try {
            List<byte[]> pdfs = new ArrayList<byte[]>();
            pdfs.add(FormUtils.getBytesFromFile(new File(Application.findFileOnClassPath("resources/test_f2848.pdf"))));
            pdfs.add(FormUtils.getBytesFromFile(new File(Application.findFileOnClassPath(new Form8850().getFileName()))));
            pdfs.add(FormUtils.getBytesFromFile(new File(Application.findFileOnClassPath(new Form9061().getFileName()))));

            byte[] generatedForm = FormUtils.combinePdfs(pdfs);                                        
            PdfReader pdfReader = new PdfReader(generatedForm);
            assertEquals("number of pages", 8, pdfReader.getNumberOfPages());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }


    private void assertFormsEqual(byte[] expectedPdf, byte[] actualPdf) throws Exception {
        assertTrue("form is empty", actualPdf.length > 0);
        assertEquals("forms do not match", expectedPdf.length, expectedPdf.length);
    }

    private void assertFormsEqual(String expectedPdfFileName, byte[] actualPdf) throws Exception {
        assertFormsEqual(FormUtils.getBytesFromFile(new File(Application.findFileOnClassPath(expectedPdfFileName))), actualPdf);
    }
    

}

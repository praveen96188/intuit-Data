package com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.*;
import com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TaxCredits9061DTO;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.gateways.echosign.EchoSignGateway;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: dweinberg
 * Date: Jan 4, 2010
 * Time: 12:23:00 PM
 */
public class TaxCreditsAdapter {

    private static SecureRandom random = new SecureRandom();

    private static final ISpcfImmutableConfiguration sfConfig;
    static {
        sfConfig = ConfigurationManager.getSettings(ConfigurationModule.EmailGateway);
    }

    private static final int DEADLINE_DAYS = 23;

    private Map<String, String> states;
    private Map<String, Set<String>> ruralRenewalCounties;
    private Map<String, List<List<String>>> documentation;

    private String[] numbers = {"", "", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen"};

    private static final SpcfLogger logger = PayrollServices.getLogger(TaxCreditsAdapter.class);

    public TaxCreditsAdapter() throws Exception {
        try {
            createStatesMap();
            createCountiesMap();
            createDocumentationMap();
        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }

    private void createStatesMap() throws IOException {
        states = new HashMap<String,String>();

        File statesFile = new File(Application.findFileOnClassPath("resources/states.csv"));
        BufferedReader statesFileReader  = new BufferedReader(new FileReader(statesFile));
        String line;

        while((line = statesFileReader.readLine()) != null) {
            String[] values = line.split(",");
            states.put(values[1].trim(), values[0].trim());
        }
    }

    private void createCountiesMap() throws IOException {
        ruralRenewalCounties = new HashMap<String, Set<String>>();
        File countiesFile = new File(Application.findFileOnClassPath("resources/ruralRenewalCounties.csv"));
        BufferedReader countiesFileReader  = new BufferedReader(new FileReader(countiesFile));
        String line;

        while((line = countiesFileReader.readLine()) != null) {
            String[] values = line.split(",");
            String state = values[0].trim().toUpperCase();
            Set<String> counties = new TreeSet<String>();
            for (int i = 1; i < values.length; i++) {
                counties.add(values[i].trim().toUpperCase());
            }
            ruralRenewalCounties.put(state, counties);
        }
    }

    private void createDocumentationMap() throws Exception {
        documentation = new HashMap<String, List<List<String>>>();
        File documentationFile = new File(Application.findFileOnClassPath("resources/documentation.xml"));
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(documentationFile);
        NodeList categories = doc.getElementsByTagName("Category");
        for (int i=0; i < categories.getLength(); i++) {
            org.w3c.dom.Element category = (org.w3c.dom.Element) categories.item(i);
            String categoryName = category.getAttribute("name");
            documentation.put(categoryName, new ArrayList<List<String>>());

            NodeList groups = category.getElementsByTagName("Group");
            for (int j=0; j < groups.getLength(); j++) {
                org.w3c.dom.Element group = (org.w3c.dom.Element) groups.item(j);
                documentation.get(categoryName).add(j, new ArrayList<String>());

                NodeList options = group.getElementsByTagName("Option");
                for (int k=0; k < options.getLength(); k++) {
                    org.w3c.dom.Element option = (org.w3c.dom.Element) options.item(k);    
                    documentation.get(categoryName).get(j).add(option.getFirstChild().getNodeValue());
                }

            }

            
        }


    }


    public Date getPSPDate() {
        return TaxCreditsTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime());                
    }

    public ApplicationInfo submitApplication(EmployerInfo employerInfo, EmployeeInfo employeeInfo, EligibilityInfo eligibilityInfo) throws Exception {
        ApplicationInfo applicationInfo = new ApplicationInfo();

        validateEligibilityInfo(eligibilityInfo);
        validateEmployeeInfo(employeeInfo);
        validateEmployerInfo(employerInfo);

        //get categories
        List<WOTCCategory> qualifyingCategories = getCategories(eligibilityInfo, employeeInfo);
        if (qualifyingCategories.size() == 0){
            throw new Exception("Invalid input: Must have one more qualifying categories");
        }

        //build proof docs
        applicationInfo.setProofDocumentGroups(new ArrayList<List<String>>());
        for (WOTCCategory category : qualifyingCategories) {
            for (List<String> group : documentation.get(category.getDocumentationName())) {                
                applicationInfo.getProofDocumentGroups().add(group);
            }
        }

        List<byte[]> pdfs = new ArrayList<byte[]>();

        //create cover page
        pdfs.add(createCoverPage(employeeInfo, employerInfo));

        //create summary
        pdfs.add(createSummaryPage(employeeInfo, employerInfo, qualifyingCategories, eligibilityInfo.getUnemployed60()));

        //create 2848
        pdfs.add(create2848(employerInfo));

        //create 8850
        pdfs.add(create8850(employerInfo, employeeInfo, eligibilityInfo));
        
        //create CT disclosure if needed
        if (employeeInfo.getWorkState().equals("CT")) {
            pdfs.add(createCTJS182(employerInfo, employeeInfo));            
        }

        //create attestment if disconnected youth
        createAttestment(employeeInfo, employerInfo, eligibilityInfo, qualifyingCategories, pdfs);

        //create TOS
        pdfs.add(FormUtils.getBytesFromFile(new File(Application.findFileOnClassPath("resources/TermsOfService.pdf"))));

        //combine packet
        byte[] packet = FormUtils.combinePdfs(pdfs);

        String password = generateRandomPassword();
        applicationInfo.setPassword(password);

        //send out for signing
        String newDocId = EchoSignGateway.sendPacketForSigning(packet,
                employerInfo.getAuthSignerEmail(),
                employeeInfo.getEmail(),
                employerInfo.getCompanyLegalName(),
                employeeInfo.getFullName(),
                password,
                employeeInfo.getHireDate(),
                DEADLINE_DAYS
        );

        //create and store 9061
        byte[] generated9061 = create9061(employerInfo, employeeInfo, eligibilityInfo);
        PayrollServices.beginUnitOfWork();
        try {
            
            TaxCredits9061DTO dto = new TaxCredits9061DTO(generated9061, employeeInfo.getFullName(), employerInfo.getEin(), employeeInfo.getSsn(), employeeInfo.getEmail(), employerInfo.getAuthSignerEmail(), newDocId, password, packet);
            ProcessResult pr = PayrollServices.taxCreditsManager.add9061Form(dto);
            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                throw new Exception("Error adding 9061 form: " + pr.getMessages().toString());
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return applicationInfo;
    }

    private String generateRandomPassword() {
        int count = 8 + (Math.abs(random.nextInt()) % 3);
        return RandomStringUtils.random(count, 0, 0, true, true, null, random);
    }

    private byte[] createCoverPage(EmployeeInfo employeeInfo, EmployerInfo employerInfo) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        SpcfCalendar hireCal = SpcfCalendar.createInstance(employeeInfo.getHireDate().getTime());        
        hireCal.addDays(DEADLINE_DAYS);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        String postMarkDate = sdf.format(new Date(hireCal.getTimeInMilliseconds()));                        

        fieldValues.put(CoverPage.POSTMARK_DATE, postMarkDate);

        CoverPage coverPage = new CoverPage();
        return coverPage.generateForm(fieldValues);

    }


    private byte[] createSummaryPage(EmployeeInfo employeeInfo, EmployerInfo employerInfo, List<WOTCCategory> qualifyingCategories, boolean hireActExemptionEligible) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(SummaryPage.COMPANY_ADDRESS, employerInfo.getLegalAddress().getAllAddressLinesAsOneLine());
        fieldValues.put(SummaryPage.COMPANY_CITY, employerInfo.getLegalAddress().getCity());
        fieldValues.put(SummaryPage.COMPANY_CONTACT_EMAIL, employerInfo.getContactEmail());
        fieldValues.put(SummaryPage.COMPANY_CONTACT_NAME, employerInfo.getContactName());
        fieldValues.put(SummaryPage.COMPANY_CONTACT_PHONE, TaxCreditsTranslator.formatPhoneNumber(employerInfo.getTelephoneNumber(), employerInfo.getTelephoneExtension()));
        fieldValues.put(SummaryPage.COMPANY_EIN, TaxCreditsTranslator.formatEIN(employerInfo.getEin()));
        fieldValues.put(SummaryPage.COMPANY_NAME, employerInfo.getCompanyLegalName());
        fieldValues.put(SummaryPage.COMPANY_STATE, employerInfo.getLegalAddress().getState());
        fieldValues.put(SummaryPage.COMPANY_ZIP, employerInfo.getLegalAddress().getZip());
        if (! employerInfo.getFiscalYearStartDateString().equals("0000")) {
            fieldValues.put(SummaryPage.COMPANY_FISCAL_YEAR_START_DATE, employerInfo.getFiscalYearStartDateString().substring(0,2) + "/" + employerInfo.getFiscalYearStartDateString().substring(2,4));
        }
        fieldValues.put(SummaryPage.COMPANY_OFFER, employerInfo.getOfferCode());

        fieldValues.put(SummaryPage.EMPLOYEE_ADDRESS, employeeInfo.getLiveAddress().getAllAddressLinesAsOneLine());
        fieldValues.put(SummaryPage.EMPLOYEE_CITY, employeeInfo.getLiveAddress().getCity());
        fieldValues.put(SummaryPage.EMPLOYEE_COUNTY, employeeInfo.getLiveAddress().getCounty());
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_COMPLETED, TaxCreditsTranslator.formatShortDate(getProvidedInfoDate(employeeInfo.getJobOfferDate())));
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_HIRED, TaxCreditsTranslator.formatShortDateString(employeeInfo.getHireDateString()));
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_OFFERED, TaxCreditsTranslator.formatShortDateString(employeeInfo.getJobOfferDateString()));
        fieldValues.put(SummaryPage.EMPLOYEE_DATE_STARTED, TaxCreditsTranslator.formatShortDateString(employeeInfo.getStartDateString()));
        fieldValues.put(SummaryPage.EMPLOYEE_DOB, TaxCreditsTranslator.formatShortDateString(employeeInfo.getDateOfBirthString()));
        fieldValues.put(SummaryPage.EMPLOYEE_FIRST_NAME, employeeInfo.getFirstName());
        fieldValues.put(SummaryPage.EMPLOYEE_LAST_NAME, employeeInfo.getLastName());
        fieldValues.put(SummaryPage.EMPLOYEE_MIDDLE_NAME, employeeInfo.getMiddleInitial());
        fieldValues.put(SummaryPage.EMPLOYEE_PHONE, TaxCreditsTranslator.formatPhoneNumber(employeeInfo.getTelephoneNumber(), employeeInfo.getTelephoneExtension()));
        fieldValues.put(SummaryPage.EMPLOYEE_SSN, TaxCreditsTranslator.formatSSN(employeeInfo.getSsn()));
        fieldValues.put(SummaryPage.EMPLOYEE_STARTING_WAGE, employeeInfo.getStartingWage());
        fieldValues.put(SummaryPage.EMPLOYEE_STATE, employeeInfo.getLiveAddress().getState());
        fieldValues.put(SummaryPage.EMPLOYEE_TITLE, employeeInfo.getPosition());
        fieldValues.put(SummaryPage.EMPLOYEE_WORK_STATE, employeeInfo.getWorkState());
        fieldValues.put(SummaryPage.EMPLOYEE_WORKED_FOR_EMPLOYER, "No");
        fieldValues.put(SummaryPage.EMPLOYEE_ZIP, employeeInfo.getLiveAddress().getZip());


        StringBuffer qualifyingCategoryText = new StringBuffer();
        for (WOTCCategory qualifyingCategory : qualifyingCategories) {
            qualifyingCategoryText.append(qualifyingCategory.getCategory()).append(", ");
        }
        qualifyingCategoryText.setLength(qualifyingCategoryText.length() - 2);
        if (hireActExemptionEligible) {
            qualifyingCategoryText.append(", HIRE Act Retention Bonus");
        }
        fieldValues.put(SummaryPage.EMPLOYEE_QUALIFYING_CATEGORY, qualifyingCategoryText.toString());

        SummaryPage summaryPage = new SummaryPage();

        return summaryPage.generateForm(fieldValues);

    }

    //as per enhancement "Interview Modification," provided info date = job offer date - 1
    private Date getProvidedInfoDate(Date jobOfferDate) {
        SpcfCalendar jobOfferCal = TaxCreditsTranslator.getSpcfCalendarFromDate(jobOfferDate);
        jobOfferCal.addDays(-1);
        return TaxCreditsTranslator.getDateFromSpcfCalendar(jobOfferCal);
    }

    private byte[] create8850(EmployerInfo employerInfo, EmployeeInfo employeeInfo, EligibilityInfo eligibilityInfo) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        int hireDateAge = getAgeAsOf(employeeInfo.getDateOfBirth(), employeeInfo.getHireDate());
        //employee section

        fieldValues.put(Form8850.EMPLOYEE_NAME, employeeInfo.getFullName());
        fieldValues.put(Form8850.SSN_1, employeeInfo.getSsn().substring(0,3));
        fieldValues.put(Form8850.SSN_2, employeeInfo.getSsn().substring(3,5));
        fieldValues.put(Form8850.SSN_3, employeeInfo.getSsn().substring(5,9));
        fieldValues.put(Form8850.EMPLOYEE_ADDRESS, employeeInfo.getLiveAddress().getAllAddressLinesAsOneLine());
        fieldValues.put(Form8850.EMPLOYEE_CITY_STATE_ZIP, employeeInfo.getLiveAddress().getCityStateZipAsOneLine());
        fieldValues.put(Form8850.EMPLOYEE_COUNTY, employeeInfo.getLiveAddress().getCounty());
        fieldValues.put(Form8850.EMPLOYEE_PHONE_1, employeeInfo.getTelephoneNumber().substring(0,3));
        fieldValues.put(Form8850.EMPLOYEE_PHONE_2, employeeInfo.getTelephoneNumber().substring(3,6));
        String phoneLast4 = employeeInfo.getTelephoneNumber().substring(6);
        if (employeeInfo.getTelephoneExtension() != null && !employeeInfo.getTelephoneExtension().equals("")) {
            phoneLast4 += " x" + employeeInfo.getTelephoneExtension();
        }
        fieldValues.put(Form8850.EMPLOYEE_PHONE_3, phoneLast4);

        SpcfCalendar fortyYearsAgo = PSPDate.getPSPTime();
        fortyYearsAgo.addYears(-40);
        SpcfCalendar dob = TaxCreditsTranslator.getSpcfCalendarFromDate(employeeInfo.getDateOfBirth());
        if (dob.after(fortyYearsAgo)) {
            fieldValues.put(Form8850.DOB_MONTH, Integer.toString(dob.getMonth()));
            fieldValues.put(Form8850.DOB_DAY, Integer.toString(dob.getDay()));
            fieldValues.put(Form8850.DOB_YEAR, Integer.toString(dob.getYear()));
        }

        //eligibility section
        if (eligibilityInfo.getConditionalCertification()) {
            fieldValues.put(Form8850.CHECKBOX_2, Form.CHECKED);
        }

        if (
                //snap
                eligibilityInfo.getSnapLast6Months() ||
                eligibilityInfo.getSnapLast3of5MonthsNoLongerEligible() ||
                //tanf
                eligibilityInfo.getTanf9of18Months() ||
                //referral
                eligibilityInfo.getReferralVocationalRehabilitationAgency() ||
                eligibilityInfo.getReferralEmploymentNetwork() ||
                eligibilityInfo.getReferralDepartmentVeteranAffairs() ||
                //veteran
                eligibilityInfo.getVeteranSnap3of15Months() ||
                eligibilityInfo.getVeteranUnemploymentCompensation4weeksOfLastYear() ||
                //disconnected
                eligibilityInfo.getDisconnectedSchoolLessThan10Hours() ||
                eligibilityInfo.getDisconnectedAdmittedSinceCertificate() ||
                //felon
                eligibilityInfo.getFelonLastYear() ||
                //SSI
                eligibilityInfo.getSsiWithin60Days()
           ) {
            fieldValues.put(Form8850.CHECKBOX_3, Form.CHECKED);
        }

        if (eligibilityInfo.getVeteranDischargedBeforeHired() || eligibilityInfo.getVeteranUnemployed6MonthsBeforeHired()) {
            fieldValues.put(Form8850.CHECKBOX_4, Form.CHECKED);
        }

        if (
                eligibilityInfo.getTanfLast18Months() ||
                eligibilityInfo.getTanfStopLaw2Years()
           ) {
            fieldValues.put(Form8850.CHECKBOX_5, Form.CHECKED);                        
        }


        //employer section

        fieldValues.put(Form8850.EMPLOYER_NAME, employerInfo.getCompanyLegalName());
        fieldValues.put(Form8850.EMPLOYER_PHONE_1, employerInfo.getTelephoneNumber().substring(0,3));
        fieldValues.put(Form8850.EMPLOYER_PHONE_2, employerInfo.getTelephoneNumber().substring(3,6));
        fieldValues.put(Form8850.EMPLOYER_PHONE_3, employerInfo.getTelephoneNumber().substring(6));
        fieldValues.put(Form8850.EIN_1, employerInfo.getEin().substring(0,2));
        fieldValues.put(Form8850.EIN_2, employerInfo.getEin().substring(2,9));
        fieldValues.put(Form8850.EMPLOYER_ADDRESS, employerInfo.getLegalAddress().getAllAddressLinesAsOneLine());
        fieldValues.put(Form8850.EMPLOYER_CITY_STATE_ZIP, employerInfo.getLegalAddress().getCityStateZipAsOneLine());

        //secondary contact info
        fieldValues.put(Form8850.CONTACT_NAME, "LAWRENCE JOBE");
        fieldValues.put(Form8850.CONTACT_PHONE_1, "775");
        fieldValues.put(Form8850.CONTACT_PHONE_2, "424");
        fieldValues.put(Form8850.CONTACT_PHONE_3, "8027");
        fieldValues.put(Form8850.CONTACT_ADDRESS, "PO BOX 30005");
        fieldValues.put(Form8850.CONTACT_CITY_STATE_ZIP, "RENO NV 89520");


        //more eligibility
        if (eligibilityInfo.getDesignatedEZorRC() || isInRuralRenewalCounty(employeeInfo)) {
            if (hireDateAge >= 18 && hireDateAge < 40) {
                fieldValues.put(Form8850.GROUP_NUMBER, "4");
            } else if (hireDateAge >= 16 && hireDateAge < 18 && eligibilityInfo.getSummerYouthEmployedBetween1Mayand15September()) {
                fieldValues.put(Form8850.GROUP_NUMBER, "6");                
            }
        }


        //more employee info
        {
            SpcfCalendar providedInfoDate = TaxCreditsTranslator.getSpcfCalendarFromDate(getProvidedInfoDate(employeeInfo.getJobOfferDate()));
            fieldValues.put(Form8850.INFO_MONTH, Integer.toString(providedInfoDate.getMonth()));
            fieldValues.put(Form8850.INFO_DAY, Integer.toString(providedInfoDate.getDay()));
            fieldValues.put(Form8850.INFO_YEAR, Integer.toString(providedInfoDate.getYear()));
        }

        {
            SpcfCalendar jobOfferDate = TaxCreditsTranslator.getSpcfCalendarFromDate(employeeInfo.getJobOfferDate());
            fieldValues.put(Form8850.OFFER_MONTH, Integer.toString(jobOfferDate.getMonth()));
            fieldValues.put(Form8850.OFFER_DAY, Integer.toString(jobOfferDate.getDay()));
            fieldValues.put(Form8850.OFFER_YEAR, Integer.toString(jobOfferDate.getYear()));
        }

        {
            SpcfCalendar hireDate = TaxCreditsTranslator.getSpcfCalendarFromDate(employeeInfo.getHireDate());
            fieldValues.put(Form8850.HIRE_MONTH, Integer.toString(hireDate.getMonth()));
            fieldValues.put(Form8850.HIRE_DAY, Integer.toString(hireDate.getDay()));
            fieldValues.put(Form8850.HIRE_YEAR, Integer.toString(hireDate.getYear()));
        }

        {
            SpcfCalendar startDate = TaxCreditsTranslator.getSpcfCalendarFromDate(employeeInfo.getStartDate());
            fieldValues.put(Form8850.START_MONTH, Integer.toString(startDate.getMonth()));
            fieldValues.put(Form8850.START_DAY, Integer.toString(startDate.getDay()));
            fieldValues.put(Form8850.START_YEAR, Integer.toString(startDate.getYear()));
        }

        Form8850 form8850 = new Form8850();
        return form8850.generateForm(fieldValues);        
    }

    private void createAttestment(EmployeeInfo employeeInfo, EmployerInfo employerInfo, EligibilityInfo eligibilityInfo, List<WOTCCategory> qualifyingCategories, List<byte[]> pdfs) throws Exception {        
        for (WOTCCategory category : qualifyingCategories) {
            if (category.getCategory().equals(WOTCCategoryName.DisconnectedYouth.toString())) {
                pdfs.add(createYouthAttestation(employeeInfo, employerInfo, eligibilityInfo));
            }
        }
    }

    private byte[] createYouthAttestation(EmployeeInfo employeeInfo, EmployerInfo employerInfo, EligibilityInfo eligibilityInfo) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(YouthSelfAttestation.NEW_HIRE_NAME, employeeInfo.getFullName());
        fieldValues.put(YouthSelfAttestation.SOCIAL_SECURITY_NUMBER, TaxCreditsTranslator.formatSSN(employeeInfo.getSsn()));
        fieldValues.put(YouthSelfAttestation.DATE_OF_BIRTH, TaxCreditsTranslator.formatShortDate(employeeInfo.getDateOfBirth()));
        fieldValues.put(YouthSelfAttestation.EMPLOYER_NAME, employerInfo.getCompanyLegalName());
        fieldValues.put(YouthSelfAttestation.EMPLOYER_FEDERAL_ID_EIN_NUMBER, TaxCreditsTranslator.formatEIN(employerInfo.getEin()));
        if (eligibilityInfo.getDisconnectedSchoolLessThan10Hours()) {
            fieldValues.put(YouthSelfAttestation.CHECK, Form.CHECKED);
        }
        if (! eligibilityInfo.getDisconnectedGraduateGED()) {
            fieldValues.put(YouthSelfAttestation.CHECK2, Form.CHECKED);
        }
        if (eligibilityInfo.getDisconnectedGraduateGED() && ! eligibilityInfo.getDisconnectedAdmittedSinceCertificate() && eligibilityInfo.getDisconnectedNotRegularlyEmployedLast6Months()) {
            fieldValues.put(YouthSelfAttestation.CHECK3, Form.CHECKED);
        }

        YouthSelfAttestation ysa = new YouthSelfAttestation();
        return ysa.generateForm(fieldValues);
    }

    private byte[] create9061(EmployerInfo employerInfo, EmployeeInfo employeeInfo, EligibilityInfo eligibilityInfo) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        int hireDateAge = getAgeAsOf(employeeInfo.getDateOfBirth(), employeeInfo.getHireDate());

        fieldValues.put(Form9061.EMPLOYER_NAME, employerInfo.getCompanyLegalName());
        fieldValues.put(Form9061.ADDRESS_LINE_1, employerInfo.getLegalAddress().getAddress1());
        fieldValues.put(Form9061.ADDRESS_LINE_2, employerInfo.getLegalAddress().getAddress2());
        fieldValues.put(Form9061.CITY_STATE_ZIP, employerInfo.getLegalAddress().getCityStateZipAsOneLine());
        fieldValues.put(Form9061.PHONE, TaxCreditsTranslator.formatPhoneNumber(employerInfo.getTelephoneNumber(), employerInfo.getTelephoneExtension()));
        fieldValues.put(Form9061.EIN, TaxCreditsTranslator.formatEIN(employerInfo.getEin()));

        fieldValues.put(Form9061.EMPLOYEE_NAME, employeeInfo.getReverseFullName());
        fieldValues.put(Form9061.SSN, TaxCreditsTranslator.formatSSN(employeeInfo.getSsn()));

        fieldValues.put(Form9061.CHECK_NO_8, Form.CHECKED);

        fieldValues.put(Form9061.EMPLOYMENT_START_DATE, TaxCreditsTranslator.formatShortDate(employeeInfo.getStartDate()));
        fieldValues.put(Form9061.STARTING_WAGE, employeeInfo.getStartingWage());
        fieldValues.put(Form9061.POSITION, employeeInfo.getPosition());



        //section 12
        if (hireDateAge >= 16 && hireDateAge < 40) {
            fieldValues.put(Form9061.CHECK_YES_12, Form.CHECKED);
            fieldValues.put(Form9061.UNDER_AGE_40, TaxCreditsTranslator.formatShortDate(employeeInfo.getDateOfBirth()));
        } else {
            fieldValues.put(Form9061.CHECK_NO_12, Form.CHECKED);    
        }


        //section 13
        if (eligibilityInfo.getVeteran()) {
            fieldValues.put(Form9061.CHECK_YES_13_1, Form.CHECKED);

            if (eligibilityInfo.getVeteranSnap3of15Months()) {
                fieldValues.put(Form9061.CHECK_YES_13_2, Form.CHECKED);
                fieldValues.put(Form9061.PRIMARY_RECIPIENT_13, eligibilityInfo.getVeteranSnapPrimaryRecipient());
                fieldValues.put(Form9061.BENEFITS_CITY_STATE_13, eligibilityInfo.getVeteranSnapCityStateBenefitsReceived());                
            } else {
                fieldValues.put(Form9061.CHECK_NO_13_2, Form.CHECKED);
            }

            if (eligibilityInfo.getVeteranDisability()) {
                fieldValues.put(Form9061.CHECK_YES_13_3, Form.CHECKED);

                if (eligibilityInfo.getVeteranDischargedBeforeHired()) {
                    fieldValues.put(Form9061.CHECK_YES_13_4, Form.CHECKED);
                } else {
                    fieldValues.put(Form9061.CHECK_NO_13_4, Form.CHECKED);
                }

                if (eligibilityInfo.getVeteranUnemployed6MonthsBeforeHired()) {
                    fieldValues.put(Form9061.CHECK_YES_13_5, Form.CHECKED);
                } else {
                    fieldValues.put(Form9061.CHECK_NO_13_5, Form.CHECKED);
                }
            } else {
                fieldValues.put(Form9061.CHECK_NO_13_3, Form.CHECKED);
            }
        } else {
            fieldValues.put(Form9061.CHECK_NO_13_1, Form.CHECKED);
        }

        //section 14
        boolean fillBox14 = false;
        if (eligibilityInfo.getSnapLast6Months()) {
            fieldValues.put(Form9061.CHECK_YES_14_1, Form.CHECKED);
            fillBox14 = true;
        } else {
            fieldValues.put(Form9061.CHECK_NO_14_1, Form.CHECKED);
        }

        if (eligibilityInfo.getSnapLast3of5MonthsNoLongerEligible()) {
            fieldValues.put(Form9061.CHECK_YES_14_2, Form.CHECKED);
            fillBox14 = true;
        } else {
            fieldValues.put(Form9061.CHECK_NO_14_2, Form.CHECKED);
        }

        if (fillBox14) {
            fieldValues.put(Form9061.PRIMARY_RECIPIENT_14, eligibilityInfo.getSnapPrimaryRecipient());
            fieldValues.put(Form9061.BENEFITS_CITY_STATE_14, eligibilityInfo.getSnapCityStateBenefitsReceived());
        }

        //section 15
        if (eligibilityInfo.getReferralVocationalRehabilitationAgency()) {
            fieldValues.put(Form9061.CHECK_YES_15_1, Form.CHECKED);
        } else {
            fieldValues.put(Form9061.CHECK_NO_15_1, Form.CHECKED);
        }

        if (eligibilityInfo.getReferralEmploymentNetwork()) {
            fieldValues.put(Form9061.CHECK_YES_15_2, Form.CHECKED);
        } else {
            fieldValues.put(Form9061.CHECK_NO_15_2, Form.CHECKED);
        }

        if (eligibilityInfo.getReferralDepartmentVeteranAffairs()) {
            fieldValues.put(Form9061.CHECK_YES_15_3, Form.CHECKED);
        } else {
            fieldValues.put(Form9061.CHECK_NO_15_3, Form.CHECKED);
        }

        //section 16
        fieldValues.put(Form9061.CHECK_NO_16_2, Form.CHECKED);        
        boolean fill16 = false;
        if (eligibilityInfo.getTanfLast18Months()) {
            fieldValues.put(Form9061.CHECK_YES_16_1, Form.CHECKED);
            fill16 = true;
        } else {
            fieldValues.put(Form9061.CHECK_NO_16_1, Form.CHECKED);
            if (eligibilityInfo.getTanfStopLaw2Years()) {
                fieldValues.put(Form9061.CHECK_YES_16_3, Form.CHECKED);
                fill16 = true;
            } else {
                fieldValues.put(Form9061.CHECK_NO_16_3, Form.CHECKED);
                if (eligibilityInfo.getTanf9of18Months()) {
                    fieldValues.put(Form9061.CHECK_YES_16_4, Form.CHECKED);
                    fill16 = true;
                } else {
                    fieldValues.put(Form9061.CHECK_NO_16_4, Form.CHECKED);
                }
            }
        }



        if (fill16) {
            fieldValues.put(Form9061.PRIMARY_RECIPIENT_16, eligibilityInfo.getTanfPrimaryRecipient());
            fieldValues.put(Form9061.BENEFITS_CITY_STATE_16, eligibilityInfo.getTanfCityStateBenefitsReceived());
        }



        //section 17
        if (eligibilityInfo.getFelonLastYear()) {
            fieldValues.put(Form9061.CHECK_YES_17_1, Form.CHECKED);
            fieldValues.put(Form9061.CONVICTION_DATE, TaxCreditsTranslator.formatShortDate(eligibilityInfo.getFelonConvictionDate()));
            fieldValues.put(Form9061.RELEASE_DATE, TaxCreditsTranslator.formatShortDate(eligibilityInfo.getFelonReleaseDate()));
            if (eligibilityInfo.getFelonFederal()) {
                fieldValues.put(Form9061.FEDERAL_CONVICTION, Form.CHECKED);
            } else {
                fieldValues.put(Form9061.STATE_CONVICTION, Form.CHECKED);
            }
        } else {
            fieldValues.put(Form9061.CHECK_NO_17_1, Form.CHECKED);
        }


        //section 18
        if (eligibilityInfo.getDesignatedEZorRC()) {
            fieldValues.put(Form9061.CHECK_YES_18_1, Form.CHECKED);
        } else {
            fieldValues.put(Form9061.CHECK_NO_18_1, Form.CHECKED);            
        }

        if (isInRuralRenewalCounty(employeeInfo)) {
            fieldValues.put(Form9061.CHECK_YES_18_2, Form.CHECKED);
            fieldValues.put(Form9061.RURAL_RENEWAL_COUNTY, employeeInfo.getLiveAddress().getCounty());
        } else {
            fieldValues.put(Form9061.CHECK_NO_18_2, Form.CHECKED);
        }

        //section 19
        if (eligibilityInfo.getSsiWithin60Days()) {
            fieldValues.put(Form9061.CHECK_YES_19_1, Form.CHECKED);
        } else {
            fieldValues.put(Form9061.CHECK_NO_19_1, Form.CHECKED);
        }

        //section 20
        boolean fill20=false;
        if (eligibilityInfo.getVeteran() && eligibilityInfo.getVeteranServed180Days()) {
            fieldValues.put(Form9061.CHECK_YES_20_1, Form.CHECKED);
            fill20=true;
        } else {
            fieldValues.put(Form9061.CHECK_NO_20_1, Form.CHECKED);
        }
        if (eligibilityInfo.getVeteran() && eligibilityInfo.getVeteranDischargedServiceRelatedDisability()) {
            fieldValues.put(Form9061.CHECK_YES_20_2, Form.CHECKED);
            fill20=true;
        } else {
            fieldValues.put(Form9061.CHECK_NO_20_2, Form.CHECKED);
        }

        if (fill20) {
            if (eligibilityInfo.getVeteranDischargedPast5Years()) {
                fieldValues.put(Form9061.CHECK_YES_20_3, Form.CHECKED);
                if (eligibilityInfo.getVeteranUnemploymentCompensation4weeksOfLastYear()) {
                    fieldValues.put(Form9061.CHECK_YES_20_4, Form.CHECKED);
                } else {
                    fieldValues.put(Form9061.CHECK_NO_20_4, Form.CHECKED);
                }
            } else {
                fieldValues.put(Form9061.CHECK_NO_20_3, Form.CHECKED);
            }
        }

        //section 21
        if (hireDateAge >= 16 && hireDateAge <25) {
            fieldValues.put(Form9061.CHECK_YES_21_1, Form.CHECKED);
            if (eligibilityInfo.getDisconnectedSchoolLessThan10Hours()) {
                fieldValues.put(Form9061.CHECK_YES_21_2, Form.CHECKED);
                if (eligibilityInfo.getDisconnectedNotRegularlyEmployedLast6Months()) {
                    fieldValues.put(Form9061.CHECK_YES_21_3, Form.CHECKED);
                    if (! eligibilityInfo.getDisconnectedGraduateGED() || eligibilityInfo.getConditionalCertification() || eligibilityInfo.getDisconnectedAdmittedSinceCertificate()) {
                        fieldValues.put(Form9061.CHECK_YES_21_4, Form.CHECKED);
                    } else {
                        fieldValues.put(Form9061.CHECK_NO_21_4, Form.CHECKED);
                    }
                } else {
                    fieldValues.put(Form9061.CHECK_NO_21_3, Form.CHECKED);
                }
            } else {
                fieldValues.put(Form9061.CHECK_NO_21_2, Form.CHECKED);
            }
        } else {
            fieldValues.put(Form9061.CHECK_NO_21_1, Form.CHECKED);
        }


        Form9061 form9061 = new Form9061();
        return form9061.generateForm(fieldValues);


    }

    private byte[] create2848(EmployerInfo employerInfo) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(Form2848.TAX_PAYER_NAMES, employerInfo.getCompanyLegalName());
        fieldValues.put(Form2848.TAX_PAYER_ADDRESS_LINE_1, employerInfo.getLegalAddress().getAddress1());
        fieldValues.put(Form2848.TAX_PAYER_ADDRESS_LINE_2, employerInfo.getLegalAddress().getAddress2());
        fieldValues.put(Form2848.TAX_PAYER_CITY_STATE_ZIP, employerInfo.getLegalAddress().getCityStateZipAsOneLine());

        fieldValues.put(Form2848.PHONE_1, employerInfo.getTelephoneNumber().substring(0,3));
        String phoneLast7 = employerInfo.getTelephoneNumber().substring(3,6) + "-" + employerInfo.getTelephoneNumber().substring(6,10);
        if (employerInfo.getTelephoneExtension() != null && ! employerInfo.getTelephoneExtension().equals("")) {
            phoneLast7 += " x" + employerInfo.getTelephoneExtension();
        }
        fieldValues.put(Form2848.PHONE_2, phoneLast7);

        fieldValues.put(Form2848.EIN_1, employerInfo.getEin().substring(0,2));
        fieldValues.put(Form2848.EIN_2, employerInfo.getEin().substring(2,9));       

        Form2848 form2848 = new Form2848();
        return form2848.generateForm(fieldValues);
    }

    private byte[] createCTJS182(EmployerInfo employerInfo, EmployeeInfo employeeInfo) throws Exception {
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(CTJS182.EMPLOYEE_NAME, employeeInfo.getFullName());
        fieldValues.put(CTJS182.EMPLOYER_ADDRESS_LINE_1, employerInfo.getLegalAddress().getAddress1());
        fieldValues.put(CTJS182.EMPLOYER_ADDRESS_LINE_2, employerInfo.getLegalAddress().getAddress2());
        fieldValues.put(CTJS182.EMPLOYER_CITY_STATE_ZIP, employerInfo.getLegalAddress().getCityStateZipAsOneLine());
        fieldValues.put(CTJS182.EMPLOYER_NAME, employerInfo.getCompanyLegalName());
        fieldValues.put(CTJS182.SSN, TaxCreditsTranslator.formatSSN(employeeInfo.getSsn()));
        fieldValues.put(CTJS182.START_WORK_DATE, TaxCreditsTranslator.formatShortDate(employeeInfo.getStartDate()));

        CTJS182 ctjs182 = new CTJS182();
        return ctjs182.generateForm(fieldValues);
    }


   



    private boolean isInRuralRenewalCounty(EmployeeInfo employeeInfo) {
        String state = states.get(employeeInfo.getLiveAddress().getState().toUpperCase());
        String county = employeeInfo.getLiveAddress().getCounty().toUpperCase();
        Set<String> counties = ruralRenewalCounties.get(state);
        return counties != null && counties.contains(county);                    
    }


    public boolean isAddressInRCorEZ(String address, String zipCode) throws Exception {
        if (address.length() > 100 || zipCode.length() > 100) {
            throw new Exception("Invalid Input");
        }

        boolean hudDisabled;
        String geoUrl;
        String zoneUrl;

        try {
            PayrollServices.beginUnitOfWork();
            hudDisabled = SystemParameter.findBooleanValue(SystemParameter.Code.TAX_CREDITS_DISABLE_HUD);
            geoUrl = SystemParameter.findStringValue(SystemParameter.Code.TAX_CREDITS_HUD_GEO_URL);
            zoneUrl = SystemParameter.findStringValue(SystemParameter.Code.TAX_CREDITS_HUD_ZONE_URL);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if (hudDisabled || geoUrl == null || zoneUrl == null) {
            throw new AddressDoesNotExistException();
        }

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(geoUrl);
        httpRequest.getParameters().put("address", address);
        httpRequest.getParameters().put("zip", zipCode);
        String response = httpRequest.sendGetRequest();

        /* the response should contain this form
        <form id="form0" name="SendXY" method="post" action="mappage.asp">
			<INPUT type=hidden name=action size=30 value=address>
			<INPUT type=hidden name=x size=30 value="-119.768788">
			<INPUT type=hidden name=y size=30 value="39.612942">
			<INPUT type=hidden name=w size=30 value=0>
			<INPUT type=hidden name=h size=30 value=0>
			<INPUT type=hidden name=label size=35 value="6320 KLAMATH CT 89433-6636">
			<INPUT type=hidden name=extlabel value="State: NV - County: WASHOE COUNTY - Census Tract: 0027.01">
			<input type=hidden name=appid value=>
			<input type=hidden name=longtract value=320310027011024 ID="Hidden2">
			<input type=hidden name=sessionid value=>
		</form>
         */
        Source source = new Source(response);
        List<Element> elements = source.getAllElements("input");
        String x = null;
        String y = null;
        String longtract = null;
        for (Element element : elements) {
            String name = element.getAttributeValue("name");
            String value = element.getAttributeValue("value");
            if(name.equals("x")) {
                x = value;
            }
            else if(name.equals("y")) {
                y = value;
            }
            else if(name.equals("longtract")) {
                longtract = value;
            }
        }

        if(x == null || y == null || longtract == null) {
            throw new AddressDoesNotExistException();
        }

        httpRequest.setUrl(zoneUrl);
        httpRequest.getParameters().put("appID", "2");
        httpRequest.getParameters().put("action", "address");
        httpRequest.getParameters().put("x", x);
        httpRequest.getParameters().put("y", y);
        httpRequest.getParameters().put("longtract", longtract);
        response = httpRequest.sendGetRequest();

        if(response.indexOf("Empowerment Zone") > -1 || response.indexOf("Renewal Community") > -1) {
            return true;
        }
        // if "is in the" or ""is not in" is not in the response the page has been changed
        // and we need to make sure we are still getting a good response
        else if(response.indexOf("is in the") == -1 && response.indexOf("is not in") == -1) {
            throw new Exception("HUD response has changed: " + response);
        }

        return false;
    }

    private int getAgeAsOf(Date dob, Date asOfDate) {
        SpcfCalendar asOf = SpcfCalendar.createInstance(asOfDate.getTime());
        SpcfCalendar cob = SpcfCalendar.createInstance(dob.getTime());

        int age = asOf.getYear() - cob.getYear();

        cob.addYears(age);

        if (asOf.before(cob)) {
            age--;
        }

        return age;
    }

    public ArrayList<WOTCCategory> getCategories(EligibilityInfo eligibilityInfo, EmployeeInfo employeeInfo) throws Exception {
        validateEligibilityInfo(eligibilityInfo);
        validateEmployeeInfo(employeeInfo);
        
        ArrayList<WOTCCategory> categories = new ArrayList<WOTCCategory>();

        int hireDateAge = getAgeAsOf(employeeInfo.getDateOfBirth(), employeeInfo.getHireDate());


        if ((eligibilityInfo.getSnapLast6Months() || eligibilityInfo.getSnapLast3of5MonthsNoLongerEligible()) &&
                hireDateAge >= 18 && hireDateAge < 40) {
            categories.add(getCategory(WOTCCategoryName.SNAP));
        }

        if (eligibilityInfo.getTanfLast18Months() || eligibilityInfo.getTanfStopLaw2Years()) {
            categories.add(getCategory(WOTCCategoryName.TANFLongTermYear1));
            categories.add(getCategory(WOTCCategoryName.TANFLongTermYear2));
        }

        if (eligibilityInfo.getTanf9of18Months()) {
            categories.add(getCategory(WOTCCategoryName.TANFShortTerm));
        }

        if (eligibilityInfo.getReferralVocationalRehabilitationAgency()
            || eligibilityInfo.getReferralEmploymentNetwork()
            || eligibilityInfo.getReferralDepartmentVeteranAffairs()) {
            categories.add(getCategory(WOTCCategoryName.VocationalRehabilitationReferral));
        }

        if (eligibilityInfo.getVeteran()) {
            if (eligibilityInfo.getVeteranSnap3of15Months()) {
                categories.add(getCategory(WOTCCategoryName.FoodStampsVeteran));
            }
            if (eligibilityInfo.getVeteranDischargedPast5Years() && eligibilityInfo.getVeteranUnemploymentCompensation4weeksOfLastYear()) {
                categories.add(getCategory(WOTCCategoryName.UnemployedVeteran));
            }
            if (eligibilityInfo.getVeteranDisability()
                    && eligibilityInfo.getVeteranDischargedBeforeHired()
                    && (eligibilityInfo.getVeteranServed180Days() || eligibilityInfo.getVeteranDischargedServiceRelatedDisability())) {
                categories.add(getCategory(WOTCCategoryName.DischargedDisabledVeteran));
            }
            if (eligibilityInfo.getVeteranUnemployed6MonthsBeforeHired()
                    && (eligibilityInfo.getVeteranDisability() || eligibilityInfo.getVeteranDischargedServiceRelatedDisability())
                    && eligibilityInfo.getVeteranServed180Days()) {
                categories.add(getCategory(WOTCCategoryName.DisabledDischargedUnemployedVeteran));
            }
        }

        if (hireDateAge >= 18 && hireDateAge < 40
                && (eligibilityInfo.getDesignatedEZorRC() || isInRuralRenewalCounty(employeeInfo) )) {
            categories.add(getCategory(WOTCCategoryName.DesignatedCommunityResident));
        }

        
        if (hireDateAge >= 16 && hireDateAge < 25
                && eligibilityInfo.getDisconnectedSchoolLessThan10Hours()
                && eligibilityInfo.getDisconnectedNotRegularlyEmployedLast6Months()
                && (! eligibilityInfo.getDisconnectedGraduateGED() || ! eligibilityInfo.getDisconnectedAdmittedSinceCertificate())) {
            categories.add(getCategory(WOTCCategoryName.DisconnectedYouth));
        }

        if (eligibilityInfo.getFelonLastYear()) {
            categories.add(getCategory(WOTCCategoryName.ExFelon));
        }

        if (eligibilityInfo.getSsiWithin60Days()) {
            categories.add(getCategory(WOTCCategoryName.SSIRecipient));
        }

        if (hireDateAge >= 16 && hireDateAge < 18
                && eligibilityInfo.getSummerYouthEmployedBetween1Mayand15September()
                && (eligibilityInfo.getDesignatedEZorRC() || isInRuralRenewalCounty(employeeInfo))) {
            categories.add(getCategory(WOTCCategoryName.SummerYouth));
        }

        if (eligibilityInfo.getConditionalCertification()) {
            categories.add(getCategory(WOTCCategoryName.ConditionalCertification));
        }

        return categories;
    }

    private WOTCCategory getCategory(WOTCCategoryName categoryName) {
        WOTCCategory category = new WOTCCategory();

        //defaults
        category.setCategory(categoryName.toString());
        category.setTaxRate0(0);
        category.setTaxRate1(0.25);
        category.setTaxRate2(0.40);
        category.setWageBase(6000.);
        category.setMaxCredit(2400.);
        category.setDocumentationFileName(categoryName.toString());

        switch (categoryName) {
            case TANFLongTermYear1:
                category.setWageBase(10000);
                category.setMaxCredit(4000);
                category.setDocumentationFileName("TANF");
                break;
            case TANFLongTermYear2:
                category.setTaxRate0(0.50);
                category.setTaxRate1(0.50);
                category.setTaxRate2(0.50);
                category.setWageBase(10000);
                category.setMaxCredit(5000);
                category.setDocumentationFileName("TANF");
                break;
            case TANFShortTerm:
                category.setDocumentationFileName("TANF");
                break;
            case DischargedDisabledVeteran:
            case DisabledDischargedUnemployedVeteran:
                category.setWageBase(12000);
                category.setMaxCredit(4800);
                break;
            case SummerYouth:
                category.setWageBase(3000);
                category.setMaxCredit(1200);
        }


        return category;
    }

    public String getInstructionsHTML(String password, List<String> groups, String eeEmail, String erEmail, String submitDate, String eeName, String signerType) throws Exception {
        password = validateAndEscapeString(password);
        if (groups.size() > 20) {
            throw new Exception("Invalid input");
        }
        for (int i=0; i<groups.size(); i++) {
            groups.set(i, validateAndEscapeString(groups.get(i)));
        }
        eeEmail = validateAndEscapeString(eeEmail);
        erEmail = validateAndEscapeString(erEmail);
        submitDate = validateAndEscapeString(submitDate);
        eeName = validateAndEscapeString(eeName);
        signerType = validateAndEscapeString(signerType);

        Properties finalSteps = new Properties();
        finalSteps.load(new FileReader(new File(Application.findFileOnClassPath("resources/finalSteps.properties"))));

        StringBuffer html = new StringBuffer();

        html.append("<html><head><title>")
            .append("Application for Tax Credit Instructions</title>")
            .append("<link rel=\"stylesheet\" type=\"text/css\" href=\"assets/print.css\" />")
            .append("</head><body  onLoad='javascript:window.print();'>")
            .append("<h3>Application for Tax Credit Instructions</h3");

        html.append("<ul>");
        html.append("<li>").append(sub(finalSteps.getProperty("app_emailed"), signerType, erEmail, eeName, eeEmail));
        html.append("<li>").append(sub(finalSteps.getProperty("app_sign"), password));
        html.append("<li>").append(sub(finalSteps.getProperty("sign_required"), submitDate));
        
        List<List<String>> proofDocs = new ArrayList<List<String>>();
        for (String category : groups) {
            for (List<String> group : documentation.get(category)) {
                proofDocs.add(group);
            }
        }

        if (proofDocs.size() > 0 ) {
            html.append("<li>").append(sub(finalSteps.getProperty("proof_docs"), submitDate));
            html.append("<br>").append(sub(finalSteps.getProperty("proof_docs_submit"), "<B>" + numbers[proofDocs.size()].toUpperCase() + "</B>"));
            for (List<String> options : proofDocs) {
                html.append("<br><b>ONE</b> of the following:<ul>");
                for (String option : options) {
                    html.append("<li>").append(option);
                }
                html.append("</ul>");
            }
        }

        html.append("</ul></body></html>");

        return html.toString();
    }

    private String sub(String template, String... parameters) {
        StrSubstitutor sub = new StrSubstitutor(new FlexStyleStrLookup(parameters));
        return sub.replace(template.replaceAll("\\{\\d+\\}","\\$$0"));


    }

    public class FlexStyleStrLookup extends StrLookup {

        private String[] lookup;

        private int index=0;

        public FlexStyleStrLookup(String... lookup) {
            this.lookup = lookup;
        }

        @Override
        public String lookup(String key) {
            return lookup[index++];
        }
    }

    public void submitContactRequest(String name, String phone, String email) throws Exception {
        name = validateAndEscapeString(name);
        phone = validateAndEscapeString(phone);
        email = validateAndEscapeString(email);

        String body = String.format("Name: %s\nPhone: %s\nEmail: %s", name, phone, email);


        MailSender.sendEmail(sfConfig.getString("internalemailserver"),
                SystemParameter.findStringValue(SystemParameter.Code.TAX_CREDITS_CONTACT_EMAIL, "TaxCredits@intuit.com"),
                SystemParameter.findStringValue(SystemParameter.Code.TAX_CREDITS_CONTACT_EMAIL, "TaxCredits@intuit.com"),
                "Request for Contact",
                body,
                false,
                StringUtils.isEmpty(email) ? null : email,
                new ArrayList<String>());


    }

    //iText will probably not give an error or have any problems with too long, but double checking for safety
    //UI will not allow entering these long, so no need to be user friendly
    //also does not check against the limits on PDF (will truncate) but again, not really needed
    private void validateEmployeeInfo(EmployeeInfo employeeInfo) throws Exception {
        validateAddress(employeeInfo.getLiveAddress());
        validateString(employeeInfo.getFirstName());
        validateString(employeeInfo.getLastName());
        validateString(employeeInfo.getMiddleInitial());
        validateString(employeeInfo.getPosition());
        validateString(employeeInfo.getSsn());
        validateEINorSSN(employeeInfo.getSsn()); //this goes in db so must not be polluted
        validateString(employeeInfo.getStartingWage());
        validateString(employeeInfo.getTelephoneExtension());
        validateString(employeeInfo.getTelephoneNumber());
        validateString(employeeInfo.getEmail());
        validateString(employeeInfo.getWorkState());
    }

    private void validateEmployerInfo(EmployerInfo employerInfo) throws Exception {
        validateAddress(employerInfo.getLegalAddress());
        validateString(employerInfo.getCompanyLegalName());
        validateString(employerInfo.getContactEmail());
        validateString(employerInfo.getContactName());
        validateString(employerInfo.getEin());
        validateEINorSSN(employerInfo.getEin()); //this goes in db so must not be polluted
        validateString(employerInfo.getTelephoneExtension());
        validateString(employerInfo.getTelephoneNumber());
        validateString(employerInfo.getCompanyType());
        validateString(employerInfo.getAuthSignerEmail());
        validateString(employerInfo.getFiscalYearStartDateString());
    }

    private void validateEligibilityInfo(EligibilityInfo eligibilityInfo) throws Exception {
        validateString(eligibilityInfo.getSnapCityStateBenefitsReceived());
        validateString(eligibilityInfo.getSnapPrimaryRecipient());
        validateString(eligibilityInfo.getTanfCityStateBenefitsReceived());
        validateString(eligibilityInfo.getTanfPrimaryRecipient());
        validateString(eligibilityInfo.getVeteranSnapCityStateBenefitsReceived());
        validateString(eligibilityInfo.getVeteranSnapPrimaryRecipient());   
    }
    
    private void validateAddress(Address address) throws Exception {
        validateString(address.getAddress1());
        validateString(address.getAddress2());
        validateString(address.getCity());
        validateString(address.getCounty());
        validateString(address.getState());
        validateString(address.getZip());
    }

    private void validateString(String string) throws Exception {
        if (string != null && string.length() > 100) {
            throw new Exception("Invalid input");
        }
    }


    //escape HTML so cannot inject into emails
    private String validateAndEscapeString(String string) throws Exception {
        validateString(string);
        return StringEscapeUtils.escapeHtml(string);
    }

    private void validateEINorSSN(String string) throws Exception {
        if (string == null || !string.matches("\\d{9}")) {
            throw new Exception("Invalid input");
        }
    }




}

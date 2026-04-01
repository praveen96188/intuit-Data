package com.intuit.sbd.payroll.psp.adapters.ade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupGetServiceParams;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.schema.payroll.v3.company.TaxDepositFrequency;
import com.intuit.schema.payroll.v3.company.TaxPaymentGroup;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/*
 * User: shivanandad069
 * Date: 10/20/13
 * Time: 11:13 PM
 * To change this template use File | Settings | File Templates.
 */

public class TaxPaymentGroupGetServiceTests {
    final static String COMPANY_ID = "19670404";
    private static final String TEST_FILE_PATH = "Adapters/ade-adapter-tests/src/test/resources/expected/";

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 10, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCompanyTaxPaymentGroupByIdInvalidCompanyId() {
        String psid = "1021234";
        createCACompanyWithDF();

        ServiceResult<TaxPaymentGroup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS)
                              .service(createTaxPaymentGroupGetServiceParams(psid, "US_CA_UIETT_PAYMENT"));

        assertFalse(serviceResult.isSuccess());

        assertEquals("1007", serviceResult.getMessages().get(0).getMessageCode());
        assertEquals("Company with id '1021234' does not exist", serviceResult.getMessages().get(0).getMessage());
        assertNull(serviceResult.getResult());
    }

    @Test
    public void testCompanyTaxPaymentGroupByIdNoPayemntTemplateAgencyAdded() {
        String psid = COMPANY_ID;
        createCACompanyWithDF();

        ServiceResult<TaxPaymentGroup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS)
                              .service(createTaxPaymentGroupGetServiceParams(psid, "US_CO_UITR1_PAYMENT"));

        assertFalse(serviceResult.isSuccess());
        assertEquals("1007", serviceResult.getMessages().get(0).getMessageCode());
        assertEquals("TaxPaymentGroup with id 'US_CO_UITR1_PAYMENT' does not exist", serviceResult.getMessages().get(0).getMessage());
        assertNull(serviceResult.getResult());
    }

    @Test
    public void testCompanyTaxPaymentGroupByIdInvalidGroupId() {
        String psid = COMPANY_ID;
        createCACompanyWithDF();

        ServiceResult<TaxPaymentGroup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS)
                              .service(createTaxPaymentGroupGetServiceParams(psid, "CA_UIETT_PAYMENT"));

        assertFalse(serviceResult.isSuccess());
        assertEquals(serviceResult.toString(), "1007", serviceResult.getMessages().get(0).getMessageCode());
        assertEquals("TaxPaymentGroup with id 'CA_UIETT_PAYMENT' does not exist", serviceResult.getMessages().get(0).getMessage());
        assertNull(serviceResult.getResult());
    }

    @Test
    public void testCompanyTaxPaymentGroupById() {
        String psid = COMPANY_ID;
        createCACompanyWithDF();

        ServiceResult<TaxPaymentGroup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS)
                              .service(createTaxPaymentGroupGetServiceParams(psid, "US_CA_UIETT_PAYMENT"));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        TaxPaymentGroup jsonInput = getExpectedTaxPaymentGroup("DF_TaxPaymentGroupByID.json");
        assertTaxpaymentGroup(jsonInput, serviceResult.getResult());
    }

    @Test
    public void testCompanyTaxPaymentGroupByIdWithHistory() {
        String psid = COMPANY_ID;
        createCACompanyWithDF();

        ServiceResult<TaxPaymentGroup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS)
                              .service(createTaxPaymentGroupGetServiceParams(psid, "US_CA_UIETT_PAYMENT", true));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        TaxPaymentGroup jsonInput = getExpectedTaxPaymentGroup("DF_TaxPaymentGroupByIDWithHistory.json");
        assertTaxpaymentGroup(jsonInput, serviceResult.getResult());
    }

    @Test
    public void testCompanyTaxPaymentGroupByIdCAPITWithHistory() {
        String psid = COMPANY_ID;
        createCACompanyWithDF();

        ServiceResult<TaxPaymentGroup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS)
                              .service(createTaxPaymentGroupGetServiceParams(psid, "US_CA_PITSDI_PAYMENT", true));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        TaxPaymentGroup jsonInput = getExpectedTaxPaymentGroup("DF_TaxPaymentGroupByIDCAPITWithHistory.json");
        assertTaxpaymentGroup(jsonInput, serviceResult.getResult());
    }

    public static TaxPaymentGroup getExpectedTaxPaymentGroup(String expectedJsonFileName) {
        TaxPaymentGroup jsonInput = null;
        if (expectedJsonFileName == null || expectedJsonFileName.isEmpty()) {
            return null;
        }

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
            FileInputStream inputStream = new FileInputStream(TEST_FILE_PATH + expectedJsonFileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            jsonInput = gson.fromJson(inputReader, TaxPaymentGroup.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonInput;
    }

    void assertTaxpaymentGroup(TaxPaymentGroup expected, TaxPaymentGroup response) {
        assertEquals(expected.getAgencyId(), response.getAgencyId());
        assertEquals(expected.getDepositFrequencies().size(), response.getDepositFrequencies().size());
        assertEquals(expected.getId(), response.getId());
        assertEquals(expected.getJurisdictionId(), response.getJurisdictionId());
        assertEquals(expected.getName(), response.getName());
        assertEquals(expected.getAgencyId(), response.getAgencyId());

        assertDepositFrequencyList(expected, response);
    }

    void assertDepositFrequencyList(TaxPaymentGroup expected, TaxPaymentGroup response) {
        List<TaxDepositFrequency> expectedDFList = expected.getDepositFrequencies();
        for (TaxDepositFrequency taxDepositFrequency : response.getDepositFrequencies()) {
            boolean expectedDFfound = false;
            SpcfCalendar endDate = null;
            if(taxDepositFrequency.getEndDate() != null) {
                endDate = CalendarUtils.convertToSpcfCalendar(taxDepositFrequency.getEndDate());
                CalendarUtils.clearTime(endDate);
            }
            for (TaxDepositFrequency expectedTaxDepositFrequency : expectedDFList) {
                // DF Invalid date might have time component, so clearing time before comparing the dates
                SpcfCalendar expectedEndDate = null;
                if(expectedTaxDepositFrequency.getEndDate() != null) {
                    expectedEndDate = CalendarUtils.convertToSpcfCalendar(expectedTaxDepositFrequency.getEndDate());
                    CalendarUtils.clearTime(expectedEndDate);
                }

                if (expectedTaxDepositFrequency.getFrequency() == taxDepositFrequency.getFrequency() && expectedTaxDepositFrequency.getStartDate().equals(taxDepositFrequency.getStartDate()) && (expectedEndDate == null || expectedEndDate.compareTo(endDate) == 0)) {
                    expectedDFfound = true;
                    break;
                }
            }
            assertTrue("EDF for Payment Template Cd: "+ expected.getAgencyId() + ", Frequency: " + taxDepositFrequency.getFrequency() + " and start date: "
                               + taxDepositFrequency.getStartDate().toString() +" is found, which is not in expected list.", expectedDFfound);
        }
    }

    public void createCACompanyWithDF() {
        DataLoadServices.setupCACompany(COMPANY_ID);

        //Add user.
        PayrollServices.beginUnitOfWork();
        AuthUser user = DataLoader.addUser("UnitTestAgent1", "First1", "Last1");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        user = AuthUser.findUser(user.getCorpId());
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
        PayrollServices.commitUnitOfWork();

        //Get the latest deposit frequency for the payment template
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());
        EffectiveDepositFrequency existingLatestDepositFrequency =
                EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate,
                                                                              dto.getEffectiveDate());
        PayrollServices.commitUnitOfWork();

        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.SEMIWEEKLY);

        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), -1);
        SpcfCalendar newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, -1);
        dto.setEffectiveDate(newEffectiveDate.toLocal());
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, COMPANY_ID, dto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Effective Deposit Frequency", processResult);

        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.MONTHLY);

        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), 30);
        newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, 30);
        dto.setEffectiveDate(newEffectiveDate.toLocal());
        assertSuccess(PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, COMPANY_ID, dto));
        PayrollServices.commitUnitOfWork();

        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.MONTHLY);
        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), 30);
        newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, 30);
        dto.setEffectiveDate(newEffectiveDate.toLocal());
        dto.setPaymentTemplateCd("CA-UIETT-PAYMENT");
        assertSuccess(PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, COMPANY_ID, dto));
        PayrollServices.commitUnitOfWork();
    }

    private EffectiveDepositFrequencyDTO getDto() {
        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        dto.setAgencyId("CAEDD");
        SpcfCalendar newEffectiveDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(newEffectiveDate, 5);

        dto.setEffectiveDate(newEffectiveDate);
        dto.setPaymentTemplateCd("CA-PITSDI-PAYMENT");
        dto.setPaymentFrequencyId(DepositFrequencyCode.QUARTERLY);

        return dto;
    }

    private TaxPaymentGroupGetServiceParams createTaxPaymentGroupGetServiceParams(String psid, String taxpaymentGroupId) {
        return createTaxPaymentGroupGetServiceParams(psid, taxpaymentGroupId, false);
    }

    private TaxPaymentGroupGetServiceParams createTaxPaymentGroupGetServiceParams(String psid, String taxpaymentGroupId, boolean showHistory) {
        TaxPaymentGroupGetServiceParams taxPaymentGroupGetServiceParams = new TaxPaymentGroupGetServiceParams();
        taxPaymentGroupGetServiceParams.setCompanyId(psid);
        taxPaymentGroupGetServiceParams.setTaxPaymentGroupId(taxpaymentGroupId);
        if(showHistory) {
            taxPaymentGroupGetServiceParams.setShowAll("TAXDEPOSITFREQUENCY");
        }
        return taxPaymentGroupGetServiceParams;
    }
}
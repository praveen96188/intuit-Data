package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: May 21, 2009
 * Time: 9:21:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateDepositFrequencyCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }
    
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2009, 4, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(
                null, "123272727", null);

        PayrollServices.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {

        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(
                SourceSystemCode.QBDT, null, null);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testInvalidCompany() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(
                SourceSystemCode.QBDT, "1232727", null);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBDT:1232727 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullEffectiveDepositFrequencyDTO() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", null);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Effective Deposit Frequency DTO",
                errorMessage.getMessage());
    }

    @Test
    public void testNullDTOValues() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 4, processResult.getMessages().size());

        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Agency Id",
                errorMessage.getMessage());

        errorMessage = processResult.getMessages().get(1);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Payment Template Code",
                errorMessage.getMessage());

        errorMessage = processResult.getMessages().get(2);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: New Effective Date",
                errorMessage.getMessage());

        errorMessage = processResult.getMessages().get(3);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Payment Frequency Id",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidAgencyId() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        dto.setAgencyId("INVALID_AGENCY");

        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "10029", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBDT:123456789 is not associated with Agency INVALID_AGENCY.",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidPaymentTemplateCode() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        dto.setPaymentTemplateCd("INVALID_PAYMENT");
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "12", errorMessage.getMessageCode());
        assertEquals("Error message", "Entity PaymentTemplate with id INVALID_PAYMENT does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testPaymentTemplateNotAssignedToCompany() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        dto.setPaymentTemplateCd("ID-020-PAYMENT");
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "10041", errorMessage.getMessageCode());
        assertEquals("Error message", "Payment Template 'ID-020-PAYMENT' is not assigned to the company QBDT:123456789 Agency IRS.",
                errorMessage.getMessage());
    }

    @Test
    public void testPaymentFrequencyNotSupportedForPaymentTemplate() {
        DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        dto.setPaymentFrequencyId(DepositFrequencyCode.ACCELERATED);
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "10042", errorMessage.getMessageCode());
        assertEquals("Error message", "Payment Frequency 'ACCELERATED' is not supported for the Payment Template 'IRS-941-PAYMENT'.",
                errorMessage.getMessage());
    }

    @Test
    public void testHappyPath_CreateNewEffectiveDepositFrequency() {
        DataLoadServices.setupCompany("123456789");

        //Add user.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101216000000");
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
        Company company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());
        EffectiveDepositFrequency existingLatestDepositFrequency =
                EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate,
                        dto.getEffectiveDate());

        PayrollServices.commitUnitOfWork();

        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.ANNUAL);

        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), -1);
        SpcfCalendar newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, -1);
        dto.setEffectiveDate(newEffectiveDate);
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Effective Deposit Frequency", processResult);

        //Persistency Check - Make sure new EffectiveDepositFrequency created with new effective date
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());

        DomainEntitySet<EffectiveDepositFrequency> frequencies =
                EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, paymentTemplate,
                        null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Effective Deposit Frequencies ", 2, frequencies.size());

        //Make sure DepositFrequencyChanged event created
        PayrollServices.beginUnitOfWork();
        existingLatestDepositFrequency = PayrollServices.entityFinder.findById(EffectiveDepositFrequency.class,
                existingLatestDepositFrequency.getId());

        company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DepositFrequencyChanged, null, SpcfCalendar.createInstance(2010, 12, 16), SpcfCalendar.createInstance(2010, 12, 17));

        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent event = companyEventsList.get(0);

        assertEquals("Agent Id ", user.getCorpId(),
                event.getCompanyEventDetailValue(EventDetailTypeCode.UserId));

        assertEquals("Effective Date ", dto.getEffectiveDate().toString(),
                event.getCompanyEventDetailValue(EventDetailTypeCode.NewEffectiveDate));

        assertEquals("Deposit Frequency ", dto.getPaymentFrequencyId().toString(),
                event.getCompanyEventDetailValue(EventDetailTypeCode.NewDepositFrequency));

        assertEquals("Payment Template ", dto.getPaymentTemplateCd(),
                event.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHappyPath_UpdateDepositFrequency() {
        DataLoadServices.setupCompany("123456789");

        //Add user.
        PayrollServices.beginUnitOfWork();
        AuthUser user = DataLoader.addUser("UnitTestAgent1", "First1", "Last1");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        user = AuthUser.findUser(user.getCorpId());
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        Company company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());

        Criterion<EffectiveDepositFrequency> edfCriteria = EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(company)
                .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Agency().equalTo(paymentTemplate.getAgency()))
                .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().PaymentTemplate().equalTo(paymentTemplate));
        Expression<EffectiveDepositFrequency> edfQuery =
                new Query<EffectiveDepositFrequency>()
                        .Where(edfCriteria)
                        .OrderBy(EffectiveDepositFrequency.EffectiveDate().Descending());

        DomainEntitySet<EffectiveDepositFrequency> frequencies = Application.find(EffectiveDepositFrequency.class, edfQuery);
        dto.setEffectiveDate(frequencies.get(0).getEffectiveDate());
        dto.setPaymentFrequencyId(DepositFrequencyCode.ANNUAL);
        SpcfCalendar newEffectiveDate = dto.getEffectiveDate().copy();
       // newEffectiveDate.addDays(10);
        dto.setEffectiveDate(newEffectiveDate);
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Update Effective Deposit Frequency", processResult);

        //Persistency Check - Make sure new EffectiveDepositFrequency updated with new effective date
        PayrollServices.beginUnitOfWork();
        paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());

        frequencies =
                EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, paymentTemplate,
                        dto.getEffectiveDate());
        PayrollServices.commitUnitOfWork();

        assertEquals("Effective Deposit Frequencies ", 1, frequencies.size());
    }



    @Test
    public void testCreateDepositFrequency_RecalculateMMT() {
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123456789", payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Add user.
        PayrollServices.beginUnitOfWork();
        AuthUser user = DataLoader.addUser("UnitTestAgent1", "First1", "Last1");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        user = AuthUser.findUser(user.getCorpId());
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();

        Company company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());

        Criterion<EffectiveDepositFrequency> edfCriteria = EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(company)
                .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Agency().equalTo(paymentTemplate.getAgency()))
                .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().PaymentTemplate().equalTo(paymentTemplate));
        Expression<EffectiveDepositFrequency> edfQuery =
                new Query<EffectiveDepositFrequency>()
                        .Where(edfCriteria)
                        .OrderBy(EffectiveDepositFrequency.EffectiveDate().Descending());

        DomainEntitySet<EffectiveDepositFrequency> frequencies = Application.find(EffectiveDepositFrequency.class, edfQuery);
        SpcfCalendar oldEffectiveDate = frequencies.get(0).getEffectiveDate();
        dto.setEffectiveDate(oldEffectiveDate);
        SpcfCalendar newEffectiveDate = oldEffectiveDate.copy();
        newEffectiveDate.addDays(1);
        dto.setEffectiveDate(newEffectiveDate);
        dto.setPaymentFrequencyId(DepositFrequencyCode.ANNUAL);
        processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, "123456789", dto);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update Effective Deposit Frequency", processResult);

        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate)
                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created))
                .And(MoneyMovementTransaction.Company().equalTo(company));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, mmtCriteria);
        DepositFrequencyCode newFrequency = moneyMovementTransactions.get(0).getPaymentFrequency().getPaymentFrequencyId();
        PayrollServices.commitUnitOfWork();

      
        PayrollServices.beginUnitOfWork();
        assertEquals("MMT ", 1, moneyMovementTransactions.size());
        assertEquals("New Frequency for MMT","ANNUAL" , newFrequency.toString());


        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    //this test verifies PSRV002936: Payments are not combined for the same agency and due dates
    public void testPaymentsFromDifferentPayrollsCombineOnDepositFrequencyChange() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2011,12,2);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.invalidateDepositFrequencies(company, "IRS-941-PAYMENT");
        DataLoadServices.invalidateDepositFrequencies(company, "CA-PITSDI-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2010, 12, 31));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2010, 12, 31));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-13"));
        assertSinglePayment(company, "CA-PITSDI-PAYMENT");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-13"));
        assertSinglePayment(company, "CA-PITSDI-PAYMENT");

        DataLoadServices.invalidateDepositFrequencies(company, "CA-PITSDI-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 9, 30));
        assertSinglePayment(company, "CA-PITSDI-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1));
        assertSinglePayment(company, "CA-PITSDI-PAYMENT");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-13"));
        assertSinglePayment(company, "CA-PITSDI-PAYMENT");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-13"));
        assertSinglePayment(company, "CA-PITSDI-PAYMENT");
    }

    private void assertSinglePayment(Company company, String paymentTemplateCd) {
        PayrollServices.beginUnitOfWork();
        Assert.assertEquals(1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd(paymentTemplateCd).find().size());
        PayrollServices.rollbackUnitOfWork();
    }

    private EffectiveDepositFrequencyDTO getDto() {
        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        dto.setAgencyId("IRS");
        SpcfCalendar newEffectiveDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(newEffectiveDate, 5);

        dto.setEffectiveDate(newEffectiveDate);
        dto.setPaymentTemplateCd("IRS-941-PAYMENT");
        dto.setPaymentFrequencyId(DepositFrequencyCode.QUARTERLY);

        return dto;
    }
}

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.QueryPayrollStatusProcess;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.test.BillPaymentWebServicesTests;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.hibernate.FlushMode;
import org.junit.*;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Jeff Jones
 */
public class QueryPayrollStatusProcessTests {

    private GetPayrollInfoWSDTO mRequest;
    private PayrollInfoWSDTO mResponse;
    private String psid = null;
    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
    private Boolean assistedTest;

    @BeforeClass
    public static void beforeClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar newOfferEndDate = SpcfCalendar.createInstance();
        newOfferEndDate.addDays(30);
        offer.setEndDate(newOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @AfterClass
    public static void afterClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar oldOfferEndDate = SpcfCalendar.createInstance(2013, 7, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        offer.setEndDate(oldOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @Before
    public void startUp() {
        assistedTest = false;
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1));
    }

    @After
    public void after() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void userIdNotFound() {
        mRequest = TestDataFactory.createGetPayrollInfoWSDTO("100000000", UserRoleEnum.Payroll);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();
    }

    @Test
    public void QueryPayrollStatus_DD() throws Exception {
        mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
        psid = mCreateAccountProcessTests.getPSID();

        PayrollServices.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        mRequest = TestDataFactory.createGetPayrollInfoWSDTO(company.getSourceCompanyId(), UserRoleEnum.Payroll);
        PayrollServices.commitUnitOfWork();

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        assertTrue(payrollStatusWSDTO.getTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getModTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getFailedTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getDDRejectionsWSDTO().isEmpty());
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                TransmissionType.CreateAccount), "v1_10/test_QueryPayRoll_DD.xml",
                Arrays.asList("EIN", "RandomDebitDateTime", "DateTimeStamp", "PSID", "ServiceKey", "SubscriptionNumber"));
        }

    @Test
    public void QueryPayrollStatus_Assisted() throws Throwable {
        assistedTest = true;
        mCreateAccountProcessTests.createAccountCloudAndAssisted();
        psid = mCreateAccountProcessTests.getPSID();

        PayrollServices.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        mRequest = TestDataFactory.createGetPayrollInfoWSDTO(company.getSourceCompanyId(), UserRoleEnum.Payroll);
        PayrollServices.commitUnitOfWork();

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        assertTrue(payrollStatusWSDTO.getTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getModTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getFailedTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getDDRejectionsWSDTO().isEmpty());
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                TransmissionType.CreateAccount), "v1_10/test_QueryPayRoll_Assisted.xml",
                Arrays.asList("EIN", "RandomDebitDateTime", "DateTimeStamp", "PSID", "ServiceKey", "SubscriptionNumber"));
    }

    @Test
    public void queryPayrollStatusWithR01Return() throws Throwable {
        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        Company company = PspFactory.findCompany("8574536");
        company.setSourceCompanyId("857453611");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("857453611", UserRoleEnum.All);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        PayrollServices.beginUnitOfWork();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        Assert.assertEquals(AccountStatusEnum.Hold, payrollStatusWSDTO.getAccount().getStatus());

        List<TransmissionWSDTO> transmissions = payrollStatusWSDTO.getTransmissionsWSDTO();
        Assert.assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());

        for (TransmissionWSDTO transmission : transmissions) {
            Assert.assertEquals("10/10/2007", transmission.getCheckDate());
            Assert.assertEquals("10/09/2007", transmission.getOffloadDate());
            Assert.assertEquals("0.00", transmission.getTotalTaxes().toString());
            Assert.assertEquals("777.77", transmission.getTotalDD().toString());
            Assert.assertEquals("103.66", transmission.getTotalFees().toString());
            Assert.assertEquals("881.43", transmission.getTotal().toString());

            List<TransmissionFeeWSDTO> fees = transmission.getFees();
            Assert.assertEquals(3, fees.size());
            for (TransmissionFeeWSDTO fee : fees) {
                if ("Fee for 2 direct deposit(s) at 1.75 each".equals(fee.getName())) {
                    Assert.assertEquals("3.50", fee.getAmt().toString());
                } else if ("NSF Fee".equals(fee.getName())) {
                    Assert.assertEquals("100.00", fee.getAmt().toString());
                } else if ("Sales tax".equals(fee.getName())) {
                    Assert.assertEquals("0.16", fee.getAmt().toString());
                } else {
                    fail("Wrong Fee Name: " + fee.getName());
                }
            }
        }

        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }


    @Test
    public void queryPayrollStatusWithRiskAssesment() throws Throwable {
        assistedTest = true;
        mCreateAccountProcessTests.createAccountCloudAndAssisted();
        psid = mCreateAccountProcessTests.getPSID();

        PayrollServices.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        company.addOnHoldReason(ServiceSubStatusCode.RiskAssessment);
        mRequest = TestDataFactory.createGetPayrollInfoWSDTO(company.getSourceCompanyId(), UserRoleEnum.Payroll);
        PayrollServices.commitUnitOfWork();

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        PayrollServices.beginUnitOfWork();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        Assert.assertEquals(AccountStatusEnum.Hold, payrollStatusWSDTO.getAccount().getStatus());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void queryPayrollStatusWithPayrolls7DaysOld() throws Exception {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070822000000");

            CompanyQB1DataLoader c4dl = new CompanyQB1DataLoader();
            c4dl.persistQBCompany1();

            String transmissionId = SpcfUniqueId.createInstance(true).toString();
            SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
            sourceSystemTransmissionDTO.setRequestDocument("Test");
            sourceSystemTransmissionDTO.setResponseDocument("Test");
            sourceSystemTransmissionDTO.setRequestToken(0l);
            sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.PayrollSubmission);
            PayrollServices.commitUnitOfWork();

            ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT, "8574536", transmissionId, sourceSystemTransmissionDTO);
            assertTrue(processResult.isSuccess());
            Assert.assertEquals(processResult.getMessages().size(), 0);

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO = c4dl.get2ndCompany2PR_DoesNotExceedLimits();
            payrollRunDTO.setPayrollTXBatchId("9");
            payrollRunDTO.setTransmissionId(transmissionId);
            PayrollRun payrollRun = c4dl.persistPayrollRun(payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBDT, "8574536", transmissionId, sourceSystemTransmissionDTO);
            assertTrue(processResult.isSuccess());
            Assert.assertEquals(processResult.getMessages().size(), 0);

            PayrollServices.beginUnitOfWork();
            transmissionId = SpcfUniqueId.createInstance(true).toString();
            sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
            sourceSystemTransmissionDTO.setRequestDocument("Test");
            sourceSystemTransmissionDTO.setResponseDocument("Test");
            sourceSystemTransmissionDTO.setRequestToken(0l);
            sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.PayrollSubmission);
            PayrollServices.commitUnitOfWork();

            processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT, "8574536", transmissionId, sourceSystemTransmissionDTO);
            assertTrue(processResult.isSuccess());
            Assert.assertEquals(processResult.getMessages().size(), 0);

            PayrollServices.beginUnitOfWork();
            payrollRunDTO = c4dl.get2ndCompany2PR_DoesNotExceedLimits();
            payrollRunDTO.setPayrollTXBatchId("10");
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
            PaycheckDTO paycheckDTO = paychecks.iterator().next();
            Collection<DDTransactionDTO> ddTransactions = paycheckDTO.getDdTransactions();
            DDTransactionDTO ddTransactionDTO = ddTransactions.iterator().next();
            ddTransactionDTO.setDDTransactionAmount(BigDecimal.valueOf(999.99));
            payrollRunDTO.setTransmissionId(transmissionId);
            payrollRun = c4dl.persistPayrollRun(payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBDT, "8574536", transmissionId, sourceSystemTransmissionDTO);
            assertTrue(processResult.isSuccess());
            Assert.assertEquals(processResult.getMessages().size(), 0);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20090714000000");
            Company company = PspFactory.findCompany("8574536");
            company.setSourceCompanyId("857453611");
            Application.save(company);
            PayrollServices.commitUnitOfWork();

            mRequest = createGetPayrollInfoWSDTO("857453611", null);

            QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);
            assertNotNull(mResponse.getPayrollStatusWSDTO());

            PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
            assertNotNull(payrollStatusWSDTO.getUserID());

            assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
            assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
            assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

            assertNotNull(payrollStatusWSDTO.getAccount());
            assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

            Assert.assertEquals(payrollStatusWSDTO.getTransmissionsWSDTO().size(), 1);
            TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
            assertEquals("09/01/2007", transmissionWSDTO.getDate());
            assertEquals("12:00 AM", transmissionWSDTO.getTime());
            assertEquals("Pending", transmissionWSDTO.getOffloadDate());
            assertEquals("10/10/2007", transmissionWSDTO.getCheckDate());
            assertEquals("Regular", transmissionWSDTO.getPayrollType());
            assertEquals(new BigDecimal("0.00"), transmissionWSDTO.getTotalTaxes());
            assertEquals(new BigDecimal("1333.32"), transmissionWSDTO.getTotalDD());
            assertEquals(new BigDecimal("3.58"), transmissionWSDTO.getTotalFees());
            assertEquals(new BigDecimal("1336.90"), transmissionWSDTO.getTotal());
            assertEquals(2, transmissionWSDTO.getFees().size());

            assertEquals("Fee for 2 direct deposit(s) at 1.75 each", transmissionWSDTO.getFees().get(0).getName());
            assertEquals(new BigDecimal("3.50"), transmissionWSDTO.getFees().get(0).getAmt());
            assertEquals("Sales tax", transmissionWSDTO.getFees().get(1).getName());
            assertEquals(new BigDecimal("0.08"), transmissionWSDTO.getFees().get(1).getAmt());

            assertTrue(payrollStatusWSDTO.getModTransmissionsWSDTO().isEmpty());
            assertTrue(payrollStatusWSDTO.getFailedTransmissionsWSDTO().isEmpty());
            assertTrue(payrollStatusWSDTO.getDDRejectionsWSDTO().isEmpty());
    }

    @Test
    public void queryPayrollDD4VVendorRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testSubmit_HappyPath();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(2, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/20/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("300.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("301.83", transmissionWSDTO.getTotal().toString());

        transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(1);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/22/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("500.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("501.83", transmissionWSDTO.getTotal().toString());

        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }

    @Test
    public void queryFailedTransmissionsVendorRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testSubmit_DuplicatePayment();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(2, payrollStatusWSDTO.getTransmissionsWSDTO().size());

        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/20/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("300.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("301.83", transmissionWSDTO.getTotal().toString());

        transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(1);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/22/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("500.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("501.83", transmissionWSDTO.getTotal().toString());

        Assert.assertEquals(3, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());

        FailedTransmissionWSDTO failedTransmission = payrollStatusWSDTO.getFailedTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", failedTransmission.getDate());
        Assert.assertEquals("12:00 AM", failedTransmission.getTime());
        Assert.assertEquals("Payment Id123 was previously submitted and recorded with different information.", failedTransmission.getDescription());

        failedTransmission = payrollStatusWSDTO.getFailedTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", failedTransmission.getDate());
        Assert.assertEquals("12:00 AM", failedTransmission.getTime());
        Assert.assertEquals("Payment Id123 was previously submitted and recorded with different information.", failedTransmission.getDescription());

        failedTransmission = payrollStatusWSDTO.getFailedTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", failedTransmission.getDate());
        Assert.assertEquals("12:00 AM", failedTransmission.getTime());
        Assert.assertEquals("Payment Id123 was previously submitted and recorded with different information.", failedTransmission.getDescription());

        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
    }

    @Test
    public void queryVoidPayrollDD4VVendorRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testBillPaymentVoidOneTxn();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());

        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/20/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("500.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("501.83", transmissionWSDTO.getTotal().toString());

        Assert.assertEquals(1, payrollStatusWSDTO.getModTransmissionsWSDTO().size());

        transmissionWSDTO = payrollStatusWSDTO.getModTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Modified", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/20/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("300.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("301.83", transmissionWSDTO.getTotal().toString());

        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }

    @Test
    public void queryReturnDD4VVendorRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testSubmit_HappyPath();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070918000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        processReturn("QBDT", "123272727", TransactionTypeCode.EmployeeDdCredit.toString(), TransactionStateCode.Executed.toString(), "R02", "Account Closed");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(2, payrollStatusWSDTO.getTransmissionsWSDTO().size());

        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("09/19/2007", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/20/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("300.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("301.83", transmissionWSDTO.getTotal().toString());

        transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(1);
        Assert.assertEquals("09/10/2007", transmissionWSDTO.getDate());
        Assert.assertEquals("12:00 AM", transmissionWSDTO.getTime());
        Assert.assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        Assert.assertEquals("09/22/2007", transmissionWSDTO.getCheckDate());
        Assert.assertEquals("BillPayment", transmissionWSDTO.getPayrollType());
        Assert.assertEquals("0.00", transmissionWSDTO.getTotalTaxes().toString());
        Assert.assertEquals("500.00", transmissionWSDTO.getTotalDD().toString());
        Assert.assertEquals("1.83", transmissionWSDTO.getTotalFees().toString());
        Assert.assertEquals("501.83", transmissionWSDTO.getTotal().toString());

        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(2, payrollStatusWSDTO.getDDRejectionsWSDTO().size());

        for (DDRejectionWSDTO rejectionWSDTO : payrollStatusWSDTO.getDDRejectionsWSDTO()) {
            Assert.assertEquals("09/20/2007", rejectionWSDTO.getCheckDate());
            Assert.assertEquals("Payee Name", rejectionWSDTO.getFullName());
            Assert.assertNull(rejectionWSDTO.getFirstName());
            Assert.assertNull(rejectionWSDTO.getLastName());
            Assert.assertNull(rejectionWSDTO.getMiddleName());
            Assert.assertEquals("1234567", rejectionWSDTO.getBankAccountNumber());
            Assert.assertEquals("111111118", rejectionWSDTO.getRoutingNumber());
            Assert.assertEquals("Account Closed", rejectionWSDTO.getDescription());
            if ("100.00".equals(rejectionWSDTO.getCheckAmt().toString())) {
                Assert.assertEquals("100.00", rejectionWSDTO.getCheckAmt().toString());
            } else {
                Assert.assertEquals("200.00", rejectionWSDTO.getCheckAmt().toString());
            }
        }

        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }

    @Test
    public void queryReturnDD4VPayrollRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testSubmit_HappyPath();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070918000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        processReturn("QBDT", "123272727", TransactionTypeCode.EmployeeDdCredit.toString(), TransactionStateCode.Executed.toString(), "R02", "Account Closed");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Payroll);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(0, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }

    @Test
    public void queryPayrollDD4VPayrollRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testSubmit_HappyPath();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Payroll);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(0, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }

    @Test
    public void queryFailedTransmissionsPayrollRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testSubmit_DuplicatePayment();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Payroll);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(0, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());

    }

    @Test
    public void queryNon1099FailedTransmissionsVendorRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testBillPaymentVoid_HappyPathForNon1099();
        new BillPaymentWebServicesTests().submitBillPayment(false);
        new BillPaymentWebServicesTests().submitBillPayment(false);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(1, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(2, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
        Assert.assertEquals(1, payrollStatusWSDTO.getNon1099LatestFailedTransmissionsWSDTO().size());
        Assert.assertEquals("Change in description of message for DD4V non1099.Please update it in QueryPayrollStatusProcess.java", QueryPayrollStatusProcess.NON_1099_BILLPAYMENT_REJECTION_MESSAGE, payrollStatusWSDTO.getNon1099LatestFailedTransmissionsWSDTO().get(0).getDescription());

    }
    @Test
    public void queryNon1099FailedTransmissionsVendorRoleAfterSuccess() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testBillPaymentVoid_HappyPathForNon1099();
        new BillPaymentWebServicesTests().submitBillPayment(false);
        new BillPaymentWebServicesTests().submitBillPayment(false);
        new BillPaymentWebServicesTests().submitBillPayment(true);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(3, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(1, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(2, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getNon1099LatestFailedTransmissionsWSDTO().size());

    }
    @Test
    public void queryNon1099FailedTransmissionsVendorRoleWithNoCount() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testBillPaymentVoid_HappyPathForNon1099();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Vendor);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(1, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getNon1099LatestFailedTransmissionsWSDTO().size());

    }

    @Test
    public void queryVoidPayrollDD4VPayrollRole() throws Exception {
        // load all necessary data
        new BillPaymentWebServicesTests().testBillPaymentVoidOneTxn();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO("123272727", UserRoleEnum.Payroll);

        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertNotNull(payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        Assert.assertEquals(0, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getDDRejectionsWSDTO().size());
        Assert.assertEquals(0, payrollStatusWSDTO.getFailedTransmissionsWSDTO().size());
    }

    @Test
    public void ddAndTaxPayrollVoidAllChecks() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "100000000", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-24"), new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"61", "62", "63", "64", "66"}, new String[]{"50", "50", "50", "50", "50"});
        ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(payrollRunProcessResult);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, 1, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = Application.refresh(payrollRunProcessResult.getResult());
        Paycheck ddPayCheck = null;

        List<String> paycheckIds = new ArrayList<String>();
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            paycheckIds.add(paycheck.getSourcePaycheckId());
        }

        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(paycheckIds);
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO(company.getSourceCompanyId(), UserRoleEnum.Payroll);
        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertEquals("100000000", payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        assertFalse(payrollStatusWSDTO.getTransmissionsWSDTO().isEmpty());
        assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("12:00 AM", transmissionWSDTO.getTime());
        assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("0.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("0.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("79.08"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("79.08"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 0 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("0.00"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.08"), transmissionWSDTO.getFees().get(2).getAmt());

        assertFalse(payrollStatusWSDTO.getModTransmissionsWSDTO().isEmpty());
        assertEquals(1, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        transmissionWSDTO = payrollStatusWSDTO.getModTransmissionsWSDTO().get(0);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("12:00 AM", transmissionWSDTO.getTime());
        assertEquals("Modified", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("1000.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("2.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("79.08"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("1081.08"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 0 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("0.00"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.08"), transmissionWSDTO.getFees().get(2).getAmt());

        assertTrue(payrollStatusWSDTO.getFailedTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getDDRejectionsWSDTO().isEmpty());
    }

    @Test
    public void ddAndTaxPayrollWithVoids1() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "100000000", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-24"), new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"61", "62", "63", "64", "66"}, new String[]{"50", "50", "50", "50", "50"});
        ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(payrollRunProcessResult);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, 1, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = Application.refresh(payrollRunProcessResult.getResult());
        Paycheck ddPayCheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getDDEmployee() != null) {
                ddPayCheck = paycheck;
                break;
            }
        }
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(Arrays.asList(ddPayCheck.getSourcePaycheckId()));
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, 2, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRunProcessResult.getResult());
        Paycheck taxPayCheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getDDEmployee() == null) {
                taxPayCheck = paycheck;
                break;
            }
        }
        transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(Arrays.asList(taxPayCheck.getSourcePaycheckId()));
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO(company.getSourceCompanyId(), UserRoleEnum.Payroll);
        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertEquals("100000000", payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        assertFalse(payrollStatusWSDTO.getTransmissionsWSDTO().isEmpty());
        assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("02:00 AM", transmissionWSDTO.getTime());
        assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("500.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("1.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("80.61"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("581.61"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 1 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("1.45"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.16"), transmissionWSDTO.getFees().get(2).getAmt());

        assertFalse(payrollStatusWSDTO.getModTransmissionsWSDTO().isEmpty());
        assertEquals(2, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        transmissionWSDTO = payrollStatusWSDTO.getModTransmissionsWSDTO().get(0);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("01:00 AM", transmissionWSDTO.getTime());
        assertEquals("Modified", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("750.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("1.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("80.61"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("831.61"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 1 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("1.45"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.16"), transmissionWSDTO.getFees().get(2).getAmt());

        transmissionWSDTO = payrollStatusWSDTO.getModTransmissionsWSDTO().get(1);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("12:00 AM", transmissionWSDTO.getTime());
        assertEquals("Modified", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("1000.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("2.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("80.61"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("1082.61"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 1 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("1.45"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.16"), transmissionWSDTO.getFees().get(2).getAmt());

        assertTrue(payrollStatusWSDTO.getFailedTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getDDRejectionsWSDTO().isEmpty());
    }

    @Test
    public void ddAndTaxPayrollWithVoids2() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "100000000", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-24"), new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"61", "62", "63", "64", "66"}, new String[]{"50", "50", "50", "50", "50"});
        ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(payrollRunProcessResult);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, 1, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = Application.refresh(payrollRunProcessResult.getResult());
        Paycheck taxPayCheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getDDEmployee() == null) {
                taxPayCheck = paycheck;
                break;
            }
        }
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(Arrays.asList(taxPayCheck.getSourcePaycheckId()));
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, 2, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRunProcessResult.getResult());
        Paycheck ddPayCheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getDDEmployee() != null) {
                ddPayCheck = paycheck;
                break;
            }
        }
        transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(Arrays.asList(ddPayCheck.getSourcePaycheckId()));
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        mRequest = createGetPayrollInfoWSDTO(company.getSourceCompanyId(), UserRoleEnum.Payroll);
        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertNotNull(mResponse.getPayrollStatusWSDTO());

        PayrollStatusWSDTO payrollStatusWSDTO = mResponse.getPayrollStatusWSDTO();
        assertEquals("100000000", payrollStatusWSDTO.getUserID());

        assertNotNull(payrollStatusWSDTO.getErrorWSDTO());
        assertEquals("0", payrollStatusWSDTO.getErrorWSDTO().getCode());
        assertEquals("Success", payrollStatusWSDTO.getErrorWSDTO().getDescription());

        assertNotNull(payrollStatusWSDTO.getAccount());
        assertEquals(AccountStatusEnum.Active, payrollStatusWSDTO.getAccount().getStatus());

        assertFalse(payrollStatusWSDTO.getTransmissionsWSDTO().isEmpty());
        assertEquals(1, payrollStatusWSDTO.getTransmissionsWSDTO().size());
        TransmissionWSDTO transmissionWSDTO = payrollStatusWSDTO.getTransmissionsWSDTO().get(0);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("02:00 AM", transmissionWSDTO.getTime());
        assertEquals("Pending", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("500.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("1.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("80.61"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("581.61"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 1 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("1.45"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.16"), transmissionWSDTO.getFees().get(2).getAmt());

        assertFalse(payrollStatusWSDTO.getModTransmissionsWSDTO().isEmpty());
        assertEquals(2, payrollStatusWSDTO.getModTransmissionsWSDTO().size());
        transmissionWSDTO = payrollStatusWSDTO.getModTransmissionsWSDTO().get(0);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("01:00 AM", transmissionWSDTO.getTime());
        assertEquals("Modified", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("750.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("2.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("80.61"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("832.61"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 1 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("1.45"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.16"), transmissionWSDTO.getFees().get(2).getAmt());

        transmissionWSDTO = payrollStatusWSDTO.getModTransmissionsWSDTO().get(1);
        assertEquals("01/24/2011", transmissionWSDTO.getDate());
        assertEquals("12:00 AM", transmissionWSDTO.getTime());
        assertEquals("Modified", transmissionWSDTO.getOffloadDate());
        assertEquals("01/24/2011", transmissionWSDTO.getCheckDate());
        assertEquals("Regular", transmissionWSDTO.getPayrollType());
        assertEquals(new BigDecimal("1000.00"), transmissionWSDTO.getTotalTaxes());
        assertEquals(new BigDecimal("2.00"), transmissionWSDTO.getTotalDD());
        assertEquals(new BigDecimal("80.61"), transmissionWSDTO.getTotalFees());
        assertEquals(new BigDecimal("1082.61"), transmissionWSDTO.getTotal());
        assertEquals(3, transmissionWSDTO.getFees().size());

        assertEquals("Fee for 1 direct deposit(s) at 1.45 each", transmissionWSDTO.getFees().get(0).getName());
        assertEquals(new BigDecimal("1.45"), transmissionWSDTO.getFees().get(0).getAmt());
        assertEquals("Monthly Fee", transmissionWSDTO.getFees().get(1).getName());
        assertEquals(new BigDecimal("79.00"), transmissionWSDTO.getFees().get(1).getAmt());
        assertEquals("Sales tax", transmissionWSDTO.getFees().get(2).getName());
        assertEquals(new BigDecimal("0.16"), transmissionWSDTO.getFees().get(2).getAmt());

        assertTrue(payrollStatusWSDTO.getFailedTransmissionsWSDTO().isEmpty());
        assertTrue(payrollStatusWSDTO.getDDRejectionsWSDTO().isEmpty());
    }

    private GetPayrollInfoWSDTO createGetPayrollInfoWSDTO(String pPSID, UserRoleEnum pRole) {
        GetPayrollInfoWSDTO getPayrollInfoWSDTO = new GetPayrollInfoWSDTO();

        GetPayrollStatusWSDTO getPayrollStatusWSDTO = new GetPayrollStatusWSDTO();
        getPayrollStatusWSDTO.setUserID(pPSID);
        getPayrollStatusWSDTO.setRoleId(pRole);
        getPayrollInfoWSDTO.setPayrollStatusWSDTO(getPayrollStatusWSDTO);

        return getPayrollInfoWSDTO;
    }

    private PayrollInfoWSDTO createPayrollInfoWSDTO() {
        PayrollInfoWSDTO payrollInfoWSDTO = new PayrollInfoWSDTO();

        PayrollStatusWSDTO payrollStatusWSDTO = new PayrollStatusWSDTO();
        payrollStatusWSDTO.setUserID("12345678");
        payrollInfoWSDTO.setPayrollStatusWSDTO(payrollStatusWSDTO);

        AccountWSDTO accountWSDTO = new AccountWSDTO();
        accountWSDTO.setStatus(AccountStatusEnum.Active);
        payrollStatusWSDTO.setAccount(accountWSDTO);

        ErrorWSDTO errorWSDTO = new ErrorWSDTO();
        errorWSDTO.setCode("0");
        errorWSDTO.setDescription("Success");
        payrollStatusWSDTO.setErrorWSDTO(errorWSDTO);

        List<TransmissionWSDTO> transmissions = new ArrayList<TransmissionWSDTO>();
        TransmissionWSDTO transmission = new TransmissionWSDTO();
        transmission.setCheckDate("10/10/2007");
        transmission.setDate("10/10/2007");
        transmission.setOffloadDate("10/10/2007");
        transmission.setTime("00:00:00");
        transmission.setTotal(new BigDecimal("0.00"));
        transmission.setTotalDD(new BigDecimal("0.00"));
        transmission.setTotalFees(new BigDecimal("0.00"));
        transmission.setTotalTaxes(new BigDecimal("0.00"));
        List<TransmissionFeeWSDTO> fees = new ArrayList<TransmissionFeeWSDTO>();
        TransmissionFeeWSDTO fee = new TransmissionFeeWSDTO();
        fee.setName("Sales tax");
        fee.setAmt(new BigDecimal("0.00"));
        fees.add(fee);
        transmission.setFees(fees);
        transmissions.add(transmission);
        payrollStatusWSDTO.setTransmissionsWSDTO(transmissions);
        payrollStatusWSDTO.setModTransmissionsWSDTO(transmissions);

        List<DDRejectionWSDTO> rejections = new ArrayList<DDRejectionWSDTO>();
        DDRejectionWSDTO rejection = new DDRejectionWSDTO();
        rejection.setBankAccountNumber("0");
        rejection.setDescription("Desc");
        rejection.setFirstName("Jeff");
        rejection.setLastName("Jones");
        rejection.setMiddleName("");
        rejection.setRoutingNumber("0");
        rejection.setCheckAmt(new BigDecimal("0.00"));
        rejection.setCheckDate("10/10/2007");
        rejections.add(rejection);
        payrollStatusWSDTO.setDDRejectionsWSDTO(rejections);

        List<FailedTransmissionWSDTO> failedTransmissions = new ArrayList<FailedTransmissionWSDTO>();
        FailedTransmissionWSDTO failedTransmission = new FailedTransmissionWSDTO();
        failedTransmission.setDate("10/10/2007");
        failedTransmission.setDescription("Desc");
        failedTransmission.setTime("00:00:00");
        failedTransmissions.add(failedTransmission);
        payrollStatusWSDTO.setFailedTransmissionsWSDTO(failedTransmissions);

        return payrollInfoWSDTO;
    }

    private void processReturn(String sourceSystemCD, String sourceCompanyID, String transactionType, String transactionState, String bankReturnCD, String bankReturnData) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            // need auto-flush enabled to handle returns for different payrolls for same company (PSRV001219)
            Application.setDefaultHibernateFlushMode(FlushMode.AUTO);

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || transactionType == null || transactionState == null || bankReturnCD == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, transaction type, " +
                                                   "transaction state or bank return code can not be null");
            }

            if (bankReturnCD.startsWith("C") && bankReturnData == null) {
                throw new RuntimeException("BankReturnData is required for NOC bank return");
            }

            if (!transactionState.equals(TransactionStateCode.Executed.toString())) {
                throw new RuntimeException("Transaction state must be in Executed");
            }

            Company company = Company.findCompany(sourceCompanyID,
                                                  SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }
            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.valueOf(transactionType));
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.valueOf(transactionState));

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                        .And(FinancialTransaction.TransactionType().equalTo(txnType)
                                                                                                 .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState)));

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);
            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf(sourceSystemCD);
            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

            transactionReturnBatch = Application.save(transactionReturnBatch);
            DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
            for (FinancialTransaction financialTx : finTxs) {
                if (financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed) {
                    TransactionReturn transactionReturn = new TransactionReturn();

                    transactionReturn.setBankReturnCd(bankReturnCD);
                    transactionReturn.setBankReturnDescription(bankReturnData);
                    transactionReturn.setReturnBatch(transactionReturnBatch);
                    transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                    transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

                    if (financialTx != null) {
                        transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                        transactionReturn.setCompany(financialTx.getCompany());
                    }

                    transactionReturn = Application.save(transactionReturn);
                    transactionReturns.add(transactionReturn);
                }
            }
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            // Process TransactionReturns associated with the TransactionReturnBatch
            PayrollServices.commitUnitOfWork();

            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(transactionReturnBatch.getId());

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }
    }

}

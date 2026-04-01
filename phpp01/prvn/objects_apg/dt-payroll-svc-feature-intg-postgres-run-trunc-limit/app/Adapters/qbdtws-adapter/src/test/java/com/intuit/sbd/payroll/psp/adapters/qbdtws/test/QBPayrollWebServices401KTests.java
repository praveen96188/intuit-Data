package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXAssert;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessageLevel;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBDTWSSubmitPayrollRequestProcess;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.QBPayrollWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kBatchProcess;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.qbdtws.test.QBDTWSRequestCreator.createQBPaycheckLineItem;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
* Tests that exercise TP401K submissions and offloads.
* @author jesseanderson
*/
@Ignore ("Not using 401K service")
public class QBPayrollWebServices401KTests {
    @BeforeClass
    public static void beforeClass() {

    }

    @AfterClass
    public static void afterClass() {

        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        webService = new QBPayrollWebServices();
        DataLoadServices.reinitialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Finds the most current ThirdParty401kPaycheckState for a ThirdParty401kPaycheck
     * @param thirdParty401kPaycheck The paycheck to check
     * @return The most recent ThirdParty401kPaycheckState for a ThirdParty401kPaycheck
     */
    public static ThirdParty401kPaycheckState findCurrentPaycheckState(ThirdParty401kPaycheck thirdParty401kPaycheck) {
        DomainEntitySet<ThirdParty401kPaycheckState> thirdParty401kPaycheckStates = thirdParty401kPaycheck.
                getThirdParty401kPaycheckStateCollection().sort(ThirdParty401kPaycheckState.StateEffectiveDate().Descending());

        if (thirdParty401kPaycheckStates.size() != 0) {
            return thirdParty401kPaycheckStates.get(0);
        } else {
            return null;
        }
    }

    private QBPayrollWebServices webService;

    /**
     * Gets the corresponding QBPaycheckLineItem to the QBPayrollItem in the QBPaycheck
     * @param qbPaycheck The QBPaycheck find the QBPaycheckLineItem
     * @param qbPayrollItem The QBPayrollItem to find the corresponding QBPaycheckLineItem
     * @return The QBPaycheckLineItem for the QBPayrollItem
     */
    private QBPaycheckLineItem getQBPaycheckLineItem(QBPaycheck qbPaycheck, QBPayrollItem qbPayrollItem) {
        for (QBPaycheckLineItem qbPaycheckLineItem : qbPaycheck.getPreTaxItems()) {
            if (qbPaycheckLineItem.getPayrollItemId().equals(qbPayrollItem.getID())) {
                return qbPaycheckLineItem;
            }
        }

        return null;
    }

    /**
     * Runs various asserts on the ThirdParty401kPaycheck and its child objects to verify that the expected state,
     * ThirdParty401kPaycheckStateCollection size and pending state existence.
     * @param paycheck The paycheck containing the ThirdParty401kPaycheck to check
     * @param expectedPaycheckStateCodes The expected ThirdParty401kPaycheckStateCodes in order
     */
    private void runThirdParty401kPaycheckAsserts(Paycheck paycheck,
                                                  ThirdParty401kPaycheckStateCode... expectedPaycheckStateCodes) {
        ThirdParty401kPaycheck thirdParty401kPaycheck = paycheck.getThirdParty401kPaycheck();

        assertNotNull("ThirdParty401kPaycheck null", thirdParty401kPaycheck);
        assertNotNull("ThirdParty401kPaycheckState null", thirdParty401kPaycheck.getThirdParty401kPaycheckStateCollection());
        assertNotNull("ThirdParty401Paycheck Company null",thirdParty401kPaycheck.getCompany());
        assertEquals("Company object does not match for Paycheck and TP401Paycheck",paycheck.getCompany(),thirdParty401kPaycheck.getCompany());

        StringBuffer buffer = new StringBuffer();
        if (expectedPaycheckStateCodes.length != thirdParty401kPaycheck.getThirdParty401kPaycheckStateCollection().size()) {
            buffer.append("\nActual:\n");
            for (ThirdParty401kPaycheckState paycheckState : thirdParty401kPaycheck.getThirdParty401kPaycheckStateCollection().sort(ThirdParty401kPaycheckState.StateEffectiveDate())) {
                buffer.append(paycheckState).append("\n");
            }

            buffer.append("\nExpected:\n");
            for (ThirdParty401kPaycheckStateCode expectedPaycheckStateCode : expectedPaycheckStateCodes) {
                buffer.append(expectedPaycheckStateCode).append("\n");
            }
            buffer.append("\n");
        }

        assertEquals("ThirdParty401kPaycheckState size incorrect" + buffer,
                     expectedPaycheckStateCodes.length,
                    thirdParty401kPaycheck.getThirdParty401kPaycheckStateCollection().size());

        ThirdParty401kPaycheckStateCode expectedPaycheckStateCode = expectedPaycheckStateCodes[expectedPaycheckStateCodes.length - 1];

        assertEquals("New paycheck is not " + expectedPaycheckStateCode, expectedPaycheckStateCode,
                thirdParty401kPaycheck.getCurrentStateCd());

        ThirdParty401kPaycheckState thirdParty401kPaycheckState = findCurrentPaycheckState(thirdParty401kPaycheck);
        assertEquals("New paycheck ThirdParty401kPaycheckState is not " + expectedPaycheckStateCode, expectedPaycheckStateCode,
                thirdParty401kPaycheckState.getStateCd());

        if (expectedPaycheckStateCode.equals(ThirdParty401kPaycheckStateCode.InvalidEmployeeData) ||
                expectedPaycheckStateCode.equals(ThirdParty401kPaycheckStateCode.Pending) ||
                expectedPaycheckStateCode.equals(ThirdParty401kPaycheckStateCode.Sent)) {
            assertNotNull("ThirdParty401kPaycheckPendingState null", thirdParty401kPaycheck.getThirdParty401kPaycheckPendingState());
            assertEquals("New ThirdParty401kPaycheckPendingState is not " + expectedPaycheckStateCode, expectedPaycheckStateCode,
                    thirdParty401kPaycheck.getThirdParty401kPaycheckPendingState().getStateCd());
            assertEquals("Initiation date do not match up", thirdParty401kPaycheck.getInitiationDate(),
                    thirdParty401kPaycheck.getThirdParty401kPaycheckPendingState().getInitiationDate());
        } else {
            assertEquals("ThirdParty401kPaycheckPendingState not null", null, thirdParty401kPaycheck.getThirdParty401kPaycheckPendingState());
        }
    }

    /**
     * Runs a happy path check on ThirdParty401kPaycheck
     */
    @Test
    public void happyPathDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT, SourceSystemCode.PSP,
                TransmissionType.WS401KSubmitPayroll), "test_QBPayrollWebServices_happyPathDIY.xml",
                Arrays.asList("TransmissionId"));
    }

    /**
     * Verify that state does not change after hitting sent state
     */
    @Test
    public void testFinalSentStateChangeDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
        }

        Application.commitUnitOfWork();

        // Void and resend and should be skipped because the paycheck is in a final state
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.VOID);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
        }

        Application.commitUnitOfWork();

        // Delete and resend and should be skipped because the paycheck is in a final state
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.DELETE);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that state does not change after hitting void state
     */
    @Test
    public void testFinalVoidStateChangeDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Void and resend
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.VOID);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();

        // Run offload and should be skipped
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();

        // Delete and resend and should be skipped because the paycheck is in a final state
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.DELETE);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that void state change can be reached
     */
    @Test
    public void checkTP401VoidedStateChangesDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Void and resend
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.VOID);
        }

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that deleted state change can be reached
     */
    @Test
    public void checkTP401DeleteStateChangesDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Delete and resend
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.DELETE);
        }

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that InvalidEmployeeData state change can be reached
     */
    @Test
    public void checkTP401InvalidEmployeeDataStateChangesDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        // Null birthday to put invalid employee data
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(null);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that ineligible state change can be reached
     */
    @Test
    public void checkTP401IneligibleStateChangesDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/01/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        // Increase PSPDATE to make paychecks ineligible
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110110000000");
        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that invalid state change can be reached
     */
    @Test
    public void checkTP401InvalidPaycheckDataStateChangesDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));
        request.setPaycheckList(QBDTWSRequestCreator.createQBPaychecksForEmployees(company, company.getCloudEmployees(), PSPDate.getPSPTime()));
        request.setPayrollItemList(QBDTWSRequestCreator.createPayrollItems(company));

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            QBPayrollItem preTaxPayrollItem = null;
            for (QBPayrollItem payrollItem : request.getPayrollItemList().getPayrollItem()) {
                if (payrollItem.getPayrollItemCategory() == QBPayrollItemCategory.PRE_TAX_ITEM) {
                    preTaxPayrollItem = payrollItem;
                    preTaxPayrollItem.setAgencyNumber("401k");
                    preTaxPayrollItem.setTaxTrackingTypeId(QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_401K);
                    break;
                }
            }

            if (preTaxPayrollItem != null) {
                QBPaycheckLineItem preTaxItem = createQBPaycheckLineItem(preTaxPayrollItem.getID(), 50D, 100D);
                qbPaycheck.getPreTaxItems().add(preTaxItem);
            }
        }

        Application.commitUnitOfWork();

        ProcessingResponse response = webService.SubmitPayroll(request);
        WS_Assert.assertSuccess("SubmitPayroll failed", response);

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
        }
    }

    /**
     * Add an ineligible paycheck.  Update it with valid information.
     * NOTE: This is a placeholder until reps can do this manually
     */
    @Ignore
    @Test
    public void updateFromIneligibleToPendingDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/01/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        PSPDate.setPSPTime("20110110000000");

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible);
        }

        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible, ThirdParty401kPaycheckStateCode.Pending);
        }
    }

    /**
     * Add InvalidEmployeeInformation.  Update it with valid information.
     */
    @Test
    public void updateFromInvalidEmployeeDataToPendingDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
           QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        // Remove birthdate to make employee invalid
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(null);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        SpcfCalendar[] initiationDates = new SpcfCalendar[request.getPaycheckList().getPaycheck().size()];
        int iteration = 0;
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
            initiationDates[iteration] = paycheck.getThirdParty401kPaycheck().getInitiationDate();
            iteration++;
        }

        // Add a birthdate to make employee valid
        QBDate birthDate = QBDTWSRequestCreator.createQBDate(2000, 1, 1);

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(birthDate);
        }

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.EDIT);
        }

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        iteration = 0;

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
            assertFalse("Initiation date did not change",
                    !initiationDates[iteration].equals(paycheck.getThirdParty401kPaycheck().getInitiationDate()));
            iteration++;
        }

        Application.commitUnitOfWork();
    }

    /**
     * Add paycheck with invalid information.  Update it with valid information.
     */
    @Test
    public void updateFromInvalidPaycheckDataToPendingDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            // Create invalid 401K deferral
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    // Make paycheck have invalid 401K deferral
                    .add401kEmployeeDeferralLine(12, 100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
        }

        // Make paycheck valid again
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            QBPaycheckLineItem qb401KPaycheckLineItem = getQBPaycheckLineItem(qbPaycheck,
                    payrollItemRepository.get401kEmployeeDeferralDeductionItem());
            qb401KPaycheckLineItem.setCurrent(new BigDecimal(-12));
            qb401KPaycheckLineItem.setYTD(new BigDecimal(-100));
        }

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidPaycheckData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Creates a paycheck with valid employee information.  Update it with invalid employee information and verifies
     * TP401 paychecks are invalid.
     */
    @Test
    public void updateFromPendingToInvalidEmployeeDataDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Update employee information to make InvalidEmployeeData
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(null);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }
    }

    /**
     * Creates a valid employee and paycheck then updates with invalid employee information.  Verifies that paychecks
     * go from pending to InvalidEmployeeData
     */
    @Test
    public void createValidEmployeeAndInvalidateDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Remove all paycheck information to only update employee information
        qbPaychecks = request.getPaycheckList();

        request.setPaycheckList(null);
        request.setPayrollItemList(null);

        // Make employee invalid
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(null);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : qbPaychecks.getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Creates a company without TP401K, creates a paycheck, then adds TP401K
     */
    @Test
    public void addTP401KCompanyDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20100101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);

        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/20/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        DataLoadServices.add401kService(company);

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Creates an invalid employee and paycheck then updates with valid employee information.  Verifies that paychecks
     * go from InvalidEmployeeData to pending
     */
    @Test
    public void createInvalidEmployeeAndMakeValidDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        // Make employee invalid
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(null);
        }

        ProcessingResponse response = webService.SubmitPayroll(request);
        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, response);

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        Application.commitUnitOfWork();

        // Remove all paycheck information to only update employee information
        qbPaychecks = request.getPaycheckList();

        request.setPaycheckList(null);
        request.setPayrollItemList(null);

        QBDate birthDate = QBDTWSRequestCreator.createQBDate(2000, 1, 1);

        // Make employee valid again
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(birthDate);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : qbPaychecks.getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }
    }

    /**
     * Creates a paychecks with InvalidEmployeeData, InvalidPaycheckData, cancelled and good paychecks.
     * Verifies that only good paychecks are offloaded.
     */
    @Test
    public void happyPathOffloadWithInvalidDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);

        // Create InvalidEmployeeData
        QBEmployee qbInvalidEmployee = request.getSubmitEmployeesRequest().getEmployees().getEmployee().get(0);
        qbInvalidEmployee.setBirthDate(null);

        QBEmployee qbGoodEmployee = request.getSubmitEmployeesRequest().getEmployees().getEmployee().get(1);

        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        // Add a canceled paycheck to good employee
        QBPaycheck voidedPaycheck = paycheckGenerator.newPaycheck(qbGoodEmployee, "01/20/2011", 103)
            .addEarningLine(12, 120.50, 10200.48)
            .add401kEmployeeDeferralLine(-12, -100)
            .getPaycheck();
        qbPaychecks.getPaycheck().add(voidedPaycheck);

        // Add an invalid paycheck to good employee
        QBPaycheck invalidPaycheck = paycheckGenerator.newPaycheck(qbGoodEmployee, "01/27/2011", 104)
            .addEarningLine(12, 120.50, 10200.48)
            .add401kEmployeeDeferralLine(12, 100)
            .getPaycheck();
        qbPaychecks.getPaycheck().add(invalidPaycheck);

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        // Void one of the paychecks to prevent it from being offloaded
        voidedPaycheck.setOperation(QBPaycheckOperationEnum.VOID);

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());

            if (qbPaycheck.getEmployeeID().equals(qbInvalidEmployee.getSourceEmployeeId())) {
                // Employee has invalid information
                runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
            } else if (qbPaycheck.getEmployeeID().equals(qbGoodEmployee.getSourceEmployeeId())) {
                if (voidedPaycheck.equals(qbPaycheck)) {
                    // Its the voided paycheck, make sure it didn't offload
                    runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
                } else if (invalidPaycheck.equals(qbPaycheck)) {
                    // Its the invalid paycheck, make sure it didn't offload
                    runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
                } else {
                    // Its the good check for the good employee, make sure it offloaded
                    runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
                }
            } else {
                // Should not reach here
                assertEquals("Employee not found for paycheck", 0, 1);
            }
        }
    }

    /**
     * Add paycheck with invalid employee and invalid paycheck information.  Update employee with valid information and
     * verify that paycheck is in Invalid state.
     */
    @Test
    public void updateFromInvalidEmployeeDataToInvalidDIY() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        // Make paychecks invalid
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(12, 100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        // Make employee invalid
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(null);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        Application.commitUnitOfWork();

        QBDate birthDate = QBDTWSRequestCreator.createQBDate(2000, 1, 1);

        // Make employee valid again
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(birthDate);
        }

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            qbPaycheck.setOperation(QBPaycheckOperationEnum.EDIT);
        }

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        // Verify that paychecks did not go from InvalidEmployeeData to pending
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Runs a happy path check on ThirdParty401kPaycheck
     */
    @Test
    public void happyPathDD() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k,
                ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Runs a happy path check on 401K Loan Payments.  Run the 401K offload and verifies the output again.
     */
    @Test
    public void happyPathLoanPayment() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k,
                ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kLoanPaymentLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);

            DomainEntitySet<Deduction> deductions = paycheck.getDeductionCollection();
            assertEquals("Correct number of deductions not found", 1, deductions.size());
            assertEquals("Loan deduction amount not correct", new SpcfMoney("12"), deductions.get(0).getDeductionAmount());
            assertEquals("Loan deduction amount not correct", new SpcfMoney("100"), deductions.get(0).getDeductionYTDAmount());

            ThirdParty401kPaycheck.PayrollFilePaycheck mPayrollFilePaycheck = paycheck.getThirdParty401kPaycheck().getPayrollFilePaycheck();

            assertEquals("Paycheck Loan deduction amount not correct", "12.0", mPayrollFilePaycheck.getLoan());
        }

        PSPDate.setPSPTime("20110114000000");
        PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

        Application.commitUnitOfWork();

        new ThirdParty401kBatchProcess().createFiles();

        PayrollServices.beginUnitOfWork();
        
        ThirdParty401kBatch tp401kBatch = ThirdParty401kBatch.getMostRecentPayrollTP401kBatch(ThirdParty401kBatchStatusCode.Finalized);
        assertNotNull("ThirdParty401kBatch", tp401kBatch);
        ArrayList<String> recordList = getFileRecords(tp401kBatch.getFileName());
        Assert.assertEquals("Number of Records",  2, recordList.size());

        for (String record : recordList) {
            assertTrue("401K output does not match expected.  Output was:\"" + record + "\"",
                    record.matches("Intuit,Intuit,\\d*,\\d*,Last_\\d*,First_\\d*,120.5,0.0,,12.0,0.0,0.0,0.0,\\d*,\\d*,\\d*,.*"));
        }

        Application.rollbackUnitOfWork();
    }

    private ArrayList<String> getFileRecords(String pFileName) throws Exception {
        BufferedReader bufferedReader = null;
        ArrayList<String> recordList;
        try {
            bufferedReader = new BufferedReader(new FileReader(pFileName));

            String lastReadLine;
            recordList = new ArrayList<String>();

            lastReadLine = bufferedReader.readLine();
            while (lastReadLine != null) {
                if (lastReadLine.length() != 0) {
                    recordList.add(lastReadLine);
                }
                lastReadLine = bufferedReader.readLine();
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return recordList;
    }

    /** The OFX that was generated */
    OFX ofx;
    /** The company created by the OFX */
    Company company = null;

    /**
     * Generates the OFX
     * @param psid The PSID of the company to create
     * @param dateTime The dateTime for the generation
     * @param pCreate401K Whether or not to create the 401K entries
     */
    public void createAssistedOFX(String psid, String dateTime, boolean pCreate401K) {
        if (dateTime == null) {
            dateTime = "20110101000000";
        }
        // paychecks in balance file are hard coded to 1-11-2011
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(dateTime);
        PayrollServices.commitUnitOfWork();

        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        }

        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ofx = OFXRequestGenerator.generateBalanceFile(psid, true, false, pCreate401K);
        IEMP emp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);
        emp.getIEMPTAX().getIEMPFIT().setIFEDFILESTATUS("MARRIED");
        emp.getIEMPTAX().getIEMPSIT().setISTATEFILESTATUS("MARRIED");
    }

    /**
     * Submits the OFX to the servlet and runs asserts to verify success
     */
    private void submitOFX() {
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        Assert.assertEquals(5, payrollRuns.size());

        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        Assert.assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateCompanyPIN(company, DataLoadServices.PIN);
    }

    /**
     * Finds the 401K adjustment line for a paycheck
     * @param ipaychk The paycheck to look in
     * @param ipitems The list of paycheck items to find the 401K item in
     * @return The 401K adjustment line for the paycheck or null if not found
     */
    private IADJLINE find401KAdj(IPAYCHK ipaychk, List<IPITEM> ipitems) {
        IPITEM foundItem = null;

        // Find the 401K in the list
        for (IPITEM ipitem : ipitems) {
            if (ipitem.getIPITEMNAME().startsWith("401K ")) {
                foundItem = ipitem;
                break;
            }
        }

        if (foundItem != null) {
            // Find that paycheck's adjustment line
            for (IADJLINE iadjline : ipaychk.getIADJLINE()) {
                if (iadjline.getIPITEMID().equals(foundItem.getIPITEMID())) {
                    return iadjline;
                }
            }
        }

        return null;
    }

    /**
     * Creates an assisted company and submits.
     */
    @Test
    public void happyPathWithAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Gets the correct QBPaycheck id based on assisted or diy
     * @param qbPaycheck The paycheck to find the id
     * @return The correct id for the paycheck
     */
    private String getPaycheckId(QBPaycheck qbPaycheck) {
        if (qbPaycheck.getOfxPaycheckID() != null) {
            return qbPaycheck.getOfxPaycheckID();
        } else {
            return qbPaycheck.getPaycheckID();
        }
    }

    /**
     * Takes the current OFX and creates a paycheck mod with all paychecks voided
     */
    private OFX voidOFX() {
        OFX voidOfx = new OFX();
        List<IPAYROLLRUN> voidPayrolls = new ArrayList<IPAYROLLRUN>();
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            IPAYROLLRUN voidIpayrollrun = new IPAYROLLRUN();
            voidIpayrollrun.setIDTPAYCHKS(ipayrollrun.getIDTPAYCHKS());
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                IPAYCHK ipaychkmod = new IPAYCHK();
                ipaychkmod.setIPAYCHKID(ipaychk.getIPAYCHKID());
                ipaychkmod.setIEMPID(ipaychk.getIEMPID());
                ipaychkmod.setIPAYCHKTYPE(ipaychk.getIPAYCHKTYPE());
                ipaychkmod.setIEMPNAME(ipaychk.getIEMPNAME());
                ipaychkmod.setICLASS(ipaychk.getICLASS());
                ipaychkmod.setIACCTNAME(ipaychk.getIACCTNAME());
                ipaychkmod.setIPAYCHKINFO(ipaychk.getIPAYCHKINFO());
                ipaychkmod.getIPAYCHKINFO().setICHKNUM("c" + ipaychkmod.getIPAYCHKINFO().getICHKNUM());
                ipaychkmod.setIVOID("Y");
                ipaychkmod.setIDTPAYPDBEGIN(ipaychk.getIDTPAYPDBEGIN());
                ipaychkmod.setIDTPAYPDEND(ipaychk.getIDTPAYPDEND());
                ipaychkmod.setIMEMO(ipaychk.getIMEMO());
                ipaychkmod.setICLEARED("9");
                ipaychkmod.setIONSERVICE("Y");
                ipaychkmod.setIDTTX(ipaychk.getIDTTX());
                voidIpayrollrun.getIPAYCHKMOD().add(ipaychkmod);
            }
            voidPayrolls.add(voidIpayrollrun);
        }
        voidOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                voidPayrolls);
        company = DataLoadServices.refreshCompany(company);
        voidOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        return voidOfx;
    }

    /**
     * Creates an employee payroll mod with employees that have the supplied birthday
     * @param birthday The birthday of the employees
     * @return The OFX for the employees
     */
    private OFX createEmployeeModWithBirthday(String birthday) {
        OFX employeeOfx = new OFX();
        employeeOfx.setSIGNONMSGSRQV1(ofx.getSIGNONMSGSRQV1());

        List<IEMP> employeeMods = new ArrayList<IEMP>();
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            // Set birthday to make pending
            ICUSTOMFLD birthdayField = QBDTWSRequestCreator.getBirthday(iemp);

            birthdayField.setIFLDVALUE(birthday);

            employeeMods.add(iemp);
        }

        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 employeeMods,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);

        company = DataLoadServices.refreshCompany(company);
        employeeOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        return employeeOfx;
    }

    /**
     * Verify that state does not change after hitting sent state
     */
    @Test
    public void testFinalSentStateChangeAssisted() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // offload payroll
        DataLoadServices.runOffload(company, 2011, 1, 3);

        try {
            PayrollServices.beginUnitOfWork();
           PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
        }

        Application.commitUnitOfWork();

        // Void and resend and should be skipped because the paycheck is in a final state
        ofx = voidOFX();
        submitOFX();

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that state does not change after hitting void state
     */
    @Test
    public void testFinalVoidStateChangeAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Void and resend and should be skipped because the paycheck is in a final state
        ofx = voidOFX();
        submitOFX();

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData,
                    ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();

        // Run offload and should be skipped
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData,
                    ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that void state change can be reached
     */
    @Test
    public void checkTP401VoidedStateChangesAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData,
                    ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Void paycheck and resubmit
        ofx = voidOFX();
        submitOFX();

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData,
                    ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that InvalidEmployeeData state change can be reached
     */
    @Test
    public void checkTP401InvalidEmployeeDataStateChangesAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        // Set birthday to null to put InvalidEmployeeData
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            ICUSTOMFLD birthday = QBDTWSRequestCreator.getBirthday(iemp);
            birthday.setIFLDVALUE(null);
        }

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that ineligible state change can be reached
     */
    @Test
    public void checkTP401IneligibleStateChangesAssisted() {
        // Change PSPDATE to make paycheck ineligible
        String psid = "123456789";
        createAssistedOFX(psid, "20110110000000", true);
        submitOFX();

        // add 401k service w/1-1-2011 service start date
        DataLoadServices.add401kService(company, SpcfCalendar.createInstance(2011, 1, 1));

        Application.beginUnitOfWork();
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, ipaychk.getIPAYCHKID());
                runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible);
            }
        }

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.rollbackUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Verify that invaild state change can be reached
     */
    @Test
    public void checkTP401InvalidStateChangesAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        BigDecimal negativeOne = new BigDecimal("-1");

        // Make all paychecks invalid
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                IADJLINE iadjline401K = find401KAdj(ipaychk, ipitems);
                BigDecimal adjSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIAMT()).multiply(negativeOne);
                iadjline401K.setIAMT("$" + adjSwappedSign.toPlainString());

                BigDecimal adjYTDSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIYTDAMT()).multiply(negativeOne);
                iadjline401K.setIYTDAMT("$" + adjYTDSwappedSign.toPlainString());
            }
        }

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
        }
    }

    /**
     * Add an ineligible paycheck.  Update it with valid information.
     * NOTE: This is a placeholder until reps can do this manually
     */
    @Ignore
    @Test
    public void updateFromIneligibleToPendingAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        // Move PSPDATE to make paycheck ineligible
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110110000000");
        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible);
        }

        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());

            // Use first state because we are moving PSPDate around
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Ineligible, ThirdParty401kPaycheckStateCode.Pending);
        }
    }

    /**
     * Add InvalidEmployeeInformation.  Update it with valid information.
     */
    @Test
    public void updateFromInvalidEmployeeDataToPendingAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        // Make employee invalid
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            // Set birthday to null to put InvalidEmployeeData
            ICUSTOMFLD birthday = QBDTWSRequestCreator.getBirthday(iemp);
            birthday.setIFLDVALUE(null);
        }

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        SpcfCalendar[] initiationDates = new SpcfCalendar[request.getPaycheckList().getPaycheck().size()];
        int iteration = 0;

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
            initiationDates[iteration] = paycheck.getThirdParty401kPaycheck().getInitiationDate();
            iteration++;
        }

        Application.commitUnitOfWork();

        QBDate birthDate = QBDTWSRequestCreator.createQBDate(2000, 1, 1);

        // Set birthday to make employees valid again
        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(birthDate);
        }

        // Update the employee with valid data
        ofx = createEmployeeModWithBirthday("01/15/2000");

        submitOFX();

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        iteration = 0;

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
            assertFalse("Initiation date did not change",
                    !initiationDates[iteration].equals(paycheck.getThirdParty401kPaycheck().getInitiationDate()));
            iteration++;
        }

        Application.commitUnitOfWork();
    }

    /**
     * Add paycheck with invalid information.  Update it with valid information.
     */
    @Test
    public void updateFromInvalidPaycheckDataToPendingAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        BigDecimal negativeOne = new BigDecimal("-1");

        // Make paychecks invalid
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                IADJLINE iadjline401K = find401KAdj(ipaychk, ipitems);
                BigDecimal adjSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIAMT()).multiply(negativeOne);
                iadjline401K.setIAMT("$" + adjSwappedSign.toPlainString());

                BigDecimal adjYTDSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIYTDAMT()).multiply(negativeOne);
                iadjline401K.setIYTDAMT("$" + adjYTDSwappedSign.toPlainString());
            }
        }

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        OFX modOfx = new OFX();

        // Make paycheck mod to make values valid
        List<IPAYROLLRUN> voidPayrolls = new ArrayList<IPAYROLLRUN>();
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            IPAYROLLRUN voidIpayrollrun = new IPAYROLLRUN();
            voidIpayrollrun.setIDTPAYCHKS(ipayrollrun.getIDTPAYCHKS());
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                IPAYCHK ipaychkmod = new IPAYCHK();
                ipaychkmod.setIPAYCHKID(ipaychk.getIPAYCHKID());
                ipaychkmod.setIEMPID(ipaychk.getIEMPID());
                ipaychkmod.setIPAYCHKTYPE(ipaychk.getIPAYCHKTYPE());
                ipaychkmod.setIEMPNAME(ipaychk.getIEMPNAME());
                ipaychkmod.setICLASS(ipaychk.getICLASS());
                ipaychkmod.setIACCTNAME(ipaychk.getIACCTNAME());
                ipaychkmod.setIPAYCHKINFO(ipaychk.getIPAYCHKINFO());
                ipaychkmod.getIPAYCHKINFO().setICHKNUM("c" + ipaychkmod.getIPAYCHKINFO().getICHKNUM());
                ipaychkmod.setIVOID("N");
                ipaychkmod.setIDTPAYPDBEGIN(ipaychk.getIDTPAYPDBEGIN());
                ipaychkmod.setIDTPAYPDEND(ipaychk.getIDTPAYPDEND());
                ipaychkmod.setIMEMO(ipaychk.getIMEMO());
                ipaychkmod.setICLEARED("9");
                ipaychkmod.setIONSERVICE("Y");
                ipaychkmod.setIDTTX(ipaychk.getIDTTX());

                IADJLINE iadjline401K = find401KAdj(ipaychk, ipitems);
                BigDecimal adjSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIAMT());//.multiply(negativeOne);
                iadjline401K.setIAMT("$" + adjSwappedSign.toPlainString());

                BigDecimal adjYTDSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIYTDAMT());//.multiply(negativeOne);
                iadjline401K.setIYTDAMT("$" + adjYTDSwappedSign.toPlainString());

                ipaychkmod.getIADJLINE().add(iadjline401K);

                voidIpayrollrun.getIPAYCHKMOD().add(ipaychkmod);
            }
            voidPayrolls.add(voidIpayrollrun);
        }
        modOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                voidPayrolls);
        Application.commitUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        modOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTWSRequestCreator.createPayrollItems(modOfx, request);

        modOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().addAll(ipitems);

        ofx = modOfx;

        submitOFX();

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck,
                                             ThirdParty401kPaycheckStateCode.InvalidEmployeeData,
                                             ThirdParty401kPaycheckStateCode.Pending,
                                             ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Creates a paycheck with valid employee information.  Update it with invalid employee information and verifies
     * TP401 paychecks are invalid.
     */
    @Test
    public void updateFromPendingToInvalidEmployeeDataAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Update the employee with invalid data
        ofx = createEmployeeModWithBirthday(null);

        submitOFX();

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending,
                    ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }
    }

    /**
     * Creates a valid employee and paycheck then updates with invalid employee information.  Verifies that paychecks
     * go from pending to InvalidEmployeeData
     */
    @Test
    public void createValidEmployeeAndInvalidateAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        // Update the employee with invalid data
        ofx = createEmployeeModWithBirthday(null);

        submitOFX();

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        // For Assisted you can't just send in an employee mod, it has to have paychecks or the QBPayrollWebServices
        // rejects it
        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : qbPaychecks.getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Creates an invalid employee and paycheck then updates with valid employee information.  Verifies that paychecks
     * go from InvalidEmployeeData to pending
     */
    @Test
    public void createInvalidEmployeeAndMakeValidAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        // Make all employees invalid
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            ICUSTOMFLD birthday = QBDTWSRequestCreator.getBirthday(iemp);
            birthday.setIFLDVALUE(null);
        }

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        // Update the employee with valid data
        ofx = createEmployeeModWithBirthday("01/15/2000");

        submitOFX();

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        // For Assisted you can't just send in an employee mod, it has to have paychecks or the QBPayrollWebServices
        // rejects it
        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : qbPaychecks.getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }
    }

    /**
     * Creates a paychecks with InvalidEmployeeData, cancelled, invalid and good paychecks.  Verifies that only good
     * paychecks are offloaded.
     */
    @Test
    public void happyPathOffloadWithInvalidAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        List<IEMP> iemps = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        assertEquals("IEMP size not correct", 5, iemps.size());

        // Create employee with invalid data
        IEMP invalidIemp = iemps.get(0);
        QBDTWSRequestCreator.getBirthday(invalidIemp).setIFLDVALUE(null);

        // Make employee's paychecks invalid
        IEMP cancelledPaychecksEmp = iemps.get(1);

        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                if (cancelledPaychecksEmp.getIEMPID().equals(ipaychk.getIEMPID())) {
                    ipaychk.setIVOID("Y");
                }
            }
        }

        // Make invalid paychecks for employee
        IEMP invalidEmp = iemps.get(2);

        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        BigDecimal negativeOne = new BigDecimal("-1");

        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                if (invalidEmp.getIEMPID().equals(ipaychk.getIEMPID())) {
                    IADJLINE iadjline401K = find401KAdj(ipaychk, ipitems);
                    BigDecimal adjSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIAMT()).multiply(negativeOne);
                    iadjline401K.setIAMT("$" + adjSwappedSign.toPlainString());

                    BigDecimal adjYTDSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIYTDAMT()).multiply(negativeOne);
                    iadjline401K.setIYTDAMT("$" + adjYTDSwappedSign.toPlainString());
                }
            }
        }

        // Make "good" employees whose paychecks are valid
        IEMP goodEmp1 = iemps.get(3);
        IEMP goodEmp2 = iemps.get(4);

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();
            new ThirdParty401kBatchProcess().createFiles();
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Exception creating files");
        }

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));

            if (qbPaycheck.getEmployeeID().equals(invalidIemp.getIEMPID())) {
                // Employee is InvalidEmployeeData and verify it
                runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
            } else if (qbPaycheck.getEmployeeID().equals(cancelledPaychecksEmp.getIEMPID())) {
                // Employee has invalid paychecks and verify it
                runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Cancelled);
            } else if (qbPaycheck.getEmployeeID().equals(invalidEmp.getIEMPID())) {
                // Employee has invalid paychecks and verify it
                runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
            } else if (qbPaycheck.getEmployeeID().equals(goodEmp1.getIEMPID()) || qbPaycheck.getEmployeeID().equals(goodEmp2.getIEMPID())) {
                // Employee is verify it was offloaded
                runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending, ThirdParty401kPaycheckStateCode.Sent);
            } else {
                // Should not reach here
                assertEquals("Employee not found for paycheck", 0, 1);
            }
        }
    }

    /**
     * Add paycheck with invalid employee and invalid paycheck information.  Update employee with valid information and
     * verify that paycheck is in Invalid state.
     */
    @Test
    public void updateFromInvalidEmployeeDataToInvalidAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        // Set birthday to null to put InvalidEmployeeData
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            ICUSTOMFLD birthday = QBDTWSRequestCreator.getBirthday(iemp);
            birthday.setIFLDVALUE(null);
        }

        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        BigDecimal negativeOne = new BigDecimal("-1");

        // Make paychecks invalid too
        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                IADJLINE iadjline401K = find401KAdj(ipaychk, ipitems);
                BigDecimal adjSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIAMT()).multiply(negativeOne);
                iadjline401K.setIAMT("$" + adjSwappedSign.toPlainString());

                BigDecimal adjYTDSwappedSign = QBDTWSRequestCreator.getOFXAmount(iadjline401K.getIYTDAMT()).multiply(negativeOne);
                iadjline401K.setIYTDAMT("$" + adjYTDSwappedSign.toPlainString());
            }
        }

        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        QBDate birthDate = QBDTWSRequestCreator.createQBDate(2000, 1, 1);

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            qbEmployee.setBirthDate(birthDate);
        }
        PayrollServices.commitUnitOfWork();

        // Update the employee with valid data
        ofx = createEmployeeModWithBirthday("01/15/2000");



        submitOFX();

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        Application.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        // Verify that paychecks went from InvalidEmployeeData to invalid and not pending
        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Creates a company without TP401K, creates a paycheck, then adds TP401K
     */
    @Test
    public void addTP401KCompanyAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);

        for (IPAYROLLRUN ipayrollrun : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            ipayrollrun.setIDTPAYCHKS("20110115");

            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                ipaychk.setIDTTX("20110115");
                ipaychk.setIDTPAYPDBEGIN("20110115");
                ipaychk.setIDTPAYPDEND("20110130");
            }
        }

        submitOFX();

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company, ofx));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertCount(QBProcessingMessageLevel.ERROR, 0, webService.SubmitPayroll(request));

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            // Birthday isn't stored until a company is on 401K, so all paychecks go straight to InvalidEmployeeData
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
        }

        PayrollServices.commitUnitOfWork();
   }


   /**
     * Terminates and marks inactive an employee and verifies that their birth date and 401K paychecks are still valid
     */
    @Test
    public void terminateAndInactiveEmployeeAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        List<IEMP> employeeMods = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        List<IEMP> newEmployeeMods = new ArrayList<IEMP>(employeeMods.size());

        for (IEMP iemp : employeeMods) {
            // Terminate employee
            iemp.setIINACTIVE("Y");
            iemp.setIDTRELEASE("20110101");
            iemp.setIEMPID(iemp.getIEMPID());
            newEmployeeMods.add(iemp);
        }

        ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                newEmployeeMods,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        company = DataLoadServices.refreshCompany(company);
        ofx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        submitOFX();

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        request.setPayrollItemList(QBDTWSRequestCreator.createDefaultPayrollItems());

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0010");
        request.setPaycheckList(qbPaychecks);

        QBEmployees termEmployees = new QBEmployees();

        for (QBEmployee employee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBEmployee qbEmployee = new QBEmployee();

            qbEmployee.setSourceCompanyId(employee.getSourceCompanyId());
            qbEmployee.setSourceEmployeeId(employee.getSourceEmployeeId());
            qbEmployee.setOfxEmployeeId(employee.getOfxEmployeeId());
            qbEmployee.setActive(false);

            termEmployees.getEmployee().add(qbEmployee);
        }

        request.getSubmitEmployeesRequest().setEmployees(termEmployees);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        for (Employee employee : company.getCloudEmployees()) {
            assertNotNull("Birth date not found", employee.getBirthDate());
        }

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Terminates an employee and verifies that their birth date and 401K paychecks are still valid
     */
    @Test
    public void terminateEmployeeAssisted() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        createAssistedOFX(psid, "20110101000000", true);
        submitOFX();

        DataLoadServices.add401kService(company);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSRequestCreator.createPayrollItems(ofx, request);

        QBPaychecks qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0001");
        request.setPaycheckList(qbPaychecks);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        Application.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();

        List<IEMP> employeeMods = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        List<IEMP> newEmployeeMods = new ArrayList<IEMP>(employeeMods.size());

        for (IEMP iemp : employeeMods) {
            // Terminate employee
            iemp.setIDTRELEASE("20110101");
            newEmployeeMods.add(iemp);
        }

        ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                newEmployeeMods,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        company = DataLoadServices.refreshCompany(company);
        ofx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        submitOFX();

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        request.setPayrollItemList(QBDTWSRequestCreator.createDefaultPayrollItems());

        qbPaychecks = QBDTWSRequestCreator.createQBPaycheckFromOFX(ofx, "0010");
        request.setPaycheckList(qbPaychecks);

        QBEmployees termEmployees = new QBEmployees();

        for (QBEmployee employee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBDate qbDate = new QBDate();
            qbDate.setYear(2011);
            qbDate.setMonth(1);
            qbDate.setDay(1);

            employee.setTerminationDate(qbDate);
            employee.setActive(true);

            termEmployees.getEmployee().add(employee);
        }

        request.getSubmitEmployeesRequest().setEmployees(termEmployees);

        PayrollServices.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        for (Employee employee : company.getCloudEmployees()) {
            assertNotNull("Birth date not found", employee.getBirthDate());
        }

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.InvalidEmployeeData, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Terminates and marks inactive an employee and verifies that their birth date and 401K paychecks are still valid
     */
    @Test
    public void terminateAndInactiveEmployeeDD() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k,
                ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();

        for (QBPaycheck qbPaycheck : request.getPaycheckList().getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, qbPaycheck.getPaycheckID());
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        request.setPayrollItemList(QBDTWSRequestCreator.createDefaultPayrollItems());

        QBEmployees termEmployees = new QBEmployees();

        for (QBEmployee employee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBEmployee qbEmployee = new QBEmployee();
            QBDate qbDate = new QBDate();
            qbDate.setYear(2011);
            qbDate.setMonth(1);
            qbDate.setDay(1);

            qbEmployee.setSourceCompanyId(employee.getSourceCompanyId());
            qbEmployee.setSourceEmployeeId(employee.getSourceEmployeeId());
            qbEmployee.setOfxEmployeeId(employee.getOfxEmployeeId());
            qbEmployee.setTerminationDate(qbDate);
            qbEmployee.setActive(false);

            termEmployees.getEmployee().add(qbEmployee);
        }

        Application.commitUnitOfWork();

        request.getSubmitEmployeesRequest().setEmployees(termEmployees);

        WS_Assert.assertSuccess("SubmitPayroll failed", webService.SubmitPayroll(request));

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        for (Employee employee : company.getCloudEmployees()) {
            assertNotNull("Birth date not found", employee.getBirthDate());
        }

        for (QBPaycheck qbPaycheck : qbPaychecks.getPaycheck()) {
            Paycheck paycheck = Paycheck.findPaycheck(company, getPaycheckId(qbPaycheck));
            runThirdParty401kPaycheckAsserts(paycheck, ThirdParty401kPaycheckStateCode.Pending);
        }

        Application.commitUnitOfWork();
    }
}

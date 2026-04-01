package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentSplitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHOffloadRunner;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.*;
import java.lang.Process;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: TimothyD698
 * Date: 4/16/13
 */
public class CheckBPLimitsTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
//        runAfterEachTest();

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

        try {
            Application.executeSqlCommand("truncate table psp_limit_value", true);
            String monolithDBConnectionToken = DatabaseConfigManager.MonolithDbToken;
            String dbUserName=ConfigurationManager.getSettingValue(monolithDBConnectionToken, "dataAccess.connection.username");
            String dbPassword=ConfigurationManager.getSettingValue(monolithDBConnectionToken, "dataAccess.connection.password");

            Process process;
            if(Application.isOracleDB()) {
                process = Runtime.getRuntime().exec("sqlplus " + dbUserName + "/" + dbPassword + "@XE @" + new File(new File("").getAbsoluteFile(), "PSE/domain/src/main/sql/InitialData/populate_limit_rule.sql").getAbsolutePath());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.write("exit\n");
                writer.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                //noinspection StatementWithEmptyBody
                while (reader.readLine() != null);
            } else {
                String filePath= new File(new File("").getAbsoluteFile(), "PSE/domain/src/main/sql/postgres/monolith/InitialData/populate_limit_rule.sql").getAbsolutePath();
                String commandString = String.format("psql postgresql://%s:%s@127.0.0.1:5432/psp -f %s",dbUserName,dbPassword,filePath);
                process = Runtime.getRuntime().exec(commandString);
            }

            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSubmitPayments_HappyPath() {

        String sourceCompanyId = "123272727";
        int payeeCount = 5;
        int payrollRunCount = 3;

        // Company with 5 payees.
        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, payeeCount);

        // Run 3 bill payment payrolls 7 days apart.
        createAndSubmitNumerousSuccessfulBillPayments(company, payeeCount, new BigDecimal(150.00), payrollRunCount, 7);

        DomainEntitySet<BillingDetail> billing = Application.find(BillingDetail.class,
                                                          BillingDetail.PayrollRun().Company().equalTo(company)
                                                          .And(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.PerPayment)));
        assertEquals("Billing Count", payrollRunCount, billing.size());
        assertEquals("Billing Quantity", payeeCount, billing.getFirst().getQuantity());
    }

    @Test
    public void testSubmitPayments_ExceedCompanyLimit() {

        String sourceCompanyId = "123272727";

        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, 5);

        // Run 3 bill payment payrolls 7 days apart.
        createAndSubmitNumerousSuccessfulBillPayments(company, 5, new BigDecimal(150.00), 3, 7);

        // Run a single large bill payment (5 payees at $10,000 each).
        SpcfCalendar depositDate = PSPDate.getPSPTime();
        depositDate.addDays(7);
        ProcessResult<Collection<PayrollRun>> result = createAndSubmitBillPayment( company, 5, new BigDecimal(10000.00), depositDate );

        assertFalse(result.isSuccess());
        assertTrue(messagesContains(result.getMessages(), "612", "This payment for company QBDT:123272727, dated 02/15/2013, exceeds the current dollar limit for the company and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>"));

    }

    @Test
    public void updateDefaultsExceed() {
        String sourceCompanyId = "123272727";
        int payeeCount = 5;

        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, payeeCount);

        Application.beginUnitOfWork();
        Application.refresh(company);
        LimitRule.findLimitRule(company, ServiceCode.BillPayment).getLimitValueCollection().find(LimitValue.Name().equalTo(LimitValueType.DefaultCompanyLimit)).getFirst().setValue("400");
        Application.commitUnitOfWork();

        SpcfCalendar depositDate = PSPDate.getPSPTime();
        depositDate.addDays(7);
        ProcessResult<Collection<PayrollRun>> result = createAndSubmitBillPayment( company, 2, new BigDecimal(300.00), depositDate );

        assertFalse(result.isSuccess());
        assertTrue(messagesContains(result.getMessages(), "612", "This payment for company QBDT:123272727, dated 01/22/2013, exceeds the current dollar limit for the company and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>"));
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        CompanyEvent limitViolationEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.LimitViolation));
        assertEquals("400.00", limitViolationEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitAmount));
        assertEquals("Company", limitViolationEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));
        assertEquals("BillPayment", limitViolationEvent.getCompanyEventDetailValue(EventDetailTypeCode.ServiceCode));
        assertEquals("600.00", limitViolationEvent.getCompanyEventDetailValue(EventDetailTypeCode.ViolationAmount));

    }

    @Test
    public void testSubmitPayments_ExceedPayeeLimit() {

        String sourceCompanyId = "123272727";

        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, 5);

        // Run 3 bill payment payrolls 7 days apart.
        createAndSubmitNumerousSuccessfulBillPayments(company, 5, new BigDecimal(150.00), 3, 7);

        // Run a single large bill payment (2 payees at $20,000 each).
        SpcfCalendar depositDate = PSPDate.getPSPTime();
        depositDate.addDays(7);
        ProcessResult<Collection<PayrollRun>> result = createAndSubmitBillPayment( company, 2, new BigDecimal(20000.00), depositDate );

        assertFalse(result.isSuccess());
        assertTrue(messagesContains(result.getMessages(), "613", "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>"));
        assertTrue(messagesContains(result.getMessages(), "613", "This payment for company QBDT:123272727, to payee Payee2 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>"));

    }

    @Test
    public void testSubmitPayments_ExceedCompanyLimit_IncreaseTier1() {

        String sourceCompanyId = "123272727";

        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, 7);

        // Run 7 bill payment payrolls 7 days apart.
        createAndSubmitNumerousSuccessfulBillPayments(company, 7, new BigDecimal(150.00), 7, 7);

        // Run a single large bill payment (5 payees at $10,000 each).
        SpcfCalendar depositDate = PSPDate.getPSPTime();
        depositDate.addDays(7);
        ProcessResult<Collection<PayrollRun>> result = createAndSubmitBillPayment( company, 7, new BigDecimal(10000.00), depositDate );

        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        BPCompanyServiceInfo serviceInfo = getBPCompanyServiceInfo( company );
        assertEquals("77000.00", serviceInfo.getOverrideCompanyLimitAmount().toString());
        assertNull("Payee Limit Null", serviceInfo.getOverridePayeeLimitAmount());
        assertEquals("77000.00", serviceInfo.getCompanyLimit().toString());
        assertEquals("15000.00", serviceInfo.getPayeeLimit().toString());

        DomainEntitySet<CompanyEvent> limitViolations = CompanyEvent.findCompanyEvents(company, EventTypeCode.LimitViolation, CompanyEventStatus.Active, false);
        Assert.assertEquals(0, limitViolations.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmitPayments_ExceedCompanyLimit_NoIncrease() {

        String sourceCompanyId = "123272727";

        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, 10);

        // Run 7 bill payment payrolls 7 days apart.
        createAndSubmitNumerousSuccessfulBillPayments(company, 10, new BigDecimal(150.00), 7, 7);

        // Run a single large bill payment (5 payees at $10,000 each).
        SpcfCalendar depositDate = PSPDate.getPSPTime();
        depositDate.addDays(7);
        ProcessResult<Collection<PayrollRun>> result = createAndSubmitBillPayment( company, 10, new BigDecimal(10000.00), depositDate );

        assertFalse(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        BPCompanyServiceInfo serviceInfo = getBPCompanyServiceInfo( company );
        assertNull("Company Limit Null", serviceInfo.getOverrideCompanyLimitAmount());
        assertNull("Payee Limit Null", serviceInfo.getOverridePayeeLimitAmount());
        assertEquals("40000.00", serviceInfo.getCompanyLimit().toString());
        assertEquals("15000.00", serviceInfo.getPayeeLimit().toString());

        DomainEntitySet<CompanyEvent> limitViolations = CompanyEvent.findCompanyEvents(company, EventTypeCode.LimitViolation, CompanyEventStatus.Active, false);
        Assert.assertEquals(2, limitViolations.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmitPayments_ExceedPayeeLimit_IncreaseTier1() {

        String sourceCompanyId = "123272727";

        Company company = createAssistedCompanyWithBillPayment(sourceCompanyId, 5);

        // Run 7 bill payment payrolls 7 days apart.
        createAndSubmitNumerousSuccessfulBillPayments(company, 5, new BigDecimal(150.00), 7, 7);

        // Run a single large bill payment (2 payees at $15,000 each).
        SpcfCalendar depositDate = PSPDate.getPSPTime();
        depositDate.addDays(7);
        ProcessResult<Collection<PayrollRun>> result = createAndSubmitBillPayment( company, 2, new BigDecimal(15000.00), depositDate );

        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        BPCompanyServiceInfo serviceInfo = getBPCompanyServiceInfo( company );
        assertNotNull("Payee Limit Not Null", serviceInfo.getPayeeLimit());
        PayrollServices.rollbackUnitOfWork();
    }

    private static Company createAssistedCompanyWithBillPayment( String sourceCompanyId, int payeeCount ) {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, sourceCompanyId, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.addBillPaymentService(company);

        PayrollServices.beginUnitOfWork();
        GenerateData.generatePayees(company, payeeCount);
        PayrollServices.commitUnitOfWork();

        return company;
    }

    private static void createAndSubmitNumerousSuccessfulBillPayments( Company company, int payeeCount, BigDecimal payeeAmount, int quantity, int intervalDays ) {
        SpcfCalendar currentDate = PSPDate.getPSPTime();

        for (int i = 0 ; i < quantity ; i++) {
            // Advance the date by the interval requested.
            currentDate.addDays(intervalDays);
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(currentDate);
            PayrollServices.commitUnitOfWork();

            // Create a deposit date 3 days ahead of the current date.
            SpcfCalendar depositDate = currentDate.copy();
            depositDate.addDays(3);

            ProcessResult<Collection<PayrollRun>> submitResult = createAndSubmitBillPayment(company, payeeCount, payeeAmount, depositDate);
            assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

            // Offload the payment
            ACHOffloadRunner.runAchOffload(depositDate.format("yyyyMMdd"), 5);
        }
    }

    private static ProcessResult<Collection<PayrollRun>> createAndSubmitBillPayment( Company company, int payeeCount, BigDecimal payeeAmount, SpcfCalendar depositDate ) {
        PayrollServices.beginUnitOfWork();

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        BillPaymentDTO billPaymentDTO;

        for (int i = 1 ; i <= payeeCount ; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO(depositDate), 1);
            billPaymentDTOs.add(billPaymentDTO);
        }

        for (BillPaymentDTO bpDTO : billPaymentDTOs) {
            BillPaymentSplitDTO split = bpDTO.getPaymentTransactions().iterator().next();
            split.setAmount(payeeAmount);
            bpDTO.setAmount(SpcfUtils.convertToSpcfMoney(payeeAmount));
        }

        ProcessResult<Collection<PayrollRun>> submitResult =
                PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(),
                                                                     company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        return submitResult;
    }

    private static boolean messagesContains( MessageList messages, String errorCode, String message ) {
        for (Message currMessage : messages) {
            if (currMessage.getMessageCode().equals(errorCode)) {
                if (currMessage.getMessage().equals(message)) {
                    return true;
                }
            }
        }
        return false;
    }

    private BPCompanyServiceInfo getBPCompanyServiceInfo( Company company ) {
        return Application.find(BPCompanyServiceInfo.class,
                                BPCompanyServiceInfo.Company().equalTo(company)
                                    .And(BPCompanyServiceInfo.Service().ServiceCd().equalTo(ServiceCode.BillPayment)))
                          .getFirst();
    }
}

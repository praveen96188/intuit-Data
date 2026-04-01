package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static org.junit.Assert.assertTrue;

/**
 * User: dmehta2
 * Date: 07/09/23
 * Time: 3:04 PM
 */
public class CreatePendingTaxRefundCoreTests {

    private static final String psid = "12345678";
    private static final String DefaultGuid = "12345678-8765-4321-1234-567887654321";

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidCompanyParameters() {
        CustomerTaxPaymentDTO dto = new CustomerTaxPaymentDTO();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(null,
                psid, DefaultGuid, dto);
        PayrollServices.commitUnitOfWork();


        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source System Code is not specified.", message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.QBDT,
                null, DefaultGuid, dto);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source Company ID is not specified.", message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(null,
                null, DefaultGuid, dto);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 2);

        // validate error code
        message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source System Code is not specified.", message.getMessage());

        message = processResult.getMessages().get(1);
        Assert.assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source Company ID is not specified.", message.getMessage());

    }

    @Test
    public void testCompanyDoesNotExist() {
        CustomerTaxPaymentDTO dto = new CustomerTaxPaymentDTO();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.QBDT,
                "InvalidCompanyId", DefaultGuid, dto);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Company QBDT:InvalidCompanyId does not exist.", message.getMessage());

    }

    @Test
    public void testNullCustomerTaxPaymentDTO() {

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.QBDT,
                "InvalidCompanyId", DefaultGuid, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "5002", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Required 'CustomerTaxPaymentDTO' input is missing or blank", message.getMessage());

    }

    @Test
    public void testNullPaymentId() {

        MoneyMovementTransaction mmt = createTestDataForRefund();
        CustomerTaxPaymentDTO dto = createCustomerTaxPaymentDTO(mmt);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.QBDT,
                psid, null, dto);
        PayrollServices.commitUnitOfWork();


        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "5002", message.getMessageCode());
        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Required 'paymentId' input is missing or blank", message.getMessage());
    }

    @Test
    public void testInValidTransaction() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String[] statesList = new String[]{"AL", "IA", "NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AL-CR4WH-PAYMENT").setCompany(companies.get(0)).find());
        PayrollServices.rollbackUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        SpcfCalendar mPaymentDate = PSPDate.getPSPTime();
        SpcfCalendar mmtDate = mmt.getPaymentPeriodEnd();

        CustomerTaxPaymentDTO customerTaxPaymentDTO = new CustomerTaxPaymentDTO();
        customerTaxPaymentDTO.setApplyPayments(true);
        customerTaxPaymentDTO.setMemo("Test memo");
        customerTaxPaymentDTO.setPaymentDate(new DateDTO(mPaymentDate));
        customerTaxPaymentDTO.setPaymentTemplateId(mmt.getPaymentTemplate().getPaymentTemplateCd());
        customerTaxPaymentDTO.setYear(mmtDate.getYear());
        customerTaxPaymentDTO.setQuarter(CalendarUtils.getQuarterAsInt(mmtDate));
        customerTaxPaymentDTO.setImmediateCredit(true);
        customerTaxPaymentDTO.setPaymentAmounts(new HashMap<String, BigDecimal>());

        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            if (ft.getFinancialTransactionAmount().isGreaterThan(SpcfMoney.ZERO)) {
                customerTaxPaymentDTO.getPaymentAmounts().put(ft.getLaw().getLawId(), SpcfUtils.convertToBigDecimal(ft.getFinancialTransactionAmount()));
            }
        }

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction otherMMT = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AL-CR4WH-PAYMENT").setCompany(companies.get(1)).find());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.QBDT,
                companies.get(0).getSourceCompanyId(), otherMMT.getId().toString(), customerTaxPaymentDTO);
        PayrollServices.commitUnitOfWork();

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "195", message.getMessageCode());
    }


    @Test
    public void testTaxPaymentAmountNotCollected() {
        String[] states = {"CA"};
        long psid = 12345678l;
        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(PSPDate.getPSPTime());
        Company company = assertOne(DataLoadServices.setupCompany(psid, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2014, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO(2014, 1, 5), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CA-UIETT-PAYMENT").find());
        PayrollServices.rollbackUnitOfWork();

        CustomerTaxPaymentDTO dto = createCustomerTaxPaymentDTO(mmt);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.QBDT,
                company.getSourceCompanyId(), mmt.getId().toString(), dto);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "12018", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Transaction " + mmt.toString() + " cannot be refunded, because related Tax Payment Amount is not Collected or refund amount is not matching with the collected amount.", message.getMessage());
    }

    public MoneyMovementTransaction createTestDataForRefund(){
        String[] states = {"CA"};
        long psid = 12345678l;
        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(PSPDate.getPSPTime());
        Company company = assertOne(DataLoadServices.setupCompany(psid, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2014, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO(2014, 1, 5), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CA-UIETT-PAYMENT").find());
        PayrollServices.rollbackUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2014, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        return mmt;
    }

    public CustomerTaxPaymentDTO createCustomerTaxPaymentDTO(MoneyMovementTransaction mmt) {
        SpcfCalendar mPaymentDate = PSPDate.getPSPTime();
        SpcfCalendar mmtDate = mmt.getPaymentPeriodEnd();

        CustomerTaxPaymentDTO dto = new CustomerTaxPaymentDTO();
        dto.setApplyPayments(true);
        dto.setMemo("Test memo");
        dto.setPaymentDate(new DateDTO(mPaymentDate));
        dto.setPaymentTemplateId(mmt.getPaymentTemplate().getPaymentTemplateCd());
        dto.setYear(mmtDate.getYear());
        dto.setQuarter(CalendarUtils.getQuarterAsInt(mmtDate));
        dto.setImmediateCredit(true);
        dto.setPaymentAmounts(new HashMap<String, BigDecimal>());

        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            if (ft.getFinancialTransactionAmount().isGreaterThan(SpcfMoney.ZERO)) {
                dto.getPaymentAmounts().put(ft.getLaw().getLawId(), SpcfUtils.convertToBigDecimal(ft.getFinancialTransactionAmount()));
            }
        }

        return dto;
    }
}

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 19, 2011
 * Time: 4:24:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateInitiationDateCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }
    
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testUpdateInitDateToPSPdateBefore5() {
        prepareData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));        
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone());
        newInitDate.setValues(newInitDate.getYear(),newInitDate.getMonth(),newInitDate.getDay(),16,59,59,0);
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 01, 20, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPS));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), newInitDate);
        ProcessResult<MoneyMovementTransaction> updateProcessResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction moneyMovementTransaction = updateProcessResult.getResult();
        Assert.assertEquals("Initiation Date", newInitDate, moneyMovementTransaction.getInitiationDate());
        SpcfCalendar newSettlementDateWithoutTime = newSettlementDate.copy();
        CalendarUtils.clearTime(newSettlementDateWithoutTime);
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            Assert.assertEquals("Settlement Date", newSettlementDateWithoutTime, financialTransaction.getSettlementDate());
        }
    }

    @Test
    public void testUpdateInitDateToPSPdateAfter5() {
        prepareData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone());
        newInitDate.setValues(newInitDate.getYear(),newInitDate.getMonth(),newInitDate.getDay(),17,0,0,0);
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 01, 20, SpcfTimeZone.getLocalTimeZone());
        newSettlementDate.setValues(newSettlementDate.getYear(),newSettlementDate.getMonth(),newSettlementDate.getDay(),17,0,0,0);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPS));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), newInitDate);
        ProcessResult<MoneyMovementTransaction> updateProcessResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction moneyMovementTransaction = updateProcessResult.getResult();
        Assert.assertEquals("Initiation Date", newInitDate, moneyMovementTransaction.getInitiationDate());
        SpcfCalendar newSettlementDateWithoutTime = newSettlementDate.copy();
        CalendarUtils.clearTime(newSettlementDateWithoutTime);
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            Assert.assertEquals("Settlement Date", newSettlementDateWithoutTime, financialTransaction.getSettlementDate());
        }
    }

    @Test
    public void testHappyPath() {
        prepareData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 24, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 01, 25, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPS));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), newInitDate);
        ProcessResult<MoneyMovementTransaction> updateProcessResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction moneyMovementTransaction = updateProcessResult.getResult();
        Assert.assertEquals("Initiation Date", newInitDate, moneyMovementTransaction.getInitiationDate());
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            Assert.assertEquals("Settlement Date", newSettlementDate, financialTransaction.getSettlementDate());
        }

    }

    private ProcessResult<PayrollRun> prepareData() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61"}, new String[]{"12500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return processResult;
    }

    @Test
    public void testWeekEndSettlementDate() {
        prepareData();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 28, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 01, 31, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPS));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), newInitDate);
        ProcessResult<MoneyMovementTransaction> processResult = updateInitiationDateCoreTests.execute();
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        MoneyMovementTransaction moneyMovementTransaction = processResult.getResult();
        Assert.assertEquals("Initiation Date", newInitDate, moneyMovementTransaction.getInitiationDate());
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            Assert.assertEquals("Settlement Date", newSettlementDate, financialTransaction.getSettlementDate());
        }
    }

    @Test
    public void testBeforeOffloadDate() {
        prepareData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 11, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPS));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), newInitDate);
        ProcessResult<MoneyMovementTransaction> processResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Process success", false, processResult.isSuccess());
        Assert.assertEquals("Error message", 1, processResult.getErrorMessages().size());
        Assert.assertEquals("Error message code", "10107", processResult.getErrorMessages().get(0).getMessageCode());

    }

    @Test
    public void testWithInvalidPaymentMethod() {
        prepareData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 21, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.ACHDirectDeposit));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), newInitDate);
        ProcessResult<MoneyMovementTransaction> processResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Process success", false, processResult.isSuccess());
        Assert.assertEquals("Error message", 1, processResult.getErrorMessages().size());
        Assert.assertEquals("Error message code", "10108", processResult.getErrorMessages().get(0).getMessageCode());

    }

    @Test
    public void testWithNullInitDate() {
        prepareData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPS));

        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(moneyMovementTransactions.get(0).getId().toString(), null);
        ProcessResult<MoneyMovementTransaction> processResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals("Process success", false, processResult.isSuccess());
        Assert.assertEquals("Error message", 1, processResult.getErrorMessages().size());

    }

    @Test
    public void testWithInvalidMMT() {

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 28, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        UpdateInitiationDateCore updateInitiationDateCoreTests = new UpdateInitiationDateCore(null, newInitDate);
        ProcessResult<MoneyMovementTransaction> processResult = updateInitiationDateCoreTests.execute();
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Process success", false, processResult.isSuccess());
        Assert.assertEquals("Error message", 1, processResult.getErrorMessages().size());
    }
}

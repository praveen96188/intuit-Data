/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/PayrollSubmitDDTests.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.util.AchReturnAccountingFile;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class PayrollSubmitDDTests {
    private PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();
        PayrollSubmitDD psd = new PayrollSubmitDD(null, SourceSystemCode.QBOE, null);
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();

        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullSourceSystem() {
        Application.beginUnitOfWork();
        PayrollSubmitDD psd = new PayrollSubmitDD(null, null, "1234567");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testCompanyDNE() {
        Application.beginUnitOfWork();
        PayrollSubmitDD psd = new PayrollSubmitDD(null, SourceSystemCode.QBOE, "1234567");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testDDTxnPresent_OneDDTxn() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollSubmitDD psd = new PayrollSubmitDD(payrollRunDTO, SourceSystemCode.QBOE, "123272727");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());
    }

    @Test
    public void testDDTxnPresent_NoDDTxns() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            Collection<DDTransactionDTO> ddtxns = currPaycheck.getDdTransactions();
            currPaycheck.getDdTransactions().removeAll(ddtxns);
        }

        PayrollSubmitDD psd = new PayrollSubmitDD(payrollRunDTO, SourceSystemCode.QBOE, "123272727");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();

        assertTrue(submitDDPayroll.isSuccess());
    }

    @Test
    public void testDDTxnPresent_AfterCutoffNoDDService() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company companyWithNoDD = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyService ddService = CompanyService
                .findCompanyService(companyWithNoDD, ServiceCode.DirectDeposit);
        // We have to delete events because they may have a reference to the company service
        DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class);
        for (CompanyEvent companyEvent : companyEvents) {
            DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetailCollection();
            for(CompanyEventDetail detail : companyEventDetails){
                Application.delete(detail);
            }
            //Deleting CompanyEventEmail and CompanyEventEmailParam entries manually, as without this, we're getting an error
            DomainEntitySet<CompanyEventEmail> companyEventEmails =companyEvent.getCompanyEventEmailCollection();
            for(CompanyEventEmail email : companyEventEmails){
                DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams =Application.find(CompanyEventEmailParam.class, CompanyEventEmailParam.CompanyEventEmail().equalTo(email));
                for(CompanyEventEmailParam param : companyEventEmailParams){
                    Application.delete(param);
                }
                Application.delete(email);
            }
            Application.delete(companyEvent);
        }
        companyWithNoDD.removeCompanyService(ddService);
        ddService.setCompany(null);
        Application.save(companyWithNoDD);
        Application.delete(ddService);

        //Set the system date well after the requirements for cutoff (4 days after)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollSubmitDD psd = new PayrollSubmitDD(payrollRunDTO, SourceSystemCode.QBOE, "123272727");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();

        assertFalse(submitDDPayroll.isSuccess());
        assertEquals("Error message size ", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);

        assertEquals("Error message code", "1010", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:123272727 is not associated with the DD service.",
                errorMessage.getMessage());
    }

    @Test
    public void testCheckServiceStatus_NoDDService() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        Company companyWithNoDD = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyService ddService = CompanyService
                .findCompanyService(companyWithNoDD, ServiceCode.DirectDeposit);
        // We have to delete events because they may have a reference to the company service
        DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class);
        for (CompanyEvent companyEvent : companyEvents) {
            DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetailCollection();
            for(CompanyEventDetail detail : companyEventDetails){
                Application.delete(detail);
            }
            //Deleting CompanyEventEmail and CompanyEventEmailParam entries manually, as without this, we're getting an error
            DomainEntitySet<CompanyEventEmail> companyEventEmails =companyEvent.getCompanyEventEmailCollection();
            for(CompanyEventEmail email : companyEventEmails){
                DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams =Application.find(CompanyEventEmailParam.class, CompanyEventEmailParam.CompanyEventEmail().equalTo(email));
                for(CompanyEventEmailParam param : companyEventEmailParams){
                    Application.delete(param);
                }
                Application.delete(email);
            }
            Application.delete(companyEvent);
        }
        companyWithNoDD.removeCompanyService(ddService);
        ddService.setCompany(null);
        Application.save(companyWithNoDD);
        Application.delete(ddService);

        PayrollSubmitDD psd = new PayrollSubmitDD(payrollRunDTO, SourceSystemCode.QBOE, "123272727");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();

        assertFalse(submitDDPayroll.isSuccess());
        assertEquals("Error message size ", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);

        assertEquals("Error message code", errorMessage.getMessageCode(), "1010");
        assertEquals("Error message",
                "Company QBOE:123272727 is not associated with the DD service.",
                errorMessage.getMessage());
    }

    @Test
    public void testCheckServiceStatus_DDServiceNotActive() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        Company companyWithNoDD = Company.findCompany("123272727", SourceSystemCode.QBOE);

        DeactivateServiceCore cancelServiceCoreProcess = new DeactivateServiceCore(SourceSystemCode.QBOE,
                companyWithNoDD.getSourceCompanyId(), ServiceCode.DirectDeposit);
        ProcessResult cancelServiceProcessResult = cancelServiceCoreProcess.execute();
        assertTrue(cancelServiceProcessResult.isSuccess());

        PayrollSubmitDD psd = new PayrollSubmitDD(payrollRunDTO, SourceSystemCode.QBOE, "123272727");
        ProcessResult submitDDPayroll = psd.validate();
        Application.commitUnitOfWork();
        System.out.println(submitDDPayroll);

        assertFalse(submitDDPayroll.isSuccess());
        assertEquals("Error message size ", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);

        assertEquals("Error message code", "1100", errorMessage.getMessageCode());
        assertEquals("Error message",
                "The operation SubmitPayroll for service DirectDeposit is not allowed for company QBOE:123272727 in its current state.",
                errorMessage.getMessage());
    }

    @Test
    public void testCloudDD_RunPayroll() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
        payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCloudDDCombined_RunPayroll() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // submit DD payroll
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        ArrayList<Employee> cloudEmployees = new ArrayList<Employee>(company.getCloudEmployees());
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), cloudEmployees);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        cloudEmployees = new ArrayList<Employee>(company.getCloudEmployees());
        ArrayList<CompanyPayrollItem> companyPayrollItems = new ArrayList<CompanyPayrollItem>(company.getCompanyPayrollItemCollection());

        PayrollRunDTO updatedPayrollRunDTO = DataLoadServices.create401kPayrollRun(cloudEmployees, companyPayrollItems);
        updatedPayrollRunDTO.setPayrollTXBatchId(payrollRun.getSourcePayRunId());
        // there is only one paycheck
        for (PaycheckDTO paycheckDTO : updatedPayrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        }
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        ProcessResult submitCloudProcessResult = PayrollServices.payrollManager
                .updatePayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun, updatedPayrollRunDTO.getPaychecks());

        assertSuccess(submitCloudProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DataLoadServices.assertPayrollsEqual(updatedPayrollRunDTO, PayrollStatus.Complete, payrollRun);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPayrollSubmit_CloudDDBP401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);

        // submit DD payroll
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
        payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);

        PayrollServices.rollbackUnitOfWork();

        // submit 401k payroll
        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);
        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = DataLoadServices.create401kPayrollRun(employees, companyPayrollItems);
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit 401k Payroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // submit bp payment
        List<Payee> payees = DataLoadServices.addPayees(company, 1);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        ProcessResult<Collection<PayrollRun>> submitBPPayroll = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PSP_PRAssert.assertSuccess("submit BP Payroll", submitBPPayroll);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertBillPaymentsEqual(company, billPaymentDTOs);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVendorReturnedPayments_PayrollRunStatus() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.addBillPaymentService(company);

        // submit bp payment
        List<Payee> payees = DataLoadServices.addPayees(company, 2);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        ProcessResult<Collection<PayrollRun>> submitBPPayroll = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PSP_PRAssert.assertSuccess("submit BP Payroll", submitBPPayroll);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertBillPaymentsEqual(company, billPaymentDTOs);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.BillPayment);
        assertEquals("Number of bill payment payrolls", 1, payrollRuns.size());
        PayrollRun payrollRun = payrollRuns.get(0);
        assertEquals("Bill payment payroll run status", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertEquals("Bill payment payroll run status", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(payrollRun.getDdDebit());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2007, 8, 23);
        DataLoadServices.returnTxns(financialTransactions);

        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertEquals("Bill payment PayrollRun New status", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testDDPayrollWith1EEUsingAPayCardAccountHasNoFees() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(0);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        Employee ee = assertOne(company.getDirectDepositEmployees());

        EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(ee.getEmployeeBankAccountCollection()));
        ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
        ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
        assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), ee.getSourceEmployeeId(), ebaDTO));
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        ees.add(ee);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();


        assertEquals(SpcfDecimal.createInstance("0.00"), payrollRun.getFeeReceivableAmount());

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("1", ced.getValue());

    }
    @Test
    public void testDDPayrollUsingAPayCardAccountHasNoFeesWitNewRoutingNumber() {

        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(2);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(2);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 5, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Employee ee = assertOne(company.getDirectDepositEmployees());

        EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(ee.getEmployeeBankAccountCollection()));
        ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
        ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
        assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), ee.getSourceEmployeeId(), ebaDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 5, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        ees.add(ee);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                               payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();


        assertEquals(SpcfDecimal.createInstance("0.00"), payrollRun.getFeeReceivableAmount());

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("1", ced.getValue());

    }

    @Test
    public void testDDPayrollWith1EESplitBetweenAPayCardAndNormalCheckingAccountHasFees() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(1);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        Employee ee = assertOne(company.getDirectDepositEmployees());
        PayrollServices.commitUnitOfWork();

        EmployeeBankAccount newEba = DataLoadServices.addEEBankAccount(company, ee, BankAccountType.Checking);

        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(newEba);
        ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
        ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
        assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), ee.getSourceEmployeeId(), ebaDTO));
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        ees.add(ee);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();


        assertEquals(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(), payrollRun.getFeeReceivableAmount());
        assertEquals("no PayCard event", 0, CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false).size());

    }

    @Test
    public void testDDPayrollWith1EEUsingAPayCardAccountAnd1EEUsingACheckingAccountHasFeesOnlyForOneEE() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(0);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        Employee checkingEE = DataLoadServices.addEE(company, DataLoadServices.createEE(), true);

        PayrollServices.beginUnitOfWork();
        Employee ee = assertOne(company.getDirectDepositEmployees().find(Employee.Id().notEqualTo(checkingEE.getId())));

        EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(ee.getEmployeeBankAccountCollection()));
        ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
        ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
        assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), ee.getSourceEmployeeId(), ebaDTO));
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        ees.add(ee);
        ees.add(checkingEE);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();


        assertEquals(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(), payrollRun.getFeeReceivableAmount());

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("1", ced.getValue());
    }


    @Test
    public void testDDPayrollWithPayCardAccountAndBankAccount_Cancel_PSRV002376() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(0);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEEs(company, 2, true, false);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> directDepositEmpls = company.getDirectDepositEmployees();
        assertEquals("Direct Deposit Employees ", 3, directDepositEmpls.size());
        for(int i = 0; i < 2; i++){
            EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(directDepositEmpls.get(i).getEmployeeBankAccountCollection()));
            ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
            ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
            assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), directDepositEmpls.get(i).getSourceEmployeeId(), ebaDTO));
        }
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        directDepositEmpls = company.getDirectDepositEmployees();
        assertEquals("Direct Deposit Employees ", 3, directDepositEmpls.size());
        for (Employee directDepositEmpl : directDepositEmpls) {
            ees.add(directDepositEmpl);
        }
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("2", ced.getValue());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Created), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 1, erFeeDbts.size());
        FinancialTransaction erFeeDb = erFeeDbts.get(0);
        assertEquals("ER Fee Debit Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(), erFeeDb.getFinancialTransactionAmount());
        List<String> txCancelList = new LinkedList<String>();
        Paycheck payCheckWithPaycard = null;
        Paycheck nonPaycardPaycheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if(paycheck.isPayCardPaycheck()){
                payCheckWithPaycard = paycheck;
            } else {
                nonPaycardPaycheck = paycheck;
            }
        }
        txCancelList.add(payCheckWithPaycard.getSourcePaycheckId());
        TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
        txRecallDTO.setSourcePaycheckIdList(txCancelList);
        txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
        ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallPR);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Cancelled), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 1, erFeeDbts.size());
        assertEquals("ER Fee Debit Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(), erFeeDbts.get(0).getFinancialTransactionAmount());
        erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Created), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 1, erFeeDbts.size());
        assertEquals("ER Fee Debit Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(), erFeeDbts.get(0).getFinancialTransactionAmount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        txCancelList = new LinkedList<String>();
        txCancelList.add(nonPaycardPaycheck.getSourcePaycheckId());
        txRecallDTO = new TransactionCancelEEDTO();
        txRecallDTO.setSourcePaycheckIdList(txCancelList);
        txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
        recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallPR);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Cancelled), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 2, erFeeDbts.size());
        erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Created), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 0, erFeeDbts.size());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testDDPayrollWithPayCardAccountAndBankAccount_Cancel_PSRV002376_2() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(0);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        List<Employee> employees = DataLoadServices.addEEs(company, 3, true, false);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> directDepositEmpls = company.getDirectDepositEmployees();
        assertEquals("Direct Deposit Employees ", 4, directDepositEmpls.size());
        for(int i = 0; i < 2; i++){
            EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(directDepositEmpls.get(i).getEmployeeBankAccountCollection()));
            ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
            ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
            assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), directDepositEmpls.get(i).getSourceEmployeeId(), ebaDTO));
        }
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        directDepositEmpls = company.getDirectDepositEmployees();
        assertEquals("Direct Deposit Employees ", 4, directDepositEmpls.size());
        for (Employee directDepositEmpl : directDepositEmpls) {
            ees.add(directDepositEmpl);
        }
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("2", ced.getValue());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Created), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 1, erFeeDbts.size());
        FinancialTransaction erFeeDb = erFeeDbts.get(0);
        assertEquals("ER Fee Debit Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance("2")), erFeeDb.getFinancialTransactionAmount());
        List<String> txCancelList = new LinkedList<String>();
        Paycheck payCheckWithPaycard = null;
        Paycheck nonPaycardPaycheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if(paycheck.isPayCardPaycheck()){
                payCheckWithPaycard = paycheck;
            } else {
                nonPaycardPaycheck = paycheck;
            }
        }
        txCancelList.add(nonPaycardPaycheck.getSourcePaycheckId());
        TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
        txRecallDTO.setSourcePaycheckIdList(txCancelList);
        txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
        ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
        assertSuccess(recallPR);

        txCancelList = new LinkedList<String>();
        txCancelList.add(payCheckWithPaycard.getSourcePaycheckId());
        txRecallDTO = new TransactionCancelEEDTO();
        txRecallDTO.setSourcePaycheckIdList(txCancelList);
        txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
        recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallPR);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Cancelled), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 2, erFeeDbts.size());
        erFeeDbts = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Created), TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit));
        assertEquals("ER Fee Debits ", 1, erFeeDbts.size());
        assertEquals("ER Fee Debit Amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(), erFeeDbts.get(0).getFinancialTransactionAmount());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testDDPayrollWith1EEReturned_SubmitSecondPayroll() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", "123456789", true, ServiceCode.DirectDeposit);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        // Submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                               payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        // Return the EE DD Credit.
        PayrollRun payrollRun = processResult.getResult();
        DomainEntitySet<FinancialTransaction> txs = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployeeDdCredit);
        BankAccount returnedBankAccount = assertOne(txs).getCreditBankAccount();
        DataLoadServices.returnTxns(txs, "R02", "Non-NSF Return");

        // Submit another DD payroll with this employee.
        PayrollServices.beginUnitOfWork();
        checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        // Should be a failure here.
        assertFalse("Submit DD Payroll Failure", processResult.isSuccess());

        // Verify Company Event was created.
        DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class, CompanyEvent.EventTypeCd().equalTo(EventTypeCode.PayrollSubmittedWithEmployeeWithPendingReturn));
        assertEquals("Pending Return Events", 1, companyEvents.size());

        PayrollServices.commitUnitOfWork();

        // Add a bank account.
        PayrollServices.beginUnitOfWork();
        checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        PayrollServices.commitUnitOfWork();

        Employee employee = employeeList.get(0);
        EmployeeBankAccount eba = DataLoadServices.addEEBankAccount(company, employee, BankAccountType.Savings);
        Collection<PaycheckDTO> checks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO check : checks) {
            List<DDTransactionDTO> ddTrans = check.getDdTransactions();
            ddTrans.add(0, DataLoadServices.createDDTransactionDTO(DataLoadServices.createEmployeeBankAccount(eba), new BigDecimal(20)));
            check.setPaycheckNetAmount(new SpcfMoney("21"));
        }

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Should be another failure.
        assertFalse("Submit DD Payroll Failure after adding account", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);
        PayrollServices.commitUnitOfWork();

        // Should be one open Transaction Returns.
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txReturns = TransactionReturn.findTxnRetsForReturnType(company, TransactionReturn.ReturnTypeCodes.RETURN, TransactionReturnStatusCode.Open);
        assertEquals("One Open Transaction Return", 1, txReturns.size());
        assertEquals("Open R02", "R02", txReturns.getFirst().getBankReturnCd());

        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        // Change the bank account number for the checking account that had the ori
        // ginal return.
        checks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO check : checks) {
            for (DDTransactionDTO ddTrans : check.getDdTransactions()) {
                BankAccountDTO baDTO = ddTrans.getEmployeeBankAccount().getBankAccount();
                // Change the checking account that had the original return.
                if (returnedBankAccount.getAccountNumber().equals(baDTO.getAccountNumber())) {
                    baDTO.setAccountNumber("34987343459847");
                }
            }
        }
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        assertTrue("Submit DD Payroll Success", processResult.isSuccess());

        // Verify there are no open Transaction Returns.
        txReturns = TransactionReturn.findTxnRetsForReturnType(company, TransactionReturn.ReturnTypeCodes.RETURN, TransactionReturnStatusCode.Open);
        assertEquals("No Open Transaction Returns", 0, txReturns.size());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testVoidThreshold() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2012, 12, 15);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 7, 14, 19, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        String[] laws = {"1", "6"};
        String[] amounts = new String[]{"100000.00", "66.66"};
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-10-09"), new ArrayList<Employee>(company.getCloudEmployees()), laws, amounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollRun voidedPayrollRun = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 7, 17, 19, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 8, 12, 50, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        amounts = new String[]{"555.00", "10.66"};
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-10-10"), new ArrayList<Employee>(company.getCloudEmployees()), laws, amounts);
        ProcessResult<PayrollRun> subsPayrollProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(subsPayrollProcessResult);
        PayrollRun subsPayrollRun = subsPayrollProcessResult.getResult();
        DomainEntitySet<FinancialTransaction> subsPayrollATCFTSet = subsPayrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit);
        DomainEntitySet<FinancialTransaction> subsPayrollETDFTSet = subsPayrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit);
        PayrollServices.commitUnitOfWork();
        //Verifying ATCs
        assertEquals(2, subsPayrollATCFTSet.size());
        //ATC of 550*2 for Law 1 - 550 each for 2 employees
        FinancialTransaction ATCForLaw1 = subsPayrollATCFTSet.find(FinancialTransaction.Law().LawId().equalTo("1")).get(0);
        assertEquals(new SpcfMoney("555.00").multiply(new SpcfMoney("2")), ATCForLaw1.getFinancialTransactionAmount());
        assertEquals(new SpcfMoney("555.00").multiply(new SpcfMoney("2")), ATCForLaw1.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        //ATC of 10.66*2 for Law 6
        FinancialTransaction ATCForLaw6 = subsPayrollATCFTSet.find(FinancialTransaction.Law().LawId().equalTo("6")).get(0);
        assertEquals(new SpcfMoney("10.66").multiply(new SpcfMoney("2")), ATCForLaw6.getFinancialTransactionAmount());
        //Verifying ETDs
        // 555*2 + 10.66*2 = 1131.32
        assertEquals(new SpcfMoney("1131.32"), subsPayrollETDFTSet.get(0).getFinancialTransactionAmount());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 8, 13, 30, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 8, 17, 19, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 9, 3, 35, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 9, 12, 45, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.voidAPaycheck(voidedPayrollRun);

        //Verify transactions
        PayrollServices.beginUnitOfWork();
        //Refresh data
        voidedPayrollRun = Application.refresh(voidedPayrollRun);
        subsPayrollRun = Application.refresh(subsPayrollRun);
        subsPayrollATCFTSet = subsPayrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit);
        ATCForLaw1 = subsPayrollATCFTSet.find(FinancialTransaction.Law().LawId().equalTo("1")).get(0);
        //Verify that MMT related to ATC FT is now zero
        assertEquals(new SpcfMoney("0"), ATCForLaw1.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        //Verify that ATD of 1100 is created in this MMT
        FinancialTransaction ATDForLaw1CreatedInVoidedPayroll = ATCForLaw1.getMoneyMovementTransaction().
                getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().
                equalTo(TransactionTypeCode.AgencyTaxDebit)).get(0);
        //Tax for 2 emp 555.00*2 = 1110.00
        assertEquals(new SpcfMoney("1110.00"), ATDForLaw1CreatedInVoidedPayroll.getFinancialTransactionAmount());
        //Verify OverPayment created in the voided payroll for the remaining amount
        assertEquals(new SpcfMoney("100000.00").subtract(new SpcfMoney("1110.00")),
                     voidedPayrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxOverpayment)
                                     .find(FinancialTransaction.Law().LawId().equalTo("1")).get(0).
                             getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDDPayrollCountInBannerAndAccountFile()  {
        {
            // offload & return the EmployerDdDebit
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20141128000000");
            Application.commitUnitOfWork();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

            // submit DD payroll
            List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

            PayrollServices.beginUnitOfWork();
            SpcfCalendar checkDate = PSPDate.getPSPTime();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            ArrayList<Employee> cloudEmployees = new ArrayList<Employee>(company.getCloudEmployees());
            checkDate.addDays(2);

            PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), cloudEmployees);

            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
            PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            cloudEmployees = new ArrayList<Employee>(company.getCloudEmployees());
            ArrayList<CompanyPayrollItem> companyPayrollItems = new ArrayList<CompanyPayrollItem>(company.getCompanyPayrollItemCollection());

            PayrollRunDTO updatedPayrollRunDTO = DataLoadServices.create401kPayrollRun(cloudEmployees, companyPayrollItems);
            updatedPayrollRunDTO.setPayrollTXBatchId(payrollRun.getSourcePayRunId());
            // there is only one paycheck
            for (PaycheckDTO paycheckDTO : updatedPayrollRunDTO.getPaychecks()) {
                paycheckDTO.setPaycheckId(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
            }
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            ProcessResult submitCloudProcessResult = PayrollServices.payrollManager
                                                                    .updatePayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun, updatedPayrollRunDTO.getPaychecks());

            assertSuccess(submitCloudProcessResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

            DataLoadServices.assertPayrollsEqual(updatedPayrollRunDTO, PayrollStatus.Complete, payrollRun);
            PayrollServices.rollbackUnitOfWork();

            // offload & return the EmployerDdDebit
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20141128050000");
            Application.commitUnitOfWork();
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            // offload & return the EmployerDdDebit
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20141201000000");
            Application.commitUnitOfWork();

            company =DataLoadServices.refreshCompany(company);
            long count = company.getPayrollCount();
            junit.framework.Assert.assertEquals("Payroll count is not matching", 1, count);
            PayrollServices.beginUnitOfWork();

            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerDdDebit);
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                        .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                        .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
            transactionReturnBatch = Application.save(transactionReturnBatch);

            for (FinancialTransaction financialTx : finTxs) {
                TransactionReturn transactionReturn = new TransactionReturn();

                transactionReturn.setBankReturnCd("R01");
                transactionReturn.setBankReturnDescription("Insufficient funds");
                transactionReturn.setReturnBatch(transactionReturnBatch);
                transactionReturn.setBankReturnTraceNumber(12345678);
                transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
                transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                transactionReturn.setCompany(financialTx.getCompany());
                Application.save(transactionReturn);
            }

            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            transactionReturnBatch = Application.save(transactionReturnBatch);

            SpcfUniqueId batchId = transactionReturnBatch.getId();
            Application.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(batchId);
        /*  First test the debit returns file   */
            PayrollServices.beginUnitOfWork();
            List<File> rejectFiles = AchReturnAccountingFile.createFile(batchId);
            PayrollServices.commitUnitOfWork();
            junit.framework.Assert.assertEquals("Incorrect number of return files", 1, rejectFiles.size());
            assertTrue("ACH returns accounting files do not match", hasPayrollCountInFileMatching(rejectFiles.get(0), count));

        }
    }

    @Test
    public void testDDVMPPayrollCountInBannerAndAccountFile()  {
        {
            // offload & return the EmployerDdDebit
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20141128000000");
            Application.commitUnitOfWork();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit,ServiceCode.ViewMyPaycheck);

            // submit DD payroll
            List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

            PayrollServices.beginUnitOfWork();
            SpcfCalendar checkDate = PSPDate.getPSPTime();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            ArrayList<Employee> cloudEmployees = new ArrayList<Employee>(company.getCloudEmployees());
            checkDate.addDays(2);

            PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), cloudEmployees);

            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
            PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
            PayrollServices.commitUnitOfWork();
            Application.beginUnitOfWork();

            VmpTestUtil.createPaystub(company.getEmployees().getFirst(), "1000.00", checkDate);
            Application.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            cloudEmployees = new ArrayList<Employee>(company.getCloudEmployees());
            ArrayList<CompanyPayrollItem> companyPayrollItems = new ArrayList<CompanyPayrollItem>(company.getCompanyPayrollItemCollection());

            PayrollRunDTO updatedPayrollRunDTO = DataLoadServices.create401kPayrollRun(cloudEmployees, companyPayrollItems);
            updatedPayrollRunDTO.setPayrollTXBatchId(payrollRun.getSourcePayRunId());
            // there is only one paycheck
            for (PaycheckDTO paycheckDTO : updatedPayrollRunDTO.getPaychecks()) {
                paycheckDTO.setPaycheckId(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
            }
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            ProcessResult submitCloudProcessResult = PayrollServices.payrollManager
                                                                    .updatePayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun, updatedPayrollRunDTO.getPaychecks());

            assertSuccess(submitCloudProcessResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

            DataLoadServices.assertPayrollsEqual(updatedPayrollRunDTO, PayrollStatus.Complete, payrollRun);
            PayrollServices.rollbackUnitOfWork();

            // offload & return the EmployerDdDebit
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20141128050000");
            Application.commitUnitOfWork();
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            // offload & return the EmployerDdDebit
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20141201000000");
            Application.commitUnitOfWork();

            company =DataLoadServices.refreshCompany(company);
            long count = company.getPayrollCount();
            junit.framework.Assert.assertEquals("Payroll count is not matching", 1, count);
            PayrollServices.beginUnitOfWork();

            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerDdDebit);
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                        .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                        .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
            transactionReturnBatch = Application.save(transactionReturnBatch);

            for (FinancialTransaction financialTx : finTxs) {
                TransactionReturn transactionReturn = new TransactionReturn();

                transactionReturn.setBankReturnCd("R01");
                transactionReturn.setBankReturnDescription("Insufficient funds");
                transactionReturn.setReturnBatch(transactionReturnBatch);
                transactionReturn.setBankReturnTraceNumber(12345678);
                transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
                transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                transactionReturn.setCompany(financialTx.getCompany());
                Application.save(transactionReturn);
            }

            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            transactionReturnBatch = Application.save(transactionReturnBatch);

            SpcfUniqueId batchId = transactionReturnBatch.getId();
            Application.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(batchId);
        /*  First test the debit returns file   */
            PayrollServices.beginUnitOfWork();
            List<File> rejectFiles = AchReturnAccountingFile.createFile(batchId);
            PayrollServices.commitUnitOfWork();
            junit.framework.Assert.assertEquals("Incorrect number of return files", 1, rejectFiles.size());
            assertTrue("ACH returns accounting files do not match", hasPayrollCountInFileMatching(rejectFiles.get(0), count));

        }
    }

    public static boolean hasPayrollCountInFileMatching(File pLhsFile, long count) {
        return compareCSVFileFieldValues(pLhsFile,count,57);
    }

    /**
     *
     * @param pCSVFile
     * @param count
     * @param fieldIndexToCompare
     * @return
     * fieldIndexToCompare will start with 0
     */
    public static boolean compareCSVFileFieldValues(File pCSVFile,long count,int fieldIndexToCompare) {
        try {
            BufferedReader reader;
            boolean countMatch =false;
            StringWriter lhsContent = new StringWriter();
            reader = new BufferedReader(new FileReader(pCSVFile));
            try {
                int i=0;
                while (reader.ready()) {
                    if( i++  == 0){   //ignore first line as it is header
                        reader.readLine();
                        continue;
                    }
                    lhsContent.write(reader.readLine());
                }
            } finally {
                reader.close();
            }


            String fileContents[] = lhsContent.toString().split(",");
            String fieldValue = fileContents[fieldIndexToCompare];
            String expectedValue= "\""+String.valueOf(count)+"\"";
            if (expectedValue.equals(fieldValue)) {
                countMatch=true;
            }

            return countMatch;
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error comparing file (: %s, count: %s)", pCSVFile.getPath(), String.valueOf(count)), t);
        }
    }
    @Test
    public void testDDPayrollWith1EEUsingAGreenDotPayCardAccountHasNoFees() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(4);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        Employee ee = assertOne(company.getDirectDepositEmployees());

        EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(ee.getEmployeeBankAccountCollection()));
        ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
        ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
        assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), ee.getSourceEmployeeId(), ebaDTO));
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        ees.add(ee);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();


        assertEquals(SpcfDecimal.createInstance("0.00"), payrollRun.getFeeReceivableAmount());

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("1", ced.getValue());

    }
    @Test
    public void testDDPayrollWith1EEUsingAGreenDotPayCardAccountAnd1EEUsingACheckingAccountHasFeesOnlyForOneEE() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(4);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        Employee checkingEE = DataLoadServices.addEE(company, DataLoadServices.createEE(), true);

        PayrollServices.beginUnitOfWork();
        Employee ee = assertOne(company.getDirectDepositEmployees().find(Employee.Id().notEqualTo(checkingEE.getId())));

        EmployeeBankAccountDTO ebaDTO = PayrollServices.dtoFactory.create(assertOne(ee.getEmployeeBankAccountCollection()));
        ebaDTO.getBankAccount().setRoutingNumber(routingNumber);
        ebaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");
        assertSuccess(PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), ee.getSourceEmployeeId(), ebaDTO));
        PayrollServices.commitUnitOfWork();

        // submit DD payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Employee> ees = new ArrayList<Employee>();
        ees.add(ee);
        ees.add(checkingEE);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), ees);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = processResult.getResult();


        assertEquals(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(), payrollRun.getFeeReceivableAmount());

        CompanyEvent ce = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false));
        CompanyEventDetail ced = assertOne(ce.getCompanyEventDetails(EventDetailTypeCode.PaycheckAmount));
        assertEquals("1", ced.getValue());
    }


}

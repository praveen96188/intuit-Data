package com.intuit.sbd.payroll.psp.batchjobs.Iop;

import com.intuit.onlinepayroll.webservices.v1.*;
import com.intuit.onlinepayroll.webservices.v1.BankAccountType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.iop.SyncIOPData;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iop.IOPGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.iop.MockIOPGateway;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Jeff Jones
 */
//This class is no longer used
@Ignore
public class SyncIOPDataTests {

    private PayrollCompanyModel payrollCompanyModel;
    private ContractorPaymentCompanyModel contractorPaymentCompanyModel;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 1));
        SpcfCalendar now = PSPDate.getPSPTime().copy();
        now.addMinutes(-10);
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_TOKEN, String.valueOf(now.getTimeInMilliseconds()));
        PayrollServices.commitUnitOfWork();

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    //@Test
    public void testLiveService2() throws Throwable {
        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        PayrollServices.commitUnitOfWork();
    }

    //@Test
    public void testLiveService() throws Throwable {
        SpcfCalendar now = SpcfCalendar.createInstance();
        SpcfCalendar startDate = SpcfCalendar.createInstance(2011,2,1,0,0,0,0);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011,2,1,12,0,0,0);

        Long token = endDate.getTimeInMilliseconds();
        while (token <= now.getTimeInMilliseconds()) {
            PayrollServices.beginUnitOfWork();
            token = startDate.getTimeInMilliseconds();
            try {
                PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_TOKEN, token.toString());
            } catch (Exception e) {
                createTokenSystemParameter(SpcfCalendar.createInstance(2011,2,10,0,0,0,0));
            }
            PSPDate.setPSPTime(endDate);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            SyncIOPData syncIOPData = new SyncIOPData();
            syncIOPData.process();

            startDate.addHours(12);
            endDate.addHours(12);

            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void HappyPathOneEmpOneCheck() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFilingName(null);
        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFilingAddress(null);

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void nullCompanyDataCreate() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFilingName(null);
        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFilingAddress(null);
        payrollCompanyModel.getCompany().getPrimaryContact().getPhones().get(0).setPhoneType(PhoneType.OTHER);
        payrollCompanyModel.getCompany().getPrimaryContact().setEMailAddress(null);
        payrollCompanyModel.getCompany().getPrimaryContact().setEMailAddress("test@intuit.com");

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void nullCompanyDataUpdate() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFilingName(null);
        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFilingAddress(null);
        payrollCompanyModel.getCompany().getPrimaryContact().getPhones().get(0).setPhoneType(PhoneType.OTHER);
        payrollCompanyModel.getCompany().getPrimaryContact().setEMailAddress(null);
        payrollCompanyModel.getCompany().getPrimaryContact().setEMailAddress("test@intuit.com");

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void InvalidFEIN() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getCompany().getCompanyTaxSetup().setFederalEIN("Applied For");

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertEquals("999999999", company.getFedTaxId());
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTimeWindowParam(){
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 9, 10, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Application.beginUnitOfWork();
        SpcfCalendar sCal = PSPDate.getPSPTime().toLocal();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_TOKEN, String.valueOf(sCal.getTimeInMilliseconds()));
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_MAX_TIME_WINDOW_TOKEN, "60");
        Application.commitUnitOfWork();
        SpcfCalendar sCalCopy = sCal.copy();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 9, 10, 5, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        assertNull("End time is null", eCal);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 9, 10, 6, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(1);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 9, 10, 10, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(5);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 9, 13, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 10, 12, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_MAX_TIME_WINDOW_TOKEN, "30");
        Application.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 10, 12, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(30);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_MAX_TIME_WINDOW_TOKEN, "120");
        Application.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 10, 12, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(120);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 10, 23, 30, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_MAX_TIME_WINDOW_TOKEN, "60");
        Application.commitUnitOfWork();
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 12, 31, 23, 30, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        sCal = PSPDate.getPSPTime().toLocal();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2017, 1, 1, 23, 45, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);

    }

    @Test
    public void HappyPathOneEmpTwoChecks() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 2);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(2, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void HappyPathOneEmpTwoCheckDates() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 2);
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(5);

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void HappyPathTwoEmpOneCheck() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 2, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(2, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(2, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void HappyPathTwoEmpTwoChecks() throws Exception {
        int companyid=1000001;
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(companyid, 2, 2);
        MockIOPGateway.addPayrollCompanyModel(companyid, payrollCompanyModel);
        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(2, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        PayrollRun payrollRun = payrollRunList.get(0);
        assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

        DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
        assertEquals(4, paycheckList.size());

        for (Paycheck paycheck : paycheckList) {
            assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
            assertNotNull(paycheck.getDDEmployee());


            DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
            assertEquals(2, paycheckSplitList.size());

            for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                assertNotNull(paycheckSplit.getEmployeeBankAccount());
                assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void HappyPathTwoEmpTwoCheckDates() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 2, 2);
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(5);
        payrollCompanyModel.getPaychecks().get(2).getCheckDate().setDay(5);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(2, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(2, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void deleteOnePaycheck() throws Exception {
        HappyPathOneEmpOneCheck();

        payrollCompanyModel.setCompany(null);
        payrollCompanyModel.getEmployees().clear();
        PaycheckModel paycheckModel = payrollCompanyModel.getPaychecks().get(0);
        paycheckModel.setIsDeleted(true);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        PayrollRun payrollRun = payrollRunList.get(0);
        assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

        DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
        assertEquals(1, paycheckList.size());

        Paycheck paycheck = paycheckList.get(0);
        assertEquals(PaycheckStatusCode.Deleted, paycheck.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void deleteTwoPaycheck() throws Exception {
        HappyPathOneEmpTwoChecks();

        payrollCompanyModel.setCompany(null);
        payrollCompanyModel.getEmployees().clear();
        payrollCompanyModel.getPaychecks().get(0).setIsDeleted(true);
        payrollCompanyModel.getPaychecks().get(1).setIsDeleted(true);

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        PayrollRun payrollRun = payrollRunList.get(0);
        assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

        DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
        assertEquals(2, paycheckList.size());

        for (Paycheck paycheck : paycheckList) {
            assertEquals(PaycheckStatusCode.Deleted, paycheck.getStatus());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void OneEmpOneActiveCheckOneDeletedCheck() throws Exception {
        HappyPathOneEmpTwoCheckDates();

        payrollCompanyModel.getPaychecks().get(0).setIsDeleted(true);
        payrollCompanyModel.getPaychecks().remove(1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            Paycheck paycheck = paycheckList.get(0);
            if (payrollRun.getPaycheckDate().getDay() == 5) {
                assertEquals(PaycheckStatusCode.Deleted, paycheck.getStatus());
            } else {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
            }
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void companyCanceled() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(),
                company.getSourceCompanyId(), ServiceCode.RiskAssessment, ServiceSubStatusCode.Cancelled);
        assertTrue(companyServicePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            int activeCount = 0;
            int inactiveCount = 0;
            for (EmployeeBankAccount eba : ebaList) {
                switch (eba.getStatusCd()) {
                    case Active:
                        activeCount++;
                        break;
                    case Inactive:
                        inactiveCount++;
                }
            }
            assertEquals(2, activeCount);
            assertEquals(0, inactiveCount);
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    //Fraudulent Payroll Tests

    @Test
    public void employeePaidGreaterThanMaxTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getPaychecks().get(0).setNetCheckAmount(BigDecimal.valueOf(5001.00));
        payrollCompanyModel.getPaychecks().get(0).setCheckAmount(BigDecimal.valueOf(5001.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount(BigDecimal.valueOf(2501.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount2(BigDecimal.valueOf(2501.00));

        payrollCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 1, 1, 8, 0, 0, 0));

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeePaidGreaterThanMax);
        assertEquals(1, events.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void totalPayrollExceedsLimitTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 5, 1);

        payrollCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 1, 1, 8, 0, 0, 0));

        for (PaycheckModel paycheckModel : payrollCompanyModel.getPaychecks()) {
            paycheckModel.setNetCheckAmount(BigDecimal.valueOf(4000.00));
            paycheckModel.setCheckAmount(BigDecimal.valueOf(4000.00));
            paycheckModel.setDdAmount(BigDecimal.valueOf(2000.00));
            paycheckModel.setDdAmount2(BigDecimal.valueOf(2000.00));
        }

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(5, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(5, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.TotalPayrollExceedsLimit);
        assertEquals(1, events.size());

        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeePaidEvenDollarAmount);
        assertEquals(0, events.size());

        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeesPaidToSameBank);
        assertEquals(0, events.size());

        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeesPaidToSameBankAccount);
        assertEquals(1, events.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void currentPayrollPercentageIncreaseTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 3, 1, 16, 0, 0, 0));

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011,3,2));
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        payrollCompanyModel.getPaychecks().get(0).setNetCheckAmount(BigDecimal.valueOf(5001.00));
        payrollCompanyModel.getPaychecks().get(0).setCheckAmount(BigDecimal.valueOf(5001.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount(BigDecimal.valueOf(10001.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount2(BigDecimal.valueOf(10001.00));
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(7);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());
            payrollRun.setPayrollDirectDepositAmount(new SpcfMoney("10001"));

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.CurrentPayrollPercentageIncrease);
        assertEquals(2, events.size());

        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.SingleEmployeePercentageIncrease);
        assertEquals(0, events.size());

        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollProcessedTooSoon);
        assertEquals(2, events.size());

        PayrollServices.commitUnitOfWork();
    }

    @Ignore
    @Test
    public void isEmployeeBankAccountInTerminatedCompanyTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        company.getCompanyService(ServiceCode.RiskAssessment).updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000002, 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000002, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000002", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000002", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountInTermedCompany);
        assertEquals(2, events.size());

        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void employeePaidPercentageGreaterThanOthersTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        payrollCompanyModel.getPaychecks().remove(0);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011,3,30));
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 2, 1);
        payrollCompanyModel.getPaychecks().get(0).setNetCheckAmount(BigDecimal.valueOf(2000.00));
        payrollCompanyModel.getPaychecks().get(0).setCheckAmount(BigDecimal.valueOf(2000.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount(BigDecimal.valueOf(1000.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount2(BigDecimal.valueOf(1000.00));
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setMonth(4);
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(1);
        payrollCompanyModel.getPaychecks().get(1).setNetCheckAmount(BigDecimal.valueOf(4000.00));
        payrollCompanyModel.getPaychecks().get(1).setCheckAmount(BigDecimal.valueOf(4000.00));
        payrollCompanyModel.getPaychecks().get(1).setDdAmount(BigDecimal.valueOf(2000.00));
        payrollCompanyModel.getPaychecks().get(1).setDdAmount2(BigDecimal.valueOf(2000.00));
        payrollCompanyModel.getPaychecks().get(1).getCheckDate().setMonth(4);
        payrollCompanyModel.getPaychecks().get(1).getCheckDate().setDay(1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(2, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(2, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertFalse(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeePaidPercentageGreaterThanOthers);
        assertEquals(0, events.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void employeePaidTooManyTimesTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 3);
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(3);
        payrollCompanyModel.getPaychecks().get(1).getCheckDate().setDay(4);
        payrollCompanyModel.getPaychecks().get(2).getCheckDate().setDay(7);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(3, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());

                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertFalse(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeePaidTooManyTimes);
        assertTrue(events.size() == 0); //technically we want this to be == 1, but there is possible race condition that won't really hurt anything except on build server
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void employeeBankAccountChangedTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        payrollCompanyModel.getPaychecks().get(0).setNetCheckAmount(BigDecimal.valueOf(2.50));
        payrollCompanyModel.getPaychecks().get(0).setCheckAmount(BigDecimal.valueOf(2.50));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount(BigDecimal.valueOf(1.25));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount2(BigDecimal.valueOf(1.25));
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011,3,5));
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 2, 1);
        payrollCompanyModel.getEmployees().get(0).getDirectDepositAccount1().setBankAccountType(BankAccountType.SAVINGS);
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(7);
        payrollCompanyModel.getPaychecks().get(1).getCheckDate().setDay(7);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        PayrollServices.beginUnitOfWork();
        syncIOPData = new SyncIOPData();
        syncIOPData.process();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountChangedSpikeInPay);
        assertEquals(1, events.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void checkForInactivityFraudTest() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);

        payrollCompanyModel.getCompany().setSignupDate(null);

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        SpcfCalendar futureDate = SpcfCalendar.createInstance(2011,3,5);
        futureDate.addDays(181);
        PSPDate.setPSPTime(futureDate);
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        futureDate.addDays(3);
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setMonth(futureDate.getMonth());
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setDay(futureDate.getDay());
        payrollCompanyModel.getPaychecks().get(0).getCheckDate().setYear(futureDate.getYear());
        payrollCompanyModel.getPaychecks().get(0).setNetCheckAmount(BigDecimal.valueOf(6000.00));
        payrollCompanyModel.getPaychecks().get(0).setCheckAmount(BigDecimal.valueOf(6000.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount(BigDecimal.valueOf(6000.00));
        payrollCompanyModel.getPaychecks().get(0).setDdAmount2(BigDecimal.valueOf(6000.00));

        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
                SpcfCalendar accountChangedDate = futureDate;
                accountChangedDate.addDays(-10);
                eba.setEffectiveDate(accountChangedDate);
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(1, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        PayrollServices.commitUnitOfWork();
    }

    /** Happy path for one payee and one payment */
    @Test
    public void HappyPathOnePayeeOnePayment() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        for (Payee payee : payeeList) {
            DomainEntitySet<PayeeBankAccount> pbaList = payee.getPayeeBankAccountCollection();
            assertEquals(1, pbaList.size());

            for (PayeeBankAccount pba : pbaList) {
                assertEquals(BankAccountStatus.Active, pba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.BillPayment, payrollRun.getPayrollRunType());

            DomainEntitySet<BillPayment> paymentList = payrollRun.getBillPaymentCollection();
            assertEquals(1, paymentList.size());

            for (BillPayment payment : paymentList) {
                assertEquals(BillPaymentStatusCode.Active, payment.getStatus());
                assertNotNull(payment.getPayee());
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Happy path for one employee, one paycheck, one payee and one payment */
    @Test
    public void HappyPathOneEmployeeOnePaycheckOnePayeeOnePayment() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        boolean foundPaychecks = false;
        boolean foundPayments = false;

        for (PayrollRun payrollRun : payrollRunList) {
            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            if (paycheckList.size() != 0) {
                assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
                assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

                assertEquals(1, paycheckList.size());

                for (Paycheck paycheck : paycheckList) {
                    assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                    assertNotNull(paycheck.getDDEmployee());


                    DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                    assertEquals(2, paycheckSplitList.size());

                    for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                        assertNotNull(paycheckSplit.getEmployeeBankAccount());
                        assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                    }
                }

                foundPaychecks = true;
            }

            DomainEntitySet<BillPayment> paymentList = payrollRun.getBillPaymentCollection();
            if (paymentList.size() != 0) {
                assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
                assertEquals(PayrollType.BillPayment, payrollRun.getPayrollRunType());

                assertEquals(1, paymentList.size());

                for (BillPayment payment : paymentList) {
                    assertEquals(BillPaymentStatusCode.Active, payment.getStatus());
                    assertNotNull(payment.getPayee());
                }

                foundPayments = true;
            }
        }

        assertTrue("Paychecks not found", foundPaychecks);
        assertTrue("Payments not found", foundPayments);

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        for (Payee payee : payeeList) {
            DomainEntitySet<PayeeBankAccount> pbaList = payee.getPayeeBankAccountCollection();
            assertEquals(1, pbaList.size());

            for (PayeeBankAccount pba : pbaList) {
                assertEquals(BankAccountStatus.Active, pba.getStatusCd());
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Test the no sync parameter for one employee, one paycheck, one payee and one payment */
    @Test
    public void NoPaymentSyncOneEmployeeOnePaycheckOnePayeeOnePayment() throws Exception {
        Application.beginUnitOfWork();
        SourcePayrollParameter sourcePayrollParameter = SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.IOP,
                SourcePayrollParameterCode.SyncBillPayments);
        sourcePayrollParameter.setParameterValue("false");
        Application.commitUnitOfWork();

        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        boolean foundPaychecks = false;
        boolean foundPayments = false;

        for (PayrollRun payrollRun : payrollRunList) {
            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            if (paycheckList.size() != 0) {
                assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
                assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

                assertEquals(1, paycheckList.size());

                for (Paycheck paycheck : paycheckList) {
                    assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                    assertNotNull(paycheck.getDDEmployee());


                    DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                    assertEquals(2, paycheckSplitList.size());

                    for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                        assertNotNull(paycheckSplit.getEmployeeBankAccount());
                        assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                    }
                }

                foundPaychecks = true;
            }

            DomainEntitySet<BillPayment> paymentList = payrollRun.getBillPaymentCollection();
            assertEquals("Bill Payments found when there should be none", 0, paymentList.size());
        }

        assertTrue("Paychecks not found", foundPaychecks);

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals("Payees found when there should be none", 0, payeeList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        sourcePayrollParameter = SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.IOP,
                SourcePayrollParameterCode.SyncBillPayments);
        sourcePayrollParameter.setParameterValue("true");
        Application.commitUnitOfWork();
    }

    /** Happy path for one payee and no payments */
    @Test
    public void HappyPathOnePayee() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 0);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        for (Payee payee : payeeList) {
            DomainEntitySet<PayeeBankAccount> pbaList = payee.getPayeeBankAccountCollection();
            assertEquals(1, pbaList.size());

            for (PayeeBankAccount pba : pbaList) {
                assertEquals(BankAccountStatus.Active, pba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(0, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Verifies that payee payment deletes are ignored */
    @Test
    public void IgnoreDeletesPayee() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        contractorPaymentCompanyModel.getContractorPayments().get(0).setIsDeleted(true);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        for (Payee payee : payeeList) {
            DomainEntitySet<PayeeBankAccount> pbaList = payee.getPayeeBankAccountCollection();
            assertEquals(1, pbaList.size());

            for (PayeeBankAccount pba : pbaList) {
                assertEquals(BankAccountStatus.Active, pba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(0, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Happy path for no payee and 1 payment */
    @Test
    public void NoPayeeAndOnePayment() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 0, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(0, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(0, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Verify fraud batch job triggers on exceeded limit */
    @Test
    public void TotalBillPaymentSubmissionExceedsLimit() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 5, 1);
        contractorPaymentCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 1, 1, 8, 0, 0, 0));
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        for (ContractorPaymentModel contractorPaymentModel : contractorPaymentCompanyModel.getContractorPayments()) {
            contractorPaymentModel.setGrossAmount(new BigDecimal(1000000000));
        }

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(5, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.TotalBillPaymentExceedsLimit);
        assertEquals(1, events.size());

        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeePaidGreaterThanMax);
        assertEquals(5, events.size());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Verify fraud batch job triggers on paid greater than maximum limit */
    @Test
    public void PayeePaidGreaterThanMax() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        contractorPaymentCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 1, 1, 8, 0, 0, 0));
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        for (ContractorPaymentModel contractorPaymentModel : contractorPaymentCompanyModel.getContractorPayments()) {
            contractorPaymentModel.setGrossAmount(new BigDecimal(10000));
        }

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeePaidGreaterThanMax);
        assertEquals(1, events.size());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Verify fraud batch job triggers on paid too many times */
    @Test
    public void PayeePaidTooManyTimes() throws Exception {

        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 5);
        contractorPaymentCompanyModel.getContractorPayments().get(0).getCheckDate().setDay(9);
        contractorPaymentCompanyModel.getContractorPayments().get(1).getCheckDate().setDay(10);
        contractorPaymentCompanyModel.getContractorPayments().get(2).getCheckDate().setDay(11);
        contractorPaymentCompanyModel.getContractorPayments().get(3).getCheckDate().setDay(12);
        contractorPaymentCompanyModel.getContractorPayments().get(4).getCheckDate().setDay(13);

        contractorPaymentCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 1, 1, 8, 0, 0, 0));
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        for (ContractorPaymentModel contractorPaymentModel : contractorPaymentCompanyModel.getContractorPayments()) {
            contractorPaymentModel.setGrossAmount(new BigDecimal(100));
        }

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(5, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeePaidTooManyTimes);
        assertTrue(events.size() == 0); //technically we want this to be == 1, but there is possible race condition that won't really hurt anything except on build server

        PayrollServices.rollbackUnitOfWork();
    }

    /** Verify fraud batch job triggers on processed too soon */
    @Test
    public void BillPaymentPayrollProcessedTooSoon() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);

        contractorPaymentCompanyModel.getCompany().setSignupDate(IOPResponseCreator.createXMLGregorianCalendar(2011, 3, 1, 16, 0, 0, 0));

        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);
        for (ContractorPaymentModel contractorPaymentModel : contractorPaymentCompanyModel.getContractorPayments()) {
            contractorPaymentModel.setGrossAmount(new BigDecimal(10001));
        }

        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011,3,2));
        PayrollServices.commitUnitOfWork();

        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        contractorPaymentCompanyModel.getContractorPayments().get(0).getCheckDate().setDay(7);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        for (ContractorPaymentModel contractorPaymentModel : contractorPaymentCompanyModel.getContractorPayments()) {
            contractorPaymentModel.setGrossAmount(new BigDecimal(10001));
        }

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudBPNumberOfPaymentsInXDays).setValue("2");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollProcessedTooSoon);
        assertEquals(2, events.size());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Verify fraud batch job triggers on inactivity fraud  */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void BillPaymentCheckForInactivityFraud() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);

        //payrollCompanyModel.getCompany().setSignupDate(null);

        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        PayrollServices.beginUnitOfWork();
        SyncIOPData syncIOPData = new SyncIOPData();
        syncIOPData.process();
        SpcfCalendar futureDate = SpcfCalendar.createInstance(2011,3,5);
        futureDate.addDays(181);
        PSPDate.setPSPTime(futureDate);
        PayrollServices.commitUnitOfWork();

        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        futureDate.addDays(3);
        contractorPaymentCompanyModel.getContractorPayments().get(0).getCheckDate().setMonth(futureDate.getMonth());
        contractorPaymentCompanyModel.getContractorPayments().get(0).getCheckDate().setDay(futureDate.getDay());
        contractorPaymentCompanyModel.getContractorPayments().get(0).getCheckDate().setYear(futureDate.getYear());
        contractorPaymentCompanyModel.getContractorPayments().get(0).setGrossAmount(new BigDecimal("6000"));
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(1, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(2, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeePaidGreaterThanMax);
        assertEquals(1, events.size());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Check that null objects don't cause a NullPointerException */
    @Test
    public void NullsChecking() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 4, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        // Set up various null situations to verify everything is handled
        contractorPaymentCompanyModel.getContractors().get(0).getContact().setEmailAddress(null);
        contractorPaymentCompanyModel.getContractors().get(1).getContact().getPhones().get(0).setPhoneNumber(null);
        contractorPaymentCompanyModel.getContractors().get(2).setContact(null);
        contractorPaymentCompanyModel.getContractors().get(3).setAddressModel(null);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(4, payeeList.size());

        for (Payee payee : payeeList) {
            DomainEntitySet<PayeeBankAccount> pbaList = payee.getPayeeBankAccountCollection();
            assertEquals(1, pbaList.size());

            for (PayeeBankAccount pba : pbaList) {
                assertEquals(BankAccountStatus.Active, pba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.BillPayment, payrollRun.getPayrollRunType());

            DomainEntitySet<BillPayment> paymentList = payrollRun.getBillPaymentCollection();
            assertEquals(4, paymentList.size());

            for (BillPayment payment : paymentList) {
                assertEquals(BillPaymentStatusCode.Active, payment.getStatus());
                assertNotNull(payment.getPayee());
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    /** Check that an exception is thrown when a payee is not found */
    @Test
    public void TestPayeeNotFound() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001, 1, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000001, contractorPaymentCompanyModel);

        // Set the payment's contractor id to something that will not be found
        // This simulates IOP's malformed Contractor object inside the Contractor Payment
        contractorPaymentCompanyModel.getContractorPayments().get(0).getContractor().setId(Long.MAX_VALUE);

        // Since exception is thrown in another thread, we can only assert that nothing gets saved
        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(0, empList.size());

        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(0, payeeList.size());

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        assertEquals(0, payrollRunList.size());

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();
    }

    /** Check that IOP companies with a redundant EIN is saved correctly */
    @Test
    public void TestSameFEIN() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);

        for (int i = 0; i < 10; i++) {
            payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001 + i, 1, 2);
            payrollCompanyModel.getCompany().getCompanyTaxSetup().setFederalEIN("999999999");
            MockIOPGateway.addPayrollCompanyModel(1000001 + i, payrollCompanyModel);

            // Since exception is thrown in another thread, we can only assert that nothing gets saved
            BatchJobManager.runJob(BatchJobType.IOPDataSync);

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(String.valueOf(1000001 + i), SourceSystemCode.IOP);
            assertTrue(company.hasService(ServiceCode.RiskAssessment));

            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
            process.processFraudulentPayrolls();
            PayrollServices.commitUnitOfWork();

        }

        PayrollServices.beginUnitOfWork();
        for (int i = 0; i < 10; i++) {
            Company company = Company.findCompany(String.valueOf(1000001 + i), SourceSystemCode.IOP);
            assertFalse(company.getIsFlaggedForFraud());
        }
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testNewPaycheckWithAlreadyPersistedPayroll() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001, 2, 1);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);
        contractorPaymentCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000002, 2, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000002, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(2, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Regular);
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(2, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000002", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);


        DomainEntitySet<Payee> payeeList = Payee.findPayees(company);
        assertEquals(2, payeeList.size());

        payrollRunList = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.BillPayment);
        assertEquals(1, payrollRunList.size());

        ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();


        PaycheckModel PaycheckModel = IOPResponseCreator.createPaycheckModel(0, 3, IOPResponseCreator.createXMLGregorianCalendar(2015, 3, 4, 0, 0, 0, 0), IOPResponseCreator.createXMLGregorianCalendar(2015, 3, 1, 0, 0, 0, 0), IOPResponseCreator.createXMLGregorianCalendar(2015, 3, 7, 0, 0, 0, 0));
        PaycheckModel.setCheckAmount(new BigDecimal("333"));
        payrollCompanyModel.getPaychecks().add(PaycheckModel);
        PaycheckModel = IOPResponseCreator.createPaycheckModel(1, 4);
        PaycheckModel.setCheckAmount(new BigDecimal("222"));
        payrollCompanyModel.getPaychecks().add(PaycheckModel);
        MockIOPGateway.addPayrollCompanyModel(1000001, payrollCompanyModel);

        ContractorPaymentModel contractorPaymentModel = IOPResponseCreator.createContractorPaymentData(0, 2);
        contractorPaymentModel.setGrossAmount(new BigDecimal("555.0000"));
        contractorPaymentModel.setCheckAmount(new BigDecimal("555.0000"));
        contractorPaymentModel.setDdAmount(new BigDecimal("555.0000"));
        contractorPaymentCompanyModel.getContractorPayments().add(contractorPaymentModel);
        contractorPaymentModel = IOPResponseCreator.createContractorPaymentData(1, 3);
        contractorPaymentModel.setGrossAmount(new BigDecimal("666.0000"));
        contractorPaymentModel.setCheckAmount(new BigDecimal("666.0000"));
        contractorPaymentModel.setDdAmount(new BigDecimal("666.0000"));
        contractorPaymentModel.setCheckDate(IOPResponseCreator.createXMLGregorianCalendar(2015, 3, 4, 0, 0, 0, 0));
        contractorPaymentCompanyModel.getContractorPayments().add(contractorPaymentModel);
        MockIOPGateway.addContractorPaymentCompanyModel(1000002, contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        payrollRunList = PayrollRun.findPayrollRuns(company);
        payrollRunList = payrollRunList.sort(PayrollRun.<PayrollRun>CreatedDate());
        assertEquals(3, payrollRunList.size());
        int i = 0;
        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            if (i == 0) {
                assertEquals(2, paycheckList.size());
            } else {
                assertEquals(1, paycheckList.size());
            }


            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
            i++;
        }

        ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1000002", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);


        payeeList = Payee.findPayees(company);
        assertEquals(2, payeeList.size());

        payrollRunList = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.BillPayment);
        assertEquals(3, payrollRunList.size());

        ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());

        PayrollServices.commitUnitOfWork();
    }
    @Test
    public void testEndTimeWithinWindow() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,6,14,0,50,0,0,SpcfTimeZone.getLocalTimeZone()));
        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_TOKEN, String.valueOf(PSPDate.getPSPTime().getTimeInMilliseconds()));
        Application.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,6,14,1,0,0,0,SpcfTimeZone.getLocalTimeZone()));
        int companyid=1000001;
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(companyid, 2, 2);
        MockIOPGateway.addPayrollCompanyModel(companyid, payrollCompanyModel);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar sCal = SpcfCalendar.createInstance(SystemParameter.findLongValue(SystemParameter.Code.IOP_SYNC_TOKEN)).toLocal();
        SpcfCalendar eCal = PSPDate.getPSPTime().toLocal();
        SpcfCalendar sCalCopy = eCal.copy();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy.addMinutes(-5);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);
        PayrollServices.rollbackUnitOfWork();

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(String.valueOf(companyid), SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(++companyid, 2, 2);
        MockIOPGateway.addPayrollCompanyModel(companyid, payrollCompanyModel);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,6,14,2,5,0,0,SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        sCal = SpcfCalendar.createInstance(SystemParameter.findLongValue(SystemParameter.Code.IOP_SYNC_TOKEN)).toLocal();
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);
        PayrollServices.rollbackUnitOfWork();

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(String.valueOf(companyid), SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));
        PayrollServices.commitUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(++companyid, 2, 2);
        MockIOPGateway.addPayrollCompanyModel(companyid, payrollCompanyModel);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,6,14,2,58,0,0,SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        sCal = SpcfCalendar.createInstance(SystemParameter.findLongValue(SystemParameter.Code.IOP_SYNC_TOKEN)).toLocal();
        eCal = PSPDate.getPSPTime().toLocal();
        sCalCopy = eCal.copy();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy.addMinutes(-5);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);
        PayrollServices.rollbackUnitOfWork();
        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(String.valueOf(companyid), SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,6,14,5,5,0,0,SpcfTimeZone.getLocalTimeZone()));
        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(++companyid, 2, 2);
        MockIOPGateway.addPayrollCompanyModel(companyid, payrollCompanyModel);
        PayrollServices.beginUnitOfWork();
        sCal = SpcfCalendar.createInstance(SystemParameter.findLongValue(SystemParameter.Code.IOP_SYNC_TOKEN)).toLocal();
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);
        PayrollServices.rollbackUnitOfWork();
        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(String.valueOf(companyid), SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        sCal = SpcfCalendar.createInstance(SystemParameter.findLongValue(SystemParameter.Code.IOP_SYNC_TOKEN)).toLocal();
        eCal = PSPDate.getPSPTime().toLocal();
        eCal =  SyncIOPData.calculateEndTimeWithinTimeWindow(sCal,eCal);
        sCalCopy = sCal.copy();
        sCalCopy.addMinutes(60);
        assertTimeIgnoringMillisecond(sCalCopy, eCal);
        PayrollServices.rollbackUnitOfWork();

    }


    private void createTokenSystemParameter(SpcfCalendar pSpcfCalendar) {
        Long token = pSpcfCalendar.getTimeInMilliseconds();

        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setSystemParameterCd(SystemParameter.Code.IOP_SYNC_TOKEN.toString());
        systemParameter.setSystemParameterDescription("Token used to sync data from IOP to PSP");
        systemParameter.setSystemParameterOrg("PSP");
        systemParameter.setSystemParameterValue(token.toString());
        Application.save(systemParameter);
    }
    @Test
    public void testSaveIOPCompanyList() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        for (int i = 0; i < 2; i++) {
            payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001 + i, 1, 2);
            MockIOPGateway.addPayrollCompanyModel(1000001 + i, payrollCompanyModel);
        }
        for (int i = 0; i < 3; i++) {
            ContractorPaymentCompanyModel contractorCompanyModel;
            if (i % 2 == 0) {
                contractorCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001 + i, 1, 2, 3);
            } else {
                contractorCompanyModel = IOPResponseCreator.createContractorPaymentCompanyModel(1000001 + i, 1, 2);
            }
            MockIOPGateway.addContractorPaymentCompanyModel(1000001 + i, contractorCompanyModel);
        }


        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<IOPSyncCompany> iopSyncCompanyList = Application.find(IOPSyncCompany.class);
        assertEquals("Saved list",3,iopSyncCompanyList.size());
        for(IOPSyncCompany iopSyncCompany:iopSyncCompanyList) {
            if (iopSyncCompany.getCompanyId() == 1000003) {
                assertEquals(true, iopSyncCompany.getHasContractorPayment());
                assertEquals(false, iopSyncCompany.getHasEmployeePayroll());
            } else {
                assertEquals(true, iopSyncCompany.getHasContractorPayment());
                assertEquals(true, iopSyncCompany.getHasEmployeePayroll());
            }

        }
        Company company = Company.findCompany("1000001", SourceSystemCode.IOP);
        assertTrue(company.hasService(ServiceCode.RiskAssessment));

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertNotNull(cba);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        assertEquals(1, empList.size());

        for (Employee emp : empList) {
            assertEquals(EmployeeStatus.Active, emp.getStatusCd());

            DomainEntitySet<EmployeeBankAccount> ebaList = emp.getEmployeeBankAccountCollection();
            assertEquals(2, ebaList.size());

            for (EmployeeBankAccount eba : ebaList) {
                assertEquals(BankAccountStatus.Active, eba.getStatusCd());
            }
        }

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunType().notIn(PayrollType.BillPayment));
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.Regular, payrollRun.getPayrollRunType());

            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            assertEquals(2, paycheckList.size());

            for (Paycheck paycheck : paycheckList) {
                assertEquals(PaycheckStatusCode.Active, paycheck.getStatus());
                assertNotNull(paycheck.getDDEmployee());


                DomainEntitySet<PaycheckSplit> paycheckSplitList = paycheck.getPaycheckSplitCollection();
                assertEquals(2, paycheckSplitList.size());

                for (PaycheckSplit paycheckSplit : paycheckSplitList) {
                    assertNotNull(paycheckSplit.getEmployeeBankAccount());
                    assertTrue(paycheckSplit.getFinancialTransactions().isEmpty());
                }
            }
        }

        DomainEntitySet<FinancialTransaction> ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        payrollRunList = PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunType().in(PayrollType.BillPayment));
        assertEquals(1, payrollRunList.size());

        for (PayrollRun payrollRun : payrollRunList) {
            assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
            assertEquals(PayrollType.BillPayment, payrollRun.getPayrollRunType());

            DomainEntitySet<BillPayment> paymentList = payrollRun.getBillPaymentCollection();
            assertEquals(9, paymentList.size());

            for (BillPayment payment : paymentList) {
                assertEquals(BillPaymentStatusCode.Active, payment.getStatus());
                assertNotNull(payment.getPayee());
            }
        }

        ft = company.getFinancialTransactions();
        assertTrue(ft.isEmpty());

        DomainEntitySet<MoneyMovementTransaction> mmt = Application.find(MoneyMovementTransaction.class);
        assertTrue(mmt.isEmpty());


        PayrollServices.rollbackUnitOfWork();
    }

    private void assertTimeIgnoringMillisecond(SpcfCalendar expected, SpcfCalendar output) {
        if (output == null && expected == null) {
            assertNull("Assert failed as expected and output data is null", output);
        }
        if (output == null) {
            assertNull("Assert failed as  output data is null", output);
        }
        expected.setValues(expected.getYear(), expected.getMonth(), expected.getDay(), expected.getHour(), expected.getMinute(), expected.getSecond(), 0);
        output.setValues(output.getYear(), output.getMonth(), output.getDay(), output.getHour(), output.getMinute(), output.getSecond(), 0);
        assertEquals("Date is invalid", expected, output);
    }
    @Test
    public void testRetries() throws Exception {
        IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
        for (int i = 0; i < 3; i++) {
            payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000001 + i, 1, 2);
            contractorPaymentCompanyModel= IOPResponseCreator.createContractorPaymentCompanyModel(1000001 + i, 1, 2);
            if (i > 0 && i % 2 == 0) {
                payrollCompanyModel.getPaychecks().get(0).setEmployee(null);
                contractorPaymentCompanyModel.getContractorPayments().get(0).setContractor(null);
            }
            MockIOPGateway.addPayrollCompanyModel(1000001 + i, payrollCompanyModel);
            MockIOPGateway.addContractorPaymentCompanyModel(1000001 + i, contractorPaymentCompanyModel);
        }

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<IOPSyncCompany> iopSyncCompanyList = IOPSyncCompany.findPendingCompanyList();
        assertEquals("Pending list",1,iopSyncCompanyList.size());
        PayrollServices.rollbackUnitOfWork();

        payrollCompanyModel = IOPResponseCreator.createPayrollCompanyModel(1000005 , 1, 1);
        MockIOPGateway.addPayrollCompanyModel(1000005, payrollCompanyModel);
        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        iopSyncCompanyList = IOPSyncCompany.findPendingCompanyList();
        assertEquals("Pending list",1,iopSyncCompanyList.size());
        PayrollServices.rollbackUnitOfWork();

        BatchJobManager.runJob(BatchJobType.IOPDataSync);

        PayrollServices.beginUnitOfWork();
        iopSyncCompanyList = IOPSyncCompany.findPendingCompanyList();
        assertEquals("Pending list",0,iopSyncCompanyList.size());
        PayrollServices.rollbackUnitOfWork();
        contractorPaymentCompanyModel= IOPResponseCreator.createContractorPaymentCompanyModel(1000006, 1, 1);
        MockIOPGateway.addContractorPaymentCompanyModel(1000006 , contractorPaymentCompanyModel);

        BatchJobManager.runJob(BatchJobType.IOPDataSync);
        PayrollServices.beginUnitOfWork();
        iopSyncCompanyList = IOPSyncCompany.findPendingCompanyList();
        assertEquals("Pending list",0,iopSyncCompanyList.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        iopSyncCompanyList = Application.find(IOPSyncCompany.class);
        assertEquals("Saved list",5,iopSyncCompanyList.size());
        for(IOPSyncCompany iopSyncCompany:iopSyncCompanyList) {
            if (iopSyncCompany.getCompanyId() == 1000003) {
                assertEquals(3, iopSyncCompany.getRetryCount());
            } else {
                assertEquals(1, iopSyncCompany.getRetryCount());
            }
            if (iopSyncCompany.getCompanyId() == 1000001 || iopSyncCompany.getCompanyId() == 1000002) {
                assertEquals(true, iopSyncCompany.getHasContractorPayment());
                assertEquals(true, iopSyncCompany.getHasEmployeePayroll());
            }
            if (iopSyncCompany.getCompanyId() == 1000005) {
                assertEquals(false, iopSyncCompany.getHasContractorPayment());
                assertEquals(true, iopSyncCompany.getHasEmployeePayroll());
            }
            if (iopSyncCompany.getCompanyId() == 1000006) {
                assertEquals(true, iopSyncCompany.getHasContractorPayment());
                assertEquals(false, iopSyncCompany.getHasEmployeePayroll());
            }

        }
        PayrollServices.rollbackUnitOfWork();
    }
}

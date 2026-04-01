package com.intuit.sbd.payroll.psp.processes.accountservice;

import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.accountservices.AccountServicesDataGenerator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;

import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.intuit.sbd.payroll.psp.DomainEntitySet;


import java.util.Collection;

import static org.junit.Assert.*;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class AccountServiceSyncCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void syncAccountServices_ApprovedCase() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Approved");

        String realmId = paymentsAccount.getRealmId();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false));
        company.setIAMRealmId(realmId);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore(realmId);
        accountServiceSyncCore.execute();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void checkVBDStatusChangeEvents() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Approved");

        String realmId = paymentsAccount.getRealmId();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false));
        company.setIAMRealmId(realmId);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore(realmId);
        accountServiceSyncCore.execute();
        // Check for VBD Status Change event.
        assertEquals(CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountVBDStatusChange).size(), 1);
        accountServiceSyncCore.execute();
        // VBD Status should be raised only once.
        assertEquals(CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountVBDStatusChange).size(), 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        // Create a new payments account with a new bank account.
        PaymentsAccount paymentsAccount1 = accountServicesHelper.createPaymentsAccount("Approved", "accountservice/create_payments_account_diff_bank.json");
        String realmId1 = paymentsAccount1.getRealmId();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        company.setIAMRealmId(realmId1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore1 = new AccountServiceSyncCore(realmId1);
        // Perform sync on the updated realmId associated with the new bank account.
        accountServiceSyncCore1.execute();
        // When bank account is different, a VBD event should be raised. Total events 1 + 1 = 2.
        assertEquals(CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountVBDStatusChange).size(), 2);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void syncAccountServices_PendingCase() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Pending");

        String realmId = paymentsAccount.getRealmId();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456780", false, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false,false));
        company.setIAMRealmId(realmId);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore(realmId);
        accountServiceSyncCore.execute();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = company.getCompanyBankAccountCollection();


        com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyBankAccount> cbas = company.getCompanyBankAccountCollection();
        cbas = cbas.sort(CompanyBankAccount.StatusCd());

        //Verify that Company Bank Account has the correct values
        CompanyBankAccount companyBankAccount = cbas.get(1);
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        Collection<ServiceSubStatusCode> currentHolds= company.getCurrentOnHoldReasonCodes();

        assertTrue("AML Hold should be there", currentHolds.contains(ServiceSubStatusCode.AMLHold));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void syncAccountServices_DeclinedCase() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Declined");

        String realmId = paymentsAccount.getRealmId();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456780", false, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false,false));
        company.setIAMRealmId(realmId);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore(realmId);
        accountServiceSyncCore.execute();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = company.getCompanyBankAccountCollection();


        com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyBankAccount> cbas = company.getCompanyBankAccountCollection();
        cbas = cbas.sort(CompanyBankAccount.StatusCd());

        //Verify that Company Bank Account has the correct values
        CompanyBankAccount companyBankAccount = cbas.get(1);
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        Collection<ServiceSubStatusCode> currentHolds= company.getCurrentOnHoldReasonCodes();

        assertTrue("AML Hold should be there", currentHolds.contains(ServiceSubStatusCode.AMLHold));

        PayrollServices.commitUnitOfWork();

    }


    // @Test
    public void syncAccountServices_DeniedCase() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Pending");

        String realmId = paymentsAccount.getRealmId();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456780", false, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false,false));
        company.setIAMRealmId(realmId);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore(realmId);
        accountServiceSyncCore.execute();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = company.getCompanyBankAccountCollection();


        com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyBankAccount> cbas = company.getCompanyBankAccountCollection();
        cbas = cbas.sort(CompanyBankAccount.StatusCd());

        //Verify that Company Bank Account has the correct values
        CompanyBankAccount companyBankAccount = cbas.get(1);
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        Collection<ServiceSubStatusCode> currentHolds= company.getCurrentOnHoldReasonCodes();

        assertTrue("AML Hold should be there", currentHolds.contains(ServiceSubStatusCode.AMLHold));

        PayrollServices.commitUnitOfWork();


        /**
         * for Denied status
         */

        paymentsAccount = accountServicesHelper.createPaymentsAccount("Denied");

        realmId = paymentsAccount.getRealmId();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        company.setIAMRealmId(realmId);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        accountServiceSyncCore = new AccountServiceSyncCore(realmId);
        accountServiceSyncCore.execute();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        companyBankAccounts = company.getCompanyBankAccountCollection();


        cbas = company.getCompanyBankAccountCollection();
        cbas = cbas.sort(CompanyBankAccount.StatusCd());

        //Verify that Company Bank Account has the correct values
        companyBankAccount = cbas.get(1);
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        Collection<ServiceSubStatusCode> currentHoldsForDenied= company.getCurrentOnHoldReasonCodes();

        assertTrue("AML Hold should be there", currentHoldsForDenied.contains(ServiceSubStatusCode.AMLHold));

        PayrollServices.commitUnitOfWork();


    }

    @Test
    public void smsFailureRowShouldBeCreatedIfSyncFails() {


        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Approved");

        String realmId = paymentsAccount.getRealmId();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false));

        //invalid realmid to  make the sync fail
        company.setIAMRealmId("12353454657576");
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore("12353454657576");
        accountServiceSyncCore.execute();

        DomainEntitySet<SMSSyncFailure> smsSyncFailure = SMSSyncFailure.getSmsSyncFailureByRealmId(Long.parseLong("12353454657576"));
        assertEquals(smsSyncFailure.size(), 1);

    }
}


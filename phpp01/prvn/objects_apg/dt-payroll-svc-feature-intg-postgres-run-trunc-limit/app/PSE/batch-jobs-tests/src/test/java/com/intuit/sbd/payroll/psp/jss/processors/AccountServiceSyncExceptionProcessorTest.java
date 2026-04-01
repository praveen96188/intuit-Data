package com.intuit.sbd.payroll.psp.jss.processors;


import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.platform.integration.ius.common.types.IntuitContext;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.FakeSalesTaxGateway;
import com.intuit.sbd.payroll.psp.accountservices.AccountServicesDataGenerator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SMSSyncJobStatus;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * @author vpandey
 */
public class AccountServiceSyncExceptionProcessorTest {

    private static final String PAYROLL_PLUGIN_ASSET_ALIAS = "Intuit.payroll.dirctdeposit.qbdtpayrolltronexp";
    private static final SpcfLogger logger = SpcfLogManager.getLogger(AccountServiceSyncExceptionProcessorTest.class);

    private static class Tuple {
        private String psid;
        private String realmId;


        public String getPsid() {
            return psid;
        }

        public String getRealmId() {
            return realmId;
        }


        Tuple(String psid, String realmId) {
            this.psid = psid;
            this.realmId = realmId;

        }

    }

    @Before
    public void runBeforeEachTest() {

        beforeEachTest();

    }


    public Tuple createValidDataForSync() {

        //create PSP company with  tron enabled DD
        String psid = createCompanyWithTRONEnabledDD();
        //create SMS account
        String realmId = createSMSAccount();
        //link the realmid to psp company
        linkPSPAccountTOSMSAccount(psid, realmId);
        //create SMS sync failures
        Application.beginUnitOfWork();
        SMSSyncFailure.saveSMSSyncFailureIfAbsent(realmId, "reason1");
        Application.commitUnitOfWork();
        return new Tuple(psid, realmId);
    }


    public Tuple createInValidDataForSync() {


        String psid = createCompanyWithTRONEnabledDD();
        String realmId = createSMSAccount();
        linkPSPAccountTOSMSAccount(psid, "invalidRealmId");
        Application.beginUnitOfWork();
        SMSSyncFailure.saveSMSSyncFailureIfAbsent(realmId, "reason2");
        Application.commitUnitOfWork();
        return new Tuple(psid, realmId);
    }

    private static void beforeEachTest() {
        Application.truncateTables();
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        assertTransactionNotInProgress();
        SalesTaxGatewayFactory.setInstanceClass(FakeSalesTaxGateway.class);
    }

    private static void assertTransactionNotInProgress() {
        if (Application.hasActiveTransaction()) {
            logger.error("Transaction in progress. The current unit of work originated at:\n" +
                    Application.getSessionCache().getOriginOfUnitOfWork());
            Application.rollbackUnitOfWork();

        }
    }


    private void linkPSPAccountTOSMSAccount(String psid, String realmId) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Company2Dataloader company2Dataloader = new Company2Dataloader();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false));
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        company.setIAMRealmId(realmId);
        PayrollServices.commitUnitOfWork();
    }

    private String createSMSAccount() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Approved");

        String realmId = paymentsAccount.getRealmId();
        return realmId;
    }


    @Test
    public void companyShouldGetUpdatedIfSyncSucceeds() throws Exception {


        Tuple tuple = createValidDataForSync();
        String psid = tuple.getPsid();
        String realmId = tuple.getRealmId();
        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.AccountServiceSyncExceptionProcessor.toString(), UUID.randomUUID().toString()};
        AccountServiceSyncExceptionProcessor asp = new AccountServiceSyncExceptionProcessor(params);
        asp.execute();

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertTrue(company.getNotificationEmail().equalsIgnoreCase("testuser_mmoapproved@gmail.com"));


    }


    @Test
    public void companyShouldNotGetUpdatedIfSyncFails() throws Exception {


        Tuple tuple = createInValidDataForSync();
        String psid = tuple.getPsid();
        String realmId = tuple.getRealmId();
        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.AccountServiceSyncExceptionProcessor.toString(), UUID.randomUUID().toString()};
        AccountServiceSyncExceptionProcessor asp = new AccountServiceSyncExceptionProcessor(params);
        asp.execute();

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertFalse(company.getNotificationEmail().equalsIgnoreCase("testuser_mmoapproved@gmail.com"));


    }


    @Test
    public void syncFailureDetailsShouldNotBeDeletedIfSyncFails() throws Exception {

        Tuple tuple = createInValidDataForSync();
        String psid = tuple.getPsid();
        String realmId = tuple.getRealmId();
        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.AccountServiceSyncExceptionProcessor.toString(), UUID.randomUUID().toString()};
        AccountServiceSyncExceptionProcessor asp = new AccountServiceSyncExceptionProcessor(params);
        asp.execute();

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertFalse(company.getNotificationEmail().equalsIgnoreCase("testuser_mmoapproved@gmail.com"));
        SMSSyncFailure entryAfterSync = SMSSyncFailure.getSmsSyncFailureByRealmId(Long.parseLong(realmId)).get(0);

        assertNotNull(entryAfterSync);

    }


    @Test
    public void syncFailureDetailsShouldBeDeletedIfSyncSucceeds() throws Exception {

        Tuple tuple = createValidDataForSync();
        String psid = tuple.getPsid();
        String realmId = tuple.getRealmId();

        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.AccountServiceSyncExceptionProcessor.toString(), UUID.randomUUID().toString()};
        AccountServiceSyncExceptionProcessor asp = new AccountServiceSyncExceptionProcessor(params);
        asp.execute();

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertTrue(company.getNotificationEmail().equalsIgnoreCase("testuser_mmoapproved@gmail.com"));
        DomainEntitySet<SMSSyncFailure> entryAfterSync = SMSSyncFailure.getSmsSyncFailureByRealmId(Long.parseLong(realmId));

        assertEquals(entryAfterSync.size(), 0);

    }


    @Test
    public void smsFailureAttributesShouldBeUpdatedIfSyncFails() throws Exception {


        Tuple tuple = createInValidDataForSync();
        String psid = tuple.getPsid();
        String realmId = tuple.getRealmId();

        SMSSyncFailure entryBeforeSync = SMSSyncFailure.getSmsSyncFailureByRealmId(Long.parseLong(realmId)).get(0);
        Integer prevCount = entryBeforeSync.getCount();
        com.intuit.sbd.payroll.psp.domain.SMSSyncJobStatus prevStatus = entryBeforeSync.getStatus();
        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.AccountServiceSyncExceptionProcessor.toString(), UUID.randomUUID().toString()};
        AccountServiceSyncExceptionProcessor asp = new AccountServiceSyncExceptionProcessor(params);
        asp.execute();

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertFalse(company.getNotificationEmail().equalsIgnoreCase("testuser_mmoapproved@gmail.com"));

        SMSSyncFailure entryAfterSync = SMSSyncFailure.getSmsSyncFailureByRealmId(Long.parseLong(realmId)).get(0);
        Integer afterCount = entryAfterSync.getCount();
        SMSSyncJobStatus afterStatus = entryAfterSync.getStatus();
        assertTrue(prevCount + 1 == afterCount);
        assertTrue(prevStatus.equals(SMSSyncJobStatus.Pending));
        assertTrue(afterStatus.equals(SMSSyncJobStatus.NeverRetry));


    }

    private void setPayrollPluginContext() {
        IntuitContext intuitContext = new IntuitContext();
        intuitContext.setAssetAlias(PAYROLL_PLUGIN_ASSET_ALIAS);
        RequestAttributesUtils.setAttribute(ContextConstants.INTUIT_CONTEXT, intuitContext);
    }


    private String createCompanyWithTRONEnabledDD() {
        String sourceCompanyId = null;
        try {
            Company newCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            sourceCompanyId = newCompany.getSourceCompanyId();
            Application.beginUnitOfWork();
            Company company = Company.findCompany(newCompany.getSourceCompanyId(), SourceSystemCode.QBDT);
            company.setOIIFlag("1001100000000000");
            Application.save(company);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
            Application.rollbackUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        return sourceCompanyId;
    }


}

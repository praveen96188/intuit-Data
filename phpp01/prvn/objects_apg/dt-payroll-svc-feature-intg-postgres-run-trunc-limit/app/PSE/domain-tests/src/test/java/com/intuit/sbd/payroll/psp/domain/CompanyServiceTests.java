package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Dawn Martens
 * Date: Nov 12, 2007
 */
public class CompanyServiceTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
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
    public void testUpdateServiceStatusOnHold() {
        //Setup
        Application.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.companyManager.addOnHoldReason(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceSubStatusCode.SuspendedDirectDeposit);
        assertEquals(1, company1.getOnHoldReasonCollection().size());
        Application.commitUnitOfWork();

        //Persistence verification
        Application.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);

        assertEquals("Company Events size", 2, companyEvents.size());

        CompanyEvent statusChangeEvent = null;
        for (CompanyEvent e : companyEvents) {
            if (e.getEventTypeCd() == EventTypeCode.ServiceStatusChange) {
                statusChangeEvent = e;
                break;
            }
        }
        assertTrue("ServiceStatusChange event exists", statusChangeEvent != null);
        assertEquals("NewOnHoldReasons Size", 1, statusChangeEvent.getCompanyEventDetailValues(EventDetailTypeCode.NewOnHoldReason).size());
        assertEquals("NewOnHoldReason", "Suspended Direct Deposit", statusChangeEvent.getCompanyEventDetailValues(EventDetailTypeCode.NewOnHoldReason).iterator().next());
        Application.commitUnitOfWork();
    }

    @Test
    public void testUpdateServiceStatus() {
        //Setup
        Application.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        Application.commitUnitOfWork();


        //Test
        Application.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        foundService.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.commitUnitOfWork();

        //Persistence verification
        Application.beginUnitOfWork();
        foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);

        assertEquals("Company Events size", 2, companyEvents.size());

        CompanyEvent serviceStatusChangeEvent = null;
        for (CompanyEvent e : companyEvents) {
            if (e.getEventTypeCd() == EventTypeCode.ServiceStatusChange) {
                serviceStatusChangeEvent = e;
                break;
            }
        }
        assertTrue("ServiceStatusChange event exists", serviceStatusChangeEvent != null);

        SpcfCalendar eventTimeStamp = serviceStatusChangeEvent.getEventTimeStamp();
        if (eventTimeStamp.isUTC()) {
            eventTimeStamp = eventTimeStamp.toLocal();
        }
        assertEquals("Event timestamp", PSPDate.getPSPTime().getDayOfYear(), eventTimeStamp.getDayOfYear());
        assertEquals("Old status", EnumUtils.getReadableName(ServiceSubStatusCode.PendingBankVerification), serviceStatusChangeEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldServiceStatus));
        assertEquals("New status", ServiceSubStatusCode.Terminated.toString(), serviceStatusChangeEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewServiceStatus));
        assertEquals("Company", foundCompany, serviceStatusChangeEvent.getCompany());

        assertEquals("Service status", ServiceSubStatusCode.Terminated, foundService.getStatusCd());
        assertEquals("Service Code", EnumUtils.getReadableName(ServiceCode.DirectDeposit), serviceStatusChangeEvent.getCompanyEventDetailValue(EventDetailTypeCode.ServiceCode));
        Application.commitUnitOfWork();
    }

    @Test
    public void testCurrentSubStatuses() {
        //Setup
        Application.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<ServiceSubStatus> substatusList = ServiceSubStatus.findCurrentSubStatuses(
                foundCompany.getSourceSystemCd(), foundCompany.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        assertNotNull("SubStatus List ", substatusList);
        assertEquals("SubStatus List ", 1, substatusList.size());
        assertEquals("Service Sub Status Code ", ServiceSubStatusCode.PendingBankVerification,
                substatusList.get(0).getServiceSubStatusCd());
    }

    @Test
    public void testPossibleSubStatusesForUserRoleId() {
        //Setup
        Application.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        Application.commitUnitOfWork();

        //Assertion for PossibleSubStatuses for a given UserRole Id - Not an Agent
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> possibleSubStatusList = ServiceSubStatus.
                findPossibleSubStatuses(ServiceStatusCode.PendingActivation, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertEquals("Possible SubStatus List ", 3, possibleSubStatusList.size());

        //Add user 
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("DesktopCareManager");
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(foundRole.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", processResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) processResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName()));

        //Assertion for possible SubStatus list for an Agent
        PayrollServices.beginUnitOfWork();
        possibleSubStatusList = ServiceSubStatus.findPossibleSubStatuses(ServiceStatusCode.PendingActivation, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertEquals("Possible SubStatus List ", 0, possibleSubStatusList.size());
    }

    @Test
    public void testCancelAssistedUpdatesMigrationStatus() {

        //-------------------------
        // case: MigratingToAS400, Cancel service
        //-------------------------
        assertServiceStatus(ServiceCode.Tax,
                                        ServiceSubStatusCode.Cancelled);

        //-------------------------
        // case: MigratingToAS400, Terminate service
        //-------------------------
        assertServiceStatus(ServiceCode.Tax,
                                        ServiceSubStatusCode.Terminated);

        //-------------------------
        // case: MigratedFromAS400ButStillActiveThere, Cancel service
        //-------------------------
        assertServiceStatus(ServiceCode.Tax,
                                        ServiceSubStatusCode.Cancelled);

        //-------------------------
        // case: MigratedFromAS400ButStillActiveThere, Terminate service
        //-------------------------
        assertServiceStatus(ServiceCode.Tax,
                                        ServiceSubStatusCode.Terminated);

        //-------------------------
        // case: Cancelled, Reactivate
        //-------------------------
        assertServiceStatus(ServiceCode.Tax,
                                        ServiceSubStatusCode.Cancelled,
                                        ServiceSubStatusCode.ActiveCurrent);

        //-------------------------
        // case: Cancelled, MigratingToAS400, Reactivate
        //-------------------------
/*
        // create company w/DD
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // cancel DD
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService ddService = company.getService(ServiceCode.DirectDeposit);
        ddService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        // add Assisted
        DataLoadServices.addTaxService(company);

        // cancel Assisted
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService assistedService = company.getService(ServiceCode.Tax);
        assistedService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        // re-activate DD
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        ddService = company.getService(ServiceCode.DirectDeposit);
        ddService.updateCompanyServiceStatus(ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();

        // pretend EWS starts a migration from DD to Assisted
        // -- set MigrationStatus to MigratingToAS400
        // -- updates existing Assisted status record in AS400
        // todo_rhn: verify EWS logic
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollServices.companyManager.updateCompanyMigrationStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), MigrationStatusCode.MigratingToAS400);
        PayrollServices.commitUnitOfWork();

        // re-activate Assisted
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assistedService = company.getService(ServiceCode.Tax);
        assistedService.updateCompanyServiceStatus(ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();

        // verify Assisted service status, MigrationStatus
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assistedService = company.getService(ServiceCode.Tax);
        assertEquals("assisted service status", ServiceSubStatusCode.ActiveCurrent, assistedService.getStatusCd());
        assertEquals("migration status code", MigrationStatusCode.MigratingToAS400, company.getMigrationStatus());
        PayrollServices.rollbackUnitOfWork();
*/
        //-------------------------
        // case: DD company
        //   nothing should happen cases
        //-------------------------
       // assertServiceAndMigrationStatus(ServiceCode.DirectDeposit, MigrationStatusCode.NotAMigratedCompany, ServiceSubStatusCode.Cancelled, MigrationStatusCode.NotAMigratedCompany);
        //assertServiceAndMigrationStatus(ServiceCode.DirectDeposit, MigrationStatusCode.NotAMigratedCompany, ServiceSubStatusCode.Terminated, MigrationStatusCode.NotAMigratedCompany);

    }

    private void assertServiceStatus(ServiceCode pServiceCode, ServiceSubStatusCode pAssistedServiceStatus) {
        assertServiceStatus(pServiceCode, null, pAssistedServiceStatus);
    }

    private void assertServiceStatus(ServiceCode pServiceCode, ServiceSubStatusCode pInitialServiceStatus, ServiceSubStatusCode pAssistedServiceStatus) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pServiceCode);

        // 1. set the MigrationStatusCode value and initial service status
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        if (pInitialServiceStatus != null) {
            CompanyService taxService = company.getService(ServiceCode.Tax);
            taxService.updateCompanyServiceStatus(pInitialServiceStatus);
        }
        PayrollServices.commitUnitOfWork();

        // 2. update the service status
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService taxService = company.getService(ServiceCode.Tax);
        //Update the tax service created date to bypass the Active Cancelled Active validation.
        taxService.setCreatedDate(PSPDate.getPSPTime());
        taxService.updateCompanyServiceStatus(pAssistedServiceStatus);
        PayrollServices.commitUnitOfWork();

        // 3. verify MigrationStatusCode value
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        taxService = company.getService(pServiceCode);
        assertEquals("assisted service status", pAssistedServiceStatus, taxService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        // 4. update the service status to cancelled if given status is terminated so succeeding companies are not flagged
        if (pAssistedServiceStatus == ServiceSubStatusCode.Terminated) {
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            taxService = company.getService(ServiceCode.Tax);
            taxService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testRemoveTerminatedStatus() {
        //Setup
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        Application.commitUnitOfWork();

        //Test
        Application.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        foundService.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.commitUnitOfWork();

        // Persistence verification
        Application.beginUnitOfWork();
        foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<EmployeeBankAccount> eeBankAccounts = Application.find(EmployeeBankAccount.class);
        assertTrue("At least one employee bank account must exist", eeBankAccounts.size() > 0);
        // Employee and Company bank accounts are moved to the fraudulent collection.
        assertEquals("# Fraud bank accounts should equal # ee bank accounts", eeBankAccounts.size() + foundCompany.getCompanyBankAccountSet().size(),
                     foundService.getCompany().getFraudBankAccountCollection().size());
        for (FraudBankAccount fraudBankAccount : foundService.getCompany().getFraudBankAccountCollection()) {
            boolean found = false;
            for (EmployeeBankAccount eeBankAccount : eeBankAccounts) {
                found = eeBankAccount.getBankAccount().getRoutingNumber().equals(fraudBankAccount.getRoutingNumber()) &&
                        eeBankAccount.getBankAccount().getAccountNumber().equals(fraudBankAccount.getAccountNumber()) &&
                        eeBankAccount.getBankAccount().getAccountTypeCd().equals(fraudBankAccount.getAccountTypeCd());
                if (found) break;
            }
            if(!found) {
                for (CompanyBankAccount erBankAccount : foundCompany.getCompanyBankAccountSet()) {
                    found = erBankAccount.getBankAccount().getRoutingNumber().equals(fraudBankAccount.getRoutingNumber()) &&
                            erBankAccount.getBankAccount().getAccountNumber().equals(fraudBankAccount.getAccountNumber()) &&
                            erBankAccount.getBankAccount().getAccountTypeCd().equals(fraudBankAccount.getAccountTypeCd());
                    if (found) break;
                }
            }
            assertTrue("couldn't find bank account in fraud bank account: " + fraudBankAccount.getId(), found);
        }
        Application.commitUnitOfWork();

        //Test
        Application.beginUnitOfWork();
        foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        foundService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        //Persistence verification
        Application.beginUnitOfWork();
        foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());

        assertEquals(0, foundCompany.getFraudCompanyCollection().size());
        assertEquals(0, foundCompany.getFraudBankAccountCollection().size());
        assertEquals(0, foundCompany.getFraudAddressCollection().size());
        assertEquals(0, foundCompany.getFraudContactCollection().size());

        foundService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany);

        Application.find(EmployeeBankAccount.class);
        assertTrue("At least one employee bank account must exist", eeBankAccounts.size() > 0);
        assertTrue(foundService.getCompany().getFraudBankAccountCollection().size() == 0);

        Application.commitUnitOfWork();
    }

    @Test
    public void testFindCompanyServicesByFedTaxId(){
        //Setup
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2018, 11, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        Application.commitUnitOfWork();

        //Test
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyService> companyServicesByFedTaxId = CompanyService.findCompanyServicesByFedTaxId(SourceSystemCode.QBOE, ServiceCode.DirectDeposit, "123456789");
        assertEquals("CompanyService count does not match", 1, companyServicesByFedTaxId.size());
        companyServicesByFedTaxId = CompanyService.findCompanyServicesByFedTaxId(SourceSystemCode.QBOE, ServiceCode.DirectDeposit, "201808310");
        assertEquals("CompanyService count does not match", 0, companyServicesByFedTaxId.size());
        PayrollServices.rollbackUnitOfWork();
    }

}

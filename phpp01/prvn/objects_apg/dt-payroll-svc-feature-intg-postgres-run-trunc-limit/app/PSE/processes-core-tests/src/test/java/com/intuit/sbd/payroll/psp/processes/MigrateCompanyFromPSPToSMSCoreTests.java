package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BaseSMSMigration;

import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.processes.common.PSPToSMSMigrationHelper;
import com.intuit.money.account.model.ProfileMigrationRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


import static org.junit.Assert.assertEquals;

public class MigrateCompanyFromPSPToSMSCoreTests {
    private DataLoader dataloader = new DataLoader();
    private Company testCompany;
    

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
    public void MigrateCompanyFromPSPToSMSCompanyValidationFailure() {
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.migratePSPToSMS("1234567", "1111");
        PayrollServices.commitUnitOfWork();
        assertEquals("169", processResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void MigrateCompanyFromPSPToSMSAlreadyMigratedValidationFailure() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = DataLoader.addCompany(companyDTO);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(companyDTO.getSourceSystemCd().toString()), companyDTO.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(companyDTO.getCompanyId(), companyDTO.getSourceSystemCd());
        assertEquals("Load company", 0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId(companyDTO.getCompanyId());
        smsMigration.setMigrationStatus(SMSMigrationStatus.MigrationComplete);
        smsMigration.setCompany(company);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.migratePSPToSMS("123456", "1111");
        PayrollServices.commitUnitOfWork();
        assertEquals("10130", processResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void MigrateCompanyFromPSPToSMSServiceValidationFailure() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = DataLoader.addCompany(companyDTO);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(companyDTO.getSourceSystemCd().toString()), companyDTO.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(companyDTO.getCompanyId(), companyDTO.getSourceSystemCd());
        assertEquals("Load company", 0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId(companyDTO.getCompanyId());
        smsMigration.setMigrationStatus(SMSMigrationStatus.DataCollectionComplete);
        smsMigration.setCompany(company);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.migratePSPToSMS("123456", "1111");
        PayrollServices.commitUnitOfWork();
        assertEquals("10004", processResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void MigrateCompanyFromPSPToIsActiveValidationFailure() {


        //create company with VMP and Assisted service
        testCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456", true, ServiceCode.DirectDeposit);
        addRealmIdToCompany();
        DataLoadServices.addCompanyPIN(testCompany, null);
        DataLoadServices.addCompanyBankAccount(testCompany);
        addServiceSubStatusCode();

        PayrollServices.beginUnitOfWork();
        BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId("123456");
        smsMigration.setMigrationStatus(SMSMigrationStatus.DataCollectionComplete);
        smsMigration.setCompany(testCompany);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.migratePSPToSMS("123456", "1111");
        PayrollServices.commitUnitOfWork();
        assertEquals("1011", processResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void MigrateFailDueToNoComplianceAddress() {

        //create company with VMP and Assisted service
        testCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456", true, ServiceCode.DirectDeposit);
        addRealmIdToCompany();

        DataLoadServices.addCompanyPIN(testCompany, null);
        DataLoadServices.addCompanyBankAccount(testCompany);

        PayrollServices.beginUnitOfWork();

       /*  Current phone number, (775) 111-1111 sanitizing to remove special characters
         to avoid CompanyHasSpecialCharPhone validation failure */
        Contact primaryPrincipalContact = testCompany.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        primaryPrincipalContact.setPhone(primaryPrincipalContact.getPhone().replaceAll(Address.ONLY_DIGITS,""));

        BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId("123456");
        smsMigration.setMigrationStatus(SMSMigrationStatus.DataCollectionComplete);
        smsMigration.setCompany(testCompany);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.migratePSPToSMS("123456", "1111");
        PayrollServices.commitUnitOfWork();
        assertEquals("12012", processResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void MigrateCompanyFromPSPToProfilemigrationrequestValidationFailure() {

        //create company with VMP and Assisted service
        testCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456", true, ServiceCode.DirectDeposit);

        addRealmIdToCompany();
        addComplianceAddressToCompany();

        DataLoadServices.addCompanyPIN(testCompany, null);

        //BankAccount not added, to fail the request
        //DataLoadServices.addCompanyBankAccount(testCompany)

        PayrollServices.beginUnitOfWork();

        /* Current phone number, (775) 111-1111 sanitizing to remove special characters
         to avoid CompanyHasSpecialCharPhone validation failure */
        Contact primaryPrincipalContact = testCompany.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        primaryPrincipalContact.setPhone(primaryPrincipalContact.getPhone().replaceAll(Address.ONLY_DIGITS, ""));

        BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId("123456");
        smsMigration.setMigrationStatus(SMSMigrationStatus.DataCollectionComplete);
        smsMigration.setCompany(testCompany);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.migratePSPToSMS("123456", "1111");
        PayrollServices.commitUnitOfWork();
        assertEquals("10130", processResult.getMessages().get(0).getMessageCode());
    }


    private void addRealmIdToCompany() {
        //refresh the company object
        PayrollServices.beginUnitOfWork();
        testCompany = Application.findById(Company.class, testCompany.getId());
        testCompany.setIAMRealmId("9130354965488516");
        Application.save(testCompany);
        PayrollServices.commitUnitOfWork();
    }

    private void addServiceSubStatusCode() {
        PayrollServices.beginUnitOfWork();
        testCompany = Application.findById(Company.class, testCompany.getId());
        CompanyService companyService = testCompany.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();
    }

    private void addComplianceAddressToCompany() {
        //refresh the company object
        PayrollServices.beginUnitOfWork();
        testCompany = Application.findById(Company.class, testCompany.getId());
        //Setting compliance and Legal Address to be same to avoid validation failure
        Address address = testCompany.getLegalAddress();
        testCompany.setComplianceAddress(address);
        Application.save(testCompany);
        PayrollServices.commitUnitOfWork();
    }

}

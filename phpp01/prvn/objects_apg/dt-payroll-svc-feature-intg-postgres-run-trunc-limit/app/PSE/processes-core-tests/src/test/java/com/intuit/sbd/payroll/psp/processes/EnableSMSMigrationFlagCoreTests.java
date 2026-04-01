package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BaseSMSMigration;
import com.intuit.sbd.payroll.psp.domain.EditionType;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class EnableSMSMigrationFlagCoreTests {
    private DataLoader dataloader = new DataLoader();


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
    public void EnableMigrationFlagSuccess() {

        String realm = "9130349397822666";

        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        companyDTO.setIAMRealmId(realm);

        ProcessResult<Company> result = DataLoader.addCompany(companyDTO);

        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(companyDTO.getSourceSystemCd().toString()), companyDTO.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(companyDTO.getCompanyId(), companyDTO.getSourceSystemCd());
        assertEquals("Load company", 0, result.getMessages().size());

        DataLoadServices.addEntitlementUnit(company, "123456", "654321");

        PayrollServices.beginUnitOfWork();
        BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId(companyDTO.getCompanyId());
        smsMigration.setMigrationStatus(SMSMigrationStatus.DataCollectionComplete);
        smsMigration.setCompany(company);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.enableSMSMigratedFlags("123456", "111");
        PayrollServices.commitUnitOfWork();
        assertEquals(0, processResult.getMessages().size());

        assertEquals(SMSMigrationStatus.MigrationComplete, processResult.getResult());

    }

    /*
     Expected status: MigrationError, Due to Tron grant failure
     The Tron grant failure is generated for a company with no Entitlement.
     */
    @Test
    public void EnableMigrationFlagForTronGrantFailure() {

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
        ProcessResult processResult = PayrollServices.companyManager.enableSMSMigratedFlags("123456", "111");
        PayrollServices.commitUnitOfWork();

        assertEquals(SMSMigrationStatus.MigrationError, processResult.getResult());
    }


    @Test
    public void EnableMigrationFlagValidationFailure(){
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = DataLoader.addCompany(companyDTO);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(companyDTO.getSourceSystemCd().toString()), companyDTO.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(companyDTO.getCompanyId(), companyDTO.getSourceSystemCd());
        assertEquals("Load company", 0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.enableSMSMigratedFlags("1234567", "111");
        PayrollServices.commitUnitOfWork();
        assertEquals(1, processResult.getMessages().size());
    }
}

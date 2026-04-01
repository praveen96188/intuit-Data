package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BaseSMSMigration;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RevertSMSMigrationFlagCoreTests {
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
    public void RevertMigrationFlagSuccess(){
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
        ProcessResult processResult = PayrollServices.companyManager.revertSMSMigratedCompany("123456", "111");
        PayrollServices.commitUnitOfWork();
        assertEquals(0, processResult.getMessages().size());
        assertEquals(SMSMigration.getSmsMigrationBySourceCompanyId("123456").getFirst().getMigrationStatus(), SMSMigrationStatus.MigrationReverted);
    }

    @Test
    public void RevertMigrationFlagValidationFailure(){
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = DataLoader.addCompany(companyDTO);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(companyDTO.getSourceSystemCd().toString()), companyDTO.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(companyDTO.getCompanyId(), companyDTO.getSourceSystemCd());
        assertEquals("Load company", 0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.revertSMSMigratedCompany("1234567", "111");
        PayrollServices.commitUnitOfWork();
        assertEquals(1, processResult.getMessages().size());
    }
}

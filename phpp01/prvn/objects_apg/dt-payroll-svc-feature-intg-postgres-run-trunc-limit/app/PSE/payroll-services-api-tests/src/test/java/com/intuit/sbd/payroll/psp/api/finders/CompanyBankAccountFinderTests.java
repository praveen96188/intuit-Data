package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: May 19, 2008
 * Time: 1:41:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyBankAccountFinderTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void findActiveCompanyBankAccountTest() {
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistCompany(c1dl.getCompany1());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        assertEquals("Company bank account", null, cba);



    }
}

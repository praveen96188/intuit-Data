package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.jscape.util.Assert;
import org.junit.Before;
import org.junit.Test;

public class Add401kCompanyQBDTPItemCoreValidateTests {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testAddOrUpdate401kCompanyQBDTPItemWithNullCompanyPolicy(){
        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<Hcm401kCompanyQbdtPitem> result = PayrollServices.companyManager.addOrUpdate401kCompanyQBDTPItem(null, "er_item_id", "ee_item_id");
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(result.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(result.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(result.getErrorMessages().get(0).getMessage().contains("Hcm401kCompanyPolicy"),true, "error message missing");
    }

    @Test
    public void testAddOrUpdate401kCompanyQBDTPItemWithNullEmployeePItem(){
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
        hcm401kCompanyPolicy.setCompanyId("company_id");

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<Hcm401kCompanyQbdtPitem> result = PayrollServices.companyManager.addOrUpdate401kCompanyQBDTPItem(hcm401kCompanyPolicy, "er_item_id", null);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(result.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(result.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(result.getErrorMessages().get(0).getMessage().contains("employeePItemId"),true, "error message missing");
    }
}

package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.employer.EmployerPension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AddOrUpdate401kEmployerPensionCoreValidateTest {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testAddOrUpdate401kEmployerPensionCoreWithNullEmployerPension(){
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerPension> result = PayrollServices.companyManager.addOrUpdate401kEmployerPension(company, null);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("EmployerPension"),true);
    }

    @Test
    public void testAddOrUpdate401kEmployerPensionCoreWithNullPolicy(){
        EmployerPension employerPension = new EmployerPension();
        employerPension.setActive(true);
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerPension> result = PayrollServices.companyManager.addOrUpdate401kEmployerPension(company, employerPension);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("EmployerPension.StatutoryPensionPolicy"),true);
    }

    @Test
    public void testAddOrUpdate401kEmployerPensionCoreWithUpdateCall(){
        EmployerPension employerPension = new EmployerPension();
        employerPension.setActive(true);
        employerPension.setStatutoryPensionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);
        employerPension.setId(GlobalId.create("realm_id", "local_id"));
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerPension> result = PayrollServices.companyManager.addOrUpdate401kEmployerPension(company, employerPension);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("employer Pension cannot be updated"),true);
    }

    @Test
    public void testAddOrUpdate401kEmployerPensionCoreWithNullCompany(){
        EmployerPension employerPension = new EmployerPension();
        employerPension.setActive(true);
        employerPension.setStatutoryPensionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerPension> result = PayrollServices.companyManager.addOrUpdate401kEmployerPension(null, employerPension);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("pspCompany"),true);
    }
}

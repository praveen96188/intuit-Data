package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AddOrUpdate401kEmployerDeductionCoreValidateTest {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testAddOrUpdate401kEmployerDeductionCoreWithNullEmployerDeduction(){
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerDeduction> result = PayrollServices.companyManager.addOrUpdate401kEmployerDeduction(company, null);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("EmployerDeduction"),true);
    }

    @Test
    public void testAddOrUpdate401kEmployerDeductionCoreWithNullPolicy(){
        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setAnnualMaximum(new Amount(2000));
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerDeduction> result = PayrollServices.companyManager.addOrUpdate401kEmployerDeduction(company, employerDeduction);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("EmployerDeduction.StatutoryDeductionPolicy"),true);
    }

    @Test
    public void testAddOrUpdate401kEmployerDeductionCoreWithUpdateCall(){
        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setAnnualMaximum(new Amount(2000));
        employerDeduction.setStatutoryDeductionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_LOAN);
        employerDeduction.setId(GlobalId.create("REALM_ID", "local_id"));
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerDeduction> result = PayrollServices.companyManager.addOrUpdate401kEmployerDeduction(company, employerDeduction);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("Employer Deduction cannot be updated"),true);
    }

    @Test
    public void testAddOrUpdate401kEmployerDeductionCoreWithNullCompany(){
        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setAnnualMaximum(new Amount(2000));
        employerDeduction.setStatutoryDeductionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_LOAN);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerDeduction> result = PayrollServices.companyManager.addOrUpdate401kEmployerDeduction(null, employerDeduction);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getErrorMessages().size(),1);
        System.out.println(result.getErrorMessages().get(0).getMessage());
        Assert.assertEquals(result.getErrorMessages().get(0).getMessage().contains("pspCompany"),true);
    }
}

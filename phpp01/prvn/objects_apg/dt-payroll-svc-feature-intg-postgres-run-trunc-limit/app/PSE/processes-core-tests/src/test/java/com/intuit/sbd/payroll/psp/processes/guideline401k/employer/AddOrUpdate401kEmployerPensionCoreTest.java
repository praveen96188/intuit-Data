package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.payroll.employer.EmployerPension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class AddOrUpdate401kEmployerPensionCoreTest{
    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    public EmployerPension setupAddEmployerPensionCoreSuccess(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TppoCus401K, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TppoCus401K);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_TRADITIONAL_401K);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        ApplicationSecondary.commitUnitOfWork();

        EmployerPension employerPension = new EmployerPension();
        employerPension.setActive(true);
        employerPension.setStatutoryPensionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);
        return employerPension;
    }

    @Test
    public void testAddOrUpdate401kEmployerPensionCoreSuccess(){
        EmployerPension employerPension = setupAddEmployerPensionCoreSuccess();
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerPension> result = PayrollServices.companyManager.addOrUpdate401kEmployerPension(company, employerPension);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertNotNull(result.getResult());
    }
}
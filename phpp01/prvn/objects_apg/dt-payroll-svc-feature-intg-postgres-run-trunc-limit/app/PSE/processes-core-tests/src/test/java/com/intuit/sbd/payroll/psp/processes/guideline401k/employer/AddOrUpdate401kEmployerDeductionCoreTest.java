package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class AddOrUpdate401kEmployerDeductionCoreTest{

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    public EmployerDeduction setupForAddOrUpdate401kEmployerDeductionCoreSuccess(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TdepCusLoanRepayment, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TdepCusLoanRepayment);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_401K_LOAN);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        ApplicationSecondary.commitUnitOfWork();

        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setActive(true);
        employerDeduction.setStatutoryDeductionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_LOAN);
        return employerDeduction;
    }

    @Test
    public void testAddOrUpdate401kEmployerDeductionCoreSuccess(){
        EmployerDeduction employerDeduction = setupForAddOrUpdate401kEmployerDeductionCoreSuccess();
        Company company = new Company();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployerDeduction> result = PayrollServices.companyManager.addOrUpdate401kEmployerDeduction(company, employerDeduction);
        PayrollServices.commitUnitOfWorkWithSecondary();

        Assert.assertNotNull(result.getResult());
    }
}
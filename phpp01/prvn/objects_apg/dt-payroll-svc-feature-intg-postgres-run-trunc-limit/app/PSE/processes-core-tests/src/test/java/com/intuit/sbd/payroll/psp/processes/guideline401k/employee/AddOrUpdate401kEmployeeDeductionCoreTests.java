package com.intuit.sbd.payroll.psp.processes.guideline401k.employee;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.common.Rate;
import com.intuit.v4.network.Contact;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import com.jscape.util.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class AddOrUpdate401kEmployeeDeductionCoreTests {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    public EmployeeDeduction setupAddEmployeeDeductionSuccessData(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TdepCusLoanRepayment, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TdepCusLoanRepayment);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_401K_LOAN);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        hcm401kCompanyPolicy.setHcm401kPolicy(hcm401kPolicy);
        hcm401kCompanyPolicy.setActive(true);

//        String psid = "1234567";
//        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        Company company = new Company();
        hcm401kCompanyPolicy.setCompanyId(company.getId().getStandardFormatString());
        ApplicationSecondary.save(hcm401kCompanyPolicy);
        ApplicationSecondary.commitUnitOfWork();

        EmployeeDeduction employeeDeduction = new EmployeeDeduction();
        employeeDeduction.setActive(true);

        Contact employee = new Contact();
        employee.setId(GlobalId.create("realm_id","local_id"));

        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setId(GlobalId.create("realm_id",hcm401kCompanyPolicy.getId().getStandardFormatString()));

        employeeDeduction.setEmployee(employee);
        employeeDeduction.setEmployerDeduction(employerDeduction);

        Rate rate = new Rate();
        rate.setPercent(false);
        rate.setMoneyValue(new Amount(200));
        employeeDeduction.setAmount(rate);
        employeeDeduction.setAnnualMaximum(new Amount(2000));

        return employeeDeduction;
    }

    public EmployeeDeduction setupUpdateEmployeeDeductionSuccessData(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TdepCusLoanRepayment, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TdepCusLoanRepayment);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_401K_LOAN);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        hcm401kCompanyPolicy.setHcm401kPolicy(hcm401kPolicy);
        hcm401kCompanyPolicy.setActive(true);

//        String psid = "1234567";
//        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        Company company = new Company();
        hcm401kCompanyPolicy.setCompanyId(company.getId().getStandardFormatString());
        ApplicationSecondary.save(hcm401kCompanyPolicy);

        Hcm401kEmployeeDeduction hcm401kEmployeeDeduction = new Hcm401kEmployeeDeduction();
        hcm401kEmployeeDeduction.setActive(true);
        hcm401kEmployeeDeduction.setHcm401kDeductionContributor(Hcm401kDeductionContributor.Employee);
        hcm401kEmployeeDeduction.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
        hcm401kEmployeeDeduction.setAmount(800.00);
        hcm401kEmployeeDeduction.setHcm401kAmountType(Hcm401kAmountType.Dollar);
        hcm401kEmployeeDeduction.setEmployeeId(SpcfUniqueIdImpl.generateRandomUniqueIdString());
        hcm401kEmployeeDeduction.setMaxAmount(80000.00);

        ApplicationSecondary.save(hcm401kEmployeeDeduction);
        ApplicationSecondary.commitUnitOfWork();

        EmployeeDeduction employeeDeduction = new EmployeeDeduction();
        employeeDeduction.setActive(true);

        Contact employee = new Contact();
        employee.setId(GlobalId.create("realm_id","local_id"));

        EmployerDeduction employerDeduction = new EmployerDeduction();
        employerDeduction.setId(GlobalId.create("realm_id",hcm401kCompanyPolicy.getId().getStandardFormatString()));

        employeeDeduction.setEmployee(employee);
        employeeDeduction.setEmployerDeduction(employerDeduction);

        Rate rate = new Rate();
        rate.setPercent(false);
        rate.setMoneyValue(new Amount(300));
        employeeDeduction.setAmount(rate);
        employeeDeduction.setAnnualMaximum(new Amount(4000));
        employeeDeduction.setId(GlobalId.create("realm_id", hcm401kEmployeeDeduction.getId().getStandardFormatString()));

        return employeeDeduction;
    }

    @Test
    public void testAdd401kEmployeeDeductionCoreSuccess(){
        EmployeeDeduction employeeDeduction = setupAddEmployeeDeductionSuccessData();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeeDeduction(employeeDeduction);
        PayrollServices.commitUnitOfWorkWithSecondary();

        EmployeeDeduction kEmployeeDeduction = processResult.getResult();
        Assert.notNull(kEmployeeDeduction);
        Assert.areEqual(kEmployeeDeduction.getAmount().getMoneyValue().doubleValue(), Double.valueOf("200"), "amount is wrong");
        Assert.areEqual(kEmployeeDeduction.getAnnualMaximum().doubleValue(), Double.valueOf("2000"), "max amount is wrong");

    }

    @Test
    public void testUpdate401kEmployeeDeductionCoreSuccess(){
        EmployeeDeduction employeeDeduction = setupUpdateEmployeeDeductionSuccessData();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeeDeduction(employeeDeduction);
        PayrollServices.commitUnitOfWorkWithSecondary();

        EmployeeDeduction kEmployeeDeduction = processResult.getResult();
        Assert.notNull(kEmployeeDeduction);
        Assert.areEqual(kEmployeeDeduction.getAmount().getMoneyValue().doubleValue(), Double.valueOf("300"), "amount is wrong");
        Assert.areEqual(kEmployeeDeduction.getAnnualMaximum().doubleValue(), Double.valueOf("4000"), "max amount is wrong");
    }
}

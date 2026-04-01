package com.intuit.sbd.payroll.psp.processes.guideline401k.employee;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.common.Rate;
import com.intuit.v4.network.Contact;
import com.intuit.v4.payroll.definitions.BasePensionItem;
import com.intuit.v4.payroll.employee.EmployeePension;
import com.intuit.v4.payroll.employer.EmployerPension;
import com.jscape.util.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class AddOrUpdate401kEmployeePensionCoreTests {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    public EmployeePension setupAddEmployeePensionSuccessData(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TppoCus401K, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TppoCus401K);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_TRADITIONAL_401K);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        hcm401kCompanyPolicy.setHcm401kPolicy(hcm401kPolicy);
        hcm401kCompanyPolicy.setActive(true);

//        String psid = "1234567";
////        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        Company company = new Company();
        hcm401kCompanyPolicy.setCompanyId(company.getId().getStandardFormatString());
        ApplicationSecondary.save(hcm401kCompanyPolicy);
        ApplicationSecondary.commitUnitOfWork();

        EmployeePension employeePension = new EmployeePension();
        employeePension.setActive(true);

        Contact employee = new Contact();
        employee.setId(GlobalId.create("realm_id","local_id"));

        EmployerPension employerPension = new EmployerPension();
        employerPension.setId(GlobalId.create("realm_id", hcm401kCompanyPolicy.getId().getStandardFormatString()));

        employeePension.setEmployee(employee);
        employeePension.setEmployerPension(employerPension);

        BasePensionItem.ContributionSetup employerContribution = new BasePensionItem.ContributionSetup();
        BasePensionItem.CappingType cappingType = new BasePensionItem.CappingType();
        cappingType.setAmount(new Amount(10000));
        cappingType.setFrequency(BasePensionItem.CappingType.FrequencyEnum.BYFISCALYEAR);
        employerContribution.addCappings(cappingType);

        Rate employerRate = new Rate();
        employerRate.setPercent(false);
        employerRate.setMoneyValue(new Amount(100));

        employerContribution.setAmount(employerRate);

        employeePension.setEmployerSetup(employerContribution);

        BasePensionItem.ContributionSetup employeeContribution = new BasePensionItem.ContributionSetup();
        employeeContribution.addCappings(cappingType);

        Rate employeeRate = new Rate();
        employeeRate.setPercent(false);
        employeeRate.setMoneyValue(new Amount(100));

        employeeContribution.setAmount(employeeRate);

        employeePension.setEmployeeSetup(employeeContribution);

        return employeePension;
    }

    public EmployeePension setupUpdateEmployeePensionSuccessData(){
        ApplicationSecondary.beginUnitOfWork();
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
        Hcm401kPolicy hcm401kPolicy = Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy.TppoCus401K, DeductionItemProvider.Guideline);

        if (Objects.isNull(hcm401kPolicy)) {
            hcm401kPolicy = new Hcm401kPolicy();
            hcm401kPolicy.setDeductionItemProvider(DeductionItemProvider.Guideline);
            hcm401kPolicy.setDeductionItemPolicy(DeductionItemPolicy.TppoCus401K);
            hcm401kPolicy.setDescription(Guideline401kConstants.GUIDELINE_TRADITIONAL_401K);
            ApplicationSecondary.save(hcm401kPolicy);
        }
        hcm401kCompanyPolicy.setHcm401kPolicy(hcm401kPolicy);
        hcm401kCompanyPolicy.setActive(true);

//        String psid = "1234567";
////        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        Company company = new Company();
        hcm401kCompanyPolicy.setCompanyId(company.getId().getStandardFormatString());
        ApplicationSecondary.save(hcm401kCompanyPolicy);

        Hcm401kEmployeeDeduction hcm401kEmployeeDeductionER = new Hcm401kEmployeeDeduction();
        hcm401kEmployeeDeductionER.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
        hcm401kEmployeeDeductionER.setHcm401kDeductionContributor(Hcm401kDeductionContributor.Employer);
        hcm401kEmployeeDeductionER.setEmployeeId("local_id");
        hcm401kEmployeeDeductionER.setActive(true);
        hcm401kEmployeeDeductionER.setHcm401kAmountType(Hcm401kAmountType.Dollar);
        hcm401kEmployeeDeductionER.setAmount(100.00);
        hcm401kEmployeeDeductionER.setMaxAmount(10000.00);

        Hcm401kEmployeeDeduction hcm401kEmployeeDeductionEE = new Hcm401kEmployeeDeduction();
        hcm401kEmployeeDeductionEE.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
        hcm401kEmployeeDeductionEE.setHcm401kDeductionContributor(Hcm401kDeductionContributor.Employee);
        hcm401kEmployeeDeductionEE.setEmployeeId("local_id");
        hcm401kEmployeeDeductionEE.setActive(true);
        hcm401kEmployeeDeductionEE.setHcm401kAmountType(Hcm401kAmountType.Dollar);
        hcm401kEmployeeDeductionEE.setAmount(100.00);
        hcm401kEmployeeDeductionEE.setMaxAmount(10000.00);

        ApplicationSecondary.save(hcm401kEmployeeDeductionER);
        ApplicationSecondary.save(hcm401kEmployeeDeductionEE);

        ApplicationSecondary.commitUnitOfWork();

        EmployeePension employeePension = new EmployeePension();
        employeePension.setId(GlobalId.create("Realm_id", hcm401kEmployeeDeductionEE.getId().getStandardFormatString()));
        employeePension.setActive(true);

        Contact employee = new Contact();
        employee.setId(GlobalId.create("realm_id","local_id"));

        EmployerPension employerPension = new EmployerPension();
        employerPension.setId(GlobalId.create("realm_id", hcm401kCompanyPolicy.getId().getStandardFormatString()));

        employeePension.setEmployee(employee);
        employeePension.setEmployerPension(employerPension);

        BasePensionItem.ContributionSetup employerContribution = new BasePensionItem.ContributionSetup();
        BasePensionItem.CappingType cappingType = new BasePensionItem.CappingType();
        cappingType.setAmount(new Amount(20000));
        cappingType.setFrequency(BasePensionItem.CappingType.FrequencyEnum.BYFISCALYEAR);
        employerContribution.addCappings(cappingType);

        Rate employerRate = new Rate();
        employerRate.setPercent(false);
        employerRate.setMoneyValue(new Amount(300));

        employerContribution.setAmount(employerRate);

        employeePension.setEmployerSetup(employerContribution);

        BasePensionItem.ContributionSetup employeeContribution = new BasePensionItem.ContributionSetup();
        employeeContribution.addCappings(cappingType);

        Rate employeeRate = new Rate();
        employeeRate.setPercent(false);
        employeeRate.setMoneyValue(new Amount(400));

        employeeContribution.setAmount(employeeRate);

        employeePension.setEmployeeSetup(employeeContribution);

        return employeePension;
    }

    @Test
    public void testAdd401kEmployeePensionCoreSuccess(){
        EmployeePension employeePension = setupAddEmployeePensionSuccessData();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeePension> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
        PayrollServices.commitUnitOfWorkWithSecondary();

        EmployeePension kEmployeePension = processResult.getResult();
        Assert.notNull(kEmployeePension);

    }

    @Test
    public void testUpdate401kEmployeePensionCoreSuccess(){
        EmployeePension employeePension = setupUpdateEmployeePensionSuccessData();

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeePension> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
        PayrollServices.commitUnitOfWorkWithSecondary();

        EmployeePension kEmployeePension = processResult.getResult();
        Assert.notNull(kEmployeePension);

    }
}

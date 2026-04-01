package com.intuit.sbd.payroll.psp.processes.guideline401k.employee;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.EmployeeDeductionNotFoundException;
import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.common.Rate;
import com.intuit.v4.network.Contact;
import com.intuit.v4.payroll.definitions.BasePensionItem;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employee.EmployeePension;
import com.intuit.v4.payroll.employer.EmployerPension;
import com.jscape.util.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Objects;

public class AddOrUpdate401kEmployeePensionCoreValidateTests {

    @Before
    public void runBeforeEachTest(){
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testAdd401kEmployeePensionCoreWithNullEmployeePension(){
        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeePension(null);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("EmployeePension"),true, "error message missing");
    }

    @Test
    public void testAdd401kEmployeePensionCoreWithNullEmployeeId(){
        EmployeePension employeePension = new EmployeePension();
        Contact employee = new Contact();
        employee.setCompanyName("company_name");
        employeePension.setEmployee(employee);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("EmployeeId"),true, "error message missing");
    }

    @Test
    public void testAdd401kEmployeePensionCoreWithNullEmployerCappings(){
        EmployeePension employeePension = new EmployeePension();
        Contact employee = new Contact();
        employee.setCompanyName("company_name");
        employee.setId(GlobalId.create("realm_id", "local_id"));
        employeePension.setEmployee(employee);

        BasePensionItem.ContributionSetup employerSetup = new BasePensionItem.ContributionSetup();
        BasePensionItem.CappingType cappingType = new BasePensionItem.CappingType();
        cappingType.setFrequency(BasePensionItem.CappingType.FrequencyEnum.BYFISCALYEAR);
        employerSetup.setCappings(Collections.singletonList(cappingType));
        employeePension.setEmployerSetup(employerSetup);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("CappingAmount"),true, "error message missing");
    }

    @Test
    public void testAdd401kEmployeePensionCoreWithNullEmployerPensionId(){
        EmployeePension employeePension = new EmployeePension();
        Contact employee = new Contact();
        employee.setCompanyName("company_name");
        employee.setId(GlobalId.create("realm_id", "local_id"));
        employeePension.setEmployee(employee);

        BasePensionItem.ContributionSetup employerSetup = new BasePensionItem.ContributionSetup();
        BasePensionItem.CappingType cappingType = new BasePensionItem.CappingType();
        cappingType.setFrequency(BasePensionItem.CappingType.FrequencyEnum.BYFISCALYEAR);
        cappingType.setAmount(new Amount(200));
        employerSetup.setCappings(Collections.singletonList(cappingType));
        employeePension.setEmployerSetup(employerSetup);

        EmployerPension employerPension = new EmployerPension();
        employerPension.setStatutoryPensionPolicy("stat");
        employeePension.setEmployerPension(employerPension);

        PayrollServices.beginUnitOfWorkWithSecondary();
        ProcessResult<EmployeeDeduction> processResult = PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
        PayrollServices.rollbackUnitOfWorkWithSecondary();

        Assert.areEqual(processResult.isSuccess(), false, "The call should fail for null object.");
        Assert.areEqual(processResult.getErrorMessages().size(),1, "error message missing");
        Assert.areEqual(processResult.getErrorMessages().get(0).getMessage().contains("EmployerPensionId"),true, "error message missing");
    }

    @Test (expected = EmployeeDeductionNotFoundException.class)
    public void testAdd401kEmployeePensionCoreWithNullEmployeeDeductionSet(){
        try {
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
            employeePension.setId(GlobalId.create("Realm_id", hcm401kCompanyPolicy.getId().getStandardFormatString()));
            Contact employee = new Contact();
            employee.setCompanyName("company_name");
            employee.setId(GlobalId.create("realm_id", "local_id"));
            employeePension.setEmployee(employee);

            BasePensionItem.ContributionSetup employerSetup = new BasePensionItem.ContributionSetup();
            BasePensionItem.CappingType cappingType = new BasePensionItem.CappingType();
            cappingType.setFrequency(BasePensionItem.CappingType.FrequencyEnum.BYFISCALYEAR);
            cappingType.setAmount(new Amount(200));
            employerSetup.setCappings(Collections.singletonList(cappingType));
            employeePension.setEmployerSetup(employerSetup);

            EmployerPension employerPension = new EmployerPension();
            employerPension.setStatutoryPensionPolicy(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);
            employerPension.setId(GlobalId.create("realm_id", hcm401kCompanyPolicy.getId().getStandardFormatString()));
            employeePension.setEmployerPension(employerPension);

            PayrollServices.beginUnitOfWorkWithSecondary();
            PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    @Test (expected = EmployeeDeductionNotFoundException.class)
    public void testAdd401kEmployeePensionCoreWithNullEmployeeDeduction(){
        try {
            EmployeePension employeePension = setupEmployeeContributionEmployerPensionData();

            PayrollServices.beginUnitOfWorkWithSecondary();
            PayrollServices.employeeManager.addOrUpdate401kEmployeePension(employeePension);
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    public EmployeePension setupEmployeeContributionEmployerPensionData() {
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

        ApplicationSecondary.save(hcm401kEmployeeDeductionER);
        ApplicationSecondary.commitUnitOfWork();

        EmployeePension employeePension = new EmployeePension();
        employeePension.setId(GlobalId.create("Realm_id", hcm401kEmployeeDeductionER.getId().getStandardFormatString()));
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
}

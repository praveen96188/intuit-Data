package com.intuit.sbd.payroll.psp.processes.guideline401k.employee;

import com.intuit.iam.utilities.StringUtils;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.DuplicateEntriesFoundException;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.EmployeeDeductionNotFoundException;
import com.intuit.sbd.payroll.psp.mapper.guideline401k.employee.PspEmployeeDeductionToV4ContributionSetupMapper;
import com.intuit.sbd.payroll.psp.mapper.guideline401k.employer.PspCompanyPolicyToV4EmployerPensionMapper;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.definitions.BasePensionItem;
import com.intuit.v4.payroll.employee.EmployeePension;
import com.intuit.v4.payroll.employer.EmployerPension;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AddOrUpdate401kEmployeePensionCore extends Process {

    private final EmployeePension employeePension;
    private final PspEmployeeDeductionToV4ContributionSetupMapper pspEmployeeDeductionToV4ContributionSetupMapper;
    private final PspCompanyPolicyToV4EmployerPensionMapper pspCompanyPolicyToV4EmployerPensionMapper;

    private Hcm401kEmployeeDeduction hcm401kEmployeePensionContributorEmployee;
    private Hcm401kEmployeeDeduction hcm401kEmployeePensionContributorEmployer;

    public AddOrUpdate401kEmployeePensionCore(EmployeePension employeePension) {
        this.employeePension = employeePension;
        this.pspEmployeeDeductionToV4ContributionSetupMapper = PayrollApplicationBeanFactory.getBean(PspEmployeeDeductionToV4ContributionSetupMapper.class);
        this.pspCompanyPolicyToV4EmployerPensionMapper = PayrollApplicationBeanFactory.getBean(PspCompanyPolicyToV4EmployerPensionMapper.class);
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if(Objects.isNull(employeePension)){
            validationResult.getMessages()
                    .BadProcessArgument("EmployeePension");
            return validationResult;
        }

        if(employeePension.isEmployeeSet()) {
            if(!employeePension.getEmployee().isIdSet()){
                validationResult.getMessages()
                        .BadProcessArgument("EmployeeId");
                return validationResult;
            }
        }

        if(employeePension.isEmployerSetupSet() && employeePension.getEmployerSetup().isCappingsSet()) {
            if(Objects.isNull(employeePension.getEmployerSetup().getCappings(0).getAmount())){
                validationResult.getMessages()
                        .BadProcessArgument("CappingAmount");
                return validationResult;
            }
        }

        if(employeePension.isEmployerPensionSet()){
            if(!employeePension.getEmployerPension().isIdSet()){
                validationResult.getMessages()
                        .BadProcessArgument("EmployerPensionId");
                return validationResult;
            }
        }
        if(employeePension.isIdSet()) { //update call
            Set<Hcm401kEmployeeDeduction> employeeDeductionSet = getHcm401kEmployeePensions(employeePension);
            if (CollectionUtils.isEmpty(employeeDeductionSet)) throw new EmployeeDeductionNotFoundException(String.format("Employee Deduction not found for id : %s", employeePension.getId().getLocalId()));

            Optional<Hcm401kEmployeeDeduction> optionalHcm401kEmployeeDeductionEmployee = employeeDeductionSet.stream().filter(hcm401kEmployeeDeduction ->
                    hcm401kEmployeeDeduction.getHcm401kDeductionContributor().in(Hcm401kDeductionContributor.Employee)).findFirst();

            if (!optionalHcm401kEmployeeDeductionEmployee.isPresent()) throw new EmployeeDeductionNotFoundException(String.format("Employee Deduction not found for id : %s", employeePension.getId().getLocalId()));
            hcm401kEmployeePensionContributorEmployee = optionalHcm401kEmployeeDeductionEmployee.get();


            Optional<Hcm401kEmployeeDeduction> optionalHcm401kEmployeeDeductionEmployer = employeeDeductionSet.stream().filter(hcm401kEmployeeDeduction ->
                    hcm401kEmployeeDeduction.getHcm401kDeductionContributor().in(Hcm401kDeductionContributor.Employer)).findFirst();

            hcm401kEmployeePensionContributorEmployer = optionalHcm401kEmployeeDeductionEmployer.isPresent() ? optionalHcm401kEmployeeDeductionEmployer.get() :  null;

            if(Objects.isNull(hcm401kEmployeePensionContributorEmployee)) {
                validationResult.getMessages().
                        BadProcessArgument("hcm401kEmployeePensionContributorEmployee");
                return validationResult;
            }
        } else{ //create call
            hcm401kEmployeePensionContributorEmployee = new Hcm401kEmployeeDeduction();
            if(employeePension.isEmployerPensionSet()) {
                hcm401kEmployeePensionContributorEmployer = new Hcm401kEmployeeDeduction();
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {

        if(Objects.nonNull(hcm401kEmployeePensionContributorEmployer)) {
            setPensionEntityForEmployer();
            ApplicationSecondary.save(hcm401kEmployeePensionContributorEmployer);
        }

        setPensionEntityForEmployee();
        ApplicationSecondary.save(hcm401kEmployeePensionContributorEmployee);

        ProcessResult processResult = new ProcessResult();
        processResult.setResult(getEmployeePensionFromDb());
        return processResult;
    }

    private Set<Hcm401kEmployeeDeduction> getHcm401kEmployeePensions(EmployeePension employeeDeduction) {
        String employeeId  = employeeDeduction.getEmployee().getId().getLocalId();
        String companyPolicyFK = employeeDeduction.getEmployerPension().getId().getLocalId();

        if(StringUtils.isNullOrEmpty(employeeId) || StringUtils.isNullOrEmpty(companyPolicyFK)){
            return null;
        }

        Criterion<Hcm401kEmployeeDeduction> where = (Hcm401kEmployeeDeduction.Hcm401kCompanyPolicy().
                Id().equalTo(new SpcfUniqueIdImpl(companyPolicyFK)))
                .And(Hcm401kEmployeeDeduction.EmployeeId().equalTo(employeeId));
        Expression<Hcm401kEmployeeDeduction> query = (new Query()).Where(where);

        DomainEntitySet<Hcm401kEmployeeDeduction> employerPensions = ApplicationSecondary.find(Hcm401kEmployeeDeduction.class,query);
        if (employerPensions.size() > 2) throw new DuplicateEntriesFoundException("Employee Pensions found are more than expected.");

        return  employerPensions.isEmpty() ? null: employerPensions;
    }

    private void setPensionEntityForEmployer() {

        if(employeePension.isEmployeeSet()) {
            hcm401kEmployeePensionContributorEmployer.setEmployeeId(employeePension.getEmployee().getId().getLocalId());
        }

        if(employeePension.isActiveSet()) {
            hcm401kEmployeePensionContributorEmployer.setActive(employeePension.isActive());
        }

        if(employeePension.isEmployerSetupSet()) {
            if (employeePension.getEmployerSetup().isAmountSet()) {
                if (employeePension.getEmployerSetup().getAmount().isPercent()) {
                    hcm401kEmployeePensionContributorEmployer.setAmount(employeePension.getEmployerSetup().getAmount().getPercentValue().doubleValue());
                    hcm401kEmployeePensionContributorEmployer.setHcm401kAmountType(Hcm401kAmountType.Percentage);
                } else {
                    hcm401kEmployeePensionContributorEmployer.setAmount(employeePension.getEmployerSetup().getAmount().getMoneyValue().doubleValue());
                    hcm401kEmployeePensionContributorEmployer.setHcm401kAmountType(Hcm401kAmountType.Dollar);
                }
            }

            if(employeePension.getEmployerSetup().isCappingsSet()) {
                BasePensionItem.CappingType cappingType = employeePension.getEmployerSetup().getCappings(0);
                hcm401kEmployeePensionContributorEmployer.setMaxAmount(cappingType.getAmount().doubleValue());
            }
        }

        hcm401kEmployeePensionContributorEmployer.setHcm401kDeductionContributor(Hcm401kDeductionContributor.Employer);
        if(employeePension.isEmployerPensionSet() &&  employeePension.getEmployerPension().isIdSet()) {
            Hcm401kCompanyPolicy hcm401kCompanyPolicy = ApplicationSecondary.
                    findById(Hcm401kCompanyPolicy.class, SpcfUniqueId.createInstance(employeePension.getEmployerPension().getId().getLocalId()));
            hcm401kEmployeePensionContributorEmployer.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);

        }
    }

    private void setPensionEntityForEmployee() {
        if (employeePension.isEmployeeSet()) {
            hcm401kEmployeePensionContributorEmployee.setEmployeeId(employeePension.getEmployee().getId().getLocalId());
        }

        if (employeePension.isActiveSet()) {
            hcm401kEmployeePensionContributorEmployee.setActive(employeePension.isActive());
        }

        if (employeePension.isEmployeeSetupSet()) {
            if (employeePension.getEmployeeSetup().isAmountSet()) {
                if (employeePension.getEmployeeSetup().getAmount().isPercent()) {
                    hcm401kEmployeePensionContributorEmployee.setAmount(employeePension.getEmployeeSetup().getAmount().getPercentValue().doubleValue());
                    hcm401kEmployeePensionContributorEmployee.setHcm401kAmountType(Hcm401kAmountType.Percentage);
                } else {
                    hcm401kEmployeePensionContributorEmployee.setAmount(employeePension.getEmployeeSetup().getAmount().getMoneyValue().doubleValue());
                    hcm401kEmployeePensionContributorEmployee.setHcm401kAmountType(Hcm401kAmountType.Dollar);
                }
            }
            if (employeePension.getEmployeeSetup().isCappingsSet()) {
                BasePensionItem.CappingType cappingType = employeePension.getEmployeeSetup().getCappings(0);
                hcm401kEmployeePensionContributorEmployee.setMaxAmount(cappingType.getAmount().doubleValue());
            }
        }

        hcm401kEmployeePensionContributorEmployee.setHcm401kDeductionContributor(Hcm401kDeductionContributor.Employee);

        if (employeePension.isEmployerPensionSet()) {
            Hcm401kCompanyPolicy hcm401kCompanyPolicy = ApplicationSecondary.findById(
                    Hcm401kCompanyPolicy.class, SpcfUniqueId.createInstance(employeePension.getEmployerPension().getId().getLocalId()));
            hcm401kEmployeePensionContributorEmployee.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
        }
    }

    private EmployeePension getEmployeePensionFromDb() {
        EmployeePension employeePensionFromDb = new EmployeePension();
        employeePensionFromDb.setId(GlobalId.create("", hcm401kEmployeePensionContributorEmployee.getId().toString()));
        employeePensionFromDb.setActive(hcm401kEmployeePensionContributorEmployee.getActive());

        BasePensionItem.ContributionSetup employerSetup =  pspEmployeeDeductionToV4ContributionSetupMapper.mapToTarget(
                hcm401kEmployeePensionContributorEmployer, BasePensionItem.ContributionSetup.class);

        BasePensionItem.ContributionSetup employeeSetup = pspEmployeeDeductionToV4ContributionSetupMapper.mapToTarget(
                hcm401kEmployeePensionContributorEmployee, BasePensionItem.ContributionSetup.class);

        employeePensionFromDb.setEmployerSetup(employerSetup);
        employeePensionFromDb.setEmployeeSetup(employeeSetup);

        DomainEntitySet<Hcm401kCompanyPolicy> companyPolicies = new DomainEntitySet<>();
        companyPolicies.add(hcm401kEmployeePensionContributorEmployee.getHcm401kCompanyPolicy());
        EmployerPension v4EmployerPension = pspCompanyPolicyToV4EmployerPensionMapper.mapToTarget(
                hcm401kEmployeePensionContributorEmployee.getHcm401kCompanyPolicy(), EmployerPension.class);
        employeePensionFromDb.setEmployerPension(v4EmployerPension);
        return employeePensionFromDb;
    }

}

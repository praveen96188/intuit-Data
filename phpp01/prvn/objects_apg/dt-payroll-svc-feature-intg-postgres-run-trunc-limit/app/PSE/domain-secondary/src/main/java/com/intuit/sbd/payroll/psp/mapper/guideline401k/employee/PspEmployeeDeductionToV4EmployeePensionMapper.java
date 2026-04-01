package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.PolicyDescriptionNotFoundException;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.sbd.payroll.psp.mapper.guideline401k.employer.PspCompanyPolicyToV4EmployerPensionMapper;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.definitions.BasePensionItem;
import com.intuit.v4.payroll.employee.EmployeePension;
import com.intuit.v4.payroll.employer.EmployerPension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PspEmployeeDeductionToV4EmployeePensionMapper extends BeanMapper<DomainEntitySet<Hcm401kEmployeeDeduction>, EmployeePension> {

    private final PspEmployeeDeductionToV4ContributionSetupMapper pspEmployeeDeductionToV4ContributionSetupMapper;
    private final PspCompanyPolicyToV4EmployerPensionMapper pspCompanyPolicyToV4EmployerPensionMapper;

    @Autowired
    public PspEmployeeDeductionToV4EmployeePensionMapper(PspEmployeeDeductionToV4ContributionSetupMapper pspEmployeeDeductionToV4ContributionSetupMapper,
                                                         PspCompanyPolicyToV4EmployerPensionMapper pspCompanyPolicyToV4EmployerPensionMapper) {
        this.pspEmployeeDeductionToV4ContributionSetupMapper = pspEmployeeDeductionToV4ContributionSetupMapper;
        this.pspCompanyPolicyToV4EmployerPensionMapper = pspCompanyPolicyToV4EmployerPensionMapper;
    }

    @Override
    public EmployeePension mapToTarget(DomainEntitySet<Hcm401kEmployeeDeduction> employeeEmployerPensionsPair,
                                             Class<EmployeePension> t) throws PolicyDescriptionNotFoundException {
        if(Objects.isNull(employeeEmployerPensionsPair) && !employeeEmployerPensionsPair.isEmpty()) {
            return new EmployeePension();
        }

        // EmployeeEmployerPensionsPair will have 0th index as employee and 1st index as employer, since we have sorted it based on Contributor
        // Here we are assuming that every employeeEmployerPensionsPair at-least has the employeeContributionPension
        Hcm401kEmployeeDeduction employeeContributorPension =  employeeEmployerPensionsPair.get(0);

        EmployeePension employeePension401k  = new EmployeePension();

        setEmployeeSetup(employeeContributorPension, employeePension401k);
        setEmployerSetup(employeeEmployerPensionsPair, employeePension401k);

        DomainEntitySet<Hcm401kCompanyPolicy> companyPolicies = new DomainEntitySet<>();
        companyPolicies.add(employeeContributorPension.getHcm401kCompanyPolicy());

        EmployerPension v4EmployerPension = pspCompanyPolicyToV4EmployerPensionMapper.mapToTarget(
                employeeContributorPension.getHcm401kCompanyPolicy(), EmployerPension.class);
        employeePension401k.setEmployerPension(v4EmployerPension);
        return employeePension401k;
    }

    private void setEmployeeSetup(Hcm401kEmployeeDeduction employeeContributorPension, EmployeePension employeePension401k) {
        employeePension401k.setId(GlobalId.create("", employeeContributorPension.getId().toString()));
        employeePension401k.setActive(employeeContributorPension.getActive());

        BasePensionItem.ContributionSetup employeeSetup =
                pspEmployeeDeductionToV4ContributionSetupMapper.mapToTarget(employeeContributorPension,
                        BasePensionItem.ContributionSetup.class);
        employeePension401k.setEmployeeSetup(employeeSetup);
    }

    private void setEmployerSetup(DomainEntitySet<Hcm401kEmployeeDeduction> employeeEmployerPensionsPair, EmployeePension employeePension401k) {
        Hcm401kEmployeeDeduction employerContributorPension = employeeEmployerPensionsPair.size() > 1 ? employeeEmployerPensionsPair.get(1) : null;

        if(Objects.nonNull(employerContributorPension)) {
            BasePensionItem.ContributionSetup employerSetup =
                    pspEmployeeDeductionToV4ContributionSetupMapper.mapToTarget(employerContributorPension,
                            BasePensionItem.ContributionSetup.class);
            employeePension401k.setEmployerSetup(employerSetup);
        }
    }
}

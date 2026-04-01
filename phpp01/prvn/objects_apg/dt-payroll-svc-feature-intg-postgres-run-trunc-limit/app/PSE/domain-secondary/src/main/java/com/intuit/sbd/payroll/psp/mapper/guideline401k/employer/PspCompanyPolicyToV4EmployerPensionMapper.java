package com.intuit.sbd.payroll.psp.mapper.guideline401k.employer;

import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemProvider;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.DeductionItemPolicyNotFoundException;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.PolicyDescriptionNotFoundException;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.employer.EmployerPension;
import org.springframework.stereotype.Component;

@Component
public class PspCompanyPolicyToV4EmployerPensionMapper extends BeanMapper<Hcm401kCompanyPolicy, EmployerPension> {

    @Override
    public EmployerPension mapToTarget(Hcm401kCompanyPolicy hcm401kCompanyPolicy, Class<EmployerPension> t)
            throws PolicyDescriptionNotFoundException {
        EmployerPension employerPension401k = new EmployerPension();

        if(!hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy().isPension()) {
            throw new DeductionItemPolicyNotFoundException("deductionItemPolicy not found");
        }

        setEmployerPensionProperties(hcm401kCompanyPolicy, employerPension401k);

        return employerPension401k;
    }

    private void setEmployerPensionProperties(Hcm401kCompanyPolicy hcm401kCompanyPolicy, EmployerPension employerPension401k) {
        DeductionItemPolicy deductionItemPolicy = hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy();

        employerPension401k.setId(GlobalId.create("", hcm401kCompanyPolicy.getId().toString()));
        employerPension401k.setStatutoryPensionPolicy(
                DeductionItemPolicy.getStatutoryPolicyName(deductionItemPolicy.name()));
        employerPension401k.setName(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(deductionItemPolicy, DeductionItemProvider.Guideline).getDescription());
    }
}

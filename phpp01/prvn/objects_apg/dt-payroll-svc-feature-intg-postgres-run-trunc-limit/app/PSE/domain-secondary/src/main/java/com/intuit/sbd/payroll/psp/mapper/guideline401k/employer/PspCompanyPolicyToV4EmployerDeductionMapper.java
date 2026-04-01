package com.intuit.sbd.payroll.psp.mapper.guideline401k.employer;

import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemProvider;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.DeductionItemPolicyNotFoundException;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.PolicyDescriptionNotFoundException;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import org.springframework.stereotype.Component;

@Component
public class PspCompanyPolicyToV4EmployerDeductionMapper extends BeanMapper<Hcm401kCompanyPolicy, EmployerDeduction> {

    @Override
    public EmployerDeduction mapToTarget(Hcm401kCompanyPolicy hcm401kCompanyPolicy, Class<EmployerDeduction> t)
            throws PolicyDescriptionNotFoundException {
        EmployerDeduction employerDeduction401k = new EmployerDeduction();
        if(!hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy().isDeduction()) {
            throw new DeductionItemPolicyNotFoundException("deductionItemPolicy not found");
        }

        setEmployerDeductionProperties(hcm401kCompanyPolicy, employerDeduction401k);

        return employerDeduction401k;
    }

    private void setEmployerDeductionProperties(Hcm401kCompanyPolicy hcm401kCompanyPolicy, EmployerDeduction employerDeduction401k) {
        DeductionItemPolicy deductionItemPolicy = hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy();
        employerDeduction401k.setId(GlobalId.create("", hcm401kCompanyPolicy.getId().toString()));
        employerDeduction401k.setStatutoryDeductionPolicy(
                DeductionItemPolicy.getStatutoryPolicyName(deductionItemPolicy.name()));
        employerDeduction401k.setName(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(deductionItemPolicy, DeductionItemProvider.Guideline).getDescription());
    }
}

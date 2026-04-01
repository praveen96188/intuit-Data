package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.company.model.TaxRateCDM;
import com.intuit.payroll.api.employee.model.EmployeeTaxItemCDM;
import com.intuit.sbd.payroll.psp.domain.CompanyLawRate;
import com.intuit.sbd.payroll.psp.domain.EmployeeTax;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.EmployeeTaxToEmployeeTaxItemCDMMapper")
public class EmployeeTaxToEmployeeTaxItemCDMMapper extends BeanMapper<EmployeeTax, EmployeeTaxItemCDM> {
    @Override
    public EmployeeTaxItemCDM mapToTarget(EmployeeTax employeeTax, Class<EmployeeTaxItemCDM> target) {
        if(Objects.isNull(employeeTax)){
            return null;
        }
        EmployeeTaxItemCDM employeeTaxItemCDM = new EmployeeTaxItemCDM();
        employeeTaxItemCDM.setTaxId(employeeTax.getEmployee().getTaxId());
        //TODO: Need mapping for the TaxRateCDM
        if (Objects.nonNull(employeeTax.getCompanyLaw()) && Objects.nonNull(employeeTax.getCompanyLaw().getCompanyLawRateCollection())) {
            for (CompanyLawRate companyLawRate : employeeTax.getCompanyLaw().getCompanyLawRateCollection()) {
                employeeTaxItemCDM.getTaxRates().add(getMapper().mapToTarget(companyLawRate, TaxRateCDM.class));
            }
        }
        return employeeTaxItemCDM;
    }
}
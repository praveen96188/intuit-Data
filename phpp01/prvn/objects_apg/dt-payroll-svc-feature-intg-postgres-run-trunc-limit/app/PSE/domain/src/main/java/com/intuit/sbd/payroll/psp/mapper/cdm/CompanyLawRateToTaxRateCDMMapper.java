package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.company.model.TaxRateCDM;
import com.intuit.sbd.payroll.psp.domain.CompanyLawRate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.CompanyLawRateToTaxRateCDMMapper")
public class CompanyLawRateToTaxRateCDMMapper extends BeanMapper<CompanyLawRate, TaxRateCDM> {

    @Override
    public TaxRateCDM mapToTarget(CompanyLawRate companyLawRate, Class<TaxRateCDM> target) {
        if(Objects.isNull(companyLawRate)){
            return null;
        }
        TaxRateCDM taxRateCDM = new TaxRateCDM();
        taxRateCDM.setRate(Objects.nonNull(companyLawRate.getRate())? BigDecimal.valueOf(companyLawRate.getRate()) : null);
        //TODO: Need mapping for the setPercentageOfCompositeTaxRate
//        target.setPercentageOfCompositeTaxRate(source.getRateType());
        return taxRateCDM;
    }
}
package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.LawCategoryCode;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * User: shivnandad069
 * Date: 1/15/14
 */
public class KY_RateConverter implements IRateConverter {

    public static final BigDecimal KY_SURCHARGE_RATE_PCT = new BigDecimal("0.21");  //0.21%
    public static final String KY_SURCHARGE_LAW_ID = "202";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Pre-load the map with 0.0% rates for all SUI laws for this state.
        DomainEntitySet<Law> laws = Application.find(Law.class, Law.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI)
                                                                   .And(Law.PaymentTemplate().PaymentTemplateCd().like(state + "-%"))
                                                                   .And(Law.LawCategoryCode().in(LawCategoryCode.UnemploymentEmployer, LawCategoryCode.Supplemental))
                                                                   .And(Law.LawId().notIn(DefaultRateConverter.getExcludeDefaultZeroRateLawIds()))
                                                                   .And(Law.LawId().notIn(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE)));
        for (Law law : laws) {
            rates.put(law, BigDecimal.ZERO);
        }

        // Store the base rate.
        Law law = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(law, baseRate);

        // Iterate over the supplemental rates.
        if (supplementalRates != null) {
            for (String taxItem : supplementalRates.keySet()) {

                // Find the associated law.
                String lawId = TaxItemLawMap.getLawId(state, taxItem);
                if (lawId != null) {
                    law = Application.findById(Law.class, lawId);

                    // Some tax items are combined into single laws.
                    BigDecimal combinedRate = rates.get(law);
                    if (combinedRate == null) {
                        combinedRate = supplementalRates.get(taxItem);
                    } else {
                        combinedRate = combinedRate.add(supplementalRates.get(taxItem));
                    }
                    rates.put(law, combinedRate);
                }
            }
        }
        //If supplement law id inactive , then return

        // If the base rate is greater than 0, create the supplemental rate.
        Law surchargeLaw = Application.findById(Law.class, KY_SURCHARGE_LAW_ID);

        if(!rates.containsKey(surchargeLaw))   {
            return rates;
        }
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
            rates.put(surchargeLaw, KY_SURCHARGE_RATE_PCT);
        } else {
            // Else the surcharge rate as 0.0%.
            rates.put(surchargeLaw, BigDecimal.ZERO);
        }


        return rates;
    }

}

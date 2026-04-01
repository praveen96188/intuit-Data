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
 * User: TimothyD698
 * Date: 2/18/13
 */
public class DefaultRateConverter implements IRateConverter {

    // Exclude the following Law IDs from having a default 0.0% rate created when they are not specified in the JSON file.
    private static String[] excludeDefaultZeroRateLawIds = { "168", "170", "174", "176" };




    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Pre-load the map with 0.0% rates for all SUI laws for this state.
        DomainEntitySet<Law> laws = Application.find(Law.class, Law.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI)
                                                                   .And(Law.PaymentTemplate().PaymentTemplateCd().like(state + "-%"))
                                                                   .And(Law.LawCategoryCode().in(LawCategoryCode.UnemploymentEmployer, LawCategoryCode.Supplemental))
                                                                   .And(Law.LawId().notIn(excludeDefaultZeroRateLawIds))
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

        return rates;
    }
    public static String[] getExcludeDefaultZeroRateLawIds() {
        return excludeDefaultZeroRateLawIds;
    }
}

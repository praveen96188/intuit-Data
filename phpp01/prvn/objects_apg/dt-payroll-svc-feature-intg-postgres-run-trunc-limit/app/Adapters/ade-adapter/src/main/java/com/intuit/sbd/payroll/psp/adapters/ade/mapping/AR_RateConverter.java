package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.domain.Law;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 4/3/13
 */
public class AR_RateConverter implements IRateConverter {

    public static final BigDecimal AR_STABILIZATION_RATE_PCT = new BigDecimal("0.8");
    public static final String AR_STABILIZATION_LAW_ID = "145";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Save the base rate.
        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        //If supplement law id inactive , then return
        if(RateConverterFactory.isInActiveLaw(AR_STABILIZATION_LAW_ID))   {
            return rates;
        }
        // If the base rate is greater than 0, create the supplemental rate.
        Law stabilizationLaw = Application.findById(Law.class, AR_STABILIZATION_LAW_ID);
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
            rates.put(stabilizationLaw, AR_STABILIZATION_RATE_PCT);
        } else {
            // Else the supplemental rate as 0.0%.
            rates.put(stabilizationLaw, BigDecimal.ZERO);
        }

        return rates;
    }
}

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
public class NY_RateConverter implements IRateConverter {

    public static final BigDecimal NY_SUPPLEMENTAL_RSF_RATE_PCT = new BigDecimal("0.075");
    public static final String NY_SUPPLEMENTAL_RSF_LAW_ID = "152";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();


        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));

        // If the base rate is greater than the pre-defined supplemental rate value, subtract the supplemental
        // rate from the base rate.
        Law supLaw = Application.findById(Law.class, NY_SUPPLEMENTAL_RSF_LAW_ID);
        if (baseRate.compareTo(NY_SUPPLEMENTAL_RSF_RATE_PCT) > 0) {
            baseRate = baseRate.subtract(NY_SUPPLEMENTAL_RSF_RATE_PCT);
            rates.put(supLaw, NY_SUPPLEMENTAL_RSF_RATE_PCT);
        } else {
            rates.put(supLaw, BigDecimal.ZERO);
        }

        // Save the remaining base rate value.
        rates.put(baseLaw, baseRate);

        return rates;
    }
}

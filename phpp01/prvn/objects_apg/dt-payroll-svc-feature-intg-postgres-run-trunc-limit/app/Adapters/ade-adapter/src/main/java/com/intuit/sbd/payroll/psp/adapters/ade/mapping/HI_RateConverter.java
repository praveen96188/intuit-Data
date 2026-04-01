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
public class HI_RateConverter implements IRateConverter {

    public static final BigDecimal HI_ETA_RATE_PCT = new BigDecimal("0.01");
    public static final String HI_ETA_LAW_ID = "155";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Save the base rate.
        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        // If the base rate is greater than 0, create an Employment and Training Assessment (ETA) rate.
        Law supLaw = Application.findById(Law.class, HI_ETA_LAW_ID);
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
            rates.put(supLaw, HI_ETA_RATE_PCT);
        } else {
            // Else the supplemental rate is 0.0%.
            rates.put(supLaw, BigDecimal.ZERO);
        }

        return rates;
    }
}

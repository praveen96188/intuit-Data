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
public class MN_RateConverter implements IRateConverter {

    public static final BigDecimal MN_DWA_RATE_PCT = new BigDecimal("0.1");
    public static final String MN_DWA_LAW_ID = "158";

    // Minnesota base rate includes an additional Federal Assessment (currently 14%).
    public static final BigDecimal MN_FEDERAL_ASSESSMENT_FACTOR = new BigDecimal("1.14");

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // If the base rate is greater than 0, create a non-zero "Dislocated Worker Assessment" (DWA) rate.
        Law supLaw = Application.findById(Law.class, MN_DWA_LAW_ID);
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
            rates.put(supLaw, MN_DWA_RATE_PCT);
        } else {
            rates.put(supLaw, BigDecimal.ZERO);
        }

        // Save the base rate including the Federal Assessment. The MN UI Additional Assessment is not in effect for 2014. It is a 0% rate.
        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        return rates;
    }
}

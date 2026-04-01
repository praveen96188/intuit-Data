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
public class RI_RateConverter implements IRateConverter {

    public static final BigDecimal RI_JDF_RATE_PCT = new BigDecimal("0.21");
    public static final String RI_JDF_LAW_ID = "160";
    public static final BigDecimal RI_WBI_RATE_PCT = new BigDecimal("1.2");
    public static final String RI_WBI_LAW_ID = "199";
    public static final BigDecimal RI_THRESHOLD_UI_RATE_FOR_WBI = new BigDecimal("9.79");

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Save the base rate.
        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        // If the base rate is greater than 0, create the supplemental rates.
        Law jdfLaw = Application.findById(Law.class, RI_JDF_LAW_ID);
        Law wbiLaw = Application.findById(Law.class, RI_WBI_LAW_ID);
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
            rates.put(jdfLaw, RI_JDF_RATE_PCT);
        } else {
            // Else the supplemental rates are 0.0%.
            rates.put(jdfLaw, BigDecimal.ZERO);
        }
        if (baseRate.compareTo(RI_THRESHOLD_UI_RATE_FOR_WBI) >= 0) {
            rates.put(wbiLaw, baseRate.add(RI_JDF_RATE_PCT));
        } else {
            // Else the supplemental rates are 0.0%.
            rates.put(wbiLaw, BigDecimal.ZERO);
        }

        return rates;
    }
}

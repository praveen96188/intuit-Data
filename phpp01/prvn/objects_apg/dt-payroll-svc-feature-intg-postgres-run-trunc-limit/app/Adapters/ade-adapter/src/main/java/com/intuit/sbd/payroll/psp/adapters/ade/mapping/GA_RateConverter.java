package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.domain.Law;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 2/18/13
 */
public class GA_RateConverter implements IRateConverter {

    public static final BigDecimal GA_SUP_RATE_EXEMPT_MIN_PCT = new BigDecimal("0.04");
    public static final BigDecimal GA_SUP_RATE_EXEMPT_MAX_PCT = new BigDecimal("8.1");
    public static final BigDecimal GA_SUP_ADMINISTRATION_ACCESS_RATE_PCT = new BigDecimal("0.06");
    public static final String GA_SUP_ADMINISTRATION_ACCESS_LAW_ID = "154";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        BigDecimal suppRate = BigDecimal.ZERO;

        // If the base rate is not at the defined min or max, extract the "Administration Access" rate.
        if (GA_SUP_RATE_EXEMPT_MIN_PCT.compareTo(baseRate) != 0 && GA_SUP_RATE_EXEMPT_MAX_PCT.compareTo(baseRate) != 0) {
            suppRate = GA_SUP_ADMINISTRATION_ACCESS_RATE_PCT;
            baseRate = baseRate.subtract(suppRate);
        }

        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        Law suppLaw = Application.findById(Law.class, GA_SUP_ADMINISTRATION_ACCESS_LAW_ID);
        rates.put(suppLaw, suppRate);

        return rates;
    }
}

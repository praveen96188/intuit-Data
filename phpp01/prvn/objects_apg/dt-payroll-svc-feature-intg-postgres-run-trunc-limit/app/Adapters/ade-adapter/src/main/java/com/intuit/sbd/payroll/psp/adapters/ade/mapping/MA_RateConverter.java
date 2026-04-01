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
public class MA_RateConverter implements IRateConverter {

    public static final BigDecimal MA_WTF_RATE_PCT = new BigDecimal("0.056");
    public static final String MA_WTF_LAW_ID = "150";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();

        // Save the base rate.
        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        // If the base rate is greater than 0, create a Workforce Training Fund (WTF) rate.
        Law supLaw = Application.findById(Law.class, MA_WTF_LAW_ID);
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
             rates.put(supLaw, MA_WTF_RATE_PCT);
        } else {
            rates.put(supLaw, BigDecimal.ZERO);
        }

        return rates;
    }
}

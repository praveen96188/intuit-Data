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
public class ID_RateConverter implements IRateConverter {

    public static final BigDecimal ID_WDF_RATE_PCT = new BigDecimal("0.03");
    public static final String ID_WDF_LAW_ID = "146";

    public Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates) {
        Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();
        BigDecimal wdfRate;

        // If the base rate is greater than 0, create an Workforce Development Fund (WDF) rate.
        if (baseRate.compareTo(BigDecimal.ZERO) > 0) {
            // WDF is 3% of base rate which is then subtracted from the base rate.
            wdfRate = baseRate.multiply(ID_WDF_RATE_PCT);
            baseRate = baseRate.subtract(wdfRate);
        } else {
            // Else the supplemental rate is 0.0%.
            wdfRate = BigDecimal.ZERO;
        }

        // Save the base rate.
        Law baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
        rates.put(baseLaw, baseRate);

        // Save the supplemental (WDF) rate.
        Law supLaw = Application.findById(Law.class, ID_WDF_LAW_ID);
        rates.put(supLaw, wdfRate);

        return rates;
    }
}

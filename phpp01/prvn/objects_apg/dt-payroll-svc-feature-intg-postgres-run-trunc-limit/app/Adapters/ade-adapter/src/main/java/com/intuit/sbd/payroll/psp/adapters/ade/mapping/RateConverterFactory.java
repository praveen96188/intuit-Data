package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 2/20/13
 */
public class RateConverterFactory {
    public static String[] INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE = {"145", "149", "183", "157", "165", "166", "178", "148", "190", "55", "175", "180", "151", "163", "184", "171"};

    public static IRateConverter createInstance(String state) {
        return createInstance(state, false);
    }

    public static IRateConverter createInstance(String state, boolean isAdeCepSuiFlow) {

        // Special case rate converters.
        if ("AR".equals(state)) {
            return new AR_RateConverter();
        } else if ("GA".equals(state)) {
            return new GA_RateConverter();
        } else if ("HI".equals(state)) {
            return new HI_RateConverter();
        } else if ("ID".equals(state)) {
            return new ID_RateConverter();
        } else if ("KY".equals(state)) {
            return new KY_RateConverter();
        } else if ("MA".equals(state)) {
            return new MA_RateConverter();
        } else if ("MN".equals(state)) {
            return new MN_RateConverter();
        } else if ("NY".equals(state)) {
            return new NY_RateConverter();
        } else if ("RI".equals(state)) {
            return new RI_RateConverter();
        } else if (isAdeCepSuiFlow) {
            return new AdeCepDefaultRateConverter();
        } else {
            return new DefaultRateConverter();
        }
    }


    public static boolean isInActiveLaw(String lawid) {
        for (String lawId : RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE) {
            if (lawId.equals(lawid)) {
                return true;
            }
        }
        return false;
    }
}

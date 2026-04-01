package com.intuit.sbd.payroll.psp.adapters.ade.tools;

import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.LawRateRange;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * User: TimothyD698
 * Date: 4/3/13
 */
public class ADERateUtils {
    private static final int DEFAULT_PRECISION = 7;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static BigDecimal convertPercentageToDecimal(Law law, BigDecimal percentage) {
        return applyPrecision(law, percentage.divide(ONE_HUNDRED));
    }

    private static BigDecimal applyPrecision(Law law, BigDecimal val) {
        // Determine the precision based on the law.
        int precision = DEFAULT_PRECISION;
        LawRateRange range = law.getLawRateRange();
        if (range != null) {
            precision = range.getPrecision();
        }

        return applyPrecision(val, precision);
    }

    public static void verifyDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

    public static BigDecimal convertPercentageToDecimal(BigDecimal percentage) {
        return applyPrecision(percentage.divide(ONE_HUNDRED));
    }

    public static BigDecimal applyPrecision(BigDecimal val) {
        return applyPrecision(val, DEFAULT_PRECISION);
    }

    public static BigDecimal applyPrecision(BigDecimal val, int precision) {
        if(val.compareTo(BigDecimal.ZERO) != 0) {
            return val.setScale(precision, RoundingMode.HALF_UP);
        } else  {
            return val;
        }
    }

}

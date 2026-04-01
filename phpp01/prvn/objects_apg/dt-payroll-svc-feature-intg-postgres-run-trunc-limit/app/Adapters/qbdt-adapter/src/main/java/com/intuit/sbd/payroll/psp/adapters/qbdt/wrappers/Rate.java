package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: ankitaa186
 * Date: 9/16/13
 * Time: 1:52 AM
 */
public class Rate {

    private Double rate;
    private QbdtNumericType rateType;

    public Rate(Double pRate, QbdtNumericType pRateType) {
        this.rate = pRate;
        //default rate type is a percentage(if null is passed as rate type)
        this.rateType = (pRateType == null ? QbdtNumericType.Percentage : pRateType);
    }

    /**
     * Determines the rate type based on the input ofx rate string
     * Sample input/output:
     * Input           Output
     * "$-7.00"        MoneyType
     * "-7.5%"         Percentage
     * "-7.5"          Null
     *
     * @param rateStringFromOFX - String containing rate OFX
     * @return QbdtNumericType
     */
    public static QbdtNumericType getRateType(String rateStringFromOFX) {
        if (StringUtils.isEmpty(rateStringFromOFX)) {
            return null;
        }
        if (rateStringFromOFX.startsWith("$")) {
            return QbdtNumericType.MoneyType;
        } else if (rateStringFromOFX.endsWith("%")) {
            return QbdtNumericType.Percentage;
        }
        return null;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double pRate) {
        if (pRate != null) {
            rate = pRate;
        }
    }

    public QbdtNumericType getRateType() {
        return rateType;
    }

    public void setRateType(QbdtNumericType pRateType) {
        rateType = pRateType;
    }
}

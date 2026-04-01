package com.intuit.sbd.payroll.psp.api.dtos;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jan 18, 2010
 * Time: 11:58:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class CheckPrintPaycheckEarningLineDTO extends CheckPrintPaycheckLineDTO {
    private BigDecimal rate = ZERO_BIGDECIMAL;
    private BigDecimal hours = ZERO_BIGDECIMAL;
    private String rateType = " ";

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;        
    }

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }
}
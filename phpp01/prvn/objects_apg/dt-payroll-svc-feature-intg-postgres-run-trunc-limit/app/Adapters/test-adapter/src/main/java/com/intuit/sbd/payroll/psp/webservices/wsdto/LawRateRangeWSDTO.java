package com.intuit.sbd.payroll.psp.webservices.wsdto;

/**
 * User: dweinberg
 * Date: 3/13/13
 * Time: 3:48 PM
 */
public class LawRateRangeWSDTO {
    public String lawId;
    public Double min;
    public Double max;
    public Integer precision;

    public LawRateRangeWSDTO() {
    }

    public LawRateRangeWSDTO(String pLawId, Double pMin, Double pMax, Integer pPrecision) {
        lawId = pLawId;
        this.min = pMin;
        this.max = pMax;
        precision = pPrecision;
    }
}

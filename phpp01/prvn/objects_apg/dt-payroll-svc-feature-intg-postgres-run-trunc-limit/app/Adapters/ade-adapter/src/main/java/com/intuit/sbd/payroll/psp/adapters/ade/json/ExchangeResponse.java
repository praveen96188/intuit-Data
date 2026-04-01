package com.intuit.sbd.payroll.psp.adapters.ade.json;

import java.math.BigDecimal;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 2/11/13
 */
public class ExchangeResponse {

    private String companyId;
    private String companyName;
    private String fein;
    private String stateEin;
    private Map<String, BigDecimal> taxRate;
    private BigDecimal uiRate;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String pCompanyId) {
        companyId = pCompanyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    public String getFein() {
        return fein;
    }

    public void setFein(String pFein) {
        fein = pFein;
    }

    public String getStateEin() {
        return stateEin;
    }

    public void setStateEin(String pStateEin) {
        stateEin = pStateEin;
    }

    public Map<String, BigDecimal> getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Map<String, BigDecimal> pTaxRate) {
        taxRate = pTaxRate;
    }

    public BigDecimal getUiRate() {
        return uiRate;
    }

    public void setUiRate(BigDecimal pUiRate) {
        uiRate = pUiRate;
    }
}

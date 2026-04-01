package com.intuit.sbd.payroll.psp.adapters.ade.json;

import java.util.Date;
import java.util.List;

/**
 * User: TimothyD698
 * Date: 2/11/13
 */
public class AgencyRates {
    private String id;
    private String clientRequestId;
    private String exchangeType;
    private String filename;
    private String jurisdiction;
    private String effectiveDate;
    private String year;
    private String quarter;
    private String sourceSystem;
    private String transmissionId;
    private List<ExchangeResponse> exchangeResponse;

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String pClientRequestId) {
        clientRequestId = pClientRequestId;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String pExchangeType) {
        exchangeType = pExchangeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String pFilename) {
        filename = pFilename;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String pJurisdiction) {
        jurisdiction = pJurisdiction;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String pSourceSystem) {
        sourceSystem = pSourceSystem;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String pYear) {
        year = pYear;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String pQuarter) {
        quarter = pQuarter;
    }

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setTransmissionId(String pTransmissionId) {
        transmissionId = pTransmissionId;
    }

    public List<ExchangeResponse> getExchangeResponse() {
        return exchangeResponse;
    }

    public void setExchangeResponse(List<ExchangeResponse> pExchangeResponse) {
        exchangeResponse = pExchangeResponse;
    }
}

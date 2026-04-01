package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 15, 2008
 * Time: 10:37:41 AM
 */
public class SAPTransmission {
    private long requestToken;
    private long responseToken;
    private Date initializeDateTime;
    private String description;
    private String transmissionIdentifier;
    private double connectionTime;
    private String requestDocument;
    private String responseDocument;
    private String ipAddress;
    private String companyName;
    private SAPCompanyKey companyKey;
    private Boolean largerLog;
    private String psid;
    private String loginTime;


    public long getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(long requestToken) {
        this.requestToken = requestToken;
    }

    public long getResponseToken() {
        return responseToken;
    }

    public void setResponseToken(long responseToken) {
        this.responseToken = responseToken;
    }

    public Date getInitializeDateTime() {
        return initializeDateTime;
    }

    public void setInitializeDateTime(Date initializeDateTime) {
        this.initializeDateTime = initializeDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransmissionIdentifier() {
        return transmissionIdentifier;
    }

    public void setTransmissionIdentifier(String transmissionIdentifier) {
        this.transmissionIdentifier = transmissionIdentifier;
    }

    public double getConnectionTime() {
        return connectionTime;
    }

    public void setConnectionTime(double connectionTime) {
        this.connectionTime = connectionTime;
    }

    public String getRequestDocument() {
        return requestDocument;
    }

    public void setRequestDocument(String requestDocument) {
        this.requestDocument = requestDocument;
    }

    public String getResponseDocument() {
        return responseDocument;
    }

    public void setResponseDocument(String responseDocument) {
        this.responseDocument = responseDocument;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    public SAPCompanyKey getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(SAPCompanyKey pCompanyKey) {
        companyKey = pCompanyKey;
    }

    public Boolean getLargerLog() {
        return largerLog;
    }

    public void setLargerLog(Boolean pLargerLog) {
        largerLog = pLargerLog;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }
    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }
}

package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dmehta2
 * Date: 07/06/2023
 * Time: 1:20 PM
 */
public class SAPCompanyUnprocessedRequest {
    private String companyLegalName;
    private int requestCount;


    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}

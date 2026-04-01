package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

/**
 @author Jeff Jones
 */

public class RSService {
    private String serviceCd;
    private String statusCd;

    public String getServiceCd() {
        return serviceCd;
    }

    public void setServiceCd(String serviceCd) {
        this.serviceCd = serviceCd;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }
}

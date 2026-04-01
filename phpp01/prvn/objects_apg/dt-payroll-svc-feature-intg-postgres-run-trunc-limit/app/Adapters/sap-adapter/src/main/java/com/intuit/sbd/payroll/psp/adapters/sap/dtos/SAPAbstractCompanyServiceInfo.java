package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.ServiceStatusCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

/**
 * User: dweinberg
 * Date: Jul 9, 2009
 * Time: 5:18:04 PM
 */
public abstract class SAPAbstractCompanyServiceInfo {

    protected ServiceStatusCode serviceStatusCd;
    protected ServiceSubStatusCode serviceSubStatusCd;

    public ServiceStatusCode getServiceStatusCd() {
        return serviceStatusCd;
    }

    public void setServiceStatusCd(ServiceStatusCode serviceStatusCd) {
        this.serviceStatusCd = serviceStatusCd;
    }

    public ServiceSubStatusCode getServiceSubStatusCd() {
        return serviceSubStatusCd;
    }

    public void setServiceSubStatusCd(ServiceSubStatusCode serviceSubStatusCd) {
        this.serviceSubStatusCd = serviceSubStatusCd;
    }
}

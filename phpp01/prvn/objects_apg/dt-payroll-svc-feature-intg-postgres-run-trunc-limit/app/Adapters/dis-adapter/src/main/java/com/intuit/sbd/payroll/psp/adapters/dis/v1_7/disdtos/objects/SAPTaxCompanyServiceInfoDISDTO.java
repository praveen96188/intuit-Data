package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import com.intuit.sbd.payroll.psp.domain.ServiceStatusCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPTaxCompanyServiceInfoDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPTaxCompanyServiceInfo")
public class SAPTaxCompanyServiceInfoDISDTO {
    @XmlElement(name = "ServiceStatusCd")
    private ServiceStatusCode serviceStatusCd;

    public ServiceStatusCode getServiceStatusCd() {
        return serviceStatusCd;
    }

    public void setServiceStatusCd(ServiceStatusCode serviceStatusCd) {
        this.serviceStatusCd = serviceStatusCd;
    }

    @XmlElement(name = "ServiceSubStatusCode")
    private ServiceSubStatusCode serviceSubStatusCd;

    public ServiceSubStatusCode getServiceSubStatusCd() {
        return serviceSubStatusCd;
    }

    public void setServiceSubStatusCd(ServiceSubStatusCode serviceSubStatusCd) {
        this.serviceSubStatusCd = serviceSubStatusCd;
    }

    @XmlElement(name = "Offering")
    private ServiceSubStatusCode offering;

    public ServiceSubStatusCode getOffering() {
        return offering;
    }

    public void setOffering(ServiceSubStatusCode offering) {
        this.offering = offering;
    }
}

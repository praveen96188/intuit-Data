package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "serviceKey",
    "status"})
public class EwsQueryServiceKeyItem {

    @XmlElement(name = "ServiceKey", required = false)
    protected String serviceKey;

    @XmlElement(name = "Status", required = false)
    protected String status;

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

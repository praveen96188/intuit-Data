package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEntitlementUnitStatusCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 8/31/12
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "serviceKey",
        "diskDeliveryKey",
        "status",
        "ewsEntitlementResponse"
})
public class EwsEntitlementUnitResponse implements Cloneable {

    @XmlElement(name = "ServiceKey", required = false)
    protected String serviceKey;

    @XmlElement(name = "DiskDeliveryKey", required = false)
    protected String diskDeliveryKey;

    @XmlElement(name = "Status", required = false)
    protected EwsEntitlementUnitStatusCode status;

    @XmlElement(name = "EntitlementResponse", required = false)
    protected EwsEntitlementResponse ewsEntitlementResponse;

    public EwsEntitlementUnitResponse() {
        super();
    }

    public EwsEntitlementUnitResponse clone() throws CloneNotSupportedException {
        EwsEntitlementUnitResponse clone = (EwsEntitlementUnitResponse) super.clone();

        if (ewsEntitlementResponse != null) {
            clone.setEwsEntitlementResponse(ewsEntitlementResponse.clone());
        }

        return clone;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getDiskDeliveryKey() {
        return diskDeliveryKey;
    }

    public void setDiskDeliveryKey(String diskDeliveryKey) {
        this.diskDeliveryKey = diskDeliveryKey;
    }

    public EwsEntitlementUnitStatusCode getStatus() {
        return status;
    }

    public void setStatus(EwsEntitlementUnitStatusCode status) {
        this.status = status;
    }

    public EwsEntitlementResponse getEwsEntitlementResponse() {
        return ewsEntitlementResponse;
    }

    public void setEwsEntitlementResponse(EwsEntitlementResponse ewsEntitlementResponse) {
        this.ewsEntitlementResponse = ewsEntitlementResponse;
    }
}

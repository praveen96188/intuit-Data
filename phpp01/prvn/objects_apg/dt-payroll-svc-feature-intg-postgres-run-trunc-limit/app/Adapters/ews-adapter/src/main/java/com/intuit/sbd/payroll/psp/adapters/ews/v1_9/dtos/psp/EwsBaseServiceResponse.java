package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsServiceStatus;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlSeeAlso({
    EwsAssistedServiceResponse.class,
    EwsDirectDepositServiceResponse.class
})

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "status"
})
public class EwsBaseServiceResponse implements Cloneable {

    @XmlElement(name = "Status", required = false)
    protected EwsServiceStatus status;

    public EwsBaseServiceResponse clone() throws CloneNotSupportedException {
        return (EwsBaseServiceResponse) super.clone();
    }

    public EwsServiceStatus getStatus() {
        return status;
    }

    public void setStatus(EwsServiceStatus status) {
        this.status = status;
    }
}

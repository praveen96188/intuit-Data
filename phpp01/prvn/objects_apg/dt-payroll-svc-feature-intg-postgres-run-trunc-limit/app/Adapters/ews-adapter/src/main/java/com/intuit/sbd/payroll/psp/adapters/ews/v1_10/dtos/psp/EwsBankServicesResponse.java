package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ewsBankAssistedServiceResponse",
    "ewsBankDirectDepositServiceResponse"
})
public class EwsBankServicesResponse implements Cloneable {

    @XmlElement(name = "Assisted", required = false)
    protected EwsBankAssistedServiceResponse ewsBankAssistedServiceResponse;

    @XmlElement(name = "DirectDeposit", required = false)
    protected EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse;

    public EwsBankServicesResponse clone() throws CloneNotSupportedException {
        EwsBankServicesResponse clone = (EwsBankServicesResponse) super.clone();

        if (ewsBankAssistedServiceResponse != null) {
            clone.setEwsBankAssistedServiceResponse(ewsBankAssistedServiceResponse.clone());
        }

        if (ewsBankDirectDepositServiceResponse != null) {
            clone.setEwsBankDirectDepositServiceResponse(ewsBankDirectDepositServiceResponse.clone());
        }

        return clone;
    }

    public EwsBankAssistedServiceResponse getEwsBankAssistedServiceResponse() {
        return ewsBankAssistedServiceResponse;
    }

    public void setEwsBankAssistedServiceResponse(EwsBankAssistedServiceResponse ewsBankAssistedServiceResponse) {
        this.ewsBankAssistedServiceResponse = ewsBankAssistedServiceResponse;
    }

    public EwsBankDirectDepositServiceResponse getEwsBankDirectDepositServiceResponse() {
        return ewsBankDirectDepositServiceResponse;
    }

    public void setEwsBankDirectDepositServiceResponse(EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse) {
        this.ewsBankDirectDepositServiceResponse = ewsBankDirectDepositServiceResponse;
    }

    public void validate() throws Exception {
    }
}

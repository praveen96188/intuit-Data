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
        "ewsBankAccountResponse"
})
public class EwsDirectDepositServiceResponse extends EwsBaseServiceResponse implements Cloneable {

    @XmlElement(name = "BankAccountResponse", required = false)
    protected EwsBankAccountResponse ewsBankAccountResponse;

    public EwsDirectDepositServiceResponse() {
        super();
    }

    public EwsDirectDepositServiceResponse clone() throws CloneNotSupportedException {
        EwsDirectDepositServiceResponse clone = (EwsDirectDepositServiceResponse) super.clone();

        if (ewsBankAccountResponse != null) {
            clone.setEwsBankAccountResponse(ewsBankAccountResponse.clone());
        }

        return clone;
    }

    public EwsBankAccountResponse getEwsBankAccountResponse() {
        return ewsBankAccountResponse;
    }

    public void setEwsBankAccountResponse(EwsBankAccountResponse ewsBankAccountResponse) {
        this.ewsBankAccountResponse = ewsBankAccountResponse;
    }
}

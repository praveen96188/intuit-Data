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
    "ewsBaseBankAccountResponse"
})
public class EwsBankAssistedServiceResponse extends EwsBaseServiceResponse implements Cloneable {

    @XmlElement(name = "BankAccount", required = true)
    protected EwsBaseBankAccountResponse ewsBaseBankAccountResponse;

    public EwsBankAssistedServiceResponse clone() throws CloneNotSupportedException {
        EwsBankAssistedServiceResponse clone = (EwsBankAssistedServiceResponse) super.clone();

        if (ewsBaseBankAccountResponse != null) {
            clone.setEwsBaseBankAccountResponse(ewsBaseBankAccountResponse.clone());
        }

        return clone;
    }

    public EwsBaseBankAccountResponse getEwsBaseBankAccountResponse() {
        return ewsBaseBankAccountResponse;
    }

    public void setEwsBaseBankAccountResponse(EwsBaseBankAccountResponse ewsBaseBankAccountResponse) {
        this.ewsBaseBankAccountResponse = ewsBaseBankAccountResponse;
    }

    public void validate() throws Exception {
    }

}

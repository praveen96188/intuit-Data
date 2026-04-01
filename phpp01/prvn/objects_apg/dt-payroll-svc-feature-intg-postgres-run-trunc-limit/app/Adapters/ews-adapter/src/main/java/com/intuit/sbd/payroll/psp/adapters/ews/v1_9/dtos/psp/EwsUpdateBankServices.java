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
    "ewsUpdateBankAssistedService",
    "ewsUpdateBankDirectDepositService"
})
public class EwsUpdateBankServices implements Cloneable {

    @XmlElement(name = "AssistedService", required = false)
    protected EwsUpdateBankAssistedService ewsUpdateBankAssistedService;

    @XmlElement(name = "DirectDepositService", required = false)
    protected EwsUpdateBankDirectDepositService ewsUpdateBankDirectDepositService;

    public EwsUpdateBankServices clone() throws CloneNotSupportedException {
        EwsUpdateBankServices clone = (EwsUpdateBankServices) super.clone();

        if (ewsUpdateBankAssistedService != null) {
            clone.setEwsUpdateBankAssistedService(ewsUpdateBankAssistedService.clone());
        }

        if (ewsUpdateBankDirectDepositService != null) {
            clone.setEwsUpdateBankDirectDepositService(ewsUpdateBankDirectDepositService.clone());
        }

        return clone;
    }

    public EwsUpdateBankAssistedService getEwsUpdateBankAssistedService() {
        return ewsUpdateBankAssistedService;
    }

    public void setEwsUpdateBankAssistedService(EwsUpdateBankAssistedService ewsUpdateBankAssistedService) {
        this.ewsUpdateBankAssistedService = ewsUpdateBankAssistedService;
    }

    public EwsUpdateBankDirectDepositService getEwsUpdateBankDirectDepositService() {
        return ewsUpdateBankDirectDepositService;
    }

    public void setEwsUpdateBankDirectDepositService(EwsUpdateBankDirectDepositService ewsUpdateBankDirectDepositService) {
        this.ewsUpdateBankDirectDepositService = ewsUpdateBankDirectDepositService;
    }

    public void validate() throws Exception {
        if (ewsUpdateBankAssistedService != null) {
            ewsUpdateBankAssistedService.validate();
        }

        if (ewsUpdateBankDirectDepositService != null) {
            ewsUpdateBankDirectDepositService.validate();
        }
    }
}

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
    "ewsValidateBankAssistedService",
    "ewsValidateBankDirectDepositService"
})
public class EwsValidateBankServices implements Cloneable {

    @XmlElement(name = "Assisted", required = false)
    protected EwsValidateBankAssistedService ewsValidateBankAssistedService;

    @XmlElement(name = "DirectDeposit", required = false)
    protected EwsValidateBankDirectDepositService ewsValidateBankDirectDepositService;

    public EwsValidateBankServices clone() throws CloneNotSupportedException {
        EwsValidateBankServices clone = (EwsValidateBankServices) super.clone();

        if (ewsValidateBankAssistedService != null) {
            clone.setEwsValidateBankAssistedService(ewsValidateBankAssistedService.clone());
        }

        if (ewsValidateBankDirectDepositService != null) {
            clone.setEwsValidateBankDirectDepositService(ewsValidateBankDirectDepositService.clone());
        }

        return clone;
    }

    public EwsValidateBankAssistedService getEwsValidateBankAssistedService() {
        return ewsValidateBankAssistedService;
    }

    public void setEwsValidateBankAssistedService(EwsValidateBankAssistedService ewsValidateBankAssistedService) {
        this.ewsValidateBankAssistedService = ewsValidateBankAssistedService;
    }

    public EwsValidateBankDirectDepositService getEwsValidateBankDirectDepositService() {
        return ewsValidateBankDirectDepositService;
    }

    public void setEwsValidateBankDirectDepositService(EwsValidateBankDirectDepositService ewsValidateBankDirectDepositService) {
        this.ewsValidateBankDirectDepositService = ewsValidateBankDirectDepositService;
    }

    public void validate() throws Exception {
        if (ewsValidateBankAssistedService != null) {
            ewsValidateBankAssistedService.validate();
        }
        if (ewsValidateBankDirectDepositService != null) {
            ewsValidateBankDirectDepositService.validate();
        }
    }
}

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
    "assistedService",
    "directDepositService",
    "billPayment",
    "viewMyPaycheck",
    "cloudV2"
})
public class EwsBaseServices implements Cloneable {

    @XmlElement(name = "Assisted", required = false)
    protected EwsAssistedService assistedService;

    @XmlElement(name = "DirectDeposit", required = false)
    protected EwsDirectDepositService directDepositService;

    @XmlElement(name = "BillPayment", required = false)
    protected EwsBaseService billPayment;

    @XmlElement(name = "ViewMyPaycheck", required = false)
    protected EwsBaseService viewMyPaycheck;

    @XmlElement(name = "CloudV2", required = false)
    protected EwsBaseService cloudV2;

    public EwsBaseServices clone() throws CloneNotSupportedException {
        EwsBaseServices clone = (EwsBaseServices) super.clone();

        if (assistedService != null) {
            clone.setAssistedService(assistedService.clone());
        }

        if (directDepositService != null) {
            clone.setDirectDepositService(directDepositService.clone());
        }

        if (billPayment != null) {
            clone.setBillPayment(billPayment.clone());
        }

        if(viewMyPaycheck != null) {
            clone.setViewMyPaycheck(viewMyPaycheck.clone());
        }

        if(cloudV2 != null) {
            clone.setCloudV2(cloudV2.clone());
        }

        return clone;
    }

    public EwsAssistedService getAssistedService() {
        return assistedService;
    }

    public void setAssistedService(EwsAssistedService assistedService) {
        this.assistedService = assistedService;
    }

    public EwsDirectDepositService getDirectDepositService() {
        return directDepositService;
    }

    public void setDirectDepositService(EwsDirectDepositService directDepositService) {
        this.directDepositService = directDepositService;
    }

    public EwsBaseService getBillPayment() {
        return billPayment;
    }

    public void setBillPayment(EwsBaseService billPayment) {
        this.billPayment = billPayment;
    }

    public EwsBaseService getViewMyPaycheck() {
        return viewMyPaycheck;
    }

    public void setViewMyPaycheck(EwsBaseService pViewMyPaycheck) {
        viewMyPaycheck = pViewMyPaycheck;
    }

    public EwsBaseService getCloudV2() {
        return cloudV2;
    }

    public void setCloudV2(EwsBaseService pCloudV2) {
        cloudV2 = pCloudV2;
    }

    public void validate() throws Exception {
        if (assistedService != null) {
            assistedService.validate();
        }

        if (directDepositService != null) {
            directDepositService.validate();
        }

        if (billPayment != null) {
            billPayment.validate();
        }

        if(viewMyPaycheck != null) {
            viewMyPaycheck.validate();
        }

        if(cloudV2 != null) {
            cloudV2.validate();
        }
    }
}

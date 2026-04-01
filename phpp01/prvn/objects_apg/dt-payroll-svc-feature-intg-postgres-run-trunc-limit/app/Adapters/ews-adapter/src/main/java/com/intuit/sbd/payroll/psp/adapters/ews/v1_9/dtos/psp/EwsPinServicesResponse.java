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
    "cloudResponse",
    "assistedResponse",
    "directDepositResponse",
    "checkDistributionResponse",
    "billPaymentResponse",
    "thirdParty401kResponse"
})
public class EwsPinServicesResponse implements Cloneable {
    @XmlElement(name = "CloudResponse", required = false)
    protected EwsBaseServiceResponse cloudResponse;

    @XmlElement(name = "AssistedResponse", required = false)
    protected EwsBaseServiceResponse assistedResponse;

    @XmlElement(name = "DirectDepositResponse", required = false)
    protected EwsBaseServiceResponse directDepositResponse;

    @XmlElement(name = "CheckDistributionResponse", required = false)
    protected EwsBaseServiceResponse checkDistributionResponse;

    @XmlElement(name = "BillPaymentResponse", required = false)
    protected EwsBaseServiceResponse billPaymentResponse;

    @XmlElement(name = "ThirdParty401kResponse", required = false)
    protected EwsBaseServiceResponse thirdParty401kResponse;

    public EwsPinServicesResponse clone() throws CloneNotSupportedException {
        EwsPinServicesResponse clone = (EwsPinServicesResponse) super.clone();

        if (cloudResponse != null) {
            clone.setCloudResponse(cloudResponse.clone());
        }

        if (assistedResponse != null) {
            clone.setAssistedResponse(assistedResponse.clone());
        }

        if (directDepositResponse != null) {
            clone.setDirectDepositResponse(directDepositResponse.clone());
        }

        if (checkDistributionResponse != null) {
            clone.setCheckDistributionResponse(checkDistributionResponse.clone());
        }

        if (billPaymentResponse != null) {
            clone.setBillPaymentResponse(billPaymentResponse.clone());
        }

        if (thirdParty401kResponse != null) {
            clone.setThirdParty401kResponse(thirdParty401kResponse.clone());
        }

        return clone;
    }

    public EwsBaseServiceResponse getCloudResponse() {
        return cloudResponse;
    }

    public void setCloudResponse(EwsBaseServiceResponse cloudResponse) {
        this.cloudResponse = cloudResponse;
    }

    public EwsBaseServiceResponse getAssistedResponse() {
        return assistedResponse;
    }

    public void setAssistedResponse(EwsBaseServiceResponse assistedResponse) {
        this.assistedResponse = assistedResponse;
    }

    public EwsBaseServiceResponse getDirectDepositResponse() {
        return directDepositResponse;
    }

    public void setDirectDepositResponse(EwsBaseServiceResponse directDepositResponse) {
        this.directDepositResponse = directDepositResponse;
    }

    public EwsBaseServiceResponse getCheckDistributionResponse() {
        return checkDistributionResponse;
    }

    public void setCheckDistributionResponse(EwsBaseServiceResponse checkDistributionResponse) {
        this.checkDistributionResponse = checkDistributionResponse;
    }

    public EwsBaseServiceResponse getBillPaymentResponse() {
        return billPaymentResponse;
    }

    public void setBillPaymentResponse(EwsBaseServiceResponse billPaymentResponse) {
        this.billPaymentResponse = billPaymentResponse;
    }

    public EwsBaseServiceResponse getThirdParty401kResponse() {
        return thirdParty401kResponse;
    }

    public void setThirdParty401kResponse(EwsBaseServiceResponse thirdParty401kResponse) {
        this.thirdParty401kResponse = thirdParty401kResponse;
    }

    public void validate() throws Exception {
    }
}
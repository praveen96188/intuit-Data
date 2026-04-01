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
    "cloudResponse",
    "assistedResponse",
    "directDepositResponse",
    "checkDistributionResponse",
    "billPaymentResponse",
    "thirdParty401kResponse",
    "viewMyPaycheckResponse",
    "cloudV2Response",
    "workersCompResponse"
})
public class EwsServicesResponse implements Cloneable {

    @XmlElement(name = "CloudResponse", required = false)
    protected EwsBaseServiceResponse cloudResponse;

    @XmlElement(name = "AssistedResponse", required = false)
    protected EwsAssistedServiceResponse assistedResponse;

    @XmlElement(name = "DirectDepositResponse", required = false)
    protected EwsDirectDepositServiceResponse directDepositResponse;

    @XmlElement(name = "CheckDistributionResponse", required = false)
    protected EwsBaseServiceResponse checkDistributionResponse;

    @XmlElement(name = "BillPaymentResponse", required = false)
    protected EwsBaseServiceResponse billPaymentResponse;

    @XmlElement(name = "ThirdParty401kResponse", required = false)
    protected EwsBaseServiceResponse thirdParty401kResponse;

    @XmlElement(name = "ViewMyPaycheckResponse", required = false)
    protected EwsBaseServiceResponse viewMyPaycheckResponse;

    @XmlElement(name = "CloudV2Response", required = false)
    protected EwsBaseServiceResponse cloudV2Response;

    @XmlElement(name = "WorkersCompResponse", required = false)
    protected EwsBaseServiceResponse workersCompResponse;

    public EwsServicesResponse clone() throws CloneNotSupportedException {
        EwsServicesResponse clone = (EwsServicesResponse) super.clone();

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

        if (viewMyPaycheckResponse != null) {
            clone.setViewMyPaycheckResponse(viewMyPaycheckResponse.clone());
        }

        if(cloudV2Response != null) {
            clone.setCloudV2Response(cloudV2Response.clone());
        }

        if(workersCompResponse != null) {
            clone.setWorkersCompResponse(workersCompResponse);
        }

        return clone;
    }

    public EwsBaseServiceResponse getCloudResponse() {
        return cloudResponse;
    }

    public void setCloudResponse(EwsBaseServiceResponse cloudResponse) {
        this.cloudResponse = cloudResponse;
    }

    public EwsAssistedServiceResponse getAssistedResponse() {
        return assistedResponse;
    }

    public void setAssistedResponse(EwsAssistedServiceResponse assistedResponse) {
        this.assistedResponse = assistedResponse;
    }

    public EwsDirectDepositServiceResponse getDirectDepositResponse() {
        return directDepositResponse;
    }

    public void setDirectDepositResponse(EwsDirectDepositServiceResponse directDepositResponse) {
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

    public EwsBaseServiceResponse getViewMyPaycheckResponse() {
        return viewMyPaycheckResponse;
    }

    public void setViewMyPaycheckResponse(EwsBaseServiceResponse pViewMyPaycheckResponse) {
        viewMyPaycheckResponse = pViewMyPaycheckResponse;
    }

    public EwsBaseServiceResponse getCloudV2Response() {
        return cloudV2Response;
    }

    public void setCloudV2Response(EwsBaseServiceResponse pCloudV2Response) {
        cloudV2Response = pCloudV2Response;
    }

    public EwsBaseServiceResponse getWorkersCompResponse() {
        return workersCompResponse;
    }

    public void setWorkersCompResponse(EwsBaseServiceResponse pWorkersCompResponse) {
        workersCompResponse = pWorkersCompResponse;
    }
}

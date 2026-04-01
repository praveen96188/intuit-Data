package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "companyResponse",
        "ewsEntitlementUnitResponses",
        "ewsServicesResponse"
})
public class EwsCreateAccountResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "CompanyResponse", required = false)
    protected EwsCompanyResponse companyResponse;

    @XmlElement(name = "EntitlementUnitResponses", required = false)
    protected ArrayList<EwsEntitlementUnitResponse> ewsEntitlementUnitResponses;

    @XmlElement(name = "ServicesResponse", required = false)
    protected EwsServicesResponse ewsServicesResponse;

    public EwsCreateAccountResponse() {
        super();
    }

    public EwsCreateAccountResponse clone() throws CloneNotSupportedException {
        EwsCreateAccountResponse clone = (EwsCreateAccountResponse) super.clone();

        if (companyResponse != null) {
            clone.setCompanyResponse(companyResponse.clone());
        }

        if (ewsServicesResponse != null) {
            clone.setEwsServicesResponse(ewsServicesResponse.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsCompanyResponse getCompanyResponse() {
        return companyResponse;
    }

    public void setCompanyResponse(EwsCompanyResponse companyResponse) {
        this.companyResponse = companyResponse;
    }

    public ArrayList<EwsEntitlementUnitResponse> getEwsEntitlementUnitResponses() {
        if (ewsEntitlementUnitResponses == null)
            ewsEntitlementUnitResponses = new ArrayList<EwsEntitlementUnitResponse>();

        return ewsEntitlementUnitResponses;
    }

    public void setEwsEntitlementUnitResponses(ArrayList<EwsEntitlementUnitResponse> ewsEntitlementUnitResponses) {
        this.ewsEntitlementUnitResponses = ewsEntitlementUnitResponses;
    }

    public EwsServicesResponse getEwsServicesResponse() {
        return ewsServicesResponse;
    }

    public void setEwsServicesResponse(EwsServicesResponse ewsServicesResponse) {
        this.ewsServicesResponse = ewsServicesResponse;
    }

    public void validate() throws Exception {
        super.validate();
    }    

}

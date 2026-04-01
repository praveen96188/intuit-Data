package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "psid",
    "companyResponse"
})
public class EwsUpdateAccountResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "CompanyResponse", required = false)
    protected EwsCompanyResponse companyResponse;

    public EwsUpdateAccountResponse() {
        super();
    }

    public EwsUpdateAccountResponse clone() throws CloneNotSupportedException {
        EwsUpdateAccountResponse clone = (EwsUpdateAccountResponse) super.clone();

        if (companyResponse != null) {
            clone.setCompanyResponse(companyResponse.clone());
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

    public void validate() throws Exception {
        super.validate();
    }     
}

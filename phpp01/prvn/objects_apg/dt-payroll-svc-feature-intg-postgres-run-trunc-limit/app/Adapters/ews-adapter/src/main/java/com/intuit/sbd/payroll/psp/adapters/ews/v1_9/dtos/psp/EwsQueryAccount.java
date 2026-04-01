package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "psid",
    "ewsBaseCompany",
    "subscriptionNumber"
})
public class EwsQueryAccount extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "Company", required = true)
    protected EwsBaseCompany ewsBaseCompany;

    @XmlElement(name = "SubscriptionNumber", required = true)
    protected String subscriptionNumber;

    public EwsQueryAccount() {
        super();
    }

    public EwsQueryAccount clone() throws CloneNotSupportedException {
        EwsQueryAccount clone = (EwsQueryAccount) super.clone();
        if (ewsBaseCompany != null) {
            clone.setEwsBaseCompany(ewsBaseCompany.clone());
        }
        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsBaseCompany getEwsBaseCompany() {
        return ewsBaseCompany;
    }

    public void setEwsBaseCompany(EwsBaseCompany ewsBaseCompany) {
        this.ewsBaseCompany = ewsBaseCompany;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public void validate() throws Exception {
        super.validate();


        //Validate the one of the following fields are in the request.
        //PSID or
        //EIN and SubscriptionNumber or
        //RealmId
        if (psid == null || psid.isEmpty()) {
            if (ewsBaseCompany == null) {
                throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Request"));
            }
            if ((ewsBaseCompany.getEin() == null || ewsBaseCompany.getEin().isEmpty()) ||
                    (subscriptionNumber == null || subscriptionNumber.isEmpty())) {
                if ((ewsBaseCompany.getRealmId() == null || ewsBaseCompany.getRealmId().isEmpty())) {
                    throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Request"));
                }
            }
        }

        if (!Validation.validateValue(this.psid, true, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Request"));
        }

        if (!Validation.validateValue(this.getSubscriptionNumber(), true, "\\p{Digit}{0,18}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("SubscriptionNumber", "Request"));
        }

        ewsBaseCompany.validate();
    }
}

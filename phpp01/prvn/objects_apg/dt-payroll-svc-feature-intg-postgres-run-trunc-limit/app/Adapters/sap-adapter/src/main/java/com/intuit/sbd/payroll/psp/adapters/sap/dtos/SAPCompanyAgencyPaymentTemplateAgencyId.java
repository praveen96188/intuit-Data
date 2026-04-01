package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.PaymentMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: mwaqarbaig
 * Date: Jun 27, 2011
 * Time: 12:39:10 PM
 */
public class SAPCompanyAgencyPaymentTemplateAgencyId {
    private String mName;
    private String mId;
    private Date mModifiedDate;
    private String mModifiedBy;
    private List<SAPPaymentMethodAgencyIdRequirements> paymentMethodRequirements;

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    public Date getModifiedDate() {
        return mModifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        mModifiedDate = pModifiedDate;
    }

    public String getModifiedBy() {
        return mModifiedBy;
    }

    public void setModifiedBy(String pModifiedBy) {
        mModifiedBy = pModifiedBy;
    }

    public List<SAPPaymentMethodAgencyIdRequirements> getPaymentMethodRequirements() {
        return paymentMethodRequirements;
    }

    public void setPaymentMethodRequirements(List<SAPPaymentMethodAgencyIdRequirements> pPaymentMethodRequirements) {
        paymentMethodRequirements = pPaymentMethodRequirements;
    }

    public SAPPaymentMethodAgencyIdRequirements addOrGetRequirementsForPaymentMethod(PaymentMethod pPaymentMethod) {
        for (SAPPaymentMethodAgencyIdRequirements sapPaymentMethodAgencyIdRequirements : getPaymentMethodRequirements()) {
            if (sapPaymentMethodAgencyIdRequirements.getPaymentMethod().equals(pPaymentMethod.toString()))  {
                return sapPaymentMethodAgencyIdRequirements;
            }
        }

        SAPPaymentMethodAgencyIdRequirements newRequirements = new SAPPaymentMethodAgencyIdRequirements();
        newRequirements.setPaymentMethod(pPaymentMethod.toString());
        newRequirements.setRequirements(new ArrayList<SAPAgencyIdRequirement>());
        getPaymentMethodRequirements().add(newRequirements);
        return newRequirements;
    }
}

package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.List;

/**
 * User: dweinberg
 * Date: 3/4/13
 * Time: 10:31 AM
 */
public class SAPPaymentMethodAgencyIdRequirements {
    private String paymentMethod;
    private List<SAPAgencyIdRequirement> requirements;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String pPaymentMethod) {
        paymentMethod = pPaymentMethod;
    }

    public List<SAPAgencyIdRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<SAPAgencyIdRequirement> pRequirements) {
        requirements = pRequirements;
    }
}

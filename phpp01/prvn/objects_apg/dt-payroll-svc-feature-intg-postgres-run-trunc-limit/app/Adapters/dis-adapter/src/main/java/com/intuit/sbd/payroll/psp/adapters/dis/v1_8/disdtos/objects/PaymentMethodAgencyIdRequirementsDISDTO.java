package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyIdRequirement;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentMethodAgencyIdRequirements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class PaymentMethodAgencyIdRequirementsDISDTO {

    // From EditAgencyIdView.mxml
    // ... text="For {PaymentMethodAgencyIdRequirements(paymentMethodRepeater.currentItem).paymentMethod} to be enabled:" ...
    // Hard coded in to UI for now, so making copy here into this constant.
    public static final String REQUIREMENTS_DESCRIPTION_STRING = "For %1$s to be enabled:";

    public PaymentMethodAgencyIdRequirementsDISDTO() {
    }

    public PaymentMethodAgencyIdRequirementsDISDTO(SAPPaymentMethodAgencyIdRequirements pSAPPaymentMethodAgencyIdRequirements) {
        requirementsDescription = String.format(REQUIREMENTS_DESCRIPTION_STRING,pSAPPaymentMethodAgencyIdRequirements.getPaymentMethod());
        List<AgencyIdRequirementDISDTO> aidRequirementList = new ArrayList<AgencyIdRequirementDISDTO>();
        for (SAPAgencyIdRequirement sapAgencyIdRequirement : pSAPPaymentMethodAgencyIdRequirements.getRequirements()) {
            aidRequirementList.add(new AgencyIdRequirementDISDTO(sapAgencyIdRequirement));
        }
        setAgencyIdRequirements(aidRequirementList);
    }

    @XmlElement
    private String requirementsDescription;

    public String getRequirementsDescription() {
        return requirementsDescription;
    }

    public void setRequirementsDescription(String pRequirementsDescription) {
        requirementsDescription = pRequirementsDescription;
    }

    @XmlElement
    private List<AgencyIdRequirementDISDTO> agencyIdRequirements;

    public List<AgencyIdRequirementDISDTO> getAgencyIdRequirements() {
        return agencyIdRequirements;
    }

    public void setAgencyIdRequirements(List<AgencyIdRequirementDISDTO> pAgencyIdRequirements) {
        agencyIdRequirements = pAgencyIdRequirements;
    }


}

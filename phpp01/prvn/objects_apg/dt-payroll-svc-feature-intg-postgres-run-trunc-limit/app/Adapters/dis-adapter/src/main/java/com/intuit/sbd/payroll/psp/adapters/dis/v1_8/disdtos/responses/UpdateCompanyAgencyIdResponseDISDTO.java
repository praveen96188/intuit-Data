package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.PaymentMethodAgencyIdRequirementsDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class UpdateCompanyAgencyIdResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private List<PaymentMethodAgencyIdRequirementsDISDTO> paymentMethodAgencyIdRequirements;

    public List<PaymentMethodAgencyIdRequirementsDISDTO> getPaymentMethodAgencyIdRequirements() {
        return paymentMethodAgencyIdRequirements;
    }

    public void setPaymentMethodAgencyIdRequirements(List<PaymentMethodAgencyIdRequirementsDISDTO> pPaymentMethodAgencyIdRequirements) {
        paymentMethodAgencyIdRequirements = pPaymentMethodAgencyIdRequirements;
    }

    public void clearElements() {
    }

}

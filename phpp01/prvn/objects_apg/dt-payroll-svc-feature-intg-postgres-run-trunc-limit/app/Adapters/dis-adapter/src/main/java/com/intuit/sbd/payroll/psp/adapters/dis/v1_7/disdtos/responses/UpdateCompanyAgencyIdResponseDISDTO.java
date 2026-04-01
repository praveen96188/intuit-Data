package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.PaymentMethodAgencyIdRequirementsDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/RefundEmployerFinancialTransactionResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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

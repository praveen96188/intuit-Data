package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyPaymentMethodDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryPaymentMethodHistoryResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryPaymentMethodHistoryResponseDISDTO",propOrder = {"companyPaymentMethods"})
public class QueryPaymentMethodHistoryResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "CompanyPaymentMethods")
    private List<CompanyPaymentMethodDISDTO> companyPaymentMethods;

    public List<CompanyPaymentMethodDISDTO> getCompanyPaymentMethods() {
        return companyPaymentMethods;
    }

    public void setCompanyPaymentMethods(List<CompanyPaymentMethodDISDTO> companyPaymentMethods) {
        this.companyPaymentMethods = companyPaymentMethods;
    }

    @Override
    public void clearElements() {
        //@TODO Implement
    }

}
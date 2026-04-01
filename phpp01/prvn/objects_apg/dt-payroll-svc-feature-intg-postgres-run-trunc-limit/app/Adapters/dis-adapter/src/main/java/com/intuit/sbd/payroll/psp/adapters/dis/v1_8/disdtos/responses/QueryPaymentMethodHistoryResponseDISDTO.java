package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyPaymentMethodDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
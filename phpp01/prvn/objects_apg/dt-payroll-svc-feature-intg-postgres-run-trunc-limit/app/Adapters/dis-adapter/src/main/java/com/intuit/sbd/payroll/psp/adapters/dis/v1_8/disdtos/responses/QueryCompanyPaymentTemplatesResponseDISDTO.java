package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyPaymentTemplateDISDTO;

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
@XmlType(name = "QueryCompanyPaymentTemplatesResponseDISDTO",propOrder = {"companyPaymentTemplates"})
public class QueryCompanyPaymentTemplatesResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "CompanyPaymentTemplates")
    private List<CompanyPaymentTemplateDISDTO> companyPaymentTemplates;

    public List<CompanyPaymentTemplateDISDTO> getCompanyPaymentTemplates() {
        return companyPaymentTemplates;
    }

    public void setCompanyPaymentTemplates(List<CompanyPaymentTemplateDISDTO> companyPaymentTemplates) {
        this.companyPaymentTemplates = companyPaymentTemplates;
    }

    @Override
    public void clearElements() {
        companyPaymentTemplates = null;
    }

}

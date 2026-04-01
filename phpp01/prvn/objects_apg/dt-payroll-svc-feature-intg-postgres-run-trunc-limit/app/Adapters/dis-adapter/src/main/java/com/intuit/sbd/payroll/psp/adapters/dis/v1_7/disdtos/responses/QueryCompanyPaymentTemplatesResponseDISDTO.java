package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyPaymentTemplateDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyPaymentTemplatesResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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

package com.intuit.sbd.payroll.psp.api.dtos;

import java.util.List;

public class CompanyAgencyPaymentTemplateDTO {
    private String paymentTemplateCd;
    private String agencyTaxpayerId;
    private List<CompanyFilingAmountDTO> companyFilingAmountDTOs;

    public String getAgencyTaxpayerId() {
        return agencyTaxpayerId;
    }

    public void setAgencyTaxpayerId(String agencyTaxpayerId) {
        this.agencyTaxpayerId = agencyTaxpayerId;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    public List<CompanyFilingAmountDTO> getCompanyFilingAmountDTOs() {
        return companyFilingAmountDTOs;
    }

    public void setCompanyFilingAmountDTOs(List<CompanyFilingAmountDTO> pCompanyFilingAmountDTOs) {
        companyFilingAmountDTOs = pCompanyFilingAmountDTOs;
    }

    public CompanyFilingAmountDTO getCompanyFilingAmountDTO(String name, DateDTO effectiveDate) {
        for (CompanyFilingAmountDTO companyFilingAmountDTO : companyFilingAmountDTOs) {
            if (companyFilingAmountDTO.getName().equals(name) && companyFilingAmountDTO.getEffectiveDate().equals(effectiveDate)) {
                return companyFilingAmountDTO;
            }
        }
        return null;
    }
}

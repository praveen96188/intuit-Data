package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.util.List;

public class CompanyAgencyDTO {


    private SpcfCalendar intuitResponsibilityStartDate;
    private SpcfCalendar intuitResponsibilityEndDate;
    private List<FormTemplateDTO> formTemplateDtoList;
    private List<CompanyAgencyPaymentTemplateDTO> companyAgencyPaymentTemplateDTOList;
    private String agencyTaxpayerId;
    private Boolean erFicaDeferralEnabled;

    public SpcfCalendar getIntuitResponsibilityStartDate() {
        return intuitResponsibilityStartDate;
    }

    public void setIntuitResponsibilityStartDate(SpcfCalendar intuitResponsibilityStartDate) {
        this.intuitResponsibilityStartDate = intuitResponsibilityStartDate;
    }

    public SpcfCalendar getIntuitResponsibilityEndDate() {
        return intuitResponsibilityEndDate;
    }

    public void setIntuitResponsibilityEndDate(SpcfCalendar intuitResponsibilityEndDate) {
        this.intuitResponsibilityEndDate = intuitResponsibilityEndDate;
    }

    //
    public ProcessResult validateCompanyAgencyDTO() {
        ProcessResult result = new ProcessResult();
        // any semantic validations go here
        // not sure if I can think of any right off the bat
        return result;
    }

    public List<FormTemplateDTO> getFormTemplateDtoList() {
        return formTemplateDtoList;
    }

    public void setFormTemplateDtoList(List<FormTemplateDTO> formTemplateDtoList) {
        this.formTemplateDtoList = formTemplateDtoList;
    }

    public List<CompanyAgencyPaymentTemplateDTO> getCompanyAgencyPaymentTemplateDTOList() {
        return companyAgencyPaymentTemplateDTOList;
    }

    public CompanyAgencyPaymentTemplateDTO getCompanyAgencyPaymentTemplate(String paymentTemplateCd) {
        for (CompanyAgencyPaymentTemplateDTO companyAgencyPaymentTemplateDTO : getCompanyAgencyPaymentTemplateDTOList()) {
            if (companyAgencyPaymentTemplateDTO.getPaymentTemplateCd().equals(paymentTemplateCd))  {
                return companyAgencyPaymentTemplateDTO;
            }
        }
        return null;
    }

    public void setCompanyAgencyPaymentTemplateDTOList(List<CompanyAgencyPaymentTemplateDTO> companyAgencyPaymentTemplateDTOList) {
        this.companyAgencyPaymentTemplateDTOList = companyAgencyPaymentTemplateDTOList;
    }

    public String getAgencyTaxpayerId() {
        return agencyTaxpayerId;
    }

    // If this method is called we'll set all the capt tax payer ids to the same value
    public void setAgencyTaxpayerId(String agencyTaxpayerId) {
        this.agencyTaxpayerId = agencyTaxpayerId;
        for (CompanyAgencyPaymentTemplateDTO captDTO : getCompanyAgencyPaymentTemplateDTOList()) {
            captDTO.setAgencyTaxpayerId(agencyTaxpayerId);
        }
    }

    public Boolean getErFicaDeferralEnabled() {
        return erFicaDeferralEnabled;
    }

    public void setErFicaDeferralEnabled(Boolean erFicaDeferralEnabled) {
        this.erFicaDeferralEnabled = erFicaDeferralEnabled;
    }
}

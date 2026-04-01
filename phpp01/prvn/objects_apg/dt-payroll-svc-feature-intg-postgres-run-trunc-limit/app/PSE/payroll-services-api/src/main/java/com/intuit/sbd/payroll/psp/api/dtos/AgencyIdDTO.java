package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * User: dweinberg
 * Date: 7/14/11
 * Time: 1:20 PM
 */
public class AgencyIdDTO {
    private String paymentTemplateCd;
    private String idName;
    private String agencyTaxpayerId;

    public AgencyIdDTO(String paymentTemplateCd, String idName, String agencyTaxpayerId) {
        this.paymentTemplateCd = paymentTemplateCd;
        this.idName = idName;
        this.agencyTaxpayerId = agencyTaxpayerId;
    }

    public AgencyIdDTO() {
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public String getAgencyTaxpayerId() {
        return agencyTaxpayerId;
    }

    public void setAgencyTaxpayerId(String agencyTaxpayerId) {
        this.agencyTaxpayerId = agencyTaxpayerId;
    }
}

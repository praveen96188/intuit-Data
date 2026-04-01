package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 1:33:24 PM
 */
public class CompanyAgencyPaymentTemplateWSDTO {
    public String paymentTemplateCd;
    public String agencyTaxpayerId;
    public Collection<EffectiveDepositFrequencyWSDTO> effectiveDepositFrequencyWSDTOs;
}

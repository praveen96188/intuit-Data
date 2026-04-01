package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: May 20, 2009
 * Time: 11:15:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class EffectiveDepositFrequencyDTO {
    private String paymentTemplateCd;
    private String agencyId;
    private SpcfCalendar effectiveDate;
    private DepositFrequencyCode paymentFrequencyId;

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public SpcfCalendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(SpcfCalendar effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public DepositFrequencyCode getPaymentFrequencyId() {
        return paymentFrequencyId;
    }

    public void setPaymentFrequencyId(DepositFrequencyCode paymentFrequencyId) {
        this.paymentFrequencyId = paymentFrequencyId;
    }

    public ProcessResult validate(){
        ProcessResult validationResult = new ProcessResult();

        if (agencyId == null) {
            validationResult.getMessages().InvalidArgument(EntityName.Agency, null, "Agency Id");
        }

        if (paymentTemplateCd == null) {
            validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, null, "Payment Template Code");
        }

        if (effectiveDate == null) {
            validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, null, "New Effective Date");
        }

        if (paymentFrequencyId == null) {
            validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, null, "Payment Frequency Id");
        }
        
         return validationResult;
    }
}

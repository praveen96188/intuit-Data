package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.TaxRateUpdateDISDTO;

import javax.xml.bind.annotation.*;
import java.util.Calendar;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/requests/RefundEmployerFinancialTransactionRequestDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
@XmlType()
public class UpdateCompanyTaxRateRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false, required = true)
    private String token;

    @XmlElement(nillable = false, required = true)
    private String corpId;

    @XmlElement(nillable = false, required = true)
    private Calendar effectiveDate;

    @XmlElement(nillable = false, required = true)
    List<TaxRateUpdateDISDTO> taxRates;

    @XmlElement(nillable = false, required = false)
    private String paymentTemplateCd;

    @XmlElement(nillable = false, required = false)
    private Boolean pushToQuickbooks;

    @XmlElement(nillable = false, required = false)
    private Boolean overrideBlackout;

    @XmlElement(nillable = false, required = false)
    private Boolean supportRatesOutsideBoundaries;

    @XmlElement(nillable = false, required = false)
    private String noteToAttachToEvent;

    public SourceSystemEnum getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystemEnum pSourceSystem) {
        sourceSystem = pSourceSystem;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String pSourceCompanyId) {
        sourceCompanyId = pSourceCompanyId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String pToken) {
        token = pToken;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String pCorpId) {
        corpId = pCorpId;
    }

    public Calendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Calendar pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

    public List<TaxRateUpdateDISDTO> getTaxRates() {
        return taxRates;
    }

    public void setTaxRates(List<TaxRateUpdateDISDTO> pTaxRates) {
        taxRates = pTaxRates;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String pPaymentTemplateCd) {
        paymentTemplateCd = pPaymentTemplateCd;
    }

    public Boolean getPushToQuickbooks() {
        return pushToQuickbooks;
    }

    public void setPushToQuickbooks(Boolean pPushToQuickbooks) {
        pushToQuickbooks = pPushToQuickbooks;
    }

    public Boolean getOverrideBlackout() {
        return overrideBlackout;
    }

    public void setOverrideBlackout(Boolean pOverrideBlackout) {
        overrideBlackout = pOverrideBlackout;
    }

    public Boolean getSupportRatesOutsideBoundaries() {
        return supportRatesOutsideBoundaries;
    }

    public void setSupportRatesOutsideBoundaries(Boolean pSupportRatesOutsideBoundaries) {
        supportRatesOutsideBoundaries = pSupportRatesOutsideBoundaries;
    }

    public String getNoteToAttachToEvent() {
        return noteToAttachToEvent;
    }

    public void setNoteToAttachToEvent(String pNoteToAttachToEvent) {
        noteToAttachToEvent = pNoteToAttachToEvent;
    }
}

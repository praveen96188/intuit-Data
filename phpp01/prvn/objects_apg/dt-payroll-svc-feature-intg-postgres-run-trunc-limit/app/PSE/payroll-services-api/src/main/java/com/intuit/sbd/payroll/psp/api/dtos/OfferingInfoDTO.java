package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.EntitlementCode;
import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.domain.PayrollSubtypeCode;

/**
 * This DTO represents how a SPS identifies the Offering a Company owns.  In general,
 * the SPS won't have the SKU or the ID of the Offering entity, so this DTO provides
 * an alternate means of specifying the offering
 * 
 * Date: Mar 3, 2008
 *
 * @author Nick Nichols
 */
public class OfferingInfoDTO {

    public static final OfferingInfoDTO DIY_WITH_DD = new OfferingInfoDTO("QBOE DD"); 
    public static final OfferingInfoDTO CHECK_DISTRIBUTION = new OfferingInfoDTO("CheckDistribution");
    public static final OfferingInfoDTO THIRD_PARTY_401K = new OfferingInfoDTO("ThirdParty401k");
    public static final OfferingInfoDTO CLOUD = new OfferingInfoDTO("Cloud");
    public static final OfferingInfoDTO BILL_PAYMENT_STD3 = new OfferingInfoDTO("BILLPAYMENTSTD-3");
    public static final OfferingInfoDTO TAX = new OfferingInfoDTO("TAX");
    public static final OfferingInfoDTO RISK_ASSESSMENT = new OfferingInfoDTO("RiskAssessment");
    public static final OfferingInfoDTO VIEW_MY_PAYCHECK = new OfferingInfoDTO("ViewMyPaycheck");
    public static final OfferingInfoDTO CLOUD_V2 = new OfferingInfoDTO("CloudV2");

    private String SKU;
    private PayrollSubtypeCode payrollSubTypeCd;
    private OfferingCode offeringCode;
    private long quickBooksSubType;

    public OfferingInfoDTO() {
    }

    public OfferingInfoDTO(OfferingCode pOfferingCode) {
        offeringCode = pOfferingCode;
    }

    public OfferingInfoDTO(long pQuickBooksSubType) {
        quickBooksSubType = pQuickBooksSubType;
    }

    public String toString() {
        return "OfferingInfo.SKU='"+SKU+"'";
    }

    private OfferingInfoDTO(String pSKU) {
        SKU = pSKU;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String pSKU) {
        SKU = pSKU;
    }

    public PayrollSubtypeCode getPayrollSubTypeCd() {
        return payrollSubTypeCd;
    }

    public void setPayrollSubTypeCd(PayrollSubtypeCode pPayrollSubType) {
        payrollSubTypeCd = pPayrollSubType;
    }

    public long getQuickBooksSubtype() {
        if (getPayrollSubTypeCd()!=null) {
           return EntitlementCode.getQuickBooksSubtypeFromPayrollSubtype(payrollSubTypeCd);
        } else {
            return quickBooksSubType;
        }
    }

    public OfferingCode getOfferingCode() {
        return offeringCode;
    }

    public void setOfferingCode(OfferingCode offeringCode) {
        this.offeringCode = offeringCode;
    }
}

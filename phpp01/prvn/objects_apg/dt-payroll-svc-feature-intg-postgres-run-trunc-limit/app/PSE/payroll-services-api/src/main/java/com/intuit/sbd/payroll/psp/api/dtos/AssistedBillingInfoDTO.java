package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.DeliveryPreferenceCode;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: ihannur
 * Date: 8/10/12
 * Time: 5:38 PM
 */
public class AssistedBillingInfoDTO {

    private String offerCd;
    private DateDTO offerExpirationDate;
    protected SpcfMoney employeeDDLimit;
    protected SpcfMoney companyDDLimit;
    private OfferingInfoDTO offeringInfoDTO;
    private DeliveryPreferenceCode w2DeliveryPref;
    private DeliveryPreferenceCode clientPacketDeliveryPref;
    private int strikeCount = 0;


    public String getOfferCd() {
        return offerCd;
    }

    public void setOfferCd(String pOfferCd) {
        offerCd = pOfferCd;
    }

    public DateDTO getOfferExpirationDate() {
        return offerExpirationDate;
    }

    public void setOfferExpirationDate(DateDTO pOfferExpirationDate) {
        offerExpirationDate = pOfferExpirationDate;
    }

    public SpcfMoney getEmployeeDDLimit() {
        return employeeDDLimit;
    }

    public void setEmployeeDDLimit(SpcfMoney pEmployeeDDLimit) {
        employeeDDLimit = pEmployeeDDLimit;
    }

    public SpcfMoney getCompanyDDLimit() {
        return companyDDLimit;
    }

    public void setCompanyDDLimit(SpcfMoney pCompanyDDLimit) {
        companyDDLimit = pCompanyDDLimit;
    }

    public OfferingInfoDTO getOfferingInfoDTO() {
        return offeringInfoDTO;
    }

    public void setOfferingInfoDTO(OfferingInfoDTO pOfferingInfoDTO) {
        offeringInfoDTO = pOfferingInfoDTO;
    }

    public DeliveryPreferenceCode getW2DeliveryPref() {
        return w2DeliveryPref;
    }

    public void setW2DeliveryPref(DeliveryPreferenceCode pW2DeliveryPref) {
        w2DeliveryPref = pW2DeliveryPref;
    }

    public DeliveryPreferenceCode getClientPacketDeliveryPref() {
        return clientPacketDeliveryPref;
    }

    public void setClientPacketDeliveryPref(DeliveryPreferenceCode pClientPacketDeliveryPref) {
        clientPacketDeliveryPref = pClientPacketDeliveryPref;
    }

    public int getStrikeCount() {
        return strikeCount;
    }

    public void setStrikeCount(int pStrikeCount) {
        strikeCount = pStrikeCount;
    }
}

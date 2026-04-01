package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Feb 18, 2008
 * Time: 10:55:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class OfferingServiceChargeDTO {
    private String id;             // SPCF unique ID of the OfferingServiceCharge entity
    private String groupId;        // SPCF unique ID of the parent OfferingServiceChargeGroup entity
    private String sku;            // the service charge SKU ("child-level")
    private boolean isPriceTier;   // whether this charge is one tier of a tiered pricing structure
    private int tierNumber;        // when isPriceTier==true, this is the tier number (1-based)
    private int tierUnits;         // when isPriceTier==true, this is the number of units subject to this tier's price

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String pGroupId) {
        groupId = pGroupId;
    }

    public String getSKU() {
        return sku;
    }

    public void setSKU(String pSKU) {
        sku = pSKU;
    }

    public boolean getIsPriceTier() {
        return isPriceTier;
    }

    public void setIsPriceTier(boolean pIsPriceTier) {
        isPriceTier = pIsPriceTier;
    }

    public int getTierNumber() {
        return tierNumber;
    }
    
    public void setTierNumber(int pTierNumber) {
        tierNumber = pTierNumber;
    }

    public int getTierUnits() {
        return tierUnits;
    }

    public void setTierUnits(int pTierUnits) {
        tierUnits = pTierUnits;
    }
}

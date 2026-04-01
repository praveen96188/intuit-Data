/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.domain.SkuType;

/**
 * SAPOfferingServiceCharge - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class SAPOfferingServiceCharge {
    private String SKU;
    private boolean isTier;
    private int tierNumber;
    private int tierUnits;
    private SkuType SKUType;
    private String groupDescription;
    private OfferingServiceChargeType groupAppliesTo;
    private double price;
    private double unitPrice;

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public boolean isTier() {
        return isTier;
    }

    public void setTier(boolean tier) {
        isTier = tier;
    }

    public int getTierNumber() {
        return tierNumber;
    }

    public void setTierNumber(int tierNumber) {
        this.tierNumber = tierNumber;
    }

    public int getTierUnits() {
        return tierUnits;
    }

    public void setTierUnits(int tierUnits) {
        this.tierUnits = tierUnits;
    }

    public SkuType getSKUType() {
        return SKUType;
    }

    public void setSKUType(SkuType SKUType) {
        this.SKUType = SKUType;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public OfferingServiceChargeType getGroupAppliesTo() {
        return groupAppliesTo;
    }

    public void setGroupAppliesTo(OfferingServiceChargeType groupAppliesTo) {
        this.groupAppliesTo = groupAppliesTo;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double pUnitPrice) {
        unitPrice = pUnitPrice;
    }
}

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

import java.util.ArrayList;

/**
 * SAPOffering - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class SAPOffering {
    private String description;
    private String SKU;
    private String name;
    private ArrayList<SAPOfferingServiceCharge> serviceCharges = new ArrayList<SAPOfferingServiceCharge>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SAPOfferingServiceCharge> getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(ArrayList<SAPOfferingServiceCharge> serviceCharges) {
        this.serviceCharges = serviceCharges;
    }
}

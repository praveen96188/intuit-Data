package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/14/12
 * Time: 10:38 AM
 */
@XmlRootElement()
@XmlType(name = "ServiceChargePrice")
public class ServiceChargePrice {
    private String basePrice;
   	private String unitPrice;

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String pBasePrice) {
        basePrice = pBasePrice;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String pUnitPrice) {
        unitPrice = pUnitPrice;
    }
}

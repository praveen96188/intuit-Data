package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jan 28, 2009
 * Time: 9:33:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class BillingDetailWSDTO {
    public String basePrice;
    public Date billingPeriod;
    public String discountAmount;
    public String itemName;
    public String itemSKU;
    public String itemTotal;
    public String memo;
    public String offerCode;
    public String offeringServiceChargeType;
    public String offerName;
    public Date offloadDate;
    public int quantity;
    public String serviceCode;
    public Date serviceDate;
    public String taxAmount;
    public String taxWhenOffloaded;
    public Date taxComputedDate;
    public Boolean taxExceptionInd;
    public String taxJurisdiction;
    public String unitPrice;
}

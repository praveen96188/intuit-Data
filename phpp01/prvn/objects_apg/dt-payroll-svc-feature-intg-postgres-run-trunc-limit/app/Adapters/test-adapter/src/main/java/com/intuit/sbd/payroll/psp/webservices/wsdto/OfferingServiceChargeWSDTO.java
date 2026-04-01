package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/17/12
 * Time: 3:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class OfferingServiceChargeWSDTO {
    public String OfferingCode;
    public String AppliesTo;
    public Boolean IsTier;
    public Integer TierNumber;
    public Integer TierUnits;
    public String Sku;
    public String SkuType;
    public String BasePrice;
    public String UnitPrice;
    public Date EffectiveDate;
}

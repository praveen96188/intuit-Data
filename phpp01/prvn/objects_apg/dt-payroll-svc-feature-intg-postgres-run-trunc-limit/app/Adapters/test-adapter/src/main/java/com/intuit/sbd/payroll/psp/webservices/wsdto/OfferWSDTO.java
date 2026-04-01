package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/17/12
 * Time: 6:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class OfferWSDTO {
    public String BeginEvent;
    public String Description;
    public String DiscountAmount;
    public String DiscountPercent;
    public String DiscountType;
    public Integer DurationDays;
    public Date EffectiveDate;
    public Date EndDate;
    public String EndEvent;
    public Boolean IsApproved;
    public String Name;
    public String OfferCd;
    public String PromotionId;
    public Integer UsagesAllowed;
    public List<OfferPriceWSDTO> OfferPriceList = new ArrayList<OfferPriceWSDTO>();
}

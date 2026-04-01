package com.intuit.sbd.payroll.psp.gateways.aia;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 10/22/12
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class ItemCharge {
    private String mItemChargeId = null;
    private String mItemChargeAmount = null;
    private String mItemName = null;
    private int mUsageCount = -1;
    private SpcfCalendar mBillDate = null;


    public int getUsageCount() {
        return mUsageCount;
    }

    public void setUsageCount(int pUsageCount) {
        mUsageCount = pUsageCount;
    }

    public String getItemChargeId() {
        return mItemChargeId;
    }

    public void setItemChargeId(String pItemChargeId) {
        mItemChargeId = pItemChargeId;
    }

    public String getItemChargeAmount() {
        return mItemChargeAmount;
    }

    public void setItemChargeAmount(String pItemChargeAmount) {
        mItemChargeAmount = pItemChargeAmount;
    }

    public String getItemName() {
        return mItemName;
    }

    public void setItemName(String pItemName) {
        mItemName = pItemName;
    }

    public SpcfCalendar getBillDate() {
        return mBillDate;
    }

    public void setBillDate(SpcfCalendar pBillDate) {
        mBillDate = pBillDate;
    }

    public boolean isAnnualSubscriptionItem(){
        return mItemName!=null && mItemName.contains(AIAGateway.ANNUAL_SUBSCRIPTION_ITEM_NAME);
    }

    public boolean isMonthlySubscriptionItem(){
        return mItemName!=null && mItemName.contains(AIAGateway.MONTHLY_SUBSCRIPTION_ITEM_NAME);
    }

    public boolean isPayrollEEItem(){
        return mItemName!=null && mItemName.contains(AIAGateway.PAYROLL_EE_ITEM_NAME);
    }
}

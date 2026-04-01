package com.intuit.sbd.payroll.psp.gateways.aia;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 10/22/12
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class BillInfo {
    private String mBillPOID = null;

    public String getBillPOID() {
        return mBillPOID;
    }

    public void setBillPOID(String pBillPOID) {
        mBillPOID = pBillPOID;
    }

    public SpcfCalendar getBillDate() {
        return mBillDate;
    }

    public void setBillDate(SpcfCalendar pBillDate) {
        mBillDate = pBillDate;
    }

    private SpcfCalendar mBillDate = null;

}

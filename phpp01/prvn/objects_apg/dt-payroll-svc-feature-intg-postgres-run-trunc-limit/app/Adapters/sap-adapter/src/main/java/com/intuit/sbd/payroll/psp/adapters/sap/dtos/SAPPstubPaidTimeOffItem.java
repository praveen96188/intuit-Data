package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: ihannur
 * Date: 6/26/13
 * Time: 2:12 PM
 */
public class SAPPstubPaidTimeOffItem {
    private String name;
    private String ytdUsed;
    private String available;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getYtdUsed() {
        return ytdUsed;
    }

    public void setYtdUsed(String pYtdUsed) {
        ytdUsed = pYtdUsed;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String pAvailable) {
        available = pAvailable;
    }
}

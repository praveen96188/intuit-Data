package com.intuit.sbd.payroll.psp.adapters.lt;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: May 3, 2011
 * Time: 11:59:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class LtPayrollItemDTO {

    private String payItemId;
    private String payItemName;
    private String payItemType;
    private String payItemTaxType;
    private String payItemTaxDesc;
    private String payItemState;
    private String piSpecialType;

    public String getPayItemId() {
        return payItemId;
    }

    public void setPayItemId(String payItemId) {
        this.payItemId = payItemId;
    }

    public String getPayItemName() {
        return payItemName;
    }

    public void setPayItemName(String payItemName) {
        this.payItemName = payItemName;
    }

    public String getPayItemType() {
        return payItemType;
    }

    public void setPayItemType(String payItemType) {
        this.payItemType = payItemType;
    }

    public String getPayItemTaxType() {
        return payItemTaxType;
    }

    public void setPayItemTaxType(String payItemTaxType) {
        this.payItemTaxType = payItemTaxType;
    }

    public String getPayItemTaxDesc() {
        return payItemTaxDesc;
    }

    public void setPayItemTaxDesc(String payItemTaxDesc) {
        this.payItemTaxDesc = payItemTaxDesc;
    }

    public String getPayItemState() {
        return payItemState;
    }

    public void setPayItemState(String payItemState) {
        this.payItemState = payItemState;
    }

    public String getPiSpecialType() {
        return piSpecialType;
    }

    public void setPiSpecialType(String piSpecialType) {
        this.piSpecialType = piSpecialType;
    }
}

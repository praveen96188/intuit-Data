package com.intuit.sbd.payroll.psp.api.dtos;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jan 18, 2010
 * Time: 11:58:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class CheckPrintPaycheckLineDTO implements Comparable<CheckPrintPaycheckLineDTO> {
    protected static final BigDecimal ZERO_BIGDECIMAL = new BigDecimal(0.0);
    private long payItemId;
    private String paylineDescription;
    private BigDecimal paylineAmount = ZERO_BIGDECIMAL;
    private BigDecimal ytdAmount = ZERO_BIGDECIMAL;
    private PayLineItemType paylineItemType = PayLineItemType.C;
    private boolean isInCurrentCheck = false;
    private BigDecimal totalWages = ZERO_BIGDECIMAL;
    private BigDecimal taxableWages = ZERO_BIGDECIMAL;
    private BigDecimal tipTaxableWages = ZERO_BIGDECIMAL;

    public String getPaylineDescription() {
        return paylineDescription;
    }

    public void setPaylineDescription(String paylineDescription) {
        this.paylineDescription = paylineDescription;
    }

    public BigDecimal getPaylineAmount() {
        return paylineAmount;
    }

    public void setPaylineAmount(BigDecimal paylineAmount) {
        this.paylineAmount = paylineAmount;
    }

    public BigDecimal getYtdAmount() {
        return ytdAmount;
    }

    public void setYtdAmount(BigDecimal ytdAmount) {
        this.ytdAmount = ytdAmount;
    }

    public BigDecimal getTotalWages() {
        return totalWages;
    }

    public void setTotalWages(BigDecimal totalWages) {
        this.totalWages = totalWages;
    }

    public BigDecimal getTaxableWages() {
        return taxableWages;
    }

    public void setTaxableWages(BigDecimal taxableWages) {
        this.taxableWages = taxableWages;
    }

    public BigDecimal getTipTaxableWages() {
        return tipTaxableWages;
    }

    public void setTipTaxableWages(BigDecimal tipTaxableWages) {
        this.tipTaxableWages = tipTaxableWages;
    }

    public void setPayItemId(long payItemId) {
        this.payItemId = payItemId;
    }

    public long getPayItemId() {
        return payItemId;
    }

    public PayLineItemType getPaylineItemType() {
        return paylineItemType;
    }

    public void setPaylineItemType(PayLineItemType paylineItemType) {
        this.paylineItemType = paylineItemType;
    }

    public boolean isInCurrentCheck() {
        return isInCurrentCheck;
    }

    public void setIsInCurrentCheck(boolean inCurrentCheck) {
        isInCurrentCheck = inCurrentCheck;
    }

    public enum PayLineItemType {
        CH,
        CS,
        CB,
        CM,
        CN,
        CA,
        D,
        C,
        TF,
        TS,
        TO
    }

    public int compareTo(CheckPrintPaycheckLineDTO o) {
        if (this.isInCurrentCheck != o.isInCurrentCheck) {
            if (this.isInCurrentCheck) {
                return 1;
            }
            else {
                return -1;
            }
        }

        int compareResult = paylineItemType.compareTo(o.paylineItemType);
        if (compareResult != 0) return compareResult;

        compareResult = paylineDescription.compareTo(o.paylineDescription);
        if (compareResult != 0) return compareResult;

        return 1; // we never want to indicate the items are the same
    }
}

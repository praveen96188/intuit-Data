package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: nchandrasekaran
 * Date: Jul 7, 2008
 * Time: 2:28:59 PM
 */
public class SAPRandomDebit {
    private Date offloadedDate;
    private Date settlementDate;
    private String amount1;
    private String amount2;

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getAmount1() {
        return amount1;
    }

    public void setAmount1(String amount1) {
        this.amount1 = amount1;
    }

    public String getAmount2() {
        return amount2;
    }

    public void setAmount2(String amount2) {
        this.amount2 = amount2;
    }


    public Date getOffloadedDate() {
        return offloadedDate;
    }

    public void setOffloadedDate(Date offloadedDate) {
        this.offloadedDate = offloadedDate;
    }


}

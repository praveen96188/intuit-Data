package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:09 PM
 */
public class SAPEmployeeLineItemQuarter extends SAPEmployeeLineItemGroup {

    private int quarter; //1..4

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int pQuarter) {
        quarter = pQuarter;
    }
}

package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:09 PM
 */
public class SAPEmployeeLineItemYear extends SAPEmployeeLineItemCollection {
    private int year;
    private ArrayList<SAPEmployeeLineItemQuarter> quarters;

    public int getYear() {
        return year;
    }

    public void setYear(int pYear) {
        year = pYear;
    }

    public ArrayList<SAPEmployeeLineItemQuarter> getQuarters() {
        return quarters;
    }

    public void setQuarters(ArrayList<SAPEmployeeLineItemQuarter> pQuarters) {
        quarters = pQuarters;
    }
}

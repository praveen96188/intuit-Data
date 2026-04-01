package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 10, 2008
 * Time: 2:00:06 PM
 */
public class SAPSearchResults<T> {
    private long totalRecords;
    private Number totalAmount;
    private ArrayList<T> returnsList;

    public SAPSearchResults() {
        totalRecords = 0;
        totalAmount = 0;
        returnsList = new ArrayList<T>();
    }

    public SAPSearchResults(long totalRecords, ArrayList<T> returnsList) {
        this.totalRecords = totalRecords;
        this.returnsList = returnsList;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Number getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Number totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ArrayList<T> getReturnsList() {
        return returnsList;
    }

    public void setReturnsList(ArrayList<T> returnsList) {
        this.returnsList = returnsList;
    }
}

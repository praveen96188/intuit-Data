package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: cyoder
 * Date: Jun 23, 2009
 * Time: 9:33:40 AM
 */
public class SAPEmployeePaycheckCollection extends SAPEmployeeLineItemCollection {

    private ArrayList<SAPEmployeeLineItemPaycheck> paychecks;

    public ArrayList<SAPEmployeeLineItemPaycheck> getPaychecks() {
        return paychecks;
    }

    public void setPaychecks(ArrayList<SAPEmployeeLineItemPaycheck> pPaychecks) {
        paychecks = pPaychecks;
    }
}

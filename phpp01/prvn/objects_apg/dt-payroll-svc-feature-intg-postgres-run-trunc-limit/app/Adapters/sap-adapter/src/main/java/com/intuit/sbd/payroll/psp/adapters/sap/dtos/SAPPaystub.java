package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: ihannur
 * Date: 6/26/13
 * Time: 1:54 PM
 */
public class SAPPaystub {
    private String paystubSeq;
    private String employeeSeq;
    private Date paycheckDate;


    public String getPaystubSeq() {
        return paystubSeq;
    }

    public void setPaystubSeq(String pPaystubSeq) {
        paystubSeq = pPaystubSeq;
    }

    public String getEmployeeSeq() {
        return employeeSeq;
    }

    public void setEmployeeSeq(String pEmployeeSeq) {
        employeeSeq = pEmployeeSeq;
    }

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date pPaycheckDate) {
        paycheckDate = pPaycheckDate;
    }
}

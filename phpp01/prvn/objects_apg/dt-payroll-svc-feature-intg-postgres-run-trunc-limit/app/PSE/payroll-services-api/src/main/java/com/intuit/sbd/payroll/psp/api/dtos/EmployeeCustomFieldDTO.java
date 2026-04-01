package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 31, 2011
 * Time: 3:44:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeCustomFieldDTO {
    private int mOrder = -1;
    private String mName;
    private String mValue;

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int pOrder) {
        mOrder = pOrder;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String pValue) {
        mValue = pValue;
    }
}

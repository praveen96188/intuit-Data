package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 2/23/12
 * Time: 2:06 PM
 */
public class SAPTaxRepaymentOptions {
    private String newPaymentMethod;
    private boolean updateAll;
    private boolean recreate;

    public String getNewPaymentMethod() {
        return newPaymentMethod;
    }

    public void setNewPaymentMethod(String newPaymentMethod) {
        this.newPaymentMethod = newPaymentMethod;
    }

    public boolean getUpdateAll() {
        return updateAll;
    }

    public void setUpdateAll(boolean updateAll) {
        this.updateAll = updateAll;
    }

    public boolean getRecreate() {
        return recreate;
    }

    public void setRecreate(boolean recreate) {
        this.recreate = recreate;
    }
}
